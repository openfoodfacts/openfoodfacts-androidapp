package openfoodfacts.github.scrachx.openfood.utils;

import java.text.Normalizer;
import java.util.Comparator;

/**
 * Created by Raymond Chenon on 05/05/20.
 */
public class StringComparator implements Comparator<String> {
    @Override
    public int compare(String o1, String o2) {
        return removeDiacriticalMarks(o1).hashCode() - removeDiacriticalMarks(o2).hashCode();
    }

    public static String removeDiacriticalMarks(String string) {
        return Normalizer.normalize(string, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
