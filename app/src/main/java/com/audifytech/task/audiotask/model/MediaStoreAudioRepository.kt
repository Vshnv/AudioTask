package com.audifytech.task.audiotask.model

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.audifytech.task.audiotask.model.data.Audio

class MediaStoreAudioRepository: AudioRepository {
    override fun fetchAudios(ctx: Context): List<Audio> {
        ctx.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media._ID),
            null,
            null,
            null
        ).use { cursor ->
            if (cursor == null) {
                return emptyList()
            }
            val idIdx = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val titleIdx = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistIdx = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            return buildList {
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idIdx)
                    val title = cursor.getString(titleIdx)
                    val artist = cursor.getString(artistIdx)
                    val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
                    add(Audio(id, title, null, listOf(artist), uri))
                }
            }
        }
    }
}