package com.zulfen.zeardle.services.remote


import kotlinx.serialization.json.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.beans.factory.annotation.Qualifier
import reactor.core.publisher.Mono
import java.net.URI

@Service
class MusicbrainzService @Autowired constructor(
    @Qualifier("musicBrainzClient") private val musicbrainzClient: WebClient,
    @Qualifier("coverArtArchiveClient") private val coverArtArchiveClient: WebClient
) {

    private fun getReleaseGroupId(album: String, artist: String): Mono<String> {
        return musicbrainzClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("release-group")
                    .queryParam("query", "release:$album artist:$artist")
                    .queryParam("fmt", "json")
                    .build()
            }
            .retrieve()
            .bodyToMono(String::class.java)
            .mapNotNull { response ->
                val json = Json.parseToJsonElement(response).jsonObject
                val releaseGroups = json["release-groups"]?.jsonArray ?: JsonArray(emptyList())
                if (releaseGroups.isNotEmpty()) {
                    val firstReleaseGroup = releaseGroups[0].jsonObject
                    firstReleaseGroup["id"]?.jsonPrimitive?.content
                } else {
                    null
                }
            }

    }

    private fun getImageUri(releaseGroupId: String): Mono<URI> {
        return coverArtArchiveClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("release-group")
                    .path("/$releaseGroupId")
                    .build()
            }
            .retrieve()
            .bodyToMono(String::class.java)
            .mapNotNull { response ->
                val json = Json.parseToJsonElement(response).jsonObject
                val images = json["images"]?.jsonArray ?: JsonArray(emptyList())
                if (images.isNotEmpty()) {
                    val firstImage = images[0].jsonObject
                    val thumbnails = firstImage["thumbnails"]?.jsonObject
                    if (thumbnails != null) {
                        val imageUrl = thumbnails["small"]?.jsonPrimitive?.content
                        if (imageUrl != null) {
                            URI(imageUrl)
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
    }


    fun getImageLink(album: String, artist: String): Mono<URI> {
        return getReleaseGroupId(album, artist)
            .flatMap { id ->
                getImageUri(id)
            }
    }


}