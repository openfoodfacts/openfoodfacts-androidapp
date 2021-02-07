package openfoodfacts.github.scrachx.openfood.utils;

import org.junit.Test;

import java.text.Collator;
import java.util.Arrays;
import java.util.Locale;

import static com.google.common.truth.Truth.assertThat;

public class Stringi18nUtilsTest {
    @Test
    public void testSortWithoutComparator() {
        String[] arr = new String[]{"Ácido araquidónico", "Ácido", "Zinc", "Almidón", "Acido"};
        Arrays.sort(arr);

        assertThat(arr).isEqualTo(new String[]{"Acido", "Almidón", "Zinc", "Ácido", "Ácido araquidónico"});
    }

    @Test
    public void testSpecialCharacterSort_ES() {
        String[] arr = new String[]{"Ácido araquidónico", "Ácido", "Zinc", "Almidón", "Acido"};

        Stringi18nUtils.sortAlphabetically(arr, Collator.getInstance(Locale.US));

        assertThat(arr).isEqualTo(new String[]{"Acido", "Ácido", "Ácido araquidónico", "Almidón", "Zinc"});
    }

    @Test
    public void testSpecialCharacterSort_FR() {
        String[] arr = new String[]{"oléique", "oleique", "acide", "Acide"};

        Stringi18nUtils.sortAlphabetically(arr, Collator.getInstance(Locale.FRENCH));

        assertThat(arr).isEqualTo(new String[]{"acide", "Acide", "oleique", "oléique"});
    }

    @Test
    public void testSpecialCharacterSort_FR2() {
        String[] arr = new String[]{"Molybdène", "manganèse", "molybdene", "Manganèse"};

        Stringi18nUtils.sortAlphabetically(arr, Collator.getInstance(Locale.FRENCH));

        // upper case affects the order
        assertThat(arr).isEqualTo(new String[]{"manganèse", "Manganèse", "molybdene", "Molybdène"});
    }

    @Test
    public void testSpecialCharacterSort_RU() {
        String[] arr = new String[]{"Кофеин", "Каприновая кислота", "Кальций"};

        Stringi18nUtils.sortAlphabetically(arr, Collator.getInstance(Locale.US));

        String[] expectedResult = new String[]{"Кальций", "Каприновая кислота", "Кофеин"};
        assertThat(arr).isEqualTo(expectedResult);
    }

    @Test
    public void testRemoveDiacriticalMarks() {
        String stringWithAccents = "Ácido araquidónico";
        String transformed = Stringi18nUtils.removeAccents(stringWithAccents);
        assertThat(transformed).isEqualTo("Acido araquidonico");
    }
}
