package openfoodfacts.github.scrachx.openfood.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Created by scotscriven on 04/05/15.
 */
public class MyNullKeySerializer extends JsonSerializer<Object>
{
    @Override
    public void serialize(Object nullKey, JsonGenerator jsonGenerator, SerializerProvider unused)
            throws IOException, JsonProcessingException
    {
        jsonGenerator.writeFieldName("");
    }
}
