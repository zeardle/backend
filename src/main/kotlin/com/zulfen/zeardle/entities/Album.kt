package com.zulfen.zeardle.entities

import jakarta.persistence.*
import java.net.URI

@Entity
data class Album(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    val title: String? = null,

    @ManyToOne
    val artist: Artist? = null

)
