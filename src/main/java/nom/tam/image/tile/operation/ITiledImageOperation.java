package nom.tam.image.tile.operation;

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

import java.nio.Buffer;
import java.nio.ByteBuffer;

import nom.tam.fits.compression.algorithm.api.ICompressOption;
import nom.tam.fits.compression.algorithm.api.ICompressorControl;
import nom.tam.util.type.ElementType;

/**
 * 2D image tile compression interface. FITS tiles are always 2-dimentional, but really images of any dimensions may be
 * covered with such tiles.
 * 
 * @see TileArea
 * @see nom.tam.image.ImageTiler
 */
public interface ITiledImageOperation {
    /**
     * Retuers the tile compression options to use for tile compressing or uncompressing images.
     * 
     * @return the tile compression options
     */
    ICompressOption compressOptions();

    /**
     * Returns the element type of the image (and hence tile).
     * 
     * @return the FITS element type used in this tile.
     */
    ElementType<Buffer> getBaseType();

    /**
     * Returns a buffer containing the entire tile compressed image.
     * 
     * @return a buffer containing the tile compresed image in serialized form.
     */
    ByteBuffer getCompressedWholeArea();

    /**
     * Returns the class that can perform the tile compression or decompression with the desired tile compression
     * algorithm and options. Some image tiles may not be possible to compress with the chosen algorithm, and FITS then
     * allows alternative compression of these using GZIP instead (see {@link #getGzipCompressorControl()}
     * 
     * @return the class that can perform the desired tile compression.
     * 
     * @see    #getGzipCompressorControl()
     * @see    #compressOptions()
     */
    ICompressorControl getCompressorControl();

    /**
     * Return the class that can GZIP compress tiles that cannot be compressed otherwise. GZIP compression is failsafe
     * and is therefore a standard fallback option when compressing tiles. It may also be the desired first choice
     * method also.
     * 
     * @return the class that can perform the fail-safe tile compression using GZIP.
     * 
     * @see    #getCompressorControl()
     */
    ICompressorControl getGzipCompressorControl();

    /**
     * Returns the actual with of the image which is to be tile (de)compressed.
     * 
     * @return The width of the full image in pixels.
     */
    int getImageWidth();

    /**
     * Returns the operation that handles the parallel processing of specific tiles. Each tile can be processed by a
     * dedicated thread, with many tiles processing in parallel at the same time.
     * 
     * @param  i the sequential (flattened) index of the specific tile
     * 
     * @return   the operation instance that handles the parallel processing of that particular tiles.
     */
    ITileOperation getTileOperation(int i);
}
