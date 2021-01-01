package openfoodfacts.github.scrachx.openfood.models.entities.analysistag

import openfoodfacts.github.scrachx.openfood.models.entities.EntityResponse

/**
 * Intermediate class between [AnalysisTagsWrapper] and [AnalysisTag]
 *
 * @author Rares
 */
class AnalysisTagResponse(
        private val uniqueAnalysisTagID: String,
        private val namesMap: Map<String, String>,
        private val showIngredientsMap: Map<String, String>
) : EntityResponse<AnalysisTag> {
    /**
     * Converts an AnalysisTagResponse object into a new AnalysisTag object.
     *
     * @return The newly constructed AnalysisTag object.
     */
    override fun map(): AnalysisTag {
        val analysisTag = AnalysisTag(uniqueAnalysisTagID, arrayListOf())
        namesMap.forEach { (key, value) ->
            var showIngredients = showIngredientsMap[key]
            if (showIngredients == null) {
                showIngredients = showIngredientsMap[DEFAULT_LANGUAGE]
            }
            analysisTag.names.add(AnalysisTagName(analysisTag.tag, key, value, showIngredients))
        }
        return analysisTag
    }

    companion object {
        private const val DEFAULT_LANGUAGE = "en"
    }
}