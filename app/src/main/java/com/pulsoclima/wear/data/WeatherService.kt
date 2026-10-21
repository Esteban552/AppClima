package com.pulsoclima.wear.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class FetchedWeather(val report: WeatherReport, val rawJson: String)

class WeatherService {
    suspend fun fetch(coordinates: Coordinates, locationName: String): FetchedWeather =
        withContext(Dispatchers.IO) {
            val query = buildString {
                append("latitude=${coordinates.latitude}&longitude=${coordinates.longitude}")
                append("&current=temperature_2m,relative_humidity_2m,apparent_temperature,is_day,")
                append("precipitation,weather_code,cloud_cover,pressure_msl,wind_speed_10m,wind_direction_10m")
                append("&hourly=temperature_2m,precipitation_probability,weather_code")
                append("&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,precipitation_probability_max")
                append("&timezone=${URLEncoder.encode("auto", "UTF-8")}&forecast_days=5")
            }
            val connection = URL("https://api.open-meteo.com/v1/forecast?$query")
                .openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty("Accept", "application/json")

            try {
                if (connection.responseCode !in 200..299) {
                    error("El servicio respondió ${connection.responseCode}")
                }
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                FetchedWeather(WeatherJsonParser.parse(body, locationName), body)
            } finally {
                connection.disconnect()
            }
        }
}

object WeatherJsonParser {
    fun parse(json: String, locationName: String): WeatherReport {
        val root = JSONObject(json)
        val current = root.getJSONObject("current")
        val hourly = root.getJSONObject("hourly")
        val daily = root.getJSONObject("daily")
        val currentTime = current.getString("time")
        val hourlyTimes = hourly.getJSONArray("time")
        val startIndex = (0 until hourlyTimes.length())
            .firstOrNull { hourlyTimes.getString(it) >= currentTime } ?: 0

        return WeatherReport(
            locationName = locationName,
            timezone = root.getString("timezone"),
            observedAt = currentTime,
            current = CurrentWeather(
                temperature = current.getDouble("temperature_2m"),
                apparentTemperature = current.getDouble("apparent_temperature"),
                humidity = current.getInt("relative_humidity_2m"),
                precipitation = current.getDouble("precipitation"),
                weatherCode = current.getInt("weather_code"),
                cloudCover = current.getInt("cloud_cover"),
                pressure = current.getDouble("pressure_msl"),
                windSpeed = current.getDouble("wind_speed_10m"),
                windDirection = current.getInt("wind_direction_10m"),
                isDay = current.getInt("is_day") == 1,
            ),
            hourly = (startIndex until minOf(startIndex + 12, hourlyTimes.length())).map { index ->
                HourlyWeather(
                    time = hourlyTimes.getString(index),
                    temperature = hourly.getJSONArray("temperature_2m").getDouble(index),
                    precipitationProbability = hourly.getJSONArray("precipitation_probability").getInt(index),
                    weatherCode = hourly.getJSONArray("weather_code").getInt(index),
                )
            },
            daily = (0 until daily.getJSONArray("time").length()).map { index ->
                DailyWeather(
                    date = daily.getJSONArray("time").getString(index),
                    weatherCode = daily.getJSONArray("weather_code").getInt(index),
                    maxTemperature = daily.getJSONArray("temperature_2m_max").getDouble(index),
                    minTemperature = daily.getJSONArray("temperature_2m_min").getDouble(index),
                    sunrise = daily.getJSONArray("sunrise").getString(index),
                    sunset = daily.getJSONArray("sunset").getString(index),
                    precipitationProbability = daily.getJSONArray("precipitation_probability_max").getInt(index),
                )
            },
        )
    }
}
