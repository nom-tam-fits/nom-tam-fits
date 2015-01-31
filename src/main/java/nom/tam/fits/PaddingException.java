package nom.tam.fits;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

/**
 * This exception is thrown if an error is found reading the padding following a
 * valid FITS HDU. This padding is required by the FITS standard, but some FITS
 * writes forego writing it. To access such data users can use something like:
 * <code>
 *     Fits f = new Fits("somefile");
 *     try {
 *          f.read();
 *     } catch (PaddingException e) {
 *          f.addHDU(e.getHDU());
 *     }
 * </code> to ensure that a truncated HDU is included in the FITS object.
 * Generally the FITS file have already added any HDUs prior to the truncatd
 * one.
 */
public class PaddingException extends FitsException {

    /**
     * The HDU where the error happened.
     */
    private BasicHDU truncatedHDU;

    /**
     * When the error is thrown, the data object being read must be supplied. We
     * initially create a dummy header for this. If someone is reading the
     * entire HDU, then they can trap the exception and set the header to the
     * appropriate value.
     */
    public PaddingException(Data datum) throws FitsException {
        truncatedHDU = FitsFactory.HDUFactory(datum.getKernel());
        // We want to use the original Data object... so
        truncatedHDU = FitsFactory.HDUFactory(truncatedHDU.getHeader(), datum);
    }

    public PaddingException(String msg, Data datum) throws FitsException {
        super(msg);
        truncatedHDU = FitsFactory.HDUFactory(datum.getKernel());
        truncatedHDU = FitsFactory.HDUFactory(truncatedHDU.getHeader(), datum);
    }

    void updateHeader(Header hdr) throws FitsException {
        truncatedHDU = FitsFactory.HDUFactory(hdr, truncatedHDU.getData());
    }

    public BasicHDU getTruncatedHDU() {
        return truncatedHDU;
    }
}
