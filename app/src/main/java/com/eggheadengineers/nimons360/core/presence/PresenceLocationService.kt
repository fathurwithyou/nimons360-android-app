package com.eggheadengineers.nimons360.core.presence

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.eggheadengineers.nimons360.MainActivity
import com.eggheadengineers.nimons360.NimonsApplication
import com.eggheadengineers.nimons360.R
import com.eggheadengineers.nimons360.core.network.NetworkStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PresenceLocationService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var locationJob: Job? = null
    private var sendJob: Job? = null
    private var sensorJob: Job? = null
    private var batteryJob: Job? = null
    private var networkJob: Job? = null

    private var lat: Double = 0.0
    private var lng: Double = 0.0
    private var rotation: Float = 0f
    private var batteryLevel: Int = 0
    private var charging: Boolean = false
    private var networkStatus: NetworkStatus = NetworkStatus.OFFLINE

    private val app: NimonsApplication
        get() = application as NimonsApplication

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!hasLocationPermission() || !app.userPreferenceStore.isLocationSharingEnabled()) {
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildNotification())
        startPresenceWork()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        locationJob?.cancel()
        sendJob?.cancel()
        sensorJob?.cancel()
        batteryJob?.cancel()
        networkJob?.cancel()
        app.presenceRepository.disconnect()
    }

    private fun startPresenceWork() {
        if (sendJob?.isActive == true) return

        app.presenceRepository.connect()

        locationJob = serviceScope.launch {
            app.locationTracker.locationUpdates(minTimeMs = 1_000L).collectLatest { location ->
                lat = location.latitude
                lng = location.longitude
            }
        }
        sensorJob = serviceScope.launch {
            app.orientationProvider.azimuthFlow().collectLatest { rotation = it }
        }
        batteryJob = serviceScope.launch {
            app.batteryProvider.batteryStateFlow().collectLatest {
                batteryLevel = it.level
                charging = it.charging
            }
        }
        networkJob = serviceScope.launch {
            app.connectivityObserver.status.collectLatest { networkStatus = it }
        }
        sendJob = serviceScope.launch {
            while (true) {
                delay(1_000L)
                if (!app.userPreferenceStore.isLocationSharingEnabled()) {
                    stopSelf()
                    break
                }
                if (lat == 0.0 && lng == 0.0) continue
                app.presenceRepository.sendPresence(
                    lat = lat,
                    lng = lng,
                    rotation = rotation,
                    battery = batteryLevel,
                    charging = charging,
                    internetStatus = networkStatus.toPresenceValue(),
                )
            }
        }
    }

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun buildNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_xml_pin_filled)
            .setContentTitle("Location sharing is active")
            .setContentText("Nimons360 is sending your live location to family members.")
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Location sharing",
            NotificationManager.IMPORTANCE_LOW,
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun NetworkStatus.toPresenceValue(): String = when (this) {
        NetworkStatus.WIFI -> "wifi"
        NetworkStatus.MOBILE -> "mobile"
        NetworkStatus.OFFLINE -> "offline"
    }

    companion object {
        private const val CHANNEL_ID = "location_sharing"
        private const val NOTIFICATION_ID = 3601

        fun start(context: Context) {
            val intent = Intent(context, PresenceLocationService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, PresenceLocationService::class.java))
        }
    }
}
