package nom.tam.image.comp.rice;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
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

import nom.tam.fits.header.Compression;
import nom.tam.image.comp.ICompressOption;
import nom.tam.util.PrimitiveTypeEnum;

public class RiceCompressOption implements ICompressOption {

    public static final int DEFAULT_RISE_BLOCKSIZE = 32;

    public static final int DEFAULT_RISE_BYTEPIX = PrimitiveTypeEnum.INT.size();

    private int blockSize = DEFAULT_RISE_BLOCKSIZE;

    private Integer bytePix = DEFAULT_RISE_BYTEPIX;

    @Override
    public RiceCompressOption copy() {
        try {
            return (RiceCompressOption) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("open could not be cloned", e);
        }
    }

    public int getBlockSize() {
        return this.blockSize;
    }

    public RiceCompressOption setBlockSize(int value) {
        this.blockSize = value;
        return this;
    }

    public int getBytePix() {
        return this.bytePix;
    }

    public RiceCompressOption setBytePix(int value) {
        this.bytePix = value;
        return this;
    }

    @Override
    public RiceCompressOption setOption(String name, Object value) {
        if (Compression.BLOCKSIZE.equals(name)) {
            setBlockSize((Integer) value);
        } else if (Compression.BYTEPIX.equals(name)) {
            setBytePix((Integer) value);
        }
        return this;
    }

    @Override
    public RiceCompressOption setTileHeigth(int value) {
        return this;
    }

    @Override
    public RiceCompressOption setTileWidth(int value) {
        return this;
    }
}
