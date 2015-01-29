package nom.tam.util;

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
