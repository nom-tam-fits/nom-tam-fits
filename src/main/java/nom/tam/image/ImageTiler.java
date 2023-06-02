/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nom.tam.image;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2021 nom-tam-fits
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

/**
 * Image tiling interface. This interface supports general multi-dimensional tiling. However, FITS tiles are always
 * 2-dimentional, but really images of any dimensions may be covered with 2D tiles.
 * 
 * @author tmcglynn
 */
public interface ImageTiler {

    /**
     * Returns the entire image reconstructed from the available tiles.
     * 
     * @return             the complete reconstructed image from the available tiles, as a Java array. This may be an
     *                         array of promitives (such as <code>float[][]</code>) for images, or an
     *                         <code>Object[]</code> for binary tables.
     * 
     * @throws IOException if there was an error accessing the tile data from the input.
     * 
     * @see                #getTile(int[], int[])
     */
    Object getCompleteImage() throws IOException;

    /**
     * Returns a tile from the image of the specified size at the specified location. An image tile is returned as a
     * one-dimensional array although the image will normally be multidimensional.
     * 
     * @param  start       the pixels indices where the tile starts in the full image. The array =hould contain a value
     *                         for each image dimension.
     * @param  lengths     the tile size in pixels. The array should contain a value for each image dimension. For the
     *                         supported 2D tiles, the values beyond the first two entries should be set to 1.
     * 
     * @return             a Java array containing data for the requested tile. This will always be a flattened 1D
     *                         array, even if the image is multidimensional
     * 
     * @throws IOException if there was an error accessing the tile data from the input.
     * 
     * @see                #getTile(Object, int[], int[])
     * @see                #getTile(int[], int[], int[])
     * @see                #getCompleteImage()
     */
    Object getTile(int[] start, int[] lengths) throws IOException;

    /**
     * Returns a sparsely sampled tile from the image of the specified size at the specified location.
     * 
     * @param  start       the pixels indices where the tile starts in the full image. The array =hould contain a value
     *                         for each image dimension.
     * @param  lengths     the tile size in pixels. The array should contain a value for each image dimension. For the
     *                         supported 2D tiles, the values beyond the first two entries should be set to 1.
     * @param  steps       the sampling frequency of the original image, in pixels. The array =hould contain a value for
     *                         each image dimension.
     * 
     * @return             a Java array containing data for the requested tile. This will always be a flattened 1D
     *                         array, even if the image is multidimensional
     * 
     * @throws IOException if there was an error accessing the tile data from the input.
     * 
     * @see                #getTile(Object, int[], int[], int[])
     * @see                #getTile(int[], int[])
     * @see                #getCompleteImage()
     */
    default Object getTile(int[] start, int[] lengths, int[] steps) throws IOException {
        throw new UnsupportedOperationException("Striding feature not yet implemented.");
    }

    /**
     * Fills the supplied array or output stream with the data from an image tile of the specified size at the specified
     * location.
     * 
     * @param  output      A one-dimensional output array or stream. Data not within the valid limits of the image will
     *                         be left unchanged. For an array, the length should be the product of lengths. Optionally
     *                         provide an {@link nom.tam.util.ArrayDataOutput} to stream out data and not fill memory;
     *                         useful for web applications.
     * @param  start       the pixels indices where the tile starts in the full image. The array =hould contain a value
     *                         for each image dimension.
     * @param  lengths     the tile size in pixels. The array =hould contain a value for each image dimension. For the
     *                         supported 2D tiles, the values beyond the first two entries should be set to 1.
     * 
     * @throws IOException if there was an error writing the tile to the output.
     * 
     * @see                #getTile(Object, int[], int[])
     * @see                #getTile(int[], int[], int[])
     * @see                #getCompleteImage()
     */
    void getTile(Object output, int[] start, int[] lengths) throws IOException;

    /**
     * Fills the supplied array our output stream with the sparsely sampled data from an image tile of the specified
     * size at the specified location.
     * 
     * @param  output      A one-dimensional output array or stream. Data not within the valid limits of the image will
     *                         be left unchanged. For an array, the length should be the product of lengths. Optionally
     *                         provide an {@link nom.tam.util.ArrayDataOutput} to stream out data and not fill memory;
     *                         useful for web applications.
     * @param  start       the pixels indices where the tile starts in the full image. The array =hould contain a value
     *                         for each image dimension.
     * @param  lengths     the tile size in pixels. The array =hould contain a value for each image dimension. For the
     *                         supported 2D tiles, the values beyond the first two entries should be set to 1.
     * @param  steps       the sampling frequency of the original image, in pixels. The array =hould contain a value for
     *                         each image dimension.
     * 
     * @throws IOException if there was an error writing the tile to the output.
     * 
     * @see                #getTile(Object, int[], int[])
     * @see                #getTile(int[], int[], int[])
     * @see                #getCompleteImage()
     */
    default void getTile(Object output, int[] start, int[] lengths, int[] steps) throws IOException {
        throw new UnsupportedOperationException("Striding feature not yet implemented.");
    }
}
