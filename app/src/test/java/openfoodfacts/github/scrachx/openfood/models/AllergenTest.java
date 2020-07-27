package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import openfoodfacts.github.scrachx.openfood.models.entities.allergen.Allergen;
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergenName;
import openfoodfacts.github.scrachx.openfood.models.entities.allergen.AllergensWrapper;

import static org.junit.Assert.*;

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
        assertEquals(2, allergensWrapper.getAllergens().size());
    }

    @Test
    public void allergensWrapper_AllergenAreCorrectlyTagged() {
        Allergen allergen = allergens.get(0);

        assertEquals("en:lupin", allergen.getTag());
        assertFalse(allergen.getEnabled());
        assertEquals(9, allergen.getNames().size());
    }

    @Test
    public void allergensWrapper_SubElementsAreCorrectlyTagged(){
        Allergen allergen = allergens.get(0);
        AllergenName allergenName = allergen.getNames().get(0);

        assertEquals(allergen.getTag(), allergenName.getAllergenTag());
        assertEquals("de", allergenName.getLanguageCode());
        assertEquals("Lupinen", allergenName.getName());
    }

    private AllergensWrapper deserialize(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, AllergensWrapper.class);
    }
}