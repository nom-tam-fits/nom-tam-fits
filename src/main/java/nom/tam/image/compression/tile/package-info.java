/**
 * <p>
 * (<i>for internal use</i>) Tile compression support for images and tables. By dividing up large images into smaller
 * 'tiles' and compressing them separately, it is easy to retrieve select areas of the image without having to
 * decompress the whole thing. Thus it provides a more efficient way of storing large images while maintaining efficient
 * access to any part of it on demand.
 * </p>
 * <p>
 * The classes of these package are used internally, e.g. by {@link nom.tam.image.compression.hdu.CompressedImageHDU}
 * and {@link nom.tam.image.compression.hdu.CompressedTableHDU}, which are typically the main compression classes the
 * user will interact with.
 * </p>
 * 
 * @see nom.tam.image
 * @see nom.tam.image.compression.hdu.CompressedImageHDU
 * @see nom.tam.image.compression.hdu.CompressedTableHDU
 */

package nom.tam.image.compression.tile;

/*-
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2023 nom-tam-fits
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