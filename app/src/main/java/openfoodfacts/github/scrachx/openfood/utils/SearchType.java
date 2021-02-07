package openfoodfacts.github.scrachx.openfood.utils;

import androidx.annotation.Nullable;

/**
 * Created by Lobster on 10.03.18.
 */
public enum SearchType {
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
    private final String url;

    SearchType(String url) {
        this.url = url;
    }

    @Nullable
    public static SearchType fromUrl(String url) {
        for (SearchType type : SearchType.values()) {
            if (type.getUrl().equalsIgnoreCase(url)) {
                return type;
            }
        }
        return null;
    }

    public String getUrl() {
        return url;
    }
}
