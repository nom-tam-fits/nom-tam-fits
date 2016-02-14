package nom.tam.fits.compression.provider;

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
import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import nom.tam.fits.compression.algorithm.api.ICompressOption;
import nom.tam.fits.compression.algorithm.api.ICompressorControl;
import nom.tam.fits.compression.algorithm.gzip2.GZip2Compressor.LongGZip2Compressor;
import nom.tam.fits.compression.provider.api.ICompressorProvider;

public class TileCompressorAlternativProvider implements ICompressorProvider {

    public static ICompressorControl createControl(Class<?> clazz) {
        return new CompressorProvider.TileCompressorControl(clazz);
    }

    @Override
    public ICompressorControl createCompressorControl(String quantAlgorithm, String compressionAlgorithm, Class<?> baseType) {
        if ("X".equalsIgnoreCase(compressionAlgorithm) && quantAlgorithm == null && baseType.equals(long.class)) {
            return new ICompressorControl() {

                @Override
                public boolean compress(Buffer in, ByteBuffer out, ICompressOption options) {
                    new LongGZip2Compressor().compress((LongBuffer) in, out);
                    return true;
                }

                @Override
                public void decompress(ByteBuffer in, Buffer out, ICompressOption options) {
                    new LongGZip2Compressor().decompress(in, (LongBuffer) out);
                }

                @Override
                public ICompressOption option() {
                    return null;
                }

            };
        } else if ("FAIL".equalsIgnoreCase(compressionAlgorithm) && quantAlgorithm == null) {
            return new ICompressorControl() {

                @Override
                public boolean compress(Buffer in, ByteBuffer out, ICompressOption options) {
                    return false;
                }

                @Override
                public void decompress(ByteBuffer in, Buffer out, ICompressOption options) {
                    throw new IllegalAccessError();
                }

                @Override
                public ICompressOption option() {
                    return null;
                }

            };
        }
        return null;
    }
}
