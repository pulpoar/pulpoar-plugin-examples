package com.pulpoar.tryon.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pulpoar.tryon.PulpoAnalyticsConsent
import com.pulpoar.tryon.PulpoTryOn
import com.pulpoar.tryon.ui.PulpoTryOnUIConfiguration
import com.pulpoar.tryon.ui.PulpoTryOnView

/** Your PulpoAR Studio project. "makeup" is PulpoAR's demo project. */
const val PROJECT_SLUG = "makeup"

private enum class Tab(val title: String) {
    Shop("Shop"),
    TryOn("Try On"),
    Cart("Cart"),
}

class MainActivity : ComponentActivity() {
    private val cart = Cart()

    override fun onCreate(savedInstanceState: Bundle?) {
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
        // The try-on has its own close button; the system back gesture leaves it too.
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
        Opened.Pushed -> {
            BackHandler { opened = null }
            Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                TextButton(onClick = { opened = null }) { Text("‹ Back") }
                PulpoTryOnView(
                    projectSlug = PROJECT_SLUG,
                    modifier = Modifier.fillMaxSize(),
                    configuration = PulpoTryOnUIConfiguration(onAddToCart = { cart.add(it) }),
                )
            }
        }
        null ->
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        Tab.entries.forEach { entry ->
                            NavigationBarItem(
                                selected = tab == entry,
                                onClick = { tab = entry },
                                icon = {},
                                label = {
                                    Text(
                                        if (entry == Tab.Cart) "Cart (${cart.items.size})"
                                        else entry.title
                                    )
                                },
                            )
                        }
                    }
                }
            ) { padding ->
                Box(Modifier.fillMaxSize().padding(padding)) {
                    when (tab) {
                        Tab.Shop -> ProductPage(onOpen = { opened = it })
                        // A tab: the try-on keeps its state while you visit another tab.
                        Tab.TryOn ->
                            PulpoTryOnView(
                                projectSlug = PROJECT_SLUG,
                                modifier = Modifier.fillMaxSize(),
                                configuration =
                                    PulpoTryOnUIConfiguration(onAddToCart = { cart.add(it) }),
                            )
                        Tab.Cart -> CartPage(cart)
                    }
                }
            }
    }
}

private enum class Opened {
    FullScreen,
    Pushed,
}

/** A product page with two ways into the try-on. */
@Composable
private fun ProductPage(onOpen: (Opened) -> Unit) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Your product page", style = MaterialTheme.typography.headlineSmall)
        Text("Open the try-on full screen, or push it like any other screen.")
        Button(onClick = { onOpen(Opened.FullScreen) }) { Text("Try it on") }
        TextButton(onClick = { onOpen(Opened.Pushed) }) { Text("Open as a screen") }
    }
}

/** What Add to Cart collected, and a stand-in for your app's analytics consent prompt. */
@Composable
private fun CartPage(cart: Cart) {
    var allowsAnalytics by remember {
        mutableStateOf(PulpoTryOn.analyticsConsent == PulpoAnalyticsConsent.GRANTED)
    }
    Column(Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Cart", style = MaterialTheme.typography.headlineSmall)
        if (cart.items.isEmpty()) Text("Add a shade from the try-on.")
        cart.items.forEach { Text(it.name) }
        if (cart.items.isNotEmpty()) TextButton(onClick = cart::clear) { Text("Clear") }
        Text("Allow analytics")
        Switch(
            checked = allowsAnalytics,
            onCheckedChange = { allowed ->
                allowsAnalytics = allowed
                PulpoTryOn.analyticsConsent =
                    if (allowed) PulpoAnalyticsConsent.GRANTED else PulpoAnalyticsConsent.DENIED
            },
        )
        Text("Pass the user's answer to the SDK; until then nothing is reported.")
    }
}
