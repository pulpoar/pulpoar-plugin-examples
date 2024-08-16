package com.pulpolabs.kotlin_example.data

import com.pulpolabs.kotlin_example.Utils
import org.json.JSONObject

data class AppliedVariantsChangePayload(
    val triggererVariantId: String,
    val variants: ArrayList<Variant>
) {
    constructor(obj: JSONObject) : this(
        triggererVariantId = obj.getString("triggererVariantId"),
        variants = Utils.getArrObj<Variant>(obj.getJSONArray("variants")) { json ->
            Variant(json)
        }
    )

}
