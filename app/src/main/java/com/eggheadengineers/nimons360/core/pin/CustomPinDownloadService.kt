package com.eggheadengineers.nimons360.core.pin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.eggheadengineers.nimons360.MainActivity
import com.eggheadengineers.nimons360.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class CustomPinDownloadService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pinId = intent?.getStringExtra(EXTRA_PIN_ID) ?: return START_NOT_STICKY
        val pinUrl = intent.getStringExtra(EXTRA_PIN_URL) ?: return START_NOT_STICKY

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(0, "Preparing download"),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC else 0,
        )

        serviceScope.launch {
            downloadPin(pinId, pinUrl)
            withContext(Dispatchers.Main) {
                notifyDone(pinId)
                stopForeground(STOP_FOREGROUND_DETACH)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun downloadPin(pinId: String, url: String) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return
                val body = response.body ?: return
                val contentLength = body.contentLength()
                val inputStream = body.byteStream()
                val dir = File(filesDir, "custom_pins").apply { mkdirs() }
                val file = File(dir, "$pinId.png")
                var bytesRead = 0L
                val buffer = ByteArray(8192)
                FileOutputStream(file).use { output ->
                    while (true) {
                        val read = inputStream.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        bytesRead += read
                        val progress = if (contentLength > 0) (bytesRead * 100 / contentLength).toInt() else 50
                        notifyProgress(progress.coerceIn(0, 99), "Downloading $pinId pin")
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun notifyProgress(progress: Int, title: String) {
        getSystemService(NotificationManager::class.java).notify(
            NOTIFICATION_ID,
            buildNotification(progress, title),
        )
    }

    private fun notifyDone(pinId: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_xml_pin_filled)
            .setContentTitle("Pin ready")
            .setContentText("$pinId pin downloaded. Tap to open.")
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            )
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(progress: Int, title: String) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_xml_pin_filled)
            .setContentTitle(title)
            .setContentText("$progress%")
            .setContentIntent(
                PendingIntent.getActivity(
                    this, 1,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            )
            .setProgress(100, progress, progress == 0)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(CHANNEL_ID, "Custom pin downloads", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val EXTRA_PIN_ID = "pin_id"
        const val EXTRA_PIN_URL = "pin_url"
        private const val CHANNEL_ID = "custom_pin_downloads"
        private const val NOTIFICATION_ID = 3602

        fun start(context: Context, pinId: String, pinUrl: String) {
            val intent = Intent(context, CustomPinDownloadService::class.java).apply {
                putExtra(EXTRA_PIN_ID, pinId)
                putExtra(EXTRA_PIN_URL, pinUrl)
            }
            androidx.core.content.ContextCompat.startForegroundService(context, intent)
        }
    }
}
