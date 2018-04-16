package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Additives;

/**
 * Created by prajwalm on 16/04/18.
 */

public class AdditivesAdapter extends RecyclerView.Adapter<AdditivesAdapter.ViewHolder> {

    private List<Additives> additives;
    private ClickListener clickListener;

    public interface ClickListener {
        void onclick(int position ,String name);
    }

    public AdditivesAdapter(List<Additives> additives ,ClickListener clickListener) {

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
    }

    @Override
    public int getItemCount() {
        return additives.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView additiveName;

        public ViewHolder(View itemView) {
            super(itemView);
            additiveName = itemView.findViewById(R.id.additiveName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            clickListener.onclick(getAdapterPosition(),additives.get(getAdapterPosition()).getName());
        }
    }
}
