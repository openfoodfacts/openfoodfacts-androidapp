package openfoodfacts.github.scrachx.openfood.models.entities.allergen

import openfoodfacts.github.scrachx.openfood.models.Product
import org.jetbrains.annotations.Contract
import java.util.*

object AllergenHelper {
    @Contract(" -> new")
    private fun createEmpty() = Data(false, emptyList<String>())

    fun computeUserAllergen(product: Product, userAllergens: List<AllergenName>): Data {
        if (userAllergens.isEmpty()) return createEmpty()

        if (!product.statesTags.contains("en:ingredients-completed")) return Data(true, emptyList<String>())

        val productAllergens = HashSet(product.allergensHierarchy)
        productAllergens.addAll(product.tracesTags)
        val allergenMatch = TreeSet<String?>()
        userAllergens.filter { productAllergens.contains(it.allergenTag) }.mapTo(allergenMatch) { it.name }
        return Data(false, allergenMatch.toList())
    }

    data class Data(val isIncomplete: Boolean, val allergens: List<String?>) {
        val isEmpty get() = !isIncomplete && allergens.isEmpty()
    }
}