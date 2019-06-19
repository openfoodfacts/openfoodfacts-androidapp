package openfoodfacts.github.scrachx.openfood.views.listeners;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.view.MenuItem;
import com.afollestad.materialdialogs.MaterialDialog;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.*;

public class CommonBottomListener implements BottomNavigationView.OnNavigationItemSelectedListener {
    private final Activity activity;
    private final Context context;

    CommonBottomListener(Activity activity, Context context) {
        this.activity = activity;
        this.context = context;
    }

    private boolean isCurrentActivity(Class c){
        return activity!=null && activity.getClass().equals(c);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan_bottom_nav:
                if(isCurrentActivity(ContinuousScanActivity.class)){
                    break;
                }
                if (Utils.isHardwareCameraInstalled(context)) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
                            new MaterialDialog.Builder(context)
                                .title(R.string.action_about)
                                .content(R.string.permission_camera)
                                .neutralText(R.string.txtOk)
                                .onNeutral((dialog, which) -> ActivityCompat
                                    .requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA))
                                .show();
                        } else {
                            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA);
                        }
                    } else {
                        Intent intent = createIntent(ContinuousScanActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        activity.startActivity(intent);
                    }
                }
                break;
            case R.id.compare_products:
                if(isCurrentActivity(ProductComparisonActivity.class)){
                    break;
                }
                activity.startActivity((createIntent( ProductComparisonActivity.class)));
                break;
            case R.id.home_page:
            case R.id.home:
                if(isCurrentActivity(WelcomeActivity.class)||isCurrentActivity(MainActivity.class)){
                    break;
                }
                activity.startActivity((createIntent( MainActivity.class)));
                break;
            case R.id.history_bottom_nav:
                if(isCurrentActivity(HistoryScanActivity.class)){
                    break;
                }
                activity.startActivity(createIntent( HistoryScanActivity.class));
                break;
            case R.id.my_lists:
                if(isCurrentActivity(ProductListsActivity.class)){
                    break;
                }
                activity.startActivity(createIntent( ProductListsActivity.class));
                break;
            default:
                return true;
        }
        return true;
    }

    private Intent createIntent(Class activityClass){
        final Intent intent = new Intent(activity, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }
}
