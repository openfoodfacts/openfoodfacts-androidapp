package openfoodfacts.github.scrachx.openfood.views;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;

public class Installation {
    private static final String KEY_INSTALLATION = "INSTALLATION";
    private static String sID = null;

    private Installation(){
        //Helper class
    }

    public static synchronized String id(Context context) {
        if (sID == null || sID.isEmpty()) {
            File installation = new File(context.getFilesDir(), KEY_INSTALLATION);
            try {
                if (!installation.exists())
                    writeInstallationFile(installation);
                sID = readInstallationFile(installation);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sID;
    }

    private static String readInstallationFile(File installation) throws IOException {
        try(RandomAccessFile f = new RandomAccessFile(installation, "r")) {
            byte[] bytes = new byte[(int) f.length()];
            f.readFully(bytes);
            return new String(bytes);
        }
    }

    @SuppressWarnings("squid:S2119")
    private static void writeInstallationFile(File installation) throws IOException {
        try(  FileOutputStream out = new FileOutputStream(installation)) {
            String id = UUID.randomUUID().toString();
            Random random = new Random();//NO-SONAR ok here
            random.setSeed(1000);
            id = id + random.nextInt();
            id = getHashedString(id);
            out.write(id.getBytes());
        }
    }

    public static String getHashedString(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) hexString.append(Integer.toHexString(0xFF & b));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            Log.e(Installation.class.getSimpleName(),"getHashedString "+s,e);
        }
        return "";
    }
}

