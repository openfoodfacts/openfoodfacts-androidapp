package openfoodfacts.github.scrachx.openfood.models.entities

interface EntityResponse<T> {
    fun map(): T
}
