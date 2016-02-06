package nom.tam.util.type;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2016 nom-tam-fits
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
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;

public final class PrimitiveTypes {

    public static final PrimitiveType<Buffer> BOOLEAN = new BooleanType();

    public static final PrimitiveType<ByteBuffer> BYTE = new ByteType();

    public static final PrimitiveType<CharBuffer> CHAR = new CharType();

    public static final PrimitiveType<DoubleBuffer> DOUBLE = new DoubleType();

    public static final PrimitiveType<FloatBuffer> FLOAT = new FloatType();

    public static final PrimitiveType<IntBuffer> INT = new IntType();

    public static final PrimitiveType<LongBuffer> LONG = new LongType();

    public static final PrimitiveType<ShortBuffer> SHORT = new ShortType();

    public static final PrimitiveType<Buffer> STRING = new StringType();

    public static final PrimitiveType<Buffer> UNKNOWN = new UnknownType();

    private PrimitiveTypes() {
        // TODO Auto-generated constructor stub
    }

}
