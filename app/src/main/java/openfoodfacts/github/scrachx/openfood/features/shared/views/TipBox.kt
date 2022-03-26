package openfoodfacts.github.scrachx.openfood.features.shared.views

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.AttributeSet
import android.view.*
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.getStringOrThrow
import androidx.preference.PreferenceManager
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.databinding.TipBoxBinding
import kotlin.math.roundToLong

class TipBox(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val prefs: SharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(getContext()) }

    private val binding = TipBoxBinding.inflate(LayoutInflater.from(context), this, true)
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

        val toolTipTextColor = attributes.getColor(
            R.styleable.TipBox_textColor,
            ResourcesCompat.getColor(resources, R.color.md_black_1000, context.theme)
        )

        val toolTipBackgroundColor = attributes.getColor(
            R.styleable.TipBox_backgroundColor,
            ResourcesCompat.getColor(resources, R.color.brand_light_blue, context.theme)
        )
        attributes.recycle()

        // if identifier != "showEcoScorePrompt" , set values to button Tip Box
        // else. set value to icon tip box
        if (identifier != "showEcoScorePrompt") {
            binding.tipBoxIcon.visibility = View.GONE
            binding.tipMessageButton.setTextColor(toolTipTextColor)
            binding.tipMessageButton.text = context.getString(R.string.tip_message, message)
            binding.tipBoxContainer.setBackgroundColor(toolTipBackgroundColor)

            binding.arrow.setColorFilter(toolTipBackgroundColor)
            setArrowAlignment(arrowAlignment, marginStart, marginEnd)
        } else {
            binding.tipBoxButton.visibility = View.GONE
            binding.tipMessageIcon.setTextColor(toolTipTextColor)
            binding.tipMessageIcon.text = message
            binding.tipBoxIcon.setBackgroundColor(toolTipBackgroundColor)
        }

        visibility = GONE

        binding.gotItBtn.setOnClickListener {
            hide()
            prefs.edit { putBoolean(identifier, false) }
        }

        binding.closeBoxIcon.setOnClickListener {
            hide()
            prefs.edit { putBoolean(identifier, false) }
        }

        binding.mail.setOnClickListener {
            val contactIntent = Intent(Intent.ACTION_SENDTO)
            contactIntent.data = Uri.parse("mailto:contact@openfoodfacts.org")
            contactIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            try {
                context.startActivity(contactIntent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(getContext(), R.string.email_not_found, Toast.LENGTH_SHORT).show()
            }
        }


        if (canDisplayImmediately) loadToolTip()
    }


    fun setTipMessage(message: CharSequence?) {
        if (identifier != "showEcoScorePrompt")
            binding.tipMessageButton.text = context.getString(R.string.tip_message, message)
        else
            binding.tipMessageIcon.text = message
    }

    fun setArrowAlignment(arrowAlignment: Int, marginStart: Int, marginEnd: Int) {
        val alignment = when (arrowAlignment) {
            Gravity.START, Gravity.CENTER_HORIZONTAL, Gravity.END -> arrowAlignment
            else -> Gravity.START
        }

        binding.arrow.layoutParams = (binding.arrow.layoutParams as LayoutParams).apply {
            setMargins(marginStart, 0, marginEnd, 0)
            gravity = alignment
        }
    }

    private fun expand() {
        val matchParentMeasureSpec = MeasureSpec.makeMeasureSpec((parent as View).width, MeasureSpec.EXACTLY)
        val wrapContentMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        measure(matchParentMeasureSpec, wrapContentMeasureSpec)
        val targetHeight = measuredHeight

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        layoutParams.height = 1
        visibility = VISIBLE
        val anim = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                if (interpolatedTime == 1f) {
                    layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                } else {
                    layoutParams.height = (targetHeight * interpolatedTime).toInt()
                }
                requestLayout()
            }

            override fun willChangeBounds() = true
        }.apply {
            // Expansion speed of 1dp/ms
            duration = (targetHeight / context.resources.displayMetrics.density).roundToLong()
        }

        startAnimation(anim)
    }


    private fun collapse() {
        val initialHeight = measuredHeight
        val anim = object : Animation() {
            override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
                if (interpolatedTime == 1f) {
                    visibility = GONE
                } else {
                    layoutParams.height = initialHeight - (initialHeight * interpolatedTime).toInt()
                    requestLayout()
                }
            }

            override fun willChangeBounds() = true
        }.apply {
            // Collapse speed of 1dp/ms
            duration = (initialHeight / context.resources.displayMetrics.density).roundToLong()
        }

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

    fun show() = if (shouldAnimate) expand() else visibility = VISIBLE
    fun hide() = if (shouldAnimate) collapse() else visibility = GONE
}