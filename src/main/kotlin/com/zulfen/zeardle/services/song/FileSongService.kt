package com.zulfen.zeardle.services.song

import com.zulfen.zeardle.services.BuildableSong
import com.zulfen.zeardle.services.GenericSongService
import com.zulfen.zeardle.services.remote.MusicbrainzService
import com.zulfen.zeardle.utility.hash
import com.zulfen.zeardle.utility.song.FileSongData
import com.zulfen.zeardle.utility.song.SongType
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.images.Artwork
import org.jaudiotagger.tag.images.ArtworkFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.math.roundToInt

@Service
class FileSongService : GenericSongService(), DisposableBean {

    @Autowired
    private lateinit var musicbrainzService: MusicbrainzService

    private fun readAudioFile(file: MultipartFile): Mono<AudioFile> {
        return newTempPath()
            .publishOn(Schedulers.boundedElastic())
            .map { tempAudioFile ->
                Files.copy(file.inputStream, tempAudioFile)
                AudioFileIO.read(tempAudioFile.toFile())
            }
    }

    private fun parseSong(audioFileMono: Mono<AudioFile>): Mono<FileSongData> {
        return audioFileMono.map { audioFile ->
            FileSongData.build(audioFile)
        }
    }

    private fun getImageLink(songDataMono: Mono<FileSongData>): Mono<URI> {
        return songDataMono.flatMap { songData ->
            if (songData.fileArtwork != null) {
                Mono.just(songData.tempPath.toUri())
            } else if (songData.isComplete()) {
                musicbrainzService.getImageLink(songData.album!!, songData.artist!!)
            } else {
                Mono.empty()
            }
        }
    }

    private fun retrieveArtwork(
        imageLinkMono: Mono<URI>,
        songDataMono: Mono<FileSongData>,
    ): Mono<Artwork> {
        return Mono
            .zip(imageLinkMono, songDataMono)
            .flatMap { tuple ->
                val uri = tuple.t1
                val songData = tuple.t2
                if (uri.scheme == "file") {
                    Mono.justOrEmpty(songData.fileArtwork)
                } else {
                    if (zeardleConfig.cacheAlbumArt) {
                        obtainRemoteArtworkImage(uri)
                    } else {
                        Mono.just(ArtworkFactory.createLinkedArtworkFromURL(uri.toString()))
                    }
                }
            }
            .switchIfEmpty(defaultArtwork())
    }

    private fun getSongPath(
        file: MultipartFile,
        parsedSongMono: Mono<FileSongData>,
        downloadArtMono: Mono<Artwork>,
    ): Mono<Path> {
        return Mono.zip(parsedSongMono, downloadArtMono)
            .flatMap { tuple ->
                val songData = tuple.t1
                val artwork = tuple.t2
                if (songData.isComplete()) {
                    getSongPath(
                        artist = songData.artist!!, album = songData.album!!, artwork = artwork,
                        cacheAlbumArt = zeardleConfig.cacheAlbumArt, sentFile = file,
                    )
                } else {
                    newTempPath(UUID.randomUUID().toString(), checkFileType(file))
                }
            }
    }

    private fun checkFileType(sentFile: MultipartFile): String {
        return when (val mimeType = sentFile.contentType) {
            "audio/mpeg" -> "mp3"
            "audio/wav" -> "wav"
            "audio/ogg" -> "ogg"
            "audio/flac" -> "flac"
            else -> throw IllegalArgumentException("Unsupported song file MIME type: $mimeType")
        }
    }

    private fun hashTranscodedAudio(audioPathMono: Mono<Path>): Mono<String> {
        return audioPathMono
            .map { path ->
                path.hash()
            }
    }

    private fun transcodeAudio(audioPath: Mono<Path>, file: MultipartFile): Mono<Void> {
        return audioPath
            .map { path ->
                avutil.av_log_set_level(avutil.AV_LOG_ERROR)
                val fileOutput = Files.newOutputStream(path)
                FFmpegFrameGrabber(file.inputStream).use { grabber ->
                    grabber.start()
                    FFmpegFrameRecorder(fileOutput, grabber.audioChannels).use { recorder ->
                        val configFrames = grabber.audioFrameRate * zeardleConfig.maxSongLength
                        val actualFrames = grabber.lengthInAudioFrames
                        val targetFrames = actualFrames.coerceAtLeast(configFrames.roundToInt())
                        recorder.format = "mp3"
                        recorder.sampleRate = grabber.sampleRate
                        recorder.audioBitrate = 120 * 1000  // 120kbps
                        recorder.audioCodec = avcodec.AV_CODEC_ID_MP3
                        recorder.start()
                        var currentFrame = 0
                        while (currentFrame < targetFrames) {
                            val frame = grabber.grab()
                            if (frame?.samples == null) break
                            recorder.record(frame)
                            currentFrame++
                        }
                        recorder.stop()
                    }
                    grabber.stop()
                }
            }
            .subscribeOn(Schedulers.boundedElastic())
            .then()
    }

    private fun getSongPath(
        artist: String, album: String, artwork: Artwork, cacheAlbumArt: Boolean,
        sentFile: MultipartFile,
    ): Mono<Path> {
        return prepareDirectories(artist, album)
            .publishOn(Schedulers.boundedElastic())
            .flatMap { albumPath ->
                val songFileExtension = checkFileType(sentFile)
                val songPath = albumPath.resolve("${UUID.randomUUID()}.$songFileExtension")
                val artworkFileExtension = checkArtwork(artwork)
                val albumArtPath = albumPath.resolve("${UUID.randomUUID()}.$artworkFileExtension")
                val dataBuffer = pathToDataBuffer(albumArtPath)
                dataBufferToArtwork(dataBuffer = dataBuffer, optionalPath = albumArtPath)
                    .thenReturn(songPath)
            }
    }

    override fun processNewSong(
        optionalFile: MultipartFile?,
        optionalUrl: String?,
    ): Mono<BuildableSong> {
        optionalFile?.let { file ->
            val readAsAudioMono = readAudioFile(file)
            val parsedSongMono = parseSong(readAsAudioMono)
            val imageLinkMono = getImageLink(parsedSongMono)
            val downloadArtMono = retrieveArtwork(imageLinkMono, parsedSongMono)
            val songPath = getSongPath(file, parsedSongMono, downloadArtMono)
            val hashedAudio = transcodeAudio(songPath, file)
                .then(hashTranscodedAudio(songPath))
            val uriMono = songPath
                .map {
                    path -> path.toUri()
                }
            return BuildableSong.create(
                songHashMono = hashedAudio,
                uriMono = uriMono,
                artworkMono = downloadArtMono,
                songDataMono = parsedSongMono
            )
        } ?: return Mono.empty()
    }

    override fun songType(): SongType {
        return SongType.UPLOADED
    }

}