package com.zulfen.zeardle.config

import com.zulfen.zeardle.dto.SavedSongDTO
import com.zulfen.zeardle.services.GenericSongService
import com.zulfen.zeardle.services.song.FileSongService
import com.zulfen.zeardle.services.song.SoundcloudSongService
import com.zulfen.zeardle.utility.song.SongType
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Mono

@Configuration
class SongProcessorConfig {

    private val typeServiceMap = mutableMapOf<SongType, GenericSongService>()

    fun registerService(songType: SongType, service: GenericSongService) {
        if (!typeServiceMap.containsKey(songType)) {
            typeServiceMap[songType] = service
        } else {

        }
    }

    @PostConstruct
    fun init() {
        typeServiceMap[SongType.UPLOADED] = FileSongService()
        typeServiceMap[SongType.SOUNDCLOUD] = SoundcloudSongService()
    }

    fun handleCreation(
        songType: SongType,
        optionalFile: MultipartFile?,
        optionalUrl: String?,
        discordId: String,
    ): Mono<SavedSongDTO> {
        typeServiceMap[songType]?.let { songService ->
            return songService.createNewSong(optionalFile, optionalUrl, discordId)
        }
        return Mono.empty()
    }

    fun handleEdit(songDTO: SavedSongDTO) {

    }

}