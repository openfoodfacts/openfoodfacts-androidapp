package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.fragments.DietIngredientsProductFragment;
import openfoodfacts.github.scrachx.openfood.models.Diet;

public class DietHAdapter extends RecyclerView.Adapter<DietHAdapter.DietHHolder> {

    private final DietIngredientsProductFragment.ClickListener listener;
    List<Diet> dietList;

    //ajouter un constructeur prenant en entrée une liste
    public DietHAdapter(List<Diet> dietList, DietIngredientsProductFragment.ClickListener listener) {
        this.dietList = dietList;
        this.listener = listener;
    }

    public static class DietHHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView dietTextView;
        private WeakReference<DietIngredientsProductFragment.ClickListener> listenerRef;

        //itemView est la vue correspondante à 1 cellule
        public DietHHolder(View itemView, DietIngredientsProductFragment.ClickListener listener) {
            super(itemView);
            dietTextView = (TextView) itemView.findViewById(R.id.dietTextView);
            listenerRef = new WeakReference<>(listener);
            itemView.setOnClickListener(this);
        }

        //puis ajouter une fonction pour remplir la cellule en fonction d'un ingredient
        public void bind(String diet){
            dietTextView.setText(diet);
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

    }

    //cette fonction permet de créer les viewHolder
    //et par la même d'indiquer la vue à inflater (à partir des layout xml)
    @Override
    public DietHHolder onCreateViewHolder(ViewGroup viewGroup, int itemType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_diet_h,viewGroup,false);
        return new DietHHolder(view, listener);
    }

    //c'est ici que nous allons remplir notre cellule avec le texte/image de chaque MyObjects
    @Override
    public void onBindViewHolder(DietHHolder dietHHolder, int position) {
        String diet = dietList.get(position).getTag().substring(3);
        dietHHolder.bind(diet);
    }

    @Override
    public int getItemCount() {
        return dietList.size();
    }
}