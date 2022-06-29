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
package openfoodfacts.github.scrachx.openfood.utils

import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.View.OnTouchListener
import kotlin.math.abs

class SwipeDetector(private val view: View, private val swipeEventListener: OnSwipeEventListener?) : OnTouchListener {
    private var minDistance = 100
    private var downX = 0f
    private var downY = 0f
    private var upX = 0f
    private var upY = 0f

    private fun onRightToLeftSwipe() {
        swipeEventListener?.onSwipeEventDetected(view, SwipeTypeEnum.RIGHT_TO_LEFT)
            ?: Log.e(LOG_TAG, ERR_NO_LISTENER_MSG)
    }

    private fun onLeftToRightSwipe() {
        swipeEventListener?.onSwipeEventDetected(view, SwipeTypeEnum.LEFT_TO_RIGHT)
            ?: Log.e(LOG_TAG, ERR_NO_LISTENER_MSG)
    }

    private fun onTopToBottomSwipe() {
        swipeEventListener?.onSwipeEventDetected(view, SwipeTypeEnum.TOP_TO_BOTTOM)
            ?: Log.e(LOG_TAG, ERR_NO_LISTENER_MSG)
    }

    private fun onBottomToTopSwipe() {
        swipeEventListener?.onSwipeEventDetected(view, SwipeTypeEnum.BOTTOM_TO_TOP)
            ?: Log.e(LOG_TAG, ERR_NO_LISTENER_MSG)
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                return true
            }
            ACTION_UP -> {
                upX = event.x
                upY = event.y
                val deltaX = downX - upX
                val deltaY = downY - upY

                //HORIZONTAL SCROLL
                if (abs(deltaX) > abs(deltaY)) {
                    if (abs(deltaX) > minDistance) {
                        // left or right
                        if (deltaX < 0) {
                            onLeftToRightSwipe()
                            return true
                        }
                        if (deltaX > 0) {
                            onRightToLeftSwipe()
                            return true
                        }
                    } else {
                        //not long enough swipe...
                        return false
                    }
                } else {
                    if (abs(deltaY) > minDistance) {
                        // top or down
                        if (deltaY < 0) {
                            onTopToBottomSwipe()
                            return true
                        }
                        if (deltaY > 0) {
                            onBottomToTopSwipe()
                            return true
                        }
                    } else {
                        //not long enough swipe...
                        return false
                    }
                }
                return true
            }
        }
        return false
    }

    fun setMinDistanceInPixels(minDistance: Int) = apply { this.minDistance = minDistance }

    enum class SwipeTypeEnum {
        RIGHT_TO_LEFT, LEFT_TO_RIGHT, TOP_TO_BOTTOM, BOTTOM_TO_TOP
    }

    interface OnSwipeEventListener {
        fun onSwipeEventDetected(v: View?, swipeType: SwipeTypeEnum?)
    }

    companion object {
        const val LOG_TAG = "SwipeDetector"
        const val ERR_NO_LISTENER_MSG = "please pass SwipeDetector.onSwipeEvent Interface instance"
    }

    init {
        view.setOnTouchListener(this)
    }
}