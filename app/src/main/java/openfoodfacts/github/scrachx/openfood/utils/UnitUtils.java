package openfoodfacts.github.scrachx.openfood.utils;

import androidx.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final double SALT_PER_SODIUM = 2.54;
    private static final float KCAL_KJ_RATIO = 0.23900573614f;
    private static final float OZ_PER_L = 33.814f;

    /**
     * Converts a give quantity's unit to kcal
     *
     * @param value The value to be converted
     * @param energyUnit {@link #ENERGY_KCAL} or {@link #ENERGY_KJ}
     * @return return the converted value
     */
    public static float convertToKiloCalories(float value, String energyUnit) {
        if (ENERGY_KJ.equalsIgnoreCase(energyUnit)) {
            return (value * KCAL_KJ_RATIO);
        } else if (ENERGY_KCAL.equalsIgnoreCase(energyUnit)) {
            return value;
        } else {
            throw new IllegalArgumentException("energyUnit is neither ENERGY_KCAL nor ENERGY_KJ");
        }
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
        if (UNIT_MILLILITRE.equalsIgnoreCase(unitOfValue)) {
            return value;
        }
        //TODO : what about % DV and IU
        return value;
    }

    public static float convertFromGram(float valueInGramOrMl, String targetUnit) {
        return (float) convertFromGram((double) valueInGramOrMl, targetUnit);
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
        return saltValue / SALT_PER_SODIUM;
    }

    public static double sodiumToSalt(Double sodiumValue) {
        return sodiumValue * SALT_PER_SODIUM;
    }

    /**
     * Function which returns volume in oz if parameter is in cl, ml, or l
     *
     * @param servingSize value to transform
     * @return volume in oz if servingSize is a volume parameter else return the the parameter unchanged
     */
    public static String getServingInOz(String servingSize) {

        Pattern regex = Pattern.compile("(\\d+(?:\\.\\d+)?)");
        Matcher matcher = regex.matcher(servingSize);
        if (servingSize.toLowerCase().contains("ml")) {
            matcher.find();
            Float val = Float.parseFloat(matcher.group(1));
            val *= (OZ_PER_L / 1000);
            servingSize = Utils.getRoundNumber(val).concat(" oz");
        } else if (servingSize.toLowerCase().contains("cl")) {
            matcher.find();
            Float val = Float.parseFloat(matcher.group(1));
            val *= (OZ_PER_L / 100);
            servingSize = Utils.getRoundNumber(val).concat(" oz");
        } else if (servingSize.toLowerCase().contains("l")) {
            matcher.find();
            Float val = Float.parseFloat(matcher.group(1));
            val *= OZ_PER_L;
            servingSize = Utils.getRoundNumber(val).concat(" oz");
        }
        return servingSize;
        //TODO: HANDLE OTHER CASES, NOT L NOR OZ NOR ML NOR CL
    }

    /**
     * Function that returns the volume in liters if input parameter is in oz
     *
     * @param servingSize the value to transform: not null
     * @return volume in liter if input parameter is a volume parameter else return the parameter unchanged
     */
    public static String getServingInL(@NonNull String servingSize) {

        if (servingSize.toLowerCase().contains("oz")) {
            Pattern regex = Pattern.compile("(\\d+(?:\\.\\d+)?)");
            Matcher matcher = regex.matcher(servingSize);
            matcher.find();
            float val = Float.parseFloat(matcher.group(1));
            val /= OZ_PER_L;
            servingSize = Float.toString(val).concat(" l");
        }

        // TODO: HANDLE OTHER CASES eg. not in L nor oz

        return servingSize;
    }
}
