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
package openfoodfacts.github.scrachx.openfood.features.product.view.nutrition

import android.Manifest.permission
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_SHORT
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import io.reactivex.rxkotlin.addTo
import openfoodfacts.github.scrachx.openfood.AppFlavors.OBF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabsHelper
import openfoodfacts.github.scrachx.openfood.customtabs.WebViewFallback
import openfoodfacts.github.scrachx.openfood.databinding.FragmentNutritionProductBinding
import openfoodfacts.github.scrachx.openfood.features.FullScreenActivityOpener
import openfoodfacts.github.scrachx.openfood.features.ImagesManageActivity
import openfoodfacts.github.scrachx.openfood.features.adapters.NutrimentsGridAdapter
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.product.view.CalculateDetailsActivity
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity
import openfoodfacts.github.scrachx.openfood.features.product.view.summary.SummaryProductFragment.Companion.EDIT_PRODUCT_AFTER_LOGIN
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.features.shared.adapters.NutrientLevelListAdapter
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.*
import openfoodfacts.github.scrachx.openfood.models.Nutriments.Companion.ALCOHOL
import openfoodfacts.github.scrachx.openfood.models.Nutriments.Companion.FAT
import openfoodfacts.github.scrachx.openfood.models.Nutriments.Companion.SALT
import openfoodfacts.github.scrachx.openfood.models.Nutriments.Companion.SATURATED_FAT
import openfoodfacts.github.scrachx.openfood.models.Nutriments.Companion.SODIUM
import openfoodfacts.github.scrachx.openfood.models.Nutriments.Companion.SUGARS
import openfoodfacts.github.scrachx.openfood.models.entities.SendProduct
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.*
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.util.*

class NutritionProductFragment : BaseFragment(), CustomTabActivityHelper.ConnectionCallback {
    private var _binding: FragmentNutritionProductBinding? = null
    private val binding get() = _binding!!

    private var photoReceiverHandler = PhotoReceiverHandler { loadNutritionPhoto(it) }

    private lateinit var api: OpenFoodAPIClient
    private lateinit var product: Product

    /**
     * Boolean to determine if nutrition data should be shown
     */
    private var showNutritionData = true
    private val picasso: Picasso by lazy { Utils.picassoBuilder(requireContext()) }
    private val sharedPreferences by lazy { requireActivity().getSharedPreferences("prefs", 0) }

    /**
     * Boolean to determine if image should be loaded or not
     */
    private val isLowBatteryMode by lazy { requireContext().isDisableImageLoad() && requireContext().isBatteryLevelLow() }

    private var nutrientsImageUrl: String? = null
    private var mSendProduct: SendProduct? = null
    private var customTabActivityHelper: CustomTabActivityHelper? = null
    private var nutritionScoreUri: Uri? = null

    /**
     * The following booleans indicate whether the prompts are to be made visible
     */
    private var showNutritionPrompt = false
    private var showCategoryPrompt = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        api = OpenFoodAPIClient(requireActivity())
        _binding = FragmentNutritionProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // use VERTICAL divider
        val dividerItemDecoration = DividerItemDecoration(binding.nutrimentsRecyclerView.context, DividerItemDecoration.VERTICAL)
        binding.nutrimentsRecyclerView.addItemDecoration(dividerItemDecoration)
        binding.getNutriscorePrompt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_box_blue_18dp, 0, 0, 0)
        binding.newAdd.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_add_a_photo_blue_18dp, 0, 0, 0)

        binding.nutriscoreLink.setOnClickListener { openNutriScoreLink() }
        binding.imageViewNutrition.setOnClickListener { openFullScreen() }
        binding.calculateNutritionFacts.setOnClickListener { calculateNutritionFacts() }
        binding.getNutriscorePrompt.setOnClickListener { onNutriScoreButtonClick() }
        binding.newAdd.setOnClickListener { newNutritionImage() }

        refreshView(requireProductState())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun refreshView(productState: ProductState) {
        super.refreshView(productState)
        val langCode = LocaleHelper.getLanguage(requireActivity())
        product = productState.product!!

        checkPrompts()
        showPrompts()

        if (!showNutritionData) {
            binding.imageViewNutrition.visibility = GONE
            binding.addPhotoLabel.visibility = GONE
            binding.imageGradeLayout.visibility = GONE
            binding.calculateNutritionFacts.visibility = GONE
            binding.nutrimentsCardView.visibility = GONE
            binding.textNoNutritionData.visibility = VISIBLE
        }

        val nutriments = product.nutriments
        if (Nutriments.CARBON_FOOTPRINT !in nutriments) {
            binding.textCarbonFootprint.visibility = GONE
        }
        setupNutrientItems(nutriments)

        //checks the flags and accordingly sets the text of the prompt
        showPrompts()

        binding.textNutriScoreInfo.isClickable = true
        binding.textNutriScoreInfo.movementMethod = LinkMovementMethod.getInstance()

        val spannableStringBuilder = SpannableStringBuilder()
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                val customTabsIntent = CustomTabsIntent.Builder().build()
                customTabsIntent.intent.putExtra(
                        "android.intent.extra.REFERRER",
                        Uri.parse("android-app://${this@NutritionProductFragment.requireActivity().packageName}")
                )
                CustomTabActivityHelper.openCustomTab(requireActivity(), customTabsIntent, Uri.parse(getString(R.string.url_nutrient_values)), WebViewFallback())
            }
        }
        spannableStringBuilder.append(getString(R.string.txtNutriScoreInfo))
        spannableStringBuilder.setSpan(clickableSpan, 0, spannableStringBuilder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.textNutriScoreInfo.text = spannableStringBuilder

        var servingSize = product.servingSize
        if (servingSize.isNullOrEmpty()) {
            binding.textServingSize.visibility = GONE
            binding.servingSizeCardView.visibility = GONE
        } else {
            if (sharedPreferences.getString("volumeUnitPreference", "l").equals("oz", true)) {
                servingSize = UnitUtils.getServingInOz(servingSize)
            } else if (servingSize.contains("oz", true) && sharedPreferences.getString("volumeUnitPreference", "l") == "l") {
                servingSize = UnitUtils.getServingInL(servingSize)
            }
            binding.textServingSize.text = bold(getString(R.string.txtServingSize))
            binding.textServingSize.append(" ")
            binding.textServingSize.append(servingSize)
        }

        if (arguments != null) {
            mSendProduct = requireArguments().getSerializable("sendProduct") as SendProduct?
        }

        val nutrimentListItems = arrayListOf<NutrimentListItem>()
        val inVolume = product.isPerServingInLiter()
        binding.textNutrientTxt.setText(if (inVolume != true) R.string.txtNutrientLevel100g else R.string.txtNutrientLevel100ml)

        if (!product.servingSize.isNullOrBlank()) {
            binding.textPerPortion.text = "${getString(R.string.nutriment_serving_size)} ${product.servingSize}"
        } else {
            binding.textPerPortion.visibility = GONE
        }

        if (!product.getImageNutritionUrl(langCode).isNullOrBlank()) {
            binding.addPhotoLabel.visibility = GONE
            binding.newAdd.visibility = VISIBLE

            // Load Image if isLowBatteryMode is false
            if (!isLowBatteryMode) {
                picasso
                        .load(product.getImageNutritionUrl(langCode))
                        .into(binding.imageViewNutrition)
            } else {
                binding.imageViewNutrition.visibility = GONE
            }
            picasso
                    .load(product.getImageNutritionUrl(langCode))
                    .into(binding.imageViewNutrition)
            nutrientsImageUrl = product.getImageNutritionUrl(langCode)
        }

        //useful when this fragment is used in offline saving
        if (mSendProduct != null && mSendProduct!!.imgupload_nutrition.isNotBlank()) {
            binding.addPhotoLabel.visibility = GONE
            nutrientsImageUrl = mSendProduct!!.imgupload_nutrition
            picasso
                    .load(LOCALE_FILE_SCHEME + nutrientsImageUrl).config(Bitmap.Config.RGB_565).into(binding.imageViewNutrition)
        }

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        binding.nutrimentsRecyclerView.setHasFixedSize(true)

        // use a linear layout manager
        val mLayoutManager = LinearLayoutManager(requireActivity())
        binding.nutrimentsRecyclerView.layoutManager = mLayoutManager
        binding.nutrimentsRecyclerView.isNestedScrollingEnabled = false

        // Header hack
        nutrimentListItems.add(NutrimentListItem(inVolume == true))

        // Energy
        val energyKcal = nutriments[Nutriments.ENERGY_KCAL]
        if (energyKcal != null) {
            nutrimentListItems.add(NutrimentListItem(getString(R.string.nutrition_energy_kcal),
                    nutriments.getEnergyKcalValue(false),
                    nutriments.getEnergyKcalValue(true),
                    Units.ENERGY_KCAL,
                    energyKcal.getModifierIfNotDefault()))
        }
        val energyKj = nutriments[Nutriments.ENERGY_KJ]
        if (energyKj != null) {
            nutrimentListItems.add(NutrimentListItem(getString(R.string.nutrition_energy_kj),
                    nutriments.getEnergyKjValue(false),
                    nutriments.getEnergyKjValue(true),
                    Units.ENERGY_KJ,
                    energyKj.getModifierIfNotDefault()))
        }

        // Fat
        val fat2 = nutriments[FAT]
        if (fat2 != null) {
            nutrimentListItems.add(BoldNutrimentListItem(getString(R.string.nutrition_fat),
                    fat2.for100gInUnits,
                    fat2.forServingInUnits,
                    fat2.unit,
                    fat2.getModifierIfNotDefault()))
            nutrimentListItems.addAll(getNutrimentItems(nutriments, Nutriments.FAT_MAP))
        }

        // Carbohydrates
        val carbohydrates = nutriments[Nutriments.CARBOHYDRATES]
        if (carbohydrates != null) {
            nutrimentListItems.add(BoldNutrimentListItem(getString(R.string.nutrition_carbohydrate),
                    carbohydrates.for100gInUnits,
                    carbohydrates.forServingInUnits,
                    carbohydrates.unit,
                    carbohydrates.getModifierIfNotDefault()))
            nutrimentListItems.addAll(getNutrimentItems(nutriments, Nutriments.CARBO_MAP))
        }

        // fiber
        nutrimentListItems.addAll(getNutrimentItems(nutriments, Collections.singletonMap(Nutriments.FIBER, R.string.nutrition_fiber)))

        // Proteins
        val proteins = nutriments[Nutriments.PROTEINS]
        if (proteins != null) {
            nutrimentListItems.add(BoldNutrimentListItem(getString(R.string.nutrition_proteins),
                    proteins.for100gInUnits,
                    proteins.forServingInUnits,
                    proteins.unit,
                    proteins.getModifierIfNotDefault()))
            nutrimentListItems.addAll(getNutrimentItems(nutriments, Nutriments.PROT_MAP))
        }

        // salt and alcohol
        val map = hashMapOf(
                SALT to R.string.nutrition_salt,
                SODIUM to R.string.nutrition_sodium,
                ALCOHOL to R.string.nutrition_alcohol
        )
        nutrimentListItems.addAll(getNutrimentItems(nutriments, map))

        // Vitamins
        if (nutriments.hasVitamins) {
            nutrimentListItems.add(BoldNutrimentListItem(getString(R.string.nutrition_vitamins)))
            nutrimentListItems.addAll(getNutrimentItems(nutriments, Nutriments.VITAMINS_MAP))
        }

        // Minerals
        if (nutriments.hasMinerals) {
            nutrimentListItems.add(BoldNutrimentListItem(getString(R.string.nutrition_minerals)))
            nutrimentListItems.addAll(getNutrimentItems(nutriments, Nutriments.MINERALS_MAP))
        }

        // Show nutrition table and nutrition per portion button if nutritional values are available
        if (nutrimentListItems.size > 1) {
            binding.calculateNutritionFacts.visibility = VISIBLE
            binding.nutrimentsRecyclerView.adapter = NutrimentsGridAdapter(nutrimentListItems)
        }

    }

    private fun setupNutrientItems(nutriments: Nutriments?) {
        val levelItemList = mutableListOf<NutrientLevelItem>()
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

        if (fat == null && salt == null && saturatedFat == null && sugars == null) {
            binding.nutrientLevelsCardView.visibility = GONE
            levelItemList += NutrientLevelItem("", "", "", NO_ID)
            binding.imageGrade.visibility = GONE
        } else {
            // prefetch the uri
            customTabActivityHelper = CustomTabActivityHelper()
            customTabActivityHelper!!.connectionCallback = this
            nutritionScoreUri = Uri.parse(getString(R.string.nutriscore_uri))
            customTabActivityHelper!!.mayLaunchUrl(nutritionScoreUri, null, null)

            val fatNutriment = nutriments!![FAT]
            if (fat != null && fatNutriment != null) {
                val fatNutrimentLevel = fat.getLocalize(requireActivity())
                levelItemList += NutrientLevelItem(
                        getString(R.string.txtFat),
                        fatNutriment.displayStringFor100g,
                        fatNutrimentLevel,
                        fat.getImgRes(),
                )
            }

            val saturatedFatNutriment = nutriments[SATURATED_FAT]
            if (saturatedFat != null && saturatedFatNutriment != null) {
                val saturatedFatLocalize = saturatedFat.getLocalize(requireActivity())
                levelItemList += NutrientLevelItem(
                        getString(R.string.txtSaturatedFat),
                        saturatedFatNutriment.displayStringFor100g,
                        saturatedFatLocalize,
                        saturatedFat.getImgRes(),
                )
            }

            val sugarsNutriment = nutriments[SUGARS]
            if (sugars != null && sugarsNutriment != null) {
                val sugarsLocalize = sugars.getLocalize(requireActivity())
                levelItemList += NutrientLevelItem(
                        getString(R.string.txtSugars),
                        sugarsNutriment.displayStringFor100g,
                        sugarsLocalize,
                        sugars.getImgRes(),
                )
            }

            val saltNutriment = nutriments[SALT]
            if (salt != null && saltNutriment != null) {
                levelItemList += NutrientLevelItem(
                        getString(R.string.txtSalt),
                        saltNutriment.displayStringFor100g,
                        salt.getLocalize(requireActivity()),
                        salt.getImgRes()
                )
            }
            drawNutritionGrade()
        }
        binding.listNutrientLevels.adapter = NutrientLevelListAdapter(requireActivity(), levelItemList)
        binding.listNutrientLevels.layoutManager = LinearLayoutManager(requireActivity())
    }

    private fun drawNutritionGrade() {
        binding.imageGradeLayout.visibility = VISIBLE
        binding.imageGrade.setImageResource(product.getNutriScoreResource())
        binding.imageGrade.setOnClickListener {
            val customTabsIntent = CustomTabsHelper.getCustomTabsIntent(requireContext(), customTabActivityHelper!!.session)
            CustomTabActivityHelper.openCustomTab(requireActivity(), customTabsIntent, nutritionScoreUri!!, WebViewFallback())
        }
    }


    /**
     * Checks the product states_tags to determine which prompt to be shown
     */
    private fun checkPrompts() {
        if (product.statesTags.contains(ApiFields.StateTags.CATEGORIES_TO_BE_COMPLETED)) {
            showCategoryPrompt = true
        }
        if (product.noNutritionData == "on") {
            showNutritionPrompt = false
            showNutritionData = false
        } else if (product.statesTags.contains(ApiFields.StateTags.NUTRITION_FACTS_TO_BE_COMPLETED)) {
            showNutritionPrompt = true
        }
    }

    private fun showPrompts() {
        binding.getNutriscorePrompt.visibility = VISIBLE
        binding.getNutriscorePrompt.text = when {
            showNutritionPrompt && showCategoryPrompt -> getString(R.string.add_nutrient_category_prompt_text)
            showNutritionPrompt -> getString(R.string.add_nutrient_prompt_text)
            showCategoryPrompt -> getString(R.string.add_category_prompt_text)
            else -> {
                binding.getNutriscorePrompt.visibility = GONE
                return
            }
        }
    }

    private fun getNutrimentItems(nutriments: Nutriments, nutrimentMap: Map<String, Int>): List<NutrimentListItem> {
        return nutrimentMap.mapNotNull { (key, value) ->
            val nutriment = nutriments[key] ?: return@mapNotNull null
            NutrimentListItem(
                    getString(value),
                    nutriment.for100gInUnits,
                    nutriment.forServingInUnits,
                    if (value == R.string.ph) "" else nutriment.unit,
                    nutriment.getModifierIfNotDefault(),
            )
        }
    }

    private fun openNutriScoreLink() {
        if (product.nutritionGradeFr == null) return
        val customTabsIntent = CustomTabsHelper.getCustomTabsIntent(requireActivity(), customTabActivityHelper!!.session)
        CustomTabActivityHelper.openCustomTab(requireActivity(), customTabsIntent, nutritionScoreUri!!, WebViewFallback())
    }

    private fun openFullScreen() {
        if (nutrientsImageUrl != null) {
            FullScreenActivityOpener.openForUrl(this, product, ProductImageField.NUTRITION, nutrientsImageUrl, binding.imageViewNutrition)
        } else {
            // take a picture
            if (ContextCompat.checkSelfPermission(requireActivity(), permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
            } else {
                EasyImage.openCamera(this, 0)
            }
        }
    }

    private fun calculateNutritionFacts() {
        val dialog = MaterialDialog.Builder(requireActivity()).run {
            title(R.string.calculate_nutrition_facts)
            customView(R.layout.dialog_calculate_calories, false)
            dismissListener { Utils.hideKeyboard(requireActivity()) }
            build()
        }.apply { show() }
        val dialogView = dialog.customView ?: return

        val etWeight = dialogView.findViewById<EditText>(R.id.edit_text_weight)
        val spinner = dialogView.findViewById<Spinner>(R.id.spinner_weight)
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, i: Int, l: Long) {
                val btn = dialog.findViewById(R.id.txt_calories_result) as Button
                btn.setOnClickListener {
                    val toFloatOrNull = etWeight.text.toString().toFloatOrNull()
                    if (etWeight.text.toString().isEmpty() || toFloatOrNull == null) {
                        Snackbar.make(binding.root, resources.getString(R.string.please_enter_weight), LENGTH_SHORT).show()
                    } else {
                        CalculateDetailsActivity.start(
                                requireActivity(),
                                product,
                                spinner.selectedItem.toString(),
                                toFloatOrNull
                        )
                        dialog.dismiss()
                    }
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) = Unit // We don't care
        }
    }

    private fun loadNutritionPhoto(photoFile: File) {
        // Create a new instance of ProductImage so we can load to server
        val image = ProductImage(product.code, ProductImageField.NUTRITION, photoFile).apply {
            filePath = photoFile.absolutePath
        }

        // Load to server
        api.postImg(image).subscribe().addTo(disp)

        // Load into view
        binding.addPhotoLabel.visibility = GONE
        nutrientsImageUrl = photoFile.absolutePath
        picasso
                .load(photoFile)
                .fit()
                .into(binding.imageViewNutrition)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data)
        if (requestCode == EDIT_PRODUCT_AFTER_LOGIN
                && resultCode == Activity.RESULT_OK
                && requireActivity().isUserSet()) {
            startEditProduct()
        }
        if (ImagesManageActivity.isImageModified(requestCode, resultCode)) {
            (activity as? ProductViewActivity)?.onRefresh()
        }
    }

    private fun newNutritionImage() = doChooseOrTakePhotos(getString(R.string.nutrition_facts_picture))

    override fun doOnPhotosPermissionGranted() = newNutritionImage()

    override fun onCustomTabsConnected() {
        binding.imageGrade.isClickable = true
    }

    override fun onCustomTabsDisconnected() {
        binding.imageGrade.isClickable = false
    }

    private fun onNutriScoreButtonClick() {
        if (!isFlavors(OFF, OBF)) return

        if (requireActivity().isUserSet()) startEditProduct()
        else startLoginToEditAnd(EDIT_PRODUCT_AFTER_LOGIN, requireActivity())
    }

    private fun startEditProduct() = startActivity(
            Intent(requireContext(), ProductEditActivity::class.java).apply {
                putExtra(ProductEditActivity.KEY_EDIT_PRODUCT, product)
                //adds the information about the prompt when navigating the user to the edit the product
                putExtra(ProductEditActivity.KEY_MODIFY_CATEGORY_PROMPT, showCategoryPrompt)
                putExtra(ProductEditActivity.KEY_MODIFY_NUTRITION_PROMPT, showNutritionPrompt)
            },
    )

}