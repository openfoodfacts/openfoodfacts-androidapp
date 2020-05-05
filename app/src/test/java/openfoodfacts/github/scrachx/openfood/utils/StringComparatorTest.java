package openfoodfacts.github.scrachx.openfood.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class StringComparatorTest {
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

        StringComparator.sort(arr);

        String[] expectedResult = new String[]{"Acido", "Ácido", "Ácido araquidónico", "Almidón", "Zinc"};
        Assert.assertArrayEquals(expectedResult, arr);
    }

    @Test
    public void testSpecialCharacterSort_FR() {
        String[] arr = new String[]{"oléique", "oleique", "acide", "Acide"};

        StringComparator.sort(arr);

        String[] expectedResult = new String[]{"acide", "Acide", "oleique", "oléique"};
        Assert.assertArrayEquals(expectedResult, arr);
    }

    @Test
    public void testSpecialCharacterSort_FR2() {
        String[] arr = new String[]{"Molybdène", "manganèse", "molybdene", "Manganèse"};

        StringComparator.sort(arr);

        // upper case affects the order
        String[] expectedResult = new String[]{"manganèse", "Manganèse", "molybdene", "Molybdène"};
        Assert.assertArrayEquals(expectedResult, arr);
    }

    @Test
    public void testSpecialCharacterSort_RU() {
        String[] arr = new String[]{"Кофеин", "Каприновая кислота", "Кальций"};

        StringComparator.sort(arr);

        String[] expectedResult = new String[]{"Кальций", "Каприновая кислота", "Кофеин"};
        Assert.assertArrayEquals(expectedResult, arr);
    }

    @Test
    public void testRemoveDiacriticalMarks() {
        String stringWithAccents = "Ácido araquidónico";
        String transformed = StringComparator.removeAccents(stringWithAccents);
        Assert.assertEquals("Acido araquidonico", transformed);
    }
}
