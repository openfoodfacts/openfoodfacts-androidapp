package openfoodfacts.github.scrachx.openfood.features.product.view.attribute

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.net.toUri
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.databinding.FragmentProductAttributeDetailsBinding
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveNameDao
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import openfoodfacts.github.scrachx.openfood.utils.SearchType
import javax.inject.Inject

@AndroidEntryPoint
class ProductAttributeFragment : BottomSheetDialogFragment() {
    private lateinit var mpInfantsImage: AppCompatImageView
    private lateinit var mpToddlersImage: AppCompatImageView
    private lateinit var mpChildrenImage: AppCompatImageView
    private lateinit var mpAdolescentsImage: AppCompatImageView
    private lateinit var mpAdultsImage: AppCompatImageView
    private lateinit var mpElderlyImage: AppCompatImageView
    private lateinit var spInfantsImage: AppCompatImageView
    private lateinit var spToddlersImage: AppCompatImageView
    private lateinit var spChildrenImage: AppCompatImageView
    private lateinit var spAdolescentsImage: AppCompatImageView
    private lateinit var spAdultsImage: AppCompatImageView
    private lateinit var spElderlyImage: AppCompatImageView
    private val customTabsIntent by lazy {
        CustomTabsHelper.getCustomTabsIntent(requireContext(), CustomTabActivityHelper().session)
    }

    private var _binding: FragmentProductAttributeDetailsBinding? = null
    private val binding get() = _binding!!

    private val mapper by lazy { jacksonObjectMapper() }

    @Inject
    lateinit var daoSession: DaoSession

    @Inject
    lateinit var localeManager: LocaleManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductAttributeDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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

        try {
            val arguments = requireArguments()
            val result = arguments.getString(ARG_OBJECT)?.let { mapper.readTree(it) }

            binding.titleBottomSheet.text = arguments.getString(ARG_TITLE) ?: ""

            val descriptionsNode = result?.get("descriptions")
            val description = descriptionsNode?.let { getDescription(it) } ?: ""

            if (description.isNotEmpty()) {
                binding.description.text = description
                binding.description.visibility = View.VISIBLE
            } else {
                binding.description.visibility = View.GONE
            }

            val siteLinks = result?.get("sitelinks")
            val wikiLink = siteLinks?.let { getWikiLink(it) } ?: ""

            if (wikiLink.isNotEmpty()) {
                binding.wikipediaButton.setOnClickListener { openInCustomTab(wikiLink) }
                binding.wikipediaButton.visibility = View.VISIBLE
            } else {
                binding.wikipediaButton.visibility = View.GONE
            }

            val searchType = arguments.getSerializable(ARG_SEARCH_TYPE) as SearchType
            binding.buttonToBrowseProducts.setOnClickListener {
                ProductSearchActivity.start(
                    requireContext(),
                    searchType,
                    arguments.getString(ARG_TITLE) ?: ""
                )
            }
            val id = arguments.getLong(ARG_ID)
            if (searchType == SearchType.ADDITIVE) {
                daoSession.additiveNameDao.queryBuilder()
                    .where(AdditiveNameDao.Properties.Id.eq(id))
                    .unique()
                    ?.let { updateContent(view, it) }

            }
        } catch (e: JsonProcessingException) {
            Log.e(LOG_TAG, "onCreateView", e)
        }
    }

    private fun updateContent(view: View, additive: AdditiveName) {


        if (!additive.hasOverexposureData()) return

        val exposureEvalTable = binding.exposureEvalTable
        val efsaWarning = view.findViewById<TextView>(R.id.efsaWarning)

        val overexposureRisk = additive.overexposureRisk

        val isHighRisk = "high".equals(overexposureRisk, true)
        if (isHighRisk) {
            binding.titleBottomSheetIcon.setImageResource(R.drawable.ic_additive_high_risk)
        } else {
            binding.titleBottomSheetIcon.setImageResource(R.drawable.ic_additive_moderate_risk)
        }
        efsaWarning.text = getString(R.string.efsa_warning_high_risk, additive.name)
        binding.titleBottomSheetIcon.visibility = View.VISIBLE

        // noel will override adi evaluation if present
        updateAdditiveExposureTable(0, additive.exposureMeanGreaterThanAdi, R.drawable.yellow_circle)
        updateAdditiveExposureTable(0, additive.exposureMeanGreaterThanNoael, R.drawable.red_circle)
        updateAdditiveExposureTable(1, additive.exposure95ThGreaterThanAdi, R.drawable.yellow_circle)
        updateAdditiveExposureTable(1, additive.exposure95ThGreaterThanNoael, R.drawable.red_circle)
        exposureEvalTable.visibility = View.VISIBLE
    }

    private fun updateAdditiveExposureTable(row: Int, exposure: String?, drawableResId: Int) {
        if (exposure == null) return

        when (row) {
            0 -> {
                if ("infants" in exposure) {
                    mpInfantsImage.setImageResource(drawableResId)
                }
                if ("toddlers" in exposure) {
                    mpToddlersImage.setImageResource(drawableResId)
                }
                if ("children" in exposure) {
                    mpChildrenImage.setImageResource(drawableResId)
                }
                if ("adolescents" in exposure) {
                    mpAdolescentsImage.setImageResource(drawableResId)
                }
                if ("adults" in exposure) {
                    mpAdultsImage.setImageResource(drawableResId)
                }
                if ("elderly" in exposure) {
                    mpElderlyImage.setImageResource(drawableResId)
                }
            }
            1 -> {
                if ("infants" in exposure) {
                    spInfantsImage.setImageResource(drawableResId)
                }
                if ("toddlers" in exposure) {
                    spToddlersImage.setImageResource(drawableResId)
                }
                if ("children" in exposure) {
                    spChildrenImage.setImageResource(drawableResId)
                }
                if ("adolescents" in exposure) {
                    spAdolescentsImage.setImageResource(drawableResId)
                }
                if ("adults" in exposure) {
                    spAdultsImage.setImageResource(drawableResId)
                }
                if ("elderly" in exposure) {
                    spElderlyImage.setImageResource(drawableResId)
                }
            }
        }
    }

    private fun getDescription(map: JsonNode): String? {
        var descriptionString: String? = null

        val languageCode = localeManager.getLanguage()
        if (map.has(languageCode)) {
            descriptionString = map[languageCode]["value"].asText()
        }

        if (descriptionString.isNullOrEmpty() && map.has("en")) {
            descriptionString = map["en"]["value"].asText()
        }

        if (descriptionString.isNullOrEmpty()) {
            Log.w(LOG_TAG, "Result for description is not found in native or english language.")
        }
        return descriptionString
    }

    private fun getWikiLink(map: JsonNode): String? {
        val languageCode = "${localeManager.getLanguage()}wiki"
        return when {
            map.has(languageCode) -> map[languageCode]["url"].asText()
            map.has("enwiki") -> map["enwiki"]["url"].asText()
            else -> {
                Log.i(LOG_TAG, "Result for wikilink is not found in native or english language.")
                null
            }
        }
    }

    private fun openInCustomTab(url: String) {
        // Url might be empty string if there is no wiki link in english or the user's language
        if (url.isEmpty()) {
            Toast.makeText(context, R.string.wikidata_unavailable, Toast.LENGTH_SHORT).show()
            return
        }
        val uri = url.toUri()
        CustomTabActivityHelper.openCustomTab(
            requireActivity(),
            customTabsIntent,
            uri,
            WebViewFallback()
        )
    }

    companion object {
        private val LOG_TAG = this::class.simpleName
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
