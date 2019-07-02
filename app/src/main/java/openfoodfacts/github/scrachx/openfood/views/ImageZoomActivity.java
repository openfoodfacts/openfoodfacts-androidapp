package openfoodfacts.github.scrachx.openfood.views;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.images.ImageKeyHelper;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * Activity to display/edit product images
 */
public class ImageZoomActivity extends BaseActivity {
    @BindView(R.id.imageViewFullScreen)
    PhotoView mPhotoView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.textInfo)
    TextView textInfo;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    private PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zoom_image);

        Intent intent = getIntent();

        mAttacher = new PhotoViewAttacher(mPhotoView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //delaying the transition until the view has been laid out
            postponeEnterTransition();
        }
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setTitle(R.string.imageFullscreen);
        loadImage(intent.getStringExtra(ImageKeyHelper.IMAGE_URL));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        toolbar.setTitle(R.string.imageFullscreen);
        super.onResume();
    }

    private void loadImage(String imageUrl) {
        if (isNotEmpty(imageUrl)) {
            startRefresh(getString(R.string.txtLoading));
            Picasso.get()
                .load(imageUrl)
                .into(mPhotoView, new Callback() {
                    @Override
                    public void onSuccess() {
                        mAttacher.update();
                        scheduleStartPostponedTransition(mPhotoView);
                        mPhotoView.setVisibility(View.VISIBLE);
                        stopRefresh();
                    }

                    @Override
                    public void onError(Exception ex) {
                        mPhotoView.setVisibility(View.VISIBLE);
                        Toast.makeText(ImageZoomActivity.this, getResources().getString(R.string.txtConnectionError), Toast.LENGTH_LONG).show();
                        stopRefresh();
                    }
                });
        } else {
            mPhotoView.setImageDrawable(null);
            stopRefresh();
        }
    }

    private void stopRefresh() {
        progressBar.setVisibility(View.GONE);
        textInfo.setVisibility(View.GONE);
    }


    private void startRefresh(String text) {
        progressBar.setVisibility(View.VISIBLE);
        if (text != null) {
            textInfo.setTextColor(ContextCompat.getColor(this, R.color.white));
            textInfo.setText(text);
        }
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
}
