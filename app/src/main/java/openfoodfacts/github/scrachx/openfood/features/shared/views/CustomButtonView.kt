package openfoodfacts.github.scrachx.openfood.features.shared.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import openfoodfacts.github.scrachx.openfood.R

/**
 * Based on
 * [this](https://stackoverflow.com/questions/35761636/is-it-possible-to-use-vectordrawable-in-buttons-and-textviews-using-androiddraw)
 * and
 * [this](https://medium.com/@elye.project/better-way-of-declaring-custom-view-attributes-23f876c28534)
 */
class CustomButtonView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    init {
        if (attrs != null) {
            val attributeArray = context.obtainStyledAttributes(attrs, R.styleable.CustomButtonView)
            var drawableLeft: Drawable? = null
            var drawableRight: Drawable? = null
            var drawableBottom: Drawable? = null
            var drawableTop: Drawable? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawableLeft = attributeArray.getDrawable(R.styleable.CustomButtonView_drawableLeftCompat)
                drawableRight = attributeArray.getDrawable(R.styleable.CustomButtonView_drawableRightCompat)
                drawableBottom = attributeArray.getDrawable(R.styleable.CustomButtonView_drawableBottomCompat)
                drawableTop = attributeArray.getDrawable(R.styleable.CustomButtonView_drawableTopCompat)
            } else {
                val drawableLeftId = attributeArray.getResourceId(R.styleable.CustomButtonView_drawableLeftCompat, -1)
                val drawableRightId = attributeArray.getResourceId(R.styleable.CustomButtonView_drawableRightCompat, -1)
                val drawableBottomId = attributeArray.getResourceId(R.styleable.CustomButtonView_drawableBottomCompat, -1)
                val drawableTopId = attributeArray.getResourceId(R.styleable.CustomButtonView_drawableTopCompat, -1)
                if (drawableLeftId != -1) drawableLeft = AppCompatResources.getDrawable(context, drawableLeftId)
                if (drawableRightId != -1) drawableRight = AppCompatResources.getDrawable(context, drawableRightId)
                if (drawableBottomId != -1) drawableBottom = AppCompatResources.getDrawable(context, drawableBottomId)
                if (drawableTopId != -1) drawableTop = AppCompatResources.getDrawable(context, drawableTopId)
            }
            setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight, drawableBottom)
            attributeArray.recycle()
        }
    }

}