package nom.tam.fits.utilities;

import static nom.tam.fits.header.Checksum.CHECKSUM;
import static nom.tam.fits.header.Checksum.DATASUM;

/*-
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2022 nom-tam-fits
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

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Data;
import nom.tam.fits.FitsElement;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.util.FitsIO;
import nom.tam.util.FitsOutputStream;
import nom.tam.util.RandomAccess;

/**
 * <p>
 * Helper class for dealing with FITS checksums. It implements the Seaman-Pence 32-bit 1's complement checksum
 * calculation. The implementation accumulates in two 64-bit integer values the low and high-order 16-bits of adjacent
 * 4-byte groups. A carry-over of bits are calculated only at the end of the loop. Given the use of 64-bit accumulators,
 * overflow would occur approximately at 280 billion short values.
 * </p>
 * <p>
 * This updated version of the class is a little more flexible than the prior incarnation. Specifically it allows
 * incremental updates to data sums, and provides methods for dealing with partial checksums (e.g. from modified
 * segments of the data), which may be used e.g. to calculate delta sums from changed blocks of data. The new
 * implementation provides methods for decoding encoded checksums, and for calculating checksums directly from files
 * without the need to read potentially huge data into RAM first, and for easily accessing the values stored in FITS
 * headers.
 * </p>
 * <p>
 * See <a href="http://arxiv.org/abs/1201.1345" target="@top">FITS Checksum Proposal</a>
 * </p>
 * 
 * @author R J Mather, Tony Johnson, Attila Kovacs
 * 
 * @see nom.tam.fits.header.Checksum#CHECKSUM
 */
public final class FitsCheckSum {

    private static final int CHECKSUM_BLOCK_SIZE = 4;
    private static final int CHECKSUM_BLOCK_MASK = CHECKSUM_BLOCK_SIZE - 1;
    private static final int CHECKSUM_STRING_SIZE = 16;
    private static final int SHIFT_2_BYTES = 16;
    private static final int MASK_2_BYTES = 0xffff;
    private static final int MASK_4_BYTES = 0xffffffff;
    private static final int MASK_BYTE = 0xff;
    private static final int ASCII_ZERO = '0';
    private static final int BUFFER_SIZE = 0x8000; // 32 kB

    private static final int[] SELECT_BYTE = {24, 16, 8, 0};
    private static final String EXCLUDE = ":;<=>?@[\\]^_`";
    private static final String CHECKSUM_DEFAULT = "0000000000000000";

    private FitsCheckSum() {
    }

    /**
     * Internal class for accumulating FITS checksums.
     */
    private static class Checksum {
        private long h, l;

        Checksum(long prior) throws IllegalArgumentException {
            h = (prior >>> SHIFT_2_BYTES) & MASK_2_BYTES;
            l = prior & MASK_2_BYTES;
        }

        void add(int i) {
            h += i >>> SHIFT_2_BYTES;
            l += i & MASK_2_BYTES;
        }

        long getChecksum() {
            long hi = h & MASK_4_BYTES; // as unsigned 32-bit integer
            long lo = l & MASK_4_BYTES;

            for (;;) {
                long hicarry = hi >>> SHIFT_2_BYTES;
                long locarry = lo >>> SHIFT_2_BYTES;
                if ((hicarry | locarry) == 0) {
                    break;
                }
                hi = (hi & MASK_2_BYTES) + locarry;
                lo = (lo & MASK_2_BYTES) + hicarry;
            }
            return (hi << SHIFT_2_BYTES) | lo;
        }

    }

    private static class PipeWriter extends Thread {
        private Exception exception;
        private PipedOutputStream out;
        private FitsElement data;

        PipeWriter(FitsElement data, PipedInputStream in) throws IOException {
            this.data = data;
            this.out = new PipedOutputStream(in);
        }

        @Override
        public void run() {
            exception = null;
            try (FitsOutputStream fos = new FitsOutputStream(out)) {
                data.write(fos);
            } catch (Exception e) {
                exception = e;
            }
        }

        public Exception getException() {
            return exception;
        }
    }

    /**
     * Computes the checksum for a byte array.
     * 
     * @param data the byte sequence for which to calculate a chekcsum
     * 
     * @return the 32bit checksum in the range from 0 to 2^32-1
     * 
     * @see #checksum(byte[], int, int)
     */
    public static long checksum(byte[] data) {
        return checksum(ByteBuffer.wrap(data));
    }

    /**
     * Computes the checksum for a segment of a byte array.
     * 
     * @param data the byte sequence for which to calculate a chekcsum
     * @param from Stating index of bytes to include in checksum calculation
     * @param to Ending index (exclusive) of bytes to include in checksum
     * 
     * @return the 32-bit checksum in the range from 0 to 2^32-1
     * 
     * @see #checksum(RandomAccess, long, long)
     * 
     * @since 1.17
     */
    public static long checksum(byte[] data, int from, int to) {
        return checksum(ByteBuffer.wrap(data, from, to));
    }

    /**
     * Computes the checksum from a buffer. This method can be used to calculate partial checksums for any data that can
     * be wrapped into a buffer one way or another. As such it is suitable for calculating partial sums from segments of
     * data that can be used to update datasums incrementally (e.g. by incrementing the datasum with the difference of
     * the checksum of the new data segment vs the old data segment), or for updating the checksum for new data that has
     * been added to the existing data (e.g. new rows in a binary table) as long as the modified data segment is a
     * multiple of 4 bytes.
     * 
     * @param data the buffer for which to calculate a (partial) checksum
     * 
     * @return the computed 32-bit unsigned checksum as a Java <code>long</code>
     * 
     * @since 1.17
     * 
     * @see #checksum(Data)
     * @see #checksum(Header)
     * @see #sumOf(long...)
     * @see #differenceOf(long, long)
     */
    public static long checksum(ByteBuffer data) {
        Checksum sum = new Checksum(0);
        if (!(data.remaining() % CHECKSUM_BLOCK_SIZE == 0)) {
            throw new IllegalArgumentException("fits blocks must always be divisible by 4");
        }
        data.position(0);
        data.order(ByteOrder.BIG_ENDIAN);
        IntBuffer iData = data.asIntBuffer();
        while (iData.hasRemaining()) {
            sum.add(iData.get());
        }
        return sum.getChecksum();
    }

    private static long checksum(InputStream in) throws IOException {
        Checksum sum = new Checksum(0);
        DataInputStream din = new DataInputStream(in);

        for (;;) {
            try {
                sum.add(din.readInt());
            } catch (EOFException e) {
                break;
            }
        }

        return sum.getChecksum();
    }

    private static long compute(final FitsElement data) throws FitsException {
        try (PipedInputStream in = new PipedInputStream()) {
            PipeWriter writer = new PipeWriter(data, in);
            writer.start();

            long sum = checksum(in);
            in.close();

            writer.join();
            if (writer.getException() != null) {
                throw writer.getException();
            }

            return sum;
        } catch (Exception e) {
            if (e instanceof FitsException) {
                throw (FitsException) e;
            }
            throw new FitsException("Exception while checksumming FITS element: " + e.getMessage(), e);
        }
    }

    /**
     * Computes the checksum for a FITS data object, e.g. to be used with {@link #setDatasum(Header, long)}. This call
     * will always calculate the checksum for the data in memory, and as such load deferred mode data into RAM as
     * necessary to perform the calculation. If you rather not load a huge amount of data into RAM, you might consider
     * using {@link #checksum(RandomAccess, long, long)} instead.
     * 
     * @param data The FITS data object for which to calculate a checksum
     * 
     * @return The checksum of the data
     * 
     * @throws FitsException If there was an error serializing the data object
     * 
     * @see Data#calcChecksum()
     * @see nom.tam.fits.Fits#calcDatasum(int)
     * @see #checksum(RandomAccess, long, long)
     * @see #setDatasum(Header, long)
     * @see #setChecksum(BasicHDU)
     * 
     * @since 1.17
     */
    public static long checksum(Data data) throws FitsException {
        return compute(data);
    }

    /**
     * Computes the checksum for a FITS header object. If the header already contained a CHECKSUM card, it will be kept.
     * Otherwise, it will add a CHECKSUM card to the header with the newly calculated sum.
     * 
     * @param header The FITS header object for which to calculate a checksum
     * 
     * @return The checksum of the data
     * 
     * @throws FitsException If there was an error serializing the FITS header
     * 
     * @see #checksum(Data)
     * 
     * @since 1.17
     */
    public static long checksum(Header header) throws FitsException {
        HeaderCard hc = header.findCard(CHECKSUM);
        String prior = null;

        if (hc != null) {
            prior = hc.getValue();
            hc.setValue(CHECKSUM_DEFAULT);
        } else {
            hc = header.addValue(CHECKSUM, CHECKSUM_DEFAULT);
        }

        long sum = compute(header);

        hc.setValue(prior == null ? encode(sum) : prior);

        return sum;
    }

    /**
     * Calculates the FITS checksum for a HDU, e.g to compare agains the value stored under the CHECKSUM header keyword.
     * The
     * 
     * @param hdu The Fits HDU for which to calculate a checksum, including both the header and data segments.
     * 
     * @return The calculated checksum for the given HDU.
     * 
     * @throws FitsException if there was an error accessing the contents of the HDU.
     * 
     * @see BasicHDU#calcChecksum()
     * @see #checksum(Data)
     * @see #sumOf(long...)
     *
     * @since 1.17
     */
    public static long checksum(BasicHDU<?> hdu) throws FitsException {
        return sumOf(checksum(hdu.getHeader()), checksum(hdu.getData()));
    }

    /**
     * Computes the checksum directly from a region of a random access file, by buffering moderately sized chunks from
     * the file as necessary. The file may be very large, up to the full range of 64-bit addresses.
     * 
     * @param f the random access file, from which to compute a checksum
     * @param from the starting position in the file, where to start computing the checksum from.
     * @param size the number of bytes in the file to include in the checksum calculation.
     * 
     * @return the checksum for the given segment of the file
     * 
     * @throws IOException if there was a problem accessing the file during the computation.
     * 
     * @since 1.17
     * 
     * @see #checksum(ByteBuffer)
     * @see #checksum(Data)
     */
    public static long checksum(RandomAccess f, long from, long size) throws IOException {
        if (f == null) {
            return 0L;
        }

        int len = (int) Math.min(BUFFER_SIZE, size);
        byte[] buf = new byte[len];
        long oldpos = f.position();
        f.position(from);
        long sum = 0;

        while (size > 0) {
            len = (int) Math.min(BUFFER_SIZE, size);
            len = f.read(buf, 0, len);
            sum = sumOf(sum, checksum(buf, 0, len));
            from += len;
            size -= len;
        }

        f.position(oldpos);
        return sum;
    }

    /**
     * @deprecated Use {@link #encode(long, boolean)} instead.
     * 
     * @param c The calculated 32-bit (unsigned) checksum
     * @param compl Whether to complement the raw checksum (as defined by the convention).
     * 
     * @return The encoded checksum, suitably encoded for use with the CHECKSUM header
     */
    @Deprecated
    public static String checksumEnc(final long c, final boolean compl) {
        return encode(c, compl);
    }

    /**
     * Encodes the complemented checksum. It is the same as <code>encode(checksum, true)</code>.
     * 
     * @param checksum The calculated 32-bit (unsigned) checksum
     * 
     * @return The encoded checksum, suitably encoded for use with the CHECKSUM header
     * 
     * @see #decode(String)
     * 
     * @since 1.17
     */
    public static String encode(long checksum) {
        return encode(checksum, true);
    }

    /**
     * Encodes the given checksum as is or by its complement.
     * 
     * @param checksum The calculated 32-bit (unsigned) checksum
     * @param compl If <code>true</code> the complement of the specified value will be encoded. Otherwise, the value as
     *            is will be encoded. (FITS normally uses the complemenyed value).
     * 
     * @return The encoded checksum, suitably encoded for use with the CHECKSUM header
     * 
     * @see #decode(String, boolean)
     * 
     * @since 1.17
     */
    public static String encode(long checksum, boolean compl) {
        if (compl) {
            checksum = ~checksum & FitsIO.INTEGER_MASK;
        }

        final byte[] asc = new byte[CHECKSUM_STRING_SIZE];
        final byte[] ch = new byte[CHECKSUM_BLOCK_SIZE];
        final int sum = (int) checksum;

        for (int i = 0; i < CHECKSUM_BLOCK_SIZE; i++) {
            // each byte becomes four
            final int byt = MASK_BYTE & (sum >>> SELECT_BYTE[i]);

            Arrays.fill(ch, (byte) ((byt >>> 2) + ASCII_ZERO)); // quotient
            ch[0] += byt & CHECKSUM_BLOCK_MASK; // remainder

            for (int j = 0; j < CHECKSUM_BLOCK_SIZE; j += 2) {
                while (EXCLUDE.indexOf(ch[j]) >= 0 || EXCLUDE.indexOf(ch[j + 1]) >= 0) {
                    ch[j]++;
                    ch[j + 1]--;
                }
            }

            for (int j = 0; j < CHECKSUM_BLOCK_SIZE; j++) {
                int k = CHECKSUM_BLOCK_SIZE * j + i;
                k = (k == CHECKSUM_STRING_SIZE - 1) ? 0 : k + 1; // rotate right
                asc[k] = ch[j];
            }
        }

        return new String(asc, StandardCharsets.US_ASCII);
    }

    /**
     * Decodes an encoded (and complemented) checksum. The same as <code>decode(encoded, true)</code>, and the the
     * inverse of {@link #encode(long)}.
     * 
     * @param encoded The encoded checksum (16 character string)
     * 
     * @return The unsigned 32-bit integer complemeted checksum.
     * 
     * @throws IllegalArgumentException if the checksum string is invalid (wrong length or contains illegal ASCII
     *             characters)
     * 
     * @see #encode(long)
     * 
     * @since 1.17
     */
    public static long decode(String encoded) throws IllegalArgumentException {
        return decode(encoded, true);
    }

    /**
     * Decodes an encoded checksum, complementing it as required. It is the inverse of {@link #encode(long, boolean)}.
     * 
     * @param encoded the encoded checksum (16 character string)
     * @param compl whether to complement the checksum after decoding. Normally FITS uses complemented 32-bit checksums,
     *            so typically this optional argument should be <code>true</code>.
     * 
     * @return The unsigned 32-bit integer checksum.
     * 
     * @throws IllegalArgumentException if the checksum string is invalid (wrong length or contains illegal ASCII
     *             characters)
     * 
     * @see #encode(long, boolean)
     * 
     * @since 1.17
     */
    public static long decode(String encoded, boolean compl) throws IllegalArgumentException {
        byte[] bytes = encoded.getBytes(StandardCharsets.US_ASCII);
        if (bytes.length != CHECKSUM_STRING_SIZE) {
            throw new IllegalArgumentException("Bad checksum with " + bytes.length + " chars (expected 16)");
        }
        // Rotate the bytes one to the left
        byte tmp = bytes[0];
        System.arraycopy(bytes, 1, bytes, 0, CHECKSUM_STRING_SIZE - 1);
        bytes[CHECKSUM_STRING_SIZE - 1] = tmp;

        for (int i = 0; i < CHECKSUM_STRING_SIZE; i++) {
            if (bytes[i] < ASCII_ZERO) {
                throw new IllegalArgumentException("Bad checksum with illegal char " + Integer.toHexString(bytes[i])
                        + " at pos " + i + " (ASCII below 0x30)");
            }
            bytes[i] -= ASCII_ZERO;
        }

        ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);

        long sum = (bb.getInt() + bb.getInt() + bb.getInt() + bb.getInt());
        return (compl ? ~sum : sum) & FitsIO.INTEGER_MASK;
    }

    /**
     * Calculates the total checksum from partial sums. For example combining checksums from a header and data segment
     * of a HDU, or for composing a data checksum from image tiles.
     * 
     * @param parts The partial sums that are to be added together.
     * 
     * @return The aggregated checksum as a 32-bit unsigned value.
     * 
     * @see #differenceOf(long, long)
     * 
     * @since 1.17
     */
    public static long sumOf(long... parts) {
        Checksum sum = new Checksum(0);

        for (long part : parts) {
            sum.h += part >>> SHIFT_2_BYTES;
            sum.l += part & MASK_2_BYTES;
        }
        return sum.getChecksum();
    }

    /**
     * Subtracts a partial checksum from an aggregated total. One may use it, for example to update the datasum of a
     * large data, when modifying only a small segment of it. Thus, one would first subtract the checksum of the old
     * segment from tha prior datasum, and then add the checksum of the new data segment -- without hacing to
     * recalculate the checksum for the entire data again.
     * 
     * @param total The total checksum.
     * @param part The partial checksum to be subtracted from the total.
     * 
     * @return The checksum after subtracting the partial sum, as a 32-bit unsigned value.
     * 
     * @see #sumOf(long...)
     * 
     * @since 1.17
     */
    public static long differenceOf(long total, long part) {
        Checksum sum = new Checksum(total);
        sum.h -= (part >>> SHIFT_2_BYTES);
        sum.l -= part & MASK_2_BYTES;
        return sum.getChecksum();
    }

    /**
     * Sets the <code>DATASUM</code> and <code>CHECKSUM</code> keywords in a FITS header, based on the provided checksum
     * of the data (calculated elsewhere) and the checksum calculated afresh for the header.
     * 
     * @param header the header in which to store the <code>DATASUM</code> and <code>CHECKSUM</code> values
     * @param datasum the checksum for the data segment that follows the header in the HDU.
     * 
     * @throws FitsException if there was an error serializing the header. Note, the method never throws any other type
     *             of exception (including runtime exceptions), which are instead wrapped into a
     *             <code>FitsException</code> when they occur.
     * 
     * @see #setChecksum(BasicHDU)
     * @see #getStoredChecksum(Header)
     * @see #getStoredDatasum(Header)
     * 
     * @since 1.17
     */
    public static void setDatasum(Header header, long datasum) throws FitsException {
        // Add the freshly calculated datasum to the header, before calculating the checksum
        header.addValue(DATASUM, Long.toString(datasum));
        header.addValue(CHECKSUM, encode(sumOf(checksum(header), datasum)));
    }

    /**
     * Computes and sets the DATASUM and CHECKSUM keywords for a given HDU. This method calculates the sums from
     * scratch, and can be computationally expensive. There are less expensive incremental update methods that can be
     * used if the HDU already had sums recorded earlier, which need to be updated e.g. because there were modifications
     * to the header, or (parts of) the data.
     * 
     * @param hdu the HDU to be updated.
     * 
     * @throws FitsException if there was an error serializing the HDU. Note, the method never throws any other type of
     *             exception (including runtime exceptions), which are instead wrapped into a <code>FitsException</code>
     *             when they occur.
     * 
     * @see #setDatasum(Header, long)
     * @see #getStoredChecksum(Header)
     * @see #sumOf(long...)
     * @see #differenceOf(long, long)
     * 
     * @author R J Mather, Attila Kovacs
     */
    public static void setChecksum(BasicHDU<?> hdu) throws FitsException {
        try {
            setDatasum(hdu.getHeader(), checksum(hdu.getData()));
        } catch (FitsException e) {
            throw e;
        } catch (Exception e) {
            throw new FitsException("Exception while computing data checksum: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the DATASUM value stored in a FITS header.
     * 
     * @param header the FITS header
     * 
     * @return The stored datasum value (unsigned 32-bit integer) as a Java <code>long</code>.
     * 
     * @throws FitsException if the header does not contain a <code>DATASUM</code> entry.
     * 
     * @since 1.17
     * 
     * @see #getStoredChecksum(Header)
     * @see #setDatasum(Header, long)
     * @see BasicHDU#getStoredDatasum()
     */
    public static long getStoredDatasum(Header header) throws FitsException {
        HeaderCard hc = header.findCard(DATASUM);

        if (hc == null) {
            throw new FitsException("Header does not have a DATASUM value.");
        }

        return hc.getValue(Long.class, 0L) & FitsIO.INTEGER_MASK;
    }

    /**
     * Returns the decoded CHECKSUM value stored in a FITS header.
     * 
     * @param header the FITS header
     * 
     * @return The decoded <code>CHECKSUM</code> value (unsigned 32-bit integer) recorded in the header as a Java
     *             <code>long</code>.
     * 
     * @throws FitsException if the header does not contain a <code>CHECKSUM</code> entry, or it is invalid.
     * 
     * @since 1.17
     * 
     * @see #getStoredDatasum(Header)
     * @see #setChecksum(BasicHDU)
     * @see BasicHDU#getStoredChecksum()
     */
    public static long getStoredChecksum(Header header) throws FitsException {
        String encoded = header.getStringValue(CHECKSUM);

        if (encoded == null) {
            throw new FitsException("Header does not have a CHECKUM value.");
        }

        return decode(encoded);
    }
}
