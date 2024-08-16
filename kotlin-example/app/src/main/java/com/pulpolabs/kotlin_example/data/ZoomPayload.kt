package com.pulpolabs.kotlin_example.data

import com.pulpolabs.kotlin_example.Utils
import org.json.JSONObject

data class ZoomPayload(
    val type: String, // "in" or "out"
    val value: Int
) {
    constructor(obj: JSONObject) : this(
        type = Utils.getObj<String>("type", obj),
        value = Utils.getObj<Int>("value", obj)

    )
}
