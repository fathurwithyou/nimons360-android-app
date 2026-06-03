package com.eggheadengineers.nimons360.core.pin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.eggheadengineers.nimons360.MainActivity
import com.eggheadengineers.nimons360.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class CustomPinDownloadService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification(0, "Preparing pin download"))
        serviceScope.launch {
            for (progress in listOf(18, 42, 67, 86)) {
                delay(450L)
                notifyProgress(progress, "Downloading custom pin")
            }
            writePinFile()
            notifyDone()
            stopForeground(STOP_FOREGROUND_DETACH)
            stopSelf()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun writePinFile() {
        val dir = File(filesDir, "custom_pins").apply { mkdirs() }
        val file = File(dir, PIN_FILE_NAME)
        FileOutputStream(file).use { output ->
            createPinBitmap().compress(Bitmap.CompressFormat.PNG, 100, output)
        }
    }

    private fun notifyProgress(progress: Int, title: String) {
        getSystemService(NotificationManager::class.java).notify(
            NOTIFICATION_ID,
            buildNotification(progress, title),
        )
    }

    private fun notifyDone() {
        val uri = FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            File(File(filesDir, "custom_pins"), PIN_FILE_NAME),
        )
        val openIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "image/png")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            2,
            Intent.createChooser(openIntent, "Open custom pin"),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_xml_pin_filled)
            .setContentTitle("Custom pin ready")
            .setContentText("Your Nimons360 pin has been downloaded.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotification(progress: Int, title: String) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_xml_pin_filled)
            .setContentTitle(title)
            .setContentText("$progress% complete")
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    1,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
            .setProgress(100, progress, false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Custom pin downloads",
            NotificationManager.IMPORTANCE_LOW,
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun createPinBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(18, 18, 18) }
        val highlight = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(241, 236, 229) }
        val path = Path().apply {
            moveTo(128f, 236f)
            cubicTo(78f, 172f, 46f, 132f, 46f, 88f)
            cubicTo(46f, 42f, 82f, 18f, 128f, 18f)
            cubicTo(174f, 18f, 210f, 42f, 210f, 88f)
            cubicTo(210f, 132f, 178f, 172f, 128f, 236f)
            close()
        }
        canvas.drawPath(path, fill)
        canvas.drawCircle(128f, 86f, 34f, highlight)
        return bitmap
    }

    companion object {
        private const val CHANNEL_ID = "custom_pin_downloads"
        private const val NOTIFICATION_ID = 3602
        const val PIN_FILE_NAME = "nimons_custom_pin.png"

        fun start(context: Context) {
            androidx.core.content.ContextCompat.startForegroundService(
                context,
                Intent(context, CustomPinDownloadService::class.java),
            )
        }
    }
}
