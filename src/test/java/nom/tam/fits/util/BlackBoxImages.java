package nom.tam.fits.util;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
 * %%
 * This is free and unencumbered software released into the public domain.
 * 
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * 
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * #L%
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.GZIPInputStream;

import nom.tam.util.SafeClose;

public class BlackBoxImages {

    private BlackBoxImages() {
    }

    public static String getBlackBoxImage(final String fileName) {
        String skipBackboxImages = System.getProperty("skip.backbox.images", "false");
        if (skipBackboxImages.equals("true")) {
            org.junit.Assume.assumeTrue(false);
        }
        if (new File("../blackbox-images/" + fileName).exists()) {
            return "../blackbox-images/" + fileName;
        } else if (new File("../blackbox-images/" + fileName + ".gz").exists()) {
            gunzipIt(new File("../blackbox-images/" + fileName + ".gz"), new File("../blackbox-images/" + fileName));
            return "../blackbox-images/" + fileName;
        } else if (new File("../blackbox-images/" + fileName + "00.part").exists()) {
            unsplitt(new File("../blackbox-images/" + fileName), new File("../blackbox-images/" + fileName));
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
            int partNumber = 0;
            url = "https://raw.githubusercontent.com/nom-tam-fits/blackbox-images/master/" + fileName + String.format("%02d", partNumber) + ".part";
            loadedFileName = "target/blackbox-images/" + fileName;
            String partFileName = loadedFileName + String.format("%02d", partNumber) + ".part";
            while (new File(partFileName).exists() || downloadImage(url, partFileName)) {
                partNumber++;
                url = "https://raw.githubusercontent.com/nom-tam-fits/blackbox-images/master/" + fileName + String.format("%02d", partNumber) + ".part";
                partFileName = loadedFileName + String.format("%02d", partNumber) + ".part";
            }
            if (new File(loadedFileName + "00.part").exists()) {
                unsplitt(new File(loadedFileName), new File(loadedFileName));
                return loadedFileName;
            }

        }
        throw new UnsupportedOperationException("could not get blackbox image from anywhere");
    }

    private static void unsplitt(File base, File to) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(to);
            FileChannel outChannel = out.getChannel();
            int number = 0;
            long offset = 0L;
            File from = new File(base.getParentFile(), base.getName() + String.format("%02d", number) + ".part");
            while (from.exists()) {
                ReadableByteChannel rbc = Channels.newChannel(new FileInputStream(from));
                offset += outChannel.transferFrom(rbc, offset, Long.MAX_VALUE);
                number++;
                from = new File(base.getParentFile(), base.getName() + String.format("%02d", number) + ".part");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            SafeClose.close(out);
        }
    }

    protected static boolean downloadImage(String url, String localFile) {
        try {
            URL website = new URL(url);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            new File(localFile).getParentFile().mkdirs();
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
