package com.zulfen.zeardle.services.song

import com.zulfen.zeardle.services.BuildableSong
import com.zulfen.zeardle.services.GenericSongService
import com.zulfen.zeardle.utility.getSongInfo
import com.zulfen.zeardle.utility.hash
import com.zulfen.zeardle.utility.song.SongType
import com.zulfen.zeardle.utility.song.SoundcloudSongData
import com.zulfen.zeardle.utility.song.data.ParsedSongData
import kotlinx.serialization.json.*
import org.jaudiotagger.tag.images.Artwork
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URI
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class SoundcloudSongService : GenericSongService() {

    @Autowired
    @Qualifier("soundCloudClient")
    private lateinit var soundCloudClient: WebClient

    private fun getSongDataFromUrl(url: String): Mono<JsonObject> {
        return soundCloudClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("resolve")
                    .queryParam("url", url).queryParam("format", "json")
                    // TODO: nuh uhhh
                    .queryParam("client_id", zeardleConfig.soundcloud.getSoundcloudClientId())
                    .build()
            }
            .retrieve()
            .bodyToMono(String::class.java)
            .mapNotNull { response ->
                Json.parseToJsonElement(response).jsonObject
            }
    }


    private fun getSongData(url: String) : Mono<SoundcloudSongData> {
        return getSongDataFromUrl(url)
            .mapNotNull { json ->
                // try parsing the publisher metadata first
                var artist: String? = null
                var album: String? = null
                var releaseDate: String?
                var title: String?
                val id = json["id"]?.jsonPrimitive?.content
                val albumCoverUrl = json["artwork_url"]?.jsonPrimitive?.content
                val fullDuration = json["full_duration"]?.jsonPrimitive?.int
                val genre = json["genre"]?.jsonPrimitive?.content
                val publisherMetaData = json["publisher_metadata"]?.jsonObject ?: JsonObject(emptyMap())
                releaseDate = publisherMetaData["release_date"]?.jsonPrimitive?.content
                title = json["title"]?.jsonPrimitive?.content
                title?.let {
                    try {
                        val songInfo = it.getSongInfo()
                        album = songInfo.first
                        title = songInfo.second
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
                releaseDate?.let { dateString ->
                    val dateTime = LocalDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME)
                    releaseDate = dateTime.year.toString()
                }
                if (publisherMetaData.isNotEmpty()) {
                    artist = publisherMetaData["artist"]?.jsonPrimitive?.content
                    album = publisherMetaData["album"]?.jsonPrimitive?.content
                }
                fullDuration?.let { duration ->
                    SoundcloudSongData(
                        ParsedSongData(
                            duration = duration,
                            title = title,
                            album = album,
                            type = SongType.SOUNDCLOUD,
                            artist = artist,
                            genre = genre,
                            year = releaseDate,
                        ),
                        artworkUrl = albumCoverUrl,
                        id = id
                    )
                }
                null
            }
    }

    private fun toBuildableSong(url: String): Mono<BuildableSong> {
        return getSongData(url)
            .flatMap { soundCloudSongData ->
                soundCloudSongData.artworkUrl?.let { dataUrl ->
                    processArtwork(dataUrl, soundCloudSongData)
                }
                Mono.empty()
            }
    }

    private fun processArtwork(dataUrl: String, soundCloudSongData: SoundcloudSongData): Mono<BuildableSong> {
        val uri = URI(dataUrl)
        return obtainRemoteArtworkImage(uri)
            .flatMap { artwork ->
                createBuildableSong(soundCloudSongData, uri, artwork)
            }
    }

    private fun createBuildableSong(soundCloudSongData: SoundcloudSongData, uri: URI, artwork: Artwork): Mono<BuildableSong> {
        return BuildableSong.create(
            songData = soundCloudSongData,
            uri = uri,
            songHash = soundCloudSongData
                .hashCode()
                .toString()
                .hash(),
            artwork = artwork
        )
    }

    override fun processNewSong(
        optionalFile: MultipartFile?,
        optionalUrl: String?,
    ): Mono<BuildableSong> {
        optionalUrl?.let { url ->
            return toBuildableSong(url)
        }
        return Mono.empty()
    }

    override fun songType(): SongType {
        return SongType.SOUNDCLOUD
    }

}