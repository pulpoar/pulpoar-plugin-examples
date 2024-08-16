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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requestCameraPermission()
        val view = inflater.inflate(R.layout.fragment_web_view, container, false)
        webView = view.findViewById(R.id.webView)
        initializeWebView()
        actions = Actions(webView)

        return view
    }

    private fun initializeWebView() {
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
        webView.addJavascriptInterface(SDKInterface(), "AndroidInterface")
        sdk = SDKInterface()
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                webView.evaluateJavascript(
                    sdk.getInitialSDKScript(
                        listOf(
                            Events.onReady,
                            Events.onAddToCart,
                            Events.onPathChange,
                        )
                    )
                ) { data -> Log.i("Js Result:", data) }
            }
        }
        webView.loadUrl("https://plugin.pulpoar.com/vto/makeup")
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