package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.AllergenName;
import openfoodfacts.github.scrachx.openfood.repositories.IProductRepository;

import java.util.ArrayList;
import java.util.List;

public class AllergensAdapter extends RecyclerView.Adapter<AllergensAdapter.CustomViewHolder> {

    private IProductRepository mProductRepository;
    private List<AllergenName> mAllergens;

    public AllergensAdapter(IProductRepository productRepository, List<AllergenName> allergens) {
        mProductRepository = productRepository;
        mAllergens = allergens;
    }

    public void setAllergens(List<AllergenName> allergens) {
        mAllergens = allergens;
    }

    public static class CustomViewHolder extends RecyclerView.ViewHolder {

        TextView nameTextView;
        Button messageButton;

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
        if (mAllergens == null) {
            mAllergens = new ArrayList<>();
        }

        return mAllergens.size();
    }

}
