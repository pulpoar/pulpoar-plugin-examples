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

The app uses **standard Android fragment navigation** with **slide animations** and **proper backstack management**, while preserving WebView state. This approach mimics partner integrations that provide native Android navigation feel with AR session persistence.

## Navigation Strategy

### **Standard Android Navigation + State Preservation**

- **Visual Experience**: Full Android navigation animations (slide in/out transitions)
- **Backstack Management**: Uses Android's fragment backstack for proper navigation history
- **WebView Persistence**: Preserves AR session state by reusing fragment instances
- **Partner Integration Feel**: Matches the look and behavior of production partner apps

### Navigation Screens

- **Welcome Screen** (`WelcomeFragment`) - Entry point with "Try On" button
- **Product Detail Screen** (`ProductDetailFragment`) - Shows mock product info with "Try On" button
- **WebView Screen** (`PulpoARFragment`) - AR try-on experience (state preserved)

## Navigation Flow & Animations

### **Flow 1: Welcome → WebView**

```
User Action: Welcome screen → "Try On" button
Animation: Slide in from left (standard Android animation)
Backstack: [Welcome] → [Welcome, WebView]
WebView State: Created fresh (first time)
```

### **Flow 2: WebView → Product Detail**

```
User Action: WebView → onGoToProduct event triggered
Animation: Slide in from left (new screen push)
Backstack: [Welcome, WebView] → [Welcome, WebView, Product]
WebView State: Preserved in background
```

### **Flow 3: Product Detail → WebView (Key Feature)**

```
User Action: Product Detail → "Try On" button
Behavior: Pop Product Detail (no recreation of WebView)
Animation: Slide out to right (pop) + Slide in from left (WebView)
Backstack: [Welcome, WebView, Product] → [Welcome, WebView]
WebView State: ✅ PRESERVED - User returns to exact same AR session
```

### **Flow 4: Back Navigation**

```
User Action: Back button
Behavior: Standard Android back navigation
Animation: Slide out to right (pop animation)
Backstack: Removes top fragment from stack
```

## Technical Implementation

### **Fragment Transaction Strategy**

#### **All Screens Use Standard Navigation**

```kotlin
fragmentManager.beginTransaction()
    .setCustomAnimations(
        android.R.anim.slide_in_left,   // Enter animation
        android.R.anim.slide_out_right, // Exit animation
        android.R.anim.slide_in_left,   // Pop enter animation
        android.R.anim.slide_out_right  // Pop exit animation
    )
    .setReorderingAllowed(true)
    .replace(R.id.fragment_container, fragment, tag)
    .addToBackStack(backStackName)
    .commit()
```

#### **WebView State Preservation**

The WebView fragment instance is created only once and always reused:

```kotlin
// Create once, reuse many times
if (pulpoARFragment == null) {
    pulpoARFragment = PulpoARFragment()  // Create once
    pulpoARFragment?.setNavigationListener(this)
} else {
    // Reuse existing instance - preserves state
}

// Always use the same instance in transactions
.replace(R.id.fragment_container, pulpoARFragment!!, "webview_fragment")
```

### **Navigation Methods**

#### **Push Product Detail**

```kotlin
override fun navigateToProduct(productId: Int) {
    val productFragment = ProductDetailFragment.newInstance(productId)
    productFragment.setProductDetailListener(this)
    fragmentManager.beginTransaction()
        .setCustomAnimations(
            android.R.anim.slide_in_left,
            android.R.anim.slide_out_right,
            android.R.anim.slide_in_left,
            android.R.anim.slide_out_right
        )
        .setReorderingAllowed(true)
        .replace(R.id.fragment_container, productFragment, "product_detail_fragment")
        .addToBackStack("product_detail_$productId")
        .commit()
}
```

#### **Return to WebView (Pop Only, Never Recreate)**

```kotlin
override fun onProductTryOnClicked() {
    // Only pop the backstack to reveal the existing WebView
    if (fragmentManager.backStackEntryCount > 0) {
        fragmentManager.popBackStack()
    } else {
        // This should never happen; WebView should always be underneath
        Log.e("MainActivity", "No backstack entries found! Navigation error.")
    }
}
```

> **Note:** The WebView is never recreated after the first launch. Returning from Product Detail always pops the backstack, revealing the preserved AR session.

## Navigation Examples

### **Example 1: Complete User Journey**

```
Start: Welcome Screen
↓ [Try On] - slides in →
WebView (AR session starts)
↓ [onGoToProduct] - slides in →
Product Detail (WebView hidden but preserved)
↓ [Try On] - pops out, slides in →
WebView (SAME AR session continues) ✅
```

### **Example 2: Complex Navigation**

```
Welcome → WebView → Product A → WebView → Product B → Back → Back → Welcome

Backstack progression:
[Welcome]
[Welcome, WebView]
[Welcome, WebView, Product A]
[Welcome, WebView]              // WebView state preserved
[Welcome, WebView, Product B]
[Welcome, WebView]              // Back pressed
[Welcome]                       // Back pressed
[]                              // Back pressed (app exits)
```

## Key Benefits

### ✅ **Native Android Feel**

- Standard slide animations match system behavior
- Proper backstack management
- Familiar navigation patterns for users

### ✅ **WebView State Preservation**

- AR session continues seamlessly
- Camera permissions maintained
- User progress never lost

### ✅ **Partner Integration Compatible**

- Matches behavior of production partner apps
- Professional UX with smooth transitions
- No unexpected behaviors or glitches

### ✅ **Performance Optimized**

- WebView instance reused (no recreation overhead)
- Fast transitions between screens
- Memory efficient fragment management

## Animation Details

### **Built-in Android Animations Used:**

- `android.R.anim.slide_in_left` - New screen enters from left
- `android.R.anim.slide_out_right` - Current screen exits to right
- `android.R.anim.slide_in_left` - Previous screen returns from right (pop enter)
- `android.R.anim.slide_out_right` - Current screen exits to right (pop exit)

### **Animation Behavior:**

- **Forward Navigation**: Slides in from left (push feel)
- **Back Navigation**: Slides out to right (pop feel)
- **System Consistent**: Matches Android's standard navigation animations

## Troubleshooting

- **WebView is recreated or AR session is lost:**
  - Ensure you never call `showWebView()` when returning from Product Detail. Only pop the backstack.
  - The WebView fragment should be created once and reused for all navigation.
- **Back navigation does not work as expected:**
  - Check that all navigation to Product Detail uses `.addToBackStack()`.
  - Ensure you are not clearing the backstack or replacing the WebView fragment unnecessarily.
- **Black screen or empty view after navigation:**
  - This usually means the fragment stack is not managed correctly. Review the navigation flow and make sure the WebView is always present underneath Product Detail.

This architecture provides the exact navigation experience found in professional AR e-commerce applications, combining native Android navigation patterns with the technical requirements of AR session persistence.
