package openfoodfacts.github.scrachx.openfood.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.entities.ProductLists;
import openfoodfacts.github.scrachx.openfood.models.entities.YourListedProduct;
import openfoodfacts.github.scrachx.openfood.models.entities.YourListedProductDao;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.YourListedProductsActivity;

//recyclerview adapter to display product lists in a dialog
public class DialogAddToListAdapter extends RecyclerView.Adapter<DialogAddToListAdapter.TvListViewHolder> {
    private final String barcode;
    private final String imageUrl;
    private final Context mContext;
    private final String productDetails;
    private YourListedProductDao yourListedProductDao;
    private final List<ProductLists> productLists;
    private final String productName;

    public DialogAddToListAdapter(Context context, List<ProductLists> productLists,
                                  String barcode, String productName, String productDetails, String imageUrl) {
        this.mContext = context;
        this.productLists = productLists;
        this.barcode = barcode;
        this.productName = productName;
        this.productDetails = productDetails;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public TvListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
            .inflate(R.layout.dialog_add_to_list_recycler_item, parent, false);
        return new TvListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TvListViewHolder holder, int position) {
        String listName = productLists.get(position).getListName();
        holder.tvListTitle.setText(listName);
        holder.itemView.setOnClickListener(v -> {
            Long listId = productLists.get(position).getId();
            YourListedProduct product = new YourListedProduct();
            product.setBarcode(barcode);
            product.setListId(listId);
            product.setListName(listName);
            product.setProductName(productName);
            product.setProductDetails(productDetails);
            product.setImageUrl(imageUrl);

            yourListedProductDao = Utils.getDaoSession().getYourListedProductDao();
            yourListedProductDao.insertOrReplace(product);

            Intent intent = new Intent(mContext, YourListedProductsActivity.class);
            intent.putExtra("listName", listName);
            intent.putExtra("listId", listId);
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productLists.size();
    }

    static class TvListViewHolder extends RecyclerView.ViewHolder {
        final TextView tvListTitle;

        TvListViewHolder(View itemView) {
            super(itemView);
            tvListTitle = itemView.findViewById(R.id.tvDialogListName);
        }
    }
}

