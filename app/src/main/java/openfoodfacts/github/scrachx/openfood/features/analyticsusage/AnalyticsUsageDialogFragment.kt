package openfoodfacts.github.scrachx.openfood.features.analyticsusage

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
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

    private var _binding: FragmentAnalyticsUsageBottomSheetBinding? = null
    private val binding get() = _binding!!


    @Inject
    lateinit var matomoAnalytics: MatomoAnalytics

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    init {
        isCancelable = false
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAnalyticsUsageBottomSheetBinding.inflate(inflater, container, false)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun saveAnalyticsReportingPref(value: Boolean) {
        sharedPreferences.edit {
            putBoolean(getString(R.string.pref_analytics_reporting_key), value)
        }
    }
}
