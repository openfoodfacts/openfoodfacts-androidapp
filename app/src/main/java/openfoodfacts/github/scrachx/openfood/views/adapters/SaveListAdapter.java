package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.SaveItem;

public class SaveListAdapter extends RecyclerView.Adapter<SaveListAdapter.SaveViewHolder> {

    private static boolean isUploading;
    private final Context context;
    private final List<SaveItem> saveItems;
    private SaveClickInterface mSaveClickInterface;

    public SaveListAdapter(Context context, List<SaveItem> saveItems, SaveClickInterface saveClickInterface) {
        this.context = context;
        this.saveItems = saveItems;
        this.mSaveClickInterface = saveClickInterface;
        isUploading = false;
    }

    public static void showProgressDialog() {
        isUploading = true;
    }

    @NonNull
    @Override
    public SaveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.save_list_item, parent, false);
        return new SaveViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SaveViewHolder holder, int position) {
        SaveItem item = saveItems.get(position);
        if (item.getFieldsCompleted() < 10) {
            holder.percentageCompleted.getProgressDrawable().setColorFilter(
                    Color.RED, PorterDuff.Mode.MULTIPLY);
        } else if (item.getFieldsCompleted() >= 10 && item.getFieldsCompleted() < 19) {
            holder.percentageCompleted.getProgressDrawable().setColorFilter(
                    Color.YELLOW, PorterDuff.Mode.MULTIPLY);
        } else {
            holder.percentageCompleted.getProgressDrawable().setColorFilter(
                    Color.GREEN, PorterDuff.Mode.MULTIPLY);
        }
        holder.percentageCompleted.setProgress(item.getFieldsCompleted());
        int percentageValue = (item.getFieldsCompleted() * 100) / 25;
        if (percentageValue > 100) {
            percentageValue = 100;
        }
        holder.txtPercentage.setText(context.getString(R.string.percent, percentageValue));
        if (isUploading) {
            holder.percentageCompleted.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.VISIBLE);
        }
        holder.txtTitle.setText(item.getTitle());
        Picasso.with(context).load("file://" + item.getUrl()).config(Bitmap.Config.RGB_565).into(holder.imgProduct);
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
        ProgressBar percentageCompleted;
        TextView txtPercentage;
        TextView txtTitle;
        TextView txtBarcode;
        ImageView imgProduct;
        TextView txtWeight;
        TextView txtBrand;
        ProgressBar progressBar;

        SaveViewHolder(View itemView) {
            super(itemView);
            percentageCompleted = itemView.findViewById(R.id.percentage_completed);
            txtPercentage = itemView.findViewById(R.id.txt_percentage);
            txtTitle = itemView.findViewById(R.id.titleSave);
            txtBarcode = itemView.findViewById(R.id.barcodeSave);
            imgProduct = itemView.findViewById(R.id.imgSaveProduct);
            txtWeight = itemView.findViewById(R.id.offlineWeight);
            txtBrand = itemView.findViewById(R.id.offlineBrand);
            progressBar = itemView.findViewById(R.id.offlineUploadProgressBar);
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