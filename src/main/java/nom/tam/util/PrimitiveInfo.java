package nom.tam.util;

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
