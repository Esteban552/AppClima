package com.pulsoclima.wear.data

import android.content.Context

class WeatherCache(context: Context) {
    private val preferences = context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)

    fun save(json: String, locationName: String) {
        preferences.edit()
            .putString("report", json)
            .putString("location", locationName)
            .apply()
    }

    fun load(): WeatherReport? {
        val json = preferences.getString("report", null) ?: return null
        val location = preferences.getString("location", "Ubicación guardada") ?: "Ubicación guardada"
        return runCatching { WeatherJsonParser.parse(json, location) }.getOrNull()
    }
}
