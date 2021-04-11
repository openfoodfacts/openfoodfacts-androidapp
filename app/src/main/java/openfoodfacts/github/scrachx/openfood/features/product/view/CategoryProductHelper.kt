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
import androidx.core.text.inSpans
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity.Companion.start
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName
import openfoodfacts.github.scrachx.openfood.network.WikiDataApiClient
import openfoodfacts.github.scrachx.openfood.utils.SearchType
import openfoodfacts.github.scrachx.openfood.utils.showBottomSheet

class CategoryProductHelper(
        private val categoryText: TextView,
        private val categories: List<CategoryName>,
        private val fragment: BaseFragment,
        private val apiClient: WikiDataApiClient,
        private val disp: CompositeDisposable
) {
    var containsAlcohol = false
        private set

    fun showCategories() = categoryText.let {
        it.movementMethod = LinkMovementMethod.getInstance()
        it.isClickable = true
        it.movementMethod = LinkMovementMethod.getInstance()

        val text = SpannableStringBuilder()
                .bold { append(fragment.getString(R.string.txtCategories)) }
                .append(" ")

        if (categories.isEmpty()) {
            it.visibility = View.GONE
        } else {
            it.visibility = View.VISIBLE
            // Add all the categories to text view and link them to wikidata is possible
            categories.forEach { category ->
                // Add category name to text view
                text.append(getCategoriesTag(category))

                // Add a comma if not the last item
                if (category != categories.last()) text.append(", ")

                if (category.categoryTag == "en:alcoholic-beverages") {
                    containsAlcohol = true
                }
            }
        }
        it.text = text
    }

    private fun getCategoriesTag(category: CategoryName): CharSequence {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                if (category.isWikiDataIdPresent == true) {
                    apiClient.doSomeThing(category.wikiDataId!!).subscribe { result ->
                        if (result != null) {
                            val activity = fragment.activity
                            if (activity != null && !activity.isFinishing) {
                                showBottomSheet(result, category, activity.supportFragmentManager)
                                return@subscribe
                            }
                        }
                        start(fragment.requireContext(), SearchType.CATEGORY, category.categoryTag!!, category.name!!)
                    }.addTo(disp)
                } else {
                    start(fragment.requireContext(), SearchType.CATEGORY, category.categoryTag!!, category.name!!)
                }
            }
        }

        val spannableStringBuilder = SpannableStringBuilder()
                .inSpans(clickableSpan) { append(category.name) }
        if (!category.isNotNull) {
            // Span to make text italic
            spannableStringBuilder.setSpan(StyleSpan(Typeface.ITALIC), 0, spannableStringBuilder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return spannableStringBuilder
    }

    fun showAlcoholAlert(alcoholAlertText: TextView) {
        val alcoholAlertIcon = ContextCompat.getDrawable(
                fragment.requireContext(),
                R.drawable.ic_alert_alcoholic_beverage
        )!!.apply {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        }
        val riskAlcoholConsumption = fragment.getString(R.string.risk_alcohol_consumption)
        alcoholAlertText.visibility = View.VISIBLE
        alcoholAlertText.text = SpannableStringBuilder().apply {
            inSpans(ImageSpan(alcoholAlertIcon, DynamicDrawableSpan.ALIGN_BOTTOM)) { append("-") }
            append(" ")
            inSpans(ForegroundColorSpan(ContextCompat.getColor(fragment.requireContext(), R.color.red)))
            { append(riskAlcoholConsumption) }

        }
    }
}