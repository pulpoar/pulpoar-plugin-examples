package com.pulpolabs.kotlin_example.data

import com.pulpolabs.kotlin_example.Utils
import org.json.JSONObject

data class TryNowClickPayload(
    val fallback: String
) {
    constructor(obj: JSONObject) : this(
        fallback = Utils.getObj<String>("fallback", obj),
    )
}
