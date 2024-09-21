package com.zulfen.zeardle.controllers.util

import com.zulfen.zeardle.utility.song.SongType
import java.beans.PropertyEditorSupport

class SongTypeEditor : PropertyEditorSupport() {
    override fun setAsText(text: String?) {
        value = text?.let { SongType.valueOf(it) }
    }
}