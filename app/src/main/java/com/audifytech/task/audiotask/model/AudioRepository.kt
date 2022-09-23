package com.audifytech.task.audiotask.model

import android.content.Context
import com.audifytech.task.audiotask.model.data.Audio

interface AudioRepository {
    fun fetchAudios(ctx: Context): List<Audio>
}