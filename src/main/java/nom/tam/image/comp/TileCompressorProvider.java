package nom.tam.image.comp;

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
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.header.Compression;
import nom.tam.image.comp.gzip.GZipCompress.ByteGZipCompress;
import nom.tam.image.comp.gzip.GZipCompress.DoubleGZipCompress;
import nom.tam.image.comp.gzip.GZipCompress.FloatGZipCompress;
import nom.tam.image.comp.gzip.GZipCompress.IntGZipCompress;
import nom.tam.image.comp.gzip.GZipCompress.LongGZipCompress;
import nom.tam.image.comp.gzip.GZipCompress.ShortGZipCompress;
import nom.tam.image.comp.gzip2.GZip2Compress.ByteGZip2Compress;
import nom.tam.image.comp.gzip2.GZip2Compress.IntGZip2Compress;
import nom.tam.image.comp.gzip2.GZip2Compress.LongGZip2Compress;
import nom.tam.image.comp.gzip2.GZip2Compress.ShortGZip2Compress;
import nom.tam.image.comp.hcompress.HCompressor.ByteHCompress;
import nom.tam.image.comp.hcompress.HCompressor.DoubleHCompress;
import nom.tam.image.comp.hcompress.HCompressor.FloatHCompress;
import nom.tam.image.comp.hcompress.HCompressor.IntHCompress;
import nom.tam.image.comp.hcompress.HCompressor.ShortHCompress;
import nom.tam.image.comp.plio.PLIOCompress.BytePLIOCompress;
import nom.tam.image.comp.plio.PLIOCompress.ShortPLIOCompress;
import nom.tam.image.comp.rice.RiceCompress.ByteRiceCompress;
import nom.tam.image.comp.rice.RiceCompress.DoubleRiceCompress;
import nom.tam.image.comp.rice.RiceCompress.FloatRiceCompress;
import nom.tam.image.comp.rice.RiceCompress.IntRiceCompress;
import nom.tam.image.comp.rice.RiceCompress.ShortRiceCompress;

public class TileCompressorProvider implements ITileCompressorProvider {

    /**
     * private implementation of the tile compression provider, all is based on
     * the option based constructor of the compressors.
     */
    protected static class TileCompressorControl implements ITileCompressorControl {

        private final Constructor<ITileCompressor<Buffer>> constructor;

        private final Class<? extends ICompressOption>[] optionClasses;

        @SuppressWarnings("unchecked")
        protected TileCompressorControl(Class<?> compressorClass) {
            this.constructor = (Constructor<ITileCompressor<Buffer>>) compressorClass.getConstructors()[0];
            this.optionClasses = (Class<? extends ICompressOption>[]) this.constructor.getParameterTypes();
        }

        @Override
        public boolean compress(Buffer in, ByteBuffer out, ICompressOption... options) {
            try {
                this.constructor.newInstance((Object[]) options).compress(in, out);
            } catch (Exception e) {
                LOG.log(Level.FINE, "could not compress using " + this.constructor + " must fallback to other compression method", e);
                return false;
            }
            return true;
        }

        @Override
        public void decompress(ByteBuffer in, Buffer out, ICompressOption... options) {
            try {
                this.constructor.newInstance((Object[]) options).decompress(in, out);
            } catch (Exception e) {
                throw new IllegalStateException("could not decompress " + this.constructor, e);
            }
        }

        @Override
        public ICompressOption[] options() {
            try {
                ICompressOption[] result = new ICompressOption[this.optionClasses.length];
                for (int index = 0; index < result.length; index++) {
                    result[index] = this.optionClasses[index].newInstance();
                }
                return result;
            } catch (Exception e) {
                throw new IllegalStateException("could not instanciate option classes for " + this.constructor, e);
            }
        }
    }

    private static final Class<?>[] AVAILABLE_COMPRESSORS = {
        ByteRiceCompress.class,
        ShortRiceCompress.class,
        IntRiceCompress.class,
        FloatRiceCompress.class,
        DoubleRiceCompress.class,
        
        BytePLIOCompress.class,
        ShortPLIOCompress.class,

        ByteHCompress.class,
        ShortHCompress.class,
        IntHCompress.class,
        FloatHCompress.class,
        DoubleHCompress.class,

        ByteGZip2Compress.class,
        ShortGZip2Compress.class,
        IntGZip2Compress.class,
        LongGZip2Compress.class,

        ByteGZipCompress.class,
        ShortGZipCompress.class,
        IntGZipCompress.class,
        LongGZipCompress.class,
        FloatGZipCompress.class,
        DoubleGZipCompress.class
    };

    /**
     * logger to log to.
     */
    private static final Logger LOG = Logger.getLogger(TileCompressorProvider.class.getName());

    public static ITileCompressorControl findCompressorControl(String quantAlgorithm, String compressionAlgorithm, Class<?> baseType) {
        Iterator<ITileCompressorProvider> providers = ServiceLoader.load(ITileCompressorProvider.class, Thread.currentThread().getContextClassLoader()).iterator();
        ITileCompressorProvider defaultProvider = null;
        while (providers.hasNext()) {
            ITileCompressorProvider iTileCompressorProvider = providers.next();
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

    private String classNameForCompresseion(String quantAlgorithm, String compressionAlgorithm, Class<?> baseType) {
        StringBuilder classsName = new StringBuilder();
        classsName.append(standardizeBaseType(baseType.getSimpleName()));
        if (classsName.indexOf(Float.class.getSimpleName()) == 0 || classsName.indexOf(Double.class.getSimpleName()) == 0) {
            quantAlgorithm = null; // default so not in the className
        }
        classsName.append(standardizeQuantAlgorithm(quantAlgorithm, baseType));
        classsName.append(standardizeCompressionAlgorithm(compressionAlgorithm));
        classsName.append("Compress");
        return classsName.toString();
    }

    @Override
    public ITileCompressorControl createCompressorControl(String quantAlgorithm, String compressionAlgorithm, Class<?> baseType) {

        String classsName = classNameForCompresseion(quantAlgorithm, compressionAlgorithm, baseType);
        for (Class<?> clazz : AVAILABLE_COMPRESSORS) {
            if (clazz.getSimpleName().equals(classsName)) {
                return new TileCompressorControl(clazz);
            }
        }
        return null;
    }

    private Object standardizeBaseType(String simpleName) {
        return Character.toUpperCase(simpleName.charAt(0)) + simpleName.substring(1).toLowerCase();
    }

    private Object standardizeCompressionAlgorithm(String compressionAlgorithm) {
        if (Compression.ZCMPTYPE_RICE_1.equalsIgnoreCase(compressionAlgorithm) || //
                Compression.ZCMPTYPE_RICE_ONE.equalsIgnoreCase(compressionAlgorithm)) {
            return "Rice";
        } else if (Compression.ZCMPTYPE_PLIO_1.equalsIgnoreCase(compressionAlgorithm)) {
            return "PLIO";
        } else if (Compression.ZCMPTYPE_HCOMPRESS_1.equalsIgnoreCase(compressionAlgorithm)) {
            return "H";
        } else if (Compression.ZCMPTYPE_GZIP_2.equalsIgnoreCase(compressionAlgorithm)) {
            return "GZip2";
        } else if (Compression.ZCMPTYPE_GZIP_1.equalsIgnoreCase(compressionAlgorithm)) {
            return "GZip";
        }
        return "Unknown";
    }

    private Object standardizeQuantAlgorithm(String quantAlgorithm, Class<?> baseType) {
        if (quantAlgorithm != null) {
            if (Compression.ZQUANTIZ_NO_DITHER.equalsIgnoreCase(quantAlgorithm) || //
                    Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_1.equalsIgnoreCase(quantAlgorithm) || //
                    Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2.equalsIgnoreCase(quantAlgorithm)) {
                return "Quant";
            } else {
                return "Unknown";
            }
        }
        return "";
    }
}
