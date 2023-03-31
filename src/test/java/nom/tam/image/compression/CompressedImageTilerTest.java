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

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2022 nom-tam-fits
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
import nom.tam.fits.compression.algorithm.quant.QuantizeOption;
import nom.tam.fits.compression.algorithm.rice.RiceCompressOption;
import nom.tam.fits.compression.provider.param.rice.RiceCompressParameters;
import nom.tam.fits.header.Compression;
import nom.tam.image.compression.hdu.CompressedImageData;
import nom.tam.image.compression.hdu.CompressedImageHDU;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.ColumnTable;
import nom.tam.util.FitsInputStream;
import nom.tam.util.FitsOutputStream;
import nom.tam.util.type.ElementType;
import org.junit.Assert;
import org.junit.Test;

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

import static org.junit.Assert.assertEquals;

public class CompressedImageTilerTest {
    private static final Logger LOGGER = Logger.getLogger(CompressedImageTilerTest.class.getName());

    static {
        LOGGER.setLevel(Level.INFO);
    }

    @Test
    public void doCompressedImageTest() throws Exception {
        final File sourceFile = new File("src/test/resources/nom/tam/image/provided/m13real_rice.fits");
        final Fits sourceFits = new Fits(sourceFile, true);

        final CompressedImageHDU cfitsioTable = (CompressedImageHDU) sourceFits.getHDU(1);
        final ElementType<?> elementType =
                ElementType.forBitpix(cfitsioTable.getHeader().getIntValue(Compression.ZBITPIX));
        final CompressedImageTiler testSubject = new CompressedImageTiler(cfitsioTable);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ArrayDataOutput arrayDataOutput = new FitsOutputStream(byteArrayOutputStream);
        final int[] cornerStarts = new int[]{10, 10};
        final int[] lengths = new int[]{20, 20};
        final int[] steps = new int[]{1, 1};
        testSubject.getTile(arrayDataOutput, cornerStarts, lengths, steps);
        arrayDataOutput.flush();
        arrayDataOutput.close();

        byte[] data = byteArrayOutputStream.toByteArray();
        assertEquals(String.format("Wrong data (%d)", data.length),
                     (lengths[0] * lengths[1] * elementType.size()), data.length);

        Assert.assertArrayEquals("Wrong data shape.", (float[][]) cfitsioTable.asImageHDU().getData().getData(),
                                 (float[][]) testSubject.getCompleteImage());
        try {
            testSubject.getTile(cornerStarts, lengths);
            Assert.fail("Should throw UnsupportedOperationException.");
        } catch (UnsupportedOperationException unsupportedOperationException) {
            // Good.
        }

        try {
            testSubject.getTile(Array.newInstance(Integer.class, 20 * 20), cornerStarts, lengths);
            Assert.fail("Should throw UnsupportedOperationException.");
        } catch (UnsupportedOperationException unsupportedOperationException) {
            // Good.
        }

        byteArrayOutputStream = new ByteArrayOutputStream();
        arrayDataOutput = new FitsOutputStream(byteArrayOutputStream);
        testSubject.getTile(arrayDataOutput, cornerStarts, lengths);
        arrayDataOutput.flush();
        arrayDataOutput.close();

        data = byteArrayOutputStream.toByteArray();
        assertEquals(String.format("Wrong data secondary call (%d)", data.length),
                     (lengths[0] * lengths[1] * elementType.size()), data.length);
    }

    @Test
    public void doTestCompleteImageFail() throws Exception {
        final File sourceFile = new File("src/test/resources/nom/tam/image/provided/m13real_rice.fits");
        try (final Fits sourceFits = new Fits(sourceFile, true)) {

            final CompressedImageHDU cfitsioTable = (CompressedImageHDU) sourceFits.getHDU(1);
            final CompressedImageData compressedImageData = new CompressedImageData(cfitsioTable.getHeader()) {
                @Override
                public ColumnTable<SaveState> getData() throws FitsException {
                    throw new FitsException("getData");
                }
            };
            final CompressedImageHDU compressedImageHDU = new CompressedImageHDU(cfitsioTable.getHeader(),
                                                                                 compressedImageData);

            final CompressedImageTiler testSubject = new CompressedImageTiler(compressedImageHDU);

            try {
                testSubject.getCompleteImage();
                Assert.fail("Doesn't support UNKNOWN writes.");
            } catch (IOException ioException) {
                // Good.
            }
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
            Assert.assertEquals("Wrong block size", 32, option.getBlockSize());
        }
    }

    @Test
    public void doTestM13RealRice() throws Exception {
        final File sourceFile = new File("src/test/resources/nom/tam/image/provided/m13real_rice.fits");
        final Fits sourceFits = new Fits(sourceFile, true);
        final CompressedImageHDU compressedImageHDU = (CompressedImageHDU) sourceFits.getHDU(1);
        final Header compressedHeader = compressedImageHDU.getHeader();
        final ElementType<?> elementType = ElementType.forBitpix(compressedHeader.getIntValue(Compression.ZBITPIX));

        // [100:200, 100:200]
        final int[] cornerStarts = new int[]{100, 100};
        final int[] lengths = new int[]{100, 100};
        final int[] steps = new int[]{1, 1};
        final CompressedImageTiler testSubject = new CompressedImageTiler(compressedImageHDU);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ArrayDataOutput arrayDataOutput = new FitsOutputStream(byteArrayOutputStream);
        testSubject.getTile(arrayDataOutput, cornerStarts, lengths, steps);
        Assert.assertEquals("Wrong length of output.", lengths[0] * lengths[1] * elementType.size(),
                            byteArrayOutputStream.toByteArray().length);
        final long expected = (long) lengths[0] * lengths[1] * elementType.size();
        FitsUtil.pad(arrayDataOutput, expected);
        arrayDataOutput.flush();
        arrayDataOutput.close();

        final Fits fits = new Fits();
        final File target = File.createTempFile("m13real_rice_test", ".fits");
        if (target.exists()) {
            Assert.assertTrue("File delete failed.", target.delete());
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
        hdu.getData().read(new FitsInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray())));
        fits.addHDU(hdu);
        fits.write(target);
        fits.close();
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

        final Object[] rowData = new Object[]{new byte[0], gzipData};
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

        try {
            testSubject.decompressRow(0, rowData);
            Assert.fail("Should throw FitsException");
        } catch (FitsException fitsException) {
            Assert.assertEquals("Wrong message",
                                "No tile available at column 0: (" + Arrays.deepToString(rowData) + ")",
                                fitsException.getMessage());
            // Good!
        }
    }

    @Test
    public void testDecompressRowIllegalStateException() {
        final List<String> columnNames = new ArrayList<String>();
        columnNames.add(Compression.COMPRESSED_DATA_COLUMN);
        columnNames.add(Compression.GZIP_COMPRESSED_DATA_COLUMN);

        final byte[] gzipData = new byte[12];
        final Random random = new Random();
        random.nextBytes(gzipData);

        final Object[] rowData = new Object[]{new byte[0], gzipData};
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

        try {
            testSubject.decompressRow(0, rowData);
            Assert.fail("Should throw FitsException");
        } catch (FitsException fitsException) {
            Assert.assertEquals("Wrong message", "Cannot decompress.", fitsException.getMessage());
            // Good!
        }
    }

    @Test
    public void doGetNoTileData() {
        final List<String> columnNames = new ArrayList<String>();
        columnNames.add(Compression.COMPRESSED_DATA_COLUMN);
        columnNames.add(Compression.ZZERO_COLUMN);

        final Object[] rowData = new Object[]{new byte[0]};
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
                } else {
                    throw new RuntimeException("Should not get here as index should always be zero (0).");
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

        try {
            testSubject.getTileData(new int[] {0, 0}, new int[] {16, 4});
        } catch (FitsException fitsException) {
            Assert.assertEquals("Wrong message.", "Nothing in row to read: ([[]]).",
                                fitsException.getMessage());
            // Good!
        }
    }

    @Test
    public void doGetGZIPTileData() throws Exception {
        final List<String> columnNames = new ArrayList<String>();
        columnNames.add(Compression.COMPRESSED_DATA_COLUMN);
        columnNames.add(Compression.GZIP_COMPRESSED_DATA_COLUMN);

        final float[] uncompressedArray = new float[16 * 4];
        Arrays.fill(uncompressedArray, 1.0F);

        final byte[] gzipArray = new byte[16];
        final Object[] rowData = new Object[]{new byte[0], gzipArray};
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

        final Object multiDimensionalArray = testSubject.getTileData(new int[]{0, 0}, new int[]{16, 4});
        Assert.assertArrayEquals("Wrong array.",
                                 (float[][]) ArrayFuncs.curl(uncompressedArray, new int[]{16, 4}),
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

        final Object[] rowData = new Object[]{new byte[0], decompressedArray};
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

        final Object multiDimensionalArray = testSubject.getTileData(new int[]{0, 0}, new int[]{12, 4});
        Assert.assertArrayEquals("Wrong array.",
                                 (float[][]) ArrayFuncs.curl(decompressedArray, new int[]{12, 4}),
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

        try {
            testSubject.getTile(null, new int[] {1, 1}, new int[0], new int[0]);
            Assert.fail("Should throw IOException.");
        } catch (IOException ioException) {
            Assert.assertEquals("Wrong message.", "Inconsistent sub-image request",
                                ioException.getMessage());
        }

        try {
            testSubject.getTile(null, new int[] {1, 1, 3}, new int[0], new int[0]);
            Assert.fail("Should throw IOException.");
        } catch (IOException ioException) {
            Assert.assertEquals("Wrong message.", "Inconsistent sub-image request",
                                ioException.getMessage());
        }

        try {
            testSubject.getTile(null, new int[] {1, 1, 3}, new int[] {2, 2, 2}, new int[0]);
            Assert.fail("Should throw IOException.");
        } catch (IOException ioException) {
            Assert.assertEquals("Wrong message.", "Inconsistent sub-image request",
                                ioException.getMessage());
        }

        try {
            testSubject.getTile(null, new int[] {1, 1, 3}, new int[] {2, 2, 2}, new int[]{1, 1, 1});
            Assert.fail("Should throw IOException.");
        } catch (IOException ioException) {
            Assert.assertEquals("Wrong message.", "Attempt to write to null data output",
                                ioException.getMessage());
        }

        try {
            final ArrayDataOutput output = new FitsOutputStream(new ByteArrayOutputStream());
            testSubject.getTile(output, new int[] {-1, 1, 3}, new int[] {2, 2, 2}, new int[]{1, 1, 1});
            Assert.fail("Should throw IOException.");
        } catch (IOException ioException) {
            Assert.assertEquals("Wrong message.", "Sub-image not within image",
                                ioException.getMessage());
        }

        try {
            final ArrayDataOutput output = new FitsOutputStream(new ByteArrayOutputStream());
            testSubject.getTile(output, new int[] {1, 1, 3}, new int[] {2, 2, 2}, new int[]{1, 1, 1});
            Assert.fail("Should throw IOException.");
        } catch (IOException ioException) {
            Assert.assertEquals("Wrong message.", "Sub-image not within image",
                                ioException.getMessage());
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
        Assert.assertArrayEquals("Wrong offset.", new int[]{1, 1},
                                 testSubject.getTileOffsets(new int[] {21, 21}, new int[] {5, 5}));

        // Start at 21, 21 and get offsets for tiles of size 5x5.
        Assert.assertArrayEquals("Wrong offset.", new int[]{4, 4},
                                 testSubject.getTileOffsets(new int[] {19, 4}, new int[] {5, 5}));
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

        Assert.assertEquals("Wrong block size.", CompressedImageTiler.DEFAULT_BLOCK_SIZE,
                            testSubject.getBlockSize());

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

        Assert.assertEquals("Wrong block size.", 16, testSubject.getBlockSize());
    }

    @Test
    public void doTestGetBaseType() {
        final CompressedImageTiler testSubject = new CompressedImageTiler(null) {
            @Override
            void init() {

            }

            @Override
            int getZBitPix() {
                // Bad bit pix.
                return 31;
            }
        };

        Assert.assertEquals("Wrong base type.", ElementType.forBitpix(32),
                            testSubject.getBaseType());
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

        try {
            testSubject.getTileDimensionLength(-1);
            Assert.fail("Should throw FitsException.");
        } catch (FitsException fitsException) {
            // Good.
        }

        try {
            testSubject.getTileDimensionLength(3);
            Assert.fail("Should throw FitsException.");
        } catch (FitsException fitsException) {
            // Good.
        }
    }

    @Test
    public void doTestGetTileOutArray() {
        final CompressedImageTiler testSubject = new CompressedImageTiler(null) {
            @Override
            void init() {

            }

            @Override
            int getNumberOfDimensions() {
                return 2;
            }

            @Override
            public void getTile(Object output, int[] corners, int[] lengths, int[] steps) throws IOException {
                throw new IOException("Test exception");
            }
        };

        try {
            final ArrayDataOutput output = new FitsOutputStream(new ByteArrayOutputStream());
            testSubject.getTile(output, new int[]{2, 2}, new int[]{4, 4});
        } catch (IOException ioException) {
            Assert.assertEquals("Wrong message.", "Test exception", ioException.getMessage());
            // Good!
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

        final Object[] row = new Object[] {
                new byte[]{1, 2, 3},
                new double[]{3.45D},
                new double[]{6.78D}
        };
        final QuantizeOption testOption = new QuantizeOption(new RiceCompressOption());
        testSubject.initRowOption(testOption, row);

        Assert.assertEquals("Wrong BScale", 3.45D, testOption.getBScale(), 0.0D);
        Assert.assertEquals("Wrong BZero", 6.78D, testOption.getBZero(), 0.0D);
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

        final Object[] row = new Object[] {
                new byte[]{1, 2, 3},
                new double[]{6.78D}
        };
        final QuantizeOption testOption = new QuantizeOption(new RiceCompressOption());
        testSubject.initRowOption(testOption, row);

        Assert.assertEquals("Wrong BScale", Double.NaN, testOption.getBScale(), 0.0D);
        Assert.assertEquals("Wrong BZero", 6.78D, testOption.getBZero(), 0.0D);
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

        final Object[] row = new Object[] {
                new byte[]{1, 2, 3},
                new double[]{2.34D}
        };
        final QuantizeOption testOption = new QuantizeOption(new RiceCompressOption());
        testSubject.initRowOption(testOption, row);

        Assert.assertEquals("Wrong BScale", 2.34D, testOption.getBScale(), 0.0D);
        Assert.assertEquals("Wrong BZero", Double.NaN, testOption.getBZero(), 0.0D);
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
                return new int[]{10, 10};
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
            Object getTileData(int[] positions, int[] tileDimensions) throws FitsException {
                return new int[10][10];
            }

            @Override
            int[] getTileIndexes(int[] pixelPositions, int[] tileDimensions) {
                return new int[]{0, 0};
            }
        };
        final ByteArrayOutputStream outputByteStream = new ByteArrayOutputStream();
        final ArrayDataOutput output = new FitsOutputStream(outputByteStream);
        testSubject.getTile(output, new int[]{91, 9}, new int[]{2, 2}, new int[]{1, 1});
    }

    @Test
    public void doTestFZStep() throws Exception {
        final File sourceFile = new File("src/test/resources/nom/tam/image/provided/m13real_rice.fits");
        final Fits sourceFits = new Fits(sourceFile, true);
        final CompressedImageHDU compressedImageHDU = (CompressedImageHDU) sourceFits.getHDU(1);
        final Header compressedHeader = compressedImageHDU.getHeader();
        final ElementType<?> bufferElementType =
                ElementType.forBitpix(compressedHeader.getIntValue(Compression.ZBITPIX));

        // [10:69:2, 10:49:2]
        final int[] cornerStarts = new int[]{10, 10};
        final int[] lengths = new int[]{40, 60};
        final int[] steps = new int[]{2, 2};
        final CompressedImageTiler testSubject = new CompressedImageTiler(compressedImageHDU);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ArrayDataOutput arrayDataOutput = new FitsOutputStream(byteArrayOutputStream);
        final long expected = ((long) (lengths[0] / steps[0]) * (lengths[1] / steps[1])) * bufferElementType.size();
        testSubject.getTile(arrayDataOutput, cornerStarts, lengths, steps);
        Assert.assertEquals("Wrong length of output.",
                            expected, byteArrayOutputStream.toByteArray().length);
        FitsUtil.pad(arrayDataOutput, expected);
        arrayDataOutput.flush();
        arrayDataOutput.close();

        final Fits fits = new Fits();
        final File target = File.createTempFile("m13real_rice_test_step", ".fits");
        if (target.exists()) {
            Assert.assertTrue("File delete failed.", target.delete());
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
        hdu.getData().read(new FitsInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray())));
        fits.addHDU(hdu);
        fits.write(target);
        fits.close();

        final ImageHDU resultImageHDU = (ImageHDU) fits.getHDU(0);
        Assert.assertEquals("Result data is incorrect.", hdu.getData(), resultImageHDU.getData());
    }
}
