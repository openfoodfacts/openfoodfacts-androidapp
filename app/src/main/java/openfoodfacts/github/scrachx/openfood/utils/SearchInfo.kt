package openfoodfacts.github.scrachx.openfood.utils

import android.os.Parcel
import android.os.Parcelable
import org.jetbrains.annotations.Contract

/**
 * Class that contains search information
 */
class SearchInfo : Parcelable {
    var searchQuery: String
    var searchTitle: String
    var searchType: SearchType

    /**
     * Constructor for search information used by [ProductSearchActivity]
     *
     * @param mSearchQuery the search query
     * @param mSearchTitle title of the search
     * @param mSearchType type of search
     */
    constructor(mSearchType: SearchType, mSearchQuery: String, mSearchTitle: String = mSearchQuery) {
        searchQuery = mSearchQuery
        searchTitle = mSearchTitle
        searchType = mSearchType
    }


    private constructor(parcel: Parcel) {
        searchQuery = parcel.readString()!!
        searchTitle = parcel.readString()!!
        searchType = parcel.readSerializable() as SearchType
    }

    /**
     * Parcelable implementation
     */
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(searchQuery)
        dest.writeString(searchTitle)
        dest.writeSerializable(searchType)
    }


    companion object CREATOR : Parcelable.Creator<SearchInfo> {
        /**
         * @return search info for an incomplete product
         */
        @Contract(value = " -> new", pure = true)
        fun emptySearchInfo() = SearchInfo(SearchType.INCOMPLETE_PRODUCT, "", "")

        override fun createFromParcel(parcel: Parcel): SearchInfo {
            return SearchInfo(parcel)
        }

        override fun newArray(size: Int): Array<SearchInfo?> {
            return arrayOfNulls(size)
        }
    }
}