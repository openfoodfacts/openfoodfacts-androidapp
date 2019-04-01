package openfoodfacts.github.scrachx.openfood.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductComparisonAdapter;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;


public class ProductComparisonActivity extends AppCompatActivity {

    Button productComparisonButton;

    private RecyclerView productComparisonRv;
    private RecyclerView.Adapter productComparisonAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Product> products = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_comparison);

        if (getIntent().getExtras() != null && getIntent().getBooleanExtra("product_found", false)){
            products = (ArrayList<Product>) getIntent().getExtras().get("products_to_compare");
            if (getIntent().getBooleanExtra("product_already_exists", false)) {
                Toast.makeText(this, "The product already exists in the comparison list", Toast.LENGTH_SHORT).show();
            }
        }

        productComparisonButton = findViewById(R.id.product_comparison_button);
        productComparisonRv = (RecyclerView) findViewById(R.id.product_comparison_rv);

        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        productComparisonRv.setLayoutManager(layoutManager);

        productComparisonAdapter = new ProductComparisonAdapter(products, this);
        productComparisonRv.setAdapter(productComparisonAdapter);

        productComparisonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utils.isHardwareCameraInstalled(ProductComparisonActivity.this)) {
                    if (ContextCompat.checkSelfPermission(ProductComparisonActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(ProductComparisonActivity.this, Manifest.permission.CAMERA)) {
                            new MaterialDialog.Builder(ProductComparisonActivity.this)
                                    .title(R.string.action_about)
                                    .content(R.string.permission_camera)
                                    .neutralText(R.string.txtOk)
                                    .onNeutral((dialog, which) -> ActivityCompat.requestPermissions(ProductComparisonActivity.this, new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA))
                                    .show();
                        } else {
                            ActivityCompat.requestPermissions(ProductComparisonActivity.this, new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA);
                        }
                    } else {
                        Intent intent = new Intent(ProductComparisonActivity.this, ContinuousScanActivity.class);
                        intent.putExtra("compare_product", true);
                        intent.putExtra("products_to_compare", products);
                        startActivity(intent);
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();

                ((ProductComparisonAdapter) productComparisonAdapter).setImageOnPhotoReturn(new File(resultUri.getPath()));

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
            }

            @Override
            public void onImagesPicked(List<File> imageFiles, EasyImage.ImageSource source, int type) {
                CropImage.activity(Uri.fromFile(imageFiles.get(0)))
                        .setCropMenuCropButtonIcon(R.drawable.ic_check_white_24dp)
                        .setAllowFlipping(false)
                        .start(ProductComparisonActivity.this);
            }

            @Override
            public void onCanceled(EasyImage.ImageSource source, int type) {
                //Cancel handling, you might wanna remove taken photo if it was canceled
                if (source == EasyImage.ImageSource.CAMERA) {
                    File photoFile = EasyImage.lastlyTakenButCanceledPhoto(ProductComparisonActivity.this);
                    if (photoFile != null) photoFile.delete();
                }
            }
        });
    }
}
