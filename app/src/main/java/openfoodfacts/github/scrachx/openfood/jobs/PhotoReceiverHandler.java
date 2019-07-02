package openfoodfacts.github.scrachx.openfood.jobs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;
import com.theartofdev.edmodo.cropper.CropImage;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.images.PhotoReceiver;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import java.io.File;
import java.util.List;

public class PhotoReceiverHandler {
    private final PhotoReceiver photoReceiver;

    public PhotoReceiverHandler(PhotoReceiver photoReceiver) {
        this.photoReceiver = photoReceiver;
    }

    public void onActivityResult(Fragment fragment, int requestCode, int resultCode, Intent data) {
        onActivityResult(null, fragment, requestCode, resultCode, data);
    }

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        onActivityResult(activity, null, requestCode, resultCode, data);
    }

    public void onActivityResult(Activity activity, Fragment fragment, int requestCode, int resultCode, Intent data) {
        if (onCropResult(requestCode, resultCode, data)) {
            return;
        }
        final FragmentActivity fragmentActivity = fragment == null ? null : fragment.getActivity();
        final Activity mainActivity = activity == null ? fragmentActivity : activity;
        final Context fragmentContext = fragment == null ? OFFApplication.getInstance() : fragment.getContext();
        final Context mainContext = activity == null ? fragmentContext : activity;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(OFFApplication.getInstance());
        final boolean cropActionEnabled = preferences == null ? true : preferences.getBoolean("cropNewImage", true);
        EasyImage.handleActivityResult(requestCode, resultCode, data, mainActivity, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
            }

            @Override
            public void onImagesPicked(List<File> imageFiles, EasyImage.ImageSource source, int type) {
                if (cropActionEnabled) {
                    if (activity == null) {
                        CropImage.activity(Uri.fromFile(imageFiles.get(0)))
                            .setCropMenuCropButtonIcon(R.drawable.ic_check_white_24dp)
                            .setAllowFlipping(false)
                            .setAllowRotation(true)
                            .setAllowCounterRotation(true)
                            .setAutoZoomEnabled(false)
                            .setInitialCropWindowPaddingRatio(0f)
                            .setOutputUri(Utils.getOutputPicUri(mainContext))
                            .start(mainContext, fragment);
                    } else {
                        CropImage.activity(Uri.fromFile(imageFiles.get(0)))
                            .setCropMenuCropButtonIcon(R.drawable.ic_check_white_24dp)
                            .setAllowFlipping(false)
                            .setAllowRotation(true)
                            .setAllowCounterRotation(true)
                            .setAutoZoomEnabled(false)
                            .setInitialCropWindowPaddingRatio(0f)
                            .start(activity);
                    }
                } else {
                    for (File image : imageFiles) {
                        photoReceiver.onPhotoReturned(image);
                    }
                }
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(mainContext);
                    if (photoFile != null) {
                        boolean deleted = photoFile.delete();
                        if (!deleted) {
                            Log.w(PhotoReceiverHandler.class.getSimpleName(), "photo file not deleted " + photoFile.getAbsolutePath());
                        }
                    }
                }
            }
        });
    }

    private boolean onCropResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK && result.getUri() != null) {
                Uri resultUri = result.getUri();
                photoReceiver.onPhotoReturned(new File(resultUri.getPath()));
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.w(PhotoReceiverHandler.class.getSimpleName(), "Can't process photo", result.getError());
            }
            return true;
        }
        return false;
    }
}
