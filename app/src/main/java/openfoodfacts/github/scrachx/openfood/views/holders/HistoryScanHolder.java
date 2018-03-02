package openfoodfacts.github.scrachx.openfood.views.holders;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;

public class HistoryScanHolder extends RecyclerView.ViewHolder {

    public TextView txtDate;
    public TextView txtTitle;
    public TextView txtBarcode;
    public TextView txtProductDetails;
    public ImageView imgProduct;
    public ImageView imgNutritionGrade;
    public Activity mActivity;

    public HistoryScanHolder(final View itemView, final String productUrl, Activity activity) {
        super(itemView);
        txtTitle = (TextView) itemView.findViewById(R.id.titleHistory);
        txtBarcode = (TextView) itemView.findViewById(R.id.barcodeHistory);
        txtProductDetails = (TextView) itemView.findViewById(R.id.productDetailsHistory);
        imgProduct = (ImageView) itemView.findViewById(R.id.imgHistoryProduct);
        imgNutritionGrade = (ImageView) itemView.findViewById(R.id.nutritionGradeImage);
        txtDate = (TextView) itemView.findViewById(R.id.dateView);
        mActivity = activity;

        itemView.setOnClickListener(v -> {
            ConnectivityManager cm = (ConnectivityManager) v.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            if (isConnected) {
                OpenFoodAPIClient api = new OpenFoodAPIClient(mActivity);
                api.getProduct(txtBarcode.getText().toString(), (Activity) v.getContext());
            }
        });
    }

}
