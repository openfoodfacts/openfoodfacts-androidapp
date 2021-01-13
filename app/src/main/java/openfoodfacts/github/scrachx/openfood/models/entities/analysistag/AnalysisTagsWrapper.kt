package openfoodfacts.github.scrachx.openfood.models.entities.analysistag

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import openfoodfacts.github.scrachx.openfood.models.entities.EntityResponse
import openfoodfacts.github.scrachx.openfood.models.entities.EntityWrapper

/**
 * JSON from URL https://ssl-api.openfoodfacts.org/data/taxonomies/ingredients_analysis.json
 *
 * @author Rares
 */
@JsonDeserialize(using = AnalysisTagsWrapperDeserializer::class)
class AnalysisTagsWrapper(analysisTags: List<EntityResponse<AnalysisTag>>) : EntityWrapper<AnalysisTag>(analysisTags)