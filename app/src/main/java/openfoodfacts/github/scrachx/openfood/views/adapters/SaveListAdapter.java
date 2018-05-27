package org.openfoodfacts.scanner.views.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.Executors;

import org.openfoodfacts.scanner.R;
import org.openfoodfacts.scanner.models.SaveItem;

public class SaveListAdapter extends RecyclerView.Adapter<SaveListAdapter.SaveViewHolder> {

    private final Context context;
    private final List<SaveItem> saveItems;
    private SaveClickInterface mSaveClickInterface;


    public SaveListAdapter(Context context, List<SaveItem> saveItems, SaveClickInterface saveClickInterface) {
        this.context = context;
        this.saveItems = saveItems;
        this.mSaveClickInterface = saveClickInterface;
    }

    @Override
    public SaveViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.save_list_item, parent, false);
        return new SaveViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SaveViewHolder holder, int position) {

        SaveItem item = saveItems.get(position);

        holder.imgIcon.setImageDrawable(AppCompatResources.getDrawable(context, item.getIcon()));
        holder.txtTitle.setText(item.getTitle());
        Picasso.with(context).load("file://"+item.getUrl()).config(Bitmap.Config.RGB_565).into(holder.imgProduct);
        holder.txtBarcode.setText(item.getBarcode());
        holder.txtWeight.setText(item.getWeight());
        holder.txtBrand.setText(item.getBrand());

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return saveItems.size();
    }

    public interface SaveClickInterface {
        void onClick(int position);

        void onLongClick(int position);
    }

    class SaveViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        ImageView imgIcon;
        TextView txtTitle;
        TextView txtBarcode;
        ImageView imgProduct;
        TextView txtWeight;
        TextView txtBrand;

        public SaveViewHolder(View itemView) {
            super(itemView);
            imgIcon = itemView.findViewById(R.id.iconSave);
            txtTitle = itemView.findViewById(R.id.titleSave);
            txtBarcode = itemView.findViewById(R.id.barcodeSave);
            imgProduct = itemView.findViewById(R.id.imgSaveProduct);
            txtWeight = itemView.findViewById(R.id.offlineWeight);
            txtBrand = itemView.findViewById(R.id.offlineBrand);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {

            int pos = getAdapterPosition();
            mSaveClickInterface.onClick(pos);
        }

        @Override
        public boolean onLongClick(View view) {
            int pos = getAdapterPosition();
            mSaveClickInterface.onLongClick(pos);
            return true;
        }
    }

}