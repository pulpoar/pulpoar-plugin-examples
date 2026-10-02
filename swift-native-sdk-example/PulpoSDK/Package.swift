// swift-tools-version:5.7
import PackageDescription

// The PulpoAR SDK as a Swift package. Xcode downloads the framework from the URL below.
let package = Package(
    name: "PulpoSDK",
    platforms: [.iOS("18.4")],
    products: [.library(name: "PulpoModule", targets: ["PulpoModule"])],
    targets: [
        .binaryTarget(
            name: "PulpoModule",
            url: "https://assets.pulpoar.com/vision/pulpo-module/builds/ExpoPulpoModule_v0.0.21/ios/PulpoModule.xcframework.zip",
            checksum: "d84a675dbc42620828b58d99a216f6d80ea9d30b0d5615268ee19418609d52ec"
        )
    ]
)
