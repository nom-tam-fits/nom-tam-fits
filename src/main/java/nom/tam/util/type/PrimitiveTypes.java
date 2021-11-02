package nom.tam.util.type;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2021 nom-tam-fits
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
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

/**
 * @deprecated  Use identical static fields of {@link ElementType} instead.
 * 
 */
@Deprecated
public final class PrimitiveTypes {

    /** @deprecated Use {@link ElementType#BOOLEAN} instead */
    public static final ElementType<Buffer> BOOLEAN = ElementType.BOOLEAN;

    /** @deprecated Use {@link ElementType#BYTE} instead */
    public static final ElementType<ByteBuffer> BYTE = ElementType.BYTE;

    /** @deprecated Use {@link ElementType#CHAR} instead */
    public static final ElementType<ByteBuffer> CHAR = ElementType.CHAR;

    /** @deprecated Use {@link ElementType#DOUBLE} instead */
    public static final ElementType<DoubleBuffer> DOUBLE = ElementType.DOUBLE;

    /** @deprecated Use {@link ElementType#FLOAT} instead */
    public static final ElementType<FloatBuffer> FLOAT = ElementType.FLOAT;

    /** @deprecated Use {@link ElementType#INT} instead */
    public static final ElementType<IntBuffer> INT = ElementType.INT;

    /** @deprecated Use {@link ElementType#LONG} instead */
    public static final ElementType<LongBuffer> LONG = ElementType.LONG;

    /** @deprecated Use {@link ElementType#SHORT} instead */
    public static final ElementType<ShortBuffer> SHORT = ElementType.SHORT;

    /** @deprecated Use {@link ElementType#STRING} instead */
    public static final ElementType<Buffer> STRING = ElementType.STRING;

    /** @deprecated Use {@link ElementType#UNKNOWN} instead */
    public static final ElementType<Buffer> UNKNOWN = ElementType.UNKNOWN;
    
    private PrimitiveTypes() {
        // TODO Auto-generated constructor stub
    }

}
