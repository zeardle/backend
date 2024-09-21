package com.zulfen.zeardle.services

import com.zulfen.zeardle.config.ZeardleConfig
import com.zulfen.zeardle.entities.AlbumArt
import com.zulfen.zeardle.repositories.AlbumArtRepository
import com.zulfen.zeardle.utility.toAlbumArt
import org.jaudiotagger.tag.images.Artwork
import org.jaudiotagger.tag.images.ArtworkFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

@Service
class AlbumArtService @Autowired constructor(
    val albumArtRepository: AlbumArtRepository,
    val zeardleConfig: ZeardleConfig,
    val resourceLoader: ResourceLoader
) {

    @OptIn(ExperimentalStdlibApi::class)
    fun artworkToAlbumArt(artwork: Artwork): AlbumArt {
        val messageDigest = MessageDigest.getInstance("SHA-512")
        val digest = messageDigest.digest(artwork.binaryData)
        val hash = digest.toHexString()
        return AlbumArt(
            imageHash = hash,
            imageUri = URI(artwork.imageUrl),
        )
    }

    protected fun obtainRemoteArtworkImage(imageUrl: URI, path: Path): Mono<Artwork> {
        val dataBuffer = WebClient.builder()
            .baseUrl(imageUrl.toString())
            .build()
            .get()
            .accept(MediaType.IMAGE_JPEG, MediaType.IMAGE_PNG)
            .retrieve()
            .bodyToFlux(DataBuffer::class.java)
        return dataBufferToArtwork(imageUri = imageUrl, dataBuffer = dataBuffer, path = path)
    }

    private fun inputStreamRead(inputStream: InputStream): Flux<DataBuffer> {
        return DataBufferUtils.readInputStream({ inputStream }, DefaultDataBufferFactory(), 8192)
    }

    protected fun pathToDataBuffer(path: Path): Flux<DataBuffer> {
        return Mono
            .fromCallable {
                Files.newInputStream(path)
            }
            .publishOn(Schedulers.boundedElastic())
            .flatMapMany { inputStream ->
                inputStreamRead(inputStream)
            }
    }

    protected fun dataBufferToArtwork(
        dataBuffer: Flux<DataBuffer>,
        imageUri: URI? = null,
        path: Path
    ): Mono<Artwork> {
        return Mono
            .justOrEmpty(path)
            .filter { zeardleConfig.cacheAlbumArt }
            .flatMap { tempPath ->
                DataBufferUtils.write(dataBuffer, tempPath)
                    .then(Mono.fromCallable {
                        val artwork = ArtworkFactory.createArtworkFromFile(tempPath.toFile())
                        artwork.imageUrl = tempPath
                            .toUri()
                            .toString()
                        artwork
                    })
            }
            .switchIfEmpty(
                Mono.justOrEmpty(imageUri)
                    .map { uri ->
                        ArtworkFactory.createLinkedArtworkFromURL(uri.toString())
                    }
            )
            .switchIfEmpty(defaultArtwork())
    }

    protected fun checkArtwork(artwork: Artwork): String {
        return when (val mimeType = artwork.mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            else -> throw IllegalArgumentException("Unsupported album art MIME type: $mimeType")
        }
    }

    protected fun defaultArtwork(): Mono<Artwork> {
        return Mono.fromCallable {
            val defaultArtwork = resourceLoader.getResource(zeardleConfig.defaultArtworkPath).file
            val artwork = ArtworkFactory.createArtworkFromFile(defaultArtwork)
            artwork.imageUrl = defaultArtwork
                .toURI()
                .toString()
            artwork
        }
    }

    fun findByImageHash(hash: String): Mono<AlbumArt> {
        return albumArtRepository.findByImageHash(hash)
    }

    fun newAlbumArt(albumArt: AlbumArt): Mono<AlbumArt> {
        return albumArtRepository.save(albumArt)
    }

    fun updateAlbumArt(image: MultipartFile, desiredPath: Path, albumArt: AlbumArt): Mono<Void> {
        val dataBuffers = inputStreamRead(image.inputStream)
        return dataBufferToArtwork(dataBuffer = dataBuffers, path = desiredPath)
            .flatMap { artwork ->
                val newArt = artwork.toAlbumArt()
                val toSave = albumArt.copy(
                    imageUri = newArt.imageUri,
                    imageHash = newArt.imageHash
                )
                albumArtRepository.save(toSave)
            }
            .then()
    }

}