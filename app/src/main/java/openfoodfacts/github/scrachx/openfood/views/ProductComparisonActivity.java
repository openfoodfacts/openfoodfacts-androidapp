package openfoodfacts.github.scrachx.openfood.views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.apache.commons.collections.BagUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.AdditiveName;
import openfoodfacts.github.scrachx.openfood.models.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.CategoryName;
import openfoodfacts.github.scrachx.openfood.models.CountryName;
import openfoodfacts.github.scrachx.openfood.models.LabelName;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.adapters.ProductComparisonAdapter;
import openfoodfacts.github.scrachx.openfood.views.product.summary.ISummaryProductPresenter;

public class ProductComparisonActivity extends AppCompatActivity implements ISummaryProductPresenter.View{

    TextView productTextView;
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
    public void showAllergens(List<AllergenName> allergens) {

    }

    @Override
    public void showCategories(List<CategoryName> categories) {

    }

    @Override
    public void showLabels(List<LabelName> labels) {

    }

    @Override
    public void showCountries(List<CountryName> countries) {

    }

    @Override
    public void showCategoriesState(String state) {

    }

    @Override
    public void showLabelsState(String state) {

    }

    @Override
    public void showCountriesState(String state) {

    }

    @Override
    public void showAdditives(List<AdditiveName> additives) {

    }

    @Override
    public void showAdditivesState(String state) {

    }
}
