package openfoodfacts.github.scrachx.openfood.utils

import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@Throws(IOException::class)
fun readTextFileFromResources(filepath: String, classLoader: ClassLoader): String {
    classLoader.getResourceAsStream(filepath).use { stream ->
        if (stream == null) throw FileNotFoundException("File not found: $filepath")
        BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
                sb.append("\n")
            }
            stream.close()
            return sb.toString()
        }
    }
}