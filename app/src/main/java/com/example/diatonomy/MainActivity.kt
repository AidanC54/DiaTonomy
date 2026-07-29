package com.example.diatonomy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.diatonomy.data.AppDatabase
import com.example.diatonomy.data.DoseLogEntry
import com.example.diatonomy.data.PenRegistry
import com.example.diatonomy.data.PenType
import com.example.diatonomy.data.SettingsPrefs
import com.example.diatonomy.data.SyncPrefs
import com.example.diatonomy.data.deriveId
import com.example.diatonomy.network.NightscoutClient
import com.example.diatonomy.network.NightscoutSyncWorker
import com.example.diatonomy.ui.JournalScreen
import com.example.diatonomy.ui.ManagePensScreen
import com.example.diatonomy.ui.NotConfiguredDialog
import com.example.diatonomy.ui.SettingsScreen
import com.example.diatonomy.ui.UnregisteredPenDialog
import com.example.diatonomy.ui.theme.DiaTonomyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.cacheux.nvplib.data.InsulinDose
import net.cacheux.nvplib.data.PenResult
import net.cacheux.nvplib.data.PenResultData
import net.cacheux.nvplib.nfc.NfcController

private sealed class Screen {
    object Home : Screen()
    object ManagePens : Screen()
    object Settings : Screen()
}

class MainActivity : ComponentActivity() {

    private lateinit var nfcController: NfcController
    private val db by lazy { AppDatabase.getInstance(applicationContext) }
    private val syncPrefs by lazy { SyncPrefs(applicationContext) }
    private val settingsPrefs by lazy { SettingsPrefs(applicationContext) }

    private var pendingScanHandler: ((PenResultData) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        nfcController = NfcController(this) { block ->
            lifecycleScope.launch(Dispatchers.Main) { block() }
        }

        enqueueRetryWorker()

        setContent {
            DiaTonomyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
                    var unregisteredData by remember { mutableStateOf<PenResultData?>(null) }
                    var showNotConfiguredDialog by remember { mutableStateOf(false) }
                    var isConfigured by remember { mutableStateOf(settingsPrefs.isConfigured()) }

                    val twoWeeksAgo = remember { System.currentTimeMillis() - 14L * 24 * 60 * 60 * 1000 }
                    val entries by db.doseLogEntryDao().getRecentFlow(twoWeeksAgo).collectAsState(initial = emptyList())
                    val pens by db.penRegistryDao().getAllFlow().collectAsState(initial = emptyList())

                    LaunchedEffect(Unit) {
                        pendingScanHandler = handler@{ data ->
                            if (!settingsPrefs.isConfigured()) {
                                android.util.Log.e("NvpDebug", "Nightscout not configured — ignoring scan")
                                showNotConfiguredDialog = true
                                return@handler
                            }
                            lifecycleScope.launch(Dispatchers.IO) {
                                val registryEntry = db.penRegistryDao().getBySerial(data.serial)
                                if (registryEntry == null) {
                                    kotlinx.coroutines.withContext(Dispatchers.Main) {
                                        unregisteredData = data
                                    }
                                } else {
                                    val type = PenType.valueOf(registryEntry.type)
                                    processDoseHistory(data, type)
                                }
                            }
                        }
                    }

                    when (screen) {
                        is Screen.Home -> JournalScreen(
                            entries = entries,
                            pens = pens,
                            isConfigured = isConfigured,
                            onManagePensClick = { screen = Screen.ManagePens },
                            onSettingsClick = { screen = Screen.Settings }
                        )
                        is Screen.ManagePens -> ManagePensScreen(
                            pens = pens,
                            onBackClick = { screen = Screen.Home },
                            onDeletePen = { pen ->
                                lifecycleScope.launch(Dispatchers.IO) {
                                    db.penRegistryDao().delete(pen)
                                }
                            }
                        )
                        is Screen.Settings -> SettingsScreen(
                            initialUrl = settingsPrefs.getNightscoutUrl(),
                            initialSecret = settingsPrefs.getApiSecret(),
                            onBackClick = { screen = Screen.Home },
                            onSave = { url, secret ->
                                settingsPrefs.setNightscoutUrl(url)
                                settingsPrefs.setApiSecret(secret)
                                isConfigured = settingsPrefs.isConfigured()
                                screen = Screen.Home
                            }
                        )
                    }

                    unregisteredData?.let { data ->
                        UnregisteredPenDialog(
                            serial = data.serial,
                            onConfirm = { type, nickname ->
                                lifecycleScope.launch(Dispatchers.IO) {
                                    db.penRegistryDao().upsert(PenRegistry(data.serial, type.name, nickname))
                                    processDoseHistory(data, type)
                                }
                                unregisteredData = null
                            },
                            onCancel = { unregisteredData = null }
                        )
                    }

                    if (showNotConfiguredDialog) {
                        NotConfiguredDialog(
                            onGoToSettings = {
                                showNotConfiguredDialog = false
                                screen = Screen.Settings
                            },
                            onDismiss = { showNotConfiguredDialog = false }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        android.util.Log.d("NvpDebug", "Starting NFC monitoring")
        nfcController.monitorNfc(
            onTagDetected = { tag -> android.util.Log.d("NvpDebug", "Tag detected: $tag") },
            onDataRead = { result -> handlePenResult(result) },
            onError = { e -> handleReadError(e) }
        )
    }

    override fun onPause() {
        super.onPause()
        nfcController.stopNfc()
    }

    private fun handlePenResult(result: PenResult) {
        when (result) {
            is PenResult.Success -> {
                val data: PenResultData = result.data
                android.util.Log.d("NvpDebug", "model=${data.model} serial=${data.serial} startTime=${data.startTime} doses=${data.doseList}")
                pendingScanHandler?.invoke(data)
            }
            is PenResult.Failure -> {
                android.util.Log.e("NvpDebug", "Read failed: ${result.message}")
            }
        }
    }

    private fun processDoseHistory(data: PenResultData, penType: PenType) {
        lifecycleScope.launch(Dispatchers.IO) {
            val serial = data.serial

            val validDoses: List<InsulinDose> = data.doseList.filter { dose ->
                (dose.flags and InsulinDose.VALID_FLAG) != 0
            }

            val watermark = syncPrefs.getWatermark(serial)
            val bufferMs = 10 * 60_000L
            val candidates = validDoses.filter { it.time > (watermark - bufferMs) }

            android.util.Log.d("NvpDebug", "[$serial] Total valid: ${validDoses.size}, candidates: ${candidates.size}")

            val alreadyLogged = db.doseLogEntryDao().getAllIds().toSet()
            val newDoses = candidates.filter { it.deriveId(serial) !in alreadyLogged }

            android.util.Log.d("NvpDebug", "[$serial] new: ${newDoses.size}")

            if (newDoses.isEmpty()) return@launch

            val newEntries = newDoses.map { dose ->
                DoseLogEntry(
                    doseId = dose.deriveId(serial),
                    serial = serial,
                    doseKind = penType.name,
                    time = dose.time,
                    units = dose.units,
                    status = DoseLogEntry.STATUS_PENDING,
                    syncedAt = null
                )
            }
            db.doseLogEntryDao().insertAll(newEntries)

            val maxTime = newDoses.maxOf { it.time }
            syncPrefs.setWatermark(serial, maxTime)

            val client = NightscoutClient(settingsPrefs.getNightscoutUrl(), settingsPrefs.getApiSecret())
            val success = client.postTreatments(newDoses, penType.nightscoutEventType, serial)
            android.util.Log.d("NvpDebug", "[$serial] Nightscout POST success: $success")

            if (success) {
                newEntries.forEach { entry ->
                    db.doseLogEntryDao().updateStatus(entry.doseId, DoseLogEntry.STATUS_SYNCED, System.currentTimeMillis())
                }
            } else {
                enqueueRetryWorker()
            }
        }
    }

    private fun enqueueRetryWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<NightscoutSyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueue(request)
    }

    private fun handleReadError(e: Exception) {
        android.util.Log.e("NvpDebug", "NFC error: ${e.message}")
    }
}