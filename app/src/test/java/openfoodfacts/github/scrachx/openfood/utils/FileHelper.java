package openfoodfacts.github.scrachx.openfood.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public final class FileHelper {

    public static String readTextFileFromResources(String filepath, ClassLoader classLoader) throws IOException {
        InputStream is = null;
        BufferedReader reader = null;
        try {
            is = classLoader.getResourceAsStream(filepath);
            if (is == null) {
                throw new FileNotFoundException("file not found:" + filepath);
            }

            reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }

            is.close();
            return sb.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (is != null) {
                is.close();
            }
        }
    }
}
