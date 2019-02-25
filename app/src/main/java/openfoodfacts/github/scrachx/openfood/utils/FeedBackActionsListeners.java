package openfoodfacts.github.scrachx.openfood.utils;

import android.content.DialogInterface;

public interface FeedBackActionsListeners {
    void onPositiveFeedback(FeedBackDialog dialog);
    void onNegativeFeedback(FeedBackDialog dialog);
    void onAmbiguityFeedback(FeedBackDialog dialog);
    void onCancelListener(DialogInterface dialog);
}