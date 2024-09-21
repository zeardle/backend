package com.zulfen.zeardle.utility

import java.net.URI

fun URI.strip() : String {
    val path = this.path
    val segments = path.split("/").filter { segment -> segment.isNotEmpty() }
    return segments.lastOrNull().toString()
}


