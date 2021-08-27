package openfoodfacts.github.scrachx.openfood.features.analyticsusage

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.analytics.MatomoAnalytics
import openfoodfacts.github.scrachx.openfood.databinding.FragmentAnalyticsUsageBottomSheetBinding
import javax.inject.Inject

@AndroidEntryPoint
class AnalyticsUsageDialogFragment : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "AnalyticsUsageDialogFragment"
    }

    @Inject
    lateinit var matomoAnalytics: MatomoAnalytics

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    init {
        isCancelable = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding: FragmentAnalyticsUsageBottomSheetBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_analytics_usage_bottom_sheet, container, false)
        binding.grantButton.setOnClickListener {
            saveAnalyticsReportingPref(true)
            matomoAnalytics.setEnabled(true)
            dismiss()
        }
        binding.declineButton.setOnClickListener {
            saveAnalyticsReportingPref(false)
            matomoAnalytics.setEnabled(false)
            dismiss()
        }
        return binding.root
    }

    private fun saveAnalyticsReportingPref(value: Boolean) {
        sharedPreferences.edit {
            putBoolean(getString(R.string.pref_analytics_reporting_key), value)
        }
    }
}
