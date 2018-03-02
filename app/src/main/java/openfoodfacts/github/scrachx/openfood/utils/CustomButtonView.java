package openfoodfacts.github.scrachx.openfood.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

import openfoodfacts.github.scrachx.openfood.R;

/**
 * Based on
 * <a href="http://stackoverflow.com/questions/35761636/is-it-possible-to-use-vectordrawable-in-buttons-and-textviews-using-androiddraw></a>
 * and <a href="https://medium.com/@elye.project/better-way-of-declaring-custom-view-attributes-23f876c28534>this</a>
 */

public class CustomButtonView extends AppCompatButton {


    public CustomButtonView(Context context) {
        super(context);
    }

    public CustomButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    public CustomButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray attributeArray = context.obtainStyledAttributes(
                    attrs,
                    R.styleable.CustomButtonView);

            Drawable drawableLeft = null;
            Drawable drawableRight = null;
            Drawable drawableBottom = null;
            Drawable drawableTop = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawableLeft = attributeArray.getDrawable(R.styleable.CustomButtonView_drawableLeftCompat);
                drawableRight = attributeArray.getDrawable(R.styleable.CustomButtonView_drawableRightCompat);
                drawableBottom = attributeArray.getDrawable(R.styleable.CustomButtonView_drawableBottomCompat);
                drawableTop = attributeArray.getDrawable(R.styleable.CustomButtonView_drawableTopCompat);
            } else {
                final int drawableLeftId = attributeArray.getResourceId(R.styleable.CustomButtonView_drawableLeftCompat, -1);
                final int drawableRightId = attributeArray.getResourceId(R.styleable.CustomButtonView_drawableRightCompat, -1);
                final int drawableBottomId = attributeArray.getResourceId(R.styleable.CustomButtonView_drawableBottomCompat, -1);
                final int drawableTopId = attributeArray.getResourceId(R.styleable.CustomButtonView_drawableTopCompat, -1);

                if (drawableLeftId != -1)
                    drawableLeft = AppCompatResources.getDrawable(context, drawableLeftId);
                if (drawableRightId != -1)
                    drawableRight = AppCompatResources.getDrawable(context, drawableRightId);
                if (drawableBottomId != -1)
                    drawableBottom = AppCompatResources.getDrawable(context, drawableBottomId);
                if (drawableTopId != -1)
                    drawableTop = AppCompatResources.getDrawable(context, drawableTopId);
            }
            setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight,
                    drawableBottom);
            attributeArray.recycle();
        }
    }


}
