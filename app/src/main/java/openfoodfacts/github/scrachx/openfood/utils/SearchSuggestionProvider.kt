package openfoodfacts.github.scrachx.openfood.utils

import android.content.SearchRecentSuggestionsProvider
import openfoodfacts.github.scrachx.openfood.BuildConfig

class SearchSuggestionProvider : SearchRecentSuggestionsProvider() {
    companion object {
        const val AUTHORITY = BuildConfig.APPLICATION_ID + ".utils.SearchSuggestionProvider"
        const val MODE = DATABASE_MODE_QUERIES
    }

    init {
        setupSuggestions(AUTHORITY, MODE)
    }
}