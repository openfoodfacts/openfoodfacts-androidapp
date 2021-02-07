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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior.from
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
import openfoodfacts.github.scrachx.openfood.AppFlavors.OFF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.databinding.ActivityContinuousScanBinding
import openfoodfacts.github.scrachx.openfood.features.ImagesManageActivity
import openfoodfacts.github.scrachx.openfood.features.compare.ProductCompareActivity
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.installBottomNavigation
import openfoodfacts.github.scrachx.openfood.features.listeners.CommonBottomListenerInstaller.selectNavigationItem
import openfoodfacts.github.scrachx.openfood.features.product.edit.ProductEditActivity
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity.ShowIngredientsAction
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.ingredients_analysis.IngredientsWithTagDialogFragment
import openfoodfacts.github.scrachx.openfood.features.product.view.summary.AbstractSummaryProductPresenter
import openfoodfacts.github.scrachx.openfood.features.product.view.summary.IngredientAnalysisTagsAdapter
import openfoodfacts.github.scrachx.openfood.features.product.view.summary.SummaryProductPresenter
import openfoodfacts.github.scrachx.openfood.models.InvalidBarcodeDao
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProductDao
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenHelper
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfig
import openfoodfacts.github.scrachx.openfood.models.eventbus.ProductNeedsRefreshEvent
import openfoodfacts.github.scrachx.openfood.network.ApiFields
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.*
import openfoodfacts.github.scrachx.openfood.utils.Utils.daoSession
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class ContinuousScanActivity : AppCompatActivity() {
    private var _binding: ActivityContinuousScanBinding? = null
    private val binding get() = _binding!!

    private lateinit var beepManager: BeepManager
    private lateinit var quickViewBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var bottomSheetCallback: BottomSheetCallback
    private lateinit var errorDrawable: VectorDrawableCompat

    private val barcodeInputListener = BarcodeInputListener()
    private val barcodeScanCallback = BarcodeScannerCallback()

    private val client by lazy { OpenFoodAPIClient(this@ContinuousScanActivity) }
    private val cameraPref by lazy { getSharedPreferences("camera", 0) }

    private val commonDisp = CompositeDisposable()
    private var productDisp: Disposable? = null
    private var hintBarcodeDisp: Disposable? = null

    private var peekLarge = 0
    private var peekSmall = 0

    private var cameraState = 0
    private var autoFocusActive = false
    private var flashActive = false
    private var analysisTagsEmpty = true
    private var productShowing = false
    private var beepActive = false

    private var offlineSavedProduct: OfflineSavedProduct? = null
    private var product: Product? = null
    private var lastBarcode: String? = null
    private var productViewFragment: ProductViewFragment? = null

    private var popupMenu: PopupMenu? = null
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
        binding.barcodeScanner.visibility = View.GONE
        binding.barcodeScanner.pause()
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
        productDisp?.dispose()
        summaryProductPresenter?.dispose()

        // First, try to show if we have an offline saved product in the db
        offlineSavedProduct = OfflineProductService.getOfflineProductByBarcode(barcode).also { product ->
            product?.let { showOfflineSavedDetails(it) }
        }

        // Then query the online db
        productDisp = client.getProductStateFull(barcode, Utils.HEADER_USER_AGENT_SCAN)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    hideAllViews()
                    quickViewBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    binding.quickView.setOnClickListener(null)
                    binding.quickViewProgress.visibility = View.VISIBLE
                    binding.quickViewProgressText.visibility = View.VISIBLE
                    binding.quickViewProgressText.text = getString(R.string.loading_product, barcode)
                }
                .doOnError {
                    try {
                        // A network error happened
                        if (it is IOException) {
                            hideAllViews()
                            val offlineSavedProduct = daoSession.offlineSavedProductDao!!.queryBuilder()
                                    .where(OfflineSavedProductDao.Properties.Barcode.eq(barcode))
                                    .unique()
                            tryDisplayOffline(offlineSavedProduct, barcode, R.string.addProductOffline)
                            binding.quickView.setOnClickListener { navigateToProductAddition(barcode) }
                        } else {
                            binding.quickViewProgress.visibility = View.GONE
                            binding.quickViewProgressText.visibility = View.GONE
                            Toast.makeText(this, R.string.txtConnectionError, Toast.LENGTH_LONG).run {
                                setGravity(CENTER, 0, 0)
                                show()
                            }
                            Log.i(LOG_TAG, it.message, it)
                        }
                    } catch (err: Exception) {
                        Log.w(LOG_TAG, err.message, err)
                    }
                }
                .subscribe { productState ->
                    //clear product tags
                    analysisTagsEmpty = true
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
                            it.setText(if (isProductIncomplete()) R.string.product_not_complete else R.string.scan_tooltip)
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
                        } else if (product.productName == null || product.productName == "") {
                            binding.quickViewName.setText(R.string.productNameNull)
                        } else {
                            binding.quickViewName.text = product.productName
                        }

                        // Set product additives
                        val addTags = product.additivesTags
                        binding.quickViewAdditives.text = when {
                            addTags.isNotEmpty() -> resources.getQuantityString(R.plurals.productAdditives, addTags.size, addTags.size)
                            product.statesTags.contains(ApiFields.StateTags.INGREDIENTS_COMPLETED) -> getString(R.string.productAdditivesNone)
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

        summaryProductPresenter = SummaryProductPresenter(product, object : AbstractSummaryProductPresenter() {
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

            override fun showAnalysisTags(analysisTags: List<AnalysisTagConfig>) {
                super.showAnalysisTags(analysisTags)
                if (analysisTags.isEmpty()) {
                    binding.quickViewTags.visibility = View.GONE
                    analysisTagsEmpty = true
                    return
                }
                binding.quickViewTags.visibility = View.VISIBLE
                analysisTagsEmpty = false
                val adapter = IngredientAnalysisTagsAdapter(this@ContinuousScanActivity, analysisTags)
                adapter.setOnItemClickListener { view: View?, _ ->
                    if (view == null) return@setOnItemClickListener
                    IngredientsWithTagDialogFragment.newInstance(product, view.getTag(R.id.analysis_tag_config) as AnalysisTagConfig).run {
                        show(supportFragmentManager, "fragment_ingredients_with_tag")
                        onDismissListener = { adapter.filterVisibleTags() }
                    }
                }

                binding.quickViewTags.adapter = adapter
            }
        }).also {
            it.loadAllergens { binding.callToActionImageProgress.visibility = View.GONE }
            it.loadAnalysisTags()
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
        quickViewBehavior.peekHeight = peekLarge
        binding.quickView.let {
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            it.requestLayout()
            it.rootView.requestLayout()
        }
    }

    private fun showOfflineSavedDetails(offlineSavedProduct: OfflineSavedProduct) {
        showAllViews()
        val pName = offlineSavedProduct.name
        binding.quickViewName.text = if (!pName.isNullOrEmpty()) pName else getString(R.string.productNameNull)
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
            lang = LocaleHelper.getLanguage(this@ContinuousScanActivity)
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
        OFFApplication.appComponent.inject(this)
        super.onCreate(savedInstanceState)
        _binding = ActivityContinuousScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toggleFlash.setOnClickListener { toggleFlash() }
        binding.buttonMore.setOnClickListener { showMoreSettings() }

        actionBar?.hide()

        peekLarge = resources.getDimensionPixelSize(R.dimen.scan_summary_peek_large)
        peekSmall = resources.getDimensionPixelSize(R.dimen.scan_summary_peek_small)

        errorDrawable = VectorDrawableCompat.create(resources, R.drawable.ic_product_silhouette, null)
                ?: error("Could not create vector drawable.")

        binding.quickViewTags.isNestedScrollingEnabled = false


        // The system bars are visible.
        hideSystemUI()

        hintBarcodeDisp = Completable.timer(15, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete {
                    if (productShowing) return@doOnComplete

                    hideAllViews()
                    quickViewBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    binding.quickViewSearchByBarcode.visibility = View.VISIBLE
                    binding.quickViewSearchByBarcode.requestFocus()
                }.subscribe()

        quickViewBehavior = from(binding.quickView)

        // Initial state
        quickViewBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        bottomSheetCallback = QuickViewCallback()
        quickViewBehavior.addBottomSheetCallback(bottomSheetCallback)
        cameraPref.let {
            beepActive = it.getBoolean(SETTING_RING, false)
            flashActive = it.getBoolean(SETTING_FLASH, false)
            autoFocusActive = it.getBoolean(SETTING_FOCUS, true)
            cameraState = it.getInt(SETTING_STATE, 0)
        }

        // Setup barcode scanner
        binding.barcodeScanner.barcodeView.decoderFactory = DefaultDecoderFactory(BARCODE_FORMATS)
        binding.barcodeScanner.setStatusText(null)
        binding.barcodeScanner.barcodeView.cameraSettings.run {
            requestedCameraId = cameraState
            isAutoFocusEnabled = autoFocusActive
        }

        // Setup popup menu
        setupPopupMenu()

        // Start continuous scanner
        binding.barcodeScanner.decodeContinuous(barcodeScanCallback)
        beepManager = BeepManager(this)
        binding.quickViewSearchByBarcode.setOnEditorActionListener(barcodeInputListener)
        binding.bottomNavigation.bottomNavigation.installBottomNavigation(this)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigation.bottomNavigation.selectNavigationItem(R.id.scan_bottom_nav)
        if (quickViewBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
            binding.barcodeScanner.resume()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        productDisp?.dispose()
        super.onSaveInstanceState(outState)
    }

    override fun onPause() {
        binding.barcodeScanner.pause()
        super.onPause()
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    override fun onDestroy() {
        summaryProductPresenter?.dispose()

        // Dispose all RxJava disposable
        hintBarcodeDisp?.dispose()
        commonDisp.dispose()

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

    override fun attachBaseContext(newBase: Context) = super.attachBaseContext(LocaleHelper.onCreate(newBase))

    private fun isProductIncomplete() = if (product == null) {
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
        cameraPref.edit { putInt(SETTING_STATE, cameraState) }
        binding.barcodeScanner.resume()
    }

    private fun toggleFlash() {
        cameraPref.edit {
            if (flashActive) {
                binding.barcodeScanner.setTorchOff()
                flashActive = false
                binding.toggleFlash.setImageResource(R.drawable.ic_flash_off_white_24dp)
                putBoolean(SETTING_FLASH, false)
            } else {
                binding.barcodeScanner.setTorchOn()
                flashActive = true
                binding.toggleFlash.setImageResource(R.drawable.ic_flash_on_white_24dp)
                putBoolean(SETTING_FLASH, true)
            }
        }
    }

    private fun showMoreSettings() {
        popupMenu?.let {
            it.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.toggleBeep -> {
                        beepActive = !beepActive
                        item.isChecked = beepActive
                        cameraPref.edit {
                            putBoolean(SETTING_RING, beepActive)
                            apply()
                        }
                    }
                    R.id.toggleAutofocus -> {
                        if (binding.barcodeScanner.barcodeView.isPreviewActive) {
                            binding.barcodeScanner.pause()
                        }
                        val settings = binding.barcodeScanner.barcodeView.cameraSettings
                        autoFocusActive = !autoFocusActive
                        settings.isAutoFocusEnabled = autoFocusActive
                        item.isChecked = autoFocusActive

                        cameraPref.edit { putBoolean(SETTING_FOCUS, autoFocusActive) }

                        binding.barcodeScanner.resume()
                        binding.barcodeScanner.barcodeView.cameraSettings = settings
                    }
                    R.id.troubleScanning -> {
                        hideAllViews()
                        hintBarcodeDisp?.dispose()

                        binding.quickView.setOnClickListener(null)
                        binding.quickViewSearchByBarcode.text = null
                        binding.quickViewSearchByBarcode.visibility = View.VISIBLE
                        binding.quickView.visibility = View.INVISIBLE
                        quickViewBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        commonDisp.add(Completable.timer(500, TimeUnit.MILLISECONDS)
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

    fun showIngredientsTab(action: ShowIngredientsAction) {
        quickViewBehavior.state = BottomSheetBehavior.STATE_EXPANDED
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
                    quickViewBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }
            if (binding.quickViewSearchByBarcode.visibility == View.VISIBLE) {
                quickViewBehavior.peekHeight = peekSmall
                bottomSheet.layoutParams.height = quickViewBehavior.peekHeight
            } else {
                quickViewBehavior.peekHeight = peekLarge
                bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            }
            bottomSheet.requestLayout()
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            val slideDelta = slideOffset - previousSlideOffset
            if (binding.quickViewSearchByBarcode.visibility != View.VISIBLE && binding.quickViewProgress.visibility != View.VISIBLE) {
                if (slideOffset > 0.01f || slideOffset < -0.01f) {
                    binding.txtProductCallToAction.visibility = View.GONE
                } else if (binding.quickViewProductNotFound.visibility != View.VISIBLE) {
                    binding.txtProductCallToAction.visibility = View.VISIBLE
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
                    binding.quickViewTags.visibility = if (analysisTagsEmpty) View.GONE else View.VISIBLE
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
        override fun onEditorAction(textView: TextView, actionId: Int, event: KeyEvent?): Boolean {
            // When user search from "having trouble" edit text
            if (actionId != EditorInfo.IME_ACTION_SEARCH) return false

            Utils.hideKeyboard(this@ContinuousScanActivity)
            hideSystemUI()

            // Check for barcode validity
            val barcodeText = textView.text.toString()
            // For debug only: the barcode 1 is used for test
            if (barcodeText.isEmpty() || (barcodeText.length <= 2 && ApiFields.Defaults.DEBUG_BARCODE != barcodeText) || !isBarcodeValid(barcodeText)) {
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
            hintBarcodeDisp?.dispose()

            // Prevent duplicate scans
            if (result.text == null || result.text.isEmpty() || result.text == lastBarcode) return

            val invalidBarcode = daoSession.invalidBarcodeDao.queryBuilder()
                    .where(InvalidBarcodeDao.Properties.Barcode.eq(result.text))
                    .unique()
            // Scanned barcode is in the list of invalid barcodes, do nothing
            if (invalidBarcode != null) return

            if (beepActive) {
                beepManager.playBeepSound()
            }
            lastBarcode = result.text.also { if (!isFinishing) setShownProduct(it) }

        }

        // Here possible results are useless but we must implement this
        override fun possibleResultPoints(resultPoints: List<ResultPoint>) = Unit
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
        private const val SETTING_RING = "ring"
        private const val SETTING_FLASH = "flash"
        private const val SETTING_FOCUS = "focus"
        private const val SETTING_STATE = "cameraState"
        private val LOG_TAG = this::class.simpleName!!
    }
}
