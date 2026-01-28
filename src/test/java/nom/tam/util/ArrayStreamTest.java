package nom.tam.util;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArrayStreamTest {

    @Test
    public void testPublicInputConstructor() throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream(100);
        ArrayOutputStream o = new ArrayOutputStream(bo, 100, new FitsEncoder(OutputWriter.from(bo)));
        o.write(1);
        o.close();

        // All is good if wse got this far without an exception.
    }

    @Test
    public void testPublicOutputConstructors() throws Exception {
        ByteArrayInputStream bi = new ByteArrayInputStream(new byte[100]);
        ArrayInputStream i = new ArrayInputStream(bi, 100, new FitsDecoder(InputReader.from(bi)));
        i.read();
        i.close();

        // All is good if wse got this far without an exception.
    }

    @Test
    public void testReadArrayFully() throws Exception {
        Object[] array = {new byte[1], new boolean[1], new char[1], new short[1], new int[1], new long[1], new float[1],
                new double[1]};
        ByteArrayInputStream bi = new ByteArrayInputStream(new byte[100]);
        ArrayInputStream i = new ArrayInputStream(bi, 100, new FitsDecoder(InputReader.from(bi)));
        i.readArrayFully(array);
    }

    @Test
    public void testReadImageWrongType() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            Object[] array = {new byte[1], new boolean[1], new char[1], new short[1], new int[1], new long[1], new float[1],
                    new double[1]};
            ByteArrayInputStream bi = new ByteArrayInputStream(new byte[100]);
            ArrayInputStream i = new ArrayInputStream(bi, 100, new FitsDecoder(InputReader.from(bi)));
            // boolean[] and char[] not supported by readImage...
            i.readImage(array);

        });
    }

}
