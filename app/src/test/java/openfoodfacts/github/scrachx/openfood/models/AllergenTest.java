package openfoodfacts.github.scrachx.openfood.models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Test;

import java.util.List;

import openfoodfacts.github.scrachx.openfood.network.deserializers.AllergensWrapperDeserializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AllergenTest {

    @Test
    public void deserialization() {
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

    private AllergensWrapper deserialize(String json) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(AllergensWrapper.class, new AllergensWrapperDeserializer());
        Gson gson = gsonBuilder.create();

        return gson.fromJson(json, AllergensWrapper.class);
    }

}
