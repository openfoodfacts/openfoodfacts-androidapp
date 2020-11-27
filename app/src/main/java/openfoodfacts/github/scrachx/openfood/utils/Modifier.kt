package openfoodfacts.github.scrachx.openfood.utils

object Modifier {
    const val GREATER_THAN = ">"
    const val LESS_THAN = "<"
    const val EQUALS_TO = "="
    const val DEFAULT_MODIFIER = EQUALS_TO
    @JvmField
    val MODIFIERS = arrayOf(EQUALS_TO, LESS_THAN, GREATER_THAN)
}