package openfoodfacts.github.scrachx.openfood.models.entities;

import android.util.Base64;
import android.util.Log;

import org.greenrobot.greendao.converter.PropertyConverter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

class MapOfStringsToStringConverter implements PropertyConverter<Map<String, String>, String> {
    private static final String LOG_TAG;

    @Override
    public Map<String, String> convertToEntityProperty(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }
        try {
            ByteArrayInputStream bis=new ByteArrayInputStream(Base64.decode(databaseValue,Base64.DEFAULT));
            ObjectInputStream objectInputStream=new ObjectInputStream(bis);
            objectInputStream.readObject();

        } catch (IOException e) {
            Log.e(LOG_TAG, "Cannot serialize map to database.", e);
        } catch (ClassNotFoundException e) {
            Log.e(LOG_TAG, "Cannot serialize map to database.", e);
        }
        return null;
    }

    @Override
    public String convertToDatabaseValue(Map<String, String> entityProperty) {
        if (entityProperty == null) {
            return null;
        }
        try {
            ByteArrayOutputStream bos=new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream=new ObjectOutputStream(bos);
            objectOutputStream.writeObject(new HashMap<>(entityProperty));
            objectOutputStream.flush();
            return Base64.encodeToString(bos.toByteArray(),Base64.DEFAULT);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Cannot serialize map to database.", e);
        }
        return null;
    }

    static {
        LOG_TAG = MapOfStringsToStringConverter.class.getSimpleName();
    }
}
