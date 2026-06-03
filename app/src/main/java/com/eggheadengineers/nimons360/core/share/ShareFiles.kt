package com.eggheadengineers.nimons360.core.share

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun writeShareFile(
    context: Context,
    fileName: String,
    bytes: ByteArray,
): Uri {
    val dir = File(context.cacheDir, "shares").apply { mkdirs() }
    val file = File(dir, fileName)
    file.writeBytes(bytes)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file,
    )
}
