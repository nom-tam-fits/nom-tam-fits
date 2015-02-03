package nom.tam.image.comp;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;

import nom.tam.fits.Header;
import nom.tam.util.BufferedDataOutputStream;

/**
 * This class implements the H compression and decompression algorithm. This
 * compression scheme works as follows: Suppose we have an input array a[n,m]
 * <ul>
 * <li>** Write a magic number, followed by the dimensions of the 2-d image and
 * the scale value.
 * <li>Create a stream of the sign bits of each non-zero element in the array.
 * (no sign bits are saved for zeroes). Save this array. Replace each negative
 * value in the in the array with it's absolute value.
 * <li>Calculate the maximum number of bits needed in each quadrant of the
 * image. For reasons that are unclear to me two of the quadrants are combined
 * <li>** Write out the three values for the number of bits in the quadrants.
 * <li>Starting with a stride of one replace the four values a[n,m],
 * a[n+stride,m], a[n,m+stride], a[n+stride,m+stride] with the sum of the four
 * numbers and three differences (with two positive and negative elements) over
 * the array. I.e., first do a[00],a[01],a[10],a[11], then process
 * a[02],a[03],a[12],a[13], then... until the entire array has been used. Then
 * double the stride so that we we are just processing the elements where we put
 * in the sum in the first iteration, a[00], a[02], a[20], a[22], and keep going
 * until we have processed the entire image up to the maximum power of two less
 * than the dimension of the image. Eventually a[00] will have the sum of all
 * pixels in the image.
 * <li>Rearrange the pixels in the image, so that the pixels that are were sums
 * the longest in the process come first.
 * <li>Apply any digitization of the image. This substantially enhances
 * compression but makes the compression lossy.
 * <li>For each bit-plane in the image extract the bits in sets of four, so that
 * we get a nybble. Apply a Huffman compression to these nybbles. For reasons
 * that are unclear this is step done separately for each quadrant.
 * <li>** Write out the Huffman output.
 * <li>** Write out a 0 nybble to indicate the end of the Huffman output.
 * <li>** Write out the sign array.
 * </ul>
 * The original hcompress code written by Rick White (USRA/STScI) and this code
 * is based upon that but has been extensively modified in the transition from
 * C. The original code includes the following copyright. ** Copyright (c) 1993
 * Association of Universities for Research ** ** in Astronomy. All rights
 * reserved. Produced under National ** ** Aeronautics and Space Administration
 * Contract No. NAS5-26555. ** The bit twiddling functions found in the original
 * HCompress library are largely handled within the In/OutputBitStream methods
 * which provide more general (and presumably a bit less efficient) support for
 * consuming or emitting a bit stream.
 */
public class HCompress implements CompressionScheme {

    public String name() {
        return "hcomp_0";
    }

    /**
     * Dimensions of the image NOTE: the nx and ny dimensions as defined within
     * this code are reversed from the usual FITS notation. ny is the fastest
     * varying dimension, which is usually considered the X axis in the FITS
     * image display
     */
    private int nx;

    private int ny;

    /**
     * Input scale value. Not used. Should be integer but sometimes specified as
     * real by FITSIO.
     */
    private double inScale;

    /**
     * Scaling to be used for the image
     */
    private int scale;

    /**
     * Temporary work array. Use of this means that only a single thread should
     * use a given instance of an HCompress object.
     */
    private int[] tmp;

    /**
     * Magic number (only low byte in each used)
     */
    static final int[] code_magic = {
        0xDD,
        0x99
    };

    /*
     * Huffman code values and number of bits in each code
     */
    private static final int[] code = {
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

    private static final int[] ncode = {
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

    /** Natural log of 2 */
    private static final double log2 = Math.log(2.);

    private boolean initialized = false;

    public int getHeight() {
        return nx;
    }

    public int getWidth() {
        return ny;
    }

    public void initialize(Map<String, String> params) {
        try {
            System.err.println("NX");
            nx = Integer.parseInt(params.get("nx"));
            System.err.println("Ny");
            ny = Integer.parseInt(params.get("ny"));
            // The scale parameter is supposed to be integer, but FITSIO writes
            // the wrong value here and it can be float. This value is not used.
            inScale = Double.parseDouble(params.get("scale"));

        } catch (Exception e) {
            throw new IllegalArgumentException("Required HCompress parameters not found:" + e, e);

        }
        initialized = true;
    }

    public void updateForWrite(Header hdr, Map<String, String> parameters) {
    }

    public void getParameters(Map<String, String> params, Header hdr) {

        if (!params.containsKey("nx")) {
            if (hdr.containsKey("ZNAXIS1")) {
                params.put("nx", hdr.getIntValue("ZNAXIS1") + "");
            }
        }
        if (!params.containsKey("ny")) {
            if (hdr.containsKey("ZNAXIS2")) {
                params.put("ny", hdr.getIntValue("ZNAXIS2") + "");
            }
        }
        System.err.println("After get:" + params.keySet());
    }

    /**
     * compress the input image using the H-compress algorithm input - input
     * image array as a byte array.
     */
    public byte[] compress(byte[] input) throws IOException {

        if (!initialized) {
            throw new IllegalStateException("Compression before initialization");
        }
        try {
            int[] a = new int[nx * ny];
            DataInputStream is = new DataInputStream(new ByteArrayInputStream(input));
            for (int i = 0; i < a.length; i += 1) {
                a[i] = is.readInt();
            }
            return compress(a);
        } catch (Exception e) {
            throw new IOException("Unable to read input byte buffer", e);
        }
    }

    private void printSample(String label, int[] array) {
        System.out.println(label + ":");
        for (int i = 0; i < 10; i += 1) {
            System.out.println("i,a[i]:" + i + " " + array[i]);
        }
        for (int i = array.length - 10; i < array.length; i += 1) {
            System.out.println("i,a[i]:" + " " + i + " " + array[i]);
        }
        int sum = 0;
        for (int i = 0; i < array.length; i += 1) {
            sum += i * array[i];
        }
        System.out.println("Sum is:" + sum);
    }

    /**
     * compress the input image using the H-compress algorithm input - input
     * image array as an int array.
     */
    public byte[] compress(int[] input) throws IOException {
        htrans(input);
        digitize(input);
        byte[] data = encode(input);
        return data;
    }

    private void htrans(int[] a) {

        /*
         * log2n is log2 of max(nx,ny) rounded up to next power of 2
         */
        int nmax = (nx > ny) ? nx : ny;
        int log2n = (int) (Math.log((float) nmax) / log2 + 0.5);
        if (nmax > (1 << log2n)) {
            log2n += 1;
        }

        /*
         * get temporary storage for shuffling elements
         */
        tmp = new int[(nmax + 1) / 2];

        /*
         * set up rounding and shifting masks
         */
        int shift = 0;

        int mask = -2;
        int mask2 = mask << 1;

        int prnd = 1;

        int prnd2 = prnd << 1;
        int nrnd2 = prnd2 - 1;

        /*
         * do log2n reductions We're indexing a as a 2-D array with dimensions
         * (nx,ny).
         */
        int nxtop = nx;
        int nytop = ny;

        for (int k = 0; k < log2n; k++) {
            int oddx = nxtop % 2;
            int oddy = nytop % 2;
            int i;
            for (i = 0; i < nxtop - oddx; i += 2) {
                int s00 = i * ny; /* s00 is index of a[i,j] */
                int s10 = s00 + ny; /* s10 is index of a[i+1,j] */
                for (int j = 0; j < nytop - oddy; j += 2) {
                    /*
                     * Divide h0,hx,hy,hc by 2 (1 the first time through).
                     */
                    int h0 = (a[s10 + 1] + a[s10] + a[s00 + 1] + a[s00]) >> shift;
                    int hx = (a[s10 + 1] + a[s10] - a[s00 + 1] - a[s00]) >> shift;
                    int hy = (a[s10 + 1] - a[s10] + a[s00 + 1] - a[s00]) >> shift;
                    int hc = (a[s10 + 1] - a[s10] - a[s00 + 1] + a[s00]) >> shift;

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
                    int h0 = (a[s10] + a[s00]) << (1 - shift);
                    int hx = (a[s10] - a[s00]) << (1 - shift);

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
                int s00 = i * ny;
                for (int j = 0; j < nytop - oddy; j += 2) {

                    int h0 = (a[s00 + 1] + a[s00]) << (1 - shift);
                    int hy = (a[s00 + 1] - a[s00]) << (1 - shift);

                    a[s00 + 1] = ((hy >= 0) ? (hy + prnd) : hy) & mask;
                    a[s00] = ((h0 >= 0) ? (h0 + prnd2) : (h0 + nrnd2)) & mask2;
                    s00 += 2;
                }
                if (oddy != 0) {
                    /*
                     * do corner element if both row and column lengths are odd
                     * s00+1, s10, s10+1 are off edge
                     */
                    int h0 = a[s00] << (2 - shift);
                    a[s00] = ((h0 >= 0) ? (h0 + prnd2) : (h0 + nrnd2)) & mask2;
                }
            }
            /*
             * now shuffle in each dimension to group coefficients by order
             */
            for (i = 0; i < nxtop; i++) {
                shuffle(a, ny * i, nytop, 1);
            }
            for (int j = 0; j < nytop; j++) {
                shuffle(a, j, nxtop, ny);
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
    }

    void shuffle(int[] a, int offset, int n, int n2) {

        /*
         * copy odd elements to tmp
         */
        int pt = 0;
        int p1 = offset + n2;
        int stride = 2 * n2;

        for (int i = 1; i < n; i += 2) {
            tmp[pt] = a[p1];
            pt += 1;
            p1 += stride;
        }

        /*
         * compress even elements into first half of A
         */
        p1 = offset + n2;
        int p2 = offset + n2 + n2;
        for (int i = 2; i < n; i += 2) {
            a[p1] = a[p2];
            p1 += n2;
            p2 += (n2 + n2);
        }
        /*
         * put odd elements into 2nd half
         */
        pt = 0;
        for (int i = 1; i < n; i += 2) {
            a[p1] = tmp[pt];
            p1 += n2;
            pt += 1;
        }
    }

    private void digitize(int a[]) {
        /*
         * round to multiple of scale
         */
        if (scale <= 1) {
            return;
        }
        int d = (scale + 1) / 2 - 1;

        for (int p = 0; p < a.length; p += 1) {
            a[p] = ((a[p] > 0) ? (a[p] + d) : (a[p] - d)) / scale;
        }
    }

    private int getSignBits(int a[], byte[] signbits, int nel) {
        int nsign = 0;
        int bits_to_go = 8;

        signbits[0] = 0;

        for (int i = 0; i < nel; i++) {
            if (a[i] > 0) {
                signbits[nsign] <<= 1;
                bits_to_go -= 1;

                // If negative shift in 1 and replace original with abs.
            } else if (a[i] < 0) {
                signbits[nsign] <<= 1;
                bits_to_go -= 1;
                signbits[nsign] |= 1;
                a[i] = -a[i];
            }
            if (bits_to_go == 0) {
                bits_to_go = 8;
                nsign += 1;
            }
        }

        // Shift last byte if we don't end on even byte boundary
        if (bits_to_go != 8) {
            signbits[nsign] <<= bits_to_go;
            nsign += 1;
        }
        return nsign;
    }

    private byte[] getBitPlaneCounts(int[] a, int nel) {

        int[] vmax = new int[3];
        byte[] bitPlanes = new byte[3];

        /*
         * get maximum absolute value in each quadrant
         */
        int nx2 = (nx + 1) / 2;
        int ny2 = (ny + 1) / 2;
        int j = 0; /* column counter */
        int k = 0; /* row counter */
        for (int i = 0; i < nel; i++) {
            int q = 0;
            if (j >= ny2) {
                q += 1;
            }
            if (k >= nx2) {
                q += 1;
            }
            if (vmax[q] < a[i]) {
                vmax[q] = a[i];
            }
            if (++j >= ny) {
                j = 0;
                k += 1;
            }
        }

        /*
         * now calculate number of bits for each quadrant
         */
        for (int q = 0; q < 3; q++) {
            bitPlanes[q] = 0;
            while (vmax[q] > 0) {
                bitPlanes[q] += 1;
                vmax[q] >>= 1;
            }
        }
        return bitPlanes;
    }

    private byte[] encode(int a[]) throws IOException {

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        OutputBitStream os = new OutputBitStream(bytes);

        int noutchar = 0;
        /* initialize the number of compressed bytes that have been written */
        int nel = nx * ny;
        /*
         * write magic value
         */
        for (int i = 0; i < code_magic.length; i += 1) {
            os.writeBits(code_magic[i], 8);
        }
        os.writeBits(nx, 32);
        os.writeBits(ny, 32);
        os.writeBits(scale, 32);
        os.writeBits(0, 32); // high order 32 bits of long/long value
        os.writeBits(a[0], 32);

        a[0] = 0;
        /*
         * allocate array for sign bits and save values, 8 per byte
         */
        byte[] signbits = new byte[(nel + 7) / 8];
        int nsign = getSignBits(a, signbits, nel);

        /*
         * calculate number of bit planes for 3 quadrants quadrant 0=bottom
         * left, 1=bottom right or top left, 2=top right,
         */
        byte[] bitPlanes = getBitPlaneCounts(a, nel);

        /*
         * write nbitplanes
         */
        for (int i = 0; i < bitPlanes.length; i += 1) {
            os.writeBits(bitPlanes[i], 8);
        }

        /*
         * write coded array
         */
        doencode(a, os, bitPlanes);

        if (nsign > 0) {
            for (int i = 0; i < nsign; i += 1) {
                os.writeBits(signbits[i], 8);
            }
        }

        os.close();
        return bytes.toByteArray();

    }

    /**
     * Encode 2-D array onto bit stream. This version assumes that A is
     * positive.
     */

    /* ######################################################################### */
    void doencode(int a[], OutputBitStream os, byte[] nbitplanes) throws IOException {

        int nx2 = (nx + 1) / 2;
        int ny2 = (ny + 1) / 2;
        os.flush();
        qtreeEncode(a, 0, ny, nx2, ny2, nbitplanes[0], os);
        qtreeEncode(a, ny2, ny, nx2, ny / 2, nbitplanes[1], os);
        qtreeEncode(a, ny * nx2, ny, nx / 2, ny2, nbitplanes[1], os);
        qtreeEncode(a, ny * nx2 + ny2, ny, nx / 2, ny / 2, nbitplanes[2], os);
        /*
         * Add zero as an EOF symbol
         */
        os.writeBits(0, 4);
        os.flush(); // Write partial bytes
    }

    void qtreeEncode(int a[], int offset, int n, int nqx, int nqy, int nbitplanes, OutputBitStream os) throws IOException {

        /*
         * log2n is log2 of max(nqx,nqy) rounded up to next power of 2
         */
        int nqmax = (nqx > nqy) ? nqx : nqy;
        int log2n = (int) (Math.log((float) nqmax) / log2 + 0.5);
        if (nqmax > (1 << log2n)) {
            log2n += 1;
        }

        /*
         * initialize buffer point, max buffer size
         */
        int nqx2 = (nqx + 1) / 2;
        int nqy2 = (nqy + 1) / 2;
        int bmax = (nqx2 * nqy2 + 1) / 2;

        /*
         * We're indexing A as a 2-D array with dimensions (nqx,nqy). Scratch is
         * 2-D with dimensions (nqx/2,nqy/2) rounded up. Buffer is used to store
         * string of codes for output.
         */
        byte[] scratch = new byte[2 * bmax];
        // Note that this is a little endian stream, which we are going
        // to invert.

        // byte[] buffer = new byte[bmax];

        /*
         * now encode each bit plane, starting with the top
         */

        bit_loop: for (int bit = nbitplanes - 1; bit >= 0; bit--) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(bmax);
            OutputBitStream bo = new OutputBitStream(baos, true);

            /*
             * on first pass copy A to scratch array
             */
            qtreeOnebit(a, offset, n, nqx, nqy, scratch, bit);
            int tnx = (nqx + 1) >> 1;
            int tny = (nqy + 1) >> 1;
            /*
             * copy non-zero values to output buffer, which will be written in
             * reverse order
             */
            if (huffEncode(scratch, tnx * tny, bo, bmax)) {
                /*
                 * quadtree is expanding data, change warning code and just fill
                 * buffer with bit-map
                 */
                writeDirect(a, offset, n, nqx, nqy, scratch, bit, os);

            } else {
                /*
                 * do log2n reductions
                 */
                for (int k = 1; k < log2n; k++) {

                    qtreeReduce(scratch, tny, tnx, tny, scratch);
                    tnx = (tnx + 1) >> 1;
                    tny = (tny + 1) >> 1;

                    // If we ever run off the buffer we just
                    // do a direct write.
                    if (huffEncode(scratch, tnx * tny, bo, bmax)) {
                        writeDirect(a, offset, n, nqx, nqy, scratch, bit, os);
                        continue bit_loop;
                    }
                }

                /*
                 * OK, we've got the code in buffer Write quadtree warning code,
                 * then write buffer in reverse order
                 */
                os.writeBits(0xF, 4);
                if (bo.size() == 0) {
                    /*
                     * have to write a zero nybble if there are no 1's in array
                     */
                    os.writeBits(code[0], ncode[0]);

                } else {
                    bo.close();
                    int lastBits = bo.lastBitsUsed();
                    byte[] buffer = baos.toByteArray();
                    // Since the last byte may not have been filled up we
                    // check before writing it.
                    os.writeBits(buffer[buffer.length - 1], lastBits);
                    for (int i = buffer.length - 2; i >= 0; i -= 1) {
                        os.writeBits(buffer[i], 8);
                    }
                }
            }
        }
    }

    /* ######################################################################### */
    /*
     * This routine does the Huffman encoding of the output bytes. Copy non-zero
     * codes from array to buffer
     * @return true if the encoded string exceeds a maximum length.
     */

    private boolean huffEncode(byte[] a, int n, OutputBitStream buffer, int bmax) throws IOException {

        for (int i = 0; i < n; i += 1) {
            if (a[i] != 0) {
                /*
                 * add Huffman code for a[i] to buffer
                 */
                buffer.writeBits(code[a[i]], ncode[a[i]]);
                if (buffer.size() > bmax) {
                    return true;
                }
            }
        }
        return false;
    }

    /* ######################################################################### */
    /*
     * Do first quadtree reduction step on bit BIT of array A. Results put into
     * B.
     */
    void qtreeOnebit(int[] a, int offset, int n, int nx, int ny, byte[] b, int bit) {

        /*
         * use selected bit to get amount to shift
         */
        int b0 = 1 << bit;
        int b1 = b0 << 1;
        int b2 = b0 << 2;
        int b3 = b0 << 3;
        int k = 0; /* k is index of b[i/2,j/2] */

        int i;
        for (i = 0; i < nx - 1; i += 2) {

            int s00 = n * i; /* s00 is index of a[i,j] */
            int s10 = s00 + n; /* s10 is index of a[i+1,j] */

            int j;
            for (j = 0; j < ny - 1; j += 2) {

                // Extract a nybble containing the same bit
                // for each of the four corner pixels. Remember
                // that we are big-endian, so the s00 bit is
                // the highest order bit.
                b[k] = (byte) (((a[offset + s10 + 1] & b0) | ((a[offset + s10] << 1) & b1) | ((a[offset + s00 + 1] << 2) & b2) | ((a[offset + s00] << 3) & b3)) >> bit);

                k += 1;
                s00 += 2;
                s10 += 2;
            }
            if (j < ny) {
                /*
                 * row size is odd, do last element in row s00+1,s10+1 are off
                 * edge
                 */
                b[k] = (byte) (((a[s10] << 1 & b1) | (a[s00] << 3 & b3)) >> bit);
                k += 1;
            }
        }
        if (i < nx) {
            /*
             * column size is odd, do last row s10,s10+1 are off edge
             */
            int s00 = n * i;
            int j;
            for (j = 0; j < ny - 1; j += 2) {
                b[k] = (byte) (((a[s00 + 1] << 2 & b2) | (a[s00] << 3 & b3)) >> bit);
                k += 1;
                s00 += 2;
            }
            if (j < ny) {
                /*
                 * both row and column size are odd, do corner element s00+1,
                 * s10, s10+1 are off edge
                 */
                b[k] = (byte) (((a[s00] << 3 & b3)) >> bit);
                k += 1;
            }
        }
    }

    /* ######################################################################### */
    /*
     * do one quadtree reduction step on array a results put into b (which may
     * be the same as a)
     */
    void qtreeReduce(byte a[], int n, int nx, int ny, byte b[]) {

        int k = 0; /* k is index of b[i/2,j/2] */
        int i;
        for (i = 0; i < nx - 1; i += 2) {

            int s00 = n * i; /* s00 is index of a[i,j] */
            int s10 = s00 + n; /* s10 is index of a[i+1,j] */
            int j;

            for (j = 0; j < ny - 1; j += 2) {
                b[k] = (byte) (((a[s10 + 1] != 0) ? 1 : 0) | (((a[s10] != 0) ? 1 : 0) << 1) | (((a[s00 + 1] != 0) ? 1 : 0) << 2) | (((a[s00] != 0) ? 1 : 0) << 3));
                k += 1;
                s00 += 2;
                s10 += 2;
            }

            if (j < ny) {
                /*
                 * row size is odd, do last element in row s00+1,s10+1 are off
                 * edge
                 */
                b[k] = (byte) ((((a[s10] != 0) ? 1 : 0) << 1) | (((a[s00] != 0) ? 1 : 0) << 3));
                k += 1;
            }
        }
        if (i < nx) {
            /*
             * column size is odd, do last row s10,s10+1 are off edge
             */
            int s00 = n * i;
            int j;
            for (j = 0; j < ny - 1; j += 2) {
                b[k] = (byte) ((((a[s00 + 1] != 0) ? 1 : 0) << 2) | (((a[s00] != 0) ? 1 : 0) << 3));
                k += 1;
                s00 += 2;
            }
            if (j < ny) {
                /*
                 * both row and column size are odd, do corner element s00+1,
                 * s10, s10+1 are off edge
                 */
                b[k] = (byte) ((((a[s00] != 0) ? 1 : 0) << 3));
                k += 1;
            }
        }
    }

    private void writeDirect(int a[], int offset, int n, int nqx, int nqy, byte[] scratch, int bit, OutputBitStream os) throws IOException {

        /*
         * Write the direct bitmap warning code
         */
        os.writeBits(0x0, 4);
        /*
         * Copy A to scratch array (again!), packing 4 bits/nybble
         */
        qtreeOnebit(a, offset, n, nqx, nqy, scratch, bit);
        /*
         * write to outfile
         */
        for (int i = 0; i < ((nqx + 1) / 2) * ((nqy + 1) / 2); i++) {
            os.writeBits(scratch[i], 4);
        }
    }

    /**
     * Decompress the input stream. The result left in the a[] array and can be
     * accessed using the various get methods.
     */
    public int[] decomp(byte[] input) throws IOException {

        InputBitStream dis = new InputBitStream(new ByteArrayInputStream(input));

        System.err.println("Before decode");
        int[] a = decode(dis); // Launch decoding
        System.err.println("After decode");
        undigitize(a);
        System.err.println("After undigitze");

        boolean[] flag = null;

        if (tmp == null || tmp.length < Math.max(nx, ny)) {
            tmp = new int[Math.max(nx, ny)];
            flag = new boolean[Math.max(nx, ny)];
        }
        System.err.println("About to call hinv");
        hinv(a, flag); // Inverse H-transform
        System.err.println("Done decomp!");
        return a;
    }

    public byte[] decompress(byte[] input) throws IOException {
        int[] a = decomp(input);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        BufferedDataOutputStream bd = new BufferedDataOutputStream(bytes);
        bd.write(a);
        bd.close();
        return bytes.toByteArray();
    }

    public byte[] decompress(byte[] input, int length) throws IOException {
        return decompress(input);
    }

    /**
     * Input buffering.
     * 
     * @return the next int
     */
    private int getint(InputBitStream dis) throws IOException {
        return dis.readBits(32);
    }

    /**
     * Huffman decoding for fixed codes Coded values range from 0-15 Huffman
     * code values (hex):
     * 
     * <pre>
     * 3e, 00, 01, 08, 02, 09, 1a, 1b,
     * 03, 1c, 0a, 1d, 0b, 1e, 3f, 0c
     * 
     * and number of bits in each code:
     * 
     * 6,  3,  3,  4,  3,  4,  5,  5,
     * 3,  5,  4,  5,  4,  5,  6,  4
     * </pre>
     * 
     * @return The appropriate Huffman code.
     */
    private int decodeHuffman(InputBitStream dis) throws IOException {
        int c;

        /* get first 3 bits to start */
        c = dis.readBits(3);
        if (c < 4) {
            /*
             * this is all we need return 1,2,4,8 for c=0,1,2,3
             */
            return (1 << c);
        }

        /* get the next bit */
        c = dis.readBits(1) | (c << 1);
        if (c < 13) {
            /* OK, 4 bits is enough */
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

        /* get yet another bit */
        c = dis.readBits(1) | (c << 1);
        if (c < 31) {
            /* OK, 5 bits is enough */
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

        /* need the 6th bit */
        c = dis.readBits(1) | (c << 1);
        if (c == 62)
            return (0);
        else
            return (14);
    }

    /**
     * Copy 4-bit values from a[(nx+1)/2,(ny+1)/2] to b[nx,ny], expanding each
     * value to 2x2 pixels and inserting into bitplane BIT of B. A,B may NOT be
     * same array (it wouldn't make sense to be inserting bits into the same
     * array anyway.)
     * 
     * @param n
     *            declared y dimension of b
     */
    private void qtreeBitInsert(int[] a, byte d[], int nx, int ny, int off, int n, int bit) {
        int i, j, k;
        int s00, s10;
        int nxN = nx - 1;
        int nyN = ny - 1;
        int c;
        int dc;

        /* expand each 2x2 block */
        c = 0; /* k is index of a[i/2,j/2] */
        for (i = 0; i < nxN; i += 2) {
            s00 = off + n * i; /* s00 is index of a[i,j] */
            s10 = s00 + n; /* s10 is index of a[i+1,j] */
            for (j = 0; j < nyN; j += 2) {
                dc = d[c];
                a[s10 + 1] |= (dc & 1) << bit;
                a[s10] |= ((dc >> 1) & 1) << bit;
                a[s00 + 1] |= ((dc >> 2) & 1) << bit;
                a[s00] |= ((dc >> 3) & 1) << bit;
                s00 += 2;
                s10 += 2;
                c += 1;
            }
            if (j < ny) {
                /*
                 * row size is odd, do last element in row s00+1, s10+1 are off
                 * edge
                 */
                dc = d[c];
                a[s10] |= ((dc >> 1) & 1) << bit;
                a[s00] |= ((dc >> 3) & 1) << bit;
                c++;
            }
        }
        if (i < nx) {
            /*
             * column size is odd, do last row s10, s10+1 are off edge
             */
            s00 = off + n * i;
            for (j = 0; j < nyN; j += 2) {
                dc = d[c];
                a[s00 + 1] |= ((dc >> 2) & 1) << bit;
                a[s00] |= ((dc >> 3) & 1) << bit;
                s00 += 2;
                c++;
            }
            if (j < ny) {
                /*
                 * both row and column size are odd, do corner element s00+1,
                 * s10, s10+1 are off edge
                 */
                a[s00] |= ((d[c] >> 3) & 1) << bit;
                c++;
            }
        }
    }

    private void readDirect(int[] a, int off, int n, int nqx, int nqy, byte scratch[], int bit, InputBitStream dis) throws IOException {
        int i;
        int j = ((nqx + 1) / 2) * ((nqy + 1) / 2);

        /* read bit image packed 4 pixels/nybble */
        for (i = 0; i < j; i++) {
            scratch[i] = (byte) (dis.readBits(4));
        }

        /* insert in bitplane BIT of image A */
        qtreeBitInsert(a, scratch, nqx, nqy, off, n, bit);
    }

    /**
     * copy 4-bit values from a[(nx+1)/2,(ny+1)/2] to b[nx,ny], expanding each
     * value to 2x2 pixels a,b may be same array
     * 
     * @param n
     *            declared y dimension of b
     */
    private void qtreeCopy(byte d[], int nx, int ny, byte b[], int n) {
        int i, j, k, nx2, ny2;
        int s00, s10;
        int nxN = nx - 1;
        int nyN = ny - 1;
        int bs00;

        /*
         * first copy 4-bit values to b start at end in case a,b are same array
         */
        nx2 = (nx + 1) / 2;
        ny2 = (ny + 1) / 2;
        k = ny2 * (nx2 - 1) + ny2 - 1; /* k is index of a[i,j] */

        for (i = nx2 - 1; i >= 0; i--) {
            s00 = (n * i + ny2 - 1) << 1; /* s00 is index of b[2*i,2*j] */
            for (j = ny2 - 1; j >= 0; j--) {
                b[s00] = d[k--];
                s00 -= 2;
            }
        }

        /* now expand each 2x2 block */
        for (i = 0; i < nxN; i += 2) {
            s00 = n * i; /* s00 is index of b[i,j] */
            s10 = s00 + n; /* s10 is index of b[i+1,j] */
            for (j = 0; j < nyN; j += 2) {
                bs00 = b[s00];
                b[s10 + 1] = (byte) (bs00 & 1);
                b[s10] = (byte) ((bs00 >> 1) & 1);
                b[s00 + 1] = (byte) ((bs00 >> 2) & 1);
                b[s00] = (byte) ((bs00 >> 3) & 1);
                s00 += 2;
                s10 += 2;
            }
            if (j < ny) {
                /*
                 * row size is odd, do last element in row s00+1, s10+1 are off
                 * edge
                 */
                bs00 = b[s00];
                b[s10] = (byte) ((bs00 >> 1) & 1);
                b[s00] = (byte) ((bs00 >> 3) & 1);
            }
        }
        if (i < nx) {
            /*
             * column size is odd, do last row s10, s10+1 are off edge
             */
            s00 = n * i;
            for (j = 0; j < nyN; j += 2) {
                bs00 = b[s00];
                b[s00 + 1] = (byte) ((bs00 >> 2) & 1);
                b[s00] = (byte) ((bs00 >> 3) & 1);
                s00 += 2;
            }
            if (j < ny) {
                /*
                 * both row and column size are odd, do corner element s00+1,
                 * s10, s10+1 are off edge
                 */
                b[s00] = (byte) ((b[s00] >> 3) & 1);
            }
        }
    }

    /**
     * do one quadtree expansion step on array a[(nqx+1)/2,(nqy+1)/2] results
     * put into b[nqx,nqy] (which may be the same as a)
     */
    private void qtreeExpand(int[] a, byte d[], int nx, int ny, byte b[], InputBitStream dis) throws IOException {
        int i;

        /* first copy a to b, expanding each 4-bit value */
        qtreeCopy(d, nx, ny, b, ny);

        /* now read new 4-bit values into b for each non-zero element */
        for (i = nx * ny - 1; i >= 0; i--) {
            if (b[i] != 0)
                b[i] = (byte) decodeHuffman(dis);
        }
    }

    /**
     * @param n
     *            length of full row in a
     * @param nqx
     *            partial length of row to decode
     * @param nqy
     *            partial length of column (<=n)
     * @param nbitplanes
     *            number of bitplanes to decode
     */
    private void qtreeDecode(int[] a, int off, int n, int nqx, int nqy, int nbitplanes, InputBitStream dis) throws IOException {
        int log2n, k, bit, b, nqmax;
        int nx, ny, nfx, nfy, c;
        int nqx2, nqy2;

        /* log2n is log2 of max(nqx,nqy) rounded up to next power of 2 */
        nqmax = (nqx > nqy) ? nqx : nqy;
        log2n = (int) (Math.log(nqmax) / log2 + 0.5);
        if (nqmax > (1 << log2n))
            log2n += 1;

        /* allocate scratch array for working space */
        nqx2 = (nqx + 1) / 2;
        nqy2 = (nqy + 1) / 2;

        byte[] scratch = new byte[nqx2 * nqy2];

        /*
         * now decode each bit plane, starting at the top A is assumed to be
         * initialized to zero
         */
        for (bit = nbitplanes - 1; bit >= 0; bit--) {

            /* Was bitplane was quadtree-coded or written directly? */
            b = dis.readBits(4);
            if (b == 0) {
                /* bit map was written directly */
                readDirect(a, off, n, nqx, nqy, scratch, bit, dis);
            } else if (b != 0xf) {
                throw new IOException("qtreeDecode: bad format code " + b);
            } else {

                /*
                 * bitmap was quadtree-coded, do log2n expansions read first
                 * code
                 */
                scratch[0] = (byte) decodeHuffman(dis);

                /* now do log2n expansions, reading codes from file as necessary */
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

                    qtreeExpand(a, scratch, nx, ny, scratch, dis);
                }

                /* now copy last set of 4-bit codes to bitplane bit of array a */
                qtreeBitInsert(a, scratch, nqx, nqy, off, n, bit);
            }
        }
        scratch = null;
    }

    private void dodecode(InputBitStream dis, int[] a, int[] nbitplanes) throws IOException {

        int i, nx2, ny2, aa;

        nx2 = (nx + 1) / 2;
        ny2 = (ny + 1) / 2;

        /* Read bit planes for each quadrant */
        qtreeDecode(a, 0, ny, nx2, ny2, nbitplanes[0], dis);
        qtreeDecode(a, ny2, ny, nx2, ny / 2, nbitplanes[1], dis);
        qtreeDecode(a, ny * nx2, ny, nx / 2, ny2, nbitplanes[1], dis);
        qtreeDecode(a, ny * nx2 + ny2, ny, nx / 2, ny / 2, nbitplanes[2], dis);

        /* Make sure there is an EOF symbol (nybble=0) at end */
        if (dis.readBits(4) != 0) {
            System.err.println("dodecode: bad bit plane values\n");
            throw new IOException("Error in dodecode decompression");
        }

        /* Now get the sign bits - Re-initialize bit input */
        dis.flush();

        for (i = 0; i < (nx * ny); i++) {
            if (((aa = a[i]) != 0) && (dis.readBits(1) != 0)) {
                a[i] = -aa;
            }
        }
    }

    private int[] decode(InputBitStream dis) throws IOException {
        int sumall;
        int q = 0, w = 0;

        int m1 = dis.readBits(8) & 0xff;
        int m2 = dis.readBits(8) & 0xff;
        // Read magic number
        if ((m1 != code_magic[0]) || (m2 != code_magic[1])) {
            throw new IOException("Bad magic number");
        }

        // read size
        nx = dis.readBits(32);
        ny = dis.readBits(32);
        int nel = nx * ny;

        // read scale
        scale = dis.readBits(32);

        int[] a = new int[nel];

        // sum of all pixels
        sumall = dis.readBits(32);

        int[] nbitplanes = new int[3];

        // # bits in quadrants
        nbitplanes[0] = dis.readBits(8);
        nbitplanes[1] = dis.readBits(8);
        nbitplanes[2] = dis.readBits(8);

        dodecode(dis, a, nbitplanes);

        // at the end
        a[0] = sumall;
        return a;
    }

    private void undigitize(int[] a) {
        if (scale <= 1)
            return;
        for (int i = 0; i < a.length; i += 1) {
            a[i] *= scale;
        }
    }

    /** Invert the H transform */
    private void hinv(int[] a, boolean[] flag) {

        /*
         * log2n is log2 of max(nx,ny) rounded up to next power of 2
         */
        int nmax = (nx > ny) ? nx : ny;
        int log2n = (int) (Math.log((double) nmax) / log2 + 0.5);

        if (nmax > (1 << log2n)) {
            log2n += 1;
        }
        /*
         * do log2n expansions We're indexing a as a 2-D array with dimensions
         * (nx,ny).
         */
        int nxtop = 1;
        int nytop = 1;

        int nxf = nx;
        int nyf = ny;
        int c = 1 << log2n;

        for (int k = log2n - 1; k > 0; k--) {
            /*
             * this somewhat cryptic code generates the sequence ntop[k-1] =
             * (ntop[k]+1)/2, where ntop[log2n] = n
             */
            c >>= 1;
            nxtop <<= 1;
            nytop <<= 1;

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
             * unshuffle in each dimension to interleave coefficients
             */
            xunshuffle(a, nxtop, nytop, ny);
            yunshuffle(a, nxtop, nytop, ny, flag);
            for (int i = 0; i < nxtop - 1; i += 2) {
                int pend = ny * i + nytop - 1;

                int p00 = 0, p10 = 0;

                for (p00 = ny * i, p10 = ny * (i + 1); p00 < pend; p00 += 2, p10 += 2) {

                    int h0 = a[p00];
                    int hx = a[p10];
                    int hy = a[p00 + 1];
                    int hc = a[p10 + 1];

                    /*
                     * Divide sums by 2
                     */
                    int sum1 = h0 + hx + 1;
                    int sum2 = hy + hc;
                    a[p10 + 1] = (sum1 + sum2) >> 1;
                    a[p10] = (sum1 - sum2) >> 1;
                    sum1 = h0 - hx + 1;
                    sum2 = hy - hc;
                    a[p00 + 1] = (sum1 + sum2) >> 1;
                    a[p00] = (sum1 - sum2) >> 1;

                }
                if (p00 == pend) {
                    /*
                     * do last element in row if row length is odd p00+1, p10+1
                     * are off edge
                     */
                    int h0 = a[p00];
                    int hx = a[p10];
                    a[p10] = (h0 + hx + 1) >> 1;
                    a[p00] = (h0 - hx + 1) >> 1;
                }
            }
            if (nxtop % 2 == 1) {
                int i = nxtop - 1;
                /*
                 * do last row if column length is odd p10, p10+1 are off edge
                 */
                int pend = ny * i + nytop - 1;
                int p00;
                for (p00 = ny * i; p00 < pend; p00 += 2) {

                    int h0 = a[p00];
                    int hy = a[p00 + 1];
                    a[p00 + 1] = (h0 + hy + 1) >> 1;
                    a[p00] = (h0 - hy + 1) >> 1;
                }

                if (p00 == pend) {
                    /*
                     * do corner element if both row and column lengths are odd
                     * p00+1, p10, p10+1 are off edge
                     */
                    a[p00] = (a[p00] + 1) >> 1;
                }
            }
        }

        /*
         * Last pass (k=0) has some differences: Shift by 2 instead of 1 Use
         * explicit values for all variables to avoid unnecessary shifts etc: N
         * bitN maskN prndN nrndN 0 1 -1 0 0 (note nrnd0 != prnd0-1) 1 2 -2 1 0
         * 2 4 -4 2 1
         */

        /*
         * Check nxtop=nx, nytop=ny
         */
        c >>= 1;
        nxtop <<= 1;
        nytop <<= 1;

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

        if (nxtop != nx || nytop != ny) {
            System.err.println("hinv: error, final image size is " + nxtop + " x " + nytop + " not " + nx + " x " + ny);
        }

        /*
         * unshuffle in each dimension to interleave coefficients
         */
        xunshuffle(a, nx, ny, ny);
        yunshuffle(a, nx, ny, ny, flag);

        for (int i = 0; i < nx - 1; i += 2) {

            int pend = ny * i + ny - 1;
            int p00 = 0, p10 = 0;
            for (p00 = ny * i, p10 = p00 + ny; p00 < pend; p00 += 2, p10 += 2) {
                int h0 = a[p00];
                int hx = a[p10];
                int hy = a[p00 + 1];
                int hc = a[p10 + 1];
                /*
                 * Divide sums by 4
                 */
                int sum1 = h0 + hx + 2;
                int sum2 = hy + hc;
                a[p10 + 1] = (sum1 + sum2) >> 2;
                a[p10] = (sum1 - sum2) >> 2;
                sum1 = h0 - hx + 2;
                sum2 = hy - hc;
                a[p00 + 1] = (sum1 + sum2) >> 2;
                a[p00] = (sum1 - sum2) >> 2;
            }
            if (p00 == pend) {
                /*
                 * Do last element in row if row length is odd p00+1, p10+1 are
                 * off edge
                 */
                int h0 = a[p00];
                int hx = a[p10];
                a[p10] = (h0 + hx + 2) >> 2;
                a[p00] = (h0 - hx + 2) >> 2;
            }
        }
        if (nx % 2 == 1) {
            int i = nx - 1;
            /*
             * Do last row if column length is odd p10, p10+1 are off edge
             */
            int pend = ny * i + ny - 1;
            int p00;
            for (p00 = ny * i; p00 < pend; p00 += 2) {
                int h0 = a[p00];
                int hy = a[p00 + 1];
                a[p00 + 1] = (h0 + hy + 2) >> 2;
                a[p00] = (h0 - hy + 2) >> 2;
            }
            if (p00 == pend) {
                /*
                 * Do corner element if both row and column lengths are odd
                 * p00+1, p10, p10+1 are off edge
                 */
                a[p00] = a[p00 + 2] >> 2;
            }
        }
    }

    private void xunshuffle(int[] a, int nx, int ny, int nydim) {

        int nhalf = (ny + 1) >> 1;
        for (int j = 0; j < nx; j++) {
            /*
             * copy 2nd half of array to tmp
             */
            System.arraycopy(a, j * nydim + nhalf, tmp, 0, ny - nhalf);
            /*
             * distribute 1st half of array to even elements
             */
            int pend = j * nydim;
            for (int p2 = j * nydim + nhalf - 1, p1 = j * nydim + ((nhalf - 1) << 1); p2 >= pend; p1 -= 2, p2 -= 1) {
                a[p1] = a[p2];
            }
            /*
             * now distribute 2nd half of array (in tmp) to odd elements
             */
            pend = j * nydim + ny;
            for (int pt = 0, p1 = j * nydim + 1; p1 < pend; p1 += 2, pt += 1) {
                a[p1] = tmp[pt];
            }
        }
    }

    private void yunshuffle(int[] a, int nx, int ny, int nydim, boolean[] flag) {

        /*
         * initialize flag array telling whether row is done
         */
        for (int j = 0; j < nx; j++)
            flag[j] = true;

        int oddoffset = (nx + 1) / 2;
        /*
         * shuffle each row to appropriate location row 0 is already in right
         * location
         */
        int k = 0;

        for (int j = 1; j < nx; j++) {
            if (flag[j]) {
                flag[j] = false;
                /*
                 * where does this row belong?
                 */
                if (j >= oddoffset) {
                    /* odd row */
                    k = ((j - oddoffset) << 1) + 1;
                } else {
                    /* even row */
                    k = j << 1;
                }
                if (j != k) {
                    /*
                     * copy the row
                     */
                    System.arraycopy(a, nydim * j, tmp, 0, ny);
                    /*
                     * keep shuffling until we reach a row that is done
                     */
                    while (flag[k]) {
                        flag[k] = false;
                        /*
                         * do the exchange
                         */
                        for (int p = nydim * k, pt = 0; p < nydim * k + ny; p++, pt++) {
                            int tt = a[p];
                            a[p] = tmp[pt];
                            tmp[pt] = tt;

                        }
                        if (k >= oddoffset) {
                            k = ((k - oddoffset) << 1) + 1;
                        } else {
                            k <<= 1;
                        }
                    }
                    /*
                     * copy the last row into place this should always end up
                     * with j=k
                     */
                    System.arraycopy(tmp, 0, a, nydim * k, ny);
                    if (j != k) {
                        System.err.println("error: yunshuffle failed!\nj=" + j + " k=" + k);
                    }
                }
            }
        }
    }
}
