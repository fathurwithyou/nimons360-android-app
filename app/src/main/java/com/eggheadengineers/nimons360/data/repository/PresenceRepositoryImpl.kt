package com.eggheadengineers.nimons360.data.repository

import android.util.Log
import com.eggheadengineers.nimons360.core.session.SessionManager
import com.eggheadengineers.nimons360.data.dto.MemberPresenceUpdatedPayloadDto
import com.eggheadengineers.nimons360.data.dto.UpdatePresencePayloadDto
import com.eggheadengineers.nimons360.domain.mapper.toDomain
import com.eggheadengineers.nimons360.domain.model.MemberPresence
import com.eggheadengineers.nimons360.domain.repository.PresenceRepository
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PresenceRepositoryImpl(
    private val okHttpClient: OkHttpClient,
    private val sessionManager: SessionManager,
) : PresenceRepository {

    companion object {
        private const val TAG = "PresenceWS"
        private const val WS_URL = "ws://10.0.2.2:3000/ws/live"
        private const val RECONNECT_DELAY_MS = 3000L
    }

    private val gson = Gson()
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private var webSocket: WebSocket? = null
    private val _members = MutableStateFlow<Map<String, MemberPresence>>(emptyMap())
    @Volatile private var shouldReconnect = false

    override fun connect() {
        shouldReconnect = true
        openSocket()
    }

    private fun openSocket() {
        webSocket?.close(1000, null)
        val token = runBlocking { sessionManager.getToken() }
        val url = if (token != null) "$WS_URL?token=$token" else WS_URL
        val request = Request.Builder()
            .url(url)
            .build()

        Log.d(TAG, "Opening WebSocket to $WS_URL")

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "WS message: $text")
                handleMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket failure: ${t.message}")
                scheduleReconnect()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $code $reason")
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        if (!shouldReconnect) return
        Thread {
            Thread.sleep(RECONNECT_DELAY_MS)
            if (shouldReconnect) {
                // Refresh lastSeen on all existing members so they aren't pruned
                // while we're reconnecting and waiting for the first round of updates.
                val now = System.currentTimeMillis()
                _members.update { current ->
                    current.mapValues { (_, m) -> m.copy(lastSeen = now) }
                }
                openSocket()
            }
        }.start()
    }

    private fun handleMessage(text: String) {
        runCatching {
            val envelope = gson.fromJson(text, JsonObject::class.java)
            val type = envelope.get("type")?.asString ?: return
            Log.d(TAG, "Message type: $type")
            if (type == "member_presence_updated") {
                val payloadJson = envelope.get("payload")?.toString() ?: return
                val payload = gson.fromJson(payloadJson, MemberPresenceUpdatedPayloadDto::class.java)
                val presence = payload.toDomain()
                Log.d(TAG, "Presence update: userId=${presence.userId} name=${presence.name}")
                if (presence.userId.isNotEmpty()) {
                    _members.update { current ->
                        current.toMutableMap().also { it[presence.userId] = presence }
                    }
                }
            }
        }.onFailure { e ->
            Log.e(TAG, "Failed to parse WS message: ${e.message}")
        }
    }

    override fun disconnect() {
        shouldReconnect = false
        webSocket?.close(1000, "Disconnecting")
        webSocket = null
    }

    override fun sendPresence(
        lat: Double, lng: Double, rotation: Float,
        battery: Int, charging: Boolean, internetStatus: String
    ) {
        val name = runBlocking { sessionManager.getUserName() } ?: ""
        val payload = UpdatePresencePayloadDto(
            name = name,
            latitude = lat,
            longitude = lng,
            rotation = rotation,
            batteryLevel = battery,
            isCharging = charging,
            internetStatus = internetStatus,
        )
        val envelope = mapOf(
            "type" to "update_presence",
            "payload" to payload,
            "timestamp" to isoFormat.format(Date()),
        )
        webSocket?.send(gson.toJson(envelope))
    }

    override fun observeMembers(): Flow<Map<String, MemberPresence>> = _members.asStateFlow()

    override fun removeStaleMembers(maxAgeMs: Long) {
        val cutoff = System.currentTimeMillis() - maxAgeMs
        _members.update { current ->
            current.filter { (_, m) -> m.lastSeen > cutoff }
        }
    }
}
