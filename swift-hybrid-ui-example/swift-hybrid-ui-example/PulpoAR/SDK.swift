import WebKit

class PulpoARSDK {
    

    // WebView reference
    weak var webView: WKWebView?

    init(webView: WKWebView) {
        self.webView = webView
    }
    // Function to change application path
    func setPath(_ path: String) {
        print("pulpoar.setPath(\"\(path)\")")
        webView?.evaluateJavaScript("pulpoar.setPath(\"\(path)\")", completionHandler: { (id, error) in
            print(error as Any)
        })
    }

    // Function to apply variants by slugs
    func applyVariants(_ array: [String]) {
        do {
            // Convert the array to JSON data
            let jsonData = try JSONEncoder().encode(array)
            print(jsonData)
            // Convert JSON data to a string
            if let jsonString = String(data: jsonData, encoding: .utf8) {
                print(jsonString)
                webView?.evaluateJavaScript("pulpoar.applyVariants(\(jsonString))", completionHandler: nil)
            }
        } catch {
            print("Failed to encode JSON: \(error)")
        }
    }

    // Function to set image to apply
    func setImageToApply(_ image: String) {
        webView?.evaluateJavaScript("pulpoar.setImageToApply(\"\(image)\")", completionHandler: nil)
    }

    // Function to initialize camera with constraints
    func initCamera(mode: [String: String]) {
        let jsonData = try? JSONSerialization.data(withJSONObject: mode, options: [])
        if let jsonString = jsonData.flatMap({ String(data: $0, encoding: .utf8) }) {
            webView?.evaluateJavaScript("pulpoar.initCamera(\(jsonString))", completionHandler: nil)
        }
    }

    // Function to build SDK event scripts
   static func sdkEventBuilder(_ funcName: String) -> String {
        return """
        pulpoar['\(funcName)']((data)=>{
            window.webkit.messageHandlers.eventHandler.postMessage({event: "\(funcName)", data: data});
        });
        """
    }


    
    static func onReady() -> String {
        return sdkEventBuilder("onReady")
    }

    static func onError() -> String {
        return sdkEventBuilder("onError")
    }

    static func onPathChange() -> String {
        return sdkEventBuilder("onPathChange")
    }

    static func onTryNowClick() -> String {
        return sdkEventBuilder("onTryNowClick")
    }

    static func onGdprApprove() -> String {
        return sdkEventBuilder("onGdprApprove")
    }

    static func onUploadPhoto() -> String {
        return sdkEventBuilder("onUploadPhoto")
    }

    static  func onModelSelect() -> String {
        return sdkEventBuilder("onModelSelect")
    }

    static  func onTakePhoto() -> String {
        return sdkEventBuilder("onTakePhoto")
    }

    static  func onUsePhoto() -> String {
        return sdkEventBuilder("onUsePhoto")
    }

    static func onTakePhotoAgain() -> String {
        return sdkEventBuilder("onTakePhotoAgain")
    }

    static  func onCurtainToggle() -> String {
        return sdkEventBuilder("onCurtainToggle")
    }

    static  func onCurtainSlideStart() -> String {
        return sdkEventBuilder("onCurtainSlideStart")
    }

    static  func onCurtainSlideEnd() -> String {
        return sdkEventBuilder("onCurtainSlideEnd")
    }

    static func onOpacitySlideStart() -> String {
        return sdkEventBuilder("onOpacitySlideStart")
    }

    static  func onOpacitySlideEnd() -> String {
        return sdkEventBuilder("onOpacitySlideEnd")
    }

    static  func onZoom() -> String {
        return sdkEventBuilder("onZoom")
    }

    static func onCameraMirror() -> String {
        return sdkEventBuilder("onCameraMirror")
    }

    static  func onExperienceSelect() -> String {
        return sdkEventBuilder("onExperienceSelect")
    }

    static func onLookSelect() -> String {
        return sdkEventBuilder("onLookSelect")
    }

    static func onGoBack() -> String {
        return sdkEventBuilder("onGoBack")
    }

    static func onBrandSelect() -> String {
        return sdkEventBuilder("onBrandSelect")
    }

    static func onCategorySelect() -> String {
        return sdkEventBuilder("onCategorySelect")
    }

    static func onProductSelect() -> String {
        return sdkEventBuilder("onProductSelect")
    }

    static  func onVariantSelect() -> String {
        return sdkEventBuilder("onVariantSelect")
    }

    static func onAppliedVariantsChange() -> String {
        return sdkEventBuilder("onAppliedVariantsChange")
    }

    static func onAddToCart() -> String {
        return sdkEventBuilder("onAddToCart")
    }

    static func onGoToProduct() -> String {
        return sdkEventBuilder("onGoToProduct")
    }
    
}
