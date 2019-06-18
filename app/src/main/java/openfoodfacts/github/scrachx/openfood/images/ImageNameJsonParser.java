package openfoodfacts.github.scrachx.openfood.images;

import android.support.annotation.NonNull;
import android.util.Log;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Extract images informations form json.
 */
public class ImageNameJsonParser {
    private ImageNameJsonParser() {
    }

    /**
     * @param imagesAsJsonObject json representing images entries given by api/v0/product/XXXX.json?fields=images
     */
    public static List<String> extractImagesNameSortedByUploadTimeDesc(JSONObject imagesAsJsonObject) {
        TreeSet<NameUploadedTimeKey> nameWithTime = new TreeSet<>();
        if (imagesAsJsonObject != null) {
            final JSONArray names = imagesAsJsonObject.names();
            if (names != null) {
                // loop through all the image names and store them in a array list
                for (int i = 0; i < names.length(); i++) {
                    try {
                        // do not include images with contain nutrients,ingredients or other in their names
                        // as they are duplicate and do not load as well
                        final String namesString = names.getString(i);
                        if (isNameAccepted(namesString)) {
                            final long uploadedTime = imagesAsJsonObject.getJSONObject(namesString).getLong("uploaded_t");
                            nameWithTime.add(new NameUploadedTimeKey(namesString, uploadedTime));
                        }
                    } catch (JSONException e) {
                        Log.w(ImageNameJsonParser.class.getSimpleName(), "can't get product / images in json", e);
                    }
                }
            }
        }
        ArrayList<String> imageNames = new ArrayList<>();
        for (NameUploadedTimeKey key : nameWithTime) {
            imageNames.add(key.name);
        }
        return imageNames;
    }

    private static boolean isNameAccepted(String namesString) {
        return StringUtils.isNotBlank(namesString)
            && !namesString.contains("n")
            && !namesString.contains("f")
            && !namesString.contains("i")
            && !namesString.contains("o");
    }

    private static class NameUploadedTimeKey implements Comparable<NameUploadedTimeKey> {
        private final String name;
        private final long timestamp;

        NameUploadedTimeKey(String name, long timestamp) {
            this.name = name;
            this.timestamp = timestamp;
        }

        @SuppressWarnings("EqualsReplaceableByObjectsCall")
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            NameUploadedTimeKey that = (NameUploadedTimeKey) o;

            if (timestamp != that.timestamp) {
                return false;
            }
            return name != null ? name.equals(that.name) : that.name == null;
        }

        @Override
        public String toString() {
            return "NameUploadKey{" +
                "name='" + name + '\'' +
                ", timestamp=" + timestamp +
                '}';
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
            return result;
        }

        /**
         * to be ordered from newer to older.
         */
        @Override
        public int compareTo(@NonNull NameUploadedTimeKey o) {
            long deltaInTime = o.timestamp - timestamp;
            if (deltaInTime > 0) {
                return 1;
            }
            if (deltaInTime < 0) {
                return -1;
            }
            return name.compareTo(o.name);
        }
    }
}
