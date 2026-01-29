package nom.tam.util;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
public class RandomAccessFileChannelTest {

    @Test
    public void testConstructor() throws Exception {
        Assertions.assertThrows(NullPointerException.class, () -> new RandomAccessFileChannel(0, null));
    }

    @Test
    public void testReadCall() throws Exception {
        final File tempFile = File.createTempFile("test", "tmp");
        tempFile.deleteOnExit();

        try (final FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.write("testdata");
        }

        final Path testPath = tempFile.toPath();
        final int readLength = 2;
        final byte[] buffer = new byte[readLength];
        try (final RandomAccessFileChannel testSubject = new RandomAccessFileChannel(testPath)) {
            testSubject.read(buffer, "te".getBytes(StandardCharsets.UTF_8).length, readLength);
        }

        Assertions.assertEquals("st", new String(buffer));
    }

    @Test
    public void testWriteCall() throws Exception {
        final File tempFile = File.createTempFile("test", "tmp");
        tempFile.deleteOnExit();

        final Path testPath = tempFile.toPath();
        final byte[] buffer = "testdataforwrite".getBytes(StandardCharsets.UTF_8);

        try (final RandomAccessFileChannel testSubject = new RandomAccessFileChannel(testPath, false)) {
            testSubject.write(buffer);
        }

        final byte[] readBuffer = Files.readAllBytes(testPath);
        Assertions.assertArrayEquals(buffer, readBuffer);
    }

    @Test
    public void testLength() throws Exception {
        final File tempFile = File.createTempFile("test", "tmp");
        tempFile.deleteOnExit();

        final byte[] buffer = "testdataforlength".getBytes(StandardCharsets.UTF_8);
        try (final FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.write(new String(buffer));
        }

        final Path testPath = tempFile.toPath();
        try (final RandomAccessFileChannel testSubject = new RandomAccessFileChannel(testPath, false)) {
            Assertions.assertEquals(buffer.length, testSubject.length());
        }
    }

    @Test
    public void testPosition() throws Exception {
        final File tempFile = File.createTempFile("test", "tmp");
        tempFile.deleteOnExit();

        final byte[] buffer = "testdataforposition".getBytes(StandardCharsets.UTF_8);
        try (final FileWriter fileWriter = new FileWriter(tempFile)) {
            fileWriter.write(new String(buffer));
        }

        final Path testPath = tempFile.toPath();
        try (final RandomAccessFileChannel testSubject = new RandomAccessFileChannel(testPath, false)) {
            testSubject.position(5);
            Assertions.assertEquals(5, testSubject.position());
        }
    }

    @Test
    public void testClose() throws Exception {
        final File tempFile = File.createTempFile("test", "tmp");
        tempFile.deleteOnExit();

        final Path testPath = tempFile.toPath();
        final RandomAccessFileChannel testSubject = new RandomAccessFileChannel(testPath);
        testSubject.close();

        Assertions.assertFalse(testSubject.getChannel().isOpen());
    }

    @Test
    public void testWrite() throws Exception {
        final File tempFile = File.createTempFile("test", "tmp");
        tempFile.deleteOnExit();

        final Path testPath = tempFile.toPath();

        try (final RandomAccessFileChannel testSubject = new RandomAccessFileChannel(testPath, false)) {
            Assertions.assertThrows(UnsupportedOperationException.class, () -> testSubject.write(0));
        }
    }

    @Test
    public void testReadUTF() throws Exception {
        final File tempFile = File.createTempFile("test", "tmp");
        tempFile.deleteOnExit();

        final Path testPath = tempFile.toPath();

        try (final RandomAccessFileChannel testSubject = new RandomAccessFileChannel(testPath, false)) {
            Assertions.assertThrows(UnsupportedOperationException.class, () -> testSubject.readUTF());
        }
    }

    @Test
    public void testGetFD() throws Exception {
        final File tempFile = File.createTempFile("test", "tmp");
        tempFile.deleteOnExit();

        final Path testPath = tempFile.toPath();

        try (final RandomAccessFileChannel testSubject = new RandomAccessFileChannel(testPath, false)) {
            Assertions.assertThrows(UnsupportedOperationException.class, () -> testSubject.getFD());
        }
    }

    @Test
    public void testSetLength() throws Exception {
        final File tempFile = File.createTempFile("test", "tmp");
        tempFile.deleteOnExit();

        final Path testPath = tempFile.toPath();

        try (final RandomAccessFileChannel testSubject = new RandomAccessFileChannel(testPath, false)) {
            Assertions.assertThrows(UnsupportedOperationException.class, () -> testSubject.setLength(3));
        }
    }

    @Test
    public void testWriteUTF() throws Exception {
        final File tempFile = File.createTempFile("test", "tmp");
        tempFile.deleteOnExit();

        final Path testPath = tempFile.toPath();

        try (final RandomAccessFileChannel testSubject = new RandomAccessFileChannel(testPath, false)) {
            Assertions.assertThrows(UnsupportedOperationException.class, () -> testSubject.writeUTF("data"));
        }
    }

    @Test
    public void testRead() throws Exception {
        final File tempFile = File.createTempFile("test", "tmp");
        tempFile.deleteOnExit();

        final Path testPath = tempFile.toPath();

        try (final RandomAccessFileChannel testSubject = new RandomAccessFileChannel(testPath, false)) {
            Assertions.assertThrows(UnsupportedOperationException.class, () -> testSubject.read());
        }
    }
}
