package openfoodfacts.github.scrachx.openfood.models.entities.country;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH;
import static openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameTestData.GERMANY_EN;
import static openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameTestData.GERMANY_FR;

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
        mCountryResponse = new CountryResponse(COUNTRY_TAG, NAMES_MAP, new HashMap<>(), new HashMap<>());
        country = mCountryResponse.map();
    }

    @Test
    public void map_returnsCorrectTag() {
        assertThat(country.getTag()).isEqualTo(COUNTRY_TAG);
    }

    @Test
    public void map_returnsCorrectSize() {
        assertThat(country.getNames()).hasSize(2);
    }

    @Test
    public void map_returnsFirstCountryWithMappedNames() {
        CountryName countryName1 = country.getNames().get(0);
        assertThat(countryName1.getCountyTag()).isEqualTo(COUNTRY_TAG);
        assertThat(countryName1.getLanguageCode()).isEqualTo(LANGUAGE_CODE_ENGLISH);
        assertThat(countryName1.getName()).isEqualTo(GERMANY_EN);
    }

    @Test
    public void map_returnsSecondCountryWithMappedNames() {
        CountryName countryName2 = country.getNames().get(1);
        assertThat(countryName2.getCountyTag()).isEqualTo(COUNTRY_TAG);
        assertThat(countryName2.getLanguageCode()).isEqualTo(LANGUAGE_CODE_FRENCH);
        assertThat(countryName2.getName()).isEqualTo(GERMANY_FR);
    }
}
