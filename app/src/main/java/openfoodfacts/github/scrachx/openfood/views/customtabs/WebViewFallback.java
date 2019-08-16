package openfoodfacts.github.scrachx.openfood.views.customtabs;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * Fallback of customtab with a standard web view
 */
public class WebViewFallback implements CustomTabActivityHelper.CustomTabFallback {

    @Override
    public void openUri(Activity activity, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            activity.startActivity(intent);
        } catch (Exception e) {
            Log.e(WebViewFallback.class.getSimpleName(),"can't start activity"+activity.getClass().getName(),e);
        }
    }
}
