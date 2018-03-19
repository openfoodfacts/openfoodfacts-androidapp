package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.ProductImageField;
import openfoodfacts.github.scrachx.openfood.models.SaveItem;
import openfoodfacts.github.scrachx.openfood.models.SendProduct;
import openfoodfacts.github.scrachx.openfood.models.SendProductDao;
import openfoodfacts.github.scrachx.openfood.network.OpenFoodAPIClient;
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.NavigationDrawerType;
import openfoodfacts.github.scrachx.openfood.utils.Utils;
import openfoodfacts.github.scrachx.openfood.views.MainActivity;
import openfoodfacts.github.scrachx.openfood.views.SaveProductOfflineActivity;
import openfoodfacts.github.scrachx.openfood.views.adapters.SaveListAdapter;

import static openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener.ITEM_OFFLINE;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class OfflineEditFragment extends NavigationBaseFragment implements SaveListAdapter.SaveClickInterface {

    public static final String LOG_TAG = "OFFLINE_EDIT";
    @BindView(R.id.listOfflineSave)
    RecyclerView mRecyclerView;
    @BindView(R.id.buttonSendAll)
    Button buttonSend;
    @BindView(R.id.message_container_card_view)
    CardView mCardView;
    private List<SaveItem> saveItems;
    private String loginS, passS;
    private SendProductDao mSendProductDao;
    private int size;
    @BindView(R.id.noDataImg)
    ImageView noDataImage;
    @BindView(R.id.noDataText)
    TextView noDataText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return createView(inflater, container, R.layout.fragment_offline_edit);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.findItem(R.id.action_search);
        item.setVisible(false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSendProductDao = Utils.getAppDaoSession(getActivity()).getSendProductDao();

        final SharedPreferences settingsLogin = getContext().getSharedPreferences("login", 0);
        final SharedPreferences settingsUsage = getContext().getSharedPreferences("usage", 0);
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
        final SharedPreferences settingsUsage = getContext().getSharedPreferences("usage", 0);
        settingsUsage.edit().putBoolean("is_offline_msg_dismissed", true).apply();

    }

    /**
     * User has clicked "upload all" to upload the offline products.
     */
    @OnClick(R.id.buttonSendAll)
    protected void onSendAllProducts() {
        if (!Utils.isAirplaneModeActive(getContext()) && Utils.isNetworkConnected(getContext()) && PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("enableMobileDataUpload", true)) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.txtDialogsTitle)
                    .content(R.string.txtDialogsContentSend)
                    .positiveText(R.string.txtYes)
                    .negativeText(R.string.txtNo)
                    .onPositive((dialog, which) -> uploadProducts())
                    .show();
        } else if (Utils.isAirplaneModeActive(getContext())) {
            new MaterialDialog.Builder(getActivity())
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
        } else if (!Utils.isNetworkConnected(getContext())) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.device_offline_dialog_title)
                    .content(R.string.device_offline_dialog_message)
                    .positiveText(R.string.device_offline_dialog_positive)
                    .negativeText(R.string.device_offline_dialog_negative)
                    .onPositive((dialog, which) -> startActivity(new Intent(Settings.ACTION_SETTINGS)))
                    .show();
        } else if (!PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("enableMobileDataUpload", true) && Utils.isConnectedToMobileData(getContext())) {
            new MaterialDialog.Builder(getActivity())
                    .title(R.string.device_on_mobile_data_warning_title)
                    .content(R.string.device_on_mobile_data_warning_message)
                    .positiveText(R.string.device_on_mobile_data_warning_positive)
                    .negativeText(R.string.device_on_mobile_data_warning_negative)
                    .onPositive((dialog, which) -> ((MainActivity) getActivity()).moveToPreferences())
                    .onNegative((dialog, which) -> uploadProducts())
                    .show();
        }
        SharedPreferences.Editor editor = getContext().getSharedPreferences("usage", 0).edit();
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
        OpenFoodAPIClient apiClient = new OpenFoodAPIClient(getActivity());
        final List<SendProduct> listSaveProduct = mSendProductDao.loadAll();
        size = saveItems.size();

        for (final SendProduct product : listSaveProduct) {
            if (isEmpty(product.getBarcode()) || isEmpty(product.getImgupload_front())) {
                continue;
            }

            if (!loginS.isEmpty() && !passS.isEmpty()) {
                product.setUserId(loginS);
                product.setPassword(passS);
            }

            if (isNotEmpty(product.getImgupload_ingredients())) {
                product.compress(ProductImageField.INGREDIENTS);
            }

            if (isNotEmpty(product.getImgupload_nutrition())) {
                product.compress(ProductImageField.NUTRITION);
            }

            if (isNotEmpty(product.getImgupload_front())) {
                product.compress(ProductImageField.FRONT);
            }
            size--;

            apiClient.post(getActivity(), product, value -> {
                if (value) {
                    int productIndex = listSaveProduct.indexOf(product);

                    if (productIndex >= 0 && productIndex < saveItems.size()) {
                        saveItems.remove(productIndex);
                    }
                    updateDrawerBadge();
                    ((SaveListAdapter) mRecyclerView.getAdapter()).notifyDataSetChanged();
                    mSendProductDao.deleteInTx(mSendProductDao.queryBuilder().where(SendProductDao.Properties.Barcode.eq(product.getBarcode())).list());
                }
            });
        }

    }

    private void updateDrawerBadge() {
        size--;
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.updateBadgeOfflineEditDrawerITem(size);
    }

    @Override
    public void onResume() {
        super.onResume();
        new FillAdapter().execute(getActivity());

        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.offline_edit_drawer));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(int position) {

        Intent intent = new Intent(getActivity(), SaveProductOfflineActivity.class);
        SaveItem si = (SaveItem) saveItems.get(position);
        intent.putExtra("barcode", si.getBarcode());
        startActivity(intent);
    }

    @Override
    public void onLongClick(int position) {

        final int lapos = position;
        new MaterialDialog.Builder(getActivity())
                .title(R.string.txtDialogsTitle)
                .content(R.string.txtDialogsContentDelete)
                .positiveText(R.string.txtYes)
                .negativeText(R.string.txtNo)
                .onPositive((dialog, which) -> {
                    String barcode = saveItems.get(lapos).getBarcode();
                    mSendProductDao.deleteInTx(mSendProductDao.queryBuilder().where(SendProductDao.Properties.Barcode.eq(barcode)).list());
                    final SaveListAdapter sl = (SaveListAdapter) mRecyclerView.getAdapter();
                    size = saveItems.size();
                    saveItems.remove(lapos);
                    updateDrawerBadge();
                    getActivity().runOnUiThread(() -> sl.notifyDataSetChanged());
                })
                .show();


    }

    public class FillAdapter extends AsyncTask<Context, Void, Context> {

        @Override
        protected void onPreExecute() {
            saveItems.clear();
            List<SendProduct> listSaveProduct = mSendProductDao.loadAll();
            SharedPreferences settingsUsage = getContext().getSharedPreferences("usage", 0);
            boolean firstUpload = settingsUsage.getBoolean("firstUpload", false);
            boolean msgdismissed= settingsUsage.getBoolean("is_offline_msg_dismissed",false);
            if (listSaveProduct.size() == 0) {
                if(msgdismissed) {

                    noDataImage.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                    noDataText.setVisibility(View.VISIBLE);
                    noDataText.setText(R.string.no_offline_data);
                    buttonSend.setVisibility(View.GONE);
                    if (!firstUpload) {
                        noDataImage.setImageResource(R.drawable.ic_cloud_upload);
                        noDataText.setText(R.string.first_offline);
                    }
                }
                else{
                    noDataImage.setVisibility(View.INVISIBLE);
                    noDataText.setVisibility(View.INVISIBLE);
                }
            } else {

                noDataImage.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                noDataText.setVisibility(View.GONE);
                buttonSend.setVisibility(View.VISIBLE);
                mCardView.setVisibility(View.GONE);
                Toast toast = Toast.makeText(getActivity(), R.string.txtLoading, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }

        @Override
        protected Context doInBackground(Context... ctx) {
            List<SendProduct> listSaveProduct = mSendProductDao.loadAll();

            int imageIcon = R.drawable.ic_done_green_24dp;

            for (SendProduct product : listSaveProduct) {
                if (isEmpty(product.getBarcode()) || isEmpty(product.getImgupload_front())
                        || isEmpty(product.getBrands()) || isEmpty(product.getWeight()) || isEmpty(product.getName())) {
                    imageIcon = R.drawable.ic_no_red_24dp;
                }

                Bitmap bitmap = Utils.decodeFile(new File(product.getImgupload_front()));
                if (bitmap == null) {
                    Log.e(LOG_TAG, "Unable to load the image of the product: " + product.getBarcode());
                    continue;
                }

                Bitmap imgUrl = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
                saveItems.add(new SaveItem(product.getName(), imageIcon, imgUrl, product.getBarcode(), product.getWeight() + " " + product.getWeight_unit(), product.getBrands()));
            }

            return ctx[0];
        }

        @Override
        protected void onPostExecute(Context ctx) {
            List<SendProduct> listSaveProduct = mSendProductDao.loadAll();
            if (listSaveProduct.isEmpty()) {
                return;
            }

            SaveListAdapter adapter = new SaveListAdapter(ctx, saveItems, OfflineEditFragment.this);
            mRecyclerView.setAdapter(adapter);

            boolean canSend = true;
            for (SendProduct sp : listSaveProduct) {
                if (isEmpty(sp.getBarcode()) || isEmpty(sp.getImgupload_front())) {
                    canSend = false;
                    break;
                }
            }

            buttonSend.setEnabled(canSend);
        }
    }
}
