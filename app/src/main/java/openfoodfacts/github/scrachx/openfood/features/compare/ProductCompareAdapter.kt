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
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import openfoodfacts.github.scrachx.openfood.AppFlavors
import openfoodfacts.github.scrachx.openfood.AppFlavors.OPF
import openfoodfacts.github.scrachx.openfood.AppFlavors.isFlavors
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.ProductComparisonListItemBinding
import openfoodfacts.github.scrachx.openfood.features.FullScreenActivityOpener
import openfoodfacts.github.scrachx.openfood.features.shared.adapters.NutrientLevelListAdapter
import openfoodfacts.github.scrachx.openfood.models.*
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName
import openfoodfacts.github.scrachx.openfood.repositories.ProductRepository
import openfoodfacts.github.scrachx.openfood.utils.*
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File

class ProductCompareAdapter(
    private val compareProducts: List<ProductCompareViewModel.CompareProduct>,
    internal val activity: Activity,
    private val lifecycleOwner: LifecycleOwner,
    private val client: ProductRepository,
    private val picasso: Picasso,
    private val language: String

) : RecyclerView.Adapter<ProductCompareAdapter.ViewHolder>() {
    var imageReturnedListener: ((Product, File) -> Unit)? = null
    var fullProductClickListener: ((Product) -> Unit)? = null

    private val addProductButton = activity.findViewById<Button>(R.id.product_comparison_button)
    private val viewHolders = mutableListOf<ViewHolder>()
    private var imageReturnedPosition: Int? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ProductComparisonListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        val viewHolder = ViewHolder(binding)
        viewHolders += viewHolder

        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (compareProducts.isEmpty()) {
            holder.binding.productComparisonListItemLayout.visibility = View.GONE
            return
        }

        // Support synchronous scrolling
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            holder.binding.productComparisonListItemLayout.setOnScrollChangeListener { _, scrollX, scrollY, _, _ ->
                viewHolders.forEach { it.binding.productComparisonListItemLayout.scrollTo(scrollX, scrollY) }
            }
        }

        val compareProduct = compareProducts[position]
        val product = compareProduct.product

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
                lifecycleOwner.lifecycle.coroutineScope.launchWhenCreated {
                    FullScreenActivityOpener.openForUrl(
                        activity,
                        client,
                        product,
                        ProductImageField.FRONT,
                        imageUrl,
                        holder.binding.productComparisonImage,
                        language
                    )
                }
            } else {
                // take a picture
                when {
                    checkSelfPermission(activity, permission.CAMERA) != PERMISSION_GRANTED -> {
                        ActivityCompat.requestPermissions(activity, arrayOf(permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
                    }
                    else -> {
                        imageReturnedPosition = holder.bindingAdapterPosition
                        if (isHardwareCameraInstalled(activity)) {
                            EasyImage.openCamera(activity, 0)
                        } else {
                            EasyImage.openGallery(activity, 0, false)
                        }
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
            holder.binding.productComparisonQuantity.text = buildSpannedString {
                bold { append(activity.getString(R.string.compare_quantity)) }
                append(" ")
                append(product.quantity)
            }
        } else {
            holder.binding.productComparisonQuantity.visibility = View.INVISIBLE
        }

        // Brands
        val brands = product.brands
        if (!brands.isNullOrBlank()) {
            holder.binding.productComparisonBrand.text = buildSpannedString {
                bold { append(activity.getString(R.string.compare_brands)) }
                append(" ")
                append(brands.split(",").joinToString(", ") { it.trim() })
            }
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
            holder.binding.productComparisonListNutrientLevels.adapter =
                NutrientLevelListAdapter(activity, getLevelItems(product))
        } else {
            holder.binding.productComparisonScoresLayout.visibility = View.GONE
            holder.binding.productComparisonNutrientCv.visibility = View.GONE
        }

        // Additives
        if (product.additivesTags.isNotEmpty()) loadAdditives(compareProduct.additiveNames, holder.binding.productComparisonAdditiveText)

        // Full product button
        holder.binding.fullProductButton.setOnClickListener { fullProductClickListener?.invoke(product) }
    }

    override fun getItemCount() = compareProducts.count()

    private fun loadAdditives(additiveNames: List<AdditiveName>, view: TextView) {
        if (additiveNames.isEmpty()) return

        view.text = buildSpannedString {
            bold { append(activity.getString(R.string.compare_additives)) }
            append("\n")
            append(additiveNames.joinToString("\n") { it.name })
        }

        updateCardsHeight()
    }

    private fun getLevelItems(product: Product): List<NutrientLevelItem> {
        val nutriments = product.nutriments
        val nutrientLevels = product.nutrientLevels

        return listOfNotNull(
            nutriments.buildLevelItem(activity, Nutriment.FAT, nutrientLevels?.fat),
            nutriments.buildLevelItem(activity, Nutriment.SATURATED_FAT, nutrientLevels?.saturatedFat),
            nutriments.buildLevelItem(activity, Nutriment.SUGARS, nutrientLevels?.sugars),
            nutriments.buildLevelItem(activity, Nutriment.SALT, nutrientLevels?.salt)
        )
    }


    fun onImageReturned(file: File) {
        val pos = imageReturnedPosition
        checkNotNull(pos) { "Position null." }

        imageReturnedListener?.invoke(compareProducts[pos].product, file)
        imageReturnedPosition = null
        notifyItemChanged(pos)
    }

    private fun updateCardsHeight() {
        // Get all the heights of CardViews
        val detailsHeights = arrayListOf<Int>()
        val nutrientsHeights = arrayListOf<Int>()
        val additivesHeights = arrayListOf<Int>()

        viewHolders.forEach {
            detailsHeights += it.binding.productComparisonDetailsCv.height
            nutrientsHeights += it.binding.productComparisonNutrientCv.height
            additivesHeights += it.binding.productComparisonAdditiveText.height
        }

        // Set all the heights to be the maximum
        viewHolders.forEach { vh ->
            detailsHeights.maxOrNull()
                ?.let { vh.binding.productComparisonDetailsCv.minimumHeight = it }
            nutrientsHeights.maxOrNull()
                ?.let { vh.binding.productComparisonNutrientCv.minimumHeight = it }
            additivesHeights.maxOrNull()?.toPx(activity)
                ?.let { vh.binding.productComparisonAdditiveText.height = it }
        }
    }


    class ViewHolder(
        val binding: ProductComparisonListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.fullProductButton.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_fullscreen_blue_18dp,
                0,
                0,
                0
            )
        }
    }
}

