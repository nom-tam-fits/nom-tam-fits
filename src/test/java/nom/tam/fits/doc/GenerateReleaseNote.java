package nom.tam.fits.doc;

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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GenerateReleaseNote {

    public static void main(String[] args) throws Exception {
        File fXmlFile = new File("src/changes/changes.xml");
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);
        doc.getDocumentElement().normalize();
        Element release = (Element) doc.getElementsByTagName("release").item(0);

        String version = release.getAttribute("version");
        String description = release.getAttribute("description");

        String fileName = "NOTE.v" + version.substring(0, version.indexOf('.'))
                + version.substring(version.indexOf('.') + 1);

        try (PrintStream out = new PrintStream(new File("target/" + fileName))) {
            out.print("Release ");
            out.println(version);
            out.println();
            println(out, limitString(description, 80));
            out.println();
            NodeList nodes = release.getElementsByTagName("action");
            for (int index = 0; index < nodes.getLength(); index++) {
                Element child = (Element) nodes.item(index);
                String dev = child.getAttribute("dev");
                String issue = child.getAttribute("issue");
                if (isEmptyOrNull(dev) && isEmptyOrNull(issue)) {
                    println(out, limitString(child.getTextContent().trim(), 80));
                    out.println();
                }
            }
            out.println("Other changes in this edition include:");
            out.println();

            for (int index = 0; index < nodes.getLength(); index++) {
                Element child = (Element) nodes.item(index);
                String dev = child.getAttribute("dev");
                String issue = child.getAttribute("issue");
                if (!isEmptyOrNull(dev) || !isEmptyOrNull(issue)) {
                    println(out, "     - ", limitString(child.getTextContent().trim(), 72));
                }
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void println(PrintStream out, List<String> limitString) {
        println(out, "", limitString);
    }

    private static void println(PrintStream out, String prefix, List<String> limitString) {
        for (String string : limitString) {
            out.print(prefix);
            out.println(string);
            prefix = "                                                       ".substring(0, prefix.length());
        }
    }

    static boolean isEmptyOrNull(String value) {
        return value == null || value.trim().isEmpty();
    }

    static List<String> limitString(String string, int size) {
        string = string.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').replace("  ", " ").replace("  ", " ")
                .replace("  ", " ");
        List<String> result = new ArrayList<>();
        while (string.length() > 80) {
            int split = 80;
            while (!Character.isWhitespace(string.charAt(split))) {
                split--;
            }
            result.add(string.substring(0, split));
            string = string.substring(split + 1);
        }
        result.add(string);
        return result;
    }
}
