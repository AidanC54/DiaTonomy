package com.example.diatonomy.network

import com.example.diatonomy.util.sha1
import net.cacheux.nvplib.data.InsulinDose
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

class NightscoutClient(
    private val baseUrl: String,
    private val apiSecretPlain: String
) {
    private val client = OkHttpClient()

    fun postTreatments(doses: List<InsulinDose>, eventType: String, serial: String): Boolean {
        if (doses.isEmpty()) return true
        if (baseUrl.isBlank() || apiSecretPlain.isBlank()) {
            android.util.Log.e("NvpDebug", "Nightscout not configured — skipping POST")
            return false
        }

        val json = JSONArray().apply {
            doses.forEach { dose ->
                put(JSONObject().apply {
                    put("eventType", eventType)
                    put("insulin", dose.units / 10.0)
                    put("created_at", Instant.ofEpochMilli(dose.time).toString())
                    put("notes", "DiaTonomy auto-sync (pen: $serial)")
                })
            }
        }

        val request = Request.Builder()
            .url("$baseUrl/api/v1/treatments")
            .addHeader("API-SECRET", sha1(apiSecretPlain))
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            client.newCall(request).execute().use { it.isSuccessful }
        } catch (e: Exception) {
            android.util.Log.e("NvpDebug", "Nightscout POST failed: ${e.message}")
            false
        }
    }
}