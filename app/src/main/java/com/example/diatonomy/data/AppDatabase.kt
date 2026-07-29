package com.example.diatonomy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DoseLogEntry::class, PenRegistry::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun doseLogEntryDao(): DoseLogEntryDao
    abstract fun penRegistryDao(): PenRegistryDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "diatonomy-db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build().also { INSTANCE = it }
            }
        }
    }
}