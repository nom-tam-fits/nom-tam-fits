package nom.tam.image.comp;

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
