package nom.tam.util;

/*-
 * #%L
 * nom-tam-fits
 * %%
 * Copyright (C) 2025 nom-tam-fits
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

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A random accessible {@link FileChannel} implementation for FITS I/O purposes. It enables file-like access for
 * nom-tam-fits that goes beyond local files. This class provides a generic implementation, which may be used, for
 * example, with the NIO SPI file system provider to Amazon S3.
 *
 * @author Dustin Jenkins, Attila Kovacs
 * 
 * @since  1.21
 *
 * @see    <a href="https://github.com/awslabs/aws-java-nio-spi-for-s3">AWS NIO SPI library</a>
 * @see    FitsFile
 */
public class RandomAccessFileChannel implements RandomAccessFileIO {
    private static final Logger LOGGER = LoggerHelper.getLogger(RandomAccessFileChannel.class);

    private final long fileSize;
    private final FileChannel fileChannel;

    /**
     * Constructor for a readable/writable random accessible {@link FileChannel}, which can be used for accessing FITS
     * files.
     * 
     * @param  filePath    The path to the file
     * 
     * @throws IOException if the file is not accessible
     * 
     * @since              1.21
     * 
     * @see                #RandomAccessFileChannel(Path, boolean)
     */
    public RandomAccessFileChannel(final Path filePath) throws IOException {
        this(filePath, true);
    }

    /**
     * Constructor for a random accessible {@link FileChannel}, for reading or for reading and writing. It may be used
     * for accessing FITS files.
     * 
     * @param  filePath    The path to the file
     * @param  readOnly    <code>true</code> if the file should only be used for reading, otherwise <code>false</code>.
     * 
     * @throws IOException if the file is not accessible
     * 
     * @since              1.21
     * 
     * @see                #RandomAccessFileChannel(Path, boolean)
     */
    @SuppressWarnings("resource")
    public RandomAccessFileChannel(final Path filePath, final boolean readOnly) throws IOException {
        this(Files.size(filePath), FileChannel.open(filePath, readOnly ? new OpenOption[] {StandardOpenOption.READ} :
                new OpenOption[] {StandardOpenOption.READ, StandardOpenOption.WRITE}));
    }

    /**
     * (<i>for internal use</i>) for testing purposes only.
     *
     * @param fileSize    The size (length) of the file in bytes.
     * @param fileChannel The open channel to the file.
     */
    RandomAccessFileChannel(long fileSize, FileChannel fileChannel) {
        this.fileSize = fileSize;
        this.fileChannel = Objects.requireNonNull(fileChannel, "FileChannel cannot be null.");
    }

    // Read UTF string (mock implementation)
    @Override
    public String readUTF() {
        throw new UnsupportedOperationException("readUTF is not implemented for FileChannels.");
    }

    @Override
    public final FileChannel getChannel() {
        return this.fileChannel;
    }

    @Override
    public FileDescriptor getFD() {
        throw new UnsupportedOperationException("FileDescriptor is not implemented for FileChannels.");
    }

    @Override
    public void setLength(long l) {
        throw new UnsupportedOperationException("Setting file length is not implemented for FileChannels.");
    }

    @Override
    public void writeUTF(String s) {
        throw new UnsupportedOperationException("writeUTF operation is not implemented for FileChannels.");
    }

    @Override
    public void close() throws IOException {
        fileChannel.close();
    }

    @Override
    public final long position() throws IOException {
        return fileChannel.position();
    }

    @Override
    public final void position(long l) throws IOException {
        fileChannel.position(l);
    }

    @Override
    public final long length() {
        return this.fileSize;
    }

    @Override
    public int read() {
        throw new UnsupportedOperationException("read is not implemented for FileChannels.");
    }

    /**
     * Read bytes from the underlying file channel.
     *
     * @param  bytes       The byte array to read into.
     * @param  offset      The offset in the FileChannel to start reading.
     * @param  readLength  The number of bytes to read.
     * 
     * @return             The number of bytes read.
     * 
     * @throws IOException If an error occurs reading from the FileChannel.
     */
    @Override
    public int read(byte[] bytes, int offset, int readLength) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(Math.min(bytes.length, readLength));
        position(offset);
        final int bytesRead = fileChannel.read(buffer);
        System.arraycopy(buffer.array(), 0, bytes, 0, bytesRead);

        LOGGER.log(Level.FINE, "Read {0} bytes from the file channel.", bytesRead);
        return bytesRead;
    }

    @Override
    public void write(int i) {
        throw new UnsupportedOperationException("write is not implemented for FileChannels.");
    }

    /**
     * Write bytes to the underlying file channel.
     *
     * @param  bytes       The byte array to write.
     * @param  offset      The offset in the FileChannel to start writing from.
     * @param  writeLength The number of bytes to write.
     * 
     * @throws IOException If an error occurs writing to the FileChannel.
     */
    @Override
    public void write(byte[] bytes, int offset, int writeLength) throws IOException {
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        position(offset);
        final int bytesWritten = this.fileChannel.write(buffer);
        LOGGER.log(Level.FINE, "Wrote {0} bytes to the file channel.", bytesWritten);
    }
}
