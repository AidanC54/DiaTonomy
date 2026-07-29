package com.example.diatonomy.data

import android.content.Context

class SyncPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("sync_state", Context.MODE_PRIVATE)

    fun getWatermark(serial: String): Long = prefs.getLong("last_synced_dose_time_$serial", 0L)

    fun setWatermark(serial: String, time: Long) {
        if (time > getWatermark(serial)) {
            prefs.edit().putLong("last_synced_dose_time_$serial", time).apply()
        }
    }
}