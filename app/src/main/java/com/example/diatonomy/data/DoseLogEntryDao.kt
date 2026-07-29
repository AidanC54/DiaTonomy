package com.example.diatonomy.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseLogEntryDao {
    @Query("SELECT doseId FROM DoseLogEntry")
    suspend fun getAllIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entries: List<DoseLogEntry>)

    @Query("UPDATE DoseLogEntry SET status = :status, syncedAt = :syncedAt WHERE doseId = :doseId")
    suspend fun updateStatus(doseId: String, status: String, syncedAt: Long?)

    @Query("SELECT * FROM DoseLogEntry WHERE status = 'PENDING'")
    suspend fun getPending(): List<DoseLogEntry>

    @Query("SELECT * FROM DoseLogEntry WHERE time > :sinceMillis ORDER BY time DESC")
    fun getRecentFlow(sinceMillis: Long): Flow<List<DoseLogEntry>>
}