package openfoodfacts.github.scrachx.openfood.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import openfoodfacts.github.scrachx.openfood.views.OFFApplication;
import org.apache.commons.text.WordUtils;

import java.util.*;

/**
 * This class is used to change your application locale and persist this change for the next time
 * that your app is going to be used.
 * <p/>
 * You can also change the locale of your application on the fly by using the setLocale method.
 * <p/>
 * Created by gunhansancar on 07/10/15.
 */
public class LocaleHelper {
    public static int find(List<LanguageData> availableLanguageForImage, String language) {
        if (language != null) {
            for (int i = 0; i < availableLanguageForImage.size(); i++) {
                if (language.equals(availableLanguageForImage.get(i).code)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static class LanguageData implements Comparable<LanguageData> {
        private final String code;
        private final String name;
        private final boolean supported;

        LanguageData(String code, String name, boolean supported) {
            this.code = code;
            this.name = name;
            this.supported = supported;
        }

        public boolean isSupported() {
            return supported;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name+" ["+code+"]";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (LanguageData.class.equals(obj.getClass())) {
                return code.equals(((LanguageData) obj).code);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return code.hashCode();
        }

        @Override
        public int compareTo(@NonNull LanguageData o) {
            return name.compareTo(o.name);
        }
    }

    private LocaleHelper() {
        //Helper class
    }

    private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";
    public static final String USER_COUNTRY_PREFERENCE_KEY = "user_country";

    public static Context onCreate(Context context) {
        String lang = getLanguageInPreferences(context, Locale.getDefault().getLanguage());
        return setLocale(context, lang);
    }

    public static Context onCreate(Context context, String defaultLanguage) {
        String lang = getLanguageInPreferences(context, defaultLanguage);
        return setLocale(context, lang);
    }

    public static List<LanguageData> getLanguageData(Collection<String> codes, boolean supported) {
        List<LanguageData> res = new ArrayList<>();
        if (codes != null) {
            for (String code : codes) {
                final LanguageData languageData = getLanguageData(code, supported);
                if (languageData != null) {
                    res.add(languageData);
                }
            }
        }
        Collections.sort(res);
        return res;
    }

    public static LanguageData getLanguageData(String code, boolean supported) {
        Locale locale = getLocale(code);
        if (locale == null) {
            return null;
        }
        return new LanguageData(locale.getLanguage(), WordUtils.capitalize(locale.getDisplayName(locale)), supported);
    }

    public static Locale getLocale() {
        return getLocale(OFFApplication.getInstance());
    }

    /**
     * Used by screenshots test
     */
    @SuppressWarnings("unused")
    public static Context setLocale(Locale locale) {
        return setLocale(OFFApplication.getInstance(), locale);
    }

    public static String getLanguage(Context context) {
        String lang = getLanguageInPreferences(context, Locale.getDefault().getLanguage());
        if (lang.contains("-")) {
            lang = lang.split("-")[0];
        }
        return lang;
    }

    public static Locale getLocale(Context context) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        final Locale locale = configuration.locale;
        return locale == null ? Locale.getDefault() : locale;
    }

    public static Context setLocale(Context context, String language) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(SELECTED_LANGUAGE, language)
            .apply();

        Locale locale = getLocale(language);
        return setLocale(context, locale);
    }

    private static Context setLocale(Context context, Locale locale) {
        if (locale == null) {
            return context;
        }
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(SELECTED_LANGUAGE, locale.getLanguage())
            .apply();

        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();
        if (Build.VERSION.SDK_INT >= 17) {
            configuration.setLocale(locale);
            context = context.createConfigurationContext(configuration);
        } else {
            configuration.locale = locale;
        }
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return context;
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
        Locale localeObj = null;
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
        } else {
            localeObj = new Locale(language, country);
        }
        return localeObj;
    }

    private static String getLanguageInPreferences(Context context, String defaultLanguage) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }
}
