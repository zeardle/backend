package com.zulfen.zeardle.controllers

import com.zulfen.zeardle.config.SongProcessorConfig
import com.zulfen.zeardle.controllers.util.SongTypeEditor
import com.zulfen.zeardle.dto.SongUploadDTO
import com.zulfen.zeardle.dto.SavedSongDTO
import com.zulfen.zeardle.services.auth.DiscordOAuth2Service
import com.zulfen.zeardle.utility.song.SongType
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.WebDataBinder
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/api/v1/songs")
class SongController (
    private val discordOAuth2Service: DiscordOAuth2Service,
    private val songProcessorConfig: SongProcessorConfig
) {

    @InitBinder
    fun initBinder(binder: WebDataBinder) {
        binder.registerCustomEditor(SongType::class.java, SongTypeEditor())
    }

    @PostMapping("/upload")
    fun uploadSong(
        @Valid @ModelAttribute songUploadDTO: SongUploadDTO,
        @AuthenticationPrincipal principal: OAuth2User
    ): Mono<SavedSongDTO> {
        val discordId = principal.attributes["id"] as String
        return songProcessorConfig
            .handleCreation(songUploadDTO.songType, songUploadDTO.file, songUploadDTO.link, discordId)
    }

    @PostMapping("/edit")
    @PreAuthorize("isAuthenticated()")
    fun editSong(
        @Valid @ModelAttribute songDTO: SavedSongDTO,
        @AuthenticationPrincipal principal: OAuth2User
    ) {

    }

}