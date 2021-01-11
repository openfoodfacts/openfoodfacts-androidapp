package openfoodfacts.github.scrachx.openfood.features

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
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
            product: Product,
            imageType: ProductImageField,
            mUrlImage: String?,
            mImageFront: View
    ) {
        // A new file added just now
        if (isAbsoluteUrl(mUrlImage)) {
            loadImageServerUrl(fragment, product, imageType, mImageFront)
            return
        }
        startActivity(fragment, mImageFront, createIntent(fragment.context, product, imageType, mUrlImage))
    }

    private fun startActivity(fragment: Fragment, mImageFront: View?, intent: Intent) {
        if (mImageFront != null && fragment.activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(fragment.requireActivity(), mImageFront, fragment.requireActivity().getString(R.string.product_transition))
            fragment.startActivityForResult(intent, ImagesManageActivity.REQUEST_EDIT_IMAGE, options.toBundle())
        } else {
            fragment.startActivityForResult(intent, ImagesManageActivity.REQUEST_EDIT_IMAGE)
        }
    }

    fun openForUrl(
            activity: Activity,
            product: Product,
            imageType: ProductImageField,
            mUrlImage: String?,
            mImageFront: View?
    ) = startActivity(activity, mImageFront, createIntent(activity, product, imageType, mUrlImage))

    private fun startActivity(activity: Activity, mImageFront: View?, intent: Intent) {
        if (mImageFront != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity,
                    mImageFront,
                    activity.getString(R.string.product_transition)
            ).toBundle()
            activity.startActivityForResult(intent, ImagesManageActivity.REQUEST_EDIT_IMAGE, bundle)
        } else activity.startActivityForResult(intent, ImagesManageActivity.REQUEST_EDIT_IMAGE)
    }

    fun openZoom(
            activity: Activity,
            mUrlImage: String?,
            mImageFront: View?
    ) = startActivity(activity, mImageFront, Intent(activity, ImageZoomActivity::class.java).apply {
        putExtra(IMAGE_URL, mUrlImage)
    })

    @CheckResult
    @Contract(pure = true)
    private fun createIntent(context: Context?, product: Product, imageType: ProductImageField, mUrlImage: String?): Intent {
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
            fragment: Fragment,
            product: Product,
            imageType: ProductImageField,
            mImageFront: View
    ): Disposable {
        val client = OpenFoodAPIClient(fragment.requireContext())
        return client.getProductImages(product.code).subscribe { newState ->
            val newStateProduct = newState.product
            if (newStateProduct != null) {
                val language = getLanguage(fragment.context)
                val imageUrl = newStateProduct.getSelectedImage(language, imageType, ImageSize.DISPLAY)
                if (!imageUrl.isNullOrBlank()) {
                    openForUrl(fragment, newStateProduct, imageType, imageUrl, mImageFront)
                } else {
                    Toast.makeText(fragment.context, R.string.cant_edit_image_not_yet_uploaded, Toast.LENGTH_LONG).show()
                }
            }
        }

    }
}