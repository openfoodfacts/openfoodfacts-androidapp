package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.util.Log
import org.jetbrains.annotations.Contract
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

object InstallationUtils {
    private const val KEY_INSTALLATION = "INSTALLATION"
    private var sID: String? = null

    @Synchronized
    fun id(context: Context?): String? {
        if (context == null) {
            return "(no id)"
        }
        if (sID == null || sID!!.isEmpty()) {
            val installation = File(context.filesDir, KEY_INSTALLATION)
            try {
                if (!installation.exists()) {
                    writeInstallationFile(installation)
                }
                sID = readInstallationFile(installation)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
        return sID
    }

    @Contract("_ -> new")
    @Throws(IOException::class)
    private fun readInstallationFile(installation: File): String {
        RandomAccessFile(installation, "r").use { f ->
            val bytes = ByteArray(f.length().toInt())
            f.readFully(bytes)
            return String(bytes)
        }
    }

    @Throws(IOException::class)
    private fun writeInstallationFile(installation: File) {
        FileOutputStream(installation).use { out ->
            var id = UUID.randomUUID().toString()
            val random = Random() //NO-SONAR ok here
            random.setSeed(1000)
            id += random.nextInt()
            id = getHashedString(id)
            out.write(id.toByteArray())
        }
    }

    private fun getHashedString(str: String): String {
        try {
            // Create MD5 Hash
            val messageDigest = MessageDigest.getInstance("MD5").apply {
                update(str.toByteArray())
            }.digest()

            // Create Hex String
            return StringBuilder().also {
                messageDigest.forEach { b -> it.append(Integer.toHexString(0xFF and b.toInt())) }
            }.toString()
        } catch (e: NoSuchAlgorithmException) {
            Log.e(InstallationUtils::class.simpleName, "getHashedString $str", e)
        }
        return ""
    }
}