package openfoodfacts.github.scrachx.openfood.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class FullScreenImageRotate extends BaseActivity {

    Bitmap imageBitmap;

    @BindView(R.id.rotate_image_left_icon)
    ImageView rotateLeft;
    @BindView(R.id.rotate_image_right_icon)
    ImageView rotateRight;
    @BindView(R.id.imageViewFullScreen)
    PhotoView mPhotoView;
    PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_full_screen_image_rotate);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        String imageurl = intent.getExtras().getString("imageurl");

        mAttacher = new PhotoViewAttacher(mPhotoView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //delaying the transition until the view has been laid out
            postponeEnterTransition();
        }
        if (isNotEmpty(imageurl)) {

            Picasso.with(this)
                    .load(imageurl)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            imageBitmap = bitmap;
                            mPhotoView.setImageBitmap(bitmap);
                            mAttacher.update();
                            scheduleStartPostponedTransition(mPhotoView);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
        }


    }

    private void rotateImage(float angle) {

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);
        mPhotoView.setImageBitmap(imageBitmap);

    }

    /*For scheduling a postponed transition after the proper measures of the view are done
    and the view has been properly laid out in the View hierarchy*/
    private void scheduleStartPostponedTransition(final View sharedElement) {
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            startPostponedEnterTransition();
                        }
                        return true;
                    }
                });
    }

    @OnClick(R.id.rotate_image_right_icon)
    void rotateRight(){
        rotateImage(90.0f);
    }

    @OnClick(R.id.rotate_image_left_icon)
    void rotateLeft(){
        rotateImage(-90.0f);
    }

    @OnClick(R.id.check_upload_icon)
    void saveImageAndUpload() {
        if (isStoragePermissionGranted()) {
            File file = storeImage(imageBitmap);
            Intent data = new Intent();
            String path = file.getAbsolutePath();
            data.setData(Uri.parse(path));
            setResult(RESULT_OK, data);
            finish();
        }
    }

    private File storeImage(Bitmap image) {

        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d("Error", "Error creating media file, check storage permissions.");
            return null;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                final Uri contentUri = Uri.fromFile(pictureFile);
                scanIntent.setData(contentUri);
                sendBroadcast(scanIntent);
            } else {
                final Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory()));
                sendBroadcast(intent);
            }
        } catch (FileNotFoundException e) {
            Log.d("Error", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("Error", "Unable to access file: " + e.getMessage());
        }
        return pictureFile;
    }

    private File getOutputMediaFile() {

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data"
                + getApplicationContext().getPackageName()
                + "/Files");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName = "RI_" + timeStamp + ".png";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;

    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("Error","Permission is granted");
                return true;
            } else {

                Log.v("Error","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("Error","Permission is granted");
            return true;
        }
    }


}
