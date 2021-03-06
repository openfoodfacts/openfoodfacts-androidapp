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
import io.reactivex.disposables.CompositeDisposable
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.listeners.OnRefreshListener
import openfoodfacts.github.scrachx.openfood.features.listeners.OnRefreshView
import openfoodfacts.github.scrachx.openfood.models.ProductState
import openfoodfacts.github.scrachx.openfood.utils.isAllGranted
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File

abstract class BaseFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, OnRefreshView {

    private val cameraPermissionRequestLauncher = registerForActivityResult(RequestMultiplePermissions())
    { results ->
        if (isAllGranted(results)) {
            // Callback
            doOnPhotosPermissionGranted()
        } else {
            // Tell the user how to give permission
            MaterialDialog.Builder(requireActivity())
                    .title(R.string.permission_title)
                    .content(R.string.permission_denied)
                    .negativeText(R.string.txtNo)
                    .positiveText(R.string.txtYes)
                    .onPositive { _, _ ->
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

    protected val disp = CompositeDisposable()

    /**
     * Dispose [disp] and then call super
     */
    override fun onDestroyView() {
        super.onDestroyView()
        disp.dispose()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh)
        swipeRefreshLayout?.setOnRefreshListener(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRefreshListener) {
            refreshListener = context
        }
    }

    override fun onRefresh() {
        refreshListener?.onRefresh()
    }


    override fun refreshView(productState: ProductState) {
        swipeRefreshLayout?.isRefreshing = false
    }

    protected fun doChooseOrTakePhotos() {
        if (canTakePhotos()) {
            EasyImage.openCamera(this, 0)
            return
        }
        // Ask for permissions
        cameraPermissionRequestLauncher.launch(arrayOf(permission.CAMERA))
    }

    protected open fun doOnPhotosPermissionGranted() = Unit

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

    private fun canTakePhotos() =
            ContextCompat.checkSelfPermission(requireContext(), permission.CAMERA) == PackageManager.PERMISSION_GRANTED

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
