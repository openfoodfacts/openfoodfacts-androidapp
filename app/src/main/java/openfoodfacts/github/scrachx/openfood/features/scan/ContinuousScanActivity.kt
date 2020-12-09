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
package openfoodfacts.github.scrachx.openfood.features.scan

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Camera
import android.os.Bundle
import android.os.PersistableBundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.mikepenz.iconics.IconicsColor
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.IconicsSize
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.databinding.ActivityContinuousScanBinding
import openfoodfacts.github.scrachx.openfood.features.ImagesManageActivity
import openfoodfacts.github.scrachx.openfood.features.compare.ProductCompareActivity
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.install
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity.ShowIngredientsAction
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewFragment.Companion.newInstance
import openfoodfacts.github.scrachx.openfood.features.product.view.ingredients_analysis.IngredientsWithTagDialogFragment.Companion.newInstance
import openfoodfacts.github.scrachx.openfood.features.product.view.summary.IngredientAnalysisTagsAdapter
import openfoodfacts.github.scrachx.openfood.features.product.view.summary.SummaryProductPresenter
import openfoodfacts.github.scrachx.openfood.features.product.view.summary.SummaryProductPresenterView
import openfoodfacts.github.scrachx.openfood.models.InvalidBarcodeDao
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProductDao
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenHelper
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfig
import openfoodfacts.github.scrachx.openfood.models.eventbus.ProductNeedsRefreshEvent
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class ContinuousScanActivity : AppCompatActivity() {
    private lateinit var beepManager: BeepManager
    private var _binding: ActivityContinuousScanBinding? = null
    private val binding get() = _binding!!
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private val barcodeInputListener: OnEditorActionListener = BarcodeInputListener()
    private val barcodeScanCallback: BarcodeCallback = BarcodeScannerCallback()
    private lateinit var bottomSheetCallback: BottomSheetCallback
    private var cameraState = 0
    private lateinit var client: OpenFoodAPIClient
    private lateinit var errorDrawable: VectorDrawableCompat
    private var productDisp: Disposable? = null
    private var isAnalysisTagsEmpty = true
    private var lastBarcode: String? = null
    private var autoFocusActive = false
    private var beepActive = false
    private var mInvalidBarcodeDao: InvalidBarcodeDao? = null
    private var mOfflineSavedProductDao: OfflineSavedProductDao? = null
    private var offlineSavedProduct: OfflineSavedProduct? = null
    private var product: Product? = null
    private var productViewFragment: ProductViewFragment? = null
    private var cameraPref: SharedPreferences? = null
    private var peekLarge = 0
    private var peekSmall = 0
    private var popupMenu: PopupMenu? = null
    private var productShowing = false
    private var flashActive = false
    private var summaryProductPresenter: SummaryProductPresenter? = null
    private var hintBarcodeDisp: Disposable? = null
    private var commonDisp: CompositeDisposable? = null
    private val productActivityResultLauncher = registerForActivityResult(
            StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            lastBarcode?.let { setShownProduct(it) }
        }
    }

    /**
     * Used by screenshot tests.
     *
     * @param barcode barcode to serach
     */
    fun showProduct(barcode: String) {
        productShowing = true
        binding.barcodeScanner.visibility = View.GONE
        binding.barcodeScanner.pause()
        binding.imageForScreenshotGenerationOnly.visibility = View.VISIBLE
        setShownProduct(barcode)
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        productDisp?.dispose()

        super.onSaveInstanceState(outState, outPersistentState)
    }

    /**
     * Makes network call and search for the product in the database
     *
     * @param barcode Barcode to be searched
     */
    private fun setShownProduct(barcode: String) {
        if (isFinishing) {
            return
        }
        // Dispose the previous call if not ended.
        productDisp?.dispose()


        summaryProductPresenter?.dispose()


        // First, try to show if we have an offline saved product in the db
        offlineSavedProduct = OfflineProductService.getOfflineProductByBarcode(barcode)
        offlineSavedProduct?.let { showOfflineSavedDetails(it) }

        // Then query the online db
        productDisp = client.getProductStateFull(barcode, Utils.HEADER_USER_AGENT_SCAN)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    hideAllViews()
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    binding.quickView.setOnClickListener(null)
                    binding.quickViewProgress.visibility = View.VISIBLE
                    binding.quickViewProgressText.visibility = View.VISIBLE
                    binding.quickViewProgressText.text = getString(R.string.loading_product, barcode)
                }
                .subscribe({ productState: ProductState ->
                    //clear product tags
                    isAnalysisTagsEmpty = true
                    binding.quickViewTags.adapter = null
                    binding.quickViewProgress.visibility = View.GONE
                    binding.quickViewProgressText.visibility = View.GONE
                    if (productState.status == 0L) {
                        tryDisplayOffline(offlineSavedProduct, barcode, R.string.product_not_found)
                    } else {
                        val product = productState.product!!
                        this.product = product

                        // If we're here from comparison -> add product, return to comparison activity
                        if (intent.getBooleanExtra(ProductCompareActivity.KEY_COMPARE_PRODUCT, false)) {
                            startActivity(Intent(this@ContinuousScanActivity, ProductCompareActivity::class.java).apply {
                                putExtra(ProductCompareActivity.KEY_PRODUCT_FOUND, true)

                                val productsToCompare = intent.extras!!.getSerializable(ProductCompareActivity.KEY_PRODUCTS_TO_COMPARE) as ArrayList<Product>
                                if (productsToCompare.contains(product)) {
                                    putExtra(ProductCompareActivity.KEY_PRODUCT_ALREADY_EXISTS, true)
                                } else {
                                    productsToCompare.add(product)
                                }
                                putExtra(ProductCompareActivity.KEY_PRODUCTS_TO_COMPARE, productsToCompare)
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            })
                        }

                        // Add product to scan history
                        productDisp = client.addToHistory(product).subscribeOn(Schedulers.io()).subscribe()
                        showAllViews()
                        binding.txtProductCallToAction.let {
                            it.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                            it.background = ContextCompat.getDrawable(this, R.drawable.rounded_quick_view_text)
                            it.setText(if (isProductIncomplete) R.string.product_not_complete else R.string.scan_tooltip)
                            it.visibility = View.VISIBLE
                        }
                        setupSummary(product)
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        showProductFullScreen()
                        binding.quickViewProductNotFound.visibility = View.GONE
                        binding.quickViewProductNotFoundButton.visibility = View.GONE

                        // Set product name, prefer offline
                        if (offlineSavedProduct != null && !offlineSavedProduct?.name.isNullOrEmpty()) {
                            binding.quickViewName.text = offlineSavedProduct!!.name
                        } else if (product.productName == null || product.productName == "") {
                            binding.quickViewName.setText(R.string.productNameNull)
                        } else {
                            binding.quickViewName.text = product.productName
                        }

                        // Set product additives
                        val addTags = product.additivesTags
                        binding.quickViewAdditives.text = when {
                            addTags.isNotEmpty() -> resources.getQuantityString(R.plurals.productAdditives, addTags.size, addTags.size)

                            product.statesTags.contains("en:ingredients-completed") -> getString(R.string.productAdditivesNone)

                            else -> getString(R.string.productAdditivesUnknown)
                        }

                        // Show nutriscore in quickView only if app flavour is OFF and the product has one
                        if (isFlavors(AppFlavors.OFF) && product.getNutritionGradeTag() != null) {
                            if (getNutriScoreDrawable(product.getNutritionGradeTag()) != Utils.NO_DRAWABLE_RESOURCE) {
                                binding.quickViewNutriScore.visibility = View.VISIBLE
                                binding.quickViewNutriScore.setImageResource(getNutriScoreDrawable(product.nutritionGradeFr))
                            } else {
                                binding.quickViewNutriScore.visibility = View.INVISIBLE
                            }
                        } else {
                            binding.quickViewNutriScore.visibility = View.GONE
                        }

                        // Show nova group in quickView only if app flavour is OFF and the product has one
                        if (isFlavors(AppFlavors.OFF) && product.novaGroups != null) {
                            val novaGroupDrawable = product.getNovaGroupDrawable()
                            if (novaGroupDrawable != Utils.NO_DRAWABLE_RESOURCE) {
                                binding.quickViewNovaGroup.visibility = View.VISIBLE
                                binding.quickViewAdditives.visibility = View.VISIBLE
                                binding.quickViewNovaGroup.setImageResource(novaGroupDrawable)
                            } else {
                                binding.quickViewNovaGroup.visibility = View.INVISIBLE
                            }
                        } else {
                            binding.quickViewNovaGroup.visibility = View.GONE
                        }

                        // If the product has an ecoscore, show it instead of the CO2 icon
                        binding.quickViewEcoscoreIcon.visibility = View.GONE
                        binding.quickViewCo2Icon.visibility = View.GONE
                        val ecoScoreRes = product.getEcoscoreDrawable()
                        val co2Res = product.getCO2Drawable()
                        if (ecoScoreRes != Utils.NO_DRAWABLE_RESOURCE) {
                            binding.quickViewEcoscoreIcon.setImageResource(ecoScoreRes)
                            binding.quickViewEcoscoreIcon.visibility = View.VISIBLE
                        } else if (co2Res != Utils.NO_DRAWABLE_RESOURCE) {
                            binding.quickViewCo2Icon.setImageResource(co2Res)
                            binding.quickViewCo2Icon.visibility = View.VISIBLE
                        }

                        // Create the product view fragment and add it to the layout
                        val newProductViewFragment = newInstance(productState)
                        supportFragmentManager
                                .beginTransaction()
                                .replace(R.id.frame_layout, newProductViewFragment)
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                .commit()
                        productViewFragment = newProductViewFragment
                    }
                }) { e: Throwable ->
                    try {
                        // A network error happened
                        if (e is IOException) {
                            hideAllViews()
                            val offlineSavedProduct = mOfflineSavedProductDao!!.queryBuilder()
                                    .where(OfflineSavedProductDao.Properties.Barcode.eq(barcode))
                                    .unique()
                            tryDisplayOffline(offlineSavedProduct, barcode, R.string.addProductOffline)
                            binding.quickView.setOnClickListener { navigateToProductAddition(barcode) }
                        } else {
                            binding.quickViewProgress.visibility = View.GONE
                            binding.quickViewProgressText.visibility = View.GONE
                            val errorMessage = Toast.makeText(this, R.string.txtConnectionError, Toast.LENGTH_LONG)
                            errorMessage.setGravity(Gravity.CENTER, 0, 0)
                            errorMessage.show()
                            Log.i(LOG_TAG, e.message, e)
                        }
                    } catch (err: Exception) {
                        Log.w(LOG_TAG, err.message, err)
                    }
                }
    }

    private fun tryDisplayOffline(offlineSavedProduct: OfflineSavedProduct?, barcode: String, @StringRes errorMsg: Int) {
        if (offlineSavedProduct != null) {
            showOfflineSavedDetails(offlineSavedProduct)
        } else {
            showProductNotFound(getString(errorMsg, barcode))
        }
    }

    private fun setupSummary(product: Product?) {
        binding.callToActionImageProgress.visibility = View.VISIBLE
        summaryProductPresenter = SummaryProductPresenter(product!!, object : SummaryProductPresenterView() {
            override fun showAllergens(allergens: List<AllergenName>) {
                val data = AllergenHelper.computeUserAllergen(product, allergens)
                binding.callToActionImageProgress.visibility = View.GONE
                if (data.isEmpty) {
                    return
                }
                val iconicsDrawable = IconicsDrawable(this@ContinuousScanActivity, GoogleMaterial.Icon.gmd_warning)
                        .color(IconicsColor.colorInt(ContextCompat.getColor(this@ContinuousScanActivity, R.color.white)))
                        .size(IconicsSize.dp(24))
                binding.txtProductCallToAction.setCompoundDrawablesWithIntrinsicBounds(iconicsDrawable, null, null, null)
                binding.txtProductCallToAction.background = ContextCompat.getDrawable(this@ContinuousScanActivity, R.drawable.rounded_quick_view_text_warn)
                if (data.isIncomplete) {
                    binding.txtProductCallToAction.setText(R.string.product_incomplete_message)
                } else {
                    binding.txtProductCallToAction.text =
                            "${resources.getString(R.string.product_allergen_prompt)}\n${data.allergens.joinToString(", ")}"
                }
            }

            override fun showAnalysisTags(analysisTags: List<AnalysisTagConfig>) {
                super.showAnalysisTags(analysisTags)
                if (analysisTags.isEmpty()) {
                    binding.quickViewTags.visibility = View.GONE
                    isAnalysisTagsEmpty = true
                    return
                }
                binding.quickViewTags.visibility = View.VISIBLE
                isAnalysisTagsEmpty = false
                val adapter = IngredientAnalysisTagsAdapter(this@ContinuousScanActivity, analysisTags)
                adapter.setOnItemClickListener { view: View?, _: Int ->
                    if (view == null) return@setOnItemClickListener
                    val fragment = newInstance(product, view.getTag(R.id.analysis_tag_config) as AnalysisTagConfig)
                    fragment.show(supportFragmentManager, "fragment_ingredients_with_tag")
                    fragment.onDismissListener =  { adapter.filterVisibleTags() }
                }

                binding.quickViewTags.adapter = adapter
            }
        })
        summaryProductPresenter!!.loadAllergens { binding.callToActionImageProgress.visibility = View.GONE }
        summaryProductPresenter!!.loadAnalysisTags()
    }

    private fun showProductNotFound(text: String) {
        hideAllViews()
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        binding.quickView.setOnClickListener { lastBarcode?.let { code -> navigateToProductAddition(code) } }
        binding.quickViewProductNotFound.text = text
        binding.quickViewProductNotFound.visibility = View.VISIBLE
        binding.quickViewProductNotFoundButton.visibility = View.VISIBLE
        binding.quickViewProductNotFoundButton.setOnClickListener { lastBarcode?.let { code -> navigateToProductAddition(code) } }
    }

    private fun showProductFullScreen() {
        bottomSheetBehavior.peekHeight = peekLarge
        binding.quickView.let {
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            it.requestLayout()
            it.rootView.requestLayout()
        }
    }

    private fun showOfflineSavedDetails(offlineSavedProduct: OfflineSavedProduct) {
        showAllViews()
        val pName = offlineSavedProduct.name
        if (!TextUtils.isEmpty(pName)) {
            binding.quickViewName.text = pName
        } else {
            binding.quickViewName.setText(R.string.productNameNull)
        }
        binding.txtProductCallToAction.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.txtProductCallToAction.background = ContextCompat.getDrawable(this@ContinuousScanActivity, R.drawable.rounded_quick_view_text)
        binding.txtProductCallToAction.setText(R.string.product_not_complete)
        binding.txtProductCallToAction.visibility = View.VISIBLE
        binding.quickViewSlideUpIndicator.visibility = View.GONE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun navigateToProductAddition(productBarcode: String) {
        val pd = Product()
        pd.code = productBarcode
        navigateToProductAddition(pd)
    }

    private fun navigateToProductAddition(product: Product?) {
        val intent = Intent(this@ContinuousScanActivity, ProductEditActivity::class.java)
        intent.putExtra(ProductEditActivity.KEY_EDIT_PRODUCT, product)
        productActivityResultLauncher.launch(intent)
    }

    private fun showAllViews() {
        binding.quickViewSlideUpIndicator.visibility = View.VISIBLE
        binding.quickViewName.visibility = View.VISIBLE
        binding.frameLayout.visibility = View.VISIBLE
        binding.quickViewAdditives.visibility = View.VISIBLE
        if (!isAnalysisTagsEmpty) {
            binding.quickViewTags.visibility = View.VISIBLE
        } else {
            binding.quickViewTags.visibility = View.GONE
        }
    }

    private fun hideAllViews() {
        binding.quickViewSearchByBarcode.visibility = View.GONE
        binding.quickViewProgress.visibility = View.GONE
        binding.quickViewProgressText.visibility = View.GONE
        binding.quickViewSlideUpIndicator.visibility = View.GONE
        binding.quickViewName.visibility = View.GONE
        binding.frameLayout.visibility = View.GONE
        binding.quickViewAdditives.visibility = View.GONE
        binding.quickViewNutriScore.visibility = View.GONE
        binding.quickViewNovaGroup.visibility = View.GONE
        binding.quickViewCo2Icon.visibility = View.GONE
        binding.quickViewProductNotFound.visibility = View.GONE
        binding.quickViewProductNotFoundButton.visibility = View.GONE
        binding.txtProductCallToAction.visibility = View.GONE
        binding.quickViewTags.visibility = View.GONE
    }

    override fun onDestroy() {
        if (summaryProductPresenter != null) {
            summaryProductPresenter!!.dispose()
        }

        // Dispose all RxJava disposable
        if (productDisp != null) {
            productDisp!!.dispose()
        }
        if (hintBarcodeDisp != null) {
            hintBarcodeDisp!!.dispose()
        }
        commonDisp!!.dispose()

        // Remove bottom sheet callback as it uses binding
        bottomSheetBehavior.removeBottomSheetCallback(bottomSheetCallback)
        _binding = null
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    override fun onPause() {
        binding.barcodeScanner.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        selectNavigationItem(binding.bottomNavigation.bottomNavigation, R.id.scan_bottom_nav)
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            binding.barcodeScanner.resume()
        }
    }

    @Subscribe
    fun onEventBusProductNeedsRefreshEvent(event: ProductNeedsRefreshEvent) {
        val lastBarcode = lastBarcode ?: return
        if (event.barcode == lastBarcode) {
            runOnUiThread { setShownProduct(lastBarcode) }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        //status bar will remain visible if user presses home and then reopens the activity
        // hence hiding status bar again
        hideSystemUI()
    }

    private fun hideSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        val actionBar = actionBar
        actionBar?.hide()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        OFFApplication.appComponent.inject(this)
        client = OpenFoodAPIClient(this)
        commonDisp = CompositeDisposable()
        super.onCreate(savedInstanceState)
        _binding = ActivityContinuousScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toggleFlash.setOnClickListener { toggleFlash() }
        binding.buttonMore.setOnClickListener { showMoreSettings() }
        val actionBar = actionBar
        actionBar?.hide()
        peekLarge = resources.getDimensionPixelSize(R.dimen.scan_summary_peek_large)
        peekSmall = resources.getDimensionPixelSize(R.dimen.scan_summary_peek_small)
        errorDrawable = VectorDrawableCompat.create(resources, R.drawable.ic_product_silhouette, null)!!
        binding.quickViewTags.isNestedScrollingEnabled = false
        val decorView = window.decorView
        decorView.setOnSystemUiVisibilityChangeListener { visibility: Int ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                // The system bars are visible.
                hideSystemUI()
            }
        }
        hintBarcodeDisp = Completable.timer(15, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    if (productShowing) {
                        return@doOnComplete
                    }
                    hideAllViews()
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    binding.quickViewSearchByBarcode.visibility = View.VISIBLE
                    binding.quickViewSearchByBarcode.requestFocus()
                }.subscribe()
        bottomSheetBehavior = BottomSheetBehavior.from(binding.quickView)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetCallback = QuickViewCallback()
        bottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        mInvalidBarcodeDao = Utils.daoSession.invalidBarcodeDao
        mOfflineSavedProductDao = Utils.daoSession.offlineSavedProductDao
        cameraPref = getSharedPreferences("camera", 0).also {
            beepActive = it.getBoolean(SETTING_RING, false)
            flashActive = it.getBoolean(SETTING_FLASH, false)
            autoFocusActive = it.getBoolean(SETTING_FOCUS, true)
            cameraState = it.getInt("cameraState", 0)
        }

        // Setup barcode scanner
        binding.barcodeScanner.barcodeView.decoderFactory = DefaultDecoderFactory(BARCODE_FORMATS)
        binding.barcodeScanner.setStatusText(null)
        val settings = binding.barcodeScanner.barcodeView.cameraSettings
        settings.requestedCameraId = cameraState
        settings.isAutoFocusEnabled = autoFocusActive

        // Setup popup menu
        setupPopupMenu()

        // Start continuous scanner
        binding.barcodeScanner.decodeContinuous(barcodeScanCallback)
        beepManager = BeepManager(this)
        binding.quickViewSearchByBarcode.setOnEditorActionListener(barcodeInputListener)
        install(this, binding.bottomNavigation.bottomNavigation)
    }

    private fun setupPopupMenu() {
        popupMenu = PopupMenu(this, binding.buttonMore).also {
            it.menuInflater.inflate(R.menu.popup_menu, it.menu)
            if (flashActive) {
                binding.barcodeScanner.setTorchOn()
                binding.toggleFlash.setImageResource(R.drawable.ic_flash_on_white_24dp)
            }
            if (beepActive) {
                it.menu.findItem(R.id.toggleBeep).isChecked = true
            }
            if (autoFocusActive) {
                it.menu.findItem(R.id.toggleAutofocus).isChecked = true
            }
        }

    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onCreate(newBase))
    }

    private val isProductIncomplete: Boolean
        get() = if (product == null) {
            false
        } else product!!.imageFrontUrl == null
                || product!!.imageFrontUrl == ""
                || product!!.quantity == null
                || product!!.quantity == ""
                || product!!.productName == null
                || product!!.productName == ""
                || product!!.brands == null
                || product!!.brands == ""
                || product!!.ingredientsText == null
                || product!!.ingredientsText == ""

    private fun toggleCamera() {
        cameraPref?.edit()?.let {
            val settings = binding.barcodeScanner.barcodeView.cameraSettings
            if (binding.barcodeScanner.barcodeView.isPreviewActive) {
                binding.barcodeScanner.pause()
            }
            cameraState = if (settings.requestedCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                Camera.CameraInfo.CAMERA_FACING_FRONT
            } else {
                Camera.CameraInfo.CAMERA_FACING_BACK
            }
            settings.requestedCameraId = cameraState
            binding.barcodeScanner.barcodeView.cameraSettings = settings
            it.putInt("cameraState", cameraState)
            it.apply()
            binding.barcodeScanner.resume()
        }
    }

    private fun toggleFlash() {
        cameraPref?.edit()?.let {
            if (flashActive) {
                binding.barcodeScanner.setTorchOff()
                flashActive = false
                binding.toggleFlash.setImageResource(R.drawable.ic_flash_off_white_24dp)
                it.putBoolean(SETTING_FLASH, false)
            } else {
                binding.barcodeScanner.setTorchOn()
                flashActive = true
                binding.toggleFlash.setImageResource(R.drawable.ic_flash_on_white_24dp)
                it.putBoolean(SETTING_FLASH, true)
            }
            it.apply()
        }
    }

    private fun showMoreSettings() {
        popupMenu?.let {
            it.setOnMenuItemClickListener { item: MenuItem ->
                val editor: SharedPreferences.Editor
                when (item.itemId) {
                    R.id.toggleBeep -> {
                        editor = cameraPref!!.edit()
                        beepActive = !beepActive
                        item.isChecked = beepActive
                        editor.putBoolean(SETTING_RING, beepActive)
                        editor.apply()
                    }
                    R.id.toggleAutofocus -> {
                        if (binding.barcodeScanner.barcodeView.isPreviewActive) {
                            binding.barcodeScanner.pause()
                        }
                        editor = cameraPref!!.edit()
                        val settings = binding.barcodeScanner.barcodeView.cameraSettings
                        autoFocusActive = !autoFocusActive
                        settings.isAutoFocusEnabled = autoFocusActive
                        item.isChecked = autoFocusActive
                        editor.putBoolean(SETTING_FOCUS, autoFocusActive)
                        binding.barcodeScanner.barcodeView.cameraSettings = settings
                        binding.barcodeScanner.resume()
                        editor.apply()
                    }
                    R.id.troubleScanning -> {
                        hideAllViews()
                        if (hintBarcodeDisp != null) {
                            hintBarcodeDisp!!.dispose()
                        }
                        binding.quickView.setOnClickListener(null)
                        binding.quickViewSearchByBarcode.text = null
                        binding.quickViewSearchByBarcode.visibility = View.VISIBLE
                        binding.quickView.visibility = View.INVISIBLE
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        commonDisp!!.add(Completable.timer(500, TimeUnit.MILLISECONDS)
                                .doOnComplete { binding.quickView.visibility = View.VISIBLE }
                                .subscribeOn(AndroidSchedulers.mainThread()).subscribe())
                        binding.quickViewSearchByBarcode.requestFocus()
                    }
                    R.id.toggleCamera -> toggleCamera()
                }
                true
            }
            it.show()
        }
    }

    /**
     * Overridden to collapse bottom view after a back action from edit form.
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ImagesManageActivity.REQUEST_EDIT_IMAGE && (resultCode == RESULT_OK || resultCode == RESULT_CANCELED)) {
            lastBarcode?.let { setShownProduct(it) }
        } else if (resultCode == RESULT_OK && requestCode == LOGIN_ACTIVITY_REQUEST_CODE) {
            navigateToProductAddition(product)
        }
    }

    fun collapseBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun showIngredientsTab(action: ShowIngredientsAction) {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        productViewFragment?.showIngredientsTab(action)
    }

    private inner class QuickViewCallback : BottomSheetCallback() {
        private var previousSlideOffset = 0f
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            when (newState) {
                BottomSheetBehavior.STATE_HIDDEN -> {
                    lastBarcode = null
                    binding.txtProductCallToAction.visibility = View.GONE
                }
                BottomSheetBehavior.STATE_COLLAPSED -> binding.barcodeScanner.resume()
                BottomSheetBehavior.STATE_DRAGGING -> if (product == null) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
            if (binding.quickViewSearchByBarcode.visibility == View.VISIBLE) {
                bottomSheetBehavior.peekHeight = peekSmall
                bottomSheet.layoutParams.height = bottomSheetBehavior.peekHeight
            } else {
                bottomSheetBehavior.peekHeight = peekLarge
                bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            }
            bottomSheet.requestLayout()
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            val slideDelta = slideOffset - previousSlideOffset
            if (binding.quickViewSearchByBarcode.visibility != View.VISIBLE && binding.quickViewProgress.visibility != View.VISIBLE) {
                if (slideOffset > 0.01f || slideOffset < -0.01f) {
                    binding.txtProductCallToAction.visibility = View.GONE
                } else {
                    if (binding.quickViewProductNotFound.visibility != View.VISIBLE) {
                        binding.txtProductCallToAction.visibility = View.VISIBLE
                    }
                }
                if (slideOffset > 0.01f) {
                    binding.quickViewDetails.visibility = View.GONE
                    binding.quickViewTags.visibility = View.GONE
                    binding.barcodeScanner.pause()
                    if (slideDelta > 0 && productViewFragment != null) {
                        productViewFragment!!.bottomSheetWillGrow()
                        binding.bottomNavigation.bottomNavigation.visibility = View.GONE
                    }
                } else {
                    binding.barcodeScanner.resume()
                    binding.quickViewDetails.visibility = View.VISIBLE
                    if (!isAnalysisTagsEmpty) {
                        binding.quickViewTags.visibility = View.VISIBLE
                    } else {
                        binding.quickViewTags.visibility = View.GONE
                    }
                    binding.bottomNavigation.bottomNavigation.visibility = View.VISIBLE
                    if (binding.quickViewProductNotFound.visibility != View.VISIBLE) {
                        binding.txtProductCallToAction.visibility = View.VISIBLE
                    }
                }
            }
            previousSlideOffset = slideOffset
        }
    }

    private inner class BarcodeInputListener : OnEditorActionListener {
        override fun onEditorAction(textView: TextView, actionId: Int, event: KeyEvent): Boolean {
            // When user search from "having trouble" edit text
            if (actionId != EditorInfo.IME_ACTION_SEARCH) return false

            Utils.hideKeyboard(this@ContinuousScanActivity)
            hideSystemUI()

            // Check for barcode validity
            if (textView.text.toString().isNotEmpty()) {
                val barcodeText = textView.text.toString()

                // For debug only: the barcode 1 is used for test
                if ((barcodeText.length > 2 || ApiFields.Defaults.DEBUG_BARCODE == barcodeText)
                        && isBarcodeValid(barcodeText)) {
                    lastBarcode = barcodeText
                    textView.visibility = View.GONE
                    setShownProduct(barcodeText)
                    return true
                }
            }
            textView.requestFocus()
            Snackbar.make(binding.root, this@ContinuousScanActivity.getString(R.string.txtBarcodeNotValid), BaseTransientBottomBar.LENGTH_SHORT).show()
            return true
        }
    }

    private inner class BarcodeScannerCallback : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            hintBarcodeDisp?.dispose()

            if (result.text == null || result.text.isEmpty() || result.text == lastBarcode) {
                // Prevent duplicate scans
                return
            }
            val invalidBarcode = mInvalidBarcodeDao!!.queryBuilder()
                    .where(InvalidBarcodeDao.Properties.Barcode.eq(result.text)).unique()
            if (invalidBarcode != null) {
                // scanned barcode is in the list of invalid barcodes, do nothing
                return
            }
            if (beepActive) {
                beepManager.playBeepSound()
            }
            lastBarcode = result.text.also{
                if (!isFinishing) {
                    setShownProduct(it)
                }
            }

        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {
            // Here possible results are useless but we must implement this
        }
    }

    companion object {
        private const val LOGIN_ACTIVITY_REQUEST_CODE = 2
        val BARCODE_FORMATS = listOf(
                BarcodeFormat.UPC_A,
                BarcodeFormat.UPC_E,
                BarcodeFormat.EAN_13,
                BarcodeFormat.EAN_8,
                BarcodeFormat.RSS_14,
                BarcodeFormat.CODE_39,
                BarcodeFormat.CODE_93,
                BarcodeFormat.CODE_128,
                BarcodeFormat.ITF
        )
        const val SETTING_RING = "ring"
        const val SETTING_FLASH = "flash"
        const val SETTING_FOCUS = "focus"
        val LOG_TAG = ContinuousScanActivity::class.simpleName!!
    }
}