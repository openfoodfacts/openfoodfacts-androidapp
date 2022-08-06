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
package openfoodfacts.github.scrachx.openfood.features.images.select

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductImagesListBinding
import openfoodfacts.github.scrachx.openfood.features.adapters.ProductImagesSelectionAdapter
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.images.*
import openfoodfacts.github.scrachx.openfood.network.services.ProductsAPI
import openfoodfacts.github.scrachx.openfood.utils.*
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ImagesSelectActivity : BaseActivity() {
    private var _binding: ActivityProductImagesListBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var productsApi: ProductsAPI

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var photoReceiverHandler: PhotoReceiverHandler

    private lateinit var adapter: ProductImagesSelectionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityProductImagesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = intent.getStringExtra(TOOLBAR_TITLE)

        binding.closeZoom.setOnClickListener { closeZoom() }
        binding.expandedImage.setOnClickListener { closeZoom() }
        binding.btnAcceptSelection.setOnClickListener { acceptSelection() }
        binding.btnChooseImage.setOnClickListener { chooseImage() }

        // Get intent data
        val code = intent.getStringExtra(PRODUCT_BARCODE) ?: error("Cannot start activity without product barcode.")
        loadProductImages(code)
    }

    private fun loadProductImages(code: String) {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        lifecycleScope.launchWhenCreated {
            val imageNames = ImageNamesParser.extractImageNames(productsApi.getProductImages(code))
                .sortedByTimestampDescending()
                .map { it.key }

            // Check if user is logged in
            adapter = ProductImagesSelectionAdapter(picasso, imageNames, code) {
                setSelectedImage(it)
            }

            binding.imagesRecycler.adapter = adapter
            binding.imagesRecycler.layoutManager = GridLayoutManager(this@ImagesSelectActivity, 3)
        }
    }

    private fun setSelectedImage(selectedPosition: Int) {
        if (selectedPosition >= 0) {
            val imageUrl = adapter.getImageUrl(selectedPosition)
            picasso.load(imageUrl)
                .resize(400, 400)
                .centerInside()
                .into(binding.expandedImage)
            binding.zoomContainer.visibility = View.VISIBLE
            binding.imagesRecycler.visibility = View.INVISIBLE
        }
        updateButtonAccept()
    }

    private fun closeZoom() {
        binding.zoomContainer.visibility = View.INVISIBLE
        binding.imagesRecycler.visibility = View.VISIBLE
    }

    private fun acceptSelection() {
        setResult(RESULT_OK, Intent().apply {
            putExtra(IMG_ID, adapter.getSelectedImageName())
        })
        finish()
    }

    private fun chooseImage() {
        when {
            checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(READ_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_STORAGE
                )
            }
            else -> {
                EasyImage.openGallery(this, -1, false)
            }
        }
    }

    private fun updateButtonAccept() {
        val visible = isUserSet() && adapter.isSelectionDone()
        binding.btnAcceptSelection.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        binding.txtInfo.visibility = binding.btnAcceptSelection.visibility
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    override fun onSupportNavigateUp(): Boolean {
        setResult(RESULT_CANCELED)
        finish()
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data) { newPhotoFile ->
            setResult(RESULT_OK, Intent().apply {
                putExtra(IMAGE_FILE, newPhotoFile)
            })
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_STORAGE && isAllGranted(grantResults)) {
            chooseImage()
        }
    }


    companion object {
        const val TOOLBAR_TITLE = "TOOLBAR_TITLE"

        @JvmStatic
        fun start(context: Context, toolbarTitle: String, productCode: String) =
            context.startActivity(Intent(context, this::class.java).apply {
                putExtra(TOOLBAR_TITLE, toolbarTitle)
                putExtra(PRODUCT_BARCODE, productCode)
            })

        class SelectImageContract(
            private val toolbarTitle: String,
        ) : ActivityResultContract<String, Pair<String?, File?>>() {
            override fun createIntent(context: Context, input: String) = Intent<ImagesSelectActivity>(context) {
                putExtra(TOOLBAR_TITLE, toolbarTitle)
                putExtra(PRODUCT_BARCODE, input)
            }


            override fun parseResult(resultCode: Int, intent: Intent?) =
                if (resultCode != RESULT_OK) null to null
                else intent?.getStringExtra(IMG_ID) to intent?.getSerializableExtra(IMAGE_FILE) as File?

        }
    }
}
