package com.pulpolabs.kotlin_example.data

import com.pulpolabs.kotlin_example.Utils
import org.json.JSONObject

data class ModelSelectPayload(
    val id: Int,
    val image: String
) {
    constructor(obj: JSONObject) : this(
        id = Utils.getObj<Int>("id", obj),
        image = Utils.getObj<String>("image", obj)
    )
}
