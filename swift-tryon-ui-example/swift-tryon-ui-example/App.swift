//
//  App.swift
//  swift-tryon-ui-example
//

import PulpoTryOnKit
import SwiftUI

@main
struct SwiftTryOnUIExampleApp: App {
    @StateObject private var cart = Cart()

    init() {
        // Once, before any try-on is shown. Ask your users for analytics consent and pass their answer here.
        PulpoTryOn.configure(PulpoTryOnConfiguration(analyticsConsent: .granted))
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(cart)
        }
    }
}
