package openfoodfacts.github.scrachx.openfood.models

class NavDrawerItem {
    var title: String? = null
    var icon = 0

    constructor()
    constructor(title: String?, icon: Int) {
        this.title = title
        this.icon = icon
    }
}