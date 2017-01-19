package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import openfoodfacts.github.scrachx.openfood.R;

public class CameraSelectorDialogFragment extends DialogFragment {

    public interface CameraSelectorDialogListener {
        void onCameraSelected(int cameraId);
    }

    private int mCameraId;
    private CameraSelectorDialogListener mListener;

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setRetainInstance(true);
    }

    public static CameraSelectorDialogFragment newInstance(CameraSelectorDialogListener listener, int cameraId) {
        CameraSelectorDialogFragment fragment = new CameraSelectorDialogFragment();
        fragment.mCameraId = cameraId;
        fragment.mListener = listener;
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if(mListener == null) {
            dismiss();
            return null;
        }

        int numberOfCameras = Camera.getNumberOfCameras();
        String[] cameraNames = new String[numberOfCameras];
        int checkedIndex = 0;

        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraNames[i] = "Front Facing";
            } else if(info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraNames[i] = "Rear Facing";
            } else {
                cameraNames[i] = "Camera ID: " + i;
            }
            if(i == mCameraId) {
                checkedIndex = i;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title
        builder.setTitle(R.string.select_camera)
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setSingleChoiceItems(cameraNames, checkedIndex,
                        (dialog, which) -> {
                            mCameraId = which;
                        })
                // Set the action buttons
                .setPositiveButton(R.string.ok_button, (dialog, id) -> {
                    // User clicked OK, so save the mSelectedIndices results somewhere
                    // or return them to the component that opened the dialog
                    if (mListener != null) {
                        mListener.onCameraSelected(mCameraId);
                    }
                })
                .setNegativeButton(R.string.cancel_button, (dialog, id) -> {
                });

        return builder.create();
    }
}

