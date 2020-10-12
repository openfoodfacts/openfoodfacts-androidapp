package openfoodfacts.github.scrachx.openfood.features.additives.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import openfoodfacts.github.scrachx.openfood.R;

class AdditiveViewHolder extends RecyclerView.ViewHolder {
    final TextView additiveName;

    AdditiveViewHolder(View itemView) {
        super(itemView);
        additiveName = itemView.findViewById(R.id.additiveName);
    }
}
