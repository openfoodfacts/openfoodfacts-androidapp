package openfoodfacts.github.scrachx.openfood.views.holders;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import openfoodfacts.github.scrachx.openfood.R;

public class UserRatingsHolder extends RecyclerView.ViewHolder {

    public CardView vCardView;
    public ImageView vProductImage;
    public RatingBar vProductRating;
    public TextView vProductName;
    public TextView vComment;
    public Activity mActivity;

    public UserRatingsHolder(final View itemView,  Activity activity) {
        super(itemView);
        vCardView = (CardView) itemView.findViewById(R.id.cardViewHistory);
        vProductImage = (ImageView) itemView.findViewById(R.id.productImageReview);
        vProductRating = (RatingBar) itemView.findViewById(R.id.ratingBarReview);
        vProductName = (TextView) itemView.findViewById(R.id.productTitleReview);
        vComment = (TextView) itemView.findViewById(R.id.commentReview);
        mActivity = activity;
    }
}
