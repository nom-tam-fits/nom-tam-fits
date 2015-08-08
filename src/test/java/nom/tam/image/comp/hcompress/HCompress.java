package nom.tam.image.comp.hcompress;

import java.nio.ByteBuffer;

import nom.tam.util.ArrayFuncs;

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

public class HCompress {

    static class IntArP {

        long[] a;

        int offset;

        public IntArP copy() {
            IntArP intAP = new IntArP();
            intAP.a = a;
            intAP.offset = offset;
            return intAP;
        }

        public long get() {
            return a[offset];
        }

        public void set(long value) {
            a[offset] = value;
        }

        public long get(int i) {
            return a[offset + i];
        }

        public void set(int i, long value) {
            a[offset + i] = value;

        }

        public IntArP copy(int extraOffset) {
            IntArP intAP = new IntArP();
            intAP.a = a;
            intAP.offset = offset + extraOffset;
            return intAP;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (int index = offset; index < Math.min(code.length, offset + 10); index++) {
                String hextString = Long.toHexString(a[index]);
                hextString = "00000000000000000000000000000000".substring(0, 16 - hextString.length()) + hextString + " " + a[index];
                builder.append(hextString);
                builder.append('\n');
            }
            return builder.toString();
        }
    }

    /* ---------------------------------------------------------------------- */
    public void fits_hcompress(long[] aa, int ny, int nx, int scale, ByteBuffer output, long nbytes) {
        /*
         * compress the input image using the H-compress algorithm a - input
         * image array nx - size of X axis of image ny - size of Y axis of image
         * scale - quantization scale factor. Larger values results in more
         * (lossy) compression scale = 0 does lossless compression output -
         * pre-allocated array to hold the output compressed stream of bytes
         * nbyts - input value = size of the output buffer; returned value =
         * size of the compressed byte stream, in bytes NOTE: the nx and ny
         * dimensions as defined within this code are reversed from the usual
         * FITS notation. ny is the fastest varying dimension, which is usually
         * considered the X axis in the FITS image display
         */

        /* H-transform */
        htrans(aa, nx, ny);

        IntArP a = new IntArP();
        a.a = aa;
        a.offset = 0;

        /* digitize */
        digitize(a, 0, nx, ny, scale);

        /* encode and write to output array */

        nbytes = 0; /* reset */

        nbytes = encode(output, a, nx, ny, scale);

    }

    /*
     * Copyright (c) 1993 Association of Universities for Research in Astronomy.
     * All rights reserved. Produced under National Aeronautics and Space
     * Administration Contract No. NAS5-26555.
     */
    /*
     * htrans.c H-transform of NX x NY integer image Programmer: R. White Date:
     * 11 May 1992
     */

    /* ######################################################################### */
    int htrans(long a[], int nx, int ny) {
        int nmax, log2n, nxtop, nytop, i, j, k;
        int oddx, oddy;
        int s10, s00;
        long h0, hx, hy, hc, shift, mask, mask2, prnd, prnd2, nrnd2;
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
         * set up rounding and shifting masks
         */
        shift = 0;
        mask = -2;
        mask2 = mask << 1;
        prnd = 1;
        prnd2 = prnd << 1;
        nrnd2 = prnd2 - 1;
        /*
         * do log2n reductions We're indexing a as a 2-D array with dimensions
         * (nx,ny).
         */
        nxtop = nx;
        nytop = ny;

        for (k = 0; k < log2n; k++) {
            oddx = (nxtop % 2);
            oddy = (nytop % 2);
            for (i = 0; i < nxtop - oddx; i += 2) {
                s00 = i * ny; /* s00 is index of a[i,j] */
                s10 = s00 + ny; /* s10 is index of a[i+1,j] */
                for (j = 0; j < nytop - oddy; j += 2) {
                    /*
                     * Divide h0,hx,hy,hc by 2 (1 the first time through).
                     */
                    h0 = (a[s10 + 1] + a[s10] + a[s00 + 1] + a[s00]) >> shift;
                    hx = (a[s10 + 1] + a[s10] - a[s00 + 1] - a[s00]) >> shift;
                    hy = (a[s10 + 1] - a[s10] + a[s00 + 1] - a[s00]) >> shift;
                    hc = (a[s10 + 1] - a[s10] - a[s00 + 1] + a[s00]) >> shift;

                    /*
                     * Throw away the 2 bottom bits of h0, bottom bit of hx,hy.
                     * To get rounding to be same for positive and negative
                     * numbers, nrnd2 = prnd2 - 1.
                     */
                    a[s10 + 1] = hc;
                    a[s10] = ((hx >= 0) ? (hx + prnd) : hx) & mask;
                    a[s00 + 1] = ((hy >= 0) ? (hy + prnd) : hy) & mask;
                    a[s00] = ((h0 >= 0) ? (h0 + prnd2) : (h0 + nrnd2)) & mask2;
                    s00 += 2;
                    s10 += 2;
                }
                if (oddy != 0) {
                    /*
                     * do last element in row if row length is odd s00+1, s10+1
                     * are off edge
                     */
                    h0 = (a[s10] + a[s00]) << (1 - shift);
                    hx = (a[s10] - a[s00]) << (1 - shift);
                    a[s10] = ((hx >= 0) ? (hx + prnd) : hx) & mask;
                    a[s00] = ((h0 >= 0) ? (h0 + prnd2) : (h0 + nrnd2)) & mask2;
                    s00 += 1;
                    s10 += 1;
                }
            }
            if (oddx != 0) {
                /*
                 * do last row if column length is odd s10, s10+1 are off edge
                 */
                s00 = i * ny;
                for (j = 0; j < nytop - oddy; j += 2) {
                    h0 = (a[s00 + 1] + a[s00]) << (1 - shift);
                    hy = (a[s00 + 1] - a[s00]) << (1 - shift);
                    a[s00 + 1] = ((hy >= 0) ? (hy + prnd) : hy) & mask;
                    a[s00] = ((h0 >= 0) ? (h0 + prnd2) : (h0 + nrnd2)) & mask2;
                    s00 += 2;
                }
                if (oddy != 0) {
                    /*
                     * do corner element if both row and column lengths are odd
                     * s00+1, s10, s10+1 are off edge
                     */
                    h0 = a[s00] << (2 - shift);
                    a[s00] = ((h0 >= 0) ? (h0 + prnd2) : (h0 + nrnd2)) & mask2;
                }
            }
            /*
             * now shuffle in each dimension to group coefficients by order
             */
            // achtung eigenlich pointer nach a
            for (i = 0; i < nxtop; i++) {
                shuffle(a, ny * i, nytop, 1, tmp);
            }
            for (j = 0; j < nytop; j++) {
                shuffle(a, j, nxtop, ny, tmp);
            }
            /*
             * image size reduced by 2 (round up if odd)
             */
            nxtop = (nxtop + 1) >> 1;
            nytop = (nytop + 1) >> 1;
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
        return (0);
    }

    void shuffle(long[] a, int aOffet, int n, int n2, long tmp[]) {

        /*
         * int a[]; array to shuffle int n; number of elements to shuffle int
         * n2; second dimension int tmp[]; scratch storage
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
        p1Offset = aOffet + n2;
        for (i = 1; i < n; i += 2) {
            pt[ptOffset] = p1[p1Offset];
            ptOffset += 1;
            p1Offset += (n2 + n2);
        }
        /*
         * compress even elements into first half of A
         */
        p1 = a;
        p1Offset = aOffet + n2;
        p2 = a;
        p2Offset = aOffet + n2 + n2;
        for (i = 2; i < n; i += 2) {
            p1[p1Offset] = p2[p2Offset];
            p1Offset += n2;
            p2Offset += (n2 + n2);
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
    }/*
      * #########################################################################
      */

    /* ######################################################################### */
    /*
     * Copyright (c) 1993 Association of Universities for Research in Astronomy.
     * All rights reserved. Produced under National Aeronautics and Space
     * Administration Contract No. NAS5-26555.
     */
    /*
     * digitize.c digitize H-transform Programmer: R. White Date: 11 March 1991
     */

    /* ######################################################################### */
    void digitize(IntArP a, int aOffset, int nx, int ny, long scale) {
        long d;
        IntArP p;

        /*
         * round to multiple of scale
         */
        if (scale <= 1)
            return;
        d = (scale + 1) / 2 - 1;
        for (p = a.copy(); p.offset <= a.offset + (nx * ny - 1); p.offset++) {
            p.set(((p.offset > 0) ? (p.get() + d) : (p.get() - d)) / scale);
        }
    }

    static byte[] code_magic = {
        (byte) 0xDD,
        (byte) 0x99
    };

    /* ######################################################################### */
    int encode(ByteBuffer outfile, IntArP a, int nx, int ny, int scale) {

        /* FILE *outfile; - change outfile to a char array */
        /*
         * long * nlength returned length (in bytes) of the encoded array) int
         * a[]; input H-transform array (nx,ny) int nx,ny; size of H-transform
         * array int scale; scale factor for digitization
         */
        long nlength;
        int nel, nx2, ny2, i, j, k, q, nsign, bits_to_go;
        long[] vmax = new long[3];
        byte[] nbitplanes = new byte[3];
        byte[] signbits;
        int stat;

        long noutchar = 0; /*
                            * initialize the number of compressed bytes that
                            * have been written
                            */
        nel = nx * ny;
        /*
         * write magic value
         */
        outfile.put(code_magic);
        outfile.putInt(nx); /* size of image */
        outfile.putInt(ny);
        outfile.putInt(scale); /* scale factor for digitization */
        /*
         * write first value of A (sum of all pixels -- the only value which
         * does not compress well)
         */
        outfile.putLong(a.get());

        a.set(0);
        /*
         * allocate array for sign bits and save values, 8 per byte
         */
        signbits = new byte[(nel / 8) + 1];

        nsign = 0;
        bits_to_go = 8;
        signbits[0] = 0;
        for (i = 0; i < nel; i++) {
            if (a.get(i) > 0) {
                /*
                 * positive element, put zero at end of buffer
                 */
                signbits[nsign] <<= 1;
                bits_to_go -= 1;
            } else if (a.get(i) < 0) {
                /*
                 * negative element, shift in a one
                 */
                signbits[nsign] <<= 1;
                signbits[nsign] |= 1;
                bits_to_go -= 1;
                /*
                 * replace a by absolute value
                 */
                a.set(i, -a.get(i));
            }
            if (bits_to_go == 0) {
                /*
                 * filled up this byte, go to the next one
                 */
                bits_to_go = 8;
                nsign += 1;
                signbits[nsign] = 0;
            }
        }
        if (bits_to_go != 8) {
            /*
             * some bits in last element move bits in last byte to bottom and
             * increment nsign
             */
            signbits[nsign] <<= bits_to_go;
            nsign += 1;
        }
        /*
         * calculate number of bit planes for 3 quadrants quadrant 0=bottom
         * left, 1=bottom right or top left, 2=top right,
         */
        for (q = 0; q < 3; q++) {
            vmax[q] = 0;
        }
        /*
         * get maximum absolute value in each quadrant
         */
        nx2 = (nx + 1) / 2;
        ny2 = (ny + 1) / 2;
        j = 0; /* column counter */
        k = 0; /* row counter */
        for (i = 0; i < nel; i++) {
            q = (j >= ny2 ? 1 : 0) + (k >= nx2 ? 1 : 0);
            if (vmax[q] < a.get(i))
                vmax[q] = a.get(i);
            if (++j >= ny) {
                j = 0;
                k += 1;
            }
        }
        /*
         * now calculate number of bits for each quadrant
         */

        /* this is a more efficient way to do this, */

        for (q = 0; q < 3; q++) {
            for (nbitplanes[q] = 0; vmax[q] > 0; vmax[q] = vmax[q] >> 1, nbitplanes[q]++)
                ;
        }

        /*
         * write nbitplanes
         */
        outfile.put(nbitplanes, 0, nbitplanes.length);

        /*
         * write coded array
         */
        doencode(outfile, a, nx, ny, nbitplanes);
        /*
         * write sign bits
         */

        if (nsign > 0) {
            outfile.put(signbits, 0, nsign);
        }

        return (int) noutchar;

    }

    void doencode(ByteBuffer outfile, IntArP a, int nx, int ny, byte[] nbitplanes) {
        /*
         * char *outfile; output data stream int a[]; Array of values to encode
         * int nx,ny; Array dimensions [nx][ny] unsigned char nbitplanes[3];
         * Number of bit planes in quadrants
         */

        int nx2, ny2, stat;

        nx2 = (nx + 1) / 2;
        ny2 = (ny + 1) / 2;
        /*
         * Initialize bit output
         */
        start_outputing_bits();
        /*
         * write out the bit planes for each quadrant
         */
        qtree_encode(outfile, a.copy(), ny, nx2, ny2, nbitplanes[0]);

        qtree_encode(outfile, a.copy(ny2), ny, nx2, ny / 2, nbitplanes[1]);

        qtree_encode(outfile, a.copy(ny * nx2), ny, nx / 2, ny2, nbitplanes[1]);

        qtree_encode(outfile, a.copy(ny * nx2 + ny2), ny, nx / 2, ny / 2, nbitplanes[2]);
        /*
         * Add zero as an EOF symbol
         */
        output_nybble(outfile, 0);
        done_outputing_bits(outfile);

    }

    /* THE BIT BUFFER */

    static long bitcount;

    static int buffer2; /* Bits buffered for output */

    static int bits_to_go2; /* Number of bits free in buffer */

    void start_outputing_bits() {
        buffer2 = 0; /* Buffer is empty to start */
        bits_to_go2 = 8; /* with */
        bitcount = 0;
    }

    void done_outputing_bits(ByteBuffer outfile) {
        if (bits_to_go2 < 8) {
            /* putc(buffer2<<bits_to_go2,outfile); */

            outfile.put((byte) (buffer2 << bits_to_go2));

            /* count the garbage bits too */
            bitcount += bits_to_go2;
        }
    }

    /*
     * Huffman code values and number of bits in each code
     */
    static int[] code = {
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

    static int[] ncode = {
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

    /*
     * variables for bit output to buffer when Huffman coding
     */
    int bitbuffer;

    int bits_to_go3;

    /*
     * macros to write out 4-bit nybble, Huffman code for this value
     */

    /* ######################################################################### */
    int qtree_encode(ByteBuffer outfile, IntArP a, int n, int nqx, int nqy, int nbitplanes) {

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
        nqmax = (nqx > nqy) ? nqx : nqy;
        log2n = (int) (Math.log((float) nqmax) / Math.log(2.0) + 0.5);
        if (nqmax > (1 << log2n)) {
            log2n += 1;
        }
        /*
         * initialize buffer point, max buffer size
         */
        nqx2 = (nqx + 1) / 2;
        nqy2 = (nqy + 1) / 2;
        bmax = (nqx2 * nqy2 + 1) / 2;
        /*
         * We're indexing A as a 2-D array with dimensions (nqx,nqy). Scratch is
         * 2-D with dimensions (nqx/2,nqy/2) rounded up. Buffer is used to store
         * string of codes for output.
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
            bitbuffer = 0;
            bits_to_go3 = 0;
            /*
             * on first pass copy A to scratch array
             */
            qtree_onebit(a, n, nqx, nqy, scratch, bit);
            nx = (nqx + 1) >> 1;
            ny = (nqy + 1) >> 1;
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
                write_bdirect(outfile, a, n, nqx, nqy, scratch, bit);
                continue bitplane_done;
            }
            /*
             * do log2n reductions
             */
            for (k = 1; k < log2n; k++) {
                qtree_reduce(scratch, ny, nx, ny, scratch);
                nx = (nx + 1) >> 1;
                ny = (ny + 1) >> 1;
                b = bufcopy(scratch, nx * ny, buffer, b, bmax);
                if (b >= bmax) {
                    write_bdirect(outfile, a, n, nqx, nqy, scratch, bit);
                    continue bitplane_done;
                }
            }
            /*
             * OK, we've got the code in buffer Write quadtree warning code,
             * then write buffer in reverse order
             */
            output_nybble(outfile, 0xF);
            if (b == 0) {
                if (bits_to_go3 > 0) {
                    /*
                     * put out the last few bits
                     */
                    output_nbits(outfile, bitbuffer & ((1 << bits_to_go3) - 1), bits_to_go3);
                } else {
                    /*
                     * have to write a zero nybble if there are no 1's in array
                     */
                    output_nbits(outfile, code[0], ncode[0]);
                }
            } else {
                if (bits_to_go3 > 0) {
                    /*
                     * put out the last few bits
                     */
                    output_nbits(outfile, bitbuffer & ((1 << bits_to_go3) - 1), bits_to_go3);
                }
                for (i = b - 1; i >= 0; i--) {
                    output_nbits(outfile, buffer[i], 8);
                }
            }
        }
        return (0);
    }

    void qtree_onebit(IntArP a, int n, int nx, int ny, byte[] b, int bit) {
        int i, j, k;
        long b0, b1, b2, b3;
        int s10, s00;

        /*
         * use selected bit to get amount to shift
         */
        b0 = 1L << bit;
        b1 = b0 << 1;
        b2 = b0 << 2;
        b3 = b0 << 3;
        k = 0; /* k is index of b[i/2,j/2] */
        for (i = 0; i < nx - 1; i += 2) {
            s00 = n * i; /* s00 is index of a[i,j] */
            /*
             * tried using s00+n directly in the statements, but this had no
             * effect on performance
             */
            s10 = s00 + n; /* s10 is index of a[i+1,j] */
            for (j = 0; j < ny - 1; j += 2) {

                b[k] = (byte) (((a.get(s10 + 1) & b0) //
                        | ((a.get(s10) << 1) & b1) //
                        | ((a.get(s00 + 1) << 2) & b2) //
                | ((a.get(s00) << 3) & b3)) >> bit);

                k += 1;
                s00 += 2;
                s10 += 2;
            }
            if (j < ny) {
                /*
                 * row size is odd, do last element in row s00+1,s10+1 are off
                 * edge
                 */
                b[k] = (byte) ((((a.get(s10) << 1) & b1) | ((a.get(s00) << 3) & b3)) >> bit);
                k += 1;
            }
        }
        if (i < nx) {
            /*
             * column size is odd, do last row s10,s10+1 are off edge
             */
            s00 = n * i;
            for (j = 0; j < ny - 1; j += 2) {
                b[k] = (byte) ((((a.get(s00 + 1) << 2) & b2) | ((a.get(s00) << 3) & b3)) >> bit);
                k += 1;
                s00 += 2;
            }
            if (j < ny) {
                /*
                 * both row and column size are odd, do corner element s00+1,
                 * s10, s10+1 are off edge
                 */
                b[k] = (byte) ((((a.get(s00) << 3) & b3)) >> bit);
                k += 1;
            }
        }
    }

    int qtree_encode(ByteBuffer outfile, IntArP a, int aOffset, int n, int nqx, int nqy, int nbitplanes) {

        /*
         * int a[]; int n; physical dimension of row in a int nqx; length of row
         * int nqy; length of column (<=n) int nbitplanes; number of bit planes
         * to output
         */

        int log2n, i, k, bit, b, bmax, nqmax, nqx2, nqy2, nx, ny;
        byte[] scratch, buffer;

        /*
         * log2n is log2 of max(nqx,nqy) rounded up to next power of 2
         */
        nqmax = (nqx > nqy) ? nqx : nqy;
        log2n = (int) (Math.log((float) nqmax) / Math.log(2.0) + 0.5);
        if (nqmax > (1 << log2n)) {
            log2n += 1;
        }
        /*
         * initialize buffer point, max buffer size
         */
        nqx2 = (nqx + 1) / 2;
        nqy2 = (nqy + 1) / 2;
        bmax = (nqx2 * nqy2 + 1) / 2;
        /*
         * We're indexing A as a 2-D array with dimensions (nqx,nqy). Scratch is
         * 2-D with dimensions (nqx/2,nqy/2) rounded up. Buffer is used to store
         * string of codes for output.
         */
        scratch = new byte[2 * bmax];
        buffer = new byte[bmax];

        /*
         * now encode each bit plane, starting with the top
         */
        bitplane_done: for (bit = nbitplanes - 1; bit >= 0; bit--) {
            /*
             * initial bit buffer
             */
            b = 0;
            bitbuffer = 0;
            bits_to_go3 = 0;
            /*
             * on first pass copy A to scratch array
             */
            qtree_onebit(a, n, nqx, nqy, scratch, bit);
            nx = (nqx + 1) >> 1;
            ny = (nqy + 1) >> 1;
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
                write_bdirect(outfile, a, n, nqx, nqy, scratch, bit);
                continue bitplane_done;
            }
            /*
             * do log2n reductions
             */
            for (k = 1; k < log2n; k++) {
                qtree_reduce(scratch, ny, nx, ny, scratch);
                nx = (nx + 1) >> 1;
                ny = (ny + 1) >> 1;
                b = bufcopy(scratch, nx * ny, buffer, b, bmax);
                if (b >= bmax) {
                    write_bdirect(outfile, a, n, nqx, nqy, scratch, bit);
                    continue bitplane_done;
                }
            }
            /*
             * OK, we've got the code in buffer Write quadtree warning code,
             * then write buffer in reverse order
             */
            output_nybble(outfile, 0xF);
            if (b == 0) {
                if (bits_to_go3 > 0) {
                    /*
                     * put out the last few bits
                     */
                    output_nbits(outfile, bitbuffer & ((1 << bits_to_go3) - 1), bits_to_go3);
                } else {
                    /*
                     * have to write a zero nybble if there are no 1's in array
                     */
                    output_nbits(outfile, code[0], ncode[0]);
                }
            } else {
                if (bits_to_go3 > 0) {
                    /*
                     * put out the last few bits
                     */
                    output_nbits(outfile, bitbuffer & ((1 << bits_to_go3) - 1), bits_to_go3);
                }
                for (i = b - 1; i >= 0; i--) {
                    output_nbits(outfile, buffer[i], 8);
                }
            }
        }
        return (0);
    }

    void qtree_reduce(byte a[], int n, int nx, int ny, byte[] b) {
        int i, j, k;
        int s10, s00;

        k = 0; /* k is index of b[i/2,j/2] */
        for (i = 0; i < nx - 1; i += 2) {
            s00 = n * i; /* s00 is index of a[i,j] */
            s10 = s00 + n; /* s10 is index of a[i+1,j] */
            for (j = 0; j < ny - 1; j += 2) {
                b[k] = (byte) (b2i(a[s10 + 1] != 0) | (b2i(a[s10] != 0) << 1) | (b2i(a[s00 + 1] != 0) << 2) | (b2i(a[s00] != 0) << 3));
                k += 1;
                s00 += 2;
                s10 += 2;
            }
            if (j < ny) {
                /*
                 * row size is odd, do last element in row s00+1,s10+1 are off
                 * edge
                 */
                b[k] = (byte) ((b2i(a[s10] != 0) << 1) | (b2i(a[s00] != 0) << 3));
                k += 1;
            }
        }
        if (i < nx) {
            /*
             * column size is odd, do last row s10,s10+1 are off edge
             */
            s00 = n * i;
            for (j = 0; j < ny - 1; j += 2) {
                b[k] = (byte) ((b2i(a[s00 + 1] != 0) << 2) | (b2i(a[s00] != 0) << 3));
                k += 1;
                s00 += 2;
            }
            if (j < ny) {
                /*
                 * both row and column size are odd, do corner element s00+1,
                 * s10, s10+1 are off edge
                 */
                b[k] = (byte) (b2i(a[s00] != 0) << 3);
                k += 1;
            }
        }
    }

    private int b2i(boolean b) {
        return (b ? 1 : 0);
    }

    void output_nybble(ByteBuffer outfile, int bits) {
        /*
         * insert 4 bits at end of buffer
         */
        buffer2 = (buffer2 << 4) | (bits & 15);
        bits_to_go2 -= 4;
        if (bits_to_go2 <= 0) {
            /*
             * buffer2 full, put out top 8 bits
             */

            outfile.put((byte) ((buffer2 >> (-bits_to_go2)) & 0xff));

            bits_to_go2 += 8;
        }
        bitcount += 4;
    }

    int bufcopy(byte[] a, int n, byte[] buffer, int b, long bmax) {
        int i;

        for (i = 0; i < n; i++) {
            if (a[i] != 0) {
                /*
                 * add Huffman code for a[i] to buffer
                 */
                bitbuffer |= code[a[i]] << bits_to_go3;
                bits_to_go3 += ncode[a[i]];
                if (bits_to_go3 >= 8) {
                    buffer[b] = (byte) (bitbuffer & 0xFF);
                    b += 1;
                    /*
                     * return warning code if we fill buffer
                     */
                    if (b >= bmax)
                        return b;
                    bitbuffer >>= 8;
                    bits_to_go3 -= 8;
                }
            }
        }
        return b;
    }

    void write_bdirect(ByteBuffer outfile, IntArP a, int n, int nqx, int nqy, byte[] scratch, int bit) {

        /*
         * Write the direct bitmap warning code
         */
        output_nybble(outfile, 0x0);
        /*
         * Copy A to scratch array (again!), packing 4 bits/nybble
         */
        qtree_onebit(a, n, nqx, nqy, scratch, bit);
        /*
         * write to outfile
         */
        /*
         * int i; for (i = 0; i < ((nqx+1)/2) * ((nqy+1)/2); i++) {
         * output_nybble(outfile,scratch[i]); }
         */
        output_nnybble(outfile, ((nqx + 1) / 2) * ((nqy + 1) / 2), scratch);

    }

    void output_nnybble(ByteBuffer outfile, int n, byte[] array) {
        /*
         * pack the 4 lower bits in each element of the array into the outfile
         * array
         */

        int ii, jj, kk = 0, shift;

        if (n == 1) {
            output_nybble(outfile, (int) array[0]);
            return;
        }
        /*
         * forcing byte alignment doesn;t help, and even makes it go slightly
         * slower if (bits_to_go2 != 8) output_nbits(outfile, kk, bits_to_go2);
         */
        if (bits_to_go2 <= 4) {
            /* just room for 1 nybble; write it out separately */
            output_nybble(outfile, array[0]);
            kk++; /* index to next array element */

            if (n == 2) /* only 1 more nybble to write out */
            {
                output_nybble(outfile, (int) array[1]);
                return;
            }
        }

        /* bits_to_go2 is now in the range 5 - 8 */
        shift = 8 - bits_to_go2;

        /*
         * now write out pairs of nybbles; this does not affect value of
         * bits_to_go2
         */
        jj = (n - kk) / 2;

        if (bits_to_go2 == 8) {
            /* special case if nybbles are aligned on byte boundary */
            /* this actually seems to make very little differnece in speed */
            buffer2 = 0;
            for (ii = 0; ii < jj; ii++) {
                outfile.put((byte) (((array[kk] & 15) << 4) | (array[kk + 1] & 15)));
                kk += 2;
            }
        } else {
            for (ii = 0; ii < jj; ii++) {
                buffer2 = (buffer2 << 8) | ((array[kk] & 15) << 4) | (array[kk + 1] & 15);
                kk += 2;

                /*
                 * buffer2 full, put out top 8 bits
                 */

                outfile.put((byte) ((buffer2 >> shift) & 0xff));
            }
        }

        bitcount += (8 * (ii - 1));

        /* write out last odd nybble, if present */
        if (kk != n)
            output_nybble(outfile, (int) array[n - 1]);

        return;
    }

    void output_nbits(ByteBuffer outfile, int bits, int n) {
        /* AND mask for the right-most n bits */
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
        /*
         * insert bits at end of buffer
         */
        buffer2 <<= n;
        /* buffer2 |= ( bits & ((1<<n)-1) ); */
        buffer2 |= (bits & (mask[n]));
        bits_to_go2 -= n;
        if (bits_to_go2 <= 0) {
            /*
             * buffer2 full, put out top 8 bits
             */

            outfile.put((byte) ((buffer2 >> (-bits_to_go2)) & 0xff));

            bits_to_go2 += 8;
        }
        bitcount += n;
    }

    /**
     * debug routine
     * 
     * @param value
     * @return
     */
    private String binaryString(long value) {
        String binaryString = Long.toBinaryString(value);
        binaryString = "000000000000000000000000000000000000000000000000000000".subSequence(0, 64 - binaryString.length()) + binaryString;
        return binaryString;
    }

    public void fits_hcompress(int[] intArray, int ny, int nx, int scale, ByteBuffer compressed, int limit) {
        long[] longArray = new long[intArray.length];
        ArrayFuncs.copyInto(intArray, longArray);
        fits_hcompress(longArray, ny, nx, scale, compressed, limit);
    }
}
