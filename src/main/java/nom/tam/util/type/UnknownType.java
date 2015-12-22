package nom.tam.util.type;

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

import java.nio.Buffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UnknownType extends PrimitiveTypeBase<Buffer> {

    private static final int BIT_PIX_OFFSET = 64;

    private PrimitiveType<?>[] byBitPix;

    private Map<Class<?>, PrimitiveType<?>> byClass;

    protected UnknownType() {
        super(0, true, Object.class, Object.class, null, 'L', 0);
    }

    private void initialize() {
        if (this.byBitPix == null) {
            this.byBitPix = new PrimitiveType[BIT_PIX_OFFSET * 2 + 1];
            Map<Class<?>, PrimitiveType<?>> initialByClass = new HashMap<>();
            for (PrimitiveType<?> type : values()) {
                if (type.bitPix() != 0) {
                    this.byBitPix[type.bitPix() + BIT_PIX_OFFSET] = type;
                }
                initialByClass.put(type.primitiveClass(), type);
                initialByClass.put(type.wrapperClass(), type);
                if (type.bufferClass() != null) {
                    initialByClass.put(type.bufferClass(), type);
                }
            }
            this.byClass = Collections.unmodifiableMap(initialByClass);
        }
    }

    @Override
    public int size(Object instance) {
        return 0;
    }

    public <T extends PrimitiveType> T valueOf(Class<?> clazz) {
        initialize();
        PrimitiveType<?> primitiveType = this.byClass.get(clazz);
        if (primitiveType == null) {
            for (Class<?> interf : clazz.getInterfaces()) {
                primitiveType = this.byClass.get(interf);
                if (primitiveType != null) {
                    return (T) primitiveType;
                }
            }
            return (T) valueOf(clazz.getSuperclass());
        }
        return (T) primitiveType;
    }

    public PrimitiveType<?> valueOf(int bitPix) {
        initialize();
        return this.byBitPix[bitPix + BIT_PIX_OFFSET];
    }

    private PrimitiveType<?>[] values() {
        return new PrimitiveType[]{
            BOOLEAN,
            BYTE,
            CHAR,
            DOUBLE,
            FLOAT,
            INT,
            LONG,
            SHORT,
            STRING,
            UNKNOWN
        };
    }
}
