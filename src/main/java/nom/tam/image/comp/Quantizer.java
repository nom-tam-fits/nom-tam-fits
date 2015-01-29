package nom.tam.image.comp;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import nom.tam.image.QuantizeRandoms;
import nom.tam.image.TileDescriptor;
import nom.tam.util.ArrayFuncs;

/**
 * This class takes a tile from a real image and converts it into integers which
 * may then be compressed.
 * 
 * @author tmcglynn
 */
public class Quantizer {

    private double scale;

    private double offset;

    private int[] corner;

    private int[] size;

    private int ndim;

    private boolean isFloat = false;

    private QuantizeRandoms qr = new QuantizeRandoms();

    /**
     * Create a quantizer that will transform data to/from integer values using
     * the algorithm: i = round(r*scale-offset + rand - 0.5) where the rand term
     * represents a standard dither.
     */
    public Quantizer(double scale, double offset) {

        this.scale = scale;
        this.offset = offset;
    }

    /**
     * This method is called for each tile to output the data for that tile. A
     * byte array representing the output integer data is returned. This method
     * calls recurseOut to descend to the low level primitive arrays and which
     * in turn calls process to spit out the actual data.
     * 
     * @param input
     *            The input image, a multidimensional float or double array.
     * @param td
     *            A descriptor of the current tile.
     * @param tileIndex
     *            The tileIndex which is used to get the appropriate dither
     *            values.
     * @return The byte data representing the integerized tile.
     */

    public byte[] quantize(Object input, TileDescriptor td, int tileIndex) {
        Class cs = ArrayFuncs.getBaseClass(input);
        if (cs == float.class) {
            isFloat = true;
        } else {
            if (cs != double.class) {
                throw new IllegalArgumentException("Only real arrays supported");
            }
        }
        // Extract info from the tile descriptor.
        this.corner = td.corner;
        this.size = td.size;
        ndim = this.size.length;
        int n = 1;
        for (int i = 0; i < ndim; i += 1) {
            n *= this.size[i];
        }

        ByteArrayOutputStream bo = new ByteArrayOutputStream(n);
        DataOutputStream ds = new DataOutputStream(bo);

        qr.computeOffset(tileIndex);
        // Now recurse over the dimensionalities of the input.
        recurseOut(input, ds, 1);

        try {
            ds.close();
        } catch (IOException e) {
            System.err.println("Impossible exception");
            // Ignore, can't happen (famous last words!).
        }
        return bo.toByteArray();

    }

    private void recurseOut(Object input, DataOutputStream ds, int level) {
        // Note we start level at 1.

        int cc = corner[ndim - level];
        int cs = size[ndim - level];
        if (level < ndim) {
            recurseOut(((Object[]) input)[cc], ds, level + 1);
        } else {
            if (isFloat) {
                process((float[]) input, ds, cc, cs);
            } else {
                process((double[]) input, ds, cc, cs);
            }
        }
    }

    private void process(float[] input, DataOutputStream ds, int start, int len) {
        try {
            for (int i = 0; i < len; i += 1) {
                float val = input[i + start];
                if (Float.isNaN(val)) {
                    ds.writeInt(0);
                } else {
                    double dither = qr.next();
                    int v = (int) Math.round((val - offset) / scale + dither);
                    ds.writeInt(v);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected IO error writing to array", e);
        }
    }

    private void process(double[] input, DataOutputStream ds, int start, int len) {
        try {
            for (int i = 0; i < len; i += 1) {
                double val = input[i + start];
                if (Double.isNaN(val)) {
                    ds.writeInt(0);
                } else {
                    int v = (int) Math.round((val - offset) / scale + qr.next());
                    ds.writeInt(v);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unexpected IO error writing to array", e);
        }
    }

    /**
     * Restore a tile of the original data using the input byte array as input.
     * The method calls recurseIn to descend to the bottom level of the array
     * which calls the input method when it reaches the bottom level of the
     * array to actually read and convert the data values.
     * 
     * @param tileStream
     *            The stream of uncompressed bytes from the tile.
     * @param tileData
     *            The data array to be filled.
     * @param td
     *            The corners and size of the tile.
     * @param tileIndex
     *            The index of the tile.
     */
    public void fill(byte[] tileData, Object data, TileDescriptor td) {
        this.corner = td.corner;
        this.size = td.size;
        ndim = this.size.length;
        int n = 1;

        ByteArrayInputStream bi = new ByteArrayInputStream(tileData);
        DataInputStream ds = new DataInputStream(bi);

        qr.computeOffset(td.count);

        recurseIn(data, ds, 1);
    }

    private void recurseIn(Object data, DataInputStream ds, int level) {

        int cs = size[ndim - level];
        if (level < ndim) {
            for (int i = 0; i < cs; i += 1) {
                recurseIn(((Object[]) data)[i], ds, level + 1);
            }
        } else {
            if (isFloat) {
                input((float[]) data, ds, cs);
            } else {
                input((double[]) data, ds, cs);
            }
        }
    }

    private void input(float[] data, DataInputStream ds, int cs) {
        try {
            for (int i = 0; i < cs; i += 1) {
                int val = ds.readInt();
                double dither = qr.next();
                float fval = (float) ((val - dither) * scale + offset);

                data[i] = fval;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read from array", e);
        }
    }

    private void input(double[] data, DataInputStream ds, int cs) {
        try {
            for (int i = 0; i < cs; i += 1) {
                int val = ds.readInt();
                double dither = qr.next();
                data[i] = (val - dither) * scale + offset;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read from array", e);
        }
    }
}
