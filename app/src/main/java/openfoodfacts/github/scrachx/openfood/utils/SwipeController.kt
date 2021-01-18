package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import openfoodfacts.github.scrachx.openfood.R

/**
 * Created by Mehrosh.Mehboob on 19-Feb-18.
 */
class SwipeController(
        context: Context,
        private val swipeController: Actions
) : ItemTouchHelper.Callback() {
    private val backgroundColor: Int = ResourcesCompat.getColor(context.resources, R.color.material_red, context.theme)
    private val deleteDrawable: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_delete_white_24dp)!!
    private val mBackground = ColorDrawable()

    private val mClearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        swipeController.onRightClicked(position)
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) =
            makeMovementFlags(0, ItemTouchHelper.LEFT)

    override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            viewHolder1: RecyclerView.ViewHolder
    ) = false

    override fun onChildDraw(
            canvas: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val itemView = viewHolder.itemView
        val itemHeight = itemView.height
        val isCancelled = dX == 0f && !isCurrentlyActive
        if (isCancelled) {
            clearCanvas(canvas, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
            super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }
        mBackground.color = backgroundColor
        mBackground.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
        mBackground.draw(canvas)
        val deleteIconTop = itemView.top + (itemHeight - deleteDrawable.intrinsicHeight) / 2
        val deleteIconMargin = (itemHeight - deleteDrawable.intrinsicHeight) / 2
        val deleteIconLeft = itemView.right - deleteIconMargin - deleteDrawable.intrinsicWidth
        val deleteIconRight = itemView.right - deleteIconMargin
        val deleteIconBottom = deleteIconTop + deleteDrawable.intrinsicHeight
        deleteDrawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
        deleteDrawable.draw(canvas)
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun clearCanvas(c: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        c.drawRect(left, top, right, bottom, mClearPaint)
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = 0.7f


    /**
     * Created by Mehrosh.Mehboob on 20-Feb-18.
     */
    fun interface Actions {
        fun onRightClicked(position: Int)
    }
}
