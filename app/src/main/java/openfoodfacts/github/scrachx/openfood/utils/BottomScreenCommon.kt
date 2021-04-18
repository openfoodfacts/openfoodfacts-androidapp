package openfoodfacts.github.scrachx.openfood.utils

import androidx.fragment.app.FragmentManager
import com.fasterxml.jackson.databind.JsonNode
import openfoodfacts.github.scrachx.openfood.features.product.view.attribute.ProductAttributeFragment
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelName

private fun showBottomSheet(
        result: JsonNode?,
        id: Long,
        name: String?,
        searchType: SearchType,
        fragmentTag: String,
        fragmentManager: FragmentManager
) = ProductAttributeFragment.newInstance(result, id, searchType, name).show(fragmentManager, fragmentTag)

fun showBottomSheet(
        result: JsonNode,
        allergen: AllergenName,
        fragmentManager: FragmentManager
) = showBottomSheet(
        result,
        allergen.id,
        allergen.name,
        SearchType.ALLERGEN,
        "allergen_details_fragment",
        fragmentManager
)

fun showBottomSheet(
        result: JsonNode,
        category: CategoryName,
        fragmentManager: FragmentManager
) = showBottomSheet(
        result,
        category.id!!,
        category.name,
        SearchType.CATEGORY,
        "category_details_fragment",
        fragmentManager
)

fun showBottomSheet(
        result: JsonNode,
        label: LabelName,
        fragmentManager: FragmentManager
) = showBottomSheet(
        result,
        label.id,
        label.name,
        SearchType.LABEL,
        "label_details_fragment",
        fragmentManager
)

fun showBottomSheet(
        result: JsonNode?,
        additive: AdditiveName,
        fragmentManager: FragmentManager
) = showBottomSheet(
        result,
        additive.id,
        additive.name,
        SearchType.ADDITIVE,
        "additive_details_fragment",
        fragmentManager
)