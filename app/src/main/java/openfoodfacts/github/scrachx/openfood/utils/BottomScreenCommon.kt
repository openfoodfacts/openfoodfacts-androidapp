package openfoodfacts.github.scrachx.openfood.utils

import androidx.fragment.app.FragmentManager
import com.fasterxml.jackson.databind.JsonNode
import openfoodfacts.github.scrachx.openfood.features.product.view.attribute.ProductAttributeFragment
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelName

private fun FragmentManager.showBottomSheet(
    result: JsonNode?,
    id: Long,
    name: String?,
    searchType: SearchType,
    fragmentTag: String,
) = ProductAttributeFragment.newInstance(
    result,
    id,
    searchType,
    name
).show(this, fragmentTag)

fun showBottomSheet(
    result: JsonNode?,
    allergen: AllergenName,
    fragmentManager: FragmentManager,
) = fragmentManager.showBottomSheet(
    result,
    allergen.id,
    allergen.name,
    SearchType.ALLERGEN,
    "allergen_details_fragment"
)

fun showBottomSheet(
    result: JsonNode,
    category: CategoryName,
    fragmentManager: FragmentManager,
) = fragmentManager.showBottomSheet(
    result,
    category.id!!,
    category.name,
    SearchType.CATEGORY,
    "category_details_fragment"
)

fun showBottomSheet(
    result: JsonNode?,
    label: LabelName,
    fragmentManager: FragmentManager,
) = fragmentManager.showBottomSheet(
    result,
    label.id,
    label.name,
    SearchType.LABEL,
    "label_details_fragment"
)

fun FragmentManager.showBottomSheet(
    result: JsonNode?,
    additive: AdditiveName,
) = this.showBottomSheet(
    result,
    additive.id,
    additive.name,
    SearchType.ADDITIVE,
    "additive_details_fragment"
)