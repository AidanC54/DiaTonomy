package com.example.diatonomy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PenRegistry(
    @PrimaryKey val serial: String,
    val type: String,       // PenType.name — "BOLUS" or "BASAL"
    val nickname: String
)