package com.example.diatonomy.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PenRegistryDao {
    @Query("SELECT * FROM PenRegistry")
    fun getAllFlow(): Flow<List<PenRegistry>>

    @Query("SELECT * FROM PenRegistry WHERE serial = :serial LIMIT 1")
    suspend fun getBySerial(serial: String): PenRegistry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(pen: PenRegistry)

    @Delete
    suspend fun delete(pen: PenRegistry)
}