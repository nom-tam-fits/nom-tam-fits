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

import java.util.Arrays;

import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.header.Compression;
import nom.tam.image.comp.ICompressOption;
import nom.tam.image.comp.ICompressOptionParameter;
import nom.tam.util.type.PrimitiveType;

public class RiceCompressOption implements ICompressOption {

    public static final int DEFAULT_RICE_BLOCKSIZE = 32;

    public static final int DEFAULT_RICE_BYTEPIX = PrimitiveType.INT.size();

    private final ICompressOptionParameter[] parameters = new ICompressOptionParameter[]{
        new ICompressOptionParameter() {

            @Override
            public String getName() {
                return Compression.BLOCKSIZE;
            }

            @Override
            public Type getType() {
                return Type.ZVAL;
            }

            @Override
            public void getValueFromHeader(HeaderCard value) {
                RiceCompressOption.this.blockSize = value.getValue(Integer.class, DEFAULT_RICE_BLOCKSIZE);

            }

            @Override
            public int setValueInHeader(Header header, int zvalIndex) throws HeaderCardException {
                header.addValue(Compression.ZNAMEn.n(zvalIndex), getName());
                header.addValue(Compression.ZVALn.n(zvalIndex), RiceCompressOption.this.blockSize);
                return zvalIndex + 1;
            }
        },
        new ICompressOptionParameter() {

            @Override
            public String getName() {
                return Compression.BYTEPIX;
            }

            @Override
            public Type getType() {
                return Type.ZVAL;
            }

            @Override
            public void getValueFromHeader(HeaderCard value) {
                RiceCompressOption.this.bytePix = value.getValue(Integer.class, null);
            }

            @Override
            public int setValueInHeader(Header header, int zvalIndex) throws HeaderCardException {
                header.addValue(Compression.ZNAMEn.n(zvalIndex), getName());
                header.addValue(Compression.ZVALn.n(zvalIndex), RiceCompressOption.this.bytePix);
                return zvalIndex + 1;
            }
        },
    };

    private int blockSize = DEFAULT_RICE_BLOCKSIZE;

    private Integer bytePix = null;

    private RiceCompressOption original;

    @Override
    public RiceCompressOption copy() {
        try {
            RiceCompressOption clone = (RiceCompressOption) clone();
            clone.original = this;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("option could not be cloned", e);
        }
    }

    public int getBlockSize() {
        return this.blockSize;
    }

    @Override
    public Integer getBNull() {
        return null;
    }

    @Override
    public double getBScale() {
        return Double.NaN;
    }

    public int getBytePix() {
        return this.bytePix;
    }

    @Override
    public double getBZero() {
        return Double.NaN;
    }

    @Override
    public ICompressOptionParameter getCompressionParameter(String name) {
        for (ICompressOptionParameter parameter : this.parameters) {
            if (parameter.getName().equals(name)) {
                return parameter;
            }
        }
        return ICompressOptionParameter.NULL;
    }

    @Override
    public ICompressOptionParameter[] getCompressionParameters() {
        return Arrays.copyOf(this.parameters, this.parameters.length);
    }

    public RiceCompressOption setBlockSize(int value) {
        this.blockSize = value;
        return this;
    }

    @Override
    public ICompressOption setBNull(Integer blank) {
        return this;
    }

    @Override
    public ICompressOption setBScale(double scale) {
        return this;
    }

    public RiceCompressOption setBytePix(int value) {
        this.bytePix = value;
        return this;
    }

    @Override
    public ICompressOption setBZero(double zero) {
        return this;
    }

    protected RiceCompressOption setDefaultBytePix(int defaultBytePix) {
        if (this.original != null) {
            this.original.setDefaultBytePix(defaultBytePix);
            this.bytePix = this.original.getBytePix();
        } else if (this.bytePix == null) {
            this.bytePix = defaultBytePix;
        }
        return this;
    }

    @Override
    public void setReadDefaults() {
        this.bytePix = DEFAULT_RICE_BYTEPIX;
    }

    @Override
    public RiceCompressOption setTileHeight(int value) {
        return this;
    }

    @Override
    public RiceCompressOption setTileWidth(int value) {
        return this;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.isAssignableFrom(this.getClass())) {
            return clazz.cast(this);
        }
        return null;
    }
}
