package nom.tam.fits.compression;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import nom.tam.fits.compression.provider.CompressorControlNameComputer;
import nom.tam.fits.header.Compression;

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

import static nom.tam.fits.header.Compression.ZCMPTYPE_HCOMPRESS_1;
import static nom.tam.fits.header.Compression.ZCMPTYPE_PLIO_1;
import static nom.tam.fits.header.Compression.ZCMPTYPE_RICE_1;
import static nom.tam.fits.header.Compression.ZQUANTIZ_NO_DITHER;

/**
 * Note that the purpose of these tests is to demonstrate how the class names are computed, not to achieve high code
 * coverage - integration tests are used for the latter purpose.
 */
public class TileCompressorControlNameComputerTest {
    private CompressorControlNameComputer nameComputer;

    @Before
    public void setUp() {
        nameComputer = new CompressorControlNameComputer();
    }

    @Test
    public void withAbsurdBaseType() {
        assertEquals("TilecompressorcontrolnamecomputertestQuantHCompressor", //
                nameComputer.createCompressorClassName(ZQUANTIZ_NO_DITHER, ZCMPTYPE_HCOMPRESS_1,
                        TileCompressorControlNameComputerTest.class));
    }

    @Test
    public void withIgnoredQuantizeAlgorithm() {
        assertEquals("DoublePLIOCompressor",
                nameComputer.createCompressorClassName(ZQUANTIZ_NO_DITHER, ZCMPTYPE_PLIO_1, double.class));
    }

    @Test
    public void withoutQuantizeAlgorithm() {
        assertEquals("IntRiceCompressor", nameComputer.createCompressorClassName(null, ZCMPTYPE_RICE_1, int.class));
    }

    @Test
    public void withValidQuantizeAlgorithm() {
        assertEquals("LongQuantHCompressor",
                nameComputer.createCompressorClassName(ZQUANTIZ_NO_DITHER, ZCMPTYPE_HCOMPRESS_1, long.class));
    }

    @Test
    public void withWrongCompressAlgorithm() {
        assertEquals("ShortQuantUnknownCompressor",
                nameComputer.createCompressorClassName(ZQUANTIZ_NO_DITHER, "DoesNotExist", short.class));
    }

    @Test
    public void withWrongQuantizeAlgorithm() {
        assertEquals("ByteUnknownPLIOCompressor",
                nameComputer.createCompressorClassName("Whatever", ZCMPTYPE_PLIO_1, byte.class));
    }

    @Test
    public void testGZip1() {
        String name = nameComputer.createCompressorClassName(ZQUANTIZ_NO_DITHER, Compression.ZCMPTYPE_GZIP_1, int.class);
        assertTrue(name.contains("GZip"));
    }

    @Test
    public void testRice() {
        String name = nameComputer.createCompressorClassName(ZQUANTIZ_NO_DITHER, Compression.ZCMPTYPE_RICE_1, int.class);
        assertTrue(name.contains("Rice"));
    }

    @Test
    public void testRiceAlt() {
        String name = nameComputer.createCompressorClassName(ZQUANTIZ_NO_DITHER, Compression.ZCMPTYPE_RICE_ONE, int.class);
        assertTrue(name.contains("Rice"));
    }

    @Test
    public void testPLIO() {
        String name = nameComputer.createCompressorClassName(ZQUANTIZ_NO_DITHER, Compression.ZCMPTYPE_PLIO_1, int.class);
        assertTrue(name.contains("PLIO"));
    }

    @Test
    public void testHCompress() {
        String name = nameComputer.createCompressorClassName(ZQUANTIZ_NO_DITHER, Compression.ZCMPTYPE_HCOMPRESS_1,
                int.class);
        assertTrue(name.contains("H"));
    }

    @Test
    public void testGZip2() {
        String name = nameComputer.createCompressorClassName(ZQUANTIZ_NO_DITHER, Compression.ZCMPTYPE_GZIP_2, int.class);
        assertTrue(name.contains("GZip2"));
    }

    @Test
    public void testNoCompress() {
        String name = nameComputer.createCompressorClassName(ZQUANTIZ_NO_DITHER, Compression.ZCMPTYPE_NOCOMPRESS,
                int.class);
        assertTrue(name.contains("NoCompress"));
    }

    @Test
    public void testDither1() {
        String name = nameComputer.createCompressorClassName(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_1,
                Compression.ZCMPTYPE_NOCOMPRESS, int.class);
        assertTrue(name.contains("Quant"));
    }

    @Test
    public void testDither2() {
        String name = nameComputer.createCompressorClassName(Compression.ZQUANTIZ_SUBTRACTIVE_DITHER_2,
                Compression.ZCMPTYPE_NOCOMPRESS, int.class);
        assertTrue(name.contains("Quant"));
    }

    @Test
    public void testNoDither() {
        String name = nameComputer.createCompressorClassName(ZQUANTIZ_NO_DITHER, Compression.ZCMPTYPE_NOCOMPRESS,
                int.class);
        assertTrue(name.contains("Quant"));
    }

    @Test
    public void testUnknonwDither() {
        String name = nameComputer.createCompressorClassName("blah", Compression.ZCMPTYPE_NOCOMPRESS, int.class);
        assertTrue(name.contains("Unknown"));
    }

    @Test
    public void testByte() {
        String name = nameComputer.createCompressorClassName(ZQUANTIZ_NO_DITHER, Compression.ZCMPTYPE_NOCOMPRESS,
                byte.class);
        assertTrue(name.contains("Byte"));
    }

    @Test
    public void testShort() {
        String name = nameComputer.createCompressorClassName(ZQUANTIZ_NO_DITHER, Compression.ZCMPTYPE_NOCOMPRESS,
                short.class);
        assertTrue(name.contains("Short"));
    }

    @Test
    public void testInt() {
        String name = nameComputer.createCompressorClassName(ZQUANTIZ_NO_DITHER, Compression.ZCMPTYPE_NOCOMPRESS,
                int.class);
        assertTrue(name.contains("Int"));
    }

    @Test
    public void testFloat() {
        String name = nameComputer.createCompressorClassName(ZQUANTIZ_NO_DITHER, Compression.ZCMPTYPE_NOCOMPRESS,
                float.class);
        assertTrue(name.contains("Float"));
    }

    @Test
    public void testDouble() {
        String name = nameComputer.createCompressorClassName(ZQUANTIZ_NO_DITHER, Compression.ZCMPTYPE_NOCOMPRESS,
                double.class);
        assertTrue(name.contains("Double"));
    }
}
