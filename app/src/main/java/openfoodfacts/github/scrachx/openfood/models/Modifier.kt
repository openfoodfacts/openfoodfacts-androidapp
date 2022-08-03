package openfoodfacts.github.scrachx.openfood.models

enum class Modifier(val sym: String) {
    EQUALS_TO("="),
    GREATER_THAN(">"),
    LESS_THAN("<");

    companion object {
        fun findBySymbol(symbol: String) = values().find { it.sym == symbol }
        val DEFAULT = EQUALS_TO
    }
}

