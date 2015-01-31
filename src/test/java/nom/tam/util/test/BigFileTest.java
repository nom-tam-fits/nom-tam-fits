package nom.tam.util.test;

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

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import junit.framework.JUnit4TestAdapter;

import nom.tam.util.BufferedFile;
import nom.tam.util.BufferedDataInputStream;
import java.io.FileInputStream;

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

            long val1 = bf.skipBytes(skip);
            long val2 = bf.getFilePointer();
            int val = bf.read();
            bf.close();

            assertEquals("SkipResult", skip, val1);
            assertEquals("SkipPos", skip, val2);
            assertEquals("SkipVal", (int) sample, val);

            BufferedDataInputStream bdis = new BufferedDataInputStream(new FileInputStream(fname));
            val1 = bdis.skipBytes(skip);
            val = bdis.read();
            bdis.close();
            assertEquals("SSkipResult", skip, val1);
            assertEquals("SSkipVal", (int) sample, val);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw e;
        }
    }
}
