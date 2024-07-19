# PulpoAR Integration

This module provides React components and utility functions for integrating with the PulpoAR platform using react-native-webview.
## Running This Example

Make sure `pod` is installed on your computer.

```bash
npm install && cd ios && pod install && cd ..

#for IOS
npm run ios 

#for android
npm run android
```
## Required Integration Steps
### IOS
Add the lines below to give permission for camera in Info.plist.
```html
<key>NSCameraUsageDescription</key>
<string>Your reason for accessing the camera.</string>
```

### Android

Add the line below to give permission for camera in AndroidManifest.xml for Android:


```html
<uses-permission android:name="android.permission.CAMERA" />
```   

## Usage

The PulpoAR component is a React component that integrates PulpoAR functionality within a WebView. It handles events and provides methods for interacting with the PulpoAR SDK.


### Example Usage

First, copy the `pulpoar` folder into an appropriate location within your project.

```tsx
import React, { useRef } from 'react';
import { PulpoAR } from './pulpoar';

const MyPulpoARComponent = () => {
  const actionsRef = useRef(null);

  return (
    <SafeAreaView style=style={{flex: 1}>
      <PulpoAR
        plugin="vto"
        slug="makeup"
        actionsRef={actionsRef}
        onReady={(data) => console.log("PulpoAR is ready", data)}
        onAddToCart={(data) => console.log('Add to Cart clicked', data)}
        onPathChange={(data) => console.log('Path changed', data)}
        onAppliedVariantsChange={(data) => console.log('Applied variants changed', data)}
      />
      <Button
        title="Set Path"
        onPress={() => {
          actionsRef.current?.setPath('choose-model');
        }}
      />
    </SafeAreaView>
  );
};

export default MyPulpoARComponent;

```

### Component Props

Props

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

| Function         | Description                                                                              | Parameters                                                                                                    | Example Usage                                                         |
|------------------|------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------|
| `setPath`        | Changes the application's path within the PulpoAR plugin.                                | - `path`: 'root' \| 'choose-model' \| 'take-photo' \| 'apply-live' \| 'apply-photo' <br> - `ref`: WebView ref | ```setPath('take-photo', webViewRef);  ```                         |
| `applyVariants`  | Initializes or updates the variants to be applied in the AR experience.                  | - `array`: Variant slug array <br> - `ref`: WebView ref                                                       | ``` applyVariants(['variant1', 'variant2'], webViewRef);```        |
| `setImageToApply`| Sets a specific image to be used in the AR experience, either from a URL or base64 string.| - `image`: Image URL or base64 string <br> - `ref`: WebView ref                                               | ```setImageToApply('https://example.com/image.jpg', webViewRef);``` |
| `initCamera`     | Initializes the camera with specified constraints such as facing mode.                   | - `mode`: { facingMode: 'user' \| 'environment' } <br> - `ref`: WebView ref                                   | ```initCamera({ facingMode: 'environment' }, webViewRef);``` |

