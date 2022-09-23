package com.audifytech.task.audiotask.model.data

import android.graphics.Bitmap
import android.media.Image
import android.net.Uri

data class Audio(
    val id: Long,
    val title: String,
    val thumbnail: Bitmap?,
    val artists: List<String>,
    val uri: Uri?
)