package nom.tam.fits;

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

import java.io.Closeable;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.compress.CompressionManager;
import nom.tam.fits.header.Standard;
import nom.tam.fits.utilities.FitsCheckSum;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.FitsFile;
import nom.tam.util.FitsIO;
import nom.tam.util.FitsInputStream;
import nom.tam.util.FitsOutputStream;
import nom.tam.util.RandomAccess;
import nom.tam.util.RandomAccessFileIO;
import nom.tam.util.SafeClose;

import static nom.tam.fits.header.Standard.EXTNAME;
import static nom.tam.fits.header.Standard.EXTVER;

/**
 * <p>
 * Handling of FITS files and streams. This class is a container of HDUs (header-data units), which together constitute
 * a complete FITS file. Users of this library are strongly encouraged to study the
 * <a href="https://fits.gsfc.nasa.gov/fits_standard.html">FITS Standard</a> documentation before using this library, as
 * the library typically requires a level of familiarity with FITS and its capabilities. When constructing FITS files,
 * users will typically want to populate their headers with as much of the standard information as possible to provide a
 * full and accurate description of the data they wish to represent way beyond the bare essentials that are handled
 * automatically by this library.
 * </p>
 * <p>
 * <code>Fits</code> objects can be built-up HDU-by-HDU, and then written to a file (or stream), e.g.:
 * </p>
 * 
 * <pre>
 *   // Create a new empty Fits containe
 *   Fits fits = new Fits(); 
 *   
 *   // Create an image HDU, e.g. from a 2D array we have prepared earlier
 *   float[][] image = ...
 *   BasicHDU&lt;?&gt; imageHDU = Fits.makeHDU(image);
 *   
 *   // ... we can of course add data to the HDU's header as we like...
 *   
 *   // Make this image the first HDU...
 *   fits.addHDU(imageHDU); 
 *   
 *   // Write the FITS to a file...
 *   fits.write("myimage.fits");
 * </pre>
 * <p>
 * Or, we may read a <code>Fits</code> object from the input, e.g. as:
 * </p>
 * 
 * <pre>
 *   // Create and empty Fits assigned to an input file
 *   Fits f = new Fits(new File("myimage.fits");
 *   
 *   // Read the entire FITS (skipping over the data for now...)
 *   f.read();
 *   
 *   // Get the image data from the first HDU (will actually read the image now)
 *   float[][] image = (float[][]) f.getHDU(0).getKernel();
 * </pre>
 * <p>
 * When reading FITS from random-accessible files (like in the example above), the {@link #read()} call will parse the
 * header for each HDU but will defer reading of actual data to a later time when it's actually accessed. This makes
 * <code>Fits</code> objects fast, frugal, and lean, especially when one is interested in certain parts of the data
 * contained in the FITS file. (When reading from streams, deferred reading is not an option, so {@link #read()} will
 * load all HDUs into memory each time).
 * </p>
 * <p>
 * <code>Fits</code> objects also allow reading HDUs sequentially one at a time using the {@link #readHDU()}, or even
 * when using {@link #getHDU(int)} or {@link #getHDU(String)} methods, even if {@link #read()} was not called
 * previously, e.g.:
 * </p>
 * 
 * <pre>
 *   // Create and empty Fits assigned to an input
 *   Fits f = new Fits(new File("myimage.fits");
 *   
 *   // Get HDU index 2 (0-based, i.e. 3rd HDU) FITS. It will read (stream) or skim (file) the FITS up to the 3rd
 *   // HDU, returning it. If the FITS file or stream contains further HDUs they will not be accessed until we
 *   // need them later (if at all).
 *   BasucHDU&lt;?&gt; hdu = f.getHDU(2);
 * </pre>
 * <p>
 * When building <code>Fits</code> from local Java data objects, it's best to use {@link #makeHDU(Object)} to create
 * HDUs, which will chose the most appropriate type of HDU for the given data object (taking into some of the static
 * preferences set in <code>FitsFactory</code> prior). {@link #makeHDU(Object)} will return one of the following HDU
 * objects:
 * <ul>
 * <li>{@link NullDataHDU}</li>
 * <li>{@link ImageHDU}</li>
 * <li>{@link BinaryTableHDU}</li>
 * <li>{@link AsciiTableHDU}</li>
 * <li>{@link UndefinedHDU}</li>
 * </ul>
 * <p>
 * all of which derive from {@link BasicHDU}.
 * </p>
 * <p>
 * Since HDU literally means 'header-data unit', they constitute of a header and data entities, which can be accessed
 * separately. The {@link Header} class provides many functions to add, delete and read header keywords in HDUs in a
 * variety of formats. The {@link Data} class, and its concrete subclassses provide access to the specific data object
 * that the HDU encapsulates.
 * </p>
 * 
 * @see     FitsFactory
 *
 * @version 1.21
 */
@SuppressWarnings("deprecation")
public class Fits implements Closeable {

    /**
     * logger to log to.
     */
    private static final Logger LOG = Logger.getLogger(Fits.class.getName());

    /**
     * The input stream associated with this Fits object.
     */
    private ArrayDataInput dataStr;

    /**
     * A vector of HDUs that have been added to this Fits object.
     */
    private final List<BasicHDU<?>> hduList = new ArrayList<>();

    /**
     * Has the input stream reached the EOF?
     */
    private boolean atEOF;

    /**
     * The last offset we reached. A -1 is used to indicate that we cannot use the offset.
     */
    private long lastFileOffset = -1;

    /**
     * Creates an empty Fits object which is not associated with an input stream.
     */
    public Fits() {
    }

    /**
     * <p>
     * Creates a new (empty) FITS container associated with a file input. If the file is compressed a stream will be
     * used, otherwise random access will be supported.
     * </p>
     * <p>
     * While the FITS object is associated with the specified file, it is initialized as an empty container with no data
     * loaded from the input automatically. You may want to call {@link #read()} to load all data from the input and/or
     * {@link #readHDU()}/{@link #getHDU(int)} for select HDUs, which you can then add via {@link #addHDU(BasicHDU)} to
     * the container.
     * </p>
     *
     * @param  myFile        The File object. The content of this file will not be read into the Fits object until the
     *                           user makes some explicit request. * @throws FitsException if the operation failed
     *
     * @throws FitsException if the operation failed
     *
     * @see                  #Fits(FitsFile)
     * @see                  #Fits(RandomAccessFileIO)
     * @see                  #Fits(String)
     * @see                  #read()
     * @see                  #getHDU(int)
     * @see                  #readHDU()
     * @see                  #skipHDU()
     * @see                  #addHDU(BasicHDU)
     */
    public Fits(File myFile) throws FitsException {
        this(myFile, CompressionManager.isCompressed(myFile));
    }

    /**
     * <p>
     * Creates a new (empty) FITS container associated with a file input.
     * </p>
     * <p>
     * While the FITS object is associated with the specified file, it is initialized as an empty container with no data
     * loaded from the input automatically. You may want to call {@link #read()} to load all data from the input and/or
     * {@link #readHDU()}/{@link #getHDU(int)} for select HDUs, which you can then add via {@link #addHDU(BasicHDU)} to
     * the container.
     * </p>
     * 
     * @deprecated               Use {@link #Fits(File)} instead (compression is auto detected). Will remove in the
     *                               future.
     *
     * @param      myFile        The File object. The content of this file will not be read into the Fits object until
     *                               the user makes some explicit request.
     * @param      compressed    Is the data compressed?
     *
     * @throws     FitsException if the operation failed
     *
     * @see                      #Fits(File)
     */
    public Fits(File myFile, boolean compressed) throws FitsException {
        fileInit(myFile, compressed);
    }

    /**
     * <p>
     * Creates a new (empty) FITS container associated with an input that supports generalized random access.
     * </p>
     * <p>
     * While the FITS object is associated with the specified input, it is initialized as an empty container with no
     * data loaded from the input automatically. You may want to call {@link #read()} to load all data from the input
     * and/or {@link #readHDU()}/{@link #getHDU(int)} for select HDUs, which you can then add via
     * {@link #addHDU(BasicHDU)} to the container.
     * </p>
     *
     * @param  src           the random access input. The content of this input will not be read into the Fits object
     *                           until the user makes some explicit request.
     *
     * @throws FitsException if the operation failed
     *
     * @see                  #Fits(File)
     * @see                  #Fits(FitsFile)
     * @see                  #read()
     * @see                  #getHDU(int)
     * @see                  #readHDU()
     * @see                  #skipHDU()
     * @see                  #addHDU(BasicHDU)
     */
    public Fits(RandomAccessFileIO src) throws FitsException {
        randomInit(src);
    }

    /**
     * <p>
     * Creates a new (empty) FITS container associated with {@link FitsFile} input.
     * </p>
     * <p>
     * While the FITS object is associated with the specified file input, it is initialized as an empty container with
     * no data loaded from the input automatically. You may want to call {@link #read()} to load all data from the input
     * and/or {@link #readHDU()}/{@link #getHDU(int)} for select HDUs, which you can then add via
     * {@link #addHDU(BasicHDU)} to the container.
     * </p>
     *
     * @param  src           the random access input. The content of this input will not be read into the Fits object
     *                           until the user makes some explicit request.
     *
     * @throws FitsException if the input could not bew repositions to its beginning
     *
     * @see                  #Fits(File)
     * @see                  #Fits(RandomAccessFileIO)
     * @see                  #read()
     * @see                  #getHDU(int)
     * @see                  #readHDU()
     * @see                  #skipHDU()
     * @see                  #addHDU(BasicHDU)
     *
     * @since                1.18
     */
    public Fits(FitsFile src) throws FitsException {
        dataStr = src;
        try {
            src.seek(0);
        } catch (Exception e) {
            throw new FitsException("Could not create Fits: " + e.getMessage(), e);
        }
    }

    /**
     * <p>
     * Creates a new (empty) FITS container associated with the given input stream. Compression is determined from the
     * first few bytes of the stream.
     * </p>
     * <p>
     * While the FITS object is associated with the specified input stream, it is initialized as an empty container with
     * no data loaded from the input automatically. You may want to call {@link #read()} to load all data from the input
     * and/or {@link #readHDU()}/{@link #getHDU(int)} for select HDUs, which you can then add via
     * {@link #addHDU(BasicHDU)} to the container.
     * </p>
     *
     * @param  str           The data stream. The content of this stream will not be read into the Fits object until the
     *                           user makes some explicit request.
     *
     * @throws FitsException if the operation failed
     *
     * @see                  #Fits(File)
     * @see                  #Fits(FitsFile)
     * @see                  #read()
     * @see                  #getHDU(int)
     * @see                  #readHDU()
     * @see                  #skipHDU()
     * @see                  #addHDU(BasicHDU)
     */
    public Fits(InputStream str) throws FitsException {
        streamInit(str);
    }

    /**
     * <p>
     * Creates a new (empty) FITS container associated with an input stream.
     * </p>
     * <p>
     * While the FITS object is associated with the specified input stream, it is initialized as an empty container with
     * no data loaded from the input automatically. You may want to call {@link #read()} to load all data from the input
     * and/or {@link #readHDU()}/{@link #getHDU(int)} for select HDUs, which you can then add via
     * {@link #addHDU(BasicHDU)} to the container.
     * </p>
     *
     * @param      str           The data stream. The content of this stream will not be read into the Fits object until
     *                               the user makes some explicit request.
     * @param      compressed    Is the stream compressed? This is currently ignored. Compression is determined from the
     *                               first two bytes in the stream.
     *
     * @throws     FitsException if the operation failed
     *
     * @deprecated               Use {@link #Fits(InputStream)} instead (compression is auto detected). Will remove in
     *                               the future.
     *
     * @see                      #Fits(InputStream)
     */
    @Deprecated
    public Fits(InputStream str, boolean compressed) throws FitsException {
        this(str);
        LOG.log(Level.INFO, "compression ignored, will be autodetected. was set to " + compressed);
    }

    /**
     * <p>
     * Creates a new (empty) FITS container with a file or URL as its input. The string is assumed to be a URL if it
     * begins one of the protocol strings. If the string ends in .gz it is assumed that the data is in a compressed
     * format. All string comparisons are case insensitive.
     * </p>
     * <p>
     * While the FITS object is associated with the specified file, it is initialized as an empty container with no data
     * loaded from the input automatically. You may want to call {@link #read()} to load all data from the input and/or
     * {@link #readHDU()}/{@link #getHDU(int)} for select HDUs, which you can then add via {@link #addHDU(BasicHDU)} to
     * the container.
     * </p>
     *
     * @param  filename      The name of the file or URL to be processed. The content of this file will not be read into
     *                           the Fits object until the user makes some explicit request.
     *
     * @throws FitsException Thrown if unable to find or open a file or URL from the string given.
     *
     * @see                  #Fits(URL)
     * @see                  #Fits(FitsFile)
     * @see                  #Fits(File)
     * @see                  #read()
     * @see                  #getHDU(int)
     * @see                  #readHDU()
     * @see                  #skipHDU()
     * @see                  #addHDU(BasicHDU)
     **/
    public Fits(String filename) throws FitsException {
        this(filename, CompressionManager.isCompressed(filename));
    }

    /**
     * <p>
     * Creates a new (empty) FITS container associated with a file or URL as its input. The string is assumed to be a
     * URL if it begins one of the protocol strings. If the string ends in .gz it is assumed that the data is in a
     * compressed format. All string comparisons are case insensitive.
     * </p>
     * <p>
     * While the FITS object is associated with the specified file, it is initialized as an empty container with no data
     * loaded from the input automatically. You may want to call {@link #read()} to load all data from the input and/or
     * {@link #readHDU()}/{@link #getHDU(int)} for select HDUs, which you can then add via {@link #addHDU(BasicHDU)} to
     * the container.
     * </p>
     *
     * @param      filename      The name of the file or URL to be processed. The content of this file will not be read
     *                               into the Fits object until the user makes some explicit request.
     * @param      compressed    is the file compressed?
     *
     * @throws     FitsException Thrown if unable to find or open a file or URL from the string given.
     *
     * @deprecated               Use {@link #Fits(String)} instead (compression is auto detected). Will be a private
     *                               method in the future.
     *
     * @see                      #Fits(String)
     **/
    @SuppressWarnings("resource")
    public Fits(String filename, boolean compressed) throws FitsException {
        if (filename == null) {
            throw new FitsException("Null FITS Identifier String");
        }
        try {
            File fil = new File(filename);
            if (fil.exists()) {
                fileInit(fil, compressed);
                return;
            }
        } catch (Exception e) {
            LOG.log(Level.FINE, "not a file " + filename, e);
            throw new FitsException("could not detect type of " + filename, e);
        }
        try {
            InputStream str = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
            if (str != null) {
                streamInit(str);
                return;
            }
        } catch (Exception e) {
            LOG.log(Level.FINE, "not a resource " + filename, e);
            throw new FitsException("could not detect type of " + filename, e);
        }
        try {
            InputStream is = FitsUtil.getURLStream(new URL(filename), 0);
            streamInit(is);
            return;
        } catch (Exception e) {
            LOG.log(Level.FINE, "not a url " + filename, e);
            throw new FitsException("could not detect type of " + filename, e);
        }

    }

    /**
     * <p>
     * Creates a new (empty) FITS container with a given URL as its input.
     * </p>
     * <p>
     * While the FITS object is associated with the resource, it is initialized as an empty container with no data
     * loaded from the input automatically. You may want to call {@link #read()} to load all data from the input and/or
     * {@link #readHDU()}/{@link #getHDU(int)} for select HDUs, which you can then add via {@link #addHDU(BasicHDU)} to
     * the container.
     * </p>
     *
     * @param  myURL         The URL to be read. The content of this URL will not be read into the Fits object until the
     *                           user makes some explicit request.
     *
     * @throws FitsException Thrown if unable to find or open a file or URL from the string given.
     *
     * @see                  #Fits(String)
     * @see                  #Fits(RandomAccessFileIO)
     * @see                  #read()
     * @see                  #getHDU(int)
     * @see                  #readHDU()
     * @see                  #skipHDU()
     * @see                  #addHDU(BasicHDU)
     */
    @SuppressWarnings("resource")
    public Fits(URL myURL) throws FitsException {
        try {
            streamInit(FitsUtil.getURLStream(myURL, 0));
        } catch (IOException e) {
            throw new FitsException("Unable to open input from URL:" + myURL, e);
        }
    }

    /**
     * <p>
     * Creates a new (empty) FITS container associated with a given uncompressed URL as its input.
     * </p>
     * <p>
     * While the FITS object is associated with the resource, it is initialized as an empty container with no data
     * loaded from the input automatically. You may want to call {@link #read()} to load all data from the input and/or
     * {@link #readHDU()}/{@link #getHDU(int)} for select HDUs, which you can then add via {@link #addHDU(BasicHDU)} to
     * the container.
     * </p>
     *
     * @param      myURL         The URL to be associated with the FITS file. The content of this URL will not be read
     *                               into the Fits object until the user makes some explicit request.
     * @param      compressed    Compression flag, ignored.
     *
     * @throws     FitsException Thrown if unable to use the specified URL.
     *
     * @deprecated               Use {@link #Fits(URL)} instead (compression is auto detected). Will remove in the
     *                               future.
     *
     * @see                      #Fits(URL)
     */
    @Deprecated
    public Fits(URL myURL, boolean compressed) throws FitsException {
        this(myURL);
        LOG.log(Level.INFO, "compression ignored, will be autodetected. was set to " + compressed);
    }

    /**
     * Creates a new empty HDU for the given data type.
     * 
     * @return               a newly created HDU from the given Data.
     *
     * @param  data          The data to be described in this HDU.
     * @param  <DataClass>   the class of the HDU
     *
     * @throws FitsException if the operation failed
     */
    public static <DataClass extends Data> BasicHDU<DataClass> makeHDU(DataClass data) throws FitsException {
        Header hdr = new Header();
        data.fillHeader(hdr);
        return FitsFactory.hduFactory(hdr, data);
    }

    /**
     * Creates a new empty HDU based on the header description of the data
     * 
     * @return               a newly created HDU from the given header (and including the header).
     *
     * @param  h             The header which describes the FITS extension
     *
     * @throws FitsException if the header could not be converted to a HDU.
     */
    public static BasicHDU<?> makeHDU(Header h) throws FitsException {
        Data d = FitsFactory.dataFactory(h);
        return FitsFactory.hduFactory(h, d);
    }

    /**
     * <p>
     * Creates an HDU that wraps around the specified data object. The HDUs header will be created and populated with
     * the essential description of the data. The following HDU types may be returned depending on the nature of the
     * argument:
     * </p>
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
     * <p>
     * As of 1.18, this metohd will not create and return random group HDUs for <code>Object[][2]</code> style data.
     * Instead, it will return an appropriate binary or ASCII table, since the FITS standard recommends against using
     * random groups going forward, except for reading some old data from certain radio telescopes. If the need ever
     * arises to create new random groups HDUs with this library, you may use
     * {@link RandomGroupsHDU#createFrom(Object[][])} instead.
     * </p>
     * 
     * @return               a newly created HDU from the given data kernel.
     *
     * @param  o             The data to be described in this HDU.
     *
     * @throws FitsException if the parameter could not be converted to a HDU.
     * 
     * @see                  RandomGroupsHDU#createFrom(Object[][])
     */
    public static BasicHDU<?> makeHDU(Object o) throws FitsException {
        return FitsFactory.hduFactory(o);
    }

    /**
     * Returns the version sting of this FITS library
     * 
     * @return the version of the library.
     */
    public static String version() {
        Properties props = new Properties();
        try (InputStream versionProperties = Fits.class
                .getResourceAsStream("/META-INF/maven/gov.nasa.gsfc.heasarc/nom-tam-fits/pom.properties")) {
            props.load(versionProperties);
            return props.getProperty("version");
        } catch (IOException e) {
            LOG.log(Level.INFO, "reading version failed, ignoring", e);
            return "unknown";
        }
    }

    /**
     * close the input stream, and ignore eventual errors.
     * 
     * @deprecated    Use <b>try-with-resources</b> constructs in Java 8+ instead.
     *
     * @param      in the input stream to close.
     */
    public static void saveClose(InputStream in) {
        SafeClose.close(in);
    }

    /**
     * Add an HDU to the Fits object. Users may intermix calls to functions which read HDUs from an associated input
     * stream with the addHDU and insertHDU calls, but should be careful to understand the consequences.
     *
     * @param  myHDU         The HDU to be added to the end of the FITS object.
     *
     * @throws FitsException if the HDU could not be inserted.
     *
     * @see                  #readHDU()
     */
    public void addHDU(BasicHDU<?> myHDU) throws FitsException {
        insertHDU(myHDU, getNumberOfHDUs());
    }

    /**
     * Get the current number of HDUs in the Fits object.
     *
     * @return     The number of HDU's in the object.
     *
     * @deprecated use {@link #getNumberOfHDUs()} instead
     */
    @Deprecated
    public int currentSize() {
        return getNumberOfHDUs();
    }

    /**
     * Delete an HDU from the HDU list.
     *
     * @param  n             The index of the HDU to be deleted. If n is 0 and there is more than one HDU present, then
     *                           the next HDU will be converted from an image to primary HDU if possible. If not a dummy
     *                           header HDU will then be inserted.
     *
     * @throws FitsException if the HDU could not be deleted.
     */
    public void deleteHDU(int n) throws FitsException {
        int size = getNumberOfHDUs();
        if (n < 0 || n >= size) {
            throw new FitsException("Attempt to delete non-existent HDU:" + n);
        }
        hduList.remove(n);
        if (n == 0 && size > 1) {
            BasicHDU<?> newFirst = hduList.get(0);
            if (newFirst.canBePrimary()) {
                newFirst.setPrimaryHDU(true);
            } else {
                insertHDU(BasicHDU.getDummyHDU(), 0);
            }
        }
    }

    /**
     * @deprecated               Will be private in 2.0. Get a stream from the file and then use the stream
     *                               initialization.
     *
     * @param      myFile        The File to be associated.
     * @param      compressed    Is the data compressed?
     *
     * @throws     FitsException if the opening of the file failed.
     */
    // TODO Make private
    @Deprecated
    @SuppressWarnings("resource")
    protected void fileInit(File myFile, boolean compressed) throws FitsException {
        try {
            if (compressed) {
                streamInit(Files.newInputStream(myFile.toPath()));
            } else {
                randomInit(myFile);
            }
        } catch (IOException e) {
            throw new FitsException("Unable to create Input Stream from File: " + myFile, e);
        }
    }

    /**
     * Returns the n'th HDU. If the HDU is already read simply return a pointer to the cached data. Otherwise read the
     * associated stream until the n'th HDU is read.
     *
     * @param  n                         The index of the HDU to be read. The primary HDU is index 0.
     *
     * @return                           The n'th HDU or null if it could not be found.
     *
     * @throws FitsException             if the header could not be read
     * @throws IOException               if the underlying buffer threw an error
     * @throws IndexOutOfBoundsException if the Fits contains no HDU by the given index.
     *
     * @see                              #getHDU(String)
     * @see                              #getHDU(String, int)
     */
    public BasicHDU<?> getHDU(int n) throws FitsException, IOException, IndexOutOfBoundsException {
        for (int i = getNumberOfHDUs(); i <= n; i++) {
            BasicHDU<?> hdu = readHDU();
            if (hdu == null) {
                return null;
            }
        }
        return hduList.get(n);
    }

    /**
     * Returns the primary header of this FITS file, that is the header of the primary HDU in this Fits object. This
     * method differs from <code>getHDU(0).getHeader()</code>, int that the primary header this way will be properly
     * configured as the primary HDU with all mandatory keywords, even if the HDU's header did not contain these entries
     * originally. (Subsequent calls to <code>getHDU(0).getHeader()</code> will also contain the populated mandatory
     * keywords).
     * 
     * @return               The primary header of this FITS file/object.
     * 
     * @throws FitsException If the Fits is empty (does not contain a primary HDU)
     * @throws IOException   if there was a problem accessing the FITS from the input
     * 
     * @see                  #getCompleteHeader(int)
     * @see                  BasicHDU#getHeader()
     * 
     * @since                1.19
     */
    public Header getPrimaryHeader() throws FitsException, IOException {
        if (hduList.isEmpty()) {
            throw new FitsException("Empty Fits object");
        }
        BasicHDU<?> primary = getHDU(0);
        primary.setPrimaryHDU(true);
        return primary.getHeader();
    }

    /**
     * Returns the 'complete' header of the n<sup>th</sup> HDU in this FITS file/object. This differs from
     * {@link #getHDU(int)}<code>.getHeader()</code> in two important ways:
     * <ul>
     * <li>The header will be populated with the mandatory FITS keywords based on whether it is that of a primary or
     * extension HDU in this Fits, and the type of HDU it is. (Subsequent calls to <code>getHDU(n).getHeader()</code>
     * will also include the populated mandatory keywords.)</li>
     * <li>If the header contains the {@link Standard#INHERIT} keyword, a new header object is returned, which merges
     * the non-conflicting primary header keys on top of the keywords explicitly defined in the HDU already.
     * </ul>
     * 
     * @param  n                         The zero-based index of the HDU.
     * 
     * @return                           The completed header of the HDU. If the HDU contains the INHERIT key this
     *                                       header will be a new header object constructed by this call to include also
     *                                       all non-conflicting primary header keywords. Otherwise it will simply
     *                                       return the HDUs header (after adding the mandatory keywords).
     * 
     * @throws FitsException             If the FITS is empty
     * @throws IOException               If the HDU is not accessible from its source
     * @throws IndexOutOfBoundsException If the FITS does not contain a HDU by the specified index
     * 
     * @see                              #getCompleteHeader(String)
     * @see                              #getCompleteHeader(String, int)
     * @see                              #getPrimaryHeader()
     * @see                              #getHDU(int)
     * 
     * @since                            1.19
     */
    public Header getCompleteHeader(int n) throws FitsException, IOException, IndexOutOfBoundsException {
        BasicHDU<?> hdu = getHDU(n);
        if (hdu == null) {
            throw new IndexOutOfBoundsException("FITS has no HDU index " + n);
        }
        return getCompleteHeader(hdu);
    }

    /**
     * Returns the complete header of the first HDU by the specified name in this FITS file/object. This differs from
     * {@link #getHDU(String)}<code>.getHeader()</code> in two important ways:
     * <ul>
     * <li>The header will be populated with the mandatory FITS keywords based on whether it is that of a primary or
     * extension HDU in this Fits, and the type of HDU it is. (Subsequent calls to <code>getHDU(n).getHeader()</code>
     * will also include the populated mandatory keywords.)</li>
     * <li>If the header contains the {@link Standard#INHERIT} keyword, a new header object is returned, which merges
     * the non-conflicting primary header keys on top of the keywords explicitly defined in the HDU already.
     * </ul>
     * 
     * @param  name                   The HDU name
     * 
     * @return                        The completed header of the HDU. If the HDU contains the INHERIT key this header
     *                                    will be a new header object constructed by this call to include also all
     *                                    non-conflicting primary header keywords. Otherwise it will simply return the
     *                                    HDUs header (after adding the mandatory keywords).
     * 
     * @throws FitsException          If the FITS is empty
     * @throws IOException            If the HDU is not accessible from its source
     * @throws NoSuchElementException If the FITS does not contain a HDU by the specified name
     * 
     * @see                           #getCompleteHeader(String, int)
     * @see                           #getCompleteHeader(int)
     * @see                           #getPrimaryHeader()
     * @see                           #getHDU(int)
     * 
     * @since                         1.19
     */
    public Header getCompleteHeader(String name) throws FitsException, IOException, NoSuchElementException {
        BasicHDU<?> hdu = getHDU(name);
        if (hdu == null) {
            throw new NoSuchElementException("Fits contains no HDU named " + name);
        }
        return getCompleteHeader(hdu);
    }

    /**
     * Returns the complete header of the first HDU by the specified name and version in this FITS file/object. This
     * differs from {@link #getHDU(String)}<code>.getHeader()</code> in two important ways:
     * <ul>
     * <li>The header will be populated with the mandatory FITS keywords based on whether it is that of a primary or
     * extension HDU in this Fits, and the type of HDU it is. (Subsequent calls to <code>getHDU(n).getHeader()</code>
     * will also include the populated mandatory keywords.)</li>
     * <li>If the header contains the {@link Standard#INHERIT} keyword, a new header object is returned, which merges
     * the non-conflicting primary header keys on top of the keywords explicitly defined in the HDU already.
     * </ul>
     * 
     * @param  name                   The HDU name
     * @param  version                The HDU version
     * 
     * @return                        The completed header of the HDU. If the HDU contains the INHERIT key this header
     *                                    will be a new header object constructed by this call to include also all
     *                                    non-conflicting primary header keywords. Otherwise it will simply return the
     *                                    HDUs header (after adding the mandatory keywords).
     * 
     * @throws FitsException          If the FITS is empty
     * @throws IOException            If the HDU is not accessible from its source
     * @throws NoSuchElementException If the FITS does not contain a HDU by the specified name and version
     * 
     * @see                           #getCompleteHeader(String)
     * @see                           #getCompleteHeader(int)
     * @see                           #getPrimaryHeader()
     * @see                           #getHDU(int)
     * 
     * @since                         1.19
     */
    public Header getCompleteHeader(String name, int version) throws FitsException, IOException, NoSuchElementException {
        BasicHDU<?> hdu = getHDU(name, version);
        if (hdu == null) {
            throw new NoSuchElementException("Fits contains no HDU named " + name);
        }
        return getCompleteHeader(hdu);
    }

    private Header getCompleteHeader(BasicHDU<?> hdu) throws FitsException, IOException {
        if (hdu == getHDU(0)) {
            return getPrimaryHeader();
        }
        hdu.setPrimaryHDU(false);
        Header h = hdu.getHeader();
        if (h.getBooleanValue(Standard.INHERIT)) {
            Header merged = new Header();
            merged.mergeDistinct(h);
            merged.mergeDistinct(getPrimaryHeader());
            return merged;
        }
        return h;
    }

    /**
     * Checks if the value of the EXTNAME keyword of the specified HDU matches the specified name.
     *
     * @param  hdu  The HDU whose EXTNAME to check
     * @param  name The expected name
     *
     * @return      <code>true</code> if the HDU has an EXTNAME keyword whose value matches the specified name (case
     *                  sensitive!), otherwise <code>false</code>
     *
     * @see         #getHDU(String)
     */
    private boolean isNameMatch(BasicHDU<?> hdu, String name) {
        Header h = hdu.getHeader();
        if (!h.containsKey(EXTNAME)) {
            return false;
        }
        return name.equals(hdu.getHeader().getStringValue(EXTNAME));
    }

    /**
     * Checks if the value of the EXTNAME and EXTVER keywords of the specified HDU match the specified name and version.
     *
     * @param  hdu     The HDU whose EXTNAME to check
     * @param  name    The expected name
     * @param  version The expected extension version
     *
     * @return         <code>true</code> if the HDU has an EXTNAME keyword whose value matches the specified name (case
     *                     sensitive!) AND has an EXTVER keyword whose value matches the specified integer version. In
     *                     all other cases <code>false</code> is returned.
     *
     * @see            #getHDU(String, int)
     */
    private boolean isNameVersionMatch(BasicHDU<?> hdu, String name, int version) {
        Header h = hdu.getHeader();
        if (!h.containsKey(EXTNAME) || !name.equals(h.getStringValue(EXTNAME)) || !h.containsKey(EXTVER)) {
            return false;
        }
        return h.getIntValue(EXTVER) == version;
    }

    /**
     * Returns the HDU by the given extension name (defined by <code>EXTNAME</code> header keyword). This method checks
     * only for EXTNAME but will ignore the version (defined by <code>EXTVER</code>). If multiple HDUs have the same
     * matching <code>EXTNAME</code>, this method will return the first match only.
     *
     * @param  name          The name of the HDU as defined by <code>EXTNAME</code> (case sensitive)
     *
     * @return               The first HDU that matches the specified extension name and version, or <code>null</code>
     *                           if the FITS does not contain a matching HDU.
     *
     * @throws FitsException if the header could not be read
     * @throws IOException   if the underlying buffer threw an error
     *
     * @since                1.17.0
     *
     * @see                  #getHDU(String, int)
     * @see                  #getHDU(int)
     */
    public BasicHDU<?> getHDU(String name) throws FitsException, IOException {
        // Check HDUs we already read...
        for (BasicHDU<?> hdu : hduList) {
            if (isNameMatch(hdu, name)) {
                return hdu;
            }
        }

        // Read additional HDUs as necessary...
        BasicHDU<?> hdu;
        while ((hdu = readHDU()) != null) {
            if (isNameMatch(hdu, name)) {
                return hdu;
            }
        }

        return null;
    }

    /**
     * Returns the HDU by the given extension name and version (defined by <code>EXTNAME</code> and <code>EXTVER</code>
     * keywords). If multiple HDUs have the same matching name and version, this method will return the first match
     * only.
     *
     * @param  name          The name of the HDU as defined by <code>EXTNAME</code> (case sensitive)
     * @param  version       The extension version as defined by <code>EXTVER</code> in the matching HDU.
     *
     * @return               The first HDU that matches the specified extension name and version, or <code>null</code>
     *                           if the FITS does not contain a matching HDU.
     *
     * @throws FitsException if the header could not be read
     * @throws IOException   if the underlying buffer threw an error
     *
     * @since                1.17.0
     *
     * @see                  #getHDU(String)
     * @see                  #getHDU(int)
     */
    public BasicHDU<?> getHDU(String name, int version) throws FitsException, IOException {
        // Check HDUs we already read...
        for (BasicHDU<?> hdu : hduList) {
            if (isNameVersionMatch(hdu, name, version)) {
                return hdu;
            }
        }

        // Read additional HDUs as necessary...
        BasicHDU<?> hdu;
        while ((hdu = readHDU()) != null) {
            if (isNameVersionMatch(hdu, name, version)) {
                return hdu;
            }
        }

        return null;
    }

    /**
     * Get the number of HDUs currently available in memory. For FITS objects associated with an input this method
     * returns only the number of HDUs that have already been read / scanned, e.g. via {@link #readHDU()} or
     * {@link #read()} methods. Thus, if you want to know how many HDUs a FITS file might actually contain, you should
     * call {@link #read()} to register them all before calling this method.
     *
     * @return The number of HDU's in the object.
     * 
     * @see    #read()
     * @see    #readHDU()
     */
    public int getNumberOfHDUs() {
        return hduList.size();
    }

    /**
     * Returns the input from which this <code>Fits</code> is associated to (if any)..
     *
     * @return The associated data input, or <code>null</code> if this <code>Fits</code> container was not read from an
     *             input. Users may wish to call this function after opening a Fits object when they want low-level
     *             rea/wrte access to the FITS resource directly.
     */
    public ArrayDataInput getStream() {
        return dataStr;
    }

    /**
     * Insert a FITS object into the list of HDUs.
     *
     * @param  myHDU         The HDU to be inserted into the list of HDUs.
     * @param  position      The location at which the HDU is to be inserted.
     *
     * @throws FitsException if the HDU could not be inserted.
     */
    public void insertHDU(BasicHDU<?> myHDU, int position) throws FitsException {
        if (myHDU == null) {
            return;
        }
        if (position < 0 || position > getNumberOfHDUs()) {
            throw new FitsException("Attempt to insert HDU at invalid location: " + position);
        }
        if (myHDU instanceof RandomGroupsHDU && position != 0) {
            throw new FitsException("Random groups HDUs must be the first (primary) HDU. Requested pos: " + position);
        }

        try {
            if (position == 0) {
                // Note that the previous initial HDU is no longer the first.
                // If we were to insert tables backwards from last to first,
                // we could get a lot of extraneous DummyHDUs but we currently
                // do not worry about that.
                if (getNumberOfHDUs() > 0) {
                    hduList.get(0).setPrimaryHDU(false);
                }
                if (myHDU.canBePrimary()) {
                    myHDU.setPrimaryHDU(true);
                    hduList.add(0, myHDU);
                } else {
                    insertHDU(BasicHDU.getDummyHDU(), 0);
                    myHDU.setPrimaryHDU(false);
                    hduList.add(1, myHDU);
                }
            } else {
                myHDU.setPrimaryHDU(false);
                hduList.add(position, myHDU);
            }
        } catch (NoSuchElementException e) {
            throw new FitsException("hduList inconsistency in insertHDU", e);
        }
    }

    /**
     * Initialize using buffered random access. This implies that the data is uncompressed.
     *
     * @param  file          the file to open
     *
     * @throws FitsException if the file could not be read
     *
     * @see                  #randomInit(RandomAccessFileIO)
     */
    // TODO make private
    @Deprecated
    protected void randomInit(File file) throws FitsException {

        if (!file.exists() || !file.canRead()) {
            throw new FitsException("Non-existent or unreadable file");
        }
        try {
            // Attempt to open the file for reading and writing.
            dataStr = new FitsFile(file, "rw");
            ((FitsFile) dataStr).seek(0);
        } catch (IOException e) {
            try {
                // If that fails, try read-only.
                dataStr = new FitsFile(file, "r");
                ((FitsFile) dataStr).seek(0);
            } catch (IOException e2) {
                throw new FitsException("Unable to open file " + file.getPath(), e2);
            }
        }
    }

    /**
     * Initialize using buffered random access. This implies that the data is uncompressed.
     *
     * @param  src           the random access data
     *
     * @throws FitsException ` if the data is not readable
     *
     * @see                  #randomInit(File)
     */
    protected void randomInit(RandomAccessFileIO src) throws FitsException {
        try {
            dataStr = new FitsFile(src, FitsIO.DEFAULT_BUFFER_SIZE);
            ((FitsFile) dataStr).seek(0);
        } catch (IOException e) {
            throw new FitsException("Unable to open data " + src, e);
        }
    }

    /**
     * Return all HDUs for the Fits object. If the FITS file is associated with an external stream make sure that we
     * have exhausted the stream.
     *
     * @return               an array of all HDUs in the Fits object. Returns null if there are no HDUs associated with
     *                           this object.
     *
     * @throws FitsException if the reading failed.
     */
    public BasicHDU<?>[] read() throws FitsException {
        readToEnd();
        int size = getNumberOfHDUs();
        if (size == 0) {
            return new BasicHDU<?>[0];
        }
        return hduList.toArray(new BasicHDU<?>[size]);
    }

    /**
     * Read a FITS file from an InputStream object.
     *
     * @param      is            The InputStream stream whence the FITS information is found.
     *
     * @throws     FitsException if the data read could not be interpreted
     * 
     * @deprecated               Use {@link #Fits(InputStream)} constructor instead. We will remove this method in the
     *                               future.
     */
    public void read(InputStream is) throws FitsException {
        is = CompressionManager.decompress(is);

        if (is instanceof ArrayDataInput) {
            dataStr = (ArrayDataInput) is;
        } else {
            dataStr = new FitsInputStream(is);
        }
        read();
    }

    /**
     * Read the next HDU on the default input stream. This call may return any concrete subclass of {@link BasicHDU},
     * including compressed HDU types.
     *
     * @return               The HDU read, or null if an EOF was detected. Note that null is only returned when the EOF
     *                           is detected immediately at the beginning of reading the HDU.
     *
     * @throws FitsException if the header could not be read
     * @throws IOException   if the underlying buffer threw an error
     *
     * @see                  #skipHDU()
     * @see                  #getHDU(int)
     * @see                  #addHDU(BasicHDU)
     */
    public BasicHDU<?> readHDU() throws FitsException, IOException {
        if (dataStr == null || atEOF) {
            if (dataStr == null) {
                LOG.warning("trying to read a hdu, without an input source!");
            }
            return null;
        }

        if (dataStr instanceof RandomAccess && lastFileOffset > 0) {
            FitsUtil.reposition(dataStr, lastFileOffset);
        }

        Header hdr = Header.readHeader(dataStr);
        if (hdr == null) {
            atEOF = true;
            return null;
        }

        Data data = FitsFactory.dataFactory(hdr);
        try {
            data.read(dataStr);
            if (Fits.checkTruncated(dataStr)) {
                // Check for truncation even if we successfully skipped to the expected
                // end since skip may allow going beyond the EOF.
                LOG.warning("Missing padding after data segment");
            }
        } catch (PaddingException e) {
            // Stream end before required padding after data...
            LOG.warning(e.getMessage());
        }

        lastFileOffset = FitsUtil.findOffset(dataStr);
        BasicHDU<Data> hdu = FitsFactory.hduFactory(hdr, data);

        hduList.add(hdu);

        return hdu;
    }

    /**
     * Read to the end of the associated input stream
     *
     * @throws FitsException if the operation failed
     */
    private void readToEnd() throws FitsException {
        try {
            while (dataStr != null && !atEOF) {
                if (readHDU() == null) {
                    if (getNumberOfHDUs() == 0) {
                        throw new FitsException("Not FITS file.");
                    }
                    return;
                }
            }
        } catch (IOException e) {
            throw new FitsException("Corrupted FITS file: " + e, e);
        }
    }

    /**
     * <p>
     * Computes the <code>CHECKSUM</code> and <code>DATASUM</code> values for the specified HDU index and stores them in
     * the HUS's header. For deferred data the data sum is calculated directly from the file (if possible), without
     * loading the entire (potentially huge) data into RAM for the calculation.
     * </p>
     *
     * @param  hduIndex      The index of the HDU for which to compute and set the <code>CHECKSUM</code> and
     *                           <code>DATASUM</code> header values.
     *
     * @throws FitsException if there was a problem computing the checksum for the HDU
     * @throws IOException   if there was an I/O error while accessing the data from the input
     *
     * @see                  #setChecksum()
     * @see                  BasicHDU#verifyIntegrity()
     * @see                  BasicHDU#verifyDataIntegrity()
     *
     * @since                1.17
     */
    public void setChecksum(int hduIndex) throws FitsException, IOException {
        FitsCheckSum.setDatasum(getHDU(hduIndex).getHeader(), calcDatasum(hduIndex));
    }

    /**
     * <p>
     * Add or modify the CHECKSUM keyword in all headers. As of 1.17 the checksum for deferred data is calculated
     * directly from the file (if possible), without loading the entire (potentially huge) data into RAM for the
     * calculation.
     * </p>
     * <p>
     * As of 1.17, the routine calculates checksums both for HDUs that are in RAM, as well as HDUs that were not yet
     * loaded from the input (if any). Any HDUs not in RAM at the time of the call will stay in deferred mode (if the
     * HDU itself supports it). After setting (new) checksums, you may want to call #rewrite()
     * </p>
     *
     * @throws FitsException if there was an error during the checksumming operation
     * @throws IOException   if there was an I/O error while accessing the data from the input
     *
     * @author               R J Mather, Attila Kovacs
     *
     * @see                  #setChecksum(int)
     * @see                  BasicHDU#getStoredDatasum()
     * @see                  #rewrite()
     */
    public void setChecksum() throws FitsException, IOException {
        int i = 0;

        // Start with HDU's already loaded, leaving deferred data in unloaded
        // state
        for (; i < getNumberOfHDUs(); i++) {
            setChecksum(i);
        }

        // Check if Fits is read from an input of sorts, with potentially more
        // HDUs there...
        if (dataStr == null) {
            return;
        }

        // Continue with unread HDUs (if any...)
        while (readHDU() != null) {
            setChecksum(i++);
        }
    }

    /**
     * <p>
     * Calculates the data checksum for a given HDU in the Fits. If the HDU does not currently have data loaded from
     * disk (in deferred read mode), the method will calculate the checksum directly from disk. Otherwise, it will
     * calculate the datasum from the data in memory.
     * </p>
     * 
     * @param  hduIndex      The index of the HDU for which to calculate the data checksum
     *
     * @return               The data checksum. This may differ from the datasum or the original FITS input due to
     *                           differences in padding used at the end of the data record by this library vs the
     *                           library that was used to generate the FITS.
     *
     * @throws FitsException if there was an error processing the HDU.
     * @throws IOException   if there was an I/O error accessing the input.
     *
     * @see                  Data#calcChecksum()
     * @see                  BasicHDU#verifyDataIntegrity()
     * @see                  #setChecksum(int)
     * @see                  BasicHDU#getStoredDatasum()
     * @see                  FitsCheckSum#setDatasum(Header, long)
     *
     * @since                1.17
     */
    public long calcDatasum(int hduIndex) throws FitsException, IOException {
        BasicHDU<?> hdu = getHDU(hduIndex);
        Data data = hdu.getData();
        if (data.isDeferred()) {
            // Compute datasum directly from file...
            return FitsCheckSum.checksum((RandomAccess) dataStr, data.getFileOffset(), data.getSize());
        }
        return data.calcChecksum();
    }

    /**
     * Calculates the FITS checksum for a given HDU in the Fits. If the HDU does not currently have data loaded from
     * disk (i.e. in deferred read mode), the method will compute the checksum directly from disk. Otherwise, it will
     * calculate the checksum from the data in memory and using the standard padding after it.
     * 
     * @deprecated               Use {@link BasicHDU#verifyIntegrity()} instead when appropriate. It's not particularly
     *                               useful since integrity checking does not use or require knowledge of this sum. May
     *                               be removed from future releases.
     *
     * @param      hduIndex      The index of the HDU for which to calculate the HDU checksum
     *
     * @return                   The checksum value that would appear in the header if this HDU was written to an
     *                               output. This may differ from the checksum recorded in the input, due to different
     *                               formating conventions used by this library vs the one that was used to generate the
     *                               input.
     * 
     * @throws     FitsException if there was an error processing the HDU.
     * @throws     IOException   if there was an I/O error accessing the input.
     *
     * @see                      BasicHDU#calcChecksum()
     * @see                      #calcDatasum(int)
     * @see                      #setChecksum(int)
     *
     * @since                    1.17
     */
    public long calcChecksum(int hduIndex) throws FitsException, IOException {
        return FitsCheckSum.sumOf(FitsCheckSum.checksum(getHDU(hduIndex).getHeader()), calcDatasum(hduIndex));
    }

    /**
     * Checks the integrity of all HDUs. HDUs that do not specify either CHECKSUM or DATASUM keyword will be ignored.
     * 
     * @throws FitsIntegrityException if the FITS is corrupted, the message will inform about which HDU failed the
     *                                    integrity test first.
     * @throws FitsException          if the header or HDU is invalid or garbled.
     * @throws IOException            if the Fits object is not associated to a random-accessible input, or if there was
     *                                    an I/O error accessing the input.
     * 
     * @see                           BasicHDU#verifyIntegrity()
     * 
     * @since                         1.18.1
     */
    public void verifyIntegrity() throws FitsIntegrityException, FitsException, IOException {
        for (int i = 0;; i++) {
            BasicHDU<?> hdu = readHDU();
            if (hdu == null) {
                break;
            }

            try {
                hdu.verifyIntegrity();
            } catch (FitsIntegrityException e) {
                throw new FitsIntegrityException(i, e);
            }
        }
    }

    /**
     * @deprecated        This method is poorly conceived as we cannot really read FITS from just any
     *                        <code>ArrayDataInput</code> but only those, which utilize {@link nom.tam.util.FitsDecoder}
     *                        to convert Java types to FITS binary format, such as {@link FitsInputStream} or
     *                        {@link FitsFile} (or else a wrapped <code>DataInputStream</code>). As such, this method is
     *                        inherently unsafe as it can be used to parse FITS content iscorrectly. It will be removed
     *                        from the public API in a future major release. Set the data stream to be used for future
     *                        input.
     *
     * @param      stream The data stream to be used.
     */
    @Deprecated
    public void setStream(ArrayDataInput stream) {
        dataStr = stream;
        atEOF = false;
        lastFileOffset = -1;
    }

    /**
     * Return the number of HDUs in the Fits object. If the FITS file is associated with an external stream make sure
     * that we have exhausted the stream.
     *
     * @return                   number of HDUs.
     *
     * @deprecated               The meaning of size of ambiguous. Use {@link #getNumberOfHDUs()} instead. Note size()
     *                               will read the input file/stream to the EOF before returning the number of HDUs
     *                               which {@link #getNumberOfHDUs()} does not. If you wish to duplicate this behavior
     *                               and ensure that the input has been exhausted before getting the number of HDUs then
     *                               use the sequence: <code>
     *    read();
     *    getNumberOfHDUs();
     * </code>
     *
     * @throws     FitsException if the file could not be read.
     */
    @Deprecated
    public int size() throws FitsException {
        readToEnd();
        return getNumberOfHDUs();
    }

    /**
     * Skip the next HDU on the default input stream.
     *
     * @throws FitsException if the HDU could not be skipped
     * @throws IOException   if the underlying stream failed
     *
     * @see                  #skipHDU(int)
     * @see                  #readHDU()
     */
    public void skipHDU() throws FitsException, IOException {
        if (atEOF) {
            return;
        }

        Header hdr = new Header(dataStr);
        int dataSize = (int) hdr.getDataSize();
        dataStr.skipAllBytes(dataSize);
        if (dataStr instanceof RandomAccess) {
            lastFileOffset = ((RandomAccess) dataStr).getFilePointer();
        }
    }

    /**
     * Skip HDUs on the associate input stream.
     *
     * @param  n             The number of HDUs to be skipped.
     *
     * @throws FitsException if the HDU could not be skipped
     * @throws IOException   if the underlying stream failed
     *
     * @see                  #skipHDU()
     */
    public void skipHDU(int n) throws FitsException, IOException {
        for (int i = 0; i < n; i++) {
            skipHDU();
        }
    }

    /**
     * Initializes the input stream. Mostly this checks to see if the stream is compressed and wraps the stream if
     * necessary. Even if the stream is not compressed, it will likely be wrapped in a PushbackInputStream. So users
     * should probably not supply a BufferedDataInputStream themselves, but should allow the Fits class to do the
     * wrapping.
     *
     * @param  inputStream   stream to initialize
     *
     * @throws FitsException if the initialization failed
     */
    @SuppressWarnings("resource")
    protected void streamInit(InputStream inputStream) throws FitsException {
        dataStr = new FitsInputStream(CompressionManager.decompress(inputStream));
    }

    /**
     * Writes the contents to a designated FITS file. It is up to the caller to close the file as appropriate after
     * writing to it.
     *
     * @param  file          a file that support FITS encoding
     *
     * @throws FitsException if there were any errors writing the contents themselves.
     * @throws IOException   if the underlying file could not be trimmed or closed.
     *
     * @since                1.16
     *
     * @see                  #write(FitsOutputStream)
     */
    public void write(FitsFile file) throws IOException, FitsException {
        write((ArrayDataOutput) file);
        file.setLength(file.getFilePointer());
    }

    /**
     * Writes the contents to a designated FITS output stream. It is up to the caller to close the stream as appropriate
     * after writing to it.
     *
     * @param  out           an output stream that supports FITS encoding.
     *
     * @throws FitsException if there were any errors writing the contents themselves.
     * @throws IOException   if the underlying file could not be flushed or closed.
     *
     * @since                1.16
     *
     * @see                  #write(FitsFile)
     * @see                  #write(File)
     * @see                  #write(String)
     */
    public void write(FitsOutputStream out) throws IOException, FitsException {
        write((ArrayDataOutput) out);
        out.flush();
    }

    /**
     * Writes the contents to a new file.
     *
     * @param  file          a file to which the FITS is to be written.
     *
     * @throws FitsException if there were any errors writing the contents themselves.
     * @throws IOException   if the underlying output stream could not be created or closed.
     *
     * @see                  #write(FitsOutputStream)
     */
    public void write(File file) throws IOException, FitsException {
        try (FileOutputStream o = new FileOutputStream(file)) {
            write(new FitsOutputStream(o));
            o.flush();
        }
    }

    /**
     * Re-writes all HDUs that have been loaded (and possibly modified) to the disk, if possible -- or else does
     * nothing. For HDUs that are in deferred mode (data unloaded and unchanged), only the header is re-written to disk.
     * Otherwise, both header and data is re-written. Of course, rewriting is possible only if the sizes of all headers
     * and data segments remain the same as before.
     *
     * @throws FitsException If one or more of the HDUs cannot be re-written, or if there was some other error
     *                           serializing the HDUs to disk.
     * @throws IOException   If there was an I/O error accessing the output file.
     *
     * @since                1.17
     *
     * @see                  BasicHDU#rewriteable()
     */
    public void rewrite() throws FitsException, IOException {
        for (int i = 0; i < getNumberOfHDUs(); i++) {
            if (!getHDU(i).rewriteable()) {
                throw new FitsException("HDU[" + i + "] cannot be re-written in place. Aborting rewrite.");
            }
        }

        for (int i = 0; i < getNumberOfHDUs(); i++) {
            getHDU(i).rewrite();
        }
    }

    /**
     * Writes the contents to the specified file. It simply wraps {@link #write(File)} for convenience.
     *
     * @param  fileName      the file name/path
     *
     * @throws FitsException if there were any errors writing the contents themselves.
     * @throws IOException   if the underlying stream could not be created or closed.
     *
     * @since                1.16
     *
     * @see                  #write(File)
     */
    public void write(String fileName) throws IOException, FitsException {
        write(new File(fileName));
    }

    // TODO For DataOutputStream this one conflicts with write(DataOutput).
    // However
    // once that one is deprecated, this one can be exposed safely.
    // public void write(OutputStream os) throws IOException, FitsException {
    // write(new FitsOutputStream(os));
    // }

    /**
     * Writes the contents to the specified output. This should not be exposed outside of this class, since the output
     * object must have FITS-specific encoding, and we can only make sure of that if this is called locally only.
     *
     * @param  out           the output with a FITS-specific encoding.
     *
     * @throws FitsException if the operation failed
     */
    private void write(ArrayDataOutput out) throws FitsException {
        for (BasicHDU<?> basicHDU : hduList) {
            basicHDU.write(out);
        }
    }

    /**
     * @deprecated               This method is poorly conceived as we cannot really write FITS to just any
     *                               <code>DataOutput</code> but only to specific {@link ArrayDataOutput}, which utilize
     *                               {@link nom.tam.util.FitsEncoder} to convert Java types to FITS binary format, such
     *                               as {@link FitsOutputStream} or {@link FitsFile} (or else a wrapped
     *                               <code>DataOutputStream</code>). As such, this method is inherently unsafe as it can
     *                               be used to create unreadable FITS files. It will be removed from a future major
     *                               release. Use one of the more appropriate other <code>write()</code> methods
     *                               instead. Writes the contents to an external file or stream. The file or stream
     *                               remains open and it is up to the caller to close it as appropriate.
     *
     * @param      os            A <code>DataOutput</code> stream.
     *
     * @throws     FitsException if the operation failed
     *
     * @see                      #write(FitsFile)
     * @see                      #write(FitsOutputStream)
     * @see                      #write(File)
     * @see                      #write(String)
     */
    @Deprecated
    public void write(DataOutput os) throws FitsException {
        if (os instanceof FitsFile) {
            try {
                write((FitsFile) os);
            } catch (IOException e) {
                throw new FitsException("Error writing to FITS file: " + e, e);
            }
            return;
        }

        if (os instanceof FitsOutputStream) {
            try {
                write((FitsOutputStream) os);
            } catch (IOException e) {
                throw new FitsException("Error writing to FITS output stream: " + e, e);
            }
            return;
        }

        if (!(os instanceof DataOutputStream)) {
            throw new FitsException("Cannot create FitsOutputStream from class " + os.getClass().getName());
        }

        try {
            write(new FitsOutputStream((DataOutputStream) os));
        } catch (IOException e) {
            throw new FitsException("Error writing to the FITS output stream: " + e, e);
        }
    }

    @Override
    public void close() throws IOException {
        if (dataStr != null) {
            dataStr.close();
        }
    }

    /**
     * set the checksum of a HDU.
     *
     * @param      hdu           the HDU to add a checksum
     *
     * @throws     FitsException the checksum could not be added to the header
     *
     * @deprecated               use {@link FitsCheckSum#setChecksum(BasicHDU)}
     */
    @Deprecated
    public static void setChecksum(BasicHDU<?> hdu) throws FitsException {
        FitsCheckSum.setChecksum(hdu);
    }

    /**
     * calculate the checksum for the block of data
     *
     * @param      data the data to create the checksum for
     *
     * @return          the checksum
     *
     * @deprecated      use {@link FitsCheckSum#checksum(byte[])}
     */
    @Deprecated
    public static long checksum(final byte[] data) {
        return FitsCheckSum.checksum(data);
    }

    /**
     * Checks if the file ends before the current read positon, and if so, it may log a warning. This may happen with
     * {@link FitsFile} where the contract of {@link RandomAccess} allows for skipping ahead beyond the end of file,
     * since expanding the file is allowed when writing. Only a subsequent read call would fail.
     *
     * @param  in          the input from which the FITS content was read.
     *
     * @return             <code>true</code> if the current read position is beyond the end-of-file, otherwise
     *                         <code>false</code>.
     *
     * @throws IOException if there was an IO error accessing the file or stream.
     *
     * @see                ArrayDataInput#skip(long)
     * @see                ArrayDataInput#skipBytes(int)
     * @see                ArrayDataInput#skipAllBytes(long)
     *
     * @since              1.16
     */
    static boolean checkTruncated(ArrayDataInput in) throws IOException {
        if (!(in instanceof RandomAccess)) {
            // We cannot skip more than is available in an input stream.
            return false;
        }

        RandomAccess f = (RandomAccess) in;
        long pos = f.getFilePointer();
        long len = f.length();
        if (pos > len) {
            LOG.log(Level.WARNING, "Premature file end at " + len + " (expected " + pos + ")", new Throwable());
            return true;
        }
        return false;
    }
}
