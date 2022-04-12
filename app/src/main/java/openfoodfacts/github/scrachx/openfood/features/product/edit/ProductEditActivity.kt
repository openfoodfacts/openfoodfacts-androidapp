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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import openfoodfacts.github.scrachx.openfood.AppFlavors.OBF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPF
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ActivityEditProductBinding
import openfoodfacts.github.scrachx.openfood.features.product.ProductFragmentPagerAdapter
import openfoodfacts.github.scrachx.openfood.features.product.edit.ingredients.EditIngredientsFragment
import openfoodfacts.github.scrachx.openfood.features.product.edit.nutrition.ProductEditNutritionFactsFragment
import openfoodfacts.github.scrachx.openfood.features.product.edit.overview.EditOverviewFragment
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.models.*
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.repositories.OfflineProductRepository
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.clearCameraCache
import openfoodfacts.github.scrachx.openfood.utils.getProductState
import openfoodfacts.github.scrachx.openfood.utils.hideKeyboard
import javax.inject.Inject

@AndroidEntryPoint
class ProductEditActivity : BaseActivity() {
    private var _binding: ActivityEditProductBinding? = null
    private val binding get() = _binding!!

    private val editViewModel: ProductEditViewModel by viewModels()

    @Inject
    lateinit var productRepository: ProductRepository

    @Inject
    lateinit var offlineRepository: OfflineProductRepository

    @Inject
    lateinit var daoSession: DaoSession

    @Inject
    lateinit var productsApi: ProductsAPI

    private val fragmentsBundle = Bundle()

    private val addProductPhotosFragment = ProductEditPhotosFragment()
    private val nutritionFactsFragment = ProductEditNutritionFactsFragment()
    private val ingredientsFragment = EditIngredientsFragment()
    private val editOverviewFragment = EditOverviewFragment()

    private var editingMode = false

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.product_edit_menu, menu)
        return true
    }

    /**
     * This method is used to update the timeline.
     * 0 means inactive stage, 1 means active stage and 2 means completed stage
     *
     * @param overviewStage change the state of overview indicator
     * @param ingredientsStage change the state of ingredients indicator
     * @param nutritionFactsStage change the state of nutrition facts indicator
     */
    private fun updateTimelineIndicator(overviewStage: Int, ingredientsStage: Int, nutritionFactsStage: Int) {
        binding.overviewIndicator.updateTimeLine(overviewStage)
        binding.ingredientsIndicator.updateTimeLine(ingredientsStage)
        binding.nutritionFactsIndicator.updateTimeLine(nutritionFactsStage)
    }

    override fun onBackPressed() {
        // If the user changed something, alert before exiting
        if (getUpdatedFields().isNotEmpty()) showExitConfirmDialog()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (getUpdatedFields().isNotEmpty()) {
                    showExitConfirmDialog()
                    true
                } else false
            }
            R.id.save_product -> {
                checkFieldsThenSave()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showExitConfirmDialog() {
        MaterialAlertDialogBuilder(this)
            .setMessage(R.string.save_product)
            .setPositiveButton(R.string.txtSave) { _, _ -> checkFieldsThenSave() }
            .setNegativeButton(R.string.txt_discard) { _, _ -> super.onBackPressed() }
            .show()
    }

    private fun selectPage(position: Int) = when (position) {
        1 -> updateTimelineIndicator(2, 1, 0)
        2 -> updateTimelineIndicator(2, 2, 1)
        0 -> updateTimelineIndicator(1, 0, 0)
        else -> updateTimelineIndicator(1, 0, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup view binding
        _binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setTitle(R.string.offline_product_addition_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        editViewModel.sideEffects.observe(this, ::handleSideEffect)

        // Setup onclick listeners
        binding.overviewIndicator.setOnClickListener { switchToOverviewPage() }
        binding.ingredientsIndicator.setOnClickListener { switchToIngredientsPage() }
        binding.nutritionFactsIndicator.setOnClickListener { switchToNutritionFactsPage() }
        binding.viewpager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) = selectPage(position)
        })

        val productState = getProductState()
        var offlineSavedProduct = intent.getSerializableExtra(KEY_EDIT_OFFLINE_PRODUCT) as OfflineSavedProduct?

        val editProduct = intent.getSerializableExtra(KEY_EDIT_PRODUCT) as Product?
        if (intent.getBooleanExtra(KEY_PERFORM_OCR, false)) {
            fragmentsBundle.putBoolean(KEY_PERFORM_OCR, true)
        }
        if (intent.getBooleanExtra(KEY_SEND_UPDATED, false)) {
            fragmentsBundle.putBoolean(KEY_SEND_UPDATED, true)
        }
        if (productState != null) {
            val product = productState.product!!

            editViewModel.setProduct(product)

            // Search if the barcode already exists in the OfflineSavedProducts db
            offlineSavedProduct = runBlocking { offlineRepository.getOfflineProductByBarcode(product.code) }
        }
        if (editProduct != null) {
            setTitle(R.string.edit_product_title)
            editViewModel.setProduct(editProduct)
            editingMode = true
            fragmentsBundle.putBoolean(KEY_IS_EDITING, true)
        } else if (offlineSavedProduct != null) {
            fragmentsBundle.putSerializable(KEY_EDIT_OFFLINE_PRODUCT, offlineSavedProduct)

            // Save the already existing images in productDetails for UI
            editViewModel.updateImagesData {
                pathsMap[ImageType.FRONT] = offlineSavedProduct.imageFront
                pathsMap[ImageType.INGREDIENTS] = offlineSavedProduct.productDetails[ApiFields.Keys.IMAGE_INGREDIENTS]
                pathsMap[ImageType.NUTRITION] = offlineSavedProduct.productDetails[ApiFields.Keys.IMAGE_NUTRITION]

                // get the status of images from productDetailsMap, whether uploaded or not
                uploadedMap[ImageType.FRONT] = offlineSavedProduct.productDetails[ApiFields.Keys.IMAGE_FRONT_UPLOADED].toBoolean()
                uploadedMap[ImageType.INGREDIENTS] = offlineSavedProduct.productDetails[ApiFields.Keys.IMAGE_INGREDIENTS_UPLOADED].toBoolean()
                uploadedMap[ImageType.NUTRITION] = offlineSavedProduct.productDetails[ApiFields.Keys.IMAGE_NUTRITION_UPLOADED].toBoolean()
            }
        }
        if (productState == null && offlineSavedProduct == null && editProduct == null) {
            Toast.makeText(this, R.string.error_adding_product, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setupViewPager(binding.viewpager)
    }

    private fun handleSideEffect(sideEffect: ProductEditViewModel.SideEffect) {
        when (sideEffect) {
            is ProductEditViewModel.SideEffect.NextFragment -> proceed()
            else -> Unit
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        clearCameraCache()
        _binding = null
    }

    private fun setupViewPager(viewPager: ViewPager2) {
        // Initialize fragments
        fragmentsBundle.putSerializable(KEY_PRODUCT, editViewModel.product.value)

        editOverviewFragment.arguments = fragmentsBundle
        ingredientsFragment.arguments = fragmentsBundle

        val adapterResult = ProductFragmentPagerAdapter(this).also {
            it += editOverviewFragment to getString(R.string.overview)
            it += ingredientsFragment to getString(R.string.ingredients)
        }

        // If on off or opff, add Nutrition Facts fragment
        when {
            isFlavors(OFF, OPFF) -> {
                nutritionFactsFragment.arguments = fragmentsBundle
                adapterResult += nutritionFactsFragment to getString(R.string.nutrition_facts)
            }
            isFlavors(OBF, OPF) -> {
                binding.textNutritionFactsIndicator.setText(R.string.photos)
                addProductPhotosFragment.arguments = fragmentsBundle
                adapterResult += addProductPhotosFragment to getString(R.string.photos)
            }
        }

        viewPager.offscreenPageLimit = 2
        viewPager.adapter = adapterResult
    }

    private fun getUpdatedFields(): Fields {
        val updatedValues = mutableFieldsOf().apply {
            putAll(editOverviewFragment.getUpdatedFields())
            putAll(ingredientsFragment.getUpdatedFields())
        }

        if (isFlavors(OFF, OPFF))
            updatedValues += nutritionFactsFragment.getUpdatedFields()

        return updatedValues
    }

    private fun saveProduct() {
        editViewModel.setProductDetails(getUpdatedFields() + productRepository.getUserFields())
        saveProductOffline()
    }

    fun proceed() = if (binding.viewpager.currentItem < 2) {
        binding.viewpager.setCurrentItem(binding.viewpager.currentItem + 1, true)
    } else checkFieldsThenSave()

    /**
     * Save the current product in the offline db
     */
    private fun saveProductOffline() {
        editViewModel.saveProductOffline(editingMode)

        Toast.makeText(this, R.string.productSavedToast, Toast.LENGTH_SHORT).show()
        hideKeyboard()

        setResult(RESULT_OK)
        finish()
    }

    private fun checkFieldsThenSave() {
        if (editingMode) {
            // edit mode, therefore do not check whether front image is empty or not however do check the nutrition facts values.
            if (isFlavors(OFF, OPFF) && nutritionFactsFragment.anyInvalid()) {
                // If there are any invalid field and there is nutrition data, scroll to the nutrition fragment
                binding.viewpager.setCurrentItem(2, true)
                return
            }
        } else {
            // add mode, check if we have required fields
            if (editOverviewFragment.anyInvalid()) {
                binding.viewpager.setCurrentItem(0, true)
                return
            } else if (isFlavors(OFF, OPFF) && nutritionFactsFragment.anyInvalid()) {
                binding.viewpager.setCurrentItem(2, true)
                return
            }
        }

        // If all is correct, save the product
        saveProduct()
    }


    private fun switchToOverviewPage() = binding.viewpager.setCurrentItem(0, true)

    private fun switchToIngredientsPage() = binding.viewpager.setCurrentItem(1, true)

    private fun switchToNutritionFactsPage() = binding.viewpager.setCurrentItem(2, true)


    class PerformOCRContract : ActivityResultContract<Product?, Boolean>() {
        override fun createIntent(context: Context, input: Product?) =
            Intent(context, ProductEditActivity::class.java).apply {
                putExtra(KEY_EDIT_PRODUCT, input)
                putExtra(KEY_PERFORM_OCR, true)
            }

        override fun parseResult(resultCode: Int, intent: Intent?) = resultCode == RESULT_OK
    }

    class SendUpdatedImgContract : ActivityResultContract<Product, Boolean>() {
        override fun createIntent(context: Context, input: Product) =
            Intent(context, ProductEditActivity::class.java).apply {
                putExtra(KEY_EDIT_PRODUCT, input)
                putExtra(KEY_SEND_UPDATED, true)
            }

        override fun parseResult(resultCode: Int, intent: Intent?) = resultCode == RESULT_OK
    }

    open class EditProductContract : ActivityResultContract<Product, Boolean>() {
        override fun createIntent(context: Context, input: Product) =
            Intent(context, ProductEditActivity::class.java).apply {
                putExtra(KEY_EDIT_PRODUCT, input)
            }

        override fun parseResult(resultCode: Int, intent: Intent?) = resultCode == RESULT_OK
    }

    companion object {

        const val KEY_PERFORM_OCR = "perform_ocr"
        const val KEY_SEND_UPDATED = "send_updated"
        const val KEY_MODIFY_NUTRITION_PROMPT = "modify_nutrition_prompt"
        const val KEY_MODIFY_CATEGORY_PROMPT = "modify_category_prompt"

        const val KEY_EDIT_OFFLINE_PRODUCT = "edit_offline_product"
        const val KEY_EDIT_PRODUCT = "edit_product"
        const val KEY_PRODUCT = "product"

        const val KEY_IS_EDITING = "is_edition"
        const val KEY_STATE = "state"


        private fun View.updateTimeLine(stage: Int) {
            when (stage) {
                0 -> setBackgroundResource(R.drawable.stage_inactive)
                1 -> setBackgroundResource(R.drawable.stage_active)
                2 -> setBackgroundResource(R.drawable.stage_complete)
            }
        }

        fun start(
            context: Context,
            product: Product,
            sendUpdated: Boolean = false,
            performOcr: Boolean = false,
            showCategoryPrompt: Boolean = false,
            showNutritionPrompt: Boolean = false
        ) {
            Intent(context, ProductEditActivity::class.java).apply {
                putExtra(KEY_EDIT_PRODUCT, product)

                if (sendUpdated) putExtra(KEY_SEND_UPDATED, true)
                if (performOcr) putExtra(KEY_PERFORM_OCR, true)
                if (showCategoryPrompt) putExtra(KEY_MODIFY_CATEGORY_PROMPT, true)
                if (showNutritionPrompt) putExtra(KEY_MODIFY_NUTRITION_PROMPT, true)

                context.startActivity(this)
            }
        }

        fun start(
            context: Context,
            offlineProduct: OfflineSavedProduct,
            sendUpdated: Boolean = false,
            performOcr: Boolean = false,
            showCategoryPrompt: Boolean = false,
            showNutritionPrompt: Boolean = false
        ) {
            Intent(context, ProductEditActivity::class.java).apply {
                putExtra(KEY_EDIT_OFFLINE_PRODUCT, offlineProduct)

                if (sendUpdated) putExtra(KEY_SEND_UPDATED, true)
                if (performOcr) putExtra(KEY_PERFORM_OCR, true)
                if (showCategoryPrompt) putExtra(KEY_MODIFY_CATEGORY_PROMPT, true)
                if (showNutritionPrompt) putExtra(KEY_MODIFY_NUTRITION_PROMPT, true)

                context.startActivity(this)
            }
        }

    }
}
