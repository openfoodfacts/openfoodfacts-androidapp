package openfoodfacts.github.scrachx.openfood.utils

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.jetbrains.annotations.Contract

/**
 * Class that contains search information
 *
 * @param searchQuery the search query
 * @param searchTitle title of the search
 * @param searchType type of search
 */
@Parcelize
data class SearchInfo(
        var searchType: SearchType,
        var searchQuery: String,
        var searchTitle: String = searchQuery
) : Parcelable {
    companion object {
        @Contract(value = " -> new", pure = true)
        fun emptySearchInfo() = SearchInfo(SearchType.INCOMPLETE_PRODUCT, "", "")
    }
}