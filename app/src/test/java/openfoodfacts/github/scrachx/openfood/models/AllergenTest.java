package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AllergenTest {

    @Test
    public void deserialization() throws IOException {
        String json = "{\"en:lupin\":" +
                "{\"name\":" +
                "{\"es\":\"Altramuces\"," +
                "\"nl\":\"Lupine\"," +
                "\"et\":\"Lupiin\"," +
                "\"de\":\"Lupinen\"," +
                "\"fi\":\"Lupiinit\"," +
                "\"sv\":\"Lupin\"," +
                "\"lt\":\"Lubinai\"," +
                "\"mt\":\"Lupina\"," +
                "\"da\":\"Lupin\"}}," +
                "\"en:molluscs\":" +
                "{\"name\":" +
                "{\"it\":\"Molluschi\"," +
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
                "\"en\":\"Molluscs\"}}" +
                "}";

        AllergensWrapper allergensWrapper = deserialize(json);
        assertEquals(allergensWrapper.getAllergens().size(), 2);

        List<Allergen> allergens = allergensWrapper.map();
        assertEquals(allergens.size(), 2);

        Allergen allergen = allergens.get(0);
        assertEquals(allergen.getTag(), "en:lupin");
        assertFalse(allergen.getEnabled());
        assertEquals(allergen.getNames().size(), 9);

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