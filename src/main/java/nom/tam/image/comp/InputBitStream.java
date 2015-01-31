package nom.tam.image.comp;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
        this.input = in;
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
        int outOffset = 0;
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
            int bits = ((currentByte >> (8 - (inOffset + getting))) & masks[getting]) << (need - getting);

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
