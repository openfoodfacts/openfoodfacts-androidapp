package openfoodfacts.github.scrachx.openfood.repositories;

public class DownloadState {

    private final boolean downloadActivated;
    private final long lastModifiedDateOnServer;

    public DownloadState(boolean downloadActivated, long lastModifiedDateOnServer) {
        this.downloadActivated = downloadActivated;
        this.lastModifiedDateOnServer = lastModifiedDateOnServer;
    }

    public boolean isDownloadActivated() {
        return downloadActivated;
    }

    public long getLastModifiedDateOnServer() {
        return lastModifiedDateOnServer;
    }
}
