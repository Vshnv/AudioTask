package com.audifytech.task.audiotask.model

import com.audifytech.task.audiotask.model.data.LikedAudioId
import com.audifytech.task.audiotask.model.db.LikedAudioIdDao

class RoomLikedAudioDataStore(private val likedAudioIdDao: LikedAudioIdDao): LikedAudioDataStore {
    override fun setLiked(id: Long, state: Boolean) {
        if (state) {
            likedAudioIdDao.insert(LikedAudioId(id))
        } else {
            likedAudioIdDao.remove(LikedAudioId(id))
        }
    }

    override fun isLiked(id: Long) = likedAudioIdDao.exists(id)
}