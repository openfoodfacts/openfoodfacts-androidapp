package openfoodfacts.github.scrachx.openfood.listeners

import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener

/**
 * Implementing the [RecyclerView.OnItemTouchListener] to respond to only single tap events
 * @author aurélien leboulanger
 */
class RecyclerItemClickListener(
        context: Context,
        private val onItemClick: (view: View, position: Int) -> Unit
) : OnItemTouchListener {
    private val mGestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent) = true
    })

    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {
        val childView = view.findChildViewUnder(e.x, e.y)
        if (childView != null && mGestureDetector.onTouchEvent(e)) {
            onItemClick(childView, view.getChildAdapterPosition(childView))
        }
        return false
    }

    override fun onTouchEvent(view: RecyclerView, motionEvent: MotionEvent) = Unit
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) = Unit
}