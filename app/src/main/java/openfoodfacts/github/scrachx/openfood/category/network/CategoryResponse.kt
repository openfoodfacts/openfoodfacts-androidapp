package openfoodfacts.github.scrachx.openfood.category.network

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Class for response received from CategoryNetworkService class
 */
data class CategoryResponse @JvmOverloads constructor(
        val count: Int = 0,
        val tags: List<Tag> = emptyList()
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Tag @JvmOverloads constructor(
            val id: String = "",
            val name: String = "",
            val url: String = "",
            val products: Int = 0
    )
}