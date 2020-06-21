package openfoodfacts.github.scrachx.openfood.utils;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class SwipeDetector implements View.OnTouchListener {
    private int minDistance = 100;
    private float downX, downY, upX, upY;
    private final View v;
    private OnSwipeEventListener swipeEventListener;

    public SwipeDetector(View v) {
        this.v = v;
        v.setOnTouchListener(this);
    }

    public void setOnSwipeListener(OnSwipeEventListener listener) {
        try {
            swipeEventListener = listener;
        } catch (ClassCastException e) {
            Log.e("ClassCastException", "please pass SwipeDetector.onSwipeEvent Interface instance", e);
        }
    }

    public void onRightToLeftSwipe() {
        if (swipeEventListener != null) {
            swipeEventListener.onSwipeEventDetected(v, SwipeTypeEnum.RIGHT_TO_LEFT);
        } else {
            Log.e("SwipeDetector error", "please pass SwipeDetector.onSwipeEvent Interface instance");
        }
    }

    public void onLeftToRightSwipe() {
        if (swipeEventListener != null) {
            swipeEventListener.onSwipeEventDetected(v, SwipeTypeEnum.LEFT_TO_RIGHT);
        } else {
            Log.e("SwipeDetector error", "please pass SwipeDetector.onSwipeEvent Interface instance");
        }
    }

    public void onTopToBottomSwipe() {
        if (swipeEventListener != null) {
            swipeEventListener.onSwipeEventDetected(v, SwipeTypeEnum.TOP_TO_BOTTOM);
        } else {
            Log.e("SwipeDetector error", "please pass SwipeDetector.onSwipeEvent Interface instance");
        }
    }

    public void onBottomToTopSwipe() {
        if (swipeEventListener != null) {
            swipeEventListener.onSwipeEventDetected(v, SwipeTypeEnum.BOTTOM_TO_TOP);
        } else {
            Log.e("SwipeDetector error", "please pass SwipeDetector.onSwipeEvent Interface instance");
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                downX = event.getX();
                downY = event.getY();
                return true;
            }
            case MotionEvent.ACTION_UP: {
                upX = event.getX();
                upY = event.getY();

                float deltaX = downX - upX;
                float deltaY = downY - upY;

                //HORIZONTAL SCROLL
                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    if (Math.abs(deltaX) > minDistance) {
                        // left or right
                        if (deltaX < 0) {
                            this.onLeftToRightSwipe();
                            return true;
                        }
                        if (deltaX > 0) {
                            this.onRightToLeftSwipe();
                            return true;
                        }
                    } else {
                        //not long enough swipe...
                        return false;
                    }
                }
                //VERTICAL SCROLL
                else {
                    if (Math.abs(deltaY) > minDistance) {
                        // top or down
                        if (deltaY < 0) {
                            this.onTopToBottomSwipe();
                            return true;
                        }
                        if (deltaY > 0) {
                            this.onBottomToTopSwipe();
                            return true;
                        }
                    } else {
                        //not long enough swipe...
                        return false;
                    }
                }

                return true;
            }
        }
        return false;
    }

    public SwipeDetector setMinDistanceInPixels(int minDistance) {
        this.minDistance = minDistance;
        return this;
    }

    public enum SwipeTypeEnum {
        RIGHT_TO_LEFT, LEFT_TO_RIGHT, TOP_TO_BOTTOM, BOTTOM_TO_TOP
    }

    public interface OnSwipeEventListener {
        void onSwipeEventDetected(View v, SwipeTypeEnum swipeType);
    }
}