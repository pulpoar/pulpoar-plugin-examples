# PulpoAR Try-On UI — Swift Example

A small SwiftUI shop that opens PulpoAR's ready-made virtual try-on in the three places apps usually put it: full screen from a product page, as a pushed screen, and in its own tab. Add to Cart puts the shades in the app's own cart.

The try-on is **PulpoTryOnUI**: every screen of PulpoAR's web try-on (start, camera consent, live camera, take or upload a photo, model photos, looks and products, before/after), built natively and styled by your project's PulpoAR Studio theme. You write no try-on code: one view does it all.

It comes as a Swift package from [`pulpoar/pulpoar-ios-sdk`](https://github.com/pulpoar/pulpoar-ios-sdk), so there is nothing to download by hand.

## Requirements

- Xcode 26+ (the SDK is built with Swift 6.3)
- **A physical iPhone running iOS 15.6 or later.** The simulator has no camera, and the SDK has no Apple Silicon simulator slice yet.
- An agreement with PulpoAR: the SDK is proprietary.

## Run it

1. Turn on **Developer Mode** on your iPhone (Settings → Privacy & Security → Developer Mode).
2. Open `swift-tryon-ui-example.xcodeproj`.
3. Under *Signing & Capabilities*, choose your Team and change the Bundle Identifier to one of your own, such as `com.yourcompany.tryon-example`. The default belongs to PulpoAR's account and can't be signed by yours.
4. Choose your iPhone and run. Xcode downloads the SDK automatically the first time.

## Add it to your own app, step by step

### Step 1: Add the package

1. In Xcode, choose **File → Add Package Dependencies…**
2. Paste `https://github.com/pulpoar/pulpoar-ios-sdk`, set the rule to **Up to Next Major Version** from `0.1.0`, and click **Add Package**.
3. When asked, add **PulpoTryOnUI** to your app target. That's the only one you need: it brings the Try-On Kit and the engine with it.

### Step 2: Set the iPhone version

In your target's **General** tab, set **Minimum Deployments** to **iOS 15.6** or higher.

### Step 3: Ask to use the camera

In your target's **Info** tab, add:

- Key: `Privacy - Camera Usage Description`
- Value: `The camera shows you wearing the products.`

Photo upload uses the system photo picker, which needs no permission.

### Step 4: Configure once

When your app starts, before any try-on is shown:

```swift
import PulpoTryOnKit

PulpoTryOn.configure(PulpoTryOnConfiguration(analyticsConsent: .pending))
```

Nothing is reported while consent is `.pending`. When the user answers your app's consent prompt, pass their answer:

```swift
PulpoTryOn.analyticsConsent = userAccepted ? .granted : .denied
```

If your app already knows the answer at launch, pass it to `configure` instead. Never pass `.granted` without the user's consent. In this example, the switch in the Cart tab plays the consent prompt.

### Step 5: Show the try-on

Anywhere in your SwiftUI views:

```swift
import PulpoTryOnUI

PulpoTryOnView(projectSlug: "your-project")
```

`projectSlug` is your project's slug from PulpoAR Studio. The view loads your catalog and theme by itself.

### Step 6: Connect it to your app

Everything is optional:

```swift
PulpoTryOnView(projectSlug: "your-project", configuration: .init(
    onAddToCart: { variants in cart.add(variants) },        // without it, Add to Cart opens the product's web page
    onGoToProduct: { variant in openProductPage(variant) }, // the Details button
    onClose: { isTryOnPresented = false },                  // the try-on's close button; dismiss it here
    customizeTheme: { theme in
        theme.catalog.withAddToCart = true                  // anything in your Studio theme can be changed here
    }
))
```

Open it on a product with `initialSelection: .product("product-slug")`, on shades with `.variants(["variant-slug"])`, or on a look with `.look("look-id")`. Force a language with `locale: "fr"` (en, tr, pt, it, fr, ru); otherwise it follows the theme and the device.

### UIKit

```swift
let tryOn = PulpoTryOnViewController(projectSlug: "your-project", configuration: .init(
    onClose: { [weak self] in self?.dismiss(animated: true) }
))
tryOn.modalPresentationStyle = .fullScreen
present(tryOn, animated: true)
```

### That's it!

| You want to… | Use |
|---|---|
| Set up the SDK | `PulpoTryOn.configure(…)` once at launch |
| Pass analytics consent | `PulpoTryOn.analyticsConsent = .granted` (or `.denied`) |
| Show the try-on | `PulpoTryOnView(projectSlug:)` (UIKit: `PulpoTryOnViewController`) |
| Get Add to Cart | `onAddToCart` |
| Get the Details button | `onGoToProduct` |
| Close it | `onClose` |
| Start on a product, shades or a look | `initialSelection` |
| Change the theme | `customizeTheme` |

## Good to know

- **Hidden and shown again:** when the try-on is in a tab you leave, or covered by another screen, it lets go of the camera and comes back where it was.
- **Landscape:** the try-on supports it (catalog on the right). This example allows it; lock your app to portrait if you prefer.
- **One try-on at a time:** the engine is shared, so the visible try-on uses it.

## How it works

| File | What it does |
|------|--------------|
| `App.swift` | Configures the SDK once and creates the cart. |
| `ContentView.swift` | The tabs, the product page (full screen and pushed try-on) and the cart page. |
| `Cart.swift` | A stand-in for your app's cart, filled by `onAddToCart`. |

Want your own screens instead? Add **PulpoTryOnKit** instead of PulpoTryOnUI: it's the same try-on without the UI (camera, catalog, makeup), for building your own.
