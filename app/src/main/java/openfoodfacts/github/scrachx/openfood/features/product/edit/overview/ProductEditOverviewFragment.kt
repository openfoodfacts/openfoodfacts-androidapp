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
package openfoodfacts.github.scrachx.openfood.features.product.edit.overview

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.net.toFile
import androidx.fragment.app.viewModels
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.integration.android.IntentIntegrator
import com.hootsuite.nachos.NachoTextView
import com.hootsuite.nachos.terminator.ChipTerminatorHandler
import com.hootsuite.nachos.validator.ChipifyingNachoValidator
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import openfoodfacts.github.scrachx.openfood.AppFlavors.OBF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsView
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.databinding.FragmentAddProductOverviewBinding
import openfoodfacts.github.scrachx.openfood.features.adapters.autocomplete.EmbCodeAutoCompleteAdapter
import openfoodfacts.github.scrachx.openfood.features.adapters.autocomplete.PeriodAfterOpeningAutoCompleteAdapter
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_PERFORM_OCR
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_SEND_UPDATED
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditFragment
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.category.CategoryNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.label.LabelNameDao
import openfoodfacts.github.scrachx.openfood.models.entities.tag.TagDao
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.ApiFields.Keys.lcProductNameKey
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.*
import openfoodfacts.github.scrachx.openfood.utils.FileDownloader.download
import org.apache.commons.lang3.StringUtils
import org.jetbrains.annotations.Contract
import java.io.File
import java.util.*
import javax.inject.Inject

/**
 * Product Overview fragment of AddProductActivity
 */
@AndroidEntryPoint
class ProductEditOverviewFragment : ProductEditFragment() {
    private var _binding: FragmentAddProductOverviewBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var client: OpenFoodAPIClient

    @Inject
    lateinit var matomoAnalytics: MatomoAnalytics

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var localeManager: LocaleManager

    private lateinit var appLanguageCode: String
    private val photoReceiverHandler by lazy {
        PhotoReceiverHandler(sharedPreferences) { newPhotoFile ->
            photoFile = newPhotoFile
            val image: ProductImage
            val position: Int
            if (isFrontImagePresent) {
                image = ProductImage(barcode!!, ProductImageField.FRONT, newPhotoFile, localeManager.getLanguage())
                frontImageUrl = newPhotoFile.absolutePath
                position = 0
            } else {
                image = ProductImage(barcode!!, ProductImageField.OTHER, newPhotoFile, localeManager.getLanguage())
                position = 3
            }
            image.filePath = newPhotoFile.toURI().path
            (activity as? ProductEditActivity)?.addToPhotoMap(image, position)

            hideImageProgress(false, StringUtils.EMPTY)
        }
    }

    private var barcode: String? = null
    private var editingMode = false
    private var isFrontImagePresent = false
    private var languageCode: String? = null
    private var frontImageUrl: String? = null
    private var savedProduct: OfflineSavedProduct? = null
    private var photoFile: File? = null
    private var product: Product? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddProductOverviewBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnOtherPictures.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_a_photo_blue_18dp, 0, 0, 0)

        binding.btnNext.setOnClickListener { next() }
        binding.imgFront.setOnClickListener { openFrontImage() }
        binding.btnEditImgFront.setOnClickListener { chooseOrTakeFrontImage() }
        binding.btnOtherPictures.setOnClickListener { chooseOrTakeOtherImage() }
        binding.sectionManufacturingDetails.setOnClickListener { toggleManufacturingSectionVisibility() }
        binding.sectionPurchasingDetails.setOnClickListener { togglePurchasingSectionVisibility() }
        binding.extractUrlText.setOnClickListener { scanProductLink() }
        binding.searchUrlText.setOnClickListener { searchProductLink() }
        binding.language.setOnClickListener { selectProductLanguage() }

        //checks the information about the prompt clicked and takes action accordingly
        if (requireActivity().intent.getBooleanExtra(ProductEditActivity.KEY_MODIFY_CATEGORY_PROMPT, false)) {
            binding.categories.requestFocus()
        } else if (requireActivity().intent.getBooleanExtra(ProductEditActivity.KEY_MODIFY_NUTRITION_PROMPT, false)) {
            (requireActivity() as ProductEditActivity).proceed()
        }
        appLanguageCode = localeManager.getLanguage()
        val args = arguments
        if (args == null) {
            Toast.makeText(activity, R.string.error_adding_product_details, Toast.LENGTH_SHORT).show()
            requireActivity().finish()
            return
        }

        product = args.getSerializable("product") as Product?
        savedProduct = getEditOfflineProductFromArgs()
        editingMode = args.getBoolean(ProductEditActivity.KEY_IS_EDITING)


        binding.language.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down, 0)
        binding.sectionManufacturingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_grey_24dp, 0)
        binding.sectionPurchasingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_grey_24dp, 0)

        if (product != null) {
            barcode = product!!.code
        }

        if (editingMode && product != null) {
            barcode = product!!.code

            val languageToUse = if (product!!.isLanguageSupported(appLanguageCode)) appLanguageCode
            else product!!.lang

            preFillProductValues(getLCOrDefault(languageToUse))
        } else if (savedProduct != null) {
            barcode = savedProduct!!.barcode
            preFillValuesFromOffline()
        } else {
            // Addition
            enableFastAdditionMode(requireContext().isFastAdditionMode())
        }

        binding.barcode.text = "${getString(R.string.txtBarcode)} $barcode"

        if (isFlavors(OBF, OPF)) {
            binding.btnOtherPictures.visibility = View.GONE
        }
        if (args.getBoolean(KEY_PERFORM_OCR)) {
            (activity as ProductEditActivity).proceed()
        }
        if (args.getBoolean(KEY_SEND_UPDATED)) {
            (activity as ProductEditActivity).proceed()
        }

        initializeChips()

        setupAutoSuggestion()

        if (activity is ProductEditActivity && (activity as ProductEditActivity).initialValues != null) {
            addAllFieldsToMap((activity as ProductEditActivity).initialValues!!)
        }
        if (languageCode.isNullOrEmpty()) {
            setProductLanguage(appLanguageCode)
        }
    }

    override fun onResume() {
        super.onResume()
        matomoAnalytics.trackView(AnalyticsView.ProductEditOverview)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * To enable fast addition mode
     *
     * @param enable
     */
    private fun enableFastAdditionMode(enable: Boolean) {
        val visibility = if (enable) View.GONE else View.VISIBLE

        binding.sectionManufacturingDetails.visibility = visibility
        binding.sectionPurchasingDetails.visibility = visibility
        binding.packaging.visibility = visibility
        binding.label.visibility = visibility
        binding.periodOfTimeAfterOpeningTil.visibility = visibility

        changeVisibilityManufacturingSectionTo(visibility)
        changePurchasingSectionVisibilityTo(visibility)

        binding.greyLine2.visibility = visibility
        binding.greyLine3.visibility = visibility
        binding.greyLine4.visibility = visibility
    }

    /**
     * Pre fill the fields of the product which are already present on the server.
     */
    private fun preFillProductValues(lang: String) {
        if (!product!!.productName.isNullOrEmpty()) {
            binding.name.setText(product!!.productName)
        }
        if (!product!!.quantity.isNullOrEmpty()) {
            binding.quantity.setText(product!!.quantity)
        }
        if (!product!!.brands.isNullOrEmpty()) {
            binding.brand.setText(extractProductBrandsChipsValues(product))
        }
        if (!product!!.packaging.isNullOrEmpty()) {
            binding.packaging.setText(extractProductPackagingChipsValues(product))
        }
        if (!product!!.categoriesTags.isNullOrEmpty()) {
            binding.categories.setText(extractProductCategoriesChipsValues(product))
        }
        if (!product!!.labelsTags.isNullOrEmpty()) {
            binding.label.setText(extractProductTagsChipsValues(product))
        }
        if (!product!!.origins.isNullOrEmpty()) {
            binding.originOfIngredients.setText(extractProductOriginsChipsValues(product))
        }
        if (!product!!.manufacturingPlaces.isNullOrEmpty()) {
            binding.manufacturingPlace.setText(product!!.manufacturingPlaces)
        }
        if (product!!.embTags.toString().trim { it <= ' ' } != "[]") {
            binding.embCode.setText(extractProductEmbTagsChipsValues(product))
        }
        if (!product!!.manufacturerUrl.isNullOrEmpty()) {
            binding.link.setText(product!!.manufacturerUrl)
        }
        if (!product!!.purchasePlaces.isNullOrEmpty()) {
            binding.countryWherePurchased.setText(extractProductPurchasePlaces(product))
        }
        if (!product!!.stores.isNullOrEmpty()) {
            binding.stores.setText(extractProductStoresChipValues(product))
        }
        if (!product!!.countriesTags.isNullOrEmpty()) {
            val chipValues = extractProductCountriesTagsChipValues(product).toMutableList()
            //Also add the country set by the user in preferences
            val savedCountry = sharedPreferences.getString(getString(R.string.pref_country_key), "") ?: ""

            if (savedCountry.isNotEmpty()) chipValues.add(savedCountry)

            binding.countriesWhereSold.setText(chipValues)
        }
        setProductLanguage(lang)
    }

    @Contract("null -> new")
    private fun extractProductCountriesTagsChipValues(product: Product?) =
        product?.countriesTags?.map { getCountryName(appLanguageCode, it) } ?: emptyList()

    @Contract("null -> new")
    private fun extractProductStoresChipValues(product: Product?) =
        product?.stores?.split(',')?.map { it.trim() } ?: emptyList()

    @Contract("null -> new")
    private fun extractProductPurchasePlaces(product: Product?) =
        product?.purchasePlaces?.split(',')?.map { it.trim() } ?: emptyList()

    @Contract("null -> new")
    private fun extractProductEmbTagsChipsValues(product: Product?) = (product?.embTags ?: emptyList())
        .toString().removeSurrounding("[", "]")
        .split(",")
        .map { getEmbCode(it.trim()) }

    @Contract("null -> new")
    private fun extractProductOriginsChipsValues(product: Product?) =
        product?.origins?.split(Regex("\\s*,\\s*")) ?: emptyList()

    @Contract("null -> new")
    private fun extractProductTagsChipsValues(product: Product?) =
        product?.labelsTags?.map { getLabelName(appLanguageCode, it) } ?: emptyList()

    @Contract("null -> new")
    private fun extractProductCategoriesChipsValues(product: Product?) =
        product?.categoriesTags?.map { getCategoryName(appLanguageCode, it) } ?: emptyList()

    @Contract("null -> new")
    private fun extractProductPackagingChipsValues(product: Product?) =
        product?.packaging?.split(',')?.map { it.trim() } ?: emptyList()

    @Contract("null -> new")
    private fun extractProductBrandsChipsValues(product: Product?) =
        product?.brands?.split(',')?.map { it.trim() } ?: emptyList()

    /**
     * Loads front image of the product into the imageview
     *
     * @param language language used for adding product
     */
    private fun loadFrontImage(language: String?) {
        photoFile = null
        val imageFrontUrl = product!!.getImageFrontUrl(language)
        if (imageFrontUrl != null && imageFrontUrl.isNotEmpty()) {
            frontImageUrl = imageFrontUrl
            binding.imageProgress.visibility = View.VISIBLE
            binding.btnEditImgFront.visibility = View.INVISIBLE
            picasso
                .load(imageFrontUrl)
                .resize(requireContext().dpsToPixel(50), requireContext().dpsToPixel(50))
                .centerInside()
                .into(binding.imgFront, object : Callback {
                    override fun onSuccess() = frontImageLoaded()

                    override fun onError(ex: Exception) = frontImageLoaded()
                })
        }
    }

    /**
     * @param languageCode 2 letter language code. example hi, en etc.
     * @param tag the complete tag. example en:india
     * @return returns the name of the country if found in the db or else returns the tag itself.
     */
    private fun getCountryName(languageCode: String?, tag: String) =
        daoSession.countryNameDao.queryBuilder()
            .where(
                CountryNameDao.Properties.CountyTag.eq(tag),
                CountryNameDao.Properties.LanguageCode.eq(languageCode)
            )
            .unique()?.name ?: tag

    /**
     * @param languageCode 2 letter language code. example de, en etc.
     * @param tag the complete tag. example de:hoher-omega-3-gehalt
     * @return returns the name of the label if found in the db or else returns the tag itself.
     */
    private fun getLabelName(languageCode: String?, tag: String) =
        daoSession.labelNameDao.queryBuilder()
            .where(
                LabelNameDao.Properties.LabelTag.eq(tag),
                LabelNameDao.Properties.LanguageCode.eq(languageCode)
            ).unique()
            ?.name ?: tag

    /**
     * @param languageCode 2 letter language code. example en, fr etc.
     * @param tag the complete tag. example en:plant-based-foods-and-beverages
     * @return returns the name of the category (example Plant-based foods and beverages) if found in the db or else returns the tag itself.
     */
    private fun getCategoryName(languageCode: String?, tag: String): String {
        return daoSession.categoryNameDao
            .queryBuilder()
            .where(
                CategoryNameDao.Properties.CategoryTag.eq(tag),
                CategoryNameDao.Properties.LanguageCode.eq(languageCode)
            ).unique()
            ?.name ?: tag
    }

    private fun getEmbCode(embTag: String) =
        daoSession.tagDao.queryBuilder()
            .where(TagDao.Properties.Id.eq(embTag))
            .unique()
            ?.name ?: embTag

    /**
     * Pre fill the fields if the product is already present in SavedProductOffline db.
     */
    private fun preFillValuesFromOffline() {
        val productDetails = savedProduct!!.productDetails
        if (savedProduct!!.imageFrontLocalUrl != null) {
            binding.imageProgress.visibility = View.VISIBLE
            binding.btnEditImgFront.visibility = View.INVISIBLE
            frontImageUrl = savedProduct!!.imageFrontLocalUrl
            Picasso.get()
                .load(frontImageUrl)
                .resize(requireContext().dpsToPixel(50), requireContext().dpsToPixel(50))
                .centerInside()
                .into(binding.imgFront, object : Callback {
                    override fun onSuccess() = frontImageLoaded()
                    override fun onError(ex: Exception) = frontImageLoaded()
                })
        }
        val offLineProductLanguage = savedProduct!!.language
        offLineProductLanguage?.let {
            if (it.isNotEmpty()) setProductLanguage(it)
        }
        val offlineProductName = savedProduct!!.name
        if (!offlineProductName.isNullOrEmpty()) {
            binding.name.setText(offlineProductName)
        }
        if (productDetails[ApiFields.Keys.QUANTITY] != null) {
            binding.quantity.setText(productDetails[ApiFields.Keys.QUANTITY])
        }
        preFillChip(productDetails, ApiFields.Keys.BRANDS, binding.brand)
        preFillChip(productDetails, ApiFields.Keys.PACKAGING, binding.packaging)
        preFillChip(productDetails, ApiFields.Keys.CATEGORIES, binding.categories)
        preFillChip(productDetails, ApiFields.Keys.LABELS, binding.label)
        preFillChip(productDetails, ApiFields.Keys.ORIGINS, binding.originOfIngredients)
        if (productDetails[ApiFields.Keys.MANUFACTURING_PLACES] != null) {
            binding.manufacturingPlace.setText(productDetails[ApiFields.Keys.MANUFACTURING_PLACES])
        }
        preFillChip(productDetails, ApiFields.Keys.EMB_CODES, binding.embCode)
        if (productDetails[ApiFields.Keys.LINK] != null) {
            binding.link.setText(productDetails[ApiFields.Keys.LINK])
        }
        preFillChip(productDetails, ApiFields.Keys.ADD_PURCHASE, binding.countryWherePurchased)
        preFillChip(productDetails, ApiFields.Keys.ADD_STORES, binding.stores)
        preFillChip(productDetails, ApiFields.Keys.ADD_COUNTRIES, binding.countriesWhereSold)
    }

    private fun frontImageLoaded() {
        binding.imageProgress.visibility = View.GONE
        binding.btnEditImgFront.visibility = View.VISIBLE
    }

    private fun preFillChip(productDetails: Map<String, String?>, paramName: String, nachoTextView: NachoTextView) {
        productDetails[paramName]?.let {
            val chipValues = it.split(',').map(String::trim)
            nachoTextView.setText(chipValues)
        }
    }

    private fun initializeChips() = listOf(
        binding.brand,
        binding.packaging,
        binding.categories,
        binding.label,
        binding.originOfIngredients,
        binding.embCode,
        binding.countryWherePurchased,
        binding.stores,
        binding.countriesWhereSold
    ).forEach { nachoTextView ->
        nachoTextView.addChipTerminator(',', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_CURRENT_TOKEN)
        nachoTextView.setNachoValidator(ChipifyingNachoValidator())
        nachoTextView.enableEditChipOnTouch(false, true)
    }

    @Inject
    lateinit var daoSession: DaoSession

    private val viewModel: OverviewViewModel by viewModels()

    /**
     * Auto load suggestions into various NachoTextViews
     */
    private fun setupAutoSuggestion() {

        viewModel.suggestCountries.observe(viewLifecycleOwner) { countries ->
            val countryAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                countries
            )

            binding.originOfIngredients.setAdapter(countryAdapter)
            binding.countryWherePurchased.setAdapter(countryAdapter)
            binding.countriesWhereSold.setAdapter(countryAdapter)
            binding.embCode.setAdapter(
                EmbCodeAutoCompleteAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    client
                )
            )
        }
        viewModel.suggestLabels.observe(viewLifecycleOwner) { suggestLabels ->
            binding.label.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    suggestLabels
                )
            )
        }

        viewModel.suggestCategories.observe(viewLifecycleOwner) { categories ->
            binding.categories.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    categories
                )
            )
        }

        viewModel.suggestStores.observe(viewLifecycleOwner) { suggestStores ->
            binding.stores.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    suggestStores
                )
            )
        }
        viewModel.suggestBrands.observe(viewLifecycleOwner) { suggestBrands ->
            binding.brand.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    suggestBrands
                )
            )
        }

        if (isFlavors(OBF)) {
            binding.periodOfTimeAfterOpeningTil.visibility = View.VISIBLE
            val customAdapter = PeriodAfterOpeningAutoCompleteAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                client
            )
            binding.periodOfTimeAfterOpening.setAdapter(customAdapter)
        }
    }

    /**
     * Set language of the product to the language entered
     *
     * @param lang language code
     */
    private fun setProductLanguage(lang: String) {
        languageCode = lang
        val current = LocaleUtils.parseLocale(lang)
        binding.language.setText(R.string.product_language)
        binding.language.append(current.getDisplayName(current).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() })

        (activity as? ProductEditActivity)?.setProductLanguage(lang)

        if (editingMode) {
            loadFrontImage(lang)
            val fields = "ingredients_text_$lang,product_name_$lang"
            client.getProductStateFull(product!!.code, fields)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    binding.name.setText(getString(R.string.txtLoading))
                    binding.name.isActivated = false
                }
                .doOnError {
                    Log.e(ProductEditOverviewFragment::class.java.simpleName, "Error retrieving product state from server api.", it)
                    binding.name.setText(StringUtils.EMPTY)
                    binding.name.isActivated = true
                }
                .subscribe { productState ->
                    if (productState.status != 1L) {
                        Log.e(
                            ProductEditOverviewFragment::class.simpleName,
                            "Retrieved product with code ${productState.code}, but status was not successful."
                        )
                        binding.name.setText(StringUtils.EMPTY)
                        binding.name.isActivated = true
                        return@subscribe
                    }
                    val product = productState.product!!
                    if (product.getProductName(lang) != null) {
                        if (languageCode == lang) {
                            binding.name.setText(product.getProductName(lang))
                            binding.name.isActivated = true
                            if (activity is ProductEditActivity) {
                                (activity as ProductEditActivity).setIngredients("set", product.getIngredientsText(lang))
                                (activity as ProductEditActivity).updateLanguage()
                            }
                        }
                    } else {
                        binding.name.setText(StringUtils.EMPTY)
                        binding.name.isActivated = true
                        (activity as? ProductEditActivity)?.setIngredients("set", null)
                    }
                }.addTo(disp)
        }
    }

    override fun allValid() = if (!frontImageUrl.isNullOrEmpty()) true
    else {
        Snackbar.make(binding.root, R.string.add_at_least_one_picture, BaseTransientBottomBar.LENGTH_SHORT).show()
        binding.scrollView.fullScroll(View.FOCUS_UP)
        false
    }

    private fun openFrontImage() {
        if (frontImageUrl == null) {
            // No image, take one
            chooseOrTakeFrontImage()
        } else {
            // Image found, download it if necessary and edit it
            isFrontImagePresent = true
            if (photoFile == null) {
                download(requireContext(), frontImageUrl!!, client)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { uri ->
                        photoFile = uri.toFile()
                        cropRotateImage(uri, getString(R.string.set_img_front))
                    }.addTo(disp)
            } else {
                cropRotateImage(photoFile!!, getString(R.string.set_img_front))
            }
        }
    }

    private fun chooseOrTakeFrontImage() {
        // add front image.
        isFrontImagePresent = true
        doChooseOrTakePhotos()
    }

    private fun chooseOrTakeOtherImage() {
        isFrontImagePresent = false
        doChooseOrTakePhotos()
    }

    override fun doOnPhotosPermissionGranted() =
        if (isFrontImagePresent) chooseOrTakeOtherImage()
        else openFrontImage()

    /**
     * adds all the fields to the query map even those which are null or empty.
     */
    private fun addAllFieldsToMap(targetMap: MutableMap<String, String?>) {
        chipifyAllUnterminatedTokens()
        if (activity !is ProductEditActivity) return

        val lc = getLCOrDefault(languageCode)
        targetMap[ApiFields.Keys.BARCODE] = barcode
        targetMap[ApiFields.Keys.LANG] = lc
        targetMap[ApiFields.Keys.LC] = appLanguageCode
        targetMap[lcProductNameKey(lc)] = binding.name.text.toString()
        targetMap[ApiFields.Keys.QUANTITY] = binding.quantity.text.toString()
        targetMap[ApiFields.Keys.BRANDS] = getNachoValues(binding.brand)
        targetMap[ApiFields.Keys.PACKAGING] = getNachoValues(binding.packaging)
        targetMap[ApiFields.Keys.CATEGORIES] = getNachoValues(binding.categories)
        targetMap[ApiFields.Keys.LABELS] = getNachoValues(binding.label)
        if (isFlavors(OBF)) {
            targetMap[ApiFields.Keys.PERIODS_AFTER_OPENING] = binding.periodOfTimeAfterOpening.text.toString()
        }
        if (frontImageUrl != null) {
            targetMap["imageUrl"] = frontImageUrl
        }
        targetMap[ApiFields.Keys.ORIGINS] = getNachoValues(binding.originOfIngredients)
        targetMap[ApiFields.Keys.MANUFACTURING_PLACES] = binding.manufacturingPlace.text.toString()
        targetMap[ApiFields.Keys.EMB_CODES] = getNachoValues(binding.embCode)
        targetMap[ApiFields.Keys.LINK] = binding.link.text.toString()
        targetMap[ApiFields.Keys.PURCHASE_PLACES] = getNachoValues(binding.countryWherePurchased)
        targetMap[ApiFields.Keys.STORES] = getNachoValues(binding.stores)
        targetMap[ApiFields.Keys.COUNTRIES] = getNachoValues(binding.countriesWhereSold)
    }

    /**
     * adds only those fields to the query map which have changed.
     */
    override fun addUpdatedFieldsToMap(targetMap: MutableMap<String, String?>) {
        chipifyAllUnterminatedTokens()

        if (activity !is ProductEditActivity) return

        barcode?.let { if (it.isNotEmpty()) targetMap[ApiFields.Keys.BARCODE] = it }
        languageCode?.let { if (it.isNotEmpty()) targetMap[ApiFields.Keys.LANG] = it }
        appLanguageCode.let { if (it.isNotEmpty()) targetMap[ApiFields.Keys.LC] = it }

        val lc = getLCOrDefault(languageCode)
        if (binding.name.isNotEmpty() && binding.name.isContentDifferent(if (product != null) product!!.getProductName(lc) else null)) {
            targetMap[lcProductNameKey(lc)] = binding.name.text.toString()
        }
        if (binding.quantity.isNotEmpty() && binding.quantity.isContentDifferent(if (product != null) product!!.quantity else null)) {
            targetMap[ApiFields.Keys.QUANTITY] = binding.quantity.text.toString()
        }
        if (binding.brand.areChipsDifferent(extractProductBrandsChipsValues(product))) {
            targetMap[ApiFields.Keys.BRANDS] = getNachoValues(binding.brand)
        }
        if (binding.packaging.areChipsDifferent(extractProductPackagingChipsValues(product))) {
            targetMap[ApiFields.Keys.PACKAGING] = getNachoValues(binding.packaging)
        }
        if (binding.categories.areChipsDifferent(extractProductCategoriesChipsValues(product))) {
            targetMap[ApiFields.Keys.CATEGORIES] = getNachoValues(binding.categories)
        }
        if (binding.label.areChipsDifferent(extractProductTagsChipsValues(product))) {
            targetMap[ApiFields.Keys.LABELS] = getNachoValues(binding.label)
        }
        if (binding.periodOfTimeAfterOpening.isNotEmpty()) {
            targetMap[ApiFields.Keys.PERIODS_AFTER_OPENING] = binding.periodOfTimeAfterOpening.text.toString()
        }
        frontImageUrl?.let { targetMap["imageUrl"] = it }
        if (binding.originOfIngredients.areChipsDifferent(extractProductOriginsChipsValues(product))) {
            targetMap[ApiFields.Keys.ORIGINS] = getNachoValues(binding.originOfIngredients)
        }
        if (binding.manufacturingPlace.isNotEmpty()
            && binding.manufacturingPlace.isContentDifferent(if (product != null) product!!.manufacturingPlaces else null)
        ) {
            targetMap[ApiFields.Keys.MANUFACTURING_PLACES] = binding.manufacturingPlace.text.toString()
        }
        if (binding.embCode.areChipsDifferent(extractProductEmbTagsChipsValues(product))) {
            targetMap[ApiFields.Keys.EMB_CODES] = getNachoValues(binding.embCode)
        }
        if (binding.link.isNotEmpty()
            && binding.link.isContentDifferent(if (product != null) product!!.manufacturerUrl else null)
        ) {
            targetMap[ApiFields.Keys.LINK] = binding.link.text.toString()
        }
        if (binding.countryWherePurchased.areChipsDifferent(extractProductPurchasePlaces(product))) {
            targetMap[ApiFields.Keys.PURCHASE_PLACES] = getNachoValues(binding.countryWherePurchased)
        }
        if (binding.stores.areChipsDifferent(extractProductStoresChipValues(product))) {
            targetMap[ApiFields.Keys.STORES] = getNachoValues(binding.stores)
        }
        if (binding.countriesWhereSold.areChipsDifferent(extractProductCountriesTagsChipValues(product))) {
            targetMap[ApiFields.Keys.COUNTRIES] = getNachoValues(binding.countriesWhereSold)
        }
    }

    /**
     * Chipifies all existing plain text in all the NachoTextViews.
     */
    private fun chipifyAllUnterminatedTokens() = listOf(
        binding.brand,
        binding.packaging,
        binding.categories,
        binding.label,
        binding.originOfIngredients,
        binding.embCode,
        binding.countryWherePurchased,
        binding.stores,
        binding.countriesWhereSold
    ).forEach { it.chipifyAllUnterminatedTokens() }

    @Contract(pure = true)
    private fun getNachoValues(nachoTextView: NachoTextView) = nachoTextView.chipValues.joinToString(",")

    private fun toggleManufacturingSectionVisibility() {
        if (binding.manufacturingPlaceTil.visibility != View.VISIBLE) {
            changeVisibilityManufacturingSectionTo(View.VISIBLE)
            binding.originOfIngredients.requestFocus()
            binding.sectionManufacturingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_up_grey_24dp, 0)
        } else {
            changeVisibilityManufacturingSectionTo(View.GONE)
            binding.sectionManufacturingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_grey_24dp, 0)
        }
    }

    private fun changeVisibilityManufacturingSectionTo(visibility: Int) {
        binding.originOfIngredientsTil.visibility = visibility
        binding.originHint.visibility = visibility
        binding.manufacturingPlaceTil.visibility = visibility
        binding.embCodeTil.visibility = visibility
        binding.linkTil.visibility = visibility
        binding.linearLayout.visibility = visibility
        binding.traceHint.visibility = visibility
        if (isFlavors(OFF))
            binding.originWarning.visibility = visibility
    }

    private fun togglePurchasingSectionVisibility() {
        if (binding.storesTil.visibility != View.VISIBLE) {
            changePurchasingSectionVisibilityTo(View.VISIBLE)
            binding.countryWherePurchased.requestFocus()
            binding.sectionPurchasingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_up_grey_24dp, 0)
        } else {
            changePurchasingSectionVisibilityTo(View.GONE)
            binding.sectionPurchasingDetails.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_keyboard_arrow_down_grey_24dp, 0)
        }
    }

    private fun changePurchasingSectionVisibilityTo(visibility: Int) {
        binding.countryWherePurchasedTil.visibility = visibility
        binding.storesTil.visibility = visibility
        binding.countriesWhereSoldTil.visibility = visibility
    }

    private fun searchProductLink() {
        var url = "https://www.google.com/search?q=$barcode"
        if (binding.brand.chipAndTokenValues.isNotEmpty()) {
            val brandNames = binding.brand.chipAndTokenValues
            url = "$url ${brandNames.joinToString(" ")}"
        }
        if (binding.name.isNotEmpty()) {
            url = "$url ${binding.name.getContent()}"
        }
        url = "$url ${getString(R.string.official_website)}"
        val customTabsIntent = CustomTabsHelper.getCustomTabsIntent(requireActivity(), null)
        CustomTabActivityHelper.openCustomTab(requireActivity(), customTabsIntent, Uri.parse(url), WebViewFallback())
    }

    private fun scanProductLink() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setRequestCode(INTENT_INTEGRATOR_REQUEST_CODE)
        integrator.setPrompt(getString(R.string.scan_QR_code))
        integrator.initiateScan()
    }

    private fun selectProductLanguage() {
        val localeValues = SupportedLanguages.codes()
        val localeLabels = arrayOfNulls<String>(localeValues.size)
        val finalLocalValues = ArrayList<String>()
        val finalLocalLabels = ArrayList<String?>()
        var selectedIndex = 0
        localeValues.withIndex().forEach { (i, localeCode) ->
            if (localeCode == languageCode) {
                selectedIndex = i
            }
            val current = LocaleUtils.parseLocale(localeCode)
            localeLabels[i] = current.getDisplayName(current).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            finalLocalLabels.add(localeCode)
            finalLocalValues.add(localeCode)
        }
        MaterialDialog.Builder(requireContext())
            .title(R.string.preference_choose_language_dialog_title)
            .items(finalLocalLabels)
            .itemsCallbackSingleChoice(selectedIndex) { _, _, which, _ ->
                binding.name.text = null

                (activity as? ProductEditActivity)?.setIngredients("set", null)

                setProductLanguage(finalLocalValues[which])
                true
            }
            .positiveText(R.string.ok_button)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Scanned QR code returned
        if (requestCode == INTENT_INTEGRATOR_REQUEST_CODE) {
            val result = IntentIntegrator.parseActivityResult(resultCode, data)
            if (result.contents != null) {
                binding.link.setText(result.contents)
                binding.link.requestFocus()
            }
        }
        // Returning from editing image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            isFrontImagePresent = true
        }
        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun showImageProgress() {
        if (!isAdded) return
        binding.imageProgress.visibility = View.VISIBLE
        binding.imageProgressText.visibility = View.VISIBLE
        binding.imgFront.visibility = View.INVISIBLE
        binding.btnEditImgFront.visibility = View.INVISIBLE
    }

    override fun hideImageProgress(errorInUploading: Boolean, message: String) {
        if (!isAdded) return
        binding.imageProgress.visibility = View.GONE
        binding.imageProgressText.visibility = View.GONE
        binding.imgFront.visibility = View.VISIBLE
        binding.btnEditImgFront.visibility = View.VISIBLE
        if (!errorInUploading) {
            Picasso.get()
                .load(photoFile!!)
                .resize(requireContext().dpsToPixel(50), requireContext().dpsToPixel(50))
                .centerInside()
                .into(binding.imgFront)
        }
    }

    fun showOtherImageProgress() {
        binding.otherImageProgress.visibility = View.VISIBLE
        binding.otherImageProgressText.visibility = View.VISIBLE
        binding.otherImageProgressText.setText(R.string.toastSending)
    }

    fun hideOtherImageProgress(errorUploading: Boolean, message: String?) {
        binding.otherImageProgress.visibility = View.GONE
        if (errorUploading) {
            binding.otherImageProgressText.visibility = View.GONE
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        } else {
            binding.otherImageProgressText.setText(R.string.image_uploaded_successfully)
        }
    }

    private fun getLCOrDefault(languageCode: String?) =
        if (!languageCode.isNullOrEmpty()) languageCode else ApiFields.Defaults.DEFAULT_LANGUAGE

    companion object {
        private const val INTENT_INTEGRATOR_REQUEST_CODE = 1
    }
}
