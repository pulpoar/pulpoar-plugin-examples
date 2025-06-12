package com.pulpolabs.kotlin_example.data

import com.pulpolabs.kotlin_example.Utils
import org.json.JSONObject

data class AddToCartPayload(
    val barcode: String?,
    val config: Config,
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
        barcode = obj.optString("barcode"),
        id = obj.getString("id"),
        config = Config(obj.getJSONObject("config")),
        image = obj.optString("image"),
        name = obj.getString("name"),
        product = Product(obj.getJSONObject("product")),
        slug = obj.optString("slug"),
        thumbnailColor = obj.optString("thumbnail_color"),
        thumbnailImage = obj.optString("thumbnail_image"),
        webLink = obj.optString("web_link"),
        translations = Utils.getArrObj<Translation>(obj.getJSONArray("translations")) { json ->
            Translation(json)
        })
}
