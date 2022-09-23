package com.audifytech.task.audiotask.model

import androidx.lifecycle.LiveData

interface LikedAudioDataStore {
    fun setLiked(id: Long, state: Boolean)
    fun isLiked(id: Long): LiveData<Boolean>
}