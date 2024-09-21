package com.zulfen.zeardle.entities

import jakarta.persistence.*
import java.net.URI

@Entity
data class AlbumArt (

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    @Column(nullable = false)
    val imageUri: URI,

    @Column(nullable = false)
    val imageHash: String,

    @OneToMany(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
    val songs: MutableSet<Song> = mutableSetOf()

)

