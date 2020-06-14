package openfoodfacts.github.scrachx.openfood.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Created by n27 on 4/4/17.
 */
public class LocaleHelperTest {
    @Test
    public void getLocale_fr() {
        Locale locale = LocaleHelper.getLocale("fr");
        assertEquals(Locale.FRENCH, locale);
    }

    @Test
    public void getLocale_en() {
        Locale locale = LocaleHelper.getLocale("en");
        assertEquals(Locale.ENGLISH, locale);
    }

    @Test
    public void getLocale_en_US() {
        Locale locale = LocaleHelper.getLocale("en-US");
        assertEquals(Locale.US, locale);
    }

    @Test
    public void getLocale_FromContext(){
        Context context = Mockito.mock(Context.class);
        Resources resources = Mockito.mock(Resources.class);
        Configuration configuration = Mockito.mock(Configuration.class);

        Locale locale = LocaleHelper.getLocale("en-US");
        configuration.locale = locale;

        Mockito.when(context.getResources()).thenReturn(resources);
        Mockito.when(resources.getConfiguration()).thenReturn(configuration);

        assertEquals(locale, LocaleHelper.getLocale(context));
    }
}
