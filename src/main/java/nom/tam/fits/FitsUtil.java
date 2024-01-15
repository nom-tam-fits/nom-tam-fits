package nom.tam.fits;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2024 nom-tam-fits
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
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.util.ArrayDataOutput;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.FitsDecoder;
import nom.tam.util.FitsEncoder;
import nom.tam.util.FitsIO;
import nom.tam.util.RandomAccess;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Static utility functions used throughout the FITS classes.
 */
public final class FitsUtil {

    /** Lowest ASCII value that can be in FITS strings */
    static final byte BLANK_SPACE = 0x20;

    /** Highest ASCII value that can be in FITS strings */
    static final byte MIN_ASCII_VALUE = 0x20;

    /** Highest ASCII value that can be in FITS strings */
    static final byte MAX_ASCII_VALUE = 0x7e;

    /** Highest ASCII value that can be in FITS strings */
    static final byte ASCII_NULL = 0x00;

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

    /**
     * Converts an array of <code>boolean</code> or {@link Boolean} values to FITS logicals (bytes containint 'T', 'F'
     * or '\0'). The shapes and size of the resulting array matches that of the input. Values of '\0' are converted to
     * <code>null</code> values.
     * 
     * @param  o a new array of <code>Boolean</code>
     * 
     * @return   and array of FITS logical values with the same size and shape as the input
     * 
     * @see      #bytesToBooleanObjects(Object)
     * @see      #byteToBoolean(byte[])
     * 
     * @since    1.18
     */
    static Object booleansToBytes(Object o) {
        if (o == null) {
            return FitsEncoder.byteForBoolean(null);
        }

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

    /**
     * Converts an array of FITS logicals (bytes containint 'T', 'F' or '\0') to an array of {@link Boolean}. The shapes
     * and size of the resulting array matches that of the input. Values of '\0' are converted to <code>null</code>
     * values.
     * 
     * @param  bytes and array of FITS logical values
     * 
     * @return       a new array of <code>Boolean</code> with the same size and shape as the input
     * 
     * @see          #booleansToBytes(Object)
     * 
     * @since        1.18
     */
    static Object bytesToBooleanObjects(Object bytes) {
        if (bytes instanceof Byte) {
            return FitsDecoder.booleanObjectFor(((Number) bytes).intValue());
        }

        if (bytes instanceof byte[]) {
            byte[] b = (byte[]) bytes;
            Boolean[] bool = new Boolean[b.length];
            for (int i = 0; i < b.length; i++) {
                bool[i] = FitsDecoder.booleanObjectFor(b[i]);
            }
            return bool;
        }

        if (bytes instanceof Object[]) {
            Object[] array = (Object[]) bytes;
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

        throw new IllegalArgumentException("Cannot convert to boolean values: " + bytes.getClass().getName());
    }

    /**
     * Converts an array of booleans into the bits packed into a block of bytes.
     * 
     * @param  bits an array of bits
     * 
     * @return      a new byte array containing the packed bits (in big-endian order)
     * 
     * @see         #bytesToBits(Object)
     * 
     * @since       1.18
     */
    static byte[] bitsToBytes(boolean[] bits) throws IllegalArgumentException {
        byte[] bytes = new byte[(bits.length + Byte.SIZE - 1) / Byte.SIZE];
        for (int i = 0; i < bits.length; i++) {
            if (bits[i]) {
                int pos = Byte.SIZE - 1 - i % Byte.SIZE;
                bytes[i / Byte.SIZE] |= 1 << pos;
            }
        }

        return bytes;
    }

    /**
     * Converts an array of bit segments into the bits packed into a blocks of bytes.
     * 
     * @param  bits an array of bits
     * @param  l    the number of bits in a segment that are to be kept together
     * 
     * @return      a new byte array containing the packed bits (in big-endian order)
     * 
     * @see         #bytesToBits(Object)
     * 
     * @since       1.18
     */
    static byte[] bitsToBytes(boolean[] bits, int l) throws IllegalArgumentException {
        int n = bits.length / l; // Number of bit segments
        int bl = (l + Byte.SIZE - 1) / Byte.SIZE; // Number of bytes per segment
        byte[] bytes = new byte[n * bl]; // The converted byte array

        for (int i = 0; i < n; i++) {
            int off = i * l; // bit offset
            int boff = i * bl; // byte offset

            for (int j = 0; j < l; j++) {
                if (bits[off + j]) {
                    int pos = Byte.SIZE - 1 - j % Byte.SIZE;
                    bytes[boff + (j / Byte.SIZE)] |= 1 << pos;
                }
            }
        }

        return bytes;
    }

    /**
     * Converts the bits packed into a block of bytes into a boolean array.
     * 
     * @param  bytes the byte array containing the packed bits (in big-endian order)
     * @param  count the number of bits to extract
     * 
     * @return       an array of boolean with the separated bit values.
     * 
     * @see          #bitsToBytes(Object)
     * 
     * @since        1.18
     */
    static boolean[] bytesToBits(byte[] bytes, int count) {
        boolean[] bits = new boolean[count];

        for (int i = 0; i < bits.length; i++) {
            int pos = Byte.SIZE - 1 - i % Byte.SIZE;
            bits[i] = ((bytes[i / Byte.SIZE] >>> pos) & 1) == 1;
        }

        return bits;
    }

    /**
     * Extracts a string from a byte array at the specified offset, maximal length and termination byte. This method
     * trims trailing spaces but not leading ones.
     * 
     * @param  bytes      an array of ASCII bytes
     * @param  offset     the array index at which the string begins
     * @param  maxLen     the maximum number of bytes to extract from the position
     * @param  terminator the byte value that terminates the string, such as 0x00.
     * 
     * @return            a new String with the relevant bytes, with length not exceeding the specified limit.
     * 
     * @since             1.18
     */
    static String extractString(byte[] bytes, ParsePosition pos, int maxLen, byte terminator) {
        int offset = pos.getIndex();

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
            pos.setIndex(offset + i);

            if (b == terminator || b == 0) {
                break;
            }

            if (b != BLANK_SPACE) {
                end = i;
            }
        }

        pos.setIndex(pos.getIndex() + 1);

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
                b = BLANK_SPACE;
            }

            sanitized[i] = b;
        }

        return AsciiFuncs.asciiString(sanitized);
    }

    /**
     * Converts a FITS byte sequence to a Java string array, triming spaces at the heads and tails of each element.
     * While FITS typically considers leading spaces significant, this library has been removing them from regularly
     * shaped string arrays for a very long time, apparently based on request by users then... Even though it seems like
     * a bad choice, since users could always call {@link String#trim()} if they needed to, we cannot recover the
     * leading spaces once the string was trimmed. At this point we have no real choice but to continue the tradition,
     * lest we want to break exising applications, which may rely on this behavior.
     * 
     * @return            Convert bytes to Strings, removing leading and trailing spaces from each entry.
     *
     * @param      bytes  byte array to convert
     * @param      maxLen the max string length
     * 
     * @deprecated        (<i>for internal use</i>) No longer used internally, will be removed in the future.
     */
    public static String[] byteArrayToStrings(byte[] bytes, int maxLen) {
        // Note that if a String in a binary table contains an internal 0,
        // the FITS standard says that it is to be considered as terminating
        // the string at that point, so that software reading the
        // data back may not include subsequent characters.
        // No warning of this truncation is given.

        String[] res = new String[bytes.length / maxLen];
        for (int i = 0; i < res.length; i++) {
            res[i] = extractString(bytes, new ParsePosition(i * maxLen), maxLen, (byte) '\0').trim();
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
     * Parses a logical value from a string, using loose conversion. The string may contain either 'true'/'false' or
     * 'T'/'F' (case insensitive), or else a zero (<code>false</code>) or non-zero number value (<code>true</code>). All
     * other strings will return <code>null</code> corresponding to an undefined logical value.
     * 
     * @param  s A string
     * 
     * @return   <code>true</code>, <code>false</code>, or <code>null</code> (if undefined).
     */
    @SuppressFBWarnings(value = "NP_BOOLEAN_RETURN_NULL", justification = "null has specific meaning here")
    static Boolean parseLogical(String s) {
        if (s == null) {
            return null;
        }

        s = s.trim();

        if (s.isEmpty()) {
            return null;
        }
        if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("t")) {
            return true;
        }
        if (s.equalsIgnoreCase("false") || s.equalsIgnoreCase("f")) {
            return false;
        }

        try {
            long l = Long.parseLong(s);
            return l != 0;
        } catch (NumberFormatException e) {
            // Nothing to do....
        }

        try {
            double d = Double.parseDouble(s);
            if (!Double.isNaN(d)) {
                return d != 0;
            }
        } catch (NumberFormatException e) {
            // Nothing to do....
        }

        return null;
    }

    /**
     * Gets the file offset for the given IO resource.
     * 
     * @return       The offset from the beginning of file (if random accessible), or -1 otherwise.
     *
     * @param      o the stream to get the position
     * 
     * @deprecated   (<i>for internal use</i>) Visibility may be reduced to the package level in the future.
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
     * Returns the maximum string length in an array.
     * 
     * @return             the maximum length of string in an array.
     *
     * @param      strings array of strings to check
     * 
     * @deprecated         (<i>for internal use</i>) No longer used internally, may be removed in the future.
     */
    public static int maxLength(String[] strings) {
        int max = 0;
        for (String element : strings) {
            if (element != null && element.length() > max) {
                max = element.length();
            }
        }
        return max;
    }

    /**
     * Returns the maximum string length in an array of strings. Non-string elements nd null values are ignored.
     * 
     * @return   the maximum length of strings in an array.
     *
     * @param  o array of strings to check
     */
    static int maxStringLength(Object o) {
        if (o instanceof String) {
            return ((String) o).length();
        }

        int max = 0;

        if (o instanceof Object[]) {
            for (Object e : (Object[]) o) {
                if (e == null) {
                    continue;
                }

                int l = maxStringLength(e);
                if (l > max) {
                    max = l;
                }
            }
        }

        return max;
    }

    /**
     * Returns the minimum string length in an array of strings. Non-string elements nd null values are ignored.
     * 
     * @return   the minimum length of strings in an array.
     *
     * @param  o strings array of strings to check
     */
    static int minStringLength(Object o) {
        if (o instanceof String) {
            return ((String) o).length();
        }

        int min = -1;

        if (o instanceof Object[]) {
            for (Object e : (Object[]) o) {
                if (e == null) {
                    return 0;
                }

                int l = minStringLength(e);
                if (l == 0) {
                    return 0;
                }

                if (min < 0 || l < min) {
                    min = l;
                }
            }
        }

        return min < 0 ? 0 : min;
    }

    /**
     * Adds the necessary amount of padding needed to complete the last FITS block.
     *
     * @param      stream        stream to pad
     * @param      size          the current size of the stream (total number of bytes written to it since the beginning
     *                               of the FITS).
     *
     * @throws     FitsException if the operation failed
     * 
     * @see                      #pad(ArrayDataOutput, long, byte)
     * 
     * @deprecated               (<i>for internal use</i>) Visibility may be reduced to package level in the future
     */
    public static void pad(ArrayDataOutput stream, long size) throws FitsException {
        pad(stream, size, (byte) 0);
    }

    /**
     * Adds the necessary amount of padding needed to complete the last FITS block., usign the designated padding byte
     * value.
     *
     * @param      stream        stream to pad
     * @param      size          the current size of the stream (total number of bytes written to it since the beginning
     *                               of the FITS).
     * @param      fill          the byte value to use for the padding
     *
     * @throws     FitsException if the operation failed
     * 
     * @see                      #pad(ArrayDataOutput, long)
     * 
     * @deprecated               (<i>for internal use</i>) Visibility may be reduced to private in the future
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

    /**
     * Converts a string to ASCII bytes in the specified array, padding (with 0x00) or truncating as necessary to
     * provide the expected length at the specified arrya offset.
     * 
     * @param s      a string
     * @param res    the byte array into which to extract the ASCII bytes
     * @param offset array index in the byte array at which the extracted bytes should begin
     * @param len    the maximum number of bytes to extract, truncating or padding (with 0x00) as needed.
     * @param pad    the byte value to use to pad the remainder of the string
     */
    private static void stringToBytes(String s, byte[] res, int offset, int len, byte pad) {
        int l = 0;

        if (s != null) {
            byte[] b = AsciiFuncs.getBytes(s);
            l = Math.min(b.length, len);
            if (l > 0) {
                System.arraycopy(b, 0, res, offset, l);
            }
        }

        // Terminate and pad as necessary
        if (l < len) {
            Arrays.fill(res, offset + l, offset + len, pad);
        }
    }

    /**
     * Converts a string to an array of ASCII bytes, padding (with 0x00) or truncating as necessary to provide the
     * expected length.
     * 
     * @param  s   a string
     * @param  len the number of bytes for the return value, also the maximum number of bytes that are extracted from
     *                 the string
     * 
     * @return     a byte array of the specified length containing the truncated or padded string value.
     * 
     * @see        #stringsToByteArray(String[], int, byte) q
     * 
     * @since      1.18
     */
    static byte[] stringToByteArray(String s, int len) {
        byte[] res = new byte[len];
        stringToBytes(s, res, 0, len, BLANK_SPACE);
        return res;
    }

    /**
     * Convert an array of Strings to bytes. padding (with 0x00) or truncating as necessary to provide the expected
     * length.
     *
     * @return                 the resulting bytes
     *
     * @param      stringArray the array with Strings
     * @param      len         the number of bytes used for each string element. The string will be truncated ot padded
     *                             as necessary to fit into that size.
     * 
     * @deprecated             (<i>for internal use</i>) Visibility may be reduced to package level in the future.
     */
    public static byte[] stringsToByteArray(String[] stringArray, int len) {
        return stringsToByteArray(stringArray, len, BLANK_SPACE);
    }

    /**
     * Convert an array of Strings to bytes. padding (with 0x00) or truncating as necessary to provide the expected
     * length.
     *
     * @return             the resulting bytes
     *
     * @param  stringArray the array with Strings
     * @param  len         the number of bytes used for each string element. The string will be truncated ot padded as
     *                         necessary to fit into that size.
     * @param  pad         the byte value to use for padding strings as necessary to the requisite length.
     */
    static byte[] stringsToByteArray(String[] stringArray, int len, byte pad) {
        byte[] res = new byte[stringArray.length * len];
        for (int i = 0; i < stringArray.length; i++) {
            stringToBytes(stringArray[i], res, i * len, len, pad);
        }
        return res;
    }

    /**
     * Convert an array of strings to a delimited sequence of bytes, e.g. for sequentialized variable-sized storage of
     * multiple string elements. The last string component is terminated by an ASCII NUL (0x00).
     *
     * @return        the resulting bytes
     *
     * @param  array  the array with Strings
     * @param  maxlen the maximum string length or -1 of unlimited.
     * @param  delim  the byte value that delimits string components
     * 
     * @see           #stringToByteArray(String, int)
     * 
     * @since         1.18
     */
    static byte[] stringsToDelimitedBytes(String[] array, int maxlen, byte delim) {
        int l = array.length - 1;
        for (String s : array) {
            l += (s == null) ? 1 : Math.max(s.length() + 1, maxlen);
        }
        byte[] b = new byte[l];
        l = 0;
        for (String s : array) {
            if (s != null) {
                stringToBytes(s, b, l, s.length(), BLANK_SPACE);
                l += s.length();
            }
            b[l++] = delim;
        }
        b[l - 1] = (byte) 0;
        return b;
    }

    /**
     * Extracts strings from a packed delimited byte sequence. Strings start either immediately after the prior string
     * reached its maximum length, or else immediately after the specified delimiter byte value.
     * 
     * @param  bytes  bytes containing the packed strings
     * @param  maxlen the maximum length of individual string components
     * @param  delim  the byte value that delimits strings shorter than the maximum length
     * 
     * @return        An array of the extracted strings
     *
     * @see           #stringsToDelimitedBytes(String[], int, byte)
     * 
     * @since         1.18
     */
    private static String[] delimitedBytesToStrings(byte[] bytes, byte delim) {
        ArrayList<String> s = new ArrayList<>();
        ParsePosition pos = new ParsePosition(0);
        while (pos.getIndex() < bytes.length) {
            s.add(extractString(bytes, pos, bytes.length, delim));
        }
        String[] a = new String[s.size()];
        s.toArray(a);
        return a;
    }

    /**
     * Extracts strings from a packed delimited byte sequence. Strings start either immediately after the prior string
     * reached its maximum length, or else immediately after the specified delimiter byte value.
     * 
     * @param  bytes  bytes containing the packed strings
     * @param  maxlen the maximum length of individual string components
     * @param  delim  the byte value that delimits strings shorter than the maximum length
     * 
     * @return        An array of the extracted strings
     *
     * @see           #stringsToDelimitedBytes(String[], int, byte)
     * 
     * @since         1.18
     */
    static String[] delimitedBytesToStrings(byte[] bytes, int maxlen, byte delim) {
        if (maxlen <= 0) {
            return delimitedBytesToStrings(bytes, delim);
        }

        String[] res = new String[(bytes.length + maxlen - 1) / maxlen];
        ParsePosition pos = new ParsePosition(0);
        for (int i = 0; i < res.length; i++) {
            res[i] = extractString(bytes, pos, maxlen, delim);
        }
        return res;
    }

}
