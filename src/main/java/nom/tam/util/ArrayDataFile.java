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

import java.io.File;
import java.io.IOException;

/**
 * Efficient reading and writing of arrays to and from files, with custom
 * encoding.
 * 
 * @author Attila Kovacs
 * @since 1.16
 * @see ArrayInputStream
 * @see ArrayOutputStream
 */
public class ArrayDataFile extends BufferedFileIO {

    private ArrayEncoder encoder;

    private ArrayDecoder decoder;

    protected ArrayDataFile(File f, String mode, int bufferSize) throws IOException {
        super(f, mode, bufferSize);
    }

    public ArrayDataFile(File f, String mode, int bufferSize, ArrayEncoder java2bin, ArrayDecoder bin2java) throws IOException {
        this(f, mode, bufferSize);
        setEncoder(java2bin);
        setDecoder(bin2java);
    }

    protected void setEncoder(ArrayEncoder java2bin) {
        this.encoder = java2bin;
    }

    protected ArrayEncoder getEncoder() {
        return encoder;
    }

    protected void setDecoder(ArrayDecoder bin2java) {
        this.decoder = bin2java;
    }

    protected ArrayDecoder getDecoder() {
        return decoder;
    }

    public final void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    public final void readFully(byte[] b, int off, int len) throws IOException {
        decoder.readFully(b, off, len);
    }

    public final void write(byte[] b) throws IOException {
        encoder.write(b, 0, b.length);
    }
}
