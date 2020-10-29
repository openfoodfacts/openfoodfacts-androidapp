package openfoodfacts.github.scrachx.openfood.features.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

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
    private static final String LOG_TAG = ImagesAdapter.class.getSimpleName();
    private final String barcode;
    private final Activity context;
    private final List<String> images;
    private final Consumer<Integer> onImageClick;
    private final HashMap<String, String> imgMap;
    private final boolean isLoggedIn;
    private final OpenFoodAPIClient openFoodAPIClient;
    private final Product product;

    public ImagesAdapter(Activity activity, @NonNull Product product, boolean isLoggedin, List<String> images, Consumer<Integer> onImageClick) {
        this.context = activity;
        this.images = images;
        this.barcode = product.getCode();
        this.onImageClick = onImageClick;
        this.product = product;
        openFoodAPIClient = new OpenFoodAPIClient(activity);
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

        Log.d(LOG_TAG, String.format("Loading image %s...", finalUrlString));
        Utils.picassoBuilder(context).load(finalUrlString).resize(400, 400).centerInside().into(imageView);

        if (!isLoggedIn) {
            menuButton.setVisibility(View.INVISIBLE);
        }
        menuButton.setOnClickListener(v -> {

            PopupMenu popupMenu = new PopupMenu(context, menuButton);
            popupMenu.inflate(R.menu.menu_image_edit);
            popupMenu.setOnMenuItemClickListener(new PopupItemClickListener(position));

            popupMenu.show();
        });
    }

    public void displaySetImageName(@NonNull String response) {
        JSONObject jsonObject = Utils.createJsonObject(response);
        String imageName;
        try {
            imageName = jsonObject.getString("imagefield");
        } catch (JSONException | NullPointerException e) {
            Log.e(LOG_TAG, "displaySetImageName", e);
            Toast.makeText(context, String.format("Error while setting image from response %s", response), Toast.LENGTH_LONG).show();
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
            onImageClick.accept(position);
        }
    }

    private class PopupItemClickListener implements PopupMenu.OnMenuItemClickListener {
        private final int position;

        public PopupItemClickListener(int position) {
            this.position = position;
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            final String imgIdKey = ImageKeyHelper.IMG_ID;
            switch (item.getItemId()) {

                case R.id.set_ingredient_image:
                    imgMap.put(imgIdKey, images.get(position));
                    imgMap.put(ImageKeyHelper.PRODUCT_BARCODE, barcode);
                    imgMap.put(ImageKeyHelper.IMAGE_STRING_ID, ImageKeyHelper.getImageStringKey(ProductImageField.INGREDIENTS, product));

                    openFoodAPIClient.editImage(product.getCode(), imgMap, (value, response) -> ImagesAdapter.this.displaySetImageName(response));
                    break;

                case R.id.set_nutrition_image:
                    imgMap.put(imgIdKey, images.get(position));
                    imgMap.put(ImageKeyHelper.PRODUCT_BARCODE, barcode);
                    imgMap.put(ImageKeyHelper.IMAGE_STRING_ID, ImageKeyHelper.getImageStringKey(ProductImageField.NUTRITION, product));

                    openFoodAPIClient.editImage(product.getCode(), imgMap, (value, response) -> ImagesAdapter.this.displaySetImageName(response));
                    break;

                case R.id.set_front_image:
                    imgMap.put(imgIdKey, images.get(position));
                    imgMap.put(ImageKeyHelper.PRODUCT_BARCODE, barcode);
                    imgMap.put(ImageKeyHelper.IMAGE_STRING_ID, ImageKeyHelper.getImageStringKey(ProductImageField.FRONT, product));

                    openFoodAPIClient.editImage(product.getCode(), imgMap, (value, response) -> ImagesAdapter.this.displaySetImageName(response));
                    break;

                case R.id.report_image:
                    context.startActivity(Intent.createChooser(
                        new Intent(Intent.ACTION_SEND)
                            .setData(Uri.parse("mailto:"))
                            .setType(OpenFoodAPIClient.MIME_TEXT)
                            .putExtra(Intent.EXTRA_EMAIL, new String[]{"Open Food Facts <contact@openfoodfacts.org>"})
                            .putExtra(Intent.EXTRA_SUBJECT, String.format("Photo report for product %s", barcode))
                            .putExtra(Intent.EXTRA_TEXT, String.format("I've spotted a problematic photo for product %s", barcode)),
                        "Send mail"));
                    break;
                default:
                    break;
            }
            return true;
        }
    }
}
