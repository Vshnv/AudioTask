package com.audifytech.task.audiotask.model.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.audifytech.task.audiotask.model.data.LikedAudioId

@Dao
interface LikedAudioIdDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(id: LikedAudioId)
    @Delete
    fun remove(id: LikedAudioId)
    @Query("SELECT EXISTS(SELECT * FROM likes WHERE likedId = :id)")
    fun exists(id: Long): LiveData<Boolean>
}