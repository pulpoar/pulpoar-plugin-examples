# PulpoAR Integration

This module provides an example about how to use PulpoAR plugin and utility functions for interacting with the PulpoAR platform through events and actions using webview.



### 1. Setup Permissions in Info.plist

Add the lines below to give permission for camera in Info.plist.
```html
<key>NSCameraUsageDescription</key>
<string>Your reason for accessing the camera.</string>
``` 

### 2. Define PulpoAR Component in Swift


The PulpoAR controller integrates PulpoAR functionality within a WebView. It handles events and provides methods for interacting with the PulpoAR SDK.

### Example Usage
```swift
import SwiftUI
import WebKit

struct ContentView: View {
    
    var body: some View {
        let listeners = Listeners (
           onAddToCart: { AddToCartPayload in
                print(AddToCartPayload)},
           onPathChange: { PathChangePayload in
             print(PathChangePayload.path)},
           onAppliedVariantsChange: { AppliedVariantsChangePayload in
                print(AppliedVariantsChangePayload)},
           onVariantSelect: { Variant in
                print(Variant)},
           onZoom:{ ZoomPayload in
               print(ZoomPayload)},
           onGoToProduct: { Product in
               print(Product)},
           onModelSelect: { ModelSelectPayload in
               print(ModelSelectPayload)},
           onReady: { ApplicationData in
                print(ApplicationData)}
        )
        let props = PulpoARProps(plugin:"vto",slug: "makeup", listeners: listeners)
        
        PulpoARViewRepresentable(props: props)
            .frame(maxWidth: .infinity)
    }
}
```
### Architecture
This project consists of two main classes and several supporting structures that manage the AR view, handle web interactions, and process various events and data. Below is an overview of the architecture:
1. **PulpoARView**
    -  This class is a `UIViewController` that manages a WebView for displaying AR content.
    -  Communicates with webview with evaluating javascript code.


2. **SDK**
    - The `SDK` class is designed to provide a simple interface for interacting with the PulpoAR SDK through a `WKWebView`. It includes methods for setting paths, applying variants, and handling various events within the AR experience.
```swift
@objc func buttonTapped() { // Setting the path when pressing a button
    let sdk = PulpoARSDK(webView: self.webView)
    sdk.setPath("choose-model")
}
```
3. **Types**

    - This module encompasses all necessary mappers and classes for handling data returned from events triggered through the WebView.

4. **Utils**
    - Includes functions for managing communication between the WebView and native code using JavaScript.

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

