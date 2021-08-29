/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package openfoodfacts.github.scrachx.openfood.features.product.view.summary

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsEvent
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.databinding.FragmentSummaryProductBinding
import openfoodfacts.github.scrachx.openfood.features.FullScreenActivityOpener
import openfoodfacts.github.scrachx.openfood.features.ImagesManageActivity
import openfoodfacts.github.scrachx.openfood.features.additives.AdditiveFragmentHelper.showAdditives
import openfoodfacts.github.scrachx.openfood.features.compare.ProductCompareActivity.Companion.start
import openfoodfacts.github.scrachx.openfood.features.login.LoginActivity.Companion.LoginContract
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_STATE
import openfoodfacts.github.scrachx.openfood.features.product.view.CategoryProductHelper
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity
import openfoodfacts.github.scrachx.openfood.features.product.view.ingredients_analysis.IngredientsWithTagDialogFragment
import openfoodfacts.github.scrachx.openfood.features.productlist.ProductListActivity
import openfoodfacts.github.scrachx.openfood.features.productlists.ProductListsActivity
import openfoodfacts.github.scrachx.openfood.features.productlists.ProductListsActivity.Companion.getProductListsDaoWithDefaultList
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.features.shared.adapters.NutrientLevelListAdapter
import openfoodfacts.github.scrachx.openfood.features.shared.views.showQuestionDialog
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.*
import openfoodfacts.github.scrachx.openfood.models.entities.ListedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.ListedProductDao
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenHelper
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfig
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelName
import openfoodfacts.github.scrachx.openfood.models.entities.tag.TagDao
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.network.WikiDataApiClient
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.*
import java.io.File
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class SummaryProductFragment : BaseFragment(), ISummaryProductPresenter.View {
    private var _binding: FragmentSummaryProductBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var client: OpenFoodAPIClient

    @Inject
    lateinit var wikidataClient: WikiDataApiClient

    @Inject
    lateinit var daoSession: DaoSession

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var matomoAnalytics: MatomoAnalytics

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var localeManager: LocaleManager

    @Inject
    lateinit var productRepository: ProductRepository

    private lateinit var presenter: ISummaryProductPresenter.Actions
    private lateinit var mTagDao: TagDao

    private lateinit var product: Product
    private lateinit var customTabActivityHelper: CustomTabActivityHelper

    private lateinit var customTabsIntent: CustomTabsIntent
    private var annotation: AnnotationAnswer? = null

    private var hasCategoryInsightQuestion = false

    private var insightId: String? = null

    //boolean to determine if image should be loaded or not
    private val isLowBatteryMode by lazy { requireContext().isDisableImageLoad() && requireContext().isBatteryLevelLow() }
    private var mUrlImage: String? = null

    private var nutritionScoreUri: Uri? = null
    private val photoReceiverHandler by lazy {
        PhotoReceiverHandler(sharedPreferences) { newPhotoFile: File ->
            //the pictures are uploaded with the correct path
            val resultUri = newPhotoFile.toURI()
            val photoFile = if (sendOther) newPhotoFile else File(resultUri.path)
            val field = if (sendOther) ProductImageField.OTHER else ProductImageField.FRONT
            val image = ProductImage(product.code, field, photoFile, localeManager.getLanguage())
            image.filePath = photoFile.absolutePath
            uploadImage(image)
            if (!sendOther) {
                loadPhoto(photoFile)
            }
        }
    }

    private var productQuestion: Question? = null

    private val loginThenProcessInsight = registerForActivityResult(LoginContract()) { isLogged ->
        if (isLogged) {
            matomoAnalytics.trackEvent(AnalyticsEvent.RobotoffLoggedInAfterPrompt)
            processInsight()
        }
    }
    private lateinit var productState: ProductState

    private var sendOther = false

    /**boolean to determine if category prompt should be shown*/
    private var showCategoryPrompt = false

    /**boolean to determine if nutrient prompt should be shown*/
    private var showNutrientPrompt = false


    /**boolean to determine if eco score prompt should be shown*/
    private var showEcoScorePrompt = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        customTabActivityHelper = CustomTabActivityHelper().apply {
            setConnectionCallback(
                onConnected = { binding.imageGrade.isClickable = true },
                onDisconnected = { binding.imageGrade.isClickable = false }
            )
        }
        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(requireContext(), customTabActivityHelper.session)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mTagDao = daoSession.tagDao
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSummaryProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageViewFront.setOnClickListener { openFrontImageFullscreen() }
        binding.buttonNewFrontImage.setOnClickListener { newFrontImage() }
        binding.buttonMorePictures.setOnClickListener { takeMorePicture() }
        binding.actionAddToListButton.setOnClickListener { onBookmarkProductButtonClick() }
        binding.actionEditButton.setOnClickListener { onEditProductButtonClick() }
        binding.actionShareButton.setOnClickListener { onShareProductButtonClick() }
        binding.actionCompareButton.setOnClickListener { onCompareProductButtonClick() }
        binding.addNutriscorePrompt.setOnClickListener { onAddNutriScorePromptClick() }
        binding.productQuestionDismiss.setOnClickListener {
            binding.productQuestionLayout.visibility = View.GONE
        }
        binding.productQuestionLayout.setOnClickListener { productQuestion?.let { onProductQuestionClick(it) } }
        productState = requireProductState()
        refreshView(productState)

        presenter = SummaryProductPresenter(localeManager.getLanguage(), product, this, productRepository)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onRefresh() {
        super.onRefresh()
        refreshView(productState)
    }

    /**
     * Starts uploading image to backend
     *
     * @param image image to upload
     */
    private fun uploadImage(image: ProductImage) {
        binding.uploadingImageProgress.visibility = View.VISIBLE
        binding.uploadingImageProgressText.visibility = View.VISIBLE
        binding.uploadingImageProgressText.setText(R.string.toastSending)

        lifecycleScope.launch {
            try {
                withContext(IO) { client.postImg(image) }
            } catch (err: Exception) {
                binding.uploadingImageProgress.visibility = View.GONE
                binding.uploadingImageProgressText.visibility = View.GONE
                Toast.makeText(requireContext(), err.message, Toast.LENGTH_SHORT).show()
            }
            binding.uploadingImageProgress.visibility = View.GONE
            binding.uploadingImageProgressText.setText(R.string.image_uploaded_successfully)
        }
    }

    /**
     * Sets photo as current front image and displays it
     *
     * @param photoFile file to set
     */
    private fun loadPhoto(photoFile: File) {
        binding.addPhotoLabel.visibility = View.GONE
        mUrlImage = photoFile.absolutePath
        picasso.load(photoFile)
            .fit()
            .into(binding.imageViewFront)
    }

    override fun refreshView(productState: ProductState) {
        this.productState = productState
        product = productState.product!!
        presenter = SummaryProductPresenter(localeManager.getLanguage(), product, this, productRepository)

        binding.categoriesText.text = buildSpannedString {
            bold { append(getString(R.string.txtCategories)) }
        }

        binding.labelsText.text = buildSpannedString {
            bold { append(getString(R.string.txtLabels)) }
        }

        binding.textAdditiveProduct.text = buildSpannedString {
            bold { append(getString(R.string.txtAdditives)) }
        }

        // Refresh visibility of UI components
        binding.textBrandProduct.visibility = View.VISIBLE
        binding.textQuantityProduct.visibility = View.VISIBLE
        binding.textNameProduct.visibility = View.VISIBLE
        binding.embText.visibility = View.VISIBLE
        binding.embIcon.visibility = View.VISIBLE
        binding.labelsText.visibility = View.VISIBLE
        binding.labelsIcon.visibility = View.VISIBLE

        // Checks if the product belongs in any of the user's list and displays them as chips if it does
        showListChips()

        // Checks the product states_tags to determine which prompt to be shown
        refreshStatesTagsPrompt()
        lifecycleScope.launchWhenResumed {
            presenter.loadAllergens()
            presenter.loadCategories()
            presenter.loadLabels()
            presenter.loadProductQuestion()
            presenter.loadAdditives()
            presenter.loadAnalysisTags()
        }

        val langCode = localeManager.getLanguage()
        val imageUrl = product.getImageUrl(langCode)
        if (!imageUrl.isNullOrBlank()) {
            binding.addPhotoLabel.visibility = View.GONE

            // Load Image if isLowBatteryMode is false
            if (!isLowBatteryMode) {
                picasso.load(imageUrl).into(binding.imageViewFront)
            } else {
                binding.imageViewFront.visibility = View.GONE
            }
            mUrlImage = imageUrl
        }

        //TODO use OpenFoodApiService to fetch product by packaging, brands, categories etc
        binding.textNameProduct.text = product.getProductName(langCode) ?: getString(R.string.productNameNull)

        if (!product.quantity.isNullOrBlank()) {
            binding.textQuantityProduct.text = product.quantity
        } else {
            binding.textQuantityProduct.visibility = View.INVISIBLE
        }

        val pBrands = product.brands
        if (!pBrands.isNullOrBlank()) {
            binding.textBrandProduct.isClickable = true
            binding.textBrandProduct.movementMethod = LinkMovementMethod.getInstance()
            binding.textBrandProduct.text = ""
            pBrands.split(",").withIndex().forEach { (i, brand) ->
                if (i > 0) binding.textBrandProduct.append(", ")
                binding.textBrandProduct.append(
                    getSearchLinkText(
                        brand.trim { it <= ' ' },
                        SearchType.BRAND,
                        requireActivity()
                    )
                )
            }
        } else {
            binding.textBrandProduct.visibility = View.GONE
        }

        if (product.embTags.isNotEmpty() && product.embTags.toString().trim { it <= ' ' } != "[]") {
            binding.embText.movementMethod = LinkMovementMethod.getInstance()

            binding.embText.text = buildSpannedString {
                bold { append(getString(R.string.txtEMB)) }
                append(" ")


                product.embTags.toString()
                    .removeSurrounding("[", "]")
                    .split(", ")
                    .map {
                        getSearchLinkText(
                            getEmbCode(it).trim { it <= ' ' },
                            SearchType.EMB,
                            requireActivity()
                        )
                    }.forEachIndexed { i, embTag ->
                        if (i > 0) append(", ")

                        append(embTag)
                    }
            }
        } else {
            binding.embText.visibility = View.GONE
            binding.embIcon.visibility = View.GONE
        }

        // if the device does not have a camera, hide the button
        if (!isHardwareCameraInstalled(requireContext())) {
            binding.buttonMorePictures.visibility = View.GONE
        }

        if (isFlavors(OFF)) {
            binding.scoresLayout.visibility = View.VISIBLE
            val levelItems = mutableListOf<NutrientLevelItem>()
            val nutriments = product.nutriments
            val nutrientLevels = product.nutrientLevels
            var fat: NutrimentLevel? = null
            var saturatedFat: NutrimentLevel? = null
            var sugars: NutrimentLevel? = null
            var salt: NutrimentLevel? = null
            if (nutrientLevels != null) {
                fat = nutrientLevels.fat
                saturatedFat = nutrientLevels.saturatedFat
                sugars = nutrientLevels.sugars
                salt = nutrientLevels.salt
            }

            val servingInL = product.isPerServingInLiter()
            binding.textNutrientTxt.setText(if (servingInL != true) R.string.txtNutrientLevel100g else R.string.txtNutrientLevel100ml)
            if (fat != null || salt != null || saturatedFat != null || sugars != null) {
                // prefetch the URL
                nutritionScoreUri = Uri.parse(getString(R.string.nutriscore_uri))
                customTabActivityHelper.mayLaunchUrl(nutritionScoreUri, null, null)
                binding.cvNutritionLights.visibility = View.VISIBLE
                val fatNutriment = nutriments[Nutriment.FAT]
                if (fat != null && fatNutriment != null) {
                    levelItems += NutrientLevelItem(
                        getString(R.string.txtFat),
                        fatNutriment.getPer100gDisplayString(),
                        fat.getLocalize(requireContext()),
                        fat.getImgRes(),
                    )
                }
                val saturatedFatNutriment = nutriments[Nutriment.SATURATED_FAT]
                if (saturatedFat != null && saturatedFatNutriment != null) {
                    val saturatedFatLocalize = saturatedFat.getLocalize(requireContext())
                    levelItems += NutrientLevelItem(
                        getString(R.string.txtSaturatedFat),
                        saturatedFatNutriment.getPer100gDisplayString(),
                        saturatedFatLocalize,
                        saturatedFat.getImgRes()
                    )
                }
                val sugarsNutriment = nutriments[Nutriment.SUGARS]
                if (sugars != null && sugarsNutriment != null) {
                    levelItems += NutrientLevelItem(
                        getString(R.string.txtSugars),
                        sugarsNutriment.getPer100gDisplayString(),
                        sugars.getLocalize(requireContext()),
                        sugars.getImgRes(),
                    )
                }
                val saltNutriment = nutriments[Nutriment.SALT]
                if (salt != null && saltNutriment != null) {
                    val saltLocalize = salt.getLocalize(requireContext())
                    levelItems += NutrientLevelItem(
                        getString(R.string.txtSalt),
                        saltNutriment.getPer100gDisplayString(),
                        saltLocalize,
                        salt.getImgRes(),
                    )
                }
            } else {
                binding.cvNutritionLights.visibility = View.GONE
            }

            binding.listNutrientLevels.layoutManager = LinearLayoutManager(requireContext())
            binding.listNutrientLevels.adapter = NutrientLevelListAdapter(requireContext(), levelItems)

            refreshNutriScore()

            refreshNovaIcon()

            refreshCO2OrEcoscoreIcon()

            refreshScoresLayout()
        } else {
            binding.scoresLayout.visibility = View.GONE
        }

        // To be sure that top of the product view is visible at start
        binding.textNameProduct.requestFocus()
        binding.textNameProduct.clearFocus()

        // Set refreshing animation to false after all processing is done
        super.refreshView(productState)
    }

    private fun refreshScoresLayout() {
        binding.scoresLayout.visibility = if (binding.novaGroup.visibility != View.GONE
            || binding.imageGrade.visibility != View.GONE
            || binding.ecoscoreIcon.visibility != View.GONE
            || binding.addNutriscorePrompt.visibility != View.GONE
        ) View.VISIBLE else View.GONE
    }

    private fun refreshNutriScore() {
        val nutriScoreResource = product.getNutriScoreResource()
        binding.imageGrade.setImageResource(nutriScoreResource)
        binding.imageGrade.setOnClickListener {
            nutritionScoreUri?.let { uri ->
                val customTabsIntent = CustomTabsHelper.getCustomTabsIntent(
                    requireContext(),
                    customTabActivityHelper.session
                )
                CustomTabActivityHelper.openCustomTab(requireActivity(), customTabsIntent, uri, WebViewFallback())
            }
        }
    }

    private fun refreshNovaIcon() {
        binding.novaGroup.setImageResource(product.getNovaGroupResource())
        binding.novaGroup.setOnClickListener {
            val uri = Uri.parse(getString(R.string.url_nova_groups))
            val customTabsIntent = CustomTabsHelper.getCustomTabsIntent(requireContext(), customTabActivityHelper.session)
            CustomTabActivityHelper.openCustomTab(requireActivity(), customTabsIntent, uri, WebViewFallback())
        }
    }

    private fun refreshCO2OrEcoscoreIcon() {
        binding.ecoscoreIcon.setImageResource(product.getEcoscoreResource())
        binding.ecoscoreIcon.setOnClickListener {
            val uri = getString(R.string.ecoscore_url).toUri()
            val customTabsIntent = CustomTabsHelper.getCustomTabsIntent(requireContext(), customTabActivityHelper.session)
            CustomTabActivityHelper.openCustomTab(requireActivity(), customTabsIntent, uri, WebViewFallback())
        }
    }

    private fun showListChips() {

        // remove the existing childviews on chip group if any
        binding.listChips.removeAllViews()

        lifecycleScope.launch {
            val lists = daoSession.listedProductDao.list {
                where(ListedProductDao.Properties.Barcode.eq(product.code))
            }

            if (lists.isNotEmpty()) {
                binding.actionAddToListButtonLayout.background = ResourcesCompat.getDrawable(resources, R.color.grey_300, null)
                binding.actionButtonsLayout.updatePadding(bottom = 0, top = 0)
                binding.listChips.visibility = View.VISIBLE
            }
            lists.forEach { list ->
                // set a random color to the chip's background, we want a dark background as our text color is white so we will limit our rgb to 180
                val chipColor = Color.rgb(
                    Random.nextInt(180),
                    Random.nextInt(180),
                    Random.nextInt(180)
                )

                val chip = Chip(context).apply {
                    text = list.listName

                    chipBackgroundColor = ColorStateList.valueOf(chipColor)
                    setTextColor(Color.WHITE)

                    // open list when the user clicks on chip
                    setOnClickListener {
                        ProductListActivity.start(requireContext(), list.listId, list.listName)
                    }
                }

                binding.listChips.addView(chip)
            }
        }
    }

    private fun refreshStatesTagsPrompt() {
        //checks the product states_tags to determine which prompt to be shown
        val statesTags = product.statesTags
        showCategoryPrompt = "en:categories-to-be-completed" in statesTags && !hasCategoryInsightQuestion
        showNutrientPrompt = "en:nutrition-facts-to-be-completed" in statesTags && product.noNutritionData != "on"
        showEcoScorePrompt = "en:categories-completed" in statesTags && (product.ecoscore.isNullOrEmpty() || product.ecoscore.equals("unknown", true))

        Log.d(LOG_TAG, "Show category prompt: $showCategoryPrompt")
        Log.d(LOG_TAG, "Show nutrient prompt: $showNutrientPrompt")
        Log.d(LOG_TAG, "Show Eco Score prompt: $showEcoScorePrompt")

        if (showEcoScorePrompt) {
            binding.tipBoxEcoScore.loadToolTip()
        }

        binding.addNutriscorePrompt.visibility = View.VISIBLE
        when {
            showNutrientPrompt && showCategoryPrompt -> {
                binding.addNutriscorePrompt.text = getString(R.string.add_nutrient_category_prompt_text)
            }
            showNutrientPrompt -> {
                binding.addNutriscorePrompt.text = getString(R.string.add_nutrient_prompt_text)
            }
            showCategoryPrompt -> {
                binding.addNutriscorePrompt.text = getString(R.string.add_category_prompt_text)
            }
            else -> binding.addNutriscorePrompt.visibility = View.GONE
        }

    }


    override fun showAdditivesState(state: ProductInfoState<List<AdditiveName>>) {
        requireActivity().runOnUiThread {
            when (state) {
                is ProductInfoState.Loading -> {
                    binding.textAdditiveProduct.append(getString(R.string.txtLoading))
                    binding.textAdditiveProduct.visibility = View.VISIBLE
                }
                is ProductInfoState.Empty -> binding.textAdditiveProduct.visibility = View.GONE
                is ProductInfoState.Data -> {
                    showAdditives(state.data, binding.textAdditiveProduct, wikidataClient, this)
                }
            }
        }
    }

    override suspend fun showAnalysisTags(state: ProductInfoState<List<AnalysisTagConfig>>) {
        withContext(Main) {
            when (state) {
                is ProductInfoState.Data -> {
                    val analysisTags = state.data

                    binding.analysisContainer.visibility = View.VISIBLE

                    binding.analysisTags.adapter = IngredientAnalysisTagsAdapter(
                        requireContext(),
                        analysisTags,
                        picasso,
                        sharedPreferences
                    ).apply adapter@{
                        setOnItemClickListener { view, _ ->
                            IngredientsWithTagDialogFragment.newInstance(
                                product,
                                view.getTag(R.id.analysis_tag_config) as AnalysisTagConfig
                            ).run {
                                onDismissListener = { filterVisibleTags() }
                                show(childFragmentManager, "fragment_ingredients_with_tag")
                            }
                        }
                    }
                }
                ProductInfoState.Empty -> {
                    // TODO:
                }
                ProductInfoState.Loading -> {
                    // TODO:
                }
            }

        }
    }

    override fun showAllergens(allergens: List<AllergenName>) {
        val data = AllergenHelper.computeUserAllergen(product, allergens)
        if (data.isEmpty()) return

        if (data.incomplete) {
            binding.productAllergenAlertText.setText(R.string.product_incomplete_message)
            binding.productAllergenAlertLayout.visibility = View.VISIBLE
            return
        }
        binding.productAllergenAlertText.text = buildSpannedString {
            append(getString(R.string.product_allergen_prompt))
            append("\n")
            append(data.allergens.joinToString(", "))
        }

        binding.productAllergenAlertLayout.visibility = View.VISIBLE
    }


    override suspend fun showProductQuestion(question: Question) = withContext(Main) {

        if (!question.isEmpty()) {
            productQuestion = question
            binding.productQuestionText.text = buildSpannedString {
                append(question.questionText)
                append("\n")
                append(question.value)
            }
            binding.productQuestionLayout.visibility = View.VISIBLE
            hasCategoryInsightQuestion = question.insightType == "category"
        } else {
            binding.productQuestionLayout.visibility = View.GONE
            productQuestion = null
        }

        if (isFlavors(OFF)) {
            refreshStatesTagsPrompt()
            refreshScoresLayout()
        }
    }

    private fun onProductQuestionClick(productQuestion: Question) {
        showQuestionDialog(requireContext()) {
            backgroundColor = R.color.colorPrimaryDark
            question = productQuestion.questionText
            value = productQuestion.value


            onPositiveFeedback = {
                sendProductInsights(productQuestion.insightId, AnnotationAnswer.POSITIVE)
                it.dismiss()
            }

            onNegativeFeedback = {
                sendProductInsights(productQuestion.insightId, AnnotationAnswer.NEGATIVE)
                it.dismiss()
            }

            onAmbiguityFeedback = {
                sendProductInsights(productQuestion.insightId, AnnotationAnswer.AMBIGUITY)
                it.dismiss()
            }

            onCancelListener = { it.dismiss() }

        }

    }

    private fun sendProductInsights(insightId: String?, annotation: AnnotationAnswer?) {
        this.insightId = insightId
        this.annotation = annotation

        if (requireActivity().isUserSet()) {
            processInsight()
        } else {
            matomoAnalytics.trackEvent(AnalyticsEvent.RobotoffLoginPrompt)

            MaterialAlertDialogBuilder(requireActivity())
                .setTitle(getString(R.string.sign_in_to_answer))
                .setPositiveButton(getString(R.string.sign_in_or_register)) { dialog, _ ->
                    loginThenProcessInsight.launch(Unit)
                    dialog.dismiss()
                }
                .setNegativeButton(R.string.dialog_cancel) { d, _ -> d.dismiss() }
                .show()
        }
    }

    private fun processInsight() {
        val insightId = this.insightId ?: error("Property 'insightId' not set.")
        val annotation = this.annotation ?: error("Property 'annotation' not set.")

        lifecycleScope.launch { presenter.annotateInsight(insightId, annotation) }

        Log.d(LOG_TAG, "Annotation $annotation received for insight $insightId")
        binding.productQuestionLayout.visibility = View.GONE
        productQuestion = null
    }

    override fun showAnnotatedInsightToast(annotationResponse: AnnotationResponse) {
        if (annotationResponse.status == "updated" && activity != null) {
            Snackbar.make(binding.root, R.string.product_question_submit_message, LENGTH_SHORT).show()
        }
    }

    override fun showLabelsState(state: ProductInfoState<List<LabelName>>) {
        requireActivity().runOnUiThread {
            when (state) {
                is ProductInfoState.Data -> {
                    binding.labelsText.isClickable = true
                    binding.labelsText.movementMethod = LinkMovementMethod.getInstance()

                    binding.labelsText.text = buildSpannedString {
                        bold { append(getString(R.string.txtLabels)) }
                        state.data.map(::getLabelTag).forEachIndexed { i, el ->
                            append(el)
                            if (i != state.data.size) append(", ")
                        }

                    }
                }
                is ProductInfoState.Loading -> {
                    binding.labelsText.text = buildSpannedString {
                        bold { append(getString(R.string.txtLabels)) }
                        append(getString(R.string.txtLoading))
                    }
                }

                is ProductInfoState.Empty -> {
                    binding.labelsText.visibility = View.GONE
                    binding.labelsIcon.visibility = View.GONE
                }
            }
        }
    }

    override fun showCategoriesState(state: ProductInfoState<List<CategoryName>>) {
        requireActivity().runOnUiThread {
            when (state) {
                is ProductInfoState.Loading -> {
                    binding.categoriesText.text = buildSpannedString {
                        bold { append(getString(R.string.txtCategories)) }
                        append(getString(R.string.txtLoading))
                    }
                }
                is ProductInfoState.Empty -> {
                    binding.categoriesText.visibility = View.GONE
                    binding.categoriesIcon.visibility = View.GONE
                }
                is ProductInfoState.Data -> {
                    val categories = state.data
                    if (categories.isEmpty()) {
                        binding.categoriesLayout.visibility = View.GONE
                        return@runOnUiThread
                    }
                    CategoryProductHelper.showCategories(
                        this,
                        binding.categoriesText,
                        binding.textCategoryAlcoholAlert,
                        categories,
                        wikidataClient,
                    )
                }
            }
        }
    }

    private suspend fun getEmbUrl(embTag: String): String? = withContext(IO) {
        if (mTagDao.queryBuilder().where(TagDao.Properties.Id.eq(embTag)).list().isEmpty()) null
        else mTagDao.queryBuilder().where(TagDao.Properties.Id.eq(embTag)).unique().url
    }

    private fun getEmbCode(embTag: String) =
        mTagDao.queryBuilder().where(TagDao.Properties.Id.eq(embTag)).unique()?.name ?: embTag

    private fun getLabelTag(label: LabelName): CharSequence {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                if (label.isWikiDataIdPresent) {
                    lifecycleScope.launch {
                        val result = wikidataClient.getEntityData(label.wikiDataId)
                        val activity = activity
                        if (activity?.isFinishing == false) {
                            showBottomSheet(result, label, activity.supportFragmentManager)
                        }
                    }
                } else {
                    ProductSearchActivity.start(requireContext(), SearchType.LABEL, label.labelTag, label.name)
                }
            }
        }
        return buildSpannedString {
            inSpans(clickableSpan) { append(label.name) }
        }
    }


    private val loginThenEditLauncher = registerForActivityResult(LoginContract())
    { logged -> if (logged) editProduct() }

    private val loginThenEditNutrition = registerForActivityResult(LoginContract())
    { logged -> if (logged) editProductNutriscore() }

    private fun onEditProductButtonClick() {
        if (requireActivity().isUserSet()) {
            editProduct()
        } else {
            buildSignInDialog(requireActivity(),
                onPositive = { d, _ ->
                    d.dismiss()
                    loginThenEditLauncher.launch(null)
                },
                onNegative = { d, _ -> d.dismiss() }
            ).show()
        }
    }

    private fun onAddNutriScorePromptClick() {
        if (!isFlavors(OFF)) return
        if (requireActivity().isUserSet()) {
            editProductNutriscore()
        } else {
            buildSignInDialog(
                requireActivity(),
                onPositive = { d, _ ->
                    d.dismiss()
                    loginThenEditNutrition.launch(null)
                },
                onNegative = { d, _ -> d.dismiss() }
            ).show()
        }
    }

    private fun editProductNutriscore() {
        startActivity(Intent(requireContext(), ProductEditActivity::class.java).apply {
            putExtra(ProductEditActivity.KEY_EDIT_PRODUCT, product)
            //adds the information about the prompt when navigating the user to the edit the product
            putExtra(ProductEditActivity.KEY_MODIFY_CATEGORY_PROMPT, showCategoryPrompt)
            putExtra(ProductEditActivity.KEY_MODIFY_NUTRITION_PROMPT, showNutrientPrompt)

        })
    }

    private fun onCompareProductButtonClick() {
        start(requireContext(), product)
    }

    private fun onShareProductButtonClick() {
        val shareUrl = "${getString(R.string.website_product)}${product.code}"
        val shareBody = "${getString(R.string.msg_share)} $shareUrl"
        startActivity(Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareBody)
        }, null))
    }

    private val editProductLauncher = registerForActivityResult(ProductEditActivity.EditProductContract())
    { isOk -> if (isOk) (activity as? ProductViewActivity)?.onRefresh() }

    private fun editProduct() = editProductLauncher.launch(product)

    private fun onBookmarkProductButtonClick() {
        val context = requireContext()

        // TODO: 19/06/2021 remove runBlocking
        val productLists = runBlocking { daoSession.getProductListsDaoWithDefaultList(context).loadAll() }

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(R.string.add_to_product_lists)
            .setView(R.layout.dialog_add_to_list)
            .show()

        // Set recycler view
        val addToListRecyclerView = dialog.findViewById<RecyclerView>(R.id.rv_dialogAddToList)!!
        addToListRecyclerView.layoutManager = LinearLayoutManager(context)

        addToListRecyclerView.adapter = DialogAddToListAdapter(
            context,
            productLists.filter { list -> product.code !in list.products.map { it.barcode } }
        ) { list ->
            val product = ListedProduct().also {
                it.barcode = product.code
                it.listId = list.id
                it.listName = list.listName
                it.productName = product.productName
                it.productDetails = product.getProductBrandsQuantityDetails()
                it.imageUrl = product.getImageSmallUrl(localeManager.getLanguage())
            }
            daoSession.listedProductDao.insertOrReplace(product)
            dialog.dismiss()
            onRefresh()
        }

        // Add listener to text view
        val addToNewList = dialog.findViewById<TextView>(R.id.tvAddToNewList)!!
        addToNewList.setOnClickListener {
            context.startActivity(Intent(context, ProductListsActivity::class.java).apply {
                putExtra("product", product)
            })
        }
    }


    private fun takeMorePicture() {
        sendOther = true
        doChooseOrTakePhotos()
    }

    private fun openFrontImageFullscreen() {
        when (val url = mUrlImage) {
            null -> newFrontImage()
            else -> {
                FullScreenActivityOpener.openForUrl(
                    this,
                    client,
                    product,
                    ProductImageField.FRONT,
                    url,
                    binding.imageViewFront,
                    localeManager.getLanguage()
                )
            }
        }
    }

    private fun newFrontImage() {
        // add front image.
        sendOther = false
        doChooseOrTakePhotos()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data)

        val shouldRefresh = ImagesManageActivity.isImageModified(requestCode, resultCode)

        if (shouldRefresh && activity is ProductViewActivity) {
            (activity as ProductViewActivity).onRefresh()
        }
    }


    override fun doOnPhotosPermissionGranted() =
        if (sendOther) takeMorePicture()
        else newFrontImage()


    fun resetScroll() {
        // TODO: should not check for binding, this should be done in another way
        if (!isAdded) return
        binding.scrollView.scrollTo(0, 0)
        binding.analysisTags.adapter?.let {
            (it as IngredientAnalysisTagsAdapter).filterVisibleTags()
        }
    }

    companion object {
        private val LOG_TAG = SummaryProductFragment::class.simpleName!!

        fun newInstance(productState: ProductState) = SummaryProductFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY_STATE, productState)
            }
        }
    }
}
