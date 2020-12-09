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
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TableRow
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.FragmentAddProductPhotosBinding
import openfoodfacts.github.scrachx.openfood.features.shared.BaseFragment
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.models.entities.OfflineSavedProduct
import openfoodfacts.github.scrachx.openfood.utils.MY_PERMISSIONS_REQUEST_CAMERA
import openfoodfacts.github.scrachx.openfood.utils.PhotoReceiverHandler
import openfoodfacts.github.scrachx.openfood.utils.dpsToPixel
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File

/**
 * Fragment for adding photos of the product
 *
 * @see R.layout.fragment_add_product_photos
 */
class ProductEditPhotosFragment : BaseFragment() {
    private var _binding: FragmentAddProductPhotosBinding? = null
    private val binding get() = _binding!!
    private var photoReceiverHandler: PhotoReceiverHandler? = null
    private var code: String? = null
    private var activity: Activity? = null
    private var photoFile: File? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddProductPhotosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAddOtherImage.setOnClickListener { addOtherImage() }
        binding.btnAdd.setOnClickListener { next() }
        photoReceiverHandler = PhotoReceiverHandler { newPhotoFile: File? ->
            photoFile = newPhotoFile
            val image = ProductImage(code, ProductImageField.OTHER, photoFile)
            image.filePath = photoFile!!.toURI().path
            if (activity is ProductEditActivity) {
                (activity as ProductEditActivity).addToPhotoMap(image, 4)
            }
        }
        val b = arguments
        if (b != null) {
            val product = b.getSerializable("product") as Product?
            val offlineSavedProduct = b.getSerializable("edit_offline_product") as OfflineSavedProduct?
            val editionMode = b.getBoolean(ProductEditActivity.KEY_IS_EDITING)
            if (product != null) {
                code = product.code
            }
            if (editionMode && product != null) {
                binding.btnAdd.setText(R.string.save_edits)
            } else if (offlineSavedProduct != null) {
                code = offlineSavedProduct.barcode
            }
        } else {
            Toast.makeText(activity, R.string.error_adding_product_photos, Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun addOtherImage() {
        if (ContextCompat.checkSelfPermission(requireActivity(), permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
        } else {
            EasyImage.openCamera(this, 0)
        }
    }

    private operator fun next() {
        val fragmentActivity = getActivity()
        if (fragmentActivity is ProductEditActivity) {
            fragmentActivity.proceed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoReceiverHandler!!.onActivityResult(this, requestCode, resultCode, data)
    }

    fun showImageProgress() {
        binding.imageProgress.visibility = View.VISIBLE
        binding.imageProgressText.visibility = View.VISIBLE
        binding.imageProgressText.setText(R.string.toastSending)
        addImageRow()
    }

    fun hideImageProgress(errorUploading: Boolean, message: String?) {
        binding.imageProgress.visibility = View.GONE
        binding.btnAddOtherImage.visibility = View.VISIBLE
        if (errorUploading) {
            binding.imageProgressText.visibility = View.GONE
        } else {
            binding.imageProgressText.setText(R.string.image_uploaded_successfully)
        }
    }

    /**
     * Load image into the image view and add it to tableLayout
     */
    private fun addImageRow() {
        val image = TableRow(activity)
        val lp = TableRow.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpsToPixel(100, getActivity()))
        lp.topMargin = dpsToPixel(10, getActivity())
        val imageView = ImageView(activity)
        Picasso.get()
                .load(photoFile!!)
                .resize(dpsToPixel(100, getActivity()), dpsToPixel(100, getActivity()))
                .centerInside()
                .into(imageView)
        imageView.adjustViewBounds = true
        imageView.scaleType = ImageView.ScaleType.FIT_CENTER
        imageView.layoutParams = lp
        image.addView(imageView)
        binding.tableLayout.addView(image)
    }
}