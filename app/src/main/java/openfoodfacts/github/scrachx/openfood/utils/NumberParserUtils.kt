package openfoodfacts.github.scrachx.openfood.utils

private fun parseDouble(number: String?, defaultValue: Double): Double {
    return if (number.isNullOrBlank()) {
        defaultValue
    } else try {
        number.toDouble()
    } catch (e: NumberFormatException) {
        defaultValue
    }
}

fun getAsInt(number: Any?, defaultValue: Int): Int {
    if (number == null) return defaultValue

    return if (number is Number) number.toInt()
    else parseDouble(number.toString(), defaultValue.toDouble()).toInt()
}

fun getAsInt(imgDetails: Map<String, *>?, key: String?, defaultValue: Int): Int {
    return if (imgDetails == null || key == null) {
        defaultValue
    } else getAsInt(imgDetails[key], defaultValue)
}

fun getAsFloat(imgDetails: Map<String, *>?, key: String?, defaultValue: Float): Float {
    return if (imgDetails == null || key == null) {
        defaultValue
    } else getAsFloat(imgDetails[key], defaultValue)
}

fun getAsFloat(number: Any?, defaultValue: Float): Float {
    if (number == null) {
        return defaultValue
    }
    return if (number is Number) number.toFloat()
    else parseDouble(number.toString(), defaultValue.toDouble()).toFloat()
}