package nom.tam.fits.compression.algorithm.rice;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.logging.Logger;

import nom.tam.fits.compression.algorithm.api.ICompressor;
import nom.tam.fits.compression.algorithm.quant.QuantizeProcessor.DoubleQuantCompressor;
import nom.tam.fits.compression.algorithm.quant.QuantizeProcessor.FloatQuantCompressor;
import nom.tam.util.FitsIO;
import nom.tam.util.type.ElementType;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2024 nom-tam-fits
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

/**
 * (<i>for internal use</i>) The Rice compression algorithm. The original compression was designed by Rice, Yeh, and
 * Miller the code was written by Richard White at STScI at the STScI and included (ported to c and adapted) in cfitsio
 * by William Pence, NASA/GSFC. That code was then ported to java by R. van Nieuwenhoven. Later it was massively
 * refactored to harmonize the different compression algorithms and reduce the duplicate code pieces without obscuring
 * the algorithm itself as far as possible.
 *
 * @author     Richard White
 * @author     William Pence
 * @author     Richard van Nieuwenhoven
 *
 * @param  <T> the genetic type of NIO buffer on which this compressor operates.
 */
@SuppressWarnings({"deprecation", "javadoc"})
public abstract class RiceCompressor<T extends Buffer> implements ICompressor<T> {

    public static class ByteRiceCompressor extends RiceCompressor<ByteBuffer> {

        private ByteBuffer pixelBuffer;

        public ByteRiceCompressor(RiceCompressOption option) {
            super(option.setDefaultBytePix(ElementType.BYTE.size()));
        }

        @Override
        public boolean compress(ByteBuffer buffer, ByteBuffer writeBuffer) {
            pixelBuffer = buffer;
            super.compress(buffer.limit(), pixelBuffer.get(pixelBuffer.position()), new BitBuffer(writeBuffer));
            return true;
        }

        @Override
        public void decompress(ByteBuffer readBuffer, ByteBuffer buffer) {
            pixelBuffer = buffer;
            super.decompressBuffer(readBuffer, buffer.limit());
        }

        @Override
        protected int nextPixel() {
            return pixelBuffer.get();
        }

        @Override
        protected void nextPixel(int pixel) {
            pixelBuffer.put((byte) pixel);
        }
    }

    public static class DoubleRiceCompressor extends DoubleQuantCompressor {
        public DoubleRiceCompressor(RiceQuantizeCompressOption options) {
            super(options, new IntRiceCompressor(options.getRiceCompressOption()));
        }
    }

    public static class FloatRiceCompressor extends FloatQuantCompressor {
        public FloatRiceCompressor(RiceQuantizeCompressOption options) {
            super(options, new IntRiceCompressor(options.getRiceCompressOption()));
        }
    }

    public static class IntRiceCompressor extends RiceCompressor<IntBuffer> {

        private IntBuffer pixelBuffer;

        public IntRiceCompressor(RiceCompressOption option) {
            super(option.setDefaultBytePix(ElementType.INT.size()));
        }

        @Override
        public boolean compress(IntBuffer buffer, ByteBuffer writeBuffer) {
            pixelBuffer = buffer;
            super.compress(buffer.limit(), pixelBuffer.get(pixelBuffer.position()), new BitBuffer(writeBuffer));
            return true;
        }

        @Override
        public void decompress(ByteBuffer readBuffer, IntBuffer buffer) {
            pixelBuffer = buffer;
            super.decompressBuffer(readBuffer, buffer.limit());
        }

        @Override
        protected int nextPixel() {
            return pixelBuffer.get();
        }

        @Override
        protected void nextPixel(int pixel) {
            pixelBuffer.put(pixel);
        }
    }

    public static class ShortRiceCompressor extends RiceCompressor<ShortBuffer> {

        private ShortBuffer pixelBuffer;

        public ShortRiceCompressor(RiceCompressOption option) {
            super(option.setDefaultBytePix(ElementType.SHORT.size()));
        }

        @Override
        public boolean compress(ShortBuffer buffer, ByteBuffer writeBuffer) {
            pixelBuffer = buffer;
            super.compress(buffer.limit(), pixelBuffer.get(pixelBuffer.position()), new BitBuffer(writeBuffer));
            return true;
        }

        @Override
        public void decompress(ByteBuffer readBuffer, ShortBuffer buffer) {
            pixelBuffer = buffer;
            super.decompressBuffer(readBuffer, buffer.limit());
        }

        @Override
        protected int nextPixel() {
            return pixelBuffer.get();
        }

        @Override
        protected void nextPixel(int pixel) {
            pixelBuffer.put((short) pixel);
        }
    }

    /**
     * mask to convert a "unsigned" byte to a long.
     */
    private static final long UNSIGNED_BYTE_MASK = 0xFFL;

    /**
     * mask to convert a "unsigned" short to a long.
     */
    private static final long UNSIGNED_SHORT_MASK = 0xFFFFL;

    /**
     * mask to convert a "unsigned" int to a long.
     */
    private static final long UNSIGNED_INTEGER_MASK = 0xFFFFFFFFL;

    /**
     * logger to log to.
     */
    private static final Logger LOG = Logger.getLogger(RiceCompressor.class.getName());

    private static final int BITS_OF_1_BYTE = 8;

    private static final int BITS_PER_BYTE = 8;

    private static final int BYTE_MASK = 0xff;

    private static final int FS_BITS_FOR_BYTE = 3;

    private static final int FS_BITS_FOR_INT = 5;

    private static final int FS_BITS_FOR_SHORT = 4;

    private static final int FS_MAX_FOR_BYTE = 6;

    private static final int FS_MAX_FOR_INT = 25;

    private static final int FS_MAX_FOR_SHORT = 14;

    /*
     * nonzero_count is lookup table giving number of bits in 8-bit values not including leading zeros used in
     * fits_rdecomp, fits_rdecomp_short and fits_rdecomp_byte.
     *
     * @formatter:off
     */
    private static final int[] NONZERO_COUNT = {0, 1, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5,
            5, 5, 5, 5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8, 8, 8, 8, 8,
            8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
            8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
            8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8,
            8, 8, 8, 8, 8, 8, 8, 8, 8};
    // @formatter:on

    private final int bBits;

    private final int bitsPerPixel;

    private final int blockSize;

    private final int fsBits;

    private final int fsMax;

    private RiceCompressor(RiceCompressOption option) throws UnsupportedOperationException {
        blockSize = option.getBlockSize();
        if (option.getBytePix() == ElementType.BYTE.size()) {
            fsBits = FS_BITS_FOR_BYTE;
            fsMax = FS_MAX_FOR_BYTE;
            bitsPerPixel = FitsIO.BITS_OF_1_BYTE;
        } else if (option.getBytePix() == ElementType.SHORT.size()) {
            fsBits = FS_BITS_FOR_SHORT;
            fsMax = FS_MAX_FOR_SHORT;
            bitsPerPixel = FitsIO.BITS_OF_2_BYTES;
        } else if (option.getBytePix() == ElementType.INT.size()) {
            fsBits = FS_BITS_FOR_INT;
            fsMax = FS_MAX_FOR_INT;
            bitsPerPixel = FitsIO.BITS_OF_4_BYTES;
        } else {
            throw new UnsupportedOperationException("Implemented for 1/2/4 bytes only");
        }
        /*
         * From bsize derive: FSBITS = # bits required to store FS FSMAX = maximum value for FS BBITS = bits/pixel for
         * direct coding
         */
        bBits = 1 << fsBits;
    }

    /**
     * <p>
     * undo mapping and differencing Note that some of these operations will overflow the unsigned int arithmetic --
     * that's OK, it all works out to give the right answers in the output file.
     * </p>
     * <p>
     * In java this is more complicated because of the missing unsigned integers. trying to simulate the behavior
     * </p>
     *
     * @param  lastpix the current last pix value
     * @param  diff    the difference to "add"
     *
     * @return         return the new lastpiy value
     */
    private long undoMappingAndDifferencing(long lastpix, long diff) {
        diff &= UNSIGNED_INTEGER_MASK;
        if ((diff & 1) == 0) {
            diff = diff >>> 1;
        } else {
            diff = diff >>> 1 ^ UNSIGNED_INTEGER_MASK;
        }
        lastpix = diff + lastpix & UNSIGNED_INTEGER_MASK;
        nextPixel((int) lastpix);
        return lastpix;
    }

    /**
     * compress the integer tiledImageOperation on a rise compressed byte buffer.
     *
     * @param dataLength length of the data to compress
     * @param firstPixel the value of the first pixel
     * @param buffer     the buffer to write to
     */
    protected void compress(final int dataLength, int firstPixel, BitBuffer buffer) {
        /* the first difference will always be zero */
        int lastpix = firstPixel;
        /* write out first int value to the first 4 bytes of the buffer */
        buffer.putInt(firstPixel, bitsPerPixel);
        int thisblock = blockSize;
        for (int i = 0; i < dataLength; i += blockSize) {
            /* last block may be shorter */
            if (dataLength - i < blockSize) {
                thisblock = dataLength - i;
            }
            /*
             * Compute differences of adjacent pixels and map them to unsigned values. Note that this may overflow the
             * integer variables -- that's OK, because we can recover when decompressing. If we were compressing shorts
             * or bytes, would want to do this arithmetic with short/byte working variables (though diff will still be
             * passed as an int.) compute sum of mapped pixel values at same time use double precision for sum to allow
             * 32-bit integer inputs
             */
            long[] diff = new long[blockSize];
            double pixelsum = 0.0;
            int nextpix;
            /*
             * tiledImageOperation for differences mapped to non-negative values
             */
            for (int j = 0; j < thisblock; j++) {
                nextpix = nextPixel();
                long pdiff = (nextpix - lastpix);
                diff[j] = (pdiff < 0 ? (pdiff << 1) ^ UNSIGNED_INTEGER_MASK : pdiff << 1) & UNSIGNED_INTEGER_MASK;
                pixelsum += diff[j];
                lastpix = nextpix;
            }

            /*
             * compute number of bits to split from sum
             */
            double dpsum = (pixelsum - thisblock / 2d - 1d) / thisblock;
            if (dpsum < 0) {
                dpsum = 0.0;
            }
            long psum = (long) dpsum >> 1;
            int fs;
            for (fs = 0; psum > 0; fs++) { // NOSONAR
                psum >>= 1;
            }

            /*
             * write the codes fsbits ID bits used to indicate split level
             */
            if (fs >= fsMax) {
                /*
                 * Special high entropy case when FS >= fsmax Just write pixel difference values directly, no Rice
                 * coding at all.
                 */
                buffer.putInt(fsMax + 1, fsBits);
                for (int j = 0; j < thisblock; j++) {
                    buffer.putLong(diff[j], bBits);
                }
            } else if (fs == 0 && pixelsum == 0) { // NOSONAR
                /*
                 * special low entropy case when FS = 0 and pixelsum=0 (all pixels in block are zero.) Output a 0 and
                 * return
                 */
                buffer.putInt(0, fsBits);
            } else {
                /* normal case: not either very high or very low entropy */
                buffer.putInt(fs + 1, fsBits);
                int fsmask = (1 << fs) - 1;
                /*
                 * local copies of bit buffer to improve optimization
                 */
                int bitsToGo = buffer.missingBitsInCurrentByte();
                int bitBuffer = buffer.bitbuffer() >> bitsToGo;
                buffer.movePosition(bitsToGo - BITS_OF_1_BYTE);
                for (int j = 0; j < thisblock; j++) {
                    int v = (int) diff[j];
                    int top = v >> fs;
                    /*
                     * top is coded by top zeros + 1
                     */
                    if (bitsToGo >= top + 1) {
                        bitBuffer <<= top + 1;
                        bitBuffer |= 1;
                        bitsToGo -= top + 1;
                    } else {
                        bitBuffer <<= bitsToGo;
                        buffer.putByte((byte) (bitBuffer & BYTE_MASK));
                        for (top -= bitsToGo; top >= BITS_OF_1_BYTE; top -= BITS_OF_1_BYTE) {
                            buffer.putByte((byte) 0);
                        }
                        bitBuffer = 1;
                        bitsToGo = BITS_OF_1_BYTE - 1 - top;
                    }
                    /*
                     * bottom FS bits are written without coding code is output_nbits, moved into this routine to reduce
                     * overheads This code potentially breaks if FS>24, so I am limiting FS to 24 by choice of FSMAX
                     * above.
                     */
                    if (fs > 0) {
                        bitBuffer <<= fs;
                        bitBuffer |= v & fsmask;
                        bitsToGo -= fs;
                        while (bitsToGo <= 0) {
                            buffer.putByte((byte) (bitBuffer >> -bitsToGo & BYTE_MASK));
                            bitsToGo += BITS_OF_1_BYTE;
                        }
                    }
                }
                buffer.putByte((byte) (bitBuffer & BYTE_MASK), BITS_OF_1_BYTE - bitsToGo);
            }
        }
        buffer.close();
    }

    /**
     * decompress the readbuffer and fill the pixelarray.
     *
     * @param readBuffer input buffer
     * @param nx         the number of pixel to uncompress
     */
    protected void decompressBuffer(final ByteBuffer readBuffer, final int nx) {
        /* first x bytes of input buffer contain the value of the first */
        /* x byte integer value, without any encoding */
        long lastpix = 0L;
        if (bitsPerPixel == ElementType.BYTE.bitPix()) {
            lastpix = readBuffer.get() & UNSIGNED_BYTE_MASK;
        } else if (bitsPerPixel == ElementType.SHORT.bitPix()) {
            lastpix = readBuffer.getShort() & UNSIGNED_SHORT_MASK;
        } else {
            // Must be (this.bitsPerPixel == ElementType.INT.bitPix())
            lastpix = readBuffer.getInt() & UNSIGNED_INTEGER_MASK;
        }
        long b = readBuffer.get() & BYTE_MASK; /* bit buffer */
        int nbits = BITS_PER_BYTE; /* number of bits remaining in b */
        for (int i = 0; i < nx;) {
            /* get the FS value from first fsbits */
            nbits -= fsBits;
            while (nbits < 0) {
                b = b << BITS_PER_BYTE | readBuffer.get() & BYTE_MASK;
                nbits += BITS_PER_BYTE;
            }
            long fs = (b >>> nbits) - 1L;

            b &= (1 << nbits) - 1;
            /* loop over the next block */
            int imax = i + blockSize;
            if (imax > nx) {
                imax = nx;
            }
            if (fs < 0) {
                /* low-entropy case, all zero differences */
                for (; i < imax; i++) {
                    nextPixel((int) lastpix);
                }
            } else if (fs == fsMax) {
                /* high-entropy case, directly coded pixel values */
                for (; i < imax; i++) {
                    int k = bBits - nbits;
                    long diff = b << k;
                    for (k -= BITS_PER_BYTE; k >= 0; k -= BITS_PER_BYTE) {
                        b = readBuffer.get() & BYTE_MASK;
                        diff |= b << k;
                    }
                    if (nbits > 0) {
                        b = readBuffer.get() & BYTE_MASK;
                        diff |= b >>> -k;
                        b &= (1 << nbits) - 1L;
                    } else {
                        b = 0;
                    }
                    lastpix = undoMappingAndDifferencing(lastpix, diff);
                }
            } else {
                /* normal case, Rice coding */
                for (; i < imax; i++) {
                    /* count number of leading zeros */
                    while (b == 0) {
                        nbits += BITS_PER_BYTE;
                        b = readBuffer.get() & BYTE_MASK;
                    }
                    long nzero = nbits - NONZERO_COUNT[(int) (b & BYTE_MASK)];
                    nbits -= nzero + 1;
                    /* flip the leading one-bit */
                    b ^= 1 << nbits;
                    /* get the FS trailing bits */
                    nbits -= fs;
                    while (nbits < 0) {
                        b = b << BITS_PER_BYTE | readBuffer.get() & BYTE_MASK;
                        nbits += BITS_PER_BYTE;
                    }
                    long diff = nzero << fs | b >> nbits;
                    b &= (1 << nbits) - 1L;

                    lastpix = undoMappingAndDifferencing(lastpix, diff);
                }
            }
        }
        if (readBuffer.limit() > readBuffer.position()) {
            LOG.warning("decompressing left over some extra bytes got: " + readBuffer.limit() + " but needed only "
                    + readBuffer.position());
        }

    }

    protected abstract int nextPixel();

    protected abstract void nextPixel(int pixel);

}
