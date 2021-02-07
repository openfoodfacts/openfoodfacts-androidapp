package openfoodfacts.github.scrachx.openfood.features.product.view

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.CalculateDetailsBinding
import openfoodfacts.github.scrachx.openfood.features.adapters.CalculatedNutrimentsGridAdapter
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.models.*
import openfoodfacts.github.scrachx.openfood.utils.UnitUtils.convertToGrams
import openfoodfacts.github.scrachx.openfood.utils.isPerServingInLiter
import java.util.*
import kotlin.properties.Delegates

class CalculateDetailsActivity : BaseActivity() {
    private val nutrimentListItems = mutableListOf<NutrimentListItem>()
    private val nutriMap = hashMapOf(
            Nutriments.SALT to R.string.nutrition_salt,
            Nutriments.SODIUM to R.string.nutrition_sodium,
            Nutriments.ALCOHOL to R.string.nutrition_alcohol
    )
    private lateinit var nutriments: Nutriments
    private lateinit var product: Product
    private lateinit var spinnerValue: String

    private var weight by Delegates.notNull<Float>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (resources.getBoolean(R.bool.portrait_only)) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        val binding = CalculateDetailsBinding.inflate(layoutInflater)

        setContentView(binding.root)
        title = getString(R.string.app_name_long)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val intent = intent
        val product = intent.getSerializableExtra(KEY_PRODUCT) as Product?
        val spinnerValue = intent.getStringExtra(KEY_SPINNER_VALUE)
        val weight = intent.getFloatExtra(KEY_WEIGHT, -1f)

        if (product == null || spinnerValue == null || weight == -1f) {
            Log.e(CalculateDetailsActivity::class.simpleName, "Activity instantiated with wrong intent extras.")
            finish()
            return
        }

        this.product = product
        this.spinnerValue = spinnerValue
        this.weight = weight
        this.nutriments = product.nutriments

        binding.resultTextView.text = getString(R.string.display_fact, "$weight $spinnerValue")
        binding.nutrimentsRecyclerViewCalc.setHasFixedSize(true)

        // use a linear layout manager
        val mLayoutManager = LinearLayoutManager(this)
        binding.nutrimentsRecyclerViewCalc.layoutManager = mLayoutManager
        binding.nutrimentsRecyclerViewCalc.isNestedScrollingEnabled = false

        // use VERTICAL divider
        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.nutrimentsRecyclerViewCalc.addItemDecoration(dividerItemDecoration)

        // Header hack
        nutrimentListItems.add(NutrimentListItem(product.isPerServingInLiter() ?: false))

        // Energy
        val energyKcal = nutriments[Nutriments.ENERGY_KCAL]
        if (energyKcal != null) {
            nutrimentListItems += NutrimentListItem(
                    getString(R.string.nutrition_energy_short_name),
                    calculateCalories(weight, spinnerValue).toString(),
                    energyKcal.forServingInUnits,
                    Units.ENERGY_KCAL,
                    energyKcal.getModifierIfNotDefault(),
            )
        }
        val energyKj = nutriments[Nutriments.ENERGY_KJ]
        if (energyKj != null) {
            nutrimentListItems += NutrimentListItem(
                    getString(R.string.nutrition_energy_short_name),
                    calculateKj(weight, spinnerValue).toString(),
                    energyKj.forServingInUnits,
                    Units.ENERGY_KJ.toLowerCase(Locale.getDefault()),
                    energyKj.getModifierIfNotDefault(),
            )
        }

        // Fat
        val fat = nutriments[Nutriments.FAT]
        if (fat != null) {
            nutrimentListItems += BoldNutrimentListItem(
                    getString(R.string.nutrition_fat),
                    fat.getForPortion(weight, spinnerValue),
                    fat.forServingInUnits,
                    fat.unit,
                    fat.getModifierIfNotDefault()
            )
            nutrimentListItems.addAll(getNutrimentItems(nutriments, Nutriments.FAT_MAP))
        }

        // Carbohydrates
        val carbohydrates = nutriments[Nutriments.CARBOHYDRATES]
        if (carbohydrates != null) {
            nutrimentListItems += BoldNutrimentListItem(
                    getString(R.string.nutrition_carbohydrate),
                    carbohydrates.getForPortion(weight, spinnerValue),
                    carbohydrates.forServingInUnits,
                    carbohydrates.unit,
                    carbohydrates.getModifierIfNotDefault()
            )
            nutrimentListItems.addAll(getNutrimentItems(nutriments, Nutriments.CARBO_MAP))
        }

        // fiber
        nutrimentListItems.addAll(getNutrimentItems(nutriments, Collections.singletonMap(Nutriments.FIBER, R.string.nutrition_fiber)))

        // Proteins
        val proteins = nutriments[Nutriments.PROTEINS]
        if (proteins != null) {
            nutrimentListItems += BoldNutrimentListItem(
                    getString(R.string.nutrition_proteins),
                    proteins.getForPortion(weight, spinnerValue),
                    proteins.forServingInUnits,
                    proteins.unit,
                    proteins.getModifierIfNotDefault()
            )
            nutrimentListItems.addAll(getNutrimentItems(nutriments, Nutriments.PROT_MAP))
        }

        // salt and alcohol
        nutrimentListItems.addAll(getNutrimentItems(nutriments, nutriMap))

        // Vitamins
        if (nutriments.hasVitamins) {
            nutrimentListItems += BoldNutrimentListItem(getString(R.string.nutrition_vitamins))
            nutrimentListItems += getNutrimentItems(nutriments, Nutriments.VITAMINS_MAP)
        }

        // Minerals
        if (nutriments.hasMinerals) {
            nutrimentListItems += BoldNutrimentListItem(getString(R.string.nutrition_minerals))
            nutrimentListItems += getNutrimentItems(nutriments, Nutriments.MINERALS_MAP)
        }
        binding.nutrimentsRecyclerViewCalc.adapter = CalculatedNutrimentsGridAdapter(nutrimentListItems)
    }

    private fun getNutrimentItems(nutriments: Nutriments, nutrimentMap: Map<String, Int>): List<NutrimentListItem> {
        return nutrimentMap.mapNotNull { (key, value) ->
            val nutriment = nutriments[key]
            if (nutriment != null) {
                NutrimentListItem(
                        getString(value),
                        nutriment.getForPortion(weight, spinnerValue),
                        nutriment.forServingInUnits,
                        nutriment.unit,
                        nutriment.getModifierIfNotDefault()
                )
            } else null
        }
    }

    private fun calculateCalories(weight: Float, unit: String?): Float {
        val caloriePer100g = product.nutriments[Nutriments.ENERGY_KCAL]!!.for100gInUnits.toFloat()
        val weightInG = convertToGrams(weight, unit)
        return caloriePer100g / 100 * weightInG
    }

    private fun calculateKj(weight: Float, unit: String?): Float {
        val caloriePer100g = product.nutriments[Nutriments.ENERGY_KJ]!!.for100gInUnits.toFloat()
        val weightInG = convertToGrams(weight, unit)
        return caloriePer100g / 100 * weightInG
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Respond to the action bar's Up/Home button
        android.R.id.home -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    companion object {
        private const val KEY_PRODUCT = "product"
        private const val KEY_SPINNER_VALUE = "spinnerValue"
        private const val KEY_WEIGHT = "weight"

        @Deprecated("Use {@link #start(Context, Product, String, float)}", ReplaceWith("start(context, product, spinnerValue, weight.toFloat())", "openfoodfacts.github.scrachx.openfood.features.product.view.CalculateDetailsActivity.Companion.start"))
        fun start(context: Context, product: Product, spinnerValue: String, weight: String) {
            start(context, product, spinnerValue, weight.toFloat())
        }

        fun start(context: Context, product: Product, spinnerValue: String, weight: Float) {
            context.startActivity(Intent(context, CalculateDetailsActivity::class.java).apply {
                putExtra(KEY_PRODUCT, product)
                putExtra(KEY_SPINNER_VALUE, spinnerValue)
                putExtra(KEY_WEIGHT, weight)
            })
        }
    }
}