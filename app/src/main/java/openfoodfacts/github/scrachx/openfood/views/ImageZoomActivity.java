package openfoodfacts.github.scrachx.openfood.views;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.github.chrisbanes.photoview.PhotoViewAttacher;
import com.squareup.picasso.Callback;

import java.util.Objects;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.ActivityZoomImageBinding;
import openfoodfacts.github.scrachx.openfood.images.ImageKeyHelper;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

/**
 * Activity to display/edit product images
 */
public class ImageZoomActivity extends BaseActivity {
    private ActivityZoomImageBinding binding;
    private PhotoViewAttacher mAttacher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityZoomImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();

        mAttacher = new PhotoViewAttacher(binding.imageViewFullScreen);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //delaying the transition until the view has been laid out
            postponeEnterTransition();
        }

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setTitle(R.string.imageFullscreen);

        loadImage(intent.getStringExtra(ImageKeyHelper.IMAGE_URL));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        binding.toolbar.setTitle(R.string.imageFullscreen);
        super.onResume();
    }

    private void loadImage(String imageUrl) {
        if (isNotEmpty(imageUrl)) {
            startRefresh(getString(R.string.txtLoading));
            Utils.picassoBuilder(this)
                .load(imageUrl)
                .into(binding.imageViewFullScreen, new Callback() {
                    @Override
                    public void onSuccess() {
                        // Activity could have been destroyed while we load the image
                        if (binding == null) {
                            return;
                        }
                        mAttacher.update();
                        scheduleStartPostponedTransition(binding.imageViewFullScreen);
                        binding.imageViewFullScreen.setVisibility(View.VISIBLE);
                        stopRefresh();
                    }

                    @Override
                    public void onError(Exception ex) {
                        // Activity could have been destroyed while we load the image
                        if (binding == null) {
                            return;
                        }
                        binding.imageViewFullScreen.setVisibility(View.VISIBLE);
                        Toast.makeText(ImageZoomActivity.this, getResources().getString(R.string.txtConnectionError), Toast.LENGTH_LONG).show();
                        stopRefresh();
                    }
                });
        } else {
            binding.imageViewFullScreen.setImageDrawable(null);
            stopRefresh();
        }
    }

    private void stopRefresh() {
        binding.progressBar.setVisibility(View.GONE);
        binding.textInfo.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        binding = null;
        super.onDestroy();
    }

    private void startRefresh(String text) {
        binding.progressBar.setVisibility(View.VISIBLE);
        if (text != null) {
            binding.textInfo.setTextColor(ContextCompat.getColor(this, R.color.white));
            binding.textInfo.setText(text);
        }
    }

    /**
     * For scheduling a postponed transition after the proper measures of the view are done
     * and the view has been properly laid out in the View hierarchy
     */
    private void scheduleStartPostponedTransition(@NonNull final View sharedElement) {
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
