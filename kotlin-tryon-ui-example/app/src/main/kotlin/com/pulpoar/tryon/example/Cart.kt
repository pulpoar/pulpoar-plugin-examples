package com.pulpoar.tryon.example

import androidx.compose.runtime.mutableStateListOf
import com.pulpoar.tryon.PulpoVariant

/** A stand-in for your app's cart, filled by the try-on's Add to Cart. */
class Cart {
    val items = mutableStateListOf<PulpoVariant>()

    fun add(variants: List<PulpoVariant>) {
        items.addAll(variants)
    }

    fun clear() = items.clear()
}
