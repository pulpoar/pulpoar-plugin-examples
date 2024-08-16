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


| Prop                  | Payload                                                                                           | Description                                                              |
|-----------------------|---------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------|
| `plugin`              | `'vto'`                                                                                           | Specifies the PulpoAR plugin to use.                                     |
| `slug`                | `'makeup'`                                                                                        | Specifies the slug for the PulpoAR plugin.                               |
| `onAddToCart`         | Variant Array	                                                                                    | Callback function triggered when the Add to Cart button is clicked.      |
| `onPathChange`        | {referer:string; path:string}	                                                                    | Callback function triggered when the application path changes.           |
| `onAppliedVariantsChange` | {variants:Variant[],triggerVariantId:string}	                                                     | Callback function triggered when the applied variants change.            |
| `onVariantSelect`     | Variant                                                                                           | Callback function triggered when a variant is selected.                  |
| `onBrandSelect`       | Brand                                                                                             | Callback function triggered when a brand is selected.                    |
| `onCameraMirror`      | {active:boolean}	                                                                                 | Callback function triggered when the camera mirror configuration changes. |
| `onExperienceSelect`  | {type: 'look' \| 'product' \| 'live-camera' \| 'take-photo' \| 'upload-photo' \| 'choose-model' } | Callback function triggered when an experience is selected.              |
| `onLookSelect`        | Look                                                                                              | Callback function triggered when a look is selected.                     |
| `onGoBack`            | undefiend                                                                                         | Callback function triggered when the Go Back button is clicked.          |
| `onCategorySelect`    | Category                                                                                          | Callback function triggered when a category is selected.                 |
| `onProductSelect`     | Product                                                                                           | Callback function triggered when a product is selected.                  |
| `onCurtainSlideEnd`   | {posX:number}	                                                                                    | Callback function triggered when the Before-After slide ends.            |
| `onCurtainSlideStart` | {posX:number}	                                                                                    | Callback function triggered when the Before-After slide starts.          |
| `onOpacitySlideStart` | {percentage:number}	                                                                              | Callback function triggered when the Opacity slide starts.               |
| `onOpacitySlideEnd`   | {percentage:number}	                                                                              | Callback function triggered when the Opacity slide ends.                 |
| `onZoom`              | { type: 'in' \| 'out'; value: number }                                                            | Callback function triggered when the Zoom changes.                          |
| `onCurtainToggle`     | {active:boolean}	                                                                                 | Callback function triggered when the Before-After curtain is toggled.    |
| `onTryNowClick`       | {targetPath:string}                                                                               | Callback function triggered when the Try Now button is clicked.          |
| `onGdprApprove`       | {approved:boolean}                                                                                | Callback function triggered when the GDPR checkbox is clicked.           |
| `onUploadPhoto`       | undefined                                                                                         | Callback function triggered when a photo is uploaded.                    |
| `onModelSelect`       | { image: string; id: number }                                                                     | Callback function triggered when a model is selected.                    |
| `onTakePhotoAgain`    | undefined                                                                                         | Callback function triggered when the Take Again button is clicked.       |
| `onUsePhoto`          | undefined                                                                                         | Callback function triggered when the Use Photo button is clicked.        |
| `onError`             | Error                                                                                             | Callback function triggered when an error occurs.                        |
| `onReady`             | Project data	                                                                                     | Callback function triggered when the PulpoAR plugin is ready.            |
| `onTakePhoto`         | undefined                                                                                         | Callback function triggered when a photo is taken.                       |




## Actions Summary

| Function         | Description                                                                              | Parameters                                                                                           | Example Usage                                                   |
|------------------|------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------|
| `setPath`        | Changes the application's path within the PulpoAR plugin.                                | - `path`: 'root' \| 'choose-model' \| 'take-photo' \| 'apply-live' \| 'apply-photo' <br>  | ```sdk.setPath('take-photo');  ```                      |
| `applyVariants`  | Initializes or updates the variants to be applied in the AR experience.                  | - `array`: Variant slug array <br>                                              | ``` sdk.applyVariants(['variant1', 'variant2']);```     |
| `setImageToApply`| Sets a specific image to be used in the AR experience, either from a URL or base64 string.| - `image`: Image URL or base64 string <br>                                      | ```sdk.setImageToApply('https://example.com/image.jpg');``` |
| `initCamera`     | Initializes the camera with specified constraints such as facing mode.                   | - `mode`: { facingMode: 'user' \| 'environment' } <br>                         | ```sdk.initCamera({ facingMode: 'environment' });```  |

