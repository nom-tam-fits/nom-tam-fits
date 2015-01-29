package nom.tam.image.comp;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * This class opens a regular output stream and allows the user to write 0-32
 * bits at a time. It assumes big-endian integers as would be used in a FITS
 * file.
 * 
 * @author tmcglynn
 */
public class OutputBitStream {

    private OutputStream output;

    private int currentByte;

    private static final int BYTESIZE = 8;

    private int outOffset = 0; // Start with an empty byte.

    private int length = 0; // Number of bytes written +1 if outOffset > 0.

    // Do we fill in bits starting with the low order bits or high order
    // bits first. E.g., given three bit requests of 4, 2 and 5 bits do
    // a3 a2 a1 a0, b1 b0, c4 c3 c2 c1 c0
    // with the two byte with bits (high to low)
    // c1 c0 b1 b0 a3 a2 a1 a0, 0 0 0 0 0 c4 c3 c2 (littleEndian = true) or
    // a3 a2 a1 a0 b1 b0 c4 c3, c2 c1 c0 0 0 0 0 0 (littleEndian = false)
    private boolean littleEndian = false;

    /** The number of bits written in the last byte */
    private int lastBitsUsed;

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

    /** Initialize the bit stream to read from an input stream. */
    public OutputBitStream(OutputStream out) {
        this(out, false);
    }

    /** Initialize the bit stream and set the endianness */
    public OutputBitStream(OutputStream out, boolean littleEndian) {
        this.output = out;
        this.littleEndian = littleEndian;
    }

    /** Read from 0-32 bits */
    public void writeBits(int val, int n) throws IOException {
        int result = 0;
        if (n <= 0) {
            return;
        } else if (n > 32) {
            throw new IllegalArgumentException("Can only ask for up to 32 bits");
        }

        int need = n; // Number of bits we need to get
        int got = 0; // The number of bits we have gotten

        while (need > 0) {
            if (outOffset == BYTESIZE) {
                emit();
            }
            int getting = BYTESIZE - outOffset; // The number of bits to get on
                                                // this iteration.0
            if (getting > need) {
                getting = need;
            }

            int base; // The low order bit we'll extract from
            int dest; // The low order bit we'll OR into.

            // The endianess controls the location at which we extract and
            // insert
            // bits.
            if (littleEndian) {
                base = got;
                dest = outOffset;
            } else {
                base = n - getting - got;
                dest = BYTESIZE - outOffset - getting;
            }

            int x = (masks[getting] << base) & val;
            // Do we need to shift the result left or right?
            if (base == dest) {
                currentByte |= x;
            } else if (base > dest) {
                currentByte |= (x >>> (base - dest));
            } else {
                currentByte |= (x << (dest - base));
            }

            outOffset += getting;
            need -= getting;
            got += getting;
        }
    }

    /** Get rid of any outstanding data. */
    public void flush() throws IOException {
        if (outOffset > 0) {
            emit();
        }
    }

    private void emit() throws IOException {
        lastBitsUsed = outOffset;
        output.write(currentByte);
        length += 1;
        outOffset = 0;
        currentByte = 0;
    }

    /**
     * How my bytes have been used in this stream. This does not include the
     * currentByte even if some bits of that have been filled.
     */
    public int size() {
        return length;
    }

    /** Close the stream */
    public void close() throws IOException {
        flush();
        output.close();
    }

    /** How many bits are used in the current byte ? */
    public int lastBitsUsed() {
        return lastBitsUsed;
    }
}
