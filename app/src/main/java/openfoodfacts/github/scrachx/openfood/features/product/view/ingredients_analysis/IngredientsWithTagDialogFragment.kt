package openfoodfacts.github.scrachx.openfood.features.product.view.ingredients_analysis

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.customtabs.CustomTabActivityHelper
import openfoodfacts.github.scrachx.openfood.databinding.IngredientsWithTagBinding
import openfoodfacts.github.scrachx.openfood.features.product.view.IProductView
import openfoodfacts.github.scrachx.openfood.features.product.view.ProductViewActivity
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig.AnalysisTagConfig
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class IngredientsWithTagDialogFragment : DialogFragment() {
    private var _binding: IngredientsWithTagBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var picasso: Picasso

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    var onDismissListener: ((DialogInterface) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = IngredientsWithTagBinding.inflate(inflater, container, false)
        requireDialog().window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        requireDialog().window!!.setGravity(Gravity.CENTER)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val arguments = requireArguments()
        val tag = arguments.getString(TAG_KEY)
        val type = arguments.getString(TYPE_KEY)
        val typeName = arguments.getString(TYPE_NAME_KEY) ?: "UNKNOWN"
        val iconUrl = arguments.getString(ICON_URL_KEY)
        val color = arguments.getString(COLOR_KEY)
        val name = arguments.getString(NAME_KEY)
        val ingredients = arguments.getString(INGREDIENTS_KEY)
        val ambiguousIngredient = arguments.getString(AMBIGUOUS_INGREDIENT_KEY)
        val ingredientsImageUrl = arguments.getString(INGREDIENTS_IMAGE_URL_KEY)

        picasso
            .load(iconUrl)
            .into(binding.icon)
        binding.iconFrame.background =
            ResourcesCompat.getDrawable(requireActivity().resources, R.drawable.rounded_button, requireActivity().theme)
                ?.apply {
                    colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                        Color.parseColor(color),
                        BlendModeCompat.SRC_IN
                    )
                }
        binding.title.text = name
        binding.cb.let {
            it.text = getString(R.string.display_analysis_tag_status, typeName.lowercase(Locale.getDefault()))
            it.isChecked = sharedPreferences.getBoolean(type, true)
            it.setOnCheckedChangeListener { _, isChecked ->
                sharedPreferences.edit { putBoolean(type, isChecked) }
            }
        }
        var messageToBeShown =
            HtmlCompat.fromHtml(
                getString(
                    R.string.ingredients_in_this_product_are,
                    name!!.lowercase(Locale.getDefault())
                ), HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        val showHelpTranslate = tag != null && tag.contains("unknown")

        if (arguments.getBoolean(PHOTOS_TO_BE_VALIDATED_KEY, false)) {
            messageToBeShown = HtmlCompat.fromHtml(
                getString(R.string.unknown_status_missing_ingredients),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            binding.image.setImageResource(R.drawable.ic_add_a_photo_dark_48dp)
            binding.image.setOnClickListener { goToAddPhoto() }
            binding.helpNeeded.text = HtmlCompat.fromHtml(
                getString(R.string.add_photo_to_extract_ingredients),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            binding.helpNeeded.setOnClickListener { goToAddPhoto() }
        } else if (tag != null && ambiguousIngredient != null) {
            messageToBeShown =
                HtmlCompat.fromHtml(
                    getString(R.string.unknown_status_ambiguous_ingredients, ambiguousIngredient),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            binding.helpNeeded.visibility = View.GONE
        } else if (showHelpTranslate && arguments.getBoolean(MISSING_INGREDIENTS_KEY, false)) {
            picasso.load(ingredientsImageUrl).into(binding.image)

            binding.image.setOnClickListener { goToExtract() }
            messageToBeShown = HtmlCompat.fromHtml(
                getString(R.string.unknown_status_missing_ingredients),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )

            binding.helpNeeded.text =
                HtmlCompat.fromHtml(
                    getString(
                        R.string.help_extract_ingredients,
                        typeName.lowercase(Locale.getDefault())
                    ), HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            binding.helpNeeded.setOnClickListener { goToExtract() }
            binding.helpNeeded.visibility = View.VISIBLE

        } else if (showHelpTranslate) {
            messageToBeShown =
                HtmlCompat.fromHtml(getString(R.string.unknown_status_no_translation), HtmlCompat.FROM_HTML_MODE_LEGACY)
            binding.helpNeeded.text =
                HtmlCompat.fromHtml(getString(R.string.help_translate_ingredients), HtmlCompat.FROM_HTML_MODE_LEGACY)
            binding.helpNeeded.setOnClickListener {
                val customTabsIntent = CustomTabsIntent.Builder().build()
                CustomTabActivityHelper.openCustomTab(
                    requireActivity(),  // activity
                    customTabsIntent,
                    Uri.parse(getString(R.string.help_translate_ingredients_link, Locale.getDefault().language))
                ) { activity: Activity, uri: Uri ->
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = uri
                    activity.startActivity(i)
                }
            }
            binding.helpNeeded.visibility = View.VISIBLE
        } else {
            binding.image.visibility = View.GONE
            if (!TextUtils.isEmpty(ingredients)) {
                messageToBeShown = HtmlCompat.fromHtml(
                    "${
                        getString(
                            R.string.ingredients_in_this_product,
                            name.lowercase(Locale.getDefault())
                        )
                    }$ingredients",
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }
            binding.helpNeeded.visibility = View.GONE
        }
        val message: AppCompatTextView = binding.message
        message.text = messageToBeShown
        binding.close.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun goToAddPhoto() {
        dismiss()
        (activity as? IProductView)?.showIngredientsTab(ProductViewActivity.ShowIngredientsAction.SEND_UPDATED)
    }

    private fun goToExtract() {
        dismiss()
        (activity as? IProductView)?.showIngredientsTab(ProductViewActivity.ShowIngredientsAction.PERFORM_OCR)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke(dialog)
    }

    companion object {
        private const val TAG_KEY = "tag"
        private const val TYPE_KEY = "type"
        private const val TYPE_NAME_KEY = "type_name"
        private const val ICON_URL_KEY = "icon_url"
        private const val COLOR_KEY = "color"
        private const val NAME_KEY = "name"
        private const val INGREDIENTS_IMAGE_URL_KEY = "ingredients_image_url"
        private const val INGREDIENTS_KEY = "ingredients"
        private const val PACKAGING_IMAGE_URL_KEY = "packaging_image_url"
        private const val MISSING_INGREDIENTS_KEY = "missing_ingredients"
        private const val PHOTOS_TO_BE_VALIDATED_KEY = "photos_to_be_validated"
        private const val AMBIGUOUS_INGREDIENT_KEY = "ambiguous_ingredient"

        @JvmStatic
        fun newInstance(product: Product, config: AnalysisTagConfig) = IngredientsWithTagDialogFragment().apply {
            arguments = Bundle().apply {
                putString(TAG_KEY, config.analysisTag)
                putString(TYPE_KEY, config.type)
                putString(TYPE_NAME_KEY, config.typeName)
                putString(ICON_URL_KEY, config.iconUrl)
                putString(COLOR_KEY, config.color)
                putString(NAME_KEY, config.name.name)
                putString(INGREDIENTS_IMAGE_URL_KEY, product.imageIngredientsUrl)

                if (product.ingredients.isEmpty()) {
                    val statesTags = product.statesTags
                    var ingredientsToBeCompleted = false
                    var photosToBeValidated = false
                    statesTags.forEach { stateTag ->
                        ingredientsToBeCompleted = stateTag == "en:ingredients-to-be-completed"
                        photosToBeValidated = stateTag == "en:photos-to-be-validated"
                    }
                    if (ingredientsToBeCompleted && photosToBeValidated) {
                        putBoolean(PHOTOS_TO_BE_VALIDATED_KEY, true)
                    } else {
                        putBoolean(MISSING_INGREDIENTS_KEY, true)
                    }
                } else {
                    val showIngredients = config.name.showIngredients
                    if (showIngredients != null) {
                        putSerializable(
                            INGREDIENTS_KEY,
                            getMatchingIngredientsText(product, showIngredients.split(":").toTypedArray())
                        )
                    }
                    val ambiguousIngredient = product.ingredients
                        .filter {
                            it.additionalProperties.containsKey(config.type) && it.additionalProperties.containsValue(
                                "maybe"
                            )
                        }
                        .mapNotNull { it.text }


                    if (ambiguousIngredient.isNotEmpty()) {
                        putString(AMBIGUOUS_INGREDIENT_KEY, ambiguousIngredient.joinToString(","))
                    }
                }
            }
        }

        private fun getMatchingIngredientsText(
            product: Product,
            ingredients: Array<String>,
        ): String? {

            val matchingIngredients = product.ingredients
                .filter { ingredients[1] == it.additionalProperties[ingredients[0]] }
                .mapNotNull { it.text }
                .map { it.lowercase(Locale.getDefault()).replace("_", "") }


            return if (matchingIngredients.isEmpty()) null
            else StringBuilder().apply {
                append(" <b>")
                matchingIngredients.withIndex().forEach { (index, ingredient) ->
                    if (index != 0) append(", ")
                    append(ingredient)
                }
                append("</b>")
            }.toString()
        }
    }
}
