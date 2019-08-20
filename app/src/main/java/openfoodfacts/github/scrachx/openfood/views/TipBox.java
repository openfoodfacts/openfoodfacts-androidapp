package openfoodfacts.github.scrachx.openfood.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
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
    private String identifier;
    private boolean animate;
    private SharedPreferences prefs;
    private TextView tipMessage;
    private ImageView arrow;

    public TipBox(Context context, @Nullable AttributeSet attrs) throws Exception {
        super(context, attrs);
        inflate(context, R.layout.tip_box, this);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.TipBox);
        identifier = attributes.getString(R.styleable.TipBox_identifier);
        if (identifier == null) {
            throw new Exception("Tip box identifier not set!!!");
        }
        animate = attributes.getBoolean(R.styleable.TipBox_animate, true);
        String message = attributes.getString(R.styleable.TipBox_message);
        if (message != null) {
            tipMessage.setText(message);
        }
        int marginStart = attributes.getDimensionPixelSize(R.styleable.TipBox_arrowMarginStart, 0);
        arrow = findViewById(R.id.arrow);
        setArrowMarginStart(marginStart);

        tipMessage = findViewById(R.id.tipMessage);
        attributes.recycle();

        // gone by default
        setVisibility(View.GONE);
        findViewById(R.id.gotItBtn).setOnClickListener(v -> {
            if (animate) {
                collapse();
            } else {
                setVisibility(View.GONE);
            }

            prefs.edit().putBoolean(identifier, false).apply();
        });

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean show = prefs.getBoolean(identifier, true);
        if (!show) {
            return;
        }

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    if (animate) {
                        expand();
                    } else {
                        setVisibility(View.VISIBLE);
                    }
                }, 500);

                getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        });
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

    public void setArrowMarginStart(int marginStart) {
        LinearLayout.LayoutParams layoutParams = (LayoutParams) arrow.getLayoutParams();
        layoutParams.setMargins(marginStart, 0, 0, 0);
        arrow.setLayoutParams(layoutParams);
    }

    public void expand() {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        getLayoutParams().height = 1;
        setVisibility(View.VISIBLE);
        Animation a = new Animation() {
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
        a.setDuration((int) (targetHeight / getContext().getResources().getDisplayMetrics().density));
        startAnimation(a);
    }

    public void collapse() {
        final int initialHeight = getMeasuredHeight();

        Animation a = new Animation() {
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
        a.setDuration((int) (initialHeight / getContext().getResources().getDisplayMetrics().density));
        startAnimation(a);
    }
}
