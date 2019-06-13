package openfoodfacts.github.scrachx.openfood.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.squareup.picasso.Picasso;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.BaseFragment;
import openfoodfacts.github.scrachx.openfood.fragments.ProductPhotosFragment;
import openfoodfacts.github.scrachx.openfood.images.ImageKeyHelper;
import openfoodfacts.github.scrachx.openfood.images.PhotoReceiver;
import openfoodfacts.github.scrachx.openfood.jobs.PhotoReceiverHandler;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductImagesSelectionAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import pl.aprilapps.easyphotopicker.EasyImage;

import java.io.File;
import java.util.ArrayList;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static openfoodfacts.github.scrachx.openfood.utils.Utils.MY_PERMISSIONS_REQUEST_STORAGE;

public class ImagesSelectionActivity extends BaseActivity implements PhotoReceiver {
    static final String TOOLBAR_TITLE = "TOOLBAR_TITLE";
    private ProductImagesSelectionAdapter adapter;
    @BindView(R.id.imagesRecycler)
    RecyclerView imagesRecycler;
    @BindView(R.id.expandedImage)
    ImageView expandedImage;
    @BindView(R.id.btnChooseImage)
    Button btnChooseImage;
    @BindView(R.id.btnAcceptSelection)
    View btnAcceptSelection;
    @BindView(R.id.zoomContainer)
    View expandedContainer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.txtInfo)
    TextView txtInfo;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenFoodAPIClient openFoodAPIClient = new OpenFoodAPIClient(this);
        setContentView(R.layout.activity_product_images_list);
        btnChooseImage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_photo_library, 0, 0, 0);
        ArrayList<String> imageNames = new ArrayList<>();

        Intent intent = getIntent();

        String code = intent.getStringExtra(ImageKeyHelper.PRODUCT_BARCODE);
        toolbar.setTitle(intent.getStringExtra(TOOLBAR_TITLE));

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
                if (images != null) {
                    final JSONArray names = images.names();
                    if (names != null) {
                        // loop through all the image names and store them in a array list
                        for (int i = 0; i < names.length(); i++) {
                            try {
                                // do not include images with contain nutrients,ingredients or other in their names
                                // as they are duplicate and do not load as well
                                final String namesString = names.getString(i);
                                if (namesString.contains("n") ||
                                    namesString.contains("f") ||
                                    namesString.contains("i") ||
                                    namesString.contains("o")) {

                                    continue;
                                }
                                imageNames.add(namesString);
                            } catch (JSONException e) {
                                Log.w(ProductPhotosFragment.class.getSimpleName(), "can't get product / images in json", e);
                            }
                        }
                    }
                }

                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }

                //Check if user is logged in
                adapter = new ProductImagesSelectionAdapter(this, imageNames, code, position -> imageSelected());

                imagesRecycler.setAdapter(adapter);
                imagesRecycler.setLayoutManager(new GridLayoutManager(this, 3));
            }
        });
    }

    private void imageSelected() {
        final int selectedPosition = adapter.getSelectedPosition();
        if(selectedPosition>=0) {
            String finalUrlString = adapter.getImageUrl(selectedPosition);
            Picasso.with(this).load(finalUrlString).resize(400, 400).centerInside().into(expandedImage);
            expandedContainer.setVisibility(View.VISIBLE);
            imagesRecycler.setVisibility(View.INVISIBLE);
        }
        updateButtonAccept();
    }

    @OnClick(R.id.closeZoom)
    void onCloseZoom() {
        expandedContainer.setVisibility(View.INVISIBLE);
        imagesRecycler.setVisibility(View.VISIBLE);
    }
    @OnClick(R.id.expandedImage)
    void onClickOnExpandedImage() {
        onCloseZoom();
    }


    @OnClick(R.id.btnAcceptSelection)
    void onBtnAcceptSelection() {
        Intent intent = new Intent();
        intent.putExtra(ImageKeyHelper.IMG_ID, adapter.getSelectedImageName());
        setResult(RESULT_OK, intent);
        finish();
    }

    @OnClick(R.id.btnChooseImage)
    void onBtnChooseImage() {
        if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_STORAGE);
        } else {
            EasyImage.openGallery(this, -1, false);
        }
    }

    private void updateButtonAccept() {
        boolean visible = isUserLoggedIn() && adapter.isSelectionDone();
        btnAcceptSelection.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        txtInfo.setVisibility(btnAcceptSelection.getVisibility());
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_CANCELED);
        finish();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        new PhotoReceiverHandler(this).onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_STORAGE && BaseFragment.isAllGranted(grantResults)) {
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
