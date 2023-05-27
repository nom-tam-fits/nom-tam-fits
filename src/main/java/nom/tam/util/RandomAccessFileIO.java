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
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * Interface for IO objects that support file-like random access. The methods defined here are those used by FitsFile to
 * access a RandomAccessFile. The RandomAccessFileExt class adds this interface to RandomAccessFile, but other systems
 * could provide an alternate implementation of this interface to access an arbitrary FITS data object.
 *
 * @author pdowler
 */
public interface RandomAccessFileIO extends ReadWriteAccess, Closeable {

    /**
     * Reads data, fully populating the supplied array.
     * 
     * @param  bytes       An array which to fill with data from the input
     * 
     * @return             The number of bytes read, which may be fewer than the destination array size.
     * 
     * @throws IOException if there was an error reading the requisite data from the input
     * 
     * @see                #read(byte[], int, int)
     * @see                #write(byte[])
     */
    default int read(byte[] bytes) throws IOException {
        return read(bytes, 0, bytes.length);
    }

    /**
     * See {@link RandomAccessFile#readUTF()} for a contract of this method.
     * 
     * @return             Reads a UTF string from the input, up to an EOL termination (with <code>\n</code>) or until
     *                         the end of file, whichever comes first.
     * 
     * @throws IOException if there was an error reading the requisite data from the input
     * 
     * @see                #writeUTF(String)
     * @see                RandomAccessFile#readUTF()
     */
    String readUTF() throws IOException;

    /**
     * Obtain the current FileChannel instance. For instances that do not use File backed sources
     *
     * @see    RandomAccessFile#getChannel()
     *
     * @return FileChannel instance, possibly null.
     */
    FileChannel getChannel();

    /**
     * Obtain the current FileDescriptor instance.
     *
     * @see                RandomAccessFile#getFD()
     *
     * @return             FileDescriptor instance, or possibly null.
     *
     * @throws IOException For any I/O errors.
     */
    FileDescriptor getFD() throws IOException;

    /**
     * Write all data from the the supplied array.
     * 
     * @param  bytes       the data array
     * 
     * @throws IOException if there was an error writing the data to the output
     * 
     * @see                #write(byte[], int, int)
     * @see                #read(byte[])
     */
    default void write(byte[] bytes) throws IOException {
        write(bytes, 0, bytes.length);
    }

    /**
     * See {@link RandomAccessFile#setLength(long)} for a contract of this method.
     * 
     * @param  length      The new file length.
     * 
     * @throws IOException if there was an error executing the request.
     * 
     * @see                RandomAccessFile#setLength(long)
     */
    void setLength(long length) throws IOException;

    /**
     * See {@link RandomAccessFile#writeUTF(String)} for a contract of this method.
     * 
     * @param  s           A java string.
     * 
     * @throws IOException if there was an error writing the requisite data to the output
     * 
     * @see                #readUTF()
     * @see                RandomAccessFile#writeUTF(String)
     */
    void writeUTF(String s) throws IOException;
}
