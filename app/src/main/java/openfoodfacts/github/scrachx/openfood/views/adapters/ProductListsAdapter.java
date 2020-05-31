package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.ProductLists;

public class ProductListsAdapter extends RecyclerView.Adapter<ProductListsAdapter.ViewHolder> {
    final Context mContext;
    final List<ProductLists> productLists;

    public ProductListsAdapter(Context context, List<ProductLists> productLists)
    {
        this.mContext=context;
        this.productLists=productLists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(mContext)
                .inflate(R.layout.your_product_lists_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String listName=productLists.get(position).getListName();
        Long listId=productLists.get(position).getId();

        int numOfProducts=productLists.get(position).getProducts().size();
        productLists.get(position).setNumOfProducts(numOfProducts);

        holder.tvListTitle.setText(listName);
        holder.tvNumOfProducts.setText(String.valueOf(productLists.get(position).getNumOfProducts()));
    }

    @Override
    public int getItemCount() {
        return productLists.size();
    }

    public void remove(ProductLists data) {
        int position = productLists.indexOf(data);
        productLists.remove(position);
        notifyItemRemoved(position);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        final CardView itemCardView;
        final TextView tvListTitle;
        final TextView tvNumOfProducts;

        public ViewHolder(View itemView) {
            super(itemView);
            tvListTitle=itemView.findViewById(R.id.tvProductListName);
            itemCardView=itemView.findViewById(R.id.cvYourProductList);
            tvNumOfProducts=itemView.findViewById(R.id.tvlistSize);

        }
    }
}
