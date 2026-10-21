package com.pulsoclima.wear.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import android.os.CancellationSignal
import com.pulsoclima.wear.data.Coordinates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class WatchLocationProvider(private val context: Context) {
    private val locationManager = context.getSystemService(LocationManager::class.java)

    @SuppressLint("MissingPermission")
    suspend fun currentCoordinates(): Coordinates? = suspendCancellableCoroutine { continuation ->
        val cancellationSignal = CancellationSignal()
        continuation.invokeOnCancellation { cancellationSignal.cancel() }
        locationManager.getCurrentLocation(
            LocationManager.FUSED_PROVIDER,
            cancellationSignal,
            context.mainExecutor,
        ) { location ->
            if (continuation.isActive) {
                continuation.resume(location?.let { Coordinates(it.latitude, it.longitude) })
            }
        }
    }

    @Suppress("DEPRECATION")
    suspend fun locationName(coordinates: Coordinates): String = withContext(Dispatchers.IO) {
        runCatching {
            Geocoder(context, Locale("es", "MX"))
                .getFromLocation(coordinates.latitude, coordinates.longitude, 1)
                ?.firstOrNull()
                ?.let { it.locality ?: it.subAdminArea ?: it.adminArea }
        }.getOrNull() ?: "Mi ubicación"
    }
}
