package nom.tam.util;

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

import static org.junit.Assert.assertEquals;

import java.io.EOFException;

import org.junit.Test;

import nom.tam.util.ByteArrayIO;

public class ByteArrayIOTest {

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidConstructorArgument() throws Exception {
        new ByteArrayIO(0);
    }
    
    @Test
    public void testPosition1() throws Exception {
        ByteArrayIO b = new ByteArrayIO(10);
        b.position(0);
        assertEquals("beginning", 0L, b.position());
        
        b.position(5);
        assertEquals("inside", 5L, b.position());
        
        b.position(10);
        assertEquals("end", 10L, b.position());
    }
    
    @Test
    public void testReadWrite() throws Exception {
        ByteArrayIO b = new ByteArrayIO(10);

        byte[] data = new byte[20];
        for(int i = 0; i < data.length; i++) {
            data[i] = (byte) (i + 1);
        }

        b.write(1);
        b.write(data, 1, 9);
        
        b.position(0);
        assertEquals("read()", 1, b.read());
        
        byte[] read = new byte[10];
        
        assertEquals("read array", 9, b.read(read, 1, 9));

        for(int i = 1; i < 10; i++) {
            assertEquals("read[" + i + "]", data[i], read[i]);
        }
    }
   
    @Test(expected = EOFException.class)
    public void testNegativePosition() throws Exception {
        ByteArrayIO b = new ByteArrayIO(10);
        b.position(-1);
    }
    
    @Test
    public void testPositionBeyondGrowable() throws Exception {
        ByteArrayIO b = new ByteArrayIO(10);
        b.position(11);
        b.write(0);
        b.position(20);
        b.write(new byte[40], 0, 40);
        assertEquals(60L, b.length());
    }
    
    @Test
    public void testReadBeyond1() throws Exception {
        ByteArrayIO b = new ByteArrayIO(10);
        b.position(11);
        assertEquals(-1, b.read());
    }
    
    @Test
    public void testReadBeyond2() throws Exception {
        ByteArrayIO b = new ByteArrayIO(10);
        b.position(11);
        assertEquals(-1, b.read(new byte[40], 0, 40));
    }
    
    
    @Test(expected = EOFException.class)
    public void testPositionBeyondFixed() throws Exception {
        ByteArrayIO b = new ByteArrayIO(new byte[10]);
        b.position(11);
    }
    
    @Test(expected = EOFException.class)
    public void testWriteBeyondFixed() throws Exception {
        ByteArrayIO b = new ByteArrayIO(new byte[10]);
        b.write(new byte[11], 0, 11);
    }
    
    @Test(expected = EOFException.class)
    public void testWriteBeyondFixed2() throws Exception {
        ByteArrayIO b = new ByteArrayIO(new byte[10]);
        for(int i=0; i<11; i++) {
            b.write(i);
        }
    }
    
    @Test
    public void testWriteBeyondGrowable() throws Exception {
        ByteArrayIO b = new ByteArrayIO(10);
        b.write(new byte[11], 0, 11);
        assertEquals(11L, b.length());
    }
    
    @Test
    public void testWriteBeyondGrowable2() throws Exception {
        ByteArrayIO b = new ByteArrayIO(10);
        for(int i=0; i<11; i++) {
            b.write(i);
        }
        assertEquals(11L, b.length());
    }
    
    @Test
    public void testReadBeyondGrowable() throws Exception {
        ByteArrayIO b = new ByteArrayIO(10);
        b.setLength(10);
        assertEquals(10, b.read(new byte[11], 0, 11));
    }
    
    @Test
    public void testReadBeyondFixed() throws Exception {
        ByteArrayIO b = new ByteArrayIO(new byte[10]);
        b.setLength(10);
        assertEquals(10, b.read(new byte[11], 0, 11));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNegativeLength() throws Exception {
        ByteArrayIO b = new ByteArrayIO(new byte[10]);
        b.setLength(-1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSetLengthBeyondFixed() throws Exception {
        ByteArrayIO b = new ByteArrayIO(new byte[10]);
        b.setLength(11);
    }
    
    @Test
    public void testSetLengthBeyondGrowable() throws Exception {
        ByteArrayIO b = new ByteArrayIO(10);
        b.setLength(11);
    }
    
    @Test
    public void testTruncate() throws Exception {
        ByteArrayIO b = new ByteArrayIO(10);
        b.position(10);
        b.setLength(5);
        assertEquals(5, b.position());
    }
    
    @Test
    public void testGrowPow2() throws Exception {
        ByteArrayIO b = new ByteArrayIO(16);
        b.setLength(32);
        assertEquals(32, b.capacity());
    }
    
    @Test
    public void testReadNegativeLength() throws Exception {
        ByteArrayIO b = new ByteArrayIO(16);
        assertEquals(0, b.read(new byte[10], 0, -1));
    }
}
