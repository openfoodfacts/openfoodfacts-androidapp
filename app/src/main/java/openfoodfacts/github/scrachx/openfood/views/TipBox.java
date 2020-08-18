package openfoodfacts.github.scrachx.openfood.views;

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

public class TipBox extends LinearLayout {
    public static final int ALIGN_START = Gravity.START;
    public static final int ALIGN_CENTER = Gravity.CENTER_HORIZONTAL;
    public static final int ALIGN_END = Gravity.END;
    private boolean animate;
    private ImageView arrow;
    private String identifier;
    private SharedPreferences prefs;
    private TextView tipMessage;

    public TipBox(Context context, @Nullable AttributeSet attrs) throws Exception {
        super(context, attrs);
        inflate(context, R.layout.tip_box, this);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.TipBox);
        identifier = attributes.getString(R.styleable.TipBox_identifier);
        if (identifier == null) {
            throw new InflateException("Tip box identifier not set!!!");
        }
        animate = attributes.getBoolean(R.styleable.TipBox_animate, true);
        tipMessage = findViewById(R.id.tipMessage);
        String message = attributes.getString(R.styleable.TipBox_message);
        if (message != null) {
            tipMessage.setText(context.getString(R.string.tip_message, message));
        }
        arrow = findViewById(R.id.arrow);
        int marginStart = attributes.getDimensionPixelSize(R.styleable.TipBox_arrowMarginStart, 0);
        int marginEnd = attributes.getDimensionPixelSize(R.styleable.TipBox_arrowMarginEnd, 0);
        int arrowAlignment = attributes.getInt(R.styleable.TipBox_arrowAlignment, Gravity.START);
        boolean canDisplayImmediately = attributes.getBoolean(R.styleable.TipBox_shouldDisplayImmediately, false);
        setArrowAlignment(arrowAlignment, marginStart, marginEnd);
        int toolTipTextColor = attributes.getColor(R.styleable.TipBox_textColor, getResources().getColor(R.color.md_black_1000));
        int toolTipBackgroundColor = attributes.getColor(R.styleable.TipBox_backgroundColor,
            getResources().getColor(R.color.brand_light_blue));
        attributes.recycle();

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

    public void setAnimate(boolean animate) {
        this.animate = animate;
    }

    public void setTipMessage(CharSequence message) {
        tipMessage.setText(message);
    }

    public void setArrowAlignment(int arrowAlignment, int marginStart, int marginEnd) {
        if (arrowAlignment != ALIGN_START && arrowAlignment != ALIGN_CENTER && arrowAlignment != ALIGN_END) {
            arrowAlignment = ALIGN_START;
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
                getLayoutParams().height = interpolatedTime == 1
                    ? LayoutParams.WRAP_CONTENT
                    : (int) (targetHeight * interpolatedTime);
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
        if (animate) {
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
        if (animate) {
            collapse();
        } else {
            setVisibility(View.GONE);
        }
    }
}
