package com.riceleaf.local.ml

import android.graphics.Bitmap
import android.graphics.Color

object LesionEstimator {

    fun estimateLesionRatio(bitmap: Bitmap): Float {
        val w = bitmap.width
        val h = bitmap.height
        if (w == 0 || h == 0) return 0f

        var leafPixels = 0
        var greenPixels = 0

        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)

        for (pixel in pixels) {
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)

            val gray = (r + g + b) / 3
            if (gray < 30) continue

            leafPixels++

            val hsv = FloatArray(3)
            rgbToHsv(r, g, b, hsv)
            val hue = hsv[0]
            val sat = hsv[1]
            val v = hsv[2]

            if (hue in 25f..90f && sat >= 0.15f && v >= 0.15f) {
                greenPixels++
            }
        }

        if (leafPixels == 0) return 0f
        return ((leafPixels - greenPixels).toFloat() / leafPixels * 100f)
            .coerceIn(0f, 100f)
    }

    private fun rgbToHsv(r: Int, g: Int, b: Int, out: FloatArray) {
        val rf = r / 255f
        val gf = g / 255f
        val bf = b / 255f
        val max = maxOf(rf, gf, bf)
        val min = minOf(rf, gf, bf)
        val delta = max - min

        out[2] = max
        if (delta == 0f) { out[0] = 0f; out[1] = 0f }
        else {
            out[1] = delta / max
            out[0] = when (max) {
                rf -> ((gf - bf) / delta) % 6f * 60f
                gf -> ((bf - rf) / delta + 2f) * 60f
                else -> ((rf - gf) / delta + 4f) * 60f
            }
            if (out[0] < 0) out[0] += 360f
        }
    }
}
