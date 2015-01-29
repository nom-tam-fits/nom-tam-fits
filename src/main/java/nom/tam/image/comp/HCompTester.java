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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsFactory;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.BufferedDataOutputStream;
import nom.tam.util.BufferedFile;

/**
 * @author tmcglynn
 */
public class HCompTester {

    public static void main(String[] args) throws Exception {
        File f = new File(args[0]);
        long len = f.length();
        FileInputStream fs = new FileInputStream(args[0]);
        byte[] input = new byte[(int) len];
        int need = (int) len;
        int have = 0;
        while (need > 0) {
            int got = fs.read(input, have, need);
            if (got > 0) {
                need -= got;
                have += got;
            } else {
                throw new IOException("Got was:" + got + " with need: " + need);
            }
        }
        HCompress hc = new HCompress();
        int[] a = hc.decomp(input);
        System.err.println("Number of ints:" + a.length);
        int[] dims = new int[]{
            hc.getHeight(),
            hc.getWidth()
        };

        int[][] img = (int[][]) ArrayFuncs.curl(a, dims);
        Fits ff = new Fits();
        ff.addHDU(FitsFactory.HDUFactory(img));
        BufferedFile bf = new BufferedFile(args[1], "rw");
        ff.write(bf);
        bf.close();

        bf = new BufferedFile("int.out", "rw");
        bf.write(a);
        bf.close();

        ByteArrayOutputStream bd = new ByteArrayOutputStream();
        BufferedDataOutputStream out = new BufferedDataOutputStream(bd);
        out.write(a);
        out.close();

        byte[] data = bd.toByteArray();
        System.err.println("Number of bytes:" + data.length);

        Map<String, String> mp = new HashMap<String, String>();
        mp.put("nx", "" + img[0].length);
        mp.put("ny", "" + img.length);
        mp.put("scale", "" + 900);
        hc.initialize(mp);
        byte[] cmp = hc.compress(data);
        int sum = 0;
        for (int i = 0; i < cmp.length; i += 1) {
            sum += cmp[i] & 0xff;
        }
        System.out.println("Sum is:" + sum);
        bf = new BufferedFile(args[2], "rw");
        bf.write(cmp);
        bf.close();
    }

}
