package com.zulfen.zeardle.services

import com.zulfen.zeardle.entities.Artist
import com.zulfen.zeardle.entities.DailySong
import com.zulfen.zeardle.entities.Song
import com.zulfen.zeardle.repositories.DailySongRepository
import jakarta.transaction.Transactional
import kotlinx.coroutines.reactive.collect
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.LocalDate

@Service
class DailySongService @Autowired constructor(
    val dailySongRepository: DailySongRepository
) {


    fun getAllDailySongs(): Flux<DailySong> {
        return dailySongRepository.findAll()
    }

    /**
     * Repeats the same daily songs again.
     */
    @Transactional
    fun findSongByDate(date: LocalDate = LocalDate.now(), artist: Artist): Mono<DailySong> {
        return dailySongRepository
            .findByDateAndSongArtist(date, artist)
            .switchIfEmpty(
                adjustStartDate(date, artist)
                    .next()
            )
    }

    @Transactional
    fun checkDailySong(song: Song, date: LocalDate): Mono<Void> {
        return dailySongRepository
            .findAllBySong(song)
            .map { dailySong ->
                dailySong.song = song
                dailySong
            }
            .defaultIfEmpty(DailySong(song = song, date = date))
            .flatMap { dailySong ->
                dailySongRepository.save(dailySong)
            }
            .then()
    }

    @Transactional
    fun shuffleDailySongs(artist: Artist): Flux<DailySong> {
        return dailySongRepository
            .findAllBySongArtist(artist)
            .collectList()
            .flatMapMany { dailySongs ->
                Flux.fromIterable(dailySongs.shuffled())
            }
            .flatMap { dailySong ->
                dailySongRepository.save(dailySong)
            }
    }

    @Transactional
    fun adjustStartDate(startDate: LocalDate = LocalDate.now(), artist: Artist): Flux<DailySong> {
        return dailySongRepository
            .findAllBySongArtist(artist)
            .index()
            .map { tuple ->
                val index = tuple.t1
                val dailySong = tuple.t2
                dailySong.date = startDate.plusDays(index)
                dailySong
            }
            .collectList()
            .flatMapMany { dailySong ->
                dailySongRepository.saveAll(dailySong)
            }
    }

}