package openfoodfacts.github.scrachx.openfood.models.entities.label

/**
 * Created by Lobster on 03.03.18.
 */
class LabelResponse {
    var code: String
    var names: Map<String, String>
    var wikiDataCode: String? = null
        private set
    var wikiDataIdPresent = false
        private set

    constructor(code: String, names: Map<String, String>, wikiDataCode: String?) {
        this.code = code
        this.wikiDataCode = wikiDataCode
        this.names = names
        wikiDataIdPresent = true
    }

    constructor(code: String, names: Map<String, String>) {
        this.code = code
        this.names = names
        wikiDataIdPresent = false
    }

    fun map(): Label {
        val label: Label
        if (wikiDataIdPresent) {
            label = Label(code, arrayListOf(), wikiDataCode)
            names.forEach { (key, value) ->
                label.names.add(LabelName(label.tag, key, value, wikiDataCode))
            }
        } else {
            label = Label(code, arrayListOf())
            names.forEach { (key, value) ->
                label.names.add(LabelName(label.tag, key, value))
            }
        }
        return label
    }
}