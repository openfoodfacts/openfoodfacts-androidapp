package openfoodfacts.github.scrachx.openfood.features.shared.views

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.view.*
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.getStringOrThrow
import androidx.preference.PreferenceManager
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.TipBoxBinding
import kotlin.math.roundToLong

class TipBox(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val prefs: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(getContext()) }

    private val binding = TipBoxBinding.inflate(LayoutInflater.from(context), this, false)
    private val shouldAnimate: Boolean
    private val identifier: String

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.TipBox)
        identifier = attributes.getStringOrThrow(R.styleable.TipBox_identifier)
        shouldAnimate = attributes.getBoolean(R.styleable.TipBox_animate, true)
        val message = attributes.getStringOrThrow(R.styleable.TipBox_message)
        val marginStart = attributes.getDimensionPixelSize(R.styleable.TipBox_arrowMarginStart, 0)
        val marginEnd = attributes.getDimensionPixelSize(R.styleable.TipBox_arrowMarginEnd, 0)
        val arrowAlignment = attributes.getInt(R.styleable.TipBox_arrowAlignment, Gravity.START)
        val canDisplayImmediately = attributes.getBoolean(R.styleable.TipBox_shouldDisplayImmediately, false)
        val toolTipTextColor = attributes.getColor(R.styleable.TipBox_textColor, ResourcesCompat.getColor(resources, R.color.md_black_1000, context.theme))
        val toolTipBackgroundColor = attributes.getColor(R.styleable.TipBox_backgroundColor, resources.getColor(R.color.brand_light_blue))
        attributes.recycle()

        binding.tipMessage.setTextColor(toolTipTextColor)
        binding.tipMessage.text = context.getString(R.string.tip_message, message)

        binding.arrow.setColorFilter(toolTipBackgroundColor)
        setArrowAlignment(arrowAlignment, marginStart, marginEnd)

        visibility = GONE

        binding.gotItBtn.setOnClickListener {
            hide()
            prefs.edit { putBoolean(identifier, false) }
        }

        findViewById<View>(R.id.tipBoxContainer).setBackgroundColor(toolTipBackgroundColor)

        if (canDisplayImmediately) loadToolTip()
    }


    fun setTipMessage(message: CharSequence?) {
        binding.tipMessage.text = message
    }

    fun setArrowAlignment(arrowAlignment: Int, marginStart: Int, marginEnd: Int) {
        var alignment = arrowAlignment
        if (alignment != Gravity.START && alignment != Gravity.CENTER_HORIZONTAL && alignment != Gravity.END) {
            alignment = Gravity.START
        }
        val layoutParams = binding.arrow.layoutParams as LayoutParams
        layoutParams.setMargins(marginStart, 0, marginEnd, 0)
        layoutParams.gravity = alignment
        binding.arrow.layoutParams = layoutParams
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
}