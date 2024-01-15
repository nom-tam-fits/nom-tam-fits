package nom.tam.fits;

import java.io.Serializable;
import java.util.Hashtable;

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

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2024 nom-tam-fits
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
 * This class implements a comparator which ensures that FITS keywords are written out in a proper order.
 * 
 * @deprecated (<i>for internal use</i>) Visibility should be reduced to package level in the future
 */
public class HeaderOrder implements java.util.Comparator<String>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5900038332559417655L;

    /**
     * This array defines the order of ordered keywords, except END (which we handle separately)
     */
    private static final String[] ORDER = {SIMPLE.key(), XTENSION.key(), BITPIX.key(), NAXIS.key(), PCOUNT.key(),
            GCOUNT.key(), EXTEND.key(), TFIELDS.key(), BLOCKED.key(), THEAP.key()};

    /**
     * Every keyword is assigned an index. Because NAXIS can have 999 NAXISn variants, we'll space the indices of the
     * ordered keys by 1000, to allow adding in 999 ordered variants between the major slots.
     */
    private static final int SPACING = 1000;

    /**
     * Keys that do not need ordering get an index that comes after the last ordered key, but before END.
     */
    private static final int UNORDERED = SPACING * ORDER.length;

    /**
     * The END keyword comes last, so assign it an index after unordered.
     */
    private static final int LAST = UNORDERED + SPACING;

    /**
     * Hash table for looking up the index of ordered keys.
     */
    private static final Hashtable<String, Integer> LOOKUP = new Hashtable<>();

    // Initialize the hash lookup from the order array
    static {
        for (int i = 0; i < ORDER.length; i++) {
            LOOKUP.put(ORDER[i], SPACING * i);
        }
    }

    /**
     * Returns a virtual ordering index of a given keyword. Keywords with lower indices should precede keywords that
     * have higher indices. Order does not matter if the indices are the same.
     *
     * @param  key FITS keyword
     *
     * @return     The ordering index of that key
     */
    private static int indexOf(String key) {
        if (key == null) {
            return UNORDERED;
        }
        if (key.startsWith(NAXIS.key())) {
            if (NAXIS.key().length() == key.length()) {
                return LOOKUP.get(NAXIS.key());
            }
            try {
                int i = Integer.parseInt(key.substring(NAXIS.key().length()));
                if (i < 0 || i >= SPACING) {
                    return UNORDERED;
                }
                return LOOKUP.get(NAXIS.key()) + i;
            } catch (NumberFormatException e) {
                return UNORDERED;
            }
        }
        if (key.equals(END.key())) {
            return LAST;
        }
        Integer i = LOOKUP.get(key);
        return i == null ? UNORDERED : i;
    }

    /**
     * Determines the order in which the cards should be added to the header. This method assumes that the arguments are
     * either the FITS Header keywords as strings, and some other type (or null) for comment style keywords.
     *
     * @return -1 if the first argument should be written first <br>
     *             1 if the second argument should be written first <br>
     *             0 if either is legal.
     */
    @Override
    public int compare(String c1, String c2) {
        int i1 = indexOf(c1);
        int i2 = indexOf(c2);
        if (i1 == i2) {
            return 0;
        }
        return i1 < i2 ? -1 : 1;
    }

}
