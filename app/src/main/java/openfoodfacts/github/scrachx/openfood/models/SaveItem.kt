package openfoodfacts.github.scrachx.openfood.models

class SaveItem {
    var title: String? = null
    var fieldsCompleted = 0
        private set
    var url: String? = null
    var barcode: String? = null
    var weight: String? = null
    var brand: String? = null

    constructor()
    constructor(title: String?, fieldsCompleted: Int, url: String?, barcode: String?, weight: String?, brand: String?) {
        this.title = title
        this.fieldsCompleted = fieldsCompleted
        this.url = url
        this.barcode = barcode
        this.brand = brand
        this.weight = weight
    }
}