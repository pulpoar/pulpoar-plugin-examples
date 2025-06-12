package com.pulpolabs.kotlin_example

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.pulpolabs.kotlin_example.data.Events


/**
 * A simple [Fragment] subclass.
 * Use the [PulpoARFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PulpoARFragment : Fragment() {
    private var uploadMessage: ValueCallback<Array<Uri>>? = null
    private lateinit var webView: WebView
    private lateinit var actions: Actions
    private var CAMERA_PERMISSION_CODE = 200
    private val FILE_CHOOSER_RESULT_CODE = 1
    private lateinit var sdk: SDKInterface
    private var pendingNavigationListener: SDKInterface.NavigationListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("PulpoARFragment", "onCreate called")
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("PulpoARFragment", "onCreateView called")
        requestCameraPermission()
        val view = inflater.inflate(R.layout.fragment_web_view, container, false)
        webView = view.findViewById(R.id.webView)
        
        initializeWebView()
        actions = Actions(webView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("PulpoARFragment", "onViewCreated called")
        
        // Ensure WebView is visible and properly attached
        webView.visibility = View.VISIBLE
        Log.d("PulpoARFragment", "WebView visibility set to VISIBLE")
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        Log.d("PulpoARFragment", "onHiddenChanged called, hidden: $hidden")
        
        if (::webView.isInitialized) {
            if (hidden) {
                Log.d("PulpoARFragment", "Fragment hidden, pausing WebView")
                webView.onPause()
            } else {
                Log.d("PulpoARFragment", "Fragment shown, resuming WebView")
                webView.onResume()
                webView.visibility = View.VISIBLE
                webView.requestLayout()
                webView.invalidate()
                
                // Re-ensure navigation listener is set
                pendingNavigationListener?.let { listener ->
                    Log.d("PulpoARFragment", "Re-setting navigation listener after fragment shown")
                    if (::sdk.isInitialized) {
                        sdk.setNavigationListener(listener)
                    }
                }
            }
        }
    }

    private fun initializeWebView() {
        Log.d("PulpoARFragment", "initializeWebView called")
        webView.settings.apply {
            javaScriptEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = false
            domStorageEnabled = true
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                request.grant(request.resources)
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                uploadMessage = filePathCallback

                val pickIntent = Intent(Intent.ACTION_GET_CONTENT)
                pickIntent.type = "image/*"
                pickIntent.addCategory(Intent.CATEGORY_OPENABLE)

                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, pickIntent)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Select Source")

                startActivityForResult(chooserIntent, FILE_CHOOSER_RESULT_CODE)
                return true
            }
        }
        
        Log.d("PulpoARFragment", "Creating new SDKInterface")
        sdk = SDKInterface()
        
        // Set the pending navigation listener if it was set before SDK initialization
        pendingNavigationListener?.let {
            Log.d("PulpoARFragment", "Setting pending navigation listener")
            sdk.setNavigationListener(it)
        }
        
        webView.addJavascriptInterface(sdk, "AndroidInterface")
        
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.d("PulpoARFragment", "Page started loading: $url")
            }
            
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("PulpoARFragment", "Page finished loading: $url")

                // Re-set navigation listener after page loads in case JavaScript context was reset
                pendingNavigationListener?.let { listener ->
                    Log.d("PulpoARFragment", "Re-setting navigation listener after page finished")
                    sdk.setNavigationListener(listener)
                }

                webView.evaluateJavascript(
                    sdk.getInitialSDKScript(
                        listOf(
                            Events.onReady,
                            Events.onAddToCart,
                            Events.onPathChange,
                            Events.onGoToProduct
                        )
                    )
                ) { data -> Log.i("Js Result:", data) }
            }
        }
        
        Log.d("PulpoARFragment", "Loading WebView URL")
        webView.loadUrl("https://plugin.pulpoar.com/vto/makeup-demo-1")
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PulpoARFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }

    fun setPath(path: String) {
        actions.setPath(path)
    }

    override fun onResume() {
        super.onResume()
        Log.d("PulpoARFragment", "onResume called")
        
        // Ensure WebView is resumed and visible
        if (::webView.isInitialized) {
            Log.d("PulpoARFragment", "Resuming WebView and ensuring visibility")
            webView.onResume()
            webView.visibility = View.VISIBLE
            webView.requestLayout()
            webView.invalidate()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("PulpoARFragment", "onPause called")
        
        // Pause WebView
        if (::webView.isInitialized) {
            webView.onPause()
        }
    }

    fun setNavigationListener(listener: SDKInterface.NavigationListener) {
        Log.d("PulpoARFragment", "setNavigationListener called")
        // Always store the listener as pending in case SDK gets recreated
        pendingNavigationListener = listener
        
        if (::sdk.isInitialized) {
            Log.d("PulpoARFragment", "SDK is initialized, setting listener directly")
            sdk.setNavigationListener(listener)
        } else {
            Log.d("PulpoARFragment", "SDK not initialized, stored as pending listener")
        }
    }

    fun refreshNavigationListener() {
        Log.d("PulpoARFragment", "refreshNavigationListener called")
        pendingNavigationListener?.let { listener ->
            Log.d("PulpoARFragment", "Refreshing navigation listener")
            if (::sdk.isInitialized) {
                sdk.setNavigationListener(listener)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            val results = data?.data?.let { arrayOf(it) }
            uploadMessage?.onReceiveValue(results)
            uploadMessage = null
        } else {
            uploadMessage?.onReceiveValue(null)
            uploadMessage = null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, access the camera
            }
        }
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission_group.CAMERA
                ),
                CAMERA_PERMISSION_CODE
            )
        }
    }
}