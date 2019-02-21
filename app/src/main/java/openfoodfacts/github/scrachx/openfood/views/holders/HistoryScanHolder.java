package openfoodfacts.github.scrachx.openfood.views.holders;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
    public ProgressBar historyImageProgressbar;

    public HistoryScanHolder(final View itemView, final String productUrl, Activity activity) {
        super(itemView);
        txtTitle = itemView.findViewById(R.id.titleHistory);
        txtBarcode = itemView.findViewById(R.id.barcodeHistory);
        txtProductDetails = itemView.findViewById(R.id.productDetailsHistory);
        imgProduct = itemView.findViewById(R.id.imgHistoryProduct);
        imgNutritionGrade = itemView.findViewById(R.id.nutritionGradeImage);
        txtDate = itemView.findViewById(R.id.dateView);
        mActivity = activity;
        historyImageProgressbar = itemView.findViewById(R.id.historyImageProgressbar);

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
