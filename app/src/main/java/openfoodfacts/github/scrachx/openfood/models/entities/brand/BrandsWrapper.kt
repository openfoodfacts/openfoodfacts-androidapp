package openfoodfacts.github.scrachx.openfood.models.entities.brand

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import openfoodfacts.github.scrachx.openfood.models.entities.EntityResponse
import openfoodfacts.github.scrachx.openfood.models.entities.EntityWrapper

@JsonDeserialize(using = BrandsWrapperDeserializer::class)
class BrandsWrapper(brands: List<EntityResponse<Brand>>) : EntityWrapper<Brand>(brands)