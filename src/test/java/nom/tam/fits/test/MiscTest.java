package nom.tam.fits.test;

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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import nom.tam.fits.Fits;

public class MiscTest {

    private String extractComment(String string, int end) {
        int startOfComment = string.indexOf('/', end);
        int endOfComment = string.length() - 1;
        if (startOfComment > 0 && endOfComment > startOfComment) {
            startOfComment++;
            if (Character.isWhitespace(string.charAt(startOfComment))) {
                startOfComment++;
            }
            while (Character.isWhitespace(string.charAt(endOfComment))) {
                endOfComment--;
            }
            if (!Character.isWhitespace(string.charAt(endOfComment))) {
                endOfComment++;
            }
            if (endOfComment > startOfComment) {
                return string.substring(startOfComment, endOfComment);
            }
        }
        return null;
    }

    private String replaceAll(Pattern doubleQuotePattern, String quotedString) {
        Matcher doubleQuoteMatcher = doubleQuotePattern.matcher(quotedString);
        StringBuffer sb = new StringBuffer();
        if (doubleQuoteMatcher.find(1)) {
            do {
                doubleQuoteMatcher.appendReplacement(sb, "'");
            } while (doubleQuoteMatcher.find());
        }
        doubleQuoteMatcher.appendTail(sb);
        sb.deleteCharAt(0);
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    @Test
    public void testLibVersion() throws Exception {
        Properties props = new Properties();
        props.put("version", "1.1.1-SNAPSHOT");
        File propFile = new File("target/test-classes/META-INF/maven/gov.nasa.gsfc.heasarc/nom-tam-fits/pom.properties");
        propFile.getParentFile().mkdirs();
        OutputStream out = new FileOutputStream(propFile);
        props.store(out, "test version");
        out.close();
        String version = Fits.version();
        assertEquals("1.1.1-SNAPSHOT", version);
    }

    @Test
    public void testRegex() throws Exception {
        Pattern doubleQuotePattern = Pattern.compile("''");
        Pattern stringPattern = Pattern.compile("'(?:[^']|'{2})*'");
        String[] testStrings = {"xx''   ", "xx'test'   / ", "xx''''  ", "xx'test''' /    ", "xx'test''a''aaa' /",
                "xx'' / yy", "xx'test' / yy", "xx'''' / yy    ", "xx'test''' / yy    ",
                "xx'test''a''aaa' / yy                    ", "xx'test''a''aaa              ' / yy                    ",};
        for (String string : testStrings) {
            try {
                System.out.println("---" + string + "---");
                Matcher matcher = stringPattern.matcher(string);
                while (matcher.find()) {
                    String result = replaceAll(doubleQuotePattern, matcher.group(0));
                    String comment = extractComment(string, matcher.end());

                    System.out.println(result);
                    System.out.println("comment:" + comment);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
