package nom.tam.fits;

/*-
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2022 nom-tam-fits
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
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;

public class DeferredTest {

    @Test
    public void isDeferredAsciiTableNew() throws Exception {
        assertFalse(new AsciiTable().isDeferred());
    }

    @Test
    public void isDeferredBinaryTableNew() throws Exception {
        assertFalse(new BinaryTable().isDeferred());
    }

    @Test
    public void isDeferredImageDataNew() throws Exception {
        assertFalse(new ImageData().isDeferred());
    }

    @Test
    public void isDeferredRandomGroupsDataNew() throws Exception {
        assertFalse(new RandomGroupsData().isDeferred());
    }

    @Test
    public void isDeferredUndefinedDataNew() throws Exception {
        assertFalse(new UndefinedData(new int[10]).isDeferred());
    }

    @Test
    public void isDeferredDataNew() throws Exception {
        assertFalse(new DefaultData().isDeferred());
    }

    @Test
    public void deferredDataRewrite() throws Exception {
        new DefaultData() {
            @Override
            public boolean isDeferred() {
                return true;
            }
        }.rewrite(); // No exception is good enough!
    }

    class DefaultData extends Data {
        @Override
        protected void fillHeader(Header head) throws FitsException {
        }

        @Override
        public Object getData() throws FitsException {
            return null;
        }

        @Override
        protected long getTrueSize() {
            return 0;
        }

        @Override
        public void read(ArrayDataInput in) throws FitsException {
            throw new FitsException("not implemented");
        }

        @Override
        public void write(ArrayDataOutput o) throws FitsException {
            throw new FitsException("not implemented");
        }

        @Override
        protected Object getCurrentData() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected void loadData(ArrayDataInput in) {
            // TODO Auto-generated method stub

        }
    }
}
