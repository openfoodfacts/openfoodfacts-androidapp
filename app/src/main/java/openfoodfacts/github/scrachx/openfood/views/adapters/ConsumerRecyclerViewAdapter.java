package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import holloway.allergenChecker.Consumer;
import holloway.allergenChecker.JSONManager;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.ConsumerFragment.OnListFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} for {@link Consumer} objects.
 */
public class ConsumerRecyclerViewAdapter extends RecyclerView.Adapter<ConsumerRecyclerViewAdapter.ViewHolder> {

    private List<Consumer> mValues;
    private OnListFragmentInteractionListener mListener;
    private JSONManager jsonManager = JSONManager.getInstance();


    public ConsumerRecyclerViewAdapter(@NonNull HashSet<Consumer> items, OnListFragmentInteractionListener listener) {
        mValues = new ArrayList<>();
        if (!items.isEmpty()) {
            mValues.addAll(items);
        }
        mListener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_consumer_list, parent, false);
        final ViewHolder mViewHolder = new ViewHolder(mView);
        mView.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when a view has been clicked.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                mListener.onConsumerItemTouch(v, mViewHolder.getAdapterPosition());
            }
        });

        return new ViewHolder(mView);
    }

    @Override
    public void onBindViewHolder(@NonNull ConsumerRecyclerViewAdapter.ViewHolder holder, int position) {
        try {
            Consumer consumer = mValues.get(position);

            TextView textView = holder.mContentView;
            //ref: https://stackoverflow.com/questions/5725892/how-to-capitalize-the-first-letter-of-word-in-a-string-using-java
            String capitalizedName = consumer.getName().substring(0, 1).toUpperCase() + consumer.getName().substring(1).toLowerCase();
            textView.setText(capitalizedName);


        } catch (Exception e) {
            Log.e("ConsumerRecyclerViewAda", "onBindViewHolder: Error binding view holder");
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mContentView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = view.findViewById(R.id.content);
        }

        public void bindView(int position) {
            mContentView.setText(mValues.get(position).getName());
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }

    /**
     * Removes the consumer from the <code>RecyclerView</code> and deletes the <code>Consumer</code>
     * file. Called when the user taps the delete button, shown by swiping.
     *
     * @param position The position of the <code>Consumer</code> in the <code>RecyclerView</code>
     */
    public void removeAt(int position) {
        Consumer consumer = mValues.get(position);
        System.out.println(consumer.getLinkedJSON().toString());
        if (jsonManager.deleteConsumer(consumer)) {
            if (mValues.remove(consumer)) {
                Log.d("ConsumerAdapter", "removeAt: consumer removed from adapter list");
            }
            notifyItemRemoved(position);
            Log.d("ConsumerAdapter", "removeAt: deleted " + consumer.getName());
        } else {
            Log.w("ConsumerAdapter", "Failed to delete consumer JSON file.");
        }
    }
}
