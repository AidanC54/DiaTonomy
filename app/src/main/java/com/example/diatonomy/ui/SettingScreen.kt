package com.example.diatonomy.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    initialUrl: String,
    initialSecret: String,
    onBackClick: () -> Unit,
    onSave: (url: String, secret: String) -> Unit
) {
    var url by remember { mutableStateOf(initialUrl) }
    var secret by remember { mutableStateOf(initialSecret) }

    Column(modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(16.dp)) {
        OutlinedButton(onClick = onBackClick) {
            Text("Back")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Nightscout settings",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            label = { Text("Nightscout URL") },
            placeholder = { Text("http://192.168.1.x:1337") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = secret,
            onValueChange = { secret = it },
            label = { Text("API secret") },
            placeholder = { Text("Your plain-text API_SECRET") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "This is the plain-text secret you set in your Nightscout server's API_SECRET environment variable — DiaTonomy hashes it automatically, you don't need to compute anything yourself.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { onSave(url.trim(), secret.trim()) }) {
            Text("Save")
        }
    }
}