package openfoodfacts.github.scrachx.openfood.models;

import java.util.ArrayList;
import java.util.Map;

/**
 * Intermediate class between {@link openfoodfacts.github.scrachx.openfood.models.AllergensWrapper} and {@link openfoodfacts.github.scrachx.openfood.models.Allergen}
 *
 * @author Lobster 2018-03-04
 * @author ross-holloway94 2018-03-14
 */

public class AllergenResponse {

    private String uniqueAllergenID;

    private Map<String, String> names;

    private String wikiDataCode;
    private Boolean isWikiDataIdPresent;

    /**
     * Constructor.
     *
     * @param uniqueAllergenId Unique ID of the allergen
     * @param names            Map of key: Country code, value: Translated name of allergen.
     * @param wikiDataCode     Code to look up allergen in wikidata
     */
    public AllergenResponse(String uniqueAllergenId, Map<String, String> names, String wikiDataCode) {
        this.uniqueAllergenID = uniqueAllergenId;
        this.names = names;
        this.wikiDataCode = wikiDataCode;
        this.isWikiDataIdPresent = true;
    }

    /**
     * Constructor.
     *
     * @param uniqueAllergenId Unique ID of the allergen
     * @param names            Map of key: Country code, value: Translated name of allergen.
     */
    public AllergenResponse(String uniqueAllergenId, Map<String, String> names) {
        this.uniqueAllergenID = uniqueAllergenId;
        this.names = names;
        this.isWikiDataIdPresent = false;
    }

    /**
     * Converts an AllergenResponse object into a new Allergen object.
     *
     * @return The newly constructed Allergen object.
     */
    public Allergen map() {
        Allergen allergen;
        if (isWikiDataIdPresent) {
            allergen = new Allergen(uniqueAllergenID, new ArrayList<>(), wikiDataCode);
            for (Map.Entry<String, String> name : names.entrySet()) {
                allergen.getNames()
                        .add(new AllergenName(allergen.getTag(), name.getKey(), name.getValue(), wikiDataCode));
            }
        } else {
            allergen = new Allergen(uniqueAllergenID, new ArrayList<>());
            for (Map.Entry<String, String> name : names.entrySet()) {
                allergen.getNames()
                        .add(new AllergenName(allergen.getTag(), name.getKey(), name.getValue()));
            }
        }

        return allergen;
    }

    public String getUniqueAllergenID() {
        return uniqueAllergenID;
    }

    public void setUniqueAllergenID(String uniqueAllergenID) {
        this.uniqueAllergenID = uniqueAllergenID;
    }

    public Map<String, String> getNames() {
        return names;
    }

    public void setNames(Map<String, String> names) {
        this.names = names;
    }

}
