package com.riceleaf.app.util

import android.content.Context
import java.io.File

object FileUtil {

    fun clearCache(context: Context) {
        val cacheDir = context.cacheDir
        cacheDir.listFiles()?.forEach { it.deleteRecursively() }
    }

    fun getCacheSize(context: Context): String {
        val size = getDirSize(context.cacheDir)
        return when {
            size < 1024 -> "${size}B"
            size < 1024 * 1024 -> "${size / 1024}KB"
            else -> "${"%.1f".format(size / (1024.0 * 1024.0))}MB"
        }
    }

    private fun getDirSize(dir: File): Long {
        var size = 0L
        dir.listFiles()?.forEach {
            size += if (it.isDirectory) getDirSize(it) else it.length()
        }
        return size
    }
}
