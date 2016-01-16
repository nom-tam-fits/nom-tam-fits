package nom.tam.image.comp.provider;

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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.BinaryTable;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.image.comp.ICompressOption;
import nom.tam.image.comp.ICompressParameters;
import nom.tam.image.comp.ITileCompressor;
import nom.tam.image.comp.ITileCompressorControl;
import nom.tam.image.comp.ITileCompressorProvider;
import nom.tam.image.comp.gzip.GZipCompressor.ByteGZipCompressor;
import nom.tam.image.comp.gzip.GZipCompressor.DoubleGZipCompressor;
import nom.tam.image.comp.gzip.GZipCompressor.FloatGZipCompressor;
import nom.tam.image.comp.gzip.GZipCompressor.IntGZipCompressor;
import nom.tam.image.comp.gzip.GZipCompressor.LongGZipCompressor;
import nom.tam.image.comp.gzip.GZipCompressor.ShortGZipCompressor;
import nom.tam.image.comp.gzip2.GZip2Compressor.IntGZip2Compressor;
import nom.tam.image.comp.gzip2.GZip2Compressor.LongGZip2Compressor;
import nom.tam.image.comp.gzip2.GZip2Compressor.ShortGZip2Compressor;
import nom.tam.image.comp.hcompress.HCompressor.ByteHCompressor;
import nom.tam.image.comp.hcompress.HCompressor.DoubleHCompressor;
import nom.tam.image.comp.hcompress.HCompressor.FloatHCompressor;
import nom.tam.image.comp.hcompress.HCompressor.IntHCompressor;
import nom.tam.image.comp.hcompress.HCompressor.ShortHCompressor;
import nom.tam.image.comp.hcompress.par.HCompressParameters;
import nom.tam.image.comp.hcompress.par.HCompressQuantizeParameters;
import nom.tam.image.comp.plio.PLIOCompress.BytePLIOCompressor;
import nom.tam.image.comp.plio.PLIOCompress.IntPLIOCompressor;
import nom.tam.image.comp.plio.PLIOCompress.ShortPLIOCompressor;
import nom.tam.image.comp.rice.RiceCompressor.ByteRiceCompressor;
import nom.tam.image.comp.rice.RiceCompressor.DoubleRiceCompressor;
import nom.tam.image.comp.rice.RiceCompressor.FloatRiceCompressor;
import nom.tam.image.comp.rice.RiceCompressor.IntRiceCompressor;
import nom.tam.image.comp.rice.RiceCompressor.ShortRiceCompressor;
import nom.tam.image.comp.rice.par.RiceCompressParameters;
import nom.tam.image.comp.rice.par.RiceQuantizeCompressParameters;

/**
 * Standard implementation of the {@code ITileCompressorProvider} interface.
 */
public class TileCompressorProvider implements ITileCompressorProvider {

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

        @Override
        public void setParameters(ICompressParameters parameters) {
        }
    };

    private static final ICompressParameters NULL_PARAMETERS = new ICompressParameters() {

        @Override
        public void addColumnsToTable(BinaryTableHDU hdu) {
        }

        @Override
        public ICompressParameters copy(ICompressOption clone) {
            return this;
        }

        @Override
        public void getValuesFromColumn(int index) {
        }

        @Override
        public void getValuesFromHeader(Header header) {
        }

        @Override
        public void initializeColumns(Header header, BinaryTable binaryTable, int size) throws FitsException {
        }

        @Override
        public void initializeColumns(int length) {
        }

        @Override
        public void setValueFromColumn(int index) {
        }

        @Override
        public void setValuesInHeader(Header header) {
        }
    };

    /**
     * private implementation of the tile compression provider, all is based on
     * the option based constructor of the compressors.
     */
    protected static class TileCompressorControl implements ITileCompressorControl {

        private final Constructor<ITileCompressor<Buffer>> constructor;

        private final Class<? extends ICompressOption> optionClass;

        private final Constructor<ICompressParameters> parametersConstructor;

        protected TileCompressorControl(Class<?> compressorClass) {
            this(compressorClass, null);
        }

        @SuppressWarnings("unchecked")
        protected TileCompressorControl(Class<?> compressorClass, Class<?> parametersClass) {
            this.constructor = (Constructor<ITileCompressor<Buffer>>) compressorClass.getConstructors()[0];
            this.optionClass = (Class<? extends ICompressOption>) (this.constructor.getParameterTypes().length == 0 ? null : this.constructor.getParameterTypes()[0]);
            if (parametersClass != null) {
                this.parametersConstructor = (Constructor<ICompressParameters>) parametersClass.getConstructors()[0];
            } else {
                this.parametersConstructor = null;
            }
        }

        @Override
        public boolean compress(Buffer in, ByteBuffer out, ICompressOption option) {
            try {
                return newCompressor(option).compress(in, out);
            } catch (Exception e) {
                LOG.log(Level.FINE, "could not compress using " + this.constructor + " must fallback to other compression method", e);
                return false;
            }
        }

        @Override
        public void decompress(ByteBuffer in, Buffer out, ICompressOption option) {
            try {
                newCompressor(option).decompress(in, out);
            } catch (Exception e) {
                throw new IllegalStateException("could not decompress " + this.constructor, e);
            }
        }

        private ITileCompressor<Buffer> newCompressor(ICompressOption option) throws InstantiationException, IllegalAccessException, InvocationTargetException {
            return this.constructor.getParameterTypes().length == 0 ? this.constructor.newInstance() : this.constructor.newInstance(option);
        }

        @Override
        public ICompressOption option() {
            if (this.optionClass != null) {
                try {
                    ICompressOption option = this.optionClass.newInstance();
                    if (this.parametersConstructor != null) {
                        option.setParameters(this.parametersConstructor.newInstance(option));
                    } else {
                        option.setParameters(NULL_PARAMETERS);
                    }
                    return option;
                } catch (Exception e) {
                    throw new IllegalStateException("could not instantiate option class for " + this.constructor, e);
                }
            }
            return NULL_OPTION;
        }
    }

    // @formatter:off
    private static final Class<?>[][] AVAILABLE_COMPRESSORS = {
        {ByteRiceCompressor.class, RiceCompressParameters.class},
        {ShortRiceCompressor.class, RiceCompressParameters.class},
        {IntRiceCompressor.class, RiceCompressParameters.class},
        {FloatRiceCompressor.class, RiceQuantizeCompressParameters.class},
        {DoubleRiceCompressor.class, RiceQuantizeCompressParameters.class},
        {BytePLIOCompressor.class},
        {ShortPLIOCompressor.class},
        {IntPLIOCompressor.class},
        {ByteHCompressor.class, HCompressParameters.class},
        {ShortHCompressor.class, HCompressParameters.class},
        {IntHCompressor.class, HCompressParameters.class},
        {FloatHCompressor.class, HCompressQuantizeParameters.class},
        {DoubleHCompressor.class, HCompressQuantizeParameters.class},
        {DoubleHCompressor.class},
        {ShortGZip2Compressor.class},
        {IntGZip2Compressor.class},
        {LongGZip2Compressor.class},
        {ByteGZipCompressor.class},
        {ShortGZipCompressor.class},
        {IntGZipCompressor.class},
        {LongGZipCompressor.class},
        {FloatGZipCompressor.class},
        {DoubleGZipCompressor.class},
    };
    // @formatter:on

    private static final TileCompressorControlNameComputer NAME_COMPUTER = new TileCompressorControlNameComputer();

    /**
     * logger to log to.
     */
    private static final Logger LOG = Logger.getLogger(TileCompressorProvider.class.getName());

    public static ITileCompressorControl findCompressorControl(String quantAlgorithm, String compressionAlgorithm, Class<?> baseType) {
        ITileCompressorProvider defaultProvider = null;
        for (ITileCompressorProvider iTileCompressorProvider : ServiceLoader.load(ITileCompressorProvider.class, Thread.currentThread().getContextClassLoader())) {
            if (iTileCompressorProvider instanceof TileCompressorProvider) {
                defaultProvider = iTileCompressorProvider;
            } else {
                ITileCompressorControl result = iTileCompressorProvider.createCompressorControl(quantAlgorithm, compressionAlgorithm, baseType);
                if (result != null) {
                    return result;
                }
            }
        }
        return defaultProvider.createCompressorControl(quantAlgorithm, compressionAlgorithm, baseType);
    }

    @Override
    public ITileCompressorControl createCompressorControl(String quantAlgorithm, String compressionAlgorithm, Class<?> baseType) {

        String className = NAME_COMPUTER.createCompressorClassName(quantAlgorithm, compressionAlgorithm, baseType);
        for (Class<?>[] clazz : AVAILABLE_COMPRESSORS) {
            Class<?> compressorClass = clazz[0];
            if (compressorClass.getSimpleName().equals(className)) {
                if (clazz.length > 1) {
                    Class<?> parametersClass = clazz[1];
                    return new TileCompressorControl(compressorClass, parametersClass);
                } else {
                    return new TileCompressorControl(compressorClass);
                }
            }
        }
        return null;
    }
}
