/**
 * <p>
 * Standardized FITS header keywords. In addition to the reserved keywords defined by the FITS standard, there are
 * conventions and commonly adopted usage patterns. Many organisations have defined their own sets of keywords also. To
 * help users conform to the standards and conventions, the library offers a collection the standard and commonly used
 * FITS keywords.
 * </p>
 * <p>
 * Using these 'standardized' keywords can now make compiler checked references to keywords (No more pruney String
 * references), and prevents improper usage by restricting the types of HDUs keywords may appear in, as well as what
 * type of values they may be associated with. See
 * {@link nom.tam.fits.Header#setKeywordChecking(nom.tam.fits.Header.KeywordCheck)} and
 * {@link nom.tam.fits.HeaderCard#setValueCheckingPolicy(nom.tam.fits.HeaderCard.ValueCheck)} on controlling what
 * checking is done on keyword usage and how violations are dealt with.
 * </p>
 * <p>
 * Some of these dictionaries contain keywords that have equivalent usage. These are listed in
 * {@link nom.tam.fits.header.Synonyms}.
 * </p>
 */
package nom.tam.fits.header;

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
