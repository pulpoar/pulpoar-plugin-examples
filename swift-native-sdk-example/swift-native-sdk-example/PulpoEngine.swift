//
//  PulpoEngine.swift
//  swift-native-sdk-example
//
//  Thin wrapper around the native PulpoModule SDK: loads the face models, runs either
//  the live front camera or a still model photo through the engine and shows the
//  rendered frames in `frameView` (see PulpoFrameView.swift).
//

import AVFoundation
import Observation
import PulpoModule
import UIKit

// Face-detection models the engine downloads on init (same set the PulpoAR app uses).
private let FACE_MODEL_URLS = (
    landmark: "https://assets.pulpoar.com/vision/pulpo-module/models/landmark_model_310724.enc.gz",
    face: "https://assets.pulpoar.com/vision/pulpo-module/models/fd_model_110822.enc.gz",
    quality: "https://assets.pulpoar.com/vision/pulpo-module/models/quality_simplified_fp16.onnx.enc",
    skinSeg: "https://assets.pulpoar.com/vision/pulpo-module/models/skin_segmentation_221225_fp16.enc.gz"
)

// Square size camera frames are normalized to before entering the engine.
private let CAMERA_FRAME_SIZE: Int32 = 1080

// @unchecked Sendable: engine state is only touched on engineQueue, the session on cameraQueue,
// observed state on main.
@Observable
final class PulpoEngine: NSObject, @unchecked Sendable {
    enum Status: Equatable {
        case idle
        case loading
        case running
        case failed(String)
    }

    private(set) var status: Status = .idle
    private(set) var faceFound = true

    /// Where rendered frames are drawn. Show it with `PulpoFrameView(engine:)`.
    @ObservationIgnored let frameView = MetalFrameView.make()

    // The native engine is NOT thread-safe. Every PulpoModule call goes through this
    // serial queue, so product changes never race the per-frame setFrame/analyseFace/apply
    // sequence.
    @ObservationIgnored private let engineQueue = DispatchQueue(label: "pulpo.engine")
    // Camera frames arrive on their own queue and then wait their turn on engineQueue.
    // If the camera delivered straight onto engineQueue, frames would keep jumping ahead
    // of product changes whenever processing is slower than the camera (heavy makeup,
    // slower iPhones), and makeup changes would stall.
    @ObservationIgnored private let cameraQueue = DispatchQueue(label: "pulpo.camera")
    @ObservationIgnored private let session = AVCaptureSession()

    // Only touched on engineQueue. True while a still photo (model) is shown instead
    // of the live camera.
    @ObservationIgnored private var isPhotoMode = false
    @ObservationIgnored private var lastFaceFound: Bool?

    // MARK: - Lifecycle

    @MainActor
    func start() async {
        guard status == .idle else { return }

        guard await AVCaptureDevice.requestAccess(for: .video) else {
            status = .failed("Camera permission denied")
            return
        }

        status = .loading
        let error = await onEngineQueue { () -> String? in
            let initialized = PulpoModule.initFaceByUrl(
                withLandmarkModel: FACE_MODEL_URLS.landmark,
                faceUrl: FACE_MODEL_URLS.face,
                qualityUrl: FACE_MODEL_URLS.quality,
                skinSegUrl: FACE_MODEL_URLS.skinSeg,
                landmarkOpt: 0,
                faceOpt: 0,
                qualityOpt: 0,
                skinSegOpt: 0
            )
            guard initialized else { return "Face module init failed" }

            // Correct upper-eye rendering for mascara/eyeliner (off by default in the SDK).
            PulpoModule.setIsUpperEyePolynomialFix(true)
            PulpoModule.setGapCorrector(true)
            return self.configureCamera()
        }
        if let error {
            status = .failed(error)
            return
        }
        showCamera()
        status = .running
    }

    // MARK: - Source: camera or photo

    /// Switches to the live front camera.
    func showCamera() {
        engineQueue.async {
            self.isPhotoMode = false
            // reset() clears the engine's face tracking and frame geometry. Photos and
            // camera frames have different sizes, so without it makeup renders offset.
            PulpoModule.reset()
        }
        cameraQueue.async { self.session.startRunning() }
    }

    /// Stops the camera and runs a still photo through the engine.
    func showPhoto(_ image: UIImage) {
        cameraQueue.async { self.session.stopRunning() }
        engineQueue.async {
            self.isPhotoMode = true
            PulpoModule.reset()
            PulpoModule.setFrame(image.normalizedOrientation())
            self.render(faceFound: PulpoModule.analyseFace())
        }
    }

    // MARK: - Products

    /// Applies variant configs (the `config.config` object of a PulpoAR variant).
    /// Pass an empty array to remove all makeup. Does nothing if the calling task
    /// was cancelled (a newer selection replaced it) while textures were loading.
    func setProducts(_ configs: [[String: Any]]) async {
        await loadTextures(for: configs)

        guard !Task.isCancelled,
              let data = try? JSONSerialization.data(withJSONObject: configs),
              let json = String(data: data, encoding: .utf8) else { return }
        await onEngineQueue {
            // Workaround: setProducts doesn't drop products missing from a shorter list,
            // so clear first.
            PulpoModule.setProducts("[]", isEncrypted: false)
            PulpoModule.setProducts(json, isEncrypted: false)
            // The live loop picks products up on its next frame; a still photo must be re-rendered.
            if self.isPhotoMode { self.render() }
        }
    }

    /// Some products (blush, mascara, eyeliner…) reference textures that must be loaded
    /// into the engine before they render.
    private func loadTextures(for configs: [[String: Any]]) async {
        for texture in configs.flatMap({ $0["texture_ids_to_fetch"] as? [[String: String]] ?? [] }) {
            guard let id = texture["id"], let url = texture["url"] else { continue }
            await onEngineQueue {
                if !PulpoModule.isTextureLoaded(id) {
                    PulpoModule.setTextureEncryptedByUrl(url, textureId: id)
                }
            }
        }
    }

    // MARK: - Rendering

    /// Runs on engineQueue. Applies the products to the current frame and shows the result.
    private func render(faceFound: Bool? = nil) {
        PulpoModule.apply()
        // A GPU-ready buffer for the Metal view; no UIImage conversion needed.
        frameView?.display(PulpoModule.getResultFrameAsPixelBuffer())

        // Only bother SwiftUI when the face-found state actually changes.
        if let faceFound, faceFound != lastFaceFound {
            lastFaceFound = faceFound
            DispatchQueue.main.async { self.faceFound = faceFound }
        }
    }

    // MARK: - Camera

    /// Runs on engineQueue. Returns an error message on failure.
    private func configureCamera() -> String? {
        guard let device = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .front),
              let input = try? AVCaptureDeviceInput(device: device) else {
            return "Front camera not available"
        }

        let output = AVCaptureVideoDataOutput()
        output.videoSettings = [kCVPixelBufferPixelFormatTypeKey as String: kCVPixelFormatType_32BGRA]
        output.alwaysDiscardsLateVideoFrames = true
        output.setSampleBufferDelegate(self, queue: cameraQueue)

        session.beginConfiguration()
        session.sessionPreset = .hd1920x1080
        if session.canAddInput(input) { session.addInput(input) }
        if session.canAddOutput(output) { session.addOutput(output) }

        // Deliver upright, mirrored (selfie) frames so the engine gets what the user sees.
        if let connection = output.connection(with: .video) {
            if connection.isVideoRotationAngleSupported(90) {
                connection.videoRotationAngle = 90
            }
            if connection.isVideoMirroringSupported {
                connection.automaticallyAdjustsVideoMirroring = false
                connection.isVideoMirrored = true
            }
        }
        session.commitConfiguration()
        return nil
    }

    // MARK: - Helpers

    private func onEngineQueue<T>(_ work: @escaping () -> T) async -> T {
        await withCheckedContinuation { continuation in
            engineQueue.async { continuation.resume(returning: work()) }
        }
    }
}

// MARK: - Frame loop

extension PulpoEngine: AVCaptureVideoDataOutputSampleBufferDelegate {
    // Runs on cameraQueue for every camera frame. While this waits for engineQueue, the
    // camera drops newer frames instead of piling them up.
    func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
        guard let pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else { return }

        engineQueue.sync {
            guard !isPhotoMode else { return }
            PulpoModule.setFrameWith(pixelBuffer, targetSize: CAMERA_FRAME_SIZE)
            render(faceFound: PulpoModule.analyseFace())
        }
    }
}

private extension UIImage {
    /// Redraws the image so its pixels are upright (`.up`), which the engine expects.
    func normalizedOrientation() -> UIImage {
        guard imageOrientation != .up else { return self }
        return UIGraphicsImageRenderer(size: size, format: imageRendererFormat).image { _ in
            draw(in: CGRect(origin: .zero, size: size))
        }
    }
}
