package nom.tam.fits.test;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.ImageData;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.header.NonStandard;
import nom.tam.fits.header.hierarch.BlanksDotHierarchKeyFormatter;
import nom.tam.fits.header.hierarch.StandardIHierarchKeyFormatter;
import nom.tam.fits.util.BlackBoxImages;
import nom.tam.image.ImageTiler;
import nom.tam.util.BufferedFile;
import nom.tam.util.Cursor;
import nom.tam.util.SafeClose;

public class UserProvidedTest {

    private boolean longStringsEnabled;

    private boolean useHierarch;

    private boolean isAllowTerminalJunk;

    private boolean isAllowHeaderRepairs;

    @Before
    public void before() {
        longStringsEnabled = FitsFactory.isLongStringsEnabled();
        useHierarch = FitsFactory.getUseHierarch();
        isAllowTerminalJunk = FitsFactory.getAllowTerminalJunk();
        isAllowHeaderRepairs = FitsFactory.isAllowHeaderRepairs();
        FitsFactory.setHierarchFormater(new StandardIHierarchKeyFormatter());
    }

    @After
    public void after() {
        FitsFactory.setLongStringsEnabled(longStringsEnabled);
        FitsFactory.setUseHierarch(useHierarch);
        FitsFactory.setAllowTerminalJunk(isAllowTerminalJunk);
    }

    @Test
    public void testRewriteableHierarchImageWithLongStrings() throws Exception {
        boolean longStringsEnabled = FitsFactory.isLongStringsEnabled();
        boolean useHierarch = FitsFactory.getUseHierarch();
        try {
            FitsFactory.setUseHierarch(true);
            FitsFactory.setLongStringsEnabled(true);

            String filename = "src/test/resources/nom/tam/image/provided/issue49test.fits";
            Fits fits = new Fits(filename);
            Header headerRewriter = fits.getHDU(0).getHeader();
            // the real test is if this throws an exception, it should not!
            headerRewriter.rewrite();
            fits.close();
        } finally {
            FitsFactory.setLongStringsEnabled(longStringsEnabled);
            FitsFactory.setUseHierarch(useHierarch);

        }
    }

    private static ArrayList<Header> getHeaders(String filename) throws IOException, FitsException {
        ArrayList<Header> list = new ArrayList<Header>();
        Fits f = null;
        try {
            f = new Fits(filename);
            BasicHDU<?> readHDU = f.readHDU();
            while (readHDU != null) {
                f.deleteHDU(0);
                list.add(readHDU.getHeader());
                readHDU = f.readHDU();
            }
        } finally {
            SafeClose.close(f);
        }
        return list;
    }

    @Test
    public void testDoRead() throws FileNotFoundException, Exception {
        FitsFactory.setLongStringsEnabled(true);
        ArrayList<Header> headers = getHeaders(BlackBoxImages.getBlackBoxImage("bad.fits"));
        Assert.assertTrue(headers.get(0).getStringValue("INFO____").endsWith("&"));
        Assert.assertEquals(6, headers.size());
    }

    @Test
    public void testCommentStyleInput() throws FitsException, IOException {
        float[][] data = new float[500][500];
        Fits f = null;
        try {
            f = new Fits();
            BasicHDU<?> hdu = FitsFactory.hduFactory(data);
            hdu.getHeader().insertCommentStyle("        ", "---------------FITS Data Generator---------------");
            hdu.getHeader().insertCommentStyle("        ", "This product is generated by me.");
            hdu.getHeader().addValue("NAXIS3", 1000, "Actual number of energy channels");
            f.addHDU(hdu);
            BufferedFile bf = null;
            try {
                bf = new BufferedFile("target/testCommentStyleInput.fits", "rw");
                f.write(bf);
            } finally {
                SafeClose.close(bf);
            }
        } finally {
            SafeClose.close(f);
        }
        try {
            f = new Fits("target/testCommentStyleInput.fits");
            Header header = f.readHDU().getHeader();
            int foundComments = 0;
            Cursor<String, HeaderCard> iter = header.iterator();
            while (iter.hasNext()) {
                HeaderCard headerCard = iter.next();
                String comment = headerCard.getComment();
                if (comment != null) {
                    if (comment.contains("This product is generated by me.")) {
                        foundComments++;
                    }
                    if (comment.contains("---------------FITS Data Generator---------------")) {
                        foundComments++;
                    }
                }
            }
            Assert.assertEquals(2, foundComments);
        } finally {
            SafeClose.close(f);
        }
    }

    @Test
    public void testSpecialLongStringCaseWithDuplicateHierarch() throws FitsException, IOException {
        FitsFactory.setUseHierarch(true);
        int hierarchKeys = 0;
        Fits f = null;
        try {
            f = new Fits(BlackBoxImages.getBlackBoxImage("16913-1.fits"));
            Header header = f.readHDU().getHeader();
            Assert.assertEquals("", header.findCard("META_0").getValue());
            Cursor<String, HeaderCard> iter = header.iterator();
            while (iter.hasNext()) {
                HeaderCard headerCard = iter.next();
                if (headerCard.getKey().startsWith(NonStandard.HIERARCH.key())) {
                    hierarchKeys++;
                }
            }
            Assert.assertEquals(10, hierarchKeys);
        } finally {
            SafeClose.close(f);
        }
    }

    @Test
    public void testMultidimentionalStringArray() throws FitsException, IOException {
        Fits fits = null;
        try {
            fits = new Fits(BlackBoxImages.getBlackBoxImage("map_one_source_a_level_1_cal.fits"));
            BasicHDU<?> hdu = fits.getHDU(1);
            nom.tam.fits.TableData data = (nom.tam.fits.TableData) hdu.getData();

            StringBuilder builder = new StringBuilder("StringColumns:\n");
            for (int i = 0; i < data.getNCols(); i++) {
                printArray(data.getColumn(i), 0, builder);
            }
            Assert.assertEquals("StringColumns:\n"//
                    + "[2]\n"//
                    + "[2]\n"//
                    + "[S2]\n"//
                    + "[S2]\n"//
                    + "[2]\n"//
                    + "[2]\n"//
                    + "[2]\n"//
                    + "[2]\n"//
                    + "[2]\n"//
                    + "[2]\n"//
                    + "[2]\n"//
                    + "[2]\n"//
                    + "[\n"//
                    + "[S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3]\n"//
                    + "]\n"//
                    + "[\n"//
                    + "[S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3]\n"//
                    + "]\n"//
                    + "[\n"//
                    + "[S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3]\n"//
                    + "]\n"//
                    + "[\n"//
                    + "[S3, S3, S3, S3]\n"//
                    + "]\n"//
                    + "[\n"//
                    + "[S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3]\n"//
                    + "]\n"//
                    + "[\n"//
                    + "[S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3]\n"//
                    + "]\n"//
                    + "[\n"//
                    + "[S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3, S3]\n"//
                    + "]\n"//
                    + "[S2]\n"//
                    + "[S2]\n", builder.toString());

        } finally {
            SafeClose.close(fits);
        }
    }

    private void printArray(Object array, int indent, StringBuilder builder) {
        indent++;
        if (array.getClass().isArray()) {
            if (builder.charAt(builder.length() - 1) == '[') {
                builder.append("\n");
            }
            builder.append("[");
            int length = Array.getLength(array);
            for (int index = 0; index < length; index++) {
                if (index > 0) {
                    builder.append(", ");
                }
                printArray(Array.get(array, index), indent, builder);
            }
            builder.append("]\n");
        } else if (array instanceof String) {
            builder.append("S");
            builder.append(indent);
        } else {
            builder.append(indent);
        }
    }

    @Test
    public void testBlanksSituation() throws Exception {
        FitsFactory.setLongStringsEnabled(true);
        FitsFactory.setUseHierarch(true);
        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(2));
        Fits fitsSrc = null;
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(BlackBoxImages.getBlackBoxImage("16913-1.fits"));
            BasicHDU<?> bhduMain = null;

            fitsSrc = new Fits(stream);
            bhduMain = fitsSrc.readHDU(); // Product
            Cursor<String, HeaderCard> iterator = bhduMain.getHeader().iterator();
            while (iterator.hasNext()) {
                HeaderCard headerCard = (HeaderCard) iterator.next();
                String start = headerCard.getKey();
                if (start.isEmpty()) {
                    "".toString();
                }
                start = (start + "         ").substring(0, 8);
                Assert.assertTrue(headerCard.toString().startsWith(start));
            }

        } finally {
            SafeClose.close(fitsSrc);
            SafeClose.close(stream);
        }
    }

    @Test
    public void testIOTransparentSituation() throws Exception {
        FitsFactory.setLongStringsEnabled(true);
        FitsFactory.setUseHierarch(true);
        FitsFactory.setHierarchFormater(new BlanksDotHierarchKeyFormatter(2));

        Fits fitsSrc = null;
        FileInputStream stream = null;
        BasicHDU<?> bhduMain = null;
        try {
            stream = new FileInputStream(BlackBoxImages.getBlackBoxImage("16913-1.fits"));
            fitsSrc = new Fits(stream);
            bhduMain = fitsSrc.readHDU(); // Product
        } finally {
            SafeClose.close(fitsSrc);
            SafeClose.close(stream);
        }

        BasicHDU<?> bhduMainAgain = null;
        Fits fitsEmpty = null;
        try {
            fitsEmpty = new Fits();
            fitsEmpty.addHDU(bhduMain);
            bhduMainAgain = fitsEmpty.getHDU(fitsEmpty.getNumberOfHDUs() - 1);
        } finally {
            SafeClose.close(fitsEmpty);
        }

        Assert.assertSame(bhduMainAgain, bhduMain);
    }

    @Test
    public void testBinaryTableEncurlProblem() throws Exception {
        FitsFactory.setLongStringsEnabled(true);
        FitsFactory.setUseHierarch(true);

        FileInputStream stream = null;
        BinaryTableHDU bhduMain = null;
        Fits fitsSrc = null;
        try {
            stream = new FileInputStream(BlackBoxImages.getBlackBoxImage("varlen-bintable.fits"));
            fitsSrc = new Fits(stream);
            fitsSrc.readHDU(); // skip the image
            bhduMain = (BinaryTableHDU) fitsSrc.readHDU(); // theres the table
            assertCurledHdu(bhduMain);
            fitsSrc.close();
            fitsSrc.write(new File("target/varlen-bintable-rewite.fits"));
            fitsSrc.close();
            // now read it again and see if the data is still as expected
            fitsSrc = new Fits(new File("target/varlen-bintable-rewite.fits"));
            fitsSrc.readHDU(); // skip the image
            bhduMain = (BinaryTableHDU) fitsSrc.readHDU(); // theres the table
            assertCurledHdu(bhduMain);
        } finally {
            SafeClose.close(fitsSrc);
            SafeClose.close(stream);
        }
    }

    @Test
    public void tetestInvalidHeaderProblemstInvalidHeaderProblem() throws Exception {
        FitsFactory.setLongStringsEnabled(true);
        FitsFactory.setUseHierarch(true);
        FitsFactory.setAllowHeaderRepairs(true);
        FitsFactory.setAllowTerminalJunk(true);

        FileInputStream stream = null;
        BinaryTableHDU bhduMain = null;
        Fits fitsSrc = null;
        try {

            String file1 = BlackBoxImages.getBlackBoxImage("16bits-RGB-compressed-ScottRosen-450D-03h-80dec--C_CVT_2013-12-29-MW1-03h_Light_600SecISO200_000042.fit");
            String file2 = BlackBoxImages.getBlackBoxImage("PetraVanDerMeijs-astroforum-sadr__001.fit");
            String file3 = BlackBoxImages.getBlackBoxImage("16bits-RGB-M45_100mmF2_8_ISO200_300sSeries512 exp_007-Patrick Duis.fit");

            String file4 = BlackBoxImages.getBlackBoxImage("16bit-mono-M34.fit");
            String file5 = BlackBoxImages.getBlackBoxImage("8bit-mono-Convertjup_0_1_L_01.FIT");
            String file6 = BlackBoxImages.getBlackBoxImage("A102rot-AndreVanDerHoeven-Nebulosity30.FIT");

            fitsSrc = new Fits(new File(file1));
            BasicHDU<?>[] image1 = fitsSrc.read();
            Assert.assertEquals(4, image1.length);
            fitsSrc = new Fits(new File(file2));
            BasicHDU<?>[] image2 = fitsSrc.read();
            Assert.assertEquals(1, image2.length);
            fitsSrc = new Fits(new File(file3));
            BasicHDU<?>[] image3 = fitsSrc.read();
            Assert.assertEquals(1, image3.length);
            fitsSrc = new Fits(new File(file4));
            BasicHDU<?>[] image4 = fitsSrc.read();
            Assert.assertEquals(1, image4.length);
            fitsSrc = new Fits(new File(file5));
            BasicHDU<?>[] image5 = fitsSrc.read();
            Assert.assertEquals(1, image5.length);
            fitsSrc = new Fits(new File(file6));
            BasicHDU<?>[] image6 = fitsSrc.read();
            Assert.assertEquals(1, image6.length);
        } catch (UnsupportedOperationException unsupportedOperationException) {
            System.err.println("Unable to fetch a file > " + unsupportedOperationException.getMessage());
            System.err.println("Skipping this test for now...");
        } finally {
            SafeClose.close(fitsSrc);
            SafeClose.close(stream);
        }
    }

    private void assertCurledHdu(BinaryTableHDU bhduMain) throws FitsException {
        //@formatter:off
        Object[][] expected = {
                new double[][]{
                      {  54237.5535530787   },
                      {  54237.55355314815  },
                      {  54237.553552777776 },
                      {  54237.553552777776 },
                      {  54237.553552777776 },
                      {  54237.553552777776 },
                      {  54237.553553287034 },
                      {  54237.553552777776 },
                      {  54237.55355329861  },
                      {  54237.55355331019   },
                },new String[]{
                        "FOCOBS_X_Y_Z"      ,
                        "PHIOBS_X_Y_Z"      ,
                        "INCLINOMETER_3"    ,
                        "INCLINOMETER_1"    ,
                        "PHI_X_Y_Z"         ,
                        "INCLINOMETER_2"    ,
                        "LAPSE_RATE"        ,
                        "PTC_METR_MODE"     ,
                        "DPHI_X_Y_Z"        ,
                        "DFOCUS_X_Y_Z"
                }, new double[][]{
                        {2.78, -4.4, 6.479},  
                        {0.0040, 0.0060, 0.0},
                        {23.31, 49.64, 1.3  },
                        {-12.26, -51.35, 2.7},
                        {0.04, 0.0060, 0.0  },
                        {32.86, 52.75, 0.0  },
                        {0.0065             },
                        {32.0               },
                        {0.0, 0.0, 0.0      },
                        {0.0, 0.0, 0.0      }
                },new byte[][]{                        
                        {109, 109, 32, 47, 32, 109, 109, 32, 47, 32, 109, 109},
                        {100, 101, 103, 32, 47, 32, 100, 101, 103, 32, 47, 32, 100, 101, 103},
                        {97, 114, 99, 115, 101, 99, 32, 47, 32, 97, 114, 99, 115, 101, 99, 32, 47, 32, 100, 101, 103, 67},
                        {97, 114, 99, 115, 101, 99, 32, 47, 32, 97, 114, 99, 115, 101, 99, 32, 47, 32, 100, 101, 103, 67},
                        {100, 101, 103, 32, 47, 32, 100, 101, 103, 32, 47, 32, 100, 101, 103},
                        {97, 114, 99, 115, 101, 99, 32, 47, 32, 97, 114, 99, 115, 101, 99, 32, 47, 32, 100, 101, 103, 67},
                        {75, 47, 109},
                        {45},
                        {100, 101, 103, 32, 47, 32, 100, 101, 103, 32, 47, 32, 100, 101, 103},
                        {109, 109, 32, 47, 32, 109, 109, 32, 47, 32, 109, 109},
                }
        };
        //@formatter:on
        Assert.assertEquals(4, bhduMain.getNCols());
        Assert.assertEquals(10, bhduMain.getNRows());
        for (int column = 0; column < 4; column++) {
            for (int row = 0; row < 10; row++) {
                Object element = bhduMain.getElement(row, column);
                if (column == 0 || column == 2) {
                    Assert.assertArrayEquals((double[]) expected[column][row], (double[]) element, 0.00001);
                } else if (column == 1) {
                    Assert.assertEquals((String) expected[column][row], (String) element);
                } else if (column == 3) {
                    Assert.assertArrayEquals((byte[]) expected[column][row], (byte[]) element);
                }
            }
        }
    }

    @Test
    public void testCorruptedImageTiller() throws Exception {
        long maxMemory = Runtime.getRuntime().maxMemory();
        long memoryUsageForTest = 1000L * 326136L * 8L * 2L;
        if (maxMemory < memoryUsageForTest) {
            return; // skip test
        }
        double[][] double_data = new double[1000][326136];
        double value = 2.8784133518557868E-5d;
        for (int index = 0; index < double_data.length; index++) {
            for (int index2 = 0; index2 < double_data[index].length; index2++) {
                double_data[index][index2] = value;
                value += 2.8784133518557868E-7d;
            }
        }
        ImageData imagedata = ImageHDU.encapsulate(double_data);
        ImageHDU imageHDU = new ImageHDU(ImageHDU.manufactureHeader(imagedata), imagedata);
        Fits fits = new Fits();
        fits.addHDU(imageHDU);
        File fitsFile = new File("target/testCorruptedImageTiller.fits");
        fits.write(fitsFile);
        fits.close();

        Fits fits2 = new Fits(fitsFile);
        try {
            ImageHDU hdu = (ImageHDU) fits2.getHDU(0);
            ImageTiler tiler = hdu.getTiler();
            double[] values = (double[]) tiler.getTile(new int[]{
                0,
                231607
            }, new int[]{
                1000,
                1
            });
            Assert.assertEquals(((double[][]) hdu.getKernel())[800][231607], values[800], 0.00000000001d);
            Assert.assertEquals(((double[][]) hdu.getKernel())[850][231607], values[850], 0.00000000001d);
        } finally {
            fits2.close();
        }
        fitsFile.delete();
    }

}
