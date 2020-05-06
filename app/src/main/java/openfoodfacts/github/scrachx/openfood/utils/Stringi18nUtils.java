package openfoodfacts.github.scrachx.openfood.utils;

import java.text.Collator;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by Raymond Chenon on 05/05/20.
 * String Internationalization (i18n)
 */
public class Stringi18nUtils {
    /**
     * Compare the strings ignoring the accents on letters ([a-z][A-Z])
     * Sort string by ignoring accents on letters. The sorted strings will keep their accents.
     *
     * @param arrays
     */
    public static void sortAlphabetically(String[] arrays) {
        Arrays.sort(arrays, Collator.getInstance(Locale.US));
    }

    /**
     * remove diacritical marks (accents) in the string
     *
     * @param string ex: Ácido
     * @return string without accent ex: Acido
     */
    static String removeAccents(String string) {
        return Normalizer.normalize(string, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
