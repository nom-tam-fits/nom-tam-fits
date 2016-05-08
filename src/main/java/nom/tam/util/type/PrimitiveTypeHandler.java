package nom.tam.util.type;

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

import java.nio.Buffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class PrimitiveTypeHandler {

    private static final int MAX_TYPE_VALUE = 256;

    private static final int BIT_PIX_OFFSET = 64;

    private static PrimitiveType<?>[] byBitPix;

    private static PrimitiveType<?>[] byType;

    private static Map<Class<?>, PrimitiveType<? extends Buffer>> byClass;

    @SuppressWarnings("unchecked")
    private static <B extends Buffer> PrimitiveType<B> cast(PrimitiveType<?> primitiveType) {
        return (PrimitiveType<B>) primitiveType;
    }

    static {
        byBitPix = new PrimitiveType[BIT_PIX_OFFSET * 2 + 1];
        byType = new PrimitiveType[MAX_TYPE_VALUE];
        Map<Class<?>, PrimitiveType<?>> initialByClass = new HashMap<Class<?>, PrimitiveType<?>>();
        for (PrimitiveType<?> type : values()) {
            if (type.bitPix() != 0) {
                byBitPix[type.bitPix() + BIT_PIX_OFFSET] = type;
            }
            byType[type.type()] = type;
            initialByClass.put(type.primitiveClass(), type);
            initialByClass.put(type.wrapperClass(), type);
            if (type.bufferClass() != null) {
                initialByClass.put(type.bufferClass(), type);
            }
        }
        byClass = Collections.unmodifiableMap(initialByClass);
    }

    public static <B extends Buffer> PrimitiveType<B> valueOf(Class<?> clazz) {
        PrimitiveType<?> primitiveType = byClass.get(clazz);
        if (primitiveType == null) {
            for (Class<?> interf : clazz.getInterfaces()) {
                primitiveType = byClass.get(interf);
                if (primitiveType != null) {
                    return cast(primitiveType);
                }
            }
            return valueOf(clazz.getSuperclass());
        }
        return cast(primitiveType);
    }

    private static PrimitiveType<?>[] values() {
        return new PrimitiveType[]{
            PrimitiveTypes.BOOLEAN,
            PrimitiveTypes.BYTE,
            PrimitiveTypes.CHAR,
            PrimitiveTypes.DOUBLE,
            PrimitiveTypes.FLOAT,
            PrimitiveTypes.INT,
            PrimitiveTypes.LONG,
            PrimitiveTypes.SHORT,
            PrimitiveTypes.STRING,
            PrimitiveTypes.UNKNOWN
        };
    }

    private PrimitiveTypeHandler() {
    }

    public static PrimitiveType<Buffer> valueOf(int bitPix) {
        return cast(byBitPix[bitPix + BIT_PIX_OFFSET]);
    }

    public static PrimitiveType<Buffer> valueOf(char type) {
        return cast(byType[type]);
    }
}
