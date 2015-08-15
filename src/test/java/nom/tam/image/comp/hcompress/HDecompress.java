package nom.tam.image.comp.hcompress;

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

import java.nio.ByteBuffer;

public class HDecompress {

    private static final byte[] CODE_MAGIC = {
        (byte) 0xDD,
        (byte) 0x99
    };

    private static class LongArrayPointer {

        private final long[] a;

        private int offset;

        public LongArrayPointer(long[] tmp) {
            a = tmp;
            offset = 0;
        }

        public LongArrayPointer copy() {
            LongArrayPointer intAP = new LongArrayPointer(this.a);
            intAP.offset = this.offset;
            return intAP;
        }

        public LongArrayPointer copy(int extraOffset) {
            LongArrayPointer intAP = new LongArrayPointer(this.a);
            intAP.offset = this.offset + extraOffset;
            return intAP;
        }

        public long get() {
            return this.a[this.offset];
        }

        public long get(int i) {
            return this.a[this.offset + i];
        }

        public void set(int i, long value) {
            this.a[this.offset + i] = value;

        }

        public void set(long value) {
            this.a[this.offset] = value;
        }

        public void bitOr(int i, long plane_val) {
            this.a[this.offset + i] |= plane_val;

        }

    }

    int ny;

    int nx;

    int scale;

    void fits_hdecompress64(ByteBuffer input, int smooth, long[] aa) {
        /*
         * decompress the input byte stream using the H-compress algorithm input
         * - input array of compressed bytes a - pre-allocated array to hold the
         * output uncompressed image nx - returned X axis size ny - returned Y
         * axis size NOTE: the nx and ny dimensions as defined within this code
         * are reversed from the usual FITS notation. ny is the fastest varying
         * dimension, which is usually considered the X axis in the FITS image
         * display
         */

        LongArrayPointer a = new LongArrayPointer(aa);
        int stat, ii, nval;
        int[] iarray;

        /* decode the input array */

        decode64(input, a);

        /*
         * Un-Digitize
         */
        undigitize64(a);

        /*
         * Inverse H-transform
         */
        hinv64(a, smooth);

    }

    int hinv64(LongArrayPointer a, int smooth)
    /*
     * int smooth; 0 for no smoothing, else smooth during inversion int scale;
     * used if smoothing is specified
     */
    {
        int nmax, log2n, i, j, k;
        int nxtop, nytop, nxf, nyf, c;
        int oddx, oddy;
        int shift;
        long mask0, mask1, mask2, prnd0, prnd1, prnd2, bit0, bit1, bit2;
        long nrnd0, nrnd1, nrnd2, lowbit0, lowbit1;
        long h0, hx, hy, hc;
        int s10, s00;
        long[] tmp;

        /*
         * log2n is log2 of max(nx,ny) rounded up to next power of 2
         */
        nmax = (nx > ny) ? nx : ny;
        log2n = (int) (Math.log((float) nmax) / Math.log(2.0) + 0.5);
        if (nmax > (1 << log2n)) {
            log2n += 1;
        }
        /*
         * get temporary storage for shuffling elements
         */
        tmp = new long[(nmax + 1) / 2];
        /*
         * set up masks, rounding parameters
         */
        shift = 1;
        bit0 = ((long) 1) << (log2n - 1);
        bit1 = bit0 << 1;
        bit2 = bit0 << 2;
        mask0 = -bit0;
        mask1 = mask0 << 1;
        mask2 = mask0 << 2;
        prnd0 = bit0 >> 1;
        prnd1 = bit1 >> 1;
        prnd2 = bit2 >> 1;
        nrnd0 = prnd0 - 1;
        nrnd1 = prnd1 - 1;
        nrnd2 = prnd2 - 1;
        /*
         * round h0 to multiple of bit2
         */
        a.set(0, (a.get(0) + ((a.get(0) >= 0) ? prnd2 : nrnd2)) & mask2);
        /*
         * do log2n expansions We're indexing a as a 2-D array with dimensions
         * (nx,ny).
         */
        nxtop = 1;
        nytop = 1;
        nxf = nx;
        nyf = ny;
        c = 1 << log2n;
        for (k = log2n - 1; k >= 0; k--) {
            /*
             * this somewhat cryptic code generates the sequence ntop[k-1] =
             * (ntop[k]+1)/2, where ntop[log2n] = n
             */
            c = c >> 1;
            nxtop = nxtop << 1;
            nytop = nytop << 1;
            if (nxf <= c) {
                nxtop -= 1;
            } else {
                nxf -= c;
            }
            if (nyf <= c) {
                nytop -= 1;
            } else {
                nyf -= c;
            }
            /*
             * double shift and fix nrnd0 (because prnd0=0) on last pass
             */
            if (k == 0) {
                nrnd0 = 0;
                shift = 2;
            }
            /*
             * unshuffle in each dimension to interleave coefficients
             */
            for (i = 0; i < nxtop; i++) {
                unshuffle64(a.copy(ny * i), nytop, 1, tmp);
            }
            for (j = 0; j < nytop; j++) {
                unshuffle64(a.copy(j), nxtop, ny, tmp);
            }
            /*
             * smooth by interpolating coefficients if SMOOTH != 0
             */
            if (smooth != 0)
                hsmooth64(a, nxtop, nytop, ny, scale);
            oddx = nxtop % 2;
            oddy = nytop % 2;
            for (i = 0; i < nxtop - oddx; i += 2) {
                s00 = ny * i; /* s00 is index of a[i,j] */
                s10 = s00 + ny; /* s10 is index of a[i+1,j] */
                for (j = 0; j < nytop - oddy; j += 2) {
                    h0 = a.get(s00);
                    hx = a.get(s10);
                    hy = a.get(s00 + 1);
                    hc = a.get(s10 + 1);
                    /*
                     * round hx and hy to multiple of bit1, hc to multiple of
                     * bit0 h0 is already a multiple of bit2
                     */
                    hx = (hx + ((hx >= 0) ? prnd1 : nrnd1)) & mask1;
                    hy = (hy + ((hy >= 0) ? prnd1 : nrnd1)) & mask1;
                    hc = (hc + ((hc >= 0) ? prnd0 : nrnd0)) & mask0;
                    /*
                     * propagate bit0 of hc to hx,hy
                     */
                    lowbit0 = hc & bit0;
                    hx = (hx >= 0) ? (hx - lowbit0) : (hx + lowbit0);
                    hy = (hy >= 0) ? (hy - lowbit0) : (hy + lowbit0);
                    /*
                     * Propagate bits 0 and 1 of hc,hx,hy to h0. This could be
                     * simplified if we assume h0>0, but then the inversion
                     * would not be lossless for images with negative pixels.
                     */
                    lowbit1 = (hc ^ hx ^ hy) & bit1;
                    h0 = (h0 >= 0) ? (h0 + lowbit0 - lowbit1) : (h0 + ((lowbit0 == 0) ? lowbit1 : (lowbit0 - lowbit1)));
                    /*
                     * Divide sums by 2 (4 last time)
                     */
                    a.set(s10 + 1, (h0 + hx + hy + hc) >> shift);
                    a.set(s10, (h0 + hx - hy - hc) >> shift);
                    a.set(s00 + 1, (h0 - hx + hy - hc) >> shift);
                    a.set(s00, (h0 - hx - hy + hc) >> shift);
                    s00 += 2;
                    s10 += 2;
                }
                if (oddy != 0) {
                    /*
                     * do last element in row if row length is odd s00+1, s10+1
                     * are off edge
                     */
                    h0 = a.get(s00);
                    hx = a.get(s10);
                    hx = ((hx >= 0) ? (hx + prnd1) : (hx + nrnd1)) & mask1;
                    lowbit1 = hx & bit1;
                    h0 = (h0 >= 0) ? (h0 - lowbit1) : (h0 + lowbit1);
                    a.set(s10, (h0 + hx) >> shift);
                    a.set(s00, (h0 - hx) >> shift);
                }
            }
            if (oddx != 0) {
                /*
                 * do last row if column length is odd s10, s10+1 are off edge
                 */
                s00 = ny * i;
                for (j = 0; j < nytop - oddy; j += 2) {
                    h0 = a.get(s00);
                    hy = a.get(s00 + 1);
                    hy = ((hy >= 0) ? (hy + prnd1) : (hy + nrnd1)) & mask1;
                    lowbit1 = hy & bit1;
                    h0 = (h0 >= 0) ? (h0 - lowbit1) : (h0 + lowbit1);
                    a.set(s00 + 1, (h0 + hy) >> shift);
                    a.set(s00, (h0 - hy) >> shift);
                    s00 += 2;
                }
                if (oddy != 0) {
                    /*
                     * do corner element if both row and column lengths are odd
                     * s00+1, s10, s10+1 are off edge
                     */
                    h0 = a.get(s00);
                    a.set(s00, h0 >> shift);
                }
            }
            /*
             * divide all the masks and rounding values by 2
             */
            bit2 = bit1;
            bit1 = bit0;
            bit0 = bit0 >> 1;
            mask1 = mask0;
            mask0 = mask0 >> 1;
            prnd1 = prnd0;
            prnd0 = prnd0 >> 1;
            nrnd1 = nrnd0;
            nrnd0 = prnd0 - 1;
        }
        return (0);
    }

    void unshuffle64(LongArrayPointer a, int n, int n2, long tmp[])
    /*
     * long a[]; array to shuffle int n; number of elements to shuffle int n2;
     * second dimension long tmp[]; scratch storage
     */
    {
        int i;
        int nhalf;
        LongArrayPointer p1, p2, pt;

        /*
         * copy 2nd half of array to tmp
         */
        nhalf = (n + 1) >> 1;
        pt = new LongArrayPointer(tmp);
        p1 = a.copy(n2 * nhalf); /* pointer to a[i] */
        for (i = nhalf; i < n; i++) {
            pt.set(p1.get());
            p1.offset += n2;
            pt.offset += 1;
        }
        /*
         * distribute 1st half of array to even elements
         */
        p2 = a.copy(n2 * (nhalf - 1)); /* pointer to a[i] */
        p1 = a.copy((n2 * (nhalf - 1)) << 1); /* pointer to a[2*i] */
        for (i = nhalf - 1; i >= 0; i--) {
            p1.set(p2.get());
            p2.offset -= n2;
            p1.offset -= (n2 + n2);
        }
        /*
         * now distribute 2nd half of array (in tmp) to odd elements
         */
        pt = new LongArrayPointer(tmp);
        p1 = a.copy(n2); /* pointer to a[i] */
        for (i = 1; i < n; i += 2) {
            p1.set(pt.get());
            p1.offset += (n2 + n2);
            pt.offset += 1;
        }
    }

    void hsmooth64(LongArrayPointer a, int nxtop, int nytop, int ny, int scale)
    /*
     * long a[]; array of H-transform coefficients int nxtop,nytop; size of
     * coefficient block to use int ny; actual 1st dimension of array int scale;
     * truncation scale factor that was used
     */
    {
        int i, j;
        int ny2, s10, s00;
        long hm, h0, hp, hmm, hpm, hmp, hpp, hx2, hy2, diff, dmax, dmin, s, smax, m1, m2;

        /*
         * Maximum change in coefficients is determined by scale factor. Since
         * we rounded during division (see digitize.c), the biggest permitted
         * change is scale/2.
         */
        smax = (scale >> 1);
        if (smax <= 0)
            return;
        ny2 = ny << 1;
        /*
         * We're indexing a as a 2-D array with dimensions (nxtop,ny) of which
         * only (nxtop,nytop) are used. The coefficients on the edge of the
         * array are not adjusted (which is why the loops below start at 2
         * instead of 0 and end at nxtop-2 instead of nxtop.)
         */
        /*
         * Adjust x difference hx
         */
        for (i = 2; i < nxtop - 2; i += 2) {
            s00 = ny * i; /* s00 is index of a[i,j] */
            s10 = s00 + ny; /* s10 is index of a[i+1,j] */
            for (j = 0; j < nytop; j += 2) {
                /*
                 * hp is h0 (mean value) in next x zone, hm is h0 in previous x
                 * zone
                 */
                hm = a.get(s00 - ny2);
                h0 = a.get(s00);
                hp = a.get(s00 + ny2);
                /*
                 * diff = 8 * hx slope that would match h0 in neighboring zones
                 */
                diff = hp - hm;
                /*
                 * monotonicity constraints on diff
                 */
                dmax = Math.max(Math.min((hp - h0), (h0 - hm)), 0) << 2;
                dmin = Math.min(Math.max((hp - h0), (h0 - hm)), 0) << 2;
                /*
                 * if monotonicity would set slope = 0 then don't change hx.
                 * note dmax>=0, dmin<=0.
                 */
                if (dmin < dmax) {
                    diff = Math.max(Math.min(diff, dmax), dmin);
                    /*
                     * Compute change in slope limited to range +/- smax.
                     * Careful with rounding negative numbers when using shift
                     * for divide by 8.
                     */
                    s = diff - (a.get(s10) << 3);
                    s = (s >= 0) ? (s >> 3) : ((s + 7) >> 3);
                    s = Math.max(Math.min(s, smax), -smax);
                    a.set(s10, a.get(s10) + s);
                }
                s00 += 2;
                s10 += 2;
            }
        }
        /*
         * Adjust y difference hy
         */
        for (i = 0; i < nxtop; i += 2) {
            s00 = ny * i + 2;
            s10 = s00 + ny;
            for (j = 2; j < nytop - 2; j += 2) {
                hm = a.get(s00 - 2);
                h0 = a.get(s00);
                hp = a.get(s00 + 2);
                diff = hp - hm;
                dmax = Math.max(Math.min((hp - h0), (h0 - hm)), 0) << 2;
                dmin = Math.min(Math.max((hp - h0), (h0 - hm)), 0) << 2;
                if (dmin < dmax) {
                    diff = Math.max(Math.min(diff, dmax), dmin);
                    s = diff - (a.get(s00 + 1) << 3);
                    s = (s >= 0) ? (s >> 3) : ((s + 7) >> 3);
                    s = Math.max(Math.min(s, smax), -smax);
                    a.set(s00 + 1, a.get(s00 + 1) + s);
                }
                s00 += 2;
                s10 += 2;
            }
        }
        /*
         * Adjust curvature difference hc
         */
        for (i = 2; i < nxtop - 2; i += 2) {
            s00 = ny * i + 2;
            s10 = s00 + ny;
            for (j = 2; j < nytop - 2; j += 2) {
                /*
                 * ------------------ y | hmp | | hpp | | ------------------ | |
                 * | h0 | | | ------------------ -------x | hmm | | hpm |
                 * ------------------
                 */
                hmm = a.get(s00 - ny2 - 2);
                hpm = a.get(s00 + ny2 - 2);
                hmp = a.get(s00 - ny2 + 2);
                hpp = a.get(s00 + ny2 + 2);
                h0 = a.get(s00);
                /*
                 * diff = 64 * hc value that would match h0 in neighboring zones
                 */
                diff = hpp + hmm - hmp - hpm;
                /*
                 * 2 times x,y slopes in this zone
                 */
                hx2 = a.get(s10) << 1;
                hy2 = a.get(s00 + 1) << 1;
                /*
                 * monotonicity constraints on diff
                 */
                m1 = Math.min(Math.max(hpp - h0, 0) - hx2 - hy2, Math.max(h0 - hpm, 0) + hx2 - hy2);
                m2 = Math.min(Math.max(h0 - hmp, 0) - hx2 + hy2, Math.max(hmm - h0, 0) + hx2 + hy2);
                dmax = Math.min(m1, m2) << 4;
                m1 = Math.max(Math.min(hpp - h0, 0) - hx2 - hy2, Math.min(h0 - hpm, 0) + hx2 - hy2);
                m2 = Math.max(Math.min(h0 - hmp, 0) - hx2 + hy2, Math.min(hmm - h0, 0) + hx2 + hy2);
                dmin = Math.max(m1, m2) << 4;
                /*
                 * if monotonicity would set slope = 0 then don't change hc.
                 * note dmax>=0, dmin<=0.
                 */
                if (dmin < dmax) {
                    diff = Math.max(Math.min(diff, dmax), dmin);
                    /*
                     * Compute change in slope limited to range +/- smax.
                     * Careful with rounding negative numbers when using shift
                     * for divide by 64.
                     */
                    s = diff - (a.get(s10 + 1) << 6);
                    s = (s >= 0) ? (s >> 6) : ((s + 63) >> 6);
                    s = Math.max(Math.min(s, smax), -smax);
                    a.set(s10 + 1, a.get(s10 + 1) + s);
                }
                s00 += 2;
                s10 += 2;
            }
        }
    }

    void undigitize64(LongArrayPointer a) {
        LongArrayPointer p;
        long scale64;

        /*
         * multiply by scale
         */
        if (scale <= 1)
            return;
        scale64 = (long) scale; /*
                                 * use a 64-bit int for efficiency in the big
                                 * loop
                                 */

        for (int index = 0; index < a.a.length; index++) {
            a.a[index] = a.a[index] * scale64;
        }
    }

    void decode64(ByteBuffer infile, LongArrayPointer a)
    /*
     * char *infile; input file long *a; address of output array [nx][ny] int
     * *nx,*ny; size of output array int *scale; scale factor for digitization
     */
    {
        int nel, stat;
        long sumall;
        byte[] nbitplanes = new byte[3];
        byte[] tmagic = new byte[2];

        /* initialize the byte read position to the beginning of the array */;
        int nextchar = 0;

        /*
         * File starts either with special 2-byte magic code or with FITS
         * keyword "SIMPLE  ="
         */
        infile.get(tmagic);
        /*
         * check for correct magic code value
         */
        if (tmagic[0] != CODE_MAGIC[0] || tmagic[1] != CODE_MAGIC[1]) {
            throw new RuntimeException("compresseionError");
        }
        nx = infile.getInt(); /* x size of image */
        ny = infile.getInt(); /* y size of image */
        scale = infile.getInt(); /* scale factor for digitization */

        nel = (nx) * (ny);

        /* sum of all pixels */
        sumall = infile.getLong();
        /* # bits in quadrants */

        infile.get(nbitplanes);

        dodecode64(infile, a, nbitplanes);
        /*
         * put sum of all pixels back into pixel 0
         */
        a.set(0, sumall);
    }

    int dodecode64(ByteBuffer infile, LongArrayPointer a, byte[] nbitplanes)

    /*
     * long a[]; int nx,ny; Array dimensions are [nx][ny] unsigned char
     * nbitplanes[3]; Number of bit planes in quadrants
     */
    {
        int i, nel, nx2, ny2, stat;

        nel = nx * ny;
        nx2 = (nx + 1) / 2;
        ny2 = (ny + 1) / 2;

        /*
         * initialize a to zero
         */
        for (i = 0; i < nel; i++)
            a.set(i, 0);
        /*
         * Initialize bit input
         */
        start_inputing_bits();
        /*
         * read bit planes for each quadrant
         */
        qtree_decode64(infile, a.copy(0), ny, nx2, ny2, nbitplanes[0]);

        qtree_decode64(infile, a.copy(ny2), ny, nx2, ny / 2, nbitplanes[1]);

        qtree_decode64(infile, a.copy(ny * nx2), ny, nx / 2, ny2, nbitplanes[1]);

        qtree_decode64(infile, a.copy(ny * nx2 + ny2), ny, nx / 2, ny / 2, nbitplanes[2]);

        /*
         * make sure there is an EOF symbol (nybble=0) at end
         */
        if (input_nybble(infile) != 0) {
            throw new RuntimeException("compresseionError");
        }
        /*
         * now get the sign bits Re-initialize bit input
         */
        start_inputing_bits();
        for (i = 0; i < nel; i++) {
            if (a.get(i) != 0) {
                if (input_bit(infile) != 0)
                    a.set(i, -a.get(i));
            }
        }
        return (0);
    }

    int qtree_decode64(ByteBuffer infile, LongArrayPointer a, int n, int nqx, int nqy, int nbitplanes)

    /*
     * char *infile; long a[]; a is 2-D array with dimensions (n,n) int n;
     * length of full row in a int nqx; partial length of row to decode int nqy;
     * partial length of column (<=n) int nbitplanes; number of bitplanes to
     * decode
     */
    {
        int log2n, k, bit, b, nqmax;
        int nx, ny, nfx, nfy, c;
        int nqx2, nqy2;
        byte[] scratch;

        /*
         * log2n is log2 of max(nqx,nqy) rounded up to next power of 2
         */
        nqmax = (nqx > nqy) ? nqx : nqy;
        log2n = (int) (Math.log((float) nqmax) / Math.log(2.0) + 0.5);
        if (nqmax > (1 << log2n)) {
            log2n += 1;
        }
        /*
         * allocate scratch array for working space
         */
        nqx2 = (nqx + 1) / 2;
        nqy2 = (nqy + 1) / 2;
        scratch = new byte[(nqx2 * nqy2)];

        /*
         * now decode each bit plane, starting at the top A is assumed to be
         * initialized to zero
         */
        for (bit = nbitplanes - 1; bit >= 0; bit--) {
            /*
             * Was bitplane was quadtree-coded or written directly?
             */
            b = input_nybble(infile);

            if (b == 0) {
                /*
                 * bit map was written directly
                 */
                read_bdirect64(infile, a, n, nqx, nqy, scratch, bit);
            } else if (b != 0xf) {
                throw new RuntimeException("compresseionError");
            } else {
                /*
                 * bitmap was quadtree-coded, do log2n expansions read first
                 * code
                 */
                scratch[0] = (byte) input_huffman(infile);
                /*
                 * now do log2n expansions, reading codes from file as necessary
                 */
                nx = 1;
                ny = 1;
                nfx = nqx;
                nfy = nqy;
                c = 1 << log2n;
                for (k = 1; k < log2n; k++) {
                    /*
                     * this somewhat cryptic code generates the sequence n[k-1]
                     * = (n[k]+1)/2 where n[log2n]=nqx or nqy
                     */
                    c = c >> 1;
                    nx = nx << 1;
                    ny = ny << 1;
                    if (nfx <= c) {
                        nx -= 1;
                    } else {
                        nfx -= c;
                    }
                    if (nfy <= c) {
                        ny -= 1;
                    } else {
                        nfy -= c;
                    }
                    qtree_expand(infile, scratch, nx, ny, scratch);
                }
                /*
                 * now copy last set of 4-bit codes to bitplane bit of array a
                 */
                qtree_bitins64(scratch, nqx, nqy, a, n, bit);
            }
        }
        return (0);
    }

    int buffer2; /* Bits waiting to be input */

    int bits_to_go; /* Number of bits still in buffer */

    /* INITIALIZE BIT INPUT */

    /*
     * ##########################################################################
     * ##
     */
    void start_inputing_bits() {
        /*
         * Buffer starts out with no bits in it
         */
        bits_to_go = 0;
    }

    int input_nybble(ByteBuffer infile) {
        if (bits_to_go < 4) {
            /*
             * need another byte's worth of bits
             */

            buffer2 = (buffer2 << 8) | (infile.get() & 0xff);
            bits_to_go += 8;
        }
        /*
         * now pick off the first 4 bits
         */
        bits_to_go -= 4;

        return ((buffer2 >> bits_to_go) & 15);
    }

    int input_bit(ByteBuffer infile) {
        if (bits_to_go == 0) { /* Read the next byte if no */

            buffer2 = (infile.get() & 0xff);

            bits_to_go = 8;
        }
        /*
         * Return the next bit
         */
        bits_to_go -= 1;
        return ((buffer2 >> bits_to_go) & 1);
    }

    void read_bdirect64(ByteBuffer infile, LongArrayPointer a, int n, int nqx, int nqy, byte[] scratch, int bit) {
        /*
         * read bit image packed 4 pixels/nybble
         */
        /*
         * int i; for (i = 0; i < ((nqx+1)/2) * ((nqy+1)/2); i++) { scratch[i] =
         * input_nybble(infile); }
         */
        input_nnybble(infile, ((nqx + 1) / 2) * ((nqy + 1) / 2), scratch);

        /*
         * insert in bitplane BIT of image A
         */
        qtree_bitins64(scratch, nqx, nqy, a, n, bit);
    }

    int input_nnybble(ByteBuffer infile, int n, byte[] array) {
        /* copy n 4-bit nybbles from infile to the lower 4 bits of array */

        int ii, kk, shift1, shift2;

        /*
         * forcing byte alignment doesn;t help, and even makes it go slightly
         * slower if (bits_to_go != 8) input_nbits(infile, bits_to_go);
         */
        if (n == 1) {
            array[0] = (byte) input_nybble(infile);
            return (0);
        }

        if (bits_to_go == 8) {
            /*
             * already have 2 full nybbles in buffer2, so backspace the infile
             * array to reuse last char
             */
            infile.position(infile.position() - 1);
            bits_to_go = 0;
        }

        /* bits_to_go now has a value in the range 0 - 7. After adding */
        /* another byte, bits_to_go effectively will be in range 8 - 15 */

        shift1 = bits_to_go + 4; /* shift1 will be in range 4 - 11 */
        shift2 = bits_to_go; /* shift2 will be in range 0 - 7 */
        kk = 0;

        /* special case */
        if (bits_to_go == 0) {
            for (ii = 0; ii < n / 2; ii++) {
                /*
                 * refill the buffer with next byte
                 */
                buffer2 = (buffer2 << 8) | (infile.get() & 0xff);
                array[kk] = (byte) ((buffer2 >> 4) & 15);
                array[kk + 1] = (byte) ((buffer2) & 15); /* no shift required */
                kk += 2;
            }
        } else {
            for (ii = 0; ii < n / 2; ii++) {
                /*
                 * refill the buffer with next byte
                 */
                buffer2 = (buffer2 << 8) | (infile.get() & 0xff);
                array[kk] = (byte) ((buffer2 >> shift1) & 15);
                array[kk + 1] = (byte) ((buffer2 >> shift2) & 15);
                kk += 2;
            }
        }

        if (ii * 2 != n) { /* have to read last odd byte */
            array[n - 1] = (byte) input_nybble(infile);
        }

        return ((buffer2 >> bits_to_go) & 15);
    }

    /*
     * Huffman decoding for fixed codes Coded values range from 0-15 Huffman
     * code values (hex): 3e, 00, 01, 08, 02, 09, 1a, 1b, 03, 1c, 0a, 1d, 0b,
     * 1e, 3f, 0c and number of bits in each code: 6, 3, 3, 4, 3, 4, 5, 5, 3, 5,
     * 4, 5, 4, 5, 6, 4
     */
    int input_huffman(ByteBuffer infile) {
        int c;

        /*
         * get first 3 bits to start
         */
        c = input_nbits(infile, 3);
        if (c < 4) {
            /*
             * this is all we need return 1,2,4,8 for c=0,1,2,3
             */
            return (1 << c);
        }
        /*
         * get the next bit
         */
        c = input_bit(infile) | (c << 1);
        if (c < 13) {
            /*
             * OK, 4 bits is enough
             */
            switch (c) {
                case 8:
                    return (3);
                case 9:
                    return (5);
                case 10:
                    return (10);
                case 11:
                    return (12);
                case 12:
                    return (15);
            }
        }
        /*
         * get yet another bit
         */
        c = input_bit(infile) | (c << 1);
        if (c < 31) {
            /*
             * OK, 5 bits is enough
             */
            switch (c) {
                case 26:
                    return (6);
                case 27:
                    return (7);
                case 28:
                    return (9);
                case 29:
                    return (11);
                case 30:
                    return (13);
            }
        }
        /*
         * need the 6th bit
         */
        c = input_bit(infile) | (c << 1);
        if (c == 62) {
            return (0);
        } else {
            return (14);
        }
    }

    int input_nbits(ByteBuffer infile, int n) {
        /* AND mask for retreiving the right-most n bits */
        int[] mask = {
            0,
            1,
            3,
            7,
            15,
            31,
            63,
            127,
            255
        };

        if (bits_to_go < n) {
            /*
             * need another byte's worth of bits
             */

            buffer2 = (buffer2 << 8) | (infile.get() & 0xff);
            bits_to_go += 8;
        }
        /*
         * now pick off the first n bits
         */
        bits_to_go -= n;

        /* there was a slight gain in speed by replacing the following line */
        /* return( (buffer2>>bits_to_go) & ((1<<n)-1) ); */
        return ((buffer2 >> bits_to_go) & (mask[n]));
    }

    /*
     * do one quadtree expansion step on array a[(nqx+1)/2,(nqy+1)/2] results
     * put into b[nqx,nqy] (which may be the same as a)
     */
    void qtree_expand(ByteBuffer infile, byte[] a, int nx, int ny, byte[] b) {
        int i;

        /*
         * first copy a to b, expanding each 4-bit value
         */
        qtree_copy(a, nx, ny, b, ny);
        /*
         * now read new 4-bit values into b for each non-zero element
         */
        for (i = nx * ny - 1; i >= 0; i--) {
            if (b[i] != 0)
                b[i] = (byte) input_huffman(infile);
        }
    }

    /*
     * copy 4-bit values from a[(nx+1)/2,(ny+1)/2] to b[nx,ny], expanding each
     * value to 2x2 pixels a,b may be same array
     */
    static void qtree_copy(byte[] a, int nx, int ny, byte[] b, int n)
    /* int n; declared y dimension of b */
    {
        int i, j, k, nx2, ny2;
        int s00, s10;

        /*
         * first copy 4-bit values to b start at end in case a,b are same array
         */
        nx2 = (nx + 1) / 2;
        ny2 = (ny + 1) / 2;
        k = ny2 * (nx2 - 1) + ny2 - 1; /* k is index of a[i,j] */
        for (i = nx2 - 1; i >= 0; i--) {
            s00 = 2 * (n * i + ny2 - 1); /* s00 is index of b[2*i,2*j] */
            for (j = ny2 - 1; j >= 0; j--) {
                b[s00] = a[k];
                k -= 1;
                s00 -= 2;
            }
        }
        /*
         * now expand each 2x2 block
         */
        for (i = 0; i < nx - 1; i += 2) {

            /*
             * Note: Unlike the case in qtree_bitins, this code runs faster on a
             * 32-bit linux machine using the s10 intermediate variable, rather
             * that using s00+n. Go figure!
             */
            s00 = n * i; /* s00 is index of b[i,j] */
            s10 = s00 + n; /* s10 is index of b[i+1,j] */

            for (j = 0; j < ny - 1; j += 2) {

                switch (b[s00]) {
                    case (0):
                        b[s10 + 1] = 0;
                        b[s10] = 0;
                        b[s00 + 1] = 0;
                        b[s00] = 0;

                        break;
                    case (1):
                        b[s10 + 1] = 1;
                        b[s10] = 0;
                        b[s00 + 1] = 0;
                        b[s00] = 0;

                        break;
                    case (2):
                        b[s10 + 1] = 0;
                        b[s10] = 1;
                        b[s00 + 1] = 0;
                        b[s00] = 0;

                        break;
                    case (3):
                        b[s10 + 1] = 1;
                        b[s10] = 1;
                        b[s00 + 1] = 0;
                        b[s00] = 0;

                        break;
                    case (4):
                        b[s10 + 1] = 0;
                        b[s10] = 0;
                        b[s00 + 1] = 1;
                        b[s00] = 0;

                        break;
                    case (5):
                        b[s10 + 1] = 1;
                        b[s10] = 0;
                        b[s00 + 1] = 1;
                        b[s00] = 0;

                        break;
                    case (6):
                        b[s10 + 1] = 0;
                        b[s10] = 1;
                        b[s00 + 1] = 1;
                        b[s00] = 0;

                        break;
                    case (7):
                        b[s10 + 1] = 1;
                        b[s10] = 1;
                        b[s00 + 1] = 1;
                        b[s00] = 0;

                        break;
                    case (8):
                        b[s10 + 1] = 0;
                        b[s10] = 0;
                        b[s00 + 1] = 0;
                        b[s00] = 1;

                        break;
                    case (9):
                        b[s10 + 1] = 1;
                        b[s10] = 0;
                        b[s00 + 1] = 0;
                        b[s00] = 1;
                        break;
                    case (10):
                        b[s10 + 1] = 0;
                        b[s10] = 1;
                        b[s00 + 1] = 0;
                        b[s00] = 1;

                        break;
                    case (11):
                        b[s10 + 1] = 1;
                        b[s10] = 1;
                        b[s00 + 1] = 0;
                        b[s00] = 1;

                        break;
                    case (12):
                        b[s10 + 1] = 0;
                        b[s10] = 0;
                        b[s00 + 1] = 1;
                        b[s00] = 1;

                        break;
                    case (13):
                        b[s10 + 1] = 1;
                        b[s10] = 0;
                        b[s00 + 1] = 1;
                        b[s00] = 1;

                        break;
                    case (14):
                        b[s10 + 1] = 0;
                        b[s10] = 1;
                        b[s00 + 1] = 1;
                        b[s00] = 1;

                        break;
                    case (15):
                        b[s10 + 1] = 1;
                        b[s10] = 1;
                        b[s00 + 1] = 1;
                        b[s00] = 1;

                        break;
                }
                /*
                 * b[s10+1] = b[s00] & 1; b[s10 ] = (b[s00]>>1) & 1; b[s00+1] =
                 * (b[s00]>>2) & 1; b[s00 ] = (b[s00]>>3) & 1;
                 */

                s00 += 2;
                s10 += 2;
            }

            if (j < ny) {
                /*
                 * row size is odd, do last element in row s00+1, s10+1 are off
                 * edge
                 */
                /* not worth converting this to use 16 case statements */
                b[s10] = (byte) ((b[s00] >> 1) & 1);
                b[s00] = (byte) ((b[s00] >> 3) & 1);
            }
        }
        if (i < nx) {
            /*
             * column size is odd, do last row s10, s10+1 are off edge
             */
            s00 = n * i;
            for (j = 0; j < ny - 1; j += 2) {
                /* not worth converting this to use 16 case statements */
                b[s00 + 1] = (byte) ((b[s00] >> 2) & 1);
                b[s00] = (byte) ((b[s00] >> 3) & 1);
                s00 += 2;
            }
            if (j < ny) {
                /*
                 * both row and column size are odd, do corner element s00+1,
                 * s10, s10+1 are off edge
                 */
                /* not worth converting this to use 16 case statements */
                b[s00] = (byte) ((b[s00] >> 3) & 1);
            }
        }
    }

    /*
     * Copy 4-bit values from a[(nx+1)/2,(ny+1)/2] to b[nx,ny], expanding each
     * value to 2x2 pixels and inserting into bitplane BIT of B. A,B may NOT be
     * same array (it wouldn't make sense to be inserting bits into the same
     * array anyway.)
     */
    static void qtree_bitins64(byte[] a, int nx, int ny, LongArrayPointer b, int n, int bit)
    /*
     * int n; declared y dimension of b
     */
    {
        int i, j, k;
        int s00;
        long plane_val;

        plane_val = 1L << bit;

        /*
         * expand each 2x2 block
         */
        k = 0; /* k is index of a[i/2,j/2] */
        for (i = 0; i < nx - 1; i += 2) {
            s00 = n * i; /* s00 is index of b[i,j] */

            /*
             * Note: this code appears to run very slightly faster on a 32-bit
             * linux machine using s00+n rather than the s10 intermediate
             * variable
             */
            /* s10 = s00+n; *//* s10 is index of b[i+1,j] */
            for (j = 0; j < ny - 1; j += 2) {

                switch (a[k]) {
                    case (0):
                        break;
                    case (1):
                        b.bitOr(s00 + n + 1, plane_val);
                        break;
                    case (2):
                        b.bitOr(s00 + n, plane_val);
                        break;
                    case (3):
                        b.bitOr(s00 + n + 1, plane_val);
                        b.bitOr(s00 + n, plane_val);
                        break;
                    case (4):
                        b.bitOr(s00 + 1, plane_val);
                        break;
                    case (5):
                        b.bitOr(s00 + n + 1, plane_val);
                        b.bitOr(s00 + 1, plane_val);
                        break;
                    case (6):
                        b.bitOr(s00 + n, plane_val);
                        b.bitOr(s00 + 1, plane_val);
                        break;
                    case (7):
                        b.bitOr(s00 + n + 1, plane_val);
                        b.bitOr(s00 + n, plane_val);
                        b.bitOr(s00 + 1, plane_val);
                        break;
                    case (8):
                        b.bitOr(s00, plane_val);
                        break;
                    case (9):
                        b.bitOr(s00 + n + 1, plane_val);
                        b.bitOr(s00, plane_val);
                        break;
                    case (10):
                        b.bitOr(s00 + n, plane_val);
                        b.bitOr(s00, plane_val);
                        break;
                    case (11):
                        b.bitOr(s00 + n + 1, plane_val);
                        b.bitOr(s00 + n, plane_val);
                        b.bitOr(s00, plane_val);
                        break;
                    case (12):
                        b.bitOr(s00 + 1, plane_val);
                        b.bitOr(s00, plane_val);
                        break;
                    case (13):
                        b.bitOr(s00 + n + 1, plane_val);
                        b.bitOr(s00 + 1, plane_val);
                        b.bitOr(s00, plane_val);
                        break;
                    case (14):
                        b.bitOr(s00 + n, plane_val);
                        b.bitOr(s00 + 1, plane_val);
                        b.bitOr(s00, plane_val);
                        break;
                    case (15):
                        b.bitOr(s00 + n + 1, plane_val);
                        b.bitOr(s00 + n, plane_val);
                        b.bitOr(s00 + 1, plane_val);
                        b.bitOr(s00, plane_val);
                        break;
                }

                /*
                 * b.bitOr(s10+1, ((LONGLONG) ( a[k] & 1)) << bit; b.bitOr(s10 ,
                 * ((((LONGLONG)a[k])>>1) & 1) << bit; b.bitOr(s00+1,
                 * ((((LONGLONG)a[k])>>2) & 1) << bit; b.bitOr(s00 ,
                 * ((((LONGLONG)a[k])>>3) & 1) << bit;
                 */
                s00 += 2;
                /* s10 += 2; */
                k += 1;
            }
            if (j < ny) {
                /*
                 * row size is odd, do last element in row s00+1, s10+1 are off
                 * edge
                 */

                switch (a[k]) {
                    case (0):
                        break;
                    case (1):
                        break;
                    case (2):
                        b.bitOr(s00 + n, plane_val);
                        break;
                    case (3):
                        b.bitOr(s00 + n, plane_val);
                        break;
                    case (4):
                        break;
                    case (5):
                        break;
                    case (6):
                        b.bitOr(s00 + n, plane_val);
                        break;
                    case (7):
                        b.bitOr(s00 + n, plane_val);
                        break;
                    case (8):
                        b.bitOr(s00, plane_val);
                        break;
                    case (9):
                        b.bitOr(s00, plane_val);
                        break;
                    case (10):
                        b.bitOr(s00 + n, plane_val);
                        b.bitOr(s00, plane_val);
                        break;
                    case (11):
                        b.bitOr(s00 + n, plane_val);
                        b.bitOr(s00, plane_val);
                        break;
                    case (12):
                        b.bitOr(s00, plane_val);
                        break;
                    case (13):
                        b.bitOr(s00, plane_val);
                        break;
                    case (14):
                        b.bitOr(s00 + n, plane_val);
                        b.bitOr(s00, plane_val);
                        break;
                    case (15):
                        b.bitOr(s00 + n, plane_val);
                        b.bitOr(s00, plane_val);
                        break;
                }
                /*
                 * b.bitOr(s10 , ((((LONGLONG)a[k])>>1) & 1) << bit; b.bitOr(s00
                 * , ((((LONGLONG)a[k])>>3) & 1) << bit;
                 */
                k += 1;
            }
        }
        if (i < nx) {
            /*
             * column size is odd, do last row s10, s10+1 are off edge
             */
            s00 = n * i;
            for (j = 0; j < ny - 1; j += 2) {

                switch (a[k]) {
                    case (0):
                        break;
                    case (1):
                        break;
                    case (2):
                        break;
                    case (3):
                        break;
                    case (4):
                        b.bitOr(s00 + 1, plane_val);
                        break;
                    case (5):
                        b.bitOr(s00 + 1, plane_val);
                        break;
                    case (6):
                        b.bitOr(s00 + 1, plane_val);
                        break;
                    case (7):
                        b.bitOr(s00 + 1, plane_val);
                        break;
                    case (8):
                        b.bitOr(s00, plane_val);
                        break;
                    case (9):
                        b.bitOr(s00, plane_val);
                        break;
                    case (10):
                        b.bitOr(s00, plane_val);
                        break;
                    case (11):
                        b.bitOr(s00, plane_val);
                        break;
                    case (12):
                        b.bitOr(s00 + 1, plane_val);
                        b.bitOr(s00, plane_val);
                        break;
                    case (13):
                        b.bitOr(s00 + 1, plane_val);
                        b.bitOr(s00, plane_val);
                        break;
                    case (14):
                        b.bitOr(s00 + 1, plane_val);
                        b.bitOr(s00, plane_val);
                        break;
                    case (15):
                        b.bitOr(s00 + 1, plane_val);
                        b.bitOr(s00, plane_val);
                        break;
                }

                /*
                 * b.bitOr(s00+1, ((((LONGLONG)a[k])>>2) & 1) << bit;
                 * b.bitOr(s00 , ((((LONGLONG)a[k])>>3) & 1) << bit;
                 */
                s00 += 2;
                k += 1;
            }
            if (j < ny) {
                /*
                 * both row and column size are odd, do corner element s00+1,
                 * s10, s10+1 are off edge
                 */

                switch (a[k]) {
                    case (0):
                        break;
                    case (1):
                        break;
                    case (2):
                        break;
                    case (3):
                        break;
                    case (4):
                        break;
                    case (5):
                        break;
                    case (6):
                        break;
                    case (7):
                        break;
                    case (8):
                        b.bitOr(s00, plane_val);
                        break;
                    case (9):
                        b.bitOr(s00, plane_val);
                        break;
                    case (10):
                        b.bitOr(s00, plane_val);
                        break;
                    case (11):
                        b.bitOr(s00, plane_val);
                        break;
                    case (12):
                        b.bitOr(s00, plane_val);
                        break;
                    case (13):
                        b.bitOr(s00, plane_val);
                        break;
                    case (14):
                        b.bitOr(s00, plane_val);
                        break;
                    case (15):
                        b.bitOr(s00, plane_val);
                        break;
                }
                /*
                 * b.bitOr(s00 , ((((LONGLONG)a[k])>>3) & 1) << bit;
                 */
                k += 1;
            }
        }
    }
}
