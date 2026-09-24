package com.riceleaf.local.ml

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import android.graphics.Bitmap
import java.nio.FloatBuffer

object Preprocessor {

    fun preprocess(bitmap: Bitmap, info: ModelInfo, env: OrtEnvironment): OnnxTensor {
        val w = info.inputSize[0]
        val h = info.inputSize[1]
        val resized = Bitmap.createScaledBitmap(bitmap, w, h, true)

        val pixels = IntArray(w * h)
        resized.getPixels(pixels, 0, w, 0, 0, w, h)

        val mean = info.normalize.mean
        val std = info.normalize.std
        val size = w * h
        val floatArray = FloatArray(3 * size)

        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = ((pixel shr 16) and 0xFF) / 255f
            val g = ((pixel shr 8) and 0xFF) / 255f
            val b = (pixel and 0xFF) / 255f

            floatArray[i] = (r - mean[0]) / std[0]
            floatArray[size + i] = (g - mean[1]) / std[1]
            floatArray[2 * size + i] = (b - mean[2]) / std[2]
        }

        return OnnxTensor.createTensor(env, FloatBuffer.wrap(floatArray),
            longArrayOf(1, 3, h.toLong(), w.toLong()))
    }
}
