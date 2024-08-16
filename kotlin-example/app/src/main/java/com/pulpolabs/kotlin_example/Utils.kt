package com.pulpolabs.kotlin_example

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

class Utils {
    companion object {
        fun <T> getArrObj(
            jsonArr: JSONArray,
            factory: (JSONObject) -> T
        ): ArrayList<T> {
            val arrObj: ArrayList<T> = arrayListOf()
            for (i in 0 until jsonArr.length()) {
                val json: JSONObject = jsonArr.getJSONObject(i)
                arrObj.add(factory(json))
            }
            return arrObj
        }

        inline fun <reified T> getObj(
            objName: String,
            json: JSONObject,
            defaultValue: T? = null  // Default value can be nullable
        ): T {
            return try {
                val result: T? = when (T::class) {
                    String::class -> json.optString(objName) as? T
                    Int::class -> json.optInt(objName) as? T
                    Boolean::class -> json.optBoolean(objName) as? T
                    else -> defaultValue  // Unsupported types use the default or fallback
                }

                // Ensure the return is non-null
                result ?: defaultValue
                ?: throw IllegalArgumentException("No valid value for $objName and no default provided")
            } catch (e: Exception) {
                Log.e("Error", "An error occurred: ${e.message}")
                defaultValue
                    ?: throw e  // Return the default value if available or rethrow the exception
            }
        }

    }

}