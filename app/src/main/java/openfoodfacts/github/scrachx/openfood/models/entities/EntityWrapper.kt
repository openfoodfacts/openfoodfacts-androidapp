package openfoodfacts.github.scrachx.openfood.models.entities

open class EntityWrapper<T>(private val responses: List<EntityResponse<T>>) {
    open fun map() = responses.map { it.map() }
}
