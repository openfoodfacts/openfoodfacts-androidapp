package openfoodfacts.github.scrachx.openfood.models.entities.store

import openfoodfacts.github.scrachx.openfood.models.entities.EntityResponse


class StoreResponse (
    private var code: String,
    private var names: Map<String, String>,
    private val wikiDataCode: String? = null
) : EntityResponse<Store> {
    override fun map(): Store {
        val store: Store
        if (wikiDataCode != null) {
            store = Store(code, arrayListOf(), wikiDataCode)
            names.forEach { (key, value) ->
                store.names.add(StoreName(store.tag, key, value, wikiDataCode))
            }
        } else {
            store = Store(code, arrayListOf())
            names.forEach { (key, value) ->
                store.names.add(StoreName(store.tag, key, value))
            }
        }
        return store
    }
}