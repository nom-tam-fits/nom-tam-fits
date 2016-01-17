package nom.tam.fits.compression.provider;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2016 nom-tam-fits
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

import nom.tam.fits.header.Compression;

/**
 * <p>
 * Computes the name of the tile compressor class name given the algorithm used
 * to quantize and compress the tile and the type of data the tile contains.
 * </p>
 * The name of the class is built of four parts:
 * <ul>
 * <li>the capitalized simple name of the base type of the elements in the tile
 * (like Int, Long etc.);</li>
 * <li>if a known quantize algorithm is used, the word "Quant", the word
 * "Unknown" if the quantize algorithm is not recognized, nothing (i.e. the
 * empty string) if it is null;</li>
 * <li>the short name of the compression algorithm to use (Rice, PLIO, Gzip
 * etc.) or the word "Unknown" if the algorithm is not supported;</li>
 * <li>the suffix "Compressor"</li>
 * </ul>
 * <p>
 * Following exception to above rules exist:
 * </p>
 * <ul>
 * <li>If the primitive type is double or float, the quantize algorithm is
 * ignored (as if it were specified as null)</li>
 * </ul>
 * See the associated unit tests for concrete examples.
 */
public class CompressorControlNameComputer {

    private static final String COMPRESSOR_CLASS_SUFFIX = "Compressor";

    private static String standardizeBaseType(String simpleName) {
        return Character.toUpperCase(simpleName.charAt(0)) + simpleName.substring(1).toLowerCase();
    }

    private static String standardizeCompressionAlgorithm(String compressionAlgorithm) {
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

    private static String standardizeQuantAlgorithm(String quantAlgorithm) {
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

    public CompressorControlNameComputer() {
        super();
    }

    public String createCompressorClassName(String quantAlgorithm, String compressionAlgorithm, Class<?> baseType) {
        StringBuilder className = new StringBuilder();
        className.append(standardizeBaseType(baseType.getSimpleName()));
        if (className.indexOf(Float.class.getSimpleName()) == 0 || className.indexOf(Double.class.getSimpleName()) == 0) {
            quantAlgorithm = null; // default so not in the className
        }
        className.append(standardizeQuantAlgorithm(quantAlgorithm));
        className.append(standardizeCompressionAlgorithm(compressionAlgorithm));
        className.append(COMPRESSOR_CLASS_SUFFIX);
        return className.toString();
    }
}
