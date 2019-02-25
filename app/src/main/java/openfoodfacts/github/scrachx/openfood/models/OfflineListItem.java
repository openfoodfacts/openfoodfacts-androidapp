package openfoodfacts.github.scrachx.openfood.models;

public class OfflineListItem {
    public final static int TYPE_SMALL = 0;
    public final static int TYPE_LARGE = 0;

    private String name;
    private int type;
    private int size;
    private String url;
    private int progress;

    public OfflineListItem() {
        progress = 0;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
