package com.pulpolabs.kotlin_example.data

import org.json.JSONObject

data class ExperienceSelectPayload(
    val type: String
){
    constructor(obj: JSONObject) : this(
        type = obj.getString("type")
    )
}