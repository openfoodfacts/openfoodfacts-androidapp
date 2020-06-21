package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.AllergenName;
import openfoodfacts.github.scrachx.openfood.repositories.IProductRepository;

public class AllergensAdapter extends RecyclerView.Adapter<AllergensAdapter.CustomViewHolder> {
    private final IProductRepository mProductRepository;
    private List<AllergenName> mAllergens;

    public AllergensAdapter(@NonNull IProductRepository productRepository, @Nullable List<AllergenName> allergens) {
        mProductRepository = productRepository;
        setAllergens(allergens);
    }

    public void setAllergens(@Nullable List<AllergenName> allergens) {
        mAllergens = allergens != null ? allergens : new ArrayList<>();
    }

    public static class CustomViewHolder extends RecyclerView.ViewHolder {
        final Button messageButton;
        final TextView nameTextView;

        public CustomViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.allergen_name);
            messageButton = itemView.findViewById(R.id.delete_button);
        }
    }

    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.item_allergens, parent, false);
        return new CustomViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int position) {
        final AllergenName allergen = mAllergens.get(position);
        TextView textView = holder.nameTextView;
        textView.setText(allergen.getName().substring(allergen.getName().indexOf(':') + 1));
        Button button = holder.messageButton;
        button.setText(R.string.delete_txt);
        button.setOnClickListener(v -> {
            mAllergens.remove(holder.getAdapterPosition());
            notifyItemRemoved(holder.getAdapterPosition());
            mProductRepository.setAllergenEnabled(allergen.getAllergenTag(), false);
        });
    }

    @Override
    public int getItemCount() {
        return mAllergens.size();
    }
}
