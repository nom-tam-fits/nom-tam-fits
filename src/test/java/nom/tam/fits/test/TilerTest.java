package nom.tam.fits.test;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import nom.tam.fits.*;
import nom.tam.util.*;
import nom.tam.image.StandardImageTiler;
import java.io.*;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import junit.framework.JUnit4TestAdapter;

/**
 * This class tests the ImageTiler. It first creates a FITS file and then reads
 * it back and allows the user to select tiles. The values of the corner and
 * center pixels for the selected tile are displayed. Both file and memory tiles
 * are checked.
 */
public class TilerTest {

    void doTile(String test, float[][] data, StandardImageTiler t, int x, int y, int nx, int ny) throws Exception {

        float[] tile = new float[nx * ny];
        t.getTile(tile, new int[]{
            y,
            x
        }, new int[]{
            ny,
            nx
        });

        float sum0 = 0;
        float sum1 = 0;

        for (int i = 0; i < nx; i += 1) {
            for (int j = 0; j < ny; j += 1) {
                sum0 += tile[i + j * nx];
                sum1 += data[j + y][i + x];
            }
        }

        assertEquals("Tiler" + test, sum0, sum1, 0);
    }

    @Test
    public void test() throws Exception {

        float[][] data = new float[300][300];

        for (int i = 0; i < 300; i += 1) {
            for (int j = 0; j < 300; j += 1) {
                data[i][j] = 1000 * i + j;
            }
        }

        Fits f = new Fits();

        BufferedFile bf = new BufferedFile("target/tiler1.fits", "rw");
        f.addHDU(Fits.makeHDU(data));

        f.write(bf);
        bf.close();

        f = new Fits("target/tiler1.fits");

        ImageHDU h = (ImageHDU) f.readHDU();

        StandardImageTiler t = h.getTiler();
        doTile("t1", data, t, 200, 200, 50, 50);
        doTile("t2", data, t, 133, 133, 72, 26);

        Object o = h.getData().getKernel();
        doTile("t3", data, t, 200, 200, 50, 50);
        doTile("t4", data, t, 133, 133, 72, 26);
    }
}
