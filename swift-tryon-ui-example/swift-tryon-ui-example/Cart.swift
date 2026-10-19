//
//  Cart.swift
//  swift-tryon-ui-example
//

import PulpoTryOnKit
import SwiftUI

/// A stand-in for your app's cart: the try-on's Add to Cart buttons put shades here.
@MainActor
final class Cart: ObservableObject {
    @Published private(set) var items: [PulpoVariant] = []

    func add(_ variants: [PulpoVariant]) {
        items.append(contentsOf: variants)
    }

    func removeAll() {
        items.removeAll()
    }
}
