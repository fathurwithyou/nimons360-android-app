package com.eggheadengineers.nimons360.core.files

import android.content.Context
import java.io.File

class FavoriteLocationPhotoStore(context: Context) {
    private val root = File(context.filesDir, "marked_locations")

    fun save(locationId: Long, fileName: String, bytes: ByteArray): String {
        val directory = File(root, locationId.toString()).also { it.mkdirs() }
        val safeName = sanitizeFileName(fileName).ifBlank { "photo.jpg" }
        val target = uniqueFile(directory, safeName)
        target.writeBytes(bytes)
        return target.absolutePath
    }

    fun delete(path: String) {
        runCatching { File(path).delete() }
    }

    fun deleteAll(paths: List<String>) {
        paths.forEach(::delete)
    }

    private fun uniqueFile(directory: File, fileName: String): File {
        val baseName = fileName.substringBeforeLast('.', fileName)
        val extension = fileName.substringAfterLast('.', missingDelimiterValue = "")
            .takeIf { it.isNotBlank() }
            ?.let { ".$it" }
            .orEmpty()

        var candidate = File(directory, fileName)
        var counter = 1
        while (candidate.exists()) {
            candidate = File(directory, "${baseName}_$counter$extension")
            counter += 1
        }
        return candidate
    }

    private fun sanitizeFileName(value: String): String =
        value.replace(Regex("[^A-Za-z0-9._-]"), "_")
}
