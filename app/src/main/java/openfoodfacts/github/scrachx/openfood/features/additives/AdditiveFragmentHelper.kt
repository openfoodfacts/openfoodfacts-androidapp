package openfoodfacts.github.scrachx.openfood.features.additives

import android.text.method.LinkMovementMethod
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.inSpans
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.repositories.WikidataRepository
import openfoodfacts.github.scrachx.openfood.utils.ClickableSpan
import openfoodfacts.github.scrachx.openfood.utils.SearchType
import openfoodfacts.github.scrachx.openfood.utils.showBottomSheet

/**
 * Helper class for additive fragment
 */
object AdditiveFragmentHelper {
    /**
     * Show names of all additives on the TextView
     *
     * @param additiveNames list of additive names
     * @param textView TextView which displays additive names
     * @param wikidataRepository object of WikidataApiClient
     */
    @JvmStatic
    fun showAdditives(
        textView: TextView,
        additiveNames: List<AdditiveName>,
        wikidataRepository: WikidataRepository,
        fragment: Fragment,
    ) {
        textView.run {
            movementMethod = LinkMovementMethod.getInstance()
            isClickable = true
            text = buildSpannedString {
                bold { append(fragment.getString(R.string.txtAdditives)) }

                additiveNames.forEach {
                    append("\n")
                    append(getAdditiveTag(it, wikidataRepository, fragment))
                }
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
        wikidataClient: WikidataRepository,
        fragment: Fragment,
    ): CharSequence {
        val activity = fragment.requireActivity()
        val clickableSpan = ClickableSpan {
            if (additive.isWikiDataIdPresent) {
                showAdditive(fragment, wikidataClient, additive, activity)
            } else {
                onWikiNoResponse(additive, activity)
            }
        }

        return buildSpannedString {
            inSpans(clickableSpan) { append(additive.name) }

            // if the additive has an overexposure risk ("high" or "moderate") then append the warning message to it
            if (additive.hasOverexposureData()) {
                val isHighRisk = additive.overexposureRisk.equals("high", true)

                val riskIcon = if (isHighRisk) {
                    ContextCompat.getDrawable(activity, R.drawable.ic_additive_high_risk)!!
                } else {
                    ContextCompat.getDrawable(activity, R.drawable.ic_additive_moderate_risk)!!
                }.apply {
                    setBounds(0, 0, intrinsicWidth, intrinsicHeight)
                }

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

    private fun showAdditive(
        fragment: Fragment,
        wikidataRepository: WikidataRepository,
        additiveName: AdditiveName,
        activity: FragmentActivity,
    ) {
        // TODO: We should not use launch like this
        fragment.lifecycleScope.launch {
            val result = wikidataRepository.getEntityData(additiveName.wikiDataId)
            if (!activity.isFinishing) {
                activity.supportFragmentManager.showBottomSheet(result, additiveName)
            }
        }
    }

    private fun onWikiNoResponse(additive: AdditiveName, activity: FragmentActivity) {
        if (additive.hasOverexposureData() && !activity.isFinishing) {
            activity.supportFragmentManager.showBottomSheet(null, additive)
        } else {
            ProductSearchActivity.start(
                context = activity,
                type = SearchType.ADDITIVE,
                searchQuery = additive.additiveTag,
                searchTitle = additive.name
            )
        }
    }

}