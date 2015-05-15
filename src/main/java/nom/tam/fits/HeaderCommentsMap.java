/*
 * This class provides a modifiable map in which the comment fields for FITS
 * header keywords
 * produced by this library are set.  The map is a simple String -> String
 * map where the key Strings are normally class:keyword:id where class is
 * the class name where the keyword is set, keyword is the keyword set and id
 * is an integer used to distinguish multiple instances.
 *
 * Most users need not worry about this class, but users who wish to customize
 * the appearance of FITS files may update the map.  The code itself is likely
 * to be needed to understand which values in the map must be modified.
 *
 * Note that the Header writing utilities look for the prefix ntf:: in comments
 * and if this is found, the comment is replaced by looking in this map for
 * a key given by the remainder of the original comment.
 */

package nom.tam.fits;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
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

import java.util.HashMap;
import java.util.Map;

public class HeaderCommentsMap {

    private static Map<String, String> commentMap = new HashMap<String, String>();
    static {
        HeaderCommentsMap.commentMap.put("header:extend:1", "Extensions are permitted");
        HeaderCommentsMap.commentMap.put("header:simple:1", "Java FITS: " + new java.util.Date());
        HeaderCommentsMap.commentMap.put("header:xtension:1", "Java FITS: " + new java.util.Date());
        HeaderCommentsMap.commentMap.put("header:naxis:1", "Dimensionality");
        HeaderCommentsMap.commentMap.put("header:extend:2", "Extensions are permitted");
        HeaderCommentsMap.commentMap.put("asciitable:pcount:1", "No group data");
        HeaderCommentsMap.commentMap.put("asciitable:gcount:1", "One group");
        HeaderCommentsMap.commentMap.put("asciitable:tfields:1", "Number of fields in table");
        HeaderCommentsMap.commentMap.put("asciitable:tbcolN:1", "Column offset");
        HeaderCommentsMap.commentMap.put("asciitable:naxis1:1", "Size of row in bytes");
        HeaderCommentsMap.commentMap.put("undefineddata:naxis1:1", "Number of Bytes");
        HeaderCommentsMap.commentMap.put("undefineddata:extend:1", "Extensions are permitted");
        HeaderCommentsMap.commentMap.put("binarytablehdu:pcount:1", "Includes heap");
        HeaderCommentsMap.commentMap.put("binarytable:naxis1:1", "Bytes per row");
        HeaderCommentsMap.commentMap.put("fits:checksum:1", "as of " + FitsDate.getFitsDateString());
        HeaderCommentsMap.commentMap.put("basichdu:extend:1", "Allow extensions");
        HeaderCommentsMap.commentMap.put("basichdu:gcount:1", "Required value");
        HeaderCommentsMap.commentMap.put("basichdu:pcount:1", "Required value");
        HeaderCommentsMap.commentMap.put("imagedata:extend:1", "Extension permitted");
        HeaderCommentsMap.commentMap.put("imagedata:pcount:1", "No extra parameters");
        HeaderCommentsMap.commentMap.put("imagedata:gcount:1", "One group");
        HeaderCommentsMap.commentMap.put("tablehdu:tfields:1", "Number of table fields");
        /*
         * Null entries: header:bitpix:1 header:simple:2 header:bitpix:2
         * header:naxisN:1 header:naxis:2 undefineddata:pcount:1
         * undefineddata:gcount:1 randomgroupsdata:naxis1:1
         * randomgroupsdata:naxisN:1 randomgroupsdata:groups:1
         * randomgroupsdata:gcount:1 randomgroupsdata:pcount:1
         * binarytablehdu:theap:1 binarytablehdu:tdimN:1 asciitable:tformN:1
         * asciitablehdu:tnullN:1 asciitablehdu:tfields:1 binarytable:pcount:1
         * binarytable:gcount:1 binarytable:tfields:1 binarytable:tformN:1
         * binarytable:tdimN:1 tablehdu:naxis2:1
         */
    }

    public static void deleteComment(String key) {
        HeaderCommentsMap.commentMap.remove(key);
    }

    public static String getComment(String key) {
        return HeaderCommentsMap.commentMap.get(key);
    }

    public static void updateComment(String key, String comment) {
        HeaderCommentsMap.commentMap.put(key, comment);
    }
}
