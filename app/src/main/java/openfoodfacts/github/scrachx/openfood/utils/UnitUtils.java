package openfoodfacts.github.scrachx.openfood.utils;

public class UnitUtils {
    public static final String ENERGY_KJ = "kj";
    public static final String ENERGY_KCAL = "kcal";
    public static final String UNIT_GRAM = "g";
    public static final String UNIT_MILLIGRAM = "mg";
    public static final String UNIT_MICROGRAM = "µg";
    public static final String UNIT_DV = "% DV";
    public static final String UNIT_IU = "IU";

    /**
     * Converts a give quantity's unit to kcal
     *
     * @param a The value to be converted
     * @param energyUnit {@link #ENERGY_KCAL} or {@link #ENERGY_KJ}
     * @return return the converted value
     */
    public static float convertToKiloCalories(float a, String energyUnit) {
        if(ENERGY_KJ.equalsIgnoreCase(energyUnit)) {
            a *= 0.23900573614f;
        }
        return a;
    }

    public static float convertToGrams(float a, String unit) {
        return (float)convertToGrams((double)a,unit);
    }

    /**
     * Converts a given quantity's unit to grams.
     *
     * @param a The value to be converted
     * @param unit represents milligrams, 2 represents micrograms
     * @return return the converted value
     */
    public static double convertToGrams(double a, String unit) {
        if(UNIT_MILLIGRAM.equalsIgnoreCase(unit)){
            return a/1000;
        }
        if(UNIT_MICROGRAM.equalsIgnoreCase(unit)){
            return a/1000000;
        }
        //TODO : what about % DV and IU
        return a;
    }

    public static double saltToSodium(Double saltValue) {
        return saltValue * 0.39370078740157477;
    }

    public static double sodiumToSalt(Double sodiumValue) {
        return sodiumValue * 2.54;
    }
}
