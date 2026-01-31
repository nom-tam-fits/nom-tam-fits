/*
 ************************************************************************
 *******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
 **************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
 *
 *  (c) 2022.                            (c) 2022.
 *  Government of Canada                 Gouvernement du Canada
 *  National Research Council            Conseil national de recherches
 *  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
 *  All rights reserved                  Tous droits réservés
 *
 *  NRC disclaims any warranties,        Le CNRC dénie toute garantie
 *  expressed, implied, or               énoncée, implicite ou légale,
 *  statutory, of any kind with          de quelque nature que ce
 *  respect to the software,             soit, concernant le logiciel,
 *  including without limitation         y compris sans restriction
 *  any warranty of merchantability      toute garantie de valeur
 *  or fitness for a particular          marchande ou de pertinence
 *  purpose. NRC shall not be            pour un usage particulier.
 *  liable in any event for any          Le CNRC ne pourra en aucun cas
 *  damages, whether direct or           être tenu responsable de tout
 *  indirect, special or general,        dommage, direct ou indirect,
 *  consequential or incidental,         particulier ou général,
 *  arising from the use of the          accessoire ou fortuit, résultant
 *  software.  Neither the name          de l'utilisation du logiciel. Ni
 *  of the National Research             le nom du Conseil National de
 *  Council of Canada nor the            Recherches du Canada ni les noms
 *  names of its contributors may        de ses  participants ne peuvent
 *  be used to endorse or promote        être utilisés pour approuver ou
 *  products derived from this           promouvoir les produits dérivés
 *  software without specific prior      de ce logiciel sans autorisation
 *  written permission.                  préalable et particulière
 *                                       par écrit.
 *
 *  This file is part of the             Ce fichier fait partie du projet
 *  OpenCADC project.                    OpenCADC.
 *
 *  OpenCADC is free software:           OpenCADC est un logiciel libre ;
 *  you can redistribute it and/or       vous pouvez le redistribuer ou le
 *  modify it under the terms of         modifier suivant les termes de
 *  the GNU Affero General Public        la “GNU Affero General Public
 *  License as published by the          License” telle que publiée
 *  Free Software Foundation,            par la Free Software Foundation
 *  either version 3 of the              : soit la version 3 de cette
 *  License, or (at your option)         licence, soit (à votre gré)
 *  any later version.                   toute version ultérieure.
 *
 *  OpenCADC is distributed in the       OpenCADC est distribué
 *  hope that it will be useful,         dans l’espoir qu’il vous
 *  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
 *  without even the implied             GARANTIE : sans même la garantie
 *  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
 *  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
 *  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
 *  General Public License for           Générale Publique GNU Affero
 *  more details.                        pour plus de détails.
 *
 *  You should have received             Vous devriez avoir reçu une
 *  a copy of the GNU Affero             copie de la Licence Générale
 *  General Public License along         Publique GNU Affero avec
 *  with OpenCADC.  If not, see          OpenCADC ; si ce n’est
 *  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
 *                                       <http://www.gnu.org/licenses/>.
 *
 *
 ************************************************************************
 */

package nom.tam.image.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.FitsUtil;
import nom.tam.fits.Header;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.compression.algorithm.api.ICompressOption;
import nom.tam.fits.compression.algorithm.api.ICompressorControl;
import nom.tam.fits.compression.algorithm.hcompress.HCompressorOption;
import nom.tam.fits.compression.algorithm.quant.QuantizeOption;
import nom.tam.fits.compression.algorithm.rice.RiceCompressOption;
import nom.tam.fits.compression.provider.param.rice.RiceCompressParameters;
import nom.tam.fits.header.Compression;
import nom.tam.fits.header.Standard;
import nom.tam.image.StreamingTileImageData;
import nom.tam.image.compression.hdu.CompressedImageHDU;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.DefaultMethodsTest;
import nom.tam.util.FitsInputStream;
import nom.tam.util.FitsOutputStream;
import nom.tam.util.type.ElementType;

@SuppressWarnings({"javadoc", "deprecation"})
public class CompressedImageTilerTest {
    private static final Logger LOGGER = Logger.getLogger(CompressedImageTilerTest.class.getName());

    static {
        LOGGER.setLevel(Level.INFO);
    }

    @Test
    public void doCompressedImageTileTest() throws Exception {
        final File sourceFile = new File("src/test/resources/nom/tam/image/provided/m13real_rice.fits");
        final int[] cornerStarts = new int[] {10, 10};
        final int[] lengths = new int[] {20, 20};
        final int[] steps = new int[] {1, 2};
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (final Fits sourceFits = new Fits(sourceFile, true);
                final FitsOutputStream fitsOutputStream = new FitsOutputStream(byteArrayOutputStream);
                final Fits outputFits = new Fits()) {
            final CompressedImageHDU compressedImageHDU = (CompressedImageHDU) sourceFits.getHDU(1);

            // Adjust the Header.
            final Header compressedImageHDUHeader = compressedImageHDU.getHeader();

            // Copy the header
            final Header cutoutHeader = new Header();
            compressedImageHDUHeader.iterator().forEachRemaining(cutoutHeader::addLine);

            cutoutHeader.addValue(Standard.NAXISn.n(1), 5);
            cutoutHeader.addValue(Standard.NAXISn.n(2), 10);
            cutoutHeader.addValue(Standard.PCOUNT, 0);
            cutoutHeader.addValue(Standard.GCOUNT, 1);
            cutoutHeader.setSimple(true);

            final CompressedImageTiler compressedImageTiler = new CompressedImageTiler(compressedImageHDU);
            final StreamingTileImageData streamingTileImageData = new StreamingTileImageData(cutoutHeader,
                    compressedImageTiler, cornerStarts, lengths, steps);
            final ImageHDU cutoutImageHDU = new ImageHDU(cutoutHeader, streamingTileImageData);

            outputFits.addHDU(cutoutImageHDU);
            outputFits.write(fitsOutputStream);
        }

        try (final FitsInputStream fitsInputStream = new FitsInputStream(
                new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
                final Fits testFits = new Fits(fitsInputStream)) {
            final ImageHDU imageHDU = (ImageHDU) testFits.readHDU();
            Assertions.assertArrayEquals(new int[] {10, 5}, imageHDU.getAxes());
        }
    }

    @Test
    public void doCompressedImageTest() throws Exception {
        final File sourceFile = new File("src/test/resources/nom/tam/image/provided/m13real_rice.fits");

        try (final Fits sourceFits = new Fits(sourceFile, true)) {

            final CompressedImageHDU cfitsioTable = (CompressedImageHDU) sourceFits.getHDU(1);
            final ElementType<?> elementType = ElementType
                    .forBitpix(cfitsioTable.getHeader().getIntValue(Compression.ZBITPIX));
            final CompressedImageTiler testSubject = new CompressedImageTiler(cfitsioTable);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            final int[] cornerStarts = new int[] {10, 10};
            final int[] lengths = new int[] {20, 20};
            final int[] steps = new int[] {1, 1};

            try (ArrayDataOutput arrayDataOutput = new FitsOutputStream(byteArrayOutputStream)) {
                testSubject.getTile(arrayDataOutput, cornerStarts, lengths, steps);
                arrayDataOutput.flush();
            }

            byte[] data = byteArrayOutputStream.toByteArray();
            Assertions.assertEquals((lengths[0] * lengths[1] * elementType.size()), data.length);

            Assertions.assertArrayEquals((float[][]) cfitsioTable.asImageHDU().getData().getData(),
                    (float[][]) testSubject.getCompleteImage());

            Assertions.assertThrows(UnsupportedOperationException.class, () -> testSubject.getTile(cornerStarts, lengths));
            Assertions.assertThrows(UnsupportedOperationException.class,
                    () -> testSubject.getTile(Array.newInstance(Integer.class, 20 * 20), cornerStarts, lengths));

            byteArrayOutputStream = new ByteArrayOutputStream();

            try (ArrayDataOutput arrayDataOutput = new FitsOutputStream(byteArrayOutputStream)) {
                testSubject.getTile(arrayDataOutput, cornerStarts, lengths);
                arrayDataOutput.flush();
            }

            data = byteArrayOutputStream.toByteArray();
            Assertions.assertEquals((lengths[0] * lengths[1] * elementType.size()), data.length);
        }
    }

    @Test
    public void doGetCompleteImageFail() throws Exception {
        final File sourceFile = new File("src/test/resources/nom/tam/image/provided/m13real_rice.fits");
        try (final Fits sourceFits = new Fits(sourceFile, true)) {

            final CompressedImageHDU compressedImageHDUFromFile = ((CompressedImageHDU) sourceFits.getHDU(1));
            final CompressedImageHDU compressedImageHDU = new CompressedImageHDU(compressedImageHDUFromFile.getHeader(),
                    compressedImageHDUFromFile.getData()) {
                @Override
                public ImageHDU asImageHDU() throws FitsException {
                    throw new FitsException("Simulated FitsException");
                }
            };

            final CompressedImageTiler testSubject = new CompressedImageTiler(compressedImageHDU);

            Assertions.assertThrows(IOException.class, () -> testSubject.getCompleteImage());
        }
    }

    @Test
    public void doTestGetTileFail() throws Exception {
        final File sourceFile = new File("src/test/resources/nom/tam/image/provided/m13real_rice.fits");
        try (final Fits sourceFits = new Fits(sourceFile, true)) {

            final CompressedImageHDU compressedImageHDU = (CompressedImageHDU) sourceFits.getHDU(1);
            final CompressedImageTiler testSubject = new CompressedImageTiler(compressedImageHDU) {
                @Override
                void getTile(ArrayDataOutput output, int[] imageDimensions, int[] corners, int[] lengths, int[] steps)
                        throws FitsException {
                    throw new FitsException("Test error");
                }
            };

            try (final ArrayDataOutput arrayDataOutput = new DefaultMethodsTest.DefaultOutput()) {
                Assertions.assertThrows(IOException.class,
                        () -> testSubject.getTile(arrayDataOutput, new int[2], new int[2]));
            }

            try (final ArrayDataOutput arrayDataOutput = new DefaultMethodsTest.DefaultOutput()) {
                Assertions.assertThrows(IOException.class,
                        () -> testSubject.getTile(arrayDataOutput, new int[2], new int[2], new int[2]));
            }

            Assertions.assertThrows(UnsupportedOperationException.class, () -> testSubject.getTile(new int[2], new int[2]));
        }
    }

    @Test
    public void doTestInitCompressionOption() throws Exception {
        final File sourceFile = new File("src/test/resources/nom/tam/image/provided/m13real_rice.fits");
        try (final Fits sourceFits = new Fits(sourceFile, true)) {

            final CompressedImageHDU cfitsioTable = (CompressedImageHDU) sourceFits.getHDU(1);
            final RiceCompressOption riceCompressOption = new RiceCompressOption();
            final RiceCompressParameters parameters = new RiceCompressParameters(riceCompressOption);
            final RiceCompressOption option = new RiceCompressOption() {
                @Override
                public RiceCompressParameters getCompressionParameters() {
                    return parameters;
                }
            };

            final QuantizeOption quantizeOption = new QuantizeOption(option);

            final ICompressorControl compressorControl = new ICompressorControl() {
                @Override
                public boolean compress(Buffer in, ByteBuffer out, ICompressOption option) {
                    return false;
                }

                @Override
                public void decompress(ByteBuffer in, Buffer out, ICompressOption option) {
                }

                @Override
                public ICompressOption option() {
                    return option;
                }
            };

            final CompressedImageTiler testSubject = new CompressedImageTiler(cfitsioTable) {
                @Override
                ICompressorControl getCompressorControl(ElementType<? extends Buffer> elementType) {
                    return compressorControl;
                }
            };

            testSubject.initCompressionOption(option, 8);
            Assertions.assertEquals(32, option.getBlockSize());

            testSubject.initCompressionOption(quantizeOption, 8);
            Assertions.assertEquals(quantizeOption.getBScale(), Double.NaN, 0.0D);
            Assertions.assertEquals(quantizeOption.getBZero(), Double.NaN, 0.0D);

            // Should be ignored.
            final HCompressorOption ignoredOption = new HCompressorOption();
            testSubject.initCompressionOption(ignoredOption, 8);
            Assertions.assertEquals(1, ignoredOption.getTileHeight());
        }
    }

    @Test
    public void doTestM13RealRice() throws Exception {
        final File sourceFile = new File("src/test/resources/nom/tam/image/provided/m13real_rice.fits");

        try (final Fits sourceFits = new Fits(sourceFile, true)) {
            final CompressedImageHDU compressedImageHDU = (CompressedImageHDU) sourceFits.getHDU(1);
            final Header compressedHeader = compressedImageHDU.getHeader();
            final ElementType<?> elementType = ElementType.forBitpix(compressedHeader.getIntValue(Compression.ZBITPIX));

            // [100:200, 100:200]
            final int[] cornerStarts = new int[] {100, 100};
            final int[] lengths = new int[] {100, 100};
            final int[] steps = new int[] {1, 1};

            final CompressedImageTiler testSubject = new CompressedImageTiler(compressedImageHDU);
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            try (final ArrayDataOutput arrayDataOutput = new FitsOutputStream(byteArrayOutputStream)) {
                testSubject.getTile(arrayDataOutput, cornerStarts, lengths, steps);
                Assertions.assertEquals(lengths[0] * lengths[1] * elementType.size(),
                        byteArrayOutputStream.toByteArray().length);
                final long expected = (long) lengths[0] * lengths[1] * elementType.size();
                FitsUtil.pad(arrayDataOutput, expected);
                arrayDataOutput.flush();
            }

            final File target = File.createTempFile("m13real_rice_test", ".fits");
            if (target.exists()) {
                Assertions.assertTrue(target.delete());
            }

            final ImageHDU imageHDU = compressedImageHDU.asImageHDU();
            final Header header = imageHDU.getHeader();
            header.setSimple(true);
            header.setNaxes(2);
            header.setNaxis(1, lengths[0]);
            header.setNaxis(2, lengths[1]);
            header.findCard("CRPIX1").setValue(51.5D);
            header.findCard("CRPIX2").setValue(51.5D);
            header.deleteKey("CHECKSUM");
            header.deleteKey("DATASUM");
            header.deleteKey("TFIELDS");
            final ImageHDU hdu = (ImageHDU) FitsFactory.hduFactory(header);

            try (FitsInputStream in = new FitsInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()))) {
                hdu.getData().read(in);
            }

            try (Fits fits = new Fits()) {
                fits.addHDU(hdu);
                fits.write(target);
            }
        }
    }

    @Test
    public void testDecompressRowFitsException() {
        final List<String> columnNames = new ArrayList<>();
        columnNames.add(Compression.COMPRESSED_DATA_COLUMN);
        columnNames.add(Compression.GZIP_COMPRESSED_DATA_COLUMN);

        final ElementType<Buffer> bufferElementType = ElementType.forBitpix(32);
        final Buffer buffer = bufferElementType.newBuffer(12 * 4);

        final byte[] gzipData = new byte[12];
        final Random random = new Random();
        random.nextBytes(gzipData);

        final Object[] rowData = new Object[] {new byte[0], gzipData};
        final CompressedImageTiler testSubject = new CompressedImageTiler(null) {
            @Override
            void init() {
                for (final String columnName : columnNames) {
                    addColumn(columnName);
                }
            }

            @Override
            boolean hasData(final Buffer buffer) {
                return false;
            }

            @Override
            Buffer decompressIntoBuffer(Object[] row, ByteBuffer compressed) {
                return buffer;
            }
        };

        Assertions.assertThrows(FitsException.class, () -> testSubject.decompressRow(0, rowData));
    }

    @Test
    public void testDecompressRowIllegalStateException() {
        final List<String> columnNames = new ArrayList<>();
        columnNames.add(Compression.COMPRESSED_DATA_COLUMN);
        columnNames.add(Compression.GZIP_COMPRESSED_DATA_COLUMN);

        final byte[] gzipData = new byte[12];
        final Random random = new Random();
        random.nextBytes(gzipData);

        final Object[] rowData = new Object[] {new byte[0], gzipData};
        final CompressedImageTiler testSubject = new CompressedImageTiler(null) {
            @Override
            void init() {
                for (final String columnName : columnNames) {
                    addColumn(columnName);
                }
            }

            @Override
            boolean hasData(final Buffer buffer) {
                return false;
            }

            @Override
            Buffer decompressIntoBuffer(Object[] row, ByteBuffer compressed) {
                throw new IllegalStateException("Cannot decompress.");
            }
        };

        Assertions.assertThrows(FitsException.class, () -> testSubject.decompressRow(0, rowData));
    }

    @Test
    public void doGetNoTileData() {
        final List<String> columnNames = new ArrayList<>();
        columnNames.add(Compression.COMPRESSED_DATA_COLUMN);
        columnNames.add(Compression.ZZERO_COLUMN);

        final Object[] rowData = new Object[] {new byte[0]};
        final CompressedImageTiler testSubject = new CompressedImageTiler(null) {
            @Override
            void init() {
                for (final String columnName : columnNames) {
                    addColumn(columnName);
                }
            }

            @Override
            Object decompressRow(int columnIndex, Object[] row) {
                if (columnIndex == 0) {
                    return rowData[0];
                }
                throw new RuntimeException("Should not get here as index should always be zero (0).");
            }

            @Override
            Object[] getRow(int[] positions, int[] tileDimensions) {
                return rowData;
            }

            @Override
            boolean hasData(final Buffer buffer) {
                return true;
            }
        };

        Assertions.assertThrows(FitsException.class,
                () -> testSubject.getDecompressedTileData(new int[] {0, 0}, new int[] {16, 4}));
    }

    @Test
    public void doGetGZIPTileData() throws Exception {
        final List<String> columnNames = new ArrayList<>();
        columnNames.add(Compression.COMPRESSED_DATA_COLUMN);
        columnNames.add(Compression.GZIP_COMPRESSED_DATA_COLUMN);

        final float[] uncompressedArray = new float[16 * 4];
        Arrays.fill(uncompressedArray, 1.0F);

        final byte[] gzipArray = new byte[16];
        final Object[] rowData = new Object[] {new byte[0], gzipArray};
        final CompressedImageTiler testSubject = new CompressedImageTiler(null) {
            @Override
            void init() {
                for (final String columnName : columnNames) {
                    addColumn(columnName);
                }
            }

            @Override
            Object[] getRow(int[] positions, int[] tileDimensions) {
                return rowData;
            }

            @Override
            Object decompressRow(int columnIndex, Object[] row) {
                return uncompressedArray;
            }

            @Override
            boolean hasData(final Buffer buffer) {
                return true;
            }
        };

        final Object multiDimensionalArray = testSubject.getDecompressedTileData(new int[] {0, 0}, new int[] {16, 4});
        Assertions.assertArrayEquals((float[][]) ArrayFuncs.curl(uncompressedArray, new int[] {16, 4}),
                (float[][]) multiDimensionalArray);
    }

    @Test
    public void doGetUncompressedTileData() throws Exception {
        final List<String> columnNames = new ArrayList<>();
        columnNames.add(Compression.COMPRESSED_DATA_COLUMN);
        columnNames.add(Compression.UNCOMPRESSED_DATA_COLUMN);

        final ElementType<Buffer> primitiveType = ElementType.forBitpix(-32);
        final Buffer buffer = primitiveType.newBuffer(12 * 4);
        final float[] decompressedArray = (float[]) Array.newInstance(float.class, 12 * 4);
        Arrays.fill(decompressedArray, 1.0F);
        primitiveType.putArray(buffer, decompressedArray);

        final Object[] rowData = new Object[] {new byte[0], decompressedArray};
        final CompressedImageTiler testSubject = new CompressedImageTiler(null) {
            @Override
            void init() {
                for (final String columnName : columnNames) {
                    addColumn(columnName);
                }
            }

            @Override
            Object[] getRow(int[] positions, int[] tileDimensions) {
                return rowData;
            }

            @Override
            boolean hasData(final Buffer buffer) {
                return true;
            }
        };

        final Object multiDimensionalArray = testSubject.getDecompressedTileData(new int[] {0, 0}, new int[] {12, 4});
        Assertions.assertArrayEquals((float[][]) ArrayFuncs.curl(decompressedArray, new int[] {12, 4}),
                (float[][]) multiDimensionalArray);
    }

    @Test
    public void doTestGetTileError() throws Exception {
        final CompressedImageTiler testSubject = new CompressedImageTiler(null) {
            @Override
            void init() {

            }

            @Override
            int[] getImageDimensions() {
                return new int[] {1, 2, 4};
            }
        };

        Assertions.assertThrows(IOException.class,
                () -> testSubject.getTile(null, new int[] {1, 1}, new int[0], new int[0]));
        Assertions.assertThrows(IOException.class,
                () -> testSubject.getTile(null, new int[] {1, 1, 3}, new int[0], new int[0]));
        Assertions.assertThrows(IOException.class,
                () -> testSubject.getTile(null, new int[] {1, 1, 3}, new int[] {2, 2, 2}, new int[0]));
        Assertions.assertThrows(IOException.class,
                () -> testSubject.getTile(null, new int[] {1, 1, 3}, new int[] {2, 2, 2}, new int[] {1, 1, 1}));

        try (final ArrayDataOutput output = new FitsOutputStream(new ByteArrayOutputStream())) {
            Assertions.assertThrows(IOException.class,
                    () -> testSubject.getTile(output, new int[] {-1, 1, 3}, new int[] {2, 2, 2}, new int[] {1, 1, 1}));
        }

        try (final ArrayDataOutput output = new FitsOutputStream(new ByteArrayOutputStream())) {
            Assertions.assertThrows(Exception.class,
                    () -> testSubject.getTile(output, new int[] {1, 1, 3}, new int[] {2, 2, 2}, new int[] {1, 1, 1}));
        }
    }

    @Test
    public void doTestGetTileOffsetError() {
        final CompressedImageTiler testSubject = new CompressedImageTiler(null) {
            @Override
            void init() {

            }

            @Override
            int getNumberOfDimensions() {
                return 2;
            }
        };

        // Start at 21, 21 and get offsets for tiles of size 5x5.
        Assertions.assertArrayEquals(new int[] {1, 1}, testSubject.getTileOffsets(new int[] {21, 21}, new int[] {5, 5}));

        // Start at 21, 21 and get offsets for tiles of size 5x5.
        Assertions.assertArrayEquals(new int[] {4, 4}, testSubject.getTileOffsets(new int[] {19, 4}, new int[] {5, 5}));
    }

    @Test
    public void doTestGetBlockSize() throws Exception {
        CompressedImageTiler testSubject = new CompressedImageTiler(null) {
            @Override
            void init() {

            }

            @Override
            int getNumberOfDimensions() {
                return 0;
            }
        };

        Assertions.assertEquals(CompressedImageTiler.DEFAULT_BLOCK_SIZE, testSubject.getBlockSize());

        final Header header = new Header();
        header.addValue(Compression.ZNAMEn.n(2), Compression.BLOCKSIZE);
        header.addValue(Compression.ZVALn.n(2), 16);
        testSubject = new CompressedImageTiler(null) {
            @Override
            void init() {

            }

            @Override
            Header getHeader() {
                return header;
            }

            @Override
            int getNumberOfDimensions() {
                return 2;
            }
        };

        Assertions.assertEquals(16, testSubject.getBlockSize());
    }

    @Test
    public void doTestGetBaseType() {
        CompressedImageTiler testSubject = new CompressedImageTiler(null) {
            @Override
            void init() {

            }

            @Override
            int getZBitPix() {
                // Bad bit pix.
                return 0;
            }
        };

        Assertions.assertEquals("UnknownType", testSubject.getBaseType().getClass().getSimpleName());

        testSubject = new CompressedImageTiler(null) {
            @Override
            void init() {

            }

            @Override
            int getZBitPix() {
                // Bad bit pix.
                return 31;
            }
        };

        Assertions.assertEquals(ElementType.forBitpix(32).type(), testSubject.getBaseType().type());
    }

    @Test
    public void doTestGetTileDimension() {
        final CompressedImageTiler testSubject = new CompressedImageTiler(null) {
            @Override
            void init() {
            }

            @Override
            int getNumberOfDimensions() {
                return 2;
            }
        };

        Assertions.assertThrows(FitsException.class, () -> testSubject.getTileDimensionLength(-1));
        Assertions.assertThrows(FitsException.class, () -> testSubject.getTileDimensionLength(3));
    }

    @Test
    public void doTestGetTileOutArray() throws Exception {
        final File sourceFile = new File("src/test/resources/nom/tam/image/provided/m13real_rice.fits");
        try (final Fits sourceFits = new Fits(sourceFile, true);
                final ArrayDataOutput output = new FitsOutputStream(new ByteArrayOutputStream())) {

            final CompressedImageHDU compressedImageHDUFromFile = ((CompressedImageHDU) sourceFits.getHDU(1));
            final CompressedImageHDU compressedImageHDU = new CompressedImageHDU(compressedImageHDUFromFile.getHeader(),
                    compressedImageHDUFromFile.getData());
            final CompressedImageTiler testSubject = new CompressedImageTiler(compressedImageHDU) {
                @Override
                void init() {
                }

                @Override
                int getNumberOfDimensions() {
                    return 2;
                }
            };

            Assertions.assertThrows(IOException.class, () -> testSubject.getTile(output, new int[] {2, 2}, new int[] {4}));
        }
    }

    @Test
    public void doTestInitRowRiceQuantizOption() {
        final CompressedImageTiler testSubject = new CompressedImageTiler(null) {
            @Override
            void init() {
                addColumn(Compression.COMPRESSED_DATA_COLUMN);
                addColumn(Compression.ZSCALE_COLUMN);
                addColumn(Compression.ZZERO_COLUMN);
            }
        };

        final Object[] row = new Object[] {new byte[] {1, 2, 3}, new double[] {3.45D}, new double[] {6.78D}};
        final QuantizeOption testOption = new QuantizeOption(new RiceCompressOption());
        testSubject.initRowOption(testOption, row);
        Assertions.assertEquals(3.45D, testOption.getBScale(), 0.0D);
        Assertions.assertEquals(6.78D, testOption.getBZero(), 0.0D);

        // Should pass through ignored.
        final RiceCompressOption riceOption = new RiceCompressOption();
        testSubject.initRowOption(riceOption, row);
    }

    @Test
    public void doTestInitRowRiceQuantizeOptionDefaultBScale() {
        final CompressedImageTiler testSubject = new CompressedImageTiler(null) {
            @Override
            void init() {
                addColumn(Compression.COMPRESSED_DATA_COLUMN);
                addColumn(Compression.ZZERO_COLUMN);
            }
        };

        final Object[] row = new Object[] {new byte[] {1, 2, 3}, new double[] {6.78D}};
        final QuantizeOption testOption = new QuantizeOption(new RiceCompressOption());
        testSubject.initRowOption(testOption, row);

        Assertions.assertEquals(Double.NaN, testOption.getBScale(), 0.0D);
        Assertions.assertEquals(6.78D, testOption.getBZero(), 0.0D);
    }

    @Test
    public void doTestInitRowRiceQuantizOptionDefaultBZero() {
        final CompressedImageTiler testSubject = new CompressedImageTiler(null) {
            @Override
            void init() {
                addColumn(Compression.COMPRESSED_DATA_COLUMN);
                addColumn(Compression.ZSCALE_COLUMN);
            }
        };

        final Object[] row = new Object[] {new byte[] {1, 2, 3}, new double[] {2.34D}};
        final QuantizeOption testOption = new QuantizeOption(new RiceCompressOption());
        testSubject.initRowOption(testOption, row);

        Assertions.assertEquals(2.34D, testOption.getBScale(), 0.0D);
        Assertions.assertEquals(Double.NaN, testOption.getBZero(), 0.0D);
    }

    @Test
    public void doTestStreamTileNonValidSegment() throws Exception {
        final CompressedImageTiler testSubject = new CompressedImageTiler(null) {

            @Override
            void init() {
            }

            @Override
            int getZBitPix() {
                return 32;
            }

            @Override
            int[] getTileDimensions() throws FitsException {
                return new int[] {10, 10};
            }

            @Override
            int[] getImageDimensions() {
                return new int[] {100, 100};
            }

            @Override
            int getNumberOfDimensions() {
                return 2;
            }

            @Override
            Object getDecompressedTileData(int[] positions, int[] tileDimensions) throws FitsException {
                return new int[10][10];
            }

            @Override
            int[] getTileIndexes(int[] pixelPositions, int[] tileDimensions) {
                return new int[] {0, 0};
            }
        };
        final ByteArrayOutputStream outputByteStream = new ByteArrayOutputStream();
        try (final ArrayDataOutput output = new FitsOutputStream(outputByteStream)) {
            testSubject.getTile(output, new int[] {91, 9}, new int[] {2, 2}, new int[] {1, 1});
        }
    }

    @Test
    public void doTestValidSegment() {
        Assertions.assertFalse(CompressedImageTiler.isValidSegment(-2, 1, 8));
        Assertions.assertFalse(CompressedImageTiler.isValidSegment(8, 1, 6));
        Assertions.assertTrue(CompressedImageTiler.isValidSegment(2, 1, 10));
    }

    @Test
    public void doTestFZStep() throws Exception {
        final File sourceFile = new File("src/test/resources/nom/tam/image/provided/m13real_rice.fits");

        try (final Fits sourceFits = new Fits(sourceFile, true)) {
            final CompressedImageHDU compressedImageHDU = (CompressedImageHDU) sourceFits.getHDU(1);
            final Header compressedHeader = compressedImageHDU.getHeader();
            final ElementType<?> bufferElementType = ElementType
                    .forBitpix(compressedHeader.getIntValue(Compression.ZBITPIX));

            // [10:69:2, 10:49:2]
            final int[] cornerStarts = new int[] {10, 10};
            final int[] lengths = new int[] {40, 60};
            final int[] steps = new int[] {2, 2};
            final CompressedImageTiler testSubject = new CompressedImageTiler(compressedImageHDU);
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            try (final ArrayDataOutput arrayDataOutput = new FitsOutputStream(byteArrayOutputStream)) {
                final long expected = ((long) (lengths[0] / steps[0]) * (lengths[1] / steps[1])) * bufferElementType.size();
                testSubject.getTile(arrayDataOutput, cornerStarts, lengths, steps);
                Assertions.assertEquals(expected, byteArrayOutputStream.toByteArray().length);
                FitsUtil.pad(arrayDataOutput, expected);
                arrayDataOutput.flush();
            }

            try (final Fits fits = new Fits()) {
                final File target = File.createTempFile("m13real_rice_test_step", ".fits");
                if (target.exists()) {
                    Assertions.assertTrue(target.delete());
                }

                final ImageHDU imageHDU = compressedImageHDU.asImageHDU();
                final Header header = imageHDU.getHeader();
                header.setSimple(true);
                header.setNaxes(2);
                header.setNaxis(1, lengths[1] / steps[1]);
                header.setNaxis(2, lengths[0] / steps[0]);
                header.findCard("CRPIX1").setValue(7.125e+01D);
                header.findCard("CRPIX2").setValue(7.125e+01D);
                header.deleteKey("CHECKSUM");
                header.deleteKey("DATASUM");
                header.deleteKey("TFIELDS");
                final ImageHDU hdu = (ImageHDU) FitsFactory.hduFactory(header);

                try (FitsInputStream in = new FitsInputStream(
                        new ByteArrayInputStream(byteArrayOutputStream.toByteArray()))) {
                    hdu.getData().read(in);
                    fits.addHDU(hdu);
                    fits.write(target);
                }
                fits.close();

                final ImageHDU resultImageHDU = (ImageHDU) fits.getHDU(0);
                Assertions.assertEquals(hdu.getData(), resultImageHDU.getData());
            }
        }

    }

    @Test
    public void testGetImageAxes() throws Exception {
        final File sourceFile = new File("src/test/resources/nom/tam/image/provided/m13real_rice.fits");
        try (final Fits sourceFits = new Fits(sourceFile, true)) {
            final CompressedImageHDU compressedImageHDU = (CompressedImageHDU) sourceFits.getHDU(1);

            Assertions.assertArrayEquals(new int[] {300, 300}, compressedImageHDU.getImageAxes());

            compressedImageHDU.getHeader().findCard(Compression.ZNAXIS).setValue(0);
            Assertions.assertNull(compressedImageHDU.getImageAxes());

            compressedImageHDU.getHeader().findCard(Compression.ZNAXIS).setValue(-1);
            Assertions.assertThrows(FitsException.class, () -> compressedImageHDU.getImageAxes());

            compressedImageHDU.getHeader().findCard(Compression.ZNAXIS).setValue(CompressedImageHDU.MAX_NAXIS_ALLOWED + 1);
            Assertions.assertThrows(FitsException.class, () -> compressedImageHDU.getImageAxes());
        }
    }
}
