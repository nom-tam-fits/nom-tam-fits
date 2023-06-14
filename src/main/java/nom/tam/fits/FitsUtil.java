package nom.tam.fits;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2021 nom-tam-fits
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
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.FitsDecoder;
import nom.tam.util.FitsEncoder;
import nom.tam.util.FitsIO;
import nom.tam.util.RandomAccess;

/**
 * Static utility functions used throughout the FITS classes.
 */
public final class FitsUtil {

    private static final int BLANK_SPACE = 0x20;

    private static final int MAX_ASCII_VALUE = 0x7e;

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
     * @deprecated      use {@link #addPadding(long)} instead. Calculates the amount of padding needed to complete the
     *                      last FITS block at the specified current size.
     * 
     * @return          Total size of blocked FITS element, using e.v. padding to fits block size.
     *
     * @param      size the current size.
     */
    public static int addPadding(int size) {
        return size + padding(size);
    }

    /**
     * Calculates the amount of padding needed to complete the last FITS block at the specified current size.
     * 
     * @return      Total size of blocked FITS element, using e.v. padding to fits block size.
     *
     * @param  size the current size.
     * 
     * @see         #padding(long)
     * @see         #pad(ArrayDataOutput, long)
     */
    public static long addPadding(long size) {
        return size + padding(size);
    }

    static Object booleansToBytes(Object o) {
        if (o instanceof Boolean) {
            return FitsEncoder.byteForBoolean((Boolean) o);
        }

        if (o instanceof boolean[]) {
            boolean[] bool = (boolean[]) o;
            byte[] b = new byte[bool.length];
            for (int i = 0; i < bool.length; i++) {
                b[i] = FitsEncoder.byteForBoolean(bool[i]);
            }
            return b;
        }

        if (o instanceof Boolean[]) {
            Boolean[] bool = (Boolean[]) o;
            byte[] b = new byte[bool.length];
            for (int i = 0; i < bool.length; i++) {
                b[i] = FitsEncoder.byteForBoolean(bool[i]);
            }
            return b;
        }

        if (o instanceof Object[]) {
            Object[] array = (Object[]) o;
            Object[] b = null;

            for (int i = 0; i < array.length; i++) {
                Object e = booleansToBytes(array[i]);
                if (b == null) {
                    b = (Object[]) Array.newInstance(e.getClass(), array.length);
                }
                b[i] = e;
            }

            return b;
        }

        throw new IllegalArgumentException("Not boolean values: " + o.getClass().getName());
    }

    static Object bytesToBooleanObjects(Object o) {
        if (o instanceof Number) {
            return FitsDecoder.booleanObjectFor(((Number) o).intValue());
        }
        if (o instanceof byte[]) {
            byte[] b = (byte[]) o;
            Boolean[] bool = new Boolean[b.length];
            for (int i = 0; i < b.length; i++) {
                bool[i] = FitsDecoder.booleanObjectFor(b[i]);
            }
            return bool;
        }

        if (o instanceof Object[]) {
            Object[] array = (Object[]) o;
            Object[] bool = null;

            for (int i = 0; i < array.length; i++) {
                Object e = bytesToBooleanObjects(array[i]);
                if (bool == null) {
                    bool = (Object[]) Array.newInstance(e.getClass(), array.length);
                }
                bool[i] = e;
            }

            return bool;
        }

        throw new IllegalArgumentException("Cannot convert to boolean values: " + o.getClass().getName());
    }

    static byte[] bitsToBytes(Object o) throws IllegalArgumentException {
        if (!o.getClass().isArray()) {
            throw new IllegalArgumentException("Cannot convert to bits: " + o.getClass().getName());
        }

        if (ArrayFuncs.getBaseClass(o) != boolean.class) {
            throw new IllegalArgumentException("Cannot convert to bits: " + o.getClass().getName());
        }

        boolean[] bits = (boolean[]) ArrayFuncs.flatten(o);

        byte[] bytes = new byte[(bits.length + Byte.SIZE - 1) / Byte.SIZE];
        for (int i = 0; i < bits.length; i++) {
            if (bits[i]) {
                int pos = Byte.SIZE - i % Byte.SIZE;
                bytes[i / Byte.SIZE] |= 1 << pos;
            }
        }

        return bytes;
    }

    static boolean[] bytesToBits(byte[] bytes, int count) {
        boolean[] bits = new boolean[count];

        for (int i = 0; i < bits.length; i++) {
            int pos = Byte.SIZE - i % Byte.SIZE;
            bits[i] = (bytes[i / Byte.SIZE] >>> pos) == 1;
        }

        return bits;
    }

    public static String extractString(byte[] bytes, int offset, int maxLen, char terminator) {
        if (offset >= bytes.length) {
            return "";
        }

        if (offset + maxLen > bytes.length) {
            maxLen = bytes.length - offset;
        }

        int end = -1;

        // Check up to the specified length or termination
        for (int i = 0; i < maxLen; i++) {
            byte b = bytes[offset + i];

            if (b == terminator) {
                break;
            }

            if (b != BLANK_SPACE) {
                end = i;
            }
        }

        byte[] sanitized = new byte[end + 1];
        boolean checking = FitsFactory.getCheckAsciiStrings();

        // Up to the specified length or terminator
        for (int i = 0; i <= end; i++) {
            byte b = bytes[offset + i];

            if (checking && (b < BLANK_SPACE || b > MAX_ASCII_VALUE)) {
                if (!wroteCheckingError) {
                    LOG.warning("WARNING! Converting invalid table string character[s] to spaces.");
                    wroteCheckingError = true;
                }
                b = (byte) BLANK_SPACE;
            }

            sanitized[i] = b;
        }

        return AsciiFuncs.asciiString(sanitized);
    }

    /**
     * Converts a FITS byte sequence to a Java string array, triming spaces at the heads and tails of each element.
     * While FITS typically considers leading spaces significant, this library has been removing them from regularly
     * shaped string arrays for a very long time, apparently based on request by users then... Even though it seems like
     * a bad choice, since users could always call {@link String#trim()} if they needed to, we cannot easily go in the
     * reverse direction. At this point we have no real choice but to continue the tradition, lest we want to break
     * exising applications, which count on this behavior.
     * 
     * @return        Convert bytes to Strings, removing leading and trailing spaces from each entry.
     *
     * @param  bytes  byte array to convert
     * @param  maxLen the max string length
     */
    public static String[] byteArrayToStrings(byte[] bytes, int maxLen) {
        // Note that if a String in a binary table contains an internal 0,
        // the FITS standard says that it is to be considered as terminating
        // the string at that point, so that software reading the
        // data back may not include subsequent characters.
        // No warning of this truncation is given.

        String[] res = new String[bytes.length / maxLen];
        for (int i = 0; i < res.length; i++) {
            res[i] = extractString(bytes, i * maxLen, maxLen, '\0').trim();
        }
        return res;
    }

    /**
     * Converts a FITS representation of boolean values as bytes to a java boolean array. This implementation does not
     * handle FITS <code>null</code> values.
     * 
     * @return       Convert an array of bytes to booleans.
     *
     * @param  bytes the array of bytes to get the booleans from.
     * 
     * @see          FitsDecoder#booleanFor(int)
     */
    static boolean[] byteToBoolean(byte[] bytes) {
        boolean[] bool = new boolean[bytes.length];

        for (int i = 0; i < bytes.length; i++) {
            bool[i] = FitsDecoder.booleanFor(bytes[i]);
        }
        return bool;
    }

    /**
     * Gets the file offset for the given IO resource.
     * 
     * @return   The offset from the beginning of file (if random accessible), or -1 otherwise.
     *
     * @param  o the stream to get the position
     */
    public static long findOffset(Closeable o) {
        if (o instanceof RandomAccess) {
            return ((RandomAccess) o).getFilePointer();
        }
        return -1;
    }

    /**
     * Gets and input stream for a given URL resource.
     * 
     * @return             Get a stream to a URL accommodating possible redirections. Note that if a redirection request
     *                         points to a different protocol than the original request, then the redirection is not
     *                         handled automatically.
     *
     * @param  url         the url to get the stream from
     * @param  level       max levels of redirection
     *
     * @throws IOException if the operation failed
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
     * Returns the maximum String length in an array of Strings.
     * 
     * @return               Get the maximum length of a String in a String array.
     *
     * @param  strings       array of strings to check
     *
     * @throws FitsException if the operation failed
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
     * Adds the necessary amount of padding needed to complete the last FITS block.
     *
     * @param  stream        stream to pad
     * @param  size          the current size of the stream (total number of bytes written to it since the beginning of
     *                           the FITS).
     *
     * @throws FitsException if the operation failed
     * 
     * @see                  #pad(ArrayDataOutput, long, byte)
     */
    public static void pad(ArrayDataOutput stream, long size) throws FitsException {
        pad(stream, size, (byte) 0);
    }

    /**
     * Adds the necessary amount of padding needed to complete the last FITS block., usign the designated padding byte
     * value.
     *
     * @param  stream        stream to pad
     * @param  size          the current size of the stream (total number of bytes written to it since the beginning of
     *                           the FITS).
     * @param  fill          the byte value to use for the padding
     *
     * @throws FitsException if the operation failed
     * 
     * @see                  #pad(ArrayDataOutput, long)
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
     * @deprecated      see Use {@link #padding(long)} instead.
     * 
     * @return          How many bytes are needed to fill a 2880 block?
     *
     * @param      size the size without padding
     */
    public static int padding(int size) {
        return padding((long) size);
    }

    /**
     * Calculated the amount of padding we need to add given the current size of a FITS file (under construction)
     * 
     * @param  size the current size of our FITS file before the padding
     * 
     * @return      the number of bytes of padding we need to add at the end to complete the FITS block.
     * 
     * @see         #addPadding(long)
     * @see         #pad(ArrayDataOutput, long)
     */
    public static int padding(long size) {

        int mod = (int) (size % FitsFactory.FITS_BLOCK_SIZE);
        if (mod > 0) {
            mod = FitsFactory.FITS_BLOCK_SIZE - mod;
        }
        return mod;
    }

    /**
     * Attempts to reposition a FITS input ot output. The call will succeed only if the underlying input or output is
     * random accessible. Othewise, an exception will be thrown.
     *
     * @deprecated               This method wraps an {@link IOException} into a {@link FitsException} for no good
     *                               reason really. A revision of the API could reduce the visibility of this method,
     *                               and/or procees the underlying exception instead.
     *
     * @param      o             the FITS input or output
     * @param      offset        the offset to position it to.
     *
     * @throws     FitsException if the underlying input/output is not random accessible or if the requested position is
     *                               invalid.
     */
    @Deprecated
    public static void reposition(FitsIO o, long offset) throws FitsException {
        // TODO AK: argument should be RandomAccess instead of Closeable, since
        // that's the only type we actually handle...

        if (o == null) {
            throw new FitsException("Attempt to reposition null stream");
        }

        if (!(o instanceof RandomAccess) || offset < 0) {
            throw new FitsException(
                    "Invalid attempt to reposition stream " + o + " of type " + o.getClass().getName() + " to " + offset);
        }

        try {
            ((RandomAccess) o).seek(offset);
        } catch (IOException e) {
            throw new FitsException("Unable to repostion stream " + o + " of type " + o.getClass().getName() + " to "
                    + offset + ": " + e.getMessage(), e);
        }
    }

    private static void stringToBytes(String s, byte[] res, int offset, int len) {
        int l = 0;
        if (s != null) {
            byte[] b = AsciiFuncs.getBytes(s);
            l = Math.min(b.length, len);
            if (l > 0) {
                System.arraycopy(b, 0, res, offset, l);
            }
        }
        Arrays.fill(res, offset + l, offset + len, (byte) ' ');
    }

    public static byte[] stringToByteArray(String s, int len) {
        byte[] res = new byte[len];
        stringToBytes(s, res, 0, len);
        return res;
    }

    /**
     * Convert an array of Strings to bytes.
     *
     * @return             the resulting bytes
     *
     * @param  stringArray the array with Strings
     * @param  len         the number of bytes used for each string element. The string will be truncated ot padded as
     *                         necessary to fit into that size.
     */
    public static byte[] stringsToByteArray(String[] stringArray, int len) {
        byte[] res = new byte[stringArray.length * len];
        for (int i = 0; i < stringArray.length; i++) {
            stringToBytes(stringArray[i], res, i * len, len);
        }
        return res;
    }
}
