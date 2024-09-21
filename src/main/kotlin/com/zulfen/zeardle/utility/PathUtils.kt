package com.zulfen.zeardle.utility

import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

@OptIn(ExperimentalStdlibApi::class)
fun Path.hash(): String {
    val messageDigest = MessageDigest.getInstance("SHA-512")
    val digest = messageDigest.digest(Files.readAllBytes(this))
    return digest.toHexString()
}