// Define your SwiftUI wrapper for the view controller

import WebKit
import SwiftUI

struct PulpoARViewRepresentable: UIViewControllerRepresentable {
    
    var props: PulpoARProps
    
    func makeUIViewController(context: Context) -> PulpoARView {
        return PulpoARView(props: props)
    }
    
    func updateUIViewController(_ uiViewController: PulpoARView, context: Context) {
        // Update the view controller if needed
    }
}
