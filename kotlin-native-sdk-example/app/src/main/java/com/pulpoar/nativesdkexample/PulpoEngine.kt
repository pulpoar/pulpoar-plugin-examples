package com.pulpoar.nativesdkexample

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.pulpomodule.NativeLib
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.Executors

// Face-detection models the engine downloads on init (same set the PulpoAR app uses).
private const val LANDMARK_MODEL_URL = "https://assets.pulpoar.com/vision/pulpo-module/models/landmark_model_310724.enc.gz"
private const val FACE_MODEL_URL = "https://assets.pulpoar.com/vision/pulpo-module/models/fd_model_110822.enc.gz"
private const val QUALITY_MODEL_URL = "https://assets.pulpoar.com/vision/pulpo-module/models/quality_simplified_fp16.onnx.enc"
private const val SKIN_SEG_MODEL_URL = "https://assets.pulpoar.com/vision/pulpo-module/models/skin_segmentation_221225_fp16.enc.gz"

// Square size frames are normalized to before entering the engine.
private const val FRAME_SIZE = 720

/**
 * Thin wrapper around the native PulpoModule SDK (`NativeLib`): loads the face models,
 * runs either the live front camera or a still model photo through the engine and
 * draws the rendered frames into `frameSurface` (see PulpoFrameView.kt).
 */
class PulpoEngine(private val context: Context) {
    sealed interface Status {
        data object Idle : Status
        data object Loading : Status
        data object Running : Status
        data class Failed(val message: String) : Status
    }

    var status by mutableStateOf<Status>(Status.Idle)
        private set
    var faceFound by mutableStateOf(true)
        private set

    /** Where rendered frames are drawn. Show it with `PulpoFrameView(engine)`. */
    val frameSurface = FrameSurface()

    // The native engine is NOT thread-safe. Every NativeLib call runs on this one thread,
    // which is also the camera's analyzer thread, so product changes never race the
    // per-frame setFrame/analyseFace/apply sequence.
    private val engineExecutor = Executors.newSingleThreadExecutor()
    private val engineDispatcher = engineExecutor.asCoroutineDispatcher()
    private val mainExecutor = ContextCompat.getMainExecutor(context)

    // Only touched on the engine thread. True while a still photo (model) is shown
    // instead of the live camera.
    private var isPhotoMode = false
    private var lastFaceFound: Boolean? = null

    // Main thread only.
    private var cameraProvider: ProcessCameraProvider? = null
    private var lifecycleOwner: LifecycleOwner? = null

    // region Lifecycle

    /** Downloads the face models, then turns on the front camera. Call once, on the main thread. */
    suspend fun start(lifecycleOwner: LifecycleOwner) {
        if (status != Status.Idle) return
        status = Status.Loading

        try {
            withContext(engineDispatcher) {
                // Downloads in the background, then initializes the engine on this thread.
                NativeLib.initFaceModuleByUrl(
                    context.cacheDir,
                    LANDMARK_MODEL_URL, FACE_MODEL_URL, QUALITY_MODEL_URL, SKIN_SEG_MODEL_URL,
                    0, 0, 0, 0,
                )
                // Correct upper-eye rendering for mascara/eyeliner (off by default in the SDK).
                NativeLib.setIsUpperEyePolynomialFix(true)
                NativeLib.setGapCorrector(true)
            }
            cameraProvider = ProcessCameraProvider.awaitInstance(context)
        } catch (e: Exception) {
            status = Status.Failed(e.message ?: "Engine init failed")
            return
        }

        this.lifecycleOwner = lifecycleOwner
        showCamera()
        status = Status.Running
    }

    // endregion

    // region Source: camera or photo

    /** Switches to the live front camera. Call on the main thread. */
    fun showCamera() {
        val provider = cameraProvider ?: return
        val owner = lifecycleOwner ?: return

        engineExecutor.execute {
            isPhotoMode = false
            // reset() clears the engine's face tracking and frame geometry. Without it,
            // makeup renders offset after switching between photo and camera.
            NativeLib.reset()
        }

        val analysis = ImageAnalysis.Builder()
            .setResolutionSelector(
                ResolutionSelector.Builder()
                    .setResolutionStrategy(
                        ResolutionStrategy(Size(FRAME_SIZE, FRAME_SIZE), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER)
                    )
                    .build()
            )
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

        // Runs on the engine thread for every camera frame.
        analysis.setAnalyzer(engineExecutor) { image ->
            if (!isPhotoMode) {
                NativeLib.setFrame(image.toUprightSelfie())
                render(faceFound = NativeLib.analyseFace())
            }
            image.close()
        }

        provider.unbindAll()
        provider.bindToLifecycle(owner, CameraSelector.DEFAULT_FRONT_CAMERA, analysis)
    }

    /** Stops the camera and runs a still photo through the engine. Call on the main thread. */
    fun showPhoto(photo: Bitmap) {
        cameraProvider?.unbindAll()
        engineExecutor.execute {
            isPhotoMode = true
            NativeLib.reset()
            NativeLib.setFrame(photo.centerSquare())
            render(faceFound = NativeLib.analyseFace())
        }
    }

    // endregion

    // region Products

    /**
     * Applies variant configs (the `config.config` object of a PulpoAR variant).
     * Pass an empty list to remove all makeup. Does nothing if the calling coroutine
     * was cancelled (a newer selection replaced it) while textures were loading.
     */
    suspend fun setProducts(configs: List<JSONObject>) = withContext(engineDispatcher) {
        // Some products (blush, mascara, eyeliner…) reference textures that must be
        // loaded into the engine before they render.
        for (config in configs) {
            val textures = config.optJSONArray("texture_ids_to_fetch") ?: continue
            for (i in 0 until textures.length()) {
                val id = textures.getJSONObject(i).getString("id")
                val url = textures.getJSONObject(i).getString("url")
                if (!NativeLib.isTextureLoaded(id)) {
                    // Downloads in the background; the camera keeps running meanwhile.
                    NativeLib.setTextureEncryptedByUrl(context.cacheDir, url, id)
                }
            }
        }

        ensureActive()
        // Workaround: setProducts doesn't drop products missing from a shorter list,
        // so clear first.
        NativeLib.setProducts("[]", false)
        NativeLib.setProducts(JSONArray(configs).toString(), false)
        // The live loop picks products up on its next frame; a still photo must be re-rendered.
        if (isPhotoMode) render()
    }

    // endregion

    // region Rendering

    /** Runs on the engine thread. Applies the products to the current frame and shows the result. */
    private fun render(faceFound: Boolean? = null) {
        // Only bother Compose when the face-found state actually changes.
        if (faceFound != null && faceFound != lastFaceFound) {
            lastFaceFound = faceFound
            mainExecutor.execute { this.faceFound = faceFound }
        }

        // apply() already returns the finished frame; no need for getResultFrameAsMat().
        val result: Bitmap = NativeLib.apply() ?: return
        frameSurface.display(result)
    }

    // endregion
}

/** Rotates the camera image upright, mirrors it like a selfie and crops it to a square. */
private fun ImageProxy.toUprightSelfie(): Bitmap {
    val bitmap = toBitmap()
    val matrix = Matrix().apply {
        postRotate(imageInfo.rotationDegrees.toFloat())
        postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
    }
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).centerSquare()
}

/** Crops the middle square of the image and scales it to the engine's frame size. */
private fun Bitmap.centerSquare(): Bitmap {
    val side = minOf(width, height)
    val square = Bitmap.createBitmap(this, (width - side) / 2, (height - side) / 2, side, side)
    return if (side == FRAME_SIZE) square else Bitmap.createScaledBitmap(square, FRAME_SIZE, FRAME_SIZE, true)
}
