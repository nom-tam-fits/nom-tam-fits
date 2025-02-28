package nom.tam.fits.compression.provider;

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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.FitsException;
import nom.tam.fits.compression.algorithm.api.ICompressOption;
import nom.tam.fits.compression.algorithm.api.ICompressor;
import nom.tam.fits.compression.algorithm.api.ICompressorControl;
import nom.tam.fits.compression.algorithm.gzip.GZipCompressor.ByteGZipCompressor;
import nom.tam.fits.compression.algorithm.gzip.GZipCompressor.DoubleGZipCompressor;
import nom.tam.fits.compression.algorithm.gzip.GZipCompressor.FloatGZipCompressor;
import nom.tam.fits.compression.algorithm.gzip.GZipCompressor.IntGZipCompressor;
import nom.tam.fits.compression.algorithm.gzip.GZipCompressor.LongGZipCompressor;
import nom.tam.fits.compression.algorithm.gzip.GZipCompressor.ShortGZipCompressor;
import nom.tam.fits.compression.algorithm.gzip2.GZip2Compressor.ByteGZip2Compressor;
import nom.tam.fits.compression.algorithm.gzip2.GZip2Compressor.DoubleGZip2Compressor;
import nom.tam.fits.compression.algorithm.gzip2.GZip2Compressor.FloatGZip2Compressor;
import nom.tam.fits.compression.algorithm.gzip2.GZip2Compressor.IntGZip2Compressor;
import nom.tam.fits.compression.algorithm.gzip2.GZip2Compressor.LongGZip2Compressor;
import nom.tam.fits.compression.algorithm.gzip2.GZip2Compressor.ShortGZip2Compressor;
import nom.tam.fits.compression.algorithm.hcompress.HCompressor.ByteHCompressor;
import nom.tam.fits.compression.algorithm.hcompress.HCompressor.DoubleHCompressor;
import nom.tam.fits.compression.algorithm.hcompress.HCompressor.FloatHCompressor;
import nom.tam.fits.compression.algorithm.hcompress.HCompressor.IntHCompressor;
import nom.tam.fits.compression.algorithm.hcompress.HCompressor.ShortHCompressor;
import nom.tam.fits.compression.algorithm.hcompress.HCompressorQuantizeOption;
import nom.tam.fits.compression.algorithm.plio.PLIOCompress.BytePLIOCompressor;
import nom.tam.fits.compression.algorithm.plio.PLIOCompress.IntPLIOCompressor;
import nom.tam.fits.compression.algorithm.plio.PLIOCompress.ShortPLIOCompressor;
import nom.tam.fits.compression.algorithm.quant.QuantizeOption;
import nom.tam.fits.compression.algorithm.quant.QuantizeProcessor.DoubleQuantCompressor;
import nom.tam.fits.compression.algorithm.quant.QuantizeProcessor.FloatQuantCompressor;
import nom.tam.fits.compression.algorithm.rice.RiceCompressor.ByteRiceCompressor;
import nom.tam.fits.compression.algorithm.rice.RiceCompressor.DoubleRiceCompressor;
import nom.tam.fits.compression.algorithm.rice.RiceCompressor.FloatRiceCompressor;
import nom.tam.fits.compression.algorithm.rice.RiceCompressor.IntRiceCompressor;
import nom.tam.fits.compression.algorithm.rice.RiceCompressor.ShortRiceCompressor;
import nom.tam.fits.compression.algorithm.rice.RiceQuantizeCompressOption;
import nom.tam.fits.compression.algorithm.uncompressed.NoCompressCompressor.ByteNoCompressCompressor;
import nom.tam.fits.compression.algorithm.uncompressed.NoCompressCompressor.DoubleNoCompressCompressor;
import nom.tam.fits.compression.algorithm.uncompressed.NoCompressCompressor.FloatNoCompressCompressor;
import nom.tam.fits.compression.algorithm.uncompressed.NoCompressCompressor.IntNoCompressCompressor;
import nom.tam.fits.compression.algorithm.uncompressed.NoCompressCompressor.LongNoCompressCompressor;
import nom.tam.fits.compression.algorithm.uncompressed.NoCompressCompressor.ShortNoCompressCompressor;
import nom.tam.fits.compression.provider.api.ICompressorProvider;
import nom.tam.fits.compression.provider.param.api.ICompressHeaderParameter;
import nom.tam.fits.compression.provider.param.api.ICompressParameters;
import nom.tam.fits.compression.provider.param.base.CompressParameters;
import nom.tam.fits.compression.provider.param.hcompress.HCompressParameters;
import nom.tam.fits.compression.provider.param.rice.RiceCompressParameters;

/**
 * (<i>for internal use</i>) Standard implementation of the {@code ICompressorProvider} interface.
 */
@SuppressWarnings({"javadoc", "deprecation"})
public class CompressorProvider implements ICompressorProvider {

    /**
     * private implementation of the tile compression provider, all is based on the option based constructor of the
     * compressors.
     */
    protected static class TileCompressorControl implements ICompressorControl {

        private final Constructor<ICompressor<Buffer>>[] constructors;

        private Class<? extends ICompressOption> optionClass;

        private Class<?> quantType;

        @SuppressWarnings("unchecked")
        protected TileCompressorControl(Class<?> compressorClass) {
            constructors = (Constructor<ICompressor<Buffer>>[]) compressorClass.getConstructors();
            for (Constructor<ICompressor<Buffer>> c : constructors) {
                if (c.getParameterTypes().length == 1) {
                    optionClass = (Class<? extends ICompressOption>) c.getParameterTypes()[0];
                    break;
                }
            }
        }

        /**
         * Sets the floating-point type to quantize to use for this tile compressor.
         *
         * @param  floatingPointType Floating-point primitive type to quantize. Must be either <code>double.class</code>
         *                               or else <code>float.class</code>.
         *
         * @return                   itself
         *
         * @since                    1.18
         */
        protected TileCompressorControl setQuantType(Class<?> floatingPointType) {
            quantType = floatingPointType;
            return this;
        }

        @Override
        public boolean compress(Buffer in, ByteBuffer out, ICompressOption option) {
            try {
                return newCompressor(option).compress(in, out);
            } catch (Exception e) {
                LOG.log(Level.FINE, "could not compress using " + constructors[0].getName()
                        + " must fallback to other compression method", e);
                return false;
            }
        }

        @Override
        public void decompress(ByteBuffer in, Buffer out, ICompressOption option) {
            try {
                newCompressor(option).decompress(in, out);
            } catch (Exception e) {
                throw new IllegalStateException("could not decompress " + constructors[0].getName(), e);
            }
        }

        @Override
        public ICompressOption option() {
            ICompressOption option = null;
            if (optionClass != null) {
                try {
                    option = optionClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new IllegalStateException("could not instantiate option class for " + constructors[0].getName(),
                            e);
                }
            }

            if (option == null) {
                option = NULL_OPTION;
            }

            if (quantType != null) {
                return new QuantizeOption(option);
            }

            return option;
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private ICompressor<Buffer> newCompressor(ICompressOption option)
                throws FitsException, InstantiationException, IllegalAccessException, InvocationTargetException {
            ICompressor<Buffer> compressor = null;
            QuantizeOption quantOption = null;

            if (option instanceof QuantizeOption) {
                quantOption = (QuantizeOption) option;
                option = quantOption.getCompressOption();
            }

            if (option == NULL_OPTION) {
                option = null;
            }

            try {
                for (Constructor<ICompressor<Buffer>> c : constructors) {
                    Class<?>[] parms = c.getParameterTypes();

                    if (parms.length == 0 && option == null) {
                        // Use constructor without special options...
                        compressor = c.newInstance();
                        break;
                    }

                    if (parms.length == 1 && option != null) {
                        // Use constructor with the option
                        Class<? extends ICompressOption> p = (Class<? extends ICompressOption>) parms[0];
                        if (quantOption != null && p.isAssignableFrom(quantOption.getClass())) {
                            compressor = c.newInstance(quantOption);
                            quantOption = null; // Don't wrap in a quantizer below...
                            break;
                        }
                        if (p.isAssignableFrom(option.getClass())) {
                            compressor = c.newInstance(option);
                            break;
                        }
                    }

                }

                if (compressor == null) {
                    throw new FitsException("Could not instantiate (de)compressor for the specified options");
                }

                if (quantOption != null && quantType != null) {
                    if (quantType.equals(double.class)) {
                        return (ICompressor) new DoubleQuantCompressor(quantOption, (ICompressor) compressor);
                    }
                    if (quantType.equals(float.class)) {
                        return (ICompressor) new FloatQuantCompressor(quantOption, (ICompressor) compressor);
                    }
                }

                return compressor;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private static final ICompressOption NULL_OPTION = new ICompressOption() {

        @Override
        public ICompressOption copy() {
            return this;
        }

        @Override
        public ICompressParameters getCompressionParameters() {
            return NULL_PARAMETERS;
        }

        @Override
        public boolean isLossyCompression() {
            return false;
        }

        @Override
        public void setParameters(ICompressParameters parameters) {
        }

        @Override
        public ICompressOption setTileHeight(int value) {
            return this;
        }

        @Override
        public ICompressOption setTileWidth(int value) {
            return this;
        }

        @Override
        public <T> T unwrap(Class<T> clazz) {
            return clazz.isAssignableFrom(this.getClass()) ? clazz.cast(this) : null;
        }
    };

    private static final ICompressParameters NULL_PARAMETERS = new CompressParameters() {

        @Override
        protected ICompressHeaderParameter[] headerParameters() {
            return new ICompressHeaderParameter[0];
        }

        @Override
        public ICompressParameters copy(ICompressOption option) {
            return this;
        }
    };

    // @formatter:off
    private static final Class<?>[][] AVAILABLE_COMPRESSORS = {//
            {ByteRiceCompressor.class, RiceCompressParameters.class}, //
            {ShortRiceCompressor.class, RiceCompressParameters.class}, //
            {IntRiceCompressor.class, RiceCompressParameters.class}, //
            {FloatRiceCompressor.class, RiceQuantizeCompressOption.class}, //
            {DoubleRiceCompressor.class, RiceQuantizeCompressOption.class}, //
            {BytePLIOCompressor.class}, //
            {ShortPLIOCompressor.class}, //
            {IntPLIOCompressor.class}, //
            {ByteHCompressor.class, HCompressParameters.class}, //
            {ShortHCompressor.class, HCompressParameters.class}, //
            {IntHCompressor.class, HCompressParameters.class}, //
            {FloatHCompressor.class, HCompressorQuantizeOption.class}, //
            {DoubleHCompressor.class, HCompressorQuantizeOption.class}, //
            {ByteGZip2Compressor.class}, //
            {ShortGZip2Compressor.class}, //
            {IntGZip2Compressor.class}, //
            {FloatGZip2Compressor.class}, //
            {DoubleGZip2Compressor.class}, //
            {LongGZip2Compressor.class}, //
            {ByteGZipCompressor.class}, //
            {ShortGZipCompressor.class}, //
            {IntGZipCompressor.class}, //
            {LongGZipCompressor.class}, //
            {FloatGZipCompressor.class}, //
            {DoubleGZipCompressor.class}, //
            {ByteNoCompressCompressor.class}, //
            {ShortNoCompressCompressor.class}, //
            {IntNoCompressCompressor.class}, //
            {LongNoCompressCompressor.class}, //
            {FloatNoCompressCompressor.class}, //
            {DoubleNoCompressCompressor.class}};
    // @formatter:on

    private static final CompressorControlNameComputer NAME_COMPUTER = new CompressorControlNameComputer();

    /**
     * logger to log to.
     */
    private static final Logger LOG = Logger.getLogger(CompressorProvider.class.getName());

    public static ICompressorControl findCompressorControl(String quantAlgorithm, String compressionAlgorithm,
            Class<?> baseType) {
        for (ICompressorProvider iTileCompressorProvider : ServiceLoader.load(ICompressorProvider.class,
                Thread.currentThread().getContextClassLoader())) {
            ICompressorControl result = iTileCompressorProvider.createCompressorControl(quantAlgorithm,
                    compressionAlgorithm, baseType);
            if (result != null) {
                return result;
            }
        }
        return new CompressorProvider().createCompressorControl(quantAlgorithm, compressionAlgorithm, baseType);
    }

    @Override
    public ICompressorControl createCompressorControl(String quantAlgorithm, String compressionAlgorithm,
            Class<?> baseType) {
        Class<?> quantType = null;

        if (quantAlgorithm != null) {
            // Standard compression via 32-bit integers...
            if (baseType.equals(double.class) || baseType.equals(float.class)) {
                quantType = baseType;
                baseType = int.class;
                quantAlgorithm = null;
            }
        }

        String className = NAME_COMPUTER.createCompressorClassName(quantAlgorithm, compressionAlgorithm, baseType);

        for (Class<?>[] types : AVAILABLE_COMPRESSORS) {
            Class<?> compressorClass = types[0];
            if (compressorClass.getSimpleName().equals(className)) {
                TileCompressorControl tc = new TileCompressorControl(compressorClass);
                tc.setQuantType(quantType);
                return tc;
            }
        }

        return null;
    }
}
