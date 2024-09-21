package com.zulfen.zeardle.repositories

import com.zulfen.zeardle.entities.Album
import com.zulfen.zeardle.entities.Artist
import com.zulfen.zeardle.entities.Song
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import java.util.*

interface AlbumRepository : ReactiveCrudRepository<Album, Long> {
    fun findAllByTitle(name: String): Flux<Album>
    fun findAlbumsBy(artist: Artist) : Flux<Album>
}