package nom.tam.fits.compression.algorithm.rice;

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

/**
 * A bit wise reader writer around a bytebuffer.
 * 
 * @author Ritchie
 */
public class BitBuffer {

    private static final int BITS_OF_4_BYTES = 32;

    private static final int BYTE_MASK = 0xFF;

    private static final long INTEGER_MASK = 0xFFFFFFFFL;

    private static final int BITS_OF_1_BYTE = 8;

    private static final int BITS_OF_2_BYTES = 16;

    private static final int BITS_OF_3_BYTES = 24;

    private static final int BYTE_1_OF_INT = 0x000000FF;

    private static final int BYTE_2_OF_INT = 0x0000FF00;

    private static final int BYTE_3_OF_INT = 0x00FF0000;

    private static final int BYTE_4_OF_INT = 0xFF000000;

    private final ByteBuffer buffer;

    private long position;

    public BitBuffer(ByteBuffer writeBuffer) {
        this.buffer = writeBuffer;
    }

    public int bitbuffer() {
        return this.buffer.get((int) (this.position / BITS_OF_1_BYTE));
    }

    void close() {
        if (this.position % BITS_OF_1_BYTE != 0) {
            putByte((byte) 0, (int) (BITS_OF_1_BYTE - this.position % BITS_OF_1_BYTE));
        }
        this.buffer.position((int) (this.position / BITS_OF_1_BYTE));
    }

    public int missingBitsInCurrentByte() {
        return (int) (BITS_OF_1_BYTE - this.position % BITS_OF_1_BYTE);
    }

    public void movePosition(int i) {
        this.position += i;
    }

    public void putByte(byte byteToAdd) {
        final int bytePosition = (int) (this.position / BITS_OF_1_BYTE);
        final int positionInByte = (int) (this.position % BITS_OF_1_BYTE);
        final byte old = (byte) (this.buffer.get(bytePosition) & (byte) ~(BYTE_MASK >>> positionInByte));
        final int byteAsInt = byteToAdd & BYTE_MASK;
        this.buffer.put(bytePosition, (byte) (old | (byte) (byteAsInt >>> positionInByte)));
        if (positionInByte > 0) {
            this.buffer.put(bytePosition + 1, (byte) (byteAsInt << BITS_OF_1_BYTE - positionInByte));
        }
        this.position += BITS_OF_1_BYTE;
    }

    public void putByte(byte byteToAdd, int bits) {
        final int bytePosition = (int) (this.position / BITS_OF_1_BYTE);
        final int positionInByte = (int) (this.position % BITS_OF_1_BYTE);
        final byte old = this.buffer.get(bytePosition);
        final int byteAsInt = BYTE_MASK & (byteToAdd & BYTE_MASK >>> BITS_OF_1_BYTE - bits) << BITS_OF_1_BYTE - bits;
        this.buffer.put(bytePosition, (byte) (BYTE_MASK & //
                (old & BYTE_MASK << BITS_OF_1_BYTE - positionInByte | byteAsInt >>> positionInByte)));
        if (BITS_OF_1_BYTE - positionInByte < bits) {
            this.buffer.put(bytePosition + 1, (byte) (BYTE_MASK & byteAsInt << BITS_OF_1_BYTE - positionInByte));
        }
        this.position += bits;
    }

    /**
     * write out int value to the next 4 bytes of the buffer
     * 
     * @param i
     *            integer to write
     */
    public void putInt(int i) {
        putByte((byte) ((i & BYTE_4_OF_INT) >>> BITS_OF_3_BYTES));
        putByte((byte) ((i & BYTE_3_OF_INT) >>> BITS_OF_2_BYTES));
        putByte((byte) ((i & BYTE_2_OF_INT) >>> BITS_OF_1_BYTE));
        putByte((byte) (i & BYTE_1_OF_INT));
    }

    public void putInt(int i, int bits) {
        if (bits == 0) {
            return;
        }
        do {
            if (bits >= BITS_OF_1_BYTE) {
                putByte((byte) ((i & BYTE_MASK << bits - BITS_OF_1_BYTE) >>> bits - BITS_OF_1_BYTE & BYTE_MASK));
                bits -= BITS_OF_1_BYTE;
            } else {
                putByte((byte) (i & BYTE_MASK >> -(bits - BITS_OF_1_BYTE)), bits);
                bits = 0;
            }
        } while (bits > 0);
    }

    public void putLong(long l, int bits) {
        if (bits == 0) {
            return;
        }
        do {
            if (bits >= BITS_OF_4_BYTES) {
                putInt((int) ((l & INTEGER_MASK << bits - BITS_OF_4_BYTES) >>> bits - BITS_OF_4_BYTES));
                bits -= BITS_OF_4_BYTES;
            } else {
                putInt((int) (l & INTEGER_MASK >> -(bits - BITS_OF_4_BYTES)), bits);
                bits = 0;
            }
        } while (bits > 0);
    }

}
