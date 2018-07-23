package openfoodfacts.github.scrachx.openfood.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.greenrobot.greendao.async.AsyncSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.DaoSession;
import openfoodfacts.github.scrachx.openfood.models.OfflineSavedProduct;
import openfoodfacts.github.scrachx.openfood.models.OfflineSavedProductDao;
import openfoodfacts.github.scrachx.openfood.models.SaveItem;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.network.CommonApiManager;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIService;
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.AddProductActivity;
import openfoodfacts.github.scrachx.openfood.views.FullScreenImage;
import openfoodfacts.github.scrachx.openfood.views.MainActivity;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.adapters.SaveListAdapter;

import static openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.ITEM_OFFLINE;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class OfflineEditFragment extends NavigationBaseFragment implements SaveListAdapter.SaveClickInterface {

    public static final String LOG_TAG = "OFFLINE_EDIT";
    private static final String INGREDIENTS_ON_SERVER = "ingredientsTextOnServer";
    private static final String INGREDIENTS_IMAGE_ON_SERVER = "ingredientsImageOnServer";
    private static final String QUANTITY_ON_SERVER = "quantityOnServer";
    private static final String PRODUCT_NAME_ON_SERVER = "productNameOnServer";
    private static final String LINK_ON_SERVER = "linkOnServer";
    @BindView(R.id.listOfflineSave)
    RecyclerView mRecyclerView;
    @BindView(R.id.buttonSendAll)
    Button buttonSend;
    @BindView(R.id.message_container_card_view)
    CardView mCardView;
    @BindView(R.id.noDataImg)
    ImageView noDataImage;
    @BindView(R.id.noDataText)
    TextView noDataText;
    private List<SaveItem> saveItems;
    private String loginS, passS;
    private OfflineSavedProductDao mOfflineSavedProductDao;
    private int size;
    private Activity activity;
    private OpenFoodAPIService client;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return createView(inflater, container, R.layout.fragment_offline_edit);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.findItem(R.id.action_search);
        item.setVisible(false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final SharedPreferences settingsLogin = activity.getBaseContext().getSharedPreferences("login", 0);
        final SharedPreferences settingsUsage = activity.getBaseContext().getSharedPreferences("usage", 0);
        saveItems = new ArrayList<>();
        loginS = settingsLogin.getString("user", "");
        passS = settingsLogin.getString("pass", "");
        boolean isOfflineMsgDismissed = settingsUsage.getBoolean("is_offline_msg_dismissed", false);
        if (isOfflineMsgDismissed) {
            mCardView.setVisibility(View.GONE);
        }
        buttonSend.setEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    @OnClick(R.id.message_dismiss_icon)
    protected void OnClickMessageDismissalIcon() {
        mCardView.setVisibility(View.GONE);
        final SharedPreferences settingsUsage = activity.getBaseContext().getSharedPreferences("usage", 0);
        settingsUsage.edit().putBoolean("is_offline_msg_dismissed", true).apply();

    }

    /**
     * User has clicked "upload all" to upload the offline products.
     */
    @OnClick(R.id.buttonSendAll)
    protected void onSendAllProducts() {
        if (!Utils.isAirplaneModeActive(getContext()) && Utils.isNetworkConnected(activity.getApplicationContext()) && PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("enableMobileDataUpload", true)) {
            uploadProducts();
        } else if (Utils.isAirplaneModeActive(getContext())) {
            new MaterialDialog.Builder(activity)
                    .title(R.string.airplane_mode_active_dialog_title)
                    .content(R.string.airplane_mode_active_dialog_message)
                    .positiveText(R.string.airplane_mode_active_dialog_positive)
                    .negativeText(R.string.airplane_mode_active_dialog_negative)
                    .onPositive((dialog, which) -> {
                        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            try {
                                Intent intentAirplaneMode = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                                intentAirplaneMode.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intentAirplaneMode);
                            } catch (ActivityNotFoundException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Intent intent1 = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent1);
                        }
                    })
                    .show();
        } else if (!Utils.isNetworkConnected(activity.getBaseContext())) {
            new MaterialDialog.Builder(activity)
                    .title(R.string.device_offline_dialog_title)
                    .content(R.string.device_offline_dialog_message)
                    .positiveText(R.string.device_offline_dialog_positive)
                    .negativeText(R.string.device_offline_dialog_negative)
                    .onPositive((dialog, which) -> startActivity(new Intent(Settings.ACTION_SETTINGS)))
                    .show();
        } else if (!PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("enableMobileDataUpload", true) && Utils.isConnectedToMobileData(getContext())) {
            new MaterialDialog.Builder(activity)
                    .title(R.string.device_on_mobile_data_warning_title)
                    .content(R.string.device_on_mobile_data_warning_message)
                    .positiveText(R.string.device_on_mobile_data_warning_positive)
                    .negativeText(R.string.device_on_mobile_data_warning_negative)
                    .onPositive((dialog, which) -> {
                        if (activity instanceof MainActivity) {
                            ((MainActivity) activity).moveToPreferences();
                        }
                    })
                    .onNegative((dialog, which) -> uploadProducts())
                    .show();
        }
        SharedPreferences.Editor editor = activity.getBaseContext().getSharedPreferences("usage", 0).edit();
        editor.putBoolean("firstUpload", true);
        editor.apply();
    }

    @Override
    @NavigationDrawerType
    public int getNavigationDrawerType() {
        return ITEM_OFFLINE;
    }

    /**
     * Upload the offline products.
     */
    private void uploadProducts() {
        SaveListAdapter.showProgressDialog();
        mRecyclerView.getAdapter().notifyDataSetChanged();
        client = CommonApiManager.getInstance().getOpenFoodApiService();
        final List<OfflineSavedProduct> listSaveProduct = mOfflineSavedProductDao.loadAll();
        size = saveItems.size();

        for (final OfflineSavedProduct product : listSaveProduct) {
            HashMap<String, String> productDetails = product.getProductDetailsMap();
            if (isEmpty(product.getBarcode()) || isEmpty(productDetails.get("image_front"))) {
                continue;
            }

            if (!loginS.isEmpty() && !passS.isEmpty()) {
                productDetails.put("user_id", loginS);
                productDetails.put("password", passS);
            }
            size--;

            client.getExistingProductDetails(product.getBarcode())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<State>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                        }

                        @Override
                        public void onSuccess(State state) {
                            if (state.getStatus() == 0) {
                                // Product doesn't exist yet on the server. Add as it is.
                                addProductToServer(product);
                            } else {
                                // Product already exists on the server. Compare values saved locally with the values existing on server.
                                HashMap<String, String> existingValuesMap = new HashMap<>();
                                existingValuesMap.put(INGREDIENTS_ON_SERVER, state.getProduct().getIngredientsText());
                                existingValuesMap.put(INGREDIENTS_IMAGE_ON_SERVER, state.getProduct().getImageIngredientsUrl());
                                existingValuesMap.put(PRODUCT_NAME_ON_SERVER, state.getProduct().getProductName());
                                existingValuesMap.put(QUANTITY_ON_SERVER, state.getProduct().getQuantity());
                                existingValuesMap.put(LINK_ON_SERVER, state.getProduct().getManufactureUrl());
                                checkForExistingIngredients(product, existingValuesMap);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.e(LOG_TAG, e.getMessage());
                        }
                    });
        }
    }

    /**
     * Checks if ingredients already exist on server and compare it with the ingredients stored locally.
     */
    private void checkForExistingIngredients(OfflineSavedProduct product, HashMap<String, String> existingValuesOnServer) {
        HashMap<String, String> productDetails = product.getProductDetailsMap();
        String ingredientsTextOnServer = existingValuesOnServer.get(INGREDIENTS_ON_SERVER);
        if (ingredientsTextOnServer != null && !ingredientsTextOnServer.isEmpty() && productDetails.get("ingredients_text") != null) {
            MaterialDialog.Builder builder = new MaterialDialog.Builder(activity)
                    .title("Ingredients overwrite")
                    .customView(R.layout.dialog_compare_ingredients, true)
                    .positiveText("Choose mine")
                    .negativeText("Keep previous version")
                    .onPositive((dialog, which) -> {
                        dialog.dismiss();
                        checkForExistingProductName(product, existingValuesOnServer);
                    })
                    .onNegative((dialog, which) -> {
                        dialog.dismiss();
                        productDetails.remove("ingredients_text");
                        product.setProductDetailsMap(productDetails);
                        checkForExistingProductName(product, existingValuesOnServer);
                    })
                    .cancelable(false);
            MaterialDialog dialog = builder.build();
            dialog.show();
            View view = dialog.getCustomView();
            if (view != null) {
                ImageView imageLocal = view.findViewById(R.id.image_ingredients_local);
                ImageView imageServer = view.findViewById(R.id.image_ingredients_server);
                TextView ingredientsLocal = view.findViewById(R.id.txt_ingredients_local);
                TextView ingredientsServer = view.findViewById(R.id.txt_ingredients_server);
                ProgressBar imageProgressServer = view.findViewById(R.id.image_progress_server);
                ProgressBar imageProgressLocal = view.findViewById(R.id.image_progress_local);
                ingredientsLocal.setText(productDetails.get("ingredients_text"));
                ingredientsServer.setText(existingValuesOnServer.get(INGREDIENTS_ON_SERVER));
                Picasso.with(getContext())
                        .load(existingValuesOnServer.get(INGREDIENTS_IMAGE_ON_SERVER))
                        .error(R.drawable.placeholder_thumb)
                        .into(imageServer, new Callback() {
                            @Override
                            public void onSuccess() {
                                imageProgressServer.setVisibility(View.GONE);
                                // Add option to zoom image.
                                imageServer.setOnClickListener(v -> {
                                    Intent intent = new Intent(getContext(), FullScreenImage.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("imageurl", existingValuesOnServer.get(INGREDIENTS_IMAGE_ON_SERVER));
                                    intent.putExtras(bundle);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        ActivityOptionsCompat options = ActivityOptionsCompat.
                                                makeSceneTransitionAnimation(activity, imageServer,
                                                        getString(R.string.product_transition));
                                        startActivity(intent, options.toBundle());
                                    } else {
                                        startActivity(intent);
                                    }
                                });
                            }

                            @Override
                            public void onError() {
                                imageProgressServer.setVisibility(View.GONE);
                            }
                        });
                Picasso.with(getContext())
                        .load("file://" + productDetails.get("image_ingredients"))
                        .error(R.drawable.placeholder_thumb)
                        .into(imageLocal, new Callback() {
                            @Override
                            public void onSuccess() {
                                imageProgressLocal.setVisibility(View.GONE);
                                // Add option to zoom image.
                                imageLocal.setOnClickListener(v -> {
                                    Intent intent = new Intent(getContext(), FullScreenImage.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("imageurl", "file://" + productDetails.get("image_ingredients"));
                                    intent.putExtras(bundle);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        ActivityOptionsCompat options = ActivityOptionsCompat.
                                                makeSceneTransitionAnimation(activity, imageLocal,
                                                        getString(R.string.product_transition));
                                        startActivity(intent, options.toBundle());
                                    } else {
                                        startActivity(intent);
                                    }
                                });
                            }

                            @Override
                            public void onError() {
                                imageProgressLocal.setVisibility(View.GONE);
                            }
                        });
            }
        } else {
            checkForExistingProductName(product, existingValuesOnServer);
        }
    }

    /**
     * Checks if product name already exist on server and compare it with the product name stored locally.
     */
    private void checkForExistingProductName(OfflineSavedProduct product, HashMap<String, String> existingValuesOnServer) {
        HashMap<String, String> productDetails = product.getProductDetailsMap();
        String productNameOnServer = existingValuesOnServer.get(PRODUCT_NAME_ON_SERVER);
        if (productNameOnServer != null && !productNameOnServer.isEmpty() && productDetails.get("product_name") != null) {
            new MaterialDialog.Builder(activity)
                    .title("Product name overwrite")
                    .content("Yours: " + productDetails.get("product_name") + "\n" + "Currently on " + getString(R.string.app_name_long) + ": " + productNameOnServer)
                    .positiveText("Choose mine")
                    .negativeText("Keep previous version")
                    .onPositive((dialog, which) -> {
                        dialog.dismiss();
                        checkForExistingQuantity(product, existingValuesOnServer);

                    })
                    .onNegative((dialog, which) -> {
                        dialog.dismiss();
                        productDetails.remove("product_name");
                        product.setProductDetailsMap(productDetails);
                        checkForExistingQuantity(product, existingValuesOnServer);
                    })
                    .cancelable(false)
                    .build()
                    .show();
        } else {
            checkForExistingQuantity(product, existingValuesOnServer);
        }
    }

    /**
     * Checks if quantity already exist on server and compare it with the quantity stored locally.
     */
    private void checkForExistingQuantity(OfflineSavedProduct product, HashMap<String, String> existingValuesOnServer) {
        HashMap<String, String> productDetails = product.getProductDetailsMap();
        String quantityOnServer = existingValuesOnServer.get(QUANTITY_ON_SERVER);
        if (quantityOnServer != null && !quantityOnServer.isEmpty() && productDetails.get("quantity") != null) {
            new MaterialDialog.Builder(activity)
                    .title("Quantity overwrite")
                    .content("Yours: " + productDetails.get("quantity") + "\n" + "Currently on " + getString(R.string.app_name_long) + ": " + quantityOnServer)
                    .positiveText("Choose mine")
                    .negativeText("Keep previous version")
                    .onPositive((dialog, which) -> {
                        dialog.dismiss();
                        checkForExistingLink(product, existingValuesOnServer);
                    })
                    .onNegative((dialog, which) -> {
                        dialog.dismiss();
                        productDetails.remove("quantity");
                        product.setProductDetailsMap(productDetails);
                        checkForExistingLink(product, existingValuesOnServer);
                    })
                    .cancelable(false)
                    .build()
                    .show();
        } else {
            checkForExistingLink(product, existingValuesOnServer);
        }
    }

    /**
     * Checks if link already exist on server and compare it with the link stored locally.
     */
    private void checkForExistingLink(OfflineSavedProduct product, HashMap<String, String> existingValuesOnServer) {
        HashMap<String, String> productDetails = product.getProductDetailsMap();
        String linkOnServer = existingValuesOnServer.get(LINK_ON_SERVER);
        if (linkOnServer != null && !linkOnServer.isEmpty() && productDetails.get("link") != null) {
            new MaterialDialog.Builder(activity)
                    .title("Link overwrite")
                    .content("Yours: " + productDetails.get("link") + "\n" + "Currently on " + getString(R.string.app_name_long) + ": " + linkOnServer)
                    .positiveText("Choose mine")
                    .negativeText("Keep previous version")
                    .onPositive((dialog, which) -> {
                        dialog.dismiss();
                        addProductToServer(product);
                    })
                    .onNegative((dialog, which) -> {
                        dialog.dismiss();
                        productDetails.remove("link");
                        addProductToServer(product);
                    })
                    .cancelable(false)
                    .build()
                    .show();
        } else {
            addProductToServer(product);
        }
    }

    /**
     * Performs network call and uploads the product to the server.
     *
     * @param product The offline product to be uploaded to the server.
     */
    private void addProductToServer(OfflineSavedProduct product) {
        HashMap<String, String> productDetails = product.getProductDetailsMap();
        // Remove the images from the HashMap before uploading the product details
        productDetails.remove("image_front");
        productDetails.remove("image_ingredients");
        productDetails.remove("image_nutrition_facts");
        client.saveProductSingle(product.getBarcode(), productDetails)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new SingleObserver<State>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onSuccess(State state) {
                        Iterator<SaveItem> iterator = saveItems.iterator();
                        while (iterator.hasNext()) {
                            SaveItem s = iterator.next();
                            if (s.getBarcode().equals(product.getBarcode())) {
                                iterator.remove();
                            }
                        }
                        updateDrawerBadge();
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                        mOfflineSavedProductDao.deleteInTx(mOfflineSavedProductDao.queryBuilder().where(OfflineSavedProductDao.Properties.Barcode.eq(product.getBarcode())).list());
                        // Show done when all the products are uploaded.
                        if (saveItems.isEmpty()) {
                            SharedPreferences settingsUsage = activity.getBaseContext().getSharedPreferences("usage", 0);
                            boolean firstUpload = settingsUsage.getBoolean("firstUpload", false);
                            boolean msgdismissed = settingsUsage.getBoolean("is_offline_msg_dismissed", false);
                            if (msgdismissed) {
                                noDataImage.setVisibility(View.VISIBLE);
                                mRecyclerView.setVisibility(View.GONE);
                                noDataText.setVisibility(View.VISIBLE);
                                noDataText.setText(R.string.no_offline_data);
                                buttonSend.setVisibility(View.GONE);
                                if (!firstUpload) {
                                    noDataImage.setImageResource(R.drawable.ic_cloud_upload);
                                    noDataText.setText(R.string.first_offline);
                                }
                            } else {
                                noDataImage.setVisibility(View.INVISIBLE);
                                noDataText.setVisibility(View.INVISIBLE);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(LOG_TAG, e.getMessage());
                    }
                });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = getActivity();
    }

    private void updateDrawerBadge() {
        size--;
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).updateBadgeOfflineEditDrawerITem(size);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fillAdapter();
        if (activity instanceof AppCompatActivity) {
            ActionBar actionBar = ((AppCompatActivity) activity).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(R.string.offline_edit_drawer);
            }
        }
    }

    private void fillAdapter() {
        saveItems.clear();
        DaoSession daoSession = OFFApplication.getInstance().getDaoSession();
        AsyncSession asyncSessionOfflineSavedProduct = daoSession.startAsyncSession();
        mOfflineSavedProductDao = daoSession.getOfflineSavedProductDao();
        asyncSessionOfflineSavedProduct.loadAll(OfflineSavedProduct.class);
        asyncSessionOfflineSavedProduct.setListenerMainThread(operation -> {
            @SuppressWarnings("unchecked")
            List<OfflineSavedProduct> offlineSavedProducts = (List<OfflineSavedProduct>) operation.getResult();
            SharedPreferences settingsUsage = activity.getBaseContext().getSharedPreferences("usage", 0);
            boolean firstUpload = settingsUsage.getBoolean("firstUpload", false);
            boolean msgdismissed = settingsUsage.getBoolean("is_offline_msg_dismissed", false);
            if (offlineSavedProducts.size() == 0) {
                if (msgdismissed) {
                    noDataImage.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                    noDataText.setVisibility(View.VISIBLE);
                    noDataText.setText(R.string.no_offline_data);
                    buttonSend.setVisibility(View.GONE);
                    if (!firstUpload) {
                        noDataImage.setImageResource(R.drawable.ic_cloud_upload);
                        noDataText.setText(R.string.first_offline);
                    }
                } else {
                    noDataImage.setVisibility(View.INVISIBLE);
                    noDataText.setVisibility(View.INVISIBLE);
                }
            } else {
                noDataImage.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                noDataText.setVisibility(View.GONE);
                buttonSend.setVisibility(View.VISIBLE);
                mCardView.setVisibility(View.GONE);
            }

            for (OfflineSavedProduct product : offlineSavedProducts) {
                HashMap<String, String> productDetails = product.getProductDetailsMap();
                int fieldsCompleted = productDetails.size();
                if (productDetails.get("image_front") == null) {
                    fieldsCompleted--;
                }
                if (productDetails.get("image_ingredients") == null) {
                    fieldsCompleted--;
                }
                if (productDetails.get("image_nutrition_facts") == null) {
                    fieldsCompleted--;
                }
                saveItems.add(new SaveItem(productDetails.get("product_name"), fieldsCompleted, productDetails.get("image_front"), product.getBarcode(), productDetails.get("quantity"), productDetails.get("add_brands")));
            }
            if (!offlineSavedProducts.isEmpty()) {
                SaveListAdapter adapter = new SaveListAdapter(activity.getBaseContext(), saveItems, OfflineEditFragment.this);
                mRecyclerView.setAdapter(adapter);
                boolean canSend = true;
                for (OfflineSavedProduct sp : offlineSavedProducts) {
                    HashMap<String, String> productDetails = sp.getProductDetailsMap();
                    if (isEmpty(sp.getBarcode()) || isEmpty(productDetails.get("image_front"))) {
                        canSend = false;
                        break;
                    }
                }
                buttonSend.setEnabled(canSend);
            } else if (mRecyclerView.getAdapter() != null) {
                // last product uploaded, offlineSavedProducts is empty, refresh adapter.
                updateDrawerBadge();
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onClick(int position) {
        Intent intent = new Intent(getActivity(), AddProductActivity.class);
        SaveItem si = saveItems.get(position);
        OfflineSavedProduct offlineSavedProduct = mOfflineSavedProductDao.queryBuilder().where(OfflineSavedProductDao.Properties.Barcode.eq(si.getBarcode())).unique();
        intent.putExtra("edit_offline_product", offlineSavedProduct);
        startActivity(intent);
    }

    @Override
    public void onLongClick(int position) {
        final int lapos = position;
        new MaterialDialog.Builder(activity)
                .title(R.string.txtDialogsTitle)
                .content(R.string.txtDialogsContentDelete)
                .positiveText(R.string.txtYes)
                .negativeText(R.string.txtNo)
                .onPositive((dialog, which) -> {
                    String barcode = saveItems.get(lapos).getBarcode();
                    mOfflineSavedProductDao.deleteInTx(mOfflineSavedProductDao.queryBuilder().where(OfflineSavedProductDao.Properties.Barcode.eq(barcode)).list());
                    final SaveListAdapter sl = (SaveListAdapter) mRecyclerView.getAdapter();
                    size = saveItems.size();
                    saveItems.remove(lapos);
                    updateDrawerBadge();
                    activity.runOnUiThread(sl::notifyDataSetChanged);
                })
                .show();
    }
}