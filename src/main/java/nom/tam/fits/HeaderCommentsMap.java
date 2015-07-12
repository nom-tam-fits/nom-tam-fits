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

import static nom.tam.fits.header.Standard.BITPIX;
import static nom.tam.fits.header.Standard.EXTEND;
import static nom.tam.fits.header.Standard.GCOUNT;
import static nom.tam.fits.header.Standard.GROUPS;
import static nom.tam.fits.header.Standard.NAXIS;
import static nom.tam.fits.header.Standard.NAXISn;
import static nom.tam.fits.header.Standard.PCOUNT;
import static nom.tam.fits.header.Standard.SIMPLE;
import static nom.tam.fits.header.Standard.TBCOLn;
import static nom.tam.fits.header.Standard.TDIMn;
import static nom.tam.fits.header.Standard.TFIELDS;
import static nom.tam.fits.header.Standard.TFORMn;
import static nom.tam.fits.header.Standard.THEAP;
import static nom.tam.fits.header.Standard.XTENSION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HeaderCommentsMap {

    static class StdCommentReplacement {

        private String key;

        private Class<?> context;

        private String ref;

        private String comment;

        public StdCommentReplacement(String ref, String key, Class<?> context, String comment) {
            this.ref = ref;
            this.key = key;
            this.context = context;
            this.comment = comment;
        }

        public StdCommentReplacement(String ref, String key, String comment) {
            this.ref = ref;
            this.key = key;
            this.context = Object.class;
            this.comment = comment;
        }

        public StdCommentReplacement(String ref, String key, Class<?> context) {
            this.ref = ref;
            this.key = key;
            this.context = context;
            this.comment = null;
        }

        public StdCommentReplacement(String ref, String key) {
            this.ref = ref;
            this.key = key;
            this.context = Object.class;
            this.comment = null;
        }
    }

    private static final Map<String, String> COMMENT_MAP = new HashMap<String, String>();

    private static final Map<String, List<StdCommentReplacement>> COMMENT_KEYS = new HashMap<>();
    static {
        addCommentRepacement(new StdCommentReplacement("asciitable:gcount", GCOUNT.key(), AsciiTable.class));
        addCommentRepacement(new StdCommentReplacement("asciitablehdu:tfields", TFIELDS.key(), AsciiTable.class));
        addCommentRepacement(new StdCommentReplacement("asciitable:naxis1", NAXISn.key(), AsciiTable.class, "Size of row in bytes"));
        addCommentRepacement(new StdCommentReplacement("asciitable:pcount", PCOUNT.key(), AsciiTable.class, "No group data"));
        addCommentRepacement(new StdCommentReplacement("asciitable:tbcolN", TBCOLn.key(), AsciiTable.class, "Column offset"));
        addCommentRepacement(new StdCommentReplacement("asciitable:tfields", TFIELDS.key(), AsciiTable.class, "Number of fields in table"));
        addCommentRepacement(new StdCommentReplacement("asciitable:tformN", TFORMn.key(), AsciiTable.class));
        addCommentRepacement(new StdCommentReplacement("basichdu:extend", EXTEND.key(), "Allow extensions"));
        addCommentRepacement(new StdCommentReplacement("basichdu:gcount", GCOUNT.key(), "Required value"));
        addCommentRepacement(new StdCommentReplacement("basichdu:pcount", PCOUNT.key(), "Required value"));
        addCommentRepacement(new StdCommentReplacement("binarytable:gcount", GCOUNT.key(), BinaryTable.class));
        addCommentRepacement(new StdCommentReplacement("binarytablehdu:pcount", PCOUNT.key(), BinaryTable.class, "Includes heap"));
        addCommentRepacement(new StdCommentReplacement("binarytablehdu:tdimN", TDIMn.key(), BinaryTable.class));
        addCommentRepacement(new StdCommentReplacement("binarytablehdu:theap", THEAP.key(), BinaryTable.class));
        addCommentRepacement(new StdCommentReplacement("binarytable:naxis1", NAXISn.key(), BinaryTable.class, "Bytes per row"));
        addCommentRepacement(new StdCommentReplacement("binarytable:pcount", PCOUNT.key(), BinaryTable.class));
        addCommentRepacement(new StdCommentReplacement("binarytable:tfields", TFIELDS.key(), BinaryTable.class));
        addCommentRepacement(new StdCommentReplacement("binarytable:tformN", TFORMn.key(), BinaryTable.class));
        addCommentRepacement(new StdCommentReplacement("header:bitpix", BITPIX.key()));
        addCommentRepacement(new StdCommentReplacement("headercard:tdimN", TDIMn.key()));
        addCommentRepacement(new StdCommentReplacement("header:extend", EXTEND.key(), "Extensions are permitted"));
        addCommentRepacement(new StdCommentReplacement("header:naxis", NAXIS.key(), "Dimensionality"));
        addCommentRepacement(new StdCommentReplacement("header:naxisN", NAXISn.key()));
        addCommentRepacement(new StdCommentReplacement("header:simple", SIMPLE.key(), "Java FITS: " + new java.util.Date()));
        addCommentRepacement(new StdCommentReplacement("header:xtension", XTENSION.key(), "Java FITS: " + new java.util.Date()));
        addCommentRepacement(new StdCommentReplacement("imagedata:extend", EXTEND.key(), ImageData.class, "Extension permitted"));
        addCommentRepacement(new StdCommentReplacement("imagedata:gcount", GCOUNT.key(), ImageData.class, "No extra parameters"));
        addCommentRepacement(new StdCommentReplacement("imagedata:pcount", PCOUNT.key(), ImageData.class, "One group"));
        addCommentRepacement(new StdCommentReplacement("randomgroupsdata:gcount", GCOUNT.key(), RandomGroupsData.class));
        addCommentRepacement(new StdCommentReplacement("randomgroupsdata:groups", GROUPS.key(), RandomGroupsData.class));
        addCommentRepacement(new StdCommentReplacement("randomgroupsdata:naxis1", NAXISn.key(), RandomGroupsData.class));
        addCommentRepacement(new StdCommentReplacement("randomgroupsdata:naxisN", NAXISn.key(), RandomGroupsData.class));
        addCommentRepacement(new StdCommentReplacement("randomgroupsdata:pcount", PCOUNT.key(), RandomGroupsData.class));
        addCommentRepacement(new StdCommentReplacement("tablehdu:naxis2", NAXISn.key(), TableData.class));
        addCommentRepacement(new StdCommentReplacement("tablehdu:tfields", TFIELDS.key(), TableData.class, "Number of table fields"));
        addCommentRepacement(new StdCommentReplacement("undefineddata:extend", EXTEND.key(), UndefinedData.class, "Extensions are permitted"));
        addCommentRepacement(new StdCommentReplacement("undefineddata:gcount", GCOUNT.key(), UndefinedData.class));
        addCommentRepacement(new StdCommentReplacement("undefineddata:naxis1", NAXISn.key(), UndefinedData.class, "Number of Bytes"));
        addCommentRepacement(new StdCommentReplacement("undefineddata:pcount", PCOUNT.key(), UndefinedData.class));
    };

    private static final ThreadLocal<Class<?>> CONTEXT = new ThreadLocal<>();

    public static void set(Class<?> clazz) {
        CONTEXT.set(clazz);
    }

    private static void addCommentRepacement(StdCommentReplacement stdCommentReplacement) {
        List<StdCommentReplacement> listOfCommentReplace = COMMENT_KEYS.get(stdCommentReplacement.key);
        if (listOfCommentReplace == null) {
            listOfCommentReplace = new ArrayList<>();
            COMMENT_KEYS.put(stdCommentReplacement.key, listOfCommentReplace);
        }
        if (stdCommentReplacement.context == Object.class) {
            listOfCommentReplace.add(0, stdCommentReplacement);
        } else {
            listOfCommentReplace.add(stdCommentReplacement);
        }
        COMMENT_MAP.put(stdCommentReplacement.ref, stdCommentReplacement.comment);
    }

    private HeaderCommentsMap() {
    }

    public static void deleteComment(String key) {
        key = simplyfyKey(key);
        setStdCommentText(key, "");
        HeaderCommentsMap.COMMENT_MAP.remove(key);
    }

    private static void setStdCommentText(String key, String commentText) {
        for (List<StdCommentReplacement> commentList : COMMENT_KEYS.values()) {
            for (StdCommentReplacement replacement : commentList) {
                if (replacement.ref.equals(key)) {
                    replacement.comment = commentText;
                }
            }
        }
    }

    public static String getComment(String key) {
        key = simplyfyKey(key);
        return HeaderCommentsMap.COMMENT_MAP.get(key);
    }

    public static void updateComment(String key, String comment) {
        key = simplyfyKey(key);
        setStdCommentText(key, comment);
        HeaderCommentsMap.COMMENT_MAP.put(key, comment);
    }

    private static String simplyfyKey(String key) {
        int firstDbPoint = key.indexOf(':');
        if (firstDbPoint > 0) {
            int secondDoublePoint = key.indexOf(':', firstDbPoint + 1);
            if (secondDoublePoint > 0) {
                return key.substring(0, secondDoublePoint);
            }
        }
        return key;
    }

    public static String getUserdefinedComment(String key, String comment) {
        List<StdCommentReplacement> comments = COMMENT_KEYS.get(key);
        if (comments != null) {
            Class<?> contextClass = CONTEXT.get();
            if (contextClass == null) {
                contextClass = Object.class;
            }
            for (StdCommentReplacement stdCommentReplacement : comments) {
                if (stdCommentReplacement.context.isAssignableFrom(contextClass)) {
                    comment = stdCommentReplacement.comment;
                    if (comment != null) {
                        return comment;
                    }
                }
            }
        }
        return comment;
    }
}
