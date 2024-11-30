package com.example.notihub.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [KNUAnnouncementEntity::class, UserPreferenceEntity::class], version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun knuAnnouncementDao(): KNUAnnouncementDao
    abstract fun userPreferenceDao(): UserPreferenceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, "notihub_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}