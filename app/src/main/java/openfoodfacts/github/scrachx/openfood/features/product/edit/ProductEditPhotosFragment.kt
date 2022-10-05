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

import android.Manifest.permission
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.ImageView
import android.widget.TableRow
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.FragmentAddProductPhotosBinding
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import openfoodfacts.github.scrachx.openfood.utils.MY_PERMISSIONS_REQUEST_CAMERA
import openfoodfacts.github.scrachx.openfood.utils.PhotoReceiverHandler
import openfoodfacts.github.scrachx.openfood.utils.dpsToPixel
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import javax.inject.Inject

/**
 * Fragment for adding photos of the product
 *
 * @see R.layout.fragment_add_product_photos
 */
@AndroidEntryPoint
class ProductEditPhotosFragment : ProductEditFragment() {
    private var _binding: FragmentAddProductPhotosBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var localeManager: LocaleManager

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var photoReceiverHandler: PhotoReceiverHandler
    private var code: String? = null
    private var photoFile: File? = null

    private lateinit var activity: Activity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = requireActivity()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddProductPhotosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun allValid() = true
    override fun getUpdatedFieldsMap() = mapOf<String, String?>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAddOtherImage.setOnClickListener { addOtherImage() }
        binding.btnAdd.setOnClickListener { next() }

        val bundle = arguments
        if (bundle == null) {
            Toast.makeText(activity, R.string.error_adding_product_photos, Toast.LENGTH_SHORT).show()
            error("Cannot start fragment without arguments.")
        }

        val product = bundle.getSerializable("product") as Product?
        val offlineSavedProduct = bundle.getSerializable("edit_offline_product") as OfflineSavedProduct?
        val editing = bundle.getBoolean(ProductEditActivity.KEY_IS_EDITING)
        if (product != null) {
            code = product.code
        }
        if (editing && product != null) {
            binding.btnAdd.setText(R.string.save_edits)
        } else if (offlineSavedProduct != null) {
            code = offlineSavedProduct.barcode
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addOtherImage() {
        if (ContextCompat.checkSelfPermission(requireActivity(), permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
        } else {
            EasyImage.openCamera(this, 0)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data) { newPhotoFile ->
            photoFile = newPhotoFile
            val image = ProductImage(code!!, ProductImageField.OTHER, newPhotoFile, localeManager.getLanguage())
            image.filePath = photoFile!!.toURI().path
            if (activity is ProductEditActivity) {
                (activity as ProductEditActivity).savePhoto(image, 4)
            }
        }
    }

    override fun showImageProgress() {
        binding.imageProgress.visibility = View.VISIBLE
        binding.imageProgressText.visibility = View.VISIBLE
        binding.imageProgressText.setText(R.string.toastSending)
        addImageRow()
    }

    override fun hideImageProgress(errorInUploading: Boolean, message: String) {
        binding.imageProgress.visibility = View.GONE
        binding.btnAddOtherImage.visibility = View.VISIBLE
        if (errorInUploading) {
            binding.imageProgressText.visibility = View.GONE
        } else {
            binding.imageProgressText.setText(R.string.additional_image_uploaded_successfully)
        }
    }

    /**
     * Load image into the image view and add it to tableLayout
     */
    private fun addImageRow() {
        val row = TableRow(activity)
        val lp = TableRow.LayoutParams(MATCH_PARENT, requireContext().dpsToPixel(100)).apply {
            topMargin = requireContext().dpsToPixel(10)
        }
        val imageView = ImageView(activity).apply {
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = lp
        }
        picasso.load(photoFile!!)
            .resize(requireContext().dpsToPixel(100), requireContext().dpsToPixel(100))
            .centerInside()
            .into(imageView)
        row.addView(imageView)
        binding.tableLayout.addView(row)
    }
}
