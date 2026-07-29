package com.example.diatonomy.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.example.diatonomy.data.PenType

@Composable
fun UnregisteredPenDialog(
    serial: String,
    onConfirm: (PenType, String) -> Unit,
    onCancel: () -> Unit
) {
    var nickname by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("Unrecognized pen") },
        text = {
            Column {
                Text("Serial: $serial")
                Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                Text("This pen hasn't been registered yet. What type is it?")
                Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text("Nickname (optional)") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val name = nickname.ifBlank { "Bolus pen" }
                onConfirm(PenType.BOLUS, name)
            }) { Text("Bolus") }
        },
        dismissButton = {
            Column {
                Button(onClick = {
                    val name = nickname.ifBlank { "Basal pen" }
                    onConfirm(PenType.BASAL, name)
                }) { Text("Basal") }
                TextButton(onClick = onCancel) { Text("Cancel") }
            }
        }
    )
}