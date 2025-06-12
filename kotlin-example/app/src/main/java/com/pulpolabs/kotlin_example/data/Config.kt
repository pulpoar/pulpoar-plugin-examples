package com.pulpolabs.kotlin_example.data

import com.pulpolabs.kotlin_example.Utils
import org.json.JSONObject

class Config(
    var config: ConfigDetails?,
    var module: String?,
    var category: String?
) {
    constructor(obj: JSONObject) : this(
        config = ConfigDetails(obj.getJSONObject("config")),
        module = obj.getString("module"),
        category = obj.optString("category")
    )

    class ConfigDetails(
        var colors: ArrayList<Color>?,
        var opacityMultiplier: Int?,
        var type: Int?,
        var subType: Int?,
        var config: JSONObject?,
        var textureIdsToFetch: ArrayList<JSONObject>?,
        var textureIdsToApply: ArrayList<JSONObject>?
    ) {
        constructor(obj: JSONObject) : this(
            opacityMultiplier = obj.optInt("opacity_multiplier"),
            type = obj.optInt("type"),
            subType = obj.optInt("sub_type"),
            config = obj.optJSONObject("config"),
            colors = if (obj.has("colors")) {
                Utils.getArrObj<Color>(obj.getJSONArray("colors")) { json ->
                    Color(json)
                }
            } else null,
            textureIdsToFetch = if (obj.has("texture_ids_to_fetch")) {
                Utils.getArrObj<JSONObject>(obj.getJSONArray("texture_ids_to_fetch")) { json ->
                    json
                }
            } else null,
            textureIdsToApply = if (obj.has("texture_ids_to_apply")) {
                Utils.getArrObj<JSONObject>(obj.getJSONArray("texture_ids_to_apply")) { json ->
                    json
                }
            } else null
        )
    }
}



class Color(
    var blend: Int,
    var color: String,
    var opacity: Double

) {
    constructor(obj: JSONObject) : this(
        blend = obj.getInt("blend"),
        color = obj.getString("color"),
        opacity = obj.getDouble("opacity")
    )
}
