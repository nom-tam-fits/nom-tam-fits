package nom.tam.util;

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
import java.io.IOException;

/**
 * @deprecated Use {@link FitsFile} instead, which replaces the old <code>BufferedFile</code> with a less
 * misleading name, or else {@link ArrayDataFile}, which provides a base for a more more generic implementation
 * for efficient reading/writing arrays using any (non-FITS) encoding.
 *
 * @see FitsFile
 */
@Deprecated
public class BufferedFile extends FitsFile {

    public BufferedFile(File file, String mode, int bufferSize) throws IOException {
        super(file, mode, bufferSize);
    }

    public BufferedFile(File file, String mode) throws IOException {
        super(file, mode);
    }

    public BufferedFile(File file) throws IOException {
        super(file);
    }

    public BufferedFile(String filename, String mode, int bufferSize) throws IOException {
        super(filename, mode, bufferSize);
    }

    public BufferedFile(String filename, String mode) throws IOException {
        super(filename, mode);
    }

    public BufferedFile(String filename) throws IOException {
        super(filename);
    }

}
