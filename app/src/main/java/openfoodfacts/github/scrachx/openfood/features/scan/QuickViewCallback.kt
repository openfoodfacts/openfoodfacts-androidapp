package openfoodfacts.github.scrachx.openfood.features.scan

import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.analytics.AnalyticsEvent

internal class QuickViewCallback(
    private val activity: ContinuousScanActivity
) : BottomSheetBehavior.BottomSheetCallback() {
    private var previousSlideOffset = 0f

    internal val peekSmall by lazy { activity.resources.getDimensionPixelSize(R.dimen.scan_summary_peek_small) }
    internal val peekLarge by lazy { activity.resources.getDimensionPixelSize(R.dimen.scan_summary_peek_large) }

    override fun onStateChanged(bottomSheet: View, newState: Int) {
        when (newState) {
            BottomSheetBehavior.STATE_HIDDEN -> {
                activity.lastBarcode = null
                activity.binding.txtProductCallToAction.visibility = View.GONE
                startScanner()
            }
            BottomSheetBehavior.STATE_COLLAPSED -> stopScanner()
            BottomSheetBehavior.STATE_EXPANDED -> {
                stopScanner()
                activity.matomoAnalytics.trackEvent(AnalyticsEvent.ScannedBarcodeResultExpanded(activity.lastBarcode))
            }
            else -> stopScanner()
        }
        if (activity.binding.quickViewSearchByBarcode.visibility == View.VISIBLE) {
            activity.quickViewBehavior.peekHeight = peekSmall
            bottomSheet.layoutParams.height = activity.quickViewBehavior.peekHeight
        } else {
            activity.quickViewBehavior.peekHeight = peekLarge
            bottomSheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        bottomSheet.requestLayout()
    }

    override fun onSlide(bottomSheet: View, slideOffset: Float) {
        val slideDelta = slideOffset - previousSlideOffset
        if (activity.binding.quickViewSearchByBarcode.visibility != View.VISIBLE && activity.binding.quickViewProgress.visibility != View.VISIBLE) {
            if (slideOffset > 0.01f || slideOffset < -0.01f) {
                activity.binding.txtProductCallToAction.visibility = View.GONE
            } else if (activity.binding.quickViewProductNotFound.visibility != View.VISIBLE) {
                activity.binding.txtProductCallToAction.visibility = View.VISIBLE
            }
            if (slideOffset > 0.01f) {
                activity.binding.quickViewDetails.visibility = View.GONE
                activity.binding.quickViewTags.visibility = View.GONE
                if (activity.useMLScanner) {
                    activity.mlKitView.updateWorkflowState(WorkflowState.DETECTED)
                    activity.mlKitView.stopCameraPreview()
                } else {
                    activity.binding.barcodeScanner.pause()
                }
                if (slideDelta > 0 && activity.productViewFragment != null) {
                    activity.productViewFragment!!.bottomSheetWillGrow()
                    activity.binding.bottomNavigation.bottomNavigation.visibility = View.GONE
                }
            } else {
                startScanner()
            }
        }
        previousSlideOffset = slideOffset
    }

    private fun startScanner() {
        if (activity.useMLScanner) {
            activity.mlKitView.updateWorkflowState(WorkflowState.DETECTING)
            activity.mlKitView.startCameraPreview()
        } else {
            activity.binding.barcodeScanner.resume()
        }

        activity.binding.quickViewDetails.visibility = View.VISIBLE
        activity.binding.quickViewTags.visibility = if (activity.analysisTagsEmpty) View.GONE else View.VISIBLE
        activity.binding.bottomNavigation.bottomNavigation.visibility = View.VISIBLE
        activity.binding.toggleFlash.visibility = View.VISIBLE
    }

    private fun stopScanner() {
        if (activity.useMLScanner) {
            activity.mlKitView.updateWorkflowState(WorkflowState.DETECTED)
            activity.mlKitView.stopCameraPreview()
        } else {
            activity.binding.barcodeScanner.pause()
        }

        activity.binding.toggleFlash.visibility = View.GONE
    }
}