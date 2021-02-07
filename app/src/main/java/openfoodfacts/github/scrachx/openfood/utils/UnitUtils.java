package openfoodfacts.github.scrachx.openfood.utils;

import androidx.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import openfoodfacts.github.scrachx.openfood.models.Units;

public class UnitUtils {
    private UnitUtils() {
        // Utility class
    }

    public static final String UNIT_IU = "IU";
    private static final double SALT_PER_SODIUM = 2.54;
    private static final float KJ_PER_KCAL = 4.184f;
    private static final float OZ_PER_L = 33.814f;

    /**
     * Converts a give quantity's unit to kcal
     *
     * @param value The value to be converted
     * @param originalUnit {@link Units#ENERGY_KCAL} or {@link Units#ENERGY_KJ}
     * @return return the converted value
     */
    public static int convertToKiloCalories(int value, String originalUnit) {
        if (originalUnit.equalsIgnoreCase(Units.ENERGY_KJ)) {
            return (int) (value / KJ_PER_KCAL);
        } else if (originalUnit.equalsIgnoreCase(Units.ENERGY_KCAL)) {
            return value;
        } else {
            throw new IllegalArgumentException("energyUnit is neither Units.ENERGY_KCAL nor Units.ENERGY_KJ");
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
        if (Units.UNIT_MILLIGRAM.equalsIgnoreCase(unitOfValue)) {
            return value / 1000;
        }
        if (Units.UNIT_MICROGRAM.equalsIgnoreCase(unitOfValue)) {
            return value / 1000000;
        }
        if (Units.UNIT_KILOGRAM.equalsIgnoreCase(unitOfValue)) {
            return value * 1000;
        }
        if (Units.UNIT_LITER.equalsIgnoreCase(unitOfValue)) {
            return value * 1000;
        }
        if (Units.UNIT_DECILITRE.equalsIgnoreCase(unitOfValue)) {
            return value * 100;
        }
        if (Units.UNIT_CENTILITRE.equalsIgnoreCase(unitOfValue)) {
            return value * 10;
        }
        if (Units.UNIT_MILLILITRE.equalsIgnoreCase(unitOfValue)) {
            return value;
        }
        //TODO : what about % DV and IU
        return value;
    }

    public static float convertFromGram(float valueInGramOrMl, String targetUnit) {
        return (float) convertFromGram((double) valueInGramOrMl, targetUnit);
    }

    public static double convertFromGram(double valueInGramOrMl, String targetUnit) {
        switch (targetUnit) {
            case Units.UNIT_KILOGRAM:
            case Units.UNIT_LITER:
                return valueInGramOrMl / 1000;
            case Units.UNIT_MILLIGRAM:
                return valueInGramOrMl * 1000;
            case Units.UNIT_MICROGRAM:
                return valueInGramOrMl * 1000000;
            case Units.UNIT_DECILITRE:
                return valueInGramOrMl / 100;
            case Units.UNIT_CENTILITRE:
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
            float val = Float.parseFloat(matcher.group(1));
            val *= (OZ_PER_L / 1000);
            servingSize = Utils.getRoundNumber(val).concat(" oz");
        } else if (servingSize.toLowerCase().contains("cl")) {
            matcher.find();
            float val = Float.parseFloat(matcher.group(1));
            val *= (OZ_PER_L / 100);
            servingSize = Utils.getRoundNumber(val).concat(" oz");
        } else if (servingSize.toLowerCase().contains("l")) {
            matcher.find();
            float val = Float.parseFloat(matcher.group(1));
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
