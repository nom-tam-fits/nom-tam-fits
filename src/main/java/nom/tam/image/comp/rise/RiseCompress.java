package nom.tam.image.comp.rise;

import java.nio.ByteBuffer;

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

/**
 * Rise compression algorithm, originally ported from cfitsio.
 * 
 * @author ritchie
 */
public class RiseCompress {

    private static final int BYTE_MASK = 0xff;

    private static final int BITS_OF_1_BYTE = 8;

    private static final int FS_BITS = 5;

    private static final int FSMAX = 25;

    private static final int BBITS = 1 << RiseCompress.FS_BITS;

    /**
     * compress the integer array on a rise compressed byte buffer.
     * 
     * @param dataToCompress
     *            the integer array to compress
     * @param blockSize
     *            the block size to use
     * @param writeBuffer
     *            the buffer to write to
     */
    public void compress(int[] dataToCompress, int blockSize, ByteBuffer writeBuffer) {
        BitBuffer buffer = new BitBuffer(writeBuffer);

        /* write out first int value to the first 4 bytes of the buffer */
        buffer.putInt(dataToCompress[0]);
        /* the first difference will always be zero */
        int lastpix = dataToCompress[0];

        int thisblock = blockSize;

        for (int i = 0; i < dataToCompress.length; i += blockSize) {
            /* last block may be shorter */
            if (dataToCompress.length - i < blockSize) {
                thisblock = dataToCompress.length - i;
            }
            /*
             * Compute differences of adjacent pixels and map them to unsigned
             * values. Note that this may overflow the integer variables --
             * that's OK, because we can recover when decompressing. If we were
             * compressing shorts or bytes, would want to do this arithmetic
             * with short/byte working variables (though diff will still be
             * passed as an int.) compute sum of mapped pixel values at same
             * time use double precision for sum to allow 32-bit integer inputs
             */
            long[] diff = new long[blockSize];
            double pixelsum = 0.0;
            int nextpix;
            /*
             * array for differences mapped to non-negative values
             */

            for (int j = 0; j < thisblock; j++) {
                nextpix = dataToCompress[i + j];
                long pdiff = nextpix - lastpix;
                diff[j] = Math.abs(pdiff < 0 ? ~(pdiff << 1) : pdiff << 1);
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
            for (fs = 0; psum > 0; fs++) {
                psum >>= 1;
            }

            /*
             * write the codes fsbits ID bits used to indicate split level
             */
            if (fs >= RiseCompress.FSMAX) {
                /*
                 * Special high entropy case when FS >= fsmax Just write pixel
                 * difference values directly, no Rice coding at all.
                 */
                buffer.putInt(RiseCompress.FSMAX + 1, RiseCompress.FS_BITS);
                for (int j = 0; j < thisblock; j++) {
                    buffer.putLong(diff[j], RiseCompress.BBITS);
                }
            } else if (fs == 0 && pixelsum == 0) {
                /*
                 * special low entropy case when FS = 0 and pixelsum=0 (all
                 * pixels in block are zero.) Output a 0 and return
                 */
                buffer.putInt(0, RiseCompress.FS_BITS);
            } else {
                /* normal case: not either very high or very low entropy */
                buffer.putInt(fs + 1, RiseCompress.FS_BITS);
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
                     * bottom FS bits are written without coding code is
                     * output_nbits, moved into this routine to reduce overheads
                     * This code potentially breaks if FS>24, so I am limiting
                     * FS to 24 by choice of FSMAX above.
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
                buffer.movePosition(-bitsToGo);
                buffer.putByte((byte) (bitBuffer & BYTE_MASK));
            }
        }
        buffer.close();
    }
}
