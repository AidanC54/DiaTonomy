package com.example.diatonomy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DoseLogEntry(
    @PrimaryKey val doseId: String,
    val serial: String,
    val doseKind: String,   // PenType.name — "BOLUS" or "BASAL"
    val time: Long,
    val units: Int,
    val status: String,     // STATUS_PENDING or STATUS_SYNCED
    val syncedAt: Long?
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_SYNCED = "SYNCED"
    }
}