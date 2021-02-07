package openfoodfacts.github.scrachx.openfood.models.entities.allergen;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class AllergenTest {

    String json;
    AllergensWrapper allergensWrapper;
    List<Allergen> allergens;

    @Before
    public void setUp() throws IOException {
        json = "{\"en:lupin\":" +
                    "{\"name\":" +
                        "{" +
                        "\"es\":\"Altramuces\"," +
                        "\"nl\":\"Lupine\"," +
                        "\"et\":\"Lupiin\"," +
                        "\"de\":\"Lupinen\"," +
                        "\"fi\":\"Lupiinit\"," +
                        "\"sv\":\"Lupin\"," +
                        "\"lt\":\"Lubinai\"," +
                        "\"mt\":\"Lupina\"," +
                        "\"da\":\"Lupin\"" +
                        "}" +
                    "}," +
               "\"en:molluscs\":" +
                    "{\"name\":" +
                        "{" +
                        "\"it\":\"Molluschi\"," +
                        "\"fr\":\"Mollusques\"," +
                        "\"lv\":\"Gliemji\"," +
                        "\"pt\":\"Moluscos\"," +
                        "\"nl\":\"Weekdieren\"," +
                        "\"et\":\"Molluskid\"," +
                        "\"de\":\"Weichtiere\"," +
                        "\"es\":\"Moluscos\"," +
                        "\"ga\":\"Moilisc\"," +
                        "\"mt\":\"Molluski\"," +
                        "\"lt\":\"Moliuskai\"," +
                        "\"en\":\"Molluscs\"" +
                        "}" +
                    "}" +
               "}";

        allergensWrapper = deserialize(json);
        allergens = allergensWrapper.map();
    }

    @Test
    public void deserialization_success(){
        assertThat(allergensWrapper.getAllergens()).hasSize(2);
    }

    @Test
    public void allergensWrapper_AllergenAreCorrectlyTagged() {
        Allergen allergen = allergens.get(0);

        assertThat(allergen.getTag()).isEqualTo("en:lupin");
        assertThat(allergen.getEnabled()).isFalse();
        assertThat(allergen.getNames()).hasSize(9);
    }

    @Test
    public void allergensWrapper_SubElementsAreCorrectlyTagged() {
        Allergen allergen = allergens.get(0);
        AllergenName allergenName = allergen.getNames().get(0);

        assertThat(allergenName.getAllergenTag()).isEqualTo(allergen.getTag());
        assertThat(allergenName.getLanguageCode()).isEqualTo("de");
        assertThat(allergenName.getName()).isEqualTo("Lupinen");
    }

    private AllergensWrapper deserialize(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, AllergensWrapper.class);
    }
}