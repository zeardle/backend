package com.zulfen.zeardle.utility.song.data

import com.zulfen.zeardle.utility.song.SongDataImpl
import com.zulfen.zeardle.utility.song.SongType

/**
* Represents a parsed song. Things like the hash, uri or artwork that does not get included are
* handled separately as these can change once they are first read (see FileSongService).
 * This should also match what is on the front end.
*/
data class ParsedSongData (
    override val artist: String? = null,
    override val album: String? = null,
    override val title: String? = null,
    override val genre: String? = null,
    override val duration: Int = 0,
    override val type: SongType,
    override val year: String? = null
) : SongDataImpl