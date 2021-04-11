package openfoodfacts.github.scrachx.openfood.models.entities.brand

import openfoodfacts.github.scrachx.openfood.models.entities.EntityResponse

class BrandResponse (
    private var code: String,
    private var names: Map<String, String>,
    private val wikiDataCode: String? = null
) : EntityResponse<Brand> {
    override fun map() = if (wikiDataCode != null) {
        Brand(code, arrayListOf(), wikiDataCode).also {
            names.forEach { (key, value) ->
                it.names.add(BrandName(it.tag, key, value, wikiDataCode))
            }
        }
    } else {
        Brand(code, arrayListOf()).also {
            names.forEach { (key, value) ->
                it.names.add(BrandName(it.tag, key, value))
            }
        }
    }
}