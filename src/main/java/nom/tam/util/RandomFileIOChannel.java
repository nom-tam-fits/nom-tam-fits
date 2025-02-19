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
 * Concrete implementation of RandomAccessFileIO for FileChannels.  This is a generic implementation using the Java
 * FileChannel API.
 * An example of an implementation of the underlying FileChannel is the NIO SPI file system provider to Amazon S3.
 *
 * @see <a href="https://github.com/awslabs/aws-java-nio-spi-for-s3">AWS NIO SPI library</a>
 */
public class RandomFileIOChannel implements RandomAccessFileIO {
    private static final Logger LOGGER = LoggerHelper.getLogger(RandomFileIOChannel.class);

    private final long fileSize;
    private final FileChannel fileChannel;

    public RandomFileIOChannel(final Path filePath) throws IOException {
        this(filePath, true);
    }

    public RandomFileIOChannel(final Path filePath, final boolean readOnly) throws IOException {
        this(Files.size(filePath), FileChannel.open(filePath, readOnly ? new OpenOption[]{StandardOpenOption.READ} :
                new OpenOption[]{StandardOpenOption.READ, StandardOpenOption.WRITE}));
    }

    /**
     * Used for testing purposes.
     *
     * @param fileSize    The size (length) of the file in bytes.
     * @param fileChannel The open channel to the file.
     */
    RandomFileIOChannel(long fileSize, FileChannel fileChannel) {
        this.fileSize = fileSize;
        this.fileChannel = Objects.requireNonNull(fileChannel, "FileChannel cannot be null.");
    }


    // Read UTF string (mock implementation)
    @Override
    public String readUTF() {
        throw new UnsupportedOperationException("readUTF is not implemented for FileChannels.");
    }

    @Override
    public FileChannel getChannel() {
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
    public long position() throws IOException {
        return fileChannel.position();
    }

    @Override
    public void position(long l) throws IOException {
        fileChannel.position(l);
    }

    @Override
    public long length() {
        return this.fileSize;
    }

    @Override
    public int read() {
        throw new UnsupportedOperationException("read is not implemented for FileChannels.");
    }

    /**
     * Read bytes from the file channel, starting at offset for readLength bytes.
     *
     * @param bytes      The byte array to read into.
     * @param offset     The offset in the FileChannel to start reading.
     * @param readLength The number of bytes to read.
     * @return The number of bytes read.
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
     * Write bytes to the file channel.
     *
     * @param bytes       The byte array to write.
     * @param offset        The offset in the FileChannel to start writing from.
     * @param writeLength   The number of bytes to write.
     * @throws IOException  If an error occurs writing to the FileChannel.
     */
    @Override
    public void write(byte[] bytes, int offset, int writeLength) throws IOException {
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        position(offset);
        final int bytesWritten = this.fileChannel.write(buffer);
        LOGGER.log(Level.FINE, "Wrote {0} bytes to the file channel.", bytesWritten);
    }
}
