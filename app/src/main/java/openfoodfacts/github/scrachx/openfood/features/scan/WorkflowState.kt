package openfoodfacts.github.scrachx.openfood.features.scan

/**
 * State set of the application workflow.
 */
enum class WorkflowState {
    NOT_STARTED,
    DETECTING,
    DETECTED,
    CONFIRMING
}
