package openfoodfacts.github.scrachx.openfood.utils

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.canhub.cropper.CropImage
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.app.OFFApplication
import openfoodfacts.github.scrachx.openfood.utils.Utils.getOutputPicUri
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import pl.aprilapps.easyphotopicker.EasyImage.ImageSource
import java.io.File

/**
 * A class for handling photo receiver
 */
class PhotoReceiverHandler(
    private val sharedPreferences: SharedPreferences,
    private val photoReceiver: (File) -> Unit,
) {
    fun onActivityResult(fragment: Fragment, requestCode: Int, resultCode: Int, data: Intent?) =
        onActivityResult(null, fragment, requestCode, resultCode, data)

    fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) =
        onActivityResult(activity, null, requestCode, resultCode, data)

    private fun onActivityResult(activity: Activity?, fragment: Fragment?, requestCode: Int, resultCode: Int, data: Intent?) {
        if (onCropResult(requestCode, resultCode, data)) return

        val fragmentActivity = fragment?.activity
        val mainActivity = activity ?: fragmentActivity
        val mainContext = activity ?: fragment?.context
        val cropActionEnabled = sharedPreferences.getBoolean("cropNewImage", true)

        EasyImage.handleActivityResult(requestCode, resultCode, data, mainActivity, object : DefaultCallback() {

            override fun onImagesPicked(imageFiles: List<File>, source: ImageSource, type: Int) {
                if (cropActionEnabled) {
                    if (activity == null) {
                        CropImage.activity(imageFiles[0].toUri())
                            .setCropMenuCropButtonIcon(R.drawable.ic_check_white_24dp)
                            .setAllowFlipping(false)
                            .setAllowRotation(true)
                            .setAllowCounterRotation(true)
                            .setAutoZoomEnabled(false)
                            .setInitialCropWindowPaddingRatio(0f)
                            .setOutputUri(getOutputPicUri(mainContext!!))
                            .start(mainContext, fragment!!)
                    } else {
                        CropImage.activity(imageFiles[0].toUri())
                            .setCropMenuCropButtonIcon(R.drawable.ic_check_white_24dp)
                            .setAllowFlipping(false)
                            .setAllowRotation(true)
                            .setAllowCounterRotation(true)
                            .setAutoZoomEnabled(false)
                            .setInitialCropWindowPaddingRatio(0f)
                            .start(activity)
                    }
                } else {
                    imageFiles.forEach { photoReceiver(it) }
                }
            }

            override fun onCanceled(source: ImageSource, type: Int) {
                // Cancel handling, you might wanna remove taken photo if it was canceled
                if (source != ImageSource.CAMERA) return
                val photoFile = EasyImage.lastlyTakenButCanceledPhoto(mainContext) ?: return
                if (photoFile.delete()) return
                Log.w(LOG_TAG, "Cannot delete photo file ${photoFile.absolutePath}")
            }
        })
    }

    /**
     * A method called after cropping the image to process that image.
     */
    private fun onCropResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) return false
        val result = CropImage.getActivityResult(data) ?: return true

        when (resultCode) {
            RESULT_OK -> {
                if (result.uriContent != null) {

                    // FIXME: Find a method to not use context
                    photoReceiver(File(result.getUriFilePath(OFFApplication._instance)))

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Log.w(LOG_TAG, "Can't process photo", result.error)
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                Log.w(LOG_TAG, "Can't process photo", result.error)
            }
        }
        return true
    }

    companion object {
        val LOG_TAG = PhotoReceiverHandler::class.simpleName
    }
}
