package openfoodfacts.github.scrachx.openfood.features.additives.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.entities.additive.AdditiveName;

/**
 * Created by prajwalm on 16/04/18.
 */
public class AdditivesAdapter extends RecyclerView.Adapter<AdditiveViewHolder> {
    private final List<AdditiveName> additives;
    private final ClickListener clickListener;

    public AdditivesAdapter(List<AdditiveName> additives, ClickListener clickListener) {

        this.additives = additives;
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onClick(int position, String name);
    }

    @NonNull
    @Override
    public AdditiveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.additives_item, parent, false);
        return new AdditiveViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdditiveViewHolder holder, int position) {

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
}
