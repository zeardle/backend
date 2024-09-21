package com.zulfen.zeardle.repositories

import com.zulfen.zeardle.entities.Artist
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface ArtistRepository : ReactiveCrudRepository<Artist, Long> {
    fun findByFullName(name: String): Mono<Artist>
}