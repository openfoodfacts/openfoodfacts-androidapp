package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImageField;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;

/**
 * Created by prajwalm on 10/09/18.
 */

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> images;
    private String barcode;
    private final OnImageClickInterface onImageClick;
    private HashMap<String, String> imgMap;
    private Product product;
    private OpenFoodAPIClient openFoodAPIClient;


    public interface OnImageClickInterface {
        void onImageClick(int position);
    }

    public ImagesAdapter(Context context, ArrayList<String> images, String barcode, OnImageClickInterface onImageClick, Product product) {
        this.context = context;
        this.images = images;
        this.barcode = barcode;
        this.onImageClick = onImageClick;
        this.product = product;
        openFoodAPIClient = new OpenFoodAPIClient(context);
        imgMap = new HashMap<>();
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
        Button menuButton = holder.menuButton;

        String baseUrlString = "https://static.openfoodfacts.org/images/products/";
        String barcodePattern = new StringBuilder(barcode)
                .insert(3, "/")
                .insert(7, "/")
                .insert(11, "/")
                .toString();
        String finalUrlString = baseUrlString + barcodePattern + "/" + imageName + ".400" + ".jpg";
        //String finalUrlString = baseUrlString + barcodePattern + "/" + imageName +".jpg";

        Picasso.with(context).load(finalUrlString).resize(400, 400).centerInside().into(imageView);


        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popupMenu = new PopupMenu(context, menuButton);
                popupMenu.inflate(R.menu.menu_image_edit);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {

                            case R.id.set_ingredient_image:
                                imgMap.put("imgid", images.get(position));
                                imgMap.put("code", barcode);
                                imgMap.put("id", ProductImageField.INGREDIENTS.toString() + '_' + product.getLang());

                                openFoodAPIClient.editImage(product.getCode(), imgMap, new OpenFoodAPIClient.OnEditImageCallback() {
                                    @Override
                                    public void onEditResponse(boolean value, String response) {
                                        Toast.makeText(context, response, Toast.LENGTH_LONG).show();
                                    }

                                });
                                break;

                            case R.id.set_nutrition_image:
                                imgMap.put("imgid", images.get(position));
                                imgMap.put("code", barcode);
                                imgMap.put("id", ProductImageField.NUTRITION.toString() + '_' + product.getLang());

                                openFoodAPIClient.editImage(product.getCode(), imgMap, new OpenFoodAPIClient.OnEditImageCallback() {
                                    @Override
                                    public void onEditResponse(boolean value, String response) {
                                        Toast.makeText(context, response, Toast.LENGTH_LONG).show();
                                    }

                                });
                                break;

                            case R.id.set_front_image:
                                imgMap.put("imgid", images.get(position));
                                imgMap.put("code", barcode);
                                imgMap.put("id", ProductImageField.FRONT.toString() + '_' + product.getLang());

                                openFoodAPIClient.editImage(product.getCode(), imgMap, new OpenFoodAPIClient.OnEditImageCallback() {
                                    @Override
                                    public void onEditResponse(boolean value, String response) {
                                        Toast.makeText(context, response, Toast.LENGTH_LONG).show();
                                    }

                                });
                                break;

                        }
                        return true;
                    }
                });

                popupMenu.show();

            }
        });


    }


    @Override
    public int getItemCount() {
        return images.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView productImage;
        Button menuButton;

        public ViewHolder(View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.img);
            menuButton = itemView.findViewById(R.id.buttonOptions);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            int position = getAdapterPosition();
            onImageClick.onImageClick(position);

        }
    }


}
