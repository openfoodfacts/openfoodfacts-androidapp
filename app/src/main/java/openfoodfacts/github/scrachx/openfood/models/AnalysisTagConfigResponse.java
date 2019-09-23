package openfoodfacts.github.scrachx.openfood.models;

import java.util.List;

/**
 * @author Rares
 */
public class AnalysisTagConfigResponse {
    private List<AnalysisTagConfig> configs;

    /**
     * Constructor.
     *
     * @param configs
     */
    public AnalysisTagConfigResponse(List<AnalysisTagConfig> configs) {
        this.configs = configs;
    }

    public List<AnalysisTagConfig> getConfigs() {
        return configs;
    }

    public void setConfigs(List<AnalysisTagConfig> configs) {
        this.configs = configs;
    }
}
