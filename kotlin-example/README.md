# PulpoAR Integration

This module provides an example about how to use PulpoAR plugin and utility functions for interacting with the PulpoAR platform through events and actions using webview.

### 1. Setup Permissions in AndroidManifest.xml

Make sure you have these permissions in your AndroidManifest.xml.

```xml
<uses-feature android:name="android.hardware.camera" />
<uses-feature android:name="android.hardware.camera.autofocus" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.CAMERA" />
```

### 2. Define PulpoAR Fragment in Android

The PulpoAR Fragment integrates PulpoAR functionality within a WebView. It handles events and provides methods for interacting with the PulpoAR SDK.

### Example Usage

Create the fragment's view and initialize the WebView. Inject the WebView object into the actions.

```kotlin
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requestCameraPermission()
        val view = inflater.inflate(R.layout.fragment_web_view, container, false)
        webView = view.findViewById(R.id.webView)
        initializeWebView()
        actions = Actions(webView)

        return view
    }

```

Integrate the necessary events into the initial script.

```kotlin
         webView.settings.apply {
            javaScriptEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
            domStorageEnabled = true
        }
        webView.addJavascriptInterface(SDKInterface(), "AndroidInterface")
        sdk = SDKInterface()

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                webView.evaluateJavascript(
                    sdk.getInitialSDKScript(
                        listOf(
                            Events.onReady,
                            Events.onAddToCart,
                            Events.onPathChange,
                        )
                    )
                ) { data -> Log.i("Js Result:", data) }
            }
        }
        webView.loadUrl("https://plugin.pulpoar.com/vto/makeup")
```

Once completed, it will be ready for use in SDKInterface with the corresponding event name.

```kotlin

    @JavascriptInterface
    fun onReady(payload: String) {
        val jsonObject = JSONObject(payload)
        val data = ProjectData(jsonObject)

        Log.d("PulpoAR", "PulpoAR is ready: $data")
    }

    @JavascriptInterface
    fun onAddToCart(payload: String) {
        val jsonObject = JSONObject(payload)
        val cart = AddToCartPayload(jsonObject)

        Log.d("PulpoAR", "Add to Cart clicked: $cart")
    }

    @JavascriptInterface
    fun onPathChange(payload: String) {
        val jsonObject = JSONObject(payload)
        val path = PathChangePayload(jsonObject)

        Log.d("PulpoAR", "Path changed: ${path}")
    }
```

### Actions

For actions, you can invoke the function within the fragment to utilize the actions inside an activity or another fragment.

```kotlin
  button.setOnClickListener {
            sdk= SDKInterface()
            // Call a method in PulpoARFragment to set the path
            pulpoARFragment.setPath("choose-model")
        }
```

### Component Parameters

| Prop                      | Payload                                                                                           | Description                                                               |
| ------------------------- | ------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------- |
| `plugin`                  | `'vto'`                                                                                           | Specifies the PulpoAR plugin to use.                                      |
| `slug`                    | `'makeup'`                                                                                        | Specifies the slug for the PulpoAR plugin.                                |
| `onAddToCart`             | Variant Array                                                                                     | Callback function triggered when the Add to Cart button is clicked.       |
| `onPathChange`            | {referer:string; path:string}                                                                     | Callback function triggered when the application path changes.            |
| `onAppliedVariantsChange` | {variants:Variant[],triggerVariantId:string}                                                      | Callback function triggered when the applied variants change.             |
| `onVariantSelect`         | Variant                                                                                           | Callback function triggered when a variant is selected.                   |
| `onBrandSelect`           | Brand                                                                                             | Callback function triggered when a brand is selected.                     |
| `onCameraMirror`          | {active:boolean}                                                                                  | Callback function triggered when the camera mirror configuration changes. |
| `onExperienceSelect`      | {type: 'look' \| 'product' \| 'live-camera' \| 'take-photo' \| 'upload-photo' \| 'choose-model' } | Callback function triggered when an experience is selected.               |
| `onLookSelect`            | Look                                                                                              | Callback function triggered when a look is selected.                      |
| `onGoBack`                | undefiend                                                                                         | Callback function triggered when the Go Back button is clicked.           |
| `onCategorySelect`        | Category                                                                                          | Callback function triggered when a category is selected.                  |
| `onProductSelect`         | Product                                                                                           | Callback function triggered when a product is selected.                   |
| `onCurtainSlideEnd`       | {posX:number}                                                                                     | Callback function triggered when the Before-After slide ends.             |
| `onCurtainSlideStart`     | {posX:number}                                                                                     | Callback function triggered when the Before-After slide starts.           |
| `onOpacitySlideStart`     | {percentage:number}                                                                               | Callback function triggered when the Opacity slide starts.                |
| `onOpacitySlideEnd`       | {percentage:number}                                                                               | Callback function triggered when the Opacity slide ends.                  |
| `onZoom`                  | { type: 'in' \| 'out'; value: number }                                                            | Callback function triggered when the Zoom changes.                        |
| `onCurtainToggle`         | {active:boolean}                                                                                  | Callback function triggered when the Before-After curtain is toggled.     |
| `onTryNowClick`           | {targetPath:string}                                                                               | Callback function triggered when the Try Now button is clicked.           |
| `onGdprApprove`           | {approved:boolean}                                                                                | Callback function triggered when the GDPR checkbox is clicked.            |
| `onUploadPhoto`           | undefined                                                                                         | Callback function triggered when a photo is uploaded.                     |
| `onModelSelect`           | { image: string; id: number }                                                                     | Callback function triggered when a model is selected.                     |
| `onTakePhotoAgain`        | undefined                                                                                         | Callback function triggered when the Take Again button is clicked.        |
| `onUsePhoto`              | undefined                                                                                         | Callback function triggered when the Use Photo button is clicked.         |
| `onError`                 | Error                                                                                             | Callback function triggered when an error occurs.                         |
| `onReady`                 | Project data                                                                                      | Callback function triggered when the PulpoAR plugin is ready.             |
| `onTakePhoto`             | undefined                                                                                         | Callback function triggered when a photo is taken.                        |

## Actions Summary

| Function          | Description                                                                                | Parameters                                                                               | Example Usage                                           |
| ----------------- | ------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------- | ------------------------------------------------------- |
| `setPath`         | Changes the application's path within the PulpoAR plugin.                                  | - `path`: 'root' \| 'choose-model' \| 'take-photo' \| 'apply-live' \| 'apply-photo' <br> | `sdk.setPath('take-photo');  `                          |
| `applyVariants`   | Initializes or updates the variants to be applied in the AR experience.                    | - `array`: Variant slug array <br>                                                       | ` sdk.applyVariants(['variant1', 'variant2']);`         |
| `setImageToApply` | Sets a specific image to be used in the AR experience, either from a URL or base64 string. | - `image`: Image URL or base64 string <br>                                               | `sdk.setImageToApply('https://example.com/image.jpg');` |
| `initCamera`      | Initializes the camera with specified constraints such as facing mode.                     | - `mode`: { facingMode: 'user' \| 'environment' } <br>                                   | `sdk.initCamera({ facingMode: 'environment' });`        |

# PulpoAR Kotlin Example - Navigation Architecture

This document explains how the navigation system works in this Android application.

## Overview

The app uses a **custom fragment-based navigation system** with **state preservation** for the WebView. Instead of relying on Android's default navigation components, we implemented a hybrid approach that combines fragment transactions with a custom navigation stack.

## Navigation Components

### 1. Screens/Views

The app has 3 main screen types:

- **Welcome Screen** (`WelcomeFragment`) - Entry point with "Try On" button
- **Product Detail Screen** (`ProductDetailFragment`) - Shows mock product info with "Try On" button
- **WebView Screen** (`PulpoARFragment`) - AR try-on experience

### 2. Fragment Management Strategy

#### **WebView Fragment (Special Treatment)**

- **Created Once**: The WebView fragment is created only once and kept in memory
- **Never Destroyed**: Uses `hide()`/`show()` operations instead of `add()`/`remove()`
- **State Preserved**: Maintains AR session, camera permissions, user progress
- **Tagged**: Stored with tag `"webview_fragment"` for easy retrieval

```kotlin
// WebView is added once and reused
if (pulpoARFragment == null) {
    pulpoARFragment = PulpoARFragment()
    fragmentManager.beginTransaction()
        .add(R.id.fragment_container, pulpoARFragment!!, WEBVIEW_FRAGMENT_TAG)
        .commit()
} else {
    // Reuse existing fragment (preserves state)
    showFragment(pulpoARFragment!!)
}
```

#### **Other Fragments (Standard Treatment)**

- **Created Fresh**: Welcome and Product Detail fragments are recreated as needed
- **Replaced**: Uses `add()`/`remove()` operations
- **No State Preservation**: These are simple UI screens that don't need state preservation

## Navigation Flow

### How Navigation Works

```
Welcome Screen → WebView → Product Detail → WebView → Product Detail → ...
     ↑              ↑           ↑              ↑            ↑
   Fresh         Created      Fresh         Reused       Fresh
  Fragment       Once        Fragment      (State        Fragment
                                          Preserved)
```

### Custom Navigation Stack

Instead of Android's fragment backstack, we maintain our own navigation history:

```kotlin
private val navigationStack = mutableListOf<String>()

// Examples:
["welcome"] → ["welcome", "webview"] → ["welcome", "webview", "product_1"]
```

### Navigation Actions

#### **Forward Navigation**

```kotlin
// Adds to navigation stack
showWelcomeScreen() → navigationStack.add("welcome")
showWebView() → navigationStack.add("webview")
showProductDetail(1) → navigationStack.add("product_1")
```

#### **Back Navigation**

```kotlin
// Removes from stack and navigates to previous
handleBackNavigation() {
    navigationStack.removeLastOrNull()
    val previous = navigationStack.lastOrNull()
    // Navigate to previous screen without adding to stack
}
```

## Fragment Transaction Types

### 1. **Replace Operations** (Welcome ↔ Product Detail)

```kotlin
private fun replaceFragment(fragment: Fragment, tag: String) {
    // 1. Hide WebView (but keep it in memory)
    webViewFragment?.let { transaction.hide(it) }

    // 2. Remove any existing non-WebView fragments
    existingFragments.forEach { transaction.remove(it) }

    // 3. Add new fragment
    transaction.add(R.id.fragment_container, fragment, tag)
}
```

### 2. **Show/Hide Operations** (Anything ↔ WebView)

```kotlin
private fun showFragment(fragment: Fragment) {
    // 1. Hide current fragment
    currentFragment?.let { transaction.hide(it) }

    // 2. Show target fragment (WebView)
    transaction.show(fragment)
}
```

## State Management

### WebView State Preservation

The WebView maintains its state across navigation because:

1. **Fragment Persistence**: Never removed from fragment manager
2. **View Lifecycle**: Properly handles `onPause()`/`onResume()`
3. **Visibility Management**: Uses `onHiddenChanged()` to manage WebView lifecycle
4. **URL Preservation**: WebView URL is not reloaded when showing the fragment

### Navigation Listener Preservation

```kotlin
// Navigation listener is re-set when WebView is shown
override fun onHiddenChanged(hidden: Boolean) {
    if (!hidden) {
        // Re-ensure navigation listener when fragment becomes visible
        pendingNavigationListener?.let { listener ->
            sdk.setNavigationListener(listener)
        }
    }
}
```

## Navigation Examples

### Example 1: Basic Flow

```
User Action: Welcome → "Try On" button
Navigation: showWebView()
Result: WebView created and shown
Stack: ["welcome", "webview"]
```

### Example 2: Product Navigation

```
User Action: WebView → onGoToProduct event triggered
Navigation: navigateToProduct(randomId) → showProductDetail()
Result: New ProductDetailFragment created and shown, WebView hidden
Stack: ["welcome", "webview", "product_2"]
```

### Example 3: Return to WebView

```
User Action: Product Detail → "Try On" button
Navigation: showWebView() → showFragment(existingWebViewFragment)
Result: Existing WebView shown (state preserved), Product Detail removed
Stack: ["welcome", "webview", "product_2", "webview"]
```

### Example 4: Back Navigation

```
User Action: Back button pressed
Navigation: handleBackNavigation()
Process:
  1. Remove "webview" from stack → ["welcome", "webview", "product_2"]
  2. Navigate to "product_2" without adding to stack
Result: Product Detail shown, WebView hidden
Stack: ["welcome", "webview", "product_2"]
```

## Key Benefits

### ✅ **WebView State Preservation**

- User's AR session progress is maintained
- Camera permissions persist
- No reload delays when returning to WebView

### ✅ **Memory Efficiency**

- Only one WebView instance exists
- Other fragments are lightweight and recreated as needed

### ✅ **Predictable Navigation**

- Custom navigation stack provides exact control
- No conflicts with Android's fragment backstack

### ✅ **Smooth User Experience**

- Fast transitions between screens
- No unexpected black screens
- Proper back button behavior

## Technical Notes

### Fragment Lifecycle Management

```kotlin
// WebView Fragment Lifecycle
onCreate() → onCreateView() → onViewCreated() → onResume()
      ↓ (when hidden)
onPause() → onHiddenChanged(hidden=true)
      ↓ (when shown again)
onHiddenChanged(hidden=false) → onResume()
```

### Why Not Use Navigation Component?

We chose a custom solution because:

1. **WebView State**: Navigation Component destroys fragments, losing WebView state
2. **Complex Requirements**: Need to mix persistent (WebView) and temporary (Product) fragments
3. **Event Integration**: AR WebView events need to trigger native navigation
4. **Performance**: Avoiding WebView recreation provides better UX

This architecture provides a robust foundation for AR-enabled e-commerce apps where maintaining WebView state is critical for user experience.
