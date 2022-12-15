package nom.tam.util;

import static org.junit.Assert.assertEquals;


/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2021 nom-tam-fits
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

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Test;

public class FitsFileTest {

    @After
    public void cleanup() {
        new File("fftest.bin").delete();
    }
    
    @Test(expected = IOException.class)
    public void testWriteNotArray() throws Exception {
        try (FitsFile f = new FitsFile("fftest.bin", "rw", 100)) {
            // Not an array
            f.writeArray("hello");
        }
    }
   
    @Test
    public void testReadWriteBooleanObjectArray() throws Exception {
        try (FitsFile f = new FitsFile("fftest.bin", "rw", 100)) {
            Boolean[] b = new Boolean[] { Boolean.TRUE, null, Boolean.FALSE };
            f.write(b);
            f.seek(0);
            Boolean[] b2 = new Boolean[b.length];
            f.read(b2);
            f.close();
            for (int i=0; i<b.length; i++) {
                assertEquals("[" + i + "]", b[i], b2[i]);
            }
        }
    }
    
    @Test(expected = IOException.class)
    public void testSkipBeforeBeginning() throws Exception {
        try (FitsFile f = new FitsFile("fftest.bin", "rw", 100)) {
            f.seek(10);
            f.skipAllBytes(-11L);
        }
    }
    
    
    @Test
    public void testPosition() throws Exception {
        try (FitsFile f = new FitsFile("fftest.bin", "rw", 100)) {
            f.position(10);
            assertEquals(10, f.position());
        }
    }

    @Test
    public void testAltRandomAccess() throws Exception {
        try (final FitsFile f = new FitsFile(new RandomAccessDataFile(new File("fftest.bin"), "rw"),
                                             1024)) {
            f.seek(12L);
            assertEquals("Wrong position", 12L, f.position());
        }
    }
}
