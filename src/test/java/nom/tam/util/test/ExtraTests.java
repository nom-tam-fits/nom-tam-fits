package nom.tam.util.test;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.util.HashMap;
import java.util.Map;

import nom.tam.image.comp.QuantizeRandoms;
import nom.tam.image.comp.Rice;
import nom.tam.image.comp.TileDescriptor;
import nom.tam.image.comp.TileLooper;
import nom.tam.util.BufferedDataOutputStream;

import org.junit.Ignore;
import org.junit.Test;

public class ExtraTests {

    @Test
    @Ignore
    public void test() {
        // TODO use the mains to create some tests.
    }

    public static void main1(String[] args) throws Exception {
        int[] test = new int[100];
        for (int i = 0; i < test.length; i += 1) {
            if (i % 2 != 0) {
                test[i] = 1000 - 2 * i;
            } else {
                test[i] = 1000 + 2 * i;
            }
        }
        Rice comp = new Rice();
        Map<String, String> init = new HashMap<String, String>();
        init.put("bitpix", "32");
        init.put("block", "32");
        init.put("length", "100");
        comp.initialize(init);

        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        BufferedDataOutputStream d = new BufferedDataOutputStream(bo);
        d.write(test);
        d.close();
        byte[] input = bo.toByteArray();
        byte[] result = comp.compress(input);
        System.out.println("Result len:" + result.length);
        for (int i = 0; i < result.length; i += 1) {
            System.out.printf("%d: %3d %2x\n", i, result[i], result[i]);
        }

        result = comp.decompress(result, 100);
        DataInputStream bi = new DataInputStream(new ByteArrayInputStream(result));
        for (int i = 0; i < 100; i += 1) {
            System.out.println(i + ": " + bi.readInt());
        }
    }

    public static void main2(String[] args) {
        System.out.println("Starting");
        QuantizeRandoms r = new QuantizeRandoms();
        r.computeOffset(0);
        for (int i = 0; i < 10000; i += 1) {
            for (int j = 0; j < 100; j += 1) {
                r.next();
            }
            System.out.println("Got:" + r.next());
        }
    }

    public static void main3(String[] args) {
        if (args.length == 0) {
            args = new String[]{
                "512",
                "600",
                "100",
                "50"
            };
        }
        int nx = Integer.parseInt(args[0]);
        int ny = Integer.parseInt(args[1]);
        int tx = Integer.parseInt(args[2]);
        int ty = Integer.parseInt(args[3]);
        int[] img = new int[]{
            nx,
            ny
        };
        int[] tile = new int[]{
            tx,
            ty
        };
        TileLooper tl = new TileLooper(img, tile);
        for (TileDescriptor td : tl) {
            System.err.println("Corner:" + td.corner[0] + "," + td.corner[1]);
            System.err.println("  Size:" + td.size[0] + "," + td.size[1]);
        }
    }

}
