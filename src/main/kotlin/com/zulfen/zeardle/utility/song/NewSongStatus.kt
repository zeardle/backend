package com.zulfen.zeardle.utility.song

import com.fasterxml.jackson.annotation.JsonCreator

enum class NewSongStatus {

    NO_USER,
    VALIDATE,
    COMPLETE;

    companion object {
        @JvmStatic
        @JsonCreator
        fun fromString(status: String): NewSongStatus? {
            return entries.find { entry -> entry.name.equals(status, ignoreCase = true) }
        }
    }

}