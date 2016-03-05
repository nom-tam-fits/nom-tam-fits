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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.util.ArrayDataOutput;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.RandomAccess;

/**
 * This class comprises static utility functions used throughout the FITS
 * classes.
 */
public final class FitsUtil {

    private static final int BYTE_REPRESENTING_BLANK = 32;

    private static final int BYTE_REPRESENTING_MAX_ASCII_VALUE = 126;

    /**
     * the logger to log to.
     */
    private static final Logger LOG = Logger.getLogger(FitsUtil.class.getName());

    private static boolean wroteCheckingError = false;

    /**
     * Utility class, do not instantiate it.
     */
    private FitsUtil() {
    }

    /**
     * @return Total size of blocked FITS element, using e.v. padding to fits
     *         block size.
     * @param size
     *            the current size.
     */
    public static int addPadding(int size) {
        return size + padding(size);
    }

    /**
     * @return Total size of blocked FITS element, using e.v. padding to fits
     *         block size.
     * @param size
     *            the current size.
     */
    public static long addPadding(long size) {
        return size + padding(size);
    }

    /**
     * @return Convert an array of booleans to bytes.
     * @param bool
     *            array of booleans
     */
    static byte[] booleanToByte(boolean[] bool) {

        byte[] byt = new byte[bool.length];
        for (int i = 0; i < bool.length; i += 1) {
            byt[i] = bool[i] ? (byte) 'T' : (byte) 'F';
        }
        return byt;
    }

    /**
     * @return Convert bytes to Strings.
     * @param bytes
     *            byte array to convert
     * @param maxLen
     *            the max string length
     */
    public static String[] byteArrayToStrings(byte[] bytes, int maxLen) {
        boolean checking = FitsFactory.getCheckAsciiStrings();

        // Note that if a String in a binary table contains an internal 0,
        // the FITS standard says that it is to be considered as terminating
        // the string at that point, so that software reading the
        // data back may not include subsequent characters.
        // No warning of this truncation is given.

        String[] res = new String[bytes.length / maxLen];
        for (int i = 0; i < res.length; i += 1) {

            int start = i * maxLen;
            int end = start + maxLen;
            // Pre-trim the string to avoid keeping memory
            // hanging around. (Suggested by J.C. Segovia, ESA).

            // Note that the FITS standard does not mandate
            // that we should be trimming the string at all, but
            // this seems to best meet the desires of the community.
            for (; start < end; start += 1) {
                if (bytes[start] != BYTE_REPRESENTING_BLANK) {
                    break; // Skip only spaces.
                }
            }

            for (; end > start; end -= 1) {
                if (bytes[end - 1] != BYTE_REPRESENTING_BLANK) {
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

                if (bytes[j] == 0) {
                    end = j;
                    break;
                }
                if (checking && (bytes[j] < BYTE_REPRESENTING_BLANK || bytes[j] > BYTE_REPRESENTING_MAX_ASCII_VALUE)) {
                    errFound = true;
                    bytes[j] = BYTE_REPRESENTING_BLANK;
                }
            }
            res[i] = AsciiFuncs.asciiString(bytes, start, end - start);
            if (errFound && !FitsUtil.wroteCheckingError) {
                LOG.log(Level.SEVERE, "Warning: Invalid ASCII character[s] detected in string: " + res[i]
                        + " Converted to space[s].  Any subsequent invalid characters will be converted silently");
                FitsUtil.wroteCheckingError = true;
            }
        }
        return res;

    }

    /**
     * @return Convert an array of bytes to booleans.
     * @param bytes
     *            the array of bytes to get the booleans from.
     */
    static boolean[] byteToBoolean(byte[] bytes) {
        boolean[] bool = new boolean[bytes.length];

        for (int i = 0; i < bytes.length; i += 1) {
            bool[i] = bytes[i] == 'T';
        }
        return bool;
    }

    /**
     * @return Find out where we are in a random access file .
     * @param o
     *            the stream to get the position
     */
    public static long findOffset(Closeable o) {
        if (o instanceof RandomAccess) {
            return ((RandomAccess) o).getFilePointer();
        } else {
            return -1;
        }
    }

    /**
     * @return Get a stream to a URL accommodating possible redirections. Note
     *         that if a redirection request points to a different protocol than
     *         the original request, then the redirection is not handled
     *         automatically.
     * @param url
     *            the url to get the stream from
     * @param level
     *            max levels of redirection
     * @throws IOException
     *             if the operation failed
     */
    public static InputStream getURLStream(URL url, int level) throws IOException {
        URLConnection conn = null;
        int code = -1;
        try {
            conn = url.openConnection();
            if (conn instanceof HttpURLConnection) {
                code = ((HttpURLConnection) conn).getResponseCode();
            }
            return conn.getInputStream();
        } catch (ProtocolException e) {
            LOG.log(Level.WARNING, "could not connect to " + url + (code >= 0 ? " got responce-code" + code : ""), e);
            throw e;
        }
    }

    /**
     * @return Get the maximum length of a String in a String array.
     * @param strings
     *            array of strings to check
     * @throws FitsException
     *             if the operation failed
     */
    public static int maxLength(String[] strings) throws FitsException {

        int max = 0;
        for (String element : strings) {
            if (element != null && element.length() > max) {
                max = element.length();
            }
        }
        return max;
    }

    /**
     * Add padding to an output stream.
     * 
     * @param stream
     *            stream to pad
     * @param size
     *            the current size
     * @throws FitsException
     *             if the operation failed
     */
    public static void pad(ArrayDataOutput stream, long size) throws FitsException {
        pad(stream, size, (byte) 0);
    }

    /**
     * Add padding to an output stream.
     * 
     * @param stream
     *            stream to pad
     * @param size
     *            the current size
     * @param fill
     *            the fill byte to use
     * @throws FitsException
     *             if the operation failed
     */
    public static void pad(ArrayDataOutput stream, long size, byte fill) throws FitsException {
        int len = padding(size);
        if (len > 0) {
            byte[] buf = new byte[len];
            Arrays.fill(buf, fill);
            try {
                stream.write(buf);
                stream.flush();
            } catch (Exception e) {
                throw new FitsException("Unable to write padding", e);
            }
        }
    }

    /**
     * @return How many bytes are needed to fill a 2880 block?
     * @param size
     *            the size without padding
     */
    public static int padding(int size) {
        return padding((long) size);
    }

    public static int padding(long size) {

        int mod = (int) (size % FitsFactory.FITS_BLOCK_SIZE);
        if (mod > 0) {
            mod = FitsFactory.FITS_BLOCK_SIZE - mod;
        }
        return mod;
    }

    /**
     * Reposition a random access stream to a requested offset.
     * 
     * @param o
     *            the closable to reposition
     * @param offset
     *            the offset to position it to.
     * @throws FitsException
     *             if the operation was failed or not possible
     */
    public static void reposition(Closeable o, long offset) throws FitsException {
        if (o == null) {
            throw new FitsException("Attempt to reposition null stream");
        } else if (!(o instanceof RandomAccess) || offset < 0) {
            throw new FitsException("Invalid attempt to reposition stream " + o + " of type " + o.getClass().getName() + " to " + offset);
        }
        try {
            ((RandomAccess) o).seek(offset);
        } catch (IOException e) {
            throw new FitsException("Unable to repostion stream " + o + " of type " + o.getClass().getName() + " to " + offset + "   Exception:" + e.getMessage(), e);
        }
    }

    /**
     * Convert an array of Strings to bytes.
     * 
     * @return the resulting bytes
     * @param stringArray
     *            the array with Strings
     * @param maxLen
     *            the max length (in bytes) of every String
     */
    public static byte[] stringsToByteArray(String[] stringArray, int maxLen) {
        byte[] res = new byte[stringArray.length * maxLen];
        for (int i = 0; i < stringArray.length; i += 1) {
            byte[] bstr = null;
            if (stringArray[i] == null) {
                bstr = new byte[0];
            } else {
                bstr = AsciiFuncs.getBytes(stringArray[i]);
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
