package nom.tam.fits.header;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 1996 - 2015 nom-tam-fits
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
 * Specify user defined comments for standard keywords.
 * 
 * @author ritchie
 */
class StandardCommentReplacement {

    private String comment;

    private final Class<?> context;

    private final String ref;

    protected StandardCommentReplacement(String ref, Class<?> context) {
        this.ref = ref;
        this.context = context;
        this.comment = null;
    }

    protected StandardCommentReplacement(String ref, Class<?> context, String comment) {
        this.ref = ref;
        this.context = context;
        this.comment = comment;
    }

    public String getComment() {
        return this.comment;
    }

    public Class<?> getContext() {
        return this.context;
    }

    public String getRef() {
        return this.ref;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
