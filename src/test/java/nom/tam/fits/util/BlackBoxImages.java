package nom.tam.fits.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.GZIPInputStream;

public class BlackBoxImages {

    public static String getBlackBoxImage(final String fileName) {
        if (new File("../blackbox-images/" + fileName).exists()) {
            return "../blackbox-images/" + fileName;
        } else if (new File("../blackbox-images/" + fileName + ".gz").exists()) {
            gunzipIt(new File("../blackbox-images/" + fileName + ".gz"), new File("../blackbox-images/" + fileName));
            return "../blackbox-images/" + fileName;
        } else {
            new File("target/blackbox-images").mkdirs();
            String url = "https://raw.githubusercontent.com/nom-tam-fits/blackbox-images/master/" + fileName;
            String loadedFileName = "target/blackbox-images/" + fileName;
            if (downloadImage(url, loadedFileName)) {
                return loadedFileName;
            }
            url = "https://raw.githubusercontent.com/nom-tam-fits/blackbox-images/master/" + fileName + ".gz";
            loadedFileName = "target/blackbox-images/" + fileName;
            String gzFileName = loadedFileName + ".gz";
            if (downloadImage(url, gzFileName)) {
                gunzipIt(new File(gzFileName), new File(loadedFileName));
                return loadedFileName;
            }

        }
        throw new UnsupportedOperationException("could not get blackbox image from anywhere");
    }

    protected static boolean downloadImage(String url, String localFile) {
        try {
            URL website = new URL(url);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(localFile);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * GunZip it
     */
    public static void gunzipIt(File from, File to) {
        byte[] buffer = new byte[4096];
        try {
            GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(from));
            FileOutputStream out = new FileOutputStream(to);
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            gzis.close();
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
