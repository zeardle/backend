package com.zulfen.zeardle.repositories

import com.zulfen.zeardle.entities.AlbumArt
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface AlbumArtRepository : ReactiveCrudRepository<AlbumArt, Long> {
    fun findByImageHash(hash: String): Mono<AlbumArt>
}