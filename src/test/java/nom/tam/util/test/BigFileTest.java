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

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;

import nom.tam.util.BufferedDataInputStream;
import nom.tam.util.BufferedFile;

import org.junit.Test;

public class BigFileTest {

    @Test
    public void test() throws Exception {
        try {
            // First create a 3 GB file.
            String fname = System.getenv("BIGFILETEST");
            if (fname == null) {
                System.out.println("BIGFILETEST environment not set.  Returning without test");
                return;
            }
            System.out.println("Big file test.  Takes quite a while.");
            byte[] buf = new byte[100000000]; // 100 MB
            BufferedFile bf = new BufferedFile(fname, "rw");
            byte sample = 13;

            for (int i = 0; i < 30; i += 1) {
                bf.write(buf); // 30 x 100 MB = 3 GB.
                if (i == 24) {
                    bf.write(new byte[]{
                        sample
                    });
                } // Add a marker.
            }
            bf.close();

            // Now try to skip within the file.
            bf = new BufferedFile(fname, "r");
            long skip = 2500000000L; // 2.5 G

            long val1 = bf.getFilePointer();
            bf.skipAllBytes(skip);
            val1 = bf.getFilePointer() - val1;
            long val2 = bf.getFilePointer();
            int val = bf.read();
            bf.close();

            assertEquals("SkipResult", skip, val1);
            assertEquals("SkipPos", skip, val2);
            assertEquals("SkipVal", sample, val);

            BufferedDataInputStream bdis = new BufferedDataInputStream(new FileInputStream(fname));
            bdis.skipAllBytes(skip);
            val = bdis.read();
            bdis.close();
            assertEquals("SSkipVal", sample, val);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw e;
        }
    }
}
