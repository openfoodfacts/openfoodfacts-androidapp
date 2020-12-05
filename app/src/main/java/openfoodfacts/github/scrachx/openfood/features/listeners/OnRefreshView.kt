package openfoodfacts.github.scrachx.openfood.features.listeners

import openfoodfacts.github.scrachx.openfood.models.ProductState

/**
 * Created by Lobster on 19.04.18.
 */
interface OnRefreshView {
    fun refreshView(productState: ProductState)
}