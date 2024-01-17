package nom.tam.fits.header;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2024 nom-tam-fits
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
 * <p>
 * This data dictionary contains FITS keywords that have been widely used within the astronomical community. It is
 * recommended that these keywords only be used as defined here. These are the Hierarchical file grouping keywords.
 * </p>
 * <p>
 * See <a href=
 * "http://heasarc.gsfc.nasa.gov/docs/fcg/common_dict.html">http://heasarc.gsfc.nasa.gov/docs/fcg/common_dict.html</a>
 * </p>
 *
 * @author Richard van Nieuwenhoven
 */
public enum HierarchicalGrouping implements IFitsHeader {
    /**
     * TODO: find description?
     */
    GRPIDn(SOURCE.HEASARC, HDU.TABLE, VALUE.STRING, ""),
    /**
     * TODO: find description?
     */
    GRPLCn(SOURCE.HEASARC, HDU.TABLE, VALUE.STRING, ""),
    /**
     * the grouping table name. TODO: find description?
     */
    GRPNAME(SOURCE.HEASARC, HDU.TABLE, VALUE.STRING, "the grouping table name");

    private final FitsKey key;

    HierarchicalGrouping(IFitsHeader.SOURCE status, HDU hdu, VALUE valueType, String comment) {
        key = new FitsKey(name(), status, hdu, valueType, comment);
    }

    @Override
    public final FitsKey impl() {
        return key;
    }
}
