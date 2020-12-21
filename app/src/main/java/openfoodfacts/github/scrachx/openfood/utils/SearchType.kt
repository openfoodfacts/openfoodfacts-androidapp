package openfoodfacts.github.scrachx.openfood.utils

/**
 * Created by Lobster on 10.03.18.
 */
enum class SearchType(val url: String) {
    ADDITIVE("additive"),
    ALLERGEN("allergen"),
    BRAND("brand"),
    CATEGORY("category"),
    COUNTRY("country"),
    EMB("emb"),
    LABEL("label"),
    PACKAGING("packaging"),
    SEARCH("search"),
    STORE("store"),
    TRACE("trace"),
    CONTRIBUTOR("contributor"),
    INCOMPLETE_PRODUCT("incomplete_product"),
    STATE("state"),
    ORIGIN("origin"),
    MANUFACTURING_PLACE("url");

    companion object {
        fun fromUrl(url: String?) = values().firstOrNull { it.url.equals(url, ignoreCase = true) }
    }
}