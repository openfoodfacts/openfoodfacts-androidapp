package openfoodfacts.github.scrachx.openfood.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.ActivityProductComparisonBinding;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.utils.PhotoReceiverHandler;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductComparisonAdapter;
import openfoodfacts.github.scrachx.openfood.views.listeners.BottomNavigationListenerInstaller;

public class ProductComparisonActivity extends BaseActivity {
    private ActivityProductComparisonBinding binding;
    private PhotoReceiverHandler photoReceiverHandler;
    private RecyclerView.Adapter<ProductComparisonAdapter.ProductComparisonViewHolder> productComparisonAdapter;
    private ArrayList<Product> products = new ArrayList<>();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductComparisonBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setTitle(getString(R.string.compare_products));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        photoReceiverHandler = new PhotoReceiverHandler(newPhotoFile -> ((ProductComparisonAdapter) productComparisonAdapter).setImageOnPhotoReturn(newPhotoFile));

        if (getIntent().getExtras() != null && getIntent().getBooleanExtra("product_found", false)) {
            products = (ArrayList<Product>) getIntent().getExtras().getSerializable("products_to_compare");
            if (getIntent().getBooleanExtra("product_already_exists", false)) {
                Toast.makeText(this, getString(R.string.product_already_exists_in_comparison), Toast.LENGTH_SHORT).show();
            }
        }

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.productComparisonRv.setLayoutManager(layoutManager);

        productComparisonAdapter = new ProductComparisonAdapter(products, this);
        binding.productComparisonRv.setAdapter(productComparisonAdapter);

        binding.productComparisonButton.setOnClickListener(v -> {
            if (Utils.isHardwareCameraInstalled(ProductComparisonActivity.this)) {
                if (ContextCompat.checkSelfPermission(ProductComparisonActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(ProductComparisonActivity.this, Manifest.permission.CAMERA)) {
                        new MaterialDialog.Builder(ProductComparisonActivity.this)
                            .title(R.string.action_about)
                            .content(R.string.permission_camera)
                            .neutralText(R.string.txtOk)
                            .onNeutral((dialog, which) -> ActivityCompat
                                .requestPermissions(ProductComparisonActivity.this, new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_CAMERA))
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
        });

        BottomNavigationListenerInstaller.install(binding.navigationBottomInclude.bottomNavigation, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        photoReceiverHandler.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        BottomNavigationListenerInstaller.selectNavigationItem(binding.navigationBottomInclude.bottomNavigation, R.id.compare_products);
    }
}
