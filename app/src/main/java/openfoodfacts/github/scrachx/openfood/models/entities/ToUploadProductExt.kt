package openfoodfacts.github.scrachx.openfood.models.entities

import openfoodfacts.github.scrachx.openfood.images.ProductImage

fun ToUploadProduct(image: ProductImage) = ToUploadProduct(
    image.barcode,
    image.filePath,
    image.imageField.toString()
)