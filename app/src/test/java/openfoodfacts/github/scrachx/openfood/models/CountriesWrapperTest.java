package openfoodfacts.github.scrachx.openfood.models;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static openfoodfacts.github.scrachx.openfood.models.CountryNameTestData.*;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_ENGLISH;
import static openfoodfacts.github.scrachx.openfood.models.LanguageCodeTestData.LANGUAGE_CODE_FRENCH;

/**
 * Tests for {@link CountriesWrapper}
 */
public class CountriesWrapperTest {

    private static final String USA_COUNTRY_TAG = "usa";
    private static final String GERMANY_COUNTRY_TAG = "germany";

    private CountriesWrapper mCountriesWrapper;

    @Before
    public void setup() {
        mCountriesWrapper = new CountriesWrapper();
        Map<String, String> usaNamesMap = new HashMap<>();
        usaNamesMap.put(LANGUAGE_CODE_ENGLISH, USA_EN);
        usaNamesMap.put(LANGUAGE_CODE_FRENCH, USA_FR);
        CountryResponse countryResponse1 =
                new CountryResponse(USA_COUNTRY_TAG, usaNamesMap);
        Map<String, String> germanyNamesMap = new HashMap<>();
        germanyNamesMap.put(LANGUAGE_CODE_ENGLISH, GERMANY_EN);
        germanyNamesMap.put(LANGUAGE_CODE_FRENCH, GERMANY_FR);
        CountryResponse countryResponse2 =
                new CountryResponse(GERMANY_COUNTRY_TAG, germanyNamesMap);
        mCountriesWrapper.setCountries(Arrays.asList(countryResponse1, countryResponse2));
    }

    @Test
    public void map_returnsListOfCountries() {
        List<Country> countries = mCountriesWrapper.map();

        assertEquals(2, countries.size());

        Country country1 = countries.get(0);
        assertEquals(USA_COUNTRY_TAG, country1.getTag());
        assertEquals(2, country1.getNames().size());

        CountryName country1Name1 = country1.getNames().get(0);
        assertEquals(USA_COUNTRY_TAG, country1Name1.getCountyTag());
        assertEquals(LANGUAGE_CODE_ENGLISH, country1Name1.getLanguageCode());
        assertEquals(USA_EN, country1Name1.getName());

        CountryName country1Name2 = country1.getNames().get(1);
        assertEquals(USA_COUNTRY_TAG, country1Name2.getCountyTag());
        assertEquals(LANGUAGE_CODE_FRENCH, country1Name2.getLanguageCode());
        assertEquals(USA_FR, country1Name2.getName());

        Country country2 = countries.get(1);
        assertEquals(GERMANY_COUNTRY_TAG, country2.getTag());
        assertEquals(2, country2.getNames().size());

        CountryName country2Name1 = country2.getNames().get(0);
        assertEquals(GERMANY_COUNTRY_TAG, country2Name1.getCountyTag());
        assertEquals(LANGUAGE_CODE_ENGLISH, country2Name1.getLanguageCode());
        assertEquals(GERMANY_EN, country2Name1.getName());

        CountryName country2Name2 = country2.getNames().get(1);
        assertEquals(GERMANY_COUNTRY_TAG, country2Name2.getCountyTag());
        assertEquals(LANGUAGE_CODE_FRENCH, country2Name2.getLanguageCode());
        assertEquals(GERMANY_FR, country2Name2.getName());
    }
}
