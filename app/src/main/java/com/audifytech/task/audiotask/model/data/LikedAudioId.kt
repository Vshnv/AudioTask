package com.audifytech.task.audiotask.model.data

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "likes")
class LikedAudioId(@ColumnInfo(name = "likedId") @PrimaryKey @NonNull val id: Long) {}