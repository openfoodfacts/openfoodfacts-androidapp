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
package openfoodfacts.github.scrachx.openfood.features.shared

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.canhub.cropper.CropImage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.listeners.OnRefreshListener
import openfoodfacts.github.scrachx.openfood.listeners.OnRefreshViewListener
import openfoodfacts.github.scrachx.openfood.models.ProductState
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File

abstract class BaseFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, OnRefreshViewListener {

    private val cameraPermissionRequestLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission())
    { isGranted ->
        if (isGranted) {
            // Callback
            doOnPhotosPermissionGranted()
        } else {
            // Tell the user how to give permission
            MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.permission_title)
                .setMessage(R.string.permission_denied)
                .setNegativeButton(R.string.txtNo) { d, _ -> d.dismiss() }
                .setPositiveButton(R.string.txtYes) { _, _ ->
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", requireActivity().packageName, null)
                    })
                }
                .show()
        }
    }
    private var refreshListener: OnRefreshListener? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh)
        swipeRefreshLayout?.setOnRefreshListener(this)
    }

    @CallSuper
    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnRefreshListener) {
            refreshListener = context
        }
    }

    @CallSuper
    override fun onRefresh() {
        refreshListener?.onRefresh()
    }


    @CallSuper
    override fun refreshView(productState: ProductState) {
        swipeRefreshLayout?.isRefreshing = false
    }

    protected fun doChooseOrTakePhotos() {
        if (canTakePhotos()) {
            EasyImage.openCamera(this, 0)
            return
        }
        // Ask for permissions
        cameraPermissionRequestLauncher.launch(Manifest.permission.CAMERA)
    }

    protected open fun doOnPhotosPermissionGranted() = Unit

    protected fun cropRotateImage(uri: Uri, title: String?) {
        CropImage.activity(uri)
            .setCropMenuCropButtonIcon(R.drawable.ic_check_white_24dp)
            .setMinCropResultSize(MIN_CROP_RESULT_WIDTH_ACCEPTED_BY_OFF, MIN_CROP_RESULT_HEIGHT_ACCEPTED_BY_OFF)
            .setInitialCropWindowPaddingRatio(0f)
            .setAllowFlipping(false)
            .setAllowRotation(true)
            .setAllowCounterRotation(true)
            .apply { if (title != null) setActivityTitle(title) }
            .start(requireContext(), this)
    }

    protected fun cropRotateImage(image: File, title: String?) = cropRotateImage(image.toUri(), title)

    private fun canTakePhotos() =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PERMISSION_GRANTED

    companion object {
        /**
         * An image height can't be less than 160.
         *
         * [See this code in ProductOpener](https://github.com/openfoodfacts/openfoodfacts-server/blob/5bee6b8d3cad19bedd7e4194848682805b90728c/lib/ProductOpener/Images.pm#L577)
         */
        const val MIN_CROP_RESULT_HEIGHT_ACCEPTED_BY_OFF = 160

        /**
         * An image width can't be less than 640.
         *
         * [See this code in ProductOpener](https://github.com/openfoodfacts/openfoodfacts-server/blob/5bee6b8d3cad19bedd7e4194848682805b90728c/lib/ProductOpener/Images.pm#L577)
         */
        const val MIN_CROP_RESULT_WIDTH_ACCEPTED_BY_OFF = 640
    }
}
