package openfoodfacts.github.scrachx.openfood.features.changelog

sealed class ChangelogListItem {
    data class Header(val version: String, val date: String) : ChangelogListItem()
    data class Item(val description: String) : ChangelogListItem()
}
