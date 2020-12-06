/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package openfoodfacts.github.scrachx.openfood.features.compare

import android.Manifest.permission
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.features.FullScreenActivityOpener
import openfoodfacts.github.scrachx.openfood.features.shared.adapters.NutrientLevelListAdapter
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.*
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.*
import org.apache.commons.lang.StringUtils
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import java.util.*

class ProductCompareAdapter(private val productsToCompare: List<Product>, internal val activity: Activity) : RecyclerView.Adapter<ProductComparisonViewHolder>() {
    private val addProductButton = activity.findViewById<Button>(R.id.product_comparison_button)
    private val api = OpenFoodAPIClient(activity)
    private var isLowBatteryMode = false
    private val disp = CompositeDisposable()
    private val viewHolders = mutableListOf<ProductComparisonViewHolder>()
    private var onPhotoReturnPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductComparisonViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.product_comparison_list_item, parent, false)
        val viewHolder = ProductComparisonViewHolder(v)
        viewHolders.add(viewHolder)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ProductComparisonViewHolder, position: Int) {
        if (productsToCompare.isEmpty()) {
            holder.listItemLayout.visibility = View.GONE
            return
        }

        // Support synchronous scrolling
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            holder.listItemLayout.setOnScrollChangeListener { _: View?, scrollX: Int, scrollY: Int, _: Int, _: Int ->
                for (viewHolder in viewHolders) {
                    viewHolder.listItemLayout.scrollX = scrollX
                    viewHolder.listItemLayout.scrollY = scrollY
                }
            }
        }

        val product = productsToCompare[position]

        // Set the visibility of UI components
        holder.productNameTextView.visibility = View.VISIBLE
        holder.productQuantityTextView.visibility = View.VISIBLE
        holder.productBrandTextView.visibility = View.VISIBLE

        // Modify the text on the button for adding products
        addProductButton?.setText(R.string.add_another_product)

        // Image
        val imageUrl = product.getImageUrl(LocaleHelper.getLanguage(activity))
        holder.productComparisonImage.setOnClickListener {
            if (imageUrl != null) {
                FullScreenActivityOpener.openForUrl(activity, product, ProductImageField.FRONT, imageUrl, holder.productComparisonImage)
            } else {
                // take a picture
                if (ContextCompat.checkSelfPermission(activity, permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, arrayOf(permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
                } else {
                    onPhotoReturnPosition = position
                    if (Utils.isHardwareCameraInstalled(activity)) {
                        EasyImage.openCamera(activity, 0)
                    } else {
                        EasyImage.openGallery(activity, 0, false)
                    }
                }
            }
        }
        if (!imageUrl.isNullOrBlank()) {
            holder.productComparisonLabel.visibility = View.INVISIBLE
            if (Utils.isDisableImageLoad(activity) && Utils.isBatteryLevelLow(activity)) {
                isLowBatteryMode = true
            }
            // Load Image if isLowBatteryMode is false
            if (!isLowBatteryMode) {
                Utils.picassoBuilder(activity)
                        .load(imageUrl)
                        .into(holder.productComparisonImage)
            } else {
                holder.productComparisonImage.visibility = View.GONE
            }
        }

        // Name
        if (!product.productName.isNullOrBlank()) {
            holder.productNameTextView.text = product.productName
        } else {
            //TODO: product name placeholder text goes here
        }

        // Quantity
        if (StringUtils.isNotBlank(product.quantity)) {
            holder.productQuantityTextView.text = bold(
                    activity.getString(R.string.compare_quantity)
            )
            holder.productQuantityTextView.append(' '.toString() + product.quantity)
        } else {
            //TODO: product quantity placeholder goes here
        }

        // Brands
        val brands = product.brands
        if (!brands.isNullOrBlank()) {
            holder.productBrandTextView.text = bold(activity.getString(R.string.compare_brands))
            holder.productBrandTextView.append(" ")
            val brandsList = brands.split(",").toTypedArray()
            for (i in 0 until brandsList.size - 1) {
                holder.productBrandTextView.append(brandsList[i].trim { it <= ' ' })
                holder.productBrandTextView.append(", ")
            }
            holder.productBrandTextView.append(brandsList[brandsList.size - 1].trim { it <= ' ' })
        } else {
            //TODO: product brand placeholder goes here
        }

        // Open Food Facts specific
        if (isFlavors(AppFlavors.OFF)) {
            // NutriScore
            val nutritionGradeResource = getImageGradeDrawable(activity, product)
            if (nutritionGradeResource != null) {
                holder.productComparisonImageGrade.visibility = View.VISIBLE
                holder.productComparisonImageGrade.setImageDrawable(nutritionGradeResource)
            } else {
                holder.productComparisonImageGrade.visibility = View.INVISIBLE
            }

            // Nova group
            if (product.novaGroups != null) {
                holder.productComparisonNovaGroup.setImageResource(Utils.getNovaGroupDrawable(product.novaGroups))
            } else {
                holder.productComparisonNovaGroup.visibility = View.INVISIBLE
            }

            // Environment impact
            val environmentImpactResource = Utils.getImageEnvironmentImpact(product)
            if (environmentImpactResource != Utils.NO_DRAWABLE_RESOURCE) {
                holder.productComparisonCo2Icon.visibility = View.VISIBLE
                holder.productComparisonCo2Icon.setImageResource(environmentImpactResource)
            } else {
                holder.productComparisonCo2Icon.visibility = View.GONE
            }

            // Nutriments
            holder.nutrientsRecyclerView.visibility = View.VISIBLE
            holder.productComparisonNutrientText.text = activity.getString(R.string.txtNutrientLevel100g)
            holder.nutrientsRecyclerView.layoutManager = LinearLayoutManager(activity)
            holder.nutrientsRecyclerView.adapter = NutrientLevelListAdapter(activity, loadLevelItems(product))
        } else {
            holder.productComparisonScoresLayout.visibility = View.GONE
            holder.productComparisonNutrientCv.visibility = View.GONE
        }

        // Additives
        if (product.additivesTags.isNotEmpty()) loadAdditives(product, holder.productComparisonAdditiveText)

        // Full product button
        holder.fullProductButton.setOnClickListener { view: View? ->
            if (product != null) {
                val barcode = product.code
                if (Utils.isNetworkConnected(activity)) {
                    api.openProduct(barcode, activity)
                    try {
                        val view1 = activity.currentFocus
                        if (view != null) {
                            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(view1!!.windowToken, 0)
                        }
                    } catch (e: NullPointerException) {
                        Log.e(ProductCompareAdapter::class.java.simpleName, "setOnClickListener", e)
                    }
                } else {
                    MaterialDialog.Builder(activity).apply {
                        title(R.string.device_offline_dialog_title)
                        content(R.string.connectivity_check)
                        positiveText(R.string.txt_try_again)
                        negativeText(R.string.dismiss)
                        onPositive { _, _ ->
                            if (Utils.isNetworkConnected(context)) {
                                api.openProduct(barcode, context as Activity)
                            } else {
                                Toast.makeText(context, R.string.device_offline_dialog_title, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }.show()
                }
            }
        }
    }

    private fun loadAdditives(product: Product, v: View) {
        val additivesBuilder = StringBuilder()
        val additivesTags = product.additivesTags
        if (additivesTags.isEmpty()) {
            return
        }
        val languageCode = LocaleHelper.getLanguage(v.context)
        disp.add(
                Observable.fromArray(*additivesTags.toTypedArray())
                        .flatMapSingle { tag: String? ->
                            return@flatMapSingle ProductRepository.getAdditiveByTagAndLanguageCode(tag, languageCode)
                                    .flatMap { categoryName: AdditiveName ->
                                        if (categoryName.isNull) {
                                            return@flatMap ProductRepository.getAdditiveByTagAndDefaultLanguageCode(tag)
                                        } else {
                                            return@flatMap Single.just(categoryName)
                                        }
                                    }
                        }
                        .filter { it.isNotNull }
                        .toList()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ additives: List<AdditiveName> ->
                            if (additives.isNotEmpty()) {
                                additivesBuilder.append(bold(activity.getString(R.string.compare_additives)))
                                additivesBuilder.append(" ")
                                additivesBuilder.append("\n")
                                for (i in 0 until additives.size - 1) {
                                    additivesBuilder.append(additives[i].name)
                                    additivesBuilder.append("\n")
                                }
                                additivesBuilder.append(additives[additives.size - 1].name)
                                (v as TextView).text = additivesBuilder.toString()
                                setMaxCardHeight()
                            }
                        })
                        { e -> Log.e(ProductCompareAdapter::class.java.simpleName, "loadAdditives", e) }
        )
    }

    override fun getItemCount() = productsToCompare.size

    private fun loadLevelItems(product: Product?): List<NutrientLevelItem> {
        val levelItem: MutableList<NutrientLevelItem> = ArrayList()
        val nutriments = product!!.nutriments
        val nutrientLevels = product.nutrientLevels
        var fat: NutrimentLevel? = null
        var saturatedFat: NutrimentLevel? = null
        var sugars: NutrimentLevel? = null
        var salt: NutrimentLevel? = null
        if (nutrientLevels != null) {
            fat = nutrientLevels.fat
            saturatedFat = nutrientLevels.saturatedFat
            sugars = nutrientLevels.sugars
            salt = nutrientLevels.salt
        }
        if (!(fat == null && salt == null && saturatedFat == null && sugars == null)) {
            val fatNutriment = nutriments[Nutriments.FAT]
            if (fat != null && fatNutriment != null) {
                val fatNutrimentLevel = fat.getLocalize(activity)
                levelItem.add(NutrientLevelItem(
                        activity.getString(R.string.compare_fat),
                        fatNutriment.displayStringFor100g,
                        fatNutrimentLevel,
                        fat.getImageLevel()))
            }
            val saturatedFatNutriment = nutriments[Nutriments.SATURATED_FAT]
            if (saturatedFat != null && saturatedFatNutriment != null) {
                val saturatedFatLocalize = saturatedFat.getLocalize(activity)
                levelItem.add(NutrientLevelItem(
                        activity.getString(R.string.compare_saturated_fat),
                        saturatedFatNutriment.displayStringFor100g,
                        saturatedFatLocalize,
                        saturatedFat.getImageLevel()))
            }
            val sugarsNutriment = nutriments[Nutriments.SUGARS]
            if (sugars != null && sugarsNutriment != null) {
                val sugarsLocalize = sugars.getLocalize(activity)
                levelItem.add(NutrientLevelItem(
                        activity.getString(R.string.compare_sugars),
                        sugarsNutriment.displayStringFor100g,
                        sugarsLocalize,
                        sugars.getImageLevel()))
            }
            val saltNutriment = nutriments[Nutriments.SALT]
            if (salt != null && saltNutriment != null) {
                val saltLocalize = salt.getLocalize(activity)
                levelItem.add(NutrientLevelItem(
                        activity.getString(R.string.compare_salt),
                        saltNutriment.displayStringFor100g,
                        saltLocalize,
                        salt.getImageLevel()
                ))
            }
        }
        return levelItem
    }

    fun setImageOnPhotoReturn(file: File) {
        val product = productsToCompare[onPhotoReturnPosition!!]
        val image = ProductImage(product.code, ProductImageField.FRONT, file)
        image.filePath = file.absolutePath
        disp.add(api.postImg(image).subscribe())
        val mUrlImage = file.absolutePath
        product.imageUrl = mUrlImage
        onPhotoReturnPosition = null
        notifyDataSetChanged()
    }

    private fun setMaxCardHeight() {
        //getting all the heights of CardViews
        val productDetailsHeight = ArrayList<Int>()
        val productNutrientsHeight = ArrayList<Int>()
        val productAdditivesHeight = ArrayList<Int>()
        for (current in viewHolders) {
            productDetailsHeight.add(current.productComparisonDetailsCv.height)
            productNutrientsHeight.add(current.productComparisonNutrientCv.height)
            productAdditivesHeight.add(current.productComparisonAdditiveText.height)
        }

        //setting all the heights to be the maximum
        for (current in viewHolders) {
            current.productComparisonDetailsCv.minimumHeight = Collections.max(productDetailsHeight)
            current.productComparisonNutrientCv.minimumHeight = Collections.max(productNutrientsHeight)
            current.productComparisonAdditiveText.height = dpsToPixel(Collections.max(productAdditivesHeight))
        }
    }

    /**
     * helper method
     */
    private fun dpsToPixel(dps: Int) =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps + 100f, activity.resources.displayMetrics)
                    .toInt()

}

class ProductComparisonViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val fullProductButton: Button = view.findViewById(R.id.full_product_button)
    val listItemLayout: NestedScrollView = view.findViewById(R.id.product_comparison_list_item_layout)
    val nutrientsRecyclerView: RecyclerView = view.findViewById(R.id.product_comparison_listNutrientLevels)
    val productBrandTextView: TextView = view.findViewById(R.id.product_comparison_brand)
    val productComparisonAdditiveCv: CardView = view.findViewById(R.id.product_comparison_additive)
    val productComparisonAdditiveText: TextView = view.findViewById(R.id.product_comparison_additive_text)
    val productComparisonCo2Icon: ImageView = view.findViewById(R.id.product_comparison_co2_icon)
    val productComparisonDetailsCv: CardView = view.findViewById(R.id.product_comparison_details_cv)
    val productComparisonImage: ImageButton = view.findViewById(R.id.product_comparison_image)
    val productComparisonImageGrade: ImageView = view.findViewById(R.id.product_comparison_imageGrade)
    val productComparisonLabel: TextView = view.findViewById(R.id.product_comparison_label)
    val productComparisonNovaGroup: ImageView = view.findViewById(R.id.product_comparison_nova_group)
    val productComparisonNutrientCv: CardView = view.findViewById(R.id.product_comparison_nutrient_cv)
    val productComparisonNutrientText: TextView = view.findViewById(R.id.product_comparison_textNutrientTxt)
    val productComparisonScoresLayout: RelativeLayout = view.findViewById(R.id.product_comparison_scores_layout)
    val productNameTextView: TextView = view.findViewById(R.id.product_comparison_name)
    val productQuantityTextView: TextView = view.findViewById(R.id.product_comparison_quantity)

    init {
        fullProductButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fullscreen_blue_18dp, 0, 0, 0)
    }
}