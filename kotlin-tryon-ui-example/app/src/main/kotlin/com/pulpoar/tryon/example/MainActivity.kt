package com.pulpoar.tryon.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulpoar.tryon.PulpoAnalyticsConsent
import com.pulpoar.tryon.PulpoTryOn
import com.pulpoar.tryon.ui.PulpoTryOnUIConfiguration
import com.pulpoar.tryon.ui.PulpoTryOnView

/** Your PulpoAR Studio project. "makeup" is PulpoAR's demo project. */
const val PROJECT_SLUG = "makeup"

// The iOS example's look, so both examples show the same shop: system blue, white pages, grouped lists.
private val Accent = Color(0xFF007AFF)
private val Secondary = Color(0xFF8E8E93)
private val GroupedBackground = Color(0xFFF2F2F7)
private val Separator = Color(0xFFC6C6C8)

private enum class Tab(val title: String, val icon: ImageVector) {
    Shop("Shop", Icons.Outlined.ShoppingBag),
    TryOn("Try On", Icons.Outlined.Face),
    Cart("Cart", Icons.Outlined.ShoppingCart),
}

class MainActivity : ComponentActivity() {
    private val cart = Cart()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Dark status bar icons on the light pages. The try-on sets its own while it's shown.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { ExampleApp(cart) } }
    }
}

/** The three usual places for a try-on: full screen from a product page, a pushed screen, and its own tab. */
@Composable
private fun ExampleApp(cart: Cart) {
    var tab by remember { mutableStateOf(Tab.Shop) }
    // The try-on opened from the product page. `null` shows the tabs.
    var opened by remember { mutableStateOf<Opened?>(null) }

    when (opened) {
        // Full screen: close it from the try-on's X button; the system back gesture leaves it too.
        Opened.FullScreen -> {
            BackHandler { opened = null }
            PulpoTryOnView(
                projectSlug = PROJECT_SLUG,
                modifier = Modifier.fillMaxSize(),
                configuration =
                    PulpoTryOnUIConfiguration(
                        onAddToCart = { variants ->
                            cart.add(variants)
                            opened = null
                        },
                        onClose = { opened = null },
                        // Anything in the project's Studio theme can be changed here.
                        customizeTheme = { theme -> theme.catalog.withGoToProduct = false },
                    ),
            )
        }
        // Pushed: the navigation bar's back button leaves it.
        Opened.Pushed -> {
            BackHandler { opened = null }
            Column(Modifier.fillMaxSize().background(Color.White)) {
                NavigationBar(backTitle = "Shop", onBack = { opened = null })
                PulpoTryOnView(
                    projectSlug = PROJECT_SLUG,
                    modifier = Modifier.fillMaxSize(),
                    configuration = PulpoTryOnUIConfiguration(onAddToCart = { cart.add(it) }),
                )
            }
        }
        null ->
            Column(Modifier.fillMaxSize().background(Color.White)) {
                Box(Modifier.weight(1f)) {
                    when (tab) {
                        Tab.Shop -> ProductPage(onOpen = { opened = it })
                        // A tab: the try-on stays where it was while you visit another tab.
                        Tab.TryOn ->
                            PulpoTryOnView(
                                projectSlug = PROJECT_SLUG,
                                modifier = Modifier.fillMaxSize(),
                                configuration = PulpoTryOnUIConfiguration(onAddToCart = { cart.add(it) }),
                            )
                        Tab.Cart -> CartPage(cart)
                    }
                }
                TabBar(tab, cart.items.size) { tab = it }
            }
    }
}

private enum class Opened {
    FullScreen,
    Pushed,
}

/** The iOS tab bar: icon over a small label, the selected tab in the accent color. */
@Composable
private fun TabBar(selected: Tab, cartCount: Int, onSelect: (Tab) -> Unit) {
    Column(Modifier.fillMaxWidth().background(Color(0xFFF9F9F9)).navigationBarsPadding()) {
        HorizontalDivider(thickness = 0.5.dp, color = Separator)
        Row(Modifier.fillMaxWidth().height(50.dp)) {
            Tab.entries.forEach { entry ->
                val color = if (entry == selected) Accent else Secondary
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f).fillMaxSize().clickable { onSelect(entry) },
                ) {
                    Icon(entry.icon, contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
                    Text(
                        if (entry == Tab.Cart) "Cart ($cartCount)" else entry.title,
                        color = color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }
}

/** An iOS large title, with an optional trailing action. */
@Composable
private fun LargeTitle(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Column(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 16.dp)) {
        Box(Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.CenterEnd) {
            if (action != null) {
                Text(action, color = Accent, fontSize = 17.sp, modifier = Modifier.clickable(onClick = onAction))
            }
        }
        Text(title, fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

/** An iOS navigation bar with a back button. */
@Composable
private fun NavigationBar(backTitle: String, onBack: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().statusBarsPadding().height(44.dp).padding(horizontal = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable(onClick = onBack)) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = Accent, modifier = Modifier.size(32.dp))
            Text(backTitle, color = Accent, fontSize = 17.sp)
        }
    }
}

/** A product page with two ways into the try-on. */
@Composable
private fun ProductPage(onOpen: (Opened) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        LargeTitle("Shop")
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Icon(
                Icons.Outlined.Palette,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.padding(top = 40.dp).size(64.dp),
            )
            Text("Your product page", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(
                "Open the try-on full screen, or push it like any other screen.",
                color = Secondary,
                fontSize = 17.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                "Try it on",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                modifier =
                    Modifier.clip(RoundedCornerShape(12.dp))
                        .background(Accent)
                        .clickable { onOpen(Opened.FullScreen) }
                        .padding(horizontal = 20.dp, vertical = 14.dp),
            )
            Text(
                "Open as a screen",
                color = Accent,
                fontSize = 17.sp,
                modifier = Modifier.clickable { onOpen(Opened.Pushed) },
            )
        }
    }
}

/** What Add to Cart collected, and a stand-in for your app's analytics consent prompt. */
@Composable
private fun CartPage(cart: Cart) {
    var allowsAnalytics by remember {
        mutableStateOf(PulpoTryOn.analyticsConsent == PulpoAnalyticsConsent.GRANTED)
    }
    Column(Modifier.fillMaxSize().background(GroupedBackground).verticalScroll(rememberScrollState())) {
        LargeTitle("Cart", action = if (cart.items.isEmpty()) null else "Clear", onAction = cart::clear)
        Spacer(Modifier.height(16.dp))
        Section {
            if (cart.items.isEmpty()) {
                ListRow { Text("Add a shade from the try-on.", color = Secondary, fontSize = 17.sp) }
            }
            cart.items.forEachIndexed { index, variant ->
                if (index > 0) HorizontalDivider(thickness = 0.5.dp, color = Separator)
                ListRow { Text(variant.name, color = Color.Black, fontSize = 17.sp) }
            }
        }
        Spacer(Modifier.height(32.dp))
        Section {
            Row(Modifier.fillMaxWidth().heightIn(min = 44.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Allow analytics", color = Color.Black, fontSize = 17.sp, modifier = Modifier.weight(1f))
                Switch(
                    checked = allowsAnalytics,
                    onCheckedChange = { allowed ->
                        allowsAnalytics = allowed
                        PulpoTryOn.analyticsConsent =
                            if (allowed) PulpoAnalyticsConsent.GRANTED else PulpoAnalyticsConsent.DENIED
                    },
                    colors =
                        SwitchDefaults.colors(
                            checkedTrackColor = Color(0xFF34C759),
                            checkedThumbColor = Color.White,
                            checkedBorderColor = Color.Transparent,
                            uncheckedTrackColor = Color(0xFFE9E9EA),
                            uncheckedThumbColor = Color.White,
                            uncheckedBorderColor = Color.Transparent,
                        ),
                )
            }
        }
        Text(
            "Pass the user's answer to the SDK; until then nothing is reported.",
            color = Secondary,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp),
        )
    }
}

/** An iOS inset grouped section: white rounded rows on the grey page. */
@Composable
private fun Section(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp)
            .heightIn(min = 44.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        content()
    }
}

/** A 44dp list row. */
@Composable
private fun ListRow(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxWidth().heightIn(min = 44.dp), contentAlignment = Alignment.CenterStart) { content() }
}
