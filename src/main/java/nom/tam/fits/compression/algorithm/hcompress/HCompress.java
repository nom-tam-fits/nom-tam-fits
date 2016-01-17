package nom.tam.fits.compression.algorithm.hcompress;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

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
 * The original compression code was written by Richard White at the STScI and
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
public class HCompress {

    private static final int HTRANS_START_MASK = -2;

    protected static final double ROUNDING_HALF = 0.5;

    protected static final int BITS_OF_1_BYTE = 8;

    protected static final int BITS_OF_1_NYBBLE = 4;

    protected static final int BYTE_MASK = 0xff;

    protected static final int NYBBLE_MASK = 0xF;

    /**
     * to be refactored to a good name.
     */
    private static final int N3 = 3;

    private static final int[] BITS_MASK = {
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

    /*
     * Huffman code values and number of bits in each code
     */
    private static final int[] CODE = {
        0x3e,
        0x00,
        0x01,
        0x08,
        0x02,
        0x09,
        0x1a,
        0x1b,
        0x03,
        0x1c,
        0x0a,
        0x1d,
        0x0b,
        0x1e,
        0x3f,
        0x0c
    };

    private static final byte[] CODE_MAGIC = {
        (byte) 0xDD,
        (byte) 0x99
    };

    private static final int[] NCODE = {
        6,
        3,
        3,
        4,
        3,
        4,
        5,
        5,
        3,
        5,
        4,
        5,
        4,
        5,
        6,
        4
    };

    /**
     * variables for bit output to buffer when Huffman coding
     */
    private int bitbuffer;

    /** Number of bits free in buffer */
    private int bitsToGo2;

    private int bitsToGo3;

    /** Bits buffered for output */
    private int buffer2;

    private int b2i(boolean b) {
        return b ? 1 : 0;
    }

    private int bufcopy(byte[] a, int n, byte[] buffer, int b, long bmax) {
        int i;

        for (i = 0; i < n; i++) {
            if (a[i] != 0) {
                /*
                 * add Huffman code for a[i] to buffer
                 */
                this.bitbuffer |= CODE[a[i]] << this.bitsToGo3;
                this.bitsToGo3 += NCODE[a[i]];
                if (this.bitsToGo3 >= BITS_OF_1_BYTE) {
                    buffer[b] = (byte) (this.bitbuffer & BYTE_MASK);
                    b += 1;
                    /*
                     * return warning code if we fill buffer
                     */
                    if (b >= bmax) {
                        return b;
                    }
                    this.bitbuffer >>= BITS_OF_1_BYTE;
                    this.bitsToGo3 -= BITS_OF_1_BYTE;
                }
            }
        }
        return b;
    }

    protected void compress(long[] aa, int ny, int nx, int scale, ByteBuffer output) {
        /*
         * compress the input image using the H-compress algorithm a - input
         * image tiledImageOperation nx - size of X axis of image ny - size of Y
         * axis of image scale - quantization scale factor. Larger values
         * results in more (lossy) compression scale = 0 does lossless
         * compression output - pre-allocated tiledImageOperation to hold the
         * output compressed stream of bytes nbyts - input value = size of the
         * output buffer; returned value = size of the compressed byte stream,
         * in bytes NOTE: the nx and ny dimensions as defined within this code
         * are reversed from the usual FITS notation. ny is the fastest varying
         * dimension, which is usually considered the X axis in the FITS image
         * display
         */

        /* H-transform */
        htrans(aa, nx, ny);

        LongBuffer a = LongBuffer.wrap(aa);

        /* digitize */
        digitize(a, 0, nx, ny, scale);

        /* encode and write to output tiledImageOperation */
        encode(output, a, nx, ny, scale);

    }

    private LongBuffer copy(LongBuffer a, int i) {
        a.position(i);
        return a.slice();
    }

    private void digitize(LongBuffer a, int aOffset, int nx, int ny, long scale) {
        /*
         * round to multiple of scale
         */
        if (scale <= 1) {
            return;
        }
        long d = (scale + 1L) / 2L - 1L;
        for (int index = 0; index < a.limit(); index++) {
            long current = a.get(index);
            a.put(index, (current > 0 ? current + d : current - d) / scale);
        }
    }

    /**
     * encode pixels.
     * 
     * @param compressedBytes
     *            compressed data
     * @param pixels
     *            pixels to compress
     * @param nx
     *            image width dimension
     * @param ny
     *            image height dimension
     * @param nbitplanes
     *            Number of bit planes in quadrants
     */
    private void doEncode(ByteBuffer compressedBytes, LongBuffer pixels, int nx, int ny, byte[] nbitplanes) {

        int nx2 = (nx + 1) / 2;
        int ny2 = (ny + 1) / 2;
        /*
         * Initialize bit output
         */
        startOutputtingBits();
        /*
         * write out the bit planes for each quadrant
         */
        qtreeEncode(compressedBytes, copy(pixels, 0), ny, nx2, ny2, nbitplanes[0]);

        qtreeEncode(compressedBytes, copy(pixels, ny2), ny, nx2, ny / 2, nbitplanes[1]);

        qtreeEncode(compressedBytes, copy(pixels, ny * nx2), ny, nx / 2, ny2, nbitplanes[1]);

        qtreeEncode(compressedBytes, copy(pixels, ny * nx2 + ny2), ny, nx / 2, ny / 2, nbitplanes[2]);
        /*
         * Add zero as an EOF symbol
         */
        outputNybble(compressedBytes, 0);
        doneOutputtingBits(compressedBytes);

    }

    private void doneOutputtingBits(ByteBuffer outfile) {
        if (this.bitsToGo2 < BITS_OF_1_BYTE) {
            /* putc(buffer2<<bits_to_go2,outfile); */

            outfile.put((byte) (this.buffer2 << this.bitsToGo2));
        }
    }

    private int encode(ByteBuffer compressedBytes, LongBuffer a, int nx, int ny, int scale) {
        long[] vmax = new long[N3];
        byte[] nbitplanes = new byte[N3];
        // initialize the number of compressed bytes that have been written
        long noutchar = 0;
        int nel = nx * ny;
        /*
         * write magic value
         */
        compressedBytes.put(CODE_MAGIC);
        compressedBytes.putInt(nx); /* size of image */
        compressedBytes.putInt(ny);
        compressedBytes.putInt(scale); /* scale factor for digitization */
        /*
         * write first value of A (sum of all pixels -- the only value which
         * does not compress well)
         */
        compressedBytes.putLong(a.get(0));

        a.put(0, 0);
        /*
         * allocate tiledImageOperation for sign bits and save values, 8 per
         * byte
         */
        byte[] signbits = new byte[(nel + BITS_OF_1_BYTE - 1) / BITS_OF_1_BYTE];

        int nsign = 0;
        int bitsToGo = BITS_OF_1_BYTE;
        signbits[0] = 0;
        for (int i = 0; i < nel; i++) {
            if (a.get(i) > 0) {
                /*
                 * positive element, put zero at end of buffer
                 */
                signbits[nsign] <<= 1;
                bitsToGo -= 1;
            } else if (a.get(i) < 0) {
                /*
                 * negative element, shift in a one
                 */
                signbits[nsign] <<= 1;
                signbits[nsign] |= 1;
                bitsToGo -= 1;
                /*
                 * replace a by absolute value
                 */
                a.put(i, -a.get(i));
            }
            if (bitsToGo == 0) {
                /*
                 * filled up this byte, go to the next one
                 */
                bitsToGo = BITS_OF_1_BYTE;
                nsign += 1;
                signbits[nsign] = 0;
            }
        }
        if (bitsToGo != BITS_OF_1_BYTE) {
            /*
             * some bits in last element move bits in last byte to bottom and
             * increment nsign
             */
            signbits[nsign] <<= bitsToGo;
            nsign += 1;
        }
        /*
         * calculate number of bit planes for 3 quadrants quadrant 0=bottom
         * left, 1=bottom right or top left, 2=top right,
         */
        for (int q = 0; q < N3; q++) {
            vmax[q] = 0;
        }
        /*
         * get maximum absolute value in each quadrant
         */
        int nx2 = (nx + 1) / 2;
        int ny2 = (ny + 1) / 2;
        int j = 0; /* column counter */
        int k = 0; /* row counter */
        for (int i = 0; i < nel; i++) {
            int q = (j >= ny2 ? 1 : 0) + (k >= nx2 ? 1 : 0);
            if (vmax[q] < a.get(i)) {
                vmax[q] = a.get(i);
            }
            if (++j >= ny) {
                j = 0;
                k += 1;
            }
        }
        /*
         * now calculate number of bits for each quadrant
         */

        /* this is a more efficient way to do this, */

        for (int q = 0; q < N3; q++) {
            nbitplanes[q] = 0;
            while (vmax[q] > 0) {
                vmax[q] = vmax[q] >> 1;
                nbitplanes[q]++;
            }
        }

        /*
         * write nbitplanes
         */
        compressedBytes.put(nbitplanes, 0, nbitplanes.length);

        /*
         * write coded tiledImageOperation
         */
        doEncode(compressedBytes, a, nx, ny, nbitplanes);
        /*
         * write sign bits
         */

        if (nsign > 0) {
            compressedBytes.put(signbits, 0, nsign);
        }
        return (int) noutchar;

    }

    private int htrans(long[] a, int nx, int ny) {
        /*
         * log2n is log2 of max(nx,ny) rounded up to next power of 2
         */
        int nmax = nx > ny ? nx : ny;
        int log2n = log2n(nmax);
        if (nmax > 1 << log2n) {
            log2n += 1;
        }
        /*
         * get temporary storage for shuffling elements
         */
        long[] tmp = new long[(nmax + 1) / 2];

        /*
         * set up rounding and shifting masks
         */
        long shift = 0;
        long mask = HTRANS_START_MASK;
        long mask2 = mask << 1;
        long prnd = 1;
        long prnd2 = prnd << 1;
        long nrnd2 = prnd2 - 1;
        /*
         * do log2n reductions We're indexing a as a 2-D tiledImageOperation
         * with dimensions (nx,ny).
         */
        int nxtop = nx;
        int nytop = ny;

        for (int k = 0; k < log2n; k++) {
            int oddx = nxtop % 2;
            int oddy = nytop % 2;
            int i = 0;
            for (; i < nxtop - oddx; i += 2) {
                int s00 = i * ny; /* s00 is index of a[i,j] */
                int s10 = s00 + ny; /* s10 is index of a[i+1,j] */
                for (int j = 0; j < nytop - oddy; j += 2) {
                    /*
                     * Divide h0,hx,hy,hc by 2 (1 the first time through).
                     */
                    long h0 = a[s10 + 1] + a[s10] + a[s00 + 1] + a[s00] >> shift;
                    long hx = a[s10 + 1] + a[s10] - a[s00 + 1] - a[s00] >> shift;
                    long hy = a[s10 + 1] - a[s10] + a[s00 + 1] - a[s00] >> shift;
                    long hc = a[s10 + 1] - a[s10] - a[s00 + 1] + a[s00] >> shift;

                    /*
                     * Throw away the 2 bottom bits of h0, bottom bit of hx,hy.
                     * To get rounding to be same for positive and negative
                     * numbers, nrnd2 = prnd2 - 1.
                     */
                    a[s10 + 1] = hc;
                    a[s10] = (hx >= 0 ? hx + prnd : hx) & mask;
                    a[s00 + 1] = (hy >= 0 ? hy + prnd : hy) & mask;
                    a[s00] = (h0 >= 0 ? h0 + prnd2 : h0 + nrnd2) & mask2;
                    s00 += 2;
                    s10 += 2;
                }
                if (oddy != 0) {
                    /*
                     * do last element in row if row length is odd s00+1, s10+1
                     * are off edge
                     */
                    long h0 = a[s10] + a[s00] << 1 - shift;
                    long hx = a[s10] - a[s00] << 1 - shift;
                    a[s10] = (hx >= 0 ? hx + prnd : hx) & mask;
                    a[s00] = (h0 >= 0 ? h0 + prnd2 : h0 + nrnd2) & mask2;
                    s00 += 1;
                    s10 += 1;
                }
            }
            if (oddx != 0) {
                /*
                 * do last row if column length is odd s10, s10+1 are off edge
                 */
                int s00 = i * ny;
                for (int j = 0; j < nytop - oddy; j += 2) {
                    long h0 = a[s00 + 1] + a[s00] << 1 - shift;
                    long hy = a[s00 + 1] - a[s00] << 1 - shift;
                    a[s00 + 1] = (hy >= 0 ? hy + prnd : hy) & mask;
                    a[s00] = (h0 >= 0 ? h0 + prnd2 : h0 + nrnd2) & mask2;
                    s00 += 2;
                }
                if (oddy != 0) {
                    /*
                     * do corner element if both row and column lengths are odd
                     * s00+1, s10, s10+1 are off edge
                     */
                    long h0 = a[s00] << 2 - shift;
                    a[s00] = (h0 >= 0 ? h0 + prnd2 : h0 + nrnd2) & mask2;
                }
            }
            /*
             * now shuffle in each dimension to group coefficients by order
             */
            // achtung eigenlich pointer nach a
            for (i = 0; i < nxtop; i++) {
                shuffle(a, ny * i, nytop, 1, tmp);
            }
            for (int j = 0; j < nytop; j++) {
                shuffle(a, j, nxtop, ny, tmp);
            }
            /*
             * image size reduced by 2 (round up if odd)
             */
            nxtop = nxtop + 1 >> 1;
            nytop = nytop + 1 >> 1;
            /*
             * divisor doubles after first reduction
             */
            shift = 1;
            /*
             * masks, rounding values double after each iteration
             */
            mask = mask2;
            prnd = prnd2;
            mask2 = mask2 << 1;
            prnd2 = prnd2 << 1;
            nrnd2 = prnd2 - 1;
        }
        return 0;
    }

    private int log2n(int nqmax) {
        return (int) (Math.log(nqmax) / Math.log(2.0) + ROUNDING_HALF);
    }

    private void outputNbits(ByteBuffer outfile, int bits, int n) {
        /* AND mask for the right-most n bits */

        /*
         * insert bits at end of buffer
         */
        this.buffer2 <<= n;
        /* buffer2 |= ( bits & ((1<<n)-1) ); */
        this.buffer2 |= bits & BITS_MASK[n];
        this.bitsToGo2 -= n;
        if (this.bitsToGo2 <= 0) {
            /*
             * buffer2 full, put out top 8 bits
             */

            outfile.put((byte) (this.buffer2 >> -this.bitsToGo2 & BYTE_MASK));

            this.bitsToGo2 += BITS_OF_1_BYTE;
        }
    }

    private void outputNnybble(ByteBuffer outfile, int n, byte[] array) {
        /*
         * pack the 4 lower bits in each element of the tiledImageOperation into
         * the outfile tiledImageOperation
         */

        int ii, jj, kk = 0, shift;

        if (n == 1) {
            outputNybble(outfile, array[0]);
            return;
        }
        /*
         * forcing byte alignment doesn;t help, and even makes it go slightly
         * slower if (bits_to_go2 != 8) output_nbits(outfile, kk, bits_to_go2);
         */
        if (this.bitsToGo2 <= BITS_OF_1_NYBBLE) {
            /* just room for 1 nybble; write it out separately */
            outputNybble(outfile, array[0]);
            kk++; /* index to next tiledImageOperation element */

            if (n == 2) {
                // only 1 more nybble to write out
                outputNybble(outfile, array[1]);
                return;
            }
        }

        /* bits_to_go2 is now in the range 5 - 8 */
        shift = BITS_OF_1_BYTE - this.bitsToGo2;

        /*
         * now write out pairs of nybbles; this does not affect value of
         * bits_to_go2
         */
        jj = (n - kk) / 2;

        if (this.bitsToGo2 == BITS_OF_1_BYTE) {
            /* special case if nybbles are aligned on byte boundary */
            /* this actually seems to make very little difference in speed */
            this.buffer2 = 0;
            for (ii = 0; ii < jj; ii++) {
                outfile.put((byte) ((array[kk] & NYBBLE_MASK) << BITS_OF_1_NYBBLE | array[kk + 1] & NYBBLE_MASK));
                kk += 2;
            }
        } else {
            for (ii = 0; ii < jj; ii++) {
                this.buffer2 = this.buffer2 << BITS_OF_1_BYTE | (array[kk] & NYBBLE_MASK) << BITS_OF_1_NYBBLE | array[kk + 1] & NYBBLE_MASK;
                kk += 2;

                /*
                 * buffer2 full, put out top 8 bits
                 */

                outfile.put((byte) (this.buffer2 >> shift & BYTE_MASK));
            }
        }

        /* write out last odd nybble, if present */
        if (kk != n) {
            outputNybble(outfile, array[n - 1]);
        }
    }

    private void outputNybble(ByteBuffer outfile, int bits) {
        /*
         * insert 4 bits at end of buffer
         */
        this.buffer2 = this.buffer2 << BITS_OF_1_NYBBLE | bits & NYBBLE_MASK;
        this.bitsToGo2 -= BITS_OF_1_NYBBLE;
        if (this.bitsToGo2 <= 0) {
            /*
             * buffer2 full, put out top 8 bits
             */

            outfile.put((byte) (this.buffer2 >> -this.bitsToGo2 & BYTE_MASK));

            this.bitsToGo2 += BITS_OF_1_BYTE;
        }
    }

    /**
     * macros to write out 4-bit nybble, Huffman code for this value
     */
    private int qtreeEncode(ByteBuffer outfile, LongBuffer a, int n, int nqx, int nqy, int nbitplanes) {

        /*
         * int a[]; int n; physical dimension of row in a int nqx; length of row
         * int nqy; length of column (<=n) int nbitplanes; number of bit planes
         * to output
         */

        int log2n, i, k, bit, b, nqmax, nqx2, nqy2, nx, ny;
        long bmax;
        byte[] scratch, buffer;

        /*
         * log2n is log2 of max(nqx,nqy) rounded up to next power of 2
         */
        nqmax = nqx > nqy ? nqx : nqy;
        log2n = log2n(nqmax);
        if (nqmax > 1 << log2n) {
            log2n += 1;
        }
        /*
         * initialize buffer point, max buffer size
         */
        nqx2 = (nqx + 1) / 2;
        nqy2 = (nqy + 1) / 2;
        bmax = (nqx2 * nqy2 + 1) / 2;
        /*
         * We're indexing A as a 2-D tiledImageOperation with dimensions
         * (nqx,nqy). Scratch is 2-D with dimensions (nqx/2,nqy/2) rounded up.
         * Buffer is used to store string of codes for output.
         */
        scratch = new byte[(int) (2 * bmax)];
        buffer = new byte[(int) bmax];

        /*
         * now encode each bit plane, starting with the top
         */
        bitplane_done: for (bit = nbitplanes - 1; bit >= 0; bit--) {
            /*
             * initial bit buffer
             */
            b = 0;
            this.bitbuffer = 0;
            this.bitsToGo3 = 0;
            /*
             * on first pass copy A to scratch tiledImageOperation
             */
            qtreeOnebit(a, n, nqx, nqy, scratch, bit);
            nx = nqx + 1 >> 1;
            ny = nqy + 1 >> 1;
            /*
             * copy non-zero values to output buffer, which will be written in
             * reverse order
             */
            b = bufcopy(scratch, nx * ny, buffer, b, bmax);
            if (b >= bmax) {
                /*
                 * quadtree is expanding data, change warning code and just fill
                 * buffer with bit-map
                 */
                writeBdirect(outfile, a, n, nqx, nqy, scratch, bit);
                continue bitplane_done;
            }
            /*
             * do log2n reductions
             */
            for (k = 1; k < log2n; k++) {
                qtreeReduce(scratch, ny, nx, ny, scratch);
                nx = nx + 1 >> 1;
                ny = ny + 1 >> 1;
                b = bufcopy(scratch, nx * ny, buffer, b, bmax);
                if (b >= bmax) {
                    writeBdirect(outfile, a, n, nqx, nqy, scratch, bit);
                    continue bitplane_done;
                }
            }
            /*
             * OK, we've got the code in buffer Write quadtree warning code,
             * then write buffer in reverse order
             */
            outputNybble(outfile, NYBBLE_MASK);
            if (b == 0) {
                if (this.bitsToGo3 > 0) {
                    /*
                     * put out the last few bits
                     */
                    outputNbits(outfile, this.bitbuffer & (1 << this.bitsToGo3) - 1, this.bitsToGo3);
                } else {
                    /*
                     * have to write a zero nybble if there are no 1's in
                     * tiledImageOperation
                     */
                    outputNbits(outfile, CODE[0], NCODE[0]);
                }
            } else {
                if (this.bitsToGo3 > 0) {
                    /*
                     * put out the last few bits
                     */
                    outputNbits(outfile, this.bitbuffer & (1 << this.bitsToGo3) - 1, this.bitsToGo3);
                }
                for (i = b - 1; i >= 0; i--) {
                    outputNbits(outfile, buffer[i], BITS_OF_1_BYTE);
                }
            }
        }
        return 0;
    }

    private void qtreeOnebit(LongBuffer a, int n, int nx, int ny, byte[] b, int bit) {
        int i, j, k;
        long b0, b1, b2, b3;
        int s10, s00;

        /*
         * use selected bit to get amount to shift
         */
        b0 = 1L << bit;
        b1 = b0 << 1;
        b2 = b1 << 1;
        b3 = b2 << 1;
        k = 0; /* k is index of b[i/2,j/2] */
        for (i = 0; i < nx - 1; i += 2) {
            s00 = n * i; /* s00 is index of a[i,j] */
            /*
             * tried using s00+n directly in the statements, but this had no
             * effect on performance
             */
            s10 = s00 + n; /* s10 is index of a[i+1,j] */
            for (j = 0; j < ny - 1; j += 2) {

                b[k] = (byte) ((a.get(s10 + 1) & b0 //
                        | a.get(s10) << 1 & b1 //
                        | a.get(s00 + 1) << 2 & b2 //
                | a.get(s00) << N3 & b3) >> bit);

                k += 1;
                s00 += 2;
                s10 += 2;
            }
            if (j < ny) {
                /*
                 * row size is odd, do last element in row s00+1,s10+1 are off
                 * edge
                 */
                b[k] = (byte) ((a.get(s10) << 1 & b1 | a.get(s00) << N3 & b3) >> bit);
                k += 1;
            }
        }
        if (i < nx) {
            /*
             * column size is odd, do last row s10,s10+1 are off edge
             */
            s00 = n * i;
            for (j = 0; j < ny - 1; j += 2) {
                b[k] = (byte) ((a.get(s00 + 1) << 2 & b2 | a.get(s00) << N3 & b3) >> bit);
                k += 1;
                s00 += 2;
            }
            if (j < ny) {
                /*
                 * both row and column size are odd, do corner element s00+1,
                 * s10, s10+1 are off edge
                 */
                b[k] = (byte) ((a.get(s00) << N3 & b3) >> bit);
                k += 1;
            }
        }
    }

    private void qtreeReduce(byte[] a, int n, int nx, int ny, byte[] b) {
        int i, j, k;
        int s10, s00;

        k = 0; /* k is index of b[i/2,j/2] */
        for (i = 0; i < nx - 1; i += 2) {
            s00 = n * i; /* s00 is index of a[i,j] */
            s10 = s00 + n; /* s10 is index of a[i+1,j] */
            for (j = 0; j < ny - 1; j += 2) {
                b[k] = (byte) (b2i(a[s10 + 1] != 0) | b2i(a[s10] != 0) << 1 | b2i(a[s00 + 1] != 0) << 2 | b2i(a[s00] != 0) << N3);
                k += 1;
                s00 += 2;
                s10 += 2;
            }
            if (j < ny) {
                /*
                 * row size is odd, do last element in row s00+1,s10+1 are off
                 * edge
                 */
                b[k] = (byte) (b2i(a[s10] != 0) << 1 | b2i(a[s00] != 0) << N3);
                k += 1;
            }
        }
        if (i < nx) {
            /*
             * column size is odd, do last row s10,s10+1 are off edge
             */
            s00 = n * i;
            for (j = 0; j < ny - 1; j += 2) {
                b[k] = (byte) (b2i(a[s00 + 1] != 0) << 2 | b2i(a[s00] != 0) << N3);
                k += 1;
                s00 += 2;
            }
            if (j < ny) {
                /*
                 * both row and column size are odd, do corner element s00+1,
                 * s10, s10+1 are off edge
                 */
                b[k] = (byte) (b2i(a[s00] != 0) << N3);
                k += 1;
            }
        }
    }

    private void shuffle(long[] a, int aOffset, int n, int n2, long[] tmp) {

        /*
         * int a[]; tiledImageOperation to shuffle int n; number of elements to
         * shuffle int n2; second dimension int tmp[]; scratch storage
         */

        int i;
        long[] p1, p2, pt;
        int p1Offset = 0;
        int ptOffset = 0;
        int p2Offset = 0;
        /*
         * copy odd elements to tmp
         */
        pt = tmp;
        ptOffset = 0;
        p1 = a;
        p1Offset = aOffset + n2;
        for (i = 1; i < n; i += 2) {
            pt[ptOffset] = p1[p1Offset];
            ptOffset += 1;
            p1Offset += n2 + n2;
        }
        /*
         * compress even elements into first half of A
         */
        p1 = a;
        p1Offset = aOffset + n2;
        p2 = a;
        p2Offset = aOffset + n2 + n2;
        for (i = 2; i < n; i += 2) {
            p1[p1Offset] = p2[p2Offset];
            p1Offset += n2;
            p2Offset += n2 + n2;
        }
        /*
         * put odd elements into 2nd half
         */
        pt = tmp;
        ptOffset = 0;
        for (i = 1; i < n; i += 2) {
            p1[p1Offset] = pt[ptOffset];
            p1Offset += n2;
            ptOffset += 1;
        }
    }

    private void startOutputtingBits() {
        this.buffer2 = 0; /* Buffer is empty to start */
        this.bitsToGo2 = BITS_OF_1_BYTE; /* with */
    }

    private void writeBdirect(ByteBuffer outfile, LongBuffer a, int n, int nqx, int nqy, byte[] scratch, int bit) {

        /*
         * Write the direct bitmap warning code
         */
        outputNybble(outfile, 0x0);
        /*
         * Copy A to scratch tiledImageOperation (again!), packing 4 bits/nybble
         */
        qtreeOnebit(a, n, nqx, nqy, scratch, bit);
        /*
         * write to outfile
         */
        /*
         * int i; for (i = 0; i < ((nqx+1)/2) * ((nqy+1)/2); i++) {
         * output_nybble(outfile,scratch[i]); }
         */
        outputNnybble(outfile, (nqx + 1) / 2 * ((nqy + 1) / 2), scratch);

    }

}
