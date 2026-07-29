package com.example.diatonomy.data

import android.content.Context

class SettingsPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    fun getNightscoutUrl(): String = prefs.getString("nightscout_url", "") ?: ""
    fun getApiSecret(): String = prefs.getString("api_secret", "") ?: ""

    fun setNightscoutUrl(url: String) {
        prefs.edit().putString("nightscout_url", url.trimEnd('/')).apply()
    }

    fun setApiSecret(secret: String) {
        prefs.edit().putString("api_secret", secret).apply()
    }

    fun isConfigured(): Boolean = getNightscoutUrl().isNotBlank() && getApiSecret().isNotBlank()
}