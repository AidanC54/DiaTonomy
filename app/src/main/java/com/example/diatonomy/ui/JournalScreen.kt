package com.example.diatonomy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.diatonomy.data.DoseLogEntry
import com.example.diatonomy.data.PenRegistry
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val SyncedGreen = Color(0xFF2E7D32)
private val PendingAmber = Color(0xFFF9A825)
private val WarningAmber = Color(0xFFF9A825)

@Composable
fun JournalScreen(
    entries: List<DoseLogEntry>,
    pens: List<PenRegistry>,
    isConfigured: Boolean,
    onManagePensClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val nicknameBySerial = pens.associateBy({ it.serial }, { it.nickname })

    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    val zone = ZoneId.systemDefault()

    val grouped = entries
        .groupBy { Instant.ofEpochMilli(it.time).atZone(zone).toLocalDate() }
        .toSortedMap(compareByDescending { it })

    Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dose journal",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Row {
                OutlinedButton(onClick = onSettingsClick) {
                    Text("Settings")
                }
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Button(onClick = onManagePensClick) {
                    Text("Manage Pens")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (!isConfigured) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = WarningAmber.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Nightscout not set up",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Scanned doses won't be sent anywhere until you add your Nightscout URL and API secret.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onSettingsClick) {
                        Text("Set up now")
                    }
                }
            }
        }

        if (entries.isEmpty()) {
            Text(
                text = "No doses logged yet — scan a pen to get started.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        LazyColumn {
            grouped.forEach { (date, dayEntries) ->
                val mainBasalEntry = dayEntries
                    .filter { it.doseKind == "BASAL" }
                    .maxByOrNull { it.units }

                val bolusEntries = dayEntries.filter { it.doseKind == "BOLUS" }
                    .sortedByDescending { it.time }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = date.format(dateFormatter),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                mainBasalEntry?.let { basal ->
                                    val time = Instant.ofEpochMilli(basal.time).atZone(zone).toLocalTime()
                                    Text(
                                        text = "Basal ${basal.units / 10.0}U · ${time.format(timeFormatter)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            if (bolusEntries.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            bolusEntries.forEach { entry ->
                                val time = Instant.ofEpochMilli(entry.time).atZone(zone).toLocalTime()
                                val pen = nicknameBySerial[entry.serial] ?: entry.serial
                                val statusColor = if (entry.status == DoseLogEntry.STATUS_SYNCED) SyncedGreen else PendingAmber
                                val statusLabel = if (entry.status == DoseLogEntry.STATUS_SYNCED) "Synced" else "Waiting to sync"

                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(color = statusColor, shape = CircleShape)
                                    )
                                    Text(
                                        text = "${entry.units / 10.0}U",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                    Text(
                                        text = " · ${time.format(timeFormatter)} · $pen",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = statusLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = statusColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}