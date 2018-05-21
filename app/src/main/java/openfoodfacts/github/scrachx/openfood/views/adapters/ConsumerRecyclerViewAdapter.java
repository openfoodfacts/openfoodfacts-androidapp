package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import holloway.allergenChecker.Consumer;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.dummy.DummyContent.DummyItem;
import openfoodfacts.github.scrachx.openfood.fragments.ConsumerFragment.OnListFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class ConsumerRecyclerViewAdapter extends RecyclerView.Adapter<ConsumerRecyclerViewAdapter.ViewHolder> {

    private final List<Consumer> mValues;
    private final OnListFragmentInteractionListener mListener;


    /* Footer view implemention from
     * https://stackoverflow.com/questions/26448717/android-5-0-add-header-footer-to-a-recyclerview
     */
    private static final int FOOTER_VIEW = 1;

    // Define a view holder for Footer view

    public class FooterViewHolder extends ViewHolder {
        public FooterViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Do whatever you want on clicking the item
                }
            });
        }
    }

    // Now define the viewholder for Normal list item
    public class NormalViewHolder extends ViewHolder {
        public NormalViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //TODO Do whatever you want on clicking the normal items
                }
            });
        }
    }



    public ConsumerRecyclerViewAdapter(List<Consumer> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        if (viewType == FOOTER_VIEW) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.consumer_footer_layout, parent, false);

            FooterViewHolder vh = new FooterViewHolder(view);

            return vh;
        }

        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_consumer, parent, false);

        return new NormalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
 /*       holder.mItem = mValues.get(position);
        holder.mContentView.setText(mValues.get(position).getName());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });*/

        try {
            if (holder instanceof NormalViewHolder) {
                NormalViewHolder vh = (NormalViewHolder) holder;

                vh.bindView(position);
            } else if (holder instanceof FooterViewHolder) {
                FooterViewHolder vh = (FooterViewHolder) holder;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if (mValues == null) {
            return 0;
        }

        if (mValues.size() == 0)
        {
            //Return 1 to represent no items
            return 1;
        }

        return mValues.size() + 1;
    }

/**
 * {@inheritDoc}
 */
    @Override
    public int getItemViewType(int position) {
        if (position == mValues.size()) {
            // This is where we'll add footer.
            return FOOTER_VIEW;
        }

        return super.getItemViewType(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;
        public Consumer mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        public void bindView(int position) {
            
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
