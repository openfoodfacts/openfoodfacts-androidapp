package openfoodfacts.github.scrachx.openfood.views.holders;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;

public class UserRatingsHolder extends RecyclerView.ViewHolder {

    public CardView vCardView;
    public ImageView vProductImage;
    public RatingBar vProductRating;
    public TextView vProductName;
    public TextView vComment;
    public String mProductBarcode;
    public Activity mActivity;

    public UserRatingsHolder(final View itemView,  Activity activity) {
        super(itemView);
        vCardView = (CardView) itemView.findViewById(R.id.cardViewHistory);
        vProductImage = (ImageView) itemView.findViewById(R.id.productImageReview);
        vProductRating = (RatingBar) itemView.findViewById(R.id.ratingBarReview);
        vProductName = (TextView) itemView.findViewById(R.id.productTitleReview);
        vComment = (TextView) itemView.findViewById(R.id.commentReview);
        mActivity = activity;

        itemView.setOnClickListener(v -> {
            ConnectivityManager cm = (ConnectivityManager) v.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
            if(isConnected) {
                OpenFoodAPIClient api = new OpenFoodAPIClient(mActivity);
                api.getProduct(mProductBarcode, (Activity) v.getContext());
            }
        });
    }
}
