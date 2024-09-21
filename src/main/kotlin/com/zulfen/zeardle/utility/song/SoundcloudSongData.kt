package com.zulfen.zeardle.utility.song

import com.zulfen.zeardle.utility.song.data.ParsedSongData

data class SoundcloudSongData(
    private val parsedSongData: ParsedSongData,
    val artworkUrl: String?,
    val id: String?
) : SongDataImpl by parsedSongData