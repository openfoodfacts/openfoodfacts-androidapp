package openfoodfacts.github.scrachx.openfood.utils

import androidx.annotation.Keep
import com.fasterxml.jackson.databind.util.StdConverter
import org.apache.commons.text.StringEscapeUtils

class ProductStringConverter @Keep constructor() : StdConverter<String, String>() {
    override fun convert(value: String) = StringEscapeUtils.unescapeHtml4(value)
            .replace("\\'", "'")
            .replace("&quot", "'")
}