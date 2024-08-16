package com.pulpolabs.kotlin_example.data

import com.pulpolabs.kotlin_example.Utils
import org.json.JSONObject

data class CameraTogglePayload(
    val activate: Boolean
) {
    constructor(obj: JSONObject) : this(
        activate = Utils.getObj<Boolean>("activate",obj)
    )
}
