package com.pulpolabs.kotlin_example.data

import com.pulpolabs.kotlin_example.Utils
import org.json.JSONObject

data class LookSelectPayload(
    val id: String,
    val image: String,
    val name: String,
    val translations: List<Translation>,
    val variants: List<Variant>
) {
    constructor(obj: JSONObject) : this(
        id = Utils.getObj<String>("id", obj),
        image = Utils.getObj<String>("image", obj),
        name = Utils.getObj<String>("name", obj),
        translations = Utils.getArrObj<Translation>(obj.getJSONArray("translations")) { json ->
            Translation(json)
        },
        variants = Utils.getArrObj<Variant>(obj.getJSONArray("variants")) { json ->
            Variant(json)
        }
    )
}
