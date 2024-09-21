package com.zulfen.zeardle.utility

import com.zulfen.zeardle.entities.AlbumArt
import org.jaudiotagger.tag.images.Artwork
import java.net.URI
import java.security.MessageDigest

@OptIn(ExperimentalStdlibApi::class)
fun Artwork.toAlbumArt() : AlbumArt {
    val messageDigest = MessageDigest.getInstance("SHA-512")
    val digest = messageDigest.digest(this.binaryData)
    val hash = digest.toHexString()
    return AlbumArt(
        imageHash = hash,
        imageUri = URI(this.imageUrl),
    )
}
