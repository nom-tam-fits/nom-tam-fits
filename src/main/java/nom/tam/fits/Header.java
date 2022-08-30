package nom.tam.fits;

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

import static nom.tam.fits.header.Standard.BITPIX;
import static nom.tam.fits.header.Standard.BLANKS;
import static nom.tam.fits.header.Standard.COMMENT;
import static nom.tam.fits.header.Standard.END;
import static nom.tam.fits.header.Standard.EXTEND;
import static nom.tam.fits.header.Standard.GCOUNT;
import static nom.tam.fits.header.Standard.GROUPS;
import static nom.tam.fits.header.Standard.HISTORY;
import static nom.tam.fits.header.Standard.NAXIS;
import static nom.tam.fits.header.Standard.NAXISn;
import static nom.tam.fits.header.Standard.PCOUNT;
import static nom.tam.fits.header.Standard.SIMPLE;
import static nom.tam.fits.header.Standard.TFIELDS;
import static nom.tam.fits.header.Standard.XTENSION;
import static nom.tam.fits.header.Standard.XTENSION_BINTABLE;
import static nom.tam.fits.header.extra.CXCExt.LONGSTRN;

import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.FitsFactory.FitsSettings;
import nom.tam.fits.header.Bitpix;
import nom.tam.fits.header.IFitsHeader;
import nom.tam.fits.header.IFitsHeader.VALUE;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.ComplexValue;
import nom.tam.util.Cursor;
import nom.tam.util.FitsIO;
import nom.tam.util.FitsOutput;
import nom.tam.util.HashedList;
import nom.tam.util.RandomAccess;

/**
 * This class describes methods to access and manipulate the header for a FITS
 * HDU. This class does not include code specific to particular types of HDU. As
 * of version 1.1 this class supports the long keyword convention which allows
 * long string keyword values to be split among multiple keywords
 *
 * <pre>
 *    KEY        = 'ABC&amp;'   /A comment
 *    CONTINUE      'DEF&amp;'  / Another comment
 *    CONTINUE      'GHIJKL '
 * </pre>
 *
 * The methods getStringValue(key), addValue(key,value,comment) and
 * deleteCard(key) will get, create/update and delete long string values if the
 * longStringsEnabled flag is set. This flag is set automatically when a FITS
 * header with a LONGSTRN card is found. The value is not checked. It may also
 * be set/unset using the static method setLongStringsEnabled(boolean). [So if a
 * user wishes to ensure that it is not set, it should be unset after any header
 * is read] When long strings are found in the FITS header users should be
 * careful not to interpose new header cards within a long value sequence. When
 * writing long strings, the comment is included in the last card. If a user is
 * writing long strings, a the keyword LONGSTRN = 'OGIP 1.0' should be added to
 * the FITS header, but this is not done automatically for the user.
 */
public class Header implements FitsElement {

    /**
     * The default character position to which comments should be aligned if possible (0-based).
     */
    public static final int DEFAULT_COMMENT_ALIGN = 30;

    /**
     * The earliest position (0-based) at which a comment may start for a regular key/value entry.
     */
    public static final int MIN_COMMENT_ALIGN = 20;

    /**
     * The largest (0-based)  comment alignment allowed that can still contain some meaningful comment (word)
     */
    public static final int MAX_COMMENT_ALIGN = 70;

    /** 
     * The alignment position of card comments for a more pleasing visual experience. Comments will be
     * aligned to this position, provided the lengths of all fields allow for it.
     */
    private static int commentAlign = DEFAULT_COMMENT_ALIGN;


    private static final Logger LOG = Logger.getLogger(Header.class.getName());

    private static final int MIN_NUMBER_OF_CARDS_FOR_VALID_HEADER = 4;

    /**
     * The actual header data stored as a HashedList of HeaderCard's.
     */
    private final HashedList<HeaderCard> cards;

    /** Offset of this Header in the FITS file */
    private long fileOffset;

    private List<HeaderCard> duplicates;

    private HashSet<String> dupKeys;

    /** Input descriptor last time header was read */
    private ArrayDataInput input;

    /**
     * The mimimum number of cards to write, including blank header space as
     * described in the FITS 4.0 standard.
     */
    private int minCards;

    /**
     * The number of bytes that this header occupied in file.
     * (for re-writing).
     */
    private long readSize;

    /**
     * the sorter used to sort the header cards defore writing the header.
     */
    private Comparator<String> headerSorter;


    /**
     * Create a header by reading the information from the input stream.
     *
     * @param dis
     *            The input stream to read the data from.
     * @return <CODE>null</CODE> if there was a problem with the header;
     *         otherwise return the header read from the input stream.
     * @throws TruncatedFileException
     *             if the stream ended prematurely
     * @throws IOException
     *             if the header could not be read.
     */
    public static Header readHeader(ArrayDataInput dis) throws TruncatedFileException, IOException {
        Header myHeader = new Header();
        try {
            myHeader.read(dis);
        } catch (EOFException e) {
            // An EOF exception is thrown only if the EOF was detected
            // when reading the first card. In this case we want
            // to return a null.
            return null;
        }
        return myHeader;
    }

    /**
     * please use {@link FitsFactory#setLongStringsEnabled(boolean)} instead.
     *
     * @param flag
     *            the new value for long-string enabling.
     */
    @Deprecated
    public static void setLongStringsEnabled(boolean flag) {
        FitsFactory.setLongStringsEnabled(flag);
    }

    /** Create a new header with the required default keywords for a standalone header. */
    public Header() {
        this.cards = new HashedList<>();
        this.headerSorter = new HeaderOrder();
        this.duplicates = null;
        clear();
    }

    /**
     * Create a header and populate it from the input stream
     *
     * @param is
     *            The input stream where header information is expected.
     * @throws IOException
     *             if the header could not be read.
     * @throws TruncatedFileException
     *             if the stream ended prematurely
     */
    public Header(ArrayDataInput is) throws TruncatedFileException, IOException {
        this();
        read(is);
    }

    /**
     * Create a header which points to the given data object.
     *
     * @param o
     *            The data object to be described.
     * @throws FitsException
     *             if the data was not valid for this header.
     */
    public Header(Data o) throws FitsException {
        this();
        o.fillHeader(this);
    }

    /**
     * Create a header and initialize it with a vector of strings.
     *
     * @param newCards
     *            Card images to be placed in the header.
     */
    public Header(String[] newCards) {
        this();
        for (String newCard : newCards) {
            this.cards.add(HeaderCard.create(newCard));
        }
    }

    /**
     * <p>
     * Preallocates a minimum header card space. When written to a stream, the header will be large enough to 
     * hold at least the specified number of cards. If the header has fewer physical cards
     * then the remaining space will be padded with blanks, leaving space for future additions, as specified
     * by the FITS 4.0 standard for <a href="https://fits.gsfc.nasa.gov/registry/headerspace.html">
     * preallocated header space</a>. 
     * </p>
     * <p>
     * This method is also called by {@link #read(ArrayDataInput)}, with the number of cards (including 
     * reserved blank space) contained in the header input stream, in order to ensure that the header remains 
     * rewritable even if it is shortened by the removal of cards (explicitly, or because they were
     * duplicates).
     * </p>
     * <p>
     * A new setting always overrides prior ones. For example, calling this method with an argument
     * that is %lt;=1 will eliminate (reset) any prior preallocated header space.
     * </p>
     * 
     * @param nCards    the mimimum number of 80-character header records that is header
     *                  must be able to support when written to a stream, including 
     *                  preallocated blank header space.
     * 
     * @since 1.16
     *                  
     * @see #getMinimumSize()
     * @see #write(ArrayDataOutput)
     * @see #read(ArrayDataInput)
     * @see #resetOriginalSize()
     * 
     */
    public void ensureCardSpace(int nCards) {
        if (nCards < 1) {
            nCards = 1;
        }
        this.minCards = nCards;
    }

    /**
     * Insert a new header card at the current position, deleting any prior
     * occurence of the same card while maintaining the current position to
     * point to after the newly inserted card.
     *
     * @param fcard
     *            The card to be inserted.
     */
    public void addLine(HeaderCard fcard) {
        if (fcard != null) {
            cursor().add(fcard);
        }
    }

    /**
     * Add or replace a key with the given boolean value and its standardized comment.
     * If the value is not compatible with the convention of the keyword, a warning message is
     * logged but no exception is thrown (at this point).
     *
     * @param key
     *            The header key.
     * @param val
     *            The boolean value.
     * @return    the new card that was added.
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     * @throws IllegalArgumentException
     *             If the keyword is invalid
     *             
     * @see #addValue(String, Boolean, String)
     */
    public HeaderCard addValue(IFitsHeader key, Boolean val) throws HeaderCardException, IllegalArgumentException {
        HeaderCard card = HeaderCard.create(key, val);
        addLine(card);
        return card;
    }



    /**
     * Add or replace a key with the given double value and its standardized comment.
     * If the value is not compatible with the convention of the keyword, a warning message is
     * logged but no exception is thrown (at this point).
     *
     * @param key
     *            The header key.
     * @param val
     *            The double value.
     * @return    the new card that was added.
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     * @throws IllegalArgumentException
     *             If the keyword is invalid
     * 
     * @see #addValue(String, Number, String)
     */
    public HeaderCard addValue(IFitsHeader key, Number val) throws HeaderCardException, IllegalArgumentException {
        HeaderCard card = HeaderCard.create(key, val);
        addLine(card);
        return card;
    }

    /**
     * Add or replace a key with the given string value and its standardized comment. 
     * If the value is not compatible with the convention of the keyword, a warning message is
     * logged but no exception is thrown (at this point).
     *
     * @param key
     *            The header key.
     * @param val
     *            The string value.
     * @return    the new card that was added.
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     * @throws IllegalArgumentException
     *             If the keyword is invalid
     *             
     * @see #addValue(String, String, String)
     */
    public HeaderCard addValue(IFitsHeader key, String val) throws HeaderCardException, IllegalArgumentException {
        HeaderCard card = HeaderCard.create(key, val);
        addLine(card);
        return card;
    }

    /**
     * Add or replace a key with the given complex value and its standardized comment.
     * If the value is not compatible with the convention of the keyword, a warning message is
     * logged but no exception is thrown (at this point).
     *
     * @param key
     *            The header key.
     * @param val
     *            The complex value.
     * @return    the new card that was added.
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     * @throws IllegalArgumentException
     *             If the keyword is invalid
     *             
     * @see #addValue(String, ComplexValue, String)
     * 
     * @since 1.17
     */
    public HeaderCard addValue(IFitsHeader key, ComplexValue val) throws HeaderCardException, IllegalArgumentException {
        HeaderCard card = HeaderCard.create(key, val);
        addLine(card);
        return card;
    }

    /**
     * Add or replace a key with the given boolean value and comment.
     *
     * @param key
     *            The header key.
     * @param val
     *            The boolean value.
     * @param comment
     *            A comment to append to the card.
     * @return    the new card that was added.
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     *             
     * @see #addValue(IFitsHeader, Boolean)
     * @see HeaderCard#HeaderCard(String, Boolean, String)
     */
    public HeaderCard addValue(String key, Boolean val, String comment) throws HeaderCardException {
        HeaderCard hc = new HeaderCard(key, val, comment);
        addLine(hc);
        return hc;
    }


    /**
     * Add or replace a key with the given number value and comment. The value will be represented in the
     * header card with use the native precision of the value or at least {@link nom.tam.util.FlexFormat#DOUBLE_DECIMALS},
     * whichever fits in the available card space. Trailing zeroes will be ommitted.
     *
     * @param key
     *            The header key.
     * @param val
     *            The number value.
     * @param comment
     *            A comment to append to the card.
     * @return    the new card that was added.
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     *
     * @see #addValue(String, Number, int, String)
     * @see #addValue(IFitsHeader, Number)
     * @see HeaderCard#HeaderCard(String, Number, String)
     */
    public HeaderCard addValue(String key, Number val, String comment) throws HeaderCardException {
        HeaderCard hc = new HeaderCard(key, val, comment);
        addLine(hc);
        return hc;
    }


    /**
     * Add or replace a key with the given number value and comment, using up to the specified decimal
     * places after the leading figure. Trailing zeroes will be ommitted.
     *
     * @param key
     *            The header key.
     * @param val
     *            The number value.
     * @param decimals
     *            The number of decimal places to show after the leading figure, or {@link nom.tam.util.FlexFormat#AUTO_PRECISION}
     *            to use the native precision of the value or at least {@link nom.tam.util.FlexFormat#DOUBLE_DECIMALS},
     *            whichever fits in the available card space.
     * @param comment
     *            A comment to append to the card.
     * @return    the new card that was added.
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     *             
     * @see #addValue(String, Number, String)
     * @see HeaderCard#HeaderCard(String, Number, int, String)
     */
    public HeaderCard addValue(String key, Number val, int decimals, String comment) throws HeaderCardException {
        HeaderCard hc = new HeaderCard(key, val, decimals, comment);
        addLine(hc);
        return hc;
    }


    /**
     * Add or replace a key with the given complex number value and comment. Trailing zeroes will be ommitted.
     *
     * @param key
     *            The header keyword.
     * @param val
     *            The complex number value.
     * @param comment
     *            A comment to append to the card.
     * @return    the new card that was added.
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     *             
     * @since 1.16
     *
     * @see #addValue(String, ComplexValue, int, String)
     * @see HeaderCard#HeaderCard(String, ComplexValue, String)
     */
    public HeaderCard addValue(String key, ComplexValue val, String comment) throws HeaderCardException {
        HeaderCard hc = new HeaderCard(key, val, comment);
        addLine(hc);
        return hc;
    }

    /**
     * Add or replace a key with the given complex number value and comment, using up to the specified decimal
     * places after the leading figure. Trailing zeroes will be ommitted.
     *
     * @param key
     *            The header keyword.
     * @param val
     *            The complex number value.
     * @param decimals
     *            The number of decimal places to show after the leading figure, or {@link nom.tam.util.FlexFormat#AUTO_PRECISION}
     *            to use the native precision of the value, or at least {@link nom.tam.util.FlexFormat#DOUBLE_DECIMALS},
     *            whichever fits in the available card space.
     * @param comment
     *            A comment to append to the card.
     * @return    the new card that was added.
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     *             
     * @since 1.16
     *             
     * @see #addValue(String, ComplexValue, String)
     * @see HeaderCard#HeaderCard(String, ComplexValue, int, String)
     */
    public HeaderCard addValue(String key, ComplexValue val, int decimals, String comment) throws HeaderCardException {
        HeaderCard hc = new HeaderCard(key, val, decimals, comment);
        addLine(hc);
        return hc;
    }

    /**
     * Add or replace a key with the given integer value in hexadecimal representation,
     * and comment.
     *
     * @param key
     *            The header key.
     * @param val
     *            The integer value.
     * @param comment
     *            A comment to append to the card.
     * @return    the new card that was added.
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     * 
     * @since 1.16
     *            
     * @see #addValue(String, Number, String)
     * @see HeaderCard#createHexValueCard(String, long)
     * @see #getHexValue(String)
     */
    public HeaderCard addHexValue(String key, long val, String comment) throws HeaderCardException {
        HeaderCard hc = HeaderCard.createHexValueCard(key, val, comment);
        addLine(hc);
        return hc;
    }

    /**
     * Add or replace a key with the given string value and comment.
     *
     * @param key
     *            The header key.
     * @param val
     *            The string value.
     * @param comment
     *            A comment to append to the card.
     * @return    the new card that was added.
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     *             
     * @see #addValue(IFitsHeader, String)
     * @see HeaderCard#HeaderCard(String, String, String)
     */
    public HeaderCard addValue(String key, String val, String comment) throws HeaderCardException {
        HeaderCard hc = new HeaderCard(key, val, comment);
        addLine(hc);
        return hc;
    }

    /**
     * get a builder for filling the header cards using the builder pattern.
     *
     * @param key
     *            the key for the first card.
     * @return the builder for header cards.
     */
    public HeaderCardBuilder card(IFitsHeader key) {
        return new HeaderCardBuilder(this, key);
    }

    /**
     * Tests if the specified keyword is present in this table.
     *
     * @param key
     *            the keyword to be found.
     * @return <code>true</code> if the specified keyword is present in this
     *         table; <code>false</code> otherwise.
     */
    public final boolean containsKey(IFitsHeader key) {
        return this.cards.containsKey(key.key());
    }

    /**
     * Tests if the specified keyword is present in this table.
     *
     * @param key
     *            the keyword to be found.
     * @return <code>true</code> if the specified keyword is present in this
     *         table; <code>false</code> otherwise.
     */
    public final boolean containsKey(String key) {
        return this.cards.containsKey(key);
    }

    /**
     * Delete the card associated with the given key. Nothing occurs if the key
     * is not found.
     *
     * @param key
     *            The header key.
     */
    public void deleteKey(IFitsHeader key) {
        deleteKey(key.key());
    }

    /**
     * Delete the card associated with the given key. Nothing occurs if the key
     * is not found.
     *
     * @param key
     *            The header key.
     */
    public void deleteKey(String key) {
        // AK: This version will not move the current position to the deleted
        // key
        if (containsKey(key)) {
            this.cards.remove(this.cards.get(key));
        }
    }

    /**
     * Print the header to a given stream.
     *
     * @param ps
     *            the stream to which the card images are dumped.
     */
    public void dumpHeader(PrintStream ps) {
        Cursor<String, HeaderCard> iter = iterator();
        while (iter.hasNext()) {
            ps.println(iter.next());
        }
    }

    /**
     * Find the card associated with a given key. If found this sets the mark to
     * the card, otherwise it unsets the mark.
     *
     * @param key
     *            The header key.
     * @return <CODE>null</CODE> if the keyword could not be found; return the
     *         HeaderCard object otherwise.
     */
    public HeaderCard findCard(IFitsHeader key) {
        return this.findCard(key.key());
    }

    /**
     * Find the card associated with a given key. If found this sets the mark to
     * the card, otherwise it unsets the mark.
     *
     * @param key
     *            The header key.
     * @return <CODE>null</CODE> if the keyword could not be found; return the
     *         HeaderCard object otherwise.
     */
    public HeaderCard findCard(String key) {
        HeaderCard card = this.cards.get(key);
        if (card != null) {
            cursor().setKey(key);
        }
        return card;
    }

    /**
     * Find the card associated with a given key.
     *
     * @param key
     *            The header key.
     * @return <CODE>null</CODE> if the keyword could not be found; return the
     *         card image otherwise.
     */
    public String findKey(String key) {
        HeaderCard card = findCard(key);
        if (card == null) {
            return null;
        }
        return card.toString();
    }

    /**
     * Get the bid decimal value associated with the given key.
     *
     * @param key
     *            The header key.
     * @return The associated value or 0.0 if not found.
     */
    public final BigDecimal getBigDecimalValue(IFitsHeader key) {
        return getBigDecimalValue(key.key());
    }

    /**
     * Get the big decimal value associated with the given key.
     *
     * @param key
     *            The header key.
     * @param dft
     *            The default value to return if the key cannot be found.
     * @return the associated value.
     */
    public final BigDecimal getBigDecimalValue(IFitsHeader key, BigDecimal dft) {
        return getBigDecimalValue(key.key(), dft);
    }

    /**
     * Get the big decimal value associated with the given key.
     *
     * @param key
     *            The header key.
     * @return The associated value or 0.0 if not found.
     */
    public final BigDecimal getBigDecimalValue(String key) {
        return getBigDecimalValue(key, BigDecimal.ZERO);
    }

    /**
     * Get the big decimal value associated with the given key.
     *
     * @param key
     *            The header key.
     * @param dft
     *            The default value to return if the key cannot be found.
     * @return the associated value.
     */
    public BigDecimal getBigDecimalValue(String key, BigDecimal dft) {
        HeaderCard fcard = findCard(key);
        if (fcard == null) {
            return dft;
        }
        return fcard.getValue(BigDecimal.class, dft);
    }

    /**
     * Get the big integer value associated with the given key.
     *
     * @param key
     *            The header key.
     *
     * @return the associated value or 0 if not found.
     */
    public final BigInteger getBigIntegerValue(IFitsHeader key) {
        return getBigIntegerValue(key.key());
    }


    /**
     * Get the big integer value associated with the given key.
     *
     * @param key
     *            The header key.
     * @param dft
     *            The default value to be returned if the key cannot be found.
     * @return the associated value.
     */
    public final BigInteger getBigIntegerValue(IFitsHeader key, BigInteger dft) {
        return getBigIntegerValue(key.key(), dft);
    }

    /**
     * Get the big integer value associated with the given key.
     *
     * @param key
     *            The header key.
     * @return The associated value or 0 if not found.
     */
    public final BigInteger getBigIntegerValue(String key) {
        return getBigIntegerValue(key, BigInteger.ZERO);
    }

    /**
     * Get the big integer value associated with the given key.
     *
     * @param key
     *            The header key.
     * @param dft
     *            The default value to be returned if the key cannot be found.
     * @return the associated value.
     */
    public BigInteger getBigIntegerValue(String key, BigInteger dft) {
        HeaderCard fcard = findCard(key);
        if (fcard == null) {
            return dft;
        }
        return fcard.getValue(BigInteger.class, dft);
    }


    /**
     * Get the complex number value associated with the given key.
     *
     * @param key
     *            The header key.
     * @return The associated value or {@link ComplexValue#ZERO} if not found.
     * 
     * @since 1.16
     * 
     * @see #getComplexValue(String, ComplexValue)
     * @see HeaderCard#getValue(Class, Object)
     * @see #addValue(String, ComplexValue, String)
     */
    public final ComplexValue getComplexValue(String key) {
        return getComplexValue(key, ComplexValue.ZERO);
    }

    /**
     * Get the complex number value associated with the given key.
     *
     * @param key
     *            The header key.
     * @param dft
     *            The default value to return if the key cannot be found.
     * @return the associated value.
     * 
     * @since 1.16
     * 
     * @see #getComplexValue(String)
     * @see HeaderCard#getValue(Class, Object)
     * @see #addValue(String, ComplexValue, String)
     */
    public ComplexValue getComplexValue(String key, ComplexValue dft) {
        HeaderCard fcard = findCard(key);
        if (fcard == null) {
            return dft;
        }
        return fcard.getValue(ComplexValue.class, dft);
    }

    /**
     * Get the <CODE>boolean</CODE> value associated with the given key.
     *
     * @param key
     *            The header key.
     * @return The value found, or false if not found or if the keyword is not a
     *         logical keyword.
     */
    public final boolean getBooleanValue(IFitsHeader key) {
        return getBooleanValue(key.key());
    }

    /**
     * Get the <CODE>boolean</CODE> value associated with the given key.
     *
     * @param key
     *            The header key.
     * @param dft
     *            The value to be returned if the key cannot be found or if the
     *            parameter does not seem to be a boolean.
     * @return the associated value.
     */
    public final boolean getBooleanValue(IFitsHeader key, boolean dft) {
        return getBooleanValue(key.key(), dft);
    }

    /**
     * Get the <CODE>boolean</CODE> value associated with the given key.
     *
     * @param key
     *            The header key.
     * @return The value found, or false if not found or if the keyword is not a
     *         logical keyword.
     */
    public final boolean getBooleanValue(String key) {
        return getBooleanValue(key, false);
    }

    /**
     * Get the <CODE>boolean</CODE> value associated with the given key.
     *
     * @param key
     *            The header key.
     * @param dft
     *            The value to be returned if the key cannot be found or if the
     *            parameter does not seem to be a boolean.
     * @return the associated value.
     */
    public boolean getBooleanValue(String key, boolean dft) {
        HeaderCard fcard = findCard(key);
        if (fcard == null) {
            return dft;
        }
        return fcard.getValue(Boolean.class, dft).booleanValue();
    }

    /**
     * Get the n'th card image in the header
     *
     * @param n
     *            the card index to get
     * @return the card image; return <CODE>null</CODE> if the n'th card does
     *         not exist.
     * @deprecated An iterator from {@link #iterator(int)} or
     *             {@link #iterator()} should be used for sequential access to
     *             the header.
     */
    @Deprecated
    public String getCard(int n) {
        if (n >= 0 && n < this.cards.size()) {
            return this.cards.get(n).toString();
        }
        return null;
    }

    /**
     * Return the size of the data including any needed padding.
     *
     * @return the data segment size including any needed padding.
     */
    public long getDataSize() {
        return FitsUtil.addPadding(trueDataSize());
    }

    /**
     * Get the <CODE>double</CODE> value associated with the given key.
     *
     * @param key
     *            The header key.
     * @return The associated value or 0.0 if not found.
     */
    public final double getDoubleValue(IFitsHeader key) {
        return getDoubleValue(key.key());
    }

    /**
     * Get the <CODE>double</CODE> value associated with the given key.
     *
     * @param key
     *            The header key.
     * @param dft
     *            The default value to return if the key cannot be found.
     * @return the associated value.
     */
    public final double getDoubleValue(IFitsHeader key, double dft) {
        return getDoubleValue(key.key(), dft);
    }

    /**
     * Get the <CODE>double</CODE> value associated with the given key.
     *
     * @param key
     *            The header key.
     * @return The associated value or 0.0 if not found.
     */
    public final double getDoubleValue(String key) {
        return getDoubleValue(key, 0.0);
    }

    /**
     * Get the <CODE>double</CODE> value associated with the given key.
     *
     * @param key
     *            The header key.
     * @param dft
     *            The default value to return if the key cannot be found.
     * @return the associated value.
     */
    public double getDoubleValue(String key, double dft) {
        HeaderCard fcard = findCard(key);
        if (fcard == null) {
            return dft;
        }
        return fcard.getValue(Double.class, dft).doubleValue();
    }

    /**
     * <p>
     * Returns the list of duplicate cards in the order they appeared in the parsed header.
     * You can access the first occurence of each of every duplicated FITS keyword 
     * using the usual <code>Header.getValue()</code>, and find further occurrences
     * in the list returned here.
     * </p>
     * <p>
     * The FITS standared strongly discourages using the keywords multiple times with assigned
     * values, and specifies that the values of such keywords are undefined by definitions.
     * Our library is thus far more tolerant than the FITS standard, allowing you to access
     * each and every value that was specified for the same keyword.
     * </p>
     * <p>
     * On the other hand FITS does not limit how many times you can add comment-style
     * keywords to a header. If you must used the same keyword multiple times in your
     * header, you should consider using comment-style entries instead.
     * </p>
     * 
     * 
     * @return the list of duplicate cards. Note that when the header is read
     *         in, only the last entry for a given keyword is retained in the
     *         active header. This method returns earlier cards that have been
     *         discarded in the order in which they were encountered in the
     *         header. It is possible for there to be many cards with the same
     *         keyword in this list.
     *         
     * @see #hadDuplicates()
     * @see #getDuplicateKeySet()
     */
    public List<HeaderCard> getDuplicates() {
        return this.duplicates;
    }

    /**
     * Returns the set of keywords that had more than one value assignment in the parsed
     * header.
     * 
     * @return  the set of header keywords that were assigned more than once in the
     *          same header, or <code>null</code> if there were no duplicate assignments.
     *          
     * @see #hadDuplicates()
     * @see #getDuplicates()
     * 
     * @since 1.17
     */
    public Set<String> getDuplicateKeySet() {
        return dupKeys;
    }

    /**
     * @return Get the offset of this header
     */
    @Override
    public long getFileOffset() {
        return this.fileOffset;
    }

    /**
     * Get the <CODE>float</CODE> value associated with the given key.
     *
     * @param key
     *            The header key.
     * @return The associated value or 0.0 if not found.
     */
    public final float getFloatValue(IFitsHeader key) {
        return getFloatValue(key.key());

    }

    /**
     * @return the <CODE>float</CODE> value associated with the given key.
     * @param key
     *            The header key.
     * @param dft
     *            The value to be returned if the key is not found.
     */
    public final float getFloatValue(IFitsHeader key, float dft) {
        return getFloatValue(key.key(), dft);
    }

    /**
     * Get the <CODE>float</CODE> value associated with the given key.
     *
     * @param key
     *            The header key.
     * @return The associated value or 0.0 if not found.
     */
    public final float getFloatValue(String key) {
        return getFloatValue(key, 0.0F);
    }

    /**
     * @return the <CODE>float</CODE> value associated with the given key.
     * @param key
     *            The header key.
     * @param dft
     *            The value to be returned if the key is not found.
     */
    public float getFloatValue(String key, float dft) {
        HeaderCard fcard = findCard(key);
        if (fcard == null) {
            return dft;
        }
        return fcard.getValue(Float.class, dft).floatValue();
    }

    /**
     * Get the <CODE>int</CODE> value associated with the given key.
     *
     * @param key
     *            The header key.
     * @return The associated value or 0 if not found.
     */
    public final int getIntValue(IFitsHeader key) {
        return (int) getLongValue(key);
    }

    /**
     * @return the value associated with the key as an int.
     * @param key
     *            The header key.
     * @param dft
     *            The value to be returned if the key is not found.
     */
    public final int getIntValue(IFitsHeader key, int dft) {
        return (int) getLongValue(key, dft);
    }

    /**
     * Get the <CODE>int</CODE> value associated with the given key.
     *
     * @param key
     *            The header key.
     * @return The associated value or 0 if not found.
     */
    public final int getIntValue(String key) {
        return (int) getLongValue(key);
    }

    /**
     * @return the value associated with the key as an int.
     * @param key
     *            The header key.
     * @param dft
     *            The value to be returned if the key is not found.
     */
    public int getIntValue(String key, int dft) {
        return (int) getLongValue(key, dft);
    }



    /**
     * Get the n'th key in the header.
     *
     * @param n
     *            the index of the key
     * @return the card image; return <CODE>null</CODE> if the n'th key does not
     *         exist.
     * @deprecated An iterator from {@link #iterator(int)} or
     *             {@link #iterator()} should be used for sequential access to
     *             the header.
     */
    @Deprecated
    public String getKey(int n) {
        if (n >= 0 && n < this.cards.size()) {
            return this.cards.get(n).getKey();
        }
        return null;

    }

    /**
     * Get the <CODE>long</CODE> value associated with the given key.
     *
     * @param key
     *            The header key.
     * @return The associated value or 0 if not found.
     */
    public final long getLongValue(IFitsHeader key) {
        return getLongValue(key.key());
    }

    /**
     * Get the <CODE>long</CODE> value associated with the given key.
     *
     * @param key
     *            The header key.
     * @param dft
     *            The default value to be returned if the key cannot be found.
     * @return the associated value.
     */
    public final long getLongValue(IFitsHeader key, long dft) {
        return getLongValue(key.key(), dft);
    }

    /**
     * Get the <CODE>long</CODE> value associated with the given key.
     *
     * @param key
     *            The header key.
     * @return The associated value or 0 if not found.
     */
    public final long getLongValue(String key) {
        return getLongValue(key, 0L);
    }

    /**
     * Get the <CODE>long</CODE> value associated with the given key.
     *
     * @param key
     *            The header key.
     * @param dft
     *            The default value to be returned if the key cannot be found.
     * @return the associated value.
     */
    public long getLongValue(String key, long dft) {
        HeaderCard fcard = findCard(key);
        if (fcard == null) {
            return dft;
        }
        return fcard.getValue(Long.class, dft).longValue();
    }

    /**
     * Get the <CODE>long</CODE> value associated with the given key.
     *
     * @param key
     *            The header key.
     * @return The associated value or 0 if not found.
     * 
     * @since 1.16
     * 
     * @see #getHexValue(String, long)
     * @see HeaderCard#getHexValue()
     * @see #addHexValue(String, long, String)
     */
    public final long getHexValue(String key) {
        return getHexValue(key, 0L);
    }

    /**
     * Get the <CODE>long</CODE> value stored in hexadecimal format under the specified key.
     *
     * @param key
     *            The header key.
     * @param dft
     *            The default value to be returned if the key cannot be found.
     * @return the associated value.
     * 
     * @since 1.16
     * 
     * @see #getHexValue(String)
     * @see HeaderCard#getHexValue()
     * @see #addHexValue(String, long, String)
     */
    public long getHexValue(String key, long dft) {
        HeaderCard fcard = findCard(key);
        if (fcard == null) {
            return dft;
        }
        try {
            return fcard.getHexValue();
        } catch (NumberFormatException e) {
            return dft;
        }
    }


    /**
     * Returns the nominal number of currently defined cards in this header. Each card can 
     * consist of one or more 80-character wide header records.
     * 
     * @return      the number of nominal cards in the header
     * 
     * @see #getNumberOfPhysicalCards()
     */
    public int getNumberOfCards() {
        return this.cards.size();        
    }

    /**
     * Returns the number of 80-character header records in this header, including
     * an END marker (whether or not it is currently contained).
     * 
     * @return the number of physical cards in the header, including the END marker.
     * 
     * @see #getNumberOfCards()
     * @see #getSize()
     */
    public int getNumberOfPhysicalCards() {
        int count = 0;
        for (HeaderCard card : this.cards) {
            count += card.cardSize();
        }

        // AK: Count the END card, which may not have been added yet...
        if (!containsKey(END)) {
            count++;
        }

        return count;
    }

    /**
     * Returns the minimum number of bytes that will be written by this header, either
     * as the original byte size of a header that was read, or else the minimum 
     * preallocated capacity after setting {@link #ensureCardSpace(int)}.
     * 
     * @return  the minimum byte size for this header. The actual header may take up 
     *          more space than that (but never less!), depending on the number of cards 
     *          contained.
     * 
     * @since 1.16
     * 
     * @see #ensureCardSpace(int)
     * @see #read(ArrayDataInput)
     */
    public long getMinimumSize() {
        return FitsUtil.addPadding((long) this.minCards * HeaderCard.FITS_HEADER_CARD_SIZE);
    }

    /**
     * Returns the original size of the header in the stream from which it was read. 
     * 
     * @return  the size of the original header in bytes, or 0 if the header was not 
     *          read from a stream.
     *          
     * @see #read(ArrayDataInput)
     * @see #getMinimumSize()
     */
    public final long getOriginalSize() {
        return readSize;
    }

    /**
     * Returns the current byte size of this header.
     * 
     * @return      the size of the header in bytes, or 0 if the header is invalid.
     * 
     * @see #getMinimumSize()
     * @see #ensureCardSpace(int)
     */
    @Override
    public final long getSize() {
        return headerSize();
    }

    /**
     * Get the <CODE>String</CODE> value associated with the given standard key.
     *
     * @param key
     *            The standard header key.
     * @return The associated value or null if not found or if the value is not
     *         a string.
     *         
     * @see #getStringValue(String)
     * @see #getStringValue(IFitsHeader, String)
     */
    public final String getStringValue(IFitsHeader key) {
        return getStringValue(key.key());
    }

    /**
     * Get the <CODE>String</CODE> value associated with the given standard key.
     *
     * @param key
     *            The standard header key.
     * @param dft
     *            The default value.
     * @return The associated value or the default value if not found or if the value is not
     *         a string.
     *         
     * @see #getStringValue(String, String)
     * @see #getStringValue(IFitsHeader)
     * 
     */
    public final String getStringValue(IFitsHeader key, String dft) {
        return getStringValue(key.key(), dft);
    }


    /**
     * Get the <CODE>String</CODE> value associated with the given key.
     *
     * @param key
     *            The header key.
     * @return The associated value or null if not found or if the value is not
     *         a string.
     *         
     * @see #getStringValue(IFitsHeader)
     * @see #getStringValue(String, String)
     */
    public final String getStringValue(String key) {
        return getStringValue(key, null);
    }

    /**
     * Get the <CODE>String</CODE> value associated with the given key.
     *
     * @param key
     *            The header key.
     * @param dft
     *            The default value.
     * @return The associated value or the default value if not found or if the value is not
     *         a string.
     *         
     * @see #getStringValue(IFitsHeader, String)
     * @see #getStringValue(String)
     */
    public String getStringValue(String key, String dft) {

        HeaderCard fcard = findCard(key);
        if (fcard == null || !fcard.isStringValue()) {
            return dft;
        }

        return fcard.getValue();
    }

    /**
     * @return Were duplicate header keys found when this record was read in?
     */
    public boolean hadDuplicates() {
        return this.duplicates != null;
    }

    /**
     * Adds a line to the header using the COMMENT style, i.e., no '=' in column
     * 9. The comment text may be truncated to fit into a single record, which is
     * returned. Alternatively, you can split longer comments among multiple consecutive
     * cards of the same type by {@link #insertCommentStyleMultiline(String, String)}.
     *
     * @param key
     *            The comment style header keyword, or <code>null</code> for an
     *            empty comment line.
     * @param comment
     *            A string comment to follow. Illegal characters will be replaced by '?' and the
     *            comment may be truncated to fit into the card-space (71 characters).
     * @return    The new card that was inserted, or <code>null</code> if the keyword itself was 
     *            invalid or the comment was <code>null</code>.
     *            
     * @see #insertCommentStyleMultiline(String, String)
     * @see HeaderCard#createCommentStyleCard(String, String)
     * 
     */
    public HeaderCard insertCommentStyle(String key, String comment) {
        if (comment == null) {
            comment = "";
        } else if (comment.length() > HeaderCard.MAX_COMMENT_CARD_COMMENT_LENGTH) {
            comment = comment.substring(0, HeaderCard.MAX_COMMENT_CARD_COMMENT_LENGTH);
            LOG.warning("Truncated comment to fit card: [" + comment + "]");
        }

        try {
            HeaderCard hc = HeaderCard.createCommentStyleCard(key, HeaderCard.sanitize(comment));
            cursor().add(hc);
            return hc;
        } catch (HeaderCardException e) {
            LOG.log(Level.WARNING, "Ignoring comment card with invalid key [" + HeaderCard.sanitize(key) + "]", e);
            return null;
        }
    }

    /**
     * Adds a line to the header using the COMMENT style, i.e., no '=' in column
     * 9. If the comment does not fit in a single record, then it will be split
     * (wrapped) among multiple consecutive records with the same keyword. Wrapped
     * lines will end with '&amp;' (not itself a standard) to indicate comment cards
     * that might belong together.
     *
     * @param key
     *            The comment style header keyword, or <code>null</code> for an
     *            empty comment line.
     * @param comment
     *            A string comment to follow. Illegal characters will be replaced by '?' and the
     *            comment may be split among multiple records as necessary to be fully preserved.
     * @return    The number of cards inserted.
     * 
     * @since 1.16
     * 
     * @see #insertCommentStyle(String, String)
     * @see #insertComment(String)
     * @see #insertUnkeyedComment(String)
     * @see #insertHistory(String)
     */
    public int insertCommentStyleMultiline(String key, String comment) {

        // Empty comments must have at least one space char to write at least one
        // comment card...
        if (comment == null) {
            comment = " ";
        } else if (comment.isEmpty()) {
            comment = " ";
        }  

        int n = 0;    

        for (int from = 0; from < comment.length();) {
            int to = from + HeaderCard.MAX_COMMENT_CARD_COMMENT_LENGTH;
            String part = null;
            if (to < comment.length()) {
                part = comment.substring(from, --to) + "&";
            } else {
                part = comment.substring(from);
            }

            if (insertCommentStyle(key, part) == null) {
                return n;
            }
            from = to;
            n++;
        }

        return n;
    }

    /**
     * Adds one or more consecutive COMMENT records, wrapping the comment text as necessary.
     *
     * @param value
     *            The comment.
     * @return    The number of consecutive COMMENT cards that were inserted
     * 
     * @see #insertCommentStyleMultiline(String, String)
     * @see #insertUnkeyedComment(String)
     * @see #insertHistory(String)
     * @see HeaderCard#createCommentCard(String)
     */
    public int insertComment(String value) {
        return insertCommentStyleMultiline(COMMENT.key(), value);
    }

    /**
     * Adds one or more consecutive comment records with no keyword (bytes 1-9 left blank), 
     * wrapping the comment text as necessary.
     *
     * @param value
     *            The comment.
     * @return    The number of consecutive comment-style cards with no keyword (blank keyword) that were inserted.
     * 
     * @since 1.16
     * 
     * @see #insertCommentStyleMultiline(String, String)
     * @see #insertComment(String)
     * @see #insertHistory(String)
     * @see HeaderCard#createUnkeyedCommentCard(String)
     * @see #insertBlankCard()
     */
    public int insertUnkeyedComment(String value) {
        return insertCommentStyleMultiline(BLANKS.key(), value);
    }

    /**
     * Adds a blank card into the header.
     * 
     * @since 1.16
     * 
     * @see #insertUnkeyedComment(String)
     */
    public void insertBlankCard() {
        insertCommentStyle(null, null);
    }

    /**
     * Adds one or more consecutive a HISTORY records, wrapping the comment text as necessary.
     *
     * @param value
     *            The history record.
     * @return    The number of consecutive HISTORY cards that were inserted
     * 
     * @see #insertCommentStyleMultiline(String, String)
     * @see #insertComment(String)
     * @see #insertUnkeyedComment(String)
     * @see HeaderCard#createHistoryCard(String)
     */
    public int insertHistory(String value) {
        return insertCommentStyleMultiline(HISTORY.key(), value);
    }

    /** @return an iterator over the header cards */
    public Cursor<String, HeaderCard> iterator() {
        return this.cards.iterator(0);
    }

    /**
     * @return an iterator over the header cards starting at an index
     * @param index
     *            the card index to start the iterator
     */
    public Cursor<String, HeaderCard> iterator(int index) {
        return this.cards.iterator(index);
    }

    /**
     * Return the iterator that represents the current position in the header.
     * This provides a connection between editing headers through Header
     * add/append/update methods, and via Cursors, which can be used
     * side-by-side while maintaining desired card ordering. For the reverse
     * direction ( translating iterator position to current position in the
     * header), we can just use findCard().
     * 
     * @return the iterator representing the current position in the header.
     */
    private Cursor<String, HeaderCard> cursor() {
        return this.cards.cursor();
    }

    /**
     * @return Create the data element corresponding to the current header
     * @throws FitsException
     *             if the header did not contain enough information to detect
     *             the type of the data
     */
    public Data makeData() throws FitsException {
        return FitsFactory.dataFactory(this);
    }

    /**
     * @return the next card in the Header using the current iterator
     */
    public HeaderCard nextCard() {
        if (cursor().hasNext()) {
            return cursor().next();
        }
        return null;
    }

    /**
     * Create a header which points to the given data object.
     *
     * @param o
     *            The data object to be described.
     * @throws FitsException
     *             if the data was not valid for this header.
     * @deprecated Use the appropriate Header constructor.
     */
    @Deprecated
    public void pointToData(Data o) throws FitsException {
        o.fillHeader(this);
    }

    /**
     * Remove all cards and reset the header to its default status.
     * 
     */
    private void clear() {
        cards.clear();
        duplicates = null;
        dupKeys = null;
        readSize = 0;
        fileOffset = -1;
        minCards = 0;
    }

    /**
     * Checks if the header is empty, that is if it contains no cards at all.
     * 
     * @return  <code>true</code> if the header contains no cards, otherwise <code>false</code>.
     * 
     * @since 1.16
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * <p>
     * Reads new header data from an input, discarding any prior content.
     * </p>
     * <p>
     * As of 1.16, the header is ensured to (re)write at least the same number of
     * cards as before, padding with blanks as necessary, unless the user resets the preallocated card 
     * space with a call to {@link #ensureCardSpace(int)}.
     * </p>
     *
     * @param dis
     *            The input stream to read the data from.
     * @throws TruncatedFileException
     *             the the stream ended prematurely
     * @throws IOException
     *             if the operation failed
     *             
     * @see #ensureCardSpace(int)
     */
    @SuppressWarnings("deprecation")
    @Override
    public void read(ArrayDataInput dis) throws TruncatedFileException, IOException {
        // AK: Start afresh, in case the header had prior contents from before.
        clear();

        if (dis instanceof RandomAccess) {
            this.fileOffset = FitsUtil.findOffset(dis);
        } else {
            this.fileOffset = -1;
        }

        int trailingBlanks = 0;

        HeaderCardCountingArrayDataInput cardCountingArray = new HeaderCardCountingArrayDataInput(dis);
        try {
            for (;;) {
                HeaderCard fcard = new HeaderCard(cardCountingArray);

                // AK: Note, 'key' can never be null, as per contract of getKey(). So no need to check...
                String key = fcard.getKey();

                if (isEmpty()) {
                    checkFirstCard(key);
                } else if (fcard.isBlank()) {
                    // AK: We don't add the trailing blank cards, but keep count of them.
                    // (esp. in case the aren't trailing...) 
                    trailingBlanks++;
                    continue;
                } else if (END.key().equals(key)) {
                    addLine(fcard);
                    break; // Out of reading the header.
                } else if (LONGSTRN.key().equals(key)) {
                    // We don't check the value here. If the user
                    // wants to be sure that long strings are disabled,
                    // they can call setLongStringsEnabled(false) after
                    // reading the header.
                    FitsFactory.setLongStringsEnabled(true);
                }

                // AK: The preceding blank spaces were internal, not trailing
                // so add them back in now...
                for (int i = 0; i < trailingBlanks; i++) {
                    insertBlankCard();
                }
                trailingBlanks = 0;

                if (this.cards.containsKey(key)) {
                    addDuplicate(this.cards.get(key));
                }

                addLine(fcard);
            }
        } catch (EOFException e) {
            // Normal end-of-file before END key...
            throw e;   
        } catch (Exception e) { 
            if (isEmpty() && FitsFactory.getAllowTerminalJunk()) {
                // If this happened where we expect a new header to start, then
                // treat is as if end-of-file if terminal junk is allowed
                forceEOF("Junk detected at " + this.fileOffset + ".", e);
            } 
            if (e instanceof TruncatedFileException) {
                throw (TruncatedFileException) e;
            }
            throw new IOException("Invalid FITS Header" + (isEmpty() ? 
                    "" : ":\n\n --> Try FitsFactory.setAllowTerminalJunk(true) prior to reading to work around.\n"), e);
        }        

        if (this.fileOffset >= 0) {
            this.input = dis;
        }

        ensureCardSpace(cardCountingArray.getPhysicalCardsRead());
        readSize = FitsUtil.addPadding(this.minCards * HeaderCard.FITS_HEADER_CARD_SIZE);

        // Read to the end of the current FITS block.
        //
        try {
            dis.skipAllBytes(FitsUtil.padding(this.minCards * HeaderCard.FITS_HEADER_CARD_SIZE));
        } catch (EOFException e) {
            // No biggy. We got a complete header just fine, it's only that there was no
            // padding before EOF. We'll just log that, but otherwise keep going.
            LOG.log(Level.WARNING, "Premature end-of-file: no padding after header.", e);
        }

        // AK: Log if the file ends before the expected end-of-header position.
        if (Fits.checkTruncated(dis)) {
            // No biggy. We got a complete header just fine, it's only that there was no
            // padding before EOF. We'll just log that, but otherwise keep going.
            LOG.warning("Premature end-of-file: no padding after header.");
        }
    }

    /**
     * Forces an EOFException to be thrown when some other exception happened, essentially
     * treating the exception to force  a normal end the reading of the header.
     * 
     * @param message       the message to log.
     * @param cause         the exception encountered while reading the header
     * @throws EOFException the EOFException we'll throw instead.
     */
    private void forceEOF(String message, Exception cause) throws EOFException {
        LOG.log(Level.WARNING, message, cause);
        throw new EOFException("Forced EOF at " + this.fileOffset + " due to: " + message);
    }

    /**
     * Delete a key.
     *
     * @param key
     *            The header key.
     * @throws HeaderCardException
     *             if the operation failed
     * @deprecated see {@link #deleteKey(String)}
     */
    @Deprecated
    public void removeCard(String key) throws HeaderCardException {
        deleteKey(key);
    }

    /** Reset the file pointer to the beginning of the header */
    @Override
    public boolean reset() {
        try {
            FitsUtil.reposition(this.input, this.fileOffset);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Exception while repositioning " + this.input, e);
            return false;
        }
    }

    /**
     * @deprecated Use {@link #ensureCardSpace(int)} with a 1 argument instead.
     * 
     * <p>
     * Resets any prior preallocated header space, such as was explicitly set by
     * {@link #ensureCardSpace(int)}, or when the header was read from a stream 
     * to ensure it remains rewritable, if possible.
     * </p>
     * <p>
     * For headers read from a stream, this will affect {@link #rewriteable()}, 
     * so users should not call this method unless they do not intend to 
     * {@link #rewrite()} this header into the original FITS.
     * </p>
     * 
     * @see #ensureCardSpace(int)
     * @see #read(ArrayDataInput)
     * @see #getMinimumSize()
     * @see #rewriteable()
     * @see #rewrite()
     */
    @Deprecated
    public final void resetOriginalSize() {
        ensureCardSpace(1);
    }

    /** Rewrite the header. */
    @Override
    public void rewrite() throws FitsException, IOException {
        ArrayDataOutput dos = (ArrayDataOutput) this.input;

        if (rewriteable()) {
            FitsUtil.reposition(dos, this.fileOffset);
            write(dos);
            dos.flush();
        } else {
            throw new FitsException("Invalid attempt to rewrite Header.");
        }
    }

    @Override
    public boolean rewriteable() {
        int writeSize = FitsUtil.addPadding(Math.max(minCards, getNumberOfPhysicalCards()) * HeaderCard.FITS_HEADER_CARD_SIZE);
        return this.fileOffset >= 0 && this.input instanceof ArrayDataOutput && writeSize == getOriginalSize();
    }

    /**
     * @deprecated  Use the safer {@link #setBitpix(Bitpix)} instead.
     * 
     * Set the BITPIX value for the header. The following values are permitted
     * by FITS conventions:
     * <ul>
     * <li>8 -- signed byte data. Also used for tables.</li>
     * <li>16 -- signed short data.</li>
     * <li>32 -- signed int data.</li>
     * <li>64 -- signed long data.</li>
     * <li>-32 -- IEEE 32 bit floating point numbers.</li>
     * <li>-64 -- IEEE 64 bit floating point numbers.</li>
     * </ul>
     *
     * @param val
     *            The value set by the user.
     * @throws IllegalArgumentException     if the value is not a valid BITPIX value.
     *            
     * @see #setBitpix(Bitpix)
     */
    @Deprecated
    public void setBitpix(int val) throws IllegalArgumentException {
        try {
            setBitpix(Bitpix.forValue(val));
        } catch (FitsException e) {
            throw new IllegalArgumentException("Invalid BITPIX value: " + val, e);
        }
    }

    /**
     * Sets a standard BITPIX value for the header.
     * 
     * @param bitpix    The predefined enum value, e.g. {@link Bitpix#INTEGER}.
     * @since 1.16
     * 
     * @see #setBitpix(int)
     */
    public void setBitpix(Bitpix bitpix)  {
        Cursor<String, HeaderCard> iter = iterator();
        iter.next();
        iter.add(bitpix.getHeaderCard());
    }

    /**
     * Overwite the default header card sorter.
     *
     * @param headerSorter
     *            the sorter tu use or null to disable sorting
     */
    public void setHeaderSorter(Comparator<String> headerSorter) {
        this.headerSorter = headerSorter;
    }

    /**
     * Set the value of the NAXIS keyword
     *
     * @param val
     *            The dimensionality of the data.
     */
    public void setNaxes(int val) {
        Cursor<String, HeaderCard> iter = iterator();
        iter.setKey(BITPIX.key());
        if (iter.hasNext()) {
            iter.next();
        }
        iter.add(HeaderCard.create(NAXIS, val));
    }

    /**
     * Set the dimension for a given axis.
     *
     * @param axis
     *            The axis being set.
     * @param dim
     *            The dimension
     */
    public void setNaxis(int axis, int dim) {
        Cursor<String, HeaderCard> iter = iterator();
        if (axis <= 0) {
            LOG.warning("setNaxis ignored because axis less than 0");
            return;
        }
        if (axis == 1) {
            iter.setKey(NAXIS.key());
        } else if (axis > 1) {
            iter.setKey(NAXISn.n(axis - 1).key());
        }
        if (iter.hasNext()) {
            iter.next();
        }
        iter.add(HeaderCard.create(NAXISn.n(axis), dim));
    }

    /**
     * Set the SIMPLE keyword to the given value.
     *
     * @param val
     *            The boolean value -- Should be true for FITS data.
     */
    public void setSimple(boolean val) {
        deleteKey(SIMPLE);
        deleteKey(XTENSION);

        Cursor<String, HeaderCard> iter = iterator();

        // If we're flipping back to and from the primary header
        // we need to add in the EXTEND keyword whenever we become
        // a primary, because it's not permitted in the extensions
        // (at least not where it needs to be in the primary array).
        if (findCard(NAXIS) != null) {
            int nax = getIntValue(NAXIS);

            if (findCard(NAXISn.n(nax)) != null) {
                iter.next();
                deleteKey(EXTEND);
                iter.add(HeaderCard.create(EXTEND, true));
            }
        }

        iter.add(HeaderCard.create(SIMPLE, val));
    }

    /**
     * Set the XTENSION keyword to the given value.
     *
     * @param val
     *            The name of the extension.
     * @throws IllegalArgumentException     
     *                  if the string value contains characters that are not allowed in
     *                  FITS headers, that is characters outside of the 0x20 thru 0x7E
     *                  range.
     */
    public void setXtension(String val) throws IllegalArgumentException {
        deleteKey(SIMPLE);
        deleteKey(XTENSION);
        deleteKey(EXTEND);
        Cursor<String, HeaderCard> iter = iterator();
        iter.add(HeaderCard.create(XTENSION, val));
    }

    /**
     * @return the number of cards in the header
     * @deprecated use {@link #getNumberOfCards()}. The units of the size of the
     *             header may be unclear.
     */
    @Deprecated
    public int size() {
        return this.cards.size();
    }

    /**
     * Update a valued entry in the header, or adds a new header entry. If the header does not 
     * contain a prior entry for the specific keyword, or if the keyword is a comment-style key, 
     * a new entry is added at the current editing position. Otherwise, the matching existing
     * entry is updated in situ.
     *
     * @param key
     *            The key of the card to be replaced (or added).
     * @param card
     *            A new card
     * @throws HeaderCardException
     *             if the operation failed
     */
    public void updateLine(IFitsHeader key, HeaderCard card) throws HeaderCardException {
        if (key.valueType() != VALUE.NONE) {
            deleteKey(key);
        }
        cursor().add(card);
    }

    /**
     * Update an existing card in situ, without affecting the current position,
     * or else add a new card at the current position.
     *
     * @param key
     *            The key of the card to be replaced.
     * @param card
     *            A new card
     * @throws HeaderCardException
     *             if the operation failed
     */
    public final void updateLine(String key, HeaderCard card) throws HeaderCardException {
        // Remove an existing card with the matching 'key' (even if that key
        // isn't the same
        // as the key of the card argument!)
        this.cards.update(key, card);
    }

    /**
     * Overwrite the lines in the header. Add the new PHDU header to the current
     * one. If keywords appear twice, the new value and comment overwrite the
     * current contents. By Richard J Mathar.
     *
     * @param newHdr
     *            the list of new header data lines to replace the current ones.
     * @throws HeaderCardException
     *             if the operation failed
     */
    public void updateLines(final Header newHdr) throws HeaderCardException {
        Cursor<String, HeaderCard> j = newHdr.iterator();

        while (j.hasNext()) {
            HeaderCard card = j.next();
            if (card.isCommentStyleCard()) {
                insertCommentStyle(card.getKey(), card.getComment());
            } else {
                updateLine(card.getKey(), card);
            }
        }
    }

    /**
     * Writes a number of blank header records, for example to create preallocated
     * blank header space as described by the FITS 4.0 standard.
     * 
     * @param dos       the output stream to which the data is to be written.
     * @param n         the number of blank records to add.
     * @throws IOException  if there was an error writing to the stream
     * 
     * @since 1.16
     * 
     * @see #ensureCardSpace(int)
     */
    private void writeBlankCards(ArrayDataOutput dos, int n) throws IOException {
        byte[] blank = new byte[HeaderCard.FITS_HEADER_CARD_SIZE];
        Arrays.fill(blank, (byte) ' ');

        while (--n >= 0) {
            dos.write(blank);
        }
    }

    /**
     * Add required keywords, and removes conflicting ones depending on whether it is designated
     * as a primary header or not.
     * 
     * @param isPrimary         <code>true</code> if this is to be a primary header, otherwise <code>false</code>
     * @throws FitsException    if there was an error trying to edit the header.
     * 
     * @since 1.17
     * 
     * @see #validate(FitsOutput)
     */
    void editRequiredKeys(boolean isPrimary) throws FitsException {

        if (isPrimary) {
            // Delete keys that cannot be in primary
            deleteKey(XTENSION);

            // Some FITS readers don't like the PCOUNT and GCOUNT keywords in the primary header
            if (!getBooleanValue(GROUPS, false)) {
                deleteKey(PCOUNT);
                deleteKey(GCOUNT);
            }

            // Make sure we have SIMPLE
            addValue(SIMPLE, true);
        } else {
            // Delete keys that cannot be in extensions
            deleteKey(SIMPLE);

            // Some FITS readers don't like the EXTEND keyword in extensions.
            deleteKey(EXTEND);

            // Make sure we have XTENSION
            addValue(XTENSION, getStringValue(XTENSION, "UNKNOWN"));
        }

        // Make sure we have BITPIX
        addValue(BITPIX, getIntValue(BITPIX, Bitpix.VALUE_FOR_INT));

        int naxes = getIntValue(NAXIS, 0);
        addValue(NAXIS, naxes);

        for (int i = 1; i <= naxes; i++) {
            IFitsHeader naxisi = NAXISn.n(i);
            addValue(naxisi, getIntValue(naxisi, 1));
        }

        if (isPrimary) {
            addValue(EXTEND, true);
        } else {
            addValue(PCOUNT, getIntValue(PCOUNT, 0));
            addValue(GCOUNT, getIntValue(GCOUNT, 1));
        }
    }

    /**
     * <p>
     * Validates this header by making it a proper primary or extension header. In both cases it means adding
     * required keywords if missing, and removing conflicting cards. Then ordering is checked and
     * corrected as necessary and ensures that the <code>END</code> card is at the tail. 
     * 
     * 
     * @param asPrimary         <code>true</code> if this header is to be a primary FITS header
     * @throws FitsException    If there was an issue getting the header into proper form.
     * 
     * @since 1.17
     */
    public void validate(boolean asPrimary) throws FitsException {
        editRequiredKeys(asPrimary);
        validate();
    }   

    /**
     * Validates the header making sure it has the required keywords and that the essential
     * keywords appeat in the in the required order
     * 
     * @throws FitsException    If there was an issue getting the header into proper form.
     */
    private void validate() throws FitsException {
        // Ensure that all cards are in the proper order.
        if (this.headerSorter != null) {
            this.cards.sort(this.headerSorter);
        }
        checkBeginning();
        checkEnd();
    }

    /**
     * Write the current header (including any needed padding) to the output
     * stream.
     *
     * @param dos
     *            The output stream to which the data is to be written.
     * @throws FitsException
     *             if the header could not be written.
     *             
     */
    @Override
    public void write(ArrayDataOutput dos) throws FitsException {
        validate();

        FitsSettings settings = FitsFactory.current();
        this.fileOffset = FitsUtil.findOffset(dos);

        Cursor<String, HeaderCard> writeIterator = this.cards.iterator(0);
        try {
            int size = 0;

            while (writeIterator.hasNext()) {
                HeaderCard card = writeIterator.next();
                byte[] b = AsciiFuncs.getBytes(card.toString(settings));
                size += b.length;

                if (END.key().equals(card.getKey()) && minCards * HeaderCard.FITS_HEADER_CARD_SIZE > size) {
                    // AK: Add preallocated blank header space before the END key.
                    writeBlankCards(dos, minCards - size / HeaderCard.FITS_HEADER_CARD_SIZE);
                    size = minCards;
                }

                dos.write(b);
            }
            FitsUtil.pad(dos, size, (byte) ' ');
            dos.flush();
        } catch (IOException e) {
            throw new FitsException("IO Error writing header", e);
        }
    }

    private void addDuplicate(HeaderCard dup) { 
        // AK: Don't worry about duplicates for comment-style cards in general.
        if (dup.isCommentStyleCard()) {
            return;
        }

        if (duplicates == null) {
            duplicates = new ArrayList<>();
            dupKeys = new HashSet<>();
        }

        if (!dupKeys.contains(dup.getKey())) {
            HeaderCardParser.getLogger().log(Level.WARNING, "Multiple occurrences of key:" + dup.getKey());
            dupKeys.add(dup.getKey());
        }

        duplicates.add(dup);
    }

    /**
     * Check if the given key is the next one available in the header.
     */
    private void cardCheck(Cursor<String, HeaderCard> iter, IFitsHeader key) throws FitsException {
        cardCheck(iter, key.key());
    }

    /**
     * Check if the given key is the next one available in the header.
     */
    private void cardCheck(Cursor<String, HeaderCard> iter, String key) throws FitsException {
        if (!iter.hasNext()) {
            throw new FitsException("Header terminates before " + key);
        }
        HeaderCard card = iter.next();
        if (!card.getKey().equals(key)) {
            throw new FitsException("Key " + key + " not found where expected." + "Found " + card.getKey());
        }
    }

    private void checkFirstCard(String key) throws FitsException {
        // AK: key cannot be null by the caller already, so checking for it makes dead code.
        if (!SIMPLE.key().equals(key) && !XTENSION.key().equals(key)) {
            throw new FitsException("Not a proper FITS header: " + HeaderCard.sanitize(key) + " at " + this.fileOffset);
        }
    }

    private void doCardChecks(Cursor<String, HeaderCard> iter, boolean isTable, boolean isExtension) throws FitsException {
        cardCheck(iter, BITPIX);
        cardCheck(iter, NAXIS);
        int nax = getIntValue(NAXIS);

        for (int i = 1; i <= nax; i += 1) {
            cardCheck(iter, NAXISn.n(i));
        }
        if (isExtension) {
            cardCheck(iter, PCOUNT);
            cardCheck(iter, GCOUNT);
            if (isTable) {
                cardCheck(iter, TFIELDS);
            }
        }
        // This does not check for the EXTEND keyword which
        // if present in the primary array must immediately follow
        // the NAXISn.
    }

    /**
     * Move after the EXTEND keyword in images. Used in bug fix noted by V.
     * Forchi
     */
    void afterExtend() {
        if (findCard(EXTEND) != null) {
            nextCard();
        }
    }

    /**
     * Ensure that the header begins with a valid set of keywords. Note that we
     * do not check the values of these keywords.
     */
    void checkBeginning() throws FitsException {
        Cursor<String, HeaderCard> iter = iterator();
        if (!iter.hasNext()) {
            throw new FitsException("Empty Header");
        }
        HeaderCard card = iter.next();
        String key = card.getKey();
        if (!key.equals(SIMPLE.key()) && !key.equals(XTENSION.key())) {
            throw new FitsException("No SIMPLE or XTENSION at beginning of Header");
        }
        boolean isTable = false;
        boolean isExtension = false;
        if (key.equals(XTENSION.key())) {
            String value = card.getValue();
            if (value == null || value.isEmpty()) {
                throw new FitsException("Empty XTENSION keyword");
            }
            isExtension = true;
            if (value.equals(XTENSION_BINTABLE) || value.equals("A3DTABLE") || value.equals("TABLE")) {
                isTable = true;
            }
        }
        doCardChecks(iter, isTable, isExtension);

        Bitpix.fromHeader(this, false);
    }

    /**
     * Ensure that the header has exactly one END keyword in the appropriate
     * location.
     */
    void checkEnd() {
        // Ensure we have an END card only at the end of the
        // header.
        Cursor<String, HeaderCard> iter = iterator();

        HeaderCard card;

        while (iter.hasNext()) {
            card = iter.next();
            if (!card.isKeyValuePair() && card.getKey().equals(END.key())) {
                iter.remove();
            }
        }
        // End cannot have a comment

        try {
            iter.add(HeaderCard.createCommentStyleCard(END.key(), null));
        } catch (HeaderCardException e) {
            // Cannot happen.
        }

    }

    /**
     * Return the size of the header data including padding, or 0 if the header is invalid.
     *
     * @return the header size including any needed padding, or 0 if the header is invalid.
     * 
     * @see #isValidHeader()
     */
    int headerSize() {
        if (!isValidHeader()) {
            return 0;
        }

        return FitsUtil.addPadding(Math.max(minCards, getNumberOfPhysicalCards()) * HeaderCard.FITS_HEADER_CARD_SIZE);
    }

    /**
     * Is this a valid header.
     *
     * @return <CODE>true</CODE> for a valid header, <CODE>false</CODE>
     *         otherwise.
     */
    boolean isValidHeader() {
        if (getNumberOfCards() < MIN_NUMBER_OF_CARDS_FOR_VALID_HEADER) {
            return false;
        }
        Cursor<String, HeaderCard> iter = iterator();
        String key = iter.next().getKey();
        if (!key.equals(SIMPLE.key()) && !key.equals(XTENSION.key())) {
            return false;
        }
        key = iter.next().getKey();
        if (!key.equals(BITPIX.key())) {
            return false;
        }
        key = iter.next().getKey();
        if (!key.equals(NAXIS.key())) {
            return false;
        }
        while (iter.hasNext()) {
            key = iter.next().getKey();
        }
        return key.equals(END.key());
    }

    /**
     * Create a header for a null image.
     */
    void nullImage() {
        Cursor<String, HeaderCard> iter = iterator();
        iter.add(HeaderCard.create(SIMPLE, true));
        iter.add(Bitpix.BYTE.getHeaderCard());
        iter.add(HeaderCard.create(NAXIS, 0));
        iter.add(HeaderCard.create(EXTEND, true));
    }

    /**
     * Find the end of a set of keywords describing a column or axis (or
     * anything else terminated by an index. This routine leaves the header
     * ready to add keywords after any existing keywords with the index
     * specified. The user should specify a prefix to a keyword that is
     * guaranteed to be present.
     */
    Cursor<String, HeaderCard> positionAfterIndex(IFitsHeader prefix, int col) {
        String colnum = String.valueOf(col);
        cursor().setKey(prefix.n(col).key());
        if (cursor().hasNext()) {
            // Bug fix (references to forward) here by Laurent Borges
            boolean toFar = false;
            while (cursor().hasNext()) {
                String key = cursor().next().getKey().trim();
                // AK: getKey() cannot return null so no need to check.
                if (key.length() <= colnum.length() || !key.substring(key.length() - colnum.length()).equals(colnum)) {
                    toFar = true;
                    break;
                }
            }
            if (toFar) {
                cursor().prev(); // Gone one too far, so skip back an element.
            }
        }
        return cursor();
    }

    /**
     * Replace the key with a new key. Typically this is used when deleting or
     * inserting columns. If the convention of the new keyword is not compatible with the existing value 
     * a warning message is logged but no exception is thrown (at this point).
     *
     * @param oldKey
     *            The old header keyword.
     * @param newKey
     *            the new header keyword.
     * @return <CODE>true</CODE> if the card was replaced.
     * @throws HeaderCardException
     *                If <CODE>newKey</CODE> is not a valid FITS keyword.           
     */
    boolean replaceKey(IFitsHeader oldKey, IFitsHeader newKey) throws HeaderCardException {

        if (oldKey.valueType() == VALUE.NONE) {
            throw new IllegalArgumentException("cannot replace comment-style " + oldKey.key());
        }

        HeaderCard card = findCard(oldKey);
        VALUE newType = newKey.valueType();

        if (card != null && oldKey.valueType() != newType && newType != VALUE.ANY) {
            Class<?> type = card.valueType();
            Exception e = null;

            // Check that the exisating cards value is compatible with the expected type of the new key.
            if (newType == VALUE.NONE) {
                e = new IllegalArgumentException("comment-style " + newKey.key() + " cannot replace valued key " + oldKey.key());
            } else if (Boolean.class.isAssignableFrom(type) && newType != VALUE.LOGICAL) {
                e = new IllegalArgumentException(newKey.key() + " cannot not support the existing boolean value.");
            } else if (String.class.isAssignableFrom(type) && newType != VALUE.STRING) {
                e = new IllegalArgumentException(newKey.key() + " cannot not support the existing string value.");
            } else if (ComplexValue.class.isAssignableFrom(type) && newType != VALUE.COMPLEX) {
                e = new IllegalArgumentException(newKey.key() + " cannot not support the existing complex value.");
            } else if (card.isDecimalType() && newType != VALUE.REAL && newType != VALUE.COMPLEX) {
                e = new IllegalArgumentException(newKey.key() + " cannot not support the existing decimal values.");
            } else if (Number.class.isAssignableFrom(type) && newType != VALUE.REAL && newType != VALUE.INTEGER && newType != VALUE.COMPLEX) {
                e = new IllegalArgumentException(newKey.key() + " cannot not support the existing numerical value.");
            }

            if (e != null) {
                LOG.log(Level.WARNING, e.getMessage(), e);
            }
        }

        return replaceKey(oldKey.key(), newKey.key());
    }

    /**
     * Replace the key with a new key. Typically this is used when deleting or
     * inserting columns so that TFORMx -> TFORMx-1
     *
     * @param oldKey
     *            The old header keyword.
     * @param newKey
     *            the new header keyword.
     * @return <CODE>true</CODE> if the card was replaced.
     * @exception HeaderCardException
     *                If <CODE>newKey</CODE> is not a valid FITS keyword.
     */
    boolean replaceKey(String oldKey, String newKey) throws HeaderCardException {
        HeaderCard oldCard = findCard(oldKey);
        if (oldCard == null) {
            return false;
        }
        if (!this.cards.replaceKey(oldKey, newKey)) {
            throw new HeaderCardException("Duplicate key [" + newKey + "] in replace");
        }
        try {
            oldCard.changeKey(newKey);
        } catch (IllegalArgumentException e) {
            throw new HeaderCardException("New key [" + newKey + "] is invalid or too long for existing value.", e);
        }
        return true;
    }

    /**
     * Calculate the unpadded size of the data segment from the header
     * information.
     *
     * @return the unpadded data segment size.
     */
    long trueDataSize() {

        // AK: No need to be too strict here. We can get a data size even if the
        // header isn't 100% to spec,
        // as long as the necessary keys are present. So, just check for the
        // required keys, and no more...
        if (!containsKey(BITPIX.key())) {
            return 0L;
        }

        if (!containsKey(NAXIS.key())) {
            return 0L;
        }

        int naxis = getIntValue(NAXIS, 0);

        // If there are no axes then there is no data.
        if (naxis == 0) {
            return 0L;
        }

        int[] axes = new int[naxis];

        for (int axis = 1; axis <= naxis; axis += 1) {
            axes[axis - 1] = getIntValue(NAXISn.n(axis), 0);
        }

        boolean isGroup = getBooleanValue(GROUPS, false);

        int pcount = getIntValue(PCOUNT, 0);
        int gcount = getIntValue(GCOUNT, 1);

        int startAxis = 0;

        if (isGroup && naxis > 1 && axes[0] == 0) {
            startAxis = 1;
        }

        long size = 1;
        for (int i = startAxis; i < naxis; i += 1) {
            size *= axes[i];
        }

        size += pcount;
        size *= gcount;

        // Now multiply by the number of bits per pixel and
        // convert to bytes.
        size *= Math.abs(getIntValue(BITPIX, 0)) / FitsIO.BITS_OF_1_BYTE;

        return size;
    }

    /**
     * <p>
     * Sets whether warnings about FITS standard violations are logged when a header is being read (parsed).
     * Enabling this feature can help identifying various standard violations in existing FITS headers,
     * which nevertheless do not prevent the successful reading of the header by this library. 
     * </p>
     * <p>
     * If {@link FitsFactory#setAllowHeaderRepairs(boolean)} is set <code>false</code>, this will affect
     * only minor violations (e.g. a misplaced '=', missing space after '=', non-standard characters
     * in header etc.), which nevertheless do not interfere with the unamiguous parsing of the header
     * information. More severe standard violations, where some guessing may be required about the
     * intent of some malformed header record, will throw appropriate exceptions. If, however,
     * {@link FitsFactory#setAllowHeaderRepairs(boolean)} is set <code>true</code>, the 
     * parsing will throw fewer exceptions, and the additional issues may get logged as 
     * additional warning instead.
     * 
     * @param value     <code>true</code> if parser warnings about FITS standard violations when reading in
     *                  existing FITS headers are to be logged, otherwise <code>false</code>
     * 
     * @see #isParserWarningsEnabled()
     * @see FitsFactory#setAllowHeaderRepairs(boolean)
     * 
     * @since 1.16
     */
    public static void setParserWarningsEnabled(boolean value) {
        Level level = value ? Level.WARNING : Level.SEVERE;
        HeaderCardParser.getLogger().setLevel(level);
        Logger.getLogger(ComplexValue.class.getName()).setLevel(level);
    }

    /**
     * Checks whether warnings about FITS standard violations are logged when a header is being read 
     * (parsed).
     * 
     * @return      <code>true</code> if parser warnings about FITS standard violations when reading in
     *              existing FITS headers are enabled and logged, otherwise <code>false</code>
     *              
     * @see #setParserWarningsEnabled(boolean)
     * 
     * @since 1.16
     */
    public static boolean isParserWarningsEnabled() {        
        return !HeaderCardParser.getLogger().getLevel().equals(Level.SEVERE);
    }

    /**
     * Returns the current preferred alignment character position of inline header comments.
     * This is the position at which the '/' is placed for the inline comment.
     * 
     * @return  The current alignment position for inline comments.
     * 
     * @see #setCommentAlignPosition(int)
     */
    public static int getCommentAlignPosition() {
        return commentAlign;
    }

    /**
     * Sets a new alignment position for inline header comments.
     * 
     * 
     * @param pos   [20:70] The character position to which inline comments should be aligned if possible.
     * @throws IllegalArgumentException     if the position is outside of the allowed range.
     * 
     * @see #getCommentAlignPosition()
     */
    public static void setCommentAlignPosition(int pos) throws IllegalArgumentException {
        if (pos < Header.MIN_COMMENT_ALIGN || pos > Header.MAX_COMMENT_ALIGN) {
            throw new IllegalArgumentException("Comment alignment " + pos + " out of range (" + MIN_COMMENT_ALIGN + ":" + MAX_COMMENT_ALIGN + ").");
        }
        commentAlign = pos;
    }
}
