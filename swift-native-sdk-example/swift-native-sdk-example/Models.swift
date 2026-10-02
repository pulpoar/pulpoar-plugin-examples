//
//  Models.swift
//  swift-native-sdk-example
//
//  Preset face photos users can try makeup on instead of the live camera.
//

import UIKit

struct FaceModel: Identifiable, Hashable {
    let id: Int
    var url: URL { URL(string: "https://plugin.pulpoar.com/vto/images/face-model-women-\(id).webp")! }
}

let FACE_MODELS = (1...8).map { FaceModel(id: $0) }

@MainActor
enum FaceModelLoader {
    private static var cache: [Int: UIImage] = [:]

    static func image(for model: FaceModel) async throws -> UIImage {
        if let cached = cache[model.id] { return cached }

        let (data, _) = try await URLSession.shared.data(from: model.url)
        guard let image = UIImage(data: data) else { throw URLError(.cannotDecodeContentData) }
        cache[model.id] = image
        return image
    }
}
