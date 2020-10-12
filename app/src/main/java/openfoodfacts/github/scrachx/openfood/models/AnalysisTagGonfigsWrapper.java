package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.network.deserializers.AnalysisTagConfigsWrapperDeserializer;

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
        return new ArrayList<>(analysisTagConfigs);
    }

    public void setAnalysisTagConfigs(List<AnalysisTagConfig> analysisTags) {
        this.analysisTagConfigs = analysisTags;
    }

    public List<AnalysisTagConfig> getAnalysisTagConfigs() {
        return analysisTagConfigs;
    }
}
