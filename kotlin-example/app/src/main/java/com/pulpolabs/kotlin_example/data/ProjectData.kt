package com.pulpolabs.kotlin_example.data

import com.pulpolabs.kotlin_example.Utils
import org.json.JSONObject

data class ProjectData(
    val brands: List<Brand>,
    val categories: List<Category>,
    val module: String,
    val products: List<Product>,
    val projectId: String,
    val themeType: String,
    val variants: List<Variant>

) {
    constructor(obj: JSONObject) : this(
        brands = Utils.getArrObj<Brand>(obj.getJSONArray("brands")) { json ->
            Brand(json)
        },
        categories = Utils.getArrObj<Category>(obj.getJSONArray("categories")) { json ->
            Category(json)
        },
        module = Utils.getObj<String>("module", obj),
        products = Utils.getArrObj<Product>(obj.getJSONArray("products")) { json ->
            Product(json)
        },
        projectId = Utils.getObj<String>("projectId", obj),
        themeType = Utils.getObj<String>("themeType", obj),
        variants = Utils.getArrObj<Variant>(obj.getJSONArray("variants")) { json ->
            Variant(json)
        }
    )
}
