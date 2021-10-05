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

package nom.tam.util;

import java.util.StringTokenizer;

import nom.tam.fits.FitsFactory;
import nom.tam.fits.LongValueException;

public class ComplexValue {

    private double re, im;
    
    public ComplexValue() {
        re = 0.0;
        im = 0.0;
    }
    
    public ComplexValue(double re, double im) {
        this.re = re;
        this.im = im;
    }
    
    public ComplexValue(String text) throws IllegalArgumentException {  
        this();
        
        // Allow the use of 'D' or 'd' to mark the exponent, instead of the standard 'E' or 'e'...
        text = text.trim().toUpperCase().replace('D', 'E');
        
        boolean hasOpeningBracket = text.charAt(0) == '(';
        boolean hasClosingBracket = text.charAt(text.length() - 1) == ')';
        
        if (!hasOpeningBracket && !hasClosingBracket) {
            // Use just the real value.
            re = Double.parseDouble(text);
            return;
        }
        
        if (!hasClosingBracket) {
            if (!FitsFactory.isAllowHeaderRepairs()) {
                throw new IllegalArgumentException("Unfinished complex value: " + text 
                        + "\n\n --> Try FitsFactory.setAllowHeaderRepair(true).\n");
            }
        }
        
        int end = hasClosingBracket ? text.length() - 1 : text.length();
        StringTokenizer tokens = new StringTokenizer(text.substring(1, end), ",; \t");
        if (tokens.countTokens() != 2) {
            if (!FitsFactory.isAllowHeaderRepairs()) {
                throw new IllegalArgumentException("Invalid complex value: " + text 
                        + "\n\n --> Try FitsFactory.setAllowHeaderRepair(true).\n");
            }
        }
        
        if (tokens.hasMoreTokens()) {
            re = Double.parseDouble(tokens.nextToken());
        }
        if (tokens.hasMoreTokens()) {
            im = Double.parseDouble(tokens.nextToken());
        }
    }
    
    public final boolean isFinite() {
        return Double.isFinite(re) && Double.isFinite(im);
    }
    
    public final double re() {
        return re;
    }
    
    public final double im() {
        return im;
    }
    
    @Override
    public String toString() {
        return "(" + re + "," + im + ")";
    }
    
    public String toString(int decimals) {
        FlexFormat f = new FlexFormat().setPrecision(decimals); 
        return "(" + f.format(re) + "," + f.format(im) + ")";
    }
    
    public String toBoundedString(int maxLength) throws LongValueException {
        if (maxLength < MIN_STRING_LENGTH) {
            throw new LongValueException(maxLength, toString());
        }
        
        int decimals = DOUBLE_PRECISION;
        
        String s = toString(decimals);
        while (s.length() > maxLength) {
            // Assume both real and imaginary parts shorten the same amount... 
            decimals -= (s.length() - maxLength + 1) / 2;
            
            if (decimals < 0) {
                throw new LongValueException(maxLength, toString());
            }
            s = toString(decimals);
        }
        
        return s;
    }
   
    private static final int DOUBLE_PRECISION = 16;
    
    private static final int MIN_STRING_LENGTH = 5;     // "(#,#)"
}
