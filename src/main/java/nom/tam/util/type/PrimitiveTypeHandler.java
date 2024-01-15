package nom.tam.util.type;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2024 nom-tam-fits
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

/**
 * @deprecated Use equivalent static methods of {@link ElementType} instead.
 */
@Deprecated
public final class PrimitiveTypeHandler {

    /**
     * @deprecated      Use {@link ElementType#forDataID(char)} instead.
     *
     * @param      type the Java array type character ID. For example 'J' for long integers.
     *
     * @return          the corresponding FITS element type.
     */
    @Deprecated
    public static ElementType<Buffer> valueOf(char type) {
        return ElementType.forDataID(type);
    }

    /**
     * @deprecated       Use {@link ElementType#forClass(Class)} instead.
     *
     * @param      clazz the Java class of an element or an array thereof
     *
     * @return           the corresponding FITS element type.
     *
     * @param      <B>   the generic tpye of buffer used by the returned element type.
     */
    @Deprecated
    public static <B extends Buffer> ElementType<B> valueOf(Class<?> clazz) {
        return ElementType.forClass(clazz);
    }

    /**
     * @deprecated        Use {@link ElementType#forBitpix(int)} instead.
     *
     * @param      bitPix the FITS BITPIX value
     *
     * @return            the corresponding FITS element type.
     */
    @Deprecated
    public static ElementType<Buffer> valueOf(int bitPix) {
        return ElementType.forBitpix(bitPix);
    }

    /**
     * @deprecated        Use {@link ElementType#forNearestBitpix(int)} instead.
     *
     * @param      bitPix a FITS BITPIX value
     *
     * @return            the nearest corresponding FITS element type.
     */
    @Deprecated
    public static ElementType<Buffer> nearestValueOf(int bitPix) {
        return ElementType.forNearestBitpix(bitPix);
    }

    private PrimitiveTypeHandler() {
    }
}
