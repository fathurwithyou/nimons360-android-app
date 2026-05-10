package com.eggheadengineers.nimons360.core.media

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class ImagePayload(
    val fileName: String,
    val mediaType: String,
    val bytes: ByteArray,
)

suspend fun readImagePayload(
    context: Context,
    uri: Uri,
    maxBytes: Int = 500 * 1024,
): Result<ImagePayload> = withContext(Dispatchers.IO) {
    runCatching {
        val resolver = context.contentResolver
        val mediaType = resolveImageMediaType(resolver.getType(uri), uri)
        require(mediaType == "image/png" || mediaType == "image/jpeg") {
            "Image must be a PNG or JPEG file."
        }

        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Unable to open selected image.")
        require(bytes.size <= maxBytes) {
            "Image must be ${maxBytes / 1024} KB or smaller."
        }

        ImagePayload(
            fileName = queryDisplayName(context, uri) ?: defaultImageName(mediaType),
            mediaType = mediaType,
            bytes = bytes,
        )
    }
}

fun createCacheImageUri(context: Context, directoryName: String, prefix: String): Uri {
    val directory = File(context.cacheDir, directoryName).also { it.mkdirs() }
    val file = File(directory, "${prefix}_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
}

private fun queryDisplayName(context: Context, uri: Uri): String? {
    val projection = arrayOf(OpenableColumns.DISPLAY_NAME)
    return context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
    }?.takeIf { it.isNotBlank() }
}

private fun resolveImageMediaType(contentType: String?, uri: Uri): String {
    if (contentType == "image/png" || contentType == "image/jpeg") return contentType
    val path = uri.toString().lowercase()
    return when {
        path.endsWith(".png") -> "image/png"
        path.endsWith(".jpg") || path.endsWith(".jpeg") -> "image/jpeg"
        else -> contentType.orEmpty()
    }
}

private fun defaultImageName(mediaType: String): String =
    if (mediaType == "image/png") "image.png" else "image.jpg"
