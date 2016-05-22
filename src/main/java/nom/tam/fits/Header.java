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

import static nom.tam.fits.header.Standard.BITPIX;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import nom.tam.fits.header.IFitsHeader;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.Cursor;
import nom.tam.util.FitsIO;
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

    private static final int MIN_NUMBER_OF_CARDS_FOR_VALID_HEADER = 4;

    private static final int MAX_CARDS_PER_HEADER = FitsFactory.FITS_BLOCK_SIZE / HeaderCard.FITS_HEADER_CARD_SIZE;

    private static final Logger LOG = Logger.getLogger(Header.class.getName());

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
            if (e.getCause() instanceof TruncatedFileException) {
                throw e;
            }
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

    /**
     * The actual header data stored as a HashedList of HeaderCard's.
     */
    private final HashedList<HeaderCard> cards = new HashedList<HeaderCard>();

    /**
     * This iterator allows one to run through the list.
     */
    private Cursor<String, HeaderCard> iter = this.cards.iterator(0);

    /** Offset of this Header in the FITS file */
    private long fileOffset = -1;

    private List<HeaderCard> duplicates;

    /** Input descriptor last time header was read */
    private ArrayDataInput input;

    /**
     * Number of cards in header before duplicates were removed. A user may want
     * to know how large the actual FITS header was on input. Since the keyword
     * hash removes duplicate keys the internal size may be smaller. Added by
     * Booth Hartley (IPAC/Caltech).
     */
    private int originalCardCount = 0; // RBH ADDED

    /** Create an empty header */
    public Header() {
        super();
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
        o.fillHeader(this);
    }

    /**
     * Create a header and initialize it with a vector of strings.
     * 
     * @param newCards
     *            Card images to be placed in the header.
     */
    public Header(String[] newCards) {
        for (String newCard : newCards) {
            this.cards.add(HeaderCard.create(newCard));
        }
    }

    private void addDuplicate(HeaderCard dup) {
        if (!COMMENT.key().equals(dup.getKey()) && !HISTORY.key().equals(dup.getKey()) && !dup.getKey().trim().isEmpty()) {
            LOG.log(Level.WARNING, "Multiple occurrences of key:" + dup.getKey());
            if (this.duplicates == null) {
                this.duplicates = new ArrayList<HeaderCard>();
            }
            this.duplicates.add(dup);
        }
    }

    private void addHeaderCard(String key, HeaderCard card) {
        deleteKey(key);
        this.iter.add(card);
    }

    /**
     * Add a card image to the header.
     * 
     * @param fcard
     *            The card to be added.
     */
    public void addLine(HeaderCard fcard) {
        if (fcard != null) {
            this.iter.add(fcard);
        }
    }

    /**
     * Add or replace a key with the given boolean value and comment.
     * 
     * @param key
     *            The header key.
     * @param val
     *            The boolean value.
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     */
    public void addValue(IFitsHeader key, boolean val) throws HeaderCardException {
        addValue(key.key(), val, key.comment());
    }

    /**
     * Add or replace a key with the given double value and comment. Note that
     * float values will be promoted to doubles.
     * 
     * @param key
     *            The header key.
     * @param val
     *            The double value.
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     */
    public void addValue(IFitsHeader key, double val) throws HeaderCardException {
        addValue(key.key(), val, key.comment());
    }

    /**
     * Add or replace a key with the given long value and comment. Note that
     * int's will be promoted to long's.
     * 
     * @param key
     *            The header key.
     * @param val
     *            The long value.
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     */
    public void addValue(IFitsHeader key, int val) throws HeaderCardException {
        addValue(key.key(), val, key.comment());
    }

    /**
     * Add or replace a key with the given long value and comment. Note that
     * int's will be promoted to long's.
     * 
     * @param key
     *            The header key.
     * @param val
     *            The long value.
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     */
    public void addValue(IFitsHeader key, long val) throws HeaderCardException {
        addValue(key.key(), val, key.comment());
    }

    /**
     * Add or replace a key with the given string value and comment.
     * 
     * @param key
     *            The header key.
     * @param val
     *            The string value.
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     */
    public void addValue(IFitsHeader key, String val) throws HeaderCardException {
        addValue(key.key(), val, key.comment());
    }

    /**
     * Add or replace a key with the given bigdecimal value and comment. Note
     * that float values will be promoted to doubles.
     * 
     * @param key
     *            The header key.
     * @param val
     *            The bigDecimal value.
     * @param comment
     *            A comment to append to the card.
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     */
    public void addValue(String key, BigDecimal val, String comment) throws HeaderCardException {
        addHeaderCard(key, new HeaderCard(key, val, comment));
    }

    /**
     * Add or replace a key with the given BigInteger value and comment. Note
     * that float values will be promoted to doubles.
     * 
     * @param key
     *            The header key.
     * @param val
     *            The BigInteger value.
     * @param comment
     *            A comment to append to the card.
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     */
    public void addValue(String key, BigInteger val, String comment) throws HeaderCardException {
        addHeaderCard(key, new HeaderCard(key, val, comment));
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
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     */
    public void addValue(String key, boolean val, String comment) throws HeaderCardException {
        addHeaderCard(key, new HeaderCard(key, val, comment));
    }

    /**
     * Add or replace a key with the given double value and comment. Note that
     * float values will be promoted to doubles.
     * 
     * @param key
     *            The header key.
     * @param val
     *            The double value.
     * @param comment
     *            A comment to append to the card.
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     */
    public void addValue(String key, double val, String comment) throws HeaderCardException {
        addHeaderCard(key, new HeaderCard(key, val, comment));
    }

    /**
     * Add or replace a key with the given double value and comment. Note that
     * float values will be promoted to doubles.
     *
     * @param key
     *            The header key.
     * @param val
     *            The double value.
     * @param precision
     *            The fixed number of decimal places to show.
     * @param comment
     *            A comment to append to the card.
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     */
    public void addValue(String key, double val, int precision, String comment) throws HeaderCardException {
        this.iter.add(new HeaderCard(key, val, precision, comment));
    }

    /**
     * Add or replace a key with the given long value and comment. Note that
     * int's will be promoted to long's.
     * 
     * @param key
     *            The header key.
     * @param val
     *            The long value.
     * @param comment
     *            A comment to append to the card.
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     */
    public void addValue(String key, long val, String comment) throws HeaderCardException {
        addHeaderCard(key, new HeaderCard(key, val, comment));
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
     * @throws HeaderCardException
     *             If the parameters cannot build a valid FITS card.
     */
    public void addValue(String key, String val, String comment) throws HeaderCardException {
        addHeaderCard(key, new HeaderCard(key, val, comment));
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
     * Check if the given key is the next one available in the header.
     */
    private void cardCheck(IFitsHeader key) throws FitsException {
        cardCheck(key.key());
    }

    /**
     * Check if the given key is the next one available in the header.
     */
    private void cardCheck(String key) throws FitsException {

        if (!this.iter.hasNext()) {
            throw new FitsException("Header terminates before " + key);
        }
        HeaderCard card = this.iter.next();
        if (!card.getKey().equals(key)) {
            throw new FitsException("Key " + key + " not found where expected." + "Found " + card.getKey());
        }
    }

    /**
     * Ensure that the header begins with a valid set of keywords. Note that we
     * do not check the values of these keywords.
     */
    void checkBeginning() throws FitsException {
        this.iter = iterator();
        if (!this.iter.hasNext()) {
            throw new FitsException("Empty Header");
        }
        HeaderCard card = this.iter.next();
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
        doCardChecks(isTable, isExtension);
    }

    private void doCardChecks(boolean isTable, boolean isExtension) throws FitsException {
        cardCheck(BITPIX);
        cardCheck(NAXIS);
        int nax = getIntValue(NAXIS);
        this.iter.next();
        for (int i = 1; i <= nax; i += 1) {
            cardCheck(NAXISn.n(i));
        }
        if (isExtension) {
            cardCheck(PCOUNT);
            cardCheck(GCOUNT);
            if (isTable) {
                cardCheck(TFIELDS);
            }
        }
        // This does not check for the EXTEND keyword which
        // if present in the primary array must immediately follow
        // the NAXISn.
    }

    /**
     * Ensure that the header has exactly one END keyword in the appropriate
     * location.
     */
    void checkEnd() {

        // Ensure we have an END card only at the end of the
        // header.
        //
        this.iter = iterator();
        HeaderCard card;

        while (this.iter.hasNext()) {
            card = this.iter.next();
            if (!card.isKeyValuePair() && card.getKey().equals(END.key())) {
                this.iter.remove();
            }
        }
        // End cannot have a comment
        this.iter.add(HeaderCard.saveNewHeaderCard(END.key(), null, false));
    }

    private void checkFirstCard(String key) throws IOException {
        if (key == null || !key.equals(SIMPLE.key()) && !key.equals(XTENSION.key())) {
            if (this.fileOffset > 0 && FitsFactory.getAllowTerminalJunk()) {
                throw new EOFException("Not FITS format at " + this.fileOffset + ":" + key);
            } else {
                throw new IOException("Not FITS format at " + this.fileOffset + ":" + key);
            }
        }
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
        if (containsKey(key)) {
            this.iter.setKey(key);
            if (this.iter.hasNext()) {
                this.iter.next();
                this.iter.remove();
            }
        }
    }

    /**
     * Print the header to a given stream.
     * 
     * @param ps
     *            the stream to which the card images are dumped.
     */
    public void dumpHeader(PrintStream ps) {
        this.iter = iterator();
        while (this.iter.hasNext()) {
            ps.println(this.iter.next());
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
            this.iter.setKey(key);
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
        } else {
            return card.toString();
        }
    }

    /**
     * Get the <CODE>double</CODE> value associated with the given key.
     * 
     * @param key
     *            The header key.
     * @return The associated value or 0.0 if not found.
     */
    public BigDecimal getBigDecimalValue(IFitsHeader key) {
        return getBigDecimalValue(key.key());
    }

    /**
     * Get the <CODE>double</CODE> value associated with the given key.
     * 
     * @param key
     *            The header key.
     * @return The associated value or 0.0 if not found.
     */
    public BigDecimal getBigDecimalValue(String key) {
        return getBigDecimalValue(key, BigDecimal.ZERO);
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
    public BigDecimal getBigDecimalValue(String key, BigDecimal dft) {
        HeaderCard fcard = findCard(key);
        if (fcard == null) {
            return dft;
        }
        return fcard.getValue(BigDecimal.class, dft);
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
    public BigInteger getBigIntegerValue(IFitsHeader key, BigInteger dft) {
        return getBigIntegerValue(key.key(), dft);
    }

    /**
     * Get the <CODE>long</CODE> value associated with the given key.
     * 
     * @param key
     *            The header key.
     * @return The associated value or 0 if not found.
     */
    public BigInteger getBigIntegerValue(String key) {
        return getBigIntegerValue(key, BigInteger.ZERO);
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
    public BigInteger getBigIntegerValue(String key, BigInteger dft) {
        HeaderCard fcard = findCard(key);
        if (fcard == null) {
            return dft;
        }
        return fcard.getValue(BigInteger.class, dft);
    }

    /**
     * Get the <CODE>boolean</CODE> value associated with the given key.
     * 
     * @param key
     *            The header key.
     * @return The value found, or false if not found or if the keyword is not a
     *         logical keyword.
     */
    public boolean getBooleanValue(IFitsHeader key) {
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
    public boolean getBooleanValue(IFitsHeader key, boolean dft) {
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
    public boolean getBooleanValue(String key) {
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
    public double getDoubleValue(IFitsHeader key) {
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
    public double getDoubleValue(IFitsHeader key, double dft) {
        return getDoubleValue(key.key(), dft);
    }

    /**
     * Get the <CODE>double</CODE> value associated with the given key.
     * 
     * @param key
     *            The header key.
     * @return The associated value or 0.0 if not found.
     */
    public double getDoubleValue(String key) {
        return getDoubleValue(key, 0.);
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
     * @return the list of duplicate cards. Note that when the header is read
     *         in, only the last entry for a given keyword is retained in the
     *         active header. This method returns earlier cards that have been
     *         discarded in the order in which they were encountered in the
     *         header. It is possible for there to be many cards with the same
     *         keyword in this list.
     */
    public List<HeaderCard> getDuplicates() {
        return this.duplicates;
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
    public float getFloatValue(IFitsHeader key) {
        return getFloatValue(key.key());

    }

    /**
     * @return the <CODE>float</CODE> value associated with the given key.
     * @param key
     *            The header key.
     * @param dft
     *            The value to be returned if the key is not found.
     */
    public float getFloatValue(IFitsHeader key, float dft) {
        return (float) getDoubleValue(key, dft);
    }

    /**
     * Get the <CODE>float</CODE> value associated with the given key.
     * 
     * @param key
     *            The header key.
     * @return The associated value or 0.0 if not found.
     */
    public float getFloatValue(String key) {
        return (float) getDoubleValue(key);
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
    public int getIntValue(IFitsHeader key) {
        return (int) getLongValue(key);
    }

    /**
     * @return the value associated with the key as an int.
     * @param key
     *            The header key.
     * @param dft
     *            The value to be returned if the key is not found.
     */
    public int getIntValue(IFitsHeader key, int dft) {
        return (int) getLongValue(key, dft);
    }

    /**
     * Get the <CODE>int</CODE> value associated with the given key.
     * 
     * @param key
     *            The header key.
     * @return The associated value or 0 if not found.
     */
    public int getIntValue(String key) {
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
        HeaderCard fcard = findCard(key);
        if (fcard == null) {
            return dft;
        }
        return fcard.getValue(Integer.class, dft).intValue();
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
    public long getLongValue(IFitsHeader key) {
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
    public long getLongValue(IFitsHeader key, long dft) {
        return getLongValue(key.key(), dft);
    }

    /**
     * Get the <CODE>long</CODE> value associated with the given key.
     * 
     * @param key
     *            The header key.
     * @return The associated value or 0 if not found.
     */
    public long getLongValue(String key) {
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
     * @return the number of cards in the header
     */
    public int getNumberOfCards() {
        return this.cards.size();
    }

    /**
     * @return the number of physical cards in the header.
     */
    public int getNumberOfPhysicalCards() {
        int count = 0;
        for (HeaderCard card : this.cards) {
            count += card.cardSize();
        }
        return count;
    }

    /**
     * @return the size of the original header in bytes.
     */
    public long getOriginalSize() {
        return FitsUtil.addPadding(this.originalCardCount * HeaderCard.FITS_HEADER_CARD_SIZE);
    }

    /**
     * @return the size of the header in bytes
     */
    @Override
    public long getSize() {
        return headerSize();
    }

    public String getStringValue(IFitsHeader header) {
        return getStringValue(header.key());
    }

    /**
     * Get the <CODE>String</CODE> value associated with the given key.
     * 
     * @param key
     *            The header key.
     * @return The associated value or null if not found or if the value is not
     *         a string.
     */
    public String getStringValue(String key) {

        HeaderCard fcard = findCard(key);
        if (fcard == null || !fcard.isStringValue()) {
            return null;
        }

        return fcard.getValue();
    }

    /** @return Were duplicate header keys found when this record was read in? */
    public boolean hadDuplicates() {
        return this.duplicates != null;
    }

    /**
     * Return the size of the header data including padding.
     * 
     * @return the header size including any needed padding.
     */
    int headerSize() {

        if (!isValidHeader()) {
            return 0;
        }

        return FitsUtil.addPadding(getNumberOfPhysicalCards() * HeaderCard.FITS_HEADER_CARD_SIZE);
    }

    /**
     * Add a COMMENT line.
     * 
     * @param value
     *            The comment.
     * @exception HeaderCardException
     *                If the parameter is not a valid FITS comment.
     */
    public void insertComment(String value) throws HeaderCardException {
        insertCommentStyle(COMMENT.key(), value);
    }

    /**
     * Add a line to the header using the COMMENT style, i.e., no '=' in column
     * 9.
     * 
     * @param header
     *            The comment style header.
     * @param value
     *            A string to follow the header.
     */
    public void insertCommentStyle(String header, String value) {
        this.iter.add(HeaderCard.saveNewHeaderCard(header, value, false));
    }

    /**
     * Add a HISTORY line.
     * 
     * @param value
     *            The history record.
     * @exception HeaderCardException
     *                If the parameter is not a valid FITS comment.
     */
    public void insertHistory(String value) throws HeaderCardException {
        insertCommentStyle(HISTORY.key(), value);
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
        this.iter = iterator();
        String key = this.iter.next().getKey();
        if (!key.equals(SIMPLE.key()) && !key.equals(XTENSION.key())) {
            return false;
        }
        key = this.iter.next().getKey();
        if (!key.equals(BITPIX.key())) {
            return false;
        }
        key = this.iter.next().getKey();
        if (!key.equals(NAXIS.key())) {
            return false;
        }
        while (this.iter.hasNext()) {
            key = this.iter.next().getKey();
        }
        return key.equals(END.key());

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
        if (this.iter.hasNext()) {
            return this.iter.next();
        } else {
            return null;
        }
    }

    /**
     * Create a header for a null image.
     */
    void nullImage() {
        this.iter = iterator();
        this.iter.add(HeaderCard.saveNewHeaderCard(SIMPLE.key(), SIMPLE.comment(), false).setValue(true));
        this.iter.add(HeaderCard.saveNewHeaderCard(BITPIX.key(), BITPIX.comment(), false).setValue(BasicHDU.BITPIX_BYTE));
        this.iter.add(HeaderCard.saveNewHeaderCard(NAXIS.key(), NAXIS.comment(), false).setValue(0));
        this.iter.add(HeaderCard.saveNewHeaderCard(EXTEND.key(), EXTEND.comment(), false).setValue(true));
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
     * Find the end of a set of keywords describing a column or axis (or
     * anything else terminated by an index. This routine leaves the header
     * ready to add keywords after any existing keywords with the index
     * specified. The user should specify a prefix to a keyword that is
     * guaranteed to be present.
     */
    Cursor<String, HeaderCard> positionAfterIndex(IFitsHeader prefix, int col) {
        String colnum = String.valueOf(col);
        this.iter.setKey(prefix.n(col).key());
        if (this.iter.hasNext()) {
            // Bug fix (references to forward) here by Laurent Borges
            boolean toFar = false;
            while (this.iter.hasNext()) {
                String key = this.iter.next().getKey().trim();
                if (key == null || key.length() <= colnum.length() || !key.substring(key.length() - colnum.length()).equals(colnum)) {
                    toFar = true;
                    break;
                }
            }
            if (toFar) {
                this.iter.prev(); // Gone one too far, so skip back an element.
            }
        }
        return this.iter;
    }

    /**
     * Read a stream for header data.
     * 
     * @param dis
     *            The input stream to read the data from.
     * @throws TruncatedFileException
     *             the the stream ended prematurely
     * @throws IOException
     *             if the operation failed
     */
    @Override
    public void read(ArrayDataInput dis) throws TruncatedFileException, IOException {
        if (dis instanceof RandomAccess) {
            this.fileOffset = FitsUtil.findOffset(dis);
        } else {
            this.fileOffset = -1;
        }

        boolean firstCard = true;
        HeaderCardCountingArrayDataInput cardCountingArray = new HeaderCardCountingArrayDataInput(dis);
        try {
            while (true) {
                HeaderCard fcard = new HeaderCard(cardCountingArray);
                String key = fcard.getKey();
                if (firstCard) {
                    checkFirstCard(key);
                    firstCard = false;
                }

                if (key != null && this.cards.containsKey(key)) {
                    addDuplicate(this.cards.get(key));
                }

                // We don't check the value here. If the user
                // wants to be sure that long strings are disabled,
                // they can call setLongStringsEnabled(false) after
                // reading the header.
                if (LONGSTRN.key().equals(key)) {
                    FitsFactory.setLongStringsEnabled(true);
                }
                // save card
                addLine(fcard);
                if (END.key().equals(key)) {
                    break; // Out of reading the header.
                }
            }
        } catch (EOFException e) {
            if (!firstCard) {
                throw new IOException("Invalid FITS Header:", new TruncatedFileException(e.getMessage()));
            }
            throw e;

        } catch (TruncatedFileException e) {
            if (firstCard && FitsFactory.getAllowTerminalJunk()) {
                EOFException eofException = new EOFException("First card truncated");
                eofException.initCause(e);
                throw eofException;
            }
            throw new IOException("Invalid FITS Header:", new TruncatedFileException(e.getMessage()));
        } catch (Exception e) {
            throw new IOException("Invalid FITS Header", e);
        }
        if (this.fileOffset >= 0) {
            this.input = dis;
        }
        this.originalCardCount = cardCountingArray.getPhysicalCardsRead();
        // Read to the end of the current FITS block.
        //
        try {
            dis.skipAllBytes(FitsUtil.padding(this.originalCardCount * HeaderCard.FITS_HEADER_CARD_SIZE));
        } catch (IOException e) {
            throw new TruncatedFileException("Failed to skip " + FitsUtil.padding(this.originalCardCount * HeaderCard.FITS_HEADER_CARD_SIZE) + " bytes", e);
        }
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
    boolean replaceKey(IFitsHeader oldKey, IFitsHeader newKey) throws HeaderCardException {
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
            throw new HeaderCardException("Duplicate key in replace");
        }
        oldCard.setKey(newKey);
        return true;
    }

    /** Reset the file pointer to the beginning of the header */
    @Override
    public boolean reset() {
        try {
            FitsUtil.reposition(this.input, this.fileOffset);
            return true;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Exception while repositioning " + input, e);
            return false;
        }
    }

    /**
     * Indicate that we can use the current internal size of the Header as the
     * 'original' size (e.g., perhaps we've rewritten the header to disk). Note
     * that affects the results of rewriteable(), so users should not call this
     * method unless the underlying data has actually been updated.
     */
    public void resetOriginalSize() {
        this.originalCardCount = getNumberOfPhysicalCards();
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
        return this.fileOffset >= 0 && this.input instanceof ArrayDataOutput && //
                (getNumberOfPhysicalCards() + (MAX_CARDS_PER_HEADER - 1)) / MAX_CARDS_PER_HEADER == //
                (this.originalCardCount + (MAX_CARDS_PER_HEADER - 1)) / MAX_CARDS_PER_HEADER;
    }

    /**
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
     */
    public void setBitpix(int val) {
        this.iter = iterator();
        this.iter.next();
        this.iter.add(HeaderCard.saveNewHeaderCard(BITPIX.key(), BITPIX.comment(), false).setValue(val));
    }

    /**
     * Set the value of the NAXIS keyword
     * 
     * @param val
     *            The dimensionality of the data.
     */
    public void setNaxes(int val) {
        this.iter.setKey(BITPIX.key());
        if (this.iter.hasNext()) {
            this.iter.next();
        }
        this.iter.add(HeaderCard.saveNewHeaderCard(NAXIS.key(), NAXIS.comment(), false).setValue(val));
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

        if (axis <= 0) {
            LOG.warning("setNaxis ignored because axis less than 0");
            return;
        }
        if (axis == 1) {
            this.iter.setKey(NAXIS.key());
        } else if (axis > 1) {
            this.iter.setKey(NAXISn.n(axis - 1).key());
        }
        if (this.iter.hasNext()) {
            this.iter.next();
        }
        IFitsHeader naxisKey = NAXISn.n(axis);
        this.iter.add(HeaderCard.saveNewHeaderCard(naxisKey.key(), naxisKey.comment(), false).setValue(dim));
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

        // If we're flipping back to and from the primary header
        // we need to add in the EXTEND keyword whenever we become
        // a primary, because it's not permitted in the extensions
        // (at least not where it needs to be in the primary array).
        if (findCard(NAXIS) != null) {
            int nax = getIntValue(NAXIS);

            this.iter = iterator();

            if (findCard(NAXISn.n(nax)) != null) {
                this.iter.next();
                deleteKey(EXTEND);
                this.iter.add(HeaderCard.saveNewHeaderCard(EXTEND.key(), EXTEND.comment(), false).setValue(true));
            }
        }
        this.iter = iterator();
        this.iter.add(HeaderCard.saveNewHeaderCard(SIMPLE.key(), SIMPLE.comment(), false).setValue(val));
    }

    /**
     * Set the XTENSION keyword to the given value.
     * 
     * @param val
     *            The name of the extension.
     */
    public void setXtension(String val) {
        deleteKey(SIMPLE);
        deleteKey(XTENSION);
        deleteKey(EXTEND);
        this.iter = iterator();
        this.iter.add(HeaderCard.saveNewHeaderCard(XTENSION.key(), XTENSION.comment(), true).setValue(val));
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
     * Calculate the unpadded size of the data segment from the header
     * information.
     * 
     * @return the unpadded data segment size.
     */
    long trueDataSize() {

        if (!isValidHeader()) {
            return 0L;
        }

        int naxis = getIntValue(NAXIS, 0);

        // If there are no axes then there is no data.
        if (naxis == 0) {
            return 0L;
        }

        getIntValue(BITPIX);

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
     * Update a line in the header
     * 
     * @param key
     *            The key of the card to be replaced.
     * @param card
     *            A new card
     * @throws HeaderCardException
     *             if the operation failed
     */
    public void updateLine(IFitsHeader key, HeaderCard card) throws HeaderCardException {
        deleteKey(key);
        this.iter.add(card);
    }

    /**
     * Update a line in the header
     * 
     * @param key
     *            The key of the card to be replaced.
     * @param card
     *            A new card
     * @throws HeaderCardException
     *             if the operation failed
     */
    public void updateLine(String key, HeaderCard card) throws HeaderCardException {
        addHeaderCard(key, card);
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
            HeaderCard nextHCard = j.next();
            // updateLine() doesn't work with COMMENT and HISTORYs because
            // this would allow only one COMMENT in total in each header
            if (nextHCard.getKey().equals(COMMENT.key())) {
                insertComment(nextHCard.getComment());
            } else if (nextHCard.getKey().equals(HISTORY.key())) {
                insertHistory(nextHCard.getComment());
            } else {
                updateLine(nextHCard.getKey(), nextHCard);
            }
        }
    }

    /**
     * Write the current header (including any needed padding) to the output
     * stream.
     * 
     * @param dos
     *            The output stream to which the data is to be written.
     * @throws FitsException
     *             if the header could not be written.
     */
    @Override
    public void write(ArrayDataOutput dos) throws FitsException {
        this.fileOffset = FitsUtil.findOffset(dos);
        // Ensure that all cards are in the proper order.
        this.cards.sort(new HeaderOrder());
        checkBeginning();
        checkEnd();
        Cursor<String, HeaderCard> writeIterator = this.cards.iterator(0);
        try {
            while (writeIterator.hasNext()) {
                HeaderCard card = writeIterator.next();
                byte[] b = AsciiFuncs.getBytes(card.toString());
                dos.write(b);
            }
            FitsUtil.pad(dos, getNumberOfPhysicalCards() * HeaderCard.FITS_HEADER_CARD_SIZE, (byte) ' ');
            dos.flush();
        } catch (IOException e) {
            throw new FitsException("IO Error writing header: " + e);
        }
    }
}
