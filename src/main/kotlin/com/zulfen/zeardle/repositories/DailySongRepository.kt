package com.zulfen.zeardle.repositories

import com.zulfen.zeardle.entities.Artist
import com.zulfen.zeardle.entities.DailySong
import com.zulfen.zeardle.entities.Song
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

interface DailySongRepository : ReactiveCrudRepository<DailySong, Long> {
    fun findByDateAndSongArtist(date: LocalDate, artist: Artist) : Mono<DailySong>
    fun findAllBySong(song: Song): Flux<DailySong>
    fun findAllBySongArtist(artist: Artist): Flux<DailySong>
}