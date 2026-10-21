package com.pulsoclima.wear.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.pulsoclima.wear.WeatherUiState
import com.pulsoclima.wear.data.DailyWeather
import com.pulsoclima.wear.data.HourlyWeather
import com.pulsoclima.wear.data.WeatherKind
import com.pulsoclima.wear.data.WeatherReport
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

private val Spanish = Locale("es", "MX")

@Composable
fun PulsoClimaApp(state: WeatherUiState, onRefresh: () -> Unit) {
    val listState = rememberScalingLazyListState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        timeText = { TimeText() },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
    ) {
        when {
            state.report == null && state.isLoading -> LoadingScreen()
            state.report == null -> ErrorScreen(state.message, onRefresh)
            else -> WeatherContent(state, listState, onRefresh)
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(10.dp))
            Text("Buscando el cielo...", style = MaterialTheme.typography.caption1)
        }
    }
}

@Composable
private fun ErrorScreen(message: String?, onRefresh: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(horizontal = 28.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("!", color = MaterialTheme.colors.error, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text(
                message ?: "No hay datos disponibles",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2,
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onRefresh) { Text("↻", fontSize = 22.sp) }
        }
    }
}

@Composable
private fun WeatherContent(
    state: WeatherUiState,
    listState: androidx.wear.compose.foundation.lazy.ScalingLazyListState,
    onRefresh: () -> Unit,
) {
    val report = requireNotNull(state.report)
    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        autoCentering = AutoCenteringParams(itemIndex = 0),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { CurrentConditions(report) }
        if (state.isFallbackLocation) {
            item { Notice("Ubicación de referencia · activá ubicación para datos locales") }
        }
        state.message?.let { message -> item { Notice(message) } }
        item { Metrics(report) }
        item { SectionTitle("PRÓXIMAS HORAS") }
        item { HourlyStrip(report.hourly) }
        item { SectionTitle("5 DÍAS") }
        items(report.daily) { day -> DailyRow(day) }
        item { SunCard(report) }
        item {
            Button(
                onClick = onRefresh,
                enabled = !state.isLoading,
                modifier = Modifier.size(48.dp),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp))
                } else {
                    Text("↻", fontSize = 22.sp)
                }
            }
        }
        item {
            Text(
                if (state.isCached) "Datos guardados" else "Open-Meteo · ${formatTime(report.observedAt)}",
                color = MaterialTheme.colors.onBackground.copy(alpha = 0.55f),
                style = MaterialTheme.typography.caption3,
            )
        }
    }
}

@Composable
private fun CurrentConditions(report: WeatherReport) {
    val kind = WeatherKind.fromCode(report.current.weatherCode)
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(
            "⌖ ${report.locationName}",
            color = Mint,
            style = MaterialTheme.typography.caption1,
            maxLines = 1,
        )
        Spacer(Modifier.height(5.dp))
        Text(kind.symbol, color = if (report.current.isDay) Amber else Sky, fontSize = 42.sp)
        Text(
            "${report.current.temperature.roundToInt()}°",
            color = Ice,
            fontSize = 46.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(kind.label, style = MaterialTheme.typography.body1, fontWeight = FontWeight.Medium)
        Text(
            "Sensación ${report.current.apparentTemperature.roundToInt()}°",
            color = MaterialTheme.colors.onBackground.copy(alpha = 0.65f),
            style = MaterialTheme.typography.caption2,
        )
    }
}

@Composable
private fun Notice(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth(0.86f)
            .clip(RoundedCornerShape(12.dp))
            .background(Amber.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        color = Amber,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.caption3,
    )
}

@Composable
private fun Metrics(report: WeatherReport) {
    Column(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .clip(RoundedCornerShape(18.dp))
            .background(DeepOcean)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Metric("HUMEDAD", "${report.current.humidity}%")
            Metric("VIENTO", "${report.current.windSpeed.roundToInt()} km/h")
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Metric("PRESIÓN", "${report.current.pressure.roundToInt()} hPa")
            Metric("NUBES", "${report.current.cloudCover}%")
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(78.dp)) {
        Text(label, color = Mint.copy(alpha = 0.75f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        Text(value, color = Ice, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, color = Mint, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
}

@Composable
private fun HourlyStrip(hours: List<HourlyWeather>) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        hours.forEach { hour ->
            Column(
                modifier = Modifier
                    .width(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DeepOcean)
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(formatTime(hour.time), style = MaterialTheme.typography.caption3)
                Text(WeatherKind.fromCode(hour.weatherCode).symbol, color = Sky, fontSize = 17.sp)
                Text("${hour.temperature.roundToInt()}°", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("${hour.precipitationProbability}%", color = Sky, fontSize = 9.sp)
            }
        }
    }
}

@Composable
private fun DailyRow(day: DailyWeather) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.88f)
            .clip(RoundedCornerShape(15.dp))
            .background(DeepOcean)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(dayName(day.date), modifier = Modifier.width(46.dp), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(WeatherKind.fromCode(day.weatherCode).symbol, color = Sky, fontSize = 18.sp)
        Spacer(Modifier.weight(1f))
        Text("${day.precipitationProbability}%", color = Sky, fontSize = 10.sp)
        Spacer(Modifier.width(8.dp))
        Text("${day.maxTemperature.roundToInt()}°", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(" / ${day.minTemperature.roundToInt()}°", color = Color.LightGray, fontSize = 12.sp)
    }
}

@Composable
private fun SunCard(report: WeatherReport) {
    val today = report.daily.firstOrNull() ?: return
    Row(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .clip(RoundedCornerShape(16.dp))
            .background(DeepOcean)
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Metric("AMANECER", formatTime(today.sunrise))
        Metric("ATARDECER", formatTime(today.sunset))
    }
}

private fun formatTime(value: String): String = runCatching {
    LocalDateTime.parse(value).format(DateTimeFormatter.ofPattern("HH:mm"))
}.getOrElse { "--:--" }

private fun dayName(value: String): String = runCatching {
    LocalDate.parse(value).dayOfWeek.getDisplayName(TextStyle.SHORT, Spanish)
        .replaceFirstChar { it.uppercase(Spanish) }
}.getOrElse { value.takeLast(5) }
