package nom.tam.fits;

import java.nio.Buffer;

/*-
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

import nom.tam.fits.header.Bitpix;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;

import static nom.tam.fits.header.Standard.EXTEND;
import static nom.tam.fits.header.Standard.GCOUNT;
import static nom.tam.fits.header.Standard.PCOUNT;

/**
 * A subclass of <code>Data</code> containing no actual data. It wraps an underlying data of <code>null</code>.
 *
 * @author Attila Kovacs
 *
 * @since  1.18
 * 
 * @see    NullDataHDU
 */
public final class NullData extends ImageData {

    @SuppressWarnings("deprecation")
    @Override
    protected void fillHeader(Header head) {
        head.setSimple(true);
        head.setBitpix(Bitpix.INTEGER);
        head.setNaxes(0);

        try {
            // Just in case!
            head.addValue(EXTEND, true);
            head.addValue(GCOUNT, 1);
            head.addValue(PCOUNT, 0);
        } catch (HeaderCardException e) {
            // we don't really care...
        }
    }

    @Override
    protected void loadData(ArrayDataInput in) {
        return;
    }

    @Override
    protected Void getCurrentData() {
        return null;
    }

    @Override
    protected long getTrueSize() {
        return 0;
    }

    @Override
    public void read(ArrayDataInput in) {
        setFileOffset(in);
    }

    @Override
    public void write(ArrayDataOutput o) {
    }

    @Override
    public void setBuffer(Buffer data) {
        // Nothing to do.
    }

}
