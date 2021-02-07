package openfoodfacts.github.scrachx.openfood.models.entities.category

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import openfoodfacts.github.scrachx.openfood.models.entities.EntityResponse
import openfoodfacts.github.scrachx.openfood.models.entities.EntityWrapper

/**
 * Created by Lobster on 04.03.18.
 */
@JsonDeserialize(using = CategoriesWrapperDeserializer::class)
class CategoriesWrapper(categories: List<EntityResponse<Category>>) : EntityWrapper<Category>(categories)