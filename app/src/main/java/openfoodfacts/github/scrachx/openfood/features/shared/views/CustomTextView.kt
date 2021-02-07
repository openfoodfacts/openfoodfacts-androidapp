package openfoodfacts.github.scrachx.openfood.features.shared.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatTextView
import openfoodfacts.github.scrachx.openfood.R

class CustomTextView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {


    init {
        if (attrs != null) {
            val attributeArray = context.obtainStyledAttributes(attrs, R.styleable.CustomTextView)
            var drawableLeft: Drawable? = null
            var drawableRight: Drawable? = null
            var drawableBottom: Drawable? = null
            var drawableTop: Drawable? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawableLeft = attributeArray.getDrawable(R.styleable.CustomTextView_drawableLeftCompat)
                drawableRight = attributeArray.getDrawable(R.styleable.CustomTextView_drawableRightCompat)
                drawableBottom = attributeArray.getDrawable(R.styleable.CustomTextView_drawableBottomCompat)
                drawableTop = attributeArray.getDrawable(R.styleable.CustomTextView_drawableTopCompat)
            } else {
                val drawableLeftId = attributeArray.getResourceId(R.styleable.CustomTextView_drawableLeftCompat, -1)
                val drawableRightId = attributeArray.getResourceId(R.styleable.CustomTextView_drawableRightCompat, -1)
                val drawableBottomId = attributeArray.getResourceId(R.styleable.CustomTextView_drawableBottomCompat, -1)
                val drawableTopId = attributeArray.getResourceId(R.styleable.CustomTextView_drawableTopCompat, -1)
                if (drawableLeftId != -1) drawableLeft = AppCompatResources.getDrawable(context, drawableLeftId)
                if (drawableRightId != -1) drawableRight = AppCompatResources.getDrawable(context, drawableRightId)
                if (drawableBottomId != -1) drawableBottom = AppCompatResources.getDrawable(context, drawableBottomId)
                if (drawableTopId != -1) drawableTop = AppCompatResources.getDrawable(context, drawableTopId)
            }
            attributeArray.recycle()
            setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight, drawableBottom)
        }
    }

}