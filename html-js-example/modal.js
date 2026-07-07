// ============================================
// MODAL PAGE FUNCTIONALITY
// ============================================

// ============================================
// CONSTANTS
// ============================================

const DOM_IDS = {
  OPEN_BUTTON: 'open-modal-button',
  DESKTOP_CLOSE_BUTTON: 'desktop-close-button',
  MOBILE_CLOSE_BUTTON: 'mobile-close-button',
  MODAL_OVERLAY: 'modal-overlay',
  SDK_IFRAME: 'pulpo-vto-modal',
}

const SDK_CONFIG = {
  PLUGIN_SRC: 'https://plugin.pulpoar.com/vto/makeup-demo-1',
}

const CSS_CLASSES = {
  OPEN: 'open',
}

// ============================================
// STATE MANAGEMENT
// ============================================

const state = {
  isModalOpen: false,
}

// ============================================
// DOM UTILITIES
// ============================================

function getElement(id) {
  return document.getElementById(id)
}

function getOpenButton() {
  return getElement(DOM_IDS.OPEN_BUTTON)
}

function getModalOverlay() {
  return getElement(DOM_IDS.MODAL_OVERLAY)
}

function getSDKIframe() {
  return getElement(DOM_IDS.SDK_IFRAME)
}

// ============================================
// MODAL VISIBILITY MANAGEMENT
// ============================================

function loadSDKIframe() {
  const iframe = getSDKIframe()
  if (!iframe) return

  iframe.src = SDK_CONFIG.PLUGIN_SRC
}

function openModal() {
  const modalOverlay = getModalOverlay()
  if (!modalOverlay) return

  modalOverlay.classList.add(CSS_CLASSES.OPEN)
  document.body.style.overflow = 'hidden'
  state.isModalOpen = true
}

function closeModal() {
  const modalOverlay = getModalOverlay()
  if (!modalOverlay) return

  modalOverlay.classList.remove(CSS_CLASSES.OPEN)
  document.body.style.overflow = ''
  state.isModalOpen = false
}

// ============================================
// SDK EVENT HANDLERS
// ============================================

function setupModalEventListeners() {
  pulpoar.onReady(() => {
    console.log('[Pulpo] SDK ready')
  })

  pulpoar.onError(error => {
    console.error('[Pulpo] onError', error)
  })
}

// ============================================
// UI EVENT HANDLERS
// ============================================

function setupOpenButtonHandler() {
  const openButton = getOpenButton()
  if (!openButton) return

  openButton.addEventListener('click', openModal)
}

function setupCloseButtonHandlers() {
  const desktopCloseButton = getElement(DOM_IDS.DESKTOP_CLOSE_BUTTON)
  const mobileCloseButton = getElement(DOM_IDS.MOBILE_CLOSE_BUTTON)

  desktopCloseButton?.addEventListener('click', closeModal)
  mobileCloseButton?.addEventListener('click', closeModal)
}

function setupOverlayClickHandler() {
  const modalOverlay = getModalOverlay()
  if (!modalOverlay) return

  modalOverlay.addEventListener('click', event => {
    if (event.target === modalOverlay) closeModal()
  })
}

function setupEscapeKeyHandler() {
  document.addEventListener('keydown', event => {
    if (event.key === 'Escape' && state.isModalOpen) closeModal()
  })
}

// ============================================
// INITIALIZATION
// ============================================

function initModalInteractions() {
  loadSDKIframe()
  setupOpenButtonHandler()
  setupCloseButtonHandlers()
  setupOverlayClickHandler()
  setupEscapeKeyHandler()
}

// ============================================
// ENTRY POINT
// ============================================

document.addEventListener('DOMContentLoaded', function () {
  console.log('DOMContentLoaded fired, initializing...')
  setupModalEventListeners()
  initModalInteractions()
})
