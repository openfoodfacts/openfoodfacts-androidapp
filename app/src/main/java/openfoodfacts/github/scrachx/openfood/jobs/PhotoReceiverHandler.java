package openfoodfacts.github.scrachx.openfood.jobs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import com.theartofdev.edmodo.cropper.CropImage;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import java.io.File;
import java.util.List;

public class PhotoReceiverHandler {
    private final PhotoReceiver photoReceiver;
    private boolean cropActionEnabled = false;

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
        onCropResult(requestCode, resultCode, data);
        final FragmentActivity fragmentActivity = fragment == null ? null : fragment.getActivity();
        final Activity mainActivity = activity == null ? fragmentActivity : activity;
        final Context fragmentContext = fragment==null? OFFApplication.getInstance():fragment.getContext();
        final Context mainContext = activity == null ? fragmentContext : activity;
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
                            .setOutputUri(Utils.getOutputPicUri(mainContext))
                            .start(mainContext, fragment);
                    } else {
                        CropImage.activity(Uri.fromFile(imageFiles.get(0)))
                            .setCropMenuCropButtonIcon(R.drawable.ic_check_white_24dp)
                            .setAllowFlipping(false)
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
                       boolean deleted= photoFile.delete();
                       if(!deleted){
                           Log.w(PhotoReceiverHandler.class.getSimpleName(),"photo file not deleted "+photoFile.getAbsolutePath());
                       }
                    }
                }
            }
        });
    }

    private void onCropResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK && result.getUri() != null) {
                Uri resultUri = result.getUri();
                photoReceiver.onPhotoReturned(new File(resultUri.getPath()));
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.w(PhotoReceiverHandler.class.getSimpleName(), "Can't process photo", result.getError());
            }
        }
    }
}
