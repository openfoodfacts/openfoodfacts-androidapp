package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.AllergenName;
import openfoodfacts.github.scrachx.openfood.repositories.IProductRepository;

public class AllergensAdapter extends RecyclerView.Adapter<AllergensAdapter.ViewHolder> {

    private IProductRepository mProductRepository;
    private List<AllergenName> mAllergens;
    private Activity mActivity;

    public AllergensAdapter(IProductRepository productRepository, List<AllergenName> allergens, Activity activity) {
        mProductRepository = productRepository;
        mAllergens = allergens;
        mActivity = activity;
    }

    public void setAllergens(List<AllergenName> allergens) {
        mAllergens = allergens;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView nameTextView;
        public Button messageButton;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.allergen_name);
            messageButton = itemView.findViewById(R.id.delete_button);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.item_allergens, parent, false);
        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final AllergenName allergen = mAllergens.get(position);
        TextView textView = holder.nameTextView;
        textView.setText(allergen.getName().substring(allergen.getName().indexOf(":") + 1));
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
            mAllergens = new ArrayList<AllergenName>();
        }

        return mAllergens.size();
    }
}