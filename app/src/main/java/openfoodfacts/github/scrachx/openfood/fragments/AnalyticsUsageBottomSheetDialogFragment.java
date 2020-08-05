package openfoodfacts.github.scrachx.openfood.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.databinding.FragmentAnalyticsUsageBottomSheetBinding;
import openfoodfacts.github.scrachx.openfood.utils.AnalyticsService;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;

public class AnalyticsUsageBottomSheetDialogFragment extends BottomSheetDialogFragment {
    public AnalyticsUsageBottomSheetDialogFragment() {
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        FragmentAnalyticsUsageBottomSheetBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_analytics_usage_bottom_sheet, container, false);

        binding.grantButton.setOnClickListener(view -> {
            PreferenceManager.getDefaultSharedPreferences(OFFApplication.getInstance()).edit().putBoolean("privacyAnalyticsReporting", true).apply();
            AnalyticsService.getInstance().onAnalyticsEnabledToggled(true);
            dismiss();
        });
        binding.declineButton.setOnClickListener(view -> {
            PreferenceManager.getDefaultSharedPreferences(OFFApplication.getInstance()).edit().putBoolean("privacyAnalyticsReporting", false).apply();
            AnalyticsService.getInstance().onAnalyticsEnabledToggled(false);
            dismiss();
        });

        return binding.getRoot();
    }
}
