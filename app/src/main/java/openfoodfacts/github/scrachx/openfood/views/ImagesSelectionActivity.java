package openfoodfacts.github.scrachx.openfood.views;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
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
import openfoodfacts.github.scrachx.openfood.images.ImageNameJsonParser;
import openfoodfacts.github.scrachx.openfood.images.PhotoReceiver;
import openfoodfacts.github.scrachx.openfood.jobs.PhotoReceiverHandler;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductImagesSelectionAdapter;
import org.json.JSONException;
import org.json.JSONObject;
import pl.aprilapps.easyphotopicker.EasyImage;

import java.io.File;
import java.util.List;

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
                List<String> imageNames=ImageNameJsonParser.extractImagesNameSortedByUploadTimeDesc(images);

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
            Picasso.get().load(finalUrlString).resize(400, 400).centerInside().into(expandedImage);
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
