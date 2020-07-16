package openfoodfacts.github.scrachx.openfood.images;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Extract images informations form json.
 */
public class ImageNameJsonParser {
    private ImageNameJsonParser() {
    }

    /**
     * @param imagesNode json representing images entries given by api/v0/product/XXXX.json?fields=images
     */
    public static List<String> extractImagesNameSortedByUploadTimeDesc(JsonNode imagesNode) {
        ArrayList<NameUploadedTimeKey> namesWithTime = new ArrayList<>();

        if (imagesNode != null) {
            final Iterator<Map.Entry<String, JsonNode>> images = imagesNode.fields();
            if (images != null) {
                // loop through all the image names and store them in a array list
                while (images.hasNext()) {
                    final Map.Entry<String, JsonNode> image = images.next();
                    final String imageName = image.getKey();
                    // do not include images with contain nutrients, ingredients or other in their names
                    // as they are duplicate and do not load as well
                    if (!isNameAccepted(imageName)) {
                        continue;
                    }
                    final long uploadedTime = image.getValue().get("uploaded_t").asLong();
                    namesWithTime.add(new NameUploadedTimeKey(imageName, uploadedTime));
                }
            }
        }

        return namesWithTime.stream()
            .sorted()
            .map(nameUploadedTimeKey -> nameUploadedTimeKey.name)
            .collect(Collectors.toList());
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
