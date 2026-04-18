package com.eggheadengineers.nimons360.data.repository

import android.util.Log
import com.eggheadengineers.nimons360.core.session.SessionManager
import com.eggheadengineers.nimons360.data.dto.EndStreamRequestDto
import com.eggheadengineers.nimons360.data.dto.LiveStreamDto
import com.eggheadengineers.nimons360.data.dto.StartStreamRequestDto
import com.eggheadengineers.nimons360.data.dto.StreamEndedPayloadDto
import com.eggheadengineers.nimons360.data.network.LiveConfig
import com.eggheadengineers.nimons360.data.network.LiveStreamApiService
import com.eggheadengineers.nimons360.data.network.requireSuccess
import com.eggheadengineers.nimons360.domain.mapper.toDomain
import com.eggheadengineers.nimons360.domain.model.LiveStream
import com.eggheadengineers.nimons360.domain.repository.LiveStreamRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class LiveStreamRepositoryImpl(
    private val api: LiveStreamApiService,
    private val wsClient: OkHttpClient,
    private val sessionManager: SessionManager,
) : LiveStreamRepository {

    companion object {
        private const val TAG = "LiveStreamRepo"
        private const val RECONNECT_DELAY_MS = 3000L
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val _streams = MutableStateFlow<Map<String, LiveStream>>(emptyMap())
    private var webSocket: WebSocket? = null
    @Volatile private var shouldReconnect = false

    override suspend fun startStream(familyId: String, title: String?): Result<LiveStream> = runCatching {
        val broadcasterId = sessionManager.getUserId() ?: error("Not signed in")
        val broadcasterName = sessionManager.getUserName() ?: "A family member"
        val response = api.startStream(
            StartStreamRequestDto(
                familyId = familyId,
                broadcasterId = broadcasterId,
                broadcasterName = broadcasterName,
                title = title?.trim()?.takeIf { it.isNotEmpty() },
            ),
        )
        response.requireSuccess("Failed to start stream")
        val dto = response.body()?.data ?: error("Empty stream response")
        val stream = dto.toDomain()
        _streams.update { it + (stream.id to stream) }
        stream
    }

    override suspend fun endStream(streamId: String): Result<Unit> = runCatching {
        val broadcasterId = sessionManager.getUserId() ?: error("Not signed in")
        val response = api.endStream(streamId, EndStreamRequestDto(broadcasterId))
        if (!response.isSuccessful && response.code() != 404) {
            response.requireSuccess("Failed to end stream")
        }
        _streams.update { it - streamId }
    }

    override suspend fun listStreams(familyId: String): Result<List<LiveStream>> = runCatching {
        val response = api.listStreams(familyId)
        response.requireSuccess("Failed to load live streams")
        val items = response.body()?.data.orEmpty().map { it.toDomain() }
        _streams.update { current ->
            val preserved = current.filterValues { it.familyId != familyId }
            preserved + items.associateBy { it.id }
        }
        items
    }

    override fun observeFamilyStreams(familyId: String): Flow<List<LiveStream>> =
        _streams.asStateFlow().map { map ->
            map.values.filter { it.familyId == familyId }.sortedByDescending { it.startedAt }
        }

    override fun connect() {
        shouldReconnect = true
        openSocket()
    }

    override fun disconnect() {
        shouldReconnect = false
        webSocket?.close(1000, "Disconnecting")
        webSocket = null
    }

    private fun openSocket() {
        webSocket?.close(1000, null)
        val url = buildString {
            append(LiveConfig.COORDINATOR_WS_URL)
            if (LiveConfig.API_KEY.isNotEmpty()) {
                val sep = if (LiveConfig.COORDINATOR_WS_URL.contains('?')) '&' else '?'
                append(sep).append("apiKey=").append(LiveConfig.API_KEY)
            }
        }
        val request = Request.Builder().url(url).apply {
            if (LiveConfig.API_KEY.isNotEmpty()) header("x-api-key", LiveConfig.API_KEY)
        }.build()

        Log.d(TAG, "Opening live-streams WebSocket")

        webSocket = wsClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "Live-streams WS connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Live-streams WS failure: ${t.message}")
                scheduleReconnect()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Live-streams WS closed: $code $reason")
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        if (!shouldReconnect) return
        Thread {
            Thread.sleep(RECONNECT_DELAY_MS)
            if (shouldReconnect) openSocket()
        }.start()
    }

    private fun handleMessage(text: String) {
        runCatching {
            val envelope = json.parseToJsonElement(text).jsonObject
            val type = envelope["type"]?.jsonPrimitive?.content ?: return
            val payload = envelope["payload"] ?: return
            when (type) {
                "snapshot" -> {
                    val list = (payload as? JsonArray)?.map {
                        json.decodeFromJsonElement(LiveStreamDto.serializer(), it).toDomain()
                    }.orEmpty()
                    _streams.value = list.associateBy { it.id }
                }
                "stream_started" -> {
                    val dto = json.decodeFromJsonElement(LiveStreamDto.serializer(), payload)
                    val stream = dto.toDomain()
                    _streams.update { it + (stream.id to stream) }
                }
                "stream_ended" -> {
                    val ended = json.decodeFromJsonElement(StreamEndedPayloadDto.serializer(), payload)
                    _streams.update { it - ended.id }
                }
            }
        }.onFailure { Log.e(TAG, "Failed to parse live WS message: ${it.message}") }
    }

    @Suppress("unused")
    private fun currentUserIdBlocking(): String? = runBlocking { sessionManager.getUserId() }
}
