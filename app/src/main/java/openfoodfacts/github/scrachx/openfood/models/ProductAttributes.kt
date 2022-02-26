package openfoodfacts.github.scrachx.openfood.models

@JvmInline
value class Barcode(val b: String) {
    fun isEmpty() = b.isEmpty()
}

fun String.asBarcode() = Barcode(this)
