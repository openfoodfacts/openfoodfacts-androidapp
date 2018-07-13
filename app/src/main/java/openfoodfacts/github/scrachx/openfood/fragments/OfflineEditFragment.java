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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
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
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

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
import openfoodfacts.github.scrachx.openfood.views.MainActivity;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import openfoodfacts.github.scrachx.openfood.views.adapters.SaveListAdapter;

import static openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.ITEM_OFFLINE;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class OfflineEditFragment extends NavigationBaseFragment implements SaveListAdapter.SaveClickInterface {

    public static final String LOG_TAG = "OFFLINE_EDIT";
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
        OpenFoodAPIService client = CommonApiManager.getInstance().getOpenFoodApiService();
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

            // Remove the images from the HashMap before uploading the product details
            productDetails.remove("image_front");
            productDetails.remove("image_ingredients");
            productDetails.remove("image_nutrition_facts");
            client.saveProductSingle(product.getBarcode(), productDetails, "Basic b2ZmOm9mZg==")
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
                int imageIcon = R.drawable.ic_done_green_24dp;
                HashMap<String, String> productDetails = product.getProductDetailsMap();
                if (isEmpty(product.getBarcode()) || isEmpty(productDetails.get("image_front"))
                        || isEmpty(productDetails.get("add_brands")) || isEmpty(productDetails.get("quantity")) || isEmpty(productDetails.get("product_name"))) {
                    imageIcon = R.drawable.ic_no_red_24dp;
                }
                saveItems.add(new SaveItem(productDetails.get("product_name"), imageIcon, productDetails.get("image_front"), product.getBarcode(), productDetails.get("quantity"), productDetails.get("add_brands")));
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