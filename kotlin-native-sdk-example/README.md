# PulpoAR Native SDK — Kotlin Example

A minimal Jetpack Compose app that runs the native **PulpoModule** makeup engine — no WebView. Try on lipstick, blush or mascara on the live front camera or on a preset model photo.

Gradle downloads the SDK for you on the first build, so there is nothing to download by hand. Everything else is a few hundred lines of Kotlin in four files.

## Requirements

- Android Studio (recent)
- An Android phone with a front camera, Android 7.0 (API 24) or newer. An emulator works too if it has a front camera.

## Run it

Open this folder in Android Studio, plug in your phone, and press **Run**. The first build downloads the SDK into `app/libs/` (gitignored).

## Add it to your own app, step by step

Think of the SDK as a **makeup artist in a box**. You give it a picture of a face and tell it which makeup to use. It gives you back the same picture with makeup on.

### Step 1: Get the box

Download the SDK file, `PulpoModule-release.aar`:

https://assets.pulpoar.com/vision/pulpo-module/builds/NativePulpoModule_v0.0.2/android/PulpoModule-release.aar

### Step 2: Put the box in your app

1. Make a folder called `libs` inside your `app` folder.
2. Put `PulpoModule-release.aar` in it.
3. Open `app/build.gradle.kts` and add these lines inside `dependencies { }`:

```kotlin
// The PulpoAR SDK
implementation(files("libs/PulpoModule-release.aar"))
// The SDK needs these two to work
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
implementation("com.squareup.okhttp3:okhttp:4.12.0")

// For the camera
implementation("androidx.camera:camera-camera2:1.5.0")
implementation("androidx.camera:camera-lifecycle:1.5.0")
```

4. Press **Sync Now** at the top of Android Studio.

### Step 3: Set the Android version

In `app/build.gradle.kts`, make sure `minSdk` is **24** or higher.

### Step 4: Ask to use the camera and the internet

Open `AndroidManifest.xml` and add these two lines above `<application>`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
```

### Step 5: Copy the helper file

This example has a helper file that does all the hard work for you: **`PulpoEngine.kt`**.

Copy it into your project. Copy **`Variants.kt`** too if you want it to get makeup from the PulpoAR API for you. Change the `package` line at the top of each file to your app's package.

You don't need to change anything else inside them.

### Step 6: Make an engine

In the screen where you want the makeup, make one engine. Ask for the camera, and when the user says yes, start the engine:

```kotlin
@Composable
fun TryOnScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val engine = remember { PulpoEngine(context.applicationContext) }

    // 1. Ask for the camera
    var cameraAllowed by remember { mutableStateOf(false) }
    val askForCamera = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
        cameraAllowed = allowed
    }
    LaunchedEffect(Unit) { askForCamera.launch(Manifest.permission.CAMERA) }

    // 2. Start the engine when the user says yes
    LaunchedEffect(cameraAllowed) {
        if (cameraAllowed) engine.start(lifecycleOwner)   // downloads files, turns the camera on
    }
}
```

`start()` does two things for you: wakes up the SDK, and turns on the front camera.

### Step 7: Show the face

The engine gives you a new picture many times every second in `engine.frame`. Show it:

```kotlin
Box(Modifier.fillMaxSize().background(Color.Black)) {
    engine.frame?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Fit,     // fit, not crop, or the face looks too zoomed in
            modifier = Modifier.fillMaxSize(),
        )
    }
}
```

Run the app. You should see yourself.

### Step 8: Put makeup on

Get the recipe (config) for a product, then give it to the engine:

```kotlin
val scope = rememberCoroutineScope()

Button(onClick = {
    scope.launch {
        val lipstick = VariantApi.config("1e17bc16-2ae6-4a3a-9d53-45bdfed4bfe2")
        engine.setProducts(listOf(lipstick))
    }
}) {
    Text("Red lipstick")
}
```

- **More than one product?** Put them all in the list: `setProducts(listOf(lipstick, blush))`.
- **Take makeup off?** Give an empty list: `setProducts(emptyList())`.
- Always give the **full list** of what should be on the face now, not only the new one.

Wait until `engine.status == PulpoEngine.Status.Running` before you put makeup on.

### Step 9 (optional): Use a photo instead of the camera

```kotlin
engine.showPhoto(myBitmap)   // put makeup on a photo
engine.showCamera()          // go back to the camera
```

Makeup you set stays on when you switch.

### That's it!

| You want to… | Call |
|---|---|
| Start everything | `engine.start(lifecycleOwner)` |
| See the result | `engine.frame` |
| Put makeup on | `engine.setProducts(listOf(config1, config2))` |
| Take all makeup off | `engine.setProducts(emptyList())` |
| Use a photo | `engine.showPhoto(bitmap)` |
| Go back to the camera | `engine.showCamera()` |
| Know if it's ready | `engine.status == PulpoEngine.Status.Running` |
| Know if a face is seen | `engine.faceFound` |

`MainActivity.kt` in this example shows all of this together.

## How it works

| File | What it does |
|------|--------------|
| `PulpoEngine.kt` | All SDK calls: loads the face models, runs the camera or a photo through the engine, publishes rendered frames. |
| `Variants.kt` | Fetches a variant's engine config from the PulpoAR API. |
| `Models.kt` | The preset model photos. |
| `MainActivity.kt` | Shows the rendered frame, the camera/model picker and the product swatches. |

The SDK is one class, `com.example.pulpomodule.NativeLib`.

### 1. Initialize once (downloads the face models)

```kotlin
NativeLib.initFaceModuleByUrl(
    context.cacheDir,
    landmarkUrl, faceUrl, qualityUrl, skinSegUrl,
    0, 0, 0, 0,
)
```

This is a `suspend` function. It downloads in the background, then initializes the engine on the thread you called it from.

### 2. Per camera frame

```kotlin
NativeLib.setFrame(bitmap)                     // upright, mirrored, square
NativeLib.analyseFace()                        // false = no face found
NativeLib.apply()
val result = NativeLib.getResultFrameAsMat()   // Bitmap with makeup applied
```

The camera image must be rotated upright, mirrored like a selfie and cropped to a square first. See `toUprightSelfie()` in `PulpoEngine.kt`.

The result is a **square** frame. Show it with `ContentScale.Fit`. Filling a tall phone screen with it crops away about half the width and looks heavily zoomed in.

### Or: a still photo

```kotlin
NativeLib.reset()
NativeLib.setFrame(squarePhoto)
NativeLib.analyseFace()
NativeLib.apply()
val result = NativeLib.getResultFrameAsMat()
```

Unlike the live loop, a photo isn't re-rendered automatically: after changing products, call `apply()` and `getResultFrameAsMat()` again.

### 3. Apply products

A product is the `config.config` object of a PulpoAR variant:

```
GET https://api.pulpoar.com/items/vto_variants/<variant-id>?fields=config
```

Load any textures it references, then hand the array of configs to the engine:

```kotlin
// simplified — see setProducts() in PulpoEngine.kt for the real loop
for (texture in config["texture_ids_to_fetch"]) {
    if (!NativeLib.isTextureLoaded(texture.id)) {
        NativeLib.setTextureEncryptedByUrl(context.cacheDir, texture.url, texture.id)   // suspend
    }
}
NativeLib.setProducts(jsonArrayString, false)   // "[]" removes all makeup
```

The live loop picks up the new products on the next frame.

> **Temporary workaround:** on iOS, `setProducts` with a shorter list did not remove the deselected products, so this example's engine wrapper calls `setProducts("[]", false)` before every new list. Drop it once the SDK replaces the list itself.

## Notes

- **The engine is not thread-safe.** Every `NativeLib` call in this example runs on one single-thread executor, which is also the camera's analyzer thread, so product changes never race the frame loop. The two `suspend` functions (`initFaceModuleByUrl`, `setTextureEncryptedByUrl`) download in the background and then come back to that same thread, so the camera keeps running while they download.
- Call `NativeLib.reset()` when switching between camera and still photos. It clears the engine's face tracking and frame geometry. It is not how you remove products; use `setProducts("[]", …)` for that.
- Product configs and textures download asynchronously, so a quick second tap can finish before the first. Apply only the latest selection. This example uses Compose's `LaunchedEffect(key)`, which cancels the previous coroutine when the selection changes.
- The `.aar` contains native code for 4 CPU types, so the debug APK is large (~180 MB). To ship a smaller app, use an App Bundle, or add `ndk { abiFilters += listOf("arm64-v8a") }` to `defaultConfig`.
