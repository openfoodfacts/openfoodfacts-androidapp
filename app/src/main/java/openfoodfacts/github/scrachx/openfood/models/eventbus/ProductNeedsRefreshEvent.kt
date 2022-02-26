package openfoodfacts.github.scrachx.openfood.models.eventbus

import openfoodfacts.github.scrachx.openfood.models.Barcode

data class ProductNeedsRefreshEvent(val barcode: Barcode)