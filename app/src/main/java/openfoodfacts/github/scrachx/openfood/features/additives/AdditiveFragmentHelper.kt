package openfoodfacts.github.scrachx.openfood.features.additives

import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.color
import androidx.core.text.inSpans
import androidx.fragment.app.FragmentActivity
import com.fasterxml.jackson.databind.JsonNode
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.network.WikiDataApiClient
import openfoodfacts.github.scrachx.openfood.utils.SearchType
import openfoodfacts.github.scrachx.openfood.utils.showBottomSheet

/**
 * Helper class for additive fragment
 */
object AdditiveFragmentHelper {
    /**
     * Show names of all additives on the TextView
     *
     * @param additives list of additive names
     * @param additivesView TextView which displays additive names
     * @param apiClientForWikiData object of WikidataApiClient
     */
    @JvmStatic
    fun showAdditives(
            additives: List<AdditiveName>,
            additivesView: TextView,
            apiClientForWikiData: WikiDataApiClient,
            fragment: BaseFragment,
            compositeDisposable: CompositeDisposable
    ) = additivesView.run {
        movementMethod = LinkMovementMethod.getInstance()
        isClickable = true
        text = SpannableStringBuilder()
                .bold { append(fragment.getString(R.string.txtAdditives)) }
                .apply {
                    additives.forEach {
                        append("\n")
                        append(getAdditiveTag(it,
                                apiClientForWikiData,
                                fragment,
                                compositeDisposable))
                    }
                }
    }

    /**
     * Returns additive tag from additive name using WikidataApiClient
     *
     * @param additive name of the additive
     * @param apiClientForWikiData object of WikidataApiClient
     * @param fragment holds a reference to the calling fragment
     */
    private fun getAdditiveTag(additive: AdditiveName,
                               apiClientForWikiData: WikiDataApiClient,
                               fragment: BaseFragment,
                               compositeDisposable: CompositeDisposable): CharSequence {
        val activity = fragment.requireActivity()
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                if (additive.isWikiDataIdPresent) {
                    apiClientForWikiData.doSomeThing(additive.wikiDataId).subscribe { result ->
                        getOnWikiResponse(activity, additive)(result)
                    }.addTo(compositeDisposable)
                } else {
                    onWikiNoResponse(additive, activity)
                }
            }
        }

        return SpannableStringBuilder().also {
            it.inSpans(clickableSpan) { append(additive.name) }

            // if the additive has an overexposure risk ("high" or "moderate") then append the warning message to it
            if (additive.hasOverexposureData()) {
                val isHighRisk = additive.overexposureRisk.equals("high", true)

                val riskIcon = (
                        if (isHighRisk) ContextCompat.getDrawable(activity, R.drawable.ic_additive_high_risk)
                        else ContextCompat.getDrawable(activity, R.drawable.ic_additive_moderate_risk)
                        )?.apply {
                            setBounds(0, 0, this.intrinsicWidth, this.intrinsicHeight)
                        }!!
                val riskWarningStr =
                        if (isHighRisk) fragment.getString(R.string.overexposure_high)
                        else fragment.getString(R.string.overexposure_moderate)
                val riskWarningColor =
                        if (isHighRisk) ContextCompat.getColor(activity, R.color.overexposure_high)
                        else ContextCompat.getColor(activity, R.color.overexposure_moderate)

                it.append(" ")
                it.inSpans(ImageSpan(riskIcon, DynamicDrawableSpan.ALIGN_BOTTOM)) { it.append("-") }
                it.append(" ")
                it.color(riskWarningColor) { append(riskWarningStr) }
            }
        }
    }

    private fun onWikiNoResponse(additive: AdditiveName, activity: FragmentActivity) {
        if (additive.hasOverexposureData() && !activity.isFinishing) {
            showBottomSheet(null, additive, activity.supportFragmentManager)
        } else {
            ProductSearchActivity.start(activity, SearchType.ADDITIVE, additive.additiveTag, additive.name)
        }
    }

    private fun getOnWikiResponse(activity: FragmentActivity, additive: AdditiveName) = { result: JsonNode ->
        if (!activity.isFinishing) {
            showBottomSheet(result, additive, activity.supportFragmentManager)
        }
    }
}