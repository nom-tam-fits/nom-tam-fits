package nom.tam.fits.compression.algorithm.quant;

import nom.tam.fits.compression.algorithm.api.ICompressOption;
import nom.tam.fits.compression.provider.param.api.ICompressParameters;
import nom.tam.fits.compression.provider.param.base.BundledParameters;
import nom.tam.fits.compression.provider.param.quant.QuantizeParameters;

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
     * The integer value recommeded by the FITS standard to represent NaN floating-point values in integer compressed
     * data.
     */
    private static final int RECOMMENDED_NAN_INDICATOR = Integer.MIN_VALUE;

    /**
     * value used to represent zero-valued pixels when dither method 2 is used.
     */
    private static final int DITHER2_ZERO_INDICATOR = Integer.MIN_VALUE + 1;

    private static boolean useFMA = false;

    /** Shared configuration across copies */
    private Config config;

    /** The parameters that represent settings for this option in the FITS headers and/or compressed data columns */
    protected QuantizeParameters parameters;

    private ICompressOption compressOption;

    private double bScale = Double.NaN;

    private double bZero = Double.NaN;

    private double nullValue = Double.NaN;

    private Integer nullValueIndicator;

    private int intMaxValue;

    private int intMinValue;

    private double maxValue;

    private double minValue;

    private int tileIndex = 0;

    private int tileHeight;

    private int tileWidth;

    /** Quantization constant */
    private static final int RANDOM_MULTIPLICATOR = 500;

    private static final double DITHER_HALF = 0.5;

    /**
     * Dither random seed value
     */
    private int iseed;

    /**
     * Next random dither value
     */
    private int nextRandom;

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
        config = new Config();
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
     * Returns the integer value that represents missing (<code>null</code>) for integer compressed floating-point data.
     * This funtion was named poorly as it sets the <code>ZBLANK</code> value in the header or in the equivalently named
     * column.
     * 
     * @return the integer blanking value (for integer-compressed <code>NaN</code>s). If the returned value is
     *             <code>null</code>, then the recommended value -2147483647 will be used as needed.
     * 
     * @see    #setBNull(Integer)
     */
    public Integer getBNull() {
        return nullValueIndicator;
    }

    /**
     * Returns the quantization level for integer compressed floating-point data. This funtion was named poorly as it
     * sets the <code>ZSCALE</code> parameter value in the named column when compressing floating-point data with an
     * algorithm that supports integers only. It has nothing to do with the <code>BSCALE</code> header value, which
     * indicates the integer representation of floating-point data for the <i>uncompressed</i> data.
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
     * Returns the quantization offset for integer compressed floating-point data. This funtion was named poorly as it
     * sets the <code>ZZERO</code> parameter value in the named column when compressing floating-point data with an
     * algorithm that supports integers only. It has nothing to do with the <code>BZERO</code> header value, which
     * indicates the integer representation of floating-point data for the <i>uncompressed</i> data.
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
     * Returns the floating-point value that indicates missing or invalid data in the image before quantization is
     * applied. Normally, the FITS standard is that NaN values indicate <code>null</code> values in floating-point
     * images. While this class allows using other values also, they are not recommended since they are not supported by
     * FITS in a standard way.
     * 
     * @return     the floating-point value that represents a <code>null</code> value (missing data) in the image before
     *                 quantization.
     * 
     * @see        #setNullValue(double)
     * @see        #getBNull()
     * 
     * @deprecated The FITS standard allows only NaNs to indicate missing / invalid floating-point data.
     */
    @Deprecated
    public double getNullValue() {
        return nullValue;
    }

    /**
     * @deprecated use {@link #getBNull()} instead (duplicate method). Returns the integer value that represents
     *                 <code>NaN</code> values in integer-compressed floating-point data.
     * 
     * @return     the integer blanking value (<code>null</code> value).
     * 
     * @see        #setBNull(Integer)
     */
    @Deprecated
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
        return config.qlevel;
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
        return config.seed;
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
     * @return <code>true</code> if we want to keep `ZZERO` at 0.0 when quantizing automatically.
     * 
     * @see    #setCenterOnZero(boolean)
     */
    public boolean isCenterOnZero() {
        return config.centerOnZero;
    }

    /**
     * Whether the floating-point data may contain <code>null</code> values (normally NaNs).
     * 
     * @return     <code>true</code> if we should expect <code>null</code> in the floating-point data. This is
     *                 automatically <code>true</code> if {@link #setBNull(Integer)} was called with a non-null value.
     * 
     * @see        #setBNull(Integer)
     * 
     * @deprecated Use {@link #getBNull()} instead to see if a custom null-value indicator has been configured.
     */
    @Deprecated
    public final boolean isCheckNull() {
        return true;
    }

    /**
     * Whether automatic quantization treats 0.0 as a special value. The special treatment of 0.0 values is the
     * distinguishing feature of dither method 2 over method 1.
     * 
     * @deprecated Use {@link #isDither2()} instead. The special treatent of ero values is the distinghuishing feature
     *                 of the <code>SUBTRACTIVE_DITHER_2</code> method, which is otherwise the same as
     *                 <code>SUBTRACTIVE_DITHER_1</code>.
     * 
     * @return     <code>true</code> to treat 0.0 (exact) as a special value, or <code>false</code> to treat is as any
     *                 other measured value (recommended).
     * 
     * @see        #isDither2()
     * @see        #setDither2(boolean)
     * @see        #getBScale()
     */
    @Deprecated
    public boolean isCheckZero() {
        return config.checkZero;
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
        return config.dither;
    }

    /**
     * Whether dithering (when enabled) uses method 2, which treats 0.0 values as special.
     * 
     * @return <code>true</code> if method 2 is used is used for dithering, or else <code>false</code>
     * 
     * @see    #setDither2(boolean)
     * @see    #isDither()
     */
    public boolean isDither2() {
        return config.checkZero;
    }

    @Override
    public boolean isLossyCompression() {
        return true;
    }

    /**
     * Sets the integer value that represents missing data (<code>null</code>) for integer compressed floating-point
     * data. This funtion was named poorly as it sets the <code>ZBLANK</code> value in the header or in the equivalently
     * named column.
     * 
     * @param  blank the new integer value that denotes <code>NaN</code> when floating-point data is compressed with an
     *                   integer-only algorithm. Setting this option to <code>null</code> will set the header
     *                   <code>ZBLANK</code> value, when the data contains NaNs, to -2147483647 (i.e., the value
     *                   recommended by the FITS standard).
     * 
     * @return       itself
     * 
     * @see          #getBNull()
     */
    public QuantizeOption setBNull(Integer blank) {
        nullValueIndicator = blank;
        return this;
    }

    /**
     * Sets the quantization level for integer compressed floating-point data. This funtion was named poorly as it sets
     * the <code>ZZERO</code> parameter value in the named column when compressing floating-point data with an algorithm
     * that supports integers only. It has nothing to do with the <code>BZERO</code> header value, which indicates the
     * integer representation of floating-point data for the <i>uncompressed</i> data.
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
     * Sets the quantization offset for integer compressed floating-point data. This funtion was named poorly as it sets
     * the <code>ZZERO</code> parameter value in the named column when compressing floating-point data with an algorithm
     * that supports integers only. It has nothing to do with the <code>BZERO</code> header value, which indicates the
     * integer representation of floating-point data for the <i>uncompressed</i> data.
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
     * Enabled or disables keeping `ZZERO` at 0 when using automatic quantization.
     * 
     * @param  value <code>true</code> to keep `ZZERO` at 0 when quantizing automatically, that is keep the integer
     *                   quantized level 0 correspond to floating-point level 0.0. Or, <code>false</code> to let the
     *                   automatic quantization algorithm determine the optimal quantization offset.
     * 
     * @return       iftself
     * 
     * @see          #isCenterOnZero()
     */
    public QuantizeOption setCenterOnZero(boolean value) {
        config.centerOnZero = value;
        return this;
    }

    /**
     * Obsolete method that used to set whether we should expect the floating-point data to contain <code>null</code>
     * values (normally NaNs).
     * 
     * @deprecated       This feature is set automatically as needed.
     * 
     * @param      value (unused)
     * 
     * @return           itself
     * 
     * @see              #setBNull(Integer)
     */
    @Deprecated
    public QuantizeOption setCheckNull(boolean value) {
        return this;
    }

    /**
     * Sets whether automatic quantization is to treat 0.0 as a special value. This is the same as
     * {@link #setDither2(boolean)}. When enabled and dithering is used, then 0.0 values will be denoted with the
     * special value −2147483647 in the quantized representation.
     * 
     * @deprecated       Use {@link #setDither2(boolean)} instead if you want zero values to be special encoded. The
     *                       representation of true zero values is the unique feature of the
     *                       <code>SUBTRACTIVE_DITHER_2</code> method that sets it apart from
     *                       <code>SUBTRACTIVE_DITHER_1</code>.
     * 
     * @param      value (unused) value whether to treat values around 0.0 as special.
     * 
     * @return           itself
     * 
     * @see              #setDither2(boolean)
     * @see              #isDither2()
     */
    @Deprecated
    public QuantizeOption setCheckZero(boolean value) {
        return setDither2(value);
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
        config.dither = value;
        return this;
    }

    /**
     * Sets whether dithering is to use method 2, when dithering is enabled. It does not actually enable or disable
     * dithering itself -- for that you must call {@link #setDither(boolean)}. When dither method 2 is used, then 0.0
     * values will be denoted with the special value −2147483647 in the quantized representation, whereas dither method
     * 1 treats 0.0 just like any other decomal value.
     * 
     * @param  value <code>true</code> to use dither method 2, or else <code>false</code> for method 1.
     * 
     * @return       itself
     * 
     * @see          #isDither2()
     * @see          #setDither(boolean)
     */
    public QuantizeOption setDither2(boolean value) {
        config.checkZero = value;
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
     * Sets the floating-point value that indicates missing data in the floating point image image before quantization
     * is applied. Normally, the FITS standard is that NaN values indicate <code>null</code> values in floating-point
     * images. While this class allows using other values also, they are not recommended since they are not supported by
     * FITS in a standard way.
     * 
     * @param      value the new floating-point value that represents a <code>null</code> value (missing data) in the
     *                       image before quantization.
     * 
     * @return           itself
     * 
     * @see              #getNullValue()
     * @see              #setBNull(Integer)
     * 
     * @deprecated       The use of null values other than <code>NaN</code> for floating-point data types is not
     *                       standard in FITS. You should therefore avoid using this method, in general.
     */
    @Deprecated
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
        config.qlevel = value;
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
        config.seed = value;
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

    /**
     * Re-initialize the dither sequence.
     */
    void initDither() {
        if (isDither() || isDither2()) {
            iseed = (int) ((getSeed() + tileIndex - 1) % RandomSequence.length());
            initI1();
        }
    }

    private void initI1() {
        nextRandom = (int) (RandomSequence.get(iseed) * RANDOM_MULTIPLICATOR);
    }

    private double nextDither() {
        double d = RandomSequence.get(nextRandom) - DITHER_HALF;
        nextRandom++;

        if (nextRandom >= RandomSequence.length()) {
            iseed = (iseed + 1) % RandomSequence.length();
            initI1();
        }

        return d;
    }

    boolean isRegular(double x) {
        if (!Double.isFinite(x)) {
            return false;
        }
        if (x == nullValue) {
            return false;
        }
        if (isCheckZero() && x == 0.0) {
            return false;
        }
        return true;
    }

    /**
     * Converts a floating point value to a quantized integer
     * 
     * @param  d a floating point value
     * 
     * @return   the equivalent quantized integer representation
     * 
     * @since    1.23
     */
    int toInt(double d) {
        if (Double.isNaN(d) || d == nullValue) {
            if (nullValueIndicator == null) {
                nullValueIndicator = RECOMMENDED_NAN_INDICATOR;
            }
            return nullValueIndicator;
        }

        if (isDither() && isDither2() && d == 0.0) {
            return DITHER2_ZERO_INDICATOR;
        }

        d -= bZero;
        d /= bScale;
        if (isDither()) {
            d += nextDither();
        }
        return (int) Math.round(d);
    }

    /**
     * Converts a quantized integer value back to it's floating-point equivalent
     * 
     * @param  i a quantized integer value
     * 
     * @return   the equivalent floating point value
     * 
     * @since    1.23
     */
    double toDouble(int i) {
        if (isDither() && isDither2() && i == DITHER2_ZERO_INDICATOR) {
            return 0.0;
        }

        if (nullValueIndicator != null && i == nullValueIndicator) {
            return nullValue;
        }

        double d = i;
        if (isDither()) {
            d -= nextDither();
        }

        return useFMA ? Math.fma(d, bScale, bZero) : d * bScale + bZero;
    }

    void updateBZeroAndIntLimits() {
        setBZero(findBZero());
        setIntMinValue((int) Math.floor((minValue - bZero) / bScale));
        setIntMaxValue((int) Math.ceil((maxValue - bZero) / bScale));
    }

    double findBZero() {
        if (isCenterOnZero()) {
            // Force ZZERO to be 0.0, as requested
            return 0.0;
        }

        // return all positive values, if possible since some compression
        // algorithms are more efficient that way.
        if (Math.ceil((maxValue - minValue) / bScale) < Integer.MAX_VALUE) {
            // fudge the zero point so it is an integer multiple of bScale
            // This helps to ensure the same scaling will be performed if
            // the file undergoes multiple fpack/funpack cycles
            // AK: round to multiple of bScale.
            double rem = Math.IEEEremainder(minValue, bScale);
            if (rem < 0.0) {
                rem += bScale;
            }
            return minValue - rem;
        }

        // center the quantized levels around zero
        return (minValue + maxValue) / 2.;
    }

    /**
     * Selects whether {@link Math#fma(double, double, double)} should be used when converting quantized integers back
     * to doubles. Othwerwise normal arithmetic is used, which is the default. CFITSIO and astropy both rely on
     * <code>fma()</code>, which has better precision, but is not supported on some (older) architectures. When hardware
     * support is lacking, you may expect a significant performance hit from the software implementation.
     * 
     * @param value <code>true</code> to use <code>fma()</code>, or else <code>false</code> to use regular arithmetics.
     * 
     * @see         #isUseFMA()
     * 
     * @since       1.23
     */
    public static void useFMA(boolean value) {
        useFMA = value;
    }

    /**
     * Checks whether {@link Math#fma(double, double, double)} is used for converting quantized integers back to
     * doubles.
     * 
     * @return <code>true</code> if using <code>fma()</code>, or else <code>false</code> is using regular arithmetics.
     * 
     * @since  1.23
     */
    public static final boolean isUseFMA() {
        return useFMA;
    }

    /**
     * Stores configuration in a way that can be shared and modified across enclosing option copies.
     * 
     * @author Attila Kovacs
     *
     * @since  1.18
     */
    private static final class Config {

        private boolean centerOnZero;

        private boolean dither;

        private boolean checkZero;

        private double qlevel = 4.0;

        private long seed = 1L;
    }
}
