package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

object InstallationUtils {
    private const val KEY_INSTALLATION = "INSTALLATION"
    private var id: String? = null

    @Synchronized
    fun id(context: Context?): String {
        if (context == null) return "(no id)"

        if (id.isNullOrEmpty()) {
            val installFile = File(context.filesDir, KEY_INSTALLATION)
            if (!installFile.exists()) {
                writeInstallationFile(installFile)
            }
            id = installFile.readText()
        }
        return id as String
    }

    @Throws(IOException::class)
    private fun writeInstallationFile(installation: File) {
        val id = UUID.randomUUID().toString() + Random().apply { setSeed(1000) }.nextInt()
        installation.writeText(getHashedString(id))
    }

    private fun getHashedString(str: String) = try {
        // Create MD5 Hash
        val digest = MessageDigest.getInstance("MD5").digest(str.toByteArray())

        // Create Hex String
        StringBuilder().also {
            digest.forEach { b -> it.append(Integer.toHexString(0xFF and b.toInt())) }
        }.toString()
    } catch (e: NoSuchAlgorithmException) {
        Log.e(InstallationUtils::class.simpleName, "getHashedString $str", e)
        ""
    }
}