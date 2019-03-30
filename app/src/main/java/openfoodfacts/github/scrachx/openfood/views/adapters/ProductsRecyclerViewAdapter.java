package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.YourListedProducts;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

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
            if (products.get(position).getImageSmallUrl() == null)
                productHolder.vProductImageProgressbar.setVisibility(View.GONE);

            // Load Image if isLowBatteryMode is false
            if (!isLowBatteryMode) {
                Picasso.with(context)
                        .load(products.get(position).getImageSmallUrl())
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
                            public void onError() {
                                productHolder.vProductImageProgressbar.setVisibility(View.GONE);
                            }
                        });
            } else {
                Picasso.with(context).load(R.drawable.placeholder_thumb).into(productHolder.vProductImage);
                productHolder.vProductImageProgressbar.setVisibility(View.INVISIBLE);
            }

            Product product = products.get(position);

            productHolder.vProductName.setText(product.getProductName());

            String brandsQuantityDetails = YourListedProducts.getProductBrandsQuantityDetails(product);

            if (isNotEmpty(product.getNutritionGradeFr())) {
                if(Utils.getSmallImageGrade(product.getNutritionGradeFr()) != 0) {
                    productHolder.vProductGrade.setImageDrawable(ContextCompat.getDrawable(context, Utils.getSmallImageGrade(product
                            .getNutritionGradeFr())));
                } else {
                    productHolder.vProductGrade.setVisibility(View.INVISIBLE);
                }
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
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar1);
        }
    }

}
