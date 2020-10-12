package openfoodfacts.github.scrachx.openfood.features.shared.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.InflateException;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import openfoodfacts.github.scrachx.openfood.R;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class TipBox extends LinearLayout {
    private boolean shouldAnimate;
    private ImageView arrow;
    private String identifier;
    private SharedPreferences prefs;
    private TextView tipMessage;

    public TipBox(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.tip_box, this);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.TipBox);
        identifier = attributes.getString(R.styleable.TipBox_identifier);
        if (identifier == null) {
            throw new InflateException("Tip box identifier not set!!!");
        }
        shouldAnimate = attributes.getBoolean(R.styleable.TipBox_animate, true);
        tipMessage = findViewById(R.id.tipMessage);
        String message = attributes.getString(R.styleable.TipBox_message);
        if (message != null) {
            tipMessage.setText(context.getString(R.string.tip_message, message));
        }
        final int marginStart = attributes.getDimensionPixelSize(R.styleable.TipBox_arrowMarginStart, 0);
        final int marginEnd = attributes.getDimensionPixelSize(R.styleable.TipBox_arrowMarginEnd, 0);
        final int arrowAlignment = attributes.getInt(R.styleable.TipBox_arrowAlignment, Gravity.START);
        final boolean canDisplayImmediately = attributes.getBoolean(R.styleable.TipBox_shouldDisplayImmediately, false);
        final int toolTipTextColor = attributes.getColor(R.styleable.TipBox_textColor, getResources().getColor(R.color.md_black_1000));
        final int toolTipBackgroundColor = attributes.getColor(R.styleable.TipBox_backgroundColor, getResources().getColor(R.color.brand_light_blue));
        attributes.recycle();

        arrow = findViewById(R.id.arrow);
        setArrowAlignment(arrowAlignment, marginStart, marginEnd);
        // gone by default
        setVisibility(View.GONE);
        findViewById(R.id.gotItBtn).setOnClickListener(v -> {
            hide();
            prefs.edit().putBoolean(identifier, false).apply();
        });

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        //Tooltip should be shown the first time the actual content is available

        tipMessage.setTextColor(toolTipTextColor);
        findViewById(R.id.tipBoxContainer).setBackgroundColor(toolTipBackgroundColor);
        arrow.setColorFilter(toolTipBackgroundColor);
        if (canDisplayImmediately) {
            loadToolTip();
        }
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setShouldAnimate(boolean shouldAnimate) {
        this.shouldAnimate = shouldAnimate;
    }

    public void setTipMessage(CharSequence message) {
        tipMessage.setText(message);
    }

    public void setArrowAlignment(int arrowAlignment, int marginStart, int marginEnd) {
        if (arrowAlignment != Gravity.START && arrowAlignment != Gravity.CENTER_HORIZONTAL && arrowAlignment != Gravity.END) {
            arrowAlignment = Gravity.START;
        }

        LinearLayout.LayoutParams layoutParams = (LayoutParams) arrow.getLayoutParams();
        layoutParams.setMargins(marginStart, 0, marginEnd, 0);
        layoutParams.gravity = arrowAlignment;
        arrow.setLayoutParams(layoutParams);
    }

    private void expand() {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        getLayoutParams().height = 1;
        setVisibility(View.VISIBLE);
        Animation anim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    getLayoutParams().height = WRAP_CONTENT;
                } else {
                    getLayoutParams().height = (int) (targetHeight * interpolatedTime);
                }
                requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Expansion speed of 1dp/ms
        anim.setDuration((int) (targetHeight / getContext().getResources().getDisplayMetrics().density));
        startAnimation(anim);
    }

    public void show() {
        if (shouldAnimate) {
            expand();
        } else {
            setVisibility(View.VISIBLE);
        }
    }

    private void collapse() {
        final int initialHeight = getMeasuredHeight();

        Animation anim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    setVisibility(View.GONE);
                } else {
                    getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                    requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // Collapse speed of 1dp/ms
        anim.setDuration((int) (initialHeight / getContext().getResources().getDisplayMetrics().density));
        startAnimation(anim);
    }

    public void loadToolTip() {
        boolean show = prefs.getBoolean(identifier, true);
        if (!show) {
            return;
        }
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                Handler handler = new Handler();
                handler.postDelayed(TipBox.this::show, 500);

                getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
    }

    public void hide() {
        if (shouldAnimate) {
            collapse();
        } else {
            setVisibility(View.GONE);
        }
    }
}
