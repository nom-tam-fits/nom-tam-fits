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
 * This data dictionary contains FITS keywords that have been widely used within
 * the astronomical community. It is recommended that these keywords only be
 * used as defined here. These are the Keywords that denote non-standard FITS
 * keyword format conventions.
 * 
 * <pre>
 * @see <a href="http://heasarc.gsfc.nasa.gov/docs/fcg/common_dict.html">http://heasarc.gsfc.nasa.gov/docs/fcg/common_dict.html</a>
 * </pre>
 * 
 * @author Richard van Nieuwenhoven
 */
public enum NonStandard implements IFitsHeader {
    /**
     * The CONTINUE keyword, when followed by spaces in columns 9 and 10 of the
     * card image and a character string enclosed in single quotes starting in
     * column 11 or higher, indicates that the quoted string should be treated
     * as a continuation of the character string value in the previous key
     * keyword. To conform to this convention, the character string value on the
     * previous keyword must end with the ampersand character ('&amp;'), but the
     * ampersand is not part of the value string and should be deleted before
     * concatenating the strings together. The character string value may be
     * continued on any number of consecutive CONTINUE keywords, thus
     * effectively allowing arbitrarily long strings to be written as keyword
     * values.
     */
    CONTINUE(SOURCE.HEASARC, HDU.ANY, VALUE.NONE, "denotes the CONTINUE long string keyword convention"),
    /**
     * The HIERARCH keyword, when followed by spaces in columns 9 and 10 of the
     * FITS card image, indicates that the ESO HIERARCH keyword convention
     * should be used to interpret the name and value of the keyword. The
     * HIERARCH keyword formally has no value because it is not followed by an
     * equals sign in column 9. Under the HIERARCH convention the actual name of
     * the keyword begins in column 11 of the card image and is terminated by
     * the equal sign ('=') character. The name can contain any number of
     * characters as long as it fits within columns 11 and 80 of the card image
     * and also leaves enough space for the equal sign separator and the value
     * field. The name can contain any printable ASCII text character, including
     * spaces and lower-case characters, except for the equal sign character
     * which serves as the terminator of the name field. Leading and trailing
     * spaces in the name field are not significant, but embedded spaces within
     * the name are significant. The value field follows the equals sign and
     * must conform to the syntax for a free-format value field as defined in
     * the FITS Standard. The value field may be null, in which case it contains
     * only space characters, otherwise it may contain either a character string
     * enclosed in single quotes, the logical constant T or F, an integer
     * number, a floating point number, a complex integer number, or a complex
     * floating point number. The value field may be followed by an optional
     * comment string. The comment field must be separated from the value field
     * by a slash character ('/'). It is recommended that the slash character be
     * preceeded and followed by a space character. Example: "HIERARCH Filter
     * Wheel = 12 / filter position". In this example the logical name of the
     * keyword is 'Filter Wheel' and the value is 12.
     */
    HIERARCH(SOURCE.ESO, HDU.ANY, VALUE.NONE, "denotes the HIERARCH keyword convention"),
    /**
     * The presence of this keyword with a value = T in an extension key
     * indicates that the keywords contained in the primary key (except the FITS
     * Mandatory keywords, and any COMMENT, HISTORY or 'blank' keywords) are to
     * be inherited, or logically included in that extension key.
     */
    INHERIT(SOURCE.STScI, HDU.EXTENSION, VALUE.LOGICAL, "denotes the INHERIT keyword convention");

    @SuppressWarnings("CPD-START")
    private final IFitsHeader key;

    NonStandard(IFitsHeader.SOURCE status, HDU hdu, VALUE valueType, String comment) {
        this.key = new FitsHeaderImpl(name(), status, hdu, valueType, comment);
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
