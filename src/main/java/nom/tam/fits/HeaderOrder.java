package nom.tam.fits;

import static nom.tam.fits.header.Standard.BITPIX;
import static nom.tam.fits.header.Standard.BLOCKED;
import static nom.tam.fits.header.Standard.END;
import static nom.tam.fits.header.Standard.EXTEND;
import static nom.tam.fits.header.Standard.GCOUNT;
import static nom.tam.fits.header.Standard.NAXIS;
import static nom.tam.fits.header.Standard.PCOUNT;
import static nom.tam.fits.header.Standard.SIMPLE;
import static nom.tam.fits.header.Standard.TFIELDS;
import static nom.tam.fits.header.Standard.THEAP;
import static nom.tam.fits.header.Standard.XTENSION;

import java.io.Serializable;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
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
 * This class implements a comparator which ensures that FITS keywords are
 * written out in a proper order.
 */
public class HeaderOrder implements java.util.Comparator<String>, Serializable {

    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Which order should the cards indexed by these keys be written out? This
     * method assumes that the arguments are either the FITS Header keywords as
     * strings, and some other type (or null) for comment style keywords.
     *
     * @return -1 if the first argument should be written first <br>
     *         1 if the second argument should be written first <br>
     *         0 if either is legal.
     */
    @Override
    public int compare(String c1, String c2) {
        // Note that we look at each of the ordered FITS keywords in the
        // required
        // order.

        // Equals are equal
        if (c1.equals(c2)) {
            return 0;
        }

        // Now search in the order in which cards must appear
        // in the header.
        if (c1.equals(SIMPLE.key()) || c1.equals(XTENSION.key())) {
            return -1;
        } else if (c2.equals(SIMPLE.key()) || c2.equals(XTENSION.key())) {
            return 1;
        } else if (c1.equals(BITPIX.key())) {
            return -1;
        } else if (c2.equals(BITPIX.key())) {
            return 1;
        } else if (c1.equals(NAXIS.key())) {
            return -1;
        } else if (c2.equals(NAXIS.key())) {
            return 1;
        }

        // Check the NAXISn cards. These must
        // be in axis order.
        final int naxisNc1 = naxisN(c1);
        final int naxisNc2 = naxisN(c2);
        if (naxisNc1 > 0) {
            if (naxisNc2 > 0) {
                if (naxisNc1 < naxisNc2) {
                    return -1;
                } else {
                    return 1;
                }
            }
            return -1;
        } else if (naxisNc2 > 0) {
            return 1;
        }

        // The EXTEND keyword is no longer required in the FITS standard
        // but in earlier versions of the standard it was required to
        // be here if present in the primary data array.
        if (c1.equals(EXTEND.key())) {
            return -1;
        } else if (c2.equals(EXTEND.key())) {
            return 1;
        } else if (c1.equals(PCOUNT.key())) {
            return -1;
        } else if (c2.equals(PCOUNT.key())) {
            return 1;
        } else if (c1.equals(GCOUNT.key())) {
            return -1;
        } else if (c2.equals(GCOUNT.key())) {
            return 1;
        } else if (c1.equals(TFIELDS.key())) {
            return -1;
        } else if (c2.equals(TFIELDS.key())) {
            return 1;
        }

        // In principal this only needs to be in the first 36 cards,
        // but we put it here since it's convenient. BLOCKED is
        // deprecated currently.
        if (c1.equals(BLOCKED.key())) {
            return -1;
        } else if (c2.equals(BLOCKED.key())) {
            return 1;
        }

        // Note that this must be at the end, so the
        // values returned are inverted. THEAP is put to the end of the file
        // because os a bug in cfitsio that causes confusion when the header
        // appears befor any compression headers.
        if (c1.equals(THEAP.key())) {
            return 1;
        } else if (c2.equals(THEAP.key())) {
            return -1;
        } else if (c1.equals(END.key())) {
            return 1;
        } else if (c2.equals(END.key())) {
            return -1;
        }

        // All other cards can be in any order.
        return 0;
    }

    /** Find the index for NAXISn keywords */
    private static int naxisN(String key) {
        int startOfNumber = NAXIS.key().length();
        if (key.length() > startOfNumber && key.startsWith(NAXIS.key()) && Character.isDigit(key.charAt(startOfNumber))) {
            return Integer.parseInt(key.substring(startOfNumber));
        }
        return -1;
    }
}
