package openfoodfacts.github.scrachx.openfood.models.entities.label

import openfoodfacts.github.scrachx.openfood.models.entities.EntityResponse

/**
 * Created by Lobster on 03.03.18.
 */
class LabelResponse(
    private var code: String,
    private var names: Map<String, String>,
    private val wikiDataCode: String? = null,
) : EntityResponse<Label> {
    override fun map(): Label {
        val label: Label
        if (wikiDataCode != null) {
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