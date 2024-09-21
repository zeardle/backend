package com.zulfen.zeardle.utility.song

import com.zulfen.zeardle.entities.Album
import com.zulfen.zeardle.entities.Artist
import com.zulfen.zeardle.entities.Song
import com.zulfen.zeardle.entities.ZeardleUser
import org.jaudiotagger.tag.images.Artwork
import java.net.URI
import java.time.LocalDate

data class UploadedSong(
    var songId: Long? = 0,
    var artworkId: Long? = 0,
    val albumCoverUri: URI?,
    val title: String? = null,
    val artistName: String? = null,
    val albumTitle: String? = null,
    val duration: Int,
    val genre: String? = null,
    val releaseDate: String? = null,
    val songUri: URI,
    val hash: String,
    val intendedDate: LocalDate? = LocalDate.now(),
    val type: SongType,
    val artwork: Artwork
) {
    fun toSong(artist: Artist? = null, album: Album? = null, zeardleUser: ZeardleUser): Song {
        return Song(
            title = title,
            audioUri = songUri,
            artist = artist,
            album = album,
            genre = genre,
            releaseDate = releaseDate,
            hash = hash,
            duration = duration,
            type = type,
            uploadedBy = zeardleUser
        )
    }
}



