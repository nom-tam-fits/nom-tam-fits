package nom.tam.image.tile.operation;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2024 nom-tam-fits
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings("javadoc")
public class TileAreaTest {

    @Test
    public void testIntersect() {
        TileArea middle = new TileArea().start(140, 140).end(160, 160);

        Assertions.assertTrue(new TileArea().start(0, 150).end(300, 165).intersects(middle));
        Assertions.assertFalse(new TileArea().start(0, 100).end(300, 115).intersects(middle));
        Assertions.assertFalse(new TileArea().start(15, 0).end(30, 300).intersects(middle));
        Assertions.assertFalse(new TileArea().start(170, 0).end(185, 300).intersects(middle));
        Assertions.assertFalse(new TileArea().start(0, 170).end(300, 175).intersects(middle));

    }

    @Test
    public void testIntersectException() throws Exception {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {

            TileArea middle = new TileArea().start(140, 140).end(160, 160);
            middle.intersects(new TileArea().start(2, 3, 4));

        });
    }

    @Test
    public void tileAreaSubsizeTest() throws Exception {
        TileArea area = new TileArea().start(2, 3, 4).size(5);
        Assertions.assertTrue(area.intersects(new TileArea().start(6, 3, 4).size(1)));
        Assertions.assertFalse(area.intersects(new TileArea().start(7, 3, 4).size(1)));
        Assertions.assertFalse(area.intersects(new TileArea().start(6, 4, 4).size(1)));
        Assertions.assertFalse(area.intersects(new TileArea().start(6, 3, 5).size(1)));
    }

    @Test
    public void emptyTileTest() throws Exception {
        Assertions.assertEquals(0, new TileArea().dimension());
    }

}
