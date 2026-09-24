package com.riceleaf.local.util

import android.content.Context
import java.io.File

object FileUtil {

    fun getAppImageDir(context: Context): File {
        val dir = File(context.filesDir, "app_images")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getCacheSize(context: Context): String {
        val dir = getAppImageDir(context)
        var size = 0L
        dir.walkTopDown().forEach { file ->
            if (file.isFile) size += file.length()
        }
        return String.format("%.1f MB", size / (1024.0 * 1024.0))
    }

    fun clearCache(context: Context) {
        val dir = getAppImageDir(context)
        dir.listFiles()?.forEach { it.deleteRecursively() }
    }
}
