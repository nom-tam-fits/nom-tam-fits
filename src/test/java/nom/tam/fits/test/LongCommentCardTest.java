package nom.tam.fits.test;

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

import java.io.File;
import java.io.FileOutputStream;

import org.junit.Assert;
import org.junit.Test;

import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.LongValueException;
import nom.tam.fits.header.Standard;
import nom.tam.util.Cursor;
import nom.tam.util.FitsOutputStream;

public class LongCommentCardTest {

    private static int length = 200;
    private static boolean enableLongKeywords = true;

    @Test
    public void test() throws Exception {
        length = 200;

        boolean longEnabled = FitsFactory.isLongStringsEnabled();
        try {
            // Enable/disable tthe OGIP 1.0 convention for long entries as
            // requested...
            FitsFactory.setLongStringsEnabled(enableLongKeywords);

            // Create a header only HDU (no data)
            Header header = new Header();
            header.setSimple(true);
            header.setBitpix(32);
            header.setNaxes(0);

            // Add a regular keyword of the desired length...
            header.addLine(new HeaderCard("BLABERY", counts(length), ""));

            // Add a non-nullable HISTORY entry with the desired length...
            header.addLine(new HeaderCard("HISTORY", counts(length), false));

            // Add a non-nullable COMMENT entry with the desired length...
            header.addLine(HeaderCard.createCommentCard(counts(HeaderCard.MAX_COMMENT_CARD_COMMENT_LENGTH)));

            boolean thrown = false;
            try {
                header.addLine(HeaderCard.createCommentCard(counts(HeaderCard.MAX_COMMENT_CARD_COMMENT_LENGTH + 1)));
            } catch (LongValueException e) {
                thrown = true;
            }
            Assert.assertTrue(thrown);

            int n = 2;

            n += header.insertHistory(counts(length));
            n += header.insertComment(counts(length));

            // Write the result to 'longcommenttest.fits' in the user's home...
            Fits fits = new Fits();
            fits.addHDU(Fits.makeHDU(header));
            File file = new File("target/longcommenttest.fits");
            FitsOutputStream stream = new FitsOutputStream(new FileOutputStream(file));

            try {
                fits.write(stream);
            } catch (FitsException e) {
                throw e;
            } finally {
                stream.close();
            }
            Assert.assertEquals(2880L, file.length());
            int commentLike = 0;
            fits = new Fits(file);
            Cursor<String, HeaderCard> iterator = fits.getHDU(0).getHeader().iterator();
            while (iterator.hasNext()) {
                HeaderCard headerCard = iterator.next();
                if (headerCard.isCommentStyleCard() && !Standard.END.key().equals(headerCard.getKey())) {
                    commentLike++;
                    Assert.assertTrue(headerCard.getComment(),
                            headerCard.getComment().length() <= HeaderCard.MAX_COMMENT_CARD_COMMENT_LENGTH);
                }
            }
            Assert.assertEquals(n, commentLike);

        } finally {
            FitsFactory.setLongStringsEnabled(longEnabled);
        }

    }

    public String counts(int n) {
        StringBuffer buf = new StringBuffer();
        for (int i = 1; i <= n; i++) {
            buf.append(i % 10);
        }
        return new String(buf);
    }

}
