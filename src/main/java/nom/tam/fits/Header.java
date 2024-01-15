package nom.tam.fits;

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
import nom.tam.fits.header.Standard;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.ComplexValue;
import nom.tam.util.Cursor;
import nom.tam.util.FitsIO;
import nom.tam.util.FitsInputStream;
import nom.tam.util.FitsOutput;
import nom.tam.util.HashedList;
import nom.tam.util.RandomAccess;

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

/**
 * <p>
 * Access and manipulate the header of a HDU. FITS headers serve more than a single purpose:
 * </p>
 * <ol>
 * <li>provide an essential description of the type, size, and layout of the HDUs data segment</li>
 * <li>describe the data as completely as possible via standardized (or conventional) keywords</li>
 * <li>provide storage for additional user-specific key / value pairs</li>
 * <li>allow for comments to aid human readability</li>
 * </ol>
 * <p>
 * First and foremost headers provide a description of the data object that follows the header in the HDU. Some of that
 * description is essential and critical to the integrity of the FITS file, such as the header keywords that describe
 * the type, size, and layout of the data segment. This library will automatically populate the header with appropriate
 * information using the mandatory keywords (such as <code>SIMPLE</code> or <code>XTENSION</code>, <code>BITPIX</code>,
 * <code>NAXIS</code>, <code>NAXIS</code><i>n</i>, <code>PCOUNT</code>, <code>GCOUNT</code> keywords, as well as
 * essential table column format descriptions). Users of the library should avoid overwriting these mandatory keywords
 * manually, since they may corrupt the FITS file, rendering it unreadable.
 * </p>
 * <p>
 * Beyond the keywords that describe the type, shape, and size of data, the library will not add further information to
 * the header. The users of the library are responsible to complete the header description as necessary. This includes
 * non-enssential data descriptions (such as <code>EXTNAME</code>, <code>BUNIT</code>, <code>OBSERVER</code>, or
 * optional table column descriptors <code>TTYPE</code><i>n</i>, <code>TUNIT</code><i>n</i>, coordinate systems via the
 * appropriate WCS keywords, or checksums). Users of the library are responsible for completing the data description
 * using whatever standard or conventional keywords are available and appropriate. Please refer to the
 * <a href="https://fits.gsfc.nasa.gov/fits_standard.html">FITS Standard</a> documentation to see what typical
 * descriptions of data you might want to use.
 * </p>
 * <p>
 * Last but not least, the header is also a place where FITS creators can store (nearly) arbitrary key/value pairs. In
 * earlier versions of the FITS standard, header keywords were restricted to max. 8 upper case letters and numbers (plus
 * hyphen and underscore), and no more than 70 character value fields. However, as of FITS 4.0 (and even before as a
 * registered convention), string values of arbitrary length may be stored using the OGIP 1.0 long string convention,
 * while the ESO <a href="https://fits.gsfc.nasa.gov/registry/hierarch_keyword.html">HIERARCH convention</a> allows
 * keywords with more than 8 characters and hierarchical keywords. Support, conformance, and compliance to these
 * conventions can be toggled by static settings in {@link FitsFactory} to user preference.
 * </p>
 * <p>
 * As of version 1.16, we also support reserving space in headers for future additions using the
 * {@link #ensureCardSpace(int)} method, also part of the FITS 4.0 standard. It allows users to finish populating
 * headers <i>after</i> data that follows the header is already written -- a useful feature for recording data from
 * streaming sources.
 * </p>
 */
@SuppressWarnings("deprecation")
public class Header implements FitsElement {

    /**
     * The default character position to which comments should be aligned if possible (zero-based). The fITS standard
     * requires that 'fixed-format' values are right-justified to byte 30 (index 29 in Java), and recommends a space
     * after that before the comment. As such, comments should normally start at byte 30 (counted from 0). (We will add
     * a space at that position before the '/' indicating the comment start)
     */
    public static final int DEFAULT_COMMENT_ALIGN = 30;

    /**
     * The earliest position (zero-based) at which a comment may start for a regular key/value entry.
     * 
     * @deprecated We will disable changing alignment in the future because it may violate the standard for
     *                 'fixed-format' header entries, and result in files that are unreadable by some other software.
     *                 This constant will be obsoleted and removed.
     */
    public static final int MIN_COMMENT_ALIGN = 20;

    /**
     * The largest (zero-based) comment alignment allowed that can still contain some meaningful comment (word)
     * 
     * @deprecated We will disable changing alignment in the future because it may violate the standard for
     *                 'fixed-format' header entries, and result in files that are unreadable by some other software.
     *                 This constant will be obsoleted and removed.
     */
    public static final int MAX_COMMENT_ALIGN = 70;

    /**
     * The alignment position of card comments for a more pleasing visual experience. Comments will be aligned to this
     * position, provided the lengths of all fields allow for it.
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
     * The mimimum number of cards to write, including blank header space as described in the FITS 4.0 standard.
     */
    private int minCards;

    /**
     * The number of bytes that this header occupied in file. (for re-writing).
     */
    private long readSize;

    /** The checksum calculated from the input stream */
    private long streamSum = -1L;

    /**
     * the sorter used to sort the header cards defore writing the header.
     */
    private Comparator<String> headerSorter;

    private BasicHDU<?> owner;

    /**
     * Keyword checking mode when adding standardized keywords via the {@link IFitsHeader} interface.
     * 
     * @author Attila Kovacs
     * 
     * @since  1.19
     */
    public enum KeywordCheck {
        /** No keyword checking will be performed. */
        NONE,
        /** Check only that the keyword is appropriate for the type of data contained in the associated HDU */
        DATA_TYPE,
        /**
         * Strict checking, will refuse to set mandatory FITS keywords -- which should normally be set by the library
         * alone.
         */
        STRICT
    }

    /**
     * The keyword checking mode used by the library until the user changes it it.
     *
     * @since 1.19
     */
    public static final KeywordCheck DEFAULT_KEYWORD_CHECK_POLICY = KeywordCheck.DATA_TYPE;

    private static KeywordCheck defaultKeyCheck = DEFAULT_KEYWORD_CHECK_POLICY;

    private KeywordCheck keyCheck = defaultKeyCheck;

    /**
     * Create a header by reading the information from the input stream.
     *
     * @param  dis                    The input stream to read the data from.
     *
     * @return                        <CODE>null</CODE> if there was a problem with the header; otherwise return the
     *                                    header read from the input stream.
     *
     * @throws TruncatedFileException if the stream ended prematurely
     * @throws IOException            if the header could not be read.
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
     * @param flag the new value for long-string enabling.
     */
    @Deprecated
    public static void setLongStringsEnabled(boolean flag) {
        FitsFactory.setLongStringsEnabled(flag);
    }

    /** Create a new header with the required default keywords for a standalone header. */
    public Header() {
        cards = new HashedList<>();
        headerSorter = new HeaderOrder();
        duplicates = null;
        clear();
    }

    /**
     * Create a header and populate it from the input stream
     *
     * @param  is                     The input stream where header information is expected.
     *
     * @throws IOException            if the header could not be read.
     * @throws TruncatedFileException if the stream ended prematurely
     */
    public Header(ArrayDataInput is) throws TruncatedFileException, IOException {
        this();
        read(is);
    }

    /**
     * Create a header which points to the given data object.
     *
     * @param  o             The data object to be described.
     *
     * @throws FitsException if the data was not valid for this header.
     */
    public Header(Data o) throws FitsException {
        this();
        o.fillHeader(this);
    }

    /**
     * Create a header and initialize it with a vector of strings.
     *
     * @param newCards Card images to be placed in the header.
     */
    public Header(String[] newCards) {
        this();
        for (String newCard : newCards) {
            cards.add(HeaderCard.create(newCard));
        }
    }

    void assignTo(BasicHDU<?> hdu) {
        // if (owner != null) {
        // throw new IllegalStateException("This header was already assigned to a HDU");
        // }
        this.owner = hdu;
    }

    /**
     * <p>
     * Reserves header card space for populating at a later time. When written to a stream, the header will be large
     * enough to hold at least the specified number of cards. If the header has fewer physical cards then the remaining
     * space will be padded with blanks, leaving space for future additions, as specified by the FITS 4.0 standard for
     * <a href="https://fits.gsfc.nasa.gov/registry/headerspace.html"> preallocated header space</a>.
     * </p>
     * <p>
     * This method is also called by {@link #read(ArrayDataInput)}, with the number of cards (including reserved blank
     * space) contained in the header input stream, in order to ensure that the header remains rewritable even if it is
     * shortened by the removal of cards (explicitly, or because they were duplicates).
     * </p>
     * <p>
     * A new setting always overrides prior ones. For example, calling this method with an argument that is %lt;=1 will
     * eliminate (reset) any prior preallocated header space.
     * </p>
     *
     * @param nCards the mimimum number of 80-character header records that is header must be able to support when
     *                   written to a stream, including preallocated blank header space.
     *
     * @since        1.16
     *
     * @see          #getMinimumSize()
     * @see          #write(ArrayDataOutput)
     * @see          #read(ArrayDataInput)
     * @see          #resetOriginalSize()
     */
    public void ensureCardSpace(int nCards) {
        if (nCards < 1) {
            nCards = 1;
        }
        minCards = nCards;
    }

    /**
     * Merges copies of all cards from another header, provided they are not readily present in this header. That is, it
     * merges only the non-conflicting or distinct header entries from the designated source (in contrast to
     * {@link #updateLines(Header)}). All comment cards are merged also (since these can always appear multiple times,
     * so they do not conflict). The merged entries are added at the end of the header, in the same order as they appear
     * in the source. The merged entries will be copies of the cards in the original, such that subsequent modifications
     * to the source will not affect this header or vice versa.
     * 
     * @param source The header from which to inherit non-conflicting entries
     * 
     * @since        1.19
     * 
     * @see          #updateLines(Header)
     */
    public void mergeDistinct(Header source) {
        seekTail();

        Cursor<String, HeaderCard> c = source.iterator();
        while (c.hasNext()) {
            HeaderCard card = c.next();
            if (card.isCommentStyleCard() || !containsKey(card.getKey())) {
                if (card.getKey().equals(Standard.SIMPLE.key()) || card.getKey().equals(Standard.XTENSION.key())) {
                    // Do not merge SIMPLE / XTENSION -- these are private matters...
                    continue;
                }
                addLine(card.copy());
            }
        }
    }

    /**
     * Insert a new header card at the current position, deleting any prior occurence of the same card while maintaining
     * the current position to point to after the newly inserted card.
     *
     * @param  fcard                    The card to be inserted.
     * 
     * @throws IllegalArgumentException if the current keyword checking mode does not allow the headercard with its
     *                                      standard keyword in the header.
     * 
     * @see                             #setKeywordChecking(KeywordCheck)
     */
    public void addLine(HeaderCard fcard) throws IllegalArgumentException {
        if (fcard == null) {
            return;
        }

        if (fcard.getStandardKey() != null) {
            checkKeyword(fcard.getStandardKey());
        }

        cursor().add(fcard);
    }

    /**
     * <p>
     * Sets the built-in standard keyword checking mode. When populating the header using {@link IFitsHeader} keywords
     * the library will check if the given keyword is appropriate for the type of HDU that the header represents, and
     * will throw an {@link IllegalArgumentException} if the specified keyword is not allowed for that type of HDU.
     * </p>
     * <p>
     * This method changes the keyword checking mode for this header instance only. If you want to change the mode for
     * all newly created headers globally, use {@link #setDefaultKeywordChecking(KeywordCheck)} instead.
     * </p>
     * 
     * @param mode The keyword checking mode to use.
     * 
     * @see        #getKeywordChecking()
     * @see        HeaderCard#setValueCheckingPolicy(nom.tam.fits.HeaderCard.ValueCheck)
     * 
     * @since      1.19
     */
    public void setKeywordChecking(KeywordCheck mode) {
        keyCheck = mode;
    }

    /**
     * Sets the default mode of built-in standard keyword checking mode for new headers. When populating the header
     * using {@link IFitsHeader} keywords the library will check if the given keyword is appropriate for the type of HDU
     * that the header represents, and will throw an {@link IllegalArgumentException} if the specified keyword is not
     * allowed for that type of HDU.
     * 
     * @param mode The keyword checking policy to use.
     * 
     * @see        #setKeywordChecking(KeywordCheck)
     * @see        #getKeywordChecking()
     * @see        HeaderCard#setValueCheckingPolicy(nom.tam.fits.HeaderCard.ValueCheck)
     * 
     * @since      1.19
     */
    public static void setDefaultKeywordChecking(KeywordCheck mode) {
        defaultKeyCheck = mode;
    }

    /**
     * Returns the current keyword checking mode.
     * 
     * @return the current keyword checking mode
     * 
     * @see    #setKeywordChecking(KeywordCheck)
     * 
     * @since  1.19
     */
    public final KeywordCheck getKeywordChecking() {
        return keyCheck;
    }

    private void checkKeyword(IFitsHeader keyword) throws IllegalArgumentException {
        if (keyCheck == KeywordCheck.NONE || owner == null) {
            return;
        }

        if (keyCheck == KeywordCheck.STRICT
                && (keyword.status() == IFitsHeader.SOURCE.MANDATORY || keyword.status() == IFitsHeader.SOURCE.INTEGRAL)) {
            throw new IllegalArgumentException("Keyword " + keyword + " should be set by the library only");
        }

        switch (keyword.hdu()) {

        case PRIMARY:
            if (!owner.canBePrimary()) {
                throw new IllegalArgumentException(
                        "Keyword " + keyword + " is a primary keyword and may not be used in extensions");
            }
            return;
        case EXTENSION:
            if (owner instanceof RandomGroupsHDU) {
                throw new IllegalArgumentException(
                        "Keyword " + keyword + " is an extension keyword but random groups may only be primary");
            }
            return;
        case IMAGE:
            if (owner instanceof ImageHDU || owner instanceof RandomGroupsHDU) {
                return;
            }
            break;
        case GROUPS:
            if (owner instanceof RandomGroupsHDU) {
                return;
            }
            break;
        case TABLE:
            if (owner instanceof TableHDU) {
                return;
            }
            break;
        case ASCII_TABLE:
            if (owner instanceof AsciiTableHDU) {
                return;
            }
            break;
        case BINTABLE:
            if (owner instanceof BinaryTableHDU) {
                return;
            }
            break;
        default:
            return;
        }

        throw new IllegalArgumentException(
                "Keyword " + keyword.key() + " is not appropriate for " + owner.getClass().getName());
    }

    /**
     * Add or replace a key with the given boolean value and its standardized comment. If the value is not compatible
     * with the convention of the keyword, a warning message is logged but no exception is thrown (at this point). The
     * new card will be placed at the current mark position, as set e.g. by {@link #findCard(IFitsHeader)}.
     *
     * @param  key                      The header key.
     * @param  val                      The boolean value.
     *
     * @return                          the new card that was added.
     *
     * @throws HeaderCardException      If the parameters cannot build a valid FITS card.
     * @throws IllegalArgumentException If the keyword is invalid
     *
     * @see                             #addValue(String, Boolean, String)
     */
    public HeaderCard addValue(IFitsHeader key, Boolean val) throws HeaderCardException, IllegalArgumentException {
        HeaderCard card = HeaderCard.create(key, val);
        addLine(card);
        return card;
    }

    /**
     * Add or replace a key with the given double value and its standardized comment. If the value is not compatible
     * with the convention of the keyword, a warning message is logged but no exception is thrown (at this point). The
     * new card will be placed at the current mark position, as set e.g. by {@link #findCard(IFitsHeader)}.
     *
     * @param  key                      The header key.
     * @param  val                      The double value.
     *
     * @return                          the new card that was added.
     *
     * @throws HeaderCardException      If the parameters cannot build a valid FITS card.
     * @throws IllegalArgumentException If the keyword is invalid
     *
     * @see                             #addValue(String, Number, String)
     */
    public HeaderCard addValue(IFitsHeader key, Number val) throws HeaderCardException, IllegalArgumentException {
        HeaderCard card = HeaderCard.create(key, val);
        addLine(card);
        return card;
    }

    /**
     * Add or replace a key with the given string value and its standardized comment. If the value is not compatible
     * with the convention of the keyword, a warning message is logged but no exception is thrown (at this point). The
     * new card will be placed at the current mark position, as set e.g. by {@link #findCard(IFitsHeader)}.
     *
     * @param  key                      The header key.
     * @param  val                      The string value.
     *
     * @return                          the new card that was added.
     *
     * @throws HeaderCardException      If the parameters cannot build a valid FITS card.
     * @throws IllegalArgumentException If the keyword is invalid
     *
     * @see                             #addValue(String, String, String)
     */
    public HeaderCard addValue(IFitsHeader key, String val) throws HeaderCardException, IllegalArgumentException {
        HeaderCard card = HeaderCard.create(key, val);
        addLine(card);
        return card;
    }

    /**
     * Add or replace a key with the given complex value and its standardized comment. If the value is not compatible
     * with the convention of the keyword, a warning message is logged but no exception is thrown (at this point). The
     * new card will be placed at the current mark position, as set e.g. by {@link #findCard(IFitsHeader)}.
     *
     * @param  key                      The header key.
     * @param  val                      The complex value.
     *
     * @return                          the new card that was added.
     *
     * @throws HeaderCardException      If the parameters cannot build a valid FITS card.
     * @throws IllegalArgumentException If the keyword is invalid
     *
     * @see                             #addValue(String, ComplexValue, String)
     *
     * @since                           1.17
     */
    public HeaderCard addValue(IFitsHeader key, ComplexValue val) throws HeaderCardException, IllegalArgumentException {
        HeaderCard card = HeaderCard.create(key, val);
        addLine(card);
        return card;
    }

    /**
     * Add or replace a key with the given boolean value and comment. The new card will be placed at the current mark
     * position, as set e.g. by {@link #findCard(String)}.
     *
     * @param  key                 The header key.
     * @param  val                 The boolean value.
     * @param  comment             A comment to append to the card.
     *
     * @return                     the new card that was added.
     *
     * @throws HeaderCardException If the parameters cannot build a valid FITS card.
     *
     * @see                        #addValue(IFitsHeader, Boolean)
     * @see                        HeaderCard#HeaderCard(String, Boolean, String)
     */
    public HeaderCard addValue(String key, Boolean val, String comment) throws HeaderCardException {
        HeaderCard hc = new HeaderCard(key, val, comment);
        addLine(hc);
        return hc;
    }

    /**
     * Add or replace a key with the given number value and comment. The value will be represented in the header card
     * with use the native precision of the value or at least {@link nom.tam.util.FlexFormat#DOUBLE_DECIMALS}, whichever
     * fits in the available card space. Trailing zeroes will be ommitted. The new card will be placed at the current
     * mark position, as set e.g. by {@link #findCard(String)}.
     *
     * @param  key                 The header key.
     * @param  val                 The number value.
     * @param  comment             A comment to append to the card.
     *
     * @return                     the new card that was added.
     *
     * @throws HeaderCardException If the parameters cannot build a valid FITS card.
     *
     * @see                        #addValue(String, Number, int, String)
     * @see                        #addValue(IFitsHeader, Number)
     * @see                        HeaderCard#HeaderCard(String, Number, String)
     */
    public HeaderCard addValue(String key, Number val, String comment) throws HeaderCardException {
        HeaderCard hc = new HeaderCard(key, val, comment);
        addLine(hc);
        return hc;
    }

    /**
     * Add or replace a key with the given number value and comment, using up to the specified decimal places after the
     * leading figure. Trailing zeroes will be ommitted. The new card will be placed at the current mark position, as
     * set e.g. by {@link #findCard(String)}.
     *
     * @param  key                 The header key.
     * @param  val                 The number value.
     * @param  decimals            The number of decimal places to show after the leading figure, or
     *                                 {@link nom.tam.util.FlexFormat#AUTO_PRECISION} to use the native precision of the
     *                                 value or at least {@link nom.tam.util.FlexFormat#DOUBLE_DECIMALS}, whichever fits
     *                                 in the available card space.
     * @param  comment             A comment to append to the card.
     *
     * @return                     the new card that was added.
     *
     * @throws HeaderCardException If the parameters cannot build a valid FITS card.
     *
     * @see                        #addValue(String, Number, String)
     * @see                        HeaderCard#HeaderCard(String, Number, int, String)
     */
    public HeaderCard addValue(String key, Number val, int decimals, String comment) throws HeaderCardException {
        HeaderCard hc = new HeaderCard(key, val, decimals, comment);
        addLine(hc);
        return hc;
    }

    /**
     * Add or replace a key with the given complex number value and comment. Trailing zeroes will be ommitted. The new
     * card will be placed at the current mark position, as set e.g. by {@link #findCard(String)}.
     *
     * @param  key                 The header keyword.
     * @param  val                 The complex number value.
     * @param  comment             A comment to append to the card.
     *
     * @return                     the new card that was added.
     *
     * @throws HeaderCardException If the parameters cannot build a valid FITS card.
     *
     * @since                      1.16
     *
     * @see                        #addValue(String, ComplexValue, int, String)
     * @see                        HeaderCard#HeaderCard(String, ComplexValue, String)
     */
    public HeaderCard addValue(String key, ComplexValue val, String comment) throws HeaderCardException {
        HeaderCard hc = new HeaderCard(key, val, comment);
        addLine(hc);
        return hc;
    }

    /**
     * Add or replace a key with the given complex number value and comment, using up to the specified decimal places
     * after the leading figure. Trailing zeroes will be ommitted. The new card will be placed at the current mark
     * position, as set e.g. by {@link #findCard(String)}.
     *
     * @param  key                 The header keyword.
     * @param  val                 The complex number value.
     * @param  decimals            The number of decimal places to show after the leading figure, or
     *                                 {@link nom.tam.util.FlexFormat#AUTO_PRECISION} to use the native precision of the
     *                                 value, or at least {@link nom.tam.util.FlexFormat#DOUBLE_DECIMALS}, whichever
     *                                 fits in the available card space.
     * @param  comment             A comment to append to the card.
     *
     * @return                     the new card that was added.
     *
     * @throws HeaderCardException If the parameters cannot build a valid FITS card.
     *
     * @since                      1.16
     *
     * @see                        #addValue(String, ComplexValue, String)
     * @see                        HeaderCard#HeaderCard(String, ComplexValue, int, String)
     */
    public HeaderCard addValue(String key, ComplexValue val, int decimals, String comment) throws HeaderCardException {
        HeaderCard hc = new HeaderCard(key, val, decimals, comment);
        addLine(hc);
        return hc;
    }

    /**
     * @deprecated                     Not supported by the FITS standard, so do not use. It was included due to a
     *                                     misreading of the standard itself. We will remove this method in the future.
     *
     * @param      key                 The header key.
     * @param      val                 The integer value.
     * @param      comment             A comment to append to the card.
     *
     * @return                         the new card that was added.
     *
     * @throws     HeaderCardException If the parameters cannot build a valid FITS card.
     *
     * @since                          1.16
     *
     * @see                            #addValue(String, Number, String)
     * @see                            HeaderCard#createHexValueCard(String, long)
     * @see                            #getHexValue(String)
     */
    @Deprecated
    public HeaderCard addHexValue(String key, long val, String comment) throws HeaderCardException {
        HeaderCard hc = HeaderCard.createHexValueCard(key, val, comment);
        addLine(hc);
        return hc;
    }

    /**
     * Add or replace a key with the given string value and comment. The new card will be placed at the current mark
     * position, as set e.g. by {@link #findCard(String)}.
     *
     * @param  key                 The header key.
     * @param  val                 The string value.
     * @param  comment             A comment to append to the card.
     *
     * @return                     the new card that was added.
     *
     * @throws HeaderCardException If the parameters cannot build a valid FITS card.
     *
     * @see                        #addValue(IFitsHeader, String)
     * @see                        HeaderCard#HeaderCard(String, String, String)
     */
    public HeaderCard addValue(String key, String val, String comment) throws HeaderCardException {
        HeaderCard hc = new HeaderCard(key, val, comment);
        addLine(hc);
        return hc;
    }

    /**
     * get a builder for filling the header cards using the builder pattern.
     *
     * @param  key the key for the first card.
     *
     * @return     the builder for header cards.
     */
    public HeaderCardBuilder card(IFitsHeader key) {
        return new HeaderCardBuilder(this, key);
    }

    /**
     * Tests if the specified keyword is present in this table.
     *
     * @param  key the keyword to be found.
     *
     * @return     <code>true</code> if the specified keyword is present in this table; <code>false</code> otherwise.
     */
    public final boolean containsKey(IFitsHeader key) {
        return cards.containsKey(key.key());
    }

    /**
     * Tests if the specified keyword is present in this table.
     *
     * @param  key the keyword to be found.
     *
     * @return     <code>true</code> if the specified keyword is present in this table; <code>false</code> otherwise.
     */
    public final boolean containsKey(String key) {
        return cards.containsKey(key);
    }

    /**
     * Delete the card associated with the given key. Nothing occurs if the key is not found.
     *
     * @param key The header key.
     */
    public void deleteKey(IFitsHeader key) {
        deleteKey(key.key());
    }

    /**
     * Delete the card associated with the given key. Nothing occurs if the key is not found.
     *
     * @param key The header key.
     */
    public void deleteKey(String key) {
        // AK: This version will not move the current position to the deleted
        // key
        if (containsKey(key)) {
            cards.remove(cards.get(key));
        }
    }

    /**
     * Print the header to a given stream. Note that this method does not show reserved card space before the END
     * keyword, and thus does not necessarily show the same layout as what would appear in a file.
     *
     * @param ps the stream to which the card images are dumped.
     * 
     * @see      #ensureCardSpace(int)
     */
    public void dumpHeader(PrintStream ps) {
        Cursor<String, HeaderCard> iter = iterator();
        while (iter.hasNext()) {
            ps.println(iter.next());
        }
    }

    /**
     * Returns the card associated with a given key. Unlike {@link #findCard(IFitsHeader)}, it does not alter the mark
     * position at which new cards are added.
     *
     * @param  key the header key.
     *
     * @return     <CODE>null</CODE> if the keyword could not be found; return the HeaderCard object otherwise.
     * 
     * @see        #getCard(String)
     * @see        #findCard(IFitsHeader)
     * 
     * @since      1.18.1
     */
    public HeaderCard getCard(IFitsHeader key) {
        return this.getCard(key.key());
    }

    /**
     * Find the card associated with a given key. If found this sets the mark (cursor) to the card, otherwise it unsets
     * the mark. The mark is where new cards will be added to the header by default. If you do not want to change the
     * mark position, use {@link #getCard(IFitsHeader)} instead.
     *
     * @param  key The header key.
     *
     * @return     <CODE>null</CODE> if the keyword could not be found; return the HeaderCard object otherwise.
     * 
     * @see        #getCard(IFitsHeader)
     * @see        #findCard(String)
     */
    public HeaderCard findCard(IFitsHeader key) {
        return this.findCard(key.key());
    }

    /**
     * Returns the card associated with a given key. Unlike {@link #findCard(String)}, it does not alter the mark
     * position at which new cards are added.
     *
     * @param  key the header key.
     *
     * @return     <CODE>null</CODE> if the keyword could not be found; return the HeaderCard object otherwise.
     * 
     * @see        #getCard(IFitsHeader)
     * @see        #findCard(String)
     * 
     * @since      1.18.1
     */
    public HeaderCard getCard(String key) {
        return cards.get(key);
    }

    /**
     * Finds the card associated with a given key, and returns it. If found this sets the mark (cursor) to just before
     * the card, such that {@link #nextCard()} will return that very same card on the first subsequent call. If the
     * header contains no matching entry, the mark is reset to the tail of the header (the same as {@link #seekTail()}).
     * The mark determines where new cards will be added to the header by default. If you do not want to alter the mark
     * position, use {@link #getCard(String)} instead.
     * 
     * @param  key the header key.
     *
     * @return     Returns the header entry for the given keyword, or <CODE>null</CODE> if the header has no such entry.
     * 
     * @see        #getCard(String)
     * @see        #findCard(String)
     */
    public HeaderCard findCard(String key) {
        HeaderCard card = cards.get(key);
        if (card != null) {
            cursor().setKey(key);
        } else {
            cursor().end();
        }
        return card;
    }

    /**
     * @deprecated     Use {@link #findCard(String)} or {@link #getCard(String)} instead. Find the card associated with
     *                     a given key.
     *
     * @param      key The header key.
     *
     * @return         <CODE>null</CODE> if the keyword could not be found; return the card image otherwise.
     */
    @Deprecated
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
     * @deprecated     The FITS header does not support decimal types beyond those that can be represented by a 64-bit
     *                     IEEE double-precision floating point value.
     *
     * @param      key The header key.
     *
     * @return         The associated value or 0.0 if not found.
     */
    public final BigDecimal getBigDecimalValue(IFitsHeader key) {
        return getBigDecimalValue(key.key());
    }

    /**
     * Get the big decimal value associated with the given key.
     * 
     * @deprecated     The FITS header does not support decimal types beyond those that can be represented by a 64-bit
     *                     IEEE double-precision floating point value.
     *
     * @param      key The header key.
     * @param      dft The default value to return if the key cannot be found.
     *
     * @return         the associated value.
     */
    public final BigDecimal getBigDecimalValue(IFitsHeader key, BigDecimal dft) {
        return getBigDecimalValue(key.key(), dft);
    }

    /**
     * Get the big decimal value associated with the given key.
     * 
     * @deprecated     The FITS header does not support decimal types beyond those that can be represented by a 64-bit
     *                     IEEE double-precision floating point value.
     *
     * @param      key The header key.
     *
     * @return         The associated value or 0.0 if not found.
     */
    public final BigDecimal getBigDecimalValue(String key) {
        return getBigDecimalValue(key, BigDecimal.ZERO);
    }

    /**
     * Get the big decimal value associated with the given key.
     *
     * @deprecated     The FITS header does not support decimal types beyond those that can be represented by a 64-bit
     *                     IEEE double-precision floating point value.
     *
     * @param      key The header key.
     * @param      dft The default value to return if the key cannot be found.
     *
     * @return         the associated value.
     */
    public BigDecimal getBigDecimalValue(String key, BigDecimal dft) {
        HeaderCard fcard = getCard(key);
        if (fcard == null) {
            return dft;
        }
        return fcard.getValue(BigDecimal.class, dft);
    }

    /**
     * Get the big integer value associated with the given key.
     * 
     * @deprecated     The FITS header does not support integer types beyond those that can be represented by a 64-bit
     *                     integer.
     *
     * @param      key The header key.
     *
     * @return         the associated value or 0 if not found.
     */
    public final BigInteger getBigIntegerValue(IFitsHeader key) {
        return getBigIntegerValue(key.key());
    }

    /**
     * Get the big integer value associated with the given key, or return a default value.
     *
     * @deprecated     The FITS header does not support integer types beyond those that can be represented by a 64-bit
     *                     integer.
     *
     * @param      key The header key.
     * @param      dft The default value to be returned if the key cannot be found.
     *
     * @return         the associated value.
     */
    public final BigInteger getBigIntegerValue(IFitsHeader key, BigInteger dft) {
        return getBigIntegerValue(key.key(), dft);
    }

    /**
     * Get the big integer value associated with the given key.
     * 
     * @deprecated     The FITS header does not support integer types beyond those that can be represented by a 64-bit
     *                     integer.
     *
     * @param      key The header key.
     *
     * @return         The associated value or 0 if not found.
     */
    public final BigInteger getBigIntegerValue(String key) {
        return getBigIntegerValue(key, BigInteger.ZERO);
    }

    /**
     * Get the big integer value associated with the given key.
     * 
     * @deprecated     The FITS header does not support integer types beyond those that can be represented by a 64-bit
     *                     integer.
     *
     * @param      key The header key.
     * @param      dft The default value to be returned if the key cannot be found.
     *
     * @return         the associated value.
     */
    public BigInteger getBigIntegerValue(String key, BigInteger dft) {
        HeaderCard fcard = getCard(key);
        if (fcard == null) {
            return dft;
        }
        return fcard.getValue(BigInteger.class, dft);
    }

    /**
     * Get the complex number value associated with the given key.
     *
     * @param  key The header key.
     *
     * @return     The associated value or {@link ComplexValue#ZERO} if not found.
     *
     * @since      1.16
     *
     * @see        #getComplexValue(String, ComplexValue)
     * @see        HeaderCard#getValue(Class, Object)
     * @see        #addValue(String, ComplexValue, String)
     */
    public final ComplexValue getComplexValue(String key) {
        return getComplexValue(key, ComplexValue.ZERO);
    }

    /**
     * Get the complex number value associated with the given key, or return a default value.
     *
     * @param  key The header key.
     * @param  dft The default value to return if the key cannot be found.
     *
     * @return     the associated value.
     *
     * @since      1.16
     *
     * @see        #getComplexValue(String)
     * @see        HeaderCard#getValue(Class, Object)
     * @see        #addValue(String, ComplexValue, String)
     */
    public ComplexValue getComplexValue(String key, ComplexValue dft) {
        HeaderCard fcard = getCard(key);
        if (fcard == null) {
            return dft;
        }
        return fcard.getValue(ComplexValue.class, dft);
    }

    /**
     * Get the <CODE>boolean</CODE> value associated with the given key.
     *
     * @param  key The header key.
     *
     * @return     The value found, or false if not found or if the keyword is not a logical keyword.
     */
    public final boolean getBooleanValue(IFitsHeader key) {
        return getBooleanValue(key.key());
    }

    /**
     * Get the <CODE>boolean</CODE> value associated with the given key.
     *
     * @param  key The header key.
     * @param  dft The value to be returned if the key cannot be found or if the parameter does not seem to be a
     *                 boolean.
     *
     * @return     the associated value.
     */
    public final boolean getBooleanValue(IFitsHeader key, boolean dft) {
        return getBooleanValue(key.key(), dft);
    }

    /**
     * Get the <CODE>boolean</CODE> value associated with the given key.
     *
     * @param  key The header key.
     *
     * @return     The value found, or false if not found or if the keyword is not a logical keyword.
     */
    public final boolean getBooleanValue(String key) {
        return getBooleanValue(key, false);
    }

    /**
     * Get the <CODE>boolean</CODE> value associated with the given key.
     *
     * @param  key The header key.
     * @param  dft The value to be returned if the key cannot be found or if the parameter does not seem to be a
     *                 boolean.
     *
     * @return     the associated value.
     */
    public boolean getBooleanValue(String key, boolean dft) {
        HeaderCard fcard = getCard(key);
        if (fcard == null) {
            return dft;
        }
        return fcard.getValue(Boolean.class, dft).booleanValue();
    }

    /**
     * Get the n'th card image in the header
     *
     * @param      n the card index to get
     *
     * @return       the card image; return <CODE>null</CODE> if the n'th card does not exist.
     *
     * @deprecated   An iterator from {@link #iterator(int)} or {@link #iterator()} should be used for sequential access
     *                   to the header.
     */
    @Deprecated
    public String getCard(int n) {
        if (n >= 0 && n < cards.size()) {
            return cards.get(n).toString();
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
     * @param  key The header key.
     *
     * @return     The associated value or 0.0 if not found.
     */
    public final double getDoubleValue(IFitsHeader key) {
        return getDoubleValue(key.key());
    }

    /**
     * Get the <CODE>double</CODE> value associated with the given key, or return a default value.
     *
     * @param  key The header key.
     * @param  dft The default value to return if the key cannot be found.
     *
     * @return     the associated value.
     */
    public final double getDoubleValue(IFitsHeader key, double dft) {
        return getDoubleValue(key.key(), dft);
    }

    /**
     * Get the <CODE>double</CODE> value associated with the given key.
     *
     * @param  key The header key.
     *
     * @return     The associated value or 0.0 if not found.
     */
    public final double getDoubleValue(String key) {
        return getDoubleValue(key, 0.0);
    }

    /**
     * Get the <CODE>double</CODE> value associated with the given key, or return a default value.
     *
     * @param  key The header key.
     * @param  dft The default value to return if the key cannot be found.
     *
     * @return     the associated value.
     */
    public double getDoubleValue(String key, double dft) {
        HeaderCard fcard = getCard(key);
        if (fcard == null) {
            return dft;
        }
        return fcard.getValue(Double.class, dft).doubleValue();
    }

    /**
     * <p>
     * Returns the list of duplicate cards in the order they appeared in the parsed header. You can access the first
     * occurence of each of every duplicated FITS keyword using the usual <code>Header.getValue()</code>, and find
     * further occurrences in the list returned here.
     * </p>
     * <p>
     * The FITS standared strongly discourages using the keywords multiple times with assigned values, and specifies
     * that the values of such keywords are undefined by definitions. Our library is thus far more tolerant than the
     * FITS standard, allowing you to access each and every value that was specified for the same keyword.
     * </p>
     * <p>
     * On the other hand FITS does not limit how many times you can add comment-style keywords to a header. If you must
     * used the same keyword multiple times in your header, you should consider using comment-style entries instead.
     * </p>
     *
     * @return the list of duplicate cards. Note that when the header is read in, only the last entry for a given
     *             keyword is retained in the active header. This method returns earlier cards that have been discarded
     *             in the order in which they were encountered in the header. It is possible for there to be many cards
     *             with the same keyword in this list.
     *
     * @see    #hadDuplicates()
     * @see    #getDuplicateKeySet()
     */
    public List<HeaderCard> getDuplicates() {
        return duplicates;
    }

    /**
     * Returns the set of keywords that had more than one value assignment in the parsed header.
     *
     * @return the set of header keywords that were assigned more than once in the same header, or <code>null</code> if
     *             there were no duplicate assignments.
     *
     * @see    #hadDuplicates()
     * @see    #getDuplicates()
     *
     * @since  1.17
     */
    public Set<String> getDuplicateKeySet() {
        return dupKeys;
    }

    @Override
    public long getFileOffset() {
        return fileOffset;
    }

    /**
     * Get the <CODE>float</CODE> value associated with the given key.
     *
     * @param  key The header key.
     *
     * @return     The associated value or 0.0 if not found.
     */
    public final float getFloatValue(IFitsHeader key) {
        return getFloatValue(key.key());

    }

    /**
     * Get the <CODE>float</CODE> value associated with the given key, or return a default value.
     * 
     * @return     the <CODE>float</CODE> value associated with the given key.
     *
     * @param  key The header key.
     * @param  dft The value to be returned if the key is not found.
     */
    public final float getFloatValue(IFitsHeader key, float dft) {
        return getFloatValue(key.key(), dft);
    }

    /**
     * Get the <CODE>float</CODE> value associated with the given key.
     *
     * @param  key The header key.
     *
     * @return     The associated value or 0.0 if not found.
     */
    public final float getFloatValue(String key) {
        return getFloatValue(key, 0.0F);
    }

    /**
     * Get the <CODE>float</CODE> value associated with the given key, or return a default value.
     * 
     * @return     the <CODE>float</CODE> value associated with the given key.
     *
     * @param  key The header key.
     * @param  dft The value to be returned if the key is not found.
     */
    public float getFloatValue(String key, float dft) {
        HeaderCard fcard = getCard(key);
        if (fcard == null) {
            return dft;
        }
        return fcard.getValue(Float.class, dft).floatValue();
    }

    /**
     * Get the <CODE>int</CODE> value associated with the given key.
     *
     * @param  key The header key.
     *
     * @return     The associated value or 0 if not found.
     */
    public final int getIntValue(IFitsHeader key) {
        return (int) getLongValue(key);
    }

    /**
     * Get the <CODE>int</CODE> value associated with the given key, or return a default value.
     * 
     * @return     the value associated with the key as an int.
     *
     * @param  key The header key.
     * @param  dft The value to be returned if the key is not found.
     */
    public final int getIntValue(IFitsHeader key, int dft) {
        return (int) getLongValue(key, dft);
    }

    /**
     * Get the <CODE>int</CODE> value associated with the given key.
     *
     * @param  key The header key.
     *
     * @return     The associated value or 0 if not found.
     */
    public final int getIntValue(String key) {
        return (int) getLongValue(key);
    }

    /**
     * Get the <CODE>int</CODE> value associated with the given key, or return a default value.
     * 
     * @return     the value associated with the key as an int.
     *
     * @param  key The header key.
     * @param  dft The value to be returned if the key is not found.
     */
    public int getIntValue(String key, int dft) {
        return (int) getLongValue(key, dft);
    }

    /**
     * Get the n'th key in the header.
     *
     * @param      n the index of the key
     *
     * @return       the card image; return <CODE>null</CODE> if the n'th key does not exist.
     *
     * @deprecated   An iterator from {@link #iterator(int)} or {@link #iterator()} should be used for sequential access
     *                   to the header.
     */
    @Deprecated
    public String getKey(int n) {
        if (n >= 0 && n < cards.size()) {
            return cards.get(n).getKey();
        }
        return null;

    }

    /**
     * Get the <CODE>long</CODE> value associated with the given key.
     *
     * @param  key The header key.
     *
     * @return     The associated value or 0 if not found.
     */
    public final long getLongValue(IFitsHeader key) {
        return getLongValue(key.key());
    }

    /**
     * Get the <CODE>long</CODE> value associated with the given key, or return a default value.
     *
     * @param  key The header key.
     * @param  dft The default value to be returned if the key cannot be found.
     *
     * @return     the associated value.
     */
    public final long getLongValue(IFitsHeader key, long dft) {
        return getLongValue(key.key(), dft);
    }

    /**
     * Get the <CODE>long</CODE> value associated with the given key.
     *
     * @param  key The header key.
     *
     * @return     The associated value or 0 if not found.
     */
    public final long getLongValue(String key) {
        return getLongValue(key, 0L);
    }

    /**
     * Get the <CODE>long</CODE> value associated with the given key, or return a default value.
     *
     * @param  key The header key.
     * @param  dft The default value to be returned if the key cannot be found.
     *
     * @return     the associated value.
     */
    public long getLongValue(String key, long dft) {
        HeaderCard fcard = getCard(key);
        if (fcard == null) {
            return dft;
        }
        return fcard.getValue(Long.class, dft).longValue();
    }

    /**
     * @deprecated     Not supported by the FITS standard, so do not use. It was included due to a misreading of the
     *                     standard itself.
     *
     * @param      key The header key.
     *
     * @return         The associated value or 0 if not found.
     *
     * @since          1.16
     *
     * @see            #getHexValue(String, long)
     * @see            HeaderCard#getHexValue()
     * @see            #addHexValue(String, long, String)
     */
    @Deprecated
    public final long getHexValue(String key) {
        return getHexValue(key, 0L);
    }

    /**
     * @deprecated     Not supported by the FITS standard, so do not use. It was included due to a misreading of the
     *                     standard itself.
     *
     * @param      key The header key.
     * @param      dft The default value to be returned if the key cannot be found.
     *
     * @return         the associated value.
     *
     * @since          1.16
     *
     * @see            #getHexValue(String)
     * @see            HeaderCard#getHexValue()
     * @see            #addHexValue(String, long, String)
     */
    public long getHexValue(String key, long dft) {
        HeaderCard fcard = getCard(key);
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
     * Returns the nominal number of currently defined cards in this header. Each card can consist of one or more
     * 80-character wide header records.
     *
     * @return the number of nominal cards in the header
     *
     * @see    #getNumberOfPhysicalCards()
     */
    public int getNumberOfCards() {
        return cards.size();
    }

    /**
     * Returns the number of 80-character header records in this header, including an END marker (whether or not it is
     * currently contained).
     *
     * @return the number of physical cards in the header, including the END marker.
     *
     * @see    #getNumberOfCards()
     * @see    #getSize()
     */
    public int getNumberOfPhysicalCards() {
        int count = 0;
        for (HeaderCard card : cards) {
            count += card.cardSize();
        }

        // AK: Count the END card, which may not have been added yet...
        if (!containsKey(END)) {
            count++;
        }

        return count;
    }

    /**
     * Returns the minimum number of bytes that will be written by this header, either as the original byte size of a
     * header that was read, or else the minimum preallocated capacity after setting {@link #ensureCardSpace(int)}.
     *
     * @return the minimum byte size for this header. The actual header may take up more space than that (but never
     *             less!), depending on the number of cards contained.
     *
     * @since  1.16
     *
     * @see    #ensureCardSpace(int)
     * @see    #read(ArrayDataInput)
     */
    public long getMinimumSize() {
        return FitsUtil.addPadding((long) minCards * HeaderCard.FITS_HEADER_CARD_SIZE);
    }

    /**
     * @deprecated <i>for internal use</i>) It should be a private method in the future. Returns the original size of
     *                 the header in the stream from which it was read.
     *
     * @return     the size of the original header in bytes, or 0 if the header was not read from a stream.
     *
     * @see        #read(ArrayDataInput)
     * @see        #getMinimumSize()
     */
    @Deprecated
    public final long getOriginalSize() {
        return readSize;
    }

    @Override
    public final long getSize() {
        if (!isValidHeader()) {
            return 0;
        }

        return FitsUtil
                .addPadding((long) Math.max(minCards, getNumberOfPhysicalCards()) * HeaderCard.FITS_HEADER_CARD_SIZE);
    }

    /**
     * Get the <CODE>String</CODE> value associated with the given standard key.
     *
     * @param  key The standard header key.
     *
     * @return     The associated value or null if not found or if the value is not a string.
     *
     * @see        #getStringValue(String)
     * @see        #getStringValue(IFitsHeader, String)
     */
    public final String getStringValue(IFitsHeader key) {
        return getStringValue(key.key());
    }

    /**
     * Get the <CODE>String</CODE> value associated with the given standard key, or return a default value.
     *
     * @param  key The standard header key.
     * @param  dft The default value.
     *
     * @return     The associated value or the default value if not found or if the value is not a string.
     *
     * @see        #getStringValue(String, String)
     * @see        #getStringValue(IFitsHeader)
     */
    public final String getStringValue(IFitsHeader key, String dft) {
        return getStringValue(key.key(), dft);
    }

    /**
     * Get the <CODE>String</CODE> value associated with the given key.
     *
     * @param  key The header key.
     *
     * @return     The associated value or null if not found or if the value is not a string.
     *
     * @see        #getStringValue(IFitsHeader)
     * @see        #getStringValue(String, String)
     */
    public final String getStringValue(String key) {
        return getStringValue(key, null);
    }

    /**
     * Get the <CODE>String</CODE> value associated with the given key, or return a default value.
     *
     * @param  key The header key.
     * @param  dft The default value.
     *
     * @return     The associated value or the default value if not found or if the value is not a string.
     *
     * @see        #getStringValue(IFitsHeader, String)
     * @see        #getStringValue(String)
     */
    public String getStringValue(String key, String dft) {

        HeaderCard fcard = getCard(key);
        if (fcard == null || !fcard.isStringValue()) {
            return dft;
        }

        return fcard.getValue();
    }

    /**
     * Checks if the header had duplicate assignments in the FITS.
     * 
     * @return Were duplicate header keys found when this record was read in?
     */
    public boolean hadDuplicates() {
        return duplicates != null;
    }

    /**
     * Adds a line to the header using the COMMENT style, i.e., no '=' in column 9. The comment text may be truncated to
     * fit into a single record, which is returned. Alternatively, you can split longer comments among multiple
     * consecutive cards of the same type by {@link #insertCommentStyleMultiline(String, String)}.
     *
     * @param  key     The comment style header keyword, or <code>null</code> for an empty comment line.
     * @param  comment A string comment to follow. Illegal characters will be replaced by '?' and the comment may be
     *                     truncated to fit into the card-space (71 characters).
     *
     * @return         The new card that was inserted, or <code>null</code> if the keyword itself was invalid or the
     *                     comment was <code>null</code>.
     *
     * @see            #insertCommentStyleMultiline(String, String)
     * @see            HeaderCard#createCommentStyleCard(String, String)
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
     * Adds a line to the header using the COMMENT style, i.e., no '=' in column 9. If the comment does not fit in a
     * single record, then it will be split (wrapped) among multiple consecutive records with the same keyword. Wrapped
     * lines will end with '&amp;' (not itself a standard) to indicate comment cards that might belong together.
     *
     * @param  key     The comment style header keyword, or <code>null</code> for an empty comment line.
     * @param  comment A string comment to follow. Illegal characters will be replaced by '?' and the comment may be
     *                     split among multiple records as necessary to be fully preserved.
     *
     * @return         The number of cards inserted.
     *
     * @since          1.16
     *
     * @see            #insertCommentStyle(String, String)
     * @see            #insertComment(String)
     * @see            #insertUnkeyedComment(String)
     * @see            #insertHistory(String)
     */
    public int insertCommentStyleMultiline(String key, String comment) {

        // Empty comments must have at least one space char to write at least one
        // comment card...
        if ((comment == null) || comment.isEmpty()) {
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
     * @param  value The comment.
     *
     * @return       The number of consecutive COMMENT cards that were inserted
     *
     * @see          #insertCommentStyleMultiline(String, String)
     * @see          #insertUnkeyedComment(String)
     * @see          #insertHistory(String)
     * @see          HeaderCard#createCommentCard(String)
     */
    public int insertComment(String value) {
        return insertCommentStyleMultiline(COMMENT.key(), value);
    }

    /**
     * Adds one or more consecutive comment records with no keyword (bytes 1-9 left blank), wrapping the comment text as
     * necessary.
     *
     * @param  value The comment.
     *
     * @return       The number of consecutive comment-style cards with no keyword (blank keyword) that were inserted.
     *
     * @since        1.16
     *
     * @see          #insertCommentStyleMultiline(String, String)
     * @see          #insertComment(String)
     * @see          #insertHistory(String)
     * @see          HeaderCard#createUnkeyedCommentCard(String)
     * @see          #insertBlankCard()
     */
    public int insertUnkeyedComment(String value) {
        return insertCommentStyleMultiline(BLANKS.key(), value);
    }

    /**
     * Adds a blank card into the header.
     *
     * @since 1.16
     *
     * @see   #insertUnkeyedComment(String)
     */
    public void insertBlankCard() {
        insertCommentStyle(null, null);
    }

    /**
     * Adds one or more consecutive a HISTORY records, wrapping the comment text as necessary.
     *
     * @param  value The history record.
     *
     * @return       The number of consecutive HISTORY cards that were inserted
     *
     * @see          #insertCommentStyleMultiline(String, String)
     * @see          #insertComment(String)
     * @see          #insertUnkeyedComment(String)
     * @see          HeaderCard#createHistoryCard(String)
     */
    public int insertHistory(String value) {
        return insertCommentStyleMultiline(HISTORY.key(), value);
    }

    /**
     * Returns a cursor-based iterator for this header's entries starting at the first entry.
     *
     * @return an iterator over the header cards
     */
    public Cursor<String, HeaderCard> iterator() {
        return cards.iterator(0);
    }

    /**
     * Returns a cursor-based iterator for this header's entries.
     * 
     * @deprecated       We should never use indexed access to the header. This function will be removed in 2.0.
     *
     * @return           an iterator over the header cards starting at an index
     *
     * @param      index the card index to start the iterator
     */
    @Deprecated
    public Cursor<String, HeaderCard> iterator(int index) {
        return cards.iterator(index);
    }

    /**
     * Return the iterator that represents the current position in the header. This provides a connection between
     * editing headers through Header add/append/update methods, and via Cursors, which can be used side-by-side while
     * maintaining desired card ordering. For the reverse direction ( translating iterator position to current position
     * in the header), we can just use findCard().
     *
     * @return the iterator representing the current position in the header.
     * 
     * @see    #iterator()
     */
    private Cursor<String, HeaderCard> cursor() {
        return cards.cursor();
    }

    /**
     * Move the cursor to the end of the header. Subsequently, all <code>addValue()</code> calls will add new cards to
     * the end of the header.
     * 
     * @return the cursor after it has been repositioned to the end
     * 
     * @since  1.18.1
     * 
     * @see    #seekTail()
     * @see    #findCard(String)
     * @see    #nextCard()
     */
    public Cursor<String, HeaderCard> seekHead() {
        Cursor<String, HeaderCard> c = cursor();

        while (c.hasPrev()) {
            c.prev();
        }

        return c;
    }

    /**
     * Move the cursor to the end of the header. Subsequently, all <code>addValue()</code> calls will add new cards to
     * the end of the header.
     * 
     * @return the cursor after it has been repositioned to the end
     * 
     * @since  1.18.1
     * 
     * @see    #seekHead()
     * @see    #findCard(String)
     */
    public Cursor<String, HeaderCard> seekTail() {
        cursor().end();
        return cursor();
    }

    /**
     * @deprecated               (<i>for internal use</i>) Normally we either want to write a Java object to FITS (in
     *                               which case we have the dataand want to make a header for it), or we read some data
     *                               from a FITS input. In either case, there is no benefit of exposing such a function
     *                               as this to the user.
     *
     * @return                   Create the data element corresponding to the current header
     *
     * @throws     FitsException if the header did not contain enough information to detect the type of the data
     */
    @Deprecated
    public Data makeData() throws FitsException {
        return FitsFactory.dataFactory(this);
    }

    /**
     * Returns the header card at the currently set mark position and increments the mark position by one. The mark
     * position determines the location at which new entries are added to the header. The mark is set either to just
     * prior a particular card (e.g. via {@link #findCard(IFitsHeader)}.
     * 
     * @return the next card in the Header using the built-in iterator
     * 
     * @see    #prevCard()
     * @see    #findCard(IFitsHeader)
     * @see    #findCard(String)
     * @see    #seekHead()
     */
    public HeaderCard nextCard() {
        if (cursor().hasNext()) {
            return cursor().next();
        }
        return null;
    }

    /**
     * Returns the header card prior to the currently set mark position and decrements the mark position by one. The
     * mark position determines the location at which new entries are added to the header. The mark is set either to
     * just prior a particular card (e.g. via {@link #findCard(IFitsHeader)}.
     * 
     * @return the next card in the Header using the built-in iterator
     * 
     * @see    #nextCard()
     * @see    #findCard(IFitsHeader)
     * @see    #findCard(String)
     * @see    #seekHead()
     * 
     * @since  1.18.1
     */
    public HeaderCard prevCard() {
        if (cursor().hasPrev()) {
            return cursor().prev();
        }
        return null;
    }

    /**
     * Create a header which points to the given data object.
     *
     * @param      o             The data object to be described.
     *
     * @throws     FitsException if the data was not valid for this header.
     *
     * @deprecated               Use the appropriate <code>Header</code> constructor instead. Will remove in a future
     *                               releae.
     */
    @Deprecated
    public void pointToData(Data o) throws FitsException {
        o.fillHeader(this);
    }

    /**
     * Remove all cards and reset the header to its default status.
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
     * @return <code>true</code> if the header contains no cards, otherwise <code>false</code>.
     *
     * @since  1.16
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * <p>
     * Reads new header data from an input, discarding any prior content.
     * </p>
     * <p>
     * As of 1.16, the header is ensured to (re)write at least the same number of cards as before, padding with blanks
     * as necessary, unless the user resets the preallocated card space with a call to {@link #ensureCardSpace(int)}.
     * </p>
     *
     * @param  dis                    The input stream to read the data from.
     *
     * @throws TruncatedFileException the the stream ended prematurely
     * @throws IOException            if the operation failed
     *
     * @see                           #ensureCardSpace(int)
     */
    @Override
    public void read(ArrayDataInput dis) throws TruncatedFileException, IOException {
        // AK: Start afresh, in case the header had prior contents from before.
        clear();

        if (dis instanceof RandomAccess) {
            fileOffset = FitsUtil.findOffset(dis);
        } else {
            fileOffset = -1;
        }

        if (dis instanceof FitsInputStream) {
            ((FitsInputStream) dis).nextChecksum();
        }
        streamSum = -1L;

        int trailingBlanks = 0;
        minCards = 0;

        HeaderCardCountingArrayDataInput cardCountingArray = new HeaderCardCountingArrayDataInput(dis);
        try {
            for (;;) {
                HeaderCard fcard = new HeaderCard(cardCountingArray);
                minCards += fcard.cardSize();

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

                if (cards.containsKey(key)) {
                    addDuplicate(cards.get(key));
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
                forceEOF(
                        "Junk detected where header was expected to start" + ((fileOffset > 0) ? ": at " + fileOffset : ""),
                        e);
            }
            if (e instanceof TruncatedFileException) {
                throw (TruncatedFileException) e;
            }
            throw new IOException("Invalid FITS Header" + (isEmpty() ? e :
                    ":\n\n --> Try FitsFactory.setAllowTerminalJunk(true) prior to reading to work around.\n"), e);
        }

        if (fileOffset >= 0) {
            input = dis;
        }

        ensureCardSpace(cardCountingArray.getPhysicalCardsRead());
        readSize = FitsUtil.addPadding((long) minCards * HeaderCard.FITS_HEADER_CARD_SIZE);

        // Read to the end of the current FITS block.
        //
        try {
            dis.skipAllBytes(FitsUtil.padding(minCards * HeaderCard.FITS_HEADER_CARD_SIZE));
        } catch (EOFException e) {
            // No biggy. We got a complete header just fine, it's only that there was no
            // padding before EOF. We'll just log that, but otherwise keep going.
            LOG.log(Level.WARNING, "Premature end-of-file: no padding after header.", e);
        }

        if (dis instanceof FitsInputStream) {
            streamSum = ((FitsInputStream) dis).nextChecksum();
        }

        // AK: Log if the file ends before the expected end-of-header position.
        if (Fits.checkTruncated(dis)) {
            // No biggy. We got a complete header just fine, it's only that there was no
            // padding before EOF. We'll just log that, but otherwise keep going.
            LOG.warning("Premature end-of-file: no padding after header.");
        }

        // Move the cursor to after the last card -- this is where new cards will be added.
        seekTail();
    }

    /**
     * Returns the random-accessible input from which this header was read, or <code>null</code> if the header is not
     * associated with an input, or the input is not random accessible.
     * 
     * @return the random-accessible input associated with this header or <code>null</code>
     * 
     * @see    #read(ArrayDataInput)
     * 
     * @since  1.18.1
     */
    RandomAccess getRandomAccessInput() {
        return (input instanceof RandomAccess) ? (RandomAccess) input : null;
    }

    /**
     * Returns the checksum value calculated duting reading from a stream. It is only populated when reading from
     * {@link FitsInputStream} imputs, and never from other types of inputs. Valid values are greater or equal to zero.
     * Thus, the return value will be <code>-1L</code> to indicate an invalid (unpopulated) checksum.
     * 
     * @return the non-negative checksum calculated for the data read from a stream, or else <code>-1L</code> if the
     *             data was not read from the stream.
     * 
     * @see    FitsInputStream
     * @see    Data#getStreamChecksum()
     * 
     * @since  1.18.1
     */
    final long getStreamChecksum() {
        return streamSum;
    }

    /**
     * Forces an EOFException to be thrown when some other exception happened, essentially treating the exception to
     * force a normal end the reading of the header.
     *
     * @param  message      the message to log.
     * @param  cause        the exception encountered while reading the header
     *
     * @throws EOFException the EOFException we'll throw instead.
     */
    private void forceEOF(String message, Exception cause) throws EOFException {
        LOG.log(Level.WARNING, message, cause);
        throw new EOFException("Forced EOF at " + fileOffset + " due to: " + message);
    }

    /**
     * Delete a key.
     *
     * @param      key                 The header key.
     *
     * @throws     HeaderCardException if the operation failed
     *
     * @deprecated                     (<i>duplicate method</i>) Use {@link #deleteKey(String)} instead.
     */
    @Deprecated
    public void removeCard(String key) throws HeaderCardException {
        deleteKey(key);
    }

    @Override
    public boolean reset() {
        try {
            FitsUtil.reposition(input, fileOffset);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Exception while repositioning " + input, e);
            return false;
        }
    }

    /**
     * @deprecated Use {@link #ensureCardSpace(int)} with 1 as the argument instead.
     *                 <p>
     *                 Resets any prior preallocated header space, such as was explicitly set by
     *                 {@link #ensureCardSpace(int)}, or when the header was read from a stream to ensure it remains
     *                 rewritable, if possible.
     *                 </p>
     *                 <p>
     *                 For headers read from a stream, this will affect {@link #rewriteable()}, so users should not call
     *                 this method unless they do not intend to {@link #rewrite()} this header into the original FITS.
     *                 </p>
     *
     * @see        #ensureCardSpace(int)
     * @see        #read(ArrayDataInput)
     * @see        #getMinimumSize()
     * @see        #rewriteable()
     * @see        #rewrite()
     */
    @Deprecated
    public final void resetOriginalSize() {
        ensureCardSpace(1);
    }

    @Override
    public void rewrite() throws FitsException, IOException {
        ArrayDataOutput dos = (ArrayDataOutput) input;

        if (!rewriteable()) {
            throw new FitsException("Invalid attempt to rewrite Header.");
        }

        FitsUtil.reposition(dos, fileOffset);
        write(dos);
        dos.flush();
    }

    @Override
    public boolean rewriteable() {
        long writeSize = FitsUtil
                .addPadding((long) Math.max(minCards, getNumberOfPhysicalCards()) * HeaderCard.FITS_HEADER_CARD_SIZE);
        return fileOffset >= 0 && input instanceof ArrayDataOutput && writeSize == getOriginalSize();
    }

    /**
     * Set the BITPIX value for the header. The following values are permitted by FITS conventions:
     * <ul>
     * <li>8 -- signed byte data. Also used for tables.</li>
     * <li>16 -- signed short data.</li>
     * <li>32 -- signed int data.</li>
     * <li>64 -- signed long data.</li>
     * <li>-32 -- IEEE 32 bit floating point numbers.</li>
     * <li>-64 -- IEEE 64 bit floating point numbers.</li>
     * </ul>
     * 
     * @deprecated                          Use the safer {@link #setBitpix(Bitpix)} instead.
     *
     * @param      val                      The value set by the user.
     *
     * @throws     IllegalArgumentException if the value is not a valid BITPIX value.
     *
     * @see                                 #setBitpix(Bitpix)
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
     * @deprecated        (<i>for internall use</i>) Visibility will be reduced to the package level in the future.
     * 
     * @param      bitpix The predefined enum value, e.g. {@link Bitpix#INTEGER}.
     *
     * @since             1.16
     *
     * @see               #setBitpix(int)
     */
    @Deprecated
    public void setBitpix(Bitpix bitpix) {
        Cursor<String, HeaderCard> iter = iterator();
        iter.next();
        iter.add(bitpix.getHeaderCard());
    }

    /**
     * Overwite the default header card sorter.
     *
     * @param headerSorter the sorter tu use or null to disable sorting
     */
    public void setHeaderSorter(Comparator<String> headerSorter) {
        this.headerSorter = headerSorter;
    }

    /**
     * Set the value of the NAXIS keyword
     * 
     * @deprecated     (<i>for internal use</i>) Visibility will be reduced to the package level in the future.
     *
     * @param      val The dimensionality of the data.
     */
    @Deprecated
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
     * @deprecated      (<i>for internal use</i>) Visibility will be reduced to the package level in the future.
     * 
     * @param      axis The axis being set.
     * @param      dim  The dimension
     */
    @Deprecated
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
     * @deprecated     (<i>for internall use</i>) Visibility will be reduced to the package level in the future.
     *
     * @param      val <code>true</code> for the primary header, otherwise <code>false</code>
     */
    @Deprecated
    public void setSimple(boolean val) {
        deleteKey(SIMPLE);
        deleteKey(XTENSION);
        deleteKey(EXTEND);

        Cursor<String, HeaderCard> iter = iterator();

        iter.add(HeaderCard.create(SIMPLE, val));

        // If we're flipping back to and from the primary header
        // we need to add in the EXTEND keyword whenever we become
        // a primary, because it's not permitted in the extensions
        // (at least not where it needs to be in the primary array).
        if (findCard(NAXIS) != null) {
            if (findCard(NAXISn.n(getIntValue(NAXIS))) != null) {
                iter.next();
            }
        }

        iter.add(HeaderCard.create(EXTEND, true));
    }

    /**
     * Set the XTENSION keyword to the given value.
     * 
     * @deprecated                          (<i>for internall use</i>) Visibility will be reduced to the package level
     *                                          in the future.
     * 
     * @param      val                      The name of the extension.
     *
     * @throws     IllegalArgumentException if the string value contains characters that are not allowed in FITS
     *                                          headers, that is characters outside of the 0x20 thru 0x7E range.
     */
    @Deprecated
    public void setXtension(String val) throws IllegalArgumentException {
        deleteKey(SIMPLE);
        deleteKey(XTENSION);
        deleteKey(EXTEND);
        iterator().add(HeaderCard.create(XTENSION, val));
    }

    /**
     * @return     the number of cards in the header
     *
     * @deprecated use {@link #getNumberOfCards()}. The units of the size of the header may be unclear.
     */
    @Deprecated
    public int size() {
        return cards.size();
    }

    /**
     * Update a valued entry in the header, or adds a new header entry. If the header does not contain a prior entry for
     * the specific keyword, or if the keyword is a comment-style key, a new entry is added at the current editing
     * position. Otherwise, the matching existing entry is updated in situ.
     *
     * @param  key                 The key of the card to be replaced (or added).
     * @param  card                A new card
     *
     * @throws HeaderCardException if the operation failed
     */
    public void updateLine(IFitsHeader key, HeaderCard card) throws HeaderCardException {
        updateLine(key.key(), card);
    }

    /**
     * Update an existing card in situ, without affecting the current position, or else add a new card at the current
     * position.
     *
     * @param  key                 The key of the card to be replaced.
     * @param  card                A new card
     *
     * @throws HeaderCardException if the operation failed
     */
    public final void updateLine(String key, HeaderCard card) throws HeaderCardException {
        // Remove an existing card with the matching 'key' (even if that key
        // isn't the same
        // as the key of the card argument!)
        cards.update(key, card);
    }

    /**
     * Overwrite the lines in the header. Add the new PHDU header to the current one. If keywords appear twice, the new
     * value and comment overwrite the current contents. By Richard J Mathar.
     *
     * @param  newHdr              the list of new header data lines to replace the current ones.
     *
     * @throws HeaderCardException if the operation failed
     * 
     * @see                        #mergeDistinct(Header)
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
     * Writes a number of blank header records, for example to create preallocated blank header space as described by
     * the FITS 4.0 standard.
     *
     * @param  dos         the output stream to which the data is to be written.
     * @param  n           the number of blank records to add.
     *
     * @throws IOException if there was an error writing to the stream
     *
     * @since              1.16
     *
     * @see                #ensureCardSpace(int)
     */
    private void writeBlankCards(ArrayDataOutput dos, int n) throws IOException {
        byte[] blank = new byte[HeaderCard.FITS_HEADER_CARD_SIZE];
        Arrays.fill(blank, (byte) ' ');

        while (--n >= 0) {
            dos.write(blank);
        }
    }

    /**
     * Add required keywords, and removes conflicting ones depending on whether it is designated as a primary header or
     * not.
     *
     * @param  xType         The value for the XTENSION keyword, or <code>null</code> if primary HDU.
     *
     * @throws FitsException if there was an error trying to edit the header.
     *
     * @since                1.17
     *
     * @see                  #validate(FitsOutput)
     */
    void setRequiredKeys(String xType) throws FitsException {

        if (xType == null) {
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
            addValue(XTENSION, xType);
        }

        // Make sure we have BITPIX
        addValue(BITPIX, getIntValue(BITPIX, Bitpix.VALUE_FOR_INT));

        int naxes = getIntValue(NAXIS, 0);
        addValue(NAXIS, naxes);

        for (int i = 1; i <= naxes; i++) {
            IFitsHeader naxisi = NAXISn.n(i);
            addValue(naxisi, getIntValue(naxisi, 1));
        }

        if (xType == null) {
            addValue(EXTEND, true);
        } else {
            addValue(PCOUNT, getIntValue(PCOUNT, 0));
            addValue(GCOUNT, getIntValue(GCOUNT, 1));
        }
    }

    /**
     * Validates this header by making it a proper primary or extension header. In both cases it means adding required
     * keywords if missing, and removing conflicting cards. Then ordering is checked and corrected as necessary and
     * ensures that the <code>END</code> card is at the tail.
     *
     * @param  asPrimary     <code>true</code> if this header is to be a primary FITS header
     *
     * @throws FitsException If there was an issue getting the header into proper form.
     *
     * @since                1.17
     */
    public void validate(boolean asPrimary) throws FitsException {
        setRequiredKeys(asPrimary ? null : getStringValue(XTENSION, "UNKNOWN"));
        validate();
    }

    /**
     * Validates the header making sure it has the required keywords and that the essential keywords appeat in the in
     * the required order
     *
     * @throws FitsException If there was an issue getting the header into proper form.
     */
    private void validate() throws FitsException {
        // Ensure that all cards are in the proper order.
        if (headerSorter != null) {
            cards.sort(headerSorter);
        }
        checkBeginning();
        checkEnd();
    }

    @Override
    public void write(ArrayDataOutput dos) throws FitsException {
        validate();

        FitsSettings settings = FitsFactory.current();
        fileOffset = FitsUtil.findOffset(dos);

        Cursor<String, HeaderCard> writeIterator = cards.iterator(0);
        try {
            int size = 0;

            while (writeIterator.hasNext()) {
                HeaderCard card = writeIterator.next();
                byte[] b = AsciiFuncs.getBytes(card.toString(settings));
                size += b.length;

                if (END.key().equals(card.getKey()) && minCards * HeaderCard.FITS_HEADER_CARD_SIZE > size) {
                    // AK: Add preallocated blank header space before the END key.
                    writeBlankCards(dos, minCards - size / HeaderCard.FITS_HEADER_CARD_SIZE);
                    size = minCards * HeaderCard.FITS_HEADER_CARD_SIZE;
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
            throw new FitsException("Not a proper FITS header: " + HeaderCard.sanitize(key) + " at " + fileOffset);
        }
    }

    private void doCardChecks(Cursor<String, HeaderCard> iter, boolean isTable, boolean isExtension) throws FitsException {
        cardCheck(iter, BITPIX);
        cardCheck(iter, NAXIS);
        int nax = getIntValue(NAXIS);

        for (int i = 1; i <= nax; i++) {
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
     * Ensure that the header begins with a valid set of keywords. Note that we do not check the values of these
     * keywords.
     */
    private void checkBeginning() throws FitsException {
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
     * Ensure that the header has exactly one END keyword in the appropriate location.
     */
    private void checkEnd() {
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
        iter.add(HeaderCard.createCommentStyleCard(END.key(), null));
    }

    /**
     * Is this a valid header.
     *
     * @return <CODE>true</CODE> for a valid header, <CODE>false</CODE> otherwise.
     */
    // TODO retire?
    private boolean isValidHeader() {
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
     * @deprecated Use {@link NullDataHDU} instead. Create a header for a null image.
     */
    @Deprecated
    void nullImage() {
        Cursor<String, HeaderCard> iter = iterator();
        iter.add(HeaderCard.create(SIMPLE, true));
        iter.add(Bitpix.BYTE.getHeaderCard());
        iter.add(HeaderCard.create(NAXIS, 0));
        iter.add(HeaderCard.create(EXTEND, true));
    }

    /**
     * Find the end of a set of keywords describing a column or axis (or anything else terminated by an index). This
     * routine leaves the header ready to add keywords after any existing keywords with the index specified. The user
     * should specify a prefix to a keyword that is guaranteed to be present.
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
     * Replace the key with a new key. Typically this is used when deleting or inserting columns. If the convention of
     * the new keyword is not compatible with the existing value a warning message is logged but no exception is thrown
     * (at this point).
     *
     * @param  oldKey              The old header keyword.
     * @param  newKey              the new header keyword.
     *
     * @return                     <CODE>true</CODE> if the card was replaced.
     *
     * @throws HeaderCardException If <CODE>newKey</CODE> is not a valid FITS keyword.
     */
    boolean replaceKey(IFitsHeader oldKey, IFitsHeader newKey) throws HeaderCardException {

        if (oldKey.valueType() == VALUE.NONE) {
            throw new IllegalArgumentException("cannot replace comment-style " + oldKey.key());
        }

        HeaderCard card = getCard(oldKey);
        VALUE newType = newKey.valueType();

        if (card != null && oldKey.valueType() != newType && newType != VALUE.ANY) {
            Class<?> type = card.valueType();
            Exception e = null;

            // Check that the exisating cards value is compatible with the expected type of the new key.
            if (newType == VALUE.NONE) {
                e = new IllegalArgumentException(
                        "comment-style " + newKey.key() + " cannot replace valued key " + oldKey.key());
            } else if (Boolean.class.isAssignableFrom(type) && newType != VALUE.LOGICAL) {
                e = new IllegalArgumentException(newKey.key() + " cannot not support the existing boolean value.");
            } else if (String.class.isAssignableFrom(type) && newType != VALUE.STRING) {
                e = new IllegalArgumentException(newKey.key() + " cannot not support the existing string value.");
            } else if (ComplexValue.class.isAssignableFrom(type) && newType != VALUE.COMPLEX) {
                e = new IllegalArgumentException(newKey.key() + " cannot not support the existing complex value.");
            } else if (card.isDecimalType() && newType != VALUE.REAL && newType != VALUE.COMPLEX) {
                e = new IllegalArgumentException(newKey.key() + " cannot not support the existing decimal values.");
            } else if (Number.class.isAssignableFrom(type) && newType != VALUE.REAL && newType != VALUE.INTEGER
                    && newType != VALUE.COMPLEX) {
                e = new IllegalArgumentException(newKey.key() + " cannot not support the existing numerical value.");
            }

            if (e != null) {
                LOG.log(Level.WARNING, e.getMessage(), e);
            }
        }

        return replaceKey(oldKey.key(), newKey.key());
    }

    /**
     * Replace the key with a new key. Typically this is used when deleting or inserting columns so that TFORMx ->
     * TFORMx-1
     *
     * @param     oldKey              The old header keyword.
     * @param     newKey              the new header keyword.
     *
     * @return                        <CODE>true</CODE> if the card was replaced.
     *
     * @exception HeaderCardException If <CODE>newKey</CODE> is not a valid FITS keyword. TODO should be private
     */
    boolean replaceKey(String oldKey, String newKey) throws HeaderCardException {
        HeaderCard oldCard = getCard(oldKey);
        if (oldCard == null) {
            return false;
        }
        if (!cards.replaceKey(oldKey, newKey)) {
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
     * Calculate the unpadded size of the data segment from the header information.
     *
     * @return the unpadded data segment size.
     */
    private long trueDataSize() {

        // AK: No need to be too strict here. We can get a data size even if the
        // header isn't 100% to spec,
        // as long as the necessary keys are present. So, just check for the
        // required keys, and no more...
        if (!containsKey(BITPIX.key()) || !containsKey(NAXIS.key())) {
            return 0L;
        }

        int naxis = getIntValue(NAXIS, 0);

        // If there are no axes then there is no data.
        if (naxis == 0) {
            return 0L;
        }

        int[] axes = new int[naxis];

        for (int axis = 1; axis <= naxis; axis++) {
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
        for (int i = startAxis; i < naxis; i++) {
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
     * Sets whether warnings about FITS standard violations are logged when a header is being read (parsed). Enabling
     * this feature can help identifying various standard violations in existing FITS headers, which nevertheless do not
     * prevent the successful reading of the header by this library.
     * </p>
     * <p>
     * If {@link FitsFactory#setAllowHeaderRepairs(boolean)} is set <code>false</code>, this will affect only minor
     * violations (e.g. a misplaced '=', missing space after '=', non-standard characters in header etc.), which
     * nevertheless do not interfere with the unamiguous parsing of the header information. More severe standard
     * violations, where some guessing may be required about the intent of some malformed header record, will throw
     * appropriate exceptions. If, however, {@link FitsFactory#setAllowHeaderRepairs(boolean)} is set <code>true</code>,
     * the parsing will throw fewer exceptions, and the additional issues may get logged as additional warning instead.
     *
     * @param value <code>true</code> if parser warnings about FITS standard violations when reading in existing FITS
     *                  headers are to be logged, otherwise <code>false</code>
     *
     * @see         #isParserWarningsEnabled()
     * @see         FitsFactory#setAllowHeaderRepairs(boolean)
     *
     * @since       1.16
     */
    public static void setParserWarningsEnabled(boolean value) {
        Level level = value ? Level.WARNING : Level.SEVERE;
        HeaderCardParser.getLogger().setLevel(level);
        Logger.getLogger(ComplexValue.class.getName()).setLevel(level);
    }

    /**
     * Checks whether warnings about FITS standard violations are logged when a header is being read (parsed).
     *
     * @return <code>true</code> if parser warnings about FITS standard violations when reading in existing FITS headers
     *             are enabled and logged, otherwise <code>false</code>
     *
     * @see    #setParserWarningsEnabled(boolean)
     *
     * @since  1.16
     */
    public static boolean isParserWarningsEnabled() {
        return !HeaderCardParser.getLogger().getLevel().equals(Level.SEVERE);
    }

    /**
     * Returns the current preferred alignment character position of inline header comments. This is the position at
     * which the '/' is placed for the inline comment. #deprecated
     *
     * @return The current alignment position for inline comments.
     *
     * @see    #setCommentAlignPosition(int)
     * 
     * @since  1.17
     */
    public static int getCommentAlignPosition() {
        return commentAlign;
    }

    /**
     * Sets a new alignment position for inline header comments.
     *
     * @param      pos                      [20:70] The character position to which inline comments should be aligned if
     *                                          possible.
     *
     * @throws     IllegalArgumentException if the position is outside of the allowed range.
     *
     * @see                                 #getCommentAlignPosition()
     * 
     * @deprecated                          Not recommended as it may violate the FITS standart for 'fixed-format'
     *                                          header entries, and make our FITS files unreadable by software that
     *                                          expects strict adherence to the standard. We will remove this feature in
     *                                          the future.
     * 
     * @since                               1.17
     */
    public static void setCommentAlignPosition(int pos) throws IllegalArgumentException {
        if (pos < Header.MIN_COMMENT_ALIGN || pos > Header.MAX_COMMENT_ALIGN) {
            throw new IllegalArgumentException(
                    "Comment alignment " + pos + " out of range (" + MIN_COMMENT_ALIGN + ":" + MAX_COMMENT_ALIGN + ").");
        }
        commentAlign = pos;
    }
}
