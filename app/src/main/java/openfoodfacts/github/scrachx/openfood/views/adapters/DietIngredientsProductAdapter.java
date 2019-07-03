package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.DietIngredientsProductFragment;

public class DietIngredientsProductAdapter extends RecyclerView.Adapter<DietIngredientsProductAdapter.DietIngredientsProductHolder> {

    private final DietIngredientsProductFragment.ClickListener listener;
    List<SpannableStringBuilder> list;
    @BindView( R.id.toolbar )
    Toolbar toolbar;

    public DietIngredientsProductAdapter(List<SpannableStringBuilder> list, DietIngredientsProductFragment.ClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public static class DietIngredientsProductHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView ingredientTextView;
        private ImageButton stateGreenImageButton;
        private ImageButton stateGreyImageButton;
        private ImageButton stateOrangeImageButton;
        private ImageButton stateRedImageButton;
        private WeakReference<DietIngredientsProductFragment.ClickListener> listenerRef;

        public DietIngredientsProductHolder(View itemView, DietIngredientsProductFragment.ClickListener listener) {
            super(itemView);
            ingredientTextView = (TextView) itemView.findViewById(R.id.ingredientTextView);
            stateGreenImageButton = (ImageButton) itemView.findViewById(R.id.stateGreenImageButton);
            stateGreyImageButton = (ImageButton) itemView.findViewById(R.id.stateGreyImageButton);
            stateOrangeImageButton = (ImageButton) itemView.findViewById(R.id.stateOrangeImageButton);
            stateRedImageButton = (ImageButton) itemView.findViewById(R.id.stateRedImageButton);
            listenerRef = new WeakReference<>(listener);
            itemView.setOnClickListener(this);
            stateGreenImageButton.setOnClickListener(this);
            stateGreenImageButton.setOnLongClickListener(this);
            stateGreyImageButton.setOnClickListener(this);
            stateGreyImageButton.setOnLongClickListener(this);
            stateOrangeImageButton.setOnClickListener(this);
            stateOrangeImageButton.setOnLongClickListener(this);
            stateRedImageButton.setOnClickListener(this);
            stateRedImageButton.setOnLongClickListener(this);
        }

        public void bind(SpannableStringBuilder ingredient){
            ingredientTextView.setText(ingredient);
        }

        // onClick Listener for view
        @Override
        public void onClick(View v) {
            listenerRef.get().onPositionClicked(getAdapterPosition(), v);
        }

        //onLongClickListener for view
        @Override
        public boolean onLongClick(View v) {
            listenerRef.get().onLongClicked(getAdapterPosition(), v);
            return true;
        }

        public int stateFromView(View v) {
            int state = 2;
            if (v.getId() == stateGreenImageButton.getId()) {
                state = 1;
            } else if (v.getId() == stateOrangeImageButton.getId()) {
                state=0;
            } else if (v.getId() == stateRedImageButton.getId()) {
                state=-1;
            }
            return state;
        }

    }

    @Override
    public DietIngredientsProductHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_diet_ingredients_product,viewGroup,false);
        return new DietIngredientsProductHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(DietIngredientsProductHolder dietIngredientHolder, int position) {
        SpannableStringBuilder ingredient = list.get(position);
        dietIngredientHolder.bind(ingredient);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}