package com.zulfen.zeardle.services

import com.zulfen.zeardle.config.SongProcessorConfig
import com.zulfen.zeardle.config.ZeardleConfig
import com.zulfen.zeardle.dto.SavedSongDTO
import com.zulfen.zeardle.entities.*
import com.zulfen.zeardle.repositories.*
import com.zulfen.zeardle.utility.song.SongDataImpl
import com.zulfen.zeardle.utility.song.SongType
import com.zulfen.zeardle.utility.toAlbumArt
import com.zulfen.zeardle.utility.toSlug
import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import kotlinx.coroutines.*
import org.jaudiotagger.tag.images.Artwork
import org.jaudiotagger.tag.images.ArtworkFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.MediaType
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.LocalDate
import java.util.*

class BuildableSong private constructor(
    val songDataMono: Mono<SongDataImpl>,
    val uriMono: Mono<URI>,
    val artworkMono: Mono<Artwork>,
    val songHashMono: Mono<String>
) {
    companion object {
        fun <T : SongDataImpl> create(songDataMono: Mono<T>, uriMono: Mono<URI>, artworkMono: Mono<Artwork>, songHashMono: Mono<String>): Mono<BuildableSong> {
            val buildableSong = BuildableSong(
                songDataMono.cast(SongDataImpl::class.java),
                uriMono,
                artworkMono,
                songHashMono
            )
            return Mono.just(buildableSong)
        }
        fun <T : SongDataImpl> create(songData: T, uri: URI, artwork: Artwork, songHash: String): Mono<BuildableSong> {
            val buildableSong = BuildableSong(
                Mono.just(songData)
                    .cast(SongDataImpl::class.java),
                Mono.just(uri),
                Mono.just(artwork),
                Mono.just(songHash)
            )
            return Mono.just(buildableSong)
        }
    }
}

abstract class GenericSongService : DisposableBean {

    @Autowired
    protected lateinit var songRepository: SongRepository

    @Autowired
    protected lateinit var songProcessorConfig: SongProcessorConfig

    @Autowired
    protected lateinit var zeardleUserRepository: ZeardleUserRepository

    @Autowired
    protected lateinit var albumRepository: AlbumRepository

    @Autowired
    protected lateinit var artistRepository: ArtistRepository

    @Autowired
    protected lateinit var zeardleConfig: ZeardleConfig

    @Autowired
    protected lateinit var albumArtService: AlbumArtService

    private val fileProcessScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val songDeletionScope = CoroutineScope(SupervisorJob())
    private val tempFiles: MutableList<Path> = Collections.synchronizedList(mutableListOf<Path>())

    @Autowired
    private lateinit var dailySongService: DailySongService

    @Autowired
    private lateinit var zeardleUserService: ZeardleUserService

    @Autowired
    private lateinit var artistService: ArtistService

    @Autowired
    private lateinit var albumService: AlbumService

    private fun fileToDataBuffers(file: MultipartFile): Flux<DataBuffer> {
        return inputStreamRead(file.inputStream)
    }

    protected fun prepareDirectories(artist: String, album: String): Mono<Path> {
        return Mono
            .fromCallable {
                val artistsFolder = zeardleConfig.getArtistsFolder()
                val artistPath = artistsFolder.resolve(artist.toSlug())
                val albumPath = artistPath.resolve(album.toSlug())
                if (!Files.exists(artistPath)) {
                    Files.createDirectories(artistPath)
                    scheduleDeletion(artistPath, Duration.ofMinutes(30))
                }
                if (!Files.exists(albumPath)) {
                    Files.createDirectories(albumPath)
                    scheduleDeletion(albumPath, Duration.ofMinutes(30))
                }
                albumPath
            }
            .subscribeOn(Schedulers.boundedElastic())
    }

    @Transactional
    protected abstract fun processNewSong(optionalFile: MultipartFile?, optionalUrl: String?): Mono<BuildableSong>

    @Transactional
    open fun createNewSong(optionalFile: MultipartFile?, optionalUrl: String?, discordId: String, date: LocalDate = LocalDate.now()) : Mono<SavedSongDTO> {
        return processNewSong(optionalFile, optionalUrl)
            .flatMap { buildableSong ->
                buildSong(
                    songDataMono = buildableSong.songDataMono,
                    artworkMono = buildableSong.artworkMono,
                    songHashMono = buildableSong.songHashMono,
                    discordId = discordId,
                    uriMono = buildableSong.uriMono
                )
            }
            .flatMap { savedSong ->
                dailySongService
                    .checkDailySong(savedSong, date)
                    .thenReturn(
                        SavedSongDTO(
                            savedSong.createSongData(),
                            savedSong.id
                        )
                    )
            }

    }

    fun editSong(songDTO: SavedSongDTO) {
        if (songDTO.isComplete()) {
            val artistMono = findArtist(songDTO.artist!!)
            val songMono = songRepository
                .findById(songDTO.id)
                .flatMap { song ->
                    songDTO.optionalArtworkFile.let { artworkFile ->  }
                }
            val albumMono = findAlbum(songDTO.album!!, artistMono)
            songDTO.optionalArtworkFile?.let { file ->
                prepareDirectories(songDTO.artist, songDTO.album)
                    .flatMap { path ->

                    }
            }
            /*Mono.zip(artistMono, albumMono, songMono)
                .flatMap { tuple ->
                    val artist = tuple.t1
                    val album = tuple.t2
                    val foundSong = tuple.t3
                    val newSong = foundSong.copy(
                        artist = artist,
                        album = album,

                    )
                }*/
        }

    }

    protected abstract fun songType(): SongType

    @PostConstruct
    fun register() {
        songProcessorConfig.registerService(songType(), this)
    }

    private fun hashCheckSong(songHashMono: Mono<String>): Mono<Song> {
        return songHashMono
            .flatMap { songHash ->
                songRepository
                    .findByHash(songHash)
            }
    }

    private fun hashCheckArtwork(artworkMono: Mono<Artwork>): Mono<AlbumArt> {
        return artworkMono
            .flatMap { artwork ->
                val albumArt = artwork.toAlbumArt()
                albumArtService
                    .findByImageHash(albumArt.imageHash)
                    .switchIfEmpty(albumArtService.newAlbumArt(albumArt))
            }
    }


    private fun findArtist(songDataMono: Mono<SongDataImpl>): Mono<Artist> {
        return songDataMono
            .flatMap { songData ->
                findArtist(songData.artist ?: "")
            }
    }

    private fun findAlbum(songDataMono: Mono<SongDataImpl>, artistMono: Mono<Artist>): Mono<Album> {
        return songDataMono
            .flatMap { songData ->
                findAlbum(songData.artist ?: "", artistMono)
            }
    }

    protected fun findArtist(artist: String): Mono<Artist> {
        return artistService
            .findByFullName(artist)
            .switchIfEmpty(Mono.defer {
                if (StringUtils.hasText(artist)) {
                    val newArtist = Artist(fullName = artist)
                    artistRepository.save(newArtist)
                } else {
                    Mono.empty()
                }
            })
            .defaultIfEmpty(Artist())
    }

    protected fun findAlbum(album: String, artistMono: Mono<Artist>): Mono<Album> {
        return artistMono
            .flatMap { artist ->
                albumService
                    .findAllByTitle(album)
                    .filter { it.artist?.equals(artist) ?: false }
                    .next()
                    .filter { StringUtils.hasText(album) }
                    .then(Mono.defer {
                        val newAlbum = Album(
                            title = album,
                            artist = artist
                        )
                        artist.albums.add(newAlbum)
                        artistService
                            .newArtist(artist)
                            .then(albumService.newAlbum(newAlbum))
                    })
                    .defaultIfEmpty(Album())
            }
    }


    private fun songToDb(
        artistMono: Mono<Artist>,
        albumMono: Mono<Album>,
        userMono: Mono<ZeardleUser>,
        songDataMono: Mono<SongDataImpl>,
        albumArtMono: Mono<AlbumArt>,
        hashMono: Mono<String>,
        uriMono: Mono<URI>
    ): Mono<Song> {
        return Mono.zip(artistMono, albumMono, userMono, songDataMono, albumArtMono, hashMono, uriMono)
            .flatMap { tuple ->
                val artist = tuple.t1
                val album = tuple.t2
                val user = tuple.t3
                val songData = tuple.t4
                val albumArt = tuple.t5
                val hash = tuple.t6
                val uri = tuple.t7
                val song = songData.createNewSong(
                    artist = artist,
                    album = album,
                    zeardleUser = user,
                    albumArt = albumArt,
                    cacheAlbumArt = zeardleConfig.cacheAlbumArt,
                    songHash = hash,
                    uri = uri
                )
                songRepository.save(song)
                    .flatMap { savedSong ->
                        scheduleDeletion(savedSong.id)
                            .thenReturn(savedSong)
                    }
            }

    }

    private fun buildSong(
        songDataMono: Mono<SongDataImpl>,
        uriMono: Mono<URI>,
        artworkMono: Mono<Artwork>,
        songHashMono: Mono<String>,
        discordId: String,
    ): Mono<Song> {
        val userMono = zeardleUserRepository
            .findByDiscordId(discordId)
            .switchIfEmpty(Mono.error(IllegalArgumentException("User with Discord ID $discordId not found!")))
        val artistMono = findArtist(songDataMono)
        val albumMono = findAlbum(songDataMono, artistMono)
        val albumArtMono = hashCheckArtwork(artworkMono)
        return hashCheckSong(songHashMono)
            .switchIfEmpty(songToDb(artistMono, albumMono, userMono, songDataMono, albumArtMono, songHashMono, uriMono))
    }

    private fun scheduleDeletion(tempAudioFile: Path, duration: Duration) {
        fileProcessScope.launch {
            tempFiles.add(tempAudioFile)
            delay(duration.toMillis())
            deleteTempPath(tempAudioFile)
        }
    }

    private fun scheduleDeletion() {}

    private fun scheduleDeletion(songId: Long): Mono<Void> {
        return Mono
            .delay(Duration.ofMinutes(zeardleConfig.tempSongLifetime))
            .flatMap {
                songRepository.findById(songId)
                    .filter { song -> !song.isComplete() }
                    .flatMap { songRepository.delete(it) }
            }
            .then()
    }

    protected fun newTempPath(optionalName: String? = null, optionalExtension: String? = null): Mono<Path> {
        return Mono
            .fromCallable {
                val tempFile = Files.createTempFile(optionalName, optionalExtension)
                scheduleDeletion(tempAudioFile = tempFile, duration = Duration.ofMinutes(zeardleConfig.tempSongLifetime))
                tempFile
            }
            .publishOn(Schedulers.boundedElastic())
    }

    private fun deleteTempPath(tempAudioFile: Path) {
        Files.deleteIfExists(tempAudioFile)
        tempFiles.remove(tempAudioFile)
    }

    override fun destroy() {
        fileProcessScope.cancel()
        songDeletionScope.cancel()
        synchronized(tempFiles) {
            tempFiles.forEach { tempPath ->
                deleteTempPath(tempPath)
            }
        }
    }

    fun findByArtist(artist: Artist): Flux<Song> {
        return songRepository.findByArtist(artist)
    }

    fun findAllUploadedBy(discordId: String): Flux<Song> {
        return zeardleUserService
            .findByDiscordId(discordId)
            .flatMapMany { user ->
                songRepository.findAllUploadedBy(user)
            }
    }

    fun getAllSongs(): Flux<Song> {
        return songRepository.findAll()
    }

}