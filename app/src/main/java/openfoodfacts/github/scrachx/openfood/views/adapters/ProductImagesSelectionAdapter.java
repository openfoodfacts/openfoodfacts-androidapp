package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.function.Consumer;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.images.ImageKeyHelper;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

/**
 * Created by prajwalm on 10/09/18.
 */
public class ProductImagesSelectionAdapter extends RecyclerView.Adapter<ProductImagesSelectionAdapter.CustomViewHolder> {
    private final String barcode;
    private final Context context;
    private final List<String> images;
    private final Consumer<Integer> onImageClick;
    int selectedPosition = -1;

    public ProductImagesSelectionAdapter(Context context, List<String> images, String barcode, Consumer<Integer> onImageClick) {
        this.context = context;
        this.images = images;
        this.barcode = barcode;
        this.onImageClick = onImageClick;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public boolean isSelectionDone() {
        return selectedPosition >= 0;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new CustomViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.image_selectable_item, parent, false));
    }

    public String getSelectedImageName() {
        if (isSelectionDone()) {
            return images.get(selectedPosition);
        }
        return null;
    }

    public String getImageUrl(int position) {
        String imageName = images.get(position);
        return ImageKeyHelper.getImageUrl(barcode, imageName, ImageKeyHelper.IMAGE_EDIT_SIZE_FILE);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {

        String finalUrlString = getImageUrl(position);
        ImageView imageView = holder.productImage;
        ViewGroup viewGroup = holder.parent;
        if (position == selectedPosition) {
            viewGroup.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));
        } else {
            viewGroup.setBackgroundColor(0);
        }
        Utils.picassoBuilder(context).load(finalUrlString).resize(400, 400).centerInside().into(imageView);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ViewGroup parent;
        final ImageView productImage;

        public CustomViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.img);
            parent = itemView.findViewById(R.id.parentGroup);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (selectedPosition >= 0) {
                notifyItemChanged(selectedPosition);
            }
            int adapterPosition = getAdapterPosition();
            //if the user reclick on the same image -> deselect
            if (adapterPosition == selectedPosition) {
                adapterPosition = -1;
            }
            selectedPosition = adapterPosition;
            if (selectedPosition >= 0) {
                notifyItemChanged(selectedPosition);
            }
            if (onImageClick != null) {
                onImageClick.accept(selectedPosition);
            }
        }
    }
}
