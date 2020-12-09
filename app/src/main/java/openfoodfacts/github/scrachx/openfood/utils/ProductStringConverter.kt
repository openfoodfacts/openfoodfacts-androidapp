package openfoodfacts.github.scrachx.openfood.utils

import com.fasterxml.jackson.databind.util.StdConverter
import org.apache.commons.lang.StringEscapeUtils

class ProductStringConverter : StdConverter<String, String>() {
    override fun convert(value: String) = StringEscapeUtils.unescapeHtml(value)
            .replace("\\'", "'")
            .replace("&quot", "'")
}