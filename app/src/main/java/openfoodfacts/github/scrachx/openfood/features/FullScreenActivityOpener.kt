package openfoodfacts.github.scrachx.openfood.features

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.annotation.CheckResult
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import io.reactivex.disposables.Disposable
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.images.IMAGE_URL
import openfoodfacts.github.scrachx.openfood.images.ImageSize
import openfoodfacts.github.scrachx.openfood.images.createImageBundle
import openfoodfacts.github.scrachx.openfood.models.Product
import openfoodfacts.github.scrachx.openfood.models.ProductImageField
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLanguage
import openfoodfacts.github.scrachx.openfood.utils.isAbsoluteUrl
import org.jetbrains.annotations.Contract

/**
 * Used to open fullscreen activity
 */
object FullScreenActivityOpener {
    fun openForUrl(
            fragment: Fragment,
            client: OpenFoodAPIClient,
            product: Product,
            imageType: ProductImageField,
            mUrlImage: String,
            mImageFront: View
    ) = openForUrl(fragment.requireActivity(), client, product, imageType, mUrlImage, mImageFront)

    fun openForUrl(
            activity: Activity,
            client: OpenFoodAPIClient,
            product: Product,
            imageType: ProductImageField,
            mUrlImage: String,
            mImageFront: View
    ) {
        // A new file added just now
        if (isAbsoluteUrl(mUrlImage)) {
            loadImageServerUrl(activity, client, product, imageType, mImageFront)
            return
        }
        startActivity(activity, mImageFront, createIntent(activity, product, imageType, mUrlImage))
    }

    private fun startActivity(activity: Activity, mImageFront: View?, intent: Intent) {
        val bundle = mImageFront?.let {
            ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity,
                    mImageFront,
                    activity.getString(R.string.product_transition)
            ).toBundle()
        }
        activity.startActivityForResult(intent, ImagesManageActivity.REQUEST_EDIT_IMAGE, bundle)
    }

    fun openZoom(
            activity: Activity,
            mUrlImage: String,
            mImageFront: View?
    ) = startActivity(activity, mImageFront, Intent(activity, ImageZoomActivity::class.java).apply {
        putExtra(IMAGE_URL, mUrlImage)
    })

    @CheckResult
    @Contract(pure = true)
    private fun createIntent(context: Context, product: Product, imageType: ProductImageField, mUrlImage: String): Intent {
        var language = getLanguage(context)
        if (!product.isLanguageSupported(language) && product.lang.isNotBlank()) {
            language = product.lang
        }
        return Intent(context, ImagesManageActivity::class.java).apply {
            putExtras(createImageBundle(imageType, product, language, mUrlImage))
        }
    }

    @CheckResult
    private fun loadImageServerUrl(
            activity: Activity,
            client: OpenFoodAPIClient,
            product: Product,
            imageType: ProductImageField,
            mImageFront: View
    ): Disposable {
        return client.getProductImages(product.code).subscribe { state ->
            val newProduct = state.product
            if (newProduct != null) {
                val language = getLanguage(activity)
                val imageUrl = newProduct.getSelectedImage(language, imageType, ImageSize.DISPLAY)
                if (!imageUrl.isNullOrBlank()) {
                    openForUrl(activity, client, newProduct, imageType, imageUrl, mImageFront)
                } else {
                    Toast.makeText(activity, R.string.cant_edit_image_not_yet_uploaded, Toast.LENGTH_LONG).show()
                }
            }
        }

    }
}