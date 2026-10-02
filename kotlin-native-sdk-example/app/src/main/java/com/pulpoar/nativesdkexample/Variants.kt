package com.pulpoar.nativesdkexample

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

// Fetches product variant configs from the PulpoAR API. The engine only needs the
// variant's `config.config` object — this is what gets passed to setProducts().

private const val API_URL = "https://api.pulpoar.com"

data class Variant(val id: String, val name: String, val color: String)

// A few variants from the public "makeup" demo project.
val DEMO_VARIANTS = listOf(
    Variant("1e17bc16-2ae6-4a3a-9d53-45bdfed4bfe2", "Heroic", "#922152"),
    Variant("f9078949-7a97-40d5-ad34-2f5577d89e47", "Energize", "#D75A76"),
    Variant("177a1924-d271-4b62-bf76-84ecc117089d", "Scarlet", "#942227"),
    Variant("cb19ae48-3575-4ac5-ba9d-8f6e3bd454c2", "Blush", "#B46A58"),
    Variant("a2127c55-9023-4927-8fbf-88343f284d71", "Mascara", "#0F0F0F"),
)

object VariantApi {
    private val cache = mutableMapOf<String, JSONObject>()

    /** Returns the engine config for a variant id. */
    suspend fun config(variantId: String): JSONObject {
        synchronized(cache) { cache[variantId] }?.let { return it }

        val config = withContext(Dispatchers.IO) {
            val body = URL("$API_URL/items/vto_variants/$variantId?fields=config").readText()
            JSONObject(body).getJSONObject("data").getJSONObject("config").getJSONObject("config")
        }
        synchronized(cache) { cache[variantId] = config }
        return config
    }
}
