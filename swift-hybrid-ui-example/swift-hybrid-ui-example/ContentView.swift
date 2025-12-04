//
//  ContentView.swift
//  swift-hybrid-ui-example
//
//  Created by Ahmet Türkyılmaz on 16.12.2024.
//

import SwiftUI

struct ContentView: View {
    var body: some View {
        VStack (spacing: 0){
            PulpoARView()
        }.frame(maxWidth: .infinity,maxHeight: .infinity)
    }
}

#Preview {
    ContentView()
}
