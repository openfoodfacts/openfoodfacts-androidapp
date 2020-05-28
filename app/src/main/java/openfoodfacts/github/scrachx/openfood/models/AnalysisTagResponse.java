package openfoodfacts.github.scrachx.openfood.models;

import java.util.ArrayList;
import java.util.Map;

/**
 * Intermediate class between {@link AnalysisTagsWrapper} and {@link AnalysisTag}
 *
 * @author Rares
 */
public class AnalysisTagResponse {
    private static final String DEFAULT_LANGUAGE = "en";
    private final Map<String, String> namesMap;
    private final Map<String, String> showIngredientsMap;
    private final String uniqueAnalysisTagID;

    /**
     * Constructor.
     *
     * @param uniqueAnalysisTagId
     * @param namesMap
     * @param showIngredientsMap
     */
    public AnalysisTagResponse(String uniqueAnalysisTagId, Map<String, String> namesMap, Map<String, String> showIngredientsMap) {
        this.uniqueAnalysisTagID = uniqueAnalysisTagId;
        this.namesMap = namesMap;
        this.showIngredientsMap = showIngredientsMap;
    }

    /**
     * Converts an AnalysisTagResponse object into a new AnalysisTag object.
     *
     * @return The newly constructed AnalysisTag object.
     */
    public AnalysisTag map() {
        AnalysisTag analysisTag;
        analysisTag = new AnalysisTag(uniqueAnalysisTagID, new ArrayList<>());
        for (Map.Entry<String, String> name : namesMap.entrySet()) {
            String showIngredients = showIngredientsMap.get(name.getKey());
            if (showIngredients == null) {
                showIngredients = showIngredientsMap.get(DEFAULT_LANGUAGE);
            }
            analysisTag.getNames()
                .add(new AnalysisTagName(analysisTag.getTag(), name.getKey(), name.getValue(), showIngredients));
        }

        return analysisTag;
    }
}
