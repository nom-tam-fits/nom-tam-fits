package nom.tam.memory;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2016 nom-tam-fits
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

import java.io.DataOutput;
import java.io.IOException;

public class DataOutputStream implements DataOutput {

    private final FitsStructure fitsStructure;

    private long currentByteIndex;

    public DataOutputStream(FitsStructure fitsStructure) {
        this.fitsStructure = fitsStructure;
        currentByteIndex = 0L;
    }

    @Override
    public void write(int value) throws IOException {
        currentByteIndex = fitsStructure.write(currentByteIndex, value);
    }

    @Override
    public void write(byte[] value) throws IOException {
        currentByteIndex = fitsStructure.write(currentByteIndex, value);
    }

    @Override
    public void write(byte[] value, int offset, int length) throws IOException {
        currentByteIndex = fitsStructure.write(currentByteIndex, value, offset, length);
    }

    @Override
    public void writeBoolean(boolean value) throws IOException {
        currentByteIndex = fitsStructure.write(currentByteIndex, value);
    }

    @Override
    public void writeByte(int value) throws IOException {
        currentByteIndex = fitsStructure.write(currentByteIndex, value);
    }

    @Override
    public void writeShort(int value) throws IOException {
        currentByteIndex = fitsStructure.write(currentByteIndex, value);
    }

    @Override
    public void writeChar(int value) throws IOException {
        currentByteIndex = fitsStructure.write(currentByteIndex, value);

    }

    @Override
    public void writeInt(int value) throws IOException {
        currentByteIndex = fitsStructure.write(currentByteIndex, value);
    }

    @Override
    public void writeLong(long value) throws IOException {
        currentByteIndex = fitsStructure.write(currentByteIndex, value);

    }

    @Override
    public void writeFloat(float value) throws IOException {
        currentByteIndex = fitsStructure.write(currentByteIndex, value);

    }

    @Override
    public void writeDouble(double value) throws IOException {
        currentByteIndex = fitsStructure.write(currentByteIndex, value);
    }

    @Override
    public void writeBytes(String value) throws IOException {
        currentByteIndex = fitsStructure.write(currentByteIndex, value);
    }

    @Override
    public void writeChars(String value) throws IOException {
        currentByteIndex = fitsStructure.write(currentByteIndex, value);
    }

    @Override
    public void writeUTF(String value) throws IOException {
        currentByteIndex = fitsStructure.write(currentByteIndex, value);
    }

}
