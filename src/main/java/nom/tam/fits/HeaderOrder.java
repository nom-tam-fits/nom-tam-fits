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
 * This class implements a comparator which ensures that FITS keywords are
 * written out in a proper order.
 */
public class HeaderOrder implements java.util.Comparator {

    /** Can two cards be exchanged when being written out? */
    public boolean equals(Object a, Object b) {
        return compare(a, b) == 0;
    }

    /**
     * Which order should the cards indexed by these keys be written out? This
     * method assumes that the arguments are either the FITS Header keywords as
     * strings, and some other type (or null) for comment style keywords.
     * 
     * @return -1 if the first argument should be written first <br>
     *         1 if the second argument should be written first <br>
     *         0 if either is legal.
     */
    public int compare(Object a, Object b) {

        // Note that we look at each of the ordered FITS keywords in the
        // required
        // order.

        String c1, c2;

        if (a != null && a instanceof String) {
            c1 = (String) a;
        } else {
            c1 = " ";
        }

        if (b != null && b instanceof String) {
            c2 = (String) b;
        } else {
            c2 = " ";
        }

        // Equals are equal
        if (c1.equals(c2)) {
            return 0;
        }

        // Now search in the order in which cards must appear
        // in the header.

        if (c1.equals("SIMPLE") || c1.equals("XTENSION")) {
            return -1;
        }
        if (c2.equals("SIMPLE") || c2.equals("XTENSION")) {
            return 1;
        }

        if (c1.equals("BITPIX")) {
            return -1;
        }
        if (c2.equals("BITPIX")) {
            return 1;
        }

        if (c1.equals("NAXIS")) {
            return -1;
        }
        if (c2.equals("NAXIS")) {
            return 1;
        }

        // Check the NAXISn cards. These must
        // be in axis order.

        if (naxisN(c1) > 0) {
            if (naxisN(c2) > 0) {
                if (naxisN(c1) < naxisN(c2)) {
                    return -1;
                } else {
                    return 1;
                }
            }
            return -1;
        }

        if (naxisN(c2) > 0) {
            return 1;
        }

        // The EXTEND keyword is no longer required in the FITS standard
        // but in earlier versions of the standard it was required to
        // be here if present in the primary data array.
        if (c1.equals("EXTEND")) {
            return -1;
        }
        if (c2.equals("EXTEND")) {
            return 1;
        }

        if (c1.equals("PCOUNT")) {
            return -1;
        }
        if (c2.equals("PCOUNT")) {
            return 1;
        }

        if (c1.equals("GCOUNT")) {
            return -1;
        }
        if (c2.equals("GCOUNT")) {
            return 1;
        }

        if (c1.equals("TFIELDS")) {
            return -1;
        }
        if (c2.equals("TFIELDS")) {
            return 1;
        }

        // In principal this only needs to be in the first 36 cards,
        // but we put it here since it's convenient. BLOCKED is
        // deprecated currently.
        if (c1.equals("BLOCKED")) {
            return -1;
        }
        if (c2.equals("BLOCKED")) {
            return 1;
        }

        // Note that this must be at the end, so the
        // values returned are inverted.
        if (c1.equals("END")) {
            return 1;
        }
        if (c2.equals("END")) {
            return -1;
        }

        // All other cards can be in any order.
        return 0;
    }

    /** Find the index for NAXISn keywords */
    private int naxisN(String key) {

        if (key.length() > 5 && key.substring(0, 5).equals("NAXIS")) {
            for (int i = 5; i < key.length(); i += 1) {

                boolean number = true;
                char c = key.charAt(i);
                if ('0' > c || c > '9') {
                    number = false;
                    break;
                }
                if (number) {
                    return Integer.parseInt(key.substring(5));
                }
            }
        }
        return -1;
    }
}
