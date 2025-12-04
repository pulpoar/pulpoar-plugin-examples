import UIKit
import AVFoundation
import SwiftUI
import WebKit
// MARK: - SwiftUI View for Variant Buttons
struct PulpoARView: View {
    @State private var variants: [Variant] = []
    @State private var activeProducts: [Product] = []
    @State private var products: [Product] = []
    @State private var categories: [Category] = []
    @State private var selectedCategory: Category?
    @State private var activeVariants:[Variant]=[]
    @State private var iframeLoaded = false
    @State private var isLoading = true
    @State private var webView: WKWebView? = nil
    @State private var appliedVariants: [Variant] = []
    @State private var type: String = "Model"
    var body: some View {
        VStack (spacing: 0){
            ZStack (alignment: .top){
                VStack(spacing: 10) {
                    HStack(spacing: 20) {
                        Menu(type) {
                            Button(action: {
                                // Handle category selection
                                type = "Model"
                                if let unwrappedWebView = webView {
                                    let sdk = PulpoARSDK(webView: unwrappedWebView)
                                    print("Applying image")
                                    sdk.setPath("apply-photo")
                                    sdk.setImageToApply("/vto/images/face-model-women-8.png")
                                } else {
                                    print("WebView is not available.")
                                }
                            }) {
                                Text("Model")
                            }
                            Button(action: {
                                showCameraView()
                                type = "Camera"
                            }) {
                                Text("Camera")
                            }
                        }
                        .padding()
                        .background(Color.black)
                        .foregroundColor(.white)
                        .cornerRadius(100)
                        .frame(width: 200, height: 50)

                        if iframeLoaded {
                            Menu(selectedCategory?.name ?? "") {
                                ForEach(categories, id: \.id) { category in
                                    Button(action: {
                                        // Handle category selection
                                        selectedCategory = category
                                        activeProducts = products.filter { product in
                                            product.category.name == category.name
                                        }
                                        activeVariants = []
                                        print("Category selected: \(category.name)")
                                    }) {
                                        Text(category.name)
                                    }
                                }
                            }
                            .padding()
                            .background(Color.black)
                            .foregroundColor(.white)
                            .cornerRadius(100)
                            .frame(width: 200, height: 50)
                        }
                    }
                    .padding(.horizontal, 10)
                }
                .padding(.top, 10)
                .safeAreaInset(edge: .top) { Spacer().frame(height: 68) }

                VStack{
                    PulpoWebViewContainer(iframeLoaded: $iframeLoaded, webView: $webView, onEventsInjected: handleEventsInjected, events: Events(onReady: onReady))
                }.zIndex(-2)                        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)

                if iframeLoaded {
                    VStack{
                        Spacer()
                                if !activeVariants.isEmpty {
                                    VStack {
                                        VariantButtonView(variants: activeVariants) { selectedVariant in
                                            if let unwrappedWebView = webView {
                                                if let slug = selectedVariant.slug {
                                                    print("slug exists")
                                                    let sdk = PulpoARSDK(webView: unwrappedWebView)
                                                    let variantIdArr = [selectedVariant.slug ?? ""]
                                                    sdk.applyVariants(variantIdArr)
                                                } else {
                                                    print("no slug")
                                                }
                                            } else {
                                                print("WebView is not available.")
                                            }
                                        }
                                    }
                                    .frame(height: 80)
                                    .background(Color.white.opacity(0.5))
                                    .cornerRadius(8)
                                    .padding(.bottom, 10)
                                    .padding(.horizontal, 20)
                                    .frame(maxWidth: .infinity)
                                    .zIndex(-1)
                                }
                    }
                }
            }.frame(maxHeight: 700)
            VStack{
                ProductButtonView(products: activeProducts) { Product in
                    activeVariants = variants.filter({ Variant in
                        Variant.product.id == Product.id
                    })
                }
            }.frame(minHeight: 200)
        }.edgesIgnoringSafeArea(.all).frame(alignment: .top)

      
    }
    private func changePath(){
        requestCameraPermission()


    }
    private func onReady (appData:ApplicationData){
        guard variants.isEmpty else { return }
        var appProducts: [Product] = []
        let distinctVariants = appData.variants
            .filter { $0.slug != nil }
            .unique { $0.id == $1.id }

        for appVariant in distinctVariants {
            appProducts.append(appVariant.product)
        }
        appProducts = appProducts.unique{$0.id == $1.id }
        if !appProducts.isEmpty {
            products.append(contentsOf: appProducts)
        }
        variants.append(contentsOf: distinctVariants)
        let validCategories = appData.categories.filter { category in
            distinctVariants.contains { $0.product.category.id == category.id }
        }
        categories.append(contentsOf: validCategories)
        selectedCategory = validCategories.first
        activeProducts = products.filter({ Product in
            Product.category.name == selectedCategory?.name ?? "Blush"
        })
    
        
    }
    private func loadData() {
        // Initial data loading logic here if necessary
    }
    private func handleEventsInjected() {
        // Handle events injection logic when WebView loads
        iframeLoaded = true
        isLoading = false
    }
    func requestCameraPermission() {
        let cameraAuthorizationStatus = AVCaptureDevice.authorizationStatus(for: .video)
        
        switch cameraAuthorizationStatus {
        case .notDetermined: // First-time access
            AVCaptureDevice.requestAccess(for: .video) { granted in
                DispatchQueue.main.async {
                    if granted {
                        print("Camera access granted.")
                        // Proceed with camera usage, e.g., navigate to camera view
                        self.showCameraView()
                    } else {
                        print("Camera access denied.")
                        // Show an alert to inform the user about the denial
                        self.showPermissionDeniedAlert()
                    }
                }
            }
        case .restricted:
            print("Camera access restricted.")
            // Show an alert explaining that the device restrictions are preventing camera access
            self.showRestrictedAlert()
        case .denied:
            print("Camera access denied previously.")
            // Prompt the user to go to settings to enable camera access
            self.showSettingsAlert()
        case .authorized:
            print("Camera access already granted.")
            // Proceed with camera usage
            self.showCameraView()
        @unknown default:
            print("Unknown camera authorization status.")
            // Handle unexpected states
            self.showUnknownStateAlert()
        }
    }
    
    // Example helper methods for each case
    
    func showCameraView() {
        if let unwrappedWebView = webView {
            let sdk = PulpoARSDK(webView: unwrappedWebView)
            sdk.initCamera(mode: ["facingMode" : "user"])
            sdk.setPath("apply-live")
        } else {
            print("WebView is not available.")
        }
        print("Navigating to the camera view...")
    }
    
    func showPermissionDeniedAlert() {
        let alert = UIAlertController(title: "Camera Access Denied",
                                      message: "Please enable camera access in Settings to use this feature.",
                                      preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
        alert.addAction(UIAlertAction(title: "Settings", style: .default) { _ in
            if let appSettings = URL(string: UIApplication.openSettingsURLString) {
                UIApplication.shared.open(appSettings, options: [:], completionHandler: nil)
            }
        })
        // Present alert in your view controller
        UIApplication.shared.keyWindow?.rootViewController?.present(alert, animated: true, completion: nil)
    }
    
    func showRestrictedAlert() {
        let alert = UIAlertController(title: "Camera Restricted",
                                      message: "Camera access is restricted due to parental controls or device policies.",
                                      preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
        // Present alert in your view controller
        UIApplication.shared.keyWindow?.rootViewController?.present(alert, animated: true, completion: nil)
    }
    
    func showSettingsAlert() {
        let alert = UIAlertController(title: "Camera Access Required",
                                      message: "Please go to Settings and grant camera access to use this feature.",
                                      preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
        alert.addAction(UIAlertAction(title: "Settings", style: .default) { _ in
            if let appSettings = URL(string: UIApplication.openSettingsURLString) {
                UIApplication.shared.open(appSettings, options: [:], completionHandler: nil)
            }
        })
        // Present alert in your view controller
        UIApplication.shared.keyWindow?.rootViewController?.present(alert, animated: true, completion: nil)
    }
    
    func showUnknownStateAlert() {
        let alert = UIAlertController(title: "Unknown Error",
                                      message: "An unknown issue occurred while accessing the camera.",
                                      preferredStyle: .alert)
        alert.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
        // Present alert in your view controller
        UIApplication.shared.keyWindow?.rootViewController?.present(alert, animated: true, completion: nil)
    }

}
class WebViewModel: ObservableObject {
    @Published var iframeLoaded: Bool = false
    @Published var webView: WKWebView? = nil
}
struct PulpoWebViewContainer: View {
    @Binding var iframeLoaded: Bool
    @Binding var webView: WKWebView?
    var onEventsInjected: () -> Void
    var events: Events
    var body: some View {
        PulpoWebView(
            url: webViewURL(),
            iframeLoaded: $iframeLoaded,
            onEventsInjected: onEventsInjected,
            webView: $webView,
            events: events
        )
    }
    private func webViewURL() -> URL {
        let urlString = "https://plugin.pulpoar.com/vto/makeup?custom=true&catalog=true"
        guard let url = URL(string: urlString) else {
            fatalError("Invalid URL string: \(urlString)")
        }
        return url
    }
}
extension Array {
    func unique(selector:(Element,Element)->Bool) -> Array<Element> {
        return reduce(Array<Element>()){
            if let last = $0.last {
                return selector(last,$1) ? $0 : $0 + [$1]
            } else {
                return [$1]
            }
        }
    }
}

