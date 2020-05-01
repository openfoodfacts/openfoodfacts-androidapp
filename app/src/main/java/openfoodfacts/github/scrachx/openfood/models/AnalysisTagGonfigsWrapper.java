package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.network.deserializers.AnalysisTagConfigsWrapperDeserializer;
import openfoodfacts.github.scrachx.openfood.network.deserializers.AnalysisTagsWrapperDeserializer;

/**
 * JSON from URL https://ssl-api.openfoodfacts.org/files/app/ingredients-analysis.json
 *
 * @author Rares
 */
@JsonDeserialize(using = AnalysisTagConfigsWrapperDeserializer.class)
public class AnalysisTagGonfigsWrapper {
    private List<AnalysisTagConfig> analysisTagConfigs;

    /**
     * @return A list of AnalysisTagConfig objects
     */
    public List<AnalysisTagConfig> map() {
        List<AnalysisTagConfig> entityAnalysis = new ArrayList<>(analysisTagConfigs);

        return entityAnalysis;
    }

    public void setAnalysisTagConfigs(List<AnalysisTagConfig> analysisTags) {
        this.analysisTagConfigs = analysisTags;
    }

    public List<AnalysisTagConfig> getAnalysisTagConfigs() {
        return analysisTagConfigs;
    }
}
