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
import java.nio.ByteBuffer;

public interface PrimitiveType<B extends Buffer> {

    void appendBuffer(B buffer, B dataToAppend);

    void appendToByteBuffer(ByteBuffer byteBuffer, B dataToAppend);

    B asTypedBuffer(ByteBuffer buffer);

    int bitPix();

    Class<? extends B> bufferClass();

    ByteBuffer convertToByteBuffer(Object array);

    void getArray(B buffer, Object array);

    void getArray(B buffer, Object array, int length);

    void getArray(B buffer, Object array, int offset, int length);

    boolean individualSize();

    boolean is(PrimitiveType<? extends Buffer> d);

    Object newArray(int length);

    B newBuffer(int length);

    B newBuffer(long length);

    Class<?> primitiveClass();

    void putArray(B buffer, Object array);

    void putArray(B buffer, Object array, int length);

    int size();

    int size(Object instance);

    B sliceBuffer(B buffer);

    char type();

    B wrap(Object array);

    Class<?> wrapperClass();
}
