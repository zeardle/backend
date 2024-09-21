package com.zulfen.zeardle.entities

import com.zulfen.zeardle.utility.song.SongDataImpl
import com.zulfen.zeardle.utility.song.SongType
import com.zulfen.zeardle.utility.song.data.ParsedSongData
import jakarta.persistence.*
import java.net.URI

@Entity
data class Song (

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @Column(nullable = true)
    var title: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(nullable = true)
    @JoinColumn(name = "artist_song")
    var artist: Artist? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(nullable = true)
    @JoinColumn(name = "album_song")
    var album: Album ? = null,

    @Column(nullable = false)
    val duration: Int, // seconds

    @Column(nullable = false)
    val hash: String,

    @Column(nullable = false)
    var audioUri: URI,

    @Column(nullable = false)
    var type: SongType,

    @Column(nullable = true)
    var releaseDate: String? = null,

    @Column(nullable = true)
    var genre: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    val uploadedBy: ZeardleUser,

    @Column(nullable = true)
    @ManyToOne(fetch = FetchType.EAGER)
    var albumArt: AlbumArt? = null,

    @Column(nullable = false)
    var cacheAlbumArt: Boolean,

) {
    fun isComplete(): Boolean {
        return artist != null && album != null && title != null
    }
    fun createSongData(): ParsedSongData {
        return ParsedSongData(
            artist = artist?.fullName,
            album = album?.title,
            title = title,
            duration = duration,
            type = type,
        )
    }
}
