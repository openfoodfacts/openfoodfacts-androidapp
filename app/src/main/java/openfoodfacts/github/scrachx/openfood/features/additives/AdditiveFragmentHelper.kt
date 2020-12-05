package openfoodfacts.github.scrachx.openfood.features.additives

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.DynamicDrawableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.fasterxml.jackson.databind.JsonNode
import io.reactivex.disposables.CompositeDisposable
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.network.WikiDataApiClient
import openfoodfacts.github.scrachx.openfood.utils.SearchType
import openfoodfacts.github.scrachx.openfood.utils.Utils
import openfoodfacts.github.scrachx.openfood.utils.showBottomSheet

/**
 * Helper class for additive fragment
 */
object AdditiveFragmentHelper {
    /**
     * Show names of all additives on the TextView
     *
     * @param additives list of additive names
     * @param additiveProduct TextView which displays additive names
     * @param apiClientForWikiData object of WikidataApiClient
     */
    @JvmStatic
    fun showAdditives(
            additives: List<AdditiveName>,
            additiveProduct: TextView,
            apiClientForWikiData: WikiDataApiClient,
            fragment: BaseFragment,
            compositeDisposable: CompositeDisposable
    ) = with(additiveProduct) {
        text = Utils.bold(fragment.getString(R.string.txtAdditives))
        movementMethod = LinkMovementMethod.getInstance()
        append(" ")
        append("\n")
        isClickable = true
        movementMethod = LinkMovementMethod.getInstance()
        for (i in 0 until additives.size - 1) {
            append(getAdditiveTag(additives[i], apiClientForWikiData, fragment, compositeDisposable))
            append("\n")
        }
        append(getAdditiveTag(additives[additives.size - 1], apiClientForWikiData, fragment, compositeDisposable))
    }

    /**
     * Returns additive tag from additive name using WikidataApiClient
     *
     * @param additive name of the additive
     * @param apiClientForWikiData object of WikidataApiClient
     * @param fragment holds a reference to the calling fragment
     */
    private fun getAdditiveTag(additive: AdditiveName, apiClientForWikiData: WikiDataApiClient, fragment: BaseFragment, compositeDisposable: CompositeDisposable): CharSequence {
        val activity = fragment.requireActivity()
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                if (additive.isWikiDataIdPresent) {
                    compositeDisposable.add(apiClientForWikiData.doSomeThing(additive.wikiDataId).subscribe { result ->
                        getOnWikiResponse(activity, additive)(result)
                    })
                } else {
                    onWikiNoResponse(additive, activity)
                }
            }
        }

        return SpannableStringBuilder().also {
            it.append(additive.name)
            it.setSpan(clickableSpan, 0, it.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            // if the additive has an overexposure risk ("high" or "moderate") then append the warning message to it
            if (additive.hasOverexposureData()) {
                val isHighRisk = additive.overexposureRisk.equals("high", ignoreCase = true)

                val riskIcon =
                        if (isHighRisk) ContextCompat.getDrawable(activity, R.drawable.ic_additive_high_risk)
                        else ContextCompat.getDrawable(activity, R.drawable.ic_additive_moderate_risk)
                val riskWarningStr =
                        if (isHighRisk) fragment.getString(R.string.overexposure_high)
                        else fragment.getString(R.string.overexposure_moderate)
                val riskWarningColor =
                        if (isHighRisk) Utils.getColor(activity, R.color.overexposure_high)
                        else Utils.getColor(activity, R.color.overexposure_moderate)

                riskIcon!!.setBounds(0, 0, riskIcon.intrinsicWidth, riskIcon.intrinsicHeight)
                val iconSpan = ImageSpan(riskIcon, DynamicDrawableSpan.ALIGN_BOTTOM)
                it.append(" - ") // this will be replaced with the risk icon
                it.setSpan(iconSpan, it.length - 2, it.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                it.append(riskWarningStr)
                it.setSpan(ForegroundColorSpan(riskWarningColor), it.length - riskWarningStr.length, it.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
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