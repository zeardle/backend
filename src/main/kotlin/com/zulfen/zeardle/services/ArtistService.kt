package com.zulfen.zeardle.services

import com.zulfen.zeardle.entities.Artist
import com.zulfen.zeardle.repositories.ArtistRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class ArtistService @Autowired constructor(val artistRepository: ArtistRepository) {

    fun getAllArtists(): Flux<Artist> {
        return artistRepository.findAll()
    }

    fun findByFullName(fullName: String): Mono<Artist> {
        return artistRepository.findByFullName(fullName)
    }

    fun newArtist(artist: Artist): Mono<Artist> {
        return artistRepository.save(artist)
    }

}