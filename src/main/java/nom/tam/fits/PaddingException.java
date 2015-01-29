package nom.tam.fits;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
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
