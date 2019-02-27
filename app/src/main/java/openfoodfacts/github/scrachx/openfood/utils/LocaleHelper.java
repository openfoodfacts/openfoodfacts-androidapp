package openfoodfacts.github.scrachx.openfood.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.Locale;

/**
 * This class is used to change your application locale and persist this change for the next time
 * that your app is going to be used.
 * <p/>
 * You can also change the locale of your application on the fly by using the setLocale method.
 * <p/>
 * Created by gunhansancar on 07/10/15.
 */
public class LocaleHelper {

    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";

    public static void onCreate(Context context) {
        String lang = getPersistedData(context, Locale.getDefault().getLanguage());
        setLocale(context, lang);
    }

    public static void onCreate(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        setLocale(context, lang);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    //removes country specific code in the language code eg: nl-BE and returns the lang
    public static String getLanguageTrimmed(Context context) {
        String lang = LocaleHelper.getLanguage(context);
        if (lang.contains("-")) {
            String langSplit[] = lang.split("-");
            lang = langSplit[0];
        }
        return lang;
    }

    public static void setLocale(Context context, String language) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(SELECTED_LANGUAGE, language)
                .apply();

        Locale locale = getLocale(language);

        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    /**
     * Extract language and region from the locale string
     *
     * @param locale language
     * @return Locale from locale string
     */
    public static Locale getLocale(String locale) {
        String[] localeParts = locale.split("-");
        String language = localeParts[0];
        String country = localeParts.length == 2 ? localeParts[1] : "";
        Locale localeObj=null;
        if (locale.contains("+")) {
            localeParts = locale.split("\\+");
            language = localeParts[1];
            String script = localeParts[2];
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                for (Locale checkLocale : Locale.getAvailableLocales()) {
                    if (checkLocale.getISO3Language().equals(language) && checkLocale.getCountry().equals(country) && checkLocale.getVariant().equals("")) {
                        localeObj = checkLocale;
                    }
                }
            } else {
                localeObj = new Locale.Builder().setLanguage(language).setRegion(country).setScript(script).build();
            }

        }else {
            localeObj = new Locale(language,country);
        }
        return localeObj;
    }

    private static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }

}