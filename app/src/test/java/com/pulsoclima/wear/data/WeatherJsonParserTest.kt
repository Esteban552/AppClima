package com.pulsoclima.wear.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherJsonParserTest {
    @Test
    fun `parses current hourly and daily weather`() {
        val report = WeatherJsonParser.parse(fixture, "Rosario")

        assertEquals("Rosario", report.locationName)
        assertEquals(21.4, report.current.temperature, 0.01)
        assertEquals(56, report.current.humidity)
        assertEquals(2, report.hourly.size)
        assertEquals("2026-10-19T14:00", report.hourly.first().time)
        assertEquals(1, report.daily.size)
        assertTrue(report.current.isDay)
    }

    @Test
    fun `maps every wmo weather family`() {
        assertEquals(WeatherKind.CLEAR, WeatherKind.fromCode(0))
        assertEquals(WeatherKind.FOG, WeatherKind.fromCode(45))
        assertEquals(WeatherKind.RAIN, WeatherKind.fromCode(82))
        assertEquals(WeatherKind.SNOW, WeatherKind.fromCode(75))
        assertEquals(WeatherKind.STORM, WeatherKind.fromCode(99))
        assertEquals(WeatherKind.UNKNOWN, WeatherKind.fromCode(500))
    }

    private val fixture = """
        {
          "timezone":"America/Mexico_City",
          "current":{
            "time":"2026-10-19T14:00","temperature_2m":21.4,
            "relative_humidity_2m":56,"apparent_temperature":22.7,"is_day":1,
            "precipitation":0.0,"weather_code":3,"cloud_cover":98,
            "pressure_msl":1015.1,"wind_speed_10m":4.5,"wind_direction_10m":307
          },
          "hourly":{
            "time":["2026-10-19T13:00","2026-10-19T14:00","2026-10-19T15:00"],
            "temperature_2m":[20.8,21.4,21.5],
            "precipitation_probability":[21,27,33],
            "weather_code":[3,3,51]
          },
          "daily":{
            "time":["2026-10-19"],"weather_code":[80],
            "temperature_2m_max":[21.5],"temperature_2m_min":[11.9],
            "sunrise":["2026-10-19T06:32"],"sunset":["2026-10-19T18:10"],
            "precipitation_probability_max":[42]
          }
        }
    """.trimIndent()
}
