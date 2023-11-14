package com.cmu.banavision.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.InputStream

fun getImageNameFromUri(uri: Uri): String? {
    val file = uri.path?.let { File(it) }
    if (file != null) {
        return file.name
    }
    return null
}

fun getImageWidthAndHeight(contentResolver: ContentResolver, uri: Uri): Pair<Int, Int>? {
    val inputStream = contentResolver.openInputStream(uri)
    inputStream?.use { stream ->
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(stream, null, options)
        return Pair(options.outWidth, options.outHeight)
    }
    return null
}

fun getImageSize(contentResolver: ContentResolver, uri: Uri): String {
    var inputStream: InputStream? = null
    try {
        inputStream = contentResolver.openInputStream(uri)
        val bytes = inputStream?.available()?.toLong() ?: 0L

        val kilobytes = bytes / 1024f
        if (kilobytes < 1024) {
            return "%.2f KB".format(kilobytes)
        }

        val megabytes = kilobytes / 1024f
        if (megabytes < 1024) {
            return "%.2f MB".format(megabytes)
        }

        val gigabytes = megabytes / 1024f
        return "%.2f GB".format(gigabytes)
    } finally {
        inputStream?.close()
    }
}

fun Uri.toFile(context: Context): File? {
    val inputStream = context.contentResolver.openInputStream(this) ?: return null

    val file = File(context.cacheDir, "temp")
    file.outputStream().use { outputStream ->
        val buffer = ByteArray(4 * 1024) // 4KB buffer
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
        outputStream.flush()
    }
    inputStream.close()
    return file
}


