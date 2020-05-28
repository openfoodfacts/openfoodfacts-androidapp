package openfoodfacts.github.scrachx.openfood.views.product.ingredients_analysis.adapter;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.ProductIngredient;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;

public class IngredientAnalysisRecyclerAdapter extends RecyclerView.Adapter<IngredientAnalysisRecyclerAdapter.IngredientAnalysisViewHolder> implements CustomTabActivityHelper.ConnectionCallback {
    private Context mContext;
    private List<ProductIngredient> productIngredients;
    private Activity activity;

    public IngredientAnalysisRecyclerAdapter(Context context, List<ProductIngredient> productIngredients, Activity activity) {
        this.productIngredients = productIngredients;
        this.mContext = context;
        this.activity = activity;
    }

    @NonNull
    @Override
    public IngredientAnalysisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext)
            .inflate(R.layout.ingredient_analysis_list_item, parent, false);
        IngredientAnalysisViewHolder viewHolder = new IngredientAnalysisViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientAnalysisViewHolder holder, int position) {
        String text = productIngredients.get(position).getText();
        String id = productIngredients.get(position).getId().replace("\"", "");
        String name = text.replace("\"", "");  //removes quotations
        holder.tvIngredientName.setText(name);
        holder.tvIngredientName.setOnClickListener(view -> {
            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().build();
            customTabsIntent.intent.putExtra("android.intent.extra.REFERRER", Uri.parse("android-app://" + mContext.getPackageName()));
            CustomTabActivityHelper.openCustomTab(activity, customTabsIntent, Uri.parse(mContext.getString(R.string.website) + "ingredient/" + id), new WebViewFallback());
        });
    }

    public class IngredientAnalysisViewHolder extends RecyclerView.ViewHolder {
        TextView tvIngredientName;

        public IngredientAnalysisViewHolder(View itemView) {
            super(itemView);
            tvIngredientName = itemView.findViewById(R.id.tv_ingredient_name);
        }
    }

    @Override
    public int getItemCount() {
        return productIngredients.size();
    }

    @Override
    public void onCustomTabsConnected() {

    }

    @Override
    public void onCustomTabsDisconnected() {

    }
}
