package com.pulpolabs.kotlin_example.data

import com.pulpolabs.kotlin_example.Utils
import org.json.JSONObject


data class PathChangePayload(
    val path: String,
    val referer: String

) {
    constructor(obj: JSONObject) : this(
        path = Utils.getObj<String>("path", obj),
        referer = Utils.getObj<String>("referer", obj)
    )
}
