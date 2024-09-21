package com.zulfen.zeardle.config

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists

@Configuration
@ConfigurationProperties(prefix = "zeardle")
class ZeardleConfig {

    var dataPath: String = "data"
    var cacheAlbumArt: Boolean = false
    var soundcloud = Soundcloud()
    var defaultArtworkPath: String = "static/images/defaultart.png"
    var tempFilesPath: String = "temp"
    var maxSongLength: Long = 30
    var tempSongLifetime: Long = 30

    @Configuration
    @ConfigurationProperties(prefix = "zeardle.soundcloud")
    class Soundcloud {
        var clientId: String = "iyGXviHE8xjNOJChYIx9xdZ2WKCqCfQm"
        fun getSoundcloudClientId(): String {
            return clientId
        }
    }

    @PostConstruct
    fun ensureFolderStructure() {
        val fullPath = getFullDataPath()
        if (fullPath.exists()) {
            Files.createDirectories(fullPath)
        }
        val artistsFolder = getArtistsFolder()
        if (!artistsFolder.exists()) {
            Files.createDirectories(artistsFolder)
        }
        val temporaryFolder = getTemporaryFolder()
        if (!temporaryFolder.exists()) {
            Files.createDirectories(temporaryFolder)
        }
    }

    fun getFullDataPath(): Path {
        val currentWorkingDirectory = System.getProperty("user.dir")
        return Path.of(currentWorkingDirectory, dataPath)
    }

    fun getArtistsFolder(): Path {
        return getFullDataPath().resolve("artists")
    }

    fun getTemporaryFolder() : Path {
        return getFullDataPath().resolve("temp")
    }

}