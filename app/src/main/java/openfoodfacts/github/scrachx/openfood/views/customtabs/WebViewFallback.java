package openfoodfacts.github.scrachx.openfood.views.customtabs;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

/**
 * Fallback of customtab with a standard web view
 */
public class WebViewFallback implements CustomTabActivityHelper.CustomTabFallback {

    @Override
    public void openUri(Activity activity, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        activity.startActivity(intent);
    }
}
