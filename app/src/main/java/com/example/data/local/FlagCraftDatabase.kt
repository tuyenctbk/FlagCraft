package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [CountryEntity::class, QuizHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FlagCraftDatabase : RoomDatabase() {
    abstract fun countryDao(): CountryDao
    abstract fun quizHistoryDao(): QuizHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: FlagCraftDatabase? = null

        fun getDatabase(context: Context): FlagCraftDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FlagCraftDatabase::class.java,
                    "flagcraft_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
