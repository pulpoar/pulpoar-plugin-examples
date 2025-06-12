package com.pulpolabs.kotlin_example

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager

class MainActivity : AppCompatActivity(), 
    WelcomeFragment.WelcomeListener, 
    ProductDetailFragment.ProductDetailListener,
    SDKInterface.NavigationListener {
    
    private lateinit var fragmentManager: FragmentManager
    private var pulpoARFragment: PulpoARFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        fragmentManager = supportFragmentManager

        if (savedInstanceState == null) {
            showWelcomeScreen()
        }
    }

    private fun showWelcomeScreen() {
        Log.d("MainActivity", "showWelcomeScreen called")
        val welcomeFragment = WelcomeFragment.newInstance()
        welcomeFragment.setWelcomeListener(this)
        
        // Clear backstack and add welcome as root
        clearBackStack()
        
        fragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fragment_container, welcomeFragment, "welcome_fragment")
            .commit()
    }

    private fun showWebView() {
        Log.d("MainActivity", "showWebView called")
        Log.d("MainActivity", "Current backstack count: ${fragmentManager.backStackEntryCount}")
        
        // Find existing WebView fragment or create new one
        pulpoARFragment = fragmentManager.findFragmentByTag("webview_fragment") as? PulpoARFragment
        if (pulpoARFragment == null) {
            Log.d("MainActivity", "Creating new PulpoARFragment")
            pulpoARFragment = PulpoARFragment()
            pulpoARFragment?.setNavigationListener(this)
        } else {
            Log.d("MainActivity", "Found existing PulpoARFragment - preserving WebView state")
        }
        
        // Navigate to WebView with standard Android navigation
        fragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fragment_container, pulpoARFragment!!, "webview_fragment")
            .addToBackStack("webview")
            .commit()
            
        Log.d("MainActivity", "WebView transaction committed, new backstack count will be: ${fragmentManager.backStackEntryCount + 1}")
    }

    private fun clearBackStack() {
        Log.d("MainActivity", "Clearing backstack")
        // Clear all backstack entries
        for (i in 0 until fragmentManager.backStackEntryCount) {
            fragmentManager.popBackStack()
        }
    }

    // WelcomeFragment.WelcomeListener
    override fun onTryOnClicked() {
        Log.d("MainActivity", "Welcome - Try On clicked")
        showWebView()
    }

    // ProductDetailFragment.ProductDetailListener
    override fun onProductTryOnClicked() {
        Log.d("MainActivity", "Product Detail - Try On clicked")
        Log.d("MainActivity", "Current backstack count: ${fragmentManager.backStackEntryCount}")
        
        // Log current backstack entries for debugging
        for (i in 0 until fragmentManager.backStackEntryCount) {
            val entry = fragmentManager.getBackStackEntryAt(i)
            Log.d("MainActivity", "Backstack entry $i: ${entry.name}")
        }
        
        // Pop back to webview fragment without removing it
        if (fragmentManager.backStackEntryCount > 0) {
            Log.d("MainActivity", "Popping back to webview fragment")
            fragmentManager.popBackStack("webview", 0)  // 0 means don't include the webview fragment itself
        } else {
            Log.e("MainActivity", "ERROR: No backstack entries found! This should not happen.")
        }
    }

    override fun onBackClicked() {
        Log.d("MainActivity", "Product Detail - Back clicked")
        // Standard back navigation - pop current fragment
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
        } else {
            showWelcomeScreen()
        }
    }

    // SDKInterface.NavigationListener
    override fun navigateToProduct(productId: Int) {
        Log.d("MainActivity", "navigateToProduct called with productId: $productId")
        Log.d("MainActivity", "Current backstack count: ${fragmentManager.backStackEntryCount}")
        
        // Log current backstack entries for debugging
        for (i in 0 until fragmentManager.backStackEntryCount) {
            val entry = fragmentManager.getBackStackEntryAt(i)
            Log.d("MainActivity", "Current backstack entry $i: ${entry.name}")
        }
        
        val productFragment = ProductDetailFragment.newInstance(productId)
        productFragment.setProductDetailListener(this)
        
        fragmentManager.beginTransaction()
            .setReorderingAllowed(true)
            .replace(R.id.fragment_container, productFragment, "product_detail_fragment")
            .addToBackStack("product_detail_$productId")
            .commit()
            
        Log.d("MainActivity", "Product detail transaction committed, new backstack count will be: ${fragmentManager.backStackEntryCount + 1}")
    }

    private fun navigateBackToWebView() {
        Log.d("MainActivity", "navigateBackToWebView called - THIS SHOULD NOT BE CALLED")
        Log.d("MainActivity", "Use onProductTryOnClicked() instead")
        
        // This method should not be used anymore
        // Just pop the backstack
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
        }
    }

    override fun onBackPressed() {
        Log.d("MainActivity", "System back button pressed")
        // Use standard Android back navigation
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}
