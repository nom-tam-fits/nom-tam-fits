package nom.tam.fits.compression.algorithm.quant;

import nom.tam.fits.compression.algorithm.api.ICompressOption;
import nom.tam.fits.compression.provider.param.api.ICompressParameters;
import nom.tam.fits.compression.provider.param.base.BundledParameters;
import nom.tam.fits.compression.provider.param.quant.QuantizeParameters;
import nom.tam.image.ITileOption;

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

public class QuantizeOption implements ICompressOption {

    /**
     * and including NULL_VALUE. These values may not be used to represent the quantized and scaled floating point pixel
     * values If lossy Hcompression is used, and the tiledImageOperation contains null values, then it is also possible
     * for the compressed values to slightly exceed the range of the actual (lossless) values so we must reserve a
     * little more space value used to represent undefined pixels
     */
    private static final int NULL_VALUE = Integer.MIN_VALUE + 1;

    protected QuantizeParameters parameters;

    private ICompressOption compressOption;

    private double bScale = Double.NaN;

    private double bZero = Double.NaN;

    private boolean centerOnZero;

    private boolean checkNull;

    private boolean checkZero;

    private boolean dither;

    private boolean dither2;

    private int intMaxValue;

    private int intMinValue;

    private double maxValue;

    private double minValue;

    private double nullValue = Double.NaN;

    private Integer nullValueIndicator;

    private double qlevel = Double.NaN;

    private long seed = 1L;

    private int tileIndex = 0;

    private int tileHeight;

    private int tileWidth;

    QuantizeOption() {
        this(null);
    }

    /**
     * Creates a new set of qunatization options, to be used together with the specified compression options.
     * 
     * @param compressOption Compression-specific options to pair with these quantization options, or <code>null</code>.
     * 
     * @since 1.18
     */
    public QuantizeOption(ICompressOption compressOption) {
        parameters = new QuantizeParameters(this);
        this.compressOption = compressOption;
    }

    @Override
    public QuantizeOption copy() {
        try {
            QuantizeOption copy = (QuantizeOption) clone();
            if (compressOption != null) {
                copy.compressOption = compressOption.copy();
            }
            copy.parameters = this.parameters.copy(copy);
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("option could not be cloned", e);
        }
    }

    public Integer getBNull() {
        return this.nullValueIndicator;
    }

    public double getBScale() {
        return this.bScale;
    }

    public double getBZero() {
        return this.bZero;
    }

    @Override
    public ICompressParameters getCompressionParameters() {
        if (compressOption == null) {
            return this.parameters;
        }
        return new BundledParameters(this.parameters, compressOption.getCompressionParameters());
    }

    public <T> T getCompressOption(Class<T> clazz) {
        return unwrap(clazz);
    }

    public final ICompressOption getCompressOption() {
        return compressOption;
    }

    public int getIntMaxValue() {
        return this.intMaxValue;
    }

    public int getIntMinValue() {
        return this.intMinValue;
    }

    public double getMaxValue() {
        return this.maxValue;
    }

    public double getMinValue() {
        return this.minValue;
    }

    public double getNullValue() {
        return this.nullValue;
    }

    public Integer getNullValueIndicator() {
        return this.nullValueIndicator;
    }

    public double getQLevel() {
        return this.qlevel;
    }

    public long getSeed() {
        return this.seed;
    }

    public long getTileIndex() {
        return this.tileIndex;
    }

    public int getTileHeight() {
        return this.tileHeight;
    }

    public int getTileWidth() {
        return this.tileWidth;
    }

    public boolean isCenterOnZero() {
        return this.centerOnZero;
    }

    public boolean isCheckNull() {
        return this.checkNull;
    }

    public boolean isCheckZero() {
        return this.checkZero;
    }

    public boolean isDither() {
        return this.dither;
    }

    public boolean isDither2() {
        return this.dither2;
    }

    @Override
    public boolean isLossyCompression() {
        return true;
    }

    public ICompressOption setBNull(Integer blank) {
        if (blank != null) {
            this.checkNull = true;
            this.nullValueIndicator = blank;
        }
        return this;
    }

    public QuantizeOption setBScale(double value) {
        this.bScale = value;
        return this;
    }

    public QuantizeOption setBZero(double value) {
        this.bZero = value;
        return this;
    }

    public QuantizeOption setCenterOnZero(boolean value) {
        this.centerOnZero = value;
        return this;
    }

    public QuantizeOption setCheckNull(boolean value) {
        this.checkNull = value;
        if (this.nullValueIndicator == null) {
            this.nullValueIndicator = NULL_VALUE;
        }
        return this;
    }

    public QuantizeOption setCheckZero(boolean value) {
        this.checkZero = value;
        return this;
    }

    public QuantizeOption setDither(boolean value) {
        this.dither = value;
        return this;
    }

    public QuantizeOption setDither2(boolean value) {
        this.dither2 = value;
        return this;
    }

    public QuantizeOption setIntMaxValue(int value) {
        this.intMaxValue = value;
        return this;
    }

    public QuantizeOption setIntMinValue(int value) {
        this.intMinValue = value;
        return this;
    }

    public QuantizeOption setMaxValue(double value) {
        this.maxValue = value;
        return this;
    }

    public QuantizeOption setMinValue(double value) {
        this.minValue = value;
        return this;
    }

    public QuantizeOption setNullValue(double value) {
        this.nullValue = value;
        return this;
    }

    @Override
    public void setParameters(ICompressParameters parameters) {
        if (parameters instanceof QuantizeParameters) {
            this.parameters = (QuantizeParameters) parameters.copy(this);
        } else if (parameters instanceof BundledParameters) {
            BundledParameters bundle = (BundledParameters) parameters;
            for (int i = 0; i < bundle.size(); i++) {
                setParameters(bundle.get(i));
            }
        } else if (compressOption != null) {
            compressOption.setParameters(parameters);
        }
    }

    public QuantizeOption setQlevel(double value) {
        this.qlevel = value;
        return this;
    }

    /**
     * Sets the seed value for the dither random generator
     * 
     * @param value The seed value, as in <code>ZDITHER0</code>, normally a number between 1 and 10000 (inclusive).
     * 
     * @return itself
     * 
     * @see #setTileIndex(int)
     */
    public QuantizeOption setSeed(long value) {
        this.seed = value;
        return this;
    }

    /**
     * Sets the tile index for which to initialize the random number generator with the given seed (i.e.
     * <code>ZDITHER0</code> value).
     * 
     * @param index The 0-based tile index
     * 
     * @return itself
     * 
     * @see #setSeed(long)
     */
    public QuantizeOption setTileIndex(int index) {
        this.tileIndex = index;
        return this;
    }

    @Override
    public QuantizeOption setTileHeight(int value) {
        this.tileHeight = value;
        if (compressOption instanceof ITileOption) {
            ((ITileOption) compressOption).setTileHeight(value);
        }
        return this;
    }

    @Override
    public QuantizeOption setTileWidth(int value) {
        this.tileWidth = value;
        if (compressOption instanceof ITileOption) {
            ((ITileOption) compressOption).setTileWidth(value);
        }
        return this;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.isAssignableFrom(this.getClass())) {
            return clazz.cast(this);
        }
        if (compressOption != null) {
            if (clazz.isAssignableFrom(compressOption.getClass())) {
                return clazz.cast(compressOption);
            }
        }
        return null;
    }
}
