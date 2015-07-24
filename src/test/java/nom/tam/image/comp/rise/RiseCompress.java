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

        public int bits_to_go;

        public int bitbuffer;

        public int current;

        public void limit(int clen) {
            // TODO Auto-generated method stub

        }

        /* write out int value to the next 4 bytes of the buffer */
        public void write(int i) {

        }

        public boolean write(int i, int fsbits) {
            // TODO Auto-generated method stub
            return false;
        }

        public void write(long l, int bbits) {
            // TODO Auto-generated method stub

        }

        public void writeByte(int i) {
            // TODO Auto-generated method stub

        }

        /*
         * bit_output.c Bit output routines Procedures return zero on success,
         * EOF on end-of-buffer Programmer: R. White Date: 20 July 1998
         */

        /* Initialize for bit output */

        void start_outputing_bits() {
            /*
             * Buffer is empty to start with
             */
            bitbuffer = 0;
            bits_to_go = 8;
        }

        /* Flush out the last bits */

        int done_outputing_bits() {
            if (bits_to_go < 8) {
                writeByte(bitbuffer << bits_to_go);
            }
            return (0);
        }
    }/*---------------------------------------------------------------------------*/

    int fits_rcomp(int a[], /* input array */
            int nx, /* number of input pixels */
            byte[] c, /* output buffer */
            int clen, /* max length of output */
            int nblock) /* coding block size */
    {
        BitBuffer buffer = null;
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
         * Set up buffer pointers
         */
        buffer.limit(clen);

        buffer.bits_to_go = 8;
        /*
         * array for differences mapped to non-negative values
         */
        diff = new long[nblock];
        /*
         * Code in blocks of nblock pixels
         */
        buffer.start_outputing_bits();

        /* write out first int value to the first 4 bytes of the buffer */
        buffer.write(a[0]);

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
                diff[j] = ((pdiff < 0) ? ~((pdiff) << 1) : ((pdiff) << 1));
                pixelsum += diff[j];
                lastpix = nextpix;
            }

            /*
             * compute number of bits to split from sum
             */
            dpsum = (pixelsum - (thisblock / 2) - 1) / thisblock;
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
                buffer.write(fsmax + 1, fsbits);
                for (j = 0; j < thisblock; j++) {
                    buffer.write(diff[j], bbits);
                }
            } else if (fs == 0 && pixelsum == 0) {
                /*
                 * special low entropy case when FS = 0 and pixelsum=0 (all
                 * pixels in block are zero.) Output a 0 and return
                 */
                buffer.write(0, fsbits);
            } else {
                /* normal case: not either very high or very low entropy */
                buffer.write(fs + 1, fsbits);
                fsmask = (1 << fs) - 1;
                /*
                 * local copies of bit buffer to improve optimization
                 */
                lbitbuffer = buffer.bitbuffer;
                lbits_to_go = buffer.bits_to_go;
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
                        buffer.writeByte(lbitbuffer & 0xff);

                        for (top -= lbits_to_go; top >= 8; top -= 8) {
                            buffer.writeByte(0);
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
                            buffer.writeByte((lbitbuffer >> (-lbits_to_go)) & 0xff);
                            lbits_to_go += 8;
                        }
                    }
                }

                buffer.bitbuffer = lbitbuffer;
                buffer.bits_to_go = lbits_to_go;
            }
        }
        buffer.done_outputing_bits();
        /*
         * return number of bytes used
         */
        return buffer.current;
    }
}
