async function loadDrawers(type) {
  const container = document.getElementById('drawer-container')

  if (!container) {
    console.error('Drawer container not found')
    return false
  }

  try {
    const backdrop =
      '<div class="drawer-backdrop" id="drawer-backdrop" onclick="closeDrawers()"></div>'

    const eventsResponse = await fetch(`drawer-events-${type}.html`)
    if (!eventsResponse.ok)
      throw new Error(`Failed to load events drawer: ${eventsResponse.status}`)
    const eventsHTML = await eventsResponse.text()

    const guideResponse = await fetch(`drawer-guide-${type}.html`)
    if (!guideResponse.ok) throw new Error(`Failed to load guide drawer: ${guideResponse.status}`)
    const guideHTML = await guideResponse.text()

    container.innerHTML = backdrop + eventsHTML + guideHTML

    return true
  } catch (error) {
    console.error('Error loading drawers:', error)
    return false
  }
}
