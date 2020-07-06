/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.BaseActivity;
import openfoodfacts.github.scrachx.openfood.views.listeners.OnRefreshListener;
import openfoodfacts.github.scrachx.openfood.views.listeners.OnRefreshView;
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
    private OnRefreshListener refreshListener;
    private SwipeRefreshLayout swipeRefreshLayout;

    public BaseFragment() {
        super();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnRefreshListener) {
            refreshListener = (OnRefreshListener) context;
        }
    }

    int dpsToPixels(int dps) {
        return Utils.dpsToPixel(dps, getActivity());
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

    protected void doChooseOrTakePhotos(String title) {
        if (!canTakePhotos()) {
            requestPermissions(new String[]{CAMERA}, MY_PERMISSIONS_REQUEST_CAMERA);
        } else {
            EasyImage.openCamera(this, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_CAMERA) {
            if (!Utils.isAllGranted(grantResults)) {
                new MaterialDialog.Builder(requireActivity())
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
            .start(requireContext(), this);
    }

    protected boolean canTakePhotos() {
        return ContextCompat.checkSelfPermission(requireContext(), CAMERA) == PERMISSION_GRANTED;
    }
}
