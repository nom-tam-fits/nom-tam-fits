package nom.tam.util;

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

/**
 * This interface collects some information about Java primitives.
 */
public interface PrimitiveInfo {

    /** Suffixes used for the classnames for primitive arrays. */
    char[] suffixes = new char[]{
        'B',
        'S',
        'C',
        'I',
        'J',
        'F',
        'D',
        'Z'
    };

    /**
     * Classes of the primitives. These should be in widening order (char is as
     * always a problem).
     */
    Class[] classes = new Class[]{
        byte.class,
        short.class,
        char.class,
        int.class,
        long.class,
        float.class,
        double.class,
        boolean.class
    };

    /** Is this a numeric class */
    boolean[] isNumeric = new boolean[]{
        true,
        true,
        true,
        true,
        true,
        true,
        true,
        false
    };

    /** Full names */
    String[] types = new String[]{
        "byte",
        "short",
        "char",
        "int",
        "long",
        "float",
        "double",
        "boolean"
    };

    /** Sizes */
    int[] sizes = new int[]{
        1,
        2,
        2,
        4,
        8,
        4,
        8,
        1
    };

    /** Index of first element of above arrays referring to a numeric type */
    int FIRST_NUMERIC = 0;

    /** Index of last element of above arrays referring to a numeric type */
    int LAST_NUMERIC = 6;

    int BYTE_INDEX = 0;

    int SHORT_INDEX = 1;

    int CHAR_INDEX = 2;

    int INT_INDEX = 3;

    int LONG_INDEX = 4;

    int FLOAT_INDEX = 5;

    int DOUBLE_INDEX = 6;

    int BOOLEAN_INDEX = 7;
}
