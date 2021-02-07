package openfoodfacts.github.scrachx.openfood.models.entities.country;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lobster on 04.03.18.
 */

@JsonDeserialize(using = CountriesWrapperDeserializer.class)
public class CountriesWrapper {
    private List<CountryResponse> responses;

    public List<Country> map() {
        List<Country> entityCountries = new ArrayList<>();
        for (CountryResponse response : responses) {
            entityCountries.add(response.map());
        }

        return entityCountries;
    }

    public void setResponses(List<CountryResponse> responses) {
        this.responses = responses;
    }
}
