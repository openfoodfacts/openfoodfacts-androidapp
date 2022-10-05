package openfoodfacts.github.scrachx.openfood.models.entities.allergen

import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import org.jetbrains.annotations.Contract

object AllergenHelper {
    @Contract(" -> new")
    private fun createEmpty() = Data(false, emptyList())

    fun computeUserAllergen(product: Product, userAllergens: List<AllergenName>): Data {
        if (userAllergens.isEmpty()) return createEmpty()

        if (ApiFields.StateTags.INGREDIENTS_COMPLETED.tag !in product.statesTags)
            return Data(true, emptyList())

        val productAllergens = (product.allergensHierarchy + product.tracesTags).toSet()

        val matchingAllergens = userAllergens
            .filter { it.allergenTag in productAllergens }
            .map { it.name }
            .toSet()

        return Data(false, matchingAllergens.toList())
    }

    data class Data(val incomplete: Boolean, val allergens: List<String>) {
        fun isEmpty() = !incomplete && allergens.isEmpty()
    }
}