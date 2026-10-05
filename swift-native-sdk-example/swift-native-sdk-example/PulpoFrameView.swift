//
//  PulpoFrameView.swift
//  swift-native-sdk-example
//
//  Shows the engine's rendered frames on screen with Metal. Each frame goes to the GPU
//  as a pixel buffer, so the main thread never has to convert or redraw a full-size
//  image 30 times a second.
//

import MetalKit
import SwiftUI

/// SwiftUI view that shows what `engine` renders (camera or photo, with makeup).
struct PulpoFrameView: UIViewRepresentable {
    let engine: PulpoEngine

    func makeUIView(context: Context) -> UIView {
        engine.frameView ?? UIView()
    }

    func updateUIView(_ uiView: UIView, context: Context) {}
}

/// Draws the latest pixel buffer it was given, fitted to the view (black bars around it).
final class MetalFrameView: MTKView, MTKViewDelegate {
    private var commandQueue: MTLCommandQueue?
    private var textureCache: CVMetalTextureCache?
    private var pipelineState: MTLRenderPipelineState?

    private let lock = NSLock()
    private var latestBuffer: CVPixelBuffer?
    private var isDrawScheduled = false

    /// Nil only on devices without Metal (every supported iPhone has it).
    static func make() -> MetalFrameView? {
        guard let device = MTLCreateSystemDefaultDevice() else { return nil }
        let view = MetalFrameView(frame: .zero, device: device)
        return view.setUp(device: device) ? view : nil
    }

    /// Call from any thread. Only the newest frame is drawn: if the main thread is busy,
    /// older frames are skipped instead of queueing up behind it.
    func display(_ pixelBuffer: CVPixelBuffer) {
        lock.lock()
        latestBuffer = pixelBuffer
        let shouldSchedule = !isDrawScheduled
        isDrawScheduled = true
        lock.unlock()

        guard shouldSchedule else { return }
        DispatchQueue.main.async {
            self.lock.lock()
            self.isDrawScheduled = false
            self.lock.unlock()
            self.draw()
        }
    }

    private func setUp(device: MTLDevice) -> Bool {
        guard let queue = device.makeCommandQueue() else { return false }
        commandQueue = queue

        var cache: CVMetalTextureCache?
        CVMetalTextureCacheCreate(kCFAllocatorDefault, nil, device, nil, &cache)
        guard let cache else { return false }
        textureCache = cache

        // A full-screen quad, shrunk by `scale` so the square frame fits the view.
        let shaderSource = """
        #include <metal_stdlib>
        using namespace metal;

        struct VertexOut {
            float4 position [[position]];
            float2 texCoord;
        };

        vertex VertexOut frame_vertex(uint id [[vertex_id]], constant float2 &scale [[buffer(0)]]) {
            const float2 positions[4] = { float2(-1, -1), float2(1, -1), float2(-1, 1), float2(1, 1) };
            const float2 texCoords[4] = { float2(0, 1), float2(1, 1), float2(0, 0), float2(1, 0) };
            VertexOut out;
            out.position = float4(positions[id] * scale, 0, 1);
            out.texCoord = texCoords[id];
            return out;
        }

        fragment float4 frame_fragment(VertexOut in [[stage_in]], texture2d<float> tex [[texture(0)]]) {
            constexpr sampler s(mag_filter::linear, min_filter::linear);
            return tex.sample(s, in.texCoord);
        }
        """
        guard let library = try? device.makeLibrary(source: shaderSource, options: nil) else { return false }

        let descriptor = MTLRenderPipelineDescriptor()
        descriptor.vertexFunction = library.makeFunction(name: "frame_vertex")
        descriptor.fragmentFunction = library.makeFunction(name: "frame_fragment")
        descriptor.colorAttachments[0].pixelFormat = .bgra8Unorm
        guard let state = try? device.makeRenderPipelineState(descriptor: descriptor) else { return false }
        pipelineState = state

        delegate = self
        colorPixelFormat = .bgra8Unorm
        clearColor = MTLClearColor(red: 0, green: 0, blue: 0, alpha: 1)
        framebufferOnly = true
        // Draw only when a new frame arrives, not on a timer.
        isPaused = true
        enableSetNeedsDisplay = false
        return true
    }

    func mtkView(_ view: MTKView, drawableSizeWillChange size: CGSize) {
        // Redraw the last frame at the new size (matters for a still photo).
        DispatchQueue.main.async { self.draw() }
    }

    func draw(in view: MTKView) {
        lock.lock()
        let pixelBuffer = latestBuffer
        lock.unlock()

        guard let pixelBuffer, let textureCache, let commandQueue, let pipelineState,
              let drawable = currentDrawable, let passDescriptor = currentRenderPassDescriptor else { return }

        let width = CVPixelBufferGetWidth(pixelBuffer)
        let height = CVPixelBufferGetHeight(pixelBuffer)
        var cvTexture: CVMetalTexture?
        CVMetalTextureCacheCreateTextureFromImage(kCFAllocatorDefault, textureCache, pixelBuffer, nil, .bgra8Unorm, width, height, 0, &cvTexture)
        guard let cvTexture, let texture = CVMetalTextureGetTexture(cvTexture),
              let commandBuffer = commandQueue.makeCommandBuffer(),
              let encoder = commandBuffer.makeRenderCommandEncoder(descriptor: passDescriptor) else { return }

        // Fit the frame inside the view, keeping its shape.
        let frameAspect = Float(width) / Float(height)
        let viewAspect = Float(drawableSize.width / drawableSize.height)
        var scale = frameAspect > viewAspect
            ? SIMD2<Float>(1, viewAspect / frameAspect)
            : SIMD2<Float>(frameAspect / viewAspect, 1)

        encoder.setRenderPipelineState(pipelineState)
        encoder.setVertexBytes(&scale, length: MemoryLayout<SIMD2<Float>>.size, index: 0)
        encoder.setFragmentTexture(texture, index: 0)
        encoder.drawPrimitives(type: .triangleStrip, vertexStart: 0, vertexCount: 4)
        encoder.endEncoding()
        commandBuffer.present(drawable)
        commandBuffer.commit()

        // The SDK creates a new pixel buffer every frame, so drop the cached texture
        // instead of letting the cache grow.
        CVMetalTextureCacheFlush(textureCache, 0)
    }
}
