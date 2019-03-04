package openfoodfacts.github.scrachx.openfood.views.adapters;


import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.ProductLists;
import openfoodfacts.github.scrachx.openfood.models.YourListedProduct;
import openfoodfacts.github.scrachx.openfood.models.YourListedProductDao;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.YourListedProducts;

//recyclerview adapter to display product lists in a dialog
public class DialogAddToListAdapter extends RecyclerView.Adapter<DialogAddToListAdapter.ViewHolder> {
    Context mContext;
    List<ProductLists> productLists;
    String barcode,productName;
    YourListedProductDao yourListedProductDao;
    String productDetails,imageUrl;

    public DialogAddToListAdapter(Context context, List<ProductLists> productLists,
                                  String barcode,String productName,String productDetails,String imageUrl)
    {
        this.mContext=context;
        this.productLists=productLists;
        this.barcode=barcode;
        this.productName=productName;
        this.productDetails=productDetails;
        this.imageUrl=imageUrl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(mContext)
                .inflate(R.layout.dialog_add_to_list_recycler_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String listName=productLists.get(position).getListName();
        holder.tvListTitle.setText(listName);
        holder.itemView.setOnClickListener(v-> {
            Long listId=productLists.get(position).getId();
            YourListedProduct product=new YourListedProduct();
            product.setBarcode(barcode);
            product.setListId(listId);
            product.setListName(listName);
            product.setProductName(productName);
            product.setProductDetails(productDetails);
            product.setImageUrl(imageUrl);

            yourListedProductDao=Utils.getAppDaoSession(mContext).getYourListedProductDao();
            yourListedProductDao.insertOrReplace(product);

            Intent intent=new Intent(mContext,YourListedProducts.class);
            intent.putExtra("listName",listName);
            intent.putExtra("listId",listId);
            mContext.startActivity(intent);

        });
    }

    @Override
    public int getItemCount() {
        return productLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvListTitle;

        public ViewHolder(View itemView) {
            super(itemView);
            tvListTitle=itemView.findViewById(R.id.tvDialogListName);
        }
    }
}

