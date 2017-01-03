package openfoodfacts.github.scrachx.openfood.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by n27 on 12/22/16.
 */
public class NutrimentsTest {

    @Test
    public void nutriments_deserialization_ok() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree("{" +
                "\"sugars_serving\": 4.2," +
                "\"sugars_100g\": 2.8," +
                "\"sugars\": 4.2," +
                "\"sugars_value\": \"4.2\"," +
                "\"sugars_unit\": \"g\"," +

                "\"carbohydrates\": \"42\"," +
                "\"carbohydrates_value\": \"42\"," +
                "\"carbohydrates_unit\": \"g\"," +
                "\"carbohydrates_100g\": \"28\"," +
                "\"carbohydrates_serving\": \"42\"," +

                "\"salt_100g\": 1.6," +
                "\"salt\": 2.4," +
                "\"salt_serving\": 2.4," +
                "\"salt_unit\": \"g\"," +
                "\"salt_value\": \"2.4\"," +

                "\"saturated-fat_value\": \"9.6\"," +
                "\"saturated-fat\": 9.6," +
                "\"saturated-fat_unit\": \"g\"," +
                "\"saturated-fat_serving\": 9.6," +
                "\"saturated-fat_100g\": 6.4," +

                "\"fat\": \"23\"," +
                "\"fat_serving\": \"23\"," +
                "\"fat_100g\": 15.3," +
                "\"fat_unit\": \"g\"," +
                "\"fat_value\": \"23\"," +

                "\"proteins_unit\": \"g\"," +
                "\"proteins_serving\": \"17\"," +
                "\"proteins\": \"17\"," +
                "\"proteins_100g\": 11.3," +
                "\"proteins_value\": \"17\"," +

                "\"energy_serving\": \"1854\"," +
                "\"energy\": \"1854\"," +
                "\"energy_100g\": \"1240\"," +
                "\"energy_unit\": \"kJ\"," +
                "\"energy_value\": \"1854\"," +

                "\"nutrition-score-fr\": \"15\"," +
                "\"nutrition-score-fr_100g\": \"15\"," +
                "\"nutrition-score-uk_100g\": \"15\"," +
                "\"nutrition-score-uk\": \"15\"," +

                "\"sodium_value\": \"0.9448818897637794\"," +
                "\"sodium_100g\": 0.63," +
                "\"sodium\": 0.944881889763779," +
                "\"sodium_serving\": 0.944881889763779," +
                "\"sodium_unit\": \"g\"" +
                "}");

        Nutriments nutriments = objectMapper.treeToValue(json, Nutriments.class);

        assertEquals(nutriments.getSugarsServing(), "4.2");
        assertEquals(nutriments.getSugarsValue(), "4.2");
        assertEquals(nutriments.getSugars(), "4.2");
        assertEquals(nutriments.getSugarsUnit(), "g");
        assertEquals(nutriments.getSugars100g(), "2.8");

        assertEquals(nutriments.getCarbohydrates(), "42");
        assertEquals(nutriments.getCarbohydratesServing(), "42");
        assertEquals(nutriments.getCarbohydratesValue(), "42");
        assertEquals(nutriments.getCarbohydrates100g(), "28");
        assertEquals(nutriments.getCarbohydratesUnit(), "g");

        assertEquals(nutriments.getSalt100g(), "1.6");
        assertEquals(nutriments.getSalt(), "2.4");
        assertEquals(nutriments.getSaltServing(), "2.4");
        assertEquals(nutriments.getSaltUnit(), "g");

        assertEquals(nutriments.getSaturatedFat(), "9.6");
        assertEquals(nutriments.getSaturatedFatServing(), "9.6");
        assertEquals(nutriments.getSaturatedFatValue(), "9.6");
        assertEquals(nutriments.getSaturatedFat100g(), "6.4");
        assertEquals(nutriments.getSaturatedFatUnit(), "g");

        assertEquals(nutriments.getFat100g(), "15.3");
        assertEquals(nutriments.getFat(), "23");
        assertEquals(nutriments.getFatValue(), "23");
        assertEquals(nutriments.getFatServing(), "23");
        assertEquals(nutriments.getFatUnit(), "g");

        assertEquals(nutriments.getProteins(), "17");
        assertEquals(nutriments.getProteinsValue(), "17");
        assertEquals(nutriments.getProteinsServing(), "17");
        assertEquals(nutriments.getProteins100g(), "11.3");
        assertEquals(nutriments.getProteinsUnit(), "g");

        assertEquals(nutriments.getEnergy(), "1854");
        assertEquals(nutriments.getEnergyServing(), "1854");
        assertEquals(nutriments.getEnergyValue(), "1854");
        assertEquals(nutriments.getEnergy100g(), "1240");
        assertEquals(nutriments.getEnergyUnit(), "kJ");

        String score = "15";
        assertEquals(nutriments.getNutritionScoreFr(), score);
        assertEquals(nutriments.getNutritionScoreFr100g(), score);
        assertEquals(nutriments.getNutritionScoreUk(), score);
        assertEquals(nutriments.getNutritionScoreUk100g(), score);

        assertEquals(nutriments.getSodiumValue(), "0.9448818897637794");
        assertEquals(nutriments.getSodium(), "0.944881889763779");
        assertEquals(nutriments.getSodiumServing(), "0.944881889763779");
        assertEquals(nutriments.getSodium100g(), "0.63");
        assertEquals(nutriments.getSodiumUnit(), "g");
    }
}