import SwiftUI
import WebKit
struct PulpoWebView: UIViewRepresentable {
    let url: URL
    @Binding var iframeLoaded: Bool
    let onEventsInjected: () -> Void
    @Binding var webView: WKWebView?
    var events: Events
    
    func makeUIView(context: Context) -> WKWebView {
        let contentController = WKUserContentController()
        contentController.add(context.coordinator, name: "eventHandler")
        let config = WKWebViewConfiguration()
        config.userContentController = contentController
        config.allowsAirPlayForMediaPlayback = true
        config.mediaTypesRequiringUserActionForPlayback = []
        let webView = WKWebView(frame: .zero, configuration: config)
        DispatchQueue.main.async {
            self.webView = webView
        }
        webView.navigationDelegate = context.coordinator
        return webView
    }
    
    func updateUIView(_ webView: WKWebView, context: Context) {
        print("updateUIView",iframeLoaded)
       
        if iframeLoaded {
            return ;
        }
        webView.load(URLRequest(url: url))
    }
    func makeCoordinator() -> Coordinator {
        Coordinator(self, onEventsInjected: onEventsInjected)
    }
    class Coordinator: NSObject, WKScriptMessageHandler, WKNavigationDelegate {
        var parent: PulpoWebView
        let onEventsInjected: () -> Void
        init(_ parent: PulpoWebView, onEventsInjected: @escaping () -> Void) {
            self.parent = parent
            self.onEventsInjected = onEventsInjected
        }
        func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
            if message.name == "eventHandler",
               let body = (message.body as? [String: Any]),
               let event = body["event"] as? String,
               let data = body["data"],
               isEventType(event) {
                let handler = getEventHandler(events: parent.events)
                handler[event]!(data)
                if event == "onReady" {
                   
                    applyVariants()
                }}
        }
        @objc func applyVariants() {
            if let unwrappedWebView = parent.webView {
                let sdk = PulpoARSDK(webView: unwrappedWebView)
                print("Applying image")
                sdk.setImageToApply("/vto/images/face-model-women-8.png")
            } else {
                print("WebView is not available.")
            }
        }
        private func injectEvents() -> String {
            return getEventScript(events: parent.events)
        }
        func webView(_ webView: WKWebView, didFinish navigation: WKNavigation!) {
            parent.iframeLoaded = true
            onEventsInjected()
            webView.evaluateJavaScript(injectEvents(), completionHandler: nil)
        }
    }
}
