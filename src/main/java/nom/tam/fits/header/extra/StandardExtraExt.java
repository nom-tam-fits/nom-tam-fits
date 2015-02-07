package nom.tam.fits.header.extra;

import nom.tam.fits.header.FitsHeaderImpl;
import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.IFitsHeader.HDU;
import nom.tam.fits.header.IFitsHeader.SOURCE;
import nom.tam.fits.header.IFitsHeader.VALUE;

/**
 * A Set of FITS Standard Extensions that are detected as shared between alle
 * extra headers.
 * 
 * @author Richard van Nieuwenhoven.
 */
public enum StandardExtraExt implements IFitsHeader {
    /**
     * Total dark time of the observation. This is the total time during which
     * dark current is collected by the detector. If the times in the extension
     * are different the primary HDU gives one of the extension times.
     * <p>
     * units = UNITTIME
     * </p>
     * <p>
     * default value = EXPTIME
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DARKTIME(SOURCE.NOAO, HDU.PRIMARY_EXTENSION, VALUE.REAL, "Dark time");

    private IFitsHeader key;

    private StandardExtraExt(SOURCE source, HDU hdu, VALUE valueType, String comment) {
        this.key = new FitsHeaderImpl(name(), source, hdu, valueType, comment);
    }

    private StandardExtraExt(String key, SOURCE source, HDU hdu, VALUE valueType, String comment) {
        this.key = new FitsHeaderImpl(name(), source, hdu, valueType, comment);
    }

    @Override
    public String comment() {
        return key.comment();
    }

    @Override
    public HDU hdu() {
        return key.hdu();
    }

    @Override
    public String key() {
        return key.key();
    }

    @Override
    public IFitsHeader n(int number) {
        return key.n(number);
    }

    @Override
    public SOURCE status() {
        return key.status();
    }

    @Override
    public VALUE valueType() {
        return key.valueType();
    }
}
