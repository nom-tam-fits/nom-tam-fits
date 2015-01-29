/*
 * This class provides conversions to ASCII strings without breaking
 * compatibility with Java 1.5.
 */
package nom.tam.util;

/*
 * #%L
 * nom.tam FITS library
 * %%
 * Copyright (C) 2004 - 2015 nom-tam-fits
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tmcglynn
 */
public class AsciiFuncs {

    public final static String ASCII = "US-ASCII";

    /** Convert to ASCII or return null if not compatible */
    public static String asciiString(byte[] buf) {
        return asciiString(buf, 0, buf.length);
    }

    /** Convert to ASCII or return null if not compatible */
    public static String asciiString(byte[] buf, int start, int len) {
        try {
            return new String(buf, start, len, ASCII);
        } catch (java.io.UnsupportedEncodingException e) {
            // Shouldn't happen
            System.err.println("AsciiFuncs.asciiString error finding ASCII encoding");
            return null;
        }
    }

    /** Convert an ASCII string to bytes */
    public static byte[] getBytes(String in) {
        try {
            return in.getBytes(ASCII);
        } catch (UnsupportedEncodingException ex) {
            System.err.println("Unable to find ASCII encoding");
            return null;
        }
    }
}
