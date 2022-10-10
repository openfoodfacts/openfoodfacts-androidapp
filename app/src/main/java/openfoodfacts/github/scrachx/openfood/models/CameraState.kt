package openfoodfacts.github.scrachx.openfood.models

enum class CameraState(val value: Int) {
    Back(0), Front(1);

    companion object {
        fun fromInt(value: Int): CameraState =
            values().first { it.value == value }
    }
}