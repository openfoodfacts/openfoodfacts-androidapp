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

fun Modifier.takeUnlessDefault() = takeUnless { this == DEFAULT_MODIFIER }

val MODIFIERS = arrayOf(EQUALS_TO, LESS_THAN, GREATER_THAN)
val DEFAULT_MODIFIER = EQUALS_TO
