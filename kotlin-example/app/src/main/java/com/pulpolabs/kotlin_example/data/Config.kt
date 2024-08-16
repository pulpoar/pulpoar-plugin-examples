package com.pulpolabs.kotlin_example.data

import com.pulpolabs.kotlin_example.Utils
import org.json.JSONObject

class Config(
    var config: ConfigDetails?,
    var module: String?
) {
    constructor(obj: JSONObject) : this(
        config = ConfigDetails(obj.getJSONObject("module")),
        module = obj.getString("module")
    )

    class ConfigDetails(
        var colors: ArrayList<Color>?,
        var opacityMultiplier: Int?,
    ) {
        constructor(obj: JSONObject) : this(
            opacityMultiplier = obj.getInt("opacity_multiplier"),
            colors = Utils.getArrObj<Color>(obj.getJSONArray("colors")) { json ->
                Color(json)
            }
        )
    }
}



class Color(
    var blend: Int,
    var color: String,
    var opacity: String

) {
    constructor(obj: JSONObject) : this(
        blend = obj.getInt("blend"),
        color = obj.getString("color"),
        opacity = obj.getString("opacity")
    )
}
