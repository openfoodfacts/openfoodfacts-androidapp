package openfoodfacts.github.scrachx.openfood.models.entities

abstract class EntityWrapper<T>(private val responses: List<EntityResponse<T>>) {
    open fun map() = responses.map { it.map() }
}