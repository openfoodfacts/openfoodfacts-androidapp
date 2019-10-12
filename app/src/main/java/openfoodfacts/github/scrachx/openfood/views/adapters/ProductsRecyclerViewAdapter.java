package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.YourListedProducts;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @author herau & itchix
 */
public class ProductsRecyclerViewAdapter extends RecyclerView.Adapter {
    private static final int VIEW_ITEM = 1;
    private static final int VIEW_LOAD = 0;
    private Context context;
    private final List<Product> products;
    private boolean isLowBatteryMode;

    public ProductsRecyclerViewAdapter(List<Product> items, boolean isLowBatteryMode) {
        this.products = items;
        this.isLowBatteryMode = isLowBatteryMode;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();

        int layoutResourceId = viewType == VIEW_ITEM ? R.layout.products_list_item : R.layout.progressbar_endless_list;
        View v = LayoutInflater.from(parent.getContext()).inflate(layoutResourceId, parent, false);

        if (viewType == VIEW_ITEM) {
            return new ProductViewHolder(v);
        } else {
            return new ProgressViewHolder(v);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return products.get(position) != null ? VIEW_ITEM : VIEW_LOAD;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if (holder instanceof ProductViewHolder) {
            ProductViewHolder productHolder = (ProductViewHolder) holder;
            productHolder.vProductImageProgressbar.setVisibility(View.VISIBLE);
            final String imageSmallUrl = products.get(position).getImageSmallUrl(LocaleHelper.getLanguage(ProductsRecyclerViewAdapter.this.context));
            if (imageSmallUrl == null) {
                productHolder.vProductImageProgressbar.setVisibility(View.GONE);
            }

            // Load Image if isLowBatteryMode is false
            if (!isLowBatteryMode) {
                Picasso.get()
                    .load(imageSmallUrl)
                    .placeholder(R.drawable.placeholder_thumb)
                    .error(R.drawable.error_image)
                    .fit()
                    .centerCrop()
                    .into(productHolder.vProductImage, new Callback() {
                        @Override
                        public void onSuccess() {
                            productHolder.vProductImageProgressbar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception ex) {
                            productHolder.vProductImageProgressbar.setVisibility(View.GONE);
                        }
                    });
            } else {
                Picasso.get().load(R.drawable.placeholder_thumb).into(productHolder.vProductImage);
                productHolder.vProductImageProgressbar.setVisibility(View.INVISIBLE);
            }

            Product product = products.get(position);

            productHolder.vProductName.setText(product.getProductName());
            String productNameInLocale = (String) product.getAdditionalProperties().get(OpenFoodAPIClient.getLocaleProductNameField());
            if (StringUtils.isNotBlank(productNameInLocale)) {
                productHolder.vProductName.setText(productNameInLocale);
            }

            String brandsQuantityDetails = YourListedProducts.getProductBrandsQuantityDetails(product);

            final int gradeResource = Utils.getSmallImageGrade(product);
            if (gradeResource != 0) {
                productHolder.vProductGrade.setVisibility(View.VISIBLE);
                productHolder.vProductGrade.setImageResource(gradeResource);
            } else {
                productHolder.vProductGrade.setVisibility(View.INVISIBLE);
            }
            productHolder.vProductDetails.setText(brandsQuantityDetails);
        }
    }

    public Product getProduct(int position) {
        return products.get(position);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView vProductImage;
        ImageView vProductGrade;
        TextView vProductName;
        TextView vProductDetails;
        ProgressBar vProductImageProgressbar;

        ProductViewHolder(View v) {
            super(v);
            vProductImage = v.findViewById(R.id.imgProduct);
            vProductGrade = v.findViewById(R.id.imgGrade);
            vProductName = v.findViewById(R.id.nameProduct);
            vProductDetails = v.findViewById(R.id.productDetails);
            vProductImageProgressbar = v.findViewById(R.id.searchImgProgressbar);
        }
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder {

        public ProgressViewHolder(View v) {
            super(v);
        }
    }
}
