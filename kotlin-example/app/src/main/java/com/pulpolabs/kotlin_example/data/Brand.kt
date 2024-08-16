package com.pulpolabs.kotlin_example.data

import com.pulpolabs.kotlin_example.Utils
import org.json.JSONObject

data class Brand(
    val id: String,
    val image: String,
    val name: String,
    val slug: String?,
    val translations: ArrayList<Translation>
) {
    constructor(obj: JSONObject) : this(
        id = Utils.getObj<String>("id", obj),
        image = Utils.getObj<String>("image", obj),
        name = Utils.getObj<String>("name", obj),
        slug = Utils.getObj<String>("slug", obj),
        translations = Utils.getArrObj<Translation>(obj.getJSONArray("translations")) { json ->
            Translation(json)
        }
    )

}
