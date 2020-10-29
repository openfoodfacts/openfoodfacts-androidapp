package openfoodfacts.github.scrachx.openfood.features.product.view;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.CalculateDetailsBinding;
import openfoodfacts.github.scrachx.openfood.features.adapters.CalculatedNutrimentsGridAdapter;
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity;
import openfoodfacts.github.scrachx.openfood.models.HeaderNutrimentListItem;
import openfoodfacts.github.scrachx.openfood.models.NutrimentListItem;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.Units;
import openfoodfacts.github.scrachx.openfood.utils.Modifier;
import openfoodfacts.github.scrachx.openfood.utils.ProductUtils;
import openfoodfacts.github.scrachx.openfood.utils.UnitUtils;

import static androidx.recyclerview.widget.DividerItemDecoration.VERTICAL;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.CARBOHYDRATES;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.CARBO_MAP;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.ENERGY_KCAL;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.ENERGY_KJ;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.FAT;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.FAT_MAP;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.MINERALS_MAP;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.PROTEINS;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.PROT_MAP;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.VITAMINS_MAP;

public class CalculateDetailsActivity extends BaseActivity {
    private static final String KEY_SAMPLE_OBJECT = "sampleObject";
    private static final String KEY_SPINNER_VALUE = "spinnervalue";
    private static final String KEY_WEIGHT = "weight";
    Nutriments nutriments;
    private List<NutrimentListItem> nutrimentListItems;
    private Product product;
    private String spinnerValue;
    private float weight;

    /**
     * @deprecated use {@link #start(Context, Product, String, float)}
     */
    @Deprecated
    public static void start(@NonNull Context context, @NonNull Product product, @NonNull String spinnerValue, @NonNull String weight) {
        start(context, product, spinnerValue, Float.parseFloat(weight));
    }

    public static void start(@NonNull Context context, @NonNull Product product, @NonNull String spinnerValue, float weight) {
        Intent starter = new Intent(context, CalculateDetailsActivity.class);
        starter.putExtra(KEY_SAMPLE_OBJECT, product);
        starter.putExtra(KEY_SPINNER_VALUE, spinnerValue);
        starter.putExtra(KEY_WEIGHT, weight);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        openfoodfacts.github.scrachx.openfood.databinding.CalculateDetailsBinding binding = CalculateDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setTitle(getString(R.string.app_name_long));

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        final Intent intent = getIntent();

        product = (Product) intent.getSerializableExtra(KEY_SAMPLE_OBJECT);
        spinnerValue = intent.getStringExtra(KEY_SPINNER_VALUE);
        weight = intent.getFloatExtra(KEY_WEIGHT, -1);
        if (product == null || spinnerValue == null || weight == -1) {
            Log.e(CalculateDetailsActivity.class.getSimpleName(), "fragment instantiated with wrong intent extras");
            finish();
        }
        nutriments = product.getNutriments();
        nutrimentListItems = new ArrayList<>();
        binding.resultTextView.setText(getString(R.string.display_fact, weight + " " + spinnerValue));
        binding.nutrimentsRecyclerViewCalc.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        binding.nutrimentsRecyclerViewCalc.setLayoutManager(mLayoutManager);

        binding.nutrimentsRecyclerViewCalc.setNestedScrollingEnabled(false);

        // use VERTICAL divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.nutrimentsRecyclerViewCalc.getContext(), VERTICAL);
        binding.nutrimentsRecyclerViewCalc.addItemDecoration(dividerItemDecoration);

        // Header hack
        nutrimentListItems.add(new NutrimentListItem(ProductUtils.isPerServingInLiter(product)));

        // Energy
        Nutriments.Nutriment energyKcal = nutriments.get(ENERGY_KCAL);
        if (energyKcal != null) {
            nutrimentListItems.add(
                new NutrimentListItem(getString(R.string.nutrition_energy_short_name),
                    calculateCalories(weight, spinnerValue),
                    energyKcal.getForServingInUnits(),
                    Units.ENERGY_KCAL,
                    nutriments.getModifierIfNotDefault(ENERGY_KCAL)));
        }
        Nutriments.Nutriment energyKj = nutriments.get(ENERGY_KJ);
        if (energyKj != null) {
            nutrimentListItems.add(
                new NutrimentListItem(getString(R.string.nutrition_energy_short_name),
                    calculateKj(weight, spinnerValue),
                    energyKj.getForServingInUnits(),
                    Units.ENERGY_KJ.toLowerCase(),
                    nutriments.getModifierIfNotDefault(ENERGY_KJ)));
        }

        // Fat
        Nutriments.Nutriment fat = nutriments.get(FAT);
        if (fat != null) {
            String modifier = nutriments.getModifier(FAT);
            nutrimentListItems.add(new HeaderNutrimentListItem(getString(R.string.nutrition_fat),
                fat.getForAnyValue(weight, spinnerValue),
                fat.getForServingInUnits(),
                fat.getUnit(),
                Modifier.DEFAULT_MODIFIER.equals(modifier) ? "" : modifier));

            nutrimentListItems.addAll(getNutrimentItems(nutriments, FAT_MAP));
        }

        // Carbohydrates
        Nutriments.Nutriment carbohydrates = nutriments.get(CARBOHYDRATES);
        if (carbohydrates != null) {
            String modifier = nutriments.getModifier(CARBOHYDRATES);
            nutrimentListItems.add(new HeaderNutrimentListItem(getString(R.string.nutrition_carbohydrate),
                carbohydrates.getForAnyValue(weight, spinnerValue),
                carbohydrates.getForServingInUnits(),
                carbohydrates.getUnit(),
                Modifier.DEFAULT_MODIFIER.equals(modifier) ? "" : modifier));

            nutrimentListItems.addAll(getNutrimentItems(nutriments, CARBO_MAP));
        }

        // fiber
        nutrimentListItems.addAll(getNutrimentItems(nutriments, Collections.singletonMap(Nutriments.FIBER, R.string.nutrition_fiber)));

        // Proteins
        Nutriments.Nutriment proteins = nutriments.get(PROTEINS);
        if (proteins != null) {
            String modifier = nutriments.getModifier(PROTEINS);
            nutrimentListItems.add(
                new HeaderNutrimentListItem(getString(R.string.nutrition_proteins),
                    proteins.getForAnyValue(weight, spinnerValue),
                    proteins.getForServingInUnits(),
                    proteins.getUnit(),
                    Modifier.DEFAULT_MODIFIER.equals(modifier) ? "" : modifier));

            nutrimentListItems.addAll(getNutrimentItems(nutriments, PROT_MAP));
        }

        // salt and alcohol
        Map<String, Integer> map = new HashMap<>();
        map.put(Nutriments.SALT, R.string.nutrition_salt);
        map.put(Nutriments.SODIUM, R.string.nutrition_sodium);
        map.put(Nutriments.ALCOHOL, R.string.nutrition_alcohol);
        nutrimentListItems.addAll(getNutrimentItems(nutriments, map));

        // Vitamins
        if (nutriments.hasVitamins()) {
            nutrimentListItems.add(new HeaderNutrimentListItem(getString(R.string.nutrition_vitamins)));

            nutrimentListItems.addAll(getNutrimentItems(nutriments, VITAMINS_MAP));
        }

        // Minerals
        if (nutriments.hasMinerals()) {
            nutrimentListItems.add(new HeaderNutrimentListItem(getString(R.string.nutrition_minerals)));

            nutrimentListItems.addAll(getNutrimentItems(nutriments, MINERALS_MAP));
        }
        binding.nutrimentsRecyclerViewCalc.setAdapter(new CalculatedNutrimentsGridAdapter(nutrimentListItems));
    }

    private List<NutrimentListItem> getNutrimentItems(Nutriments nutriments, Map<String, Integer> nutrimentMap) {
        List<NutrimentListItem> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : nutrimentMap.entrySet()) {
            Nutriments.Nutriment nutriment = nutriments.get(entry.getKey());
            if (nutriment != null) {
                final String modifier = nutriments.getModifier(entry.getKey());
                items.add(new NutrimentListItem(getString(entry.getValue()),
                    nutriment.getForAnyValue(weight, spinnerValue),
                    nutriment.getForServingInUnits(),
                    nutriment.getUnit(),
                    Modifier.DEFAULT_MODIFIER.equals(modifier) ? "" : modifier));
            }
        }

        return items;
    }

    private String calculateCalories(float weight, String unit) {
        float caloriePer100g = Float.parseFloat(product.getNutriments().get(ENERGY_KCAL).getFor100gInUnits());
        float weightInG = UnitUtils.convertToGrams(weight, unit);
        return Float.toString(((caloriePer100g / 100) * weightInG));
    }

    private String calculateKj(float weight, String unit) {
        float caloriePer100g = Float.parseFloat(product.getNutriments().get(ENERGY_KJ).getFor100gInUnits());
        float weightInG = UnitUtils.convertToGrams(weight, unit);
        return Float.toString(((caloriePer100g / 100) * weightInG));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Respond to the action bar's Up/Home button
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

