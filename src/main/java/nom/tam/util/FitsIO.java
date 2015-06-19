package nom.tam.util;

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

import java.io.Closeable;

/**
 * super closable interface for all fits reader/writers. It defines the
 * nessesary constands common for all reader and writers.
 * 
 * @author nir
 */
public interface FitsIO extends Closeable {

    /**
     * number of bits in one byte.
     */
    public static final int BITS_OF_1_BYTE = 8;

    /**
     * number of bits in two byte.
     */
    public static final int BITS_OF_2_BYTES = 16;

    /**
     * number of bits in three byte.
     */
    public static final int BITS_OF_3_BYTES = 24;

    /**
     * number of bits in four byte.
     */
    public static final int BITS_OF_4_BYTES = 32;

    /**
     * number of bits in five byte.
     */
    public static final int BITS_OF_5_BYTES = 40;

    /**
     * number of bits in six byte.
     */
    public static final int BITS_OF_6_BYTES = 48;

    /**
     * number of bits in seven byte.
     */
    public static final int BITS_OF_7_BYTES = 56;

    /**
     * number of bytes occupied by a boolean.
     */
    public static final int BYTES_IN_BOOLEAN = 1;

    /**
     * number of bytes occupied by a byte.
     */
    public static final int BYTES_IN_BYTE = 1;

    /**
     * number of bytes occupied by a char.
     */
    public static final int BYTES_IN_CHAR = 2;

    /**
     * number of bytes occupied by a short.
     */
    public static final int BYTES_IN_SHORT = 2;

    /**
     * number of bytes occupied by a integer.
     */
    public static final int BYTES_IN_INTEGER = 4;

    /**
     * number of bytes occupied by a long.
     */
    public static final int BYTES_IN_LONG = 8;

    /**
     * number of bytes occupied by a float.
     */
    public static final int BYTES_IN_FLOAT = 4;

    /**
     * number of bytes occupied by a double.
     */
    public static final int BYTES_IN_DOUBLE = 8;

    /**
     * bit mask to get the lowest byte from an integer. Or to get an unsigned
     * integer from a byte.
     */
    public static final int BYTE_MASK = 0xFF;

    /**
     * bit mask to get the lowest integer from an long.
     */
    public static final long INTEGER_MASK = 0x00000000ffffffffL;
}
