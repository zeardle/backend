package com.zulfen.zeardle.dto

import com.zulfen.zeardle.utility.song.SongType
import org.springframework.web.multipart.MultipartFile

data class SongUploadDTO(val songType: SongType, val link: String?, val file: MultipartFile?)
