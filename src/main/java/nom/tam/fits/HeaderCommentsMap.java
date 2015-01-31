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
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.util.HashMap;
import java.util.Map;

public class HeaderCommentsMap {

    private static Map<String, String> commentMap = new HashMap<String, String>();
    static {
        commentMap.put("header:extend:1", "Extensions are permitted");
        commentMap.put("header:simple:1", "Java FITS: " + new java.util.Date());
        commentMap.put("header:xtension:1", "Java FITS: " + new java.util.Date());
        commentMap.put("header:naxis:1", "Dimensionality");
        commentMap.put("header:extend:2", "Extensions are permitted");
        commentMap.put("asciitable:pcount:1", "No group data");
        commentMap.put("asciitable:gcount:1", "One group");
        commentMap.put("asciitable:tfields:1", "Number of fields in table");
        commentMap.put("asciitable:tbcolN:1", "Column offset");
        commentMap.put("asciitable:naxis1:1", "Size of row in bytes");
        commentMap.put("undefineddata:naxis1:1", "Number of Bytes");
        commentMap.put("undefineddata:extend:1", "Extensions are permitted");
        commentMap.put("binarytablehdu:pcount:1", "Includes heap");
        commentMap.put("binarytable:naxis1:1", "Bytes per row");
        commentMap.put("fits:checksum:1", "as of " + FitsDate.getFitsDateString());
        commentMap.put("basichdu:extend:1", "Allow extensions");
        commentMap.put("basichdu:gcount:1", "Required value");
        commentMap.put("basichdu:pcount:1", "Required value");
        commentMap.put("imagedata:extend:1", "Extension permitted");
        commentMap.put("imagedata:pcount:1", "No extra parameters");
        commentMap.put("imagedata:gcount:1", "One group");
        commentMap.put("tablehdu:tfields:1", "Number of table fields");
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

    public static String getComment(String key) {
        return commentMap.get(key);
    }

    public static void updateComment(String key, String comment) {
        commentMap.put(key, comment);
    }

    public static void deleteComment(String key) {
        commentMap.remove(key);
    }
}
