package nom.tam.fits.compression.algorithm.quant;

import java.nio.Buffer;
import java.nio.BufferOverflowException;

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

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import nom.tam.fits.compression.algorithm.api.ICompressor;

/**
 * (<i>for internal use</i>) Qunatization step processor as part of compression.
 */
@SuppressWarnings({"javadoc", "deprecation"})
public class QuantizeProcessor {

    public static class DoubleQuantCompressor extends QuantizeProcessor implements ICompressor<DoubleBuffer> {
        public DoubleQuantCompressor(QuantizeOption quantizeOption, ICompressor<IntBuffer> compressor) {
            super(quantizeOption, compressor);
        }

        @Override
        public boolean compress(DoubleBuffer buffer, ByteBuffer compressed) {
            return super.compressGeneric(buffer, compressed);
        }

        @Override
        public void decompress(ByteBuffer compressed, DoubleBuffer buffer) {
            super.decompressGeneric(compressed, buffer);
        }
    }

    /**
     * TODO this is done very inefficient and should be refactored!
     */
    public static class FloatQuantCompressor extends QuantizeProcessor implements ICompressor<FloatBuffer> {
        public FloatQuantCompressor(QuantizeOption quantizeOption, ICompressor<IntBuffer> compressor) {
            super(quantizeOption, compressor);
        }

        @Override
        public boolean compress(FloatBuffer buffer, ByteBuffer compressed) {
            return super.compressGeneric(buffer, compressed);
        }

        @Override
        public void decompress(ByteBuffer compressed, FloatBuffer buffer) {
            super.decompressGeneric(compressed, buffer);
        }
    }

    private Quantize quantize;

    protected final QuantizeOption quantizeOption;

    private final ICompressor<IntBuffer> postCompressor;

    public QuantizeProcessor(QuantizeOption quantizeOption, ICompressor<IntBuffer> compressor) {
        this.quantizeOption = quantizeOption;
        this.postCompressor = compressor;

        if (quantizeOption.isDither2()) {
            quantizeOption.setCenterOnZero(true);
            quantizeOption.setCheckZero(true);
        }
        quantize = new Quantize(quantizeOption);
    }

    protected QuantizeProcessor(QuantizeOption quantizeOption) {
        this(quantizeOption, null);
    }

    public Quantize getQuantize() {
        return quantize;
    }

    protected boolean compressGeneric(Buffer buffer, ByteBuffer compressed) throws BufferOverflowException {
        IntBuffer intData = IntBuffer.wrap(new int[quantizeOption.getTileHeight() * quantizeOption.getTileWidth()]);
        if (!autoQuantize(buffer, intData)) {
            return false;
        }
        intData.rewind();
        postCompressor.compress(intData, compressed);
        return true;
    }

    protected <BufferType extends Buffer> void decompressGeneric(ByteBuffer compressed, BufferType buffer)
            throws BufferOverflowException {
        IntBuffer intData = IntBuffer.wrap(new int[quantizeOption.getTileHeight() * quantizeOption.getTileWidth()]);
        postCompressor.decompress(compressed, intData);
        intData.rewind();
        unquantizeGeneric(intData, buffer);
    }

    /**
     * Quantizes floating-point data into integer representation. The quantization parameters are determined
     * automatically based on the noise distribution of the input, and are stored in the qunatization options associated
     * to this class.
     * 
     * @param  floating floating-point input
     * @param  quants   quantized output
     * 
     * @return          <code>true</code> if quantization parameters were determined, otherwise <code>false</code>.
     * 
     * @since           1.23
     */
    protected boolean autoQuantize(Buffer floating, IntBuffer quants) {
        boolean success = quantize.guessQuantization(floating);
        if (quants != null) {
            quantizeGeneric(floating, quants);
        }
        return success;
    }

    /**
     * @deprecated         use {@link #autoQuantize(DoubleBuffer, IntBuffer)} instead.
     * 
     * @param      doubles input array of doubles
     * @param      quants  output array of integer quantized data
     * 
     * @return             <code>true</code> if successful, or else <code>false</code>.
     */
    @Deprecated
    public boolean quantize(double[] doubles, IntBuffer quants) {
        return autoQuantize(DoubleBuffer.wrap(doubles, 0, quantizeOption.getTileWidth() * quantizeOption.getTileHeight()),
                quants);
    }

    private void quantizeGeneric(final Buffer fdata, final IntBuffer intData) throws BufferOverflowException {
        if (fdata instanceof FloatBuffer) {
            quantize((FloatBuffer) fdata, intData);
        } else {
            quantize((DoubleBuffer) fdata, intData);
        }
    }

    private void unquantizeGeneric(final IntBuffer intData, final Buffer fdata) throws BufferOverflowException {
        if (fdata instanceof FloatBuffer) {
            unquantize(intData, (FloatBuffer) fdata);
        } else {
            unquantize(intData, (DoubleBuffer) fdata);
        }
    }

    /**
     * Converts floating point values into quantized integer representation.
     * 
     * @param  fdata                   floating-point input
     * @param  intData                 quantized integer output
     * 
     * @throws BufferOverflowException if the output buffer is smaller than the input buffer.
     * 
     * @since                          1.23
     */
    protected void quantize(final FloatBuffer fdata, final IntBuffer intData) throws BufferOverflowException {
        quantizeOption.initDither();
        while (fdata.hasRemaining()) {
            intData.put(quantizeOption.toInt(fdata.get()));
        }
    }

    /**
     * Converts quantized integers back into the floating point values they represent.
     * 
     * @param  intData                 quantized integer input
     * @param  fdata                   floating-point output
     * 
     * @throws BufferOverflowException if the output buffer is smaller than the input buffer.
     * 
     * @since                          1.23
     */
    protected void unquantize(final IntBuffer intData, final DoubleBuffer fdata) throws BufferOverflowException {
        quantizeOption.initDither();
        while (fdata.hasRemaining()) {
            fdata.put((float) quantizeOption.toDouble(intData.get()));
        }
    }

    /**
     * Converts floating point values into quantized integer representation.
     * 
     * @param  fdata                   floating-point input
     * @param  intData                 quantized integer output
     * 
     * @throws BufferOverflowException if the output buffer is smaller than the input buffer.
     * 
     * @since                          1.23
     */
    protected void quantize(final DoubleBuffer fdata, final IntBuffer intData) throws BufferOverflowException {
        quantizeOption.initDither();
        while (fdata.hasRemaining()) {
            intData.put(quantizeOption.toInt(fdata.get()));
        }
    }

    /**
     * Converts quantized integers back into the floating point values they represent.
     * 
     * @param  intData                 quantized integer input
     * @param  fdata                   floating-point output
     * 
     * @throws BufferOverflowException if the output buffer is smaller than the input buffer.
     * 
     * @since                          1.23
     */
    protected void unquantize(final IntBuffer intData, final FloatBuffer fdata) throws BufferOverflowException {
        quantizeOption.initDither();
        while (fdata.hasRemaining()) {
            fdata.put((float) quantizeOption.toDouble(intData.get()));
        }
    }

}
