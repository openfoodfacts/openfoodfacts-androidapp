package openfoodfacts.github.scrachx.openfood.fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.jobs.DownloadOfflineProductService;
import openfoodfacts.github.scrachx.openfood.jobs.ExtractOfflineProductService;
import openfoodfacts.github.scrachx.openfood.utils.NavigationDrawerListener;
import openfoodfacts.github.scrachx.openfood.utils.Utils;

import static openfoodfacts.github.scrachx.openfood.jobs.DownloadOfflineProductService.DOWNLOAD_PROGRESS_UPDATE_KEY;
import static openfoodfacts.github.scrachx.openfood.jobs.DownloadOfflineProductService.IDENTIFIER;
import static openfoodfacts.github.scrachx.openfood.jobs.DownloadOfflineProductService.isDownloadOfflineProductServiceRunning;
import static openfoodfacts.github.scrachx.openfood.jobs.ExtractOfflineProductService.EXTRACT_PROGRESS_UPDATE_KEY;
import static openfoodfacts.github.scrachx.openfood.jobs.ExtractOfflineProductService.IDENTIFIER_;
import static openfoodfacts.github.scrachx.openfood.jobs.ExtractOfflineProductService.isExtractOfflineProductServiceRunning;

public class ProductsDownloadFragment extends NavigationBaseFragment {

    private static final String TAG = "PDFragment";
    @BindView(R.id.d_delete_button)
    ImageButton dDeleteButton;
    @BindView(R.id.d_download_button)
    ImageButton dDownloadButton;
    @BindView(R.id.e_extract_button)
    ImageButton eExtractButton;
    @BindView(R.id.d_progressbar)
    ProgressBar dProgressBar;
    @BindView(R.id.e_progressbar)
    ProgressBar eProgressBar;
    @BindView(R.id.extract_layout)
    ConstraintLayout extractLayout;
    @BindView(R.id.d_status)
    TextView dStatus;
    @BindView(R.id.e_status)
    TextView eStatus;

    SharedPreferences settings;
    Handler handler;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getIntExtra(DOWNLOAD_PROGRESS_UPDATE_KEY, -2) != -2) {
                int downloadProgress = intent.getIntExtra(DOWNLOAD_PROGRESS_UPDATE_KEY, -2);
                handler.post(() -> {
                    if (downloadProgress == 100) {
                        dProgressBar.setVisibility(View.GONE);
                        extractLayout.setVisibility(View.VISIBLE);
                        dDeleteButton.setVisibility(View.VISIBLE);
                        dStatus.setVisibility(View.VISIBLE);
                        dStatus.setText(getString(R.string.txtDownloaded));
                        eStatus.setText(getString(R.string.txtNotSaved));
                        dDownloadButton.setImageResource(R.drawable.baseline_autorenew_24);
                    } else if (downloadProgress == -1) {
                        dProgressBar.setVisibility(View.GONE);
                        dStatus.setVisibility(View.VISIBLE);
                        dStatus.setText(getString(R.string.txtDownloadError));
                        dDownloadButton.setImageResource(R.drawable.ic_file_download_black_24dp);
                    } else {
                        dProgressBar.setIndeterminate(false);
                        dProgressBar.setProgress(downloadProgress);
                        dStatus.setVisibility(View.GONE);
                        dDownloadButton.setImageResource(R.drawable.ic_file_download_black_24dp);
                    }
                });
            }

        }
    };

    private BroadcastReceiver receiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra(EXTRACT_PROGRESS_UPDATE_KEY, -2) != -2) {
                int extractProgress = intent.getIntExtra(EXTRACT_PROGRESS_UPDATE_KEY, -2);
                handler.post(() -> {
                    if (extractProgress == 100) {
                        //done
                        eProgressBar.setVisibility(View.GONE);
                        eStatus.setVisibility(View.VISIBLE);
                        eStatus.setText(getString(R.string.txtSaved));
                        eExtractButton.setImageResource(R.drawable.baseline_autorenew_24);
                    } else if (extractProgress == -1) {
                        eProgressBar.setVisibility(View.GONE);
                        eStatus.setVisibility(View.VISIBLE);
                        eStatus.setText(getString(R.string.txtSaveError));
                        eExtractButton.setImageResource(R.drawable.baseline_save_alt_24);
                    } else {
                        eProgressBar.setIndeterminate(false);
                        eProgressBar.setProgress(extractProgress);
                        eStatus.setVisibility(View.GONE);
                        eExtractButton.setImageResource(R.drawable.baseline_autorenew_24);
                    }
                });
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_products_download);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        settings = Objects.requireNonNull(getActivity()).getSharedPreferences("prefs", Context.MODE_PRIVATE);
        handler = new Handler();

        //handling bundle arguments
        Bundle bundle = getArguments();
        if (bundle != null && bundle.getString("from_extract_service") != null && bundle.getString("from_extract_service").equals("retry")) {
            extract(view);
        }
        if (bundle != null && bundle.getString("from_download_service") != null && bundle.getString("from_download_service").equals("start_extraction")) {
            extract(view);
        }
        if (bundle != null && bundle.getString("from_download_service") != null && bundle.getString("from_download_service").equals("retry")) {
            download(view);
        }

        //managing visibility
        dProgressBar.setVisibility(View.GONE);
        eProgressBar.setVisibility(View.GONE);
        if (settings.getBoolean("is_data_downloaded", false)) {
            //Data is downloaded
            dDownloadButton.setImageResource(R.drawable.baseline_autorenew_24);
            extractLayout.setVisibility(View.VISIBLE);
            dDeleteButton.setVisibility(View.VISIBLE);
            if (settings.getBoolean("is_data_extracted", false)) {
                //Data is extracted
                eStatus.setText(getString(R.string.txtSaved));
                eExtractButton.setImageResource(R.drawable.baseline_autorenew_24);
            } else {
                //data is not extracted
                eStatus.setText(getString(R.string.txtNotSaved));
                eExtractButton.setImageResource(R.drawable.baseline_save_alt_24);
            }
            dStatus.setVisibility(View.VISIBLE);
            dStatus.setText(getString(R.string.txtDownloaded));
        } else {
            //data is not downloaded
            dDownloadButton.setImageResource(R.drawable.ic_file_download_black_24dp);
            dStatus.setVisibility(View.VISIBLE);
            dStatus.setText(getString(R.string.txtNotDownloaded));
            extractLayout.setVisibility(View.GONE);
            eStatus.setVisibility(View.GONE);
            dDeleteButton.setVisibility(View.GONE);
        }

        if (isDownloadOfflineProductServiceRunning) {
            dProgressBar.setVisibility(View.VISIBLE);
            dStatus.setVisibility(View.GONE);
            dProgressBar.setIndeterminate(true);
        } else if (isExtractOfflineProductServiceRunning) {
            eProgressBar.setVisibility(View.VISIBLE);
            eStatus.setVisibility(View.GONE);
            eProgressBar.setIndeterminate(true);
        }

    }

    @OnClick(R.id.d_download_button)
    public void download(View v) {
        if (Utils.isStoragePermissionGranted(getActivity())) {
            if (DownloadOfflineProductService.isDownloadOfflineProductServiceRunning) {
                Toast.makeText(getContext(), getString(R.string.toast_already_running), Toast.LENGTH_SHORT).show();
            } else if (ExtractOfflineProductService.isExtractOfflineProductServiceRunning) {
                Toast.makeText(getContext(), "Extraction in progress", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), getString(R.string.toast_starting_download), Toast.LENGTH_SHORT).show();
                Intent serviceIntent = new Intent(getContext(), DownloadOfflineProductService.class);
                Objects.requireNonNull(getActivity()).startService(serviceIntent);
                dProgressBar.setVisibility(View.VISIBLE);
                dStatus.setVisibility(View.GONE);
                dProgressBar.setIndeterminate(true);
            }
        }
    }

    @OnClick(R.id.d_delete_button)
    public void delete(View v) {
        if (isDownloadOfflineProductServiceRunning || isExtractOfflineProductServiceRunning) {
            Toast.makeText(getContext(), getString(R.string.toast_already_running), Toast.LENGTH_SHORT).show();
        } else {
            if (settings.getBoolean("is_data_downloaded", false)) {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "fr.openfoodfacts.org.products.small.zip");
                boolean isDelete = file.delete();
                if (isDelete) {
                    Toast.makeText(getContext(), getString(R.string.txtToastFileDeleted), Toast.LENGTH_SHORT).show();
                    dDownloadButton.setImageResource(R.drawable.ic_file_download_black_24dp);
                    dStatus.setVisibility(View.VISIBLE);
                    dStatus.setText(getString(R.string.txtNotDownloaded));
                    extractLayout.setVisibility(View.GONE);
                    eStatus.setVisibility(View.GONE);
                    dDeleteButton.setVisibility(View.GONE);
                    settings.edit().putBoolean("is_data_downloaded", false).apply();
                } else {
                    Toast.makeText(getContext(), getString(R.string.txtToastErrorInDelete), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), getString(R.string.txtToastFileNotFound), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.e_extract_button)
    public void extract(View v) {
        if (Utils.isStoragePermissionGranted(getActivity())) {
            if (ExtractOfflineProductService.isExtractOfflineProductServiceRunning) {
                Toast.makeText(getContext(), getString(R.string.toast_already_running), Toast.LENGTH_SHORT).show();
            } else if (ExtractOfflineProductService.isExtractOfflineProductServiceRunning) {
                Toast.makeText(getContext(), "Download in progress", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), getString(R.string.toast_extracting_data), Toast.LENGTH_SHORT).show();
                Intent serviceIntent = new Intent(getContext(), ExtractOfflineProductService.class);
                Objects.requireNonNull(getActivity()).startService(serviceIntent);
                eProgressBar.setVisibility(View.VISIBLE);
                eStatus.setVisibility(View.GONE);
                eProgressBar.setIndeterminate(true);
            }
        }
    }

    public void onResume() {
        super.onResume();
        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.offline_download));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        getActivity().registerReceiver(receiver, new IntentFilter(IDENTIFIER));
        getActivity().registerReceiver(receiver2, new IntentFilter(IDENTIFIER_));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(receiver);
        getActivity().unregisterReceiver(receiver2);
    }

    @Override
    public int getNavigationDrawerType() {
        return NavigationDrawerListener.ITEM_OFFLINE_DOWNLOAD;
    }
}
