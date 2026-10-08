import java.net.URL

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.pulpoar.nativesdkexample"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pulpoar.nativesdkexample"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Optional: build for one CPU type only, for a much smaller APK, e.g.
        // ./gradlew assembleDebug -PabiFilters=arm64-v8a
        (findProperty("abiFilters") as String?)?.let { ndk { abiFilters += it.split(",") } }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

// The PulpoAR SDK. Downloaded into app/libs the first time you build.
val pulpoSdk = file("libs/PulpoModule-release.aar")
if (!pulpoSdk.exists()) {
    pulpoSdk.parentFile.mkdirs()
    URL("https://assets.pulpoar.com/vision/pulpo-module/builds/NativePulpoModule_v0.0.2/android/PulpoModule-release.aar")
        .openStream().use { input -> pulpoSdk.outputStream().use { input.copyTo(it) } }
}

dependencies {
    implementation(files(pulpoSdk))
    // The SDK needs these two at runtime.
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Camera
    implementation("androidx.camera:camera-camera2:1.5.0")
    implementation("androidx.camera:camera-lifecycle:1.5.0")

    // UI
    implementation(platform("androidx.compose:compose-bom:2025.05.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")
}
