package nom.tam.fits.compression.algorithm.quant;

import nom.tam.fits.compression.algorithm.api.ICompressOption;
import nom.tam.fits.compression.provider.param.api.ICompressParameters;
import nom.tam.fits.compression.provider.param.base.BundledParameters;
import nom.tam.fits.compression.provider.param.quant.QuantizeParameters;

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

/**
 * Quantization options when they are part of the compression scheme. When compressing tables and images includes
 * quantization (integer representation of floating point data), users can control how exactly the quantization should
 * be performed. When reading compressed FITS files, these options will be set automatically based on the header values
 * recorded in the compressed HDU.
 * 
 * @see nom.tam.image.compression.hdu.CompressedImageHDU#setQuantAlgorithm(String)
 * @see nom.tam.image.compression.hdu.CompressedImageHDU#getCompressOption(Class)
 */
public class QuantizeOption implements ICompressOption {

    /**
     * and including NULL_VALUE. These values may not be used to represent the quantized and scaled floating point pixel
     * values If lossy Hcompression is used, and the tiledImageOperation contains null values, then it is also possible
     * for the compressed values to slightly exceed the range of the actual (lossless) values so we must reserve a
     * little more space value used to represent undefined pixels
     */
    private static final int NULL_VALUE = Integer.MIN_VALUE + 1;

    /** The prarameters that represent settings for thsi option in the FITS headers and/or compressed data columns */
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
     * Creates a new set of quantization options, to be used together with the specified compression options.
     *
     * @param compressOption Compression-specific options to pair with these quantization options, or <code>null</code>.
     *
     * @since                1.18
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
            copy.parameters = parameters.copy(copy);
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("option could not be cloned", e);
        }
    }

    /**
     * Returns the integer value that represents missing (<code>null</code>) data in the quantized representation.
     * 
     * @return the integer blanking value (<code>null</code> value).
     * 
     * @see    #setBNull(Integer)
     */
    public Integer getBNull() {
        return nullValueIndicator;
    }

    /**
     * Returns the quantization level.
     * 
     * @return the floating-point difference between integer levels in the quantized data.
     * 
     * @see    #setBScale(double)
     * @see    #getBZero()
     */
    public double getBScale() {
        return bScale;
    }

    /**
     * Returns the quantization offset.
     * 
     * @return the floating-point value corresponding to the integer level 0.
     * 
     * @see    #setBZero(double)
     * @see    #getBScale()
     */
    public double getBZero() {
        return bZero;
    }

    @Override
    public ICompressParameters getCompressionParameters() {
        if (compressOption == null) {
            return parameters;
        }
        return new BundledParameters(parameters, compressOption.getCompressionParameters());
    }

    /**
     * Returns the compression or quantization options, recast for the selected option class.
     * 
     * @param  <T>   the generic type of the compression option
     * @param  clazz the option class for the compression algorithm used with the quantization, or
     *                   <code>QunatizeOption.class</code> for our own options.
     * 
     * @return       the recast options for the requested class or <code>null</code> id we do not have access to options
     *                   of the requested class.
     * 
     * @see          #getCompressOption()
     */
    public <T> T getCompressOption(Class<T> clazz) {
        return unwrap(clazz);
    }

    /**
     * Returns the options for the compression algorithm that accompanies quantization.
     * 
     * @return the options for the compression algorithm, or <code>null</code>
     * 
     * @see    #getCompressOption(Class)
     */
    public final ICompressOption getCompressOption() {
        return compressOption;
    }

    /**
     * Returns the maximum integer level in the quantized representation.
     * 
     * @return the maximum integer level in the quantized data.
     * 
     * @see    #getMaxValue()
     * @see    #getIntMinValue()
     */
    public int getIntMaxValue() {
        return intMaxValue;
    }

    /**
     * Returns the maximum integer level in the quantized representation.
     * 
     * @return the maximum integer level in the quantized data.
     * 
     * @see    #getMinValue()
     * @see    #getIntMinValue()
     */
    public int getIntMinValue() {
        return intMinValue;
    }

    /**
     * Returns the maximum floating-point value in the data
     * 
     * @return the maximum floating-point value in the data before quantization.
     * 
     * @see    #getIntMaxValue()
     * @see    #getMinValue()
     */
    public double getMaxValue() {
        return maxValue;
    }

    /**
     * Returns the minimum floating-point value in the data
     * 
     * @return the minimum floating-point value in the data before quantization.
     * 
     * @see    #getIntMinValue()
     * @see    #getMaxValue()
     */
    public double getMinValue() {
        return minValue;
    }

    /**
     * Returns the floating-point value that indicates a <code>null</code> datum in the image before quantization is
     * applied. Normally, the FITS standard is that NaN values indicate <code>null</code> values in floating-point
     * images. While this class allows using other values also, they are not recommended since they are not supported by
     * FITS in a standard way.
     * 
     * @return the floating-point value that represents a <code>null</code> value (missing data) in the image before
     *             quantization.
     * 
     * @see    #setNullValue(double)
     * @see    #getNullValueIndicator()
     * @see    #isCheckNull()
     */
    public double getNullValue() {
        return nullValue;
    }

    /**
     * @deprecated use {@link #getBNull()} instead (duplicate method). Returns the integer value that represents missing
     *                 data (<code>null</code>) in the quantized representation.
     * 
     * @return     the integer blanking value (<code>null</code> value).
     * 
     * @see        #setBNull(Integer)
     */
    public final Integer getNullValueIndicator() {
        return getBNull();
    }

    /**
     * Returns the quantization resolution level used for automatic qunatization. For Gaussian noise the quantization
     * level is the standard deviation of the noise divided by this Q value. Thus Q values of a few will ensure that
     * quantization retains just about all of the information in the noisy data.
     * 
     * @return The current Q value, defined as the number of quantized levels per standard deviation (for Gaussian
     *             noise).
     * 
     * @see    #setQlevel(double)
     * @see    #getBScale()
     */
    public double getQLevel() {
        return qlevel;
    }

    /**
     * Gets the random seed value used for dithering
     * 
     * @return the random seed value used for dithering
     * 
     * @see    #setSeed(long)
     * @see    RandomSequence
     */
    public long getSeed() {
        return seed;
    }

    /**
     * Returns the sequential tile index that this option is currently configured for.
     * 
     * @return the sequential tile index that the quantization is configured for
     * 
     * @see    #setTileIndex(int)
     */
    public long getTileIndex() {
        return tileIndex;
    }

    /**
     * Returns the tile height
     * 
     * @return the tile height in pixels
     * 
     * @see    #setTileHeight(int)
     * @see    #getTileWidth()
     */
    @Override
    public int getTileHeight() {
        return tileHeight;
    }

    /**
     * Returns the tile width
     * 
     * @return the tile width in pixels
     * 
     * @see    #setTileWidth(int)
     * @see    #getTileHeight()
     */
    @Override
    public int getTileWidth() {
        return tileWidth;
    }

    /**
     * Checks whether we force the integer quantized level 0 to correspond to a floating-point level 0.0, when using
     * automatic quantization.
     * 
     * @return <code>true</code> if we want to keep `BZERO` at 0 when quantizing automatically.
     * 
     * @see    #setCenterOnZero(boolean)
     */
    public boolean isCenterOnZero() {
        return centerOnZero;
    }

    /**
     * Whether the floating-point data may contain <code>null</code> values (normally NaNs).
     * 
     * @return <code>true</code> if we should expect <code>null</code> in the floating-point data. This is automatically
     *             <code>true</code> if {@link #setBNull(Integer)} was called with a non-null value.
     * 
     * @see    #setBNull(Integer)
     */
    public boolean isCheckNull() {
        return checkNull;
    }

    /**
     * Whether automatic quantization treats 0.0 as a special value. Normally values within the `BSCALE` quantization
     * level around 0.0 will be assigned the same integer quanta, and will become indistinguishable in the quantized
     * data. Some software may, in their misguided ways, assign exact zero values a special meaning (such as no data) in
     * which case we may want to distinguish these as we apply quantization. However, it is generally not a good idea to
     * use 0 as a special value.
     * 
     * @return <code>true</code> to treat 0.0 (exact) as a special value, or <code>false</code> to treat is as any other
     *             measured value (recommended).
     * 
     * @see    #setCheckZero(boolean)
     * @see    #getBScale()
     */
    public boolean isCheckZero() {
        return checkZero;
    }

    /**
     * Whether dithering is enabled
     * 
     * @return <code>true</code> if dithering is enabled, or else <code>false</code>
     * 
     * @see    #setDither(boolean)
     * @see    #isDither2()
     */
    public boolean isDither() {
        return dither;
    }

    /**
     * Whether dither method 2 is used.
     * 
     * @return <code>true</code> if dither method 2 is used, or else <code>false</code>
     * 
     * @see    #setDither2(boolean)
     * @see    #isDither()
     */
    public boolean isDither2() {
        return dither2;
    }

    @Override
    public boolean isLossyCompression() {
        return true;
    }

    /**
     * Sets the integer value that represents missing data (<code>null</code>) in the quantized representation.
     * 
     * @param  blank the new integer blanking value (that is one that denotes a missing or <code>null</code> datum).
     *                   Setting this option to <code>null</code> disables the treatment of issing or <code>null</code>
     *                   data.
     * 
     * @return       itself
     * 
     * @see          #getBNull()
     * @see          #isCheckNull()
     */
    public ICompressOption setBNull(Integer blank) {
        if (blank != null) {
            nullValueIndicator = blank;
            checkNull = true;
        } else {
            checkNull = false;
        }
        return this;
    }

    /**
     * Sets the quantization level.
     * 
     * @param  value the new floating-point difference between integer levels in the quantized data.
     * 
     * @return       itself
     * 
     * @see          #setQlevel(double)
     * @see          #setBZero(double)
     * @see          #getBScale()
     */
    public QuantizeOption setBScale(double value) {
        bScale = value;
        return this;
    }

    /**
     * Sets the quantization offset.
     * 
     * @param  value the new floating-point value corresponding to the integer level 0.
     * 
     * @return       itself
     * 
     * @see          #setBScale(double)
     * @see          #getBZero()
     */
    public QuantizeOption setBZero(double value) {
        bZero = value;
        return this;
    }

    /**
     * Enabled or disables keeping `BZERO` at 0 when using automatic quantization.
     * 
     * @param  value <code>true</code> to keep `BZERO` at 0 when quantizing automatically, that is keep the integer
     *                   quantized level 0 correspond to floating-point level 0.0. Or, <code>false</code> to let the
     *                   automatic quantization algorithm determine the optimal quantization offset.
     * 
     * @return       iftself
     * 
     * @see          #isCenterOnZero()
     */
    public QuantizeOption setCenterOnZero(boolean value) {
        centerOnZero = value;
        return this;
    }

    /**
     * @deprecated       {@link #setBNull(Integer)} controls this feature automatically as needed. Sets whether we
     *                       should expect the floating-point data to contain <code>null</code> values (normally NaNs).
     * 
     * @param      value <code>true</code> if the floating-point data may contain <code>null</code> values.
     * 
     * @return           itself
     * 
     * @see              #setCheckNull(boolean)
     * @see              #setBNull(Integer)
     * @see              #getNullValue()
     */
    public QuantizeOption setCheckNull(boolean value) {
        checkNull = value;
        if (nullValueIndicator == null) {
            nullValueIndicator = NULL_VALUE;
        }
        return this;
    }

    /**
     * Sets whether automatic quantization is to treat 0.0 as a special value. Normally values within the `BSCALE`
     * quantization level around 0.0 will be assigned the same integer quanta, and will become indistinguishable in the
     * quantized data. However some software may assign exact zero values a special meaning (such as no data) in which
     * case we may want to distinguish these as we apply qunatization. However, it is generally not a good idea to use 0
     * as a special value. To mark missing data, the FITS standard recognises only NaN as a special value -- while all
     * other values should constitute valid measurements.
     * 
     * @deprecated       It is strongly discouraged to treat 0.0 values as special. FITS only recognises NaN as a
     *                       special floating-point value marking missing data. All other floating point values are
     *                       considered valid measurements.
     * 
     * @param      value
     * 
     * @return           itself
     * 
     * @see              #isCheckZero()
     */
    public QuantizeOption setCheckZero(boolean value) {
        checkZero = value;
        return this;
    }

    /**
     * Enables or disables dithering.
     * 
     * @param  value <code>true</code> to enable dithering, or else <code>false</code> to disable
     * 
     * @return       itself
     * 
     * @see          #isDither()
     * @see          #setDither2(boolean)
     */
    public QuantizeOption setDither(boolean value) {
        dither = value;
        return this;
    }

    /**
     * Sets whether dithering is to use method 2.
     * 
     * @param  value <code>true</code> to use dither method 2, or else <code>false</code> for method 1.
     * 
     * @return       itself
     * 
     * @see          #isDither2()
     * @see          #setDither(boolean)
     */
    public QuantizeOption setDither2(boolean value) {
        dither2 = value;
        return this;
    }

    /**
     * Sets the maximum integer level in the quantized representation.
     * 
     * @param  value the new maximum integer level in the quantized data.
     * 
     * @return       itself
     * 
     * @see          #getIntMaxValue()
     * @see          #setIntMinValue(int)
     */
    public QuantizeOption setIntMaxValue(int value) {
        intMaxValue = value;
        return this;
    }

    /**
     * Sets the minimum integer level in the quantized representation.
     * 
     * @param  value the new minimum integer level in the quantized data.
     * 
     * @return       itself
     * 
     * @see          #getIntMinValue()
     * @see          #setIntMaxValue(int)
     */
    public QuantizeOption setIntMinValue(int value) {
        intMinValue = value;
        return this;
    }

    /**
     * Sets the maximum floating-point value in the data
     * 
     * @param  value the maximum floating-point value in the data before quantization.
     * 
     * @return       itself
     * 
     * @see          #getMaxValue()
     * @see          #setMinValue(double)
     */
    public QuantizeOption setMaxValue(double value) {
        maxValue = value;
        return this;
    }

    /**
     * Sets the minimum floating-point value in the data
     * 
     * @param  value the mininum floating-point value in the data before quantization.
     * 
     * @return       itself
     * 
     * @see          #getMinValue()
     * @see          #setMaxValue(double)
     */
    public QuantizeOption setMinValue(double value) {
        minValue = value;
        return this;
    }

    /**
     * @deprecated       The use of null values other than <code>NaN</code> for floating-point data types is not
     *                       standard in FITS. You should therefore avoid using this method to change it. Returns the
     *                       floating-point value that indicates a <code>null</code> datum in the image before
     *                       quantization is applied. Normally, the FITS standard is that NaN values indicate
     *                       <code>null</code> values in floating-point images. While this class allows using other
     *                       values also, they are not recommended since they are not supported by FITS in a standard
     *                       way.
     * 
     * @param      value the new floating-point value that represents a <code>null</code> value (missing data) in the
     *                       image before quantization.
     * 
     * @return           itself
     * 
     * @see              #setNullValue(double)
     */
    public QuantizeOption setNullValue(double value) {
        nullValue = value;
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

    /**
     * Sets the quantization resolution level to use for automatic quantization. For Gaussian noise the quantization
     * level is the standard deviation of the noise divided by this Q value. Thus Q values of a few will ensusre that
     * quantization retains just about all of the information contained in the noisy data.
     * 
     * @param  value The new Q value, defined as the number of quantized levels per standard deviation (for Gaussian
     *                   noise).
     * 
     * @return       itself
     * 
     * @see          #getQLevel()
     * @see          #setBScale(double)
     */
    public QuantizeOption setQlevel(double value) {
        qlevel = value;
        return this;
    }

    /**
     * Sets the seed value for the dither random generator
     *
     * @param  value The seed value, as in <code>ZDITHER0</code>, normally a number between 1 and 10000 (inclusive).
     *
     * @return       itself
     *
     * @see          #setTileIndex(int)
     */
    public QuantizeOption setSeed(long value) {
        seed = value;
        return this;
    }

    /**
     * Sets the tile index for which to initialize the random number generator with the given seed (i.e.
     * <code>ZDITHER0</code> value).
     *
     * @param  index The 0-based tile index
     *
     * @return       itself
     *
     * @see          #setSeed(long)
     */
    public QuantizeOption setTileIndex(int index) {
        tileIndex = index;
        return this;
    }

    @Override
    public QuantizeOption setTileHeight(int value) {
        tileHeight = value;
        if (compressOption != null) {
            compressOption.setTileHeight(value);
        }
        return this;
    }

    @Override
    public QuantizeOption setTileWidth(int value) {
        tileWidth = value;
        if (compressOption != null) {
            compressOption.setTileWidth(value);
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
