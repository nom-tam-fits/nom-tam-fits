package nom.tam.util.test;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import nom.tam.fits.Fits;
import nom.tam.fits.FitsFactory;
import nom.tam.image.comp.HCompress;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.BufferedDataOutputStream;
import nom.tam.util.BufferedFile;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author tmcglynn
 */
public class HCompTest {

    @Test
    @Ignore
    public void testHcompression() throws Exception {

        String hcompressedFile = "";
        String writeFile = "";
        String writeFile2 = "";

        File f = new File(hcompressedFile);
        long len = f.length();
        FileInputStream fs = new FileInputStream(hcompressedFile);
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
        BufferedFile bf = new BufferedFile(writeFile, "rw");
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
        for (byte element : cmp) {
            sum += element & 0xff;
        }
        System.out.println("Sum is:" + sum);
        bf = new BufferedFile(writeFile2, "rw");
        bf.write(cmp);
        bf.close();
    }

}
