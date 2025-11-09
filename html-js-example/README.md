# PulpoAR Plugin SDK - HTML/JS Example

HTML/JavaScript integration example demonstrating PulpoAR VTO plugin with event monitoring and SDK actions.

## Features

- Event logging (27 SDK events)
- SDK action controls (stock management, navigation, catalog)
- Hot module replacement with Vite

## Quick Start

```bash
npm install
npm run dev
```

Vite will start at `http://localhost:3000` and automatically open your browser.

## Project Structure

```
html-js-example/
├── index.html          # Main page with iframe
├── index.js            # Event handlers & SDK integration
├── styles.css          # Responsive styling
├── vite.config.js      # Vite configuration
├── package.json        # Dependencies
└── README.md           # This file
```

## SDK Integration

### Events (27 total)

All events are logged in real-time to the events panel.

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

**`updateCatalogWithCustomValues(params)`**

Update catalog variants with custom values. Only variants in array will be available, all others marked as out of stock. Only works when catalog is initialized.

```javascript
pulpoar.updateCatalogWithCustomValues({
  variants: [
    {
      slug: 'variant-123',
      price: '$29.99',
      name: 'Custom Name',
      image: 'https://example.com/image.jpg'
    }
  ],
  outOfStockText: 'Sold out'
});
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

