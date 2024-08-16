package com.pulpolabs.kotlin_example.data

import com.pulpolabs.kotlin_example.Utils
import org.json.JSONObject

data class OpacitySlidePayload(
    val percentage: Int
) {
    constructor(obj: JSONObject) : this(
        percentage = Utils.getObj<Int>("percentage", obj)
    )
}
