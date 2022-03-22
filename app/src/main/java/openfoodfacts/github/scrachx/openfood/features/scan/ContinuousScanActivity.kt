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

import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.Gravity.CENTER
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.mikepenz.iconics.IconicsColor
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.IconicsSize
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.BuildConfig
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsEvent
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsView
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.databinding.ActivityContinuousScanBinding
import openfoodfacts.github.scrachx.openfood.features.images.manage.ImagesManageActivity
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.product.view.IProductView
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity.ShowIngredientsAction
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.ingredients_analysis.IngredientsWithTagDialogFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.summary.AbstractSummaryProductPresenter
import openfoodfacts.github.scrachx.openfood.features.product.view.summary.IngredientAnalysisTagsAdapter
import openfoodfacts.github.scrachx.openfood.features.product.view.summary.SummaryProductPresenter
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.installBottomNavigation
import openfoodfacts.github.scrachx.openfood.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.models.CameraState
import openfoodfacts.github.scrachx.openfood.models.DaoSession
import openfoodfacts.github.scrachx.openfood.models.InvalidBarcodeDao
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProductDao
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenHelper
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfig
import openfoodfacts.github.scrachx.openfood.models.eventbus.ProductNeedsRefreshEvent
import openfoodfacts.github.scrachx.openfood.network.ApiFields.StateTags
import openfoodfacts.github.scrachx.openfood.repositories.*
import openfoodfacts.github.scrachx.openfood.utils.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class ContinuousScanActivity : BaseActivity(), IProductView {
    private var _binding: ActivityContinuousScanBinding? = null
    internal val binding get() = _binding!!

    @Inject
    lateinit var client: ProductRepository

    @Inject
    lateinit var daoSession: DaoSession

    @Inject
    lateinit var offlineProductRepository: OfflineProductRepository

    @Inject
    lateinit var taxonomiesRepository: TaxonomiesRepository

    @Inject
    lateinit var robotoffRepository: RobotoffRepository

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var matomoAnalytics: MatomoAnalytics

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var localeManager: LocaleManager

    @Inject
    lateinit var scannerPrefsRepository: ScannerPreferencesRepository

    private lateinit var beepManager: BeepManager
    internal lateinit var quickViewBehavior: BottomSheetBehavior<LinearLayout>
    private val barcodeInputListener = BarcodeInputListener()

    private val barcodeScanCallback = BarcodeScannerCallback()

    private val bottomSheetCallback by lazy { QuickViewCallback(this) }

    private val settings by lazy { getAppPreferences() }

    private var productDisp: Job? = null
    private var hintBarcodeDisp: Job? = null


    private var cameraState = 0
    private var autoFocusActive = false
    private var flashActive = false
    internal var analysisTagsEmpty = true
    private var productShowing = false
    private var beepActive = false

    /**
    boolean to determine if MLKit Scanner is to be used
     */
    internal var useMLScanner = false

    internal val mlKitView by lazy { MlKitCameraView(this) }

    private var offlineSavedProduct: OfflineSavedProduct? = null
    private var product: Product? = null
    internal var lastBarcode: String? = null
    internal var productViewFragment: ProductViewFragment? = null

    private lateinit var cameraSettingMenu: PopupMenu
    private var summaryProductPresenter: SummaryProductPresenter? = null

    private val productActivityResultLauncher = registerForActivityResult(ProductEditActivity.EditProductContract())
    { result -> if (result) lastBarcode?.let { setShownProduct(it) } }

    /**
     * Used by screenshot tests.
     *
     * @param barcode barcode to serach
     */
    @Suppress("unused")
    internal fun showProduct(barcode: String) {
        productShowing = true
        binding.barcodeScanner.isVisible = false
        binding.barcodeScanner.pause()
        mlKitView.stopCameraPreview()
        binding.imageForScreenshotGenerationOnly.visibility = View.VISIBLE
        setShownProduct(barcode)
    }

    /**
     * Makes network call and search for the product in the database
     *
     * @param barcode Barcode to be searched
     */
    private fun setShownProduct(barcode: String) {
        if (isFinishing) return

        // Dispose the previous call if not ended.
        productDisp?.cancel()

        // First, try to show if we have an offline saved product in the db
        offlineSavedProduct = offlineProductRepository.getOfflineProductByBarcode(barcode).also { product ->
            product?.let { showOfflineSavedDetails(it) }
        }

        // Then query the online db
        productDisp = lifecycleScope.launch(Dispatchers.Main) {

            hideAllViews()
            quickViewBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            binding.quickView.setOnClickListener(null)
            binding.quickViewProgress.visibility = View.VISIBLE
            binding.quickViewProgressText.visibility = View.VISIBLE
            binding.quickViewProgressText.text = getString(R.string.loading_product, barcode)

            val productState = try {
                client.getProductStateFull(barcode, userAgent = Utils.HEADER_USER_AGENT_SCAN)
            } catch (err: Exception) {
                if (!isActive) return@launch
                // A network error happened
                if (err is IOException) {
                    hideAllViews()
                    val offlineSavedProduct = daoSession.offlineSavedProductDao.unique {
                        where(OfflineSavedProductDao.Properties.Barcode.eq(barcode))
                    }
                    tryDisplayOffline(offlineSavedProduct, barcode, R.string.addProductOffline)
                    binding.quickView.setOnClickListener { navigateToProductAddition(barcode) }
                } else {
                    binding.quickViewProgress.visibility = View.GONE
                    binding.quickViewProgressText.visibility = View.GONE

                    Toast.makeText(this@ContinuousScanActivity, R.string.txtConnectionError, Toast.LENGTH_LONG)
                        .apply { setGravity(CENTER, 0, 0) }
                        .show()

                    Log.w("ContinuousScanActivity", err.message, err)
                }
                return@launch
            }


            //clear product tags
            analysisTagsEmpty = true
            binding.quickViewTags.adapter = null
            binding.quickViewProgress.visibility = View.GONE
            binding.quickViewProgressText.visibility = View.GONE

            if (productState.status == 0L) {
                tryDisplayOffline(offlineSavedProduct, barcode, R.string.product_not_found)
            } else {
                val product = productState.product!!
                this@ContinuousScanActivity.product = product

                // Add product to scan history
                productDisp = lifecycleScope.launch { client.addToHistory(product) }

                showAllViews()

                binding.txtProductCallToAction.let {
                    it.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                    it.background = ContextCompat.getDrawable(this@ContinuousScanActivity, R.drawable.rounded_quick_view_text)
                    it.setText(if (product.isProductIncomplete()) R.string.product_not_complete else R.string.scan_tooltip)
                    it.visibility = View.VISIBLE
                }

                setupSummary(product)
                quickViewBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

                showProductFullScreen()
                binding.quickViewProductNotFound.visibility = View.GONE
                binding.quickViewProductNotFoundButton.visibility = View.GONE

                // Set product name, prefer offline
                if (offlineSavedProduct != null && !offlineSavedProduct?.name.isNullOrEmpty()) {
                    binding.quickViewName.text = offlineSavedProduct!!.name
                } else if (product.productName == null || product.productName.isEmpty()) {
                    binding.quickViewName.setText(R.string.productNameNull)
                } else {
                    binding.quickViewName.text = product.productName
                }

                // Set product additives
                val addTags = product.additivesTags
                binding.quickViewAdditives.text = when {
                    addTags.isNotEmpty() -> resources.getQuantityString(R.plurals.productAdditives, addTags.size, addTags.size)
                    StateTags.INGREDIENTS_COMPLETED in product.statesTags -> getString(R.string.productAdditivesNone)
                    else -> getString(R.string.productAdditivesUnknown)
                }

                // Show nutriscore in quickView only if app flavour is OFF and the product has one
                quickViewCheckNutriScore(product)

                // Show nova group in quickView only if app flavour is OFF and the product has one
                quickViewCheckNova(product)

                // If the product has an ecoscore, show it instead of the CO2 icon
                quickViewCheckEcoScore(product)

                // Create the product view fragment and add it to the layout
                val newProductViewFragment = ProductViewFragment.newInstance(productState)

                supportFragmentManager.commit {
                    replace(R.id.frame_layout, newProductViewFragment)
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                }
                productViewFragment = newProductViewFragment
            }
        }
    }

    private fun quickViewCheckNutriScore(product: Product) = if (isFlavors(OFF)) {
        binding.quickViewNutriScore.visibility = View.VISIBLE
        binding.quickViewNutriScore.setImageResource(product.getNutriScoreResource())
    } else {
        binding.quickViewNutriScore.visibility = View.GONE
    }

    private fun quickViewCheckNova(product: Product) = if (isFlavors(OFF)) {
        binding.quickViewNovaGroup.visibility = View.VISIBLE
        binding.quickViewAdditives.visibility = View.VISIBLE
        binding.quickViewNovaGroup.setImageResource(product.getNovaGroupResource())
    } else {
        binding.quickViewNovaGroup.visibility = View.GONE
    }

    private fun quickViewCheckEcoScore(product: Product) = if (isFlavors(OFF)) {
        binding.quickViewEcoscoreIcon.setImageResource(product.getEcoscoreResource())
        binding.quickViewEcoscoreIcon.visibility = View.VISIBLE
    } else {
        binding.quickViewEcoscoreIcon.visibility = View.GONE
    }

    private fun tryDisplayOffline(
        offlineSavedProduct: OfflineSavedProduct?,
        barcode: String,
        @StringRes errorMsg: Int
    ) = if (offlineSavedProduct != null) showOfflineSavedDetails(offlineSavedProduct)
    else showProductNotFound(getString(errorMsg, barcode))

    private fun setupSummary(product: Product) {
        binding.callToActionImageProgress.visibility = View.VISIBLE

        summaryProductPresenter = SummaryProductPresenter(localeManager.getLanguage(), product, object : AbstractSummaryProductPresenter() {
            override fun showAllergens(allergens: List<AllergenName>) {
                val data = AllergenHelper.computeUserAllergen(product, allergens)
                binding.callToActionImageProgress.visibility = View.GONE
                if (data.isEmpty()) return
                val iconicsDrawable = IconicsDrawable(this@ContinuousScanActivity, GoogleMaterial.Icon.gmd_warning)
                    .color(IconicsColor.colorInt(ContextCompat.getColor(this@ContinuousScanActivity, R.color.white)))
                    .size(IconicsSize.dp(24))
                binding.txtProductCallToAction.setCompoundDrawablesWithIntrinsicBounds(iconicsDrawable, null, null, null)
                binding.txtProductCallToAction.background = ContextCompat.getDrawable(this@ContinuousScanActivity, R.drawable.rounded_quick_view_text_warn)
                binding.txtProductCallToAction.text = if (data.incomplete) {
                    getString(R.string.product_incomplete_message)
                } else {
                    "${getString(R.string.product_allergen_prompt)}\n${data.allergens.joinToString(", ")}"
                }
            }

            override suspend fun showAnalysisTags(state: ProductInfoState<List<AnalysisTagConfig>>) = withContext(Dispatchers.Main) {
                when (state) {
                    is ProductInfoState.Data -> {
                        binding.quickViewTags.visibility = View.VISIBLE
                        analysisTagsEmpty = false
                        val adapter = IngredientAnalysisTagsAdapter(
                            this@ContinuousScanActivity,
                            state.data,
                            picasso,
                            sharedPreferences
                        ).apply adapter@{
                            setOnItemClickListener { view: View, _ ->
                                IngredientsWithTagDialogFragment.newInstance(
                                    product,
                                    view.getTag(R.id.analysis_tag_config) as AnalysisTagConfig
                                ).run {
                                    onDismissListener = { this@adapter.filterVisibleTags() }
                                    show(supportFragmentManager, "fragment_ingredients_with_tag")
                                }
                            }
                        }

                        binding.quickViewTags.adapter = adapter
                    }
                    is ProductInfoState.Empty -> {
                        binding.quickViewTags.visibility = View.GONE
                        analysisTagsEmpty = true
                    }
                    ProductInfoState.Loading -> {
                        // TODO
                    }
                }

            }
        }, taxonomiesRepository, robotoffRepository).also {
            lifecycleScope.launch {
                try {
                    it.loadAllergens()
                } catch (err: Exception) {
                    binding.callToActionImageProgress.visibility = View.GONE
                }
            }
            lifecycleScope.launch { it.loadAnalysisTags() }
        }
    }


    private fun showProductNotFound(text: String) {
        hideAllViews()
        quickViewBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        binding.quickView.setOnClickListener { lastBarcode?.let { navigateToProductAddition(it) } }
        binding.quickViewProductNotFound.text = text
        binding.quickViewProductNotFound.visibility = View.VISIBLE
        binding.quickViewProductNotFoundButton.visibility = View.VISIBLE
        binding.quickViewProductNotFoundButton.setOnClickListener { lastBarcode?.let { navigateToProductAddition(it) } }
    }

    private fun showProductFullScreen() {
        quickViewBehavior.peekHeight = bottomSheetCallback.peekLarge
        binding.quickView.let {
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            it.requestLayout()
            it.rootView.requestLayout()
        }
    }

    private fun showOfflineSavedDetails(savedProduct: OfflineSavedProduct) {
        showAllViews()
        binding.quickViewName.text = savedProduct.name?.takeUnless { it.isEmpty() } ?: getString(R.string.productNameNull)
        binding.txtProductCallToAction.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        binding.txtProductCallToAction.background = ContextCompat.getDrawable(this@ContinuousScanActivity, R.drawable.rounded_quick_view_text)
        binding.txtProductCallToAction.setText(R.string.product_not_complete)
        binding.txtProductCallToAction.visibility = View.VISIBLE
        binding.quickViewSlideUpIndicator.visibility = View.GONE
        quickViewBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun navigateToProductAddition(productBarcode: String) {
        navigateToProductAddition(Product().apply {
            code = productBarcode
            lang = localeManager.getLanguage()
        })
    }

    private fun navigateToProductAddition(product: Product?) {
        productActivityResultLauncher.launch(product)
    }

    private fun showAllViews() {
        binding.quickViewSlideUpIndicator.visibility = View.VISIBLE
        binding.quickViewName.visibility = View.VISIBLE
        binding.frameLayout.visibility = View.VISIBLE
        binding.quickViewAdditives.visibility = View.VISIBLE
        if (!analysisTagsEmpty) {
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
        binding.quickViewEcoscoreIcon.visibility = View.GONE

        binding.quickViewProductNotFound.visibility = View.GONE
        binding.quickViewProductNotFoundButton.visibility = View.GONE
        binding.txtProductCallToAction.visibility = View.GONE
        binding.quickViewTags.visibility = View.GONE
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityContinuousScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        useMLScanner = BuildConfig.USE_MLKIT && settings.getBoolean(getString(R.string.pref_scanner_mlkit_key), false)

        TooltipCompat.setTooltipText(binding.toggleFlash, getString(R.string.toggle_flash))

        binding.toggleFlash.setOnClickListener { toggleFlash() }
        binding.buttonMore.setOnClickListener { showMoreSettings() }

        actionBar?.hide()

        // Cannot set in xml because attribute is only API > 21
        binding.quickViewTags.isNestedScrollingEnabled = false

        // The system bars are visible.
        hideSystemUI()

        hintBarcodeDisp = lifecycleScope.launch {
            delay(15 * 1000)

            if (productShowing) return@launch

            hideAllViews()
            quickViewBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            binding.quickViewSearchByBarcode.visibility = View.VISIBLE
            binding.quickViewSearchByBarcode.requestFocus()
        }

        quickViewBehavior = BottomSheetBehavior.from(binding.quickView)

        // Initial state
        quickViewBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        quickViewBehavior.addBottomSheetCallback(bottomSheetCallback)

        beepActive = scannerPrefsRepository.getRingPref()
        flashActive = scannerPrefsRepository.getFlashPref()
        autoFocusActive = scannerPrefsRepository.getAutoFocusPref()
        cameraState = scannerPrefsRepository.getCameraPref().value

        // Setup barcode scanner
        if (!useMLScanner) {
            binding.barcodeScanner.visibility = View.VISIBLE
            binding.cameraPreviewViewStub.isVisible = false
            binding.barcodeScanner.barcodeView.decoderFactory = DefaultDecoderFactory(ScannerPreferencesRepository.BARCODE_FORMATS)
            binding.barcodeScanner.setStatusText(null)
            binding.barcodeScanner.setOnClickListener {
                quickViewBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            binding.barcodeScanner.barcodeView.cameraSettings.run {
                requestedCameraId = cameraState
                isAutoFocusEnabled = autoFocusActive
            }

            // Start continuous scanner
            binding.barcodeScanner.decodeContinuous(barcodeScanCallback)
            beepManager = BeepManager(this)
        } else {
            binding.barcodeScanner.isVisible = false
            mlKitView.attach(binding.cameraPreviewViewStub, cameraState, flashActive, autoFocusActive)
            mlKitView.onOverlayClickListener = {
                quickViewBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            mlKitView.barcodeScannedCallback = {
                mlBarcodeCallback(it)
            }
        }

        binding.quickViewSearchByBarcode.setOnEditorActionListener(barcodeInputListener)
        binding.bottomNavigation.bottomNavigation.installBottomNavigation(this)

        // Setup popup menu
        setupPopupMenu()
    }

    private fun mlBarcodeCallback(barcodeValue: String) {
        hintBarcodeDisp?.cancel()

        // Prevent duplicate scans
        if (barcodeValue.isEmpty() || barcodeValue == lastBarcode) return

        val invalidBarcode = daoSession.invalidBarcodeDao.unique {
            where(InvalidBarcodeDao.Properties.Barcode.eq(barcodeValue))
        }

        // Scanned barcode is in the list of invalid barcodes, do nothing
        if (invalidBarcode != null) return

        lastBarcode = barcodeValue.also { if (!isFinishing) setShownProduct(it) }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.bottomNavigation.selectNavigationItem(R.id.scan_bottom_nav)

        if (!useMLScanner && quickViewBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            binding.barcodeScanner.resume()
        } else if (useMLScanner && quickViewBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            mlKitView.onResume()
        }
        matomoAnalytics.trackView(AnalyticsView.Scanner)
    }

    override fun onPostResume() {
        super.onPostResume()
        // Back to working state after the bottom sheet is dismissed.
        mlKitView.updateWorkflowState(WorkflowState.DETECTING)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        productDisp?.cancel()
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        if (!useMLScanner) {
            binding.barcodeScanner.pause()
        } else {
            mlKitView.stopCameraPreview()
        }
        super.onPause()
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    override fun onDestroy() {
        mlKitView.detach()

        // Dispose all RxJava disposable
        hintBarcodeDisp?.cancel()

        // Remove bottom sheet callback as it uses binding
        quickViewBehavior.removeBottomSheetCallback(bottomSheetCallback)
        _binding = null
        super.onDestroy()
    }


    @Subscribe
    fun onEventBusProductNeedsRefreshEvent(event: ProductNeedsRefreshEvent) {
        if (event.barcode == lastBarcode) {
            runOnUiThread { setShownProduct(event.barcode) }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        //status bar will remain visible if user presses home and then reopens the activity
        // hence hiding status bar again
        hideSystemUI()
    }

    private fun hideSystemUI() {
        WindowInsetsControllerCompat(window, binding.root).hide(WindowInsetsCompat.Type.statusBars())
        this.actionBar?.hide()
    }

    private fun setupPopupMenu() {
        cameraSettingMenu = PopupMenu(this, binding.buttonMore).also {
            it.menuInflater.inflate(R.menu.popup_menu, it.menu)
            // turn flash on if flashActive true in pref
            if (flashActive) {
                binding.toggleFlash.setImageResource(R.drawable.ic_flash_on_white_24dp)
                if (!useMLScanner) {
                    binding.barcodeScanner.setTorchOn()
                }
            }
            if (beepActive) {
                it.menu.findItem(R.id.toggleBeep).isChecked = true
            }
            if (autoFocusActive) {
                it.menu.findItem(R.id.toggleAutofocus).isChecked = true
            }
        }
    }

    @Suppress("deprecation")
    private fun toggleCamera() {
        cameraState = if (cameraState == Camera.CameraInfo.CAMERA_FACING_BACK) {
            Camera.CameraInfo.CAMERA_FACING_FRONT
        } else {
            Camera.CameraInfo.CAMERA_FACING_BACK
        }
        scannerPrefsRepository.saveCameraPref(CameraState.fromInt(cameraState))

        if (!useMLScanner) {
            val settings = binding.barcodeScanner.barcodeView.cameraSettings
            if (binding.barcodeScanner.barcodeView.isPreviewActive) {
                binding.barcodeScanner.pause()
            }
            settings.requestedCameraId = cameraState
            binding.barcodeScanner.barcodeView.cameraSettings = settings
            binding.barcodeScanner.resume()
        } else {
            mlKitView.toggleCamera()
        }
    }

    private fun toggleFlash() {
        if (flashActive) {
            flashActive = false
            binding.toggleFlash.setImageResource(R.drawable.ic_flash_off_white_24dp)

            if (useMLScanner) {
                mlKitView.updateFlashSetting(flashActive)
            } else {
                binding.barcodeScanner.setTorchOff()
            }
        } else {
            flashActive = true
            binding.toggleFlash.setImageResource(R.drawable.ic_flash_on_white_24dp)

            if (useMLScanner) {
                mlKitView.updateFlashSetting(flashActive)
            } else {
                binding.barcodeScanner.setTorchOn()
            }
        }
        scannerPrefsRepository.saveFlashPref(flashActive)
    }

    private fun showMoreSettings() {
        if (useMLScanner) {
            cameraSettingMenu.menu.findItem(R.id.toggleBeep).isEnabled = false
        }
        cameraSettingMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.toggleBeep -> {
                    beepActive = !beepActive
                    item.isChecked = beepActive
                    scannerPrefsRepository.saveRingPref(beepActive)
                }
                R.id.toggleAutofocus -> {
                    autoFocusActive = !autoFocusActive
                    item.isChecked = autoFocusActive
                    scannerPrefsRepository.saveAutoFocusPref(autoFocusActive)

                    if (useMLScanner) {
                        mlKitView.updateFocusModeSetting(autoFocusActive)
                    } else {
                        if (binding.barcodeScanner.barcodeView.isPreviewActive) {
                            binding.barcodeScanner.pause()
                        }
                        val settings = binding.barcodeScanner.barcodeView.cameraSettings
                        settings.isAutoFocusEnabled = autoFocusActive
                        binding.barcodeScanner.resume()
                        binding.barcodeScanner.barcodeView.cameraSettings = settings

                    }
                }
                R.id.troubleScanning -> {
                    hideAllViews()
                    hintBarcodeDisp?.cancel()
                    binding.quickView.setOnClickListener(null)
                    binding.quickViewSearchByBarcode.text = null
                    binding.quickViewSearchByBarcode.visibility = View.VISIBLE
                    quickViewBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    binding.quickViewSearchByBarcode.requestFocus()
                }
                R.id.toggleCamera -> toggleCamera()
            }
            true
        }
        cameraSettingMenu.show()
    }

    /**
     * Overridden to collapse bottom view after a back action from edit form.
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        quickViewBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
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
        quickViewBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    override fun showIngredientsTab(action: ShowIngredientsAction) {
        quickViewBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        productViewFragment?.showIngredientsTab(action)
    }

    // The back button will close the bottom sheet if visible
    override fun onBackPressed() {
        if (quickViewBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            collapseBottomSheet()
        } else {
            super.onBackPressed()
        }
    }

    private inner class BarcodeInputListener : OnEditorActionListener {
        override fun onEditorAction(textView: TextView, actionId: Int, event: KeyEvent?): Boolean {
            // When user search from "having trouble" edit text
            if (actionId != EditorInfo.IME_ACTION_SEARCH) return false

            hideKeyboard()
            hideSystemUI()

            // Check for barcode validity
            val barcodeText = textView.text.toString()
            // For debug only: the barcode 1 is used for test
            if (!isBarcodeValid(barcodeText)) {
                textView.requestFocus()
                textView.error = getString(R.string.txtBarcodeNotValid)
                return true
            }
            lastBarcode = barcodeText

            textView.visibility = View.GONE
            setShownProduct(barcodeText)
            return true
        }
    }

    private inner class BarcodeScannerCallback : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            hintBarcodeDisp?.cancel()

            // Prevent duplicate scans
            if (result.text == null || result.text.isEmpty() || result.text == lastBarcode) return

            val invalidBarcode = daoSession.invalidBarcodeDao.unique {
                where(InvalidBarcodeDao.Properties.Barcode.eq(result.text))
            }
            // Scanned barcode is in the list of invalid barcodes, do nothing
            if (invalidBarcode != null) return

            if (beepActive) {
                beepManager.playBeepSound()
            }
            lastBarcode = result.text
            if (!isFinishing) {
                setShownProduct(result.text)
                matomoAnalytics.trackEvent(AnalyticsEvent.ScannedBarcode(result.text))
            }
        }

        // Here possible results are useless but we must implement this
        override fun possibleResultPoints(resultPoints: List<ResultPoint>) = Unit
    }

    companion object {
        private const val LOGIN_ACTIVITY_REQUEST_CODE = 2
    }
}
