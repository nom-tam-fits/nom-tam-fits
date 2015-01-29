package nom.tam.util.test;

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
