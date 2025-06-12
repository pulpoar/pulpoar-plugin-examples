package com.pulpolabs.kotlin_example

import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class MainActivity : AppCompatActivity(), 
    WelcomeFragment.WelcomeListener, 
    ProductDetailFragment.ProductDetailListener,
    SDKInterface.NavigationListener {
    
    private lateinit var fragmentManager: FragmentManager
    private var pulpoARFragment: PulpoARFragment? = null
    private var currentFragment: Fragment? = null
    private val WEBVIEW_FRAGMENT_TAG = "webview_fragment"
    private val navigationStack = mutableListOf<String>() // Track our own navigation stack

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        fragmentManager = supportFragmentManager

        if (savedInstanceState == null) {
            showWelcomeScreen()
        }
    }

    private fun showWelcomeScreen() {
        val welcomeFragment = WelcomeFragment.newInstance()
        welcomeFragment.setWelcomeListener(this)
        
        replaceFragment(welcomeFragment, "welcome_fragment")
        navigationStack.clear()
        navigationStack.add("welcome")
    }

    private fun showProductDetail(productId: Int) {
        Log.d("MainActivity", "showProductDetail called with productId: $productId")
        val productDetailFragment = ProductDetailFragment.newInstance(productId)
        productDetailFragment.setProductDetailListener(this)
        
        replaceFragment(productDetailFragment, "product_fragment_$productId")
        navigationStack.add("product_$productId")
    }

    private fun showWebView() {
        Log.d("MainActivity", "showWebView called")
        
        if (pulpoARFragment == null) {
            Log.d("MainActivity", "Creating new PulpoARFragment for the first time")
            pulpoARFragment = PulpoARFragment()
            
            // Add the WebView fragment (but don't show it yet)
            fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.fragment_container, pulpoARFragment!!, WEBVIEW_FRAGMENT_TAG)
                .commit()
                
            // Set up the navigation listener
            pulpoARFragment?.setNavigationListener(this)
        } else {
            Log.d("MainActivity", "Reusing existing PulpoARFragment - preserving WebView state")
        }
        
        // Show the WebView fragment and hide current fragment
        showFragment(pulpoARFragment!!)
        navigationStack.add("webview")
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        Log.d("MainActivity", "replaceFragment called for: ${fragment::class.java.simpleName}")
        
        // Store reference to WebView fragment if it exists
        val webViewFragment = fragmentManager.findFragmentByTag(WEBVIEW_FRAGMENT_TAG)
        
        // Hide WebView if it exists (but don't remove it)
        if (webViewFragment != null && webViewFragment.isAdded) {
            Log.d("MainActivity", "Hiding WebView fragment")
            fragmentManager.beginTransaction()
                .setReorderingAllowed(true)
                .hide(webViewFragment)
                .commit()
        }
        
        // Clear any existing non-WebView fragments before adding the new one
        val existingFragments = fragmentManager.fragments.filter { 
            it != webViewFragment && it.isAdded 
        }
        
        if (existingFragments.isNotEmpty()) {
            Log.d("MainActivity", "Removing ${existingFragments.size} existing non-WebView fragments")
            val removeTransaction = fragmentManager.beginTransaction().setReorderingAllowed(true)
            existingFragments.forEach { removeTransaction.remove(it) }
            removeTransaction.commit()
        }
        
        // Add the new fragment (without backstack)
        Log.d("MainActivity", "Adding new fragment: ${fragment::class.java.simpleName}")
        fragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.fragment_container, fragment, tag)
            .commit()
        
        currentFragment = fragment
    }

    private fun showFragment(fragment: Fragment) {
        Log.d("MainActivity", "showFragment called for: ${fragment::class.java.simpleName}")
        
        val transaction = fragmentManager.beginTransaction().setReorderingAllowed(true)
        
        // Hide current fragment if it exists and is different
        currentFragment?.let { current ->
            if (current != fragment) {
                Log.d("MainActivity", "Hiding current fragment: ${current::class.java.simpleName}")
                transaction.hide(current)
            }
        }
        
        // Check if the fragment is already added to avoid IllegalStateException
        if (fragment.isAdded) {
            Log.d("MainActivity", "Fragment already added, just showing it")
            // Show target fragment
            Log.d("MainActivity", "Showing fragment: ${fragment::class.java.simpleName}")
            transaction.show(fragment)
        } else {
            Log.e("MainActivity", "Fragment not added to fragment manager - this should not happen for WebView fragment")
            return
        }
        
        // Don't add to system backstack for show/hide operations
        transaction.commit()
        
        currentFragment = fragment
        
        // Post a runnable to ensure WebView is properly displayed after transaction
        fragment.view?.post {
            if (fragment is PulpoARFragment) {
                Log.d("MainActivity", "Post-transaction: ensuring WebView visibility")
                fragment.view?.findViewById<WebView>(R.id.webView)?.let { webView ->
                    webView.visibility = View.VISIBLE
                    webView.requestLayout()
                    webView.invalidate()
                }
            }
        }
    }

    // WelcomeFragment.WelcomeListener
    override fun onTryOnClicked() {
        showWebView()
    }

    // ProductDetailFragment.ProductDetailListener
    override fun onProductTryOnClicked() {
        showWebView()
    }

    override fun onBackClicked() {
        handleBackNavigation()
    }

    // SDKInterface.NavigationListener
    override fun navigateToProduct(productId: Int) {
        Log.d("MainActivity", "navigateToProduct called with productId: $productId")
        runOnUiThread {
            Log.d("MainActivity", "Running on UI thread, calling showProductDetail")
            showProductDetail(productId)
        }
    }

    private fun handleBackNavigation() {
        Log.d("MainActivity", "handleBackNavigation called, navigation stack: $navigationStack")
        
        if (navigationStack.size <= 1) {
            // If we're at the root or stack is empty, go to welcome
            Log.d("MainActivity", "At root, showing welcome screen")
            showWelcomeScreen()
            return
        }
        
        // Remove current screen from stack
        navigationStack.removeLastOrNull()
        
        // Get previous screen
        val previousScreen = navigationStack.lastOrNull()
        Log.d("MainActivity", "Navigating back to: $previousScreen")
        
        when {
            previousScreen == "welcome" -> {
                showWelcomeScreen()
            }
            previousScreen == "webview" -> {
                // Don't add to navigation stack again since we're going back
                if (pulpoARFragment != null) {
                    showFragmentWithoutAddingToStack(pulpoARFragment!!)
                } else {
                    showWelcomeScreen()
                }
            }
            previousScreen?.startsWith("product_") == true -> {
                // Extract product ID and show product detail
                val productId = previousScreen.substringAfter("product_").toIntOrNull() ?: 1
                showProductDetailWithoutAddingToStack(productId)
            }
            else -> {
                Log.d("MainActivity", "Unknown previous screen, going to welcome")
                showWelcomeScreen()
            }
        }
    }

    private fun showFragmentWithoutAddingToStack(fragment: Fragment) {
        Log.d("MainActivity", "showFragmentWithoutAddingToStack called")
        showFragment(fragment)
        // Don't add to navigation stack since this is a back navigation
    }

    private fun showProductDetailWithoutAddingToStack(productId: Int) {
        Log.d("MainActivity", "showProductDetailWithoutAddingToStack called with productId: $productId")
        val productDetailFragment = ProductDetailFragment.newInstance(productId)
        productDetailFragment.setProductDetailListener(this)
        
        replaceFragment(productDetailFragment, "product_fragment_$productId")
        // Don't add to navigation stack since this is a back navigation
    }

    override fun onBackPressed() {
        Log.d("MainActivity", "System back button pressed")
        handleBackNavigation()
    }
}
