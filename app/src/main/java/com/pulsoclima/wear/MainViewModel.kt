package com.pulsoclima.wear

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pulsoclima.wear.data.Coordinates
import com.pulsoclima.wear.data.WeatherCache
import com.pulsoclima.wear.data.WeatherReport
import com.pulsoclima.wear.data.WeatherService
import com.pulsoclima.wear.location.WatchLocationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeatherUiState(
    val report: WeatherReport? = null,
    val isLoading: Boolean = false,
    val isCached: Boolean = false,
    val isFallbackLocation: Boolean = false,
    val message: String? = null,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val service = WeatherService()
    private val cache = WeatherCache(application)
    private val locationProvider = WatchLocationProvider(application)
    private val _state = MutableStateFlow(WeatherUiState())
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    fun refresh(useDeviceLocation: Boolean) {
        if (_state.value.isLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = null) }

            val fallback = Coordinates(19.4326, -99.1332)
            val deviceCoordinates = if (useDeviceLocation) {
                runCatching { locationProvider.currentCoordinates() }.getOrNull()
            } else {
                null
            }
            val coordinates = deviceCoordinates ?: fallback
            val locationName = if (deviceCoordinates != null) {
                locationProvider.locationName(deviceCoordinates)
            } else {
                "Ciudad de México"
            }

            runCatching { service.fetch(coordinates, locationName) }
                .onSuccess { fetched ->
                    cache.save(fetched.rawJson, locationName)
                    _state.value = WeatherUiState(
                        report = fetched.report,
                        isFallbackLocation = deviceCoordinates == null,
                    )
                }
                .onFailure {
                    val cached = cache.load()
                    _state.value = WeatherUiState(
                        report = cached,
                        isCached = cached != null,
                        isFallbackLocation = deviceCoordinates == null,
                        message = if (cached != null) {
                            "Sin conexión: mostrando el último reporte"
                        } else {
                            "No se pudo obtener el clima. Revisá la conexión."
                        },
                    )
                }
        }
    }
}
