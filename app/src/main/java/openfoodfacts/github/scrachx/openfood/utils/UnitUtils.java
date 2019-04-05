package openfoodfacts.github.scrachx.openfood.utils;

public class UnitUtils {
    public static final String ENERGY_KJ = "kj";
    public static final String ENERGY_KCAL = "kcal";
    public static final String UNIT_KILOGRAM = "kg";
    public static final String UNIT_GRAM = "g";
    public static final String UNIT_MILLIGRAM = "mg";
    public static final String UNIT_MICROGRAM = "µg";
    public static final String UNIT_DV = "% DV";
    public static final String UNIT_LITER = "l";
    public static final String UNIT_DECILITRE = "dl";
    public static final String UNIT_CENTILITRE = "cl";
    public static final String UNIT_MILLILITRE = "ml";
    public static final String UNIT_IU = "IU";

    /**
     * Converts a give quantity's unit to kcal
     *
     * @param a The value to be converted
     * @param energyUnit {@link #ENERGY_KCAL} or {@link #ENERGY_KJ}
     * @return return the converted value
     */
    public static float convertToKiloCalories(float a, String energyUnit) {
        if (ENERGY_KJ.equalsIgnoreCase(energyUnit)) {
            a *= 0.23900573614f;
        }
        return a;
    }

    public static float convertToGrams(float a, String unit) {
        return (float) convertToGrams((double) a, unit);
    }

    /**
     * Converts a given quantity's unitOfValue to grams.
     *
     * @param value The value to be converted
     * @param unitOfValue represents milligrams, 2 represents micrograms
     * @return return the converted value
     */
    public static double convertToGrams(double value, String unitOfValue) {
        if (UNIT_MILLIGRAM.equalsIgnoreCase(unitOfValue)) {
            return value / 1000;
        }
        if (UNIT_MICROGRAM.equalsIgnoreCase(unitOfValue)) {
            return value / 1000000;
        }
        if (UNIT_KILOGRAM.equalsIgnoreCase(unitOfValue)) {
            return value * 1000;
        }
        if (UNIT_LITER.equalsIgnoreCase(unitOfValue)) {
            return value * 1000;
        }
        if (UNIT_DECILITRE.equalsIgnoreCase(unitOfValue)) {
            return value * 100;
        }
        if (UNIT_CENTILITRE.equalsIgnoreCase(unitOfValue)) {
            return value * 10;
        }
        //TODO : what about % DV and IU
        return value;
    }

    public static float convertFromGram(float valueInGramOrMl, String targetUnit) {
        return (float) convertFromGram((double)valueInGramOrMl, targetUnit);
    }

    public static double convertFromGram(double valueInGramOrMl, String targetUnit) {
        if (targetUnit.equals(UNIT_KILOGRAM)) {
            return valueInGramOrMl / 1000;
        } else if (targetUnit.equals(UNIT_MILLIGRAM)) {
            return valueInGramOrMl * 1000;
        } else if (targetUnit.equals(UNIT_MICROGRAM)) {
            return valueInGramOrMl * 1000000;
        } else if (targetUnit.equals(UNIT_LITER)) {
            return valueInGramOrMl / 1000;
        } else if (targetUnit.equals(UNIT_DECILITRE)) {
            return valueInGramOrMl / 100;
        } else if (targetUnit.equals(UNIT_CENTILITRE)) {
            return valueInGramOrMl / 10;
        }
        return valueInGramOrMl;
    }

    public static double saltToSodium(Double saltValue) {
        return saltValue * 0.39370078740157477;
    }

    public static double sodiumToSalt(Double sodiumValue) {
        return sodiumValue * 2.54;
    }
}
