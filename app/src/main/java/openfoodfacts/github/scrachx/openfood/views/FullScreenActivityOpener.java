package openfoodfacts.github.scrachx.openfood.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.widget.Toast;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.images.ImageKeyHelper;
import openfoodfacts.github.scrachx.openfood.images.ImageSize;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImageField;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.FileUtils;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import org.apache.commons.lang.StringUtils;

/**
 * Used to open fullscreen activity
 */
public class FullScreenActivityOpener {
    private FullScreenActivityOpener() {

    }

    public static void openForUrl(Fragment fragment, Product product, ProductImageField imageType, String mUrlImage, View mImageFront) {
        if (fragment == null) {
            return;
        }
        //a new file added just now
        if (FileUtils.isAbsolute(mUrlImage)) {
            loadImageServerUrl(fragment, product, imageType, mImageFront);
            return;
        }
        final Context context = fragment.getContext();
        Intent intent = createIntent(context, product, imageType, mUrlImage);
        startActivity(fragment, mImageFront, intent);
    }

    private static void startActivity(Fragment fragment, View mImageFront, Intent intent) {
        if (mImageFront != null && fragment.getActivity() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(fragment.getActivity(), mImageFront,
                    fragment.getActivity().getString(R.string.product_transition));
            fragment.startActivityForResult(intent, ProductImageManagementActivity.REQUEST_EDIT_IMAGE, options.toBundle());
        } else {
            fragment.startActivityForResult(intent, ProductImageManagementActivity.REQUEST_EDIT_IMAGE);
        }
    }

    public static void openForUrl(Activity activity, Product product, ProductImageField imageType, String mUrlImage, View mImageFront) {
        if (activity == null) {
            return;
        }
        final Context context = activity.getBaseContext();
        Intent intent = createIntent(context, product, imageType, mUrlImage);
        startActivity(activity, mImageFront, intent);
    }

    public static void openZoom(Activity activity, String mUrlImage, View mImageFront) {
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(activity, ImageZoomActivity.class);
        intent.putExtra(ImageKeyHelper.IMAGE_URL, mUrlImage);
        startActivity(activity, mImageFront, intent);
    }
    public static void openZoom(Fragment activity, String mUrlImage, View mImageFront) {
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(activity.getContext(), ImageZoomActivity.class);
        intent.putExtra(ImageKeyHelper.IMAGE_URL, mUrlImage);
        startActivity(activity, mImageFront, intent);
    }

    private static void startActivity(Activity activity, View mImageFront, Intent intent) {
        if (mImageFront != null && activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(activity, mImageFront,
                    activity.getString(R.string.product_transition));
            activity.startActivityForResult(intent, ProductImageManagementActivity.REQUEST_EDIT_IMAGE, options.toBundle());
        } else {
            activity.startActivityForResult(intent, ProductImageManagementActivity.REQUEST_EDIT_IMAGE);
        }
    }

    private static Intent createIntent(Context context, Product product, ProductImageField imageType, String mUrlImage) {
        Intent intent = new Intent(context, ProductImageManagementActivity.class);
        String language = LocaleHelper.getLanguage(context);
        if (!product.isLanguageSupported(language) && StringUtils.isNotBlank(product.getLang())) {
            language = product.getLang();
        }
        intent.putExtras(ImageKeyHelper.createImageBundle(imageType, product, language, mUrlImage));
        return intent;
    }

    private static void loadImageServerUrl(Fragment fragment, Product product, ProductImageField imageType, View mImageFront) {
        OpenFoodAPIClient client = new OpenFoodAPIClient(fragment.getContext());
        client.getProductImages(product.getCode(), newState -> {
            final Product newStateProduct = newState.getProduct();
            if (newStateProduct != null) {
                String language = LocaleHelper.getLanguage(fragment.getContext());
                String imageUrl = newStateProduct.getSelectedImage(language, imageType, ImageSize.DISPLAY);
                if (StringUtils.isNotBlank(imageUrl)) {
                    openForUrl(fragment, newStateProduct, imageType, imageUrl, mImageFront);
                } else {
                    Toast.makeText(fragment.getContext(), R.string.cant_edit_image_not_yet_uploaded, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
