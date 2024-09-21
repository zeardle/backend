package com.zulfen.zeardle.repositories

import com.zulfen.zeardle.entities.Artist
import com.zulfen.zeardle.entities.Song
import com.zulfen.zeardle.entities.ZeardleUser
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface SongRepository : ReactiveCrudRepository<Song, Int> {
    fun findByTitle(name: String): Mono<Song>
    fun findByArtist(artist: Artist): Flux<Song>
    fun findAllUploadedBy(user: ZeardleUser): Flux<Song>
    fun findByHash(hash: String): Mono<Song>
    fun findById(id: Long): Mono<Song>
}