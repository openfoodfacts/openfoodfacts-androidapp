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

import android.Manifest.permission
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.theartofdev.edmodo.cropper.CropImage
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.listeners.OnRefreshListener
import openfoodfacts.github.scrachx.openfood.features.listeners.OnRefreshView
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.utils.Utils.isAllGranted
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File

abstract class BaseFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, OnRefreshView {
    private val cameraPermissionRequestLauncher = registerForActivityResult(RequestMultiplePermissions())
    { results: Map<String?, Boolean?>? ->
        if (!isAllGranted(results!!)) {
            // Tell the user how to give permission
            MaterialDialog.Builder(requireActivity())
                    .title(R.string.permission_title)
                    .content(R.string.permission_denied)
                    .negativeText(R.string.txtNo)
                    .positiveText(R.string.txtYes)
                    .onPositive { _, _ ->
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package", requireActivity().packageName, null)
                        intent.data = uri
                        startActivity(intent)
                    }
                    .show()
        } else {
            // Callback
            doOnPhotosPermissionGranted()
        }
    }
    private var refreshListener: OnRefreshListener? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRefreshListener) {
            refreshListener = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh)
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout!!.setOnRefreshListener(this)
        }
    }

    protected val isUserLoggedIn: Boolean
        get() = BaseActivity.isUserLoggedIn(activity)
    protected val isUserNotLoggedIn: Boolean
        get() = !isUserLoggedIn

    override fun onRefresh() {
        if (refreshListener != null) {
            refreshListener!!.onRefresh()
        }
    }

    override fun refreshView(productState: ProductState) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout!!.isRefreshing = false
        }
    }

    protected fun doChooseOrTakePhotos(title: String?) {
        if (canTakePhotos()) {
            EasyImage.openCamera(this, 0)
            return
        }
        // Ask for permissions
        cameraPermissionRequestLauncher.launch(arrayOf(permission.CAMERA))
    }

    protected open fun doOnPhotosPermissionGranted() {}
    protected fun cropRotateImage(image: File?, title: String?) {
        val uri = Uri.fromFile(image)
        CropImage.activity(uri)
                .setCropMenuCropButtonIcon(R.drawable.ic_check_white_24dp)
                .setMinCropResultSize(MIN_CROP_RESULT_WIDTH_ACCEPTED_BY_OFF, MIN_CROP_RESULT_HEIGHT_ACCEPTED_BY_OFF)
                .setInitialCropWindowPaddingRatio(0f)
                .setAllowFlipping(false)
                .setAllowRotation(true)
                .setAllowCounterRotation(true)
                .setActivityTitle(title)
                .start(requireContext(), this)
    }

    private fun canTakePhotos(): Boolean {
        return ContextCompat.checkSelfPermission(requireContext(), permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        /**
         * an image height can't be less than 160. See https://github.com/openfoodfacts/openfoodfacts-server/blob/5bee6b8d3cad19bedd7e4194848682805b90728c/lib/ProductOpener/Images.pm#L577
         */
        const val MIN_CROP_RESULT_HEIGHT_ACCEPTED_BY_OFF = 160

        /**
         * an image width can't be less than 640. See https://github.com/openfoodfacts/openfoodfacts-server/blob/5bee6b8d3cad19bedd7e4194848682805b90728c/lib/ProductOpener/Images.pm#L577
         */
        const val MIN_CROP_RESULT_WIDTH_ACCEPTED_BY_OFF = 640
    }
}