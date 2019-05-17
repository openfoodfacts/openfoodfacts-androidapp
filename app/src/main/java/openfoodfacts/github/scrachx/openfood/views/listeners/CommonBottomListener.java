package openfoodfacts.github.scrachx.openfood.views.listeners;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan_bottom_nav:
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
                        Intent intent = new Intent(activity, ContinuousScanActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        activity.startActivity(intent);
                    }
                }
                break;
            case R.id.compare_products:
                activity.startActivity((new Intent(activity, ProductComparisonActivity.class)));
                break;
            case R.id.home_page:
            case R.id.home:
                activity.startActivity((new Intent(activity, WelcomeActivity.class)));
                break;
            case R.id.history_bottom_nav:
                activity.startActivity(new Intent(activity, HistoryScanActivity.class));
                break;
            case R.id.my_lists:
                activity.startActivity(new Intent(activity, ProductListsActivity.class));
                break;
//            case R.id.search_product:
//                Intent searchIntent = new Intent(activity, MainActivity.class);
//                searchIntent.putExtra("product_search", true);
//                activity.startActivity(searchIntent);
//                break;
            default:
                return true;
        }
        return true;
    }
}
