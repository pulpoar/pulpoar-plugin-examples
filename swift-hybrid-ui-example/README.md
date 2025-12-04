# HybridVTODemo - PulpoAR Hybrid Integration

This project demonstrates a **hybrid architecture** for integrating PulpoAR's Virtual Try-On (VTO) plugin, where native SwiftUI components handle the UI while a WebView powers the virtual try-on rendering engine.

## Hybrid Architecture

Unlike standard WebView-only integrations, this approach provides:
- **Native UI Controls** - SwiftUI menus, product lists, and variant selectors
- **WebView VTO Engine** - PulpoAR plugin handles virtual try-on rendering only
- **JavaScript Bridge** - Communication between native code and WebView via `WKScriptMessageHandler`

```
┌─────────────────────────────────────┐
│     Native SwiftUI UI Layer         │
│  (Menus, Products, Variants)        │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│      WKWebView + JS Bridge          │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│    PulpoAR Plugin (VTO Engine)      │
└─────────────────────────────────────┘
```

## Setup

### 1. Camera Permission in Info.plist

```xml
<key>NSCameraUsageDescription</key>
<string>This app requires camera access to take photos and videos</string>
```

### 2. WebView Configuration

The WebView loads the PulpoAR plugin with custom UI disabled:

```swift
let urlString = "https://plugin.pulpoar.com/vto/makeup?custom=true"
```

#### URL Parameters

| Parameter | Description |
|-----------|-------------|
| `custom=true` | Hides built-in UI features (opacity slider, before/after slider, etc.). |
| `catalog=false` | Hides the product catalog UI. |

**Common configurations:**
- Fully native UI: `?custom=true`
- Native product selection only (keep built-in sliders): `?catalog=false`

## Project Structure

```
swift-hybrid-ui-example/
├── PulpoAR/
│   ├── PulpoARView.swift      # Main view orchestrating native UI + WebView
│   ├── PulpoWebView.swift     # WebView wrapper with JS bridge
│   ├── SDK.swift              # PulpoAR SDK methods (setPath, applyVariants, etc.)
│   ├── Types.swift            # Data models (Variant, Product, Category, etc.)
│   ├── Utils.swift            # Event handlers and utilities
│   ├── ProductView.swift      # Native product button component
│   └── VariantView.swift      # Native variant/color selector component
├── ContentView.swift          # App entry view
└── App.swift                  # App entry point
```

## SDK Methods

| Method | Description | Parameters | Example |
|--------|-------------|------------|---------|
| `setPath` | Navigate between VTO experiences | `path`: `'root'` \| `'choose-model'` \| `'take-photo'` \| `'apply-live'` \| `'apply-photo'` | `sdk.setPath("apply-live")` |
| `applyVariants` | Apply makeup variants by slug | `array`: Variant slug array | `sdk.applyVariants(["variant-slug"])` |
| `setImageToApply` | Set image for photo mode | `image`: URL or base64 string | `sdk.setImageToApply("/vto/images/face-model-women-8.png")` |
| `initCamera` | Initialize camera with constraints | `mode`: `{ facingMode: 'user' \| 'environment' }` | `sdk.initCamera(mode: ["facingMode": "user"])` |

### Usage Example

```swift
if let webView = webView {
    let sdk = PulpoARSDK(webView: webView)

    // Switch to live camera mode
    sdk.initCamera(mode: ["facingMode": "user"])
    sdk.setPath("apply-live")

    // Apply a variant
    sdk.applyVariants(["lipstick-red-001"])
}
```

## Events

Events are received from the WebView via `WKScriptMessageHandler`:

| Event | Payload | Description |
|-------|---------|-------------|
| `onReady` | `ApplicationData` | Plugin initialized with variants, products, categories |
| `onPathChange` | `{ referer, path }` | VTO experience path changed |
| `onVariantSelect` | `Variant` | User selected a variant |
| `onAppliedVariantsChange` | `{ variants, triggerVariantId }` | Applied variants updated |
| `onCategorySelect` | `Category` | Category selected |
| `onProductSelect` | `Product` | Product selected |
| `onAddToCart` | `Variant[]` | Add to cart triggered |
| `onError` | `Error` | Error occurred |

### Handling Events

```swift
let events = Events(onReady: { appData in
    // Process variants, products, categories from appData
    let variants = appData.variants.filter { $0.slug != nil }
    // ...
})

PulpoWebViewContainer(
    iframeLoaded: $iframeLoaded,
    webView: $webView,
    onEventsInjected: handleEventsInjected,
    events: events
)
```

## Data Filtering

This implementation filters data to ensure only usable items are displayed:

### Variant Filtering
Only variants with a valid `slug` are included (required for `applyVariants`):

```swift
let distinctVariants = appData.variants
    .filter { $0.slug != nil }
    .unique { $0.id == $1.id }
```

### Category Filtering
Only categories with at least one valid variant are shown:

```swift
let validCategories = appData.categories.filter { category in
    distinctVariants.contains { $0.product.category.id == category.id }
}
```

## Image URLs

Product images use UUIDs that require the API prefix:

```swift
let imageUrl = "https://api.pulpoar.com/assets/\(product.image)"
```
