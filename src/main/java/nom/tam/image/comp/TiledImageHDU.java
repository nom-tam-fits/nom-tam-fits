package nom.tam.image.comp;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Data;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.ImageHDU;
import nom.tam.fits.TruncatedFileException;
import nom.tam.image.ImageTiler;
import nom.tam.image.TileDescriptor;
import nom.tam.image.TileLooper;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.BufferedDataInputStream;
import nom.tam.util.BufferedFile;
import nom.tam.util.Cursor;

/**
 * This class represents a FITS image that has rendered using the tiled
 * compression convention.
 * 
 * @author tmcglynn
 */
public class TiledImageHDU extends BinaryTableHDU {

    Header hdr;

    // These keywords will not be copied from the original
    // image into the copy.
    private static String[] reserved = {
        "SIMPLE",
        "XTENSION",
        "BITPIX",
        "NAXIS",
        "NAXIS1",
        "NAXIS2",
        "NAXIS3",
        "NAXIS4",
        "NAXIS5",
        "BLOCKED",
        "EXTEND",
        "PCOUNT",
        "GCOUNT",
        "ZHECKSUM",
        "ZDATASUM",
        "END",
        "ZSIMPLE",
        "ZEXTENSION",
        "ZEXTEND",
        "ZBLOCKED",
        "ZPCOUNT",
        "ZGCOUNT",
        "ZHECKSUM",
        "ZDATASUM",
        "ZTILE1",
        "ZTILE2",
        "ZTILE3",
        "ZTILE4",
        "ZTILE5",
        "ZBITPIX",
        "ZXTENSION",
        "ZNAXIS",
        "ZNAXIS1",
        "ZNAXIS2",
        "ZNAXIS3",
        "ZNAXIS4",
        "ZNAXIS5",
        "ZNAME1",
        "ZNAME2",
        "ZNAME3",
        "ZNAME4",
        "ZNAME5",
        "ZVAR1",
        "ZVAR2",
        "ZVAR3",
        "ZVAR4",
        "ZVAR5",
        "ZMASKCMP",
        "ZQUANTIZ",
        "ZSCALE",
        "ZZERO"
    };

    private static Set<String> reservedKeys = new HashSet<String>();

    private Quantizer quant;

    private CompressionScheme cs;

    private Class baseClass;

    /**
     * The tile widths in each dimension
     */
    private int[] tileSize;

    /**
     * Dimensionality
     */
    private int naxis;

    /**
     * Image dimensions
     */
    private int[] imageSize;

    /**
     * Image BITPIX
     */
    private int zbitpix;

    static {
        for (String res : reserved) {
            reservedKeys.add(res);
        }
    }

    private static Map<Integer, Class> bitpixClasses = new HashMap<Integer, Class>();

    static {
        bitpixClasses.put(8, byte.class);
        bitpixClasses.put(16, short.class);
        bitpixClasses.put(32, int.class);
        bitpixClasses.put(64, long.class);
        bitpixClasses.put(-32, float.class);
        bitpixClasses.put(-64, double.class);
    }

    private String kernelClass;

    /**
     * See if an existing binary table can be treated at a TiledImageHDU.
     * 
     * @param input
     *            A binary table that has been created/read in that may be a
     *            tiled image.
     * @throws FitsException
     *             if the input cannot be treated as a TiledImageHDU.
     */
    public TiledImageHDU(BinaryTableHDU input) throws FitsException {
        super(input.getHeader(), input.getData());
        hdr = input.getHeader();
        if (!hdr.getBooleanValue("ZIMAGE", false) || hdr.getStringValue("ZCMPTYPE") == null || hdr.getIntValue("ZBITPIX", -1) == -1 || hdr.getIntValue("ZNAXIS", -1) == -1) {
            throw new FitsException("Required keywords not found for TiledImageHDU");
        }
        naxis = hdr.getIntValue("ZNAXIS");
        tileSize = new int[naxis];
        imageSize = new int[naxis];
        for (int i = 0; i < naxis; i += 1) {
            String axis = "ZNAXIS" + (i + 1);
            imageSize[i] = hdr.getIntValue(axis, -1);
            if (imageSize[i] == -1) {
                throw new FitsException("Missing " + axis + " keyword for TileImageHDU");
            }

            String tile = "ZTILE" + (i + 1);
            tileSize[i] = hdr.getIntValue(tile, -1);
            // Default tiling is row by row.
            if (tileSize[i] == -1) {
                if (i == 0) {
                    tileSize[i] = imageSize[i];
                } else {
                    tileSize[i] = 1;
                }
            }
        }
        zbitpix = hdr.getIntValue("ZBITPIX");
        baseClass = bitpixClasses.get(zbitpix);

        cs = getCompression(hdr.getStringValue("ZCMPTYPE"));
        if (hdr.containsKey("ZQUANTIZ")) {
            if (hdr.getStringValue("ZQUANTIZ").toUpperCase().equals("SUBTRACTIVE_DITHER_1")) {
                double scale = hdr.getDoubleValue("ZSCALE");
                double offset = hdr.getDoubleValue("ZZERO");
                quant = new Quantizer(scale, offset);
            }
        }

        Map<String, String> params = getParameters();
        cs.getParameters(params, hdr);

        cs.initialize(params);

    }

    /**
     * Create a tiled image HDU from an existing Image HDU.
     */
    public TiledImageHDU(ImageHDU input, Map<String, String> parameters) throws FitsException, IOException {
        super(coreHeader(), nilData());

        hdr = getHeader(); // Get a local reference to the Header.
        String comp = parameters.get("compression");

        imageSize = input.getAxes();
        naxis = imageSize.length;
        if (naxis == 0 || imageSize[0] == 0) {
            throw new FitsException("Cannot compress nil image");
        }
        String tiling = parameters.get("tiling");
        if (tiling == null) {
            tiling = imageSize[0] + "";
            for (int i = 1; i < imageSize.length; i += 1) {
                tiling += ",1";
            }
        }
        String[] fields = tiling.split(",");
        if (fields.length != imageSize.length) {
            throw new FitsException("Tile dimensionality (" + fields.length + ") must match image (" + imageSize.length + ")");
        }
        tileSize = new int[imageSize.length];
        for (int i = 0; i < imageSize.length; i += 1) {
            tileSize[i] = Integer.parseInt(fields[i].trim());
        }

        Header old = input.getHeader();
        // Position the insertion pointer after the TFORM1.
        hdr.getStringValue("TFORM1");

        cs = getCompression(comp);
        insertTileKeywords(old, cs, parameters, imageSize, tileSize);

        Object kern = input.getKernel();

        int bitpix = old.getIntValue("BITPIX");
        zbitpix = bitpix;
        if (bitpix < 0) {
            RealStats rs = new RealStats(kern);
            double offset = rs.min;
            double scale = rs.noise3 / 16;
            double bits = Math.log((rs.max - rs.min) / scale) / Math.log(2);
            insertQuantizerKeywords(offset, scale);
            if (bits > 30) {
                throw new IllegalStateException("Cannot quantize image, noise too large");
            }

            quant = new Quantizer(scale, offset);
        }

        Cursor newPointer = hdr.iterator();
        newPointer.setKey("END");
        Cursor oldPointer = old.iterator();
        oldPointer.setKey("BITPIX");

        copyOldKeywords(oldPointer, newPointer);
        TileLooper tl = new TileLooper(imageSize, tileSize);
        cs.initialize(parameters);
        populateData(kern, bitpix, tl, cs);
    }

    private void insertQuantizerKeywords(double offset, double scale) throws FitsException {
        hdr.addValue("ZZERO", offset, " Quantizer offset value");
        hdr.addValue("ZSCALE", scale, " Quantizer scaling");
        hdr.addValue("ZQUANTIZ", "SUBTRACTIVE_DITHER_1", " Quantizing scheme");
    }

    private void populateData(Object kern, int bitpix, TileLooper tl, CompressionScheme cs) throws FitsException, IOException {

        this.getData();
        this.deleteRows(0, 2);

        int tileCount = 0;

        Iterator<TileDescriptor> ti = tl.iterator();
        kernelClass = kern.getClass().getName();
        getData();

        while (ti.hasNext()) {
            TileDescriptor td = ti.next();
            for (int element : td.size) {
            }
            byte[] data;
            if (quant == null) {
                data = getTileData(td, kern, bitpix);
            } else {
                data = quant.quantize(kern, td, tileCount);
            }
            data = cs.compress(data);
            this.addRow(new Object[]{
                data
            });
        }
    }

    private void insertTileKeywords(Header old, CompressionScheme comp, Map<String, String> parameters, int[] axes, int[] tiles) throws FitsException {

        hdr.insertComment(" ");
        hdr.insertComment("  Tile compression keywords ");
        hdr.insertComment(" ");

        // Update the header.
        hdr.addValue("ZIMAGE", true, "This is a tile compressed image");

        hdr.addValue("ZCMPTYPE", comp.name(), "The compression algorithm used");
        hdr.addValue("ZBITPIX", old.getIntValue("BITPIX"), "The original bitpix value");
        hdr.addValue("ZNAXIS", axes.length, "The original NAXIS");
        for (int i = 0; i < axes.length; i += 1) {
            String d = i + 1 + "";
            hdr.addValue("ZNAXIS" + d, axes[i], "The original NAXIS" + d);
            hdr.addValue("ZTILE" + d, tiles[i], "The tile size along this axis");
        }
        if (old.containsKey("SIMPLE")) {
            hdr.addValue("ZSIMPLE", old.getBooleanValue("SIMPLE"), "Was primary array");
        }

        if (old.containsKey("BLOCKED")) {
            hdr.addValue("ZBLOCKED", old.getIntValue("BLOCKED"), "Old BLOCKED value");
        }
        if (old.containsKey("EXTEND")) {
            hdr.addValue("ZEXTEND", old.getBooleanValue("EXTEND"), "Old EXTEND value");
        }
        if (old.containsKey("PCOUNT")) {
            hdr.addValue("ZPCOUNT", old.getIntValue("PCOUNT"), "Old PCOUNT value");
        }
        if (old.containsKey("GCOUNT")) {
            hdr.addValue("ZGCOUNT", old.getIntValue("GCOUNT"), "Old GCOUNT value");
        }
        if (old.containsKey("CHECKSUM")) {
            hdr.addValue("ZHECKSUM", old.getStringValue("CHECKSUM"), "Old CHECKSUM value");
        }
        if (old.containsKey("DATASUM")) {
            hdr.addValue("DATASUM", old.getStringValue("DATASUM"), "Old DATASUM value");
        }
        comp.updateForWrite(hdr, parameters);
    }

    private void copyOldKeywords(Cursor oldPointer, Cursor newPointer) {

        newPointer.add(HeaderCard.create("COMMENT"));
        newPointer.add(HeaderCard.create("COMMENT   Header info copied from original image"));
        newPointer.add(HeaderCard.create("COMMENT"));

        while (oldPointer.hasNext()) {
            HeaderCard card = (HeaderCard) oldPointer.next();
            String key = card.getKey();
            if (key.equals("END")) {
                break;
            }

            if (!reservedKeys.contains(key)) {
                newPointer.add(card);
            }
        }
    }

    private CompressionScheme getCompression(String comp) {
        if (comp == null) {
            comp = "rice_1";
        }
        comp = comp.toLowerCase();

        CompressionScheme cs;
        if (comp.equals("rice_1") || comp.equals("rice")) {
            cs = new Rice();
        } else if (comp.equals("gzip_1") || comp.equals("gzip")) {
            cs = new Gzip();
        } else if (comp.equals("hcompress_1") || comp.equals("hcompress")) {
            cs = new HCompress();
        } else {
            throw new IllegalArgumentException("Unsupported compression:" + comp);
        }
        return cs;
    }

    private byte[] getTileData(TileDescriptor td, Object kern, int bitpix) {
        int sz = Math.abs(bitpix) / 8;
        for (int element : td.size) {
            sz *= element;
        }
        int[] size = td.size;
        int[] corn = td.corner;

        ByteArrayOutputStream bo = new ByteArrayOutputStream(sz);
        DataOutputStream output = new DataOutputStream(bo);
        int[] pixel = new int[size.length];

        try {
            while (true) {
                writeArray(output, kern, pixel, corn, size, 0);
                // We'll handle the first index in writeArray so we
                // start at 1. Note that this indices are in FITS order,
                // the opposite of Java's.
                boolean incremented = false;
                for (int i = 1; i < size.length; i += 1) {
                    if (pixel[i] < size[i] - 1) {
                        pixel[i] += 1;
                        incremented = true;
                    }
                }
                if (!incremented) {
                    break;
                }
            }
            output.close();
            return bo.toByteArray();
        } catch (IOException e) {
            System.err.println("Unexpected IOException transferring data");
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    private void writeArray(DataOutputStream output, Object data, int[] pixel, int[] corner, int[] size, int level) throws IOException {
        char c = kernelClass.charAt(level + 1);

        // The indices are in FITS order, so we access them inverted.
        int zind = size.length - level - 1;
        int pix = corner[zind] + pixel[zind];
        int sz = size[zind];

        switch (c) {
        // Recurse to the next level, but pick out the appropriate sub-array.
            case '[':
                writeArray(output, ((Object[]) data)[pix], pixel, corner, size, level + 1);
                break;

            case 'B': {
                byte[] temp = (byte[]) data;
                for (int i = pix; i < pix + sz; i += 1) {
                    output.writeByte(temp[i]);
                }
                break;
            }

            case 'S': {
                short[] temp = (short[]) data;
                for (int i = pix; i < pix + sz; i += 1) {
                    output.writeShort(temp[i]);
                }
                break;
            }

            case 'I': {
                int[] temp = (int[]) data;
                for (int i = pix; i < pix + sz; i += 1) {
                    output.writeInt(temp[i]);
                }
                break;
            }
            case 'L': {
                long[] temp = (long[]) data;
                for (int i = pix; i < pix + sz; i += 1) {
                    output.writeLong(temp[i]);
                }
                break;
            }
            case 'F': {
                float[] temp = (float[]) data;
                for (int i = pix; i < pix + sz; i += 1) {
                    output.writeFloat(temp[i]);
                }
                break;
            }
            case 'D': {
                double[] temp = (double[]) data;
                for (int i = pix; i < pix + sz; i += 1) {
                    output.writeDouble(temp[i]);
                }
                break;
            }
            default:
                throw new IOException("Invalid type rendering tiled image:" + kernelClass);
        }

    }

    /**
     * Create the basic header for a TiledImage.
     */
    private static Header coreHeader() throws FitsException {
        Header hdr = BinaryTableHDU.manufactureHeader(nilData());
        hdr.addValue("TTYPE1", "COMPRESSED_DATA", "Compressed data for a single tile");
        return hdr;
    }

    /**
     * Create a nil data segment for a basic tiled image.
     * 
     * @return A nil data segment
     */
    private static Data nilData() throws FitsException {
        // We start with two rows so that we can ensure
        // that it is seen as a variable length column.
        // Need to delete these before adding the real data.
        byte[][] testData = new byte[2][];
        testData[0] = new byte[0];
        testData[1] = new byte[1];
        return BinaryTableHDU.encapsulate(new Object[]{
            testData
        });
    }

    /**
     * Find the size and tile information in the header
     */
    private void getDimens(int[] axes, int[] tiles) throws FitsException {

        boolean tilesFound = true;
        // First look for the ZTILEn keywords.
        for (int i = 0; i < axes.length; i += 1) {
            axes[i] = hdr.getIntValue("ZNAXIS" + (i + 1), -1);
            if (axes[i] == -1) {
                throw new FitsException("Required ZNAXISn not found");
            }
            if (tilesFound) {
                tiles[i] = hdr.getIntValue("ZTILE" + (i + 1), -1);
                if (tiles[i] == -1) {
                    tilesFound = false;
                }
            }
        }

        if (!tilesFound) {
            tiles[0] = axes[0];
            for (int i = 1; i < tiles.length; i += 1) {
                tiles[i] = 1;
            }
        }
    }

    private Map<String, String> getParameters() {
        System.err.println("Getting parameters");
        Map<String, String> params = new HashMap<String, String>();
        int i = 1;
        while (hdr.containsKey("ZNAME" + i)) {
            String name = hdr.getStringValue("ZNAME" + i).toLowerCase();
            String val = hdr.getStringValue("ZVAL" + i);
            System.err.println("Val is :" + null + "  " + (val == null));
            // If we can't read it as a string it is probably a real.
            if (val == null) {
                System.err.println("Getting double.");
                val = "" + hdr.getDoubleValue("ZVAL" + i);
            }
            System.err.println("Looking at:" + name + " -> " + val + " test:" + hdr.getStringValue("ZVAL1"));
            params.put(name, val);
            i += 1;
        }
        System.err.println("Got done with params:" + params.keySet());
        return params;
    }

    /**
     * Convert the tiled image into a regular ImageHDU.
     * 
     * @return The converted HDU.
     */
    public ImageHDU getImageHDU() throws FitsException, IOException {

        int[] axes = new int[hdr.getIntValue("ZNAXIS")];
        hdr.getIntValue("ZBITPIX");
        int[] tiles = new int[axes.length];
        getDimens(axes, tiles);

        Object data = ArrayFuncs.newInstance(baseClass, ArrayFuncs.reverseIndices(axes));
        int[] dataCorner = new int[naxis];

        TileLooper tl = new TileLooper(axes, tiles);
        tl.iterator();

        Object[] rows = (Object[]) getColumn("COMPRESSED_DATA");
        String className = data.getClass().getName();
        for (TileDescriptor td : tl) {
            byte[] tileData = (byte[]) rows[td.count];
            Object tile = getTile(td, tileData);
            insertTile(tile, td.corner, td.size, data, dataCorner, imageSize, className, 0);
        }
        System.out.println("Finished the loop");

        BasicHDU bhdu = FitsFactory.HDUFactory(data);
        // importKeywords(bhdu);
        return (ImageHDU) bhdu;
    }

    private Object getTile(TileDescriptor td, byte[] tileData) throws IOException {
        int tileLen = 1;
        int[] tsize = td.size;

        for (int element : tsize) {
            tileLen *= element;
        }

        tileData = cs.decompress(tileData, tileLen);
        Object tile = ArrayFuncs.newInstance(baseClass, ArrayFuncs.reverseIndices(td.size));
        if (quant == null) {
            BufferedDataInputStream bds = new BufferedDataInputStream(new ByteArrayInputStream(tileData));
            bds.readLArray(tile);
        } else {
            quant.fill(tileData, tile, td);
        }
        return tile;
    }

    /**
     * Return the data from the specified tile in the native format.
     */
    private Object getTile(TileDescriptor td) throws FitsException, IOException {
        byte[] buf = (byte[]) getElement(td.count, this.findColumn("COMPRESSED_DATA"));
        System.out.println("  " + td.count + " " + buf[0] + " " + buf[1] + " " + buf[2] + " " + buf[3] + " " + buf[4] + " " + buf[5] + " " + buf[6] + " " + buf[7]);

        return getTile(td, buf);
    }

    /**
     * Fill in a single tile's worth of data in the subset. Note that we are
     * using FITS ordering of indices.
     * 
     * @param tileArray
     *            The input stream containing the tile data.
     * @param cutoutArray
     *            The cutout array
     * @param tileCorners
     *            The current tile descriptor.
     * @param corners
     *            The corners of the cutout array within the full image
     * @param lengths
     *            The lengths of the cutout array
     * @throws IOException
     */
    private void insertTile(Object tileData, int[] tileCorners, int tileSize[], Object cutoutData, int[] cutoutCorners, int cutoutSize[], String className, int level) {

        // Recall that our arrays are describing the cutout in
        // the same order as FITS uses (x,y,z)
        int x = tileCorners.length - level - 1; // Inverted index

        int txStart = 0;
        int cxStart = 0;
        // Does the tile start before the cutout?
        if (tileCorners[x] < cutoutCorners[x]) {
            txStart = cutoutCorners[x] - tileCorners[x];
        } else {
            cxStart = tileCorners[x] - cutoutCorners[x];
        }

        int txCnt = tileSize[x];
        if (tileCorners[x] + tileSize[x] > cutoutCorners[x] + cutoutSize[x]) {
            txCnt = cutoutCorners[x] + cutoutSize[x] - tileCorners[x] - txStart;
        }

        if (className.charAt(level + 1) == '[') {

            // We are going to recurse to the next level.
            // Note that the arrays are in FITS order, so
            // we need to reverse them.
            Object[] t = (Object[]) tileData;
            Object[] c = (Object[]) cutoutData;
            for (int i = 0; i < txCnt; i += 1) {
                insertTile(t[txStart + i], tileCorners, tileSize, c[cxStart + i], cutoutCorners, cutoutSize, className, level + 1);
            }

        } else {
            // Just copy the data into the cutout.
            System.arraycopy(tileData, txStart, cutoutData, cxStart, txCnt);
        }
    }

    public static void main(String[] args) throws Exception {
        Fits f = new Fits(args[0]);
        ImageHDU im = (ImageHDU) f.readHDU();
        Fits g = new Fits();
        Map<String, String> params = new HashMap<String, String>();
        params.put("compression", "RICE_1");
        TiledImageHDU tHdu = new TiledImageHDU(im, params);
        g.addHDU(tHdu);
        BufferedFile bf = new BufferedFile(args[1], "rw");
        g.write(bf);
        bf.close();
        ImageHDU reconv = tHdu.getImageHDU();
        bf = new BufferedFile(args[2], "rw");
        f = new Fits();
        f.addHDU(reconv);
        f.write(bf);
        bf.close();
    }

    public ImageTiler getImageTiler() {
        System.err.println("Called getImageTiler");
        return new TiledTiler();
    }

    class TiledTiler implements ImageTiler {

        @Override
        public Object getTile(int[] corners, int[] lengths) throws IOException {
            Object array = ArrayFuncs.newInstance(baseClass, ArrayFuncs.reverseIndices(lengths));
            getTile(array, corners, lengths);
            return array;
        }

        /**
         * Fill a subset from the tiles
         */
        @Override
        public void getTile(Object array, int[] corners, int[] lengths) throws IOException {
            System.err.println("Getting tile");
            // First compute the tiles that we are going to loop over.
            int[] tFirst = new int[naxis];
            int[] tCount = new int[naxis];
            for (int i = 0; i < naxis; i += 1) {
                if (corners[i] < 0 || corners[i] >= imageSize[i] || lengths[i] <= 0 || corners[i] + lengths[i] > imageSize[i]) {
                    throw new IllegalArgumentException("Invalid tile request");
                }
                tFirst[i] = corners[i] / tileSize[i];
                tCount[i] = (corners[i] + lengths[i] - 1) / tileSize[i] - tFirst[i] + 1;
            }

            // Create a tile looper that goes over the tiles we want.
            TileLooper tl = new TileLooper(imageSize, tileSize, tFirst, tCount);
            String cName = array.getClass().getName();
            ArrayFuncs.getBaseClass(array);

            for (TileDescriptor td : tl) {
                try {
                    Object tileData = TiledImageHDU.this.getTile(td);
                    insertTile(tileData, td.corner, td.size, array, corners, lengths, cName, 0);
                } catch (FitsException e) {
                    throw new IOException("FITS error reading tile", e);
                }
            }
        }

        @Override
        public Object getCompleteImage() throws IOException {
            System.err.println("Getting complete image");
            Object array = ArrayFuncs.newInstance(baseClass, ArrayFuncs.reverseIndices(imageSize));
            int[] corner = new int[imageSize.length]; // Filled with 0's
            getTile(array, corner, imageSize);
            return array;
        }
    }
}
