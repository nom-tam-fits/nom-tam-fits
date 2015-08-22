package nom.tam.image.comp.gzip2;

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

import java.io.IOException;
import java.nio.ByteBuffer;

import nom.tam.image.comp.gzip.GZipCompress;

public class Gzip2Compress extends GZipCompress {

    private final int elementSize;

    public Gzip2Compress(int elementSize) {
        super();
        this.elementSize = elementSize;
    }

    @Override
    public void compress(byte[] byteArray, ByteBuffer compressed) throws IOException {
        super.compress(shuffle(byteArray), compressed);
    }

    public byte[] shuffle(byte[] byteArray) {
        if (elementSize == 1) {
            return byteArray;
        }
        byte[] result = new byte[byteArray.length];
        if (elementSize == 2) {
            int resultIndex = 0;
            int offset = byteArray.length / 2;
            for (int index = 0; index < byteArray.length; index += 2) {
                result[resultIndex] = byteArray[index];
                result[resultIndex + offset] = byteArray[index + 1];
                resultIndex++;
            }
        }
        if (elementSize == 4) {
            int resultIndex = 0;
            int offset = byteArray.length / 4;
            int offset2 = offset * 2;
            int offset3 = offset * 3;
            for (int index = 0; index < byteArray.length; index += 4) {
                result[resultIndex] = byteArray[index];
                result[resultIndex + offset] = byteArray[index + 1];
                result[resultIndex + offset2] = byteArray[index + 2];
                result[resultIndex + offset3] = byteArray[index + 3];
                resultIndex++;
            }
        }
        return result;
    }

    @Override
    public void decompress(ByteBuffer buffer, byte[] decompressedArray) throws IOException {
        super.decompress(buffer, decompressedArray);
        byte[] result = unshuffle(decompressedArray);
        System.arraycopy(result, 0, decompressedArray, 0, decompressedArray.length);

    }

    public byte[] unshuffle(byte[] byteArray) {
        if (elementSize == 1) {
            return byteArray;
        }
        byte[] result = new byte[byteArray.length];
        if (elementSize == 2) {
            int resultIndex = 0;
            int offset = byteArray.length / 2;
            for (int index = 0; index < byteArray.length; index += 2) {
                result[index] = byteArray[resultIndex];
                result[index + 1] = byteArray[resultIndex + offset];
                resultIndex++;
            }
        }
        if (elementSize == 4) {
            int resultIndex = 0;
            int offset = byteArray.length / 4;
            int offset2 = offset * 2;
            int offset3 = offset * 3;
            for (int index = 0; index < byteArray.length; index += 4) {
                result[index] = byteArray[resultIndex];
                result[index + 1] = byteArray[resultIndex + offset];
                result[index + 2] = byteArray[resultIndex + offset2];
                result[index + 3] = byteArray[resultIndex + offset3];
                resultIndex++;
            }
        }
        return result;
    }
}
