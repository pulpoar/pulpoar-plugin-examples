//
//  ContentView.swift
//  swift-native-sdk-example
//

import SwiftUI

struct ContentView: View {
    @StateObject private var engine = PulpoEngine()
    @State private var selectedIds: Set<String> = []
    // nil = live camera.
    @State private var selectedModel: FaceModel?

    var body: some View {
        VStack(spacing: 16) {
            sources
            Spacer()
            statusLabel
            swatches
        }
        .padding(.vertical, 16)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .disabled(engine.status != .running)
        // As a background the frame sits behind the controls without widening the layout.
        .background {
            Color.black.ignoresSafeArea()
            PulpoFrameView(engine: engine)
        }
        .task { await engine.start() }
        // SwiftUI cancels the previous task when the id changes, so only the latest
        // selection is ever applied.
        .task(id: selectedModel) { await applySource() }
        .task(id: selectedIds) { await applyProducts() }
    }

    @ViewBuilder
    private var statusLabel: some View {
        switch engine.status {
        case .loading:
            ProgressView("Downloading face models…").tint(.white).foregroundStyle(.white)
        case .failed(let message):
            Text(message).foregroundStyle(.red)
        case .running where !engine.faceFound:
            Text("No face detected").foregroundStyle(.white)
        case .idle, .running:
            EmptyView()
        }
    }

    // MARK: - Camera / model picker

    private var sources: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 12) {
                Button { selectedModel = nil } label: {
                    Image(systemName: "camera.fill")
                        .foregroundStyle(.white)
                        .frame(width: 56, height: 56)
                        .background(Circle().fill(.black.opacity(0.5)))
                        .overlay(selectionRing(selectedModel == nil))
                }

                ForEach(FACE_MODELS) { model in
                    Button { selectedModel = model } label: {
                        AsyncImage(url: model.url) { image in
                            image.resizable().scaledToFill()
                        } placeholder: {
                            Color.gray.opacity(0.4)
                        }
                        .frame(width: 56, height: 56)
                        .clipShape(Circle())
                        .overlay(selectionRing(selectedModel == model))
                    }
                }
            }
            .padding(.horizontal, 16)
        }
    }

    private func applySource() async {
        guard engine.status == .running else { return }
        guard let model = selectedModel else {
            engine.showCamera()
            return
        }
        do {
            let image = try await FaceModelLoader.image(for: model)
            guard !Task.isCancelled else { return }
            engine.showPhoto(image)
        } catch {
            if !Task.isCancelled { print("[Pulpo] Failed to load model photo:", error) }
        }
    }

    // MARK: - Product swatches

    private var swatches: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 16) {
                ForEach(DEMO_VARIANTS) { variant in
                    Button {
                        if selectedIds.remove(variant.id) == nil { selectedIds.insert(variant.id) }
                    } label: {
                        VStack(spacing: 6) {
                            Circle()
                                .fill(Color(hex: variant.color))
                                .frame(width: 48, height: 48)
                                .overlay(selectionRing(selectedIds.contains(variant.id)))
                            Text(variant.name)
                                .font(.caption)
                                .foregroundStyle(.white)
                        }
                    }
                }
            }
            .padding(.horizontal, 16)
        }
    }

    private func applyProducts() async {
        guard engine.status == .running else { return }
        do {
            var configs: [[String: Any]] = []
            for id in selectedIds {
                configs.append(try await VariantAPI.config(for: id))
            }
            await engine.setProducts(configs)
        } catch {
            if !Task.isCancelled { print("[Pulpo] Failed to load variant config:", error) }
        }
    }

    private func selectionRing(_ isSelected: Bool) -> some View {
        Circle().stroke(.white, lineWidth: isSelected ? 3 : 0)
    }
}

private extension Color {
    init(hex: String) {
        let value = UInt64(hex.trimmingCharacters(in: CharacterSet(charactersIn: "#")), radix: 16) ?? 0
        self.init(
            red: Double((value >> 16) & 0xFF) / 255,
            green: Double((value >> 8) & 0xFF) / 255,
            blue: Double(value & 0xFF) / 255
        )
    }
}
