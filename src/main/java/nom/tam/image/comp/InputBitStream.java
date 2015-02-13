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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class opens a regular input stream and allows the user to make requests
 * for 0-32 bits at a time. It assumes big-endian integers as would be found in
 * a FITS file. Bits are read high-order bits first
 * 
 * @author tmcglynn
 */
public class InputBitStream {

    private InputStream input;

    private int currentByte;

    private static final int BYTESIZE = 8;

    private int inOffset = BYTESIZE; // Start saying we've consumed the current
                                     // byte

    /** Define a set of bit masks with 0-8 bits turned on. */
    final static int[] masks = new int[]{
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

    /** How many leading zero bits */
    final static int[] zeroBits;
    static {
        zeroBits = new int[256];
        int zb = 8;
        zeroBits[0] = zb;

        int n = 1;
        int offset = 1;
        while (offset < zeroBits.length) {
            zb -= 1;
            for (int i = 0; i < n; i += 1) {
                zeroBits[i + offset] = zb;
            }
            offset += n;
            n *= 2;
        }
    }

    /** Initialize the bit stream to read from an input stream. */
    public InputBitStream(InputStream in) {
        input = in;
    }

    /** Read from 0-32 bits */
    public int readBits(int n) throws IOException {
        int result = 0;
        if (n <= 0) {
            return result;
        } else if (n > 32) {
            throw new IllegalArgumentException("Can only ask for up to 32 bits");
        }
        int need = n;
        while (need > 0) {
            if (inOffset == BYTESIZE) {
                getByte();
            }
            int getting = 8 - inOffset;
            if (getting > need) {
                getting = need;
            }
            // We want to transfer the bits from bit inOffset to
            // inOffset+getting-1 in the
            // currentByte, to bits need-getting to need-1 in the output.
            // So
            // Shift the currentByte to ignore the bits we're not reading yet.
            // Or with the mask to get only the bits that haven't already been
            // read
            // Shift bits up to the proper location in the output value.
            int bits = (currentByte >> 8 - (inOffset + getting) & masks[getting]) << need - getting;

            // Or them with the output value.
            result |= bits;
            need -= getting;
            inOffset += getting;
        }
        return result;
    }

    private void getByte() throws IOException {
        currentByte = input.read();
        inOffset = 0;
        if (currentByte < 0) {
            throw new EOFException("EOF on input stream");
        }
    }

    /** Ignore the rest of the current byte. */
    public void flush() {
        inOffset = 8;
    }

    /**
     * Skip a set of either ones or zeroes and return the number of bits skipped
     */
    public int skipBits(boolean ones) throws IOException {

        int sum = 0;
        while (true) {
            if (inOffset == 8) {
                getByte();
            }
            // Look in the remainder of the current byte
            int bitsLeft = 8 - inOffset;
            int msk = (1 << bitsLeft) - 1; // A mask of ones of the appropriate
                                           // length.

            int remainder = currentByte & msk;

            // If we're looking for ones, flip the bits.
            if (ones) {
                remainder = ~remainder;
            }

            int cnt = zeroBits[remainder] - inOffset;
            inOffset += cnt;
            sum += cnt;

            // Did we find a bit that we're not to consume?
            // If so we are done.
            if (cnt != bitsLeft) {
                break;
            }
        }
        return sum;
    }

    /** Close the input stream */
    public void close() throws IOException {
        input.close();
    }
}
