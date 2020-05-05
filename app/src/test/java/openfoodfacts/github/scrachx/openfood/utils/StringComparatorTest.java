package openfoodfacts.github.scrachx.openfood.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class StringComparatorTest {

    @Test
    public void testSpecialCharacterSort() {
        String[] arr = new String[]{"Ácido araquidónico", "Zinc", "Almidón"};

        Arrays.sort(arr, new StringComparator());

        String[] expectedResult = new String[]{"Ácido araquidónico", "Almidón", "Zinc"};
        Assert.assertArrayEquals(expectedResult, arr);
    }

    @Test
    public void testNormalSort() {
        String[] arr = new String[]{"Ácido araquidónico", "Zinc", "Almidón"};
        Arrays.sort(arr);

        String[] expectedResult = new String[]{"Almidón", "Zinc", "Ácido araquidónico"};
        Assert.assertArrayEquals(expectedResult, arr);
    }

    @Test
    public void testRemoveDiacriticalMarks() {
        String stringWithAccents = "Ácido araquidónico";
        String transformed = StringComparator.removeAccents(stringWithAccents);
        Assert.assertEquals("Acido araquidonico", transformed);
    }
}
