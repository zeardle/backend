package com.zulfen.zeardle.utility.song

import com.zulfen.zeardle.entities.*
import com.zulfen.zeardle.utility.song.data.ParsedSongData
import org.springframework.util.StringUtils
import java.net.URI

interface SongDataImpl {

    val artist: String?
    val album: String?
    val title: String?
    val year: String?
    val genre: String?
    val duration: Int
    val type: SongType

    fun createNewSong(
        artist: Artist? = null,
        album: Album? = null,
        albumArt: AlbumArt,
        uri: URI,
        songHash: String,
        cacheAlbumArt: Boolean,
        zeardleUser: ZeardleUser,
    ): Song {
        return Song(
            album = album,
            artist = artist,
            title = title,
            genre = genre,
            audioUri = uri,
            duration = duration,
            hash = songHash,
            type = type,
            albumArt = albumArt,
            uploadedBy = zeardleUser,
            cacheAlbumArt = cacheAlbumArt
        )
    }

    fun isComplete(): Boolean {
        return StringUtils.hasText(artist) && StringUtils.hasText(album) && StringUtils.hasText(title)
    }

}