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

package nom.tam.util;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.FitsFactory;
import nom.tam.util.type.ElementType;

/**
 * Class for encoding select Java arrays into FITS binary format.
 * 
 * @since 1.16
 * 
 * @see FitsDecoder
 * @see FitsFile
 * @see FitsInputStream
 */
public class FitsEncoder extends OutputEncoder {
    
    private static final Logger LOG = Logger.getLogger(FitsEncoder.class.getName());

    /** The FITS byte value for the binary representation of a boolean 'true' value */
    private static final byte BYTE_TRUE = (byte) 'T';

    /** The FITS byte value for the binary representation of a boolean 'false' value */
    private static final byte BYTE_FALSE = (byte) 'F';
    
    /**
     * Instantiates a new encoder from Java arrays to FITS binary output. To be used by subclass
     * constructors only.
     */
    protected FitsEncoder() {
        super();
    }

    /**
     * Instantiates a new FITS binary data encoder for converting Java arrays into
     * FITS data representations
     * 
     * @param o     the FITS output.
     */
    public FitsEncoder(OutputWriter o) {
        super(o);
    }

    /**
     * Returns the FITS byte value representing a logical value. 
     * This call supports <code>null</code> values, which are allowed
     * by the FITS standard. FITS defines 'T' as true, 'F' as false, and 0 as null. Prior 
     * versions of this library have used the value 1 for true, and 0 for false. Therefore, this
     * implementation will recognise both 'T' and 1 as <code>true</code>, but 0 will map
     * to <code>null</code> and everything else will return <code>false</code>.
     *              
     */
    private static byte byteForBoolean(Boolean b) {
        if (b == null) {
            return (byte) 0;
        }
        return b ? BYTE_TRUE : BYTE_FALSE;
    }

    /**
     * @deprecated Low-level reading/writing should be handled internally as arrays by this library only.
     * 
     * @param b     a boolean value or <code>null</code>.
     * @throws IOException
     *              if there was an IO error writing to the output.
     * 
     */
    @Deprecated
    protected synchronized void writeBoolean(Boolean b) throws IOException {
        write(byteForBoolean(b));
    }

    /**
     * @deprecated Low-level reading/writing should be handled internally as arrays by this library only.
     * 
     * @param c     An ASCII character.
     * @throws IOException
     *              if there was an IO error writing to the output.
     * 
     */
    @Deprecated
    protected synchronized void writeChar(int c) throws IOException {
        if (FitsFactory.isUseUnicodeChars()) {
            writeShort((short) c);
        } else {
            write(c);
        }
    }
   
    
    /**
     * Puts a boolean array into the conversion buffer, but with no guarantee of flushing the
     * conversion buffer to the underlying output. The caller may put multiple data object into
     * the conversion buffer before eventually calling {@link OutputBuffer#flush()} to ensure
     * that everything is written to the output. Note, the this call may flush the contents
     * of the conversion buffer to the output if it needs more conversion space than
     * what is avaiable.
     * 
     * @param b             the Java array containing the values
     * @param start         the offset in the array from where to start converting values.
     * @param length        the number of values to convert to FITS representation
     * @throws IOException  if there was an IO error while trying to flush the conversion
     *                      buffer to the stream before all elements were converted.
     *                      
     *                      
     * @see #byteForBoolean(Boolean)
     * @see #put(Boolean[], int, int)
     * @see #write(boolean[], int, int)
     */
    private void put(boolean[] b, int start, int length) throws IOException {
        length += start;
        OutputBuffer out = getOutputBuffer();
        while (start < length) {
            out.putByte(byteForBoolean(b[start++]));
        }
    }

    /**
     * Puts a boolean array into the conversion buffer, but with no guarantee of flushing the
     * conversion buffer to the underlying output. The caller may put multiple data object into
     * the conversion buffer before eventually calling {@link OutputBuffer#flush()} to ensure
     * that everything is written to the output. Note, the this call may flush the contents
     * of the conversion buffer to the output if it needs more conversion space than
     * what is avaiable.
     * 
     * @param b             the Java array containing the values
     * @param start         the offset in the array from where to start converting values.
     * @param length        the number of values to convert to FITS representation
     * @throws IOException  if there was an IO error while trying to flush the conversion
     *                      buffer to the stream before all elements were converted.
     * 
     * @see #byteForBoolean(Boolean)
     * @see #put(boolean[], int, int)
     * @see #write(Boolean[], int, int)
     */
    private void put(Boolean[] b, int start, int length) throws IOException {
        length += start;
        OutputBuffer out = getOutputBuffer();
        while (start < length) {
            out.putByte(byteForBoolean(b[start++]));
        }
    }
    
    /**
     * Puts a character array into the conversion buffer, but with no guarantee of flushing the
     * conversion buffer to the underlying output. The caller may put multiple data object into
     * the conversion buffer before eventually calling {@link OutputBuffer#flush()} to ensure
     * that everything is written to the output. Note, the this call may flush the contents
     * of the conversion buffer to the output if it needs more conversion space than
     * what is avaiable.
     * 
     * @param b             the Java array containing the values
     * @param start         the offset in the array from where to start converting values.
     * @param length        the number of values to convert to FITS representation
     * @throws IOException  if there was an IO error while trying to flush the conversion
     *                      buffer to the stream before all elements were converted.
     *                      
     * @see #write(char[], int, int)
     * @see #put(String)
     */
    private void put(char[] c, int start, int length) throws IOException {
        length += start;
        OutputBuffer out = getOutputBuffer();
        if (ElementType.CHAR.size() == 1) {
            while (start < length) {
                out.putByte((byte) c[start++]);
            }
        } else {
            while (start < length) {
                out.putShort((short) c[start++]);
            }
        }
    }
    
    /**
     * Puts a string array into the conversion buffer, but with no guarantee of flushing the
     * conversion buffer to the underlying output. The caller may put multiple data object into
     * the conversion buffer before eventually calling {@link OutputBuffer#flush()} to ensure
     * that everything is written to the output. Note, the this call may flush the contents
     * of the conversion buffer to the output if it needs more conversion space than
     * what is avaiable.
     * 
     * @param b             the Java array containing the values
     * @param start         the offset in the array from where to start converting values.
     * @param length        the number of values to convert to FITS representation
     * @throws IOException  if there was an IO error while trying to flush the conversion
     *                      buffer to the stream before all elements were converted.
     *                      
     * @see #put(String)
     */
    private void put(String[] str, int start, int length) throws IOException {
        length += start;
        while (start < length) {
            put(str[start++]);
        }
    }

    /**
     * Puts a string into the conversion buffer. According to FITS standard, string should
     * be represented by the restricted set of ASCII characters, or 1-byte per character.
     * The caller may put multiple data object into
     * the conversion buffer before eventually calling {@link OutputBuffer#flush()} to ensure
     * that everything is written to the output. Note, the this call may flush the contents
     * of the conversion buffer to the output if it needs more conversion space than
     * what is avaiable.
     * 
     * @param str           the Java string
     * @throws IOException  if there was an IO error while trying to flush the conversion
     *                      buffer to the stream before all elements were converted.
     *                      
     * @see #writeBytes(String)
     */
    void put(String str) throws IOException {
        OutputBuffer out = getOutputBuffer();
        for (int i = 0; i < str.length(); i++) {
            out.putByte((byte) str.charAt(i));
        }
    }

    /**
     * See {@link ArrayDataOutput#write(boolean[], int, int)} for the general contract of this method.
     * In FITS, <code>true</code> values are represented by the ASCII byte for 'T', whereas
     * <code>false</code> is represented by the ASCII byte for 'F'.
     * 
     * @param b
     *            array of booleans.
     * @param start
     *            the index of the first element in the array to write
     * @param length
     *            number of array elements to write
     * @throws IOException
     *            if there was an IO error writing to the output
     */
    protected synchronized void write(boolean[] b, int start, int length) throws IOException {
        put(b, start, length);
        flush();
    }

    /**
     * See {@link ArrayDataOutput#write(Boolean[], int, int)} for the general contract of this method.
     * In FITS, <code>true</code> values are represented by the ASCII byte for 'T',
     * <code>false</code> is represented by the ASCII byte for 'F', while <code>null</code>
     * values are represented by the value 0.
     * 
     * @param b
     *            array of booleans.
     * @param start
     *            the index of the first element in the array to write
     * @param length
     *            number of array elements to write
     * @throws IOException
     *            if there was an IO error writing to the output
     */
    protected synchronized void write(Boolean[] b, int start, int length) throws IOException {
        put(b, start, length);
        flush();
    }    
    
    /**
     * @deprecated Low-level reading/writing should be handled internally as arrays by this library only.
     * 
     * @param b     a single byte.
     * @throws IOException
     *              if there was an IO error writing to the output.
     */
    @Deprecated
    protected synchronized void writeByte(int b) throws IOException {
        write(b);
    }
    
    /**
     * @deprecated Low-level reading/writing should be handled internally as arrays by this library only.
     * 
     * @param s     a 16-bit integer value.
     * @throws IOException
     *              if there was an IO error writing to the output.
     */
    @Deprecated
    protected synchronized  void writeShort(int s) throws IOException {
        getOutputBuffer().putShort((short) s);
        flush();
    }

    /**
     * @deprecated Low-level reading/writing should be handled internally as arrays by this library only.
     * 
     * @param i     a 32-bit integer value.
     * @throws IOException
     *              if there was an IO error writing to the output.
     */
    @Deprecated
    protected synchronized void writeInt(int i) throws IOException {
        getOutputBuffer().putInt(i);
        flush();
    }

    /**
     * @deprecated Low-level reading/writing should be handled internally as arrays by this library only.
     * 
     * @param l     a 64-bit integer value.
     * @throws IOException
     *              if there was an IO error writing to the output.
     */
    @Deprecated
    protected synchronized void writeLong(long l) throws IOException {
        getOutputBuffer().putLong(l);
        flush();
    }

    /**
     * @deprecated Low-level reading/writing should be handled internally by this library only.
     * 
     * @param f     a single-precision (32-bit) floating point value.
     * @throws IOException
     *              if there was an IO error writing to the output.
     */
    @Deprecated
    protected synchronized void writeFloat(float f) throws IOException {
        getOutputBuffer().putFloat(f);
        flush();
    }

    /**
     * @deprecated Low-level reading/writing should be handled internally as arrays by this library only.
     * 
     * @param d     a double-precision (64-bit) floating point value.
     * @throws IOException
     *              if there was an IO error writing to the output.
     */
    @Deprecated
    protected synchronized void writeDouble(double d) throws IOException {
        getOutputBuffer().putDouble(d);
        flush();
    }

    /**
     * Writes a Java string as a sequence of ASCII bytes to the output. FITS does not support
     * unicode characters in its version of strings (character arrays), but instead it is
     * restricted to the ASCII set of 1-byte characters.
     * 
     * @param s             the Java string
     * @throws IOException  if the string could not be fully written to the output
     * 
     * @see #writeChars(String)
     */
    protected synchronized void writeBytes(String s) throws IOException {
        put(s);
        flush();
    }

    /**
     * In FITS characters are usually represented as 1-byte ASCII, not as the 2-byte Java types.
     * However, previous implementations if this library have erroneously written 2-byte
     * characters into the FITS. For compatibility both the FITS standard of 1-byte ASCII
     * and the old 2-byte behaviour are supported, and can be selected via 
     * {@link FitsFactory#setUseUnicodeChars(boolean)}.
     * 
     * @param s     a string containing ASCII-only characters
     * @throws IOException 
     *              if there was an IO error writing all the characters to the output.
     * 
     * @see #writeBytes(String)
     * @see FitsFactory#setUseUnicodeChars(boolean) 
     */
    protected synchronized void writeChars(String s) throws IOException {
        if (ElementType.CHAR.size() == 1) {
            writeBytes(s);
        } else {
            OutputBuffer out = getOutputBuffer();
            for (int i = 0; i < s.length(); i++) {
                out.putShort((short) s.charAt(i));
            }
            flush();
        }
        
    }

    /**
     * See {@link ArrayDataOutput#write(char[], int, int)} for the general contract of this method. In
     * FITS characters are usually represented as 1-byte ASCII, not as the 2-byte Java types.
     * However, previous implementations if this library have erroneously written 2-byte
     * characters into the FITS. For compatibility both the FITS standard of 1-byte ASCII
     * and the old 2-byte behaviour are supported, and can be selected via 
     * {@link FitsFactory#setUseUnicodeChars(boolean)}.
     * 
     * @param c
     *            array of character (ASCII only is supported).
     * @param start
     *            the index of the first element in the array to write
     * @param length
     *            number of array elements to write
     * @throws IOException
     *            if there was an IO error writing to the output
     * 
     * @see FitsFactory#setUseUnicodeChars(boolean) 
     */
    protected synchronized void write(char[] c, int start, int length) throws IOException {
        put(c, start, length);
        flush();
    }

    /**
     * See {@link ArrayDataOutput#write(short[], int, int)} for a contract of this method.
     * 
     * @param s
     *            array of 16-bit integers.
     * @param start
     *            the index of the first element in the array to write
     * @param length
     *            number of array elements to write
     * @throws IOException
     *            if there was an IO error writing to the output
     */
    protected synchronized void write(short[] s, int start, int length) throws IOException {
        getOutputBuffer().put(s, start, length);
        flush();
    }

    /**
     * See {@link ArrayDataOutput#write(int[], int, int)} for a contract of this method.
     * 
     * @param i
     *            array of 32-bit integers.
     * @param start
     *            the index of the first element in the array to write
     * @param length
     *            number of array elements to write
     * @throws IOException
     *            if there was an IO error writing to the output
     */
    protected synchronized void write(int[] i, int start, int length) throws IOException {
        getOutputBuffer().put(i, start, length);
        flush();
    }

    /**
     * See {@link ArrayDataOutput#write(long[], int, int)} for a contract of this method.
     * 
     * @param l
     *            array of 64-bit integers.
     * @param start
     *            the index of the first element in the array to write
     * @param length
     *            number of array elements to write
     * @throws IOException
     *            if there was an IO error writing to the output
     */
    protected synchronized void write(long[] l, int start, int length) throws IOException {
        getOutputBuffer().put(l, start, length);
        flush();
    }

    /**
     * See {@link ArrayDataOutput#write(float[], int, int)} for a contract of this method.
     * 
     * @param f
     *            array of single precision (32-bit) floating point values.
     * @param start
     *            the index of the first element in the array to write
     * @param length
     *            number of array elements to write
     * @throws IOException
     *            if there was an IO error writing to the output
     */
    protected synchronized void write(float[] f, int start, int length) throws IOException {
        getOutputBuffer().put(f, start, length);
        flush();
    }

    /**
     * See {@link ArrayDataOutput#write(double[], int, int)} for a contract of this method.
     * 
     * @param d
     *            array of double-precision (64-bit) floating point values.
     * @param start
     *            the index of the first element in the array to write
     * @param length
     *            number of array elements to write
     * @throws IOException
     *            if there was an IO error writing to the output
     * 
     */
    protected synchronized void write(double[] d, int start, int length) throws IOException {
        getOutputBuffer().put(d, start, length);
        flush();
    }

    /**
     * See {@link ArrayDataOutput#write(String[], int, int)} for a contract of this method.
     * 
     * @param str
     *            array of strings (containing ASCII characters only).
     * @param start
     *            the index of the first element in the array to write
     * @param length
     *            number of array elements to write
     * @throws IOException
     *            if there was an IO error writing to the output
     */
    protected synchronized void write(String[] str, int start, int length) throws IOException {
        length += start;
        while (start < length) {
            writeBytes(str[start++]);
        }
    }

    @Override
    public synchronized void writeArray(Object o) throws IOException, IllegalArgumentException {
        putArray(o);
        flush();
    }

    
    /**
     * <p>
     * Puts a Java array into the conversion buffer, but with no guarantee of flushing the
     * conversion buffer to the underlying output. The argument may be any Java array of the
     * types supported in FITS, including multi-dimensional arrays and heterogeneous arrays 
     * of arrays.
     * </p>
     * <p>
     * The caller may put multiple data object into
     * the conversion buffer before eventually calling {@link nom.tam.util.OutputEncoder.OutputBuffer#flush()} to ensure
     * that everything is written to the output. Note, the this call may flush the contents
     * of the conversion buffer to the output if it needs more conversion space than
     * what is avaiable.
     * </p>
     * 
     * @param o             A Java array, including multi-dimensional arrays and
     *                      heterogeneous arrays of arrays.
     * @throws IOException  if there was an IO error while trying to flush the conversion
     *                      buffer to the stream before all elements were converted.
     * @throws IllegalArgumentException
     *                      if the argument is not an array, or if it is or contains
     *                      an element that does not have a known FITS representation.
     *                      
     * @see #writeArray(Object)
     */
    protected void putArray(Object o) throws IOException, IllegalArgumentException {
        if (o == null) {
            return;
        }

        if (!o.getClass().isArray()) {
            throw new IllegalArgumentException("Not an array: " + o.getClass().getName());
        }

        int length = Array.getLength(o);
        if (length == 0) {
            return;
        }

        if (o instanceof byte[]) {
            // Bytes can be written directly to the stream, which is fastest
            // However, before that we need to flush any pending output in the
            // conversion buffer...
            flush();
            write((byte[]) o, 0, length);
        } else if (o instanceof boolean[]) {
            put((boolean[]) o, 0, length);
        } else if (o instanceof char[]) {
            put((char[]) o, 0, length);
        } else if (o instanceof short[]) {
            getOutputBuffer().put((short[]) o, 0, length);
        } else if (o instanceof int[]) {
            getOutputBuffer().put((int[]) o, 0, length);
        } else if (o instanceof float[]) {
            getOutputBuffer().put((float[]) o, 0, length);
        } else if (o instanceof long[]) {
            getOutputBuffer().put((long[]) o, 0, length);
        } else if (o instanceof double[]) {
            getOutputBuffer().put((double[]) o, 0, length);
        } else if (o instanceof Boolean[]) {
            put((Boolean[]) o, 0, length);
        } else if (o instanceof String[]) {
            put((String[]) o, 0, length);
        } else {
            Object[] array = (Object[]) o;
            // Is this a multidimensional array? If so process recursively
            for (int i = 0; i < length; i++) {
                putArray(array[i]);
            }
        } 
    }
    
    
    /**
     * Returns the size of this object as the number of bytes in a FITS binary representation.
     * 
     * @param o     the object
     * @return      the number of bytes in the FITS binary representation of the object or
     *              0 if the object has no FITS representation. (Also elements not known to
     *              FITS will count as 0 sized).
     *                      
     */
    public static long computeSize(Object o) {
        if (o == null) {
            return 0;
        }
        
        if (o instanceof Object[]) {
            long size = 0;
            for (Object e : (Object[]) o) {
                size += computeSize(e);
            }
            return size;            
        }
        
        Class<?> type = o.getClass();          
        ElementType<?> eType = type.isArray() ? ElementType.forClass(type.getComponentType()) : ElementType.forClass(type);
        
        if (eType == ElementType.UNKNOWN) {
            LOG.log(Level.WARNING, "computeSize() called with unknown type.", new IllegalArgumentException("Don't know FITS size of type " + type.getSimpleName()));
        }   
        
        if (eType.isVariableSize()) {
            return eType.size(o);
        }
        
        if (type.isArray()) {
            return Array.getLength(o) * eType.size();
        }
        
        return eType.size();
    }
}
