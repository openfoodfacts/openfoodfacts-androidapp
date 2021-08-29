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

        analysisTag.names += namesMap.map { (lc, name) ->
            val showIngredients = showIngredientsMap[lc] ?: showIngredientsMap[DEFAULT_LANGUAGE]
            AnalysisTagName(analysisTag.tag, lc, name, showIngredients)
        }
        return analysisTag
    }

    companion object {
        private const val DEFAULT_LANGUAGE = "en"
    }
}