package openfoodfacts.github.scrachx.openfood.utils

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.inject.Inject

class InstallationService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    val id: String by lazy {
        val installFile = File(context.filesDir, KEY_INSTALLATION)
        if (!installFile.exists()) {
            writeInstallationFile(installFile)
        }
        installFile.readText()
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
        Log.e(InstallationService::class.simpleName, "getHashedString $str", e)
        ""
    }

    companion object {
        private const val KEY_INSTALLATION = "INSTALLATION"
    }
}