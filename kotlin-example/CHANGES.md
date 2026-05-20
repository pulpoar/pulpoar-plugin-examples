# kotlin-example — onGoToProduct integration

## Why

After the plugin-side fix ([pulpoar/plugin#688](https://github.com/pulpoar/plugin/pull/688)),
`isInMobileApp()` correctly detects Android WebView via the UA `wv` token. The
plugin therefore no longer calls `window.open(_blank)` from `goToProduct()`
inside an Android WebView host — it only emits the `onGoToProduct` event.

This means the host app **must** subscribe to that event and open the product
page natively. Otherwise tapping "Go to product" is a silent no-op and the
makeup state is preserved (good), but the user can't reach the product page
(bad).

This branch adds a working reference implementation.

## What changed

| File | Change |
|---|---|
| `app/src/main/java/com/pulpolabs/kotlin_example/ProductDetailActivity.kt` (new) | Activity hosting a second WebView. Reads `EXTRA_URL`, loads it. |
| `app/src/main/res/layout/activity_product_detail.xml` (new) | Toolbar + WebView layout for the product detail screen. |
| `app/src/main/AndroidManifest.xml` | Registered `ProductDetailActivity` (`exported=false`) with an explicit product-detail no-actionbar theme. |
| `app/src/main/java/com/pulpolabs/kotlin_example/SDKInterface.kt` | Constructor now accepts an optional `onGoToProductHandler: ((String) -> Unit)?`. `onGoToProduct(payload)` parses the variant, extracts `web_link`, and invokes the handler. |
| `app/src/main/java/com/pulpolabs/kotlin_example/PulpoARFragment.kt` | Builds a handler that launches `ProductDetailActivity`, passes it to `SDKInterface`, subscribes `Events.onGoToProduct` in `getInitialSDKScript(...)`. |
| `app/src/main/res/values/themes.xml` | Adds `AppTheme.ProductDetail`, a real no-actionbar AppCompat theme for the product screen. |

## Flow

1. User applies a variant in the makeup WebView.
2. User taps "Go to product".
3. Plugin emits `onGoToProduct` (and skips `window.open` because
   `isAndroidWebView()` is true).
4. SDK forwards the payload to `AndroidInterface.onGoToProduct(jsonString)`.
5. `SDKInterface` extracts `web_link` defensively from the payload and calls
   the handler. It does not require the payload to match the full `Variant`
   model.
6. Handler posts to the WebView's thread (`webView.post { ... }`) to ensure
   it runs on the UI thread, then `startActivity(ProductDetailActivity)`
   with the URL in extras.
7. `ProductDetailActivity` loads the URL in its own WebView.
8. User presses back → `MainActivity` returns to the foreground. The
   makeup `PulpoARFragment` and its WebView were never destroyed, so the
   in-memory plugin state (applied variants, WASM module, camera stream)
   is preserved.

## Crash fix during implementation

First build crashed in `ProductDetailActivity.onCreate` with:

```
IllegalStateException: This Activity already has an action bar supplied by the window decor.
Do not request Window.FEATURE_SUPPORT_ACTION_BAR and set windowActionBar to false
in your theme to use a Toolbar instead.
```

Root cause: the app's `AppTheme.NoActionBar` declares `parent="Theme.AppCompat"`,
which provides a decor ActionBar despite the misleading style name. Calling
`setSupportActionBar(toolbar)` on top installs a second one and throws.

Fix: removed `setSupportActionBar(...)` and `supportActionBar?.setDisplayHomeAsUpEnabled(true)`,
then registered `ProductDetailActivity` with an explicit no-actionbar theme.
The toolbar is used as a plain view — title and navigation icon set directly.
App theme left untouched to avoid regressing MainActivity.

## Event payload hardening

`onGoToProduct` should not crash or no-op just because the emitted payload does
not exactly match the app's strict `Variant` model. The handler now searches the
payload recursively for `web_link`, so both a full variant payload and a nested
event wrapper can open the product page.

## Testing

1. Install `app-debug-product-detail.apk` on a device.
2. Tap "Set Path" to enter the catalog (or navigate naturally).
3. Apply a variant.
4. Tap "Go to product" on a variant.
5. Expect `ProductDetailActivity` to open with the product URL.
6. Press back.
7. Expect to return to the makeup experience with state intact (applied
   variant still on face, no reload).

## Note for partners

The same pattern applies to any Android host:

- Subscribe to `onGoToProduct` from the SDK.
- Parse the payload's `web_link`.
- Open it in a **separate Activity** or a Chrome Custom Tab —
  **never** in the same WebView, and **never** by replacing the makeup
  fragment, because that destroys the WebView and resets plugin state.
