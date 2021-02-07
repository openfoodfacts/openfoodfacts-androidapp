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
package openfoodfacts.github.scrachx.openfood.features.product.view.ingredients

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.viewpager2.widget.ViewPager2
import com.afollestad.materialdialogs.MaterialDialog
import com.squareup.picasso.Picasso
import io.reactivex.rxkotlin.addTo
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.databinding.FragmentIngredientsProductBinding
import openfoodfacts.github.scrachx.openfood.features.FullScreenActivityOpener
import openfoodfacts.github.scrachx.openfood.features.ImagesManageActivity
import openfoodfacts.github.scrachx.openfood.features.LoginActivity.Companion.LoginContract
import openfoodfacts.github.scrachx.openfood.features.additives.AdditiveFragmentHelper.showAdditives
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_STATE
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.PerformOCRContract
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.SendUpdatedImgContract
import openfoodfacts.github.scrachx.openfood.features.search.ProductSearchActivity.Companion.start
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.models.entities.SendProduct
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenNameDao
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.network.WikiDataApiClient
import openfoodfacts.github.scrachx.openfood.utils.*
import java.io.File
import java.util.regex.Pattern

class IngredientsProductFragment : BaseFragment(), IIngredientsProductPresenter.View {
    private var _binding: FragmentIngredientsProductBinding? = null
    private val binding get() = _binding!!

    private val loginPref by lazy { requireActivity().getLoginPreferences() }
    private val client by lazy { OpenFoodAPIClient(requireContext()) }
    private val wikidataClient by lazy { WikiDataApiClient() }

    private val performOCRLauncher = registerForActivityResult(PerformOCRContract())
    { result -> if (result) onRefresh() }
    private val updateImagesLauncher = registerForActivityResult(SendUpdatedImgContract())
    { result -> if (result) onRefresh() }
    private val loginLauncher = registerForActivityResult(LoginContract())
    { ProductEditActivity.start(requireContext(), productState.product!!, sendUpdatedIngredientsImage, ingredientExtracted) }

    private lateinit var productState: ProductState
    private lateinit var customTabActivityHelper: CustomTabActivityHelper
    private lateinit var customTabsIntent: CustomTabsIntent
    private lateinit var presenter: IIngredientsProductPresenter.Actions
    private val photoReceiverHandler: PhotoReceiverHandler = PhotoReceiverHandler { onPhotoReturned(it) }

    private var ingredientExtracted = false

    private var mSendProduct: SendProduct? = null

    var ingredients: String? = null
        private set

    private var sendUpdatedIngredientsImage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productState = requireProductState()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentIngredientsProductBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.extractIngredientsPrompt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_box_blue_18dp, 0, 0, 0)
        binding.changeIngImg.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_a_photo_blue_18dp, 0, 0, 0)

        binding.changeIngImg.setOnClickListener { changeIngImage() }
        binding.novaMethodLink.setOnClickListener { novaMethodLinkDisplay() }
        binding.extractIngredientsPrompt.setOnClickListener { extractIngredients() }
        binding.imageViewIngredients.setOnClickListener { openFullScreen() }

        refreshView(productState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        customTabActivityHelper = CustomTabActivityHelper()
        customTabsIntent = CustomTabsHelper.getCustomTabsIntent(requireContext(), customTabActivityHelper.session)
        productState = requireProductState()
    }

    override fun refreshView(productState: ProductState) {
        super.refreshView(productState)
        this.productState = productState
        val langCode = LocaleHelper.getLanguage(context)

        if (arguments != null) mSendProduct = getSendProduct()

        val product = this.productState.product!!
        presenter = IngredientsProductPresenter(product, this).apply { addTo(disp) }
        val vitaminTagsList = product.vitaminTags
        val aminoAcidTagsList = product.aminoAcidTags
        val mineralTags = product.mineralTags
        val otherNutritionTags = product.otherNutritionTags
        if (vitaminTagsList.isNotEmpty()) {
            binding.cvVitaminsTagsText.visibility = View.VISIBLE
            binding.vitaminsTagsText.text = bold(getString(R.string.vitamin_tags_text))
            binding.vitaminsTagsText.append(buildStringBuilder(vitaminTagsList, SPACE))
        }
        if (aminoAcidTagsList.isNotEmpty()) {
            binding.cvAminoAcidTagsText.visibility = View.VISIBLE
            binding.aminoAcidTagsText.text = bold(getString(R.string.amino_acid_tags_text))
            binding.aminoAcidTagsText.append(buildStringBuilder(aminoAcidTagsList, SPACE))
        }
        if (mineralTags.isNotEmpty()) {
            binding.cvMineralTagsText.visibility = View.VISIBLE
            binding.mineralTagsText.text = bold(getString(R.string.mineral_tags_text))
            binding.mineralTagsText.append(buildStringBuilder(mineralTags, SPACE))
        }
        if (otherNutritionTags.isNotEmpty()) {
            binding.otherNutritionTags.visibility = View.VISIBLE
            binding.otherNutritionTags.text = bold(getString(R.string.other_tags_text))
            binding.otherNutritionTags.append(buildStringBuilder(otherNutritionTags, SPACE))
        }
        binding.textAdditiveProduct.text = bold(getString(R.string.txtAdditives))
        presenter.loadAdditives()

        if (!product.getImageIngredientsUrl(langCode).isNullOrBlank()) {
            binding.ingredientImagetipBox.setTipMessage(getString(R.string.onboarding_hint_msg, getString(R.string.image_edit_tip)))
            binding.ingredientImagetipBox.loadToolTip()
            binding.addPhotoLabel.visibility = View.GONE
            binding.changeIngImg.visibility = View.VISIBLE

            // Load Image if isLowBatteryMode is false
            if (!requireContext().isBatteryLevelLow()) {
                Utils.picassoBuilder(requireContext())
                        .load(product.getImageIngredientsUrl(langCode))
                        .into(binding.imageViewIngredients)
            } else {
                binding.imageViewIngredients.visibility = View.GONE
            }
            ingredients = product.getImageIngredientsUrl(langCode)
        }

        //useful when this fragment is used in offline saving
        if (mSendProduct != null && !mSendProduct!!.imgupload_ingredients.isNullOrBlank()) {
            binding.addPhotoLabel.visibility = View.GONE
            ingredients = mSendProduct!!.imgupload_ingredients
            Picasso.get().load(LOCALE_FILE_SCHEME + ingredients).config(Bitmap.Config.RGB_565).into(binding.imageViewIngredients)
        }
        val allergens = getAllergens()
        if (!product.getIngredientsText(langCode).isNullOrEmpty()) {
            binding.cvTextIngredientProduct.visibility = View.VISIBLE
            var txtIngredients = SpannableStringBuilder(product.getIngredientsText(langCode)!!.replace("_", ""))
            txtIngredients = setSpanBoldBetweenTokens(txtIngredients, allergens)
            if (product.getIngredientsText(langCode).isNullOrEmpty()) {
                binding.extractIngredientsPrompt.visibility = View.VISIBLE
            }
            val ingredientsListAt = txtIngredients.toString().indexOf(":").coerceAtLeast(0)
            if (txtIngredients.toString().substring(ingredientsListAt).trim { it <= ' ' }.isNotEmpty()) {
                binding.textIngredientProduct.text = txtIngredients
            }
        } else {
            binding.cvTextIngredientProduct.visibility = View.GONE
            if (!product.getImageIngredientsUrl(langCode).isNullOrBlank()) {
                binding.extractIngredientsPrompt.visibility = View.VISIBLE
            }
        }
        presenter.loadAllergens()
        if (!product.traces.isNullOrBlank()) {
            val language = LocaleHelper.getLanguage(context)
            binding.cvTextTraceProduct.visibility = View.VISIBLE
            binding.textTraceProduct.movementMethod = LinkMovementMethod.getInstance()
            binding.textTraceProduct.text = bold(getString(R.string.txtTraces))
            binding.textTraceProduct.append(" ")
            val traces = product.traces.split(",")
            traces.withIndex().forEach { (i, trace) ->
                if (i > 0) binding.textTraceProduct.append(", ")
                binding.textTraceProduct.append(Utils.getClickableText(
                        getTracesName(language, trace),
                        trace,
                        SearchType.TRACE,
                        requireActivity(),
                        customTabsIntent
                ))
            }
        } else {
            binding.cvTextTraceProduct.visibility = View.GONE
        }
        val novaGroups = product.novaGroups
        if (novaGroups != null) {
            binding.novaLayout.visibility = View.VISIBLE
            binding.novaExplanation.text = Utils.getNovaGroupExplanation(novaGroups, requireContext()) ?: ""
            binding.novaGroup.setImageResource(product.getNovaGroupResource())
            binding.novaGroup.setOnClickListener {
                val uri = Uri.parse(getString(R.string.url_nova_groups))
                val tabsIntent = CustomTabsHelper.getCustomTabsIntent(requireContext(), customTabActivityHelper.session)
                CustomTabActivityHelper.openCustomTab(requireActivity(), tabsIntent, uri, WebViewFallback())
            }
        } else {
            binding.novaLayout.visibility = View.GONE
        }
    }

    private fun getTracesName(languageCode: String, tag: String): String {
        val allergenName = Utils.daoSession.allergenNameDao.queryBuilder()
                .where(AllergenNameDao.Properties.AllergenTag.eq(tag), AllergenNameDao.Properties.LanguageCode.eq(languageCode))
                .unique()
        return if (allergenName != null) allergenName.name else tag
    }

    private fun buildStringBuilder(stringList: List<String>, prefix: String) = StringBuilder().apply {
        append(prefix)
        stringList.forEach { otherSubstance ->
            append(trimLanguagePartFromString(otherSubstance))
            append(", ")
        }
    }

    private fun getAllergensTag(allergen: AllergenName): CharSequence {
        val ssb = SpannableStringBuilder()
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                if (allergen.isWikiDataIdPresent) {
                    wikidataClient.doSomeThing(
                            allergen.wikiDataId
                    ).subscribe { result ->
                        val activity = activity
                        if (activity?.isFinishing == false) {
                            showBottomSheet(result, allergen, activity.supportFragmentManager)
                        }
                    }.addTo(disp)
                } else {
                    start(requireContext(), SearchType.ALLERGEN, allergen.allergenTag, allergen.name)
                }
            }
        }
        ssb.append(allergen.name)
        ssb.setSpan(clickableSpan, 0, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        // If allergen is not in the taxonomy list then italicize it
        if (!allergen.isNotNull) {
            ssb.setSpan(StyleSpan(Typeface.ITALIC), 0, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return ssb
    }

    /**
     * @return the string after trimming the language code from the tags
     * like it returns folic-acid for en:folic-acid
     */
    private fun trimLanguagePartFromString(string: String) = string.substring(3)

    private fun setSpanBoldBetweenTokens(text: CharSequence, allergens: List<String>): SpannableStringBuilder {
        return SpannableStringBuilder(text).also {
            val m = INGREDIENT_PATTERN.matcher(it)
            while (m.find()) {
                val tm = m.group()
                val allergenValue = tm.replace("[(),.-]+".toRegex(), "")
                for (allergen in allergens) {
                    if (allergen.equals(allergenValue, ignoreCase = true)) {
                        var start = m.start()
                        var end = m.end()
                        if (tm.contains("(")) {
                            start += 1
                        } else if (tm.contains(")")) {
                            end -= 1
                        }
                        it.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            }
            it.insert(0, bold(getString(R.string.txtIngredients) + ' '))
        }
    }

    override fun showAdditives(additives: List<AdditiveName>) {
        showAdditives(additives, binding.textAdditiveProduct, wikidataClient, this, disp)
    }

    override fun showAdditivesState(state: ProductInfoState) {
        when (state) {
            ProductInfoState.LOADING -> {
                binding.cvTextAdditiveProduct.visibility = View.VISIBLE
                binding.textAdditiveProduct.append(getString(R.string.txtLoading))
            }
            ProductInfoState.EMPTY -> binding.cvTextAdditiveProduct.visibility = View.GONE
        }
    }

    override fun showAllergens(allergens: List<AllergenName>) {
        binding.textSubstanceProduct.movementMethod = LinkMovementMethod.getInstance()
        binding.textSubstanceProduct.text = bold(getString(R.string.txtSubstances))
        binding.textSubstanceProduct.append(" ")
        allergens.withIndex().forEach { (i, allergen) ->
            binding.textSubstanceProduct.append(getAllergensTag(allergen))
            // Add comma if not the last item
            if (i != allergens.lastIndex) binding.textSubstanceProduct.append(", ")
        }
    }


    fun changeIngImage() {
        sendUpdatedIngredientsImage = true
        if (activity == null) return
        val viewPager = requireActivity().findViewById<ViewPager2>(R.id.pager)
        if (isFlavors(AppFlavors.OFF)) {
            if (loginPref.getString("user", "").isNullOrEmpty()) {
                showSignInDialog()
            } else {
                productState = requireProductState()
                updateImagesLauncher.launch(productState.product)
            }
        }
        when {
            isFlavors(AppFlavors.OPFF) -> viewPager.currentItem = 4
            isFlavors(AppFlavors.OBF) -> viewPager.currentItem = 1
            isFlavors(AppFlavors.OPF) -> viewPager.currentItem = 0
        }
    }

    override fun showAllergensState(state: ProductInfoState) {
        when (state) {
            ProductInfoState.LOADING -> {
                binding.textSubstanceProduct.visibility = View.VISIBLE
                binding.textSubstanceProduct.append(getString(R.string.txtLoading))
            }
            ProductInfoState.EMPTY -> binding.textSubstanceProduct.visibility = View.GONE
        }
    }

    private fun getAllergens(): List<String> {
        val allergens = productState.product!!.allergensTags
        return if (productState.product != null && allergens.isNotEmpty()) allergens else emptyList()
    }

    private fun novaMethodLinkDisplay() {
        if (productState.product != null && productState.product!!.novaGroups != null) {
            val uri = Uri.parse(getString(R.string.url_nova_groups))
            val tabsIntent = CustomTabsHelper.getCustomTabsIntent(requireContext(), customTabActivityHelper.session)
            CustomTabActivityHelper.openCustomTab(requireActivity(), tabsIntent, uri, WebViewFallback())
        }
    }

    fun extractIngredients() {
        if (!isAdded) return
        ingredientExtracted = true
        val settings = requireContext().getLoginPreferences()
        if (settings.getString("user", "")!!.isEmpty()) {
            showSignInDialog()
        } else {
            productState = requireProductState()
            performOCRLauncher.launch(productState.product)
        }
    }

    private fun showSignInDialog() {
        MaterialDialog.Builder(requireContext()).apply {
            title(R.string.sign_in_to_edit)
            positiveText(R.string.txtSignIn)
            negativeText(R.string.dialog_cancel)
            onPositive { dialog, _ ->
                loginLauncher.launch(Unit)
                dialog.dismiss()
            }
            onNegative { dialog, _ -> dialog.dismiss() }
            build()
            show()
        }
    }

    private fun openFullScreen() {
        if (ingredients != null && productState.product != null) {
            FullScreenActivityOpener.openForUrl(
                    this,
                    productState.product!!,
                    ProductImageField.INGREDIENTS,
                    ingredients,
                    binding.imageViewIngredients,
            )
        } else {
            newIngredientImage()
        }
    }

    private fun newIngredientImage() {
        doChooseOrTakePhotos(getString(R.string.ingredients_picture))
    }

    override fun doOnPhotosPermissionGranted() {
        newIngredientImage()
    }

    private fun onPhotoReturned(newPhotoFile: File) {
        val image = ProductImage(productState.code!!, ProductImageField.INGREDIENTS, newPhotoFile)
        image.filePath = newPhotoFile.absolutePath
        client.postImg(image).subscribe().addTo(disp)
        binding.addPhotoLabel.visibility = View.GONE
        ingredients = newPhotoFile.absolutePath
        Picasso.get()
                .load(newPhotoFile)
                .fit()
                .into(binding.imageViewIngredients)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (ImagesManageActivity.isImageModified(requestCode, resultCode)) {
            onRefresh()
        }
        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        val INGREDIENT_PATTERN: Pattern = Pattern.compile("[\\p{L}\\p{Nd}(),.-]+")
        fun newInstance(productState: ProductState) = IngredientsProductFragment().apply {
            arguments = Bundle().apply {
                putSerializable(KEY_STATE, productState)
            }
        }
    }
}