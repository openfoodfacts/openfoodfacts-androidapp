package openfoodfacts.github.scrachx.openfood.utils;

import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * Created by n27 on 4/4/17.
 */
public class LocaleHelperTest {

    @Test
    public void getLocale_fr() throws Exception {
        Locale locale = LocaleHelper.getLocale("fr");

        assertEquals(Locale.FRENCH, locale);
    }

    @Test
    public void getLocale_en() throws Exception {
        Locale locale = LocaleHelper.getLocale("en");

        assertEquals(Locale.ENGLISH, locale);
    }

    @Test
    public void getLocale_en_US() throws Exception {
        Locale locale = LocaleHelper.getLocale("en-US");

        assertEquals(Locale.US, locale);
    }

}