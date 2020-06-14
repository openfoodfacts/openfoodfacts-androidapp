package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.greenrobot.greendao.DaoException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
        assertEquals(allergensWrapper.getAllergens().size(), 2);
    }

    @Test
    public void allergensWrapper_AllergenAreCorrectlyTagged() {
        Allergen allergen = allergens.get(0);

        assertEquals(allergen.getTag(), "en:lupin");
        assertFalse(allergen.getEnabled());
        assertEquals(allergen.getNames().size(), 9);
    }

    @Test
    public void allergensWrapper_SubElementsAreCorrectlyTagged(){
        Allergen allergen = allergens.get(0);
        AllergenName allergenName = allergen.getNames().get(0);

        assertEquals(allergenName.getAllergenTag(), allergen.getTag());
        assertEquals(allergenName.getLanguageCode(), "de");
        assertEquals(allergenName.getName(), "Lupinen");
    }

    private AllergensWrapper deserialize(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, AllergensWrapper.class);
    }
}