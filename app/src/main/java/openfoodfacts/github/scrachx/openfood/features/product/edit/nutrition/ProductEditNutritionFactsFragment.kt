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
package openfoodfacts.github.scrachx.openfood.features.product.edit.nutrition

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.annotation.StringRes
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsView
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.analytics.SentryAnalytics
import openfoodfacts.github.scrachx.openfood.databinding.FragmentAddProductNutritionFactsBinding
import openfoodfacts.github.scrachx.openfood.features.product.edit.*
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity.Companion.KEY_EDIT_OFFLINE_PRODUCT
import openfoodfacts.github.scrachx.openfood.features.shared.views.CustomValidatingEditTextView
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.*
import openfoodfacts.github.scrachx.openfood.models.MeasurementUnit.*
import openfoodfacts.github.scrachx.openfood.models.Nutriment.*
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.ApiFields.Defaults.NUTRITION_DATA_PER_100G
import openfoodfacts.github.scrachx.openfood.network.ApiFields.Defaults.NUTRITION_DATA_PER_SERVING
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.*
import java.io.File
import java.text.Collator
import java.util.*
import javax.inject.Inject

/**
 * @see R.layout.fragment_add_product_nutrition_facts
 */
@AndroidEntryPoint
class ProductEditNutritionFactsFragment : ProductEditFragment() {
    private var _binding: FragmentAddProductNutritionFactsBinding? = null
    private val binding get() = _binding!!

    val viewModel: ProductEditNutritionViewModel by viewModels()

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var client: ProductRepository

    @Inject
    lateinit var fileDownloader: FileDownloader

    @Inject
    lateinit var matomoAnalytics: MatomoAnalytics

    @Inject
    lateinit var sentryAnalytics: SentryAnalytics

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var localeManager: LocaleManager

    @Inject
    lateinit var photoReceiverHandler: PhotoReceiverHandler

    private var photoFile: File? = null
    private var productCode: String? = null
    private var mOfflineSavedProduct: OfflineSavedProduct? = null
    private var imagePath: String? = null

    //index list stores the index of other nutrients which are used.
    private val usedNutrientsIndexes = mutableSetOf<Int>()
    private var product: Product? = null

    private var lastEditText: EditText? = null
    private var starchEditText: CustomValidatingEditTextView? = null
    private var allEditViews = mutableSetOf<CustomValidatingEditTextView>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddProductNutritionFactsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddImageNutritionFacts.setOnClickListener { addNutritionFactsImage() }
        binding.btnEditImageNutritionFacts.setOnClickListener { newNutritionFactsImage() }
        binding.btnAdd.setOnClickListener { next() }
        binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId >= 0) {
                viewModel.dataFormat.postValue(checkedId)
            }
        }

        binding.btnAddANutrient.setOnClickListener { displayAddNutrientDialog() }

        binding.salt.doAfterTextChanged { updateSodiumValue() }
        binding.spinnerSaltComp.setOnItemSelectedListener { _, _, _, _ ->
            binding.spinnerSodiumComp.setSelection(binding.spinnerSaltComp.selectedItemPosition)
        }

        binding.sodium.doAfterTextChanged { updateSaltValue() }
        binding.spinnerSodiumComp.setOnItemSelectedListener { _, _, _, _ ->
            binding.spinnerSaltComp.setSelection(binding.spinnerSodiumComp.selectedItemPosition)
        }

        val bundle = arguments
        lastEditText = binding.alcohol

        if (bundle == null) {
            closeScreenWithAlert()
            return
        }

        product = bundle.getSerializable("product") as Product?
        mOfflineSavedProduct = bundle.getSerializable(KEY_EDIT_OFFLINE_PRODUCT) as OfflineSavedProduct?

        if (product != null) {
            productCode = product!!.code
            viewModel.product.value = product
        }

        val productEdited = bundle.getBoolean(ProductEditActivity.KEY_IS_EDITING)
        if (productEdited && product != null) {
            productCode = product!!.code
            binding.btnAdd.setText(R.string.save_edits)
            preFillProductValues()
        } else if (mOfflineSavedProduct != null) {
            productCode = mOfflineSavedProduct!!.barcode
            preFillValuesFromOffline()
        } else {
            binding.radioGroup.jumpDrawablesToCurrentState()
        }
        binding.alcohol.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.energyKcal.requestFocus()
        allEditViews = (view as ViewGroup).getViewsByType(CustomValidatingEditTextView::class.java).toHashSet()
        allEditViews.forEach {
            it.addValidListener()
            it.checkValue()
        }

        (activity as? ProductEditActivity)?.initialValues?.let { it += getAllFieldsMap() }

        viewModel.noNutritionFactsChecked.observe(viewLifecycleOwner) {
            binding.checkboxNoNutritionData.isChecked = it
            binding.nutritionFactsLayout.isVisible = !it
        }

        viewModel.dataFormat.observe(viewLifecycleOwner, binding.radioGroup::check)
    }

    override fun onResume() {
        super.onResume()
        matomoAnalytics.trackView(AnalyticsView.ProductEditNutritionFacts)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkAllValues() = allEditViews.forEach { it.checkValue() }

    override fun allValid() = allEditViews.none { it.isError() }

    private fun requireAddProductActivity() = requireActivity() as ProductEditActivity

    /**
     * Pre fill the fields of the product which are already present on the server.
     */
    private fun preFillProductValues() {
        loadNutritionImage()

        // Set no nutrition data checkbox
        if (product!!.noNutritionData.equals("on", true)) {
            binding.checkboxNoNutritionData.isChecked = true
            binding.nutritionFactsLayout.visibility = View.GONE
        }

        // Set nutrition data per
        product!!.nutritionDataPer
            ?.takeUnless { it.isEmpty() }
            ?.let(::updateSelectedDataPer)


        // Set serving size
        product!!.servingSize
            ?.takeUnless { it.isEmpty() }
            // Splits the serving size into value and unit. Example: "15g" into "15" and "g"
            ?.let(::updateServingSize)

        if (view == null) return

        val nutriments = product!!.nutriments
        binding.energyKj.setText(nutriments.getEnergyKjValue(isDataPerServing)?.let(::getRoundNumber))
        binding.energyKcal.setText(nutriments.getEnergyKcalValue(isDataPerServing)?.let(::getRoundNumber))

        // Fill default nutriments fields
        for (editView in (view as ViewGroup).getViewsByType(CustomValidatingEditTextView::class.java)) {
            var nutrimentShortName = editView.entryName

            // Workaround for saturated-fat
            if (nutrimentShortName == "saturated_fat") nutrimentShortName = "saturated-fat"

            // Skip serving size and energy view, we already filled them
            if (editView === binding.servingSize || editView === binding.energyKcal || editView === binding.energyKj) continue

            // Get the value
            val nutriment = Nutriment.findbyKey(nutrimentShortName) ?: error("Cannot find nutrient $nutrimentShortName")
            val value = (if (isDataPer100g) nutriments[nutriment]?.per100gInUnit else nutriments[nutriment]?.perServingInUnit) ?: continue

            editView.setText(getRoundNumber(value))
            editView.unitSpinner?.setSelection(getUnitIndexUnitFromShortName(nutriments, nutriment) ?: 0)
            editView.modSpinner?.setSelection(getModifierIndexFromShortName(nutriments, nutriment))
        }

        // Set the values of all the other nutrients if defined and create new row in the tableLayout.
        for ((i, nutrient) in PARAMS_OTHER_NUTRIENTS.withIndex()) {
            val nutrimentShortName = getShortName(nutrient)

            val nutriment = Nutriment.findbyKey(nutrimentShortName) ?: error("Cannot find nutrient $nutrimentShortName")

            val measurement = if (isDataPer100g) {
                nutriments[nutriment]?.per100gInUnit
            } else {
                nutriments[nutriment]?.perServingInUnit
            } ?: continue

            val unitIndex = getUnitIndexUnitFromShortName(nutriments, nutriment)
            val modIndex = getModifierIndexFromShortName(nutriments, nutriment)

            usedNutrientsIndexes += i

            val nutrientNames = resources.getStringArray(R.array.nutrients_array)
            addNutrientRow(i, nutrientNames[i], true, measurement.value, unitIndex ?: 0, modIndex)
        }
    }

    /**
     * Load the nutrition image uploaded form AddProductActivity
     */
    fun loadNutritionImage() {
        photoFile = null
        val newImageNutritionUrl = product?.getImageNutritionUrl(requireAddProductActivity().getProductLanguageForEdition())
        if (newImageNutritionUrl.isNullOrEmpty()) return

        binding.imageProgress.visibility = View.VISIBLE
        imagePath = newImageNutritionUrl
        loadNutritionImage(imagePath!!)
    }

    private fun getUnitIndexUnitFromShortName(nutriments: ProductNutriments, nutriment: Nutriment): Int? =
        nutriments[nutriment]?.unit?.let { getUnitIndex(nutriment, it) }

    private fun getUnitIndex(nutrient: Nutriment, unit: MeasurementUnit): Int {
        require(nutrient != Nutriment.ENERGY_KCAL && nutrient != Nutriment.ENERGY_KJ) { "Nutrient cannot be energy" }

        return getAllUnitsIndex(unit)
    }

    private fun getModifierIndexFromShortName(nutriments: ProductNutriments, nutriment: Nutriment): Int =
        getModifierIndex(nutriments[nutriment]?.modifier)

    private fun updateServingSize(servingSize: String) {
        val (value, unit) = try {
            parseServing(servingSize)
        } catch (ex: IllegalArgumentException) {
            // Serving size not not matching size regex. Incorrect format
            sentryAnalytics.record(ex)

            // Disable fields and return
            binding.servingSize.isEnabled = false
            binding.servingSize.unitSpinner?.isEnabled = false
            return
        }

        binding.servingSize.setText(value)
        if (unit != null) {
            binding.servingSize.unitSpinner?.setSelection(getServingUnitIndex(unit))
        }
    }

    /**
     * Pre fill the fields if the product is already present in SavedProductOffline db.
     */
    private fun preFillValuesFromOffline() {
        val productDetails = mOfflineSavedProduct!!.productDetails

        if (productDetails["image_nutrition_facts"] != null) {
            imagePath = productDetails["image_nutrition_facts"]
            val path = "$LOCALE_FILE_SCHEME$imagePath"
            binding.imageProgress.visibility = View.VISIBLE
            loadNutritionImage(path)
        }

        if (productDetails[ApiFields.Keys.NO_NUTRITION_DATA] != null) {
            binding.checkboxNoNutritionData.isChecked = true
            binding.nutritionFactsLayout.visibility = View.GONE
        }

        // can be "100g" or "serving"
        productDetails[ApiFields.Keys.NUTRITION_DATA_PER]?.let(::updateSelectedDataPer)

        // Splits the serving size into value and unit. Example: "15g" into "15" and "g"
        productDetails[ApiFields.Keys.SERVING_SIZE]?.let(::updateServingSize)

        for (view in (binding.root as ViewGroup).getViewsByType(CustomValidatingEditTextView::class.java)) {
            val nutrientShortName = view.entryName
            if (nutrientShortName == binding.servingSize.entryName) continue

            val nutrientCompleteName = getCompleteEntryName(view)

            productDetails[nutrientCompleteName]?.let { value ->
                view.setText(value)

                val unit = productDetails[nutrientCompleteName + ApiFields.Suffix.UNIT] ?: return@let
                view.unitSpinner?.setSelection(getUnitIndex(Nutriment.findbyKey(nutrientShortName)!!, MeasurementUnit.findBySymbol(unit)!!))
            }
        }
        // Set the values of all the other nutrients if defined and create new row in the tableLayout.
        for ((i, completeNutrientName) in PARAMS_OTHER_NUTRIENTS.withIndex()) {
            if (productDetails[completeNutrientName] == null) continue

            var unitIndex = 0
            var modIndex = 0
            val value = productDetails[completeNutrientName]?.toFloatOrNull() ?: continue

            productDetails[completeNutrientName + ApiFields.Suffix.UNIT]?.let {
                unitIndex = getAllUnitsIndex(MeasurementUnit.findBySymbol(it)!!)
            }
            productDetails[completeNutrientName + ApiFields.Suffix.MODIFIER]?.let {
                modIndex = getAllUnitsIndex(MeasurementUnit.findBySymbol(it)!!)
            }
            usedNutrientsIndexes += i

            val nutrients = resources.getStringArray(R.array.nutrients_array)
            addNutrientRow(i, nutrients[i], true, value, unitIndex, modIndex)
        }
    }

    /**
     * @param value [ApiFields.Defaults.NUTRITION_DATA_PER_100G] or [ApiFields.Defaults.NUTRITION_DATA_PER_SERVING]
     * @throws IllegalArgumentException if [value] is neither [ApiFields.Defaults.NUTRITION_DATA_PER_100G] nor [ApiFields.Defaults.NUTRITION_DATA_PER_SERVING]
     */
    private fun updateSelectedDataPer(value: String) {
        binding.radioGroup.clearCheck()

        when (value) {
            NUTRITION_DATA_PER_100G -> binding.radioGroup.check(R.id.for100g_100ml)
            NUTRITION_DATA_PER_SERVING -> binding.radioGroup.check(R.id.per_serving)
            else -> throw IllegalArgumentException("Value is neither $NUTRITION_DATA_PER_100G nor $NUTRITION_DATA_PER_SERVING")
        }

        binding.radioGroup.jumpDrawablesToCurrentState()
    }

    /**
     * Loads nutrition image into the ImageView
     *
     * @param path path of the image
     */
    private fun loadNutritionImage(path: String) {
        picasso.load(path)
            .resize(50.toPx(requireContext()), 50.toPx(requireContext()))
            .centerInside()
            .into(binding.btnAddImageNutritionFacts, object : Callback {
                override fun onSuccess() {
                    if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return
                    afterNutritionImgLoaded()
                }

                override fun onError(ex: Exception) {
                    if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) return
                    afterNutritionImgLoaded()
                }
            })
    }

    private fun afterNutritionImgLoaded() {
        binding.imageProgress.visibility = View.GONE
        binding.btnEditImageNutritionFacts.visibility = View.VISIBLE
    }

    private fun getUnitIndex(index: Int) = NUTRIENTS_UNITS[index]

    /**
     * @param unit The unit corresponding to which the index is to be returned.
     * @return returns the index to be set to the spinner.
     */
    private fun getAllUnitsIndex(unit: MeasurementUnit) =
        NUTRIENTS_UNITS.indexOfFirst { it == unit }.coerceAtLeast(0)

    private fun getModifierIndex(modifier: Modifier?) =
        MODIFIERS.indexOf(modifier).coerceAtLeast(0)

    private fun getServingUnitIndex(unit: MeasurementUnit) =
        SERVING_UNITS.indexOfFirst { it == unit }.coerceAtLeast(0)

    private fun getServingUnitIndex(symbol: String) =
        SERVING_UNITS.indexOfFirst { it.sym == symbol }.coerceAtLeast(0)

    private fun addNutritionFactsImage() {
        val path = imagePath
        if (path == null) {
            newNutritionFactsImage()
            return
        }
        if (photoFile != null) {
            cropRotateImage(photoFile!!, getString(R.string.nutrition_facts_picture))
        } else {
            lifecycleScope.launchWhenResumed {
                val uri = fileDownloader.download(path)
                if (uri != null) {
                    photoFile = uri.toFile()
                    cropRotateImage(uri, getString(R.string.nutrition_facts_picture))
                }
            }
        }
    }

    private fun newNutritionFactsImage() = doChooseOrTakePhotos()

    override fun doOnPhotosPermissionGranted() = newNutritionFactsImage()

    private fun updateNextBtnState() {
        val allValuesValid = allValid()
        binding.globalValidationMsg.visibility = if (allValuesValid) View.GONE else View.VISIBLE
        binding.btnAdd.isEnabled = allValuesValid
    }

    private fun CustomValidatingEditTextView.checkValue(value: Float) = sequenceOf(
        checkPh(value),
        checkAlcohol(value),
        checkEnergy(value),
        checkCarbohydrate(value),
        checkServingSize()
    ).firstOrNull { it != ValueState.NOT_TESTED } ?: this.checkAsGram(value)

    private fun CustomValidatingEditTextView.requireToValidate(condition: Boolean, @StringRes errorMsg: Int): ValueState {
        return if (condition) ValueState.VALID
        else {
            showError(context.getString(errorMsg))
            ValueState.NOT_VALID
        }
    }

    private fun CustomValidatingEditTextView.checkAsGram(value: Float): ValueState {
        val valid = convertToGrams(value, unitSpinner!!.selectedItemPosition)!!.value <= referenceValueInGram

        return requireToValidate(valid, R.string.max_nutrient_val_msg)
    }

    private fun CustomValidatingEditTextView.checkValue() {
        val wasValid = isError()
        // If no value, we suppose it's valid
        if (isBlank()) {
            cancelError()
            // If per serving is set must be not blank
            checkServingSize()
        } else {
            val value = this.getFloatValue()
            if (value == null) {
                showError(getString(R.string.error_nutrient_entry))
            } else {
                val valueState = this.checkValue(value)
                if (valueState == ValueState.VALID) {
                    cancelError()
                }
            }
        }
        if (wasValid != isValid()) {
            updateNextBtnState()
        }
    }

    private fun CustomValidatingEditTextView.checkValueAndRelated() {
        checkValue()
        if (isCarbohydrateRelated(this)) {
            binding.carbohydrates.checkValue()
        }
        if (entryName == binding.servingSize.entryName) {
            checkAllValues()
        }
    }

    private fun CustomValidatingEditTextView.addValidListener() {
        val textWatcher = ValidTextWatcher(this)
        addTextChangedListener(textWatcher)
        unitSpinner?.onItemSelectedListener = textWatcher
    }

    private fun updateSodiumValue() {
        if (requireActivity().currentFocus !== binding.salt) return

        binding.salt.getFloatValue()?.let {
            val sodiumValue = it.saltToSodium()
            binding.sodium.setText(getRoundNumber(sodiumValue))
        }
    }

    private fun updateSaltValue() {
        if (requireActivity().currentFocus !== binding.sodium) return

        binding.sodium.getFloatValue()?.let {
            val saltValue = it.sodiumToSalt()
            binding.salt.setText(getRoundNumber(saltValue))
        }
    }

    /**
     * adds only those fields to the query map which are not empty.
     */
    override fun getUpdatedFieldsMap(): Map<String, String?> {
        val targetMap = mutableMapOf<String, String?>()

        // Add no nutrition data entry to map
        if (binding.checkboxNoNutritionData.isChecked) {
            targetMap[ApiFields.Keys.NO_NUTRITION_DATA] = "on"
        } else {
            targetMap += getNutrientsModeMap()
        }

        // Add serving size entry to map if it has been changed
        if (binding.servingSize.isNotEmpty()) {
            @Suppress("USELESS_ELVIS") val servingSizeValue = binding.servingSize.getContent() +
            binding.servingSize.unitSpinner!!.selectedItem?.toString() ?: ""
            if (product == null || servingSizeValue != product!!.servingSize) {
                targetMap[ApiFields.Keys.SERVING_SIZE] = servingSizeValue
            }
        }

        // For every nutrition field add it to map if updated
        for (view in allEditViews) {
            if (view.entryName == binding.servingSize.entryName || !view.isNotEmpty()) continue
            targetMap += getNutrientMapIfUpdated(view)
        }

        return targetMap
    }

    /**
     * adds all the fields to the query map even those which are null or empty.
     */
    private fun getAllFieldsMap(): Map<String, String?> {
        if (activity !is ProductEditActivity) return emptyMap()

        if (binding.checkboxNoNutritionData.isChecked) {
            return mapOf(ApiFields.Keys.NO_NUTRITION_DATA to "on")
        }

        val targetMap = mutableMapOf<String, String?>()
        val servingSizeValue =
            if (binding.servingSize.text == null || binding.servingSize.text.toString().isEmpty()) ""
            else {
                @Suppress("USELESS_ELVIS")
                binding.servingSize.text.toString() + binding.servingSize.unitSpinner?.selectedItem ?: ""
            }
        targetMap[ApiFields.Keys.SERVING_SIZE] = servingSizeValue

        for (view in allEditViews) {
            if (binding.servingSize.entryName == view.entryName) continue
            addNutrientToMap(view, targetMap)
        }

        return targetMap
    }

    /**
     * Add nutrients to the map by from the text entered into EditText, only if the value has been edited
     *
     * @param editTextView EditText with spinner for entering the nutrients
     * @return map to enter the nutrient value received from edit texts
     */
    private fun getNutrientMapIfUpdated(editTextView: CustomValidatingEditTextView): Map<String, String?> {
        val targetMap = mutableMapOf<String, String?>()
        val productNutriments = product?.nutriments ?: ProductNutriments()

        val shortName = editTextView.entryName.replace("_", "-")
        val nutriment = Nutriment.requireByKey(shortName)
        val oldProductNutriment = productNutriments[nutriment]

        var oldValue: Float? = null
        var oldUnit: MeasurementUnit? = null
        var oldMod: Modifier? = null

        if (oldProductNutriment != null) {
            oldUnit = oldProductNutriment.unit
            oldMod = oldProductNutriment.modifier
            oldValue = if (isDataPer100g)
                oldProductNutriment.per100gInUnit.value
            else
                oldProductNutriment.perServingInUnit!!.value
        }
        val valueChanged = editTextView.isValueDifferent(oldValue)

        // Check unit and modifier for changes
        var newUnit: MeasurementUnit? = null
        var newMod: Modifier? = null

        if (editTextView.hasUnit()) {
            editTextView.unitSpinner?.let {
                newUnit = getUnitIndex(it.selectedItemPosition)
            }
        }

        editTextView.modSpinner?.let { newMod = Modifier.findBySymbol(it.selectedItem.toString()) }

        val unitChanged = oldUnit == null || oldUnit != newUnit
        val modChanged = oldMod == null || oldMod != newMod

        if (valueChanged || unitChanged || modChanged) {
            addNutrientToMap(editTextView, targetMap)
        }

        return targetMap
    }

    /**
     * Add nutrients to the map by from the text entered into EditText
     *
     * @param editTextView EditText with spinner for entering the nutrients
     * @param targetMap map to enter the nutrient value received from edit texts
     */
    private fun addNutrientToMap(
        editTextView: CustomValidatingEditTextView,
        targetMap: MutableMap<String, String?>
    ) {
        // For impl reference, see https://wiki.openfoodfacts.org/Nutrients_handling_in_Open_Food_Facts#Data_display
        val fieldName = getCompleteEntryName(editTextView)

        // Add unit field {nutrient-id}_unit to map
        if (editTextView.hasUnit() && editTextView.unitSpinner != null) {
            val selectedUnit = getUnitIndex(editTextView.unitSpinner!!.selectedItemPosition)
            targetMap[fieldName + ApiFields.Suffix.UNIT] = Html.escapeHtml(selectedUnit.sym)
        }

        // Take modifier from attached spinner, add to value if not the default one
        var mod = ""
        if (editTextView.modSpinner != null) {
            val selectedMod = Modifier.findBySymbol(editTextView.modSpinner!!.selectedItem.toString())
            if (selectedMod != null && DEFAULT_MODIFIER != selectedMod) {
                mod = selectedMod.sym
            }
        }
        // The suffix can either be _serving or _100g depending on user input
        val value = editTextView.text!!.toString()
        targetMap[fieldName] = mod + value
    }

    private fun getNutrientsModeMap(): Map<String, String> {
        return when {
            isDataPer100g -> mapOf(ApiFields.Keys.NUTRITION_DATA_PER to NUTRITION_DATA_PER_100G)
            isDataPerServing -> mapOf(ApiFields.Keys.NUTRITION_DATA_PER to NUTRITION_DATA_PER_SERVING)
            else -> mapOf()
        }
    }

    private val isDataPerServing: Boolean
        get() = viewModel.dataFormat.value == R.id.per_serving

    private val isDataPer100g: Boolean
        get() = viewModel.dataFormat.value == R.id.for100g_100ml

    private val referenceValueInGram: Float
        get() {
            var reference = 100f
            if (binding.radioGroup.checkedRadioButtonId != R.id.for100g_100ml) {
                val value = binding.servingSize.getFloatValueOr(100f)
                reference = measure(value, SERVING_UNITS[binding.servingSize.unitSpinner!!.selectedItemPosition])
                    .grams
                    .value
            }
            return reference
        }

    private fun displayAddNutrientDialog() {
        val nutrientsDefUnits = resources.getStringArray(R.array.nutrients_array).zip(PARAMS_OTHER_NUTRIENTS_DEFAULT_UNITS.keys)
        val filteredNutrients = resources.getStringArray(R.array.nutrients_array)
            .filterIndexed { index, _ -> index !in usedNutrientsIndexes }
            .sortedWith(Collator.getInstance(Locale.getDefault()))
            .toTypedArray()

        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.choose_nutrient)
            .setItems(filteredNutrients) { _, index ->
                val realIndex = nutrientsDefUnits.indexOfFirst { it.first == filteredNutrients[index] }
                val text = nutrientsDefUnits[realIndex].first
                usedNutrientsIndexes += realIndex
                val textView = addNutrientRow(
                    realIndex, text, true, unitSelectedIndex = getAllUnitsIndex(
                        MeasurementUnit.findBySymbol(
                            PARAMS_OTHER_NUTRIENTS_DEFAULT_UNITS[nutrientsDefUnits[realIndex].second]!!
                        )!!
                    )
                )

                allEditViews.add(textView)
                textView.addValidListener()
            }
            .show()
    }

    /**
     * Adds a new row in the tableLayout.
     *
     * @param index The index of the additional nutrient to add in the "PARAM_OTHER_NUTRIENTS" array.
     * @param hint The hint text to be displayed in the EditText.
     * @param preFillValues true if the created row needs to be filled by a predefined value.
     * @param value This value will be set to the EditText. Required if 'preFillValues' is true.
     * @param unitSelectedIndex This spinner will be set to this position. Required if 'preFillValues' is true.
     */
    private fun addNutrientRow(
        index: Int,
        hint: String,
        preFillValues: Boolean = false,
        value: Float? = null,
        unitSelectedIndex: Int = 0,
        modSelectedIndex: Int = 0
    ): CustomValidatingEditTextView {
        val nutrientCompleteName = PARAMS_OTHER_NUTRIENTS[index]

        val rowView = layoutInflater.inflate(R.layout.nutrition_facts_table_row, binding.tableLayout, false) as TableRow

        // Set hint
        rowView.findViewById<TextInputLayout>(R.id.value_til).hint = hint

        val nutrientShortName = getShortName(nutrientCompleteName)
        val nutriment = Nutriment.requireByKey(nutrientShortName)

        val editText = rowView.findViewById<CustomValidatingEditTextView>(R.id.value)
        editText.entryName = nutrientShortName

        lastEditText!!.nextFocusDownId = editText.id
        lastEditText!!.imeOptions = EditorInfo.IME_ACTION_NEXT
        lastEditText = editText

        editText.imeOptions = EditorInfo.IME_ACTION_DONE
        editText.requestFocus()

        if (preFillValues && value != null) editText.setText(getRoundNumber(value))

        // Setup unit spinner
        val unitSpinner = rowView.findViewById<Spinner>(R.id.spinner_unit)
        val modSpinner = rowView.findViewById<Spinner>(R.id.spinner_mod)

        when (nutriment) {
            PH -> {
                unitSpinner.visibility = View.INVISIBLE
            }
            STARCH -> {
                val arrayAdapter = ArrayAdapter(
                    requireActivity(),
                    android.R.layout.simple_spinner_item,
                    requireActivity().resources.getStringArray(R.array.weights_array)
                ).apply {
                    setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                }
                unitSpinner.adapter = arrayAdapter
                starchEditText = editText
            }
            VITAMIN_A, VITAMIN_D, VITAMIN_E -> {
                val adapter = ArrayAdapter(
                    requireActivity(),
                    android.R.layout.simple_spinner_item,
                    requireActivity().resources.getStringArray(R.array.weight_all_units)
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                unitSpinner.adapter = adapter
            }
        }
        try {
            if (preFillValues) {
                unitSpinner.setSelection(unitSelectedIndex)
                modSpinner.setSelection(modSelectedIndex)
            }
        } catch (err: Exception) {
            sentryAnalytics.record(IllegalStateException("Can't find weight units for nutriment: $nutrientShortName", err))
            closeScreenWithAlert()
        }

        binding.tableLayout.addView(rowView)
        return editText
    }

    private fun closeScreenWithAlert() {
        Toast.makeText(requireContext(), R.string.error_adding_nutrition_facts, Toast.LENGTH_SHORT).show()
        requireActivity().finish()
    }

    private fun isCarbohydrateRelated(editText: CustomValidatingEditTextView): Boolean {
        val entryName = editText.entryName
        return entryName == binding.sugars.entryName || (starchEditText != null && entryName == starchEditText!!.entryName)
    }

    /**
     * Validate the value of carbohydrate using carbs value and sugar value
     * @param value quality value with known prefix
     */
    private fun CustomValidatingEditTextView.checkCarbohydrate(value: Float): ValueState {
        if (binding.carbohydrates.entryName != entryName) return ValueState.NOT_TESTED

        val res = checkAsGram(value)

        if (res == ValueState.NOT_VALID) return res

        val carbsValue = binding.carbohydrates.getFloatValueOr(0f)
        val sugarValue = binding.sugars.getFloatValueOr(0f)

        // Check that value of (sugar + starch) is not greater than value of carbohydrates
        // Convert all the values to grams
        val carbsInG = convertToGrams(carbsValue, binding.carbohydrates.unitSpinner!!.selectedItemPosition)!!
        val sugarInG = convertToGrams(sugarValue, binding.sugars.unitSpinner!!.selectedItemPosition)!!
        val newStarchInG = convertToGrams(getStarchValue(), getStarchUnitSelectedIndex())!!

        return requireToValidate(sugarInG.value + newStarchInG.value <= carbsInG.value, R.string.error_in_carbohydrate_value)
    }

    /**
     * Validate serving size value entered by user
     */
    private fun CustomValidatingEditTextView.checkServingSize(): ValueState {
        if (entryName != binding.servingSize.entryName) return ValueState.NOT_TESTED
        if (isDataPer100g) return ValueState.VALID

        val value = binding.servingSize.getFloatValueOr(0f)
        return requireToValidate(value > 0, R.string.error_nutrient_serving_data)
    }

    /**
     * Validate oh value according to [Nutriment.PH]
     * @param value quality value with known prefix
     */
    private fun CustomValidatingEditTextView.checkPh(value: Float): ValueState {
        if (entryName != PH.key) return ValueState.NOT_TESTED

        val maxPhValue = 14f
        // Coerce the value
        if (value > maxPhValue || value == maxPhValue && this.isModifierEqualsToGreaterThan()) {
            setText(maxPhValue.toString())
            modSpinner?.setSelection(0)
        }

        return ValueState.VALID
    }

    /**
     * Validate energy value entered by user
     */
    private fun CustomValidatingEditTextView.checkEnergy(value: Float): ValueState {
        if (entryName != binding.energyKcal.entryName && entryName != binding.energyKj.entryName) return ValueState.NOT_TESTED

        var energy = value

        if (binding.radioGroup.checkedRadioButtonId != R.id.for100g_100ml) {
            energy *= 100.0f / referenceValueInGram
        }

        val isValid = when (entryName) {
            binding.energyKcal.entryName -> energy <= 2000f
            binding.energyKj.entryName -> energy <= 8368000f
            else -> true
        }

        return requireToValidate(isValid, R.string.max_energy_val_msg)
    }

    /**
     * validate alcohol content entered by user
     */
    private fun CustomValidatingEditTextView.checkAlcohol(value: Float): ValueState {
        if (entryName != binding.alcohol.entryName) return ValueState.NOT_TESTED

        return if (value <= 100) ValueState.VALID
        else {
            showError("This value is over 100") // TODO: i18n
            ValueState.NOT_VALID
        }
    }

    override fun showImageProgress() {
        if (!isAdded) return
        binding.imageProgress.visibility = View.VISIBLE
        binding.imageProgressText.visibility = View.VISIBLE
        binding.btnAddImageNutritionFacts.visibility = View.INVISIBLE
        binding.btnEditImageNutritionFacts.visibility = View.INVISIBLE
    }

    private fun getStarchValue() = starchEditText?.getFloatValue() ?: 0F
    private fun getStarchUnitSelectedIndex() = starchEditText?.unitSpinner?.selectedItemPosition ?: 0

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data) {
            val resultUri = it.toURI()
            imagePath = resultUri.path
            photoFile = it
            val image = ProductImage(productCode!!, ProductImageField.NUTRITION, it, localeManager.getLanguage()).apply {
                filePath = resultUri.path
            }
            (activity as? ProductEditActivity)?.savePhoto(image, 2)
            hideImageProgress(false, "")
        }

    }

    override fun hideImageProgress(errorInUploading: Boolean, message: String) {
        if (!isAdded) return
        binding.imageProgress.visibility = View.GONE
        binding.imageProgressText.visibility = View.GONE
        binding.btnAddImageNutritionFacts.visibility = View.VISIBLE
        binding.btnEditImageNutritionFacts.visibility = View.VISIBLE
        if (!errorInUploading) {
            picasso.load(photoFile!!)
                .resize(requireContext().dpsToPixel(50), requireContext().dpsToPixel(50))
                .centerInside()
                .into(binding.btnAddImageNutritionFacts)
        }
    }

    internal inner class ValidTextWatcher(private val editTextView: CustomValidatingEditTextView) : TextWatcher, OnItemSelectedListener {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit // Nothing to do

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit // Nothing to do

        override fun afterTextChanged(s: Editable) = editTextView.checkValueAndRelated()
        override fun onNothingSelected(parent: AdapterView<*>?) = editTextView.checkValueAndRelated()

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            editTextView.checkValueAndRelated()
            if (binding.salt.entryName == editTextView.entryName) {
                binding.sodium.unitSpinner!!.setSelection(binding.salt.unitSpinner!!.selectedItemPosition)
            }
            if (binding.sodium.entryName == editTextView.entryName) {
                binding.salt.unitSpinner!!.setSelection(binding.sodium.unitSpinner!!.selectedItemPosition)
            }
        }
    }

    companion object {
        private val NUTRIENTS_UNITS = listOf(UNIT_GRAM, UNIT_MILLIGRAM, UNIT_MICROGRAM, UNIT_DV, UNIT_IU)
        private val SERVING_UNITS = listOf(UNIT_GRAM, UNIT_MILLIGRAM, UNIT_MICROGRAM, UNIT_LITER, UNIT_MILLILITRE)

        /**
         * Converts a given quantity's unit to grams.
         *
         * @param value The value to be converted
         * @param index 1 represents milligrams, 2 represents micrograms
         * @return return the converted value
         */
        private fun convertToGrams(value: Float, index: Int): Measurement? {
            return when (val unit = NUTRIENTS_UNITS[index]) {
                UNIT_DV, UNIT_IU -> null // Can't be converted to grams.
                else -> measure(value, unit).grams
            }
        }

        private fun Spinner.setOnItemSelectedListener(
            onItemSelected: (
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) -> Unit
        ) {
            this.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) =
                    onItemSelected(parent, view, position, id)

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit // This is not possible
            }
        }

    }
}

