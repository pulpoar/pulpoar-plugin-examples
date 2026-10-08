package com.pulpoar.nativesdkexample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.coroutines.cancellation.CancellationException

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { TryOnScreen() }
    }
}

@Composable
private fun TryOnScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val engine = remember { PulpoEngine(context.applicationContext) }

    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        hasCameraPermission = it
    }

    var selectedIds by remember { mutableStateOf(emptySet<String>()) }
    // null = live camera.
    var selectedModel by remember { mutableStateOf<FaceModel?>(null) }
    val isRunning = engine.status == PulpoEngine.Status.Running

    LaunchedEffect(hasCameraPermission) {
        if (hasCameraPermission) engine.start(lifecycleOwner) else permissionLauncher.launch(Manifest.permission.CAMERA)
    }
    // Compose cancels the previous effect when the key changes, so only the latest
    // selection is ever applied.
    LaunchedEffect(selectedModel) {
        if (!isRunning) return@LaunchedEffect
        val model = selectedModel
        if (model == null) {
            engine.showCamera()
            return@LaunchedEffect
        }
        try {
            engine.showPhoto(FaceModelLoader.image(model))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("Pulpo", "Failed to load model photo", e)
        }
    }
    LaunchedEffect(selectedIds) {
        if (!isRunning) return@LaunchedEffect
        try {
            engine.setProducts(selectedIds.map { VariantApi.config(it) })
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("Pulpo", "Failed to load variant config", e)
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        // The engine outputs a square frame; it is fitted so the whole face stays visible.
        PulpoFrameView(engine, Modifier.fillMaxSize())

        Column(Modifier.fillMaxSize().systemBarsPadding().padding(vertical = 16.dp)) {
            // Camera / model picker
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Box(
                        Modifier.size(56.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .selectionRing(selectedModel == null)
                            .clickable(enabled = isRunning) { selectedModel = null },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(painterResource(android.R.drawable.ic_menu_camera), contentDescription = "Camera", tint = Color.White)
                    }
                }
                items(FACE_MODELS) { model ->
                    ModelThumbnail(
                        model,
                        Modifier.size(56.dp)
                            .clip(CircleShape)
                            .selectionRing(selectedModel == model)
                            .clickable(enabled = isRunning) { selectedModel = model },
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            StatusLabel(engine, hasCameraPermission)

            // Product swatches
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(top = 16.dp),
            ) {
                items(DEMO_VARIANTS) { variant ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable(enabled = isRunning) {
                            selectedIds = if (variant.id in selectedIds) selectedIds - variant.id else selectedIds + variant.id
                        },
                    ) {
                        Box(
                            Modifier.size(48.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(variant.color)))
                                .selectionRing(variant.id in selectedIds)
                        )
                        Text(variant.name, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusLabel(engine: PulpoEngine, hasCameraPermission: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        when (val status = engine.status) {
            PulpoEngine.Status.Loading -> {
                CircularProgressIndicator(color = Color.White)
                Text("Downloading face models…", color = Color.White)
            }
            is PulpoEngine.Status.Failed -> Text(status.message, color = Color.Red)
            PulpoEngine.Status.Running -> if (!engine.faceFound) Text("No face detected", color = Color.White)
            PulpoEngine.Status.Idle -> if (!hasCameraPermission) Text("Allow camera access to try on makeup", color = Color.White)
        }
    }
}

@Composable
private fun ModelThumbnail(model: FaceModel, modifier: Modifier) {
    val image by produceState<android.graphics.Bitmap?>(null, model) {
        value = runCatching { FaceModelLoader.image(model) }.getOrNull()
    }
    Box(modifier.background(Color.Gray.copy(alpha = 0.4f))) {
        image?.let {
            Image(it.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
    }
}

private fun Modifier.selectionRing(isSelected: Boolean) =
    if (isSelected) border(3.dp, Color.White, CircleShape) else this
