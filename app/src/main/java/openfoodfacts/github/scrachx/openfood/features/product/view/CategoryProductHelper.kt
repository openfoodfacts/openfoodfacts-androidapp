package openfoodfacts.github.scrachx.openfood.features.product.view

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.*
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import io.reactivex.disposables.CompositeDisposable
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity.Companion.start
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName
import openfoodfacts.github.scrachx.openfood.network.WikiDataApiClient
import openfoodfacts.github.scrachx.openfood.utils.SearchType
import openfoodfacts.github.scrachx.openfood.utils.Utils
import openfoodfacts.github.scrachx.openfood.utils.showBottomSheet

class CategoryProductHelper(
        private val categoryText: TextView,
        private val categories: List<CategoryName>,
        private val baseFragment: BaseFragment,
        private val apiClient: WikiDataApiClient,
        private val disp: CompositeDisposable
) {
    var containsAlcohol = false
        private set

    fun showCategories() {
        categoryText.text = Utils.bold(baseFragment.getString(R.string.txtCategories))
        categoryText.movementMethod = LinkMovementMethod.getInstance()
        categoryText.append(" ")
        categoryText.isClickable = true
        categoryText.movementMethod = LinkMovementMethod.getInstance()
        if (categories.isEmpty()) {
            categoryText.visibility = View.GONE
        } else {
            categoryText.visibility = View.VISIBLE
            // Add all the categories to text view and link them to wikidata is possible
            var i = 0
            val lastIndex = categories.size - 1
            while (i <= lastIndex) {
                val category = categories[i]
                val categoryName = getCategoriesTag(category)
                // Add category name to text view
                categoryText.append(categoryName)
                // Add a comma if not the last item
                if (i != lastIndex) {
                    categoryText.append(", ")
                }
                if (category.categoryTag != null && category.categoryTag == "en:alcoholic-beverages") {
                    containsAlcohol = true
                }
                i++
            }
        }
    }

    private fun getCategoriesTag(category: CategoryName): CharSequence {
        val spannableStringBuilder = SpannableStringBuilder()
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                if (category.isWikiDataIdPresent == true) {
                    disp.add(apiClient.doSomeThing(category.wikiDataId).subscribe { result ->
                        if (result != null) {
                            val activity = baseFragment.activity
                            if (activity != null && !activity.isFinishing) {
                                showBottomSheet(result, category, activity.supportFragmentManager)
                                return@subscribe
                            }
                        }
                        start(baseFragment.requireContext(), category.categoryTag, category.name, SearchType.CATEGORY)
                    })
                } else {
                    start(baseFragment.requireContext(), category.categoryTag, category.name, SearchType.CATEGORY)
                }
            }
        }
        spannableStringBuilder.append(category.name)
        spannableStringBuilder.setSpan(clickableSpan, 0, spannableStringBuilder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        if (!category.isNotNull) {
            val iss = StyleSpan(Typeface.ITALIC) //Span to make text italic
            spannableStringBuilder.setSpan(iss, 0, spannableStringBuilder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return spannableStringBuilder
    }

    fun showAlcoholAlert(alcoholAlertText: TextView) {
        val alcoholAlertIcon = ContextCompat.getDrawable(
                baseFragment.requireContext(),
                R.drawable.ic_alert_alcoholic_beverage
        )!!.apply {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        }
        val riskAlcoholConsumption = baseFragment.getString(R.string.risk_alcohol_consumption)
        alcoholAlertText.visibility = View.VISIBLE
        alcoholAlertText.text = SpannableStringBuilder().apply {
            append("- ")
            setSpan(
                    ImageSpan(alcoholAlertIcon, DynamicDrawableSpan.ALIGN_BOTTOM),
                    0,
                    1,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
            append(riskAlcoholConsumption)
            setSpan(
                    ForegroundColorSpan(Utils.getColor(baseFragment.context, R.color.red)),
                    length - riskAlcoholConsumption.length,
                    length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
}