package openfoodfacts.github.scrachx.openfood.models

class LanguageData internal constructor(
        val code: String,
        val name: String,
        val isSupported: Boolean
) : Comparable<LanguageData> {
    override fun toString() = "$name [$code]"

    override fun equals(other: Any?) = other is LanguageData && this.code == other.code

    override fun hashCode() = code.hashCode()

    override fun compareTo(other: LanguageData) = name.compareTo(other.name)
}

fun List<LanguageData>.findByCode(languageCode: String) = indexOfFirst { languageCode == it.code }
