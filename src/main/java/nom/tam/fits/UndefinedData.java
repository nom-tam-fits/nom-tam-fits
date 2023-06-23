package nom.tam.fits;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.header.Bitpix;
import nom.tam.fits.header.Standard;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.FitsEncoder;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2021 nom-tam-fits
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

import static nom.tam.fits.header.Standard.EXTEND;
import static nom.tam.fits.header.Standard.GCOUNT;
import static nom.tam.fits.header.Standard.NAXIS;
import static nom.tam.fits.header.Standard.NAXISn;
import static nom.tam.fits.header.Standard.PCOUNT;
import static nom.tam.util.LoggerHelper.getLogger;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A container for unknown binary data types. We can still retrieve the data as a <code>byte[]</code> array, we just
 * don't know how to interpret it ourselves. This class makes sure we don't break when we encouter HDUs that we don't
 * (yet) support, such as HDU types defined by future FITS standards.
 * 
 * @see UndefinedHDU
 */
public class UndefinedData extends Data {

    private static final Logger LOG = getLogger(UndefinedData.class);

    private int byteSize = 0;
    private byte[] data;

    /**
     * Creates a new empty container for data of unknown type based on the provided FITS header information.
     *
     * @param  h             The FITS header corresponding to the data segment in the HDU
     * 
     * @throws FitsException if there wan an error accessing or interpreting the provided header information.
     */
    public UndefinedData(Header h) throws FitsException {

        /**
         * Just get a byte buffer to hold the data.
         */
        // Bug fix by Vincenzo Forzi.
        int naxis = h.getIntValue(NAXIS);

        int size = naxis > 0 ? 1 : 0;
        for (int i = 0; i < naxis; i++) {
            size *= h.getIntValue(NAXISn.n(i + 1));
        }
        size += h.getIntValue(PCOUNT);
        if (h.getIntValue(GCOUNT) > 1) {
            size *= h.getIntValue(GCOUNT);
        }
        size *= Bitpix.fromHeader(h).byteSize();

        byteSize = size;
    }

    /**
     * Create an UndefinedData object using the specified object.
     *
     * @param x object to create the hdu from
     */
    public UndefinedData(Object x) {
        byteSize = (int) FitsEncoder.computeSize(x);
        data = new byte[byteSize];
        ArrayFuncs.copyInto(x, data);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void fillHeader(Header head) {
        try {
            Standard.context(UndefinedData.class);
            head.setXtension("UNKNOWN");
            head.setBitpix(Bitpix.BYTE);
            head.setNaxes(1);
            head.addValue(NAXISn.n(1), byteSize);
            head.addValue(PCOUNT, 0);
            head.addValue(GCOUNT, 1);
            // Just in case!
            head.addValue(EXTEND, true);
        } catch (HeaderCardException e) {
            LOG.log(Level.SEVERE, "Unable to create unknown header", e);
        } finally {
            Standard.context(null);
        }
    }

    @Override
    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "intended exposure of mutable data")
    protected Object getCurrentData() {
        return data;
    }

    @Override
    protected long getTrueSize() {
        return byteSize;
    }

    @Override
    public byte[] getData() throws FitsException {
        byte[] bytes = (byte[]) super.getData();
        if (bytes != null) {
            return bytes;
        }

        data = new byte[byteSize];
        return data;
    }

    @Override
    protected void loadData(ArrayDataInput in) throws IOException {
        data = new byte[byteSize];
        in.readFully(data);
    }

    @SuppressWarnings({"resource", "deprecation"})
    @Override
    public void write(ArrayDataOutput o) throws FitsException {
        if (o != getRandomAccessInput()) {
            ensureData();
        }
        try {
            o.write(data);
        } catch (IOException e) {
            throw new FitsException("IO Error on unknown data write", e);
        }
        FitsUtil.pad(o, getTrueSize());
    }
}
