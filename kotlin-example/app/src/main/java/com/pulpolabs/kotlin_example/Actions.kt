package com.pulpolabs.kotlin_example

import android.webkit.WebView

class Actions(private val webView: WebView) {

    fun setPath(path: String) {
        val jsCode = "pulpoar.setPath('$path')"
        webView.evaluateJavascript(jsCode, null)
    }

    // Add other actions as needed
}