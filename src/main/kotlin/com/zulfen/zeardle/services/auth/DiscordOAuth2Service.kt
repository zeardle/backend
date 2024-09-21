package com.zulfen.zeardle.services.auth

import com.zulfen.zeardle.entities.ZeardleUser
import com.zulfen.zeardle.repositories.ZeardleUserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class DiscordOAuth2Service(val zeardleUserRepository: ZeardleUserRepository) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest?): DefaultOAuth2User? {

        val oAuth2User = super.loadUser(userRequest)
        val discordUserAttributes = oAuth2User.attributes

        val discordId = discordUserAttributes["id"] as String
        val username = discordUserAttributes["username"] as String

        return zeardleUserRepository
            .findByDiscordId(discordId)
            .defaultIfEmpty(ZeardleUser(discordId = discordId, username = username))
            .flatMap { user ->
                val toSave = user.copy(
                    username = username,
                    roles = user.roles
                )
                zeardleUserRepository.save(toSave)
            }
            .map { user ->
                DefaultOAuth2User(
                    user.roles.map {
                        SimpleGrantedAuthority(user.username)
                    },
                    discordUserAttributes,
                    "id"
                )
            }
            .block()

    }

}