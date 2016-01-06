package nom.tam.fits;

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

import java.io.IOException;

import nom.tam.util.ArrayDataInput;

/**
 * A helper class to keep track of the number of physical cards for a logical
 * card.
 * 
 * @author Richard van Nieuwenhoven
 */
public class HeaderCardCountingArrayDataInput {

    /**
     * the input stream.
     */
    private final ArrayDataInput input;

    /**
     * the number of 80 byte cards read.
     */
    private int physicalCardsRead;

    private int markedPhysicalCardsRead;

    protected HeaderCardCountingArrayDataInput(ArrayDataInput input) {
        this.input = input;
    }

    /**
     * @return the number of cards realy read form the stream
     */
    protected int getPhysicalCardsRead() {
        return physicalCardsRead;
    }

    /**
     * @return the stream to read the cards from
     */
    protected ArrayDataInput in() {
        return input;
    }

    /**
     * report a readed card.
     */
    public void cardRead() {
        physicalCardsRead++;
    }

    /**
     * mark the current position in the stream.
     * 
     * @throws IOException
     *             if the underlaying stream does not allow the mark.
     */
    public void mark() throws IOException {
        input.mark(HeaderCard.FITS_HEADER_CARD_SIZE);
        markedPhysicalCardsRead = physicalCardsRead;
    }

    /**
     * reset the stream th the last marked prosition.
     * 
     * @throws IOException
     *             if the underlaying stream does not allow the mark.
     */
    public void reset() throws IOException {
        input.reset();
        physicalCardsRead = markedPhysicalCardsRead;
    }

}
