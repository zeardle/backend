package com.zulfen.zeardle.services

import com.zulfen.zeardle.entities.Role
import com.zulfen.zeardle.entities.Song
import com.zulfen.zeardle.entities.ZeardleUser
import com.zulfen.zeardle.repositories.RoleRepository
import com.zulfen.zeardle.repositories.ZeardleUserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class ZeardleUserService @Autowired constructor(
    val zeardleUserRepository: ZeardleUserRepository,
) {

    fun updateRoles(discordId: String, roles: Set<Role>): Mono<Void> {
        return zeardleUserRepository
            .findByDiscordId(discordId)
            .map { user ->
                user.roles = roles
            }
            .then()
    }

    fun addRole(zeardleUser: ZeardleUser, role: String): Mono<ZeardleUser> {
        zeardleUser.roles.
    }

    fun findAllUploadedBy(discordId: String): Flux<Song> {
        return findByDiscordId(discordId)
            .flatMapMany { user ->
                zeardleUserRepository.findSongsBy(user)
            }
    }

    fun findByDiscordId(discordId: String): Mono<ZeardleUser> {
        return zeardleUserRepository.findByDiscordId(discordId)
    }

    fun getAllUsers() : Flux<ZeardleUser> {
        return zeardleUserRepository.findAll()
    }

}