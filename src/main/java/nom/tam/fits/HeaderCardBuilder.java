package nom.tam.fits;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2021 nom-tam-fits
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


import java.util.Date;

import nom.tam.fits.header.IFitsHeader;

/**
 * builder pattern implementation for easy readable header card creation.
 * 
 * @author nir
 */
public class HeaderCardBuilder {

    /**
     * the header to fill.
     */
    private final Header header;

    /**
     * the current card to fill.
     */
    private HeaderCard card;

    /**
     * the current selected key.
     */
    private IFitsHeader key;

    /**
     * scale to use for decimal values.
     */
    private int precision = -1;

    /**
     * constructor to the header card builder.
     * 
     * @param header
     *            the header to fill.
     * @param key
     *            the first header card to set.
     */
    protected HeaderCardBuilder(Header header, IFitsHeader key) {
        this.header = header;
        card(key);
    }

    /**
     * get the current build card of the builder.
     * 
     * @return the current card
     */
    public HeaderCard card() {
        return this.card;
    }

    /**
     * switch focus to the card with the specified key. If the card does not
     * exist the card will be created when the value or the comment is set.
     * 
     * @param newKey
     *            the new card to set
     * @return this
     */
    public HeaderCardBuilder card(IFitsHeader newKey) {
        this.key = newKey;
        this.card = this.header.findCard(this.key);
        return this;
    }

    /**
     * set the comment of the current card. If the card does not exist yet the
     * card is created with a null value, if the card needs a value use the
     * value setter first!
     * 
     * @param newComment
     *            the new comment to set.
     * @return this
     * @throws HeaderCardException
     *             if the card creation failed.
     */
    public HeaderCardBuilder comment(String newComment) throws HeaderCardException {
        if (this.card == null) {
            this.card = new HeaderCard(this.key.key(), (String) null, null);
            this.header.addLine(this.card);
        }
        this.card.setComment(newComment);
        return this;
    }

    /**
     * set the value of the current card.If the card did not exist yet the card
     * will be created.
     * 
     * @param newValue
     *            the new value to set.
     * @return this
     * @throws HeaderCardException
     *             if the card creation failed.
     */
    public HeaderCardBuilder value(boolean newValue) throws HeaderCardException {
        if (this.card == null) {
            this.card = new HeaderCard(this.key.key(), newValue, null);
            this.header.addLine(this.card);
        } else {
            this.card.setValue(newValue);
        }
        return this;
    }

    /**
     * set the value of the current card. If the card did not exist yet the card
     * will be created.
     * 
     * @param newValue
     *            the new value to set.
     * @return this
     * @throws HeaderCardException
     *             if the card creation failed.
     */
    public HeaderCardBuilder value(Date newValue) throws HeaderCardException {
        return value(FitsDate.getFitsDateString(newValue));
    }

    /**
     * Sets a new number value for the current card. If the card did not exist yet the card
     * will be created.
     * 
     * @param value     the new number value to set.
     * @return this
     * @throws HeaderCardException if the card creation failed.
     * @throws LongValueException 
     *                  if the number value cannot be represented in the space available
     *                  for it in the 80-character wide FITS header record.    
     */
    public HeaderCardBuilder value(Number value) throws HeaderCardException, LongValueException {
        if (this.card == null) {
            this.card = new HeaderCard(this.key.key(), value, precision, null);
            this.header.addLine(this.card);
        } else {
            this.card.setValue(value, precision);
        }
        return this;
    }

    /**
     * set the value of the current card.If the card did not exist yet the card
     * will be created.
     * 
     * @param newValue
     *            the new value to set.
     * @return this
     * @throws HeaderCardException
     *             if the card creation failed.
     * @throws LongValueException 
     *             if the number value cannot be represented in the space available
     *             for it in the 80-character wide FITS header record.
     */
    public HeaderCardBuilder value(String newValue) throws HeaderCardException, LongValueException {
        if (this.card == null) {
            this.card = new HeaderCard(this.key.key(), newValue, null);
            this.header.addLine(this.card);
        } else {
            this.card.setValue(newValue);
        }
        return this;
    }

    /**
     * Sets the number of decimals to show for the following decimal values. This method
     * is now deprecated. Use {@link #precision(int)} instead.
     * 
     * @param decimals     the new number of decimal places to show.
     * @return this
     * 
     * @deprecated Use {@link #precision(int)} instead.
     */
    @Deprecated
    public HeaderCardBuilder scale(int decimals) {
        this.precision = decimals;
        return this;
    }
    
    
    /**
     * Sets the number of decimals to show for the following decimal values. 
     * Trailing zeroes will be ommitted.
     * 
     * 
     * @param decimals     the number of decimals to show for the following decimal values. 
     * @return              this
     * 
     * @since 1.16
     */
    public HeaderCardBuilder precision(int decimals) {
        this.precision = decimals;
        return this;
    }

    /**
     * This method has been deprecated. Please use {@link #autoPrecision()} instead.
     * 
     * @return this
     * 
     * @deprecated Use {@link #autoPrecision()} instead
     */
    @Deprecated
    public HeaderCardBuilder noScale() {
        this.precision = -1;
        return this;
    }

    /**
     * Use the native precision for the given number type. Trailing zeroes will be
     * ommitted.
     * 
     * @return this
     * 
     * @since 1.16
     */
    public HeaderCardBuilder autoPrecision() {
        this.precision = -1;
        return this;
    }
    
    /**
     * @return the filled header.
     */
    public Header header() {
        return header;
    }
}

