package openfoodfacts.github.scrachx.openfood.features.shared.views

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.view.*
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.preference.PreferenceManager
import openfoodfacts.github.scrachx.openfood.R
import kotlin.math.roundToLong

class TipBox(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    var shouldAnimate: Boolean
    private val arrowView: ImageView
    var identifier: String?
    private val prefs: SharedPreferences
    private val tipMessageView: TextView


    fun setTipMessage(message: CharSequence?) {
        tipMessageView.text = message
    }

    fun setArrowAlignment(arrowAlignment: Int, marginStart: Int, marginEnd: Int) {
        var alignment = arrowAlignment
        if (alignment != Gravity.START && alignment != Gravity.CENTER_HORIZONTAL && alignment != Gravity.END) {
            alignment = Gravity.START
        }
        val layoutParams = arrowView.layoutParams as LayoutParams
        layoutParams.setMargins(marginStart, 0, marginEnd, 0)
        layoutParams.gravity = alignment
        arrowView.layoutParams = layoutParams
    }

    private fun expand() {
        val matchParentMeasureSpec = MeasureSpec.makeMeasureSpec((parent as View).width, MeasureSpec.EXACTLY)
        val wrapContentMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        measure(matchParentMeasureSpec, wrapContentMeasureSpec)
        val targetHeight = measuredHeight

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        layoutParams.height = 1
        visibility = VISIBLE
        val anim: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                if (interpolatedTime == 1f) {
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                } else {
                    layoutParams.height = (targetHeight * interpolatedTime).toInt()
                }
                requestLayout()
            }

            override fun willChangeBounds() = true
        }

        // Expansion speed of 1dp/ms
        anim.duration = (targetHeight / context.resources.displayMetrics.density).roundToLong()
        startAnimation(anim)
    }

    fun show() {
        if (shouldAnimate) {
            expand()
        } else {
            visibility = VISIBLE
        }
    }

    private fun collapse() {
        val initialHeight = measuredHeight
        val anim: Animation = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                if (interpolatedTime == 1f) {
                    visibility = GONE
                } else {
                    layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                    requestLayout()
                }
            }

            override fun willChangeBounds() = true
        }

        // Collapse speed of 1dp/ms
        anim.duration = (initialHeight / context.resources.displayMetrics.density).roundToLong()
        startAnimation(anim)
    }

    fun loadToolTip() {
        val show = prefs.getBoolean(identifier, true)
        if (!show) return
        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                val handler = rootView.handler ?: return true
                handler.postDelayed({ show() }, 500)
                viewTreeObserver.removeOnPreDrawListener(this)
                return true
            }
        })
    }

    private fun hide() {
        if (shouldAnimate) {
            collapse()
        } else {
            visibility = GONE
        }
    }

    init {
        inflate(context, R.layout.tip_box, this)
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.TipBox)
        identifier = attributes.getString(R.styleable.TipBox_identifier)
        if (identifier == null) {
            throw InflateException("Tip box identifier not set!!!")
        }
        shouldAnimate = attributes.getBoolean(R.styleable.TipBox_animate, true)
        tipMessageView = findViewById(R.id.tipMessage)
        val message = attributes.getString(R.styleable.TipBox_message)
        if (message != null) {
            tipMessageView.text = context.getString(R.string.tip_message, message)
        }
        val marginStart = attributes.getDimensionPixelSize(R.styleable.TipBox_arrowMarginStart, 0)
        val marginEnd = attributes.getDimensionPixelSize(R.styleable.TipBox_arrowMarginEnd, 0)
        val arrowAlignment = attributes.getInt(R.styleable.TipBox_arrowAlignment, Gravity.START)
        val canDisplayImmediately = attributes.getBoolean(R.styleable.TipBox_shouldDisplayImmediately, false)
        val toolTipTextColor = attributes.getColor(R.styleable.TipBox_textColor, resources.getColor(R.color.md_black_1000))
        val toolTipBackgroundColor = attributes.getColor(R.styleable.TipBox_backgroundColor, resources.getColor(R.color.brand_light_blue))
        attributes.recycle()
        arrowView = findViewById(R.id.arrow)
        setArrowAlignment(arrowAlignment, marginStart, marginEnd)
        visibility = GONE
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext())
        findViewById<View>(R.id.gotItBtn).setOnClickListener {
            hide()
            prefs.edit().putBoolean(identifier, false).apply()
        }
        tipMessageView.setTextColor(toolTipTextColor)
        findViewById<View>(R.id.tipBoxContainer).setBackgroundColor(toolTipBackgroundColor)
        arrowView.setColorFilter(toolTipBackgroundColor)
        if (canDisplayImmediately) {
            loadToolTip()
        }
    }

}