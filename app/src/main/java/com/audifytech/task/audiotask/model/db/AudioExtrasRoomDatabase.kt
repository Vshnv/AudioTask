package com.audifytech.task.audiotask.model.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.audifytech.task.audiotask.model.data.LikedAudioId

@Database(entities = [(LikedAudioId::class)], version = 1)
abstract class AudioExtrasRoomDatabase: RoomDatabase() {

    abstract fun likedAudioDao(): LikedAudioIdDao

    companion object {

        private var INSTANCE: AudioExtrasRoomDatabase? = null

        fun getInstance(context: Context): AudioExtrasRoomDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AudioExtrasRoomDatabase::class.java,
                        "audio_extras_database"
                    ).fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}