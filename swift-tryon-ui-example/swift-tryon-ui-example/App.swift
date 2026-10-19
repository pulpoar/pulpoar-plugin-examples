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
        // Once, before any try-on is shown. Nothing is reported while consent is pending: set
        // `PulpoTryOn.analyticsConsent` when the user answers your consent prompt (here, the switch in the Cart tab).
        PulpoTryOn.configure(PulpoTryOnConfiguration(analyticsConsent: .pending))
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(cart)
        }
    }
}
