//
//  VariantView.swift
//  swift-hybrid-ui-example
//
//  Created by Ahmet Türkyılmaz on 19.12.2024.
//
import SwiftUI

struct VariantButtonView: View {
    var variants: [Variant]
    var onVariantSelected: (Variant) -> Void
    
    var body: some View {
        ScrollView(.horizontal) {
            HStack(spacing: 8) {
                ForEach(variants, id: \.id) { variant in

                    Button(action: {
                        onVariantSelected(variant)
                    }) {
                        Rectangle()
                            .fill(colorFromHex(variant.thumbnailColor ?? "#000000"))
                            .frame(width: 50, height: 50)
                            .cornerRadius(8)
                    }
                    }
                }
        }.padding(.horizontal,10)
       
    }
}
func colorFromHex(_ hex: String) -> Color {
    var hexSanitized = hex.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
    if hexSanitized.hasPrefix("#") {
        hexSanitized.remove(at: hexSanitized.startIndex)
    }
    
    guard hexSanitized.count == 6 else {
        return Color.black
    }
    
    let r = CGFloat(strtoul(String(hexSanitized.prefix(2)), nil, 16)) / 255.0
    let g = CGFloat(strtoul(String(hexSanitized.dropFirst(2).prefix(2)), nil, 16)) / 255.0
    let b = CGFloat(strtoul(String(hexSanitized.dropFirst(4).prefix(2)), nil, 16)) / 255.0
    
    return Color(red: r, green: g, blue: b)
}
