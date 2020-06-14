package openfoodfacts.github.scrachx.openfood.models.eventbus;

public class ProductNeedsRefreshEvent {
    private final String barcode;

    public ProductNeedsRefreshEvent(String barcode) {
        this.barcode = barcode;
    }

    public String getBarcode() {
        return barcode;
    }
}
