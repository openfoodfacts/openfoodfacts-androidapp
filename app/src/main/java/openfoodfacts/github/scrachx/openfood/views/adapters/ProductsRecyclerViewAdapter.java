package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.content.Context;
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
import openfoodfacts.github.scrachx.openfood.models.Product;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * @author herau
 * @modified itchix
 */
public class ProductsRecyclerViewAdapter extends RecyclerView.Adapter {

    private final int VIEW_ITEM = 1;
    private final int VIEW_LOAD = 0;

    private Context context;
    private final List<Product> products;

    public ProductsRecyclerViewAdapter(List<Product> items){
        this.products = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();

        RecyclerView.ViewHolder vh;
        View v;
        if (viewType == VIEW_ITEM) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.products_list_item, parent, false);
            vh = new ProductViewHolder(v);
        } else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.progressbar_endless_list, parent, false);
            vh = new ProgressViewHolder(v);
        }

        return vh;
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
            Picasso.with(context)
                    .load(products.get(position).getImageSmallUrl())
                    .placeholder(R.drawable.placeholder_thumb)
                    .error(R.drawable.error_image)
                    .fit()
                    .centerCrop()
                    .into(((ProductViewHolder) holder).vProductImage);

            Product product = products.get(position);

            ((ProductViewHolder) holder).vProductName.setText(product.getProductName());

            StringBuilder stringBuilder = new StringBuilder();
            if (isNotEmpty(product.getBrands())) {
                stringBuilder.append(capitalize(product.getBrands().split(",")[0].trim()));
            }

            if (isNotEmpty(product.getQuantity())) {
                stringBuilder.append(" - ").append(product.getQuantity());
            }

            ((ProductViewHolder) holder).vProductDetails.setText(stringBuilder.toString());
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
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
        TextView vProductName;
        TextView vProductDetails;

        ProductViewHolder(View v) {
            super(v);
            vProductImage = (ImageView) v.findViewById(R.id.imgProduct);
            vProductName = (TextView) v.findViewById(R.id.nameProduct);
            vProductDetails = (TextView) v.findViewById(R.id.productDetails);
        }
    }

    static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
        }
    }

}
