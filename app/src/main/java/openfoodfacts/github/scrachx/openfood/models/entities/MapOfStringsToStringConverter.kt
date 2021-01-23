package openfoodfacts.github.scrachx.openfood.models.entities

import android.util.Base64
import android.util.Log
import org.greenrobot.greendao.converter.PropertyConverter
import java.io.*
import java.util.*

class MapOfStringsToStringConverter : PropertyConverter<Map<String, String>?, String?> {
    override fun convertToEntityProperty(databaseValue: String?): Map<String, String>? {
        if (databaseValue == null) return null
        try {
            return ByteArrayInputStream(Base64.decode(databaseValue, Base64.DEFAULT)).use { bis ->
                ObjectInputStream(bis).use {
                    it.readObject() as HashMap<String, String>
                }
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Cannot deserialize map from database.", e)
        }
        return null
    }

    override fun convertToDatabaseValue(entityProperty: Map<String, String>?): String? {
        if (entityProperty == null) return null
        try {
            return ByteArrayOutputStream().use { bos ->
                ObjectOutputStream(bos).use {
                    it.writeObject(HashMap(entityProperty))
                    it.flush()
                    Base64.encodeToString(bos.toByteArray(), Base64.DEFAULT)
                }
            }
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Cannot serialize map to database.", e)
        }
        return null
    }

    companion object {
        val LOG_TAG = this::class.simpleName
    }
}