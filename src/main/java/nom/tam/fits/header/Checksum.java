package nom.tam.fits.header;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
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
 * File checksum keywords. This data dictionary contains FITS keywords that have
 * been widely used within the astronomical community. It is recommended that
 * these keywords only be used as defined here. {@link http
 * ://heasarc.gsfc.nasa.gov/docs/fcg/common_dict.html}
 * 
 * @author Richard van Nieuwenhoven
 */
public enum Checksum implements IFitsHeader {
    /**
     * The value field of the CHECKSUM keyword shall contain a 16 character
     * string, left justified starting in column 12, containing the ASCII
     * encoded complement of the checksum of the FITS HDU (Header and Data
     * Unit). The algorithm shall be the 32-bit 1's complement checksum and the
     * ASCII encoding that are described in the checksum proposal. The checksum
     * is accumulated in FITS datastream order on the same HDU, identical in all
     * respects, except that the value of the CHECKSUM keyword shall be set to
     * the string '0000000000000000' (ASCII 0's, hex 30) before the checksum is
     * computed.
     */
    CHECKSUM(HDU.ANY, VALUE.STRING, "checksum for the current HDU"),
    /**
     * The value field of the DATASUM keyword shall be a character string
     * containing the unsigned integer value of the checksum of the data records
     * of the HDU. For dataless HDU's, this keyword may either be omitted, or
     * the value field shall contain the string value '0', which is preferred. A
     * missing DATASUM keyword asserts no knowledge of the checksum of the data
     * records.
     */
    DATASUM(HDU.ANY, VALUE.STRING, "checksum of the data records"),

    /**
     * The value field of the CHECKVER keyword shall contain a string, unique in
     * the first 8 characters, which distinguishes between any future
     * alternative checksum algorithms which may be defined. The default value
     * for a missing keyword shall be 'COMPLEMENT' which will represent the
     * algorithm defined in the current proposal. It is recommended that this
     * keyword be omitted from headers which implement the default ASCII encoded
     * 32-bit 1's complement algorithm.
     */
    CHECKVER(HDU.ANY, VALUE.STRING, "version of checksum algorithm");

    private IFitsHeader key;

    private Checksum(HDU hdu, VALUE valueType, String comment) {
        this.key = new FitsHeaderImpl(name(), IFitsHeader.STATUS.CHECKSUM, hdu, valueType, comment);
    }

    @Override
    public String key() {
        return key.key();
    }

    @Override
    public STATUS status() {
        return key.status();
    }

    @Override
    public HDU hdu() {
        return key.hdu();
    }

    @Override
    public VALUE valueType() {
        return key.valueType();
    }

    @Override
    public String comment() {
        return key.comment();
    }

    @Override
    public IFitsHeader n(int number) {
        return key.n(number);
    }

}
