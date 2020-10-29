package openfoodfacts.github.scrachx.openfood.features.scanhistory;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;

public class ScanHistoryHolder extends RecyclerView.ViewHolder {
    public final TextView txtDate;
    public final TextView txtTitle;
    public final TextView txtBarcode;
    public final TextView txtProductDetails;
    public final ImageView imgProduct;
    public final ImageView imgNutritionGrade;
    public final Activity mActivity;
    public final ProgressBar historyImageProgressbar;

    public ScanHistoryHolder(final View itemView, Activity activity) {
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
                api.openProduct(txtBarcode.getText().toString(), (Activity) v.getContext());
            } else {
                Toast.makeText(mActivity, R.string.history_network_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
