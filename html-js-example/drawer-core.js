// Drawer Core Functions
let eventCount = 0

function toggleDrawer(drawerType) {
  const eventsDrawer = document.getElementById('events-drawer')
  const guideDrawer = document.getElementById('guide-drawer')
  const backdrop = document.getElementById('drawer-backdrop')

  if (drawerType === 'events') {
    guideDrawer.classList.remove('open')
    const isOpen = eventsDrawer.classList.toggle('open')
    backdrop.classList.toggle('active', isOpen)
  } else if (drawerType === 'guide') {
    eventsDrawer.classList.remove('open')
    const isOpen = guideDrawer.classList.toggle('open')
    backdrop.classList.toggle('active', isOpen)
  }
}

function closeDrawers() {
  document.getElementById('events-drawer').classList.remove('open')
  document.getElementById('guide-drawer').classList.remove('open')
  document.getElementById('drawer-backdrop').classList.remove('active')
}

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

function logEvent(eventName, payload) {
  const eventLog = document.getElementById('event-log')

  const emptyState = eventLog.querySelector('div[style*="text-align: center"]')
  if (emptyState) {
    emptyState.remove()
  }

  const eventItem = document.createElement('div')
  eventItem.className = 'event-item'

  const timestamp = new Date().toLocaleTimeString()
  eventItem.innerHTML = `
    <div style="font-weight: 600; margin-bottom: 5px; color: #000;">${eventName}</div>
    <div style="font-size: 0.75rem; color: #666; font-family: Monaco, monospace;">${timestamp}</div>
    <pre style="margin-top: 8px; font-size: 0.75rem; overflow-x: auto; background: #fafafa; padding: 10px; border-radius: 4px; border: 1px solid #e0e0e0;">${JSON.stringify(payload, null, 2)}</pre>
  `

  eventLog.insertBefore(eventItem, eventLog.firstChild)

  eventCount++
  document.getElementById('event-count').textContent = eventCount

  if (eventLog.children.length > 50) {
    eventLog.removeChild(eventLog.lastChild)
  }
}

function clearEvents() {
  const eventLog = document.getElementById('event-log')
  eventLog.innerHTML = `
    <div style="text-align: center; padding: 50px 20px; color: #999;">
      <div style="font-size: 3rem; margin-bottom: 15px; opacity: 0.4;">📭</div>
      <div style="font-size: 1rem; font-weight: 500; margin-bottom: 6px; color: #666;">No events yet</div>
      <div style="font-size: 0.85rem; color: #999;">Interact with the VTO experience to see events</div>
    </div>
  `
  eventCount = 0
  document.getElementById('event-count').textContent = '0'
}
