package openfoodfacts.github.scrachx.openfood.views.product;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.CalculateDetailsBinding;
import openfoodfacts.github.scrachx.openfood.models.HeaderNutrimentListItem;
import openfoodfacts.github.scrachx.openfood.models.NutrimentListItem;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.Units;
import openfoodfacts.github.scrachx.openfood.utils.Modifier;
import openfoodfacts.github.scrachx.openfood.utils.ProductUtils;
import openfoodfacts.github.scrachx.openfood.utils.UnitUtils;
import openfoodfacts.github.scrachx.openfood.views.BaseActivity;
import openfoodfacts.github.scrachx.openfood.views.adapters.CalculatedNutrimentsGridAdapter;

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

public class CalculateDetails extends BaseActivity {
    Nutriments nutriments;
    List<NutrimentListItem> nutrimentListItems;
    private CalculateDetailsBinding binding;
    String spinnervalue;
    String weight;
    String pname;
    float value;
    Product p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        binding = CalculateDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setTitle(getString(R.string.app_name_long));

        setSupportActionBar(binding.toolbar1);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Intent intent = getIntent();

        p = (Product) intent.getSerializableExtra("sampleObject");
        spinnervalue = intent.getStringExtra("spinnervalue");
        weight = intent.getStringExtra("weight");
        value = Float.parseFloat(weight);
        nutriments = p.getNutriments();
        nutrimentListItems = new ArrayList<>();
        binding.resultTextView.setText(getString(R.string.display_fact, weight + " " + spinnervalue));
        binding.nutrimentsRecyclerViewCalc.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        binding.nutrimentsRecyclerViewCalc.setLayoutManager(mLayoutManager);

        binding.nutrimentsRecyclerViewCalc.setNestedScrollingEnabled(false);

        // use VERTICAL divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.nutrimentsRecyclerViewCalc.getContext(), VERTICAL);
        binding.nutrimentsRecyclerViewCalc.addItemDecoration(dividerItemDecoration);

        // Header hack
        nutrimentListItems.add(new NutrimentListItem(ProductUtils.isPerServingInLiter(p)));

        // Energy
        Nutriments.Nutriment energyKcal = nutriments.get(ENERGY_KCAL);
        if (energyKcal != null) {
            nutrimentListItems.add(
                new NutrimentListItem(getString(R.string.nutrition_energy_short_name),
                    calculateCalories(value, spinnervalue),
                    energyKcal.getForServingInUnits(),
                    Units.ENERGY_KCAL,
                    nutriments.getModifierIfNotDefault(ENERGY_KCAL)));
        }
        Nutriments.Nutriment energyKj = nutriments.get(ENERGY_KJ);
        if (energyKj != null) {
            nutrimentListItems.add(
                new NutrimentListItem(getString(R.string.nutrition_energy_short_name),
                    calculateKj(value, spinnervalue),
                    energyKj.getForServingInUnits(),
                    Units.ENERGY_KJ.toLowerCase(),
                    nutriments.getModifierIfNotDefault(ENERGY_KJ)));
        }

        // Fat
        Nutriments.Nutriment fat = nutriments.get(FAT);
        if (fat != null) {
            String modifier = nutriments.getModifier(FAT);
            nutrimentListItems.add(new HeaderNutrimentListItem(getString(R.string.nutrition_fat),
                fat.getForAnyValue(value, spinnervalue),
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
                carbohydrates.getForAnyValue(value, spinnervalue),
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
                    proteins.getForAnyValue(value, spinnervalue),
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
                    nutriment.getForAnyValue(value, spinnervalue),
                    nutriment.getForServingInUnits(),
                    nutriment.getUnit(),
                    Modifier.DEFAULT_MODIFIER.equals(modifier) ? "" : modifier));
            }
        }

        return items;
    }

    private String calculateCalories(float weight, String unit) {
        float caloriePer100g = Float.parseFloat(p.getNutriments().get(ENERGY_KCAL).getFor100gInUnits());
        float weightInG = UnitUtils.convertToGrams(weight, unit);
        return Float.toString(((caloriePer100g / 100) * weightInG));
    }

    private String calculateKj(float weight, String unit) {
        float caloriePer100g = Float.parseFloat(p.getNutriments().get(ENERGY_KJ).getFor100gInUnits());
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

