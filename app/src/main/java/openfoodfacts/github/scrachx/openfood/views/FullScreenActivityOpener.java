package openfoodfacts.github.scrachx.openfood.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.view.View;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.images.ImageKeyHelper;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImageField;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import org.apache.commons.lang3.StringUtils;

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
        final Context context = fragment.getContext();
        Intent intent = createIntent(context, product, imageType, mUrlImage);
        if (mImageFront!=null && fragment.getActivity() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
        if (mImageFront!=null && activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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
}
