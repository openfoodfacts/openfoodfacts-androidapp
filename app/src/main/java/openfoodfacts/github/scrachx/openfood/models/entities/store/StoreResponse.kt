package openfoodfacts.github.scrachx.openfood.models.entities.store

import openfoodfacts.github.scrachx.openfood.models.entities.EntityResponse


class StoreResponse (
    private var code: String,
    private var names: Map<String, String>,
    private val wikiDataCode: String? = null
) : EntityResponse<Store> {
    override fun map() = if (wikiDataCode != null) {
        Store(code, arrayListOf(), wikiDataCode).also {
            names.forEach { (key, value) ->
                it.names.add(StoreName(it.tag, key, value, wikiDataCode))
            }
        }
    } else {
        Store(code, arrayListOf()).also {
            names.forEach { (key, value) ->
                it.names.add(StoreName(it.tag, key, value))
            }
        }
    }
}