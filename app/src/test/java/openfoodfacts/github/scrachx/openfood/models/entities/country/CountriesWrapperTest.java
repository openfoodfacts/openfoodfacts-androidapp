package openfoodfacts.github.scrachx.openfood.models.entities.country;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH;
import static openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameTestData.GERMANY_EN;
import static openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameTestData.GERMANY_FR;
import static openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameTestData.USA_EN;
import static openfoodfacts.github.scrachx.openfood.models.entities.country.CountryNameTestData.USA_FR;

/**
 * Tests for {@link CountriesWrapper}
 */
public class CountriesWrapperTest {
    private static final String USA_COUNTRY_TAG = "usa";
    private static final String GERMANY_COUNTRY_TAG = "germany";
    private CountriesWrapper mCountriesWrapper;
    List<Country> countries;
    Country country1;
    Country country2;

    @Before
    public void setup() {
        mCountriesWrapper = new CountriesWrapper();
        Map<String, String> usaNamesMap = new HashMap<>();
        usaNamesMap.put(LANGUAGE_CODE_ENGLISH, USA_EN);
        usaNamesMap.put(LANGUAGE_CODE_FRENCH, USA_FR);

        Map<String, String> usaCC2Map = new HashMap<>();
        usaCC2Map.put(LANGUAGE_CODE_ENGLISH, "US");
        Map<String, String> usaCC3Map = new HashMap<>();
        usaCC3Map.put(LANGUAGE_CODE_ENGLISH, "USA");
        CountryResponse countryResponse1 =
            new CountryResponse(USA_COUNTRY_TAG, usaNamesMap, usaCC2Map, usaCC3Map);
        Map<String, String> germanyNamesMap = new HashMap<>();
        germanyNamesMap.put(LANGUAGE_CODE_ENGLISH, GERMANY_EN);
        germanyNamesMap.put(LANGUAGE_CODE_FRENCH, GERMANY_FR);
        Map<String, String> gerCC2Map = new HashMap<>();
        gerCC2Map.put(LANGUAGE_CODE_ENGLISH, "DE");
        Map<String, String> gerCC3Map = new HashMap<>();
        gerCC3Map.put(LANGUAGE_CODE_ENGLISH, "DEU");
        CountryResponse countryResponse2 =
            new CountryResponse(GERMANY_COUNTRY_TAG, germanyNamesMap, gerCC2Map, gerCC3Map);
        mCountriesWrapper.setResponses(Arrays.asList(countryResponse1, countryResponse2));
        countries = mCountriesWrapper.map();

        country1 = countries.get(0);
        country2 = countries.get(1);
    }

    @Test
    public void map_returnsListOfCountries_ListHasCorrectSize() {
        assertThat(countries).hasSize(2);
    }

    @Test
    public void map_returnsListOfCountries_CountryTagsAreCorrect() {
        assertThat(country1.getTag()).isEqualTo(USA_COUNTRY_TAG);
        assertThat(country1.getNames()).hasSize(2);

        Country country2 = countries.get(1);
        assertThat(country2.getTag()).isEqualTo(GERMANY_COUNTRY_TAG);
        assertThat(country2.getNames()).hasSize(2);
    }

    @Test
    public void map_returnsListOfCountries_SubCountryTagsAreCorrect() {
        CountryName country1Name1 = country1.getNames().get(0);
        assertThat(country1Name1.getCountyTag()).isEqualTo(USA_COUNTRY_TAG);

        CountryName country1Name2 = country1.getNames().get(1);
        assertThat(country1Name2.getCountyTag()).isEqualTo(USA_COUNTRY_TAG);

        CountryName country2Name1 = country2.getNames().get(0);
        assertThat(country2Name1.getCountyTag()).isEqualTo(GERMANY_COUNTRY_TAG);

        CountryName country2Name2 = country2.getNames().get(1);
        assertThat(country2Name2.getCountyTag()).isEqualTo(GERMANY_COUNTRY_TAG);
    }

    @Test
    public void map_returnsListOfCountries_SubLanguageCodesAreCorrect() {
        CountryName country1Name1 = country1.getNames().get(0);
        assertThat(country1Name1.getLanguageCode()).isEqualTo(LANGUAGE_CODE_ENGLISH);

        CountryName country1Name2 = country1.getNames().get(1);
        assertThat(country1Name2.getLanguageCode()).isEqualTo(LANGUAGE_CODE_FRENCH);

        CountryName country2Name1 = country2.getNames().get(0);
        assertThat(country2Name1.getLanguageCode()).isEqualTo(LANGUAGE_CODE_ENGLISH);

        CountryName country2Name2 = country2.getNames().get(1);
        assertThat(country2Name2.getLanguageCode()).isEqualTo(LANGUAGE_CODE_FRENCH);
    }

    @Test
    public void map_returnsListOfCountries_SubNamesAreCorrect() {
        CountryName country1Name1 = country1.getNames().get(0);
        assertThat(country1Name1.getName()).isEqualTo(USA_EN);

        CountryName country1Name2 = country1.getNames().get(1);
        assertThat(country1Name2.getName()).isEqualTo(USA_FR);

        CountryName country2Name1 = country2.getNames().get(0);
        assertThat(country2Name1.getName()).isEqualTo(GERMANY_EN);

        CountryName country2Name2 = country2.getNames().get(1);
        assertThat(country2Name2.getName()).isEqualTo(GERMANY_FR);
    }
}
