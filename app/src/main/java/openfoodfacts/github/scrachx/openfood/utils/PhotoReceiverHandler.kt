package openfoodfacts.github.scrachx.openfood.utils

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.theartofdev.edmodo.cropper.CropImage
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
class PhotoReceiverHandler(private val photoReceiver: (File) -> Unit) {
    fun onActivityResult(fragment: Fragment?, requestCode: Int, resultCode: Int, data: Intent?) =
            onActivityResult(null, fragment, requestCode, resultCode, data)

    fun onActivityResult(activity: Activity?, requestCode: Int, resultCode: Int, data: Intent?) =
            onActivityResult(activity, null, requestCode, resultCode, data)

    fun onActivityResult(activity: Activity?, fragment: Fragment?, requestCode: Int, resultCode: Int, data: Intent?) {
        if (onCropResult(requestCode, resultCode, data)) return

        val fragmentActivity = fragment?.activity
        val mainActivity = activity ?: fragmentActivity
        val fragmentContext = fragment?.requireContext() ?: OFFApplication.instance
        val mainContext = activity ?: fragmentContext
        val preferences = PreferenceManager.getDefaultSharedPreferences(OFFApplication.instance)
        val cropActionEnabled = preferences == null || preferences.getBoolean("cropNewImage", true)


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
                                .setOutputUri(getOutputPicUri(mainContext))
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

        val result = CropImage.getActivityResult(data)
        if (resultCode == Activity.RESULT_OK && result.uri != null) {
            photoReceiver(result.uri.toFile())
        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            Log.w(LOG_TAG, "Can't process photo", result.error)
        }
        return true
    }

    companion object {
        val LOG_TAG = PhotoReceiverHandler::class.simpleName
    }
}