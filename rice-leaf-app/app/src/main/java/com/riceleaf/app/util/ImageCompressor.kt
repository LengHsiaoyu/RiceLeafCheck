package com.riceleaf.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object ImageCompressor {

    fun compress(context: Context, uri: Uri, maxWidth: Int = 1920, quality: Int = 85): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream?.close()

        val (origW, origH) = options.outWidth to options.outHeight
        var sampleSize = 1
        while (origW / sampleSize > maxWidth) {
            sampleSize *= 2
        }

        val stream = context.contentResolver.openInputStream(uri)
        val decodeOpts = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        val bitmap = BitmapFactory.decodeStream(stream, null, decodeOpts)
        stream?.close()

        val file = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }
        bitmap.recycle()

        return file
    }
}
