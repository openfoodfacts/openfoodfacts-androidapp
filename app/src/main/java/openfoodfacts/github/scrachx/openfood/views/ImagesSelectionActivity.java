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

package openfoodfacts.github.scrachx.openfood.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductImagesListBinding;
import openfoodfacts.github.scrachx.openfood.fragments.ProductPhotosFragment;
import openfoodfacts.github.scrachx.openfood.images.ImageKeyHelper;
import openfoodfacts.github.scrachx.openfood.images.ImageNameJsonParser;
import openfoodfacts.github.scrachx.openfood.images.PhotoReceiver;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.PhotoReceiverHandler;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductImagesSelectionAdapter;
import pl.aprilapps.easyphotopicker.EasyImage;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_STORAGE;

public class ImagesSelectionActivity extends BaseActivity implements PhotoReceiver {
    static final String TOOLBAR_TITLE = "TOOLBAR_TITLE";
    private ProductImagesSelectionAdapter adapter;
    private ActivityProductImagesListBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenFoodAPIClient openFoodAPIClient = new OpenFoodAPIClient(this);
        binding = ActivityProductImagesListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.closeZoom.setOnClickListener(v -> onCloseZoom());
        binding.expandedImage.setOnClickListener(v -> onClickOnExpandedImage());
        binding.btnAcceptSelection.setOnClickListener(v -> onBtnAcceptSelection());
        binding.btnChooseImage.setOnClickListener(v -> onBtnChooseImage());

        binding.btnChooseImage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_photo_library, 0, 0, 0);

        Intent intent = getIntent();

        String code = intent.getStringExtra(ImageKeyHelper.PRODUCT_BARCODE);
        binding.toolbar.setTitle(intent.getStringExtra(TOOLBAR_TITLE));

        openFoodAPIClient.getImages(code, (value, response) -> {

            if (value && response != null) {

                // a json object referring to base json object
                JSONObject jsonObject = Utils.createJsonObject(response);

                // a json object referring to images
                JSONObject images = null;
                try {
                    images = jsonObject.getJSONObject("product").getJSONObject("images");
                } catch (JSONException e) {
                    Log.w(ProductPhotosFragment.class.getSimpleName(), "can't get product / images in json", e);
                }
                List<String> imageNames = ImageNameJsonParser.extractImagesNameSortedByUploadTimeDesc(images);

                setSupportActionBar(binding.toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }

                //Check if user is logged in
                adapter = new ProductImagesSelectionAdapter(this, imageNames, code, position -> imageSelected());

                binding.imagesRecycler.setAdapter(adapter);
                binding.imagesRecycler.setLayoutManager(new GridLayoutManager(this, 3));
            }
        });
    }

    private void imageSelected() {
        final int selectedPosition = adapter.getSelectedPosition();
        if (selectedPosition >= 0) {
            String finalUrlString = adapter.getImageUrl(selectedPosition);
            Picasso.get().load(finalUrlString).resize(400, 400).centerInside().into(binding.expandedImage);
            binding.zoomContainer.setVisibility(View.VISIBLE);
            binding.imagesRecycler.setVisibility(View.INVISIBLE);
        }
        updateButtonAccept();
    }

    void onCloseZoom() {
        binding.zoomContainer.setVisibility(View.INVISIBLE);
        binding.imagesRecycler.setVisibility(View.VISIBLE);
    }

    void onClickOnExpandedImage() {
        onCloseZoom();
    }

    void onBtnAcceptSelection() {
        Intent intent = new Intent();
        intent.putExtra(ImageKeyHelper.IMG_ID, adapter.getSelectedImageName());
        setResult(RESULT_OK, intent);
        finish();
    }

    void onBtnChooseImage() {
        if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_STORAGE);
        } else {
            EasyImage.openGallery(this, -1, false);
        }
    }

    private void updateButtonAccept() {
        boolean visible = isUserLoggedIn() && adapter.isSelectionDone();
        ((View) binding.btnAcceptSelection).setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        binding.txtInfo.setVisibility(binding.btnAcceptSelection.getVisibility());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_CANCELED);
        finish();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        new PhotoReceiverHandler(this).onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_STORAGE && Utils.isAllGranted(grantResults)) {
            onBtnChooseImage();
        }
    }

    @Override
    public void onPhotoReturned(File newPhotoFile) {
        Intent intent = new Intent();
        intent.putExtra(ImageKeyHelper.IMAGE_FILE, newPhotoFile);
        setResult(RESULT_OK, intent);
        finish();
    }
}
