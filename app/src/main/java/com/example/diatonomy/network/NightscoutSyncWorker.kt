package com.example.diatonomy.network

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.diatonomy.data.AppDatabase
import com.example.diatonomy.data.DoseLogEntry
import com.example.diatonomy.data.PenType
import com.example.diatonomy.data.SettingsPrefs
import net.cacheux.nvplib.data.InsulinDose

class NightscoutSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val settings = SettingsPrefs(applicationContext)

        if (!settings.isConfigured()) {
            return Result.success() // nothing we can do until the user configures settings
        }

        val pending = db.doseLogEntryDao().getPending()
        if (pending.isEmpty()) {
            return Result.success()
        }

        val client = NightscoutClient(settings.getNightscoutUrl(), settings.getApiSecret())
        val bySerial = pending.groupBy { it.serial }
        var allSucceeded = true

        bySerial.forEach { (serial, entries) ->
            val registryEntry = db.penRegistryDao().getBySerial(serial)
            val eventType = registryEntry?.let { PenType.valueOf(it.type).nightscoutEventType } ?: "Note"

            val doses = entries.map { InsulinDose(it.time, it.units, 0) }
            val success = client.postTreatments(doses, eventType, serial)

            if (success) {
                entries.forEach { entry ->
                    db.doseLogEntryDao().updateStatus(entry.doseId, DoseLogEntry.STATUS_SYNCED, System.currentTimeMillis())
                }
            } else {
                allSucceeded = false
            }
        }

        return if (allSucceeded) Result.success() else Result.retry()
    }
}