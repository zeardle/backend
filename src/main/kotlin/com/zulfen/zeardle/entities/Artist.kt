package com.zulfen.zeardle.entities

import com.zulfen.zeardle.utility.toSlug
import jakarta.persistence.*

@Entity
data class Artist (

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long = 0,

    val fullName: String? = null,
    val slugName: String? = fullName?.toSlug(),

    @OneToMany(mappedBy = "artist", cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    val albums: MutableSet<Album> = mutableSetOf()

)
