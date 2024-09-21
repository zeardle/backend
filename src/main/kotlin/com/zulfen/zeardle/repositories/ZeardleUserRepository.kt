package com.zulfen.zeardle.repositories

import com.zulfen.zeardle.entities.Song
import com.zulfen.zeardle.entities.ZeardleUser
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.util.*

interface ZeardleUserRepository : ReactiveCrudRepository<ZeardleUser, Long> {
    fun findByDiscordId(discordId: String): Mono<ZeardleUser>
    fun findSongsBy(user: ZeardleUser): Mono<Song>
}