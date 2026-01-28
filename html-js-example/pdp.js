// ============================================
// PDP (Product Detail Page) FUNCTIONALITY
// ============================================

// ============================================
// CONSTANTS
// ============================================

const DOM_IDS = {
  STATUS_MESSAGE: 'status-message-pdp',
  TRY_ON_BUTTON: 'try-on-button',
  SDK_IFRAME: 'pulpo-sdk-iframe-pdp',
  SDK_CONTAINER: 'sdk-container-pdp',
}

const SDK_CONFIG = {
  DEFAULT_PATH: 'apply-photo',
  STATUS_TIMEOUT_MS: 5000,
  DEFAULT_MODEL_INDEX: 0,
  BUTTON_TEXT: {
    SHOW_SDK: 'Try On Virtually',
    HIDE_SDK: 'Go back to product image',
  },
  MODELS: [
    'https://plugin.pulpoar.com/vto/images/face-model-women-6.png',
    'https://plugin.pulpoar.com/vto/images/face-model-women-1.png',
    'https://plugin.pulpoar.com/vto/images/face-model-women-2.png',
  ],
}

const CSS_CLASSES = {
  VISIBLE: 'visible',
  ACTIVE: 'active',
  STATUS_MESSAGE: 'status-message-pdp',
  ERROR: 'error',
  SUCCESS: 'success',
}

const DATA_ATTRIBUTES = {
  VARIANT: 'variant',
}

const SELECTORS = {
  COLOR_SWATCHES: '.color-swatch',
}

// ============================================
// STATE MANAGEMENT
// ============================================

const state = {
  selectedVariant: null,
  isSDKVisible: false,
  isSDKReady: false,
  statusTimeoutId: null,
}

// ============================================
// SDK OPERATIONS
// ============================================

const SDKOperations = {
  applyVariant(variantId) {
    pulpoar.applyVariants([variantId])
  },

  setupForTryOn(variantId, modelUrl) {
    pulpoar.setImageToApply(modelUrl)
    pulpoar.applyVariants([variantId])
    pulpoar.setPath(SDK_CONFIG.DEFAULT_PATH)
  },
}

// ============================================
// DOM UTILITIES
// ============================================

function getElement(id) {
  return document.getElementById(id)
}

function getStatusElement() {
  return getElement(DOM_IDS.STATUS_MESSAGE)
}

function getTryOnButton() {
  return getElement(DOM_IDS.TRY_ON_BUTTON)
}

function getSDKContainer() {
  return getElement(DOM_IDS.SDK_CONTAINER)
}

function getColorSwatches() {
  return document.querySelectorAll(SELECTORS.COLOR_SWATCHES)
}

// ============================================
// STATUS MESSAGE MANAGEMENT
// ============================================

function showStatusPDP(message, isError = false) {
  const statusElement = getStatusElement()
  if (!statusElement) return

  if (!message) {
    hideStatus(statusElement)
    return
  }

  displayStatus(statusElement, message, isError)
  scheduleStatusHide(statusElement)
}

function hideStatus(element) {
  element.style.display = 'none'
  element.textContent = ''
}

function displayStatus(element, message, isError) {
  element.textContent = message
  element.className = CSS_CLASSES.STATUS_MESSAGE
  element.classList.add(isError ? CSS_CLASSES.ERROR : CSS_CLASSES.SUCCESS)
  element.style.display = 'block'
  console.log(message)
}

function scheduleStatusHide(element) {
  // Clear any existing timeout to prevent premature hiding
  if (state.statusTimeoutId !== null) {
    clearTimeout(state.statusTimeoutId)
  }

  // Schedule new timeout and store the ID
  state.statusTimeoutId = setTimeout(() => {
    hideStatus(element)
    state.statusTimeoutId = null
  }, SDK_CONFIG.STATUS_TIMEOUT_MS)
}

// ============================================
// BUTTON STATE MANAGEMENT
// ============================================

function updateButtonState() {
  const tryOnButton = getTryOnButton()
  if (!tryOnButton) {
    console.warn('Try-on button not found')
    return
  }

  const shouldEnable = canEnableTryOn()
  console.log('Updating button state:', { shouldEnable, isSDKReady: state.isSDKReady, selectedVariant: state.selectedVariant })
  tryOnButton.disabled = !shouldEnable
}

function canEnableTryOn() {
  return state.isSDKReady && state.selectedVariant !== null
}

// ============================================
// SDK EVENT HANDLERS
// ============================================

function setupPDPEventListeners() {
  registerSDKReadyHandler()
  registerSDKErrorHandler()
}

function registerSDKReadyHandler() {
  pulpoar.onReady((payload) => {
    console.log('SDK onReady fired', payload)
    state.isSDKReady = true
    logEvent('onReady', payload)
    updateButtonState()
    console.log('Button state after ready:', { isSDKReady: state.isSDKReady, selectedVariant: state.selectedVariant, canEnable: canEnableTryOn() })
  })
}

function registerSDKErrorHandler() {
  pulpoar.onError(payload => {
    const errorMessage = payload || 'Unknown error occurred'
    logEvent('onError', payload)
    state.isSDKReady = false
    updateButtonState()
  })
}

// ============================================
// COLOR SWATCH HANDLERS
// ============================================

function setupColorSwatchHandlers() {
  const colorSwatches = getColorSwatches()

  colorSwatches.forEach(swatch => {
    swatch.addEventListener('click', () => handleColorSwatchClick(swatch, colorSwatches))
  })
}

function handleColorSwatchClick(clickedSwatch, allSwatches) {
  deselectAllSwatches(allSwatches)
  selectSwatch(clickedSwatch)

  if (state.isSDKVisible) {
    applyVariantToSDK(clickedSwatch)
  }
}

function deselectAllSwatches(swatches) {
  swatches.forEach(s => s.classList.remove(CSS_CLASSES.ACTIVE))
}

function selectSwatch(swatch) {
  swatch.classList.add(CSS_CLASSES.ACTIVE)
  state.selectedVariant = swatch.dataset[DATA_ATTRIBUTES.VARIANT]
  logEvent('Color Selected', { variant: state.selectedVariant, title: swatch.title })
  updateButtonState()
}

function applyVariantToSDK(swatch) {
  SDKOperations.applyVariant(state.selectedVariant)
  logEvent('Variant Applied', { variant: state.selectedVariant, title: swatch.title })
}

// ============================================
// TRY-ON BUTTON HANDLERS
// ============================================

function setupTryOnButtonHandler() {
  const tryOnButton = getTryOnButton()
  if (!tryOnButton) return

  tryOnButton.addEventListener('click', handleTryOnButtonClick)
}

function handleTryOnButtonClick() {
  if (!isColorSelected()) {
    logEvent('Please select a color first', { error: true })
    return
  }

  toggleSDKVisibility()
}

function isColorSelected() {
  return state.selectedVariant !== null
}

function toggleSDKVisibility() {
  const sdkContainer = getSDKContainer()
  if (!sdkContainer) return

  if (state.isSDKVisible) {
    hideSDK(sdkContainer)
  } else {
    showSDK(sdkContainer)
  }
}

function showSDK(container) {
  const defaultModel = getDefaultModel()
  SDKOperations.setupForTryOn(state.selectedVariant, defaultModel)

  container.classList.add(CSS_CLASSES.VISIBLE)
  state.isSDKVisible = true

  // Update button text
  const tryOnButton = getTryOnButton()
  if (tryOnButton) {
    tryOnButton.textContent = SDK_CONFIG.BUTTON_TEXT.HIDE_SDK
  }

  logEvent('Try-On Activated', { variant: state.selectedVariant, model: defaultModel })
}

function hideSDK(container) {
  container.classList.remove(CSS_CLASSES.VISIBLE)
  state.isSDKVisible = false

  // Reset button text
  const tryOnButton = getTryOnButton()
  if (tryOnButton) {
    tryOnButton.textContent = SDK_CONFIG.BUTTON_TEXT.SHOW_SDK
  }

  logEvent('Try-On Closed', {})
}

function getDefaultModel() {
  return SDK_CONFIG.MODELS[SDK_CONFIG.DEFAULT_MODEL_INDEX]
}

// ============================================
// INITIALIZATION
// ============================================

function initPDPInteractions() {
  setupColorSwatchHandlers()
  setupTryOnButtonHandler()
}

// ============================================
// ENTRY POINT
// ============================================

function waitForPulpoar(callback, maxAttempts = 50) {
  let attempts = 0
  const checkInterval = setInterval(() => {
    attempts++
    if (typeof pulpoar !== 'undefined') {
      clearInterval(checkInterval)
      console.log('Pulpoar SDK found, initializing PDP...')
      callback()
    } else if (attempts >= maxAttempts) {
      clearInterval(checkInterval)
      console.error('Pulpoar SDK not found after', maxAttempts, 'attempts')
    }
  }, 100)
}

document.addEventListener('DOMContentLoaded', function () {
  console.log('DOMContentLoaded fired, waiting for pulpoar SDK...')
  waitForPulpoar(() => {
    setupPDPEventListeners()
    initPDPInteractions()
  })
})
