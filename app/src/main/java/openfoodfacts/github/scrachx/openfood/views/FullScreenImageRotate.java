package openfoodfacts.github.scrachx.openfood.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.BaseFragment;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

import java.io.File;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class FullScreenImageRotate extends BaseActivity {
    public static final String KEY_IMAGE_URL = "imageurl";
    public static final String KEY_IMAGE_FILE = "imagefile";
    public static final String KEY_CODE = "code";
    public static final String KEY_ID = "id";
    Bitmap imageBitmap;
    int rotationAngle = 0;
    String productCode, productId;
    private OpenFoodAPIClient api;
    @BindView(R.id.rotate_image_left_icon)
    ImageView rotateLeft;
    @BindView(R.id.rotate_image_right_icon)
    ImageView rotateRight;
    @BindView(R.id.imageViewFullScreen)
    PhotoView mPhotoView;
    PhotoViewAttacher mAttacher;
    String imageUrl;
    File imageFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image_rotate);
        ButterKnife.bind(this);
        api = new OpenFoodAPIClient(this);
        Intent intent = getIntent();
        imageUrl = intent.getExtras().getString(KEY_IMAGE_URL);
        imageFile = (File) intent.getExtras().get(KEY_IMAGE_FILE);
        productCode = intent.getExtras().getString(KEY_CODE);
        productId = intent.getExtras().getString(KEY_ID);

        mAttacher = new PhotoViewAttacher(mPhotoView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //delaying the transition until the view has been laid out
            postponeEnterTransition();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Utils.MY_PERMISSIONS_REQUEST_STORAGE);
        } else {
            loadImages();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Utils.MY_PERMISSIONS_REQUEST_STORAGE && BaseFragment.isAllGranted(grantResults)) {
            loadImages();
        }
    }

    private void loadImages() {
        if (isNotEmpty(imageUrl)) {

            try {
                final RequestCreator load = imageFile == null ? Picasso.with(this).load(imageUrl) : Picasso.with(this).load(imageFile);
                load.into(new Target() {
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
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private void rotateImage(int angle) {//rotate the image

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap bitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);
        mPhotoView.setImageBitmap(bitmap);
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
    void rotateRight() {
        rotationAngle += 90;
        rotateImage(rotationAngle);
    }

    @OnClick(R.id.rotate_image_left_icon)
    void rotateLeft() {
        rotationAngle -= 90;
        rotateImage(rotationAngle);
    }

    @OnClick(R.id.check_upload_icon)
    void uploadRotation() {
        api.rotImg(productCode, productId, rotationAngle, this);
    }
}
