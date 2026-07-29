package com.example.diatonomy.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun NotConfiguredDialog(
    onGoToSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nightscout not set up") },
        text = {
            Text("Add your Nightscout URL and API secret in Settings before scanning a pen — otherwise doses can't be sent anywhere.")
        },
        confirmButton = {
            Button(onClick = onGoToSettings) { Text("Go to Settings") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    )
}