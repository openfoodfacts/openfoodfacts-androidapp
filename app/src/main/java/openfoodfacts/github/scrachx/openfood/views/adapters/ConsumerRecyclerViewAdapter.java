package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import holloway.allergenChecker.Consumer;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.ConsumerFragment.OnListFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} for {@link Consumer} objects.
 */
public class ConsumerRecyclerViewAdapter extends RecyclerView.Adapter<ConsumerRecyclerViewAdapter.ViewHolder> {

    private final List<Consumer> mValues;
    private final OnListFragmentInteractionListener mListener;


    public ConsumerRecyclerViewAdapter(List<Consumer> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;


        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_consumer, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConsumerRecyclerViewAdapter.ViewHolder holder, int position) {
        try {
            Consumer consumer = mValues.get(position);

            TextView textView = holder.mContentView;
            textView.setText(consumer.getName());


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
        public final View mView;
        public final TextView mContentView;
        public Consumer mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = view.findViewById(R.id.content);

            view.setOnClickListener(v -> {
                //TODO Set action for clicking on Consumer
            });
        }

        public void bindView(int position) {
            mContentView.setText(mValues.get(position).getName());
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
