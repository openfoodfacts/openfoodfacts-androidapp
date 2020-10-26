/*
 * Copyright 2016-2020 Open Food Facts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package openfoodfacts.github.scrachx.openfood.utils;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

public class SwipeDetector implements View.OnTouchListener {
    public static final String LOG_TAG = "SwipeDetector";
    public static final String ERR_NO_LISTENER_MSG = "please pass SwipeDetector.onSwipeEvent Interface instance";
    private int minDistance = 100;
    private float downX, downY, upX, upY;
    private final OnSwipeEventListener swipeEventListener;
    private final View view;

    public SwipeDetector(@NonNull View view, OnSwipeEventListener listener) {
        this.view = view;
        this.swipeEventListener = listener;
        view.setOnTouchListener(this);
    }

    public void onRightToLeftSwipe() {
        if (swipeEventListener != null) {
            swipeEventListener.onSwipeEventDetected(view, SwipeTypeEnum.RIGHT_TO_LEFT);
        } else {
            Log.e(LOG_TAG, ERR_NO_LISTENER_MSG);
        }
    }

    public void onLeftToRightSwipe() {
        if (swipeEventListener != null) {
            swipeEventListener.onSwipeEventDetected(view, SwipeTypeEnum.LEFT_TO_RIGHT);
        } else {
            Log.e(LOG_TAG, ERR_NO_LISTENER_MSG);
        }
    }

    public void onTopToBottomSwipe() {
        if (swipeEventListener != null) {
            swipeEventListener.onSwipeEventDetected(view, SwipeTypeEnum.TOP_TO_BOTTOM);
        } else {
            Log.e(LOG_TAG, ERR_NO_LISTENER_MSG);
        }
    }

    public void onBottomToTopSwipe() {
        if (swipeEventListener != null) {
            swipeEventListener.onSwipeEventDetected(view, SwipeTypeEnum.BOTTOM_TO_TOP);
        } else {
            Log.e(LOG_TAG, ERR_NO_LISTENER_MSG);
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