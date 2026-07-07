# PulpoAR Plugin SDK - HTML/JS Example

HTML/JavaScript integration example demonstrating three implementation patterns for PulpoAR VTO plugin: a landing page with event monitoring and SDK actions, a product detail page (PDP) with virtual try-on overlay, and a modal launcher.

## Features

- **Three page implementations**: Landing Page, PDP, and Modal
- **Event logging**: Real-time monitoring of 27+ SDK events
- **SDK action controls**: Stock management, navigation, catalog controls
- **Clean, declarative code**: Organized with constants, state management, and single-responsibility functions
- **Hot module replacement**: Powered by Vite for instant updates

## Quick Start

```bash
npm install
npm run dev
```

Vite will start at `http://localhost:3000` and automatically open the Landing Page.

**Available Pages:**
- Landing Page: `http://localhost:3000/landing-page.html`
- PDP: `http://localhost:3000/pdp.html`
- Modal: `http://localhost:3000/modal.html`

## Project Structure

```
html-js-example/
├── landing-page.html   # Landing page
├── landing-page.js     # Landing page event handlers & SDK integration
├── pdp.html           # Product detail page with try-on overlay
├── pdp.js             # PDP SDK integration
├── modal.html          # Modal launcher page
├── modal.js            # Modal SDK integration
├── modal.css           # Modal page-specific styles
├── common.css         # Shared base styles and navigation
├── landing-page.css   # Landing page-specific styles
├── pdp.css            # PDP page-specific styles
├── vite.config.js     # Multi-page Vite configuration
├── package.json       # Dependencies
└── README.md          # This file
```

## Page Implementations

### Landing Page

Full SDK experience for testing all features, events, and SDK actions.

**Access**: `http://localhost:3000/landing-page.html`

**Features**:
- Event log panel displaying SDK events
- SDK Actions panel with stock management, navigation control, and catalog operations

**SDK Usage**:
- **Events**: Subscribes to all events (onReady, onVariantSelect, onAddToCart, etc.) and logs them to the panel
- **Actions**: Provides UI controls for `setVariantsStatus`, `setPath`, and `applyVariantsWithCatalog`

**Logic**: When the SDK fires an event, it's captured and displayed in the events panel. Users can test SDK actions through form controls that call the corresponding SDK methods.

---

### PDP (Product Detail Page)

E-commerce product page with virtual try-on overlay.

**Access**: `http://localhost:3000/pdp.html`

**Features**:
- Product layout with lipstick color swatches
- "Try On Virtually" button that toggles the makeup experience overlay

**SDK Usage**:
- **Events**: `onReady` (tracks SDK initialization), `onError` (handles errors)
- **Actions**: `setImageToApply` (sets model image), `applyVariants` (applies selected color), `setPath` (navigates to apply-photo view)

**Logic**:
1. Button enables when SDK is ready and a color is selected
2. Clicking "Try On Virtually" calls `setImageToApply()` with a model face image, `applyVariants()` with selected color, and `setPath('apply-photo')` to display the result
3. Clicking color swatches while try-on is active calls `applyVariants()` to instantly switch lipstick colors
4. Iframe overlay fades in/out using opacity transitions (not display:none, so SDK stays loaded)

---

### Modal

Landing-page-style hero that launches the try-on experience in a full-screen modal overlay.

**Access**: `http://localhost:3000/modal.html`

**Features**:
- Hero section with headline and call-to-action button ("Try It On Now")
- Full-screen modal overlay with the SDK iframe, closable via close button, backdrop click, or Escape key

**SDK Usage**:
- **Events**: `onReady` (tracks SDK initialization), `onError` (handles errors)

**Logic**:
1. Iframe `src` is set once on page load, so the SDK mounts immediately and stays loaded
2. Clicking the CTA only toggles the modal's visibility; the SDK is not reloaded
3. Modal closes via the close button, clicking the backdrop, or pressing Escape — SDK state persists across open/close cycles

---

## SDK Integration Reference

### Events (27 total)

All events are logged on the Landing Page events panel.

**Core**
- `onReady` - Plugin initialized and ready
- `onError` - Error occurred

**Navigation**
- `onPathChange` - Navigation path changed
- `onGoBack` - Back button clicked

**GDPR**
- `onGdprApprove` - GDPR consent approved
- `onGdprLinkClick` - GDPR link clicked

**Photo/Model**
- `onUploadPhoto` - Photo upload initiated
- `onModelSelect` - Model selected for try-on
- `onTakePhoto` - Camera photo taken
- `onUsePhoto` - Photo selected for use
- `onTakePhotoAgain` - Retake photo requested

**Camera**
- `onTryNowClick` - Try now button clicked
- `onCameraMirror` - Camera mirror toggled
- `onCameraSwitch` - Camera switched (front/back)

**UI Controls**
- `onCurtainToggle` - Compare curtain toggled
- `onCurtainSlideStart` - Curtain slide started
- `onCurtainSlideEnd` - Curtain slide ended
- `onOpacitySlideStart` - Opacity slider started
- `onOpacitySlideEnd` - Opacity slider ended
- `onZoom` - Zoom level changed

**Experience**
- `onExperienceSelect` - Experience type selected

**Catalog**
- `onLookSelect` - Look selected
- `onBrandSelect` - Brand selected
- `onCategorySelect` - Category selected
- `onProductSelect` - Product selected
- `onVariantSelect` - Product variant selected
- `onAppliedVariantsChange` - Applied variants changed

**Commerce**
- `onAddToCart` - Add to cart clicked
- `onGoToProduct` - Go to product clicked

### Actions (9 total)

**`applyVariants(slugs)`**

Initialize variants by slugs.

```javascript
pulpoar.applyVariants(['variant-123', 'variant-456']);
pulpoar.applyVariants([]);  // Reset
```

**`applyVariantsWithCatalog(slugs)`**

Initialize variants by slugs and automatically select their corresponding products, category, and brand in the catalog hierarchy. Only works when catalog is initialized.

```javascript
pulpoar.applyVariantsWithCatalog(['variant-123', 'variant-456']);
pulpoar.applyVariantsWithCatalog([]);  // Reset catalog
```

**`addVariantsWithCatalog(slugs)`**

Adds variants by slugs to existing catalog selections without overriding them. Merges new variants with current selections, filtering out variants from the same category. Only works when catalog is initialized.

```javascript
pulpoar.addVariantsWithCatalog(['variant-789']);
```

**`setImageToApply(imageUrl)`**

Change the image to apply (URL or base64).

```javascript
pulpoar.setImageToApply('https://example.com/image.jpg');
pulpoar.setImageToApply('data:image/png;base64,...');
```

**`setPath(path)`**

Navigate to specific pages within the plugin.

Paths: `root`, `choose-model`, `take-photo`, `apply-live`, `apply-photo`

```javascript
pulpoar.setPath('root');          // Home page
pulpoar.setPath('choose-model');  // Model selection
pulpoar.setPath('take-photo');    // Photo capture
pulpoar.setPath('apply-live');    // Live camera
pulpoar.setPath('apply-photo');   // Apply photo (requires photo to be selected or set via setImageToApply first)
```

**`initCamera(params)`**

Initialize camera with constraints.

```javascript
pulpoar.initCamera({ facingMode: 'user' });        // Front camera
pulpoar.initCamera({ facingMode: 'environment' }); // Back camera
```

**`setCameraFeedStatus(status)`**

Controls the camera feed status for live VTO experiences. Use to pause/resume camera feed without destroying the component.

```javascript
pulpoar.setCameraFeedStatus('enabled');
pulpoar.setCameraFeedStatus('disabled');
```

**`setVariantsStatus(params)`**

Set the status (enabled/disabled) of specified variants with optional custom values. Only specified variants are affected, leaving all others unchanged. Only works when catalog is initialized.

Parameters:
- `variants`: Array of variant objects with `slug` (required) and optional custom fields
  - `price`: Custom price string
  - `name`: Custom variant name
  - `image`: Custom image URL
  - `thumbnail_color`: Custom color for thumbnail
  - `web_link`: Custom product link
- `isDisabled`: Boolean (default: true) - true for out of stock, false for available
- `outOfStockText`: String shown when variant is disabled

```javascript
pulpoar.setVariantsStatus({
  variants: [
    {
      slug: 'variant-123',
      price: '$29.99',
      name: 'Ruby Red Lipstick',
      image: 'https://example.com/lipstick.jpg'
    }
  ],
  isDisabled: true,
  outOfStockText: 'Out of stock'
});

// Enable variants
pulpoar.setVariantsStatus({
  variants: [{ slug: 'variant-456' }],
  isDisabled: false
});
```
