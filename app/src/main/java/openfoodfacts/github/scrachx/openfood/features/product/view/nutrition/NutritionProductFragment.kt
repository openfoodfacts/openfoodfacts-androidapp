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
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
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
import io.reactivex.disposables.CompositeDisposable
import openfoodfacts.github.scrachx.openfood.AppFlavors
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
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.features.shared.adapters.NutrientLevelListAdapter
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.*
import openfoodfacts.github.scrachx.openfood.models.entities.SendProduct
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.*
import org.apache.commons.lang.StringUtils
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.util.*

class NutritionProductFragment : BaseFragment(), CustomTabActivityHelper.ConnectionCallback {
    private var disp = CompositeDisposable()
    private var photoReceiverHandler: PhotoReceiverHandler? = null
    private var nutrientsImageUrl: String? = null
    private var barcode: String? = null
    private var api: OpenFoodAPIClient? = null

    /**
     * Boolean to determine if image should be loaded or not
     */
    private var isLowBatteryMode = false
    private var mSendProduct: SendProduct? = null
    private var customTabActivityHelper: CustomTabActivityHelper? = null
    private var nutritionScoreUri: Uri? = null

    /**
     * The following booleans indicate whether the prompts are to be made visible
     */
    private var showNutritionPrompt = false
    private var showCategoryPrompt = false
    override fun onDestroy() {
        disp.dispose()
        _binding = null
        super.onDestroy()
    }

    /**
     * Boolean to determine if nutrition data should be shown
     */
    private var showNutritionData = true
    private lateinit var product: Product
    private lateinit var activityProductState: ProductState
    private var _binding: FragmentNutritionProductBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        api = OpenFoodAPIClient(requireActivity())
        _binding = FragmentNutritionProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoReceiverHandler = PhotoReceiverHandler { photoFile: File -> loadNutritionPhoto(photoFile) }
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
        refreshView(FragmentUtils.requireStateFromArguments(this))
    }

    override fun refreshView(productState: ProductState) {
        super.refreshView(productState)
        val currActivity: Activity = requireActivity()
        val langCode = LocaleHelper.getLanguage(requireActivity())
        activityProductState = productState
        product = productState.product
        checkPrompts()
        showPrompts()
        if (!showNutritionData) {
            binding.imageViewNutrition.visibility = View.GONE
            binding.addPhotoLabel.visibility = View.GONE
            binding.imageGradeLayout.visibility = View.GONE
            binding.calculateNutritionFacts.visibility = View.GONE
            binding.nutrimentsCardView.visibility = View.GONE
            binding.textNoNutritionData.visibility = View.VISIBLE
        }
        val settingsPreference = currActivity.getSharedPreferences("prefs", 0)
        val nutriments = product.nutriments
        if (!nutriments.contains(Nutriments.CARBON_FOOTPRINT)) {
            binding.textCarbonFootprint.visibility = View.GONE
        }
        setupNutrientItems(currActivity, nutriments)
        //checks the flags and accordingly sets the text of the prompt
        showPrompts()
        binding.textNutriScoreInfo.isClickable = true
        binding.textNutriScoreInfo.movementMethod = LinkMovementMethod.getInstance()
        val spannableStringBuilder = SpannableStringBuilder()
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(view: View) {
                val customTabsIntent = CustomTabsIntent.Builder().build()
                customTabsIntent.intent.putExtra("android.intent.extra.REFERRER", Uri.parse("android-app://" + currActivity.packageName))
                CustomTabActivityHelper.openCustomTab(requireActivity(), customTabsIntent, Uri.parse(getString(R.string.url_nutrient_values)), WebViewFallback())
            }
        }
        spannableStringBuilder.append(getString(R.string.txtNutriScoreInfo))
        spannableStringBuilder.setSpan(clickableSpan, 0, spannableStringBuilder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.textNutriScoreInfo.text = spannableStringBuilder
        if (TextUtils.isEmpty(product.servingSize)) {
            binding.textServingSize.visibility = View.GONE
            binding.servingSizeCardView.visibility = View.GONE
        } else {
            var servingSize = product.servingSize
            if (settingsPreference.getString("volumeUnitPreference", "l") == "oz") {
                servingSize = UnitUtils.getServingInOz(servingSize)
            } else if (servingSize.contains("oz", true) && settingsPreference.getString("volumeUnitPreference", "l") == "l") {
                servingSize = UnitUtils.getServingInL(servingSize)
            }
            binding.textServingSize.text = Utils.bold(getString(R.string.txtServingSize))
            binding.textServingSize.append(" ")
            binding.textServingSize.append(servingSize)
        }
        if (Utils.isDisableImageLoad(requireContext()) && Utils.isBatteryLevelLow(requireContext())) {
            isLowBatteryMode = true
        }
        if (arguments != null) {
            mSendProduct = requireArguments().getSerializable("sendProduct") as SendProduct?
        }
        barcode = product.code
        val nutrimentListItems: MutableList<NutrimentListItem> = ArrayList()
        val inVolume = ProductUtils.isPerServingInLiter(product)
        binding.textNutrientTxt.setText(if (inVolume) R.string.txtNutrientLevel100ml else R.string.txtNutrientLevel100g)
        if (StringUtils.isNotBlank(product.servingSize)) {
            binding.textPerPortion.text = "${getString(R.string.nutriment_serving_size)} ${product.servingSize}"
        } else {
            binding.textPerPortion.visibility = View.GONE
        }
        if (StringUtils.isNotBlank(product.getImageNutritionUrl(langCode))) {
            binding.addPhotoLabel.visibility = View.GONE
            binding.newAdd.visibility = View.VISIBLE

            // Load Image if isLowBatteryMode is false
            if (!isLowBatteryMode) {
                Utils.picassoBuilder(context)
                        .load(product.getImageNutritionUrl(langCode))
                        .into(binding.imageViewNutrition)
            } else {
                binding.imageViewNutrition.visibility = View.GONE
            }
            Utils.picassoBuilder(context)
                    .load(product.getImageNutritionUrl(langCode))
                    .into(binding.imageViewNutrition)
            nutrientsImageUrl = product.getImageNutritionUrl(langCode)
        }

        //useful when this fragment is used in offline saving
        if (mSendProduct != null && StringUtils.isNotBlank(mSendProduct!!.imgupload_nutrition)) {
            binding.addPhotoLabel.visibility = View.GONE
            nutrientsImageUrl = mSendProduct!!.imgupload_nutrition
            Picasso.get().load(FileUtils.LOCALE_FILE_SCHEME + nutrientsImageUrl).config(Bitmap.Config.RGB_565).into(binding.imageViewNutrition)
        }

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        binding.nutrimentsRecyclerView.setHasFixedSize(true)

        // use a linear layout manager
        val mLayoutManager = LinearLayoutManager(currActivity)
        binding.nutrimentsRecyclerView.layoutManager = mLayoutManager
        binding.nutrimentsRecyclerView.isNestedScrollingEnabled = false

        // Header hack
        nutrimentListItems.add(NutrimentListItem(inVolume))

        // Energy
        val energyKcal = nutriments[Nutriments.ENERGY_KCAL]
        if (energyKcal != null) {
            nutrimentListItems.add(
                    NutrimentListItem(getString(R.string.nutrition_energy_kcal),
                            nutriments.getEnergyKcalValue(false),
                            nutriments.getEnergyKcalValue(true),
                            Units.ENERGY_KCAL,
                            nutriments.getModifierIfNotDefault(Nutriments.ENERGY_KCAL)))
        }
        val energyKj = nutriments[Nutriments.ENERGY_KJ]
        if (energyKj != null) {
            nutrimentListItems.add(
                    NutrimentListItem(getString(R.string.nutrition_energy_kj),
                            nutriments.getEnergyKjValue(false),
                            nutriments.getEnergyKjValue(true),
                            Units.ENERGY_KJ,
                            nutriments.getModifierIfNotDefault(Nutriments.ENERGY_KJ)))
        }

        // Fat
        val fat2 = nutriments[Nutriments.FAT]
        if (fat2 != null) {
            nutrimentListItems.add(HeaderNutrimentListItem(getString(R.string.nutrition_fat),
                    fat2.for100gInUnits,
                    fat2.forServingInUnits,
                    fat2.unit,
                    nutriments.getModifierIfNotDefault(Nutriments.FAT)))
            nutrimentListItems.addAll(getNutrimentItems(nutriments, Nutriments.FAT_MAP))
        }

        // Carbohydrates
        val carbohydrates = nutriments[Nutriments.CARBOHYDRATES]
        if (carbohydrates != null) {
            nutrimentListItems.add(HeaderNutrimentListItem(getString(R.string.nutrition_carbohydrate),
                    carbohydrates.for100gInUnits,
                    carbohydrates.forServingInUnits,
                    carbohydrates.unit,
                    nutriments.getModifierIfNotDefault(Nutriments.CARBOHYDRATES)))
            nutrimentListItems.addAll(getNutrimentItems(nutriments, Nutriments.CARBO_MAP))
        }

        // fiber
        nutrimentListItems.addAll(getNutrimentItems(nutriments, Collections.singletonMap(Nutriments.FIBER, R.string.nutrition_fiber)))

        // Proteins
        val proteins = nutriments[Nutriments.PROTEINS]
        if (proteins != null) {
            nutrimentListItems.add(HeaderNutrimentListItem(getString(R.string.nutrition_proteins),
                    proteins.for100gInUnits,
                    proteins.forServingInUnits,
                    proteins.unit,
                    nutriments.getModifierIfNotDefault(Nutriments.PROTEINS)))
            nutrimentListItems.addAll(getNutrimentItems(nutriments, Nutriments.PROT_MAP))
        }

        // salt and alcohol
        val map: MutableMap<String, Int> = HashMap()
        map[Nutriments.SALT] = R.string.nutrition_salt
        map[Nutriments.SODIUM] = R.string.nutrition_sodium
        map[Nutriments.ALCOHOL] = R.string.nutrition_alcohol
        nutrimentListItems.addAll(getNutrimentItems(nutriments, map))

        // Vitamins
        if (nutriments.hasVitamins()) {
            nutrimentListItems.add(HeaderNutrimentListItem(getString(R.string.nutrition_vitamins)))
            nutrimentListItems.addAll(getNutrimentItems(nutriments, Nutriments.VITAMINS_MAP))
        }

        // Minerals
        if (nutriments.hasMinerals()) {
            nutrimentListItems.add(HeaderNutrimentListItem(getString(R.string.nutrition_minerals)))
            nutrimentListItems.addAll(getNutrimentItems(nutriments, Nutriments.MINERALS_MAP))
        }
        binding.nutrimentsRecyclerView.adapter = NutrimentsGridAdapter(nutrimentListItems)
    }

    private fun setupNutrientItems(currActivity: Activity, nutriments: Nutriments?) {
        val levelItemList: MutableList<NutrientLevelItem> = ArrayList()
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
            binding.nutrientLevelsCardView.visibility = View.GONE
            levelItemList.add(NutrientLevelItem("", "", "", 0))
            binding.imageGrade.visibility = View.GONE
        } else {
            // prefetch the uri
            customTabActivityHelper = CustomTabActivityHelper()
            customTabActivityHelper!!.setConnectionCallback(this)
            nutritionScoreUri = Uri.parse(getString(R.string.nutriscore_uri))
            customTabActivityHelper!!.mayLaunchUrl(nutritionScoreUri, null, null)
            val fatNutriment = nutriments!![Nutriments.FAT]
            if (fat != null && fatNutriment != null) {
                val fatNutrimentLevel = fat.getLocalize(currActivity)
                levelItemList.add(NutrientLevelItem(getString(R.string.txtFat),
                        fatNutriment.displayStringFor100g,
                        fatNutrimentLevel,
                        fat.imageLevel))
            }
            val saturatedFatNutriment = nutriments[Nutriments.SATURATED_FAT]
            if (saturatedFat != null && saturatedFatNutriment != null) {
                val saturatedFatLocalize = saturatedFat.getLocalize(currActivity)
                levelItemList.add(NutrientLevelItem(getString(R.string.txtSaturatedFat),
                        saturatedFatNutriment.displayStringFor100g,
                        saturatedFatLocalize,
                        saturatedFat.imageLevel))
            }
            val sugarsNutriment = nutriments[Nutriments.SUGARS]
            if (sugars != null && sugarsNutriment != null) {
                val sugarsLocalize = sugars.getLocalize(currActivity)
                levelItemList.add(NutrientLevelItem(getString(R.string.txtSugars),
                        sugarsNutriment.displayStringFor100g,
                        sugarsLocalize,
                        sugars.imageLevel))
            }
            val saltNutriment = nutriments[Nutriments.SALT]
            if (salt != null && saltNutriment != null) {
                val saltLocalize = salt.getLocalize(currActivity)
                levelItemList.add(NutrientLevelItem(getString(R.string.txtSalt),
                        saltNutriment.displayStringFor100g,
                        saltLocalize,
                        salt.imageLevel))
            }
            drawNutritionGrade()
        }
        binding.listNutrientLevels.adapter = NutrientLevelListAdapter(currActivity, levelItemList)
        binding.listNutrientLevels.layoutManager = LinearLayoutManager(currActivity)
    }

    private fun drawNutritionGrade() {
        val nutritionGrade = Utils.getImageGradeDrawable(requireActivity(), product)
        if (nutritionGrade != null) {
            binding.imageGradeLayout.visibility = View.VISIBLE
            binding.imageGrade.setImageDrawable(nutritionGrade)
            binding.imageGrade.setOnClickListener {
                val customTabsIntent = CustomTabsHelper.getCustomTabsIntent(requireContext(), customTabActivityHelper!!.session)
                CustomTabActivityHelper.openCustomTab(requireActivity(), customTabsIntent, nutritionScoreUri!!, WebViewFallback())
            }
        } else {
            binding.imageGradeLayout.visibility = View.GONE
        }
    }

    /**
     * Checks the product states_tags to determine which prompt to be shown
     */
    private fun checkPrompts() {
        val statesTags = product.statesTags
        if (statesTags.contains("en:categories-to-be-completed")) {
            showCategoryPrompt = true
        }
        if (product.noNutritionData != null && product.noNutritionData == "on") {
            showNutritionPrompt = false
            showNutritionData = false
        } else {
            if (statesTags.contains("en:nutrition-facts-to-be-completed")) {
                showNutritionPrompt = true
            }
        }
    }

    private fun showPrompts() {
        if (showNutritionPrompt || showCategoryPrompt) {
            binding.getNutriscorePrompt.visibility = View.VISIBLE
            if (showNutritionPrompt && showCategoryPrompt) {
                binding.getNutriscorePrompt.text = getString(R.string.add_nutrient_category_prompt_text)
            } else if (showNutritionPrompt) {
                binding.getNutriscorePrompt.text = getString(R.string.add_nutrient_prompt_text)
            } else if (showCategoryPrompt) {
                binding.getNutriscorePrompt.text = getString(R.string.add_category_prompt_text)
            }
        }
    }

    private fun getNutrimentItems(nutriments: Nutriments, nutrimentMap: Map<String, Int>): List<NutrimentListItem> {
        val items: MutableList<NutrimentListItem> = ArrayList()
        for ((key, value) in nutrimentMap) {
            val nutriment = nutriments[key] ?: continue
            items.add(NutrimentListItem(getString(value),
                    nutriment.for100gInUnits,
                    nutriment.forServingInUnits,
                    if (value == R.string.ph) "" else nutriment.unit,
                    nutriments.getModifierIfNotDefault(key)))
        }
        return items
    }

    private fun openNutriScoreLink() {
        if (product.nutritionGradeFr == null) {
            return
        }
        val customTabsIntent = CustomTabsHelper.getCustomTabsIntent(requireActivity(), customTabActivityHelper!!.session)
        CustomTabActivityHelper.openCustomTab(requireActivity(), customTabsIntent, nutritionScoreUri!!, WebViewFallback())
    }

    private fun openFullScreen() {
        if (nutrientsImageUrl != null) {
            FullScreenActivityOpener.openForUrl(this, product, ProductImageField.NUTRITION, nutrientsImageUrl, binding.imageViewNutrition)
        } else {
            // take a picture
            if (ContextCompat.checkSelfPermission(requireActivity(), permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission.CAMERA), Utils.MY_PERMISSIONS_REQUEST_CAMERA)
            } else {
                EasyImage.openCamera(this, 0)
            }
        }
    }

    private fun calculateNutritionFacts() {
        val dialog = with(MaterialDialog.Builder(requireActivity())) {
            title(R.string.calculate_nutrition_facts)
            customView(R.layout.dialog_calculate_calories, false)
            dismissListener { Utils.hideKeyboard(requireActivity()) }
            build()
        }.apply { show() }
        val dialogView = dialog.customView
        if (dialogView != null) {
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
                            CalculateDetailsActivity.start(requireActivity(),
                                    activityProductState.product,
                                    spinner.selectedItem.toString(),
                                    toFloatOrNull)
                            dialog.dismiss()
                        }
                    }
                }

                override fun onNothingSelected(adapterView: AdapterView<*>?) {
                    // We don't care
                }
            }
        }
    }

    private fun loadNutritionPhoto(photoFile: File) {
        // Create a new instance of ProductImage so we can load to server
        val image = ProductImage(barcode, ProductImageField.NUTRITION, photoFile)
        image.filePath = photoFile.absolutePath
        // Load to server
        disp.add(api!!.postImg(image).subscribe())
        // Load into view
        binding.addPhotoLabel.visibility = View.GONE
        nutrientsImageUrl = photoFile.absolutePath
        Picasso.get()
                .load(photoFile)
                .fit()
                .into(binding.imageViewNutrition)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoReceiverHandler!!.onActivityResult(this, requestCode, resultCode, data)
        if (requestCode == EDIT_PRODUCT_AFTER_LOGIN_REQUEST_CODE && resultCode == Activity.RESULT_OK && isUserLoggedIn) {
            startEditProduct()
        }
        if (ImagesManageActivity.isImageModified(requestCode, resultCode)
                && activity is ProductViewActivity) {
            (activity as ProductViewActivity?)!!.onRefresh()
        }
    }

    private fun newNutritionImage() {
        doChooseOrTakePhotos(getString(R.string.nutrition_facts_picture))
    }

    override fun doOnPhotosPermissionGranted() {
        newNutritionImage()
    }

    override fun onCustomTabsConnected() {
        binding.imageGrade.isClickable = true
    }

    override fun onCustomTabsDisconnected() {
        binding.imageGrade.isClickable = false
    }

    private fun onNutriScoreButtonClick() {
        if (AppFlavors.isFlavors(AppFlavors.OFF, AppFlavors.OBF)) {
            if (isUserNotLoggedIn) {
                Utils.startLoginToEditAnd(EDIT_PRODUCT_AFTER_LOGIN_REQUEST_CODE, requireActivity())
            } else {
                startEditProduct()
            }
        }
    }

    private fun startEditProduct() {
        Intent(activity, ProductEditActivity::class.java).apply {
            putExtra(ProductEditActivity.KEY_EDIT_PRODUCT, product)
            //adds the information about the prompt when navigating the user to the edit the product
            putExtra(ProductEditActivity.MODIFY_CATEGORY_PROMPT, showCategoryPrompt)
            putExtra(ProductEditActivity.MODIFY_NUTRITION_PROMPT, showNutritionPrompt)
            startActivity(this)
        }
    }

    companion object {
        private const val EDIT_PRODUCT_AFTER_LOGIN_REQUEST_CODE = 1
    }
}