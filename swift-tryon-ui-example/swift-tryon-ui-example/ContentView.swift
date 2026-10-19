//
//  ContentView.swift
//  swift-tryon-ui-example
//

import PulpoTryOnKit
import PulpoTryOnUI
import SwiftUI

/// Your PulpoAR Studio project. "makeup" is PulpoAR's demo project.
let projectSlug = "makeup"

/// The three usual places for a try-on: a full-screen cover from a product page, a pushed screen, and its own tab.
struct ContentView: View {
    @EnvironmentObject private var cart: Cart

    var body: some View {
        TabView {
            ProductPage()
                .tabItem { Label("Shop", systemImage: "bag") }

            // A tab: the try-on stays where it was while you visit another tab.
            PulpoTryOnView(projectSlug: projectSlug, configuration: .init(
                onAddToCart: { variants in cart.add(variants) }
            ))
            .tabItem { Label("Try On", systemImage: "face.smiling") }

            CartPage()
                .tabItem { Label("Cart (\(cart.items.count))", systemImage: "cart") }
        }
    }
}

/// A product page with two ways into the try-on.
struct ProductPage: View {
    @EnvironmentObject private var cart: Cart
    @State private var isTryOnPresented = false

    var body: some View {
        NavigationView {
            VStack(spacing: 16) {
                Image(systemName: "paintpalette")
                    .font(.system(size: 64))
                    .padding(.top, 40)
                Text("Your product page")
                    .font(.title2.bold())
                Text("Open the try-on full screen, or push it like any other screen.")
                    .multilineTextAlignment(.center)
                    .foregroundColor(.secondary)

                // Full screen: close it from the try-on's X button.
                Button("Try it on") { isTryOnPresented = true }
                    .buttonStyle(.borderedProminent)

                // Pushed: the navigation bar's back button leaves it.
                NavigationLink(destination: PulpoTryOnView(projectSlug: projectSlug, configuration: .init(
                    onAddToCart: { variants in cart.add(variants) }
                )).navigationBarTitleDisplayMode(.inline)) {
                    Text("Open as a screen")
                }
                Spacer()
            }
            .padding()
            .navigationTitle("Shop")
        }
        .navigationViewStyle(.stack)
        .fullScreenCover(isPresented: $isTryOnPresented) {
            PulpoTryOnView(projectSlug: projectSlug, configuration: .init(
                onAddToCart: { variants in
                    cart.add(variants)
                    isTryOnPresented = false
                },
                onClose: { isTryOnPresented = false },
                customizeTheme: { theme in
                    // Anything in the project's Studio theme can be changed here.
                    theme.catalog.withGoToProduct = false
                }
            ))
        }
    }
}

/// What Add to Cart collected, and a stand-in for your app's analytics consent prompt.
struct CartPage: View {
    @EnvironmentObject private var cart: Cart
    @State private var allowsAnalytics = PulpoTryOn.analyticsConsent == .granted

    var body: some View {
        NavigationView {
            List {
                Section {
                    if cart.items.isEmpty {
                        Text("Add a shade from the try-on.").foregroundColor(.secondary)
                    }
                    ForEach(Array(cart.items.enumerated()), id: \.offset) { _, variant in
                        Text(variant.name)
                    }
                }
                Section(footer: Text("Pass the user's answer to the SDK; until then nothing is reported.")) {
                    Toggle("Allow analytics", isOn: $allowsAnalytics)
                        .onChange(of: allowsAnalytics) { allowed in
                            PulpoTryOn.analyticsConsent = allowed ? .granted : .denied
                        }
                }
            }
            .navigationTitle("Cart")
            .toolbar {
                if !cart.items.isEmpty {
                    Button("Clear", action: cart.removeAll)
                }
            }
        }
        .navigationViewStyle(.stack)
    }
}
