package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.Product;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * @author herau
 */
public class ProductsRecyclerViewAdapter extends RecyclerView.Adapter<ProductsRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private final List<Product> products;

    public ProductsRecyclerViewAdapter(List<Product> items){
        this.products = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();

        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.products_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Picasso.with(context)
                .load(products.get(position).getImageSmallUrl())
                .placeholder(R.drawable.placeholder_thumb)
                .error(R.drawable.error_image)
                .fit()
                .centerCrop()
                .into(holder.vProductImage);

        Product product = products.get(position);

        holder.vProductName.setText(product.getProductName());

        StringBuilder stringBuilder = new StringBuilder();
        if (isNotEmpty(product.getBrands())) {
            stringBuilder.append(capitalize(product.getBrands().split(",")[0].trim()));
        }

        if (isNotEmpty(product.getQuantity())) {
            stringBuilder.append(" - ").append(product.getQuantity());
        }

        holder.vProductDetails.setText(stringBuilder.toString());
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
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView vProductImage;
        TextView vProductName;
        TextView vProductDetails;

        ViewHolder(View v) {
            super(v);
            vProductImage = (ImageView) v.findViewById(R.id.imgProduct);
            vProductName = (TextView) v.findViewById(R.id.nameProduct);
            vProductDetails = (TextView) v.findViewById(R.id.productDetails);
        }
    }

}
