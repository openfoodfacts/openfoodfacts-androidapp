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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.rxkotlin.addTo
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.databinding.FragmentSummaryProductBinding
import openfoodfacts.github.scrachx.openfood.features.FullScreenActivityOpener
import openfoodfacts.github.scrachx.openfood.features.ImagesManageActivity
import openfoodfacts.github.scrachx.openfood.features.LoginActivity.LoginContract
import openfoodfacts.github.scrachx.openfood.features.additives.AdditiveFragmentHelper.showAdditives
import openfoodfacts.github.scrachx.openfood.features.compare.ProductCompareActivity.Companion.start
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.product.view.CategoryProductHelper
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity
import openfoodfacts.github.scrachx.openfood.features.product.view.ingredients_analysis.IngredientsWithTagDialogFragment
import openfoodfacts.github.scrachx.openfood.features.productlist.ProductListActivity
import openfoodfacts.github.scrachx.openfood.features.productlists.ProductListsActivity
import openfoodfacts.github.scrachx.openfood.features.productlists.ProductListsActivity.Companion.getProductListsDaoWithDefaultList
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.features.shared.adapters.NutrientLevelListAdapter
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.*
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenHelper
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfig
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryName
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelName
import openfoodfacts.github.scrachx.openfood.models.entities.tag.TagDao
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.network.WikiDataApiClient
import openfoodfacts.github.scrachx.openfood.utils.*
import openfoodfacts.github.scrachx.openfood.utils.Utils.NO_DRAWABLE_RESOURCE
import java.io.File
import java.util.*

class SummaryProductFragment : BaseFragment(), ISummaryProductPresenter.View {
    private var _binding: FragmentSummaryProductBinding? = null
    private val binding get() = _binding!!

    private val disp = CompositeDisposable()

    private lateinit var client: OpenFoodAPIClient
    private lateinit var wikidataClient: WikiDataApiClient

    private lateinit var presenter: ISummaryProductPresenter.Actions
    private lateinit var mTagDao: TagDao
    private lateinit var product: Product

    private var annotation: AnnotationAnswer? = null
    private var customTabActivityHelper: CustomTabActivityHelper? = null
    private var customTabsIntent: CustomTabsIntent? = null
    private var hasCategoryInsightQuestion = false

    private var insightId: String? = null

    //boolean to determine if image should be loaded or not
    private var isLowBatteryMode = false
    private var mUrlImage: String? = null
    private var nutritionScoreUri: Uri? = null

    private var photoReceiverHandler = PhotoReceiverHandler { newPhotoFile: File ->
        //the pictures are uploaded with the correct path
        val resultUri = newPhotoFile.toURI()
        val photoFile = if (sendOther) newPhotoFile else File(resultUri.path)
        val field = if (sendOther) ProductImageField.OTHER else ProductImageField.FRONT
        val image = ProductImage(product.code, field, photoFile)
        image.filePath = photoFile.absolutePath
        uploadImage(image)
        if (!sendOther) {
            loadPhoto(photoFile)
        }
    }
    private var productQuestion: Question? = null

    private val loginThenProcessInsight = registerForActivityResult(LoginContract())
    { isLoggedIn -> if (isLoggedIn) processInsight() }

    private lateinit var productState: ProductState
    private var sendOther = false

    /**boolean to determine if category prompt should be shown*/
    private var showCategoryPrompt = false

    /**boolean to determine if nutrient prompt should be shown*/
    private var showNutrientPrompt = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        customTabActivityHelper = CustomTabActivityHelper().apply {
            connectionCallback = object : CustomTabActivityHelper.ConnectionCallback {
                override fun onCustomTabsConnected() {
                    binding.imageGrade.isClickable = true
                }

                override fun onCustomTabsDisconnected() {
                    binding.imageGrade.isClickable = false
                }
            }
        }
        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(requireContext(), customTabActivityHelper!!.session)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        client = OpenFoodAPIClient(requireActivity())
        wikidataClient = WikiDataApiClient()
        mTagDao = Utils.daoSession.tagDao

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
        binding.productQuestionLayout.setOnClickListener { onProductQuestionClick() }
        productState = requireProductState()
        refreshView(productState)

        presenter = SummaryProductPresenter(product, this)
        presenter.addTo(disp)
    }


    override fun onDestroyView() {
        disp.dispose()
        super.onDestroyView()
        _binding = null
    }

    private fun onImageListenerError(error: Throwable) {
        binding.uploadingImageProgress.visibility = View.GONE
        binding.uploadingImageProgressText.visibility = View.GONE
        var context = context
        if (context == null) {
            context = OFFApplication.instance
        }
        Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
    }


    override fun onRefresh() {
        refreshView(productState)
    }

    private fun onImageListenerComplete() {
        binding.uploadingImageProgress.visibility = View.GONE
        binding.uploadingImageProgressText.setText(R.string.image_uploaded_successfully)
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
        disp.add(client.postImg(image).observeOn(AndroidSchedulers.mainThread()).subscribeWith(object : DisposableCompletableObserver() {
            override fun onComplete() {
                onImageListenerComplete()
            }

            override fun onError(error: Throwable) {
                onImageListenerError(error)
            }
        }))
    }

    /**
     * Sets photo as current front image and displays it
     *
     * @param photoFile file to set
     */
    private fun loadPhoto(photoFile: File) {
        binding.addPhotoLabel.visibility = View.GONE
        mUrlImage = photoFile.absolutePath
        Picasso.get()
                .load(photoFile)
                .fit()
                .into(binding.imageViewFront)
    }

    override fun refreshView(productState: ProductState) {
        this.productState = productState
        product = productState.product!!
        presenter = SummaryProductPresenter(product, this)
        presenter.addTo(disp)
        binding.categoriesText.text = bold(getString(R.string.txtCategories))
        binding.labelsText.text = bold(getString(R.string.txtLabels))

        //refresh visibility of UI components
        binding.textBrandProduct.visibility = View.VISIBLE
        binding.textQuantityProduct.visibility = View.VISIBLE
        binding.textNameProduct.visibility = View.VISIBLE
        binding.embText.visibility = View.VISIBLE
        binding.embIcon.visibility = View.VISIBLE
        binding.labelsText.visibility = View.VISIBLE
        binding.labelsIcon.visibility = View.VISIBLE

        // If Battery Level is low and the user has checked the Disable Image in Preferences , then set isLowBatteryMode to true
        if (requireActivity().isDisableImageLoad() && requireContext().isBatteryLevelLow()) {
            isLowBatteryMode = true
        }

        //checks the product states_tags to determine which prompt to be shown
        refreshNutriScorePrompt()
        presenter.loadAllergens(null)
        presenter.loadCategories()
        presenter.loadLabels()
        presenter.loadProductQuestion()
        binding.textAdditiveProduct.text = bold(getString(R.string.txtAdditives))
        presenter.loadAdditives()
        presenter.loadAnalysisTags()

        val langCode = LocaleHelper.getLanguage(context)
        val imageUrl = product.getImageUrl(langCode)
        if (!imageUrl.isNullOrBlank()) {
            binding.addPhotoLabel.visibility = View.GONE

            // Load Image if isLowBatteryMode is false
            if (!isLowBatteryMode) {
                Utils.picassoBuilder(requireContext())
                        .load(imageUrl)
                        .into(binding.imageViewFront)
            } else {
                binding.imageViewFront.visibility = View.GONE
            }
            mUrlImage = imageUrl
        }

        //TODO use OpenFoodApiService to fetch product by packaging, brands, categories etc
        if (product.getProductName(langCode) != null) {
            binding.textNameProduct.text = product.getProductName(langCode)
        } else {
            binding.textNameProduct.visibility = View.GONE
        }
        if (!product.quantity.isNullOrBlank()) {
            binding.textQuantityProduct.text = product.quantity
        } else {
            binding.textQuantityProduct.visibility = View.GONE
        }
        if (product.brands.isNullOrBlank()) {
            binding.textBrandProduct.isClickable = true
            binding.textBrandProduct.movementMethod = LinkMovementMethod.getInstance()
            binding.textBrandProduct.text = ""
            val brands = product.brands!!.split(",").toTypedArray()
            brands.withIndex().forEach { (i, brand) ->
                if (i > 0) {
                    binding.textBrandProduct.append(", ")
                }
                binding.textBrandProduct.append(Utils.getClickableText(brand.trim { it <= ' ' }, "", SearchType.BRAND, activity, customTabsIntent))
            }
        } else {
            binding.textBrandProduct.visibility = View.GONE
        }
        if (product.embTags.isNotEmpty() && product.embTags.toString().trim { it <= ' ' } != "[]") {
            binding.embText.movementMethod = LinkMovementMethod.getInstance()
            binding.embText.text = bold(getString(R.string.txtEMB))
            binding.embText.append(" ")
            val embTags = product.embTags.toString().replace("[", "").replace("]", "").split(", ").toTypedArray()
            embTags.withIndex().forEach { (i, embTag) ->
                if (i > 0) {
                    binding.embText.append(", ")
                }
                binding.embText.append(Utils.getClickableText(
                        getEmbCode(embTag).trim { it <= ' ' },
                        getEmbUrl(embTag),
                        SearchType.EMB,
                        activity,
                        customTabsIntent
                ))
            }
        } else {
            binding.embText.visibility = View.GONE
            binding.embIcon.visibility = View.GONE
        }

        // if the device does not have a camera, hide the button
        try {
            if (!Utils.isHardwareCameraInstalled(requireActivity())) {
                binding.buttonMorePictures.visibility = View.GONE
            }
        } catch (e: NullPointerException) {
            Log.d(javaClass.simpleName, e.toString())
        }
        if (isFlavors(OFF)) {
            binding.scoresLayout.visibility = View.VISIBLE
            val levelItem: MutableList<NutrientLevelItem> = ArrayList()
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
                customTabActivityHelper!!.mayLaunchUrl(nutritionScoreUri, null, null)
                binding.cvNutritionLights.visibility = View.VISIBLE
                val fatNutriment = nutriments[Nutriments.FAT]
                if (fat != null && fatNutriment != null) {
                    val fatNutrimentLevel = fat.getLocalize(requireContext())
                    levelItem.add(NutrientLevelItem(getString(R.string.txtFat),
                            fatNutriment.displayStringFor100g,
                            fatNutrimentLevel,
                            fat.getImageLevel()))
                }
                val saturatedFatNutriment = nutriments[Nutriments.SATURATED_FAT]
                if (saturatedFat != null && saturatedFatNutriment != null) {
                    val saturatedFatLocalize = saturatedFat.getLocalize(requireContext())
                    levelItem.add(NutrientLevelItem(getString(R.string.txtSaturatedFat), saturatedFatNutriment.displayStringFor100g,
                            saturatedFatLocalize,
                            saturatedFat.getImageLevel()))
                }
                val sugarsNutriment = nutriments[Nutriments.SUGARS]
                if (sugars != null && sugarsNutriment != null) {
                    val sugarsLocalize = sugars.getLocalize(requireContext())
                    levelItem.add(NutrientLevelItem(getString(R.string.txtSugars),
                            sugarsNutriment.displayStringFor100g,
                            sugarsLocalize,
                            sugars.getImageLevel()))
                }
                val saltNutriment = nutriments[Nutriments.SALT]
                if (salt != null && saltNutriment != null) {
                    val saltLocalize = salt.getLocalize(requireContext())
                    levelItem.add(NutrientLevelItem(getString(R.string.txtSalt),
                            saltNutriment.displayStringFor100g,
                            saltLocalize,
                            salt.getImageLevel()))
                }
            } else {
                binding.cvNutritionLights.visibility = View.GONE
            }
            binding.listNutrientLevels.layoutManager = LinearLayoutManager(context)
            binding.listNutrientLevels.adapter = NutrientLevelListAdapter(requireContext(), levelItem)
            refreshNutriScore()
            refreshNovaIcon()
            refreshCO2OrEcoscoreIcon()
            refreshScoresLayout()
        } else {
            binding.scoresLayout.visibility = View.GONE
        }

        //to be sure that top of the product view is visible at start
        binding.textNameProduct.requestFocus()
        binding.textNameProduct.clearFocus()

        //Set refreshing animation to false after all processing is done
        super.refreshView(productState)
    }

    private fun refreshScoresLayout() {
        if (binding.novaGroup.visibility == View.GONE && binding.co2Icon.visibility == View.GONE && binding.imageGrade.visibility == View.GONE && binding.ecoscoreIcon.visibility == View.GONE && binding.addNutriscorePrompt.visibility == View.GONE) {
            binding.scoresLayout.visibility = View.GONE
        } else {
            binding.scoresLayout.visibility = View.VISIBLE
        }
    }

    private fun refreshNutriScore() {
        val gradeDrawable = product.getImageGradeDrawable(requireContext())
        if (gradeDrawable != null) {
            binding.imageGrade.visibility = View.VISIBLE
            binding.imageGrade.setImageDrawable(gradeDrawable)
            binding.imageGrade.setOnClickListener {
                val customTabsIntent = CustomTabsHelper.getCustomTabsIntent(requireContext(), customTabActivityHelper!!.session)
                CustomTabActivityHelper.openCustomTab(requireActivity(), customTabsIntent, nutritionScoreUri!!, WebViewFallback())
            }
        } else {
            binding.imageGrade.visibility = View.GONE
        }
    }

    private fun refreshNovaIcon() {
        if (product.novaGroups != null) {
            binding.novaGroup.visibility = View.VISIBLE
            binding.novaGroup.setImageResource(product.getNovaGroupResource())
            binding.novaGroup.setOnClickListener {
                val uri = Uri.parse(getString(R.string.url_nova_groups))
                val customTabsIntent = CustomTabsHelper.getCustomTabsIntent(requireContext(), customTabActivityHelper!!.session)
                CustomTabActivityHelper.openCustomTab(requireActivity(), customTabsIntent, uri, WebViewFallback())
            }
        } else {
            binding.novaGroup.visibility = View.GONE
            binding.novaGroup.setImageResource(0)
        }
    }

    private fun refreshCO2OrEcoscoreIcon() {
        binding.ecoscoreIcon.visibility = View.GONE
        binding.co2Icon.visibility = View.GONE

        val ecoScoreRes = product.getEcoscoreResource()
        val environmentImpactResource = product.getCO2Resource()

        if (ecoScoreRes != NO_DRAWABLE_RESOURCE) {
            binding.ecoscoreIcon.setImageResource(ecoScoreRes)
            binding.ecoscoreIcon.visibility = View.VISIBLE
        } else if (environmentImpactResource != NO_DRAWABLE_RESOURCE) {
            binding.co2Icon.setImageResource(environmentImpactResource)
            binding.co2Icon.visibility = View.VISIBLE
        }
    }

    private fun refreshNutriScorePrompt() {
        //checks the product states_tags to determine which prompt to be shown
        val statesTags = product.statesTags
        showCategoryPrompt = statesTags.contains("en:categories-to-be-completed") && !hasCategoryInsightQuestion
        showNutrientPrompt = statesTags.contains("en:nutrition-facts-to-be-completed") && product.noNutritionData != "on"

        Log.d(LOG_TAG, "Show category prompt: $showCategoryPrompt")
        Log.d(LOG_TAG, "Show nutrient prompt: $showNutrientPrompt")

        binding.addNutriscorePrompt.visibility = View.VISIBLE
        when {
            showNutrientPrompt && showCategoryPrompt -> {
                // Both true
                binding.addNutriscorePrompt.text = getString(R.string.add_nutrient_category_prompt_text)
            }
            showNutrientPrompt -> {
                // showNutrientPrompt true
                binding.addNutriscorePrompt.text = getString(R.string.add_nutrient_prompt_text)
            }
            showCategoryPrompt -> {
                // showCategoryPrompt true
                binding.addNutriscorePrompt.text = getString(R.string.add_category_prompt_text)
            }
            else -> binding.addNutriscorePrompt.visibility = View.GONE
        }
    }

    override fun showAdditives(additives: List<AdditiveName>) {
        showAdditives(additives, binding.textAdditiveProduct, wikidataClient, this, disp)
    }

    override fun showAdditivesState(state: ProductInfoState) {
        requireActivity().runOnUiThread {
            when (state) {
                ProductInfoState.LOADING -> {
                    binding.textAdditiveProduct.append(getString(R.string.txtLoading))
                    binding.textAdditiveProduct.visibility = View.VISIBLE
                }
                ProductInfoState.EMPTY -> binding.textAdditiveProduct.visibility = View.GONE
            }
        }
    }

    override fun showAnalysisTags(analysisTags: List<AnalysisTagConfig>) {
        requireActivity().runOnUiThread {
            binding.analysisContainer.visibility = View.VISIBLE
            val adapter = IngredientAnalysisTagsAdapter(requireContext(), analysisTags)
            adapter.setOnItemClickListener { view: View, _: Int ->
                val fragment = IngredientsWithTagDialogFragment
                        .newInstance(product, view.getTag(R.id.analysis_tag_config) as AnalysisTagConfig)
                fragment.show(childFragmentManager, "fragment_ingredients_with_tag")
                fragment.onDismissListener = { adapter.filterVisibleTags() }
            }
            binding.analysisTags.adapter = adapter
        }
    }

    override fun showAllergens(allergens: List<AllergenName>) {
        val data = AllergenHelper.computeUserAllergen(product, allergens)
        if (data.isEmpty) {
            return
        }
        if (data.isIncomplete) {
            binding.productAllergenAlertText.setText(R.string.product_incomplete_message)
            binding.productAllergenAlertLayout.visibility = View.VISIBLE
            return
        }
        binding.productAllergenAlertText.text =
                "${resources.getString(R.string.product_allergen_prompt)}\n${data.allergens.joinToString(", ")}"
        binding.productAllergenAlertLayout.visibility = View.VISIBLE
    }

    override fun showCategories(categories: List<CategoryName>) {
        if (categories.isEmpty()) {
            binding.categoriesLayout.visibility = View.GONE
        }
        val categoryProductHelper = CategoryProductHelper(binding.categoriesText, categories, this, wikidataClient, disp)
        categoryProductHelper.showCategories()
        if (categoryProductHelper.containsAlcohol) {
            categoryProductHelper.showAlcoholAlert(binding.textCategoryAlcoholAlert)
        }
    }

    override fun showProductQuestion(question: Question) {
        if (isRemoving) return

        if (!question.isEmpty()) {
            productQuestion = question
            binding.productQuestionText.text = "${question.questionText}\n${question.value}"
            binding.productQuestionLayout.visibility = View.VISIBLE
            hasCategoryInsightQuestion = question.insightType == "category"
        } else {
            binding.productQuestionLayout.visibility = View.GONE
            productQuestion = null
        }

        if (isFlavors(OFF)) {
            refreshNutriScorePrompt()
            refreshScoresLayout()
        }
    }

    private fun onProductQuestionClick() {
        productQuestion?.let {
            QuestionDialog(requireActivity()).run {
                backgroundColor = (R.color.colorPrimaryDark)
                question = productQuestion!!.questionText
                value = productQuestion!!.value
                onPositiveFeedback = { dialog: QuestionDialog ->
                    //init POST request
                    sendProductInsights(productQuestion!!.insightId, AnnotationAnswer.POSITIVE)
                    dialog.dismiss()
                }

                onNegativeFeedback = { dialog: QuestionDialog ->
                    sendProductInsights(productQuestion!!.insightId, AnnotationAnswer.NEGATIVE)
                    dialog.dismiss()
                }

                onAmbiguityFeedback = { dialog: QuestionDialog ->
                    sendProductInsights(productQuestion!!.insightId, AnnotationAnswer.AMBIGUITY)
                    dialog.dismiss()
                }

                onCancelListener = { dialog: QuestionDialog ->
                    dialog.dismiss()
                }
                show()
            }
        }
    }

    private fun sendProductInsights(insightId: String?, annotation: AnnotationAnswer?) {
        this.insightId = insightId
        this.annotation = annotation
        if (requireActivity().isUserSet()) {
            processInsight()
        } else {
            MaterialDialog.Builder(requireActivity()).run {
                title(getString(R.string.sign_in_to_answer))
                positiveText(getString(R.string.sign_in_or_register))
                onPositive { dialog, _ ->
                    loginThenProcessInsight.launch(Unit)
                    dialog.dismiss()
                }
                neutralText(R.string.dialog_cancel)
                onNeutral { dialog, _ -> dialog.dismiss() }
                show()
            }

        }
    }

    private fun processInsight() {
        val insightId = this.insightId ?: error("Property 'insightId' not set.")
        val annotation = this.annotation ?: error("Property 'annotation' not set.")

        presenter.annotateInsight(insightId, annotation)

        Log.d(LOG_TAG, "Annotation $annotation received for insight $insightId")
        binding.productQuestionLayout.visibility = View.GONE
        productQuestion = null
    }

    override fun showAnnotatedInsightToast(annotationResponse: AnnotationResponse) {
        if (annotationResponse.status == "updated" && activity != null) {
            Snackbar.make(binding.root, R.string.product_question_submit_message, BaseTransientBottomBar.LENGTH_SHORT).show()
        }
    }

    override fun showLabels(labels: List<LabelName>) {
        binding.labelsText.text = bold(getString(R.string.txtLabels))
        binding.labelsText.isClickable = true
        binding.labelsText.movementMethod = LinkMovementMethod.getInstance()
        binding.labelsText.append(" ")
        (0 until labels.size - 1).forEach { i ->
            binding.labelsText.append(getLabelTag(labels[i]))
            binding.labelsText.append(", ")
        }
        binding.labelsText.append(getLabelTag(labels.last()))
    }

    override fun showCategoriesState(state: ProductInfoState) {
        requireActivity().runOnUiThread {
            when (state) {
                ProductInfoState.LOADING -> if (context != null) {
                    binding.categoriesText.append(getString(R.string.txtLoading))
                }
                ProductInfoState.EMPTY -> {
                    binding.categoriesText.visibility = View.GONE
                    binding.categoriesIcon.visibility = View.GONE
                }
            }
        }
    }

    override fun showLabelsState(state: ProductInfoState) {
        requireActivity().runOnUiThread {
            when (state) {
                ProductInfoState.LOADING -> binding.labelsText.append(getString(R.string.txtLoading))
                ProductInfoState.EMPTY -> {
                    binding.labelsText.visibility = View.GONE
                    binding.labelsIcon.visibility = View.GONE
                }
            }
        }
    }

    private fun getEmbUrl(embTag: String) =
            mTagDao.queryBuilder().where(TagDao.Properties.Id.eq(embTag)).unique().name ?: embTag

    private fun getEmbCode(embTag: String) =
            mTagDao.queryBuilder().where(TagDao.Properties.Id.eq(embTag)).unique()?.name ?: embTag

    private fun getLabelTag(label: LabelName): CharSequence {
        val spannableStringBuilder = SpannableStringBuilder()
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                if (label.isWikiDataIdPresent) {
                    disp.add(wikidataClient.doSomeThing(label.wikiDataId).subscribe { result ->
                        val activity = activity
                        if (activity?.isFinishing == false) {
                            showBottomSheet(result, label, activity.supportFragmentManager)
                        }
                    })
                } else {
                    ProductSearchActivity.start(requireContext(), SearchType.LABEL, label.labelTag, label.name)
                }
            }
        }
        spannableStringBuilder.append(label.name)
        spannableStringBuilder.setSpan(clickableSpan, 0, spannableStringBuilder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannableStringBuilder
    }

    private fun onAddNutriScorePromptClick() {
        if (isFlavors(OFF)) {
            if (!requireActivity().isUserSet()) {
                startLoginToEditAnd(EDIT_PRODUCT_NUTRITION_AFTER_LOGIN, requireActivity())
            } else {
                editProductNutriscore()
            }
        }
    }

    private fun editProductNutriscore() {
        val intent = Intent(activity, ProductEditActivity::class.java)
        intent.putExtra(ProductEditActivity.KEY_EDIT_PRODUCT, product)
        //adds the information about the prompt when navigating the user to the edit the product
        intent.putExtra(ProductEditActivity.KEY_MODIFY_CATEGORY_PROMPT, showCategoryPrompt)
        intent.putExtra(ProductEditActivity.KEY_MODIFY_NUTRITION_PROMPT, showNutrientPrompt)
        startActivity(intent)
    }

    private fun onCompareProductButtonClick() {
        start(requireContext(), product)
    }

    private fun onShareProductButtonClick() {
        val shareUrl = "${getString(R.string.website_product)}${product.code}"
        val shareBody = "${getString(R.string.msg_share)} $shareUrl"
        val shareSub = "\n\n"
        val title = "Share using"
        startActivity(Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = OpenFoodAPIClient.MIME_TEXT
            putExtra(Intent.EXTRA_SUBJECT, shareSub)
            putExtra(Intent.EXTRA_TEXT, shareBody)
        }, title))
    }

    private fun onEditProductButtonClick() {
        if (!requireActivity().isUserSet()) {
            startLoginToEditAnd(EDIT_PRODUCT_AFTER_LOGIN, requireActivity())
        } else {
            editProduct()
        }
    }

    private fun editProduct() {
        startActivityForResult(Intent(activity, ProductEditActivity::class.java).apply {
            putExtra(ProductEditActivity.KEY_EDIT_PRODUCT, product)
        }, EDIT_REQUEST_CODE)
    }

    private fun onBookmarkProductButtonClick() {
        val activity: Activity = requireActivity()
        val productLists = getProductListsDaoWithDefaultList(activity).loadAll()
        val productBarcode = product.code
        val productName = product.productName
        val imageUrl = product.getImageSmallUrl(LocaleHelper.getLanguage(activity))
        val productDetails = ProductListActivity.getProductBrandsQuantityDetails(product)
        val addToListDialog = MaterialDialog.Builder(activity)
                .title(R.string.add_to_product_lists)
                .customView(R.layout.dialog_add_to_list, true)
                .build()
        addToListDialog.show()
        val dialogView = addToListDialog.customView ?: return

        // Set recycler view
        val addToListRecyclerView: RecyclerView = dialogView.findViewById(R.id.rv_dialogAddToList)
        val addToListAdapter = DialogAddToListAdapter(
                activity,
                productLists,
                productBarcode,
                productName.orEmpty(),
                productDetails,
                imageUrl!!
        )
        addToListRecyclerView.layoutManager = LinearLayoutManager(activity)
        addToListRecyclerView.adapter = addToListAdapter

        // Add listener to text view
        val addToNewList = dialogView.findViewById<TextView>(R.id.tvAddToNewList)
        addToNewList.setOnClickListener {
            activity.startActivity(Intent(activity, ProductListsActivity::class.java).apply {
                putExtra("product", product)

            })
        }
    }


    private fun takeMorePicture() {
        sendOther = true
        doChooseOrTakePhotos(getString(R.string.take_more_pictures))
    }

    private fun openFrontImageFullscreen() {
        if (mUrlImage != null) {
            FullScreenActivityOpener.openForUrl(
                    this,
                    product,
                    ProductImageField.FRONT,
                    mUrlImage,
                    binding.imageViewFront,
            )
        } else {
            // take a picture
            newFrontImage()
        }
    }

    private fun newFrontImage() {
        // add front image.
        sendOther = false
        doChooseOrTakePhotos(getString(R.string.set_img_front))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data)
        val shouldRefresh = (requestCode == EDIT_REQUEST_CODE && resultCode == Activity.RESULT_OK
                || ImagesManageActivity.isImageModified(requestCode, resultCode))
        if (shouldRefresh && activity is ProductViewActivity) {
            (activity as ProductViewActivity?)!!.onRefresh()
        }
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == EDIT_PRODUCT_AFTER_LOGIN && requireActivity().isUserSet()) {
                editProduct()
            }
            if (requestCode == EDIT_PRODUCT_NUTRITION_AFTER_LOGIN && requireActivity().isUserSet()) {
                editProductNutriscore()
            }
        }
    }

    override fun doOnPhotosPermissionGranted() {
        if (sendOther) {
            takeMorePicture()
        } else {
            newFrontImage()
        }
    }


    fun resetScroll() {
        binding.scrollView.scrollTo(0, 0)
        if (binding.analysisTags.adapter != null) {
            (binding.analysisTags.adapter as IngredientAnalysisTagsAdapter?)!!.filterVisibleTags()
        }
    }

    companion object {
        private const val EDIT_PRODUCT_AFTER_LOGIN = 1
        private const val EDIT_PRODUCT_NUTRITION_AFTER_LOGIN = 3
        private const val EDIT_REQUEST_CODE = 2
        private val LOG_TAG = this::class.simpleName!!
    }
}