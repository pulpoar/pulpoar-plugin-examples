package com.pulpolabs.kotlin_example

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
// Note: avoid setSupportActionBar() — the app's AppTheme.NoActionBar inherits from
// Theme.AppCompat (which provides a decor ActionBar), so installing a second one crashes.

class ProductDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_URL = "extra_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        toolbar.title = "Product Detail"
        toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white))
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener { finish() }

        val url = intent.getStringExtra(EXTRA_URL).orEmpty()
        val webView: WebView = findViewById(R.id.productWebView)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webViewClient = WebViewClient()

        if (url.isNotEmpty()) {
            webView.loadUrl(url)
        }
    }
}
