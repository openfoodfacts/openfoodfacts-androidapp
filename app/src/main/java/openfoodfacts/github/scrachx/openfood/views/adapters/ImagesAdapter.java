package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.images.ImageKeyHelper;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.ProductImageField;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

/**
 * Created by prajwalm on 10/09/18.
 */
public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.CustomViewHolder> {
    private final String barcode;
    private final Context context;
    private final List<String> images;
    private final OnImageClickInterface onImageClick;
    private final HashMap<String, String> imgMap;
    private final boolean isLoggedIn;
    private final OpenFoodAPIClient openFoodAPIClient;
    private final Product product;

    public interface OnImageClickInterface {
        void onImageClick(int position);
    }

    public ImagesAdapter(Context context, List<String> images, String barcode, OnImageClickInterface onImageClick, Product product, boolean isLoggedin) {
        this.context = context;
        this.images = images;
        this.barcode = barcode;
        this.onImageClick = onImageClick;
        this.product = product;
        openFoodAPIClient = new OpenFoodAPIClient(context);
        imgMap = new HashMap<>();
        this.isLoggedIn = isLoggedin;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new CustomViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.images_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {

        String imageName = images.get(position);
        ImageView imageView = holder.productImage;
        Button menuButton = holder.menuButton;
        String finalUrlString = ImageKeyHelper.getImageUrl(barcode, imageName, ImageKeyHelper.IMAGE_EDIT_SIZE_FILE);

        Picasso.get().load(finalUrlString).resize(400, 400).centerInside().into(imageView);
        Log.i("URL", finalUrlString);

        if (!isLoggedIn) {
            menuButton.setVisibility(View.INVISIBLE);
        }
        menuButton.setOnClickListener(v -> {

            PopupMenu popupMenu = new PopupMenu(context, menuButton);
            popupMenu.inflate(R.menu.menu_image_edit);
            popupMenu.setOnMenuItemClickListener(item -> {
                final String imgIdKey = ImageKeyHelper.IMG_ID;
                switch (item.getItemId()) {

                    case R.id.set_ingredient_image:
                        imgMap.put(imgIdKey, images.get(position));
                        imgMap.put(ImageKeyHelper.PRODUCT_BARCODE, barcode);
                        imgMap.put(ImageKeyHelper.IMAGE_STRING_ID, ImageKeyHelper.getImageStringKey(ProductImageField.INGREDIENTS, product));

                        openFoodAPIClient.editImage(product.getCode(), imgMap, (value, response) -> displaySetImageName(response));
                        break;

                    case R.id.set_nutrition_image:
                        imgMap.put(imgIdKey, images.get(position));
                        imgMap.put(ImageKeyHelper.PRODUCT_BARCODE, barcode);
                        imgMap.put(ImageKeyHelper.IMAGE_STRING_ID, ImageKeyHelper.getImageStringKey(ProductImageField.NUTRITION, product));

                        openFoodAPIClient.editImage(product.getCode(), imgMap, (value, response) -> displaySetImageName(response));
                        break;

                    case R.id.set_front_image:
                        imgMap.put(imgIdKey, images.get(position));
                        imgMap.put(ImageKeyHelper.PRODUCT_BARCODE, barcode);
                        imgMap.put(ImageKeyHelper.IMAGE_STRING_ID, ImageKeyHelper.getImageStringKey(ProductImageField.FRONT, product));

                        openFoodAPIClient.editImage(product.getCode(), imgMap, (value, response) -> displaySetImageName(response));
                        break;

                    case R.id.report_image:

                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setData(Uri.parse("mailto:"));
                        emailIntent.setType(OpenFoodAPIClient.MIME_TEXT);
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"contact@openfoodfacts.org"});
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Photo report for product " + barcode);
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "I've spotted a problematic photo for product " + barcode);
                        context.startActivity(Intent.createChooser(emailIntent, "Send mail"));
                        break;
                    default:
                        break;
                }
                return true;
            });

            popupMenu.show();
        });
    }

    public void displaySetImageName(@NonNull String response) {
        JSONObject jsonObject = Utils.createJsonObject(response);
        String imageName = null;
        try {
            imageName = jsonObject.getString("imagefield");
        } catch (JSONException e) {
            Log.e(getClass().getSimpleName(), "displaySetImageName", e);
            Toast.makeText(context, "Error while setting image " + imageName, Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(context, context.getString(R.string.set_image_name) + " " + imageName, Toast.LENGTH_LONG).show();
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final Button menuButton;
        final ImageView productImage;

        public CustomViewHolder(View itemView) {
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
