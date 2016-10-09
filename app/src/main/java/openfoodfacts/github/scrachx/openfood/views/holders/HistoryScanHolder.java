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
import openfoodfacts.github.scrachx.openfood.models.FoodAPIRestClientUsage;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

public class HistoryScanHolder extends RecyclerView.ViewHolder {

    public CardView cv;
    public TextView txtTitle;
    public TextView txtBarcode;
    public TextView txtBrands;
    public ImageView imgProduct;
    public ImageButton imgShare;

    public HistoryScanHolder(final View itemView) {
        super(itemView);
        cv = (CardView) itemView.findViewById(R.id.cardViewHistory);
        txtTitle = (TextView) itemView.findViewById(R.id.titleHistory);
        txtBarcode = (TextView) itemView.findViewById(R.id.barcodeHistory);
        txtBrands = (TextView) itemView.findViewById(R.id.brandsHistory);
        imgProduct = (ImageView) itemView.findViewById(R.id.imgHistoryProduct);
        imgShare = (ImageButton) itemView.findViewById(R.id.iconShareHistory);

        imgShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = " " + Utils.getUriProductByCurrentLanguage() + txtBarcode.getText();
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                String shareBody = itemView.getResources().getString(R.string.msg_share) + url;
                String shareSub = "\n\n";
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub);
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                itemView.getContext().startActivity(Intent.createChooser(sharingIntent, "Share using"));
               }
        });

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager cm = (ConnectivityManager) v.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
                if(isConnected) {
                    FoodAPIRestClientUsage api = new FoodAPIRestClientUsage(v.getContext().getString(R.string.openfoodUrl));
                    api.getProduct(txtBarcode.getText().toString(), (Activity) v.getContext());
                }
            }
        });
    }

}
