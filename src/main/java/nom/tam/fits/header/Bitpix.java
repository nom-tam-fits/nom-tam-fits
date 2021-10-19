package nom.tam.fits.header;

import java.util.logging.Logger;

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

import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.util.type.ElementType;

/**
 * Standard BITPIX values and associated functions. Since the FITS BITPIX keyword has 
 * only a handful of legal values, an <code>enum</code> provides ideal safe
 * representation. It also allows to interface the value for the type of data
 * it represents in a natural way.
 * 
 * 
 * @author Attila Kovacs
 *
 * @since 1.16
 */
public enum Bitpix {
    BYTE(Byte.TYPE, ElementType.BYTE, "bytes"),
    SHORT(Short.TYPE, ElementType.SHORT, "16-bit integers"),
    INTEGER(Integer.TYPE, ElementType.INT, "32-bit integers"),
    LONG(Long.TYPE, ElementType.LONG, "64-bit integers"),
    FLOAT(Float.TYPE, ElementType.FLOAT, "32-bit floating point"),
    DOUBLE(Double.TYPE, ElementType.DOUBLE, "64-bit floating point");
    
    private static final Logger LOG = Logger.getLogger("nom.tam.fits.HeaderCardParser");
    
    private static final int BITS_TO_BYTES_SHIFT = 3;
    
    /** BITPIX value for <code>byte</code> type data */
    public static final int BITPIX_BYTE = 8;
    
    /** BITPIX value for <code>short</code> type data */
    public static final int BITPIX_SHORT = 16;
    
    /** BITPIX value for <code>int</code> type data */
    public static final int BITPIX_INT = 32;
    
    /** BITPIX value for <code>long</code> type data */
    public static final int BITPIX_LONG = 64;
    
    /** BITPIX value for <code>float</code> type data */
    public static final int BITPIX_FLOAT = -32;
    
    /** BITPIX value for <code>double</code> type data */
    public static final int BITPIX_DOUBLE = -64;
    
    
    /** the number subclass represented this BITPIX instance */
    private Class<? extends Number> numberType;
    
    /** the library's element type */
    private ElementType<?> elementType;
  
    /** a concise description of the data type represented */
    private String description;

    
    /**
     * Constructor for a standard BITPIX instance.
     * 
     * @param numberType        the Number subclass     
     * @param elementType       the class of data element
     * @param desc              a concise description of the data type
     */
    Bitpix(Class<? extends Number> numberType, ElementType<?> elementType, String desc) {
        this.numberType = numberType;
        this.elementType = elementType;
        this.description = desc;
    }
    
    public final ElementType<?> getElementType() {
        return elementType;
    }
    
    /**
     * Returns the sublass of {@link Number} corresponding for this BITPIX value.
     * 
     * @return  the number class for this BITPIX instance.
     * 
     * @see #getPrimitiveType()
     * @see Bitpix#forNumberType(Class)
     */
    public final Class<? extends Number> getNumberType() {
        return numberType;
    }
    
    /**
     * Returns the primitive built-in Java number type corresponding for this BITPIX value.
     * 
     * @return  the primitive class for this BITPIX instance, such as <code>int.class</code>, 
     *          or <code>double.class</code>.
     * 
     * @see #getNumberType()
     * @see Bitpix#forPrimitiveType(Class)
     */
    public final Class<?> getPrimitiveType() {
        return elementType.primitiveClass();
    }
    
    /**
     * Returns the FITS standard BITPIX header value for this instance.
     * 
     * @return  the standard FITS BITPIX value, such as 8, 16, 32, 64, -32, or -64.
     * 
     * @see Bitpix#forValue(int)
     * @see #getHeaderCard()
     */
    public final int getHeaderValue() {
        return elementType.bitPix();
    }
    
    /**
     * Returns the FITS data letter ID for this BITPIX instance, such as the letter ID
     * used by the TTYPE<i>n</i> keywords for the same type of data.
     * 
     * @return  The FITS data letter ID for this BITPIX instance. 
     * 
     * @see Bitpix#forDataID(char)
     */
    public final char getDataID() {
        return elementType.type();
    }
    
    /**
     * Returns a concise description of the data type represented by this BITPIX instance.
     * 
     * @return  a brief description of the corresponding data type.
     */
    public final String getDescription() {
        return description;
    }
    
    /**
     * Returns the size of a data element, in bytes, for this BITPIX instance
     * 
     * @return      the size of a data element in bytes.
     */
    public final int byteSize() {
        return Math.abs(getHeaderValue()) >>> BITS_TO_BYTES_SHIFT;
    }
    
    /**
     * Returns the standard FITS header card for this BITPIX instance.
     * 
     * @return      the standard FITS header card with the BITPIX keyword and the corresponding value
     *              for this instance.
     *              
     * @see #getHeaderValue()
     */
    public final HeaderCard getHeaderCard() {
        return HeaderCard.create(Standard.BITPIX, getHeaderValue());
    }
    
    /**
     * Returns the standard BITPIX object for a primitive type.
     * 
     * @param dataType      the primitive class, such as <code>int.class</code>.
     * @return              the standard BITPIX associated to the number type
     * @throws FitsException    if the class is not a primitive class, or if its not
     *                          one that has a corresponding BITPIX value (e.g. <code>
     *                          boolean.class</code>).
     *                          
     * @see Bitpix#forNumberType(Class)
     * @see #getPrimitiveType()
     */
    public static Bitpix forPrimitiveType(Class<?> dataType) throws FitsException {
        if (dataType == byte.class) {
            return BYTE;
        }
        if (dataType == short.class) {
            return SHORT;
        }
        if (dataType == int.class) {
            return INTEGER;
        }
        if (dataType == long.class) {
            return LONG;
        }
        if (dataType == float.class) {
            return FLOAT;
        }
        if (dataType == double.class) {
            return DOUBLE;
        }
        if (Object.class.isAssignableFrom(dataType)) {
            throw new FitsException("No BITPIX for type: " + dataType + " (expected primitive type)");
        }
        
        throw new FitsException("No BITPIX for primitive type: " + dataType);
    }
    
    /**
     * Returns the standard BITPIX object for a number type.
     * 
     * @param dataType      the class of number, such as {@link Integer#TYPE}.
     * @return              the standard BITPIX associated to the number type
     * @throws FitsException    if there is no standard BITPIX value corresponding to the number type
     *                          (e.g. {@link java.math.BigDecimal}).
     *                          
     * @see Bitpix#forPrimitiveType(Class)
     * @see #getNumberType()
     */
    public static Bitpix forNumberType(Class<? extends Number> dataType) throws FitsException {
        if (Byte.class.isAssignableFrom(dataType)) {
            return BYTE;
        }
        if (Short.class.isAssignableFrom(dataType)) {
            return SHORT;
        }
        if (Integer.class.isAssignableFrom(dataType)) {
            return INTEGER;
        }
        if (Long.class.isAssignableFrom(dataType)) {
            return LONG;
        }
        if (Float.class.isAssignableFrom(dataType)) {
            return FLOAT;
        }
        if (Double.class.isAssignableFrom(dataType)) {
            return DOUBLE;
        }
        throw new FitsException("No BITPIX for Number type " + dataType);
    }
    
    
    /**
     * Returns the standard BITPIX object based on the value assigned to the BITPIX keyword in the header
     * 
     * @param h             the FITS header
     * @return              the standard BITPIX enum that matches the header description, or is
     *                      inferred from an invalid header description (provided 
     *                      {@link FitsFactory#setAllowHeaderRepairs(boolean)} is enabled).
     * @throws FitsException    if the header does not contain a BITPIX value or it is invalid
     *                          and cannot or will not be repaired. 
     *                          
     * @see Bitpix#fromHeader(Header, boolean)
     * @see Bitpix#forValue(int)
     * @see FitsFactory#setAllowHeaderRepairs(boolean)
     */
    public static Bitpix fromHeader(Header h) throws FitsException {
        return forValue(h.getIntValue(Standard.BITPIX, 0));
    }
    
    /**
     * Returns the standard BITPIX object based on the value assigned to the BITPIX keyword in the header
     * 
     * @param h             the FITS header
     * @param allowRepair   if we can try repair non-standard (invalid) BITPIX values.
     * @return              the standard BITPIX enum that matches the header description, or is
     *                      inferred from an invalid header description.
     * @throws FitsException    if the header does not contain a BITPIX value or it is invalid
     *                          and cannot or will not be repaired. 
     *                          
     * @see Bitpix#fromHeader(Header)
     * @see Bitpix#forValue(int, boolean)
     */
    public static Bitpix fromHeader(Header h, boolean allowRepair) throws FitsException {
        return forValue(h.getIntValue(Standard.BITPIX, 0), allowRepair);
    }
    
   /**
    * Returns the standard BITPIX enum value for a given integer value, such as 8, 16, 32, 64, -32, or -64.
    * If the value is not one of the standard values, then depending on whether header repairs are enabled
    * either an exception is thrown, or else the value the value is 'repaired' and a loh entry is made
    * to the logger of {@link Header}.
    * 
    * @param ival          The integer value of BITPIX in the FITS header.
    * @return              The standard value as a Java object.
    * @throws FitsException    if the value was invalid or irreparable.
    * 
    * @see Bitpix#forValue(int, boolean)
    * @see FitsFactory#setAllowHeaderRepairs(boolean)
    * @see #getHeaderValue()
    */
   public static Bitpix forValue(int ival) throws FitsException {
        try {
            return forValue(ival, FitsFactory.isAllowHeaderRepairs());
        } catch (FitsException e) {
            throw new FitsException(e.getMessage() + "\n\n"
                    + " --> Try FitsFactory.setAllowHeaderRepairs(true).\n");
        }
    }
        
    /**
     * Returns the standard BITPIX enum value for a given integer value, such as 8, 16, 32, 64, -32, or -64.
     * If the value is not one of the standard values, then depending on whether repairs are enabled
     * either an exception is thrown, or else the value the value is 'repaired' and a loh entry is made
     * to the logger of {@link Header}.
     * 
     * @param ival          The integer value of BITPIX in the FITS header.
     * @param allowRepair   Whether we can fix up invalid values to make them valid.
     * @return              The standard value as a Java object.
     * @throws FitsException    if the value was invalid or irreparable.
     * 
     * @see Bitpix#forValue(int)
     * @see #getHeaderValue()
     */
    public static Bitpix forValue(int ival, boolean allowRepair) throws FitsException {
        
        if (ival == 0) {
            throw new FitsException("Invalid BITPIX value:" + ival);
        }
        
        // Normally BITPIX must be one one of the supported values. Unfortunately, some
        // commercial cameras fill illegal values, such as 20.
        // We can 'repair' them by rounding up to the next valid value, so 20 repairs to 32, and
        // maxing at +/- 64, so for example -80 repairs to -64.
        if (allowRepair) {
            int fixed = 0;

            if (ival < 0) {
                fixed = ival < BITPIX_FLOAT ? BITPIX_DOUBLE : BITPIX_FLOAT;
            } else if (ival < BITPIX_BYTE) {
                fixed = BITPIX_BYTE;
            } else if (ival > BITPIX_LONG) {
                fixed = BITPIX_LONG;
            } else if (ival > Integer.highestOneBit(ival)) {
                fixed = (Integer.highestOneBit(ival) << 1);
            }
            
            if (fixed != 0) {
                LOG.warning("Repaired invalid BITPIX value:" + ival + " --> " + fixed);
                ival = fixed;
            }
        }
        
        switch (ival) {
        case BITPIX_BYTE: 
            return BYTE;
        case BITPIX_SHORT: 
            return SHORT;
        case BITPIX_INT: 
            return INTEGER;
        case BITPIX_LONG: 
            return LONG;
        case BITPIX_FLOAT: 
            return FLOAT;
        case BITPIX_DOUBLE: 
            return DOUBLE;
        default:
            throw new FitsException("Invalid BITPIX value:" + ival);
        }
    }
    
    /**
     * Returns the standard BITPIX object for the given data ID. The data ID is the same
     * letter code as FITS uses for identifying data types for table columns using the TTYPE<i>n</i>
     * keyword, such as 'I' for 32-bit integer, 'D' for <code>double</code> etc.
     * 
     * @param id        The standard FITS letter ID for a data type, same as used for TTYPE<i>n</i> keywords.
     * @return          The standard BITPIX enum corresponding to the data type.
     * @throws FitsException    if the data type is unknown or does not have a BITPIX ewquivalent.
     * 
     */
    public static Bitpix forDataID(char id) throws FitsException {
        switch (id) {
        case 'B': 
            return BYTE;
        case 'S': 
            return SHORT;
        case 'I': 
            return INTEGER;
        case 'J': 
            return LONG;
        case 'F': 
            return FLOAT;
        case 'D': 
            return DOUBLE;
        default:
            throw new FitsException("Invalid BITPIX data ID: '" + id + "'");
        }
    } 
}
