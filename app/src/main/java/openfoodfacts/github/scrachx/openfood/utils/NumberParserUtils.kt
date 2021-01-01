package openfoodfacts.github.scrachx.openfood.utils

private fun parseDouble(number: String?, defaultValue: Double) = when {
    number.isNullOrBlank() -> defaultValue
    else -> try {
        number.toDouble()
    } catch (e: NumberFormatException) {
        defaultValue
    }
}

fun getAsInt(number: Any?, defaultValue: Int) = when (number) {
    null -> defaultValue
    is Number -> number.toInt()
    else -> parseDouble(number.toString(), defaultValue.toDouble()).toInt()
}

fun getAsInt(imgDetails: Map<String, *>?, key: String?, defaultValue: Int) = when {
    imgDetails == null || key == null -> defaultValue
    else -> getAsInt(imgDetails[key], defaultValue)
}

fun getAsFloat(imgDetails: Map<String, *>?, key: String?, defaultValue: Float): Float {
    return if (imgDetails == null || key == null) {
        defaultValue
    } else getAsFloat(imgDetails[key], defaultValue)
}

fun getAsFloat(number: Any?, defaultValue: Float) = when (number) {
    null -> defaultValue
    is Number -> number.toFloat()
    else -> parseDouble(number.toString(), defaultValue.toDouble()).toFloat()
}