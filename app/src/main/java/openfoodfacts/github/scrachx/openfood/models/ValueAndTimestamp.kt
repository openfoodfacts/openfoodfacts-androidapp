package openfoodfacts.github.scrachx.openfood.models

internal data class ValueAndTimestamp<V>(
    val timestamp: Long,
    val value: V,
)
