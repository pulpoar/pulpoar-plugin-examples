const CONFIG = {
  MAX_EVENTS_DISPLAY: 50,
  MAX_PAYLOAD_LENGTH: 5000,
  AUTO_HIDE_DELAY: 5000,
  SIDEBAR_EXPANDED_WIDTH: '350px',
  SIDEBAR_COLLAPSED_WIDTH: '40px',
  STORAGE_KEY: 'sidebarCollapsed',
}

const PATH_NAMES = {
  root: 'Home / Root',
  'choose-model': 'Choose Model',
  'take-photo': 'Take Photo',
  'apply-live': 'Apply Live (Camera)',
}

function createOption(value, text) {
  const option = document.createElement('option')
  option.value = value
  option.textContent = text
  return option
}

function formatTimestamp(isoString) {
  return new Date(isoString).toLocaleTimeString('en-US', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  })
}

function truncateJSON(data, maxLength) {
  const jsonString = JSON.stringify(data, null, 2)
  if (jsonString.length <= maxLength) return jsonString

  const remaining = jsonString.length - maxLength
  return `${jsonString.substring(0, maxLength)}\n\n... (truncated ${remaining} characters)`
}

function showMessage(elementId, message, type) {
  const element = document.getElementById(elementId)
  if (!element) return

  element.textContent = message
  element.className = `response-area ${type}`
  element.style.display = 'block'

  setTimeout(() => {
    element.style.display = 'none'
  }, CONFIG.AUTO_HIDE_DELAY)
}

// ============================================
// STATE MANAGEMENT
// ============================================

const state = {
  eventCount: 0,
  uiEventCount: 0,
  hasShownFirstEvent: false,
  projectData: null,
  allVariants: [],
  isSidebarCollapsed: false,
}

const dom = {
  eventsLogContainer: null,
  eventCountBadge: null,
  eventsPanel: null,
  contentGrid: null,
}

// ============================================
// REUSABLE UI FUNCTIONS
// ============================================

function populateDropdown(selectId, variants, options = {}) {
  const select = document.getElementById(selectId)
  if (!select) return

  select.innerHTML = ''

  if (variants.length === 0) {
    select.innerHTML = '<option value="">No variants available</option>'
    select.disabled = true
    return
  }

  if (options.placeholder) {
    select.appendChild(createOption('', options.placeholder))
  }

  variants.forEach(variant => {
    select.appendChild(createOption(variant.slug, variant.displayName))
  })

  select.disabled = false
}

function filterDropdown(searchInputId, selectId, skipPlaceholder = false) {
  const searchInput = document.getElementById(searchInputId)
  const select = document.getElementById(selectId)
  if (!searchInput || !select) return

  const searchTerm = searchInput.value.toLowerCase()
  const options = select.querySelectorAll('option')

  options.forEach(option => {
    if (skipPlaceholder && option.value === '') {
      option.style.display = ''
      return
    }

    const text = option.textContent.toLowerCase()
    option.style.display = text.includes(searchTerm) ? '' : 'none'
  })
}

// ============================================
// EVENT LOGGING & DISPLAY
// ============================================

function logEvent(eventName, payload) {
  const eventData = {
    timestamp: new Date().toISOString(),
    eventName,
    data: payload,
  }

  state.eventCount++
  updateEventCount()
  displayEvent(eventData)
}

function updateEventCount() {
  if (dom.eventCountBadge) {
    dom.eventCountBadge.textContent = state.eventCount
  }
}

function displayEvent(eventData) {
  if (!dom.eventsLogContainer) return

  if (!state.hasShownFirstEvent) {
    const emptyState = dom.eventsLogContainer.querySelector('.empty-state')
    if (emptyState) {
      emptyState.remove()
      state.hasShownFirstEvent = true
    }
  }

  const eventItem = document.createElement('div')
  eventItem.className = 'event-item'

  const timestamp = formatTimestamp(eventData.timestamp)
  const displayData = truncateJSON(eventData.data, CONFIG.MAX_PAYLOAD_LENGTH)

  eventItem.innerHTML = `
    <div class="event-header">
      <div class="event-name">${eventData.eventName}</div>
      <div class="event-time">${timestamp}</div>
    </div>
    <div class="event-data">
      <pre>${displayData}</pre>
    </div>
  `

  dom.eventsLogContainer.insertBefore(eventItem, dom.eventsLogContainer.firstChild)
  state.uiEventCount++

  if (state.uiEventCount > CONFIG.MAX_EVENTS_DISPLAY) {
    const lastChild = dom.eventsLogContainer.lastChild
    if (lastChild && lastChild.classList.contains('event-item')) {
      lastChild.remove()
      state.uiEventCount--
    }
  }
}

function clearEvents() {
  if (dom.eventsLogContainer) {
    dom.eventsLogContainer.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">📭</div>
        <div class="empty-state-text">No events yet</div>
        <div class="empty-state-subtext">Interact with the makeup experience to see events appear here</div>
      </div>
    `
  }

  state.eventCount = 0
  state.uiEventCount = 0
  state.hasShownFirstEvent = false
  updateEventCount()
}

// ============================================
// SDK EVENT HANDLING
// ============================================

function handleReady(data) {
  state.projectData = data
  extractVariants(data)
  populateDropdown('variant-select', state.allVariants)
  populateDropdown('catalog-variant-select', state.allVariants, {
    placeholder: '-- Select a variant --',
  })
  populateDropdown('visibility-variant-select', state.allVariants)
}

function subscribeToEvents() {
  // Core events
  pulpoar.onReady(data => {
    handleReady(data)
    logEvent('onReady', data)
  })
  pulpoar.onError(error => logEvent('onError', error))

  // Navigation events
  pulpoar.onPathChange(data => logEvent('onPathChange', data))
  pulpoar.onGoBack(() => logEvent('onGoBack', undefined))

  // GDPR events
  pulpoar.onGdprApprove(data => logEvent('onGdprApprove', data))
  pulpoar.onGdprLinkClick(data => logEvent('onGdprLinkClick', data))

  // Photo/Model events
  pulpoar.onUploadPhoto(() => logEvent('onUploadPhoto', undefined))
  pulpoar.onModelSelect(data => logEvent('onModelSelect', data))
  pulpoar.onTakePhoto(() => logEvent('onTakePhoto', undefined))
  pulpoar.onUsePhoto(() => logEvent('onUsePhoto', undefined))
  pulpoar.onTakePhotoAgain(() => logEvent('onTakePhotoAgain', undefined))

  // Camera events
  pulpoar.onTryNowClick(data => logEvent('onTryNowClick', data))
  pulpoar.onCameraMirror(data => logEvent('onCameraMirror', data))
  pulpoar.onCameraSwitch(data => logEvent('onCameraSwitch', data))

  // UI Control events
  pulpoar.onCurtainToggle(data => logEvent('onCurtainToggle', data))
  pulpoar.onCurtainSlideStart(data => logEvent('onCurtainSlideStart', data))
  pulpoar.onCurtainSlideEnd(data => logEvent('onCurtainSlideEnd', data))
  pulpoar.onOpacitySlideStart(data => logEvent('onOpacitySlideStart', data))
  pulpoar.onOpacitySlideEnd(data => logEvent('onOpacitySlideEnd', data))
  pulpoar.onZoom(data => logEvent('onZoom', data))

  // Experience events
  pulpoar.onExperienceSelect(data => logEvent('onExperienceSelect', data))

  // Catalog events
  pulpoar.onLookSelect(data => logEvent('onLookSelect', data))
  pulpoar.onBrandSelect(data => logEvent('onBrandSelect', data))
  pulpoar.onCategorySelect(data => logEvent('onCategorySelect', data))
  pulpoar.onProductSelect(data => logEvent('onProductSelect', data))
  pulpoar.onVariantSelect(data => logEvent('onVariantSelect', data))
  pulpoar.onAppliedVariantsChange(data => logEvent('onAppliedVariantsChange', data))

  // Commerce events
  pulpoar.onAddToCart(data => logEvent('onAddToCart', data))
  pulpoar.onGoToProduct(data => logEvent('onGoToProduct', data))
}

// ============================================
// VARIANT MANAGEMENT
// ============================================

function extractVariants(data) {
  state.allVariants = []

  if (!data || !data.products || !Array.isArray(data.products)) {
    console.warn('[Variants] No products found in onReady data')
    return
  }

  data.products.forEach(product => {
    if (product.variants && Array.isArray(product.variants)) {
      product.variants.forEach(variant => {
        if (!variant.slug) return

        state.allVariants.push({
          slug: variant.slug,
          name: variant.name || variant.slug,
          productName: product.name || 'Unknown Product',
          displayName: `${product.name || 'Unknown'} - ${variant.name || variant.slug}`,
        })
      })
    }
  })

  console.log(`[Variants] Extracted ${state.allVariants.length} variants`)
}

// ============================================
// UI ACTION HANDLERS
// ============================================

function switchTab(tabName) {
  const tabButtons = document.querySelectorAll('.tab-button')
  tabButtons.forEach(btn => {
    if (btn.textContent.toLowerCase() === tabName) {
      btn.classList.add('active')
    } else {
      btn.classList.remove('active')
    }
  })

  const eventsTab = document.getElementById('events-tab')
  const actionsTab = document.getElementById('actions-tab')

  if (tabName === 'events') {
    eventsTab.classList.add('active')
    actionsTab.classList.remove('active')
  } else if (tabName === 'actions') {
    actionsTab.classList.add('active')
    eventsTab.classList.remove('active')
  }
}

function toggleSidebar() {
  if (!dom.eventsPanel || !dom.contentGrid) return

  state.isSidebarCollapsed = !state.isSidebarCollapsed

  if (state.isSidebarCollapsed) {
    dom.eventsPanel.classList.add('collapsed')
    dom.contentGrid.style.gridTemplateColumns = `1fr ${CONFIG.SIDEBAR_COLLAPSED_WIDTH}`
  } else {
    dom.eventsPanel.classList.remove('collapsed')
    dom.contentGrid.style.gridTemplateColumns = `1fr ${CONFIG.SIDEBAR_EXPANDED_WIDTH}`
  }
}

function toggleAccordion(button) {
  const content = button.nextElementSibling
  content.classList.toggle('active')
  button.classList.toggle('active')
}

// ============================================
// SDK ACTION HANDLERS
// ============================================

function updateStock(event) {
  event.preventDefault()

  const form = event.target
  const select = document.getElementById('variant-select')
  const statusRadio = form.querySelector('input[name="status"]:checked').value
  const outOfStockText = document.getElementById('out-of-stock-text').value.trim()

  const selectedOptions = Array.from(select.selectedOptions)

  if (selectedOptions.length === 0) {
    showMessage('action-response', 'Please select at least one variant', 'error')
    return
  }

  const slugs = selectedOptions.map(opt => opt.value)
  const variants = slugs.map(slug => ({ slug }))
  const params = {
    variants,
    isDisabled: statusRadio === 'disable',
  }

  if (statusRadio === 'disable' && outOfStockText) {
    params.outOfStockText = outOfStockText
  }

  try {
    pulpoar.setVariantsStatus(params)
    pulpoar.applyVariantsWithCatalog(slugs)

    const action = statusRadio === 'enable' ? 'enabled' : 'disabled'
    const message = `Successfully ${action} ${slugs.length} variant(s): ${slugs.join(', ')}`
    showMessage('action-response', message, 'success')

    console.log('[Stock Update]', params)
  } catch (error) {
    showMessage('action-response', `Error: ${error.message}`, 'error')
    console.error('[Stock Update Error]', error)
  }
}

function navigateToPath(event) {
  event.preventDefault()

  const form = event.target
  const selectedPath = form.querySelector('input[name="path"]:checked').value

  try {
    pulpoar.setPath(selectedPath)

    const message = `Navigated to: ${PATH_NAMES[selectedPath] || selectedPath}`
    showMessage('navigation-response', message, 'success')

    console.log('[Navigation]', { path: selectedPath })
  } catch (error) {
    showMessage('navigation-response', `Error: ${error.message}`, 'error')
    console.error('[Navigation Error]', error)
  }
}

function applyCatalogVariant(event) {
  event.preventDefault()

  const select = document.getElementById('catalog-variant-select')
  const selectedSlug = select.value

  if (!selectedSlug) {
    showMessage('catalog-response', 'Please select a variant', 'error')
    return
  }

  try {
    pulpoar.applyVariantsWithCatalog([selectedSlug])

    const message = `Successfully applied variant: ${selectedSlug}`
    showMessage('catalog-response', message, 'success')

    console.log('[Apply Catalog Variant]', { slug: selectedSlug })
  } catch (error) {
    showMessage('catalog-response', `Error: ${error.message}`, 'error')
    console.error('[Apply Catalog Variant Error]', error)
  }
}

function resetCatalog(event) {
  event.preventDefault()

  try {
    pulpoar.applyVariantsWithCatalog([])

    showMessage('catalog-response', 'Successfully reset catalog', 'success')

    console.log('[Reset Catalog]')
  } catch (error) {
    showMessage('catalog-response', `Error: ${error.message}`, 'error')
    console.error('[Reset Catalog Error]', error)
  }
}

function updateVisibility(event) {
  event.preventDefault()

  const select = document.getElementById('visibility-variant-select')
  const selectedOptions = Array.from(select.selectedOptions)

  if (selectedOptions.length === 0) {
    showMessage('visibility-response', 'Please select at least one variant', 'error')
    return
  }

  const slugs = selectedOptions.map(opt => opt.value)
  const action = document.querySelector('input[name="visibility-action"]:checked').value
  const mode = document.querySelector('input[name="visibility-mode"]:checked').value
  const hidden = action === 'hide'

  const params = {
    variants: slugs.map(slug => ({ slug, hidden })),
    mode,
  }

  try {
    pulpoar.setVariantsVisibility(params, response => {
      const actionText = hidden ? 'hidden' : 'shown'
      const message = `${slugs.length} variant(s) ${actionText} (mode: ${mode})`
      showMessage('visibility-response', message, 'success')

      console.log('[Visibility Update]', { params, response })
    })
  } catch (error) {
    showMessage('visibility-response', `Error: ${error.message}`, 'error')
    console.error('[Visibility Update Error]', error)
  }
}

function showAllVariants() {
  const params = { variants: [], mode: 'replace' }

  try {
    pulpoar.setVariantsVisibility(params, response => {
      showMessage('visibility-response', 'All variants are now visible', 'success')

      console.log('[Show All Variants]', { params, response })
    })
  } catch (error) {
    showMessage('visibility-response', `Error: ${error.message}`, 'error')
    console.error('[Show All Variants Error]', error)
  }
}

// ============================================
// INITIALIZATION
// ============================================

document.addEventListener('DOMContentLoaded', function () {
  dom.eventsLogContainer = document.getElementById('events-log')
  dom.eventCountBadge = document.getElementById('event-count')
  dom.eventsPanel = document.querySelector('.events-panel')
  dom.contentGrid = document.querySelector('.content')

  subscribeToEvents()
})

// ============================================
// PUBLIC API
// ============================================

window.switchTab = switchTab
window.toggleSidebar = toggleSidebar
window.toggleAccordion = toggleAccordion
window.clearEvents = clearEvents
window.updateStock = updateStock
window.filterVariants = () => filterDropdown('variant-search', 'variant-select', false)
window.navigateToPath = navigateToPath
window.applyCatalogVariant = applyCatalogVariant
window.resetCatalog = resetCatalog
window.filterCatalogVariants = () =>
  filterDropdown('catalog-variant-search', 'catalog-variant-select', true)
window.updateVisibility = updateVisibility
window.showAllVariants = showAllVariants
window.filterVisibilityVariants = () =>
  filterDropdown('visibility-variant-search', 'visibility-variant-select', false)
