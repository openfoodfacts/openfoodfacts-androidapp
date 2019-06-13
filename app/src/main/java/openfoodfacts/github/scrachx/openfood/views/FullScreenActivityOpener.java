package openfoodfacts.github.scrachx.openfood.views;

import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.widget.ImageView;
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

    public static void openForUrl(Fragment fragment, Product product, ProductImageField imageType, String mUrlImage, ImageView mImageFront) {
        if (fragment == null) {
            return;
        }
        Intent intent = new Intent(fragment.getContext(), ProductImageManagementActivity.class);
        String language = LocaleHelper.getLanguage(fragment.getContext());
        if (!product.isLanguageSupported(language) && StringUtils.isNotBlank(product.getLang())) {
            language = product.getLang();
        }
        intent.putExtras(ImageKeyHelper.createImageBundle(imageType, product, language, mUrlImage));
        if (fragment.getActivity() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(fragment.getActivity(), mImageFront,
                    fragment.getActivity().getString(R.string.product_transition));
            fragment.startActivityForResult(intent, ProductImageManagementActivity.REQUEST_EDIT_IMAGE, options.toBundle());
        } else {
            fragment.startActivityForResult(intent, ProductImageManagementActivity.REQUEST_EDIT_IMAGE);
        }
    }
}
