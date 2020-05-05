package openfoodfacts.github.scrachx.openfood.utils;

import java.text.Normalizer;
import java.util.Comparator;

/**
 * Created by Raymond Chenon on 05/05/20.
 *
 * Compare the strings ignoring the accents on letters ([a-z][A-Z])
 */
public class StringComparator implements Comparator<String> {

    @Override
    public int compare(String obj1, String obj2) {

        if (obj1 == obj2) {
            return 0;
        }
        if (obj1 == null) {
            return -1;
        }
        if (obj2 == null) {
            return 1;
        }
        return removeAccents(obj1).compareTo(removeAccents(obj2));
    }

    /**
     * remove diacritical marks (accents) in the string
     * @param string ex: Ácido
     * @return string without accent ex: Acido
     */
    static String removeAccents(String string) {
        return Normalizer.normalize(string, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
