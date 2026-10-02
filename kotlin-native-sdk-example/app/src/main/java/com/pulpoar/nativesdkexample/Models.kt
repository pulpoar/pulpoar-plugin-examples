package com.pulpoar.nativesdkexample

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

// Preset face photos users can try makeup on instead of the live camera.

data class FaceModel(val id: Int) {
    val url get() = "https://plugin.pulpoar.com/vto/images/face-model-women-$id.webp"
}

val FACE_MODELS = (1..8).map { FaceModel(it) }

object FaceModelLoader {
    private val cache = mutableMapOf<Int, Bitmap>()

    suspend fun image(model: FaceModel): Bitmap {
        synchronized(cache) { cache[model.id] }?.let { return it }

        val bitmap = withContext(Dispatchers.IO) {
            URL(model.url).openStream().use { BitmapFactory.decodeStream(it) }
        } ?: error("Could not decode ${model.url}")
        synchronized(cache) { cache[model.id] = bitmap }
        return bitmap
    }
}
