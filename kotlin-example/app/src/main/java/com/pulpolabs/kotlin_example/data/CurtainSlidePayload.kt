package com.pulpolabs.kotlin_example.data

import com.pulpolabs.kotlin_example.Utils
import org.json.JSONObject

data class CurtainSlidePayload(
    val posX: Int

) {
    constructor(obj: JSONObject) : this(
        posX = Utils.getObj<Int>("activate", obj)
    )
}
