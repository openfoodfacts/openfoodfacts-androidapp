package openfoodfacts.github.scrachx.openfood.models.entities.states

import openfoodfacts.github.scrachx.openfood.models.entities.EntityResponse



class StateResponse (
        private val tag: String,
        private val names: Map<String, String>
) : EntityResponse<States> {
    override fun map() = States(tag, arrayListOf()).also {
        names.forEach { (key, value) ->
            it.names.add(StatesName(it.tag, key, value))
        }
    }
}
