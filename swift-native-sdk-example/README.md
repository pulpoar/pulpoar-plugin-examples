# PulpoAR Native SDK — Swift Example

A minimal SwiftUI app that runs the native **PulpoModule** makeup engine — no WebView. Try on lipstick, blush or mascara on the live front camera or on a preset model photo.

The SDK comes in as a Swift package from [`pulpoar/pulpoar-ios-sdk`](https://github.com/pulpoar/pulpoar-ios-sdk), so there is nothing to download by hand. Everything else is a few hundred lines of Swift in six files.

## Requirements

- Xcode 16+
- **A physical iPhone running iOS 18.4+.** The SDK ships only an `arm64` device slice and an `x86_64` simulator slice, and the simulator has no camera anyway.

## Run it

Open `swift-native-sdk-example.xcodeproj`, choose your Team under *Signing & Capabilities*, and run on a device. Xcode downloads the SDK automatically the first time.

## Add it to your own app, step by step

Think of the SDK as a **makeup artist in a box**. You give it a picture of a face and tell it which makeup to use. It gives you back the same picture with makeup on.

### Step 1: Find the box

The SDK lives in its own repository, **`https://github.com/pulpoar/pulpoar-ios-sdk`**. There is nothing to copy into your project: Xcode downloads it for you.

### Step 2: Put the box in your app

1. In Xcode, choose **File → Add Package Dependencies…**
2. Paste `https://github.com/pulpoar/pulpoar-ios-sdk` into the search field, set the rule to **Up to Next Major Version** from `0.0.24`, and click **Add Package**.
3. When asked, add **PulpoModule** to your app target.

Xcode downloads the SDK and puts it in your app for you.

### Step 3: Set the iPhone version

In your target's **General** tab, set **Minimum Deployments** to **iOS 18.4**.

### Step 4: Ask to use the camera

Your app must say why it needs the camera. In your target's **Info** tab, add:

- Key: `Privacy - Camera Usage Description`
- Value: `We use the camera to try on makeup.`

### Step 5: Copy the helper file

This example has two helper files that do all the hard work for you: **`PulpoEngine.swift`** and **`PulpoFrameView.swift`**.

Copy both into your project (drag it into Xcode and tick your app target). Copy **`Variants.swift`** too if you want it to get makeup from the PulpoAR API for you.

You don't need to change anything inside them.

### Step 6: Make an engine

In the screen where you want the makeup, make one engine and start it:

```swift
import SwiftUI

struct TryOnView: View {
    @State private var engine = PulpoEngine()

    var body: some View {
        Color.black
            .task { await engine.start() }   // asks for the camera, downloads files, turns the camera on
    }
}
```

`start()` does three things for you: asks to use the camera, wakes up the SDK, and turns on the front camera.

### Step 7: Show the face

The engine makes a new picture many times every second. `PulpoFrameView` shows it:

```swift
struct TryOnView: View {
    @State private var engine = PulpoEngine()

    var body: some View {
        PulpoFrameView(engine: engine)   // the face, with makeup
            .ignoresSafeArea()
            .task { await engine.start() }
    }
}
```

Run the app. You should see yourself.

### Step 8: Put makeup on

Get the recipe (config) for a product, then give it to the engine:

```swift
Button("Red lipstick") {
    Task {
        let lipstick = try await VariantAPI.config(for: "1e17bc16-2ae6-4a3a-9d53-45bdfed4bfe2")
        await engine.setProducts([lipstick])
    }
}
```

- **More than one product?** Put them all in the list: `setProducts([lipstick, blush])`.
- **Take makeup off?** Give an empty list: `setProducts([])`.
- Always give the **full list** of what should be on the face now, not only the new one.

Wait until `engine.status == .running` before you put makeup on.

### Step 9 (optional): Use a photo instead of the camera

```swift
engine.showPhoto(myUIImage)   // put makeup on a photo
engine.showCamera()           // go back to the camera
```

Makeup you set stays on when you switch.

### That's it!

| You want to… | Call |
|---|---|
| Start everything | `await engine.start()` |
| See the result | `PulpoFrameView(engine: engine)` |
| Put makeup on | `await engine.setProducts([config1, config2])` |
| Take all makeup off | `await engine.setProducts([])` |
| Use a photo | `engine.showPhoto(image)` |
| Go back to the camera | `engine.showCamera()` |
| Know if it's ready | `engine.status == .running` |
| Know if a face is seen | `engine.faceFound` |

`ContentView.swift` in this example shows all of this together.

## How it works

| File | What it does |
|------|--------------|
| `PulpoEngine.swift` | All SDK calls: loads the face models, runs the camera or a photo through the engine. |
| `PulpoFrameView.swift` | Shows the rendered frames on screen with Metal. |
| `Variants.swift` | Fetches a variant's engine config from the PulpoAR API. |
| `Models.swift` | The preset model photos. |
| `ContentView.swift` | Shows the rendered frame, the camera/model picker and the product swatches. |

### 1. Initialize once (downloads the face models)

```swift
PulpoModule.initFaceByUrl(
    withLandmarkModel: landmarkUrl, faceUrl: faceUrl,
    qualityUrl: qualityUrl, skinSegUrl: skinSegUrl,
    landmarkOpt: 0, faceOpt: 0, qualityOpt: 0, skinSegOpt: 0
)
```

### 2. Per camera frame

```swift
PulpoModule.setFrameWith(pixelBuffer, targetSize: 1080)
PulpoModule.analyseFace()
PulpoModule.apply()
let buffer = PulpoModule.getResultFrameAsPixelBuffer()   // the frame with makeup, ready for Metal
```

Configure the capture connection to deliver upright, mirrored frames (`videoRotationAngle = 90`, `isVideoMirrored = true`) so the engine sees what the user sees.

Show the result with Metal (see `PulpoFrameView.swift`), not by turning it into a `UIImage`: a 1080×1080 image redrawn 30 times a second is a lot of work for slower iPhones. Use `getResultFrameAsMat()` (a `UIImage`) only for a one-off snapshot.

The result is a **square** frame. Fit it to the screen; filling a tall phone screen with it crops away about half the width and looks heavily zoomed in.

### Or: a still photo

```swift
PulpoModule.reset()
PulpoModule.setFrame(image)          // upright (.up) UIImage
PulpoModule.analyseFace()            // false = no face found
PulpoModule.apply()
let buffer = PulpoModule.getResultFrameAsPixelBuffer()
```

Unlike the live loop, a photo isn't re-rendered automatically: after changing products, call `apply()` and `getResultFrameAsPixelBuffer()` again.

### 3. Apply products

A product is the `config.config` object of a PulpoAR variant:

```
GET https://api.pulpoar.com/items/vto_variants/<variant-id>?fields=config
```

Load any textures it references, then hand the array of configs to the engine:

```swift
for texture in config["texture_ids_to_fetch"] as? [[String: String]] ?? [] {
    if let id = texture["id"], let url = texture["url"], !PulpoModule.isTextureLoaded(id) {
        PulpoModule.setTextureEncryptedByUrl(url, textureId: id)
    }
}
PulpoModule.setProducts(jsonArrayString, isEncrypted: false)   // "[]" removes all makeup
```

The live loop picks up the new products on the next frame.

> **Temporary workaround:** in testing, `setProducts` with a shorter list did not remove the deselected products, so this example's engine wrapper calls `setProducts("[]", …)` before every new list. Drop it once the SDK replaces the list itself.

## Notes

- **The engine is not thread-safe.** Every `PulpoModule` call in this example runs on one serial queue, so product changes never race the frame loop.
- **Give the camera its own queue.** Deliver camera frames on a separate queue and hand each one to the engine queue with `sync`. If the camera delivers straight onto the engine queue, frames keep jumping ahead of product changes whenever processing is slower than the camera (heavy makeup, older iPhones), and makeup changes stall for seconds or never apply.
- Call `PulpoModule.reset()` when switching between camera and still photos. It clears the engine's face tracking and frame geometry. Photos and camera frames have different sizes, so without it makeup renders offset. It is not how you remove products; use `setProducts("[]", …)` for that.
- Product configs and textures download asynchronously, so a quick second tap can finish before the first. Apply only the latest selection. This example uses SwiftUI's `.task(id:)`, which cancels the previous task when the selection changes.
- `setIsUpperEyePolynomialFix(true)` and `setGapCorrector(true)` fix how mascara and eyeliner render. Both are off by default in the SDK.
