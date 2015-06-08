package nom.tam.fits;

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
 * This class contains the code which associates particular FITS types with
 * header and data configurations. It comprises a set of Factory methods which
 * call appropriate methods in the HDU classes. If -- God forbid -- a new FITS
 * HDU type were created, then the XXHDU, XXData classes would need to be added
 * and this file modified but no other changes should be needed in the FITS
 * libraries.
 */
public class FitsFactory {

    private static boolean useAsciiTables = true;

    private static boolean useHierarch = false;

    private static boolean checkAsciiStrings = false;

    private static boolean allowTerminalJunk = false;

    private static boolean longStringsEnabled = false;

    /**
     * Given a Header return an appropriate datum.
     */
    public static Data dataFactory(Header hdr) throws FitsException {

        if (ImageHDU.isHeader(hdr)) {
            Data d = ImageHDU.manufactureData(hdr);
            hdr.afterExtend(); // Fix for positioning error noted by V. Forchi
            return d;
        } else if (RandomGroupsHDU.isHeader(hdr)) {
            return RandomGroupsHDU.manufactureData(hdr);
        } else if (FitsFactory.useAsciiTables && AsciiTableHDU.isHeader(hdr)) {
            return AsciiTableHDU.manufactureData(hdr);
        } else if (BinaryTableHDU.isHeader(hdr)) {
            return BinaryTableHDU.manufactureData(hdr);
        } else if (UndefinedHDU.isHeader(hdr)) {
            return UndefinedHDU.manufactureData(hdr);
        } else {
            throw new FitsException("Unrecognizable header in dataFactory");
        }

    }

    /**
     * Is terminal junk (i.e., non-FITS data following a valid HDU) allowed.
     */
    public static boolean getAllowTerminalJunk() {
        return FitsFactory.allowTerminalJunk;
    }

    /** Get the current status for string checking. */
    static boolean getCheckAsciiStrings() {
        return FitsFactory.checkAsciiStrings;
    }

    /** Get the current status of ASCII table writing */
    static boolean getUseAsciiTables() {
        return FitsFactory.useAsciiTables;
    }

    /** Are we processing HIERARCH style keywords */
    public static boolean getUseHierarch() {
        return FitsFactory.useHierarch;
    }

    /**
     * Given Header and data objects return the appropriate type of HDU.
     */
    @SuppressWarnings("unchecked")
    public static <DataClass extends Data> BasicHDU<DataClass> HDUFactory(Header hdr, DataClass d) throws FitsException {
        if (d instanceof ImageData) {
            return (BasicHDU<DataClass>) new ImageHDU(hdr, d);
        } else if (d instanceof RandomGroupsData) {
            return (BasicHDU<DataClass>) new RandomGroupsHDU(hdr, d);
        } else if (d instanceof AsciiTable) {
            return (BasicHDU<DataClass>) new AsciiTableHDU(hdr, d);
        } else if (d instanceof BinaryTable) {
            return (BasicHDU<DataClass>) new BinaryTableHDU(hdr, d);
        } else if (d instanceof UndefinedData) {
            return (BasicHDU<DataClass>) new UndefinedHDU(hdr, d);
        }
        return null;
    }

    /**
     * Given an object, create the appropriate FITS header to describe it.
     * 
     * @param o
     *            The object to be described.
     */
    public static BasicHDU<?> HDUFactory(Object o) throws FitsException {
        Data d;
        Header h;

        if (o instanceof Header) {
            h = (Header) o;
            d = dataFactory(h);

        } else if (ImageHDU.isData(o)) {
            d = ImageHDU.encapsulate(o);
            h = ImageHDU.manufactureHeader(d);
        } else if (RandomGroupsHDU.isData(o)) {
            d = RandomGroupsHDU.encapsulate(o);
            h = RandomGroupsHDU.manufactureHeader(d);
        } else if (FitsFactory.useAsciiTables && AsciiTableHDU.isData(o)) {
            d = AsciiTableHDU.encapsulate(o);
            h = AsciiTableHDU.manufactureHeader(d);
        } else if (BinaryTableHDU.isData(o)) {
            d = BinaryTableHDU.encapsulate(o);
            h = BinaryTableHDU.manufactureHeader(d);
        } else if (UndefinedHDU.isData(o)) {
            d = UndefinedHDU.encapsulate(o);
            h = UndefinedHDU.manufactureHeader(d);
        } else {
            throw new FitsException("Invalid data presented to HDUFactory");
        }

        return HDUFactory(h, d);
    }

    /**
     * Is long string support enabled.
     */
    public static boolean isLongStringsEnabled() {
        return FitsFactory.longStringsEnabled;
    }

    /** Do we allow junk after a valid FITS file? */
    public static void setAllowTerminalJunk(boolean flag) {
        FitsFactory.allowTerminalJunk = flag;
    }

    /**
     * Enable/Disable checking of strings values used in tables to ensure that
     * they are within the range specified by the FITS standard. The standard
     * only allows the values 0x20 - 0x7E with null bytes allowed in one limited
     * context. Disabled by default.
     */
    public static void setCheckAsciiStrings(boolean flag) {
        FitsFactory.checkAsciiStrings = flag;
    }

    /** Enable/Disable longstring support. */
    public static void setLongStringsEnabled(boolean flag) {
        FitsFactory.longStringsEnabled = flag;
    }

    /**
     * Indicate whether ASCII tables should be used where feasible.
     */
    public static void setUseAsciiTables(boolean flag) {
        FitsFactory.useAsciiTables = flag;
    }

    /** Enable/Disable hierarchical keyword processing. */
    public static void setUseHierarch(boolean flag) {
        FitsFactory.useHierarch = flag;
    }
}
