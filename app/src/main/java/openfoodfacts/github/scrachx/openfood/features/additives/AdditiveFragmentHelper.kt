package openfoodfacts.github.scrachx.openfood.features.additives

import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.inSpans
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.fasterxml.jackson.databind.JsonNode
import kotlinx.coroutines.launch
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
        fragment: BaseFragment
    ) = additivesView.run {
        movementMethod = LinkMovementMethod.getInstance()
        isClickable = true
        text = buildSpannedString {
            bold { append(fragment.getString(R.string.txtAdditives)) }

            additives.forEach {
                append("\n")
                append(getAdditiveTag(it, apiClientForWikiData, fragment))
            }
        }
    }

    /**
     * Returns additive tag from additive name using WikidataApiClient
     *
     * @param additive name of the additive
     * @param wikidataClient object of WikidataApiClient
     * @param fragment holds a reference to the calling fragment
     */
    private fun getAdditiveTag(
        additive: AdditiveName,
        wikidataClient: WikiDataApiClient,
        fragment: BaseFragment
    ): CharSequence {
        val activity = fragment.requireActivity()
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                if (additive.isWikiDataIdPresent) {
                    fragment.lifecycleScope.launch {
                        val result = wikidataClient.getEntityData(additive.wikiDataId)
                        getOnWikiResponse(activity, additive)(result)
                    }
                } else {
                    onWikiNoResponse(additive, activity)
                }
            }
        }

        return buildSpannedString {
            inSpans(clickableSpan) { append(additive.name) }

            // if the additive has an overexposure risk ("high" or "moderate") then append the warning message to it
            if (additive.hasOverexposureData()) {
                val isHighRisk = additive.overexposureRisk.equals("high", true)

                val riskIcon = if (isHighRisk)
                    ContextCompat.getDrawable(activity, R.drawable.ic_additive_high_risk)!!
                else
                    ContextCompat.getDrawable(activity, R.drawable.ic_additive_moderate_risk)!!
                riskIcon.setBounds(0, 0, riskIcon.intrinsicWidth, riskIcon.intrinsicHeight)

                val riskWarningStr =
                    if (isHighRisk) fragment.getString(R.string.overexposure_high)
                    else fragment.getString(R.string.overexposure_moderate)

                val riskWarningColor =
                    if (isHighRisk) ContextCompat.getColor(activity, R.color.overexposure_high)
                    else ContextCompat.getColor(activity, R.color.overexposure_moderate)

                append(" ")
                inSpans(ImageSpan(riskIcon, DynamicDrawableSpan.ALIGN_BOTTOM)) { append("-") }
                append(" ")
                color(riskWarningColor) { append(riskWarningStr) }
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