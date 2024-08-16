package com.pulpolabs.kotlin_example.data

import com.pulpolabs.kotlin_example.Utils
import org.json.JSONObject

data class Translation(
    val description: String,
    val id: Int,
    val languagesCode: String,
    val name: String,
) {
    constructor(obj: JSONObject) : this(
        description = Utils.getObj<String>("description", obj),
        id = Utils.getObj<Int>("id", obj),
        languagesCode = Utils.getObj<String>("languages_code", obj),
        name = Utils.getObj<String>("name", obj),
    )
}
