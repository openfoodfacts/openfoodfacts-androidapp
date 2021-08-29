package openfoodfacts.github.scrachx.openfood.models

import openfoodfacts.github.scrachx.openfood.models.Modifier.*

enum class Modifier(val sym: String) {
    GREATER_THAN(">"),
    EQUALS_TO("="),
    LESS_THAN("<");

    companion object {
        fun findBySymbol(symbol: String) = values().find { it.sym == symbol }
    }
}

fun Modifier.nullIfDefault() = if (this != DEFAULT_MODIFIER) this else null

inline fun Modifier.ifNotDefault(block: (Modifier) -> Unit) {
    if (this != DEFAULT_MODIFIER) block(this)
}

inline fun Modifier.ifDefault(block: (Modifier) -> Unit) {
    if (this == DEFAULT_MODIFIER) block(this)
}

val MODIFIERS = arrayOf(EQUALS_TO, LESS_THAN, GREATER_THAN)
val DEFAULT_MODIFIER = EQUALS_TO
