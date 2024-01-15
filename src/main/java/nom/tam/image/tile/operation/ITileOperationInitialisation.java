package nom.tam.image.tile.operation;

import nom.tam.fits.FitsException;

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

/**
 * (<i>for internal use</i>) Interface for initializing parallel operations on 2D image tiles.
 *
 * @param <OPERATION> the generic type of the operation to be performed
 */
public interface ITileOperationInitialisation<OPERATION extends ITileOperation> {

    /**
     * Creates a new instance of the supported parallel tile operation for a specifictile
     * 
     * @param  tileIndex the sequential index of the tile
     * @param  area      the location and size of the tile in the full image
     * 
     * @return           a new parallel operation for processing the particular 2D image tile.
     */
    OPERATION createTileOperation(int tileIndex, TileArea area);

    /**
     * Initializes the parallel tile operation before it can be executed. For example to set particular options /
     * parameters for the processing.
     * 
     * @param tileOperation the parallel operation that processes a specific 2D image tile.
     */
    void init(OPERATION tileOperation);

    /**
     * Sets the total number of 2D image tiles to be processed.
     * 
     * @param  tileCount     the total number of image tiles to be
     * 
     * @throws FitsException if the operation cannot support the desired number of tiles, for example because data
     *                           stored in a FITS file is for fewer tiles.
     */
    void tileCount(int tileCount) throws FitsException;
}
