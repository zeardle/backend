package com.zulfen.zeardle.utility.song

import com.zulfen.zeardle.utility.song.data.ParsedSongData
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.KeyNotFoundException
import org.jaudiotagger.tag.images.Artwork
import java.nio.file.Path

class FileSongData private constructor(
    private val parsedSongData: ParsedSongData,
    val tempPath: Path,
    val fileArtwork: Artwork? = null
) : SongDataImpl by parsedSongData {

    companion object {
        fun build(audioFile: AudioFile): FileSongData {
            val tempPath = audioFile.file.toPath()
            val duration = audioFile.audioHeader.trackLength
            val artwork: Artwork?
            if (duration > 0) {
                return try {
                    val parsedSongData = ParsedSongData(
                        artist = audioFile.tag.getFirst(FieldKey.ARTIST),
                        album = audioFile.tag.getFirst(FieldKey.ALBUM),
                        title = audioFile.tag.getFirst(FieldKey.TITLE),
                        genre = audioFile.tag.getFirst(FieldKey.GENRE),
                        duration = audioFile.audioHeader.trackLength,
                        type = SongType.UPLOADED
                    )
                    artwork = audioFile.tag.firstArtwork
                    FileSongData(parsedSongData, tempPath, artwork)
                } catch (e: KeyNotFoundException) {
                    FileSongData(ParsedSongData(type = SongType.UPLOADED), tempPath)
                }
            } else {
                throw IllegalArgumentException("Song appears to be empty (0 seconds long)")
            }
        }
    }
}
