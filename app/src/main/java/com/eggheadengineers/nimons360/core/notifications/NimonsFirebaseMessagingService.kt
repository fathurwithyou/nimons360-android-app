package com.eggheadengineers.nimons360.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.eggheadengineers.nimons360.MainActivity
import com.eggheadengineers.nimons360.NimonsApplication
import com.eggheadengineers.nimons360.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NimonsFirebaseMessagingService : FirebaseMessagingService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        serviceScope.launch {
            val app = application as NimonsApplication
            app.notificationTokenSync.syncRefreshedToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val app = application as NimonsApplication
        if (!app.userPreferenceStore.isNotificationEnabled()) return

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "Nimons360"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: message.data["message"]
            ?: return

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        ensureChannel()
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_xml_pin_filled)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        getSystemService(NotificationManager::class.java).notify(
            System.currentTimeMillis().toInt(),
            notification,
        )
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Family notifications",
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "family_notifications"
    }
}
