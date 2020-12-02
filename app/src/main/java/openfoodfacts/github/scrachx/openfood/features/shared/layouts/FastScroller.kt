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
package openfoodfacts.github.scrachx.openfood.features.shared.layouts

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.R

/**
 * Created by Manav on 2/28/2018.
 */
class FastScroller : LinearLayout {
    private val handleHider = HandleHider()
    private val scrollListener: ScrollListener = ScrollListener()
    private var bubble: View? = null
    private var handle: View? = null
    private var recyclerView: RecyclerView? = null
    private var currentHeight = 0
    private var currentAnimator: AnimatorSet? = null

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialise(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialise(context)
    }

    private fun initialise(context: Context) {
        orientation = HORIZONTAL
        clipChildren = false
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.fastscroller, this)
        bubble = findViewById(R.id.fastscroller_bubble)
        handle = findViewById(R.id.fastscroller_handle)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        currentHeight = h
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            setPosition(event.y)
            if (currentAnimator != null) {
                currentAnimator!!.cancel()
            }
            handler.removeCallbacks(handleHider)
            setRecyclerViewPosition(event.y)
            return true
        } else if (event.action == MotionEvent.ACTION_UP) {
            handler.postDelayed(handleHider, HANDLE_HIDE_DELAY.toLong())
            return true
        }
        return super.onTouchEvent(event)
    }

    fun setRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        recyclerView.addOnScrollListener(scrollListener)
    }

    private fun setRecyclerViewPosition(y: Float) {
        if (recyclerView != null) {
            val itemCount = recyclerView!!.adapter!!.itemCount
            val proportion = when {
                bubble!!.y == 0f -> {
                    0f
                }
                bubble!!.y + bubble!!.height >= currentHeight - TRACK_SNAP_RANGE -> {
                    1f
                }
                else -> {
                    y / currentHeight.toFloat()
                }
            }
            val targetPos = getValueInRange(0, itemCount - 1, (proportion * itemCount.toFloat()).toInt())
            recyclerView!!.scrollToPosition(targetPos)
        }
    }

    private fun getValueInRange(min: Int, max: Int, value: Int): Int {
        val minimum = min.coerceAtLeast(value)
        return minimum.coerceAtMost(max)
    }

    private fun setPosition(y: Float) {
        val position = y / currentHeight
        val bubbleHeight = bubble!!.height
        bubble!!.y = getValueInRange(0, currentHeight - bubbleHeight, ((currentHeight - bubbleHeight) * position).toInt()).toFloat()
        val handleHeight = handle!!.height
        handle!!.y = getValueInRange(0, currentHeight - handleHeight, ((currentHeight - handleHeight) * position).toInt()).toFloat()
    }

    private fun hideHandle() {
        currentAnimator = AnimatorSet()
        handle!!.pivotX = handle!!.width.toFloat()
        handle!!.pivotY = handle!!.height.toFloat()
        val shrinkerX: Animator = ObjectAnimator.ofFloat(handle, SCALE_X, 1f, 0f).setDuration(HANDLE_ANIMATION_DURATION.toLong())
        val shrinkerY: Animator = ObjectAnimator.ofFloat(handle, SCALE_Y, 1f, 0f).setDuration(HANDLE_ANIMATION_DURATION.toLong())
        val alpha: Animator = ObjectAnimator.ofFloat(handle, ALPHA, 1f, 0f).setDuration(HANDLE_ANIMATION_DURATION.toLong())
        currentAnimator!!.playTogether(shrinkerX, shrinkerY, alpha)
        currentAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                handle!!.visibility = INVISIBLE
                currentAnimator = null
            }

            override fun onAnimationCancel(animation: Animator) {
                super.onAnimationCancel(animation)
                handle!!.visibility = INVISIBLE
                currentAnimator = null
            }
        })
        currentAnimator!!.start()
    }

    private inner class HandleHider : Runnable {
        override fun run() {
            hideHandle()
        }
    }

    private inner class ScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            val firstVisibleView = recyclerView!!.getChildAt(0)
            val firstVisiblePosition = recyclerView!!.getChildAdapterPosition(firstVisibleView)
            val visibleRange = recyclerView!!.childCount
            val lastVisiblePosition = firstVisiblePosition + visibleRange
            val itemCount = recyclerView!!.adapter!!.itemCount
            val position: Int
            position = when {
                firstVisiblePosition == 0 -> 0
                lastVisiblePosition == itemCount - 1 -> itemCount - 1
                else -> {
                    firstVisiblePosition
                }
            }
            val proportion = position.toFloat() / itemCount
            setPosition(currentHeight * proportion)
        }
    }

    companion object {
        private const val HANDLE_HIDE_DELAY = 1000
        private const val HANDLE_ANIMATION_DURATION = 100
        private const val TRACK_SNAP_RANGE = 5
        private const val SCALE_X = "scaleX"
        private const val SCALE_Y = "scaleY"
        private const val ALPHA = "alpha"
    }
}