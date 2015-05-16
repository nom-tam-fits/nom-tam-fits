package nom.tam.fits;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
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
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.zip.GZIPInputStream;

import nom.tam.fits.compress.ICompressProvider;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.RandomAccess;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

/**
 * This class comprises static utility functions used throughout the FITS
 * classes.
 */
public class FitsUtil {

    private static boolean wroteCheckingError = false;

    /** Total size of blocked FITS element */
    public static int addPadding(int size) {
        return size + padding(size);
    }

    public static long addPadding(long size) {
        return size + padding(size);
    }

    /** Convert an array of booleans to bytes */
    static byte[] booleanToByte(boolean[] bool) {

        byte[] byt = new byte[bool.length];
        for (int i = 0; i < bool.length; i += 1) {
            byt[i] = bool[i] ? (byte) 'T' : (byte) 'F';
        }
        return byt;
    }

    /** Convert bytes to Strings */
    public static String[] byteArrayToStrings(byte[] o, int maxLen) {
        boolean checking = FitsFactory.getCheckAsciiStrings();

        // Note that if a String in a binary table contains an internal 0,
        // the FITS standard says that it is to be considered as terminating
        // the string at that point, so that software reading the
        // data back may not include subsequent characters.
        // No warning of this truncation is given.

        String[] res = new String[o.length / maxLen];
        for (int i = 0; i < res.length; i += 1) {

            int start = i * maxLen;
            int end = start + maxLen;
            // Pre-trim the string to avoid keeping memory
            // hanging around. (Suggested by J.C. Segovia, ESA).

            // Note that the FITS standard does not mandate
            // that we should be trimming the string at all, but
            // this seems to best meet the desires of the community.
            for (; start < end; start += 1) {
                if (o[start] != 32) {
                    break; // Skip only spaces.
                }
            }

            for (; end > start; end -= 1) {
                if (o[end - 1] != 32) {
                    break;
                }
            }

            // For FITS binary tables, 0 values are supposed
            // to terminate strings, a la C. [They shouldn't appear in
            // any other context.]
            // Other non-printing ASCII characters
            // should always be an error which we can check for
            // if the user requests.

            // The lack of handling of null bytes was noted by Laurent Bourges.
            boolean errFound = false;
            for (int j = start; j < end; j += 1) {

                if (o[j] == 0) {
                    end = j;
                    break;
                }
                if (checking) {
                    if (o[j] < 32 || o[j] > 126) {
                        errFound = true;
                        o[j] = 32;
                    }
                }
            }
            res[i] = AsciiFuncs.asciiString(o, start, end - start);
            if (errFound && !FitsUtil.wroteCheckingError) {
                System.err.println("Warning: Invalid ASCII character[s] detected in string:" + res[i]);
                System.err.println("   Converted to space[s].  Any subsequent invalid characters will be converted silently");
                FitsUtil.wroteCheckingError = true;
            }
        }
        return res;

    }

    /** Convert an array of bytes to booleans */
    static boolean[] byteToBoolean(byte[] byt) {
        boolean[] bool = new boolean[byt.length];

        for (int i = 0; i < byt.length; i += 1) {
            bool[i] = byt[i] == 'T';
        }
        return bool;
    }

    /** Find out where we are in a random access file */
    public static long findOffset(Object o) {

        if (o instanceof RandomAccess) {
            return ((RandomAccess) o).getFilePointer();
        } else {
            return -1;
        }
    }

    /**
     * Get a stream to a URL accommodating possible redirections. Note that if a
     * redirection request points to a different protocol than the original
     * request, then the redirection is not handled automatically.
     */
    public static InputStream getURLStream(URL url, int level) throws IOException {

        // Hard coded....sigh
        if (level > 5) {
            throw new IOException("Two many levels of redirection in URL");
        }
        URLConnection conn = url.openConnection();
        // Map<String,List<String>> hdrs = conn.getHeaderFields();
        Map hdrs = conn.getHeaderFields();

        // Read through the headers and see if there is a redirection header.
        // We loop (rather than just do a get on hdrs)
        // since we want to match without regard to case.
        String[] keys = (String[]) hdrs.keySet().toArray(new String[0]);
        // for (String key: hdrs.keySet()) {
        for (String key : keys) {
            if (key != null && key.toLowerCase().equals("location")) {
                // String val = hdrs.get(key).get(0);
                String val = (String) ((List) hdrs.get(key)).get(0);
                if (val != null) {
                    val = val.trim();
                    if (val.length() > 0) {
                        // Redirect
                        return getURLStream(new URL(val), level + 1);
                    }
                }
            }
        }
        // No redirection
        return conn.getInputStream();
    }

    /**
     * Get the maximum length of a String in a String array.
     */
    public static int maxLength(String[] o) throws FitsException {

        int max = 0;
        for (String element : o) {
            if (element != null && element.length() > max) {
                max = element.length();
            }
        }
        return max;
    }

    /** Add padding to an output stream. */
    public static void pad(ArrayDataOutput stream, long size) throws FitsException {
        pad(stream, size, (byte) 0);
    }

    /** Add padding to an output stream. */
    public static void pad(ArrayDataOutput stream, long size, byte fill) throws FitsException {
        int len = padding(size);
        if (len > 0) {
            byte[] buf = new byte[len];
            for (int i = 0; i < len; i += 1) {
                buf[i] = fill;
            }
            try {
                stream.write(buf);
                stream.flush();
            } catch (Exception e) {
                throw new FitsException("Unable to write padding", e);
            }
        }
    }

    /** How many bytes are needed to fill the last 2880 block? */
    public static int padding(int size) {
        return padding((long) size);
    }

    public static int padding(long size) {

        int mod = (int) (size % 2880);
        if (mod > 0) {
            mod = 2880 - mod;
        }
        return mod;
    }

    /** Reposition a random access stream to a requested offset */
    public static void reposition(Object o, long offset) throws FitsException {

        if (o == null) {
            throw new FitsException("Attempt to reposition null stream");
        }
        if (!(o instanceof RandomAccess) || offset < 0) {
            throw new FitsException("Invalid attempt to reposition stream " + o + " of type " + o.getClass().getName() + " to " + offset);
        }

        try {
            ((RandomAccess) o).seek(offset);
        } catch (IOException e) {
            throw new FitsException("Unable to repostion stream " + o + " of type " + o.getClass().getName() + " to " + offset + "   Exception:" + e);
        }
    }

    /**
     * Copy an array of Strings to bytes.
     */
    public static byte[] stringsToByteArray(String[] o, int maxLen) {
        byte[] res = new byte[o.length * maxLen];
        for (int i = 0; i < o.length; i += 1) {
            byte[] bstr = null;
            if (o[i] == null) {
                bstr = new byte[0];
            } else {
                bstr = AsciiFuncs.getBytes(o[i]);
            }
            int cnt = bstr.length;
            if (cnt > maxLen) {
                cnt = maxLen;
            }
            System.arraycopy(bstr, 0, res, i * maxLen, cnt);
            for (int j = cnt; j < maxLen; j += 1) {
                res[i * maxLen + j] = (byte) ' ';
            }
        }
        return res;
    }
}
