package openfoodfacts.github.scrachx.openfood.models

typealias Field = Pair<String, String?>
typealias Fields = Map<String, String?>

fun fieldsOf(vararg fields: Field): Fields = mapOf(*fields)
fun emptyFields(): Fields = emptyMap()

typealias MutableFields = MutableMap<String, String?>

fun mutableFieldsOf(vararg fields: Field): MutableFields = mutableMapOf(*fields)
