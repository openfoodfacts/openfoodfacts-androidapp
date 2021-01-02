package openfoodfacts.github.scrachx.openfood.features

import android.app.AlertDialog
import android.app.Dialog
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import openfoodfacts.github.scrachx.openfood.R

class CameraSelectorDialogFragment : DialogFragment() {
    fun interface CameraSelectorDialogListener {
        fun onCameraSelected(cameraId: Int)
    }

    private var mCameraId = 0
    private var mListener: CameraSelectorDialogListener? = null

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        retainInstance = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val numberOfCameras = Camera.getNumberOfCameras()
        val cameraNames = arrayOfNulls<String>(numberOfCameras)
        var checkedIndex = 0
        for (i in 0 until numberOfCameras) {
            val info = CameraInfo()
            Camera.getCameraInfo(i, info)
            when (info.facing) {
                CameraInfo.CAMERA_FACING_FRONT -> {
                    cameraNames[i] = "Front Facing"
                }
                CameraInfo.CAMERA_FACING_BACK -> {
                    cameraNames[i] = "Rear Facing"
                }
                else -> {
                    cameraNames[i] = "Camera ID: $i"
                }
            }
            if (i == mCameraId) {
                checkedIndex = i
            }
        }
        val builder = AlertDialog.Builder(activity)
        // Set the dialog title
        builder.setTitle(R.string.select_camera)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setSingleChoiceItems(cameraNames, checkedIndex) { _, which ->
                    mCameraId = which
                }
                // Set the action buttons
                .setPositiveButton(R.string.ok_button) { _, _ ->
                    // User clicked OK, so save the mSelectedIndices results somewhere
                    // or return them to the component that opened the dialog
                    mListener?.onCameraSelected(mCameraId)

                }
                .setNegativeButton(R.string.cancel_button) { _, _ -> }
        return builder.create()
    }

    companion object {
        fun newInstance(listener: CameraSelectorDialogListener?, cameraId: Int) = CameraSelectorDialogFragment().apply {
            mCameraId = cameraId
            mListener = listener
        }
    }
}