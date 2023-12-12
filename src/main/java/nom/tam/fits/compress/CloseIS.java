package nom.tam.fits.compress;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import static nom.tam.util.LoggerHelper.getLogger;

/**
 * (<i>for internal use</i>) Ensures that input streams aren't left open when decompressing with an external system
 * tool, such as the UNIX <b>compress</b> or <b>bzip2</b> commands. It is discouraged to use system tools for
 * decompressing such files, especially since we have native Java implementations through Apache's
 * <b>commons-compress</b> classes. The system tools are not portable, whereas the <b>commons-compress</b>
 * implementation is. Therefore, you should neer really need to use this class, which is provided only for compatibility
 * with earlier versions of this library.
 * 
 * @deprecated Needed only by deprecated compression classes. And it should not have visibility outside of this package
 *                 anyway.
 */
@Deprecated
public class CloseIS extends FilterInputStream {

    private static final Logger LOG = getLogger(CloseIS.class);

    private static final int COPY_BUFFER_SIZE = 64 * 1024;

    private InputStream output;

    private OutputStream input;

    private String errorText;

    private IOException exception;

    private final Thread stdError;

    private final Thread copier;

    private final Process proc;

    /**
     * Instantiates a new thread that will watch and close the input stream whenever the process using it compeletes.
     * 
     * @param  proc                 The process that is using the input stream
     * @param  compressed           the compressed input stream that is used by the process.
     * 
     * @throws NullPointerException if the compressed argument is <code>null</code>.
     */
    @SuppressWarnings("resource")
    public CloseIS(Process proc, final InputStream compressed) throws NullPointerException {
        super(new BufferedInputStream(proc.getInputStream(), CompressionManager.ONE_MEGABYTE));
        if (compressed == null) {
            throw new NullPointerException();
        }
        this.proc = proc;
        final InputStream error = proc.getErrorStream();
        output = proc.getInputStream();
        input = proc.getOutputStream();
        stdError = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    byte[] buffer = new byte[COPY_BUFFER_SIZE];
                    int len;
                    while ((len = error.read(buffer, 0, buffer.length)) >= 0) {
                        bytes.write(buffer, 0, len);
                    }
                    error.close();
                    errorText = new String(bytes.toByteArray(), Charset.defaultCharset());
                } catch (IOException e) {
                    exception = e;
                }
            }
        });
        // Now copy everything in a separate thread.
        copier = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    byte[] buffer = new byte[COPY_BUFFER_SIZE];
                    int len;
                    while ((len = compressed.read(buffer, 0, buffer.length)) >= 0) {
                        input.write(buffer, 0, len);
                    }
                    input.close();
                } catch (IOException e) {
                    exception = e;
                }
                try {
                    compressed.close();
                } catch (IOException e) {
                    if (exception == null) {
                        exception = e;
                    }
                }
            }
        });
        start();
    }

    @Override
    protected final void finalize() {
        // final to protect against vulnerability when throwing an exception in the constructor
        // See CT_CONSTRUCTOR_THROW in spotbugs for mode explanation.
    }

    /**
     * start all threads.
     */
    private void start() {
        stdError.start();
        copier.start();
    }

    @Override
    public int read() throws IOException {
        int result = 0;
        try {
            result = super.read();
            return result;
        } catch (IOException e) {
            result = -1;
            throw e;
        } finally {
            handledOccuredException(result);
        }
    }

    private void handledOccuredException(int result) throws IOException {
        int exitValue = 0;
        if (result < 0) {
            try {
                stdError.join();
                copier.join();
                exitValue = proc.exitValue();
            } catch (Exception e) {
                LOG.log(Level.WARNING, "could not join the stream processes", e);
            }
        }
        if (exception != null || exitValue != 0) {
            if (errorText != null && !errorText.trim().isEmpty()) {
                throw new IOException(errorText, exception);
            }
            if (exception == null) {
                throw new IOException("exit value was " + exitValue);
            }
            throw exception;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = 0;
        try {
            result = super.read(b, off, len);
            return result;
        } catch (IOException e) {
            throw e;
        } finally {
            handledOccuredException(result);
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        input.close();
        output.close();
    }

}
