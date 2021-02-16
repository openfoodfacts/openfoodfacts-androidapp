
package openfoodfacts.github.scrachx.openfood.camera

import android.app.Application
import androidx.annotation.MainThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.vision.barcode.Barcode

/**
 * View model for handling application workflow based on camera preview.
 */
class WorkflowModel(application: Application) : AndroidViewModel(application) {

    val workflowState = MutableLiveData<WorkflowState>()
    val detectedBarcode = MutableLiveData<Barcode>()

    var isCameraLive = false
        private set

    /**
     * State set of the application workflow.
     */
    enum class WorkflowState {
        NOT_STARTED,
        DETECTING,
        DETECTED,
        CONFIRMING
    }

    @MainThread
    fun setWorkflowState(workflowState: WorkflowState) {
        this.workflowState.value = workflowState
    }

    fun markCameraLive() {
        isCameraLive = true
    }

    fun markCameraFrozen() {
        isCameraLive = false
    }

}
