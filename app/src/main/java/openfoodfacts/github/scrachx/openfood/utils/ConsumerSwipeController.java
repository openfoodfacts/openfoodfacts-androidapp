package openfoodfacts.github.scrachx.openfood.utils;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;


/**
 * @author <a href="https://github.com/ross-holloway94"> Ross Holloway </a>
 * @version 19 June 2018
 */
public class ConsumerSwipeController extends ItemTouchHelper.Callback {
    /**
     * @inheritDoc
     */
    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
        //Do nothing
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        //TODO Set actions for left and right
    }
}
