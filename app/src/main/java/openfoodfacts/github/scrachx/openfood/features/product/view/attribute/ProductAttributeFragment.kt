package openfoodfacts.github.scrachx.openfood.features.product.view.attribute

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.browser.customtabs.CustomTabsIntent
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity.Companion.start
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveNameDao
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper
import openfoodfacts.github.scrachx.openfood.utils.SearchType
import openfoodfacts.github.scrachx.openfood.utils.Utils

class ProductAttributeFragment : BottomSheetDialogFragment() {
    private var bottomSheetTitleIcon: AppCompatImageView? = null
    private var mpInfantsImage: AppCompatImageView? = null
    private var mpToddlersImage: AppCompatImageView? = null
    private var mpChildrenImage: AppCompatImageView? = null
    private var mpAdolescentsImage: AppCompatImageView? = null
    private var mpAdultsImage: AppCompatImageView? = null
    private var mpElderlyImage: AppCompatImageView? = null
    private var spInfantsImage: AppCompatImageView? = null
    private var spToddlersImage: AppCompatImageView? = null
    private var spChildrenImage: AppCompatImageView? = null
    private var spAdolescentsImage: AppCompatImageView? = null
    private var spAdultsImage: AppCompatImageView? = null
    private var spElderlyImage: AppCompatImageView? = null
    private var customTabsIntent: CustomTabsIntent? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product_attribute_details, container,
                false)
        val customTabActivityHelper = CustomTabActivityHelper()
        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(requireContext(), customTabActivityHelper.session)
        val bottomSheetDescription = view.findViewById<TextView>(R.id.description)
        val bottomSheetTitle = view.findViewById<TextView>(R.id.titleBottomSheet)
        bottomSheetTitleIcon = view.findViewById(R.id.titleBottomSheetIcon)
        val buttonToBrowseProducts = view.findViewById<Button>(R.id.buttonToBrowseProducts)
        val wikipediaButton = view.findViewById<Button>(R.id.wikipediaButton)
        try {
            val descriptionString: String?
            val wikiLink: String?
            val arguments = requireArguments()

            val result = jacksonObjectMapper().readTree(arguments.getString(ARG_OBJECT))
            val description = result?.get("descriptions")
            val siteLinks = result?.get("sitelinks")
            descriptionString = description?.let { getDescription(it) } ?: ""
            wikiLink = siteLinks?.let { getWikiLink(it) } ?: ""
            val title = arguments.getString(ARG_TITLE) as String
            bottomSheetTitle.text = title
            val searchType = arguments.getSerializable(ARG_SEARCH_TYPE) as SearchType
            if (descriptionString.isNotEmpty()) {
                bottomSheetDescription.text = descriptionString
                bottomSheetDescription.visibility = View.VISIBLE
            } else {
                bottomSheetDescription.visibility = View.GONE
            }
            buttonToBrowseProducts.setOnClickListener { start(requireContext(), searchType, title) }
            if (wikiLink.isNotEmpty()) {
                wikipediaButton.setOnClickListener { openInCustomTab(wikiLink) }
                wikipediaButton.visibility = View.VISIBLE
            } else {
                wikipediaButton.visibility = View.GONE
            }
            val id = arguments.getLong(ARG_ID)
            if (SearchType.ADDITIVE == searchType) {
                val dao = Utils.daoSession.additiveNameDao
                val additiveName = dao.queryBuilder()
                        .where(AdditiveNameDao.Properties.Id.eq(id)).unique()
                updateContent(view, additiveName)
            }
        } catch (e: JsonProcessingException) {
            Log.e(javaClass.simpleName, "onCreateView", e)
        }
        return view
    }

    private fun updateContent(view: View, additive: AdditiveName?) {
        mpInfantsImage = view.findViewById(R.id.mpInfants)
        mpToddlersImage = view.findViewById(R.id.mpToddlers)
        mpChildrenImage = view.findViewById(R.id.mpChildren)
        mpAdolescentsImage = view.findViewById(R.id.mpAdolescents)
        mpAdultsImage = view.findViewById(R.id.mpAdults)
        mpElderlyImage = view.findViewById(R.id.mpElderly)
        spInfantsImage = view.findViewById(R.id.spInfants)
        spToddlersImage = view.findViewById(R.id.spToddlers)
        spChildrenImage = view.findViewById(R.id.spChildren)
        spAdolescentsImage = view.findViewById(R.id.spAdolescents)
        spAdultsImage = view.findViewById(R.id.spAdults)
        spElderlyImage = view.findViewById(R.id.spElderly)
        if (additive != null && additive.hasOverexposureData()) {
            val exposureEvalTable = view.findViewById<View>(R.id.exposureEvalTable)
            val efsaWarning = view.findViewById<TextView>(R.id.efsaWarning)
            val overexposureRisk = additive.overexposureRisk
            val isHighRisk = "high".equals(overexposureRisk, ignoreCase = true)
            if (isHighRisk) {
                bottomSheetTitleIcon!!.setImageResource(R.drawable.ic_additive_high_risk)
            } else {
                bottomSheetTitleIcon!!.setImageResource(R.drawable.ic_additive_moderate_risk)
            }
            efsaWarning.text = getString(R.string.efsa_warning_high_risk, additive.name)
            bottomSheetTitleIcon!!.visibility = View.VISIBLE

            // noel will override adi evaluation if present
            updateAdditiveExposureTable(0, additive.exposureMeanGreaterThanAdi, R.drawable.yellow_circle)
            updateAdditiveExposureTable(0, additive.exposureMeanGreaterThanNoael, R.drawable.red_circle)
            updateAdditiveExposureTable(1, additive.exposure95ThGreaterThanAdi, R.drawable.yellow_circle)
            updateAdditiveExposureTable(1, additive.exposure95ThGreaterThanNoael, R.drawable.red_circle)
            exposureEvalTable.visibility = View.VISIBLE
        }
    }

    private fun updateAdditiveExposureTable(row: Int, exposure: String?, drawableResId: Int) {
        if (exposure != null) {
            if (row == 0) {
                if (exposure.contains("infants")) {
                    mpInfantsImage!!.setImageResource(drawableResId)
                }
                if (exposure.contains("toddlers")) {
                    mpToddlersImage!!.setImageResource(drawableResId)
                }
                if (exposure.contains("children")) {
                    mpChildrenImage!!.setImageResource(drawableResId)
                }
                if (exposure.contains("adolescents")) {
                    mpAdolescentsImage!!.setImageResource(drawableResId)
                }
                if (exposure.contains("adults")) {
                    mpAdultsImage!!.setImageResource(drawableResId)
                }
                if (exposure.contains("elderly")) {
                    mpElderlyImage!!.setImageResource(drawableResId)
                }
            } else if (row == 1) {
                if (exposure.contains("infants")) {
                    spInfantsImage!!.setImageResource(drawableResId)
                }
                if (exposure.contains("toddlers")) {
                    spToddlersImage!!.setImageResource(drawableResId)
                }
                if (exposure.contains("children")) {
                    spChildrenImage!!.setImageResource(drawableResId)
                }
                if (exposure.contains("adolescents")) {
                    spAdolescentsImage!!.setImageResource(drawableResId)
                }
                if (exposure.contains("adults")) {
                    spAdultsImage!!.setImageResource(drawableResId)
                }
                if (exposure.contains("elderly")) {
                    spElderlyImage!!.setImageResource(drawableResId)
                }
            }
        }
    }

    private fun getDescription(map: JsonNode): String? {
        var descriptionString: String? = null

        val languageCode = LocaleHelper.getLanguage(OFFApplication.instance)
        if (map[languageCode] != null) {
            descriptionString = map[languageCode]["value"].asText()
        }

        if (descriptionString.isNullOrEmpty() && map["en"] != null) {
            descriptionString = map["en"]["value"].asText()
        }

        if (descriptionString.isNullOrEmpty()) {
            Log.i("ProductActivity", "Result for description is not found in native or english language.")
        }
        return descriptionString
    }

    private fun getWikiLink(map: JsonNode): String? {
        val languageCode = "${LocaleHelper.getLanguage(requireContext())}wiki"
        return when {
            map[languageCode] != null -> map[languageCode]["url"].asText()
            map["enwiki"] != null -> map["enwiki"]["url"].asText()
            else -> {
                Log.i("ProductActivity", "Result for wikilink is not found in native or english language.")
                null
            }
        }
    }

    private fun openInCustomTab(url: String) {
        // Url might be empty string if there is no wiki link in english or the user's language
        if (url != "") {
            val wikipediaUri = Uri.parse(url)
            CustomTabActivityHelper.openCustomTab(requireActivity(), customTabsIntent!!, wikipediaUri, WebViewFallback())
        } else {
            Toast.makeText(context, R.string.wikidata_unavailable, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val ARG_OBJECT = "result"
        private const val ARG_ID = "code"
        private const val ARG_SEARCH_TYPE = "search_type"
        private const val ARG_TITLE = "title"
        fun newInstance(
                jsonObjectStr: JsonNode?,
                id: Long,
                searchType: SearchType?,
                title: String?
        ) = ProductAttributeFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_OBJECT, jsonObjectStr.toString())
                putLong(ARG_ID, id)
                putSerializable(ARG_SEARCH_TYPE, searchType)
                putString(ARG_TITLE, title)
            }
        }
    }
}