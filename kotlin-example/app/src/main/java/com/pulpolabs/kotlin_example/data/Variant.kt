package com.pulpolabs.kotlin_example.data

import com.pulpolabs.kotlin_example.Utils
import org.json.JSONObject

data class Variant(
    val barcode: String?,
    val id: String,
    val image: String?,
    val name: String,
    val product: Product,
    val slug: String?,
    val thumbnailColor: String,
    val thumbnailImage: String?,
    val translations: List<Translation>,
    val webLink: String?

) {
    constructor(obj: JSONObject) : this(
        barcode = Utils.getObj<String>("barcode", obj, ""),
        id = Utils.getObj<String>("id", obj),
        image = Utils.getObj<String>("image", obj),
        name = Utils.getObj<String>("name", obj),
        product = Product(obj.getJSONObject("product")),
        slug = Utils.getObj<String>("slug", obj),
        thumbnailColor = Utils.getObj<String>("thumbnail_color", obj),
        thumbnailImage = Utils.getObj<String>("thumbnail_image", obj),

        translations = Utils.getArrObj<Translation>(obj.getJSONArray("translations")) { json ->
            Translation(json)
        },
        webLink = Utils.getObj<String>("web_link", obj),
    )

}