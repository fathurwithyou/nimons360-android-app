package com.eggheadengineers.nimons360.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

@Serializable
private data class ApiErrorEnvelope(
    @SerialName("error") val error: ApiErrorBody?,
)

@Serializable
private data class ApiErrorBody(
    @SerialName("code") val code: String?,
    @SerialName("message") val message: String?,
)

private val json = Json { ignoreUnknownKeys = true }

fun Response<*>.requireSuccess(defaultMessage: String) {
    if (isSuccessful) return

    val parsedMessage = runCatching {
        errorBody()
            ?.string()
            ?.takeIf { it.isNotBlank() }
            ?.let { body -> json.decodeFromString<ApiErrorEnvelope>(body) }
            ?.error
            ?.message
            ?.takeIf { it.isNotBlank() }
    }.getOrNull()

    error(parsedMessage ?: "$defaultMessage (${code()})")
}

fun Throwable.userFriendlyMessage(fallback: String): String = when (this) {
    is ConnectException,
    is UnknownHostException,
    -> "Unable to reach the server. Please check your internet connection and try again."
    is SocketTimeoutException ->
        "The request timed out. Please check your connection and try again."
    is SSLException ->
        "A secure connection could not be established. Please try again later."
    else -> {
        val msg = message.orEmpty()
        when {
            msg.contains("failed to connect", ignoreCase = true) ||
                msg.contains("unable to resolve host", ignoreCase = true) ->
                "Unable to reach the server. Please check your internet connection and try again."
            msg.contains("timeout", ignoreCase = true) ->
                "The request timed out. Please check your connection and try again."
            msg.isNotBlank() -> msg
            else -> fallback
        }
    }
}
