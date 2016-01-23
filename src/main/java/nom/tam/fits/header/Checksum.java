package nom.tam.fits.header;

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

/**
 * File checksum keywords. This data dictionary contains FITS keywords that have
 * been widely used within the astronomical community. It is recommended that
 * these keywords only be used as defined here.
 * 
 * <pre>
 * @see <a href="http://heasarc.gsfc.nasa.gov/docs/fcg/common_dict.html">http://heasarc.gsfc.nasa.gov/docs/fcg/common_dict.html</a>
 * </pre>
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
     * The value field of the CHECKVER keyword shall contain a string, unique in
     * the first 8 characters, which distinguishes between any future
     * alternative checksum algorithms which may be defined. The default value
     * for a missing keyword shall be 'COMPLEMENT' which will represent the
     * algorithm defined in the current proposal. It is recommended that this
     * keyword be omitted from headers which implement the default ASCII encoded
     * 32-bit 1's complement algorithm.
     */
    CHECKVER(HDU.ANY, VALUE.STRING, "version of checksum algorithm"),

    /**
     * The value field of the DATASUM keyword shall be a character string
     * containing the unsigned integer value of the checksum of the data records
     * of the HDU. For dataless HDU's, this keyword may either be omitted, or
     * the value field shall contain the string value '0', which is preferred. A
     * missing DATASUM keyword asserts no knowledge of the checksum of the data
     * records.
     */
    DATASUM(HDU.ANY, VALUE.STRING, "checksum of the data records");

    @SuppressWarnings("CPD-START")
    private final IFitsHeader key;

    Checksum(HDU hdu, VALUE valueType, String comment) {
        this.key = new FitsHeaderImpl(name(), IFitsHeader.SOURCE.CHECKSUM, hdu, valueType, comment);
    }

    @Override
    public String comment() {
        return this.key.comment();
    }

    @Override
    public HDU hdu() {
        return this.key.hdu();
    }

    @Override
    public String key() {
        return this.key.key();
    }

    @Override
    public IFitsHeader n(int... number) {
        return this.key.n(number);
    }

    @Override
    public SOURCE status() {
        return this.key.status();
    }

    @Override
    @SuppressWarnings("CPD-END")
    public VALUE valueType() {
        return this.key.valueType();
    }
}
