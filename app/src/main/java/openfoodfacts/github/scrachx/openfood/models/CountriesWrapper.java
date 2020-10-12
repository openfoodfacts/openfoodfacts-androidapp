package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.network.deserializers.CountriesWrapperDeserializer;

/**
 * Created by Lobster on 04.03.18.
 */

@JsonDeserialize(using = CountriesWrapperDeserializer.class)
public class CountriesWrapper {

    private List<CountryResponse> countries;

    public List<Country> map() {
        List<Country> entityCountries = new ArrayList<>();
        for (CountryResponse country : countries) {
            entityCountries.add(country.map());
        }

        return entityCountries;
    }

    public List<CountryResponse> getCountries() {
        return countries;
    }

    public void setCountries(List<CountryResponse> countries) {
        this.countries = countries;
    }
}
