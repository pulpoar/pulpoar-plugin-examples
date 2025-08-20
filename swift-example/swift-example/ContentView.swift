//
//  ContentView.swift
//  swift-example
//
//

import SwiftUI

struct ContentView: View {
    var body: some View {
        PulpoARViewRepresentable(
            props: PulpoARProps(
                plugin: "vto",
                slug: "makeup",
                events: Events(
                    onError: { err in
                        print("[PulpoAR] onError (Swift):", err)
                    },
                    onReady: { app in
                        print("[PulpoAR] onReady (Swift) products=\(app.products.count)")
                    }
                )
            )
        )
    }
}
