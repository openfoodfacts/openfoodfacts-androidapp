package openfoodfacts.github.scrachx.openfood.utils;

import org.junit.Assert;
import org.junit.Test;

import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;

public class Stringi18nUtilsTest {
    @Test
    public void testSortWithoutComparator() {
        String[] arr = new String[]{"Ácido araquidónico", "Ácido", "Zinc", "Almidón", "Acido"};
        Arrays.sort(arr);

        String[] expectedResult = new String[]{"Acido", "Almidón", "Zinc", "Ácido", "Ácido araquidónico"};
        Assert.assertArrayEquals(expectedResult, arr);
    }

    @Test
    public void testSpecialCharacterSort_ES() {
        String[] arr = new String[]{"Ácido araquidónico", "Ácido", "Zinc", "Almidón", "Acido"};

        Stringi18nUtils.sortAlphabetically(arr, Collator.getInstance(Locale.US));

        String[] expectedResult = new String[]{"Acido", "Ácido", "Ácido araquidónico", "Almidón", "Zinc"};
        Assert.assertArrayEquals(expectedResult, arr);
    }

    @Test
    public void testSpecialCharacterSort_FR() {
        String[] arr = new String[]{"oléique", "oleique", "acide", "Acide"};

        Stringi18nUtils.sortAlphabetically(arr, Collator.getInstance(Locale.FRENCH));

        String[] expectedResult = new String[]{"acide", "Acide", "oleique", "oléique"};
        Assert.assertArrayEquals(expectedResult, arr);
    }

    @Test
    public void testSpecialCharacterSort_FR2() {
        String[] arr = new String[]{"Molybdène", "manganèse", "molybdene", "Manganèse"};

        Stringi18nUtils.sortAlphabetically(arr, Collator.getInstance(Locale.FRENCH));

        // upper case affects the order
        String[] expectedResult = new String[]{"manganèse", "Manganèse", "molybdene", "Molybdène"};
        Assert.assertArrayEquals(expectedResult, arr);
    }

    @Test
    public void testSpecialCharacterSort_RU() {
        String[] arr = new String[]{"Кофеин", "Каприновая кислота", "Кальций"};

        Stringi18nUtils.sortAlphabetically(arr, Collator.getInstance(Locale.US));

        String[] expectedResult = new String[]{"Кальций", "Каприновая кислота", "Кофеин"};
        Assert.assertArrayEquals(expectedResult, arr);
    }

    @Test
    public void testRemoveDiacriticalMarks() {
        String stringWithAccents = "Ácido araquidónico";
        String transformed = Stringi18nUtils.removeAccents(stringWithAccents);
        Assert.assertEquals("Acido araquidonico", transformed);
    }
}
