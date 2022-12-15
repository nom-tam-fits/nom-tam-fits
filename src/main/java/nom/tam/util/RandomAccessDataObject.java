package nom.tam.util;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2022 nom-tam-fits
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

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Minimal interface for underlying data object that supports random access. The
 * methods defined here are those used by FitsFile to access a RandomAccessFile.
 * The RandomAccessFileExt class adds this interface to RandomAccessFile, but other
 * systems could provide an alternate implementation of this interface to access
 * an arbitrary FITS data object.
 * 
 * @author pdowler
 */
public interface RandomAccessDataObject extends Closeable {

    @Override
    void close() throws IOException;

    // read methods from RandomAccessFile
    
    long length() throws IOException;

    void seek(long l) throws IOException;

    long getFilePointer() throws IOException;

    int read() throws IOException;
    
    int read(byte[] bytes) throws IOException;

    int read(byte[] bytes, int offset, int length) throws IOException;
    
    String readUTF() throws IOException;

    FileChannel getChannel();

    FileDescriptor getFD() throws IOException;

    // write methods from RandomAccessFile
    
    void setLength(long l) throws IOException;

    void write(byte[] bytes, int i, int i1) throws IOException;

    void write(byte[] bytes) throws IOException;

    void write(int i) throws IOException;
    
    void writeUTF(String s) throws IOException;
}
