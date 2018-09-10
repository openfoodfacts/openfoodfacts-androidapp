package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import openfoodfacts.github.scrachx.openfood.R;

/**
 * Created by prajwalm on 10/09/18.
 */

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> images;
    private String barcode;
    private final OnImageClickInterface onImageClick;

    public interface OnImageClickInterface {
        void onImageClick(int position);
    }

    public ImagesAdapter(Context context, ArrayList<String> images, String barcode, OnImageClickInterface onImageClick) {
        this.context = context;
        this.images = images;
        this.barcode = barcode;
        this.onImageClick = onImageClick;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.images_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        String imageName = images.get(position);
        ImageView imageView = holder.productImage;

        String baseUrlString = "https://static.openfoodfacts.org/images/products/";
        String barcodePattern = new StringBuilder(barcode)
                .insert(3, "/")
                .insert(7, "/")
                .insert(11, "/")
                .toString();
        String finalUrlString = baseUrlString + barcodePattern + "/" + imageName + ".400" + ".jpg";

        Picasso.with(context).load(finalUrlString).resize(400, 400).centerInside().placeholder(R.drawable.placeholder_thumb).into(imageView);


    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView productImage;

        public ViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.img);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            int position = getAdapterPosition();
            onImageClick.onImageClick(position);

        }
    }


}
