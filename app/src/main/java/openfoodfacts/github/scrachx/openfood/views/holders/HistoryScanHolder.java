package openfoodfacts.github.scrachx.openfood.views.holders;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;

public class HistoryScanHolder extends RecyclerView.ViewHolder {

    public TextView txtDate;
    public TextView txtTitle;
    public TextView txtBarcode;
    public TextView txtBrands;
    public ImageView imgProduct;
    public ImageButton imgShare;
    public Activity mActivity;

    public HistoryScanHolder(final View itemView, final String productUrl, Activity activity) {
        super(itemView);
        txtTitle = (TextView) itemView.findViewById(R.id.titleHistory);
        txtBarcode = (TextView) itemView.findViewById(R.id.barcodeHistory);
        txtBrands = (TextView) itemView.findViewById(R.id.brandsHistory);
        imgProduct = (ImageView) itemView.findViewById(R.id.imgHistoryProduct);
        imgShare = (ImageButton) itemView.findViewById(R.id.iconShareHistory);
        txtDate = (TextView) itemView.findViewById(R.id.dateView);
        mActivity = activity;

        imgShare.setOnClickListener(view -> {
            String url = " " + productUrl + txtBarcode.getText();
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = itemView.getResources().getString(R.string.msg_share) + url;
            String shareSub = "\n\n";
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, shareSub);
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            itemView.getContext().startActivity(Intent.createChooser(sharingIntent, "Share using"));
        });

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
