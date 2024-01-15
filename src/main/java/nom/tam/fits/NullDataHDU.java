package nom.tam.fits;

/*-
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

import java.io.PrintStream;

/**
 * A class of HDU that contains only a header only with no associated data. Such HDUs are commonly used as the primary
 * HDU in FITS files where the leading data is not an image, since only images may constitute the primary HDU.
 *
 * @author Attila Kovacs
 *
 * @since  1.18
 * 
 * @see    NullData
 */
@SuppressWarnings("deprecation")
public class NullDataHDU extends ImageHDU {

    /**
     * Instantiates a new HDU with a default header and no associated data.
     */
    public NullDataHDU() {
        super(new Header(), new NullData());
        getData().fillHeader(getHeader());
    }

    @Override
    public NullData getData() {
        return (NullData) super.getData();
    }

    /**
     * Instantiates a new HDU with only the supplied header and no associated data.
     *
     * @param myHeader the FITS header for this HDU
     */
    public NullDataHDU(Header myHeader) {
        super(myHeader, new NullData());
        getData().fillHeader(getHeader());
    }

    @Override
    public void info(PrintStream stream) {
        stream.println("  Header Only");
    }
}
