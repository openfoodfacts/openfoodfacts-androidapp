package openfoodfacts.github.scrachx.openfood.utils;

import android.content.SearchRecentSuggestionsProvider;
import openfoodfacts.github.scrachx.openfood.BuildConfig;

public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = BuildConfig.APPLICATION_ID+".utils.SearchSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
