package openfoodfacts.github.scrachx.openfood.models

enum class CameraState(val value: Int) {
    Back(0), Front(1);

    companion object {
        fun fromInt(value: Int) = when (value) {
            0 -> Back
            1 -> Front
            else -> throw IllegalStateException("Unsupported camera state $value.")
        }
    }
}