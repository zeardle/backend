package com.zulfen.zeardle.entities

import jakarta.persistence.*
import java.time.LocalDate

@Entity
// make sure we don't repeat songs. this shouldn't happen but putting it in place.
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["date"])])
data class DailySong(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    var song: Song,

    @Column(nullable = false, name = "date")
    var date: LocalDate = LocalDate.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    var solvedByUser: ZeardleUser? = null

)
