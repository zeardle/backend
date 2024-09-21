package com.zulfen.zeardle.utility.artwork

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.zulfen.zeardle.utility.strip
import com.zulfen.zeardle.utility.toUri
import org.jaudiotagger.tag.images.Artwork

class ArtworkSerializer : JsonSerializer<Artwork>() {

    override fun serialize(value: Artwork?, gen: JsonGenerator, serializers: SerializerProvider) {
        value?.let { artwork ->
            if (artwork.isLinked) {
                gen.writeString(
                    artwork.imageUrl
                        .toUri()
                        .strip()
                )
            } else {
                gen.writeString("")
            }
        } ?: gen.writeString("")
    }

}