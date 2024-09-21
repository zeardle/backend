package com.zulfen.zeardle.utility

import java.net.URI
import java.security.MessageDigest
import java.util.*

@OptIn(ExperimentalStdlibApi::class)
fun String.hash() : String {
    val messageDigest = MessageDigest.getInstance("SHA-512")
    val digest = messageDigest.digest(this.toByteArray())
    return digest.toHexString()
}

fun String.toUri(): URI {
    return URI(this)
}

fun String.toSlug(): String {
    return this.lowercase()
        .replace(Regex("[^a-z0-9\\s-]"), "") // Remove special characters
        .replace(Regex("\\s+"), "-") // Replace spaces with hyphens
        .replace(Regex("-+"), "-") // Replace multiple hyphens with a single hyphen
        .trim('-') // Remove leading and trailing hyphens
}

fun String.getSongInfo(): Pair<String, String> {
    val splitSong = this.split('-').map { it.trim() }
    if (splitSong.size == 2) {
        return Pair(splitSong[0], splitSong[1])
    } else {
        throw IllegalArgumentException("Invalid song format: requires one artist and song separated with one dash.")
    }
}