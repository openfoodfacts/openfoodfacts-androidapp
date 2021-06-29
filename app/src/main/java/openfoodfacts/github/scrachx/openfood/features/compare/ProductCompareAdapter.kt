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
import android.content.pm.PackageManager
import android.os.Build
import android.text.SpannableStringBuilder
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.squareup.picasso.Picasso
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.toObservable
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ProductComparisonListItemBinding
import openfoodfacts.github.scrachx.openfood.features.FullScreenActivityOpener
import openfoodfacts.github.scrachx.openfood.features.shared.adapters.NutrientLevelListAdapter
import openfoodfacts.github.scrachx.openfood.images.ProductImage
import openfoodfacts.github.scrachx.openfood.models.*
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.*
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File

class ProductCompareAdapter(
        private val productsToCompare: List<Product>,
        internal val activity: Activity,
        private val client: OpenFoodAPIClient,
        private val productRepository: ProductRepository,
        private val picasso: Picasso,
        private val language: String,
) : RecyclerView.Adapter<ProductComparisonViewHolder>() {
    private val addProductButton = activity.findViewById<Button>(R.id.product_comparison_button)

    private val disp = CompositeDisposable()
    private val viewHolders = mutableListOf<ProductComparisonViewHolder>()

    private var onPhotoReturnPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductComparisonViewHolder {
        val binding = ProductComparisonListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val viewHolder = ProductComparisonViewHolder(binding)
        viewHolders.add(viewHolder)

        return viewHolder
    }

    override fun onBindViewHolder(holder: ProductComparisonViewHolder, position: Int) {
        if (productsToCompare.isEmpty()) {
            holder.binding.productComparisonListItemLayout.visibility = View.GONE
            return
        }

        // Support synchronous scrolling
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            holder.binding.productComparisonListItemLayout.setOnScrollChangeListener { _, scrollX, scrollY, _, _ ->
                viewHolders.forEach { it.binding.productComparisonListItemLayout.scrollTo(scrollX, scrollY) }
            }
        }

        val product = productsToCompare[position]

        // Set the visibility of UI components
        holder.binding.productComparisonName.visibility = View.VISIBLE
        holder.binding.productComparisonQuantity.visibility = View.VISIBLE
        holder.binding.productComparisonBrand.visibility = View.VISIBLE

        // if the flavor is OpenProductsFacts hide the additives card
        if (isFlavors(OPF)) {
            holder.binding.productComparisonAdditive.visibility = View.GONE
        }

        // Modify the text on the button for adding products
        addProductButton?.setText(R.string.add_another_product)

        // Image
        val imageUrl = product.getImageUrl(language)
        holder.binding.productComparisonImage.setOnClickListener {
            if (imageUrl != null) {
                FullScreenActivityOpener.openForUrl(
                        activity,
                        client,
                        product,
                        ProductImageField.FRONT,
                        imageUrl,
                        holder.binding.productComparisonImage,
                        language
                )
            } else {
                // take a picture
                if (ContextCompat.checkSelfPermission(activity, permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, arrayOf(permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
                } else {
                    onPhotoReturnPosition = position
                    if (isHardwareCameraInstalled(activity)) {
                        EasyImage.openCamera(activity, 0)
                    } else {
                        EasyImage.openGallery(activity, 0, false)
                    }
                }
            }
        }
        if (!imageUrl.isNullOrBlank()) {
            holder.binding.productComparisonLabel.visibility = View.INVISIBLE
            if (!activity.isLowBatteryMode()) {
                picasso.load(imageUrl).into(holder.binding.productComparisonImage)
            } else {
                holder.binding.productComparisonImage.visibility = View.GONE
            }
        }

        // Name
        if (!product.productName.isNullOrBlank()) {
            holder.binding.productComparisonName.text = product.productName
        } else {
            holder.binding.productComparisonName.visibility = View.INVISIBLE
        }

        // Quantity
        if (!product.quantity.isNullOrBlank()) {
            holder.binding.productComparisonQuantity.text = SpannableStringBuilder()
                    .bold { append(activity.getString(R.string.compare_quantity)) }
                    .append(" ")
                    .append(product.quantity)
        } else {
            holder.binding.productComparisonQuantity.visibility = View.INVISIBLE
        }

        // Brands
        val brands = product.brands
        if (!brands.isNullOrBlank()) {
            holder.binding.productComparisonBrand.text = SpannableStringBuilder()
                    .bold { append(activity.getString(R.string.compare_brands)) }
                    .append(" ")
                    .append(brands.split(",").joinToString(", ") { it.trim() })
        } else {
            //TODO: product brand placeholder goes here
        }

        // Open Food Facts specific
        if (isFlavors(AppFlavors.OFF)) {
            // NutriScore
            holder.binding.productComparisonImageGrade.setImageResource(product.getNutriScoreResource())

            // Nova group
            holder.binding.productComparisonNovaGroup.setImageResource(product.getNovaGroupResource())

            // Environment impact
            holder.binding.productComparisonCo2Icon.setImageResource(product.getEcoscoreResource())

            // Nutriments
            holder.binding.productComparisonTextNutrientTxt.text = activity.getString(R.string.txtNutrientLevel100g)
            holder.binding.productComparisonListNutrientLevels.visibility = View.VISIBLE
            holder.binding.productComparisonListNutrientLevels.layoutManager = LinearLayoutManager(activity)
            holder.binding.productComparisonListNutrientLevels.adapter = NutrientLevelListAdapter(activity, loadLevelItems(product))
        } else {
            holder.binding.productComparisonScoresLayout.visibility = View.GONE
            holder.binding.productComparisonNutrientCv.visibility = View.GONE
        }

        // Additives
        if (product.additivesTags.isNotEmpty()) loadAdditives(product, holder.binding.productComparisonAdditiveText)

        // Full product button
        holder.binding.fullProductButton.setOnClickListener {
            val barcode = product.code
            if (Utils.isNetworkConnected(activity)) {
                Utils.hideKeyboard(activity)

                client.openProduct(barcode, activity)
            } else {
                MaterialDialog.Builder(activity).apply {
                    title(R.string.device_offline_dialog_title)
                    content(R.string.connectivity_check)
                    positiveText(R.string.txt_try_again)
                    negativeText(R.string.dismiss)
                    onPositive { _, _ ->
                        if (Utils.isNetworkConnected(activity)) {
                            client.openProduct(barcode, activity)
                        } else {
                            Toast.makeText(activity, R.string.device_offline_dialog_title, Toast.LENGTH_SHORT).show()
                        }
                    }
                }.show()
            }
        }
    }

    private fun loadAdditives(product: Product, view: TextView) {
        product.additivesTags.toObservable()
                .flatMapSingle { tag ->
                    productRepository.getAdditiveByTagAndLanguageCode(tag, language)
                            .flatMap { categoryName ->
                                if (categoryName.isNull) {
                                    productRepository.getAdditiveByTagAndDefaultLanguageCode(tag)
                                } else Single.just(categoryName)
                            }
                }
                .filter { it.isNotNull }
                .toList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnError { Log.e(ProductCompareAdapter::class.simpleName, "loadAdditives", it) }
                .subscribe { additives ->
                    if (additives.isNotEmpty()) {
                        view.text = SpannableStringBuilder()
                            .bold { append(activity.getString(R.string.compare_additives)) }
                            .append("\n")
                            .append(additives.joinToString("\n") { it.name })
                        setMaxCardHeight()
                    }
                }.addTo(disp)
    }

    override fun getItemCount() = productsToCompare.count()

    private fun loadLevelItems(product: Product): List<NutrientLevelItem> {
        val levelItems = mutableListOf<NutrientLevelItem>()

        val nutriments = product.nutriments
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
        if (fat != null || salt != null || saturatedFat != null || sugars != null) {
            val fatNutriment = nutriments[Nutriments.FAT]
            if (fat != null && fatNutriment != null) {
                val fatNutrimentLevel = fat.getLocalize(activity)
                levelItems += NutrientLevelItem(
                        activity.getString(R.string.compare_fat),
                        fatNutriment.displayStringFor100g,
                        fatNutrimentLevel,
                        fat.getImgRes()
                )
            }
            val saturatedFatNutriment = nutriments[Nutriments.SATURATED_FAT]
            if (saturatedFat != null && saturatedFatNutriment != null) {
                val saturatedFatLocalize = saturatedFat.getLocalize(activity)
                levelItems += NutrientLevelItem(
                        activity.getString(R.string.compare_saturated_fat),
                        saturatedFatNutriment.displayStringFor100g,
                        saturatedFatLocalize,
                        saturatedFat.getImgRes()
                )
            }
            val sugarsNutriment = nutriments[Nutriments.SUGARS]
            if (sugars != null && sugarsNutriment != null) {
                val sugarsLocalize = sugars.getLocalize(activity)
                levelItems += NutrientLevelItem(
                        activity.getString(R.string.compare_sugars),
                        sugarsNutriment.displayStringFor100g,
                        sugarsLocalize,
                        sugars.getImgRes()
                )
            }
            val saltNutriment = nutriments[Nutriments.SALT]
            if (salt != null && saltNutriment != null) {
                val saltLocalize = salt.getLocalize(activity)
                levelItems += NutrientLevelItem(
                        activity.getString(R.string.compare_salt),
                        saltNutriment.displayStringFor100g,
                        saltLocalize,
                        salt.getImgRes()
                )
            }
        }
        return levelItems
    }

    fun setImageOnPhotoReturn(file: File) {
        val product = productsToCompare[onPhotoReturnPosition!!]
        val image = ProductImage(
                product.code,
                ProductImageField.FRONT,
                file,
                language
        ).apply { filePath = file.absolutePath }

        client.postImg(image).subscribe().addTo(disp)

        product.imageUrl = file.absolutePath
        onPhotoReturnPosition = null
        notifyDataSetChanged()
    }

    private fun setMaxCardHeight() {
        //getting all the heights of CardViews
        val productDetailsHeight = arrayListOf<Int>()
        val productNutrientsHeight = arrayListOf<Int>()
        val productAdditivesHeight = arrayListOf<Int>()
        viewHolders.forEach {
            productDetailsHeight += it.binding.productComparisonDetailsCv.height
            productNutrientsHeight += it.binding.productComparisonNutrientCv.height
            productAdditivesHeight += it.binding.productComparisonAdditiveText.height
        }

        //setting all the heights to be the maximum
        viewHolders.forEach {
            it.binding.productComparisonDetailsCv.minimumHeight = productDetailsHeight.maxOrNull()!!
            it.binding.productComparisonNutrientCv.minimumHeight = productNutrientsHeight.maxOrNull()!!
            it.binding.productComparisonAdditiveText.height = dpsToPixel(productAdditivesHeight.maxOrNull()!!)
        }
    }

    /**
     * helper method
     */
    private fun dpsToPixel(dps: Int) = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dps + 100f,
            activity.resources.displayMetrics
    ).toInt()

}

class ProductComparisonViewHolder(
        val binding: ProductComparisonListItemBinding
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.fullProductButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fullscreen_blue_18dp, 0, 0, 0)
    }
}
