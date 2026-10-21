package com.pulsoclima.wear.data

data class Coordinates(val latitude: Double, val longitude: Double)

data class CurrentWeather(
    val temperature: Double,
    val apparentTemperature: Double,
    val humidity: Int,
    val precipitation: Double,
    val weatherCode: Int,
    val cloudCover: Int,
    val pressure: Double,
    val windSpeed: Double,
    val windDirection: Int,
    val isDay: Boolean,
)

data class HourlyWeather(
    val time: String,
    val temperature: Double,
    val precipitationProbability: Int,
    val weatherCode: Int,
)

data class DailyWeather(
    val date: String,
    val weatherCode: Int,
    val maxTemperature: Double,
    val minTemperature: Double,
    val sunrise: String,
    val sunset: String,
    val precipitationProbability: Int,
)

data class WeatherReport(
    val locationName: String,
    val timezone: String,
    val observedAt: String,
    val current: CurrentWeather,
    val hourly: List<HourlyWeather>,
    val daily: List<DailyWeather>,
    val fetchedAtMillis: Long = System.currentTimeMillis(),
)

enum class WeatherKind(val label: String, val symbol: String) {
    CLEAR("Despejado", "☀"),
    PARTLY_CLOUDY("Parcialmente nublado", "◒"),
    CLOUDY("Nublado", "☁"),
    FOG("Niebla", "≋"),
    DRIZZLE("Llovizna", "♧"),
    RAIN("Lluvia", "☂"),
    SNOW("Nieve", "✻"),
    STORM("Tormenta", "ϟ"),
    UNKNOWN("Sin datos", "?");

    companion object {
        fun fromCode(code: Int): WeatherKind = when (code) {
            0 -> CLEAR
            1, 2 -> PARTLY_CLOUDY
            3 -> CLOUDY
            45, 48 -> FOG
            51, 53, 55, 56, 57 -> DRIZZLE
            61, 63, 65, 66, 67, 80, 81, 82 -> RAIN
            71, 73, 75, 77, 85, 86 -> SNOW
            95, 96, 99 -> STORM
            else -> UNKNOWN
        }
    }
}
