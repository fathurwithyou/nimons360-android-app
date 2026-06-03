package com.eggheadengineers.nimons360.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LocationTracker(context: Context) {
    val context: Context = context.applicationContext

    fun locationUpdates(minTimeMs: Long = 3000L, minDistanceM: Float = 0f): Flow<Location> = callbackFlow {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val hasPermission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            close()
            return@callbackFlow
        }

        val enabledProviders = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
        ).filter { lm.isProviderEnabled(it) }

        if (enabledProviders.isEmpty()) {
            close()
            return@callbackFlow
        }

        enabledProviders
            .mapNotNull { lm.getLastKnownLocation(it) }
            .maxByOrNull { it.time }
            ?.let { trySend(it) }

        val listeners = enabledProviders.map { provider ->
            object : LocationListener {
                override fun onLocationChanged(location: Location) { trySend(location) }
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            }.also { listener ->
                lm.requestLocationUpdates(provider, minTimeMs, minDistanceM, listener)
            }
        }

        awaitClose { listeners.forEach { lm.removeUpdates(it) } }
    }
}
