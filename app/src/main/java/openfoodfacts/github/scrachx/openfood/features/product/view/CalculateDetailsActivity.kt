package openfoodfacts.github.scrachx.openfood.features.product.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.CalculateDetailsBinding
import openfoodfacts.github.scrachx.openfood.features.adapters.CalculatedNutrimentsGridAdapter
import openfoodfacts.github.scrachx.openfood.features.shared.BaseActivity
import openfoodfacts.github.scrachx.openfood.models.*
import openfoodfacts.github.scrachx.openfood.models.MeasurementUnit
import openfoodfacts.github.scrachx.openfood.utils.Measurement
import openfoodfacts.github.scrachx.openfood.utils.grams
import openfoodfacts.github.scrachx.openfood.utils.isPerServingInLiter
import openfoodfacts.github.scrachx.openfood.utils.measure
import java.util.*
import kotlin.properties.Delegates

class CalculateDetailsActivity : BaseActivity() {
    private val nutrimentListItems = mutableListOf<NutrimentListItem>()

    private lateinit var nutriments: ProductNutriments
    private lateinit var product: Product

    private var weight by Delegates.notNull<Float>()
    private lateinit var unitOfMeasurement: MeasurementUnit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = CalculateDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        title = getString(R.string.app_name_long)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val product = intent.getSerializableExtra(KEY_PRODUCT) as Product?

        val weight = intent.getFloatExtra(KEY_WEIGHT, -1f)
        val unit = intent.getStringExtra(KEY_SPINNER_VALUE)?.let { MeasurementUnit.findBySymbol(it) }

        requireNotNull(product) { "${this::class.simpleName} created without product intent extra." }
        requireNotNull(unit) { "${this::class.simpleName} created without spinner value intent extra." }
        require(weight != -1f) { "${this::class.simpleName} created with weight = -1" }

        this.product = product
        this.unitOfMeasurement = unit
        this.weight = weight
        this.nutriments = product.nutriments

        binding.resultTextView.text = getString(R.string.display_fact, "$weight ${unit.sym}")

        binding.nutriments.setHasFixedSize(true)
        binding.nutriments.layoutManager = LinearLayoutManager(this)
        binding.nutriments.isNestedScrollingEnabled = false

        // use VERTICAL divider
        binding.nutriments.addItemDecoration(DividerItemDecoration(this, VERTICAL))

        // Header hack
        nutrimentListItems += NutrimentListItem(product.isPerServingInLiter() ?: false)

        val portion = measure(weight, unit)

        // Energy
        val energyKcal = nutriments[Nutriment.ENERGY_KCAL]
        if (energyKcal != null) {
            nutrimentListItems += NutrimentListItem(
                getString(R.string.nutrition_energy_short_name),
                calculateCalories(portion).value,
                energyKcal.perServingInUnit?.value,
                MeasurementUnit.ENERGY_KCAL,
                energyKcal.modifier,
            )
        }
        val energyKj = nutriments[Nutriment.ENERGY_KJ]
        if (energyKj != null) {
            nutrimentListItems += NutrimentListItem(
                getString(R.string.nutrition_energy_short_name),
                calculateKj(portion).value,
                energyKj.perServingInUnit?.value,
                MeasurementUnit.ENERGY_KJ,
                energyKj.modifier,
            )
        }

        // Fat
        val fat = nutriments[Nutriment.FAT]
        if (fat != null) {
            nutrimentListItems += BoldNutrimentListItem(
                getString(R.string.nutrition_fat),
                fat.getForPortion(portion).value,
                fat.perServingInUnit?.value,
                fat.unit,
                fat.modifier
            )
            nutrimentListItems += getNutrimentItems(nutriments, FAT_MAP)
        }

        // Carbohydrates
        val carbohydrates = nutriments[Nutriment.CARBOHYDRATES]
        if (carbohydrates != null) {
            nutrimentListItems += BoldNutrimentListItem(
                getString(R.string.nutrition_carbohydrate),
                carbohydrates.getForPortion(portion).value,
                carbohydrates.perServingInUnit?.value,
                carbohydrates.unit,
                carbohydrates.modifier
            )
            nutrimentListItems += getNutrimentItems(nutriments, CARBO_MAP)
        }

        // Fiber
        nutrimentListItems += getNutrimentItems(nutriments, mapOf(Nutriment.FIBER to R.string.nutrition_fiber))

        // Proteins
        val proteins = nutriments[Nutriment.PROTEINS]
        if (proteins != null) {
            nutrimentListItems += BoldNutrimentListItem(
                getString(R.string.nutrition_proteins),
                proteins.getForPortion(portion).value,
                proteins.perServingInUnit?.value,
                proteins.unit,
                proteins.modifier
            )
            nutrimentListItems += getNutrimentItems(nutriments, PROT_MAP)
        }

        // salt and alcohol
        nutrimentListItems += getNutrimentItems(
            nutriments, mapOf(
                Nutriment.SALT to R.string.nutrition_salt,
                Nutriment.SODIUM to R.string.nutrition_sodium,
                Nutriment.ALCOHOL to R.string.nutrition_alcohol
            )
        )

        // Vitamins
        if (nutriments.hasVitamins) {
            nutrimentListItems += BoldNutrimentListItem(getString(R.string.nutrition_vitamins))
            nutrimentListItems += getNutrimentItems(nutriments, VITAMINS_MAP)
        }

        // Minerals
        if (nutriments.hasMinerals) {
            nutrimentListItems += BoldNutrimentListItem(getString(R.string.nutrition_minerals))
            nutrimentListItems += getNutrimentItems(nutriments, MINERALS_MAP)
        }
        binding.nutriments.adapter = CalculatedNutrimentsGridAdapter(nutrimentListItems)
    }

    private fun getNutrimentItems(nutriments: ProductNutriments, nutrimentMap: Map<Nutriment, Int>): List<NutrimentListItem> {
        return nutrimentMap.mapNotNull { (name, stringRes) ->
            val nutriment = nutriments[name] ?: return@mapNotNull null

            NutrimentListItem(
                getString(stringRes),
                nutriment.getForPortion(measure(weight, unitOfMeasurement)).value,
                nutriment.perServingInUnit?.value,
                nutriment.unit,
                nutriment.modifier
            )
        }
    }

    private fun calculateCalories(portion: Measurement): Measurement {
        val energy100gCal = product.nutriments[Nutriment.ENERGY_KCAL]!!.per100gInG
        val portionGrams = portion.grams.value
        return Measurement(energy100gCal.value / 100 * portionGrams, energy100gCal.unit)
    }

    private fun calculateKj(portion: Measurement): Measurement {
        val energy100gKj = product.nutriments[Nutriment.ENERGY_KJ]!!.per100gInG
        val weightGrams = portion.grams.value
        return Measurement(energy100gKj.value / 100 * weightGrams, energy100gKj.unit)
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

        fun start(context: Context, product: Product, spinnerValue: String, weight: Float) {
            context.startActivity(Intent(context, CalculateDetailsActivity::class.java).apply {
                putExtra(KEY_PRODUCT, product)
                putExtra(KEY_SPINNER_VALUE, spinnerValue)
                putExtra(KEY_WEIGHT, weight)
            })
        }
    }
}