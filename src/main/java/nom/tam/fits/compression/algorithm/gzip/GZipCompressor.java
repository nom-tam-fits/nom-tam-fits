package nom.tam.fits.compression.algorithm.gzip;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import nom.tam.fits.compression.algorithm.api.ICompressor;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.ByteBufferInputStream;
import nom.tam.util.ByteBufferOutputStream;
import nom.tam.util.FitsIO;
import nom.tam.util.SaveClose;
import nom.tam.util.type.PrimitiveType;
import nom.tam.util.type.PrimitiveTypeHandler;
import nom.tam.util.type.PrimitiveTypes;

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

public abstract class GZipCompressor<T extends Buffer> implements ICompressor<T> {

    /**
     * Byte compress is a special case, the only one that does not extends
     * GZipCompress because it can write the buffer directly.
     */
    public static class ByteGZipCompressor extends GZipCompressor<ByteBuffer> {

        public ByteGZipCompressor() {
            super(1);
            this.nioBuffer = ByteBuffer.wrap(this.buffer);
        }

        @Override
        protected void getPixel(ByteBuffer pixelData, byte[] pixelBytes) {
            this.nioBuffer.put(pixelData);
        }

        @Override
        protected void setPixel(ByteBuffer pixelData, byte[] pixelBytes) {
            pixelData.put(this.nioBuffer);
        }
    }

    public static class DoubleGZipCompressor extends GZipCompressor<DoubleBuffer> {

        protected static final int BYTE_SIZE_OF_DOUBLE = 8;

        public DoubleGZipCompressor() {
            super(BYTE_SIZE_OF_DOUBLE);
            this.nioBuffer = ByteBuffer.wrap(this.buffer).asDoubleBuffer();
        }

        @Override
        protected void getPixel(DoubleBuffer pixelData, byte[] pixelBytes) {
            this.nioBuffer.put(pixelData);
        }

        @Override
        protected void setPixel(DoubleBuffer pixelData, byte[] pixelBytes) {
            pixelData.put(this.nioBuffer);
        }
    }

    public static class FloatGZipCompressor extends GZipCompressor<FloatBuffer> {

        protected static final int BYTE_SIZE_OF_FLOAT = 4;

        public FloatGZipCompressor() {
            super(BYTE_SIZE_OF_FLOAT);
            this.nioBuffer = ByteBuffer.wrap(this.buffer).asFloatBuffer();
        }

        @Override
        protected void getPixel(FloatBuffer pixelData, byte[] pixelBytes) {
            this.nioBuffer.put(pixelData);
        }

        @Override
        protected void setPixel(FloatBuffer pixelData, byte[] pixelBytes) {
            pixelData.put(this.nioBuffer);
        }
    }

    public static class IntGZipCompressor extends GZipCompressor<IntBuffer> {

        protected static final int BYTE_SIZE_OF_INT = 4;

        public IntGZipCompressor() {
            super(BYTE_SIZE_OF_INT);
            this.nioBuffer = ByteBuffer.wrap(this.buffer).asIntBuffer();
        }

        @Override
        protected void getPixel(IntBuffer pixelData, byte[] pixelBytes) {
            this.nioBuffer.put(pixelData);
        }

        @Override
        protected void setPixel(IntBuffer pixelData, byte[] pixelBytes) {
            pixelData.put(this.nioBuffer);
        }
    }

    public static class LongGZipCompressor extends GZipCompressor<LongBuffer> {

        protected static final int BYTE_SIZE_OF_LONG = 8;

        public LongGZipCompressor() {
            super(BYTE_SIZE_OF_LONG);
            this.nioBuffer = ByteBuffer.wrap(this.buffer).asLongBuffer();
        }

        @Override
        protected void getPixel(LongBuffer pixelData, byte[] pixelBytes) {
            this.nioBuffer.put(pixelData);
        }

        @Override
        protected void setPixel(LongBuffer pixelData, byte[] pixelBytes) {
            pixelData.put(this.nioBuffer);
        }
    }

    public static class ShortGZipCompressor extends GZipCompressor<ShortBuffer> {

        protected static final int BYTE_SIZE_OF_SHORT = 2;

        public ShortGZipCompressor() {
            super(BYTE_SIZE_OF_SHORT);
            this.nioBuffer = ByteBuffer.wrap(this.buffer).asShortBuffer();
        }

        @Override
        protected void getPixel(ShortBuffer pixelData, byte[] pixelBytes) {
            this.nioBuffer.put(pixelData);
        }

        @Override
        protected void setPixel(ShortBuffer pixelData, byte[] pixelBytes) {
            pixelData.put(this.nioBuffer);
        }
    }

    private final class TypeConversion<B extends Buffer> {

        private final PrimitiveType<B> from;

        private final PrimitiveType<T> to;

        private final B fromBuffer;

        private final T toBuffer;

        private final Object fromArray;

        private final Object toArray;

        private TypeConversion(PrimitiveType<B> from) {
            this.from = from;
            this.to = getPrimitiveType(GZipCompressor.this.primitiveSize);
            this.toBuffer = GZipCompressor.this.nioBuffer;
            this.fromBuffer = from.asTypedBuffer(ByteBuffer.wrap(GZipCompressor.this.buffer));
            this.fromArray = from.newArray(DEFAULT_GZIP_BUFFER_SIZE / from.size());
            this.toArray = this.to.newArray(DEFAULT_GZIP_BUFFER_SIZE / this.to.size());
        }

        int copy(int byteCount) {
            this.fromBuffer.rewind();
            this.toBuffer.rewind();
            this.from.getArray(this.fromBuffer, this.fromArray);
            ArrayFuncs.copyInto(this.fromArray, this.toArray);
            this.to.putArray(this.toBuffer, this.toArray);
            return byteCount * this.to.size() / this.from.size();
        }
    }

    private static final int DEFAULT_GZIP_BUFFER_SIZE = 65536;

    protected final int primitiveSize;

    protected byte[] buffer = new byte[DEFAULT_GZIP_BUFFER_SIZE];

    protected T nioBuffer;

    private final byte[] sizeArray = new byte[PrimitiveTypes.INT.size()];

    private final IntBuffer sizeBuffer = ByteBuffer.wrap(this.sizeArray).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();

    public GZipCompressor(int primitiveSize) {
        this.primitiveSize = primitiveSize;
    }

    @Override
    public boolean compress(T pixelData, ByteBuffer compressed) {
        this.nioBuffer.rewind();
        int pixelDataLimit = pixelData.limit();
        GZIPOutputStream zip = null;
        try {
            zip = createGZipOutputStream(pixelDataLimit, compressed);
            while (pixelData.hasRemaining()) {
                int count = Math.min(pixelData.remaining(), this.nioBuffer.capacity());
                pixelData.limit(pixelData.position() + count);
                getPixel(pixelData, null);
                zip.write(this.buffer, 0, this.nioBuffer.position() * this.primitiveSize);
                this.nioBuffer.rewind();
                pixelData.limit(pixelDataLimit);
            }
        } catch (IOException e) {
            throw new IllegalStateException("could not gzip data", e);
        } finally {
            SaveClose.close(zip);
        }
        compressed.limit(compressed.position());
        return true;
    }

    @Override
    public void decompress(ByteBuffer compressed, T pixelData) {
        this.nioBuffer.rewind();
        TypeConversion<Buffer> typeConverter = getTypeConverter(compressed, pixelData.limit());
        GZIPInputStream zip = null;
        try {
            zip = createGZipInputStream(compressed);
            int count;
            while ((count = zip.read(this.buffer)) >= 0) {
                if (typeConverter != null) {
                    count = typeConverter.copy(count);
                }
                this.nioBuffer.position(0);
                this.nioBuffer.limit(count / this.primitiveSize);
                setPixel(pixelData, null);
            }
        } catch (IOException e) {
            throw new IllegalStateException("could not gunzip data", e);
        } finally {
            SaveClose.close(zip);
        }
    }

    @SuppressWarnings("unchecked")
    private <B extends Buffer> PrimitiveType<B> getPrimitiveType(int size) {
        return (PrimitiveType<B>) PrimitiveTypeHandler.valueOf(size * FitsIO.BITS_OF_1_BYTE);
    }

    private TypeConversion<Buffer> getTypeConverter(ByteBuffer compressed, int nrOfPrimitiveElements) {
        if (compressed.limit() > FitsIO.BYTES_IN_INTEGER) {
            int oldPosition = compressed.position();
            try {
                compressed.position(compressed.limit() - this.sizeArray.length);
                compressed.get(this.sizeArray);
                int uncompressedSize = this.sizeBuffer.get(0);
                if (uncompressedSize > 0) {
                    compressed.position(oldPosition);
                    if (uncompressedSize % nrOfPrimitiveElements == 0) {
                        int compressedPrimitiveSize = uncompressedSize / nrOfPrimitiveElements;
                        if (compressedPrimitiveSize != this.primitiveSize) {
                            return new TypeConversion<Buffer>(getPrimitiveType(compressedPrimitiveSize));
                        }
                    }
                }
            } finally {
                compressed.position(oldPosition);
            }
        }
        return null;
    }

    protected GZIPInputStream createGZipInputStream(ByteBuffer compressed) throws IOException {
        return new GZIPInputStream(new ByteBufferInputStream(compressed), Math.min(compressed.limit() * 2, DEFAULT_GZIP_BUFFER_SIZE));
    }

    protected GZIPOutputStream createGZipOutputStream(int length, ByteBuffer compressed) throws IOException {
        return new GZIPOutputStream(new ByteBufferOutputStream(compressed), Math.min(length * 2, DEFAULT_GZIP_BUFFER_SIZE));
    }

    protected abstract void getPixel(T pixelData, byte[] pixelBytes);

    protected abstract void setPixel(T pixelData, byte[] pixelBytes);

}
