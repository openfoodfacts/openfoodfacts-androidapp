package openfoodfacts.github.scrachx.openfood.repositories;

/**
 * Contains information about taxonomy download state from settings.
 */
class DownloadState {

    private final boolean downloadActivated;
    private final long lastModifiedDateOnSettings;

    DownloadState(boolean downloadActivated, long lastModifiedDateOnSettings) {
        this.downloadActivated = downloadActivated;
        this.lastModifiedDateOnSettings = lastModifiedDateOnSettings;
    }

    boolean isDownloadActivated() {
        return downloadActivated;
    }

    long getLastModifiedDateOnSettings() {
        return lastModifiedDateOnSettings;
    }
}
