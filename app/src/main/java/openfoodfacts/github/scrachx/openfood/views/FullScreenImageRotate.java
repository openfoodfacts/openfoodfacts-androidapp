package openfoodfacts.github.scrachx.openfood.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class FullScreenImageRotate extends BaseActivity {

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image_rotate);
        ButterKnife.bind(this);
        api = new OpenFoodAPIClient( this );
        Intent intent = getIntent();
        String imageurl = intent.getExtras().getString("imageurl");
        productCode = intent.getExtras().getString("code");
        productId = intent.getExtras().getString("id");

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
    void rotateRight(){
        rotationAngle+=90;
        rotateImage(rotationAngle);
    }

    @OnClick(R.id.rotate_image_left_icon)
    void rotateLeft(){
        rotationAngle-=90;
        rotateImage(rotationAngle);
    }

    @OnClick(R.id.check_upload_icon)
    void uploadRotation(){
        api.rotImg(productCode, productId, rotationAngle, this);
    }

}
