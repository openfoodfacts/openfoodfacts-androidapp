package openfoodfacts.github.scrachx.openfood.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import openfoodfacts.github.scrachx.openfood.R;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class FullScreenImage extends BaseActivity {

    @BindView(R.id.imageViewFullScreen)
    PhotoView mPhotoView;
    PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_full_screen_image);

        Intent intent = getIntent();
        String imageurl = intent.getExtras().getString("imageurl");

        mAttacher = new PhotoViewAttacher(mPhotoView);

        if (isNotEmpty(imageurl)) {
            Picasso.with(this)
                    .load(imageurl)
                    .into(mPhotoView, new Callback() {
                        @Override
                        public void onSuccess() {
                            mAttacher.update();
                        }

                        @Override
                        public void onError() {
                        }
                    });
        }
    }
}
