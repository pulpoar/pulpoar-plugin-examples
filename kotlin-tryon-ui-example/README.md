# PulpoAR Try-On UI — Kotlin Example

A small Jetpack Compose shop that opens PulpoAR's ready-made virtual try-on in the three places apps usually put it: full screen from a product page, as a pushed screen, and in its own tab. Add to Cart puts the shades in the app's own cart.

The try-on is **PulpoTryOnUI**: every screen of PulpoAR's web try-on (start, camera consent, live camera, take or upload a photo, model photos, looks and products, before/after), built natively and styled by your project's PulpoAR Studio theme. You write no try-on code: one composable does it all.

It comes from the PulpoAR Maven repository, so there is nothing to download by hand. Releases, checksums and the verification script are in [`pulpoar/pulpoar-android-sdk`](https://github.com/pulpoar/pulpoar-android-sdk).

> **Pre-release SDK.** This example uses `0.1.0-rc.1`. Its engine is not yet 16 KB page-size aligned, so apps built on it can't be uploaded to Google Play as new apps targeting Android 15 or newer. It's fine for trying the integration. The version here moves to the first final release when there is one.

## Requirements

- Android Studio with the Android SDK 36 platform, and JDK 17 (the one bundled with Android Studio works)
- Android 7.0 (API 24) or newer
- **A physical Android phone is recommended.** The SDK ships `arm64-v8a`, `armeabi-v7a`, `x86` and `x86_64` libraries, but an emulator needs a webcam set up for the camera.
- An agreement with PulpoAR: the SDK is proprietary.

## Run it

1. Open this folder in Android Studio.
2. Let Gradle sync. The first sync downloads the SDK from the PulpoAR repository.
3. Choose your phone and run `app`.

## Add it to your own app, step by step

### Step 1: Add the repository

In `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://assets.pulpoar.com/vision/pulpo-sdk/android/maven")
            content { includeGroup("com.pulpoar") }
        }
    }
}
```

### Step 2: Add the dependency

In your app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.pulpoar:tryon-ui:0.1.0-rc.1")
}
```

`tryon-ui` is the only one you need: it brings the Try-On Kit and the engine with it. Always pin an exact version; the repository has no "latest" index. Pick the newest from the [releases](https://github.com/pulpoar/pulpoar-android-sdk/releases).

### Step 3: Set the Android version and Compose

Your module needs `minSdk = 24` or higher, and the Kotlin Compose compiler plugin (`org.jetbrains.kotlin.plugin.compose`) with `buildFeatures { compose = true }`.

### Step 4: Camera permission

Nothing to add: the SDK merges `INTERNET`, `ACCESS_NETWORK_STATE` and `CAMERA` into your manifest, and the try-on asks for the camera itself when it needs it. Photo upload uses the system photo picker, which needs no permission.

### Step 5: Configure once

In your `Application` class, registered in `AndroidManifest.xml` as `android:name`:

```kotlin
import com.pulpoar.tryon.PulpoAnalyticsConsent
import com.pulpoar.tryon.PulpoTryOn
import com.pulpoar.tryon.PulpoTryOnConfiguration

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PulpoTryOn.configure(this, PulpoTryOnConfiguration(analyticsConsent = PulpoAnalyticsConsent.PENDING))
    }
}
```

Nothing is reported while consent is `PENDING`. When the user answers your app's consent prompt, pass their answer:

```kotlin
PulpoTryOn.analyticsConsent = if (userAccepted) PulpoAnalyticsConsent.GRANTED else PulpoAnalyticsConsent.DENIED
```

If your app already knows the answer at launch, pass it to `configure` instead. Never pass `GRANTED` without the user's consent. In this example, the switch in the Cart tab plays the consent prompt.

### Step 6: Show the try-on

Anywhere in your Compose UI:

```kotlin
import com.pulpoar.tryon.ui.PulpoTryOnView

PulpoTryOnView(projectSlug = "your-project", modifier = Modifier.fillMaxSize())
```

`projectSlug` is your project's slug from PulpoAR Studio. The view loads your catalog and theme by itself.

### Step 7: Connect it to your app

Everything is optional:

```kotlin
PulpoTryOnView(
    projectSlug = "your-project",
    modifier = Modifier.fillMaxSize(),
    configuration = PulpoTryOnUIConfiguration(
        onAddToCart = { variants -> cart.add(variants) },        // without it, Add to Cart opens the product's web page
        onGoToProduct = { variant -> openProductPage(variant) }, // the Details button
        onClose = { isTryOnOpen = false },                       // the try-on's close button; dismiss it here
        customizeTheme = { theme ->
            theme.catalog.withGoToProduct = false                // anything in your Studio theme can be changed here
        },
    ),
)
```

Open it on a product with `initialSelection = PulpoTryOnInitialSelection.Product("product-slug")`, on shades with `.Variants(listOf("variant-slug"))`, or on a look with `.Look("look-id")`. Force a language with `locale = "fr"` (en, tr, pt, it, fr, ru); otherwise it follows the theme and the device.

### Views-based apps

Host the composable in a `ComposeView`:

```kotlin
findViewById<ComposeView>(R.id.tryOn).setContent {
    PulpoTryOnView(projectSlug = "your-project", configuration = PulpoTryOnUIConfiguration(onClose = { finish() }))
}
```

### That's it!

| You want to… | Use |
|---|---|
| Set up the SDK | `PulpoTryOn.configure(this, …)` once in `Application.onCreate` |
| Pass analytics consent | `PulpoTryOn.analyticsConsent = PulpoAnalyticsConsent.GRANTED` (or `DENIED`) |
| Show the try-on | `PulpoTryOnView(projectSlug = …)` |
| Get Add to Cart | `onAddToCart` |
| Get the Details button | `onGoToProduct` |
| Close it | `onClose` |
| Start on a product, shades or a look | `initialSelection` |
| Change the theme | `customizeTheme` |

## Good to know

- **R8 / minified release builds:** the SDK packages its own R8 rules, so you add none. The pre-release hasn't been run through a minified build on a device yet, so test your release build before shipping.
- **One try-on at a time:** the engine is shared, so the visible try-on uses it.
- **Hidden and shown again:** when the try-on is in a tab you leave, it keeps its state and comes back where it was.

## How it works

| File | What it does |
|------|--------------|
| `ExampleApplication.kt` | Configures the SDK once. |
| `MainActivity.kt` | The tabs, the product page (full screen and pushed try-on) and the cart page. |
| `Cart.kt` | A stand-in for your app's cart, filled by `onAddToCart`. |

Want your own screens instead? Use `com.pulpoar:tryon-kit` instead of `tryon-ui`: it's the same try-on without the UI (camera, catalog, makeup), for building your own.
