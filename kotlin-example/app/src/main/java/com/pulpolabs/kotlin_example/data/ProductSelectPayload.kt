package com.pulpolabs.kotlin_example.data

import com.pulpolabs.kotlin_example.Utils
import org.json.JSONObject

data class ProductSelectPayload(
    val brand: Brand,
    val category: Category,
    val id: String,
    val image: String,
    val name: String,
    val slug: String?,
    val translations: List<Translation>,
    val variants: List<Variant>
) {
    constructor(obj: JSONObject) : this(
        id = Utils.getObj<String>("id", obj),
        image = Utils.getObj<String>("image", obj),
        name = Utils.getObj<String>("name", obj),
        slug = Utils.getObj<String>("slug", obj),
        translations = Utils.getArrObj<Translation>(obj.getJSONArray("translations")) { json ->
            Translation(json)
        },
        variants = Utils.getArrObj<Variant>(obj.getJSONArray("translations")) { json ->
            Variant(json)
        },
        brand = Brand(obj.getJSONObject("brand")),
        category = Category(obj.getJSONObject("category"))
    )

}
