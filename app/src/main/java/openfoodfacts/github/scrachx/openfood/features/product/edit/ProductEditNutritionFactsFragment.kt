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
import com.afollestad.materialdialogs.MaterialDialog
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.FragmentAddProductNutritionFactsBinding
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.Nutriments
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.Units
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.utils.*
import openfoodfacts.github.scrachx.openfood.utils.EditTextUtils.getContent
import openfoodfacts.github.scrachx.openfood.utils.EditTextUtils.hasUnit
import openfoodfacts.github.scrachx.openfood.utils.EditTextUtils.isDifferent
import openfoodfacts.github.scrachx.openfood.utils.EditTextUtils.isNotEmpty
import openfoodfacts.github.scrachx.openfood.utils.FileDownloader.download
import openfoodfacts.github.scrachx.openfood.utils.Utils.dpsToPixel
import openfoodfacts.github.scrachx.openfood.utils.Utils.getViewsByType
import openfoodfacts.github.scrachx.openfood.utils.Utils.picassoBuilder
import org.apache.commons.lang.ObjectUtils
import org.apache.commons.lang.StringUtils
import java.io.File
import java.text.Collator
import java.util.*

/**
 * @see R.layout.fragment_add_product_nutrition_facts
 */
class ProductEditNutritionFactsFragment : BaseFragment() {
    private val keyListener: NumberKeyListener = object : NumberKeyListener() {
        override fun getInputType() = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD

        override fun getAcceptedChars() = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ',', '.')
    }
    private var _binding: FragmentAddProductNutritionFactsBinding? = null
    private val binding get() = _binding!!
    private var photoReceiverHandler: PhotoReceiverHandler? = null
    private val disp = CompositeDisposable()
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
        binding.salt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                updateSodiumValue()
            }
        })
        binding.spinnerSaltComp.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                updateSodiumMod()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // This is not possible
            }
        }
        binding.sodium.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                updateSaltValue()
            }
        })
        binding.spinnerSodiumComp.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                updateSaltMod()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // This is not possible
            }
        }
        binding.checkboxNoNutritionData.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChanged(isChecked)
        }
        photoReceiverHandler = PhotoReceiverHandler { newPhotoFile: File ->
            val resultUri = newPhotoFile.toURI()
            imagePath = resultUri.path
            photoFile = newPhotoFile
            val image = ProductImage(productCode, ProductImageField.NUTRITION, newPhotoFile)
            image.filePath = resultUri.path
            if (activity is ProductEditActivity) {
                (activity as ProductEditActivity).addToPhotoMap(image, 2)
            }
            hideImageProgress(false)
        }
        binding.btnAddANutrient.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_box_black_18dp, 0, 0, 0)
        val b = arguments
        lastEditText = binding.alcohol
        if (b != null) {
            product = b.getSerializable("product") as Product?
            mOfflineSavedProduct = b.getSerializable("edit_offline_product") as OfflineSavedProduct?
            val productEdited: Boolean = b.getBoolean(ProductEditActivity.KEY_IS_EDITING)
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
        allEditViews = HashSet(getViewsByType((view as ViewGroup), CustomValidatingEditTextView::class.java))
        for (editText in allEditViews) {
            addValidListener(editText)
            checkValue(editText)
        }
        if (getActivity() is ProductEditActivity && (getActivity() as ProductEditActivity?)!!.initialValues != null) {
            addAllFieldsToMap((getActivity() as ProductEditActivity?)!!.initialValues!!)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disp.dispose()
        _binding = null
    }

    private fun updateSodiumMod() {
        binding.spinnerSodiumComp.setSelection(binding.spinnerSaltComp.selectedItemPosition)
    }

    private fun updateSaltMod() {
        binding.spinnerSaltComp.setSelection(binding.spinnerSodiumComp.selectedItemPosition)
    }

    private fun checkAllValues() {
        for (editText in allEditTextView) {
            checkValue(editText)
        }
    }

    private val allEditTextView: Collection<CustomValidatingEditTextView>
        get() = allEditViews
    private val isAllValuesValid: Boolean
        get() {
            for (editText in allEditTextView) {
                if (editText.isError) {
                    return false
                }
            }
            return true
        }

    fun containsInvalidValue(): Boolean {
        return !isAllValuesValid
    }

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
        val editViews = getViewsByType((view as ViewGroup?)!!, CustomValidatingEditTextView::class.java)
        for (view in editViews) {
            val nutrientShortName = view.entryName
            // Skip serving size and energy view, we already filled them
            if (view === binding.servingSize || view === binding.energyKcal || view === binding.energyKj) {
                continue
            }

            // Get the value
            val value = if (isDataPer100g) nutriments.get100g(nutrientShortName) else nutriments.getServing(nutrientShortName)
            if (value.isEmpty()) {
                continue
            }
            view.setText(value)
            view.unitSpinner?.setSelection(getSelectedUnitFromShortName(nutriments, nutrientShortName))
            view.modSpinner?.setSelection(getSelectedModifierFromShortName(nutriments, nutrientShortName))
        }

        // Set the values of all the other nutrients if defined and create new row in the tableLayout.
        for (i in PARAMS_OTHER_NUTRIENTS.indices) {
            val nutrientShortName = getShortName(PARAMS_OTHER_NUTRIENTS[i])
            val value = if (isDataPer100g) nutriments.get100g(nutrientShortName) else nutriments.getServing(nutrientShortName)
            if (value.isEmpty()) {
                continue
            }
            val unitIndex = getSelectedUnitFromShortName(nutriments, nutrientShortName)
            val modIndex = getSelectedModifierFromShortName(nutriments, nutrientShortName)
            index.add(i)
            val nutrients = resources.getStringArray(R.array.nutrients_array)
            addNutrientRow(i, nutrients[i], true, value, unitIndex, modIndex)
        }
    }

    /**
     * Load the nutrition image uploaded form AddProductActivity
     */
    fun loadNutritionImage() {
        photoFile = null
        val newImageNutritionUrl = product!!.getImageNutritionUrl(requireAddProductActivity().productLanguageForEdition)
        if (newImageNutritionUrl.isNullOrEmpty()) return

        binding.imageProgress.visibility = View.VISIBLE
        imagePath = newImageNutritionUrl
        loadNutritionsImage(imagePath!!)
    }

    private fun getSelectedUnitFromShortName(nutriments: Nutriments, nutrientShortName: String): Int {
        val unit = nutriments.getUnit(nutrientShortName)
        return getSelectedUnit(nutrientShortName, unit)
    }

    private fun getSelectedUnit(nutrientShortName: String?, unit: String?): Int {
        var unitSelectedIndex = 0
        if (unit != null) {
            unitSelectedIndex = if (Nutriments.ENERGY_KCAL == nutrientShortName || Nutriments.ENERGY_KJ == nutrientShortName) {
                throw IllegalArgumentException("Nutrient cannot be energy")
            } else {
                getPositionInAllUnitArray(unit)
            }
        }
        return unitSelectedIndex
    }

    private fun getSelectedModifierFromShortName(nutriments: Nutriments, nutrientShortName: String): Int {
        val mod = nutriments.getModifier(nutrientShortName)
        return getPositionInModifierArray(mod)
    }

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
        val productDetails = mOfflineSavedProduct!!.productDetailsMap
        if (productDetails != null) {
            if (productDetails["image_nutrition_facts"] != null) {
                imagePath = productDetails["image_nutrition_facts"]
                val path = "$LOCALE_FILE_SCHEME$imagePath"
                binding.imageProgress.visibility = View.VISIBLE
                loadNutritionsImage(path)
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
            val editViews = getViewsByType((view as ViewGroup?)!!, CustomValidatingEditTextView::class.java)
            for (view in editViews) {
                val nutrientShortName = view.entryName
                if (nutrientShortName == binding.servingSize.entryName) {
                    continue
                }
                val nutrientCompleteName = getCompleteEntryName(view)
                val value = productDetails[nutrientCompleteName]
                if (value != null) {
                    view.setText(value)
                    view.unitSpinner?.setSelection(getSelectedUnit(nutrientShortName, productDetails[nutrientCompleteName + ApiFields.Suffix.UNIT]))
                }
            }
            //set the values of all the other nutrients if defined and create new row in the tableLayout.
            for (i in PARAMS_OTHER_NUTRIENTS.indices) {
                val completeNutrientName = PARAMS_OTHER_NUTRIENTS[i]
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

    private fun updateSelectedDataPer(value: String) {
        binding.radioGroup.clearCheck()
        when (value) {
            ApiFields.Defaults.NUTRITION_DATA_PER_100G -> {
                binding.radioGroup.check(R.id.for100g_100ml)
            }
            ApiFields.Defaults.NUTRITION_DATA_PER_SERVING -> {
                binding.radioGroup.check(R.id.per_serving)
            }
            else -> {
                throw IllegalArgumentException("value is neither 100g nor serving")
            }
        }
        binding.radioGroup.jumpDrawablesToCurrentState()
    }

    /**
     * Loads nutrition image into the ImageView
     *
     * @param path path of the image
     */
    private fun loadNutritionsImage(path: String) {
        picassoBuilder(activity)
                .load(path)
                .resize(dpsToPixel(50, getActivity()), dpsToPixel(50, getActivity()))
                .centerInside()
                .into(binding.btnAddImageNutritionFacts, object : Callback {
                    override fun onSuccess() {
                        afterNutritionImgLoaded()
                    }

                    override fun onError(ex: Exception) {
                        afterNutritionImgLoaded()
                    }
                })
    }

    private fun afterNutritionImgLoaded() {
        binding.imageProgress.visibility = View.GONE
        binding.btnEditImageNutritionFacts.visibility = View.VISIBLE
    }

    private fun getSelectedUnit(selectedIdx: Int) = NUTRIENTS_UNITS[selectedIdx]

    /**
     * @param unit The unit corresponding to which the index is to be returned.
     * @return returns the index to be set to the spinner.
     */
    private fun getPositionInAllUnitArray(unit: String?): Int {
        return NUTRIENTS_UNITS.indices.firstOrNull { NUTRIENTS_UNITS[it].equals(unit, ignoreCase = true) } ?: 0
    }

    private fun getPositionInModifierArray(mod: String): Int {
        return MODIFIERS.indices.firstOrNull { MODIFIERS[it] == mod } ?: 0
    }

    private fun getPositionInServingUnitArray(unit: String): Int {
        return SERVING_UNITS.indices.firstOrNull { SERVING_UNITS[it].equals(unit, ignoreCase = true) } ?: 0
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity()
    }

    private fun addNutritionFactsImage() {
        if (imagePath != null) {
            if (photoFile != null) {
                cropRotateImage(photoFile, getString(R.string.nutrition_facts_picture))
            } else {
                disp.add(download(requireContext(), imagePath!!)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { file: File? ->
                            photoFile = file
                            cropRotateImage(photoFile, getString(R.string.nutrition_facts_picture))
                        })
            }
        } else {
            newNutritionFactsImage()
        }
    }

    private fun newNutritionFactsImage() {
        doChooseOrTakePhotos(getString(R.string.nutrition_facts_picture))
    }

    override fun doOnPhotosPermissionGranted() {
        newNutritionFactsImage()
    }

    operator fun next() {
        (getActivity() as? ProductEditActivity)?.proceed()
    }

    private fun updateNextBtnState() {
        val allValuesValid = isAllValuesValid
        binding.globalValidationMsg.visibility = if (allValuesValid) View.GONE else View.VISIBLE
        binding.btnAdd.isEnabled = allValuesValid
    }

    private fun checkValue(text: CustomValidatingEditTextView, value: Float): ValueState {
        var res = checkPh(text, value)
        if (res !== ValueState.NOT_TESTED) {
            return res
        }
        res = checkAlcohol(text, value)
        if (res !== ValueState.NOT_TESTED) {
            return res
        }
        res = checkEnergyField(text, value)
        if (res !== ValueState.NOT_TESTED) {
            return res
        }
        res = checkCarbohydrate(text, value)
        if (res !== ValueState.NOT_TESTED) {
            return res
        }
        res = checkPerServing(text)
        return if (res !== ValueState.NOT_TESTED) {
            res
        } else checkAsGram(text, value)
    }

    private fun checkAsGram(text: CustomValidatingEditTextView, value: Float): ValueState {
        val reference = referenceValueInGram
        val valid = convertToGrams(value, text.unitSpinner!!.selectedItemPosition) <= reference
        if (!valid) {
            text.showError(getString(R.string.max_nutrient_val_msg))
        }
        return if (valid) ValueState.VALID else ValueState.NOT_VALID
    }

    private fun checkValue(textView: CustomValidatingEditTextView) {
        val wasValid = textView.isError
        //if no value, we suppose it's valid
        if (textView.isBlank()) {
            textView.cancelError()
            //if per serving is set must be not blank
            checkPerServing(textView)
        } else {
            val value = textView.getFloatValue()
            if (value == null) {
                textView.showError(getString(R.string.error_nutrient_entry))
            } else {
                val valueState = checkValue(textView, value)
                if (valueState == ValueState.VALID) {
                    textView.cancelError()
                }
            }
        }
        if (wasValid != textView.isValid) {
            updateNextBtnState()
        }
    }

    private fun checkValueAndRelated(text: CustomValidatingEditTextView) {
        checkValue(text)
        if (isCarbohydrateRelated(text)) {
            checkValue(binding.carbohydrates)
        }
        if (binding.servingSize.entryName == text.entryName) {
            checkAllValues()
        }
    }

    private fun addValidListener(target: CustomValidatingEditTextView) {
        val textWatcher = ValidTextWatcher(target)
        target.addTextChangedListener(textWatcher)
        target.unitSpinner?.onItemSelectedListener = textWatcher

    }

    fun updateSodiumValue() {
        if (requireActivity().currentFocus === binding.salt) {
            val saltValue = getDoubleValue(binding.salt)
            if (saltValue != null) {
                val sodiumValue = UnitUtils.saltToSodium(saltValue)
                binding.sodium.setText(sodiumValue.toString())
            }
        }
    }

    fun updateSaltValue() {
        if (requireActivity().currentFocus === binding.sodium) {
            val sodiumValue = getDoubleValue(binding.sodium)
            if (sodiumValue != null) {
                val saltValue = UnitUtils.sodiumToSalt(sodiumValue)
                binding.salt.setText(saltValue.toString())
            }
        }
    }

    private fun onCheckedChanged(isChecked: Boolean) {
        if (isChecked) {
            binding.nutritionFactsLayout.visibility = View.GONE
        } else {
            binding.nutritionFactsLayout.visibility = View.VISIBLE
        }
    }

    /**
     * adds only those fields to the query map which are not empty.
     */
    fun addUpdatedFieldsToMap(targetMap: MutableMap<String, String?>) {

        // Add no nutrition data entry to map
        if (binding.checkboxNoNutritionData.isChecked) {
            targetMap[ApiFields.Keys.NO_NUTRITION_DATA] = "on"
        } else {
            addNutrientsModeToMap(targetMap)
        }

        // Add serving size entry to map if it has been changed
        if (binding.servingSize.isNotEmpty()) {
            val servingSizeValue = getContent(binding.servingSize) + ObjectUtils
                    .toString(binding.servingSize.unitSpinner!!.selectedItem.toString())
            if (product == null || servingSizeValue != product!!.servingSize) {
                targetMap[ApiFields.Keys.SERVING_SIZE] = servingSizeValue
            }
        }

        // For every nutrition field add it to map if updated
        for (editTextView in allEditTextView) {
            if (binding.servingSize.entryName == editTextView.entryName) {
                continue
            }
            if (editTextView.text != null && editTextView.isNotEmpty()) {
                addNutrientToMapIfUpdated(editTextView, targetMap)
            }
        }
    }

    /**
     * adds all the fields to the query map even those which are null or empty.
     */
    private fun addAllFieldsToMap(targetMap: MutableMap<String, String?>) {
        if (activity !is ProductEditActivity) {
            return
        }
        val noData = binding.checkboxNoNutritionData.isChecked
        if (noData) {
            targetMap[ApiFields.Keys.NO_NUTRITION_DATA] = "on"
            return
        }
        val servingSizeValue: String = if (binding.servingSize.text == null || binding.servingSize.text.toString().isEmpty()) {
            StringUtils.EMPTY
        } else {
            binding.servingSize.text.toString() + ObjectUtils.toString(binding.servingSize.unitSpinner?.selectedItem)
        }
        targetMap[ApiFields.Keys.SERVING_SIZE] = servingSizeValue
        for (editTextView in allEditTextView) {
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
        val valueHasBeenUpdated = isDifferent(editTextView, oldValue)
        var newUnit: String? = null
        var newMod: String? = null

        editTextView.unitSpinner?.let {
            if (hasUnit(editTextView)) {
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
        if (hasUnit(editTextView) && editTextView.unitSpinner != null) {
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
            targetMap[ApiFields.Keys.NUTRITION_DATA_PER] = ApiFields.Defaults.NUTRITION_DATA_PER_100G
        } else if (isDataPerServing) {
            targetMap[ApiFields.Keys.NUTRITION_DATA_PER] = ApiFields.Defaults.NUTRITION_DATA_PER_SERVING
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
                reference = getFloatValueOrDefault(binding.servingSize, reference)
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
                    addValidListener(textView)
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
     *
     * @param editText CustomValidatingEditTextView for retrieving the value enterd by the user
     * @param value quality value with known prefix
     */
    private fun checkCarbohydrate(editText: CustomValidatingEditTextView, value: Float): ValueState {
        if (binding.carbohydrates.entryName != editText.entryName) {
            return ValueState.NOT_TESTED
        }
        val res = checkAsGram(editText, value)
        if (ValueState.NOT_VALID == res) {
            return res
        }
        var carbsValue = getFloatValueOrDefault(binding.carbohydrates, 0f)
        var sugarValue = getFloatValueOrDefault(binding.sugars, 0f)
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
    private fun checkPerServing(editText: CustomValidatingEditTextView): ValueState {
        if (binding.servingSize.entryName == editText.entryName) {
            if (isDataPer100g) {
                return ValueState.VALID
            }
            val value = getFloatValueOrDefault(binding.servingSize, 0f)
            if (value <= 0) {
                editText.showError(getString(R.string.error_nutrient_serving_data))
                return ValueState.NOT_VALID
            }
            return ValueState.VALID
        }
        return ValueState.NOT_TESTED
    }

    /**
     * Validate oh value according to [Nutriments.PH]
     *
     * @param editText [CustomValidatingEditTextView] to get the value inputted from user
     * @param value quality value with known prefix
     */
    private fun checkPh(editText: CustomValidatingEditTextView, value: Float): ValueState {
        if (Nutriments.PH == editText.entryName) {
            val maxPhValue = 14.0
            if (value > maxPhValue || value >= maxPhValue && isModifierEqualsToGreaterThan(editText)) {
                editText.setText(maxPhValue.toString())
            }
            return ValueState.VALID
        }
        return ValueState.NOT_TESTED
    }

    /**
     * Validate energy value entered by user
     */
    private fun checkEnergyField(editTextView: CustomValidatingEditTextView, value: Float): ValueState {
        when (editTextView.entryName) {
            binding.energyKcal.entryName -> {
                var energyInKcal = value
                if (binding.radioGroup.checkedRadioButtonId != R.id.for100g_100ml) {
                    energyInKcal *= 100.0f / referenceValueInGram
                }
                val isValid = energyInKcal <= 2000f
                if (!isValid) {
                    editTextView.showError(getString(R.string.max_energy_val_msg))
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
                    editTextView.showError(getString(R.string.max_energy_val_msg))
                }
                return if (isValid) ValueState.VALID else ValueState.NOT_VALID
            }
            else -> return ValueState.NOT_TESTED
        }
    }

    /**
     * validate alcohol content entered by user
     */
    private fun checkAlcohol(editTextView: CustomValidatingEditTextView, value: Float): ValueState {
        if (binding.alcohol.entryName == editTextView.entryName) {
            if (value > 100) binding.alcohol.setText("100.0")
            return ValueState.VALID
        }
        return ValueState.NOT_TESTED
    }

    fun showImageProgress() {
        if (!isAdded) {
            return
        }
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

    fun hideImageProgress(errorInUploading: Boolean) {
        if (!isAdded) {
            return
        }
        binding.imageProgress.visibility = View.GONE
        binding.imageProgressText.visibility = View.GONE
        binding.btnAddImageNutritionFacts.visibility = View.VISIBLE
        binding.btnEditImageNutritionFacts.visibility = View.VISIBLE
        if (!errorInUploading) {
            Picasso.get()
                    .load(photoFile!!)
                    .resize(dpsToPixel(50, getActivity()), dpsToPixel(50, getActivity()))
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
            checkValueAndRelated(editTextView)
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
            checkValueAndRelated(editTextView)
            if (binding.salt.entryName == editTextView.entryName) {
                binding.sodium.unitSpinner!!.setSelection(binding.salt.unitSpinner!!.selectedItemPosition)
            }
            if (binding.sodium.entryName == editTextView.entryName) {
                binding.salt.unitSpinner!!.setSelection(binding.sodium.unitSpinner!!.selectedItemPosition)
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            checkValueAndRelated(editTextView)
        }
    }

    companion object {
        private val NUTRIENTS_UNITS = arrayOf(Units.UNIT_GRAM, Units.UNIT_MILLIGRAM, Units.UNIT_MICROGRAM, Units.UNIT_DV, UnitUtils.UNIT_IU)
        private val SERVING_UNITS = arrayOf(Units.UNIT_GRAM, Units.UNIT_MILLIGRAM, Units.UNIT_MICROGRAM, Units.UNIT_LITER, Units.UNIT_MILLILITRE)
    }
}