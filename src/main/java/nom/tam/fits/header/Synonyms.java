package nom.tam.fits.header;

import nom.tam.fits.header.extra.NOAOExt;
import nom.tam.fits.header.extra.SBFitsExt;
import nom.tam.fits.header.extra.STScIExt;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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
 * This enum wil try to list synonyms inside or over different dictionaries. So
 * please use always the higest level keyword you can find.
 * 
 * @author Richard van Nieuwenhoven
 */
public enum Synonyms {
    EQUINOX(Standard.EQUINOX, Standard.EPOCH),
    TIMESYS(NOAOExt.TIMESYS, STScIExt.TIMESYS),
    RADESYS(Standard.RADESYS, Standard.RADECSYS),
    DARKTIME(NOAOExt.DARKTIME, SBFitsExt.DARKTIME);

    private final IFitsHeader primaryKeyword;

    private final IFitsHeader[] synonyms;

    Synonyms(IFitsHeader primaryKeyword, IFitsHeader... synonyms) {
        this.primaryKeyword = primaryKeyword;
        this.synonyms = synonyms;
    }

    @SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "by convention the array may not be changed.")
    public IFitsHeader[] getSynonyms() {
        return this.synonyms;
    }

    public IFitsHeader primaryKeyword() {
        return this.primaryKeyword;
    }

    public static IFitsHeader primaryKeyword(IFitsHeader header) {
        for (Synonyms synonym : values()) {
            for (IFitsHeader synHeader : synonym.synonyms) {
                if (synHeader.equals(header)) {
                    return synonym.primaryKeyword();
                }
            }
        }
        return header;
    }

    public static String primaryKeyword(String header) {
        for (Synonyms synonym : values()) {
            for (IFitsHeader synHeader : synonym.synonyms) {
                if (synHeader.key().equals(header)) {
                    return synonym.primaryKeyword().key();
                }
            }
        }
        return header;
    }
}
