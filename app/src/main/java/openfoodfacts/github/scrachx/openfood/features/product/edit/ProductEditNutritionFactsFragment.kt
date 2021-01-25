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
package openfoodfacts.github.scrachx.openfood.features.product.edit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.InputType
import android.text.TextWatcher
import android.text.method.NumberKeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.widget.doAfterTextChanged
import com.afollestad.materialdialogs.MaterialDialog
import com.squareup.picasso.Callback
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.FragmentAddProductNutritionFactsBinding
import openfoodfacts.github.scrachx.openfood.features.shared.views.CustomValidatingEditTextView
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.*
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.ApiFields.Defaults.NUTRITION_DATA_PER_100G
import openfoodfacts.github.scrachx.openfood.network.ApiFields.Defaults.NUTRITION_DATA_PER_SERVING
import openfoodfacts.github.scrachx.openfood.utils.*
import openfoodfacts.github.scrachx.openfood.utils.FileDownloader.download
import openfoodfacts.github.scrachx.openfood.utils.Utils.getRoundNumber
import openfoodfacts.github.scrachx.openfood.utils.Utils.picassoBuilder
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.text.Collator
import java.util.*

/**
 * @see R.layout.fragment_add_product_nutrition_facts
 */
class ProductEditNutritionFactsFragment : ProductEditFragment() {
    private val keyListener = object : NumberKeyListener() {
        override fun getInputType() = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

        override fun getAcceptedChars() = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ',', '.')
    }
    private var _binding: FragmentAddProductNutritionFactsBinding? = null
    private val binding get() = _binding!!
    private var photoReceiverHandler: PhotoReceiverHandler? = null
    private var activity: Activity? = null
    private var photoFile: File? = null
    private var productCode: String? = null
    private var mOfflineSavedProduct: OfflineSavedProduct? = null
    private var imagePath: String? = null

    //index list stores the index of other nutrients which are used.
    private val index = mutableSetOf<Int>()
    private var product: Product? = null

    private var lastEditText: EditText? = null
    private var starchEditText: CustomValidatingEditTextView? = null
    private var allEditViews = mutableSetOf<CustomValidatingEditTextView>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddProductNutritionFactsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAddImageNutritionFacts.setOnClickListener { addNutritionFactsImage() }
        binding.btnEditImageNutritionFacts.setOnClickListener { newNutritionFactsImage() }
        binding.btnAdd.setOnClickListener { next() }
        binding.for100g100ml.setOnClickListener { checkAllValues() }
        binding.btnAddANutrient.setOnClickListener { displayAddNutrientDialog() }

        binding.salt.doAfterTextChanged { updateSodiumValue() }
        binding.spinnerSaltComp.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) = updateSodiumMod()
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit // This is not possible
        }
        binding.sodium.doAfterTextChanged { updateSaltValue() }

        binding.spinnerSodiumComp.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) = updateSaltMod()
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit // This is not possible
        }
        binding.checkboxNoNutritionData.setOnCheckedChangeListener { _, isChecked -> toggleNoNutritionData(isChecked) }

        photoReceiverHandler = PhotoReceiverHandler { newPhotoFile ->
            val resultUri = newPhotoFile.toURI()
            imagePath = resultUri.path
            photoFile = newPhotoFile
            val image = ProductImage(productCode!!, ProductImageField.NUTRITION, newPhotoFile)
            image.filePath = resultUri.path
            (activity as? ProductEditActivity)?.addToPhotoMap(image, 2)
            hideImageProgress(false, "")
        }
        binding.btnAddANutrient.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_box_black_18dp, 0, 0, 0)
        val bundle = arguments
        lastEditText = binding.alcohol
        if (bundle != null) {
            product = bundle.getSerializable("product") as Product?
            mOfflineSavedProduct = bundle.getSerializable("edit_offline_product") as OfflineSavedProduct?
            val productEdited = bundle.getBoolean(ProductEditActivity.KEY_IS_EDITING)
            if (product != null) {
                productCode = product!!.code
            }
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
        } else {
            Toast.makeText(activity, R.string.error_adding_nutrition_facts, Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }
        binding.alcohol.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.energyKcal.requestFocus()
        allEditViews = (view as ViewGroup).getViewsByType(CustomValidatingEditTextView::class.java).toHashSet()
        allEditViews.forEach {
            it.addValidListener()
            it.checkValue()
        }
        if (getActivity() is ProductEditActivity && (getActivity() as ProductEditActivity?)!!.initialValues != null) {
            addAllFieldsToMap((getActivity() as ProductEditActivity?)!!.initialValues!!)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateSodiumMod() = binding.spinnerSodiumComp.setSelection(binding.spinnerSaltComp.selectedItemPosition)
    private fun updateSaltMod() = binding.spinnerSaltComp.setSelection(binding.spinnerSodiumComp.selectedItemPosition)

    private fun checkAllValues() = allEditViews.forEach { it.checkValue() }

    override fun allValid() = allEditViews.none { it.isError() }

    private fun requireAddProductActivity() = requireActivity() as ProductEditActivity

    /**
     * Pre fill the fields of the product which are already present on the server.
     */
    private fun preFillProductValues() {
        loadNutritionImage()

        // Set no nutrition data checkbox
        if (product!!.noNutritionData.equals("on", ignoreCase = true)) {
            binding.checkboxNoNutritionData.isChecked = true
            binding.nutritionFactsLayout.visibility = View.GONE
        }

        // Set nutrition data per
        val nutritionDataPer = product!!.nutritionDataPer
        if (!nutritionDataPer.isNullOrEmpty()) {
            updateSelectedDataPer(nutritionDataPer)
        }

        // Set serving size
        val servingSize = product!!.servingSize
        if (!servingSize.isNullOrEmpty()) {
            // Splits the serving size into value and unit. Example: "15g" into "15" and "g"
            updateServingSizeFrom(servingSize)
        }

        if (view == null) return

        val nutriments = product!!.nutriments
        binding.energyKj.setText(nutriments.getEnergyKjValue(isDataPerServing))
        binding.energyKcal.setText(nutriments.getEnergyKcalValue(isDataPerServing))

        // Fill default nutriments fields
        (view as ViewGroup).getViewsByType(CustomValidatingEditTextView::class.java).forEach { view ->
            var nutrientShortName = view.entryName

            // Workaround for saturated-fat
            if (nutrientShortName == "saturated_fat") nutrientShortName = "saturated-fat"

            // Skip serving size and energy view, we already filled them
            if (view === binding.servingSize || view === binding.energyKcal || view === binding.energyKj) return@forEach

            // Get the value
            val value = if (isDataPer100g) nutriments[nutrientShortName]?.for100g else nutriments[nutrientShortName]?.forServing
            if (value.isNullOrEmpty()) return@forEach

            view.setText(value)
            view.unitSpinner?.setSelection(getSelectedUnitFromShortName(nutriments, nutrientShortName))
            view.modSpinner?.setSelection(getSelectedModifierFromShortName(nutriments, nutrientShortName))
        }

        // Set the values of all the other nutrients if defined and create new row in the tableLayout.
        PARAMS_OTHER_NUTRIENTS.withIndex().forEach { (i, nutrient) ->
            val nutrientShortName = getShortName(nutrient)
            val value = if (isDataPer100g) nutriments[nutrientShortName]?.for100g else nutriments[nutrientShortName]?.forServing
            if (value.isNullOrEmpty()) return@forEach
            val unitIndex = getSelectedUnitFromShortName(nutriments, nutrientShortName)
            val modIndex = getSelectedModifierFromShortName(nutriments, nutrientShortName)
            index.add(i)
            val nutrientNames = resources.getStringArray(R.array.nutrients_array)
            addNutrientRow(i, nutrientNames[i], true, value, unitIndex, modIndex)
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

    private fun getSelectedUnitFromShortName(nutriments: Nutriments, nutrientShortName: String): Int =
            getSelectedUnit(nutrientShortName, nutriments[nutrientShortName]?.unit)

    private fun getSelectedUnit(nutrientShortName: String?, unit: String?) = if (unit != null) {
        if (Nutriments.ENERGY_KCAL == nutrientShortName || Nutriments.ENERGY_KJ == nutrientShortName)
            throw IllegalArgumentException("Nutrient cannot be energy")
        else getPositionInAllUnitArray(unit)
    } else 0

    private fun getSelectedModifierFromShortName(nutriments: Nutriments, nutrientShortName: String): Int =
            getPositionInModifierArray(nutriments[nutrientShortName]?.modifier ?: "")

    private fun updateServingSizeFrom(servingSize: String) {
        val part = servingSize.split(Regex("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")).toTypedArray()
        binding.servingSize.setText(part[0])
        if (part.size > 1) {
            binding.servingSize.unitSpinner?.setSelection(getPositionInServingUnitArray(part[1].trim { it <= ' ' }))
        }
    }

    /**
     * Pre fill the fields if the product is already present in SavedProductOffline db.
     */
    private fun preFillValuesFromOffline() {
        val productDetails = mOfflineSavedProduct!!.productDetails
        if (productDetails != null) {
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
            if (productDetails[ApiFields.Keys.NUTRITION_DATA_PER] != null) {
                val nutritionDataPer = productDetails[ApiFields.Keys.NUTRITION_DATA_PER]
                // can be "100g" or "serving"
                updateSelectedDataPer(nutritionDataPer!!)
            }
            val servingSize = productDetails[ApiFields.Keys.SERVING_SIZE]
            if (servingSize != null) {
                // Splits the serving size into value and unit. Example: "15g" into "15" and "g"
                updateServingSizeFrom(servingSize)
            }
            (binding.root as ViewGroup).getViewsByType(CustomValidatingEditTextView::class.java).forEach { view ->
                val nutrientShortName = view.entryName
                if (nutrientShortName == binding.servingSize.entryName) {
                    return@forEach
                }
                val nutrientCompleteName = getCompleteEntryName(view)
                val value = productDetails[nutrientCompleteName]
                if (value != null) {
                    view.setText(value)
                    view.unitSpinner?.setSelection(getSelectedUnit(nutrientShortName, productDetails[nutrientCompleteName + ApiFields.Suffix.UNIT]))
                }
            }
            //set the values of all the other nutrients if defined and create new row in the tableLayout.
            PARAMS_OTHER_NUTRIENTS.withIndex().forEach { (i, completeNutrientName) ->
                if (productDetails[completeNutrientName] != null) {
                    var unitIndex = 0
                    var modIndex = 0
                    val value = productDetails[completeNutrientName]
                    if (productDetails[completeNutrientName + ApiFields.Suffix.UNIT] != null) {
                        unitIndex = getPositionInAllUnitArray(productDetails[completeNutrientName + ApiFields.Suffix.UNIT])
                    }
                    if (productDetails[completeNutrientName + ApiFields.Suffix.MODIFIER] != null) {
                        modIndex = getPositionInAllUnitArray(productDetails[completeNutrientName + ApiFields.Suffix.MODIFIER])
                    }
                    index.add(i)
                    val nutrients = resources.getStringArray(R.array.nutrients_array)
                    addNutrientRow(i, nutrients[i], true, value, unitIndex, modIndex)
                }
            }
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
        picassoBuilder(requireContext())
                .load(path)
                .resize(dpsToPixel(50, requireContext()), dpsToPixel(50, requireContext()))
                .centerInside()
                .into(binding.btnAddImageNutritionFacts, object : Callback {
                    override fun onSuccess() = afterNutritionImgLoaded()
                    override fun onError(ex: Exception) = afterNutritionImgLoaded()
                })
    }

    private fun afterNutritionImgLoaded() {
        binding.imageProgress.visibility = View.GONE
        binding.btnEditImageNutritionFacts.visibility = View.VISIBLE
    }

    private fun getSelectedUnit(i: Int) = NUTRIENTS_UNITS[i]

    /**
     * @param unit The unit corresponding to which the index is to be returned.
     * @return returns the index to be set to the spinner.
     */
    private fun getPositionInAllUnitArray(unit: String?) =
            NUTRIENTS_UNITS.indexOfFirst { it.equals(unit, true) }.coerceAtLeast(0)

    private fun getPositionInModifierArray(mod: String) =
            MODIFIERS.indexOfFirst { it == mod }.coerceAtLeast(0)

    private fun getPositionInServingUnitArray(unit: String) =
            SERVING_UNITS.indexOfFirst { it.equals(unit, ignoreCase = true) }.coerceAtLeast(0)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity()
    }

    private fun addNutritionFactsImage() {
        val path = imagePath
        if (path == null) {
            newNutritionFactsImage()
            return
        }
        if (photoFile != null) {
            cropRotateImage(photoFile, getString(R.string.nutrition_facts_picture))
        } else {
            download(requireContext(), path)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        photoFile = it
                        cropRotateImage(photoFile, getString(R.string.nutrition_facts_picture))
                    }.addTo(disp)
        }
    }

    private fun newNutritionFactsImage() = doChooseOrTakePhotos(getString(R.string.nutrition_facts_picture))

    override fun doOnPhotosPermissionGranted() = newNutritionFactsImage()

    private fun updateNextBtnState() {
        val allValuesValid = allValid()
        binding.globalValidationMsg.visibility = if (allValuesValid) View.GONE else View.VISIBLE
        binding.btnAdd.isEnabled = allValuesValid
    }

    private fun CustomValidatingEditTextView.checkValue(value: Float) = sequenceOf(
            this.checkPh(value),
            this.checkAlcohol(value),
            this.checkEnergyField(value),
            this.checkCarbohydrate(value),
            this.checkPerServing()
    ).firstOrNull { it != ValueState.NOT_TESTED } ?: this.checkAsGram(value)

    private fun CustomValidatingEditTextView.checkAsGram(value: Float): ValueState {
        val valid = convertToGrams(value, unitSpinner!!.selectedItemPosition) <= referenceValueInGram
        return if (!valid) {
            this.showError(getString(R.string.max_nutrient_val_msg))
            ValueState.NOT_VALID
        } else ValueState.VALID
    }

    private fun CustomValidatingEditTextView.checkValue() {
        val wasValid = isError()
        //if no value, we suppose it's valid
        if (isBlank()) {
            cancelError()
            //if per serving is set must be not blank
            this.checkPerServing()
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
        if (binding.servingSize.entryName == entryName) {
            checkAllValues()
        }
    }

    private fun CustomValidatingEditTextView.addValidListener() {
        val textWatcher = ValidTextWatcher(this)
        addTextChangedListener(textWatcher)
        unitSpinner?.onItemSelectedListener = textWatcher
    }

    fun updateSodiumValue() {
        if (requireActivity().currentFocus === binding.salt) {
            val saltValue = binding.salt.getDoubleValue()
            if (saltValue != null) {
                val sodiumValue = UnitUtils.saltToSodium(saltValue)
                binding.sodium.setText(getRoundNumber(sodiumValue))
            }
        }
    }

    fun updateSaltValue() {
        if (requireActivity().currentFocus === binding.sodium) {
            val sodiumValue = binding.sodium.getDoubleValue()
            if (sodiumValue != null) {
                val saltValue = UnitUtils.sodiumToSalt(sodiumValue)
                binding.salt.setText(getRoundNumber(saltValue))
            }
        }
    }

    private fun toggleNoNutritionData(isChecked: Boolean) {
        binding.nutritionFactsLayout.visibility = if (isChecked) View.GONE else View.VISIBLE
    }

    /**
     * adds only those fields to the query map which are not empty.
     */
    override fun addUpdatedFieldsToMap(targetMap: MutableMap<String, String?>) {

        // Add no nutrition data entry to map
        if (binding.checkboxNoNutritionData.isChecked) {
            targetMap[ApiFields.Keys.NO_NUTRITION_DATA] = "on"
        } else {
            addNutrientsModeToMap(targetMap)
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
        allEditViews.forEach {
            if (binding.servingSize.entryName == it.entryName) {
                return@forEach
            }
            if (it.isNotEmpty()) {
                addNutrientToMapIfUpdated(it, targetMap)
            }
        }
    }

    /**
     * adds all the fields to the query map even those which are null or empty.
     */
    private fun addAllFieldsToMap(targetMap: MutableMap<String, String?>) {
        if (activity !is ProductEditActivity) return

        val noData = binding.checkboxNoNutritionData.isChecked
        if (noData) {
            targetMap[ApiFields.Keys.NO_NUTRITION_DATA] = "on"
            return
        }
        val servingSizeValue = if (binding.servingSize.text == null || binding.servingSize.text.toString().isEmpty()) {
            StringUtils.EMPTY
        } else {
            @Suppress("USELESS_ELVIS")
            binding.servingSize.text.toString() + binding.servingSize.unitSpinner?.selectedItem ?: ""
        }
        targetMap[ApiFields.Keys.SERVING_SIZE] = servingSizeValue
        for (editTextView in allEditViews) {
            if (binding.servingSize.entryName == editTextView.entryName) {
                continue
            }
            addNutrientToMap(editTextView, targetMap)
        }
    }

    /**
     * Add nutrients to the map by from the text entered into EditText, only if the value has been edited
     *
     * @param editTextView EditText with spinner for entering the nutients
     * @param targetMap map to enter the nutrient value recieved from edit texts
     */
    private fun addNutrientToMapIfUpdated(editTextView: CustomValidatingEditTextView, targetMap: MutableMap<String, String?>) {
        val productNutriments = if (product != null) product!!.nutriments else Nutriments()
        val shortName = editTextView.entryName
        val oldProductNutriment = productNutriments[shortName]
        var oldValue: String? = null
        var oldUnit: String? = null
        var oldMod: String? = null
        if (oldProductNutriment != null) {
            oldUnit = oldProductNutriment.unit
            oldMod = oldProductNutriment.modifier
            if (isDataPer100g) {
                oldValue = oldProductNutriment.for100gInUnits
            } else if (isDataPerServing) {
                oldValue = oldProductNutriment.forServingInUnits
            }
        }
        val valueHasBeenUpdated = editTextView.isContentDifferent(oldValue)
        var newUnit: String? = null
        var newMod: String? = null

        editTextView.unitSpinner?.let {
            if (editTextView.hasUnit()) {
                newUnit = getSelectedUnit(it.selectedItemPosition)
            }
        }
        editTextView.modSpinner?.let {
            newMod = it.selectedItem.toString()
        }


        val unitHasBeenUpdated = oldUnit == null || oldUnit != newUnit
        val modHasBeenUpdated = oldMod == null || oldMod != newMod
        if (valueHasBeenUpdated || unitHasBeenUpdated || modHasBeenUpdated) {
            addNutrientToMap(editTextView, targetMap)
        }
    }

    /**
     * Add nutrients to the map by from the text entered into EditText
     *
     * @param editTextView EditText with spinner for entering the nutrients
     * @param targetMap map to enter the nutrient value received from edit texts
     */
    private fun addNutrientToMap(editTextView: CustomValidatingEditTextView, targetMap: MutableMap<String, String?>) {
        // For impl reference, see https://wiki.openfoodfacts.org/Nutrients_handling_in_Open_Food_Facts#Data_display
        val fieldName = getCompleteEntryName(editTextView)

        // Add unit field {nutrient-id}_unit to map
        if (editTextView.hasUnit() && editTextView.unitSpinner != null) {
            val selectedUnit = getSelectedUnit(editTextView.unitSpinner!!.selectedItemPosition)
            targetMap[fieldName + ApiFields.Suffix.UNIT] = Html.escapeHtml(selectedUnit)
        }

        // Take modifier from attached spinner, add to value if not the default one
        var mod = ""
        if (editTextView.modSpinner != null) {
            val selectedMod = editTextView.modSpinner!!.selectedItem.toString()
            if (DEFAULT_MODIFIER != selectedMod) {
                mod = selectedMod
            }
        }
        // The suffix can either be _serving or _100g depending on user input
        val value = editTextView.text!!.toString()
        targetMap[fieldName] = mod + value
    }

    private fun addNutrientsModeToMap(targetMap: MutableMap<String, String?>) {
        if (isDataPer100g) {
            targetMap[ApiFields.Keys.NUTRITION_DATA_PER] = NUTRITION_DATA_PER_100G
        } else if (isDataPerServing) {
            targetMap[ApiFields.Keys.NUTRITION_DATA_PER] = NUTRITION_DATA_PER_SERVING
        }
    }

    private val isDataPerServing: Boolean
        get() = binding.radioGroup.checkedRadioButtonId == R.id.per_serving
    private val isDataPer100g: Boolean
        get() = binding.radioGroup.checkedRadioButtonId == R.id.for100g_100ml
    private val referenceValueInGram: Float
        get() {
            var reference = 100f
            if (binding.radioGroup.checkedRadioButtonId != R.id.for100g_100ml) {
                reference = binding.servingSize.getFloatValueOr(reference)
                reference = UnitUtils.convertToGrams(reference, SERVING_UNITS[binding.servingSize.unitSpinner!!.selectedItemPosition])
            }
            return reference
        }

    private fun displayAddNutrientDialog() {
        val origNutrients = resources.getStringArray(R.array.nutrients_array).toMutableList()
        index.forEach { origNutrients.removeAt(it) }
        origNutrients.sortWith(Collator.getInstance(Locale.getDefault()))

        MaterialDialog.Builder(requireActivity())
                .title(R.string.choose_nutrient)
                .items(origNutrients)
                .itemsCallback { _, _, _, text ->
                    index.add(origNutrients.indexOf(text))
                    val textView = addNutrientRow(origNutrients.indexOf(text), text.toString())
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
            value: String? = null,
            unitSelectedIndex: Int = 0,
            modSelectedIndex: Int = 0
    ): CustomValidatingEditTextView {
        val nutrientCompleteName = PARAMS_OTHER_NUTRIENTS[index]
        val rowView = layoutInflater.inflate(R.layout.nutrition_facts_table_row, binding.tableLayout, false) as TableRow
        val editText: CustomValidatingEditTextView = rowView.findViewById(R.id.value)
        editText.hint = hint
        val nutrientShortName = getShortName(nutrientCompleteName)
        editText.entryName = nutrientShortName
        editText.keyListener = keyListener
        lastEditText!!.nextFocusDownId = editText.id
        lastEditText!!.imeOptions = EditorInfo.IME_ACTION_NEXT
        lastEditText = editText
        editText.imeOptions = EditorInfo.IME_ACTION_DONE
        editText.requestFocus()
        if (preFillValues) {
            editText.setText(value)
        }

        // Setup unit spinner
        val unitSpinner = rowView.findViewById<Spinner>(R.id.spinner_unit)
        val modSpinner = rowView.findViewById<Spinner>(R.id.spinner_mod)
        if (Nutriments.PH == nutrientShortName) {
            unitSpinner.visibility = View.INVISIBLE
        } else if (Nutriments.STARCH == nutrientShortName) {
            val arrayAdapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, requireActivity().resources.getStringArray(R.array.weights_array))
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            unitSpinner.adapter = arrayAdapter
            starchEditText = editText
        }
        if (preFillValues) {
            unitSpinner.setSelection(unitSelectedIndex)
            modSpinner.setSelection(modSelectedIndex)
        }
        binding.tableLayout.addView(rowView)
        return editText
    }

    /**
     * Converts a given quantity's unit to grams.
     *
     * @param value The value to be converted
     * @param index 1 represents milligrams, 2 represents micrograms
     * @return return the converted value
     */
    private fun convertToGrams(value: Float, index: Int): Float {
        val unit = NUTRIENTS_UNITS[index]
        //can't be converted to grams.
        return if (Units.UNIT_DV == unit || UnitUtils.UNIT_IU == unit) {
            0F
        } else UnitUtils.convertToGrams(value, unit)
    }

    private fun isCarbohydrateRelated(editText: CustomValidatingEditTextView): Boolean {
        val entryName = editText.entryName
        return binding.sugars.entryName == entryName || starchEditText != null && entryName == starchEditText!!.entryName
    }

    /**
     * Validate the value of carbohydrate using carbs value and sugar value
     * @param value quality value with known prefix
     */
    private fun CustomValidatingEditTextView.checkCarbohydrate(value: Float): ValueState {
        if (binding.carbohydrates.entryName != entryName) {
            return ValueState.NOT_TESTED
        }
        val res = this.checkAsGram(value)
        if (ValueState.NOT_VALID == res) {
            return res
        }
        var carbsValue = binding.carbohydrates.getFloatValueOr(0f)
        var sugarValue = binding.sugars.getFloatValueOr(0f)
        // check that value of (sugar + starch) is not greater than value of carbohydrates
        //convert all the values to grams
        carbsValue = convertToGrams(carbsValue, binding.carbohydrates.unitSpinner!!.selectedItemPosition)
        sugarValue = convertToGrams(sugarValue, binding.sugars.unitSpinner!!.selectedItemPosition)
        val newStarch = convertToGrams(starchValue, starchUnitSelectedIndex).toDouble()
        return if (sugarValue + newStarch > carbsValue) {
            binding.carbohydrates.showError(getString(R.string.error_in_carbohydrate_value))
            ValueState.NOT_VALID
        } else {
            ValueState.VALID
        }
    }

    /**
     * Validate serving size value entered by user
     */
    private fun CustomValidatingEditTextView.checkPerServing(): ValueState {
        if (binding.servingSize.entryName == entryName) {
            if (isDataPer100g) {
                return ValueState.VALID
            }
            val value = binding.servingSize.getFloatValueOr(0f)
            if (value <= 0) {
                showError(getString(R.string.error_nutrient_serving_data))
                return ValueState.NOT_VALID
            }
            return ValueState.VALID
        }
        return ValueState.NOT_TESTED
    }

    /**
     * Validate oh value according to [Nutriments.PH]
     * @param value quality value with known prefix
     */
    private fun CustomValidatingEditTextView.checkPh(value: Float): ValueState {
        if (Nutriments.PH == entryName) {
            val maxPhValue = 14.0
            if (value > maxPhValue || value >= maxPhValue && this.isModifierEqualsToGreaterThan()) {
                setText(maxPhValue.toString())
            }
            return ValueState.VALID
        }
        return ValueState.NOT_TESTED
    }

    /**
     * Validate energy value entered by user
     */
    private fun CustomValidatingEditTextView.checkEnergyField(value: Float): ValueState {
        when (entryName) {
            binding.energyKcal.entryName -> {
                var energyInKcal = value
                if (binding.radioGroup.checkedRadioButtonId != R.id.for100g_100ml) {
                    energyInKcal *= 100.0f / referenceValueInGram
                }
                val isValid = energyInKcal <= 2000f
                if (!isValid) {
                    showError(getString(R.string.max_energy_val_msg))
                }
                return if (isValid) ValueState.VALID else ValueState.NOT_VALID
            }
            binding.energyKj.entryName -> {
                var energyInKj = value
                if (binding.radioGroup.checkedRadioButtonId != R.id.for100g_100ml) {
                    energyInKj *= 100.0f / referenceValueInGram
                }
                val isValid = energyInKj <= 8368000f
                if (!isValid) {
                    showError(getString(R.string.max_energy_val_msg))
                }
                return if (isValid) ValueState.VALID else ValueState.NOT_VALID
            }
            else -> return ValueState.NOT_TESTED
        }
    }

    /**
     * validate alcohol content entered by user
     */
    private fun CustomValidatingEditTextView.checkAlcohol(value: Float): ValueState {
        if (binding.alcohol.entryName == entryName) {
            if (value > 100) binding.alcohol.setText("100.0")
            return ValueState.VALID
        }
        return ValueState.NOT_TESTED
    }

    override fun showImageProgress() {
        if (!isAdded) return
        binding.imageProgress.visibility = View.VISIBLE
        binding.imageProgressText.visibility = View.VISIBLE
        binding.btnAddImageNutritionFacts.visibility = View.INVISIBLE
        binding.btnEditImageNutritionFacts.visibility = View.INVISIBLE
    }

    private val starchValue: Float
        get() = if (starchEditText == null) 0F else starchEditText!!.getFloatValue() ?: 0F


    private val starchUnitSelectedIndex: Int
        get() = if (starchEditText == null) 0 else starchEditText!!.unitSpinner!!.selectedItemPosition

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoReceiverHandler!!.onActivityResult(this, requestCode, resultCode, data)
    }

    override fun hideImageProgress(errorInUploading: Boolean, message: String) {
        if (!isAdded) return
        binding.imageProgress.visibility = View.GONE
        binding.imageProgressText.visibility = View.GONE
        binding.btnAddImageNutritionFacts.visibility = View.VISIBLE
        binding.btnEditImageNutritionFacts.visibility = View.VISIBLE
        if (!errorInUploading) {
            picassoBuilder(requireContext())
                    .load(photoFile!!)
                    .resize(dpsToPixel(50, requireContext()), dpsToPixel(50, requireContext()))
                    .centerInside()
                    .into(binding.btnAddImageNutritionFacts)
        }
    }

    internal inner class ValidTextWatcher(private val editTextView: CustomValidatingEditTextView) : TextWatcher, OnItemSelectedListener {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            //nothing to do
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            //nothing to do
        }

        override fun afterTextChanged(s: Editable) {
            editTextView.checkValueAndRelated()
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
            editTextView.checkValueAndRelated()
            if (binding.salt.entryName == editTextView.entryName) {
                binding.sodium.unitSpinner!!.setSelection(binding.salt.unitSpinner!!.selectedItemPosition)
            }
            if (binding.sodium.entryName == editTextView.entryName) {
                binding.salt.unitSpinner!!.setSelection(binding.sodium.unitSpinner!!.selectedItemPosition)
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            editTextView.checkValueAndRelated()
        }
    }

    companion object {
        private val NUTRIENTS_UNITS = listOf(Units.UNIT_GRAM, Units.UNIT_MILLIGRAM, Units.UNIT_MICROGRAM, Units.UNIT_DV, UnitUtils.UNIT_IU)
        private val SERVING_UNITS = listOf(Units.UNIT_GRAM, Units.UNIT_MILLIGRAM, Units.UNIT_MICROGRAM, Units.UNIT_LITER, Units.UNIT_MILLILITRE)
    }
}