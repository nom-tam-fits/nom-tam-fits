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

import org.junit.Assert;
import org.junit.Test;


import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


public class RandomFileIOChannelTest {

    @Test
    public void testConstructor() throws Exception {
        try (final RandomFileIOChannel _testSubject = new RandomFileIOChannel(0, null)) {
            Assert.fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            Assert.assertEquals("FileChannel cannot be null.", e.getMessage());
        }
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
        try (final RandomFileIOChannel testSubject = new RandomFileIOChannel(testPath)) {
            testSubject.read(buffer, "te".getBytes(StandardCharsets.UTF_8).length, readLength);
        }

        Assert.assertEquals("Wrong bytes read", "st", new String(buffer));
    }

    @Test
    public void testWriteCall() throws Exception {
        final File tempFile = File.createTempFile("test", "tmp");
        tempFile.deleteOnExit();

        final Path testPath = tempFile.toPath();
        final byte[] buffer = "testdataforwrite".getBytes(StandardCharsets.UTF_8);

        try (final RandomFileIOChannel testSubject = new RandomFileIOChannel(testPath, false)) {
            testSubject.write(buffer);
        }

        final byte[] readBuffer = Files.readAllBytes(testPath);
        Assert.assertArrayEquals("Wrong bytes written", buffer, readBuffer);
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
        try (final RandomFileIOChannel testSubject = new RandomFileIOChannel(testPath, false)) {
            Assert.assertEquals("Wrong length", buffer.length, testSubject.length());
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
        try (final RandomFileIOChannel testSubject = new RandomFileIOChannel(testPath, false)) {
            testSubject.position(5);
            Assert.assertEquals("Wrong position", 5, testSubject.position());
        }
    }

    @Test
    public void testClose() throws Exception {
        final File tempFile = File.createTempFile("test", "tmp");
        tempFile.deleteOnExit();

        final Path testPath = tempFile.toPath();
        final RandomFileIOChannel testSubject = new RandomFileIOChannel(testPath);
        testSubject.close();

        Assert.assertFalse("File channel not closed", testSubject.getChannel().isOpen());
    }

    @Test
    public void testWrite() throws Exception {
        final File tempFile = File.createTempFile("test", "tmp");
        tempFile.deleteOnExit();

        final Path testPath = tempFile.toPath();

        try (final RandomFileIOChannel testSubject = new RandomFileIOChannel(testPath, false)) {
            testSubject.write(0);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("write is not implemented for FileChannels.", e.getMessage());
        }
    }

    @Test
    public void testReadUTF() throws Exception {
        final File tempFile = File.createTempFile("test", "tmp");
        tempFile.deleteOnExit();

        final Path testPath = tempFile.toPath();

        try (final RandomFileIOChannel testSubject = new RandomFileIOChannel(testPath, false)) {
            testSubject.readUTF();
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("readUTF is not implemented for FileChannels.", e.getMessage());
        }
    }

    @Test
    public void testGetFD() throws Exception {
        final File tempFile = File.createTempFile("test", "tmp");
        tempFile.deleteOnExit();

        final Path testPath = tempFile.toPath();

        try (final RandomFileIOChannel testSubject = new RandomFileIOChannel(testPath, false)) {
            testSubject.getFD();
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("FileDescriptor is not implemented for FileChannels.", e.getMessage());
        }
    }

    @Test
    public void testSetLength() throws Exception {
        final File tempFile = File.createTempFile("test", "tmp");
        tempFile.deleteOnExit();

        final Path testPath = tempFile.toPath();

        try (final RandomFileIOChannel testSubject = new RandomFileIOChannel(testPath, false)) {
            testSubject.setLength(3);
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("Setting file length is not implemented for FileChannels.", e.getMessage());
        }
    }

    @Test
    public void testWriteUTF() throws Exception {
        final File tempFile = File.createTempFile("test", "tmp");
        tempFile.deleteOnExit();

        final Path testPath = tempFile.toPath();

        try (final RandomFileIOChannel testSubject = new RandomFileIOChannel(testPath, false)) {
            testSubject.writeUTF("data");
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("writeUTF operation is not implemented for FileChannels.", e.getMessage());
        }
    }

    @Test
    public void testRead() throws Exception {
        final File tempFile = File.createTempFile("test", "tmp");
        tempFile.deleteOnExit();

        final Path testPath = tempFile.toPath();

        try (final RandomFileIOChannel testSubject = new RandomFileIOChannel(testPath, false)) {
            testSubject.read();
            Assert.fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            Assert.assertEquals("read is not implemented for FileChannels.", e.getMessage());
        }
    }
}
