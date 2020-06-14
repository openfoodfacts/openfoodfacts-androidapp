package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.network.deserializers.AnalysisTagsWrapperDeserializer;

/**
 * JSON from URL https://ssl-api.openfoodfacts.org/data/taxonomies/ingredients_analysis.json
 *
 * @author Rares
 */
@JsonDeserialize(using = AnalysisTagsWrapperDeserializer.class)
public class AnalysisTagsWrapper {
    private List<AnalysisTagResponse> analysisTags;

    /**
     * @return A list of AnalysisTag objects
     */
    public List<AnalysisTag> map() {
        List<AnalysisTag> entityAnalysis = new ArrayList<>();
        for (AnalysisTagResponse analysisTag : analysisTags) {
            entityAnalysis.add(analysisTag.map());
        }

        return entityAnalysis;
    }

    public void setAnalysisTags(List<AnalysisTagResponse> analysisTags) {
        this.analysisTags = analysisTags;
    }

    public List<AnalysisTagResponse> getAnalysisTags() {
        return analysisTags;
    }
}
