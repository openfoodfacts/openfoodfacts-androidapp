package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static openfoodfacts.github.scrachx.openfood.models.CountryNameTestData.GERMANY_EN;
import static openfoodfacts.github.scrachx.openfood.models.CountryNameTestData.GERMANY_FR;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH;

/**
 * Tests for {@link CountryResponse}
 */
public class CountryResponseTest {

    private static final String COUNTRY_TAG = "tag";

    private static final Map<String, String> NAMES_MAP = new HashMap<>();

    private CountryResponse mCountryResponse;

    private Country country;

    @Before
    public void setup() {
        NAMES_MAP.put(LANGUAGE_CODE_ENGLISH, GERMANY_EN);
        NAMES_MAP.put(LANGUAGE_CODE_FRENCH, GERMANY_FR);
        mCountryResponse = new CountryResponse(COUNTRY_TAG, NAMES_MAP);
        country = mCountryResponse.map();
    }

    @Test
    public void map_returnsCorrectTag() {
        assertEquals(COUNTRY_TAG, country.getTag());
    }

    @Test
    public void map_returnsCorrectSize() {
        assertEquals(2, country.getNames().size());
    }

    @Test
    public void map_returnsFirstCountryWithMappedNames() {
        CountryName countryName1 = country.getNames().get(0);
        assertEquals(COUNTRY_TAG, countryName1.getCountyTag());
        assertEquals(LANGUAGE_CODE_ENGLISH, countryName1.getLanguageCode());
        assertEquals(GERMANY_EN, countryName1.getName());
    }

    @Test
    public void map_returnsSecondCountryWithMappedNames() {
        CountryName countryName2 = country.getNames().get(1);
        assertEquals(COUNTRY_TAG, countryName2.getCountyTag());
        assertEquals(LANGUAGE_CODE_FRENCH, countryName2.getLanguageCode());
        assertEquals(GERMANY_FR, countryName2.getName());
    }
}
