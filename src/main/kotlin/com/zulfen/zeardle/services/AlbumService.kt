package com.zulfen.zeardle.services

import com.zulfen.zeardle.entities.Album
import com.zulfen.zeardle.entities.Artist
import com.zulfen.zeardle.repositories.AlbumRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class AlbumService @Autowired constructor(private val albumRepository: AlbumRepository) {

    fun findAllByTitle(title: String): Flux<Album> {
        return albumRepository.findAllByTitle(title)
    }

    fun findAlbumsBy(artist: Artist) : Flux<Album> {
        return albumRepository.findAlbumsBy(artist)
    }

    fun newAlbum(album: Album): Mono<Album> {
        return albumRepository.save(album)
    }

}