package nom.tam.image.comp.hdu;

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

import static nom.tam.fits.header.Checksum.CHECKSUM;
import static nom.tam.fits.header.Checksum.DATASUM;
import static nom.tam.fits.header.Compression.ZBITPIX;
import static nom.tam.fits.header.Compression.ZBLANK;
import static nom.tam.fits.header.Compression.ZBLOCKED;
import static nom.tam.fits.header.Compression.ZCMPTYPE;
import static nom.tam.fits.header.Compression.ZDATASUM;
import static nom.tam.fits.header.Compression.ZDITHER0;
import static nom.tam.fits.header.Compression.ZEXTEND;
import static nom.tam.fits.header.Compression.ZGCOUNT;
import static nom.tam.fits.header.Compression.ZHECKSUM;
import static nom.tam.fits.header.Compression.ZIMAGE;
import static nom.tam.fits.header.Compression.ZNAMEn;
import static nom.tam.fits.header.Compression.ZNAXIS;
import static nom.tam.fits.header.Compression.ZNAXISn;
import static nom.tam.fits.header.Compression.ZPCOUNT;
import static nom.tam.fits.header.Compression.ZQUANTIZ;
import static nom.tam.fits.header.Compression.ZSIMPLE;
import static nom.tam.fits.header.Compression.ZTENSION;
import static nom.tam.fits.header.Compression.ZTILEn;
import static nom.tam.fits.header.Compression.ZVALn;
import static nom.tam.fits.header.Standard.BITPIX;
import static nom.tam.fits.header.Standard.EXTNAME;
import static nom.tam.fits.header.Standard.GCOUNT;
import static nom.tam.fits.header.Standard.NAXIS;
import static nom.tam.fits.header.Standard.NAXISn;
import static nom.tam.fits.header.Standard.PCOUNT;
import static nom.tam.fits.header.Standard.TFIELDS;
import static nom.tam.fits.header.Standard.TFORMn;
import static nom.tam.fits.header.Standard.TTYPEn;
import static nom.tam.fits.header.Standard.XTENSION;

import java.util.Map;

import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardException;
import nom.tam.fits.header.Compression;
import nom.tam.fits.header.GenericKey;
import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.IFitsHeader.VALUE;
import nom.tam.util.Cursor;

enum UncompressHeaderCardMapping {
    MAP_ANY(null) {

        @Override
        protected void copyCard(HeaderCard card, Cursor<String, HeaderCard> headerIterator) throws HeaderCardException {
            // unhandled card so just copy it to the uncompressed header
            headerIterator.add(card.copy());
        }

        @Override
        protected void copyCardBack(HeaderCard card, Cursor<String, HeaderCard> headerIterator) throws HeaderCardException {
            // unhandled card so just copy it to the uncompressed header
            headerIterator.add(card.copy());
        }
    },
    MAP_BITPIX(BITPIX),
    MAP_CHECKSUM(CHECKSUM),
    MAP_DATASUM(DATASUM),
    MAP_EXTNAME(EXTNAME) {

        @Override
        protected void copyCard(HeaderCard card, Cursor<String, HeaderCard> headerIterator) throws HeaderCardException {
            if (!card.getValue().equals("COMPRESSED_IMAGE")) {
                super.copyCard(card, headerIterator);
            }
        }
    },
    MAP_GCOUNT(GCOUNT),
    MAP_NAXIS(NAXIS),
    MAP_NAXISn(NAXISn),
    MAP_PCOUNT(PCOUNT),
    MAP_TFIELDS(TFIELDS),
    MAP_TFORMn(TFORMn),
    MAP_TTYPEn(TTYPEn),
    MAP_XTENSION(XTENSION),
    MAP_ZBITPIX(ZBITPIX),
    MAP_ZBLANK(ZBLANK),
    MAP_ZBLOCKED(ZBLOCKED),
    MAP_ZCMPTYPE(ZCMPTYPE),
    MAP_ZDATASUM(ZDATASUM),
    MAP_ZDITHER0(ZDITHER0),
    MAP_ZEXTEND(ZEXTEND),
    MAP_ZGCOUNT(ZGCOUNT),
    MAP_ZHECKSUM(ZHECKSUM),
    MAP_ZIMAGE(ZIMAGE),
    MAP_ZNAMEn(ZNAMEn),
    MAP_ZNAXIS(ZNAXIS),
    MAP_ZNAXISn(ZNAXISn) {

        @Override
        protected void copyCard(HeaderCard card, Cursor<String, HeaderCard> headerIterator) throws HeaderCardException {
            String newKey = this.uncompressedHeaderKey.n(GenericKey.getN(card.getKey())).key();
            headerIterator.add(new HeaderCard(newKey, card.getValue(Integer.class, 0), card.getComment()));
        }

        @Override
        protected void copyCardBack(HeaderCard card, Cursor<String, HeaderCard> headerIterator) throws HeaderCardException {
            String newKey = this.compressedHeaderKey.n(GenericKey.getN(card.getKey())).key();
            headerIterator.add(new HeaderCard(newKey, card.getValue(Integer.class, 0), card.getComment()));
        }
    },
    MAP_ZPCOUNT(ZPCOUNT),
    MAP_ZQUANTIZ(ZQUANTIZ),
    MAP_ZSIMPLE(ZSIMPLE),
    MAP_ZTENSION(ZTENSION),
    MAP_ZTILEn(ZTILEn),
    MAP_ZVALn(ZVALn);

    protected static void copy(HeaderCard card, Cursor<String, HeaderCard> headerIterator) throws HeaderCardException {
        UncompressHeaderCardMapping mapping = selectMapping(CompressedImageHDU.UNCOMPRESSED_HEADER_MAPPING, card);
        mapping.copyCard(card, headerIterator);
    }

    protected static void copyBack(HeaderCard card, Cursor<String, HeaderCard> headerIterator) throws HeaderCardException {
        UncompressHeaderCardMapping mapping = selectMapping(CompressedImageHDU.COMPRESSED_HEADER_MAPPING, card);
        mapping.copyCardBack(card, headerIterator);
    }

    protected static UncompressHeaderCardMapping selectMapping(Map<IFitsHeader, UncompressHeaderCardMapping> mappings, HeaderCard card) {
        IFitsHeader key = GenericKey.lookup(card.getKey());
        if (key != null) {
            UncompressHeaderCardMapping mapping = mappings.get(key);
            if (mapping != null) {
                return mapping;
            }
        }
        return MAP_ANY;
    }

    protected final IFitsHeader compressedHeaderKey;

    protected final IFitsHeader uncompressedHeaderKey;

    private UncompressHeaderCardMapping(IFitsHeader header) {
        this.compressedHeaderKey = header;
        if (header instanceof Compression) {
            this.uncompressedHeaderKey = ((Compression) this.compressedHeaderKey).getUncompressedKey();

        } else {
            this.uncompressedHeaderKey = null;
        }
        CompressedImageHDU.UNCOMPRESSED_HEADER_MAPPING.put(header, this);
        if (this.uncompressedHeaderKey != null) {
            CompressedImageHDU.COMPRESSED_HEADER_MAPPING.put(this.uncompressedHeaderKey, this);
        }
    }

    /**
     * default behaviour is to ignore the card and by that to eclude it from the
     * uncompressed header if it does not have a uncompressed equivalent..
     *
     * @param card
     *            the card from the compressed header
     * @param headerIterator
     *            the iterator for the uncumpressed header.
     * @throws HeaderCardException
     *             if the card could not be copied
     */
    protected void copyCard(HeaderCard card, Cursor<String, HeaderCard> headerIterator) throws HeaderCardException {
        IFitsHeader uncompressedKey = this.uncompressedHeaderKey;
        copyCard(card, headerIterator, uncompressedKey);
    }

    protected void copyCard(HeaderCard card, Cursor<String, HeaderCard> headerIterator, IFitsHeader targetKey) throws HeaderCardException {
        if (targetKey != null) {
            if (targetKey.valueType() == VALUE.INTEGER) {
                headerIterator.add(new HeaderCard(targetKey.key(), card.getValue(Integer.class, 0), card.getComment()));
            } else if (targetKey.valueType() == VALUE.STRING) {
                headerIterator.add(new HeaderCard(targetKey.key(), card.getValue(), card.getComment()));
            } else if (targetKey.valueType() == VALUE.LOGICAL) {
                headerIterator.add(new HeaderCard(targetKey.key(), card.getValue(Boolean.class, false), card.getComment()));
            }
        }
    }

    protected void copyCardBack(HeaderCard card, Cursor<String, HeaderCard> headerIterator) throws HeaderCardException {
        copyCard(card, headerIterator, this.compressedHeaderKey);
    }
}
