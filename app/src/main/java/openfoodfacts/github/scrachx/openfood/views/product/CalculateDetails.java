package openfoodfacts.github.scrachx.openfood.views.product;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.HeaderNutrimentItem;
import openfoodfacts.github.scrachx.openfood.models.NutrimentItem;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.BaseActivity;
import openfoodfacts.github.scrachx.openfood.views.adapters.CalculateAdapter;
import openfoodfacts.github.scrachx.openfood.views.adapters.NutrimentsRecyclerViewAdapter;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.CARBOHYDRATES;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.CARBO_MAP;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.ENERGY;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.FAT;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.FAT_MAP;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.MINERALS_MAP;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.PROTEINS;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.PROT_MAP;
import static openfoodfacts.github.scrachx.openfood.models.Nutriments.VITAMINS_MAP;

public class CalculateDetails extends BaseActivity {

    RecyclerView nutrimentsRecyclerView;
    Nutriments nutriments;
    List<NutrimentItem> nutrimentItems;
    String spinnervalue,weight,pname;
    float value;
    Product p;
    TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getBoolean(R.bool.portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.calculate_details);
        result = findViewById(R.id.result_text_view);
        Intent i = getIntent();
        p = (Product) i.getSerializableExtra("sampleObject");
        pname = p.getProductName();
        spinnervalue = i.getStringExtra("spinnervalue");
        weight = i.getStringExtra("weight");
        value = Float.valueOf(weight);
        nutriments = p.getNutriments();
        nutrimentItems = new ArrayList<>();
        nutrimentsRecyclerView = findViewById(R.id.nutriments_recycler_view_calc);
        result.setText(String.format("Table for nutrition facts of %1$s of %2$s",weight+" "+spinnervalue,pname));
        nutrimentsRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        nutrimentsRecyclerView.setLayoutManager(mLayoutManager);

        nutrimentsRecyclerView.setNestedScrollingEnabled(false);

        // use VERTICAL divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(nutrimentsRecyclerView.getContext(), VERTICAL);
        nutrimentsRecyclerView.addItemDecoration(dividerItemDecoration);

        // Header hack
        nutrimentItems.add(new NutrimentItem(null, null, null, null));

        // Energy
        Nutriments.Nutriment energy = nutriments.get(ENERGY);
        if (energy != null) {
            nutrimentItems.add(new NutrimentItem(getString(R.string.nutrition_energy_short_name), calculateCalories(value,spinnervalue), Utils.getEnergy(energy.getForServing()), "kcal"));
        }

        // Fat
        Nutriments.Nutriment fat = nutriments.get(FAT);
        if (fat != null) {
            nutrimentItems.add(new HeaderNutrimentItem(getString(R.string.nutrition_fat), fat.getforanyvalue(value,spinnervalue), fat.getForServing(), fat.getUnit()));

            nutrimentItems.addAll(getNutrimentItems(nutriments, FAT_MAP));
        }

        // Carbohydrates
        Nutriments.Nutriment carbohydrates = nutriments.get(CARBOHYDRATES);
        if (carbohydrates != null) {
            nutrimentItems.add(new HeaderNutrimentItem(getString(R.string.nutrition_carbohydrate),
                    carbohydrates.getforanyvalue(value,spinnervalue),
                    carbohydrates.getForServing(),
                    carbohydrates.getUnit()));

            nutrimentItems.addAll(getNutrimentItems(nutriments, CARBO_MAP));
        }

        // fiber
        nutrimentItems.addAll(getNutrimentItems(nutriments, Collections.singletonMap(Nutriments.FIBER, R.string.nutrition_fiber)));

        // Proteins
        Nutriments.Nutriment proteins = nutriments.get(PROTEINS);
        if (proteins != null) {
            nutrimentItems.add(new HeaderNutrimentItem(getString(R.string.nutrition_proteins),
                    proteins.getforanyvalue(value,spinnervalue),
                    proteins.getForServing(),
                    proteins.getUnit()));

            nutrimentItems.addAll(getNutrimentItems(nutriments, PROT_MAP));
        }

        // salt and alcohol
        Map<String, Integer> map = new HashMap<>();
        map.put(Nutriments.SALT, R.string.nutrition_salt);
        map.put(Nutriments.SODIUM, R.string.nutrition_sodium);
        map.put(Nutriments.ALCOHOL, R.string.nutrition_alcohol);
        nutrimentItems.addAll(getNutrimentItems(nutriments, map));

        // Vitamins
        if (nutriments.hasVitamins()) {
            nutrimentItems.add(new HeaderNutrimentItem(getString(R.string.nutrition_vitamins)));

            nutrimentItems.addAll(getNutrimentItems(nutriments, VITAMINS_MAP));
        }

        // Minerals
        if (nutriments.hasMinerals()) {
            nutrimentItems.add(new HeaderNutrimentItem(getString(R.string.nutrition_minerals)));

            nutrimentItems.addAll(getNutrimentItems(nutriments, MINERALS_MAP));
        }
        RecyclerView.Adapter adapter = new CalculateAdapter(nutrimentItems);
        nutrimentsRecyclerView.setAdapter(adapter);
    }

    private List<NutrimentItem> getNutrimentItems(Nutriments nutriments, Map<String, Integer> nutrimentMap) {
        List<NutrimentItem> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : nutrimentMap.entrySet()) {
            Nutriments.Nutriment nutriment = nutriments.get(entry.getKey());
            if (nutriment != null) {
                items.add(new NutrimentItem(getString(entry.getValue()),
                        nutriment.getforanyvalue(value,spinnervalue), nutriment.getForServing(), nutriment.getUnit()));
            }
        }

        return items;
    }

    private String calculateCalories(float weight, String unit) {
        float caloriePer100g, weightInG;
        caloriePer100g = Float.valueOf(Utils.getEnergy(p.getNutriments().get(Nutriments.ENERGY).getFor100g()));
        switch (unit) {
            case "mg":
                weightInG = weight / 1000;
                break;
            case "kg":
                weightInG = weight * 1000;
                break;
            default:
                weightInG = weight;
                break;
        }
        String snew = Float.toString(((caloriePer100g / 100) * weightInG));
        return snew;
    }
}

