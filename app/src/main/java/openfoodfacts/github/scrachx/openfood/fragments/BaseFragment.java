package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;

import butterknife.ButterKnife;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.views.BaseActivity;
import openfoodfacts.github.scrachx.openfood.views.LoginActivity;
import openfoodfacts.github.scrachx.openfood.views.listeners.OnRefreshListener;
import openfoodfacts.github.scrachx.openfood.views.listeners.OnRefreshView;
import openfoodfacts.github.scrachx.openfood.views.product.ProductFragment;
import pl.aprilapps.easyphotopicker.EasyImage;

import static android.Manifest.permission.CAMERA;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_CAMERA;

public abstract class BaseFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, OnRefreshView {
    /**
     * an image height can't be less than 160. See https://github.com/openfoodfacts/openfoodfacts-server/blob/5bee6b8d3cad19bedd7e4194848682805b90728c/lib/ProductOpener/Images.pm#L577
     */
    public static final int MIN_CROP_RESULT_HEIGHT_ACCEPTED_BY_OFF = 160;
    /**
     * an image width can't be less than 640. See https://github.com/openfoodfacts/openfoodfacts-server/blob/5bee6b8d3cad19bedd7e4194848682805b90728c/lib/ProductOpener/Images.pm#L577
     */
    public static final int MIN_CROP_RESULT_WIDTH_ACCEPTED_BY_OFF = 640;
    private SwipeRefreshLayout swipeRefreshLayout;
    private OnRefreshListener refreshListener;

    public BaseFragment() {
        super();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRefreshListener) {
            refreshListener = (OnRefreshListener) context;
        }
    }

    int dpsToPixels(int dps) {
        return BaseActivity.dpsToPixel(dps, getActivity());
    }

    public View createView(LayoutInflater inflater, ViewGroup container, int layoutId) {
        View view = inflater.inflate(layoutId, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this);
        }
    }

    protected boolean isUserLoggedIn() {
        return BaseActivity.isUserLoggedIn(getActivity());
    }

    protected boolean isUserNotLoggedIn() {
        return !isUserLoggedIn();
    }

    protected void startLoginToEditAnd(int requestCode) {
        final Context context = getContext();
        if (context == null) {
            return;
        }
        new MaterialDialog.Builder(context)
            .title(R.string.sign_in_to_edit)
            .positiveText(R.string.txtSignIn)
            .negativeText(R.string.dialog_cancel)
            .onPositive((dialog, which) -> {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivityForResult(intent, requestCode);
                dialog.dismiss();
            })
            .onNegative((dialog, which) -> dialog.dismiss())
            .build().show();
    }

    @Override
    public void onRefresh() {
        if (refreshListener != null) {
            refreshListener.onRefresh();
        }
    }

    @Override
    public void refreshView(State state) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    protected State getStateFromActivityIntent() {
        if (getActivity() != null) {
            Intent intent = getActivity().getIntent();
            if (intent != null && intent.getExtras() != null && intent.getExtras().getSerializable("state") != null) {
                return (State) intent.getExtras().getSerializable("state");
            }
        }
        return ProductFragment.productState;
    }

    protected void doChooseOrTakePhotos(String title) {
        if (!canTakePhotos()) {
            requestPermissionTakePhotos();
        } else {
            EasyImage.openCamera(this, 0);
        }
    }

    public static boolean isAllGranted(@NonNull int[] grantResults) {
        if (grantResults.length == 0) {
            return false;
        }
        for (int result : grantResults
        ) {
            if (result != PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            if (!isAllGranted(grantResults)) {
                new MaterialDialog.Builder(getActivity())
                    .title(R.string.permission_title)
                    .content(R.string.permission_denied)
                    .negativeText(R.string.txtNo)
                    .positiveText(R.string.txtYes)
                    .onPositive((dialog, which) -> {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    })
                    .show();
            } else {
                doOnPhotosPermissionGranted();
            }
        }
    }

    protected void doOnPhotosPermissionGranted() {

    }

    protected void cropRotateImage(File image, String title) {
        Uri uri = Uri.fromFile(image);
        CropImage.activity(uri)
            .setCropMenuCropButtonIcon(R.drawable.ic_check_white_24dp)
            .setMinCropResultSize(MIN_CROP_RESULT_WIDTH_ACCEPTED_BY_OFF, MIN_CROP_RESULT_HEIGHT_ACCEPTED_BY_OFF)
            .setInitialCropWindowPaddingRatio(0)
            .setAllowFlipping(false)
            .setAllowRotation(true)
            .setAllowCounterRotation(true)
            .setActivityTitle(title)
            .start(getContext(), this);
    }

    protected boolean canTakePhotos() {
        return
            ContextCompat.checkSelfPermission(getContext(), CAMERA) == PERMISSION_GRANTED;
    }

    protected void requestPermissionTakePhotos() {
        requestPermissions(new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
    }
}
