package com.pulpolabs.kotlin_example

import android.Manifest
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
    private val FILE_CHOOSER_RESULT_CODE = 1
    private val CAMERA_PERMISSION_REQUEST_CODE = 2
    private lateinit var sdk: SDKInterface
    private var pendingPermissionRequest: PermissionRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
                Log.d("PulpoAR", "Permission requested: ${request.resources.joinToString()}")

                val permissions = request.resources
                val cameraPermissionNeeded = permissions.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)

                if (cameraPermissionNeeded) {
                    if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                        Log.d("PulpoAR", "Camera permission already granted, granting WebView request")
                        request.grant(request.resources)
                    } else {
                        Log.d("PulpoAR", "Requesting camera permission from user")
                        pendingPermissionRequest = request
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.CAMERA),
                            CAMERA_PERMISSION_REQUEST_CODE
                        )
                    }
                } else {
                    Log.d("PulpoAR", "Non-camera permission, granting directly")
                    request.grant(request.resources)
                }
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
        val goToProductHandler: (String) -> Unit = { link -> launchProductDetail(link) }
        webView.addJavascriptInterface(SDKInterface(goToProductHandler), "AndroidInterface")
        sdk = SDKInterface(goToProductHandler)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                webView.evaluateJavascript(
                    sdk.getInitialSDKScript(
                        listOf(
                            Events.onError,
                            Events.onReady,
                            Events.onAddToCart,
                            Events.onPathChange,
                            Events.onGoToProduct,
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

    private fun launchProductDetail(url: String) {
        // Marshal back to the main thread because JavascriptInterface callbacks
        // arrive on a background thread.
        webView.post {
            val hostActivity = activity ?: return@post
            val intent = Intent(hostActivity, ProductDetailActivity::class.java)
            intent.putExtra(ProductDetailActivity.EXTRA_URL, url)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            pendingPermissionRequest?.let { request ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("PulpoAR", "Camera permission granted by user, granting WebView request")
                    request.grant(request.resources)
                } else {
                    Log.d("PulpoAR", "Camera permission denied by user, denying WebView request")
                    request.deny()
                }
            }
            pendingPermissionRequest = null
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

}
