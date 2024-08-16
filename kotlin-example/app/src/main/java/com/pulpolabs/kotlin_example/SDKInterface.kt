package com.pulpolabs.kotlin_example

import android.util.Log
import android.webkit.JavascriptInterface
import com.pulpolabs.kotlin_example.data.AddToCartPayload
import com.pulpolabs.kotlin_example.data.AppliedVariantsChangePayload
import com.pulpolabs.kotlin_example.data.Brand
import com.pulpolabs.kotlin_example.data.Category
import com.pulpolabs.kotlin_example.data.Events
import com.pulpolabs.kotlin_example.data.ExperienceSelectPayload
import com.pulpolabs.kotlin_example.data.LookSelectPayload
import com.pulpolabs.kotlin_example.data.PathChangePayload
import com.pulpolabs.kotlin_example.data.ProjectData
import com.pulpolabs.kotlin_example.data.CameraTogglePayload
import com.pulpolabs.kotlin_example.data.CurtainTogglePayload
import com.pulpolabs.kotlin_example.data.ModelSelectPayload
import com.pulpolabs.kotlin_example.data.OpacitySlidePayload
import com.pulpolabs.kotlin_example.data.Variant
import com.pulpolabs.kotlin_example.data.ZoomPayload
import org.json.JSONObject
import java.util.stream.Collectors

public class SDKInterface {

    @JavascriptInterface
    fun onReady(payload: String) {
        val jsonObject = JSONObject(payload)
        val data = ProjectData(jsonObject)

        Log.d("PulpoAR", "PulpoAR is ready: $data")
    }

    @JavascriptInterface
    fun onAddToCart(payload: String) {
        val jsonObject = JSONObject(payload)
        val cart = AddToCartPayload(jsonObject)

        Log.d("PulpoAR", "Add to Cart clicked: $cart")
    }

    @JavascriptInterface
    fun onPathChange(payload: String) {
        val jsonObject = JSONObject(payload)
        val path = PathChangePayload(jsonObject)

        Log.d("PulpoAR", "Path changed: ${path}")
    }

    @JavascriptInterface
    fun onAppliedVariantsChange(payload: String) {
        val jsonObject = JSONObject(payload)
        val variantData = AppliedVariantsChangePayload(jsonObject)

        Log.d("PulpoAR", "Applied variants changed: $variantData")
    }

    @JavascriptInterface
    fun onVariantSelect(payload: String) {
        val jsonObject = JSONObject(payload)
        val path = Variant(jsonObject)

        Log.d("PulpoAR", "Variant selected: $path")
    }

    @JavascriptInterface
    fun onBrandSelect(payload: String) {
        val jsonObject = JSONObject(payload)
        val path = Brand(jsonObject)

        Log.d("PulpoAR", "Brand Selected: $path")
    }

    @JavascriptInterface
    fun onCameraMirror(payload: String) {
        val jsonObject = JSONObject(payload)
        val path = CameraTogglePayload(jsonObject)

        Log.d("PulpoAR", "Camera mirror: $path")
    }

    @JavascriptInterface
    fun onExperienceSelect(payload: String) {
        val jsonObject = JSONObject(payload)
        val path = ExperienceSelectPayload(jsonObject)

        Log.d("PulpoAR", "Experience Selected: $path")
    }

    @JavascriptInterface
    fun onLookSelect(payload: String) {
        val jsonObject = JSONObject(payload)
        val path = LookSelectPayload(jsonObject)

        Log.d("PulpoAR", "Look Selected: $path")
    }

    @JavascriptInterface
    fun onGoBack(payload: String) {

        Log.d("PulpoAR", "Go Back: $payload")
    }

    @JavascriptInterface
    fun onGoToProduct(payload: String) {
        Log.d("PulpoAR", "on Go To Product: $payload")
    }

    @JavascriptInterface
    fun onCategorySelect(payload: String) {
        val jsonObject = JSONObject(payload)
        val path = Category(jsonObject)

        Log.d("PulpoAR", "Category Selected: $path")
    }

    @JavascriptInterface
    fun onProductSelect(payload: String) {
        val jsonObject = JSONObject(payload)
        val path = Category(jsonObject)

        Log.d("PulpoAR", "Product Selected: $path")
    }

    @JavascriptInterface
    fun onCurtainSlideEnd(payload: String) {
        Log.d("PulpoAR", "Curtain Slide End: $payload")
    }

    @JavascriptInterface
    fun onCurtainSlideStart(payload: String) {
        Log.d("PulpoAR", "Curtain Slide Start: $payload")
    }

    @JavascriptInterface
    fun onOpacitySlideStart(payload: String) {
        val jsonObject = JSONObject(payload)
        val path = OpacitySlidePayload(jsonObject)

        Log.d("PulpoAR", "Opacity Slide Start: $path")
    }

    @JavascriptInterface
    fun onOpacitySlideEnd(payload: String) {
        val jsonObject = JSONObject(payload)
        val path = OpacitySlidePayload(jsonObject)

        Log.d("PulpoAR", "Opacity Slide End: $path")
    }

    @JavascriptInterface
    fun onZoom(payload: String) {
        val jsonObject = JSONObject(payload)
        val path = ZoomPayload(jsonObject)

        Log.d("PulpoAR", "zoom: $path")
    }

    @JavascriptInterface
    fun onCurtainToggle(payload: String) {
        val jsonObject = JSONObject(payload)
        val path = CurtainTogglePayload(jsonObject)

        Log.d("PulpoAR", "Curtain toggle: $path")
    }

    @JavascriptInterface
    fun onTryNowClick(payload: String) {
        Log.d("PulpoAR", "Try now clicked: $payload")
    }

    @JavascriptInterface
    fun onGdprApprove(payload: String) {
        Log.d("PulpoAR", "Gdpr approve: $payload")
    }

    @JavascriptInterface
    fun onUploadPhoto(payload: String) {
        Log.d("PulpoAR", "Photo uploaded: $payload")
    }

    @JavascriptInterface
    fun onModelSelect(payload: String) {
        val jsonObject = JSONObject(payload)
        val path = ModelSelectPayload(jsonObject)

        Log.d("PulpoAR", "Model Selected: $path")
    }

    @JavascriptInterface
    fun onTakePhotoAgain(payload: String) {
        Log.d("PulpoAR", "Take again: $payload")
    }

    @JavascriptInterface
    fun onUsePhoto(payload: String) {
        Log.d("PulpoAR", "Use Photo: $payload")
    }

    @JavascriptInterface
    fun onError(payload: String) {
        Log.d("PulpoAR", "Error: $payload")
    }

    @JavascriptInterface
    fun onTakePhoto(payload: String) {
        Log.d("PulpoAR", "Take Photo: $payload")
    }

    fun getInitialSDKScript(events: List<Events>): String {
        var script: String = """
                const script = document.createElement('script');
                script.src = 'https://cdn.jsdelivr.net/npm/@pulpoar/plugin-sdk@latest/dist/index.iife.js';
                script.onload = function() {
                  ${makeSdkEvent(events)}
                }
                document.body.appendChild(script);
            
        """.trimIndent()
        return script
    }

    fun makeSdkEvent(events: List<Events>): String {
        return events.stream().map { event ->
            """
             pulpoar['${event}']((payload)=>{
                AndroidInterface.${event.toString()}(JSON.stringify(payload));
             });
            """.trimIndent()
        }.collect(Collectors.joining("\n"))
    }
}