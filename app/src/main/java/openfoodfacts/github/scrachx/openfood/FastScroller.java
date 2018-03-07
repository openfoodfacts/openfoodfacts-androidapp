package openfoodfacts.github.scrachx.openfood;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import io.reactivex.annotations.NonNull;

/**
 * Created by Manav on 2/28/2018.
 */

public class FastScroller extends LinearLayout {


    private static final int HANDLE_HIDE_DELAY = 1000;
    private static final int HANDLE_ANIMATION_DURATION = 100;
    private static final int TRACK_SNAP_RANGE = 5;
    private static final String SCALE_X = "scaleX";
    private static final String SCALE_Y = "scaleY";
    private static final String ALPHA = "alpha";
    private final HandleHider handleHider = new HandleHider();
    private final ScrollListener scrollListener = new ScrollListener();
    private View bubble;
    private View handle;
    private RecyclerView recyclerView;
    private int height;

    private AnimatorSet currentAnimator = null;

    public FastScroller(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise(context);
    }

    public FastScroller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise(context);
    }

    private void initialise(Context context) {
        setOrientation(HORIZONTAL);
        setClipChildren(false);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.fastscroller, this);
        bubble = findViewById(R.id.fastscroller_bubble);
        handle = findViewById(R.id.fastscroller_handle);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = h;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            setPosition(event.getY());
            if (currentAnimator != null) {
                currentAnimator.cancel();
            }
            getHandler().removeCallbacks(handleHider);
           /* if (handle.getVisibility() == INVISIBLE) {
                showHandle();
            }*/
            setRecyclerViewPosition(event.getY());
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            getHandler().postDelayed(handleHider, HANDLE_HIDE_DELAY);
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        recyclerView.addOnScrollListener(scrollListener);
    }

    private void setRecyclerViewPosition(float y) {
        if (recyclerView != null) {
            int itemCount = recyclerView.getAdapter().getItemCount();
            float proportion;
            if (bubble.getY() == 0) {
                proportion = 0f;
            } else if (bubble.getY() + bubble.getHeight() >= height - TRACK_SNAP_RANGE) {
                proportion = 1f;
            } else {
                proportion = y / (float) height;
            }
            int targetPos = getValueInRange(0, itemCount - 1, (int) (proportion * (float) itemCount));
            recyclerView.scrollToPosition(targetPos);
        }
    }

    private int getValueInRange(int min, int max, int value) {
        int minimum = Math.max(min, value);
        return Math.min(minimum, max);
    }

    private void setPosition(float y) {
        float position = y / height;
        int bubbleHeight = bubble.getHeight();
        bubble.setY(getValueInRange(0, height - bubbleHeight, (int) ((height - bubbleHeight) * position)));
        int handleHeight = handle.getHeight();
        handle.setY(getValueInRange(0, height - handleHeight, (int) ((height - handleHeight) * position)));
    }

    private void showHandle() {
        AnimatorSet animatorSet = new AnimatorSet();
        handle.setPivotX(handle.getWidth());
        handle.setPivotY(handle.getHeight());
        handle.setVisibility(VISIBLE);
        Animator growerX = ObjectAnimator.ofFloat(handle, SCALE_X, 0f, 1f).setDuration(HANDLE_ANIMATION_DURATION);
        Animator growerY = ObjectAnimator.ofFloat(handle, SCALE_Y, 0f, 1f).setDuration(HANDLE_ANIMATION_DURATION);
        Animator alpha = ObjectAnimator.ofFloat(handle, ALPHA, 0f, 1f).setDuration(HANDLE_ANIMATION_DURATION);
        animatorSet.playTogether(growerX, growerY, alpha);
        animatorSet.start();
    }

    private void hideHandle() {
        currentAnimator = new AnimatorSet();
        handle.setPivotX(handle.getWidth());
        handle.setPivotY(handle.getHeight());
        Animator shrinkerX = ObjectAnimator.ofFloat(handle, SCALE_X, 1f, 0f).setDuration(HANDLE_ANIMATION_DURATION);
        Animator shrinkerY = ObjectAnimator.ofFloat(handle, SCALE_Y, 1f, 0f).setDuration(HANDLE_ANIMATION_DURATION);
        Animator alpha = ObjectAnimator.ofFloat(handle, ALPHA, 1f, 0f).setDuration(HANDLE_ANIMATION_DURATION);
        currentAnimator.playTogether(shrinkerX, shrinkerY, alpha);
        currentAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                handle.setVisibility(INVISIBLE);
                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                handle.setVisibility(INVISIBLE);
                currentAnimator = null;
            }
        });
        currentAnimator.start();
    }

    private class HandleHider implements Runnable {
        @Override
        public void run() {
            hideHandle();
        }
    }

    private class ScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrolled(RecyclerView rv, int dx, int dy) {
            View firstVisibleView = recyclerView.getChildAt(0);
            int firstVisiblePosition = recyclerView.getChildAdapterPosition(firstVisibleView);
            int visibleRange = recyclerView.getChildCount();
            int lastVisiblePosition = firstVisiblePosition + visibleRange;
            int itemCount = recyclerView.getAdapter().getItemCount();
            int position;
            if (firstVisiblePosition == 0) {
                position = 0;
            } else if (lastVisiblePosition == itemCount - 1) {
                position = itemCount - 1;
            } else {
                position = firstVisiblePosition;
            }
            float proportion = (float) position / (float) itemCount;
            setPosition(height * proportion);
        }
    }
}
