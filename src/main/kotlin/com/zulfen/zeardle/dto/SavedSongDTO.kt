package com.zulfen.zeardle.dto

import com.zulfen.zeardle.utility.song.SongDataImpl
import com.zulfen.zeardle.utility.song.data.ParsedSongData
import org.springframework.web.multipart.MultipartFile

data class SavedSongDTO(
    val parsedSongData: ParsedSongData,
    val id: Long,
    val optionalArtworkFile: MultipartFile? = null
) : SongDataImpl by parsedSongData