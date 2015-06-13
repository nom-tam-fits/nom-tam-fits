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

import java.util.HashMap;
import java.util.Map;

public enum PrimitiveTypeEnum {
    BYTE(1, false, byte.class, Byte.class, 'B'),
    SHORT(2, false, short.class, Short.class, 'S'),
    CHAR(2, false, char.class, Character.class, 'C'),
    INT(4, false, int.class, Integer.class, 'I'),
    LONG(8, false, long.class, Long.class, 'J'),
    FLOAT(4, false, float.class, Float.class, 'F'),
    DOUBLE(8, false, double.class, Double.class, 'D'),
    BOOLEAN(1, false, boolean.class, Boolean.class, 'Z'),
    STRING(0, true, CharSequence.class, String.class, 'L') {

        @Override
        public int size(Object instance) {
            if (instance == null) {
                return 0;
            }
            return ((CharSequence) instance).length();
        }
    },
    UNKNOWN(0, true, Object.class, Object.class, 'L') {

        @Override
        public int size(Object instance) {
            return 0;
        }
    };

    private final int size;

    public final boolean individualSize;

    public final Class<?> primitiveClass;

    public final Class<?> wrapperClass;

    public final char type;

    private static Map<Class<?>, PrimitiveTypeEnum> lookup;

    private static Map<Class<?>, PrimitiveTypeEnum> getLookup() {
        if (lookup == null) {
            lookup = new HashMap<Class<?>, PrimitiveTypeEnum>();
            for (PrimitiveTypeEnum primitiveTypeEnum : values()) {
                lookup.put(primitiveTypeEnum.primitiveClass, primitiveTypeEnum);
                lookup.put(primitiveTypeEnum.wrapperClass, primitiveTypeEnum);
            }
        }
        return lookup;
    }

    private PrimitiveTypeEnum(int size, boolean individualSize, Class<?> primitiveClass, Class<?> wrapperClass, char type) {
        this.size = size;
        this.individualSize = individualSize;
        this.primitiveClass = primitiveClass;
        this.wrapperClass = wrapperClass;
        this.type = type;
    }

    public static PrimitiveTypeEnum valueOf(Class<?> clazz) {
        PrimitiveTypeEnum primitiveTypeEnum = getLookup().get(clazz);
        if (primitiveTypeEnum == null) {
            for (Class<?> interf : clazz.getInterfaces()) {
                primitiveTypeEnum = getLookup().get(interf);
                if (primitiveTypeEnum != null) {
                    return primitiveTypeEnum;
                }
            }

            return UNKNOWN;
        }
        return primitiveTypeEnum;
    }

    public int size() {
        return size;
    }

    /**
     * currently the only individual size primitive so, keep it simple
     * 
     * @param instance
     *            the object to calculate the size
     * @return the size in bytes of the object instance
     */
    public int size(Object instance) {
        if (instance == null) {
            return 0;
        }
        return size;
    }
}
