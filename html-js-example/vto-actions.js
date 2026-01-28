// VTO SDK Actions
function navigateTo(path) {
  try {
    pulpoar.setPath(path)
    alert(`Navigated to: ${path}`)
  } catch (error) {
    alert(`Error: ${error.message}`)
  }
}

function applyVariants() {
  const input = document.getElementById('apply-variants-input')
  const slugs = input.value
    .split(',')
    .map(s => s.trim())
    .filter(s => s)

  if (slugs.length === 0) {
    alert('Please enter at least one variant slug')
    return
  }

  try {
    pulpoar.applyVariants(slugs)
    alert(`Applied ${slugs.length} variant(s): ${slugs.join(', ')}`)
    input.value = ''
  } catch (error) {
    alert(`Error: ${error.message}`)
  }
}

function resetVariants() {
  try {
    pulpoar.applyVariants([])
    alert('Variants reset')
  } catch (error) {
    alert(`Error: ${error.message}`)
  }
}

function applyWithCatalog() {
  const input = document.getElementById('catalog-variants-input')
  const slugs = input.value
    .split(',')
    .map(s => s.trim())
    .filter(s => s)

  if (slugs.length === 0) {
    alert('Please enter at least one variant slug')
    return
  }

  try {
    pulpoar.applyVariantsWithCatalog(slugs)
    alert(`Applied with catalog: ${slugs.join(', ')}`)
    input.value = ''
  } catch (error) {
    alert(`Error: ${error.message}`)
  }
}

function resetCatalog() {
  try {
    pulpoar.applyVariantsWithCatalog([])
    alert('Catalog reset')
  } catch (error) {
    alert(`Error: ${error.message}`)
  }
}

function initCamera(facingMode) {
  try {
    pulpoar.initCamera({ facingMode })
    alert(`Camera initialized: ${facingMode === 'user' ? 'Front' : 'Back'} camera`)
  } catch (error) {
    alert(`Error: ${error.message}`)
  }
}

function setCameraFeed(status) {
  try {
    pulpoar.setCameraFeedStatus(status)
    alert(`Camera feed ${status}`)
  } catch (error) {
    alert(`Error: ${error.message}`)
  }
}

function setVariantStock(isDisabled) {
  const variantInput = document.getElementById('stock-variant-input')
  const messageInput = document.getElementById('stock-message-input')
  const slug = variantInput.value.trim()

  if (!slug) {
    alert('Please enter a variant slug')
    return
  }

  const params = {
    variants: [{ slug }],
    isDisabled,
    outOfStockText: messageInput.value.trim() || 'Out of stock',
  }

  try {
    pulpoar.setVariantsStatus(params)
    alert(`Variant ${slug} marked as ${isDisabled ? 'out of stock' : 'in stock'}`)
    variantInput.value = ''
  } catch (error) {
    alert(`Error: ${error.message}`)
  }
}
