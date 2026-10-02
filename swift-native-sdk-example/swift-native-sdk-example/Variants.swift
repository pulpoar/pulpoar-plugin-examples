//
//  Variants.swift
//  swift-native-sdk-example
//
//  Fetches product variant configs from the PulpoAR API. The engine only needs the
//  variant's `config.config` object — this is what gets passed to setProducts().
//

import Foundation

private let API_URL = "https://api.pulpoar.com"

struct Variant: Identifiable {
    let id: String
    let name: String
    let color: String
}

// A few variants from the public "makeup" demo project.
let DEMO_VARIANTS = [
    Variant(id: "1e17bc16-2ae6-4a3a-9d53-45bdfed4bfe2", name: "Heroic", color: "#922152"),
    Variant(id: "f9078949-7a97-40d5-ad34-2f5577d89e47", name: "Energize", color: "#D75A76"),
    Variant(id: "177a1924-d271-4b62-bf76-84ecc117089d", name: "Scarlet", color: "#942227"),
    Variant(id: "cb19ae48-3575-4ac5-ba9d-8f6e3bd454c2", name: "Blush", color: "#B46A58"),
    Variant(id: "a2127c55-9023-4927-8fbf-88343f284d71", name: "Mascara", color: "#0F0F0F"),
]

@MainActor
enum VariantAPI {
    private static var cache: [String: [String: Any]] = [:]

    /// Returns the engine config for a variant id.
    static func config(for variantId: String) async throws -> [String: Any] {
        if let cached = cache[variantId] { return cached }

        let url = URL(string: "\(API_URL)/items/vto_variants/\(variantId)?fields=config")!
        let (data, _) = try await URLSession.shared.data(from: url)
        let json = try JSONSerialization.jsonObject(with: data) as? [String: Any]
        let variantConfig = (json?["data"] as? [String: Any])?["config"] as? [String: Any]

        guard let config = variantConfig?["config"] as? [String: Any] else {
            throw URLError(.cannotParseResponse)
        }
        cache[variantId] = config
        return config
    }
}
