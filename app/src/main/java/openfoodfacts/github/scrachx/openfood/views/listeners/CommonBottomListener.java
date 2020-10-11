package openfoodfacts.github.scrachx.openfood.views.listeners;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.HomeFragment;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.ContinuousScanActivity;
import openfoodfacts.github.scrachx.openfood.views.HistoryScanActivity;
import openfoodfacts.github.scrachx.openfood.views.MainActivity;
import openfoodfacts.github.scrachx.openfood.views.ProductComparisonActivity;
import openfoodfacts.github.scrachx.openfood.views.ProductListsActivity;
import openfoodfacts.github.scrachx.openfood.views.WelcomeActivity;

public class CommonBottomListener implements BottomNavigationView.OnNavigationItemSelectedListener {
    private final Activity currentActivity;

    CommonBottomListener(Activity activity) {
        this.currentActivity = activity;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan_bottom_nav:
                openScanActivity();
                break;
            case R.id.compare_products:
                if (isCurrentActivity(ProductComparisonActivity.class)) {
                    break;
                }
                currentActivity.startActivity((createIntent(ProductComparisonActivity.class)));
                break;
            case R.id.home_page:
            case R.id.home:
                if (isCurrentActivity(WelcomeActivity.class) || isCurrentActivity(MainActivity.class)) {
                    ((FragmentActivity) currentActivity).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).addToBackStack(null)
                        .commit();
                    break;
                }
                currentActivity.startActivity((createIntent(MainActivity.class)));
                break;
            case R.id.history_bottom_nav:
                if (isCurrentActivity(HistoryScanActivity.class)) {
                    break;
                }
                currentActivity.startActivity(createIntent(HistoryScanActivity.class));
                break;
            case R.id.my_lists:
                if (isCurrentActivity(ProductListsActivity.class)) {
                    break;
                }
                currentActivity.startActivity(createIntent(ProductListsActivity.class));
                break;
            default:
                return true;
        }
        return true;
    }

    private void openScanActivity() {
        // If already on the continuous scan activity, just lower the bottom sheet
        if (isCurrentActivity(ContinuousScanActivity.class)) {
            ((ContinuousScanActivity) currentActivity).collapseBottomSheet();
            return;
        }
        // If no camera is installed, alert the user
        if (!Utils.isHardwareCameraInstalled(currentActivity)) {
            new MaterialDialog.Builder(currentActivity)
                .title(R.string.no_camera_dialog_title)
                .content(R.string.no_camera_dialog_content)
                .neutralText(R.string.txtOk)
                .show();
            return;
        }
        // Otherwise check permissions and go to continuous scan activity
        if (ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (currentActivity.hasWindowFocus() && ActivityCompat.shouldShowRequestPermissionRationale(currentActivity, Manifest.permission.CAMERA)) {
                new MaterialDialog.Builder(currentActivity)
                    .title(R.string.action_about)
                    .content(R.string.permission_camera)
                    .neutralText(R.string.txtOk)
                    .onNeutral((dialog, which) -> ActivityCompat
                        .requestPermissions(currentActivity, new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA))
                    .show();
            } else {
                ActivityCompat.requestPermissions(currentActivity, new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA);
            }
        } else {
            Intent intent = createIntent(ContinuousScanActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            currentActivity.startActivity(intent);
        }
    }

    ////////////////////
    // Utility functions

    private boolean isCurrentActivity(Class<? extends Activity> activityClass) {
        return currentActivity != null && currentActivity.getClass().equals(activityClass);
    }

    private Intent createIntent(Class<? extends Activity> activityClass) {
        final Intent intent = new Intent(currentActivity, activityClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        return intent;
    }
}
