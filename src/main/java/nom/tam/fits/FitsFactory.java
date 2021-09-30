package nom.tam.fits;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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
public final class FitsFactory {

    private static final boolean DEFAULT_USE_ASCII_TABLES = false;
    
    private static final boolean DEFAULT_USE_HIERARCH = true;
    
    private static final boolean DEFAULT_LONG_STRINGS_ENABLED = false;
    
    private static final boolean DEFAULT_CHECK_ASCII_STRINGS = false;
    
    private static final boolean DEFAULT_ALLOW_TERMINAL_JUNK = false;
    
    private static final boolean DEFAULT_ALLOW_HEADER_REPAIRS = false;
    
    private static final boolean DEFAULT_SKIP_BLANK_AFTER_ASSIGN = false;
    
    private static final boolean DEFAULT_CASE_SENSITIVE_HIERARCH = false;
    
    private static final IHierarchKeyFormatter DEFAULT_HIERARCH_FORMATTER = new StandardIHierarchKeyFormatter();
    
    
    protected static final class FitsSettings implements Cloneable {

        private boolean useAsciiTables;

        private boolean useHierarch;
        
        private boolean checkAsciiStrings;

        private boolean allowTerminalJunk;

        private boolean allowHeaderRepairs;

        private boolean longStringsEnabled;

        private boolean skipBlankAfterAssign;
        

        private IHierarchKeyFormatter hierarchKeyFormatter = DEFAULT_HIERARCH_FORMATTER;

        private FitsSettings() {
            useAsciiTables = DEFAULT_USE_ASCII_TABLES;

            // MBT (28-JUL-2017): change default from false to true.
            // This is required for HIERARCH-based wide fits processing
            // (see uk.ac.starlink.fits.WideFits).  If that gets backed out of,
            // this could be set back to its factory setting (false).
            useHierarch = DEFAULT_USE_HIERARCH;
            
            checkAsciiStrings = DEFAULT_CHECK_ASCII_STRINGS;
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

        protected IHierarchKeyFormatter getHierarchKeyFormatter() {
            return this.hierarchKeyFormatter;
        }
        
        

        protected boolean isAllowTerminalJunk() {
            return this.allowTerminalJunk;
        }

        protected boolean isCheckAsciiStrings() {
            return this.checkAsciiStrings;
        }

        protected boolean isLongStringsEnabled() {
            return this.longStringsEnabled;
        }

        protected boolean isSkipBlankAfterAssign() {
            return this.skipBlankAfterAssign;
        }

        protected boolean isUseAsciiTables() {
            return this.useAsciiTables;
        }

        protected boolean isUseHierarch() {
            return this.useHierarch;
        }

        protected boolean isAllowHeaderRepairs() {
            return this.allowHeaderRepairs;
        }

    }

    private static final FitsSettings GLOBAL_SETTINGS = new FitsSettings();

    private static final ThreadLocal<FitsSettings> LOCAL_SETTINGS = new ThreadLocal<>();

    private static ExecutorService threadPool;

    public static final int FITS_BLOCK_SIZE = 2880;

    /**
     * @return Given a Header construct an appropriate data.
     * @param hdr
     *            header to create the data from
     * @throws FitsException
     *             if the header did not contain enough information to detect
     *             the type of the data
     */
    public static Data dataFactory(Header hdr) throws FitsException {

        if (ImageHDU.isHeader(hdr)) {
            Data d = ImageHDU.manufactureData(hdr);
            hdr.afterExtend(); // Fix for positioning error noted by V. Forchi
            return d;
        } else if (RandomGroupsHDU.isHeader(hdr)) {
            return RandomGroupsHDU.manufactureData(hdr);
        } else if (current().isUseAsciiTables() && AsciiTableHDU.isHeader(hdr)) {
            return AsciiTableHDU.manufactureData(hdr);
        } else if (CompressedImageHDU.isHeader(hdr)) {
            return CompressedImageHDU.manufactureData(hdr);
        } else if (CompressedTableHDU.isHeader(hdr)) {
            return CompressedTableHDU.manufactureData(hdr);
        } else if (BinaryTableHDU.isHeader(hdr)) {
            return BinaryTableHDU.manufactureData(hdr);
        } else if (UndefinedHDU.isHeader(hdr)) {
            return UndefinedHDU.manufactureData(hdr);
        } else {
            throw new FitsException("Unrecognizable header in dataFactory");
        }

    }

    /**
     * @return Is terminal junk (i.e., non-FITS data following a valid HDU)
     *         allowed.
     */
    public static boolean getAllowTerminalJunk() {
        return current().isAllowTerminalJunk();
    }

    /**
     * @return Do we allow automatic header repairs, like missing end quotes?
     */
    public static boolean isAllowHeaderRepairs() {
        return current().isAllowHeaderRepairs();
    }

    /**
     * @return the formatter to use for hierarch keys.
     */
    public static IHierarchKeyFormatter getHierarchFormater() {
        return current().getHierarchKeyFormatter();
    }

    /**
     * @return <code>true</code> if we are processing HIERARCH style keywords
     */
    public static boolean getUseHierarch() {
        return current().isUseHierarch();
    }
    
    /**
     * whether ASCII tables should be used where feasible.
     * 
     * @return <code>true</code> if we ASCII tables are allowed.
     * 
     * @see #setUseAsciiTables(boolean)
     */
    public static boolean getUseAsciiTables() {
        return current().isUseAsciiTables();
    }
    

    /**
     * @return Get the current status for string checking.
     */
    public static boolean getCheckAsciiStrings() {
        return current().isCheckAsciiStrings();
    }
    
    /**
     * @return <code>true</code> If long string support is enabled.
     */
    public static boolean isLongStringsEnabled() {
        return current().isLongStringsEnabled();
    }

    /**
     * @return <code>true</code> If blanks after the assign are ommitted in the
     *         header.
     */
    public static boolean isSkipBlankAfterAssign() {
        return current().isSkipBlankAfterAssign();
    }
    
    
    /**
     * @return Given Header and data objects return the appropriate type of HDU.
     * @param hdr
     *            the header of the date
     * @param d
     *            the data
     * @param <DataClass>
     *            the class of the data
     * @throws FitsException
     *             if the operation failed
     */
    @SuppressWarnings("unchecked")
    public static <DataClass extends Data> BasicHDU<DataClass> hduFactory(Header hdr, DataClass d) throws FitsException {
        if (d instanceof ImageData) {
            return (BasicHDU<DataClass>) new ImageHDU(hdr, (ImageData) d);
        } else if (d instanceof CompressedImageData) {
            return (BasicHDU<DataClass>) new CompressedImageHDU(hdr, (CompressedImageData) d);
        } else if (d instanceof RandomGroupsData) {
            return (BasicHDU<DataClass>) new RandomGroupsHDU(hdr, (RandomGroupsData) d);
        } else if (current().isUseAsciiTables() && d instanceof AsciiTable) {
            return (BasicHDU<DataClass>) new AsciiTableHDU(hdr, (AsciiTable) d);
        } else if (d instanceof CompressedTableData) {
            return (BasicHDU<DataClass>) new CompressedTableHDU(hdr, (CompressedTableData) d);
        } else if (d instanceof BinaryTable) {
            return (BasicHDU<DataClass>) new BinaryTableHDU(hdr, (BinaryTable) d);
        } else if (d instanceof UndefinedData) {
            return (BasicHDU<DataClass>) new UndefinedHDU(hdr, (UndefinedData) d);
        }
        return null;
    }

    /**
     * @return Given an object, create the appropriate FITS header to describe
     *         it.
     * @param o
     *            The object to be described.
     * @throws FitsException
     *             if the parameter could not be converted to a hdu.
     */
    public static BasicHDU<?> hduFactory(Object o) throws FitsException {
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
        } else if (current().isUseAsciiTables() && AsciiTableHDU.isData(o)) {
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

        return hduFactory(h, d);
    }

    // CHECKSTYLE:OFF
    /**
     * @return Given Header and data objects return the appropriate type of HDU.
     * @param hdr
     *            the header of the date
     * @param d
     *            the data
     * @param <DataClass>
     *            the class of the data
     * @throws FitsException
     *             if the operation failed
     * @deprecated use {@link #hduFactory(Header, Data)} instead
     */
    @Deprecated
    public static <DataClass extends Data> BasicHDU<DataClass> HDUFactory(Header hdr, DataClass d) throws FitsException {
        return hduFactory(hdr, d);
    }

    // CHECKSTYLE:ON

    // CHECKSTYLE:OFF
    /**
     * @return Given an object, create the appropriate FITS header to describe
     *         it.
     * @param o
     *            The object to be described.
     * @throws FitsException
     *             if the parameter could not be converted to a hdu.
     * @deprecated use {@link #hduFactory(Object)} instead
     */
    @Deprecated
    public static BasicHDU<?> HDUFactory(Object o) throws FitsException {
        return hduFactory(o);
    }

    // CHECKSTYLE:ON
    
    /**
     * Restores all settings to their default values.
     * 
     */
    public static void setDefaults() {
        FitsSettings s = current();
        s.allowHeaderRepairs = DEFAULT_ALLOW_HEADER_REPAIRS;
        s.allowTerminalJunk = DEFAULT_ALLOW_TERMINAL_JUNK;
        s.checkAsciiStrings = DEFAULT_CHECK_ASCII_STRINGS;
        s.longStringsEnabled = DEFAULT_LONG_STRINGS_ENABLED;
        s.skipBlankAfterAssign = DEFAULT_SKIP_BLANK_AFTER_ASSIGN;
        s.useAsciiTables = DEFAULT_USE_ASCII_TABLES;
        s.useHierarch = DEFAULT_USE_HIERARCH;        
        s.hierarchKeyFormatter = DEFAULT_HIERARCH_FORMATTER;
        s.hierarchKeyFormatter.setCaseSensitive(DEFAULT_CASE_SENSITIVE_HIERARCH);
    }
    

    /**
     * Do we allow junk after a valid FITS file?
     *
     * @param allowTerminalJunk
     *            value to set
     */
    public static void setAllowTerminalJunk(boolean allowTerminalJunk) {
        current().allowTerminalJunk = allowTerminalJunk;
    }

    /**
     * Do we allow automatic header repairs, like missing end quotes?
     *
     * @param allowHeaderRepairs
     *            value to set
     */
    public static void setAllowHeaderRepairs(boolean allowHeaderRepairs) {
        current().allowHeaderRepairs = allowHeaderRepairs;
    }

    /**
     * Enable/Disable checking of strings values used in tables to ensure that
     * they are within the range specified by the FITS standard. The standard
     * only allows the values 0x20 - 0x7E with null bytes allowed in one limited
     * context. Disabled by default.
     *
     * @param checkAsciiStrings
     *            value to set
     */
    public static void setCheckAsciiStrings(boolean checkAsciiStrings) {
        current().checkAsciiStrings = checkAsciiStrings;
    }

    /**
     * There is not a real standard how to write hierarch keys, default we use
     * the one where every key is separated by a blank. If you want or need
     * another format assing the formater here.
     *
     * @param formatter
     *            the hierarch key formatter.
     */
    public static void setHierarchFormater(IHierarchKeyFormatter formatter) {
        current().hierarchKeyFormatter = formatter;
    }

    /**
     * Enable/Disable longstring support.
     *
     * @param longStringsEnabled
     *            value to set
     */
    public static void setLongStringsEnabled(boolean longStringsEnabled) {
        current().longStringsEnabled = longStringsEnabled;
    }

    /**
     * If set to true the blank after the assign in the header cards in not
     * written. The blank is stronly recommendet but in some cases it is
     * important that it can be ommitted.
     *
     * @param skipBlankAfterAssign
     *            value to set
     */
    public static void setSkipBlankAfterAssign(boolean skipBlankAfterAssign) {
        current().skipBlankAfterAssign = skipBlankAfterAssign;
    }

    /**
     * Indicate whether ASCII tables should be used where feasible.
     *
     * @param useAsciiTables
     *            value to set
     */
    public static void setUseAsciiTables(boolean useAsciiTables) {
        current().useAsciiTables = useAsciiTables;
    }

    /**
     * Enable/Disable hierarchical keyword processing.
     *
     * @param useHierarch
     *            value to set
     */
    public static void setUseHierarch(boolean useHierarch) {
        current().useHierarch = useHierarch;
    }

    public static ExecutorService threadPool() {
        if (threadPool == null) {
            initializeThreadPool();
        }
        return threadPool;
    }

    /**
     * Use thread local settings for the current thread instead of the global
     * ones if the parameter is set to true, else use the shared global
     * settings.
     *
     * @param useThreadSettings
     *            true if the thread should not share the global settings.
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
                                Thread thread = new Thread(r, "nom-tam-fits worker " + this.counter++);
                                thread.setDaemon(true);
                                return thread;
                            }
                        });
            }
        }
    }

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
