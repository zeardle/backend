package com.zulfen.zeardle.entities

import jakarta.persistence.*

@Entity
data class ZeardleUser(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id")
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    var discordId: String,

    var username: String,

    val score: Long = 0,

    //@Column(nullable = false)
    //var avatar: String,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles: Set<Role> = mutableSetOf(Role(name = "USER")),

    @OneToMany(mappedBy = "uploadedBy", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val songs: List<Song> = mutableListOf(),

    @ManyToOne(fetch = FetchType.LAZY)
    val lastSolvedSong: DailySong? = null

)