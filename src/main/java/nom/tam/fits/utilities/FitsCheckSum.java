package nom.tam.fits.utilities;


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

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Data;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import static nom.tam.fits.header.Checksum.CHECKSUM;
import static nom.tam.fits.header.Checksum.DATASUM;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.FitsIO;
import nom.tam.util.FitsOutputStream;

/**
 * <p>
 * Helper class for dealing with FITS checksums. This updated version of the class
 * is a little more flexible than the prior incarnation, specifically that it allows the checksum 
 * to be accumulated across several invocations of the {@link #updateChecksum(Header, long)} method, 
 * and allows for the DATASUM keyword to be updated after data has been changed. It also 
 * provides a method for decoding an encoded checksum.
 * </p>
 * <p>
 * Implements the Seaman-Pence 32-bit 1's complement checksum calculation. 
 * The implementation accumulates in two 64-bit integer values the low and high-order
 * 16-bits of adjacent 4-byte groups. A carry-over of bits are calculate only
 * at the end of the loop. Given the use of 64-bit accumulators, overflow would occur
 * approximately at 280 billion short values. As such the class is not suitable for 
 * processing FITS HDUs over 580 GB (although it can be done by aggregating sums from
 * partial datablocks each of which are of smaller size).
 * </P>
 * 
 * @author R J Mather, Tony Johnson, Attila Kovacs
 * 
 * @see <a href="http://arxiv.org/abs/1201.1345" target="@top">FITS Checksum Proposal</a>
 * 
 * @see nom.tam.fits.header.Checksum#CHECKSUM
 */
public final class FitsCheckSum {

    private static final int CHECKSUM_BLOCK_SIZE = 4;
    private static final int CHECKSUM_BLOCK_MASK = CHECKSUM_BLOCK_SIZE - 1;
    private static final int CHECKSUM_STRING_SIZE = 16;
    private static final int SHIFT_2_BYTES = 16;
    private static final int MASK_2_BYTES = 0xffff;
    private static final int MASK_BYTE = 0xff;
    private static final int ASCII_ZERO = '0';

    private static final int[] SELECT_BYTE = {24, 16, 8, 0}; 
    private static final String EXCLUDE = ":;<=>?@[\\]^_`";

    private FitsCheckSum() {
    }
    /**
     * Class for handling FITS checksums. 
     */
    private static class Checksum {
        private long h, l;

        Checksum(long sum) throws IllegalArgumentException {
            h = (sum >>> SHIFT_2_BYTES) & MASK_2_BYTES;
            l = sum & MASK_2_BYTES;
        }

        void add(int i) {
            h += i >>> SHIFT_2_BYTES;
            l += i & MASK_2_BYTES;
        }

        long getCheckSum() {
            long hi = h;
            long lo = l;
            
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
    
    
    /**
     
     * 
     * @param data      the byte sequence for which to calculate a chekcsum
     * @return the 32bit checksum in the range from 0 to 2^32-1
     */
    public static long checksum(byte[] data) {
        return checksum(ByteBuffer.wrap(data));
    }

    /**
     * Compute a FITS check sum from a ByteBuffer
     * 
     * @param data      The ByteBuffer for which to calculated a (partial) checksum
     * @return The      computed check sum
     * 
     * @since 1.17
     * 
     * @see #checksum(Data)
     * @see #checksum(Header)
     * @see #sumOf(long...)
     * @see #subtractFrom(long, long)
     */
    public static long checksum(ByteBuffer data) {
        return update(data, 0);
    }

    /**
     * Computes the checksum for a FITS data object, e.g. to be used with
     * {@link #updateDatasum(Header, long)}
     * 
     * @param data      The FITS data object for which to calculate a checksum
     * @return          The checksum of the data
     * 
     * @throws FitsException    If there was an error serializing the data object
     * 
     * @see #updateDatasum(Header, long)
     * @see #setChecksum(BasicHDU)
     * 
     * @since 1.17
     */
    public static long checksum(Data data) throws FitsException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        FitsOutputStream bdos = new FitsOutputStream(stream);

        // DATASUM keyword.
        data.write(bdos);
        return checksum(stream.toByteArray());
    }
    
    /**
     * Computes the checksum for a FITS header object, e.g. for calculating 
     * incremental checksums via {@link #updateChecksum(Header, long)} after 
     * updating a header
     * 
     * @param header    The FITS header for which to calculate a checksum
     * @return          The checksum of the header
     * 
     * @throws FitsException    If there was an error serializing the data object
     * 
     * @see #updateDatasum(Header, long)
     * @see #setChecksum(BasicHDU)
     * 
     * @since 1.17
     */
    public static long checksum(Header header) throws FitsException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        FitsOutputStream bdos = new FitsOutputStream(stream);

        header.addValue(CHECKSUM, "0000000000000000");
        
        // DATASUM keyword.
        header.write(bdos);
        return checksum(stream.toByteArray());
    }
    
    /**
     * Update a checksum from a ByteBuffer
     *
     * @param data      The ByteBuffer to use as a source of data
     * @param prior     The previously accumulated 32-but checksum.
     * with the additional data.
     * 
     * @return The aggregated checksum
     * 
     * @throws IllegalArgumentException  if the prior is not a valid 32-bit checksum
     *                  (i.e. it has bits in the higher 4 bytes).
     */
    private static long update(ByteBuffer data, long prior) {
        Checksum sum = new Checksum(prior);
        
        if (!(data.remaining() % CHECKSUM_BLOCK_SIZE == 0)) {
            throw new IllegalArgumentException("fits blocks must always be divisible by 4");
        }
        data.position(0);
        data.order(ByteOrder.BIG_ENDIAN);
        IntBuffer iData = data.asIntBuffer();
        while (iData.hasRemaining()) {
            sum.add(iData.get());
        }
        return sum.getCheckSum();
    }
    /**
     * Encode a 32bit integer according to the Seaman-Pence proposal.
     * 
     * @see <a
     *      href="http://heasarc.gsfc.nasa.gov/docs/heasarc/ofwg/docs/general/checksum/node14.html#SECTION00035000000000000000">heasarc
     *      checksum doc</a>
     * @param c
     *            the checksum previously calculated
     * @param compl
     *            complement the value
     * @return the encoded string of 16 bytes.
     * 
     * @since 1.17
     */
    public static String encode(final long c, final boolean compl) {
        return encode(compl ? ~c : c);
    }

    /**
     * @deprecated Use {@link #encode(long, boolean)} instead.
     */
    @Deprecated
    public static String checksumEnc(final long c, final boolean compl) {
        return encode(c, compl);
    }

    
    /**
     * Encode the given checksum, including the rotating the result right by one byte.
     * 
     * @param checksum      The calculated 32-bit (unsigned) checksum
     * 
     * @return The encoded checksum, suitably encoded for use with the CHECKSUM header
     * 
     * @since 1.17
     */
    public static String encode(final long checksum) {
        final byte[] asc = new byte[CHECKSUM_STRING_SIZE];
        final byte[] ch = new byte[CHECKSUM_BLOCK_SIZE];
        final int sum = (int) checksum;

        for (int i = 0; i < CHECKSUM_BLOCK_SIZE; i++) {
            // each byte becomes four
            final int byt = MASK_BYTE & (sum >>> SELECT_BYTE[i]);

            Arrays.fill(ch, (byte) ((byt >>> 2) + ASCII_ZERO)); // quotient
            ch[0] += byt & CHECKSUM_BLOCK_MASK;                 // remainder

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
        
        return new String(asc);
    }
    /**
     * Decodes an encoded checksum, the opposite of {@link #encode}
     * 
     * @param encoded The encoded checksum (16 character string)
     * 
     * @return The unsigned 32-bit integer checksum.
     */
    public static long decode(String encoded) {
        byte[] bytes = encoded.getBytes(StandardCharsets.US_ASCII);
        if (bytes.length != CHECKSUM_STRING_SIZE) {
            throw new IllegalArgumentException("Bad checksum with " + bytes.length + " chars (expected 16)");
        }
        // Shift the bytes one to the left circularly
        byte tmp = bytes[0];
        System.arraycopy(bytes, 1, bytes, 0, CHECKSUM_STRING_SIZE - 1);
        bytes[CHECKSUM_STRING_SIZE - 1] = tmp;
        for (int i = 0; i < CHECKSUM_STRING_SIZE; i++) {
            bytes[i] -= ASCII_ZERO;
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        bb.order(ByteOrder.BIG_ENDIAN);
        long result = 0;
        result += bb.getInt();
        result += bb.getInt();
        result += bb.getInt();
        result += bb.getInt();
        return result & FitsIO.INTEGER_MASK;
    }
    
    
    /**
     * Apply an incremental update to the datasum, as a result of changing one of more records.
     * It will 
     * 
     * @param header            The header to update
     * @param dataSum           The new data checksum
     * @throws FitsException    if the header did not contain a DATASUM keyword.
     * 
     * @since 1.17
     */    
    public static void updateDatasum(Header header, long dataSum) throws FitsException {
        final HeaderCard sumCard = header.findCard(DATASUM);
        
        if (sumCard == null) {
            throw new FitsException("Header does not have a DATASUM keyword to update.");
        }
        
        final long oldSum = sumCard.getValue(Long.class, 0L);
        
        long oldCardSum = checksum(AsciiFuncs.getBytes(sumCard.toString()));
        sumCard.setValue(dataSum);
        long newCardSum = checksum(AsciiFuncs.getBytes(sumCard.toString()));
        
        // Updating the datasum also changes the CHECKSUM, so that must be recomputed too.
        // Checksum is affected both by the data changing, and by the DATASUM header changing
        // Fortunately this is (relatively) easy to do.
        long delta = dataSum - oldSum + (newCardSum - oldCardSum);
        
        updateChecksum(header, delta);
    }

    /**
     * Apply an incremental update to the checksum, as a result of changing one of more records
     * 
     * @param header            The header to update
     * @param delta             The change in the checksum
     * @throws FitsException    if the header did not contain a DATASUM keyword.
     * 
     * @since 1.17
     */
    public static void updateChecksum(Header header, long delta) throws FitsException {
        final HeaderCard sumCard = header.findCard(CHECKSUM);
        
        if (sumCard == null) {
            throw new FitsException("Header does not have a DATASUM keyword to update.");
        }

        long cksum = (~FitsCheckSum.decode(sumCard.getValue())) & FitsIO.INTEGER_MASK;
        sumCard.setValue(FitsCheckSum.encode(sumOf(cksum, delta)));
    }
    
    
    private static long wrap(long sum) { 
        while ((sum & FitsIO.HIGH_INTEGER_MASK) != 0) {
            long i = sum & FitsIO.INTEGER_MASK;
            sum = i + 1;
        }
        return sum;
    }
    
    /**
     * Calculates the total checksum from partial sums. For example combining checksums from
     * a header and data segment of a HDU, or for composing a data checksum from image tiles.
     * 
     * @param parts     The partial sums that are to be added together.
     * @return          The aggregated checksum as a 32-bit unsigned value. 
     * 
     * @since 1.17
     */
    public static long sumOf(long... parts) {
        long sum = 0;
        for (long part : parts) {
            sum += part;
        }
        return wrap(sum);
    }
    
    /**
     * Subtracts a partial checksum from an aggregated total. One may use it, for example to
     * update the datasum of a large data, when modifying only a small segment of it. Thus, one would
     * first subtract the checksum of the old segment from tha prior datasum, and then add the
     * checksum of the new data segment -- without hacing to recalculate the checksum for the entire
     * data again.
     * 
     * @param total     The total checksum containing b
     * @param part      The partial checksum to be subtracted from a.
     * @return          The checksum after subtracting the partial sum, as a 32-bit unsigned value.
     * 
     * @since 1.17
     */
    public static long subtractFrom(long total, long part) {
        return wrap(total - part);
    }
    
    
    /**
     * Computes and sets the DATASUM and CHECKSUM keywords for a given HDU. This method
     * calculates the sums from scratch, and can be computationally expensive. There are 
     * less expensive incremental update methods that can be used if the HDU already 
     * had sums recorded earlier, which need to be updated e.g. because there were modifications
     * to the header, or (parts of) the data.
     * 
     * @param hdu
     *            the HDU to be updated.
     * @throws FitsException
     *             if there was an error serializing the HDU
     * 
     * @see #updateDatasum(Header, long)
     * @see #updateChecksum(Header, long)
     * @see #sumOf(long...)
     * @see #subtractFrom(long, long)
     * 
     * @author R J Mather, Attila Kovacs
     *             
     */
    public static void setChecksum(BasicHDU<?> hdu) throws FitsException {
        try {
            applyTo(hdu);
        } catch (FitsException e) {
            throw e;
        } catch (Exception e) {
            throw new FitsException("Unexpected exception in checksum calculation", e);
        }
    }
    
    // Same as setChecksum(), but may throw exceptions other than FitsException
    private static void applyTo(BasicHDU<?> hdu) throws Exception {
        long csd = checksum(hdu.getData());

        // Add the freshly calculated datasum to the header, before calculating the checksum
        hdu.addValue(DATASUM, Long.toString(csd));
        
        long csh = checksum(hdu.getHeader());
        
        /*
         * This time we do not use a deleteKey() to ensure that the keyword is
         * replaced "in place". Note that the value of the checksum is actually
         * independent to a permutation of the 80-byte records within the
         * header.
         */
        hdu.addValue(CHECKSUM, encode(sumOf(csh, csd), true));
    }

}
