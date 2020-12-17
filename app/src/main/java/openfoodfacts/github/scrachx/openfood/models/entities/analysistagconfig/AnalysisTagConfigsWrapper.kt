package openfoodfacts.github.scrachx.openfood.models.entities.analysistagconfig

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

/**
 * JSON from URL https://ssl-api.openfoodfacts.org/files/app/ingredients-analysis.json
 *
 * @author Rares
 */
@JsonDeserialize(using = AnalysisTagConfigsWrapperDeserializer::class)
class AnalysisTagConfigsWrapper(var analysisTagConfigs: List<AnalysisTagConfig>) {

    /**
     * @return A list of AnalysisTagConfig objects
     */
    fun map() = analysisTagConfigs.toList()
}