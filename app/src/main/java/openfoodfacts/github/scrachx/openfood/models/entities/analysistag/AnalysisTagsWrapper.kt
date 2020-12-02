package openfoodfacts.github.scrachx.openfood.models.entities.analysistag

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

/**
 * JSON from URL https://ssl-api.openfoodfacts.org/data/taxonomies/ingredients_analysis.json
 *
 * @author Rares
 */
@JsonDeserialize(using = AnalysisTagsWrapperDeserializer::class)
class AnalysisTagsWrapper(var analysisTags: List<AnalysisTagResponse>) {

    /**
     * @return A list of AnalysisTag objects
     */
    fun map() = analysisTags.map { it.map() }
}