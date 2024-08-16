package com.pulpolabs.kotlin_example.data

import com.pulpolabs.kotlin_example.Utils
import org.json.JSONObject

data class GDPRApprovePayload(
    val approved: Boolean

) {
    constructor(obj: JSONObject) : this(
        approved = Utils.getObj<Boolean>("approved", obj)
    )
}
