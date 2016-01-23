package nom.tam.fits.compression.algorithm.hcompress;

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

import static nom.tam.fits.compression.algorithm.hcompress.HCompress.BITS_OF_1_BYTE;
import static nom.tam.fits.compression.algorithm.hcompress.HCompress.BITS_OF_1_NYBBLE;
import static nom.tam.fits.compression.algorithm.hcompress.HCompress.BYTE_MASK;
import static nom.tam.fits.compression.algorithm.hcompress.HCompress.NYBBLE_MASK;
import static nom.tam.fits.compression.algorithm.hcompress.HCompress.ROUNDING_HALF;

import java.nio.ByteBuffer;

/**
 * The original decompression code was written by R. White at the STScI and
 * included (ported to c and adapted) in cfitsio by William Pence, NASA/GSFC.
 * That code was then ported to java by R. van Nieuwenhoven. Later it was
 * massively refactored to harmonize the different compression algorithms and
 * reduce the duplicate code pieces without obscuring the algorithm itself as
 * far as possible. The original site for the algorithm is
 *
 * <pre>
 *  @see <a href="http://www.stsci.edu/software/hcompress.html">http://www.stsci.edu/software/hcompress.html</a>
 * </pre>
 *
 * @author Richard White
 * @author William Pence
 * @author Richard van Nieuwenhoven
 */
public class HDecompress {

    private static class LongArrayPointer {

        private final long[] a;

        private int offset;

        LongArrayPointer(long[] tmp) {
            this.a = tmp;
            this.offset = 0;
        }

        public void bitOr(int i, long planeVal) {
            this.a[this.offset + i] |= planeVal;

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

    }

    private static final byte[] CODE_MAGIC = {
        (byte) 0xDD,
        (byte) 0x99
    };

    private static final int[] MASKS = {
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

    private static final byte ZERO = 0;

    private static final byte BIT_ONE = 1;

    private static final byte BIT_TWO = 2;

    private static final byte BIT_THREE = 4;

    private static final byte BIT_FOUR = 8;

    /**
     * these N constants are obscuring the algorithm and should get some
     * explaining javadoc if somebody understands the algorithm.
     */
    private static final int N03 = 3;

    private static final int N04 = 4;

    private static final int N05 = 5;

    private static final int N06 = 6;

    private static final int N07 = 7;

    private static final int N08 = 8;

    private static final int N09 = 9;

    private static final int N10 = 10;

    private static final int N11 = 11;

    private static final int N12 = 12;

    private static final int N13 = 13;

    private static final int N14 = 14;

    private static final int N15 = 15;

    private static final int N26 = 26;

    private static final int N27 = 27;

    private static final int N28 = 28;

    private static final int N29 = 29;

    private static final int N30 = 30;

    private static final int N31 = 31;

    private static final int N62 = 62;

    private static final int N63 = 63;

    /**
     * Number of bits still in buffer
     */
    private int bitsToGo;

    /** Bits waiting to be input */
    private int buffer2;

    private int nx;

    private int ny;

    private int scale;

    /**
     * log2n is log2 of max(nx,ny) rounded up to next power of 2
     */
    private int calculateLog2N(int nmax) {
        int log2n;
        log2n = (int) (Math.log(nmax) / Math.log(2.0) + ROUNDING_HALF);
        if (nmax > 1 << log2n) {
            log2n += 1;
        }
        return log2n;
    }

    /**
     * char *infile; input file long *a; address of output tiledImageOperation
     * [nx][ny] int *nx,*ny; size of output tiledImageOperation int *scale;
     * scale factor for digitization
     *
     * @param infile
     * @param a
     */
    private void decode64(ByteBuffer infile, LongArrayPointer a) {
        byte[] nbitplanes = new byte[N03];
        byte[] tmagic = new byte[2];

        /*
         * File starts either with special 2-byte magic code or with FITS
         * keyword "SIMPLE  ="
         */
        infile.get(tmagic);
        /*
         * check for correct magic code value
         */
        if (tmagic[0] != CODE_MAGIC[0] || tmagic[1] != CODE_MAGIC[1]) {
            throw new RuntimeException("Compression error");
        }
        this.nx = infile.getInt(); /* x size of image */
        this.ny = infile.getInt(); /* y size of image */
        this.scale = infile.getInt(); /* scale factor for digitization */

        /* sum of all pixels */
        long sumall = infile.getLong();
        /* # bits in quadrants */

        infile.get(nbitplanes);

        dodecode64(infile, a, nbitplanes);
        /*
         * put sum of all pixels back into pixel 0
         */
        a.set(0, sumall);
    }

    /**
     * decompress the input byte stream using the H-compress algorithm input -
     * input tiledImageOperation of compressed bytes a - pre-allocated
     * tiledImageOperation to hold the output uncompressed image nx - returned X
     * axis size ny - returned Y axis size NOTE: the nx and ny dimensions as
     * defined within this code are reversed from the usual FITS notation. ny is
     * the fastest varying dimension, which is usually considered the X axis in
     * the FITS image display
     *
     * @param input
     *            the input buffer to decompress
     * @param smooth
     *            should the image be smoothed
     * @param aa
     *            the resulting long tiledImageOperation
     */
    public void decompress(ByteBuffer input, boolean smooth, long[] aa) {

        LongArrayPointer a = new LongArrayPointer(aa);

        /* decode the input tiledImageOperation */

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

    /**
     * long a[]; int nx,ny; Array dimensions are [nx][ny] unsigned char
     * nbitplanes[3]; Number of bit planes in quadrants
     */
    private int dodecode64(ByteBuffer infile, LongArrayPointer a, byte[] nbitplanes) {
        int nel = this.nx * this.ny;
        int nx2 = (this.nx + 1) / 2;
        int ny2 = (this.ny + 1) / 2;
        /*
         * initialize a to zero
         */
        for (int i = 0; i < nel; i++) {
            a.set(i, 0);
        }
        /*
         * Initialize bit input
         */
        startInputingBits();
        /*
         * read bit planes for each quadrant
         */
        qtreeDecode64(infile, a.copy(0), this.ny, nx2, ny2, nbitplanes[0]);

        qtreeDecode64(infile, a.copy(ny2), this.ny, nx2, this.ny / 2, nbitplanes[1]);

        qtreeDecode64(infile, a.copy(this.ny * nx2), this.ny, this.nx / 2, ny2, nbitplanes[1]);

        qtreeDecode64(infile, a.copy(this.ny * nx2 + ny2), this.ny, this.nx / 2, this.ny / 2, nbitplanes[2]);

        /*
         * make sure there is an EOF symbol (nybble=0) at end
         */
        if (inputNybble(infile) != 0) {
            throw new RuntimeException("Compression error");
        }
        /*
         * now get the sign bits Re-initialize bit input
         */
        startInputingBits();
        for (int i = 0; i < nel; i++) {
            if (a.get(i) != 0) {
                if (inputBit(infile) != 0) {
                    a.set(i, -a.get(i));
                }
            }
        }
        return 0;
    }

    /**
     * int smooth; 0 for no smoothing, else smooth during inversion int scale;
     * used if smoothing is specified
     */
    private int hinv64(LongArrayPointer a, boolean smooth) {
        int nmax = this.nx > this.ny ? this.nx : this.ny;
        int log2n = calculateLog2N(nmax);
        // get temporary storage for shuffling elements
        long[] tmp = new long[(nmax + 1) / 2];
        // set up masks, rounding parameters
        int shift = 1;
        long bit0 = (long) 1 << log2n - 1;
        long bit1 = bit0 << 1;
        long bit2 = bit0 << 2;
        long mask0 = -bit0;
        long mask1 = mask0 << 1;
        long mask2 = mask0 << 2;
        long prnd0 = bit0 >> 1;
        long prnd1 = bit1 >> 1;
        long prnd2 = bit2 >> 1;
        long nrnd0 = prnd0 - 1;
        long nrnd1 = prnd1 - 1;
        long nrnd2 = prnd2 - 1;
        // round h0 to multiple of bit2
        a.set(0, a.get(0) + (a.get(0) >= 0 ? prnd2 : nrnd2) & mask2);
        // do log2n expansions We're indexing a as a 2-D tiledImageOperation
        // with dimensions
        // (nx,ny).
        int nxtop = 1;
        int nytop = 1;
        int nxf = this.nx;
        int nyf = this.ny;
        int c = 1 << log2n;
        int i;
        for (int k = log2n - 1; k >= 0; k--) {
            // this somewhat cryptic code generates the sequence ntop[k-1] =
            // (ntop[k]+1)/2, where ntop[log2n] = n
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
            // double shift and fix nrnd0 (because prnd0=0) on last pass
            if (k == 0) {
                nrnd0 = 0;
                shift = 2;
            }
            // unshuffle in each dimension to interleave coefficients
            for (i = 0; i < nxtop; i++) {
                unshuffle64(a.copy(this.ny * i), nytop, 1, tmp);
            }
            for (int j = 0; j < nytop; j++) {
                unshuffle64(a.copy(j), nxtop, this.ny, tmp);
            }
            // smooth by interpolating coefficients if SMOOTH != 0
            if (smooth) {
                hsmooth64(a, nxtop, nytop);
            }
            int oddx = nxtop % 2;
            int oddy = nytop % 2;
            for (i = 0; i < nxtop - oddx; i += 2) {
                int s00 = this.ny * i; /* s00 is index of a[i,j] */
                int s10 = s00 + this.ny; /* s10 is index of a[i+1,j] */
                for (int j = 0; j < nytop - oddy; j += 2) {
                    long h0 = a.get(s00);
                    long hx = a.get(s10);
                    long hy = a.get(s00 + 1);
                    long hc = a.get(s10 + 1);
                    // round hx and hy to multiple of bit1, hc to multiple of
                    // bit0 h0 is already a multiple of bit2
                    hx = hx + (hx >= 0 ? prnd1 : nrnd1) & mask1;
                    hy = hy + (hy >= 0 ? prnd1 : nrnd1) & mask1;
                    hc = hc + (hc >= 0 ? prnd0 : nrnd0) & mask0;
                    // propagate bit0 of hc to hx,hy
                    long lowbit0 = hc & bit0;
                    hx = hx >= 0 ? hx - lowbit0 : hx + lowbit0;
                    hy = hy >= 0 ? hy - lowbit0 : hy + lowbit0;
                    // Propagate bits 0 and 1 of hc,hx,hy to h0. This could be
                    // simplified if we assume h0>0, but then the inversion
                    // would not be lossless for images with negative pixels.
                    long lowbit1 = (hc ^ hx ^ hy) & bit1;
                    h0 = h0 >= 0 ? h0 + lowbit0 - lowbit1 : h0 + (lowbit0 == 0 ? lowbit1 : lowbit0 - lowbit1);
                    // Divide sums by 2 (4 last time)
                    a.set(s10 + 1, h0 + hx + hy + hc >> shift);
                    a.set(s10, h0 + hx - hy - hc >> shift);
                    a.set(s00 + 1, h0 - hx + hy - hc >> shift);
                    a.set(s00, h0 - hx - hy + hc >> shift);
                    s00 += 2;
                    s10 += 2;
                }
                if (oddy != 0) {
                    // do last element in row if row length is odd s00+1, s10+1
                    // are off edge
                    long h0 = a.get(s00);
                    long hx = a.get(s10);
                    hx = (hx >= 0 ? hx + prnd1 : hx + nrnd1) & mask1;
                    long lowbit1 = hx & bit1;
                    h0 = h0 >= 0 ? h0 - lowbit1 : h0 + lowbit1;
                    a.set(s10, h0 + hx >> shift);
                    a.set(s00, h0 - hx >> shift);
                }
            }
            if (oddx != 0) {
                // do last row if column length is odd s10, s10+1 are off edge
                int s00 = this.ny * i;
                for (int j = 0; j < nytop - oddy; j += 2) {
                    long h0 = a.get(s00);
                    long hy = a.get(s00 + 1);
                    hy = (hy >= 0 ? hy + prnd1 : hy + nrnd1) & mask1;
                    long lowbit1 = hy & bit1;
                    h0 = h0 >= 0 ? h0 - lowbit1 : h0 + lowbit1;
                    a.set(s00 + 1, h0 + hy >> shift);
                    a.set(s00, h0 - hy >> shift);
                    s00 += 2;
                }
                if (oddy != 0) {
                    // do corner element if both row and column lengths are odd
                    // s00+1, s10, s10+1 are off edge
                    long h0 = a.get(s00);
                    a.set(s00, h0 >> shift);
                }
            }
            // divide all the masks and rounding values by 2
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
        return 0;
    }

    /**
     * long a[]; tiledImageOperation of H-transform coefficients int
     * nxtop,nytop; size of coefficient block to use int ny; actual 1st
     * dimension of tiledImageOperation int scale; truncation scale factor that
     * was used
     */
    private void hsmooth64(LongArrayPointer a, int nxtop, int nytop) {
        int i, j;
        int ny2, s10, s00;
        long hm, h0, hp, hmm, hpm, hmp, hpp, hx2, hy2, diff, dmax, dmin, s, smax, m1, m2;

        /*
         * Maximum change in coefficients is determined by scale factor. Since
         * we rounded during division (see digitize.c), the biggest permitted
         * change is scale/2.
         */
        smax = this.scale >> 1;
        if (smax <= 0) {
            return;
        }
        ny2 = this.ny << 1;
        /*
         * We're indexing a as a 2-D tiledImageOperation with dimensions
         * (nxtop,ny) of which only (nxtop,nytop) are used. The coefficients on
         * the edge of the tiledImageOperation are not adjusted (which is why
         * the loops below start at 2 instead of 0 and end at nxtop-2 instead of
         * nxtop.)
         */
        /*
         * Adjust x difference hx
         */
        for (i = 2; i < nxtop - 2; i += 2) {
            s00 = this.ny * i; /* s00 is index of a[i,j] */
            s10 = s00 + this.ny; /* s10 is index of a[i+1,j] */
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
                dmax = Math.max(Math.min(hp - h0, h0 - hm), 0) << 2;
                dmin = Math.min(Math.max(hp - h0, h0 - hm), 0) << 2;
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
                    s = diff - (a.get(s10) << N03);
                    s = s >= 0 ? s >> N03 : s + N07 >> N03;
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
            s00 = this.ny * i + 2;
            s10 = s00 + this.ny;
            for (j = 2; j < nytop - 2; j += 2) {
                hm = a.get(s00 - 2);
                h0 = a.get(s00);
                hp = a.get(s00 + 2);
                diff = hp - hm;
                dmax = Math.max(Math.min(hp - h0, h0 - hm), 0) << 2;
                dmin = Math.min(Math.max(hp - h0, h0 - hm), 0) << 2;
                if (dmin < dmax) {
                    diff = Math.max(Math.min(diff, dmax), dmin);
                    s = diff - (a.get(s00 + 1) << N03);
                    s = s >= 0 ? s >> N03 : s + N07 >> N03;
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
            s00 = this.ny * i + 2;
            s10 = s00 + this.ny;
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
                dmax = Math.min(m1, m2) << BITS_OF_1_NYBBLE;
                m1 = Math.max(Math.min(hpp - h0, 0) - hx2 - hy2, Math.min(h0 - hpm, 0) + hx2 - hy2);
                m2 = Math.max(Math.min(h0 - hmp, 0) - hx2 + hy2, Math.min(hmm - h0, 0) + hx2 + hy2);
                dmin = Math.max(m1, m2) << BITS_OF_1_NYBBLE;
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
                    s = diff - (a.get(s10 + 1) << N06);
                    s = s >= 0 ? s >> N06 : s + N63 >> N06;
                    s = Math.max(Math.min(s, smax), -smax);
                    a.set(s10 + 1, a.get(s10 + 1) + s);
                }
                s00 += 2;
                s10 += 2;
            }
        }
    }

    private int inputBit(ByteBuffer infile) {
        if (this.bitsToGo == 0) { /* Read the next byte if no */

            this.buffer2 = infile.get() & BYTE_MASK;

            this.bitsToGo = BITS_OF_1_BYTE;
        }
        /*
         * Return the next bit
         */
        this.bitsToGo -= 1;
        return this.buffer2 >> this.bitsToGo & 1;
    }

    /*
     * Huffman decoding for fixed codes Coded values range from 0-15 Huffman
     * code values (hex): 3e, 00, 01, 08, 02, 09, 1a, 1b, 03, 1c, 0a, 1d, 0b,
     * 1e, 3f, 0c and number of bits in each code: 6, 3, 3, 4, 3, 4, 5, 5, 3, 5,
     * 4, 5, 4, 5, 6, 4
     */
    private int inputHuffman(ByteBuffer infile) {
        int c;

        /*
         * get first 3 bits to start
         */
        c = inputNbits(infile, N03);
        if (c < N04) {
            /*
             * this is all we need return 1,2,4,8 for c=0,1,2,3
             */
            return 1 << c;
        }
        /*
         * get the next bit
         */
        c = inputBit(infile) | c << 1;
        if (c < N13) {
            /*
             * OK, 4 bits is enough
             */
            switch (c) {
                case N08:
                    return N03;
                case N09:
                    return N05;
                case N10:
                    return N10;
                case N11:
                    return N12;
                case N12:
                    return N15;
                default:
            }
        }
        /*
         * get yet another bit
         */
        c = inputBit(infile) | c << 1;
        if (c < N31) {
            /*
             * OK, 5 bits is enough
             */
            switch (c) {
                case N26:
                    return N06;
                case N27:
                    return N07;
                case N28:
                    return N09;
                case N29:
                    return N11;
                case N30:
                    return N13;
                default:
            }
        }
        /*
         * need the 6th bit
         */
        c = inputBit(infile) | c << 1;
        if (c == N62) {
            return 0;
        } else {
            return N14;
        }
    }

    private int inputNbits(ByteBuffer infile, int n) {
        if (this.bitsToGo < n) {
            /*
             * need another byte's worth of bits
             */

            this.buffer2 = this.buffer2 << BITS_OF_1_BYTE | infile.get() & BYTE_MASK;
            this.bitsToGo += BITS_OF_1_BYTE;
        }
        /*
         * now pick off the first n bits
         */
        this.bitsToGo -= n;

        /* there was a slight gain in speed by replacing the following line */
        /* return( (buffer2>>bits_to_go) & ((1<<n)-1) ); */
        return this.buffer2 >> this.bitsToGo & MASKS[n];
    }

    /* INITIALIZE BIT INPUT */

    private int inputNnybble(ByteBuffer infile, int n, byte[] array) {
        /*
         * copy n 4-bit nybbles from infile to the lower 4 bits of
         * tiledImageOperation
         */

        int ii, kk, shift1, shift2;

        /*
         * forcing byte alignment doesn;t help, and even makes it go slightly
         * slower if (bits_to_go != 8) input_nbits(infile, bits_to_go);
         */
        if (n == 1) {
            array[0] = (byte) inputNybble(infile);
            return 0;
        }

        if (this.bitsToGo == BITS_OF_1_BYTE) {
            /*
             * already have 2 full nybbles in buffer2, so backspace the infile
             * tiledImageOperation to reuse last char
             */
            infile.position(infile.position() - 1);
            this.bitsToGo = 0;
        }

        /* bits_to_go now has a value in the range 0 - 7. After adding */
        /* another byte, bits_to_go effectively will be in range 8 - 15 */

        shift1 = this.bitsToGo + BITS_OF_1_NYBBLE; /*
                                                    * shift1 will be in range 4
                                                    * - 11
                                                    */
        shift2 = this.bitsToGo; /* shift2 will be in range 0 - 7 */
        kk = 0;

        /* special case */
        if (this.bitsToGo == 0) {
            for (ii = 0; ii < n / 2; ii++) {
                /*
                 * refill the buffer with next byte
                 */
                this.buffer2 = this.buffer2 << BITS_OF_1_BYTE | infile.get() & BYTE_MASK;
                array[kk] = (byte) (this.buffer2 >> BITS_OF_1_NYBBLE & NYBBLE_MASK);
                array[kk + 1] = (byte) (this.buffer2 & NYBBLE_MASK); /*
                                                                      * no shift
                                                                      * required
                                                                      */
                kk += 2;
            }
        } else {
            for (ii = 0; ii < n / 2; ii++) {
                /*
                 * refill the buffer with next byte
                 */
                this.buffer2 = this.buffer2 << BITS_OF_1_BYTE | infile.get() & BYTE_MASK;
                array[kk] = (byte) (this.buffer2 >> shift1 & NYBBLE_MASK);
                array[kk + 1] = (byte) (this.buffer2 >> shift2 & NYBBLE_MASK);
                kk += 2;
            }
        }

        if (ii * 2 != n) { /* have to read last odd byte */
            array[n - 1] = (byte) inputNybble(infile);
        }

        return this.buffer2 >> this.bitsToGo & NYBBLE_MASK;
    }

    private int inputNybble(ByteBuffer infile) {
        if (this.bitsToGo < BITS_OF_1_NYBBLE) {
            /*
             * need another byte's worth of bits
             */

            this.buffer2 = this.buffer2 << BITS_OF_1_BYTE | infile.get() & BYTE_MASK;
            this.bitsToGo += BITS_OF_1_BYTE;
        }
        /*
         * now pick off the first 4 bits
         */
        this.bitsToGo -= BITS_OF_1_NYBBLE;

        return this.buffer2 >> this.bitsToGo & NYBBLE_MASK;
    }

    /**
     * Copy 4-bit values from a[(nx+1)/2,(ny+1)/2] to b[nx,ny], expanding each
     * value to 2x2 pixels and inserting into bitplane BIT of B. A,B may NOT be
     * same tiledImageOperation (it wouldn't make sense to be inserting bits
     * into the same tiledImageOperation anyway.)
     */
    private void qtreeBitins64(byte[] a, int lnx, int lny, LongArrayPointer b, int n, int bit) {
        int i, j, s00;
        long planeVal = 1L << bit;
        // expand each 2x2 block
        ByteBuffer k = ByteBuffer.wrap(a); /* k is index of a[i/2,j/2] */
        for (i = 0; i < lnx - 1; i += 2) {
            s00 = n * i; /* s00 is index of b[i,j] */
            // Note: this code appears to run very slightly faster on a 32-bit
            // linux machine using s00+n rather than the s10 intermediate
            // variable
            // s10 = s00+n; *//* s10 is index of b[i+1,j]
            for (j = 0; j < lny - 1; j += 2) {
                byte value = k.get();
                if ((value & BIT_ONE) != ZERO) {
                    b.bitOr(s00 + n + 1, planeVal);
                }
                if ((value & BIT_TWO) != ZERO) {
                    b.bitOr(s00 + n, planeVal);
                }
                if ((value & BIT_THREE) != ZERO) {
                    b.bitOr(s00 + 1, planeVal);
                }
                if ((value & BIT_FOUR) != ZERO) {
                    b.bitOr(s00, planeVal);
                }
                // b.bitOr(s10+1, ((LONGLONG) ( a[k] & 1)) << bit; b.bitOr(s10 ,
                // ((((LONGLONG)a[k])>>1) & 1) << bit; b.bitOr(s00+1,
                // ((((LONGLONG)a[k])>>2) & 1) << bit; b.bitOr(s00
                // ,((((LONGLONG)a[k])>>3) & 1) << bit;
                s00 += 2;
                /* s10 += 2; */
            }
            if (j < lny) {
                // row size is odd, do last element in row s00+1, s10+1 are off
                // edge
                byte value = k.get();
                if ((value & BIT_TWO) != ZERO) {
                    b.bitOr(s00 + n, planeVal);
                }
                if ((value & BIT_FOUR) != ZERO) {
                    b.bitOr(s00, planeVal);
                }
                // b.bitOr(s10 , ((((LONGLONG)a[k])>>1) & 1) << bit; b.bitOr(s00
                // , ((((LONGLONG)a[k])>>3) & 1) << bit;
            }
        }
        if (i < lnx) {
            // column size is odd, do last row s10, s10+1 are off edge
            s00 = n * i;
            for (j = 0; j < lny - 1; j += 2) {
                byte value = k.get();
                if ((value & BIT_THREE) != ZERO) {
                    b.bitOr(s00 + 1, planeVal);
                }
                if ((value & BIT_FOUR) != ZERO) {
                    b.bitOr(s00, planeVal);
                } // b.bitOr(s00+1, ((((LONGLONG)a[k])>>2) & 1) << bit;
                  // b.bitOr(s00 , ((((LONGLONG)a[k])>>3) & 1) << bit;
                s00 += 2;
            }
            if (j < lny) {
                // both row and column size are odd, do corner element s00+1,
                // s10, s10+1 are off edge
                if ((k.get() & BIT_FOUR) != ZERO) {
                    b.bitOr(s00, planeVal);
                }
                // b.bitOr(s00 , ((((LONGLONG)a[k])>>3) & 1) << bit;
            }
        }
    }

    /**
     * copy 4-bit values from a[(nx+1)/2,(ny+1)/2] to b[nx,ny], expanding each
     * value to 2x2 pixels a,b may be same tiledImageOperation
     */
    private void qtreeCopy(byte[] a, int lnx, int lny, byte[] b, int n) {
        int i, j, k, nx2, ny2;
        int s00, s10;
        // first copy 4-bit values to b start at end in case a,b are same
        // tiledImageOperation
        nx2 = (lnx + 1) / 2;
        ny2 = (lny + 1) / 2;
        k = ny2 * (nx2 - 1) + ny2 - 1; /* k is index of a[i,j] */
        for (i = nx2 - 1; i >= 0; i--) {
            s00 = 2 * (n * i + ny2 - 1); /* s00 is index of b[2*i,2*j] */
            for (j = ny2 - 1; j >= 0; j--) {
                b[s00] = a[k];
                k -= 1;
                s00 -= 2;
            }
        }
        for (i = 0; i < lnx - 1; i += 2) { // now expand each 2x2 block
            // Note: Unlike the case in qtree_bitins, this code runs faster on a
            // 32-bit linux machine using the s10 intermediate variable, rather
            // that using s00+n. Go figure!
            s00 = n * i; // s00 is index of b[i,j]
            s10 = s00 + n; // s10 is index of b[i+1,j]
            for (j = 0; j < lny - 1; j += 2) {
                b[s10 + 1] = (b[s00] & BIT_ONE) == ZERO ? ZERO : BIT_ONE;
                b[s10] = (b[s00] & BIT_TWO) == ZERO ? ZERO : BIT_ONE;
                b[s00 + 1] = (b[s00] & BIT_THREE) == ZERO ? ZERO : BIT_ONE;
                b[s00] = (b[s00] & BIT_FOUR) == ZERO ? ZERO : BIT_ONE;
                s00 += 2;
                s10 += 2;
            }
            if (j < lny) {
                // row size is odd, do last element in row s00+1, s10+1 are off
                // edge not worth converting this to use 16 case statements
                b[s10] = (byte) (b[s00] >> 1 & 1);
                b[s00] = (byte) (b[s00] >> N03 & 1);
            }
        }
        if (i < lnx) {
            // column size is odd, do last row s10, s10+1 are off edge
            s00 = n * i;
            for (j = 0; j < lny - 1; j += 2) {
                // not worth converting this to use 16 case statements
                b[s00 + 1] = (byte) (b[s00] >> 2 & 1);
                b[s00] = (byte) (b[s00] >> N03 & 1);
                s00 += 2;
            }
            if (j < lny) {
                // both row and column size are odd, do corner element s00+1,
                // s10, s10+1 are off edge not worth converting this to use 16
                // case statements
                b[s00] = (byte) (b[s00] >> N03 & 1);
            }
        }
    }

    /**
     * char *infile; long a[]; a is 2-D tiledImageOperation with dimensions
     * (n,n) int n; length of full row in a int nqx; partial length of row to
     * decode int nqy; partial length of column (<=n) int nbitplanes; number of
     * bitplanes to decode
     */
    private int qtreeDecode64(ByteBuffer infile, LongArrayPointer a, int n, int nqx, int nqy, int nbitplanes) {
        int k, bit, b;
        int nx2, ny2, nfx, nfy, c;
        byte[] scratch;

        /*
         * log2n is log2 of max(nqx,nqy) rounded up to next power of 2
         */
        int nqmax = nqx > nqy ? nqx : nqy;
        int log2n = calculateLog2N(nqmax);
        /*
         * allocate scratch tiledImageOperation for working space
         */
        int nqx2 = (nqx + 1) / 2;
        int nqy2 = (nqy + 1) / 2;
        scratch = new byte[nqx2 * nqy2];

        /*
         * now decode each bit plane, starting at the top A is assumed to be
         * initialized to zero
         */
        for (bit = nbitplanes - 1; bit >= 0; bit--) {
            /*
             * Was bitplane was quadtree-coded or written directly?
             */
            b = inputNybble(infile);

            if (b == 0) {
                /*
                 * bit map was written directly
                 */
                readBdirect64(infile, a, n, nqx, nqy, scratch, bit);
            } else if (b != NYBBLE_MASK) {
                throw new RuntimeException("Compression error");
            } else {
                /*
                 * bitmap was quadtree-coded, do log2n expansions read first
                 * code
                 */
                scratch[0] = (byte) inputHuffman(infile);
                /*
                 * now do log2n expansions, reading codes from file as necessary
                 */
                nx2 = 1;
                ny2 = 1;
                nfx = nqx;
                nfy = nqy;
                c = 1 << log2n;
                for (k = 1; k < log2n; k++) {
                    /*
                     * this somewhat cryptic code generates the sequence n[k-1]
                     * = (n[k]+1)/2 where n[log2n]=nqx or nqy
                     */
                    c = c >> 1;
                    nx2 = nx2 << 1;
                    ny2 = ny2 << 1;
                    if (nfx <= c) {
                        nx2 -= 1;
                    } else {
                        nfx -= c;
                    }
                    if (nfy <= c) {
                        ny2 -= 1;
                    } else {
                        nfy -= c;
                    }
                    qtreeExpand(infile, scratch, nx2, ny2, scratch);
                }
                /*
                 * now copy last set of 4-bit codes to bitplane bit of
                 * tiledImageOperation a
                 */
                qtreeBitins64(scratch, nqx, nqy, a, n, bit);
            }
        }
        return 0;
    }

    /*
     * do one quadtree expansion step on tiledImageOperation
     * a[(nqx+1)/2,(nqy+1)/2] results put into b[nqx,nqy] (which may be the same
     * as a)
     */
    private void qtreeExpand(ByteBuffer infile, byte[] a, int nx2, int ny2, byte[] b) {
        int i;

        /*
         * first copy a to b, expanding each 4-bit value
         */
        qtreeCopy(a, nx2, ny2, b, ny2);
        /*
         * now read new 4-bit values into b for each non-zero element
         */
        for (i = nx2 * ny2 - 1; i >= 0; i--) {
            if (b[i] != 0) {
                b[i] = (byte) inputHuffman(infile);
            }
        }
    }

    private void readBdirect64(ByteBuffer infile, LongArrayPointer a, int n, int nqx, int nqy, byte[] scratch, int bit) {
        /*
         * read bit image packed 4 pixels/nybble
         */
        /*
         * int i; for (i = 0; i < ((nqx+1)/2) * ((nqy+1)/2); i++) { scratch[i] =
         * input_nybble(infile); }
         */
        inputNnybble(infile, (nqx + 1) / 2 * ((nqy + 1) / 2), scratch);

        /*
         * insert in bitplane BIT of image A
         */
        qtreeBitins64(scratch, nqx, nqy, a, n, bit);
    }

    /*
     * ##########################################################################
     * ##
     */
    private void startInputingBits() {
        /*
         * Buffer starts out with no bits in it
         */
        this.bitsToGo = 0;
    }

    private void undigitize64(LongArrayPointer a) {
        long scale64;

        /*
         * multiply by scale
         */
        if (this.scale <= 1) {
            return;
        }
        scale64 = this.scale; /*
                               * use a 64-bit int for efficiency in the big loop
                               */

        for (int index = 0; index < a.a.length; index++) {
            a.a[index] = a.a[index] * scale64;
        }
    }

    /**
     * long a[]; tiledImageOperation to shuffle int n; number of elements to
     * shuffle int n2; second dimension long tmp[]; scratch storage
     */
    private void unshuffle64(LongArrayPointer a, int n, int n2, long[] tmp) {
        int i;
        int nhalf;
        LongArrayPointer p1, p2, pt;

        /*
         * copy 2nd half of tiledImageOperation to tmp
         */
        nhalf = n + 1 >> 1;
        pt = new LongArrayPointer(tmp);
        p1 = a.copy(n2 * nhalf); /* pointer to a[i] */
        for (i = nhalf; i < n; i++) {
            pt.set(p1.get());
            p1.offset += n2;
            pt.offset += 1;
        }
        /*
         * distribute 1st half of tiledImageOperation to even elements
         */
        p2 = a.copy(n2 * (nhalf - 1)); /* pointer to a[i] */
        p1 = a.copy(n2 * (nhalf - 1) << 1); /* pointer to a[2*i] */
        for (i = nhalf - 1; i >= 0; i--) {
            p1.set(p2.get());
            p2.offset -= n2;
            p1.offset -= n2 + n2;
        }
        /*
         * now distribute 2nd half of tiledImageOperation (in tmp) to odd
         * elements
         */
        pt = new LongArrayPointer(tmp);
        p1 = a.copy(n2); /* pointer to a[i] */
        for (i = 1; i < n; i += 2) {
            p1.set(pt.get());
            p1.offset += n2 + n2;
            pt.offset += 1;
        }
    }
}
