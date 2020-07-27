package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import openfoodfacts.github.scrachx.openfood.models.entities.country.CountriesWrapper;
import openfoodfacts.github.scrachx.openfood.models.entities.country.Country;
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryName;
import openfoodfacts.github.scrachx.openfood.models.entities.country.CountryResponse;

import static junit.framework.Assert.assertEquals;
import static openfoodfacts.github.scrachx.openfood.models.CountryNameTestData.GERMANY_EN;
import static openfoodfacts.github.scrachx.openfood.models.CountryNameTestData.GERMANY_FR;
import static openfoodfacts.github.scrachx.openfood.models.CountryNameTestData.USA_EN;
import static openfoodfacts.github.scrachx.openfood.models.CountryNameTestData.USA_FR;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH;

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
        CountryResponse countryResponse1 =
            new CountryResponse(USA_COUNTRY_TAG, usaNamesMap, country_code_2, country_code_3);
        Map<String, String> germanyNamesMap = new HashMap<>();
        germanyNamesMap.put(LANGUAGE_CODE_ENGLISH, GERMANY_EN);
        germanyNamesMap.put(LANGUAGE_CODE_FRENCH, GERMANY_FR);
        CountryResponse countryResponse2 =
            new CountryResponse(GERMANY_COUNTRY_TAG, germanyNamesMap, country_code_2, country_code_3);
        mCountriesWrapper.setResponses(Arrays.asList(countryResponse1, countryResponse2));
        countries = mCountriesWrapper.map();

        country1 = countries.get(0);
        country2 = countries.get(1);
    }

    @Test
    public void map_returnsListOfCountries_ListHasCorrectSize(){
        assertEquals(2, countries.size());
    }

    @Test
    public void map_returnsListOfCountries_CountryTagsAreCorrect(){
        assertEquals(USA_COUNTRY_TAG, country1.getTag());
        assertEquals(2, country1.getNames().size());

        Country country2 = countries.get(1);
        assertEquals(GERMANY_COUNTRY_TAG, country2.getTag());
        assertEquals(2, country2.getNames().size());
    }

    @Test
    public void map_returnsListOfCountries_SubCountryTagsAreCorrect() {
        CountryName country1Name1 = country1.getNames().get(0);
        assertEquals(USA_COUNTRY_TAG, country1Name1.getCountyTag());

        CountryName country1Name2 = country1.getNames().get(1);
        assertEquals(USA_COUNTRY_TAG, country1Name2.getCountyTag());

        CountryName country2Name1 = country2.getNames().get(0);
        assertEquals(GERMANY_COUNTRY_TAG, country2Name1.getCountyTag());

        CountryName country2Name2 = country2.getNames().get(1);
        assertEquals(GERMANY_COUNTRY_TAG, country2Name2.getCountyTag());
    }


    @Test
    public void map_returnsListOfCountries_SubLanguageCodesAreCorrect() {
        CountryName country1Name1 = country1.getNames().get(0);
        assertEquals(LANGUAGE_CODE_ENGLISH, country1Name1.getLanguageCode());

        CountryName country1Name2 = country1.getNames().get(1);
        assertEquals(LANGUAGE_CODE_FRENCH, country1Name2.getLanguageCode());

        CountryName country2Name1 = country2.getNames().get(0);
        assertEquals(LANGUAGE_CODE_ENGLISH, country2Name1.getLanguageCode());

        CountryName country2Name2 = country2.getNames().get(1);
        assertEquals(LANGUAGE_CODE_FRENCH, country2Name2.getLanguageCode());
    }

    @Test
    public void map_returnsListOfCountries_SubNamesAreCorrect(){
        CountryName country1Name1 = country1.getNames().get(0);
        assertEquals(USA_EN, country1Name1.getName());

        CountryName country1Name2 = country1.getNames().get(1);
        assertEquals(USA_FR, country1Name2.getName());

        CountryName country2Name1 = country2.getNames().get(0);
        assertEquals(GERMANY_EN, country2Name1.getName());

        CountryName country2Name2 = country2.getNames().get(1);
        assertEquals(GERMANY_FR, country2Name2.getName());
    }
}
