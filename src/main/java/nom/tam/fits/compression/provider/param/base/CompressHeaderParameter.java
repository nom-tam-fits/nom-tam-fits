package nom.tam.fits.compression.provider.param.base;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2016 nom-tam-fits
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

import static nom.tam.fits.header.Compression.ZNAMEn;
import static nom.tam.fits.header.Compression.ZVALn;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.compression.provider.param.api.ICompressHeaderParameter;
import nom.tam.fits.compression.provider.param.api.IHeaderAccess;

public abstract class CompressHeaderParameter<OPTION> extends CompressParameter<OPTION> implements ICompressHeaderParameter {

    protected CompressHeaderParameter(String name, OPTION option) {
        super(name, option);
    }

    public HeaderCard findZVal(IHeaderAccess header) {
        int nval = 1;
        HeaderCard card = header.findCard(ZNAMEn.n(nval));
        while (card != null) {
            if (card.getValue().equals(getName())) {
                return header.findCard(ZVALn.n(nval));
            }
            card = header.findCard(ZNAMEn.n(++nval));
        }
        return null;
    }

    public int nextFreeZVal(IHeaderAccess header) {
        int nval = 1;
        HeaderCard card = header.findCard(ZNAMEn.n(nval));
        while (card != null) {
            card = header.findCard(ZNAMEn.n(++nval));
        }
        return nval;
    }
}
