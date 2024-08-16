package com.pulpolabs.kotlin_example.data

import com.pulpolabs.kotlin_example.Utils
import org.json.JSONObject

data class CurtainTogglePayload(
    val active: Boolean
) {
    constructor(obj: JSONObject) : this(
        active = Utils.getObj<Boolean>("active", obj)
    )
}
