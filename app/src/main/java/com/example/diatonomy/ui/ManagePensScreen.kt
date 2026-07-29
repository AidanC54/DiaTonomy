package com.example.diatonomy.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.diatonomy.data.PenRegistry

@Composable
fun ManagePensScreen(
    pens: List<PenRegistry>,
    onBackClick: () -> Unit,
    onDeletePen: (PenRegistry) -> Unit
) {
    var pendingDelete by remember { mutableStateOf<PenRegistry?>(null) }

    Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp)) {
        OutlinedButton(onClick = onBackClick) {
            Text("Back")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Registered pens",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (pens.isEmpty()) {
            Text(
                text = "No pens registered yet — scan a pen to add one.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        LazyColumn {
            items(pens) { pen ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pen.nickname,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${pen.type.lowercase().replaceFirstChar { it.uppercase() }} · ${pen.serial}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { pendingDelete = pen }) {
                            Text("Remove", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { pen ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Remove ${pen.nickname}?") },
            text = {
                Text("This pen will need to be re-registered the next time it's scanned. Existing journal entries and Nightscout data won't be affected.")
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeletePen(pen)
                    pendingDelete = null
                }) { Text("Remove", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}