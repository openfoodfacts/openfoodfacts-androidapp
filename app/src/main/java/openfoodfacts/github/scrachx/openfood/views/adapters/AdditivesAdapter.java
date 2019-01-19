package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.AdditiveName;

/**
 * Created by prajwalm on 16/04/18.
 */

public class AdditivesAdapter extends RecyclerView.Adapter<AdditivesAdapter.ViewHolder> {

    private List<AdditiveName> additives;
    private ClickListener clickListener;

    public interface ClickListener {
        void onClick(int position ,String name);
    }

    public AdditivesAdapter(List<AdditiveName> additives ,ClickListener clickListener) {

        this.additives = additives;
        this.clickListener = clickListener;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.additives_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.additiveName.setText(additives.get(position).getName());
        holder.itemView.setOnClickListener(view -> {
            if (clickListener != null) {
                int pos = holder.getAdapterPosition();
                clickListener.onClick(pos, additives.get(pos).getName());
            }
        });

    }

    @Override
    public int getItemCount() {
        return additives.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView additiveName;

        public ViewHolder(View itemView) {
            super(itemView);
            additiveName = itemView.findViewById(R.id.additiveName);
        }

    }
}
