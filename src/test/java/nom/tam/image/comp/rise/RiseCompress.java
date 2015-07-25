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

public class RiseCompress {

    class BitBuffer {

        ByteBuffer buffer;

        private long position;

        public BitBuffer(int i) {
            buffer = ByteBuffer.wrap(new byte[i]);
        }

        public void limit(int newLimit) {
            buffer.limit(newLimit);

        }

        /**
         * write out int value to the next 4 bytes of the buffer
         */
        public void putInt(int i) {
            putByte((byte) ((i & 0xFF000000) >>> 24));
            putByte((byte) ((i & 0x00FF0000) >>> 16));
            putByte((byte) ((i & 0x0000FF00) >>> 8));
            putByte((byte) (i & 0x000000FF));
        }

        public void putInt(int i, int bits) {
            if (bits == 0)
                return;
            do {
                if (bits > 7) {
                    putByte((byte) ((i & (0xFF << (bits - 8))) >>> (bits - 8)));
                    bits -= 8;
                } else {
                    putByte((byte) (i & (0xFF >> -(bits - 8))), bits);
                    bits = 0;
                }
            } while (bits > 0);
        }

        public void putLong(long l, int bits) {
            if (bits == 0)
                return;
            do {
                if (bits > 31) {
                    putInt((int) ((l & (0xFFFFFFFFL << (bits - 32L))) >>> (bits - 32L)));
                    bits -= 32;
                } else {
                    putInt((int) (l & (0xFFFFFFFFL >> -(bits - 32L))), bits);
                    bits = 0;
                }
            } while (bits > 0);
        }

        public void putByte(byte b, int bits) {// ((byte)-66) &0xFF
            b = (byte) (0xFF & ((b & (0xFF >>> (8 - bits))) << (8 - bits)));
            buffer.put((int) (position / 8), (byte) (0xFF & ((buffer.get((int) (position / 8)) & (0xFF << (8 - position % 8))) | ((b & 0xFF) >>> (position % 8)))));
            if (8 - (position % 8) < bits)
                buffer.put((int) ((position / 8) + 1), (byte) (0xFF & ((b & 0xFF) << (8 - position % 8))));
            position += bits;
        }

        public void putByte(byte b) {
            byte old = (byte) (buffer.get((int) (position / 8)) & (byte) ~(0xFF >>> (position % 8)));
            buffer.put((int) (position / 8), (byte) (old | (byte) ((b & 0xFF) >>> (position % 8))));
            if (position % 8 > 0)
                buffer.put((int) ((position / 8) + 1), (byte) ((b & 0xFF) << (8 - (position % 8))));
            position += 8;
        }

        /*
         * bit_output.c Bit output routines Procedures return zero on success,
         * EOF on end-of-buffer Programmer: R. White Date: 20 July 1998
         */

        /* Initialize for bit output */

        void start_outputing_bits() {
            position = 0;
        }

        /* Flush out the last bits */

        void done_outputing_bits() {
            if (position % 8 != 0) {
                putByte((byte) 0, (int) (8 - position % 8));
            }
        }

        public int bitbuffer() {
            return buffer.get((int) (position / 8));
        }

        public int missingBitsInCurrentByte() {
            return (int) (8 - position % 8);
        }
    }/*---------------------------------------------------------------------------*/

    ByteBuffer fits_rcomp(int a[], /* input array */
            int nx, /* number of input pixels */
            int nblock) /* coding block size */
    {
        BitBuffer buffer = new BitBuffer(a.length * 4);
        /* int bsize; */
        int i, j, thisblock;
        int lastpix, nextpix;
        int v, fs, fsmask, top, fsmax, fsbits, bbits;
        int lbitbuffer, lbits_to_go;
        long psum;
        double pixelsum, dpsum;
        long[] diff;

        /* move out of switch block, to tweak performance */
        fsbits = 5;
        fsmax = 25;

        bbits = 1 << fsbits;
        /*
         * array for differences mapped to non-negative values
         */
        diff = new long[nblock];

        /* write out first int value to the first 4 bytes of the buffer */
        buffer.putInt(a[0]);

        lastpix = a[0]; /* the first difference will always be zero */

        thisblock = nblock;
        for (i = 0; i < nx; i += nblock) {
            /* last block may be shorter */
            if (nx - i < nblock)
                thisblock = nx - i;
            /*
             * Compute differences of adjacent pixels and map them to unsigned
             * values. Note that this may overflow the integer variables --
             * that's OK, because we can recover when decompressing. If we were
             * compressing shorts or bytes, would want to do this arithmetic
             * with short/byte working variables (though diff will still be
             * passed as an int.) compute sum of mapped pixel values at same
             * time use double precision for sum to allow 32-bit integer inputs
             */
            pixelsum = 0.0;
            for (j = 0; j < thisblock; j++) {
                nextpix = a[i + j];
                long pdiff = nextpix - lastpix;
                diff[j] = Math.abs(((pdiff < 0) ? ~((pdiff) << 1) : ((pdiff) << 1)));
                pixelsum += diff[j];
                lastpix = nextpix;
            }

            /*
             * compute number of bits to split from sum
             */
            dpsum = (pixelsum - (thisblock / 2d) - 1d) / thisblock;
            if (dpsum < 0)
                dpsum = 0.0;
            psum = ((long) dpsum) >> 1;
            for (fs = 0; psum > 0; fs++)
                psum >>= 1;

            /*
             * write the codes fsbits ID bits used to indicate split level
             */
            if (fs >= fsmax) {
                /*
                 * Special high entropy case when FS >= fsmax Just write pixel
                 * difference values directly, no Rice coding at all.
                 */
                buffer.putInt(fsmax + 1, fsbits);
                for (j = 0; j < thisblock; j++) {
                    buffer.putLong(diff[j], bbits);
                }
            } else if (fs == 0 && pixelsum == 0) {
                /*
                 * special low entropy case when FS = 0 and pixelsum=0 (all
                 * pixels in block are zero.) Output a 0 and return
                 */
                buffer.putInt(0, fsbits);
            } else {// buffer.buffer.array()
                /* normal case: not either very high or very low entropy */
                buffer.putInt(fs + 1, fsbits);
                fsmask = (1 << fs) - 1;
                /*
                 * local copies of bit buffer to improve optimization
                 */
                lbitbuffer = buffer.bitbuffer();
                lbits_to_go = buffer.missingBitsInCurrentByte();
                buffer.position = buffer.position - (buffer.position % 8);
                for (j = 0; j < thisblock; j++) {
                    v = (int) diff[j];
                    top = v >> fs;
                    /*
                     * top is coded by top zeros + 1
                     */
                    if (lbits_to_go >= top + 1) {
                        lbitbuffer <<= top + 1;
                        lbitbuffer |= 1;
                        lbits_to_go -= top + 1;
                    } else {
                        lbitbuffer <<= lbits_to_go;
                        buffer.putByte((byte) (lbitbuffer & 0xff));
                        for (top -= lbits_to_go; top >= 8; top -= 8) {
                            buffer.putByte((byte) 0);
                        }
                        lbitbuffer = 1;
                        lbits_to_go = 7 - top;
                    }
                    /*
                     * bottom FS bits are written without coding code is
                     * output_nbits, moved into this routine to reduce overheads
                     * This code potentially breaks if FS>24, so I am limiting
                     * FS to 24 by choice of FSMAX above.
                     */
                    if (fs > 0) {
                        lbitbuffer <<= fs;
                        lbitbuffer |= v & fsmask;
                        lbits_to_go -= fs;
                        while (lbits_to_go <= 0) {
                            buffer.putByte((byte) ((lbitbuffer >> (-lbits_to_go)) & 0xff));
                            lbits_to_go += 8;
                        }
                    }
                }
                buffer.position = buffer.position - lbits_to_go;
            }
        }
        buffer.done_outputing_bits();
        buffer.buffer.position((int) (buffer.position / 8));
        return buffer.buffer;
    }
}
