package openfoodfacts.github.scrachx.openfood.features.product.view

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.*
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.color
import androidx.core.text.inSpans
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName
import openfoodfacts.github.scrachx.openfood.network.WikiDataApiClient
import openfoodfacts.github.scrachx.openfood.utils.SearchType
import openfoodfacts.github.scrachx.openfood.utils.showBottomSheet

object CategoryProductHelper {

    fun showCategories(
        fragment: BaseFragment,
        categoryText: TextView,
        alcoholAlertText: TextView,
        categories: List<CategoryName>,
        apiClient: WikiDataApiClient,
    ) = categoryText.let { view ->
        if (categories.isEmpty()) {
            view.visibility = View.GONE
            return@let
        }

        view.visibility = View.VISIBLE
        view.movementMethod = LinkMovementMethod.getInstance()
        view.isClickable = true
        view.text = SpannableStringBuilder()
            .bold { append(fragment.getString(R.string.txtCategories)) }
            .append(" ")
            .apply {
                // Add all the categories to text view and link them to wikidata is possible
                categories.map { getCategoriesTag(it, fragment, apiClient) }.forEachIndexed { i, el ->
                    append(el)
                    if (i != categories.size) append(", ")
                }
            }

        if (categories.any { it.categoryTag == "en:alcoholic-beverages" }) {
            showAlcoholAlert(alcoholAlertText, fragment)
        }
    }

    private fun getCategoriesTag(
        category: CategoryName,
        fragment: BaseFragment,
        apiClient: WikiDataApiClient
    ): SpannableStringBuilder {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                if (category.isWikiDataIdPresent == true) {
                    fragment.lifecycleScope.launch {
                        val result = category.wikiDataId?.let { apiClient.doSomeThing(it) }
                        if (result != null) {
                            val activity = fragment.activity
                            if (activity != null && !activity.isFinishing) {
                                showBottomSheet(result, category, activity.supportFragmentManager)
                            }
                        } else ProductSearchActivity.start(
                            fragment.requireContext(),
                            SearchType.CATEGORY,
                            category.categoryTag!!,
                            category.name!!
                        )
                    }
                } else {
                    ProductSearchActivity.start(
                        fragment.requireContext(),
                        SearchType.CATEGORY,
                        category.categoryTag!!,
                        category.name!!
                    )
                }
            }
        }

        val span = SpannableStringBuilder()
            .inSpans(clickableSpan) { append(category.name) }
        if (category.isNull) {
            // Span to make text italic
            span.setSpan(
                StyleSpan(Typeface.ITALIC),
                0,
                span.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return span
    }

    private fun showAlcoholAlert(alcoholAlertText: TextView, fragment: BaseFragment) {
        val context = fragment.requireContext()
        val alcoholAlertIcon = ContextCompat.getDrawable(
            context,
            R.drawable.ic_alert_alcoholic_beverage
        )!!.apply {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        }
        val riskAlcoholConsumption = fragment.getString(R.string.risk_alcohol_consumption)
        alcoholAlertText.visibility = View.VISIBLE
        alcoholAlertText.text = SpannableStringBuilder()
            .inSpans(ImageSpan(alcoholAlertIcon, DynamicDrawableSpan.ALIGN_BOTTOM)) { append("-") }
            .append(" ")
            .color(ContextCompat.getColor(context, R.color.red)) {
                append(riskAlcoholConsumption)
            }
    }
}