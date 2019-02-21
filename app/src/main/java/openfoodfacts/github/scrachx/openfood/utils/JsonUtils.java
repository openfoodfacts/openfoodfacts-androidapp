package openfoodfacts.github.scrachx.openfood.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Jackson Utils for read and write JSON
 */

public class JsonUtils {


    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static ObjectReader readFor(Class<?> type) {
        return objectMapper.readerFor(type);
    }

    public static ObjectReader readFor(TypeReference<?> typeReference) {
        return objectMapper.readerFor(typeReference);
    }

    public static ObjectWriter writerFor(Class<?> type) {
        return objectMapper.writerFor(type);
    }

}
