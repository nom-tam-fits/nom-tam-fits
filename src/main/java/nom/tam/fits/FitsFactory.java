package nom.tam.fits;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import nom.tam.fits.header.Standard;
import nom.tam.fits.header.hierarch.IHierarchKeyFormatter;
import nom.tam.fits.header.hierarch.StandardIHierarchKeyFormatter;
import nom.tam.image.compression.hdu.CompressedImageData;
import nom.tam.image.compression.hdu.CompressedImageHDU;
import nom.tam.image.compression.hdu.CompressedTableData;
import nom.tam.image.compression.hdu.CompressedTableHDU;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2021 nom-tam-fits
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
 * This class contains the code which associates particular FITS types with header and data configurations. It comprises
 * a set of factory methods which call appropriate methods in the HDU classes. If -- God forbid -- a new FITS HDU type
 * were created, then the XXHDU, XXData classes would need to be added and this file modified but no other changes
 * should be needed in the FITS libraries.
 */
public final class FitsFactory {

    private static final boolean DEFAULT_USE_ASCII_TABLES = false;

    private static final boolean DEFAULT_USE_HIERARCH = true;

    private static final boolean DEFAULT_USE_EXPONENT_D = false;

    private static final boolean DEFAULT_LONG_STRINGS_ENABLED = true;

    private static final boolean DEFAULT_CHECK_ASCII_STRINGS = false;

    private static final boolean DEFAULT_ALLOW_TERMINAL_JUNK = true;

    private static final boolean DEFAULT_ALLOW_HEADER_REPAIRS = true;

    private static final boolean DEFAULT_SKIP_BLANK_AFTER_ASSIGN = false;

    private static final boolean DEFAULT_CASE_SENSITIVE_HIERARCH = false;

    /**
     * AK: true is the legacy behavior TODO If and when it is changed to false, the corresponding Logger warnings in
     * BinaryTable should also be removed.
     */
    private static final boolean DEFAULT_USE_UNICODE_CHARS = true;

    private static final IHierarchKeyFormatter DEFAULT_HIERARCH_FORMATTER = new StandardIHierarchKeyFormatter();

    /**
     * An class for aggregating all the settings internal to {@link FitsFactory}.
     * 
     * @author Attila Kovacs
     */
    protected static final class FitsSettings implements Cloneable {

        private boolean useAsciiTables;

        private boolean useHierarch;

        private boolean useExponentD;

        private boolean checkAsciiStrings;

        private boolean allowTerminalJunk;

        private boolean allowHeaderRepairs;

        private boolean longStringsEnabled;

        private boolean useUnicodeChars;

        @Deprecated
        private boolean skipBlankAfterAssign;

        private IHierarchKeyFormatter hierarchKeyFormatter = DEFAULT_HIERARCH_FORMATTER;

        private FitsSettings() {
            useAsciiTables = DEFAULT_USE_ASCII_TABLES;
            useHierarch = DEFAULT_USE_HIERARCH;
            useUnicodeChars = DEFAULT_USE_UNICODE_CHARS;
            checkAsciiStrings = DEFAULT_CHECK_ASCII_STRINGS;
            useExponentD = DEFAULT_USE_EXPONENT_D;
            allowTerminalJunk = DEFAULT_ALLOW_TERMINAL_JUNK;
            allowHeaderRepairs = DEFAULT_ALLOW_HEADER_REPAIRS;
            longStringsEnabled = DEFAULT_LONG_STRINGS_ENABLED;
            skipBlankAfterAssign = DEFAULT_SKIP_BLANK_AFTER_ASSIGN;
            hierarchKeyFormatter = DEFAULT_HIERARCH_FORMATTER;
            hierarchKeyFormatter.setCaseSensitive(DEFAULT_CASE_SENSITIVE_HIERARCH);
        }

        @Override
        protected FitsSettings clone() {
            try {
                return (FitsSettings) super.clone();
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }

        private FitsSettings copy() {
            return clone();
        }

        /**
         * Returns the formatter instance for HIERARCH style keywords. Our own standard is to define such keywords
         * internally as starting with the string <code>HIERARCH.</code> followed by a dot-separated hierarchy, or just
         * an unusually long FITS keywords that cannot be represented by a standard 8-byte keyword. The HIERARCH
         * formatted will take such string keywords and will format them according to its rules when writing them to
         * FITS headers.
         * 
         * @return The formatter instance used for HIERARCH-style keywords.
         */
        protected IHierarchKeyFormatter getHierarchKeyFormatter() {
            return hierarchKeyFormatter;
        }

        /**
         * Checks if we should use the letter 'D' to mark exponents of double-precision values (in FITS headers and
         * ASCII tables). For ecample, in the typical Java number formatting the String <code>1.37E-13</code> may
         * represent either a <code>float</code> or <code>double</code> value -- which are not exactly the same. For
         * that reason FITS offers the possibility to replace 'E' in the string formatted number with 'D' when the value
         * specifies a double-precision number, thus disambiguating the two.
         * 
         * @return <code>true</code> if we will use 'D' to denote the exponent of double-precision values in FITS
         *             headers and ASCII tables.
         */
        protected boolean isUseExponentD() {
            return useExponentD;
        }

        /**
         * Checks if we treat junk after the last properly formed HDU silently withotu generating an exception. When
         * this setting is <code>true</code> we can read corrupted FITS files (at least partially) without raising an
         * alarm.
         * 
         * @return <code>true</code> if we allow additional bytes after the last readable HDU to be present in FITS
         *             files without throwing an exception. Otherwise <code>false</code>.
         */
        protected boolean isAllowTerminalJunk() {
            return allowTerminalJunk;
        }

        /**
         * Whether we check if ASCII strings in FITS files conform to the restricted set of characters (0x20 trough
         * 0x7E) allowed by the FITS standard. If the checking is enabled, we will log any such violations so they can
         * be inspected and perhaps fixed.
         * 
         * @return <code>true</code> if we should check and report if string appearing in FITS files do not conform to
         *             specification. Otherwise <code>false</code>
         */
        protected boolean isCheckAsciiStrings() {
            return checkAsciiStrings;
        }

        /**
         * Checks if we allow storing long string values (using the OGIP 1.0 convention) in FITS headers. Such long
         * string may span multiple 80-character header records. They are now standard as of FITS 4.0, but they were not
         * in earlier specifications. When long strings are not enabled, we will throw a {@link LongValueException}
         * whenever one tries to add a string value that cannot be contained in a single 80-character header record.
         * 
         * @return <code>true</code> (default) if we allow adding long string values to out FITS headers. Otherwise
         *             <code>false</code>.
         */
        protected boolean isLongStringsEnabled() {
            return longStringsEnabled;
        }

        /**
         * @deprecated The FITS standard is very explicit that assignment must be "= ". If we allow skipping the space,
         *                 it will result in a non-standard FITS, that is likely to break compatibility with other
         *                 tools.
         *
         * @return     whether to use only "=", instead of the standard "= " between the keyword and the value.
         */
        @Deprecated
        protected boolean isSkipBlankAfterAssign() {
            return skipBlankAfterAssign;
        }

        /**
         * Whether to write tables as ASCII tables automatically if possible. Binary tables are generally always a
         * better option, as they are both more compact and flexible but sometimes we might want to make our table data
         * to be human readable in a terminal without needing any FITS-specific tool -- even though the 1970s is long
         * past...
         * 
         * @return <code>true</code> if we have a preference for writing table data in ASCII format (rather than
         *             binary), whenever that is possible. Otherwise <code>false</code>
         */
        protected boolean isUseAsciiTables() {
            return useAsciiTables;
        }

        /**
         * Whether we allow using HIERARCH-style keywords, which may be longer than the standard 8-character FITS
         * keywords, and may specify a hierarchy, and may also allow upper and lower-case characters depending on what
         * formatting rules we use. Our own standard is to define such keywords internally as starting with the string
         * <code>HIERARCH.</code> followed by a dot-separated hierarchy, or just an unusually long FITS keywords that
         * cannot be represented by a standard 8-byte keyword.
         * 
         * @return <code>true</code> if we allow HIERARCH keywords. Otherwise <code>false</code>
         */
        protected boolean isUseHierarch() {
            return useHierarch;
        }

        /**
         * Checks if we allow storing Java <code>char[]</code> arrays in binary tables as 16-bit <code>short[]</code>.
         * Otherwise we will store them as simple 8-bit ASCII.
         * 
         * @return <code>true</code> if <code>char[]</code> is stored as <code>short[]</code> in binary tables, or
         *             <code>false</code> if we store than as 8-bit ASCII.
         */
        protected boolean isUseUnicodeChars() {
            return useUnicodeChars;
        }

        /**
         * Checks if we are tolerant to FITS standard violations when reading 3rd party FITS files.
         * 
         * @return <code>true</code> if we tolerate minor violations of the FITS standard when interpreting headers,
         *             which are unlikely to affect the integrity of the FITS otherwise. The violations will still be
         *             logged, but no exception will be generated. Or, <code>false</code> if we want to generate
         *             exceptions for such error.s
         */
        protected boolean isAllowHeaderRepairs() {
            return allowHeaderRepairs;
        }

    }

    private static final FitsSettings GLOBAL_SETTINGS = new FitsSettings();

    private static final ThreadLocal<FitsSettings> LOCAL_SETTINGS = new ThreadLocal<>();

    private static ExecutorService threadPool;

    /**
     * the size of a FITS block in bytes.
     */
    public static final int FITS_BLOCK_SIZE = 2880;

    /**
     * @deprecated               (<i>for internal use</i>) Will reduce visibility in the future
     *
     * @return                   Given a Header construct an appropriate data.
     *
     * @param      hdr           header to create the data from
     *
     * @throws     FitsException if the header did not contain enough information to detect the type of the data
     */
    @Deprecated
    public static Data dataFactory(Header hdr) throws FitsException {

        if (ImageHDU.isHeader(hdr)) {
            if (hdr.getIntValue(Standard.NAXIS, 0) == 0) {
                return new NullData();
            }

            Data d = ImageHDU.manufactureData(hdr);
            // Fix for positioning error noted by V. Forchi
            if (hdr.findCard(Standard.EXTEND) != null) {
                hdr.nextCard();
            }
            return d;
        }
        if (RandomGroupsHDU.isHeader(hdr)) {
            return RandomGroupsHDU.manufactureData(hdr);
        }
        if (current().isUseAsciiTables() && AsciiTableHDU.isHeader(hdr)) {
            return AsciiTableHDU.manufactureData(hdr);
        }
        if (CompressedImageHDU.isHeader(hdr)) {
            return CompressedImageHDU.manufactureData(hdr);
        }
        if (CompressedTableHDU.isHeader(hdr)) {
            return CompressedTableHDU.manufactureData(hdr);
        }
        if (BinaryTableHDU.isHeader(hdr)) {
            return BinaryTableHDU.manufactureData(hdr);
        }
        if (UndefinedHDU.isHeader(hdr)) {
            return UndefinedHDU.manufactureData(hdr);
        }
        throw new FitsException("Unrecognizable header in dataFactory");
    }

    /**
     * Whether the letter 'D' may replace 'E' in the exponential notation of doubl-precision values. FITS allows (even
     * encourages) the use of 'D' to indicate double-recision values. For example to disambiguate between 1.37E-3
     * (single-precision) and 1.37D-3 (double-precision), which are not exatly the same value in binary representation.
     * 
     * @return Do we allow automatic header repairs, like missing end quotes?
     *
     * @since  1.16
     * 
     * @see    #setUseExponentD(boolean)
     */
    public static boolean isUseExponentD() {
        return current().isUseExponentD();
    }

    /**
     * Whether <code>char[]</code> arrays are written as 16-bit integers (<code>short[]</code>) int binary tables as
     * opposed as FITS character arrays (<code>byte[]</code> with column type 'A'). See more explanation in
     * {@link #setUseUnicodeChars(boolean)}.
     *
     * @return <code>true</code> if <code>char[]</code> get written as 16-bit integers in binary table columns (column
     *             type 'I'), or as FITS 1-byte ASCII character arrays (as is always the case for <code>String</code>)
     *             with column type 'A'.
     *
     * @since  1.16
     * 
     * @see    #setUseUnicodeChars(boolean)
     */
    public static boolean isUseUnicodeChars() {
        return current().isUseUnicodeChars();
    }

    /**
     * Whether extra bytes are tolerated after the end of an HDU. Normally if there is additional bytes present after an
     * HDU, it would be the beginning of another HDU -- which must start with a very specific sequence of bytes. So,
     * when there is data beyond the end of an HDU that does not appear to be another HDU, it's junk. We can either
     * ignore it, or throw an exception.
     * 
     * @return Is terminal junk (i.e., non-FITS data following a valid HDU) allowed.
     * 
     * @see    #setAllowTerminalJunk(boolean)
     */
    public static boolean getAllowTerminalJunk() {
        return current().isAllowTerminalJunk();
    }

    /**
     * Whether we allow 3rd party FITS headers to be in violation of the standard, attempting to make sense of corrupted
     * header data as much as possible.
     * 
     * @return Do we allow automatic header repairs, like missing end quotes?
     * 
     * @see    #setAllowHeaderRepairs(boolean)
     */
    public static boolean isAllowHeaderRepairs() {
        return current().isAllowHeaderRepairs();
    }

    /**
     * Returns the formatter instance for HIERARCH style keywords. Our own standard is to define such keywords
     * internally as starting with the string <code>HIERARCH.</code> followed by a dot-separated hierarchy, or just an
     * unusually long FITS keywords that cannot be represented by a standard 8-byte keyword. The HIERARCH formatted will
     * take such string keywords and will format them according to its rules when writing them to FITS headers.
     * 
     * @return the formatter to use for hierarch keys.
     * 
     * @see    #setHierarchFormater(IHierarchKeyFormatter)
     */
    public static IHierarchKeyFormatter getHierarchFormater() {
        return current().getHierarchKeyFormatter();
    }

    /**
     * Whether we can use HIERARCH style keywords. Such keywords are not part of the current FITS standard, although
     * they constitute a recognised convention. Even if other programs may not process HIRARCH keywords themselves,
     * there is generally no harm to putting them into FITS headers, since the convention is such that these keywords
     * will be simply treated as comments by programs that do not recognise them.
     * 
     * @return <code>true</code> if we are processing HIERARCH style keywords
     * 
     * @see    #setUseHierarch(boolean)
     */
    public static boolean getUseHierarch() {
        return current().isUseHierarch();
    }

    /**
     * whether ASCII tables should be used where feasible.
     *
     * @return <code>true</code> if we ASCII tables are allowed.
     *
     * @see    #setUseAsciiTables(boolean)
     */
    public static boolean getUseAsciiTables() {
        return current().isUseAsciiTables();
    }

    /**
     * Checks whether we should check and validated ASCII strings that goe into FITS. FITS only allows ASCII characters
     * between 0x20 and 0x7E in ASCII tables.
     * 
     * @return Get the current status for string checking.
     * 
     * @see    #setCheckAsciiStrings(boolean)
     */
    public static boolean getCheckAsciiStrings() {
        return current().isCheckAsciiStrings();
    }

    /**
     * Whether we allow storing long string in the header, which do not fit into a single 80-byte header record. Such
     * strings are then wrapped into multiple consecutive header records, OGIP 1.0 standard -- which is nart of FITS
     * 4.0, and was a recognised convention before.
     * 
     * @return <code>true</code> If long string support is enabled.
     * 
     * @see    #setLongStringsEnabled(boolean)
     */
    public static boolean isLongStringsEnabled() {
        return current().isLongStringsEnabled();
    }

    /**
     * @return     whether to use only "=", instead of the standard "= " between the keyword and the value.
     *
     * @deprecated The FITS standard is very explicit that assignment must be "= " (equals followed by a blank space).
     *                 If we allow skipping the space, it will result in a non-standard FITS, that is likely to break
     *                 compatibility with other tools.
     * 
     * @see        #setSkipBlankAfterAssign(boolean)
     */
    @Deprecated
    public static boolean isSkipBlankAfterAssign() {
        return current().isSkipBlankAfterAssign();
    }

    /**
     * .
     * 
     * @deprecated               (<i>for internal use</i>)/ Will reduce visibility in the future
     *
     * @return                   Given Header and data objects return the appropriate type of HDU.
     *
     * @param      hdr           the header, including a description of the data layout.
     * @param      d             the type of data object
     * @param      <DataClass>   the class of the data
     *
     * @throws     FitsException if the operation failed
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static <DataClass extends Data> BasicHDU<DataClass> hduFactory(Header hdr, DataClass d) throws FitsException {
        if (d == null) {
            return (BasicHDU<DataClass>) new NullDataHDU(hdr);
        }
        if (d instanceof ImageData) {
            return (BasicHDU<DataClass>) new ImageHDU(hdr, (ImageData) d);
        }
        if (d instanceof CompressedImageData) {
            return (BasicHDU<DataClass>) new CompressedImageHDU(hdr, (CompressedImageData) d);
        }
        if (d instanceof RandomGroupsData) {
            return (BasicHDU<DataClass>) new RandomGroupsHDU(hdr, (RandomGroupsData) d);
        }
        if (d instanceof AsciiTable) {
            return (BasicHDU<DataClass>) new AsciiTableHDU(hdr, (AsciiTable) d);
        }
        if (d instanceof CompressedTableData) {
            return (BasicHDU<DataClass>) new CompressedTableHDU(hdr, (CompressedTableData) d);
        }
        if (d instanceof BinaryTable) {
            return (BasicHDU<DataClass>) new BinaryTableHDU(hdr, (BinaryTable) d);
        }
        if (d instanceof UndefinedData) {
            return (BasicHDU<DataClass>) new UndefinedHDU(hdr, (UndefinedData) d);
        }
        return null;
    }

    /**
     * Creates an HDU that wraps around the specified data object. The HDUs header will be created and populated with
     * the essential description of the data. The following HDU types may be returned depending on the nature of the
     * argument:
     * <ul>
     * <li>{@link NullDataHDU} -- if the argument is <code>null</code></li>
     * <li>{@link ImageHDU} -- if the argument is a regular numerical array, such as a <code>double[]</code>,
     * <code>float[][]</code>, or <code>short[][][]</code></li>
     * <li>{@link BinaryTableHDU} -- the the argument is an <code>Object[rows][cols]</code> type array with a regular
     * structure and supported column data types, provided that it cannot be represented by an ASCII table <b>OR</b> if
     * {@link FitsFactory#getUseAsciiTables()} is <code>false</code></li>
     * <li>{@link AsciiTableHDU} -- Like above, but only when the data can be represented by an ASCII table <b>AND</b>
     * {@link FitsFactory#getUseAsciiTables()} is <code>true</code></li>
     * </ul>
     * 
     * @return                   An appropriate HDU to encapsulate the given Java data object
     *
     * @param      o             The object to be described.
     *
     * @throws     FitsException if the parameter could not be converted to a HDU because the binary representation of
     *                               the object is not known..
     * 
     * @deprecated               Use {@link Fits#makeHDU(Object)} instead (this method may either be migrated to
     *                               {@link Fits} entirely or else have visibility reduced to the package level).
     */
    public static BasicHDU<?> hduFactory(Object o) throws FitsException {
        Data d;
        Header h;

        if (o == null) {
            return new NullDataHDU();
        } else if (o instanceof Header) {
            h = (Header) o;
            d = dataFactory(h);
        } else if (ImageHDU.isData(o)) {
            d = ImageHDU.encapsulate(o);
            h = ImageHDU.manufactureHeader(d);
        } else if (current().isUseAsciiTables() && AsciiTableHDU.isData(o)) {
            d = AsciiTableHDU.encapsulate(o);
            h = AsciiTableHDU.manufactureHeader(d);
        } else if (BinaryTableHDU.isData(o)) {
            d = BinaryTableHDU.encapsulate(o);
            h = BinaryTableHDU.manufactureHeader(d);
        } else {
            throw new FitsException("This type of data is not supported for FITS representation");
        }

        return hduFactory(h, d);
    }

    // CHECKSTYLE:OFF
    /**
     * @deprecated               (<i>duplicate method for internal use</i>) Same as {@link #hduFactory(Header, Data)},
     *                               and will be removed in the future.
     *
     * @return                   Given Header and data objects return the appropriate type of HDU.
     *
     * @param      hdr           the header of the date
     * @param      d             the data
     * @param      <DataClass>   the class of the data
     *
     * @throws     FitsException if the operation failed
     */
    @Deprecated
    public static <DataClass extends Data> BasicHDU<DataClass> HDUFactory(Header hdr, DataClass d) throws FitsException {
        return hduFactory(hdr, d);
    }

    // CHECKSTYLE:ON

    // CHECKSTYLE:OFF
    /**
     * @return                   Given an object, create the appropriate FITS header to describe it.
     *
     * @param      o             The object to be described.
     *
     * @throws     FitsException if the parameter could not be converted to a hdu.
     *
     * @deprecated               Use {@link Fits#makeHDU(Object)} instead (will removed in the future. Duplicate of
     *                               {@link #hduFactory(Object)}
     */
    @Deprecated
    public static BasicHDU<?> HDUFactory(Object o) throws FitsException {
        return hduFactory(o);
    }

    // CHECKSTYLE:ON

    /**
     * Restores all settings to their default values.
     *
     * @since 1.16
     */
    public static void setDefaults() {
        FitsSettings s = current();
        s.useExponentD = DEFAULT_USE_EXPONENT_D;
        s.allowHeaderRepairs = DEFAULT_ALLOW_HEADER_REPAIRS;
        s.allowTerminalJunk = DEFAULT_ALLOW_TERMINAL_JUNK;
        s.checkAsciiStrings = DEFAULT_CHECK_ASCII_STRINGS;
        s.longStringsEnabled = DEFAULT_LONG_STRINGS_ENABLED;
        s.skipBlankAfterAssign = DEFAULT_SKIP_BLANK_AFTER_ASSIGN;
        s.useAsciiTables = DEFAULT_USE_ASCII_TABLES;
        s.useHierarch = DEFAULT_USE_HIERARCH;
        s.useUnicodeChars = DEFAULT_USE_UNICODE_CHARS;
        s.hierarchKeyFormatter = DEFAULT_HIERARCH_FORMATTER;
        s.hierarchKeyFormatter.setCaseSensitive(DEFAULT_CASE_SENSITIVE_HIERARCH);
    }

    /**
     * Sets whether 'D' may be used instead of 'E' to mark the exponent for a floating point value with precision beyond
     * that of a 32-bit float.
     *
     * @param allowExponentD if <code>true</code> D will be used instead of E to indicate the exponent of a decimal with
     *                           more precision than a 32-bit float.
     *
     * @since                1.16
     * 
     * @see                  #isUseExponentD()
     */
    public static void setUseExponentD(boolean allowExponentD) {
        current().useExponentD = allowExponentD;
    }

    /**
     * Do we allow junk after a valid FITS file?
     *
     * @param allowTerminalJunk value to set
     * 
     * @see                     #getAllowTerminalJunk()
     */
    public static void setAllowTerminalJunk(boolean allowTerminalJunk) {
        current().allowTerminalJunk = allowTerminalJunk;
    }

    /**
     * Do we allow automatic header repairs, like missing end quotes?
     *
     * @param allowHeaderRepairs value to set
     * 
     * @see                      #isAllowHeaderRepairs()
     */
    public static void setAllowHeaderRepairs(boolean allowHeaderRepairs) {
        current().allowHeaderRepairs = allowHeaderRepairs;
    }

    /**
     * Enable/Disable checking of strings values used in tables to ensure that they are within the range specified by
     * the FITS standard. The standard only allows the values 0x20 - 0x7E with null bytes allowed in one limited
     * context. Disabled by default.
     *
     * @param checkAsciiStrings value to set
     * 
     * @see                     #getCheckAsciiStrings()
     */
    public static void setCheckAsciiStrings(boolean checkAsciiStrings) {
        current().checkAsciiStrings = checkAsciiStrings;
    }

    /**
     * There is not a real standard how to write hierarch keys, default we use the one where every key is separated by a
     * blank. If you want or need another format assing the formater here.
     *
     * @param formatter the hierarch key formatter.
     */
    public static void setHierarchFormater(IHierarchKeyFormatter formatter) {
        current().hierarchKeyFormatter = formatter;
    }

    /**
     * Enable/Disable longstring support.
     *
     * @param longStringsEnabled value to set
     * 
     * @see                      #isLongStringsEnabled()
     */
    public static void setLongStringsEnabled(boolean longStringsEnabled) {
        current().longStringsEnabled = longStringsEnabled;
    }

    /**
     * If set to true the blank after the assign in the header cards in not written. The blank is stronly recommendet
     * but in some cases it is important that it can be ommitted.
     *
     * @param      skipBlankAfterAssign value to set
     *
     * @deprecated                      The FITS standard is very explicit that assignment must be "= " (equals followed
     *                                      by a blank space). It is also very specific that string values must have
     *                                      their opening quote in byte 11 (counted from 1). If we allow skipping the
     *                                      space, we will violate both standards in a way that is likely to break
     *                                      compatibility with other tools.
     * 
     * @see                             #isSkipBlankAfterAssign()
     */
    @Deprecated
    public static void setSkipBlankAfterAssign(boolean skipBlankAfterAssign) {
        current().skipBlankAfterAssign = skipBlankAfterAssign;
    }

    /**
     * Indicate whether ASCII tables should be used where feasible.
     *
     * @param useAsciiTables value to set
     */
    public static void setUseAsciiTables(boolean useAsciiTables) {
        current().useAsciiTables = useAsciiTables;
    }

    /**
     * Enable/Disable hierarchical keyword processing.
     *
     * @param useHierarch value to set
     */
    public static void setUseHierarch(boolean useHierarch) {
        current().useHierarch = useHierarch;
    }

    /**
     * <p>
     * Enable/Disable writing <code>char[]</code> arrays as <code>short[]</code> in FITS binary tables (with column type
     * 'I'), instead of as standard FITS 1-byte ASCII characters (with column type 'A'). The old default of this library
     * has been to use unicode, and that behavior remains the default &mdash; the same as setting the argument to
     * <code>true</code>. On the flipside, setting it to <code>false</code> provides more convergence between the
     * handling of <code>char[]</code> columns and the nearly identical <code>String</code> columns, which have already
     * been restricted to ASCII before.
     * </p>
     *
     * @param value <code>true</code> to write <code>char[]</code> arrays as if <code>short[]</code> with column type
     *                  'I' to binary tables (old behaviour, and hence default), or else <code>false</code> to write
     *                  them as <code>byte[]</code> with column type 'A', the same as for <code>String</code> (preferred
     *                  behaviour)
     *
     * @since       1.16
     *
     * @see         #isUseUnicodeChars()
     */
    public static void setUseUnicodeChars(boolean value) {
        current().useUnicodeChars = value;
    }

    /**
     * Returns the common thread pool that we use for processing FITS files.
     * 
     * @return the thread pool for processing FITS files.
     */
    public static ExecutorService threadPool() {
        if (threadPool == null) {
            initializeThreadPool();
        }
        return threadPool;
    }

    /**
     * Use thread local settings for the current thread instead of the global ones if the parameter is set to true, else
     * use the shared global settings.
     *
     * @param useThreadSettings true if the thread should not share the global settings.
     */
    public static void useThreadLocalSettings(boolean useThreadSettings) {
        if (useThreadSettings) {
            LOCAL_SETTINGS.set(GLOBAL_SETTINGS.copy());
        } else {
            LOCAL_SETTINGS.remove();
        }
    }

    private static void initializeThreadPool() {
        synchronized (GLOBAL_SETTINGS) {
            if (threadPool == null) {
                // 1.5 thread per core
                threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2, //
                        new ThreadFactory() {

                            private int counter = 1;

                            @Override
                            public Thread newThread(Runnable r) {
                                Thread thread = new Thread(r, "nom-tam-fits worker " + counter++);
                                thread.setDaemon(true);
                                return thread;
                            }
                        });
            }
        }
    }

    /**
     * Returns the current settings that guide how we read or produce FITS files.
     * 
     * @return the current active settings for generating or interpreting FITS files.
     */
    protected static FitsSettings current() {
        FitsSettings settings = LOCAL_SETTINGS.get();
        if (settings == null) {
            return GLOBAL_SETTINGS;
        }
        return settings;
    }

    private FitsFactory() {
    }
}
