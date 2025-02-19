package nom.tam.fits;

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

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import nom.tam.fits.header.Bitpix;
import nom.tam.fits.header.NonStandard;
import nom.tam.fits.header.Standard;
import nom.tam.util.ArrayDataInput;
import nom.tam.util.ArrayDataOutput;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.AsciiFuncs;
import nom.tam.util.ColumnTable;
import nom.tam.util.ComplexValue;
import nom.tam.util.Cursor;
import nom.tam.util.FitsEncoder;
import nom.tam.util.FitsIO;
import nom.tam.util.Quantizer;
import nom.tam.util.RandomAccess;
import nom.tam.util.ReadWriteAccess;
import nom.tam.util.TableException;
import nom.tam.util.type.ElementType;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Table data for binary table HDUs. It has been thoroughly re-written for 1.18 to improve consistency, increase
 * performance, make it easier to use, and to enhance.
 * 
 * @see BinaryTableHDU
 * @see AsciiTable
 */
@SuppressWarnings("deprecation")
public class BinaryTable extends AbstractTableData implements Cloneable {

    /** For fixed-length columns */
    private static final char POINTER_NONE = 0;

    /** FITS 32-bit pointer type for variable-sized columns */
    private static final char POINTER_INT = 'P';

    /** FITS 64-bit pointer type for variable-sized columns */
    private static final char POINTER_LONG = 'Q';

    /** Shape firs singleton / scalar entries */
    private static final int[] SINGLETON_SHAPE = new int[0];

    /** The substring convention marker */
    private static final String SUBSTRING_MARKER = ":SSTR";

    /**
     * Describes the data type and shape stored in a binary table column.
     */
    public static class ColumnDesc implements Cloneable {

        private boolean warnedFlatten;

        /** byte offset of element from row start */
        private int offset;

        /** The number of primitive elements in the column */
        private int fitsCount;

        /** The dimensions of the column */
        private int[] fitsShape = SINGLETON_SHAPE;

        /** Shape on the Java side. Differs from the FITS TDIM shape for String and complex values. */
        private int[] legacyShape = SINGLETON_SHAPE;

        /** Length of string elements */
        private int stringLength = -1;

        /** The class array entries on the Java side. */
        private Class<?> base;

        /** The FITS element class associated with the column. */
        private Class<?> fitsBase;

        /** Heap pointer type actually used for locating variable-length column data on the heap */
        private char pointerType;

        /**
         * String component delimiter for substring arrays, for example as defined by the TFORM keyword that uses the
         * substring array convention...
         */
        private byte delimiter;

        /**
         * Is this a complex column. Each entry will be associated with a float[2] or double[2]
         */
        private boolean isComplex;

        /**
         * Whether this column contains bit arrays. These take up to 8-times less space than logicals, which occupy a
         * byte per value.
         */
        private boolean isBits;

        /**
         * User defined column name
         */
        private String name;

        private Quantizer quant;

        /**
         * Creates a new column descriptor with default settings and 32-bit integer heap pointers.
         */
        protected ColumnDesc() {
        }

        /**
         * Creates a new column descriptor with default settings, and the specified type of heap pointers
         * 
         * @param  type          The Java type of base elements that this column is designated to contain. For example
         *                           <code>int.class</code> if the column will contain integers or arrays of integers.
         * 
         * @throws FitsException if the base type is not one that can be used in binary table columns.
         */
        private ColumnDesc(Class<?> type) throws FitsException {
            this();

            base = type;

            if (base == boolean.class) {
                fitsBase = byte.class;
                isBits = true;
            } else if (base == Boolean.class) {
                base = boolean.class;
                fitsBase = byte.class;
            } else if (base == String.class) {
                fitsBase = byte.class;
            } else if (base == ComplexValue.class) {
                base = double.class;
                fitsBase = double.class;
                isComplex = true;
            } else if (base == ComplexValue.Float.class) {
                base = float.class;
                fitsBase = float.class;
                isComplex = true;
            } else if (base.isPrimitive()) {
                fitsBase = type;
                if (base == char.class && FitsFactory.isUseUnicodeChars()) {
                    LOG.warning("char[] will be written as 16-bit integers (type 'I'), not as a ASCII bytes (type 'A')"
                            + " in the binary table. If that is not what you want, you should set FitsFactory.setUseUnicodeChars(false).");
                    LOG.warning(
                            "Future releases will disable Unicode support by default as it is not supported by the FITS standard."
                                    + " If you do want it still, use FitsFactory.setUseUnicodeChars(true) explicitly to keep the non-standard "
                                    + " behavior as is.");
                }
            } else {
                throw new TableException("Columns of type " + base + " are not supported.");
            }

        }

        /**
         * Creates a new column descriptor for the specified boxed Java type, and fixed array shape. The type may be any
         * primitive type, or else <code>String.class</code>, <code>Boolean.class</code> (for FITS logicals),
         * <code>ComplexValue.class</code> or <code>ComplexValue.Float.class</code> (for complex values with 64-bit and
         * 32-bit precision, respectively). Whereas {@link Boolean} type columns will be stored as FITS logicals (1
         * element per byte), <code>boolean</code> types will be stored as packed bits (with up to 8 bits per byte).
         * 
         * @param  base          The Java type of base elements that this column is designated to contain. For example
         *                           <code>int.class</code> if the column will contain integers or arrays of integers.
         * @param  dim           the fixed dimensions of the table entries. For strings the trailing dimension must
         *                           specify the fixed length of strings.
         * 
         * @throws FitsException if the base type is not one that can be used in binary table columns.
         * 
         * @see                  #createForScalars(Class)
         * @see                  #createForStrings(int)
         * @see                  #createForStrings(int, int[])
         * @see                  #createForVariableSize(Class)
         * 
         * @since                1.18
         */
        public ColumnDesc(Class<?> base, int... dim) throws FitsException {
            this(base);
            setBoxedShape(dim);
        }

        /**
         * Sets a user-specified name for this column. The specified name will be used as the TTYPEn value for this
         * column.
         * 
         * @param  value                    The new name for this column.
         * 
         * @return                          itself, to support builder patterns.
         * 
         * @throws IllegalArgumentException If the name contains characters outside of the ASCII range of 0x20 - 0x7F
         *                                      allowed by FITS.
         * 
         * @see                             #name()
         * @see                             #getDescriptor(String)
         * @see                             #indexOf(String)
         * @see                             #addColumn(ColumnDesc)
         * 
         * @since                           1.20
         * 
         * @author                          Attila Kovacs
         */
        public ColumnDesc name(String value) throws IllegalArgumentException {
            HeaderCard.validateChars(value);
            this.name = value;
            return this;
        }

        /**
         * Returns the name of this column, as it was stored or would be stored by a TTYPEn value in the FITS header.
         * 
         * @return the name of this column
         * 
         * @see    #name(String)
         * 
         * @since  1.20
         * 
         * @author Attila Kovacs
         */
        public String name() {
            return this.name;
        }

        /**
         * Returns the conversion between decimal and integer data representations for the column data.
         * 
         * @return the quantizer that converts between floating-point and integer data representations, which may be
         *             <code>null</code>.
         * 
         * @see    #setQuantizer(Quantizer)
         * 
         * @since  1.20
         */
        public Quantizer getQuantizer() {
            return quant;
        }

        /**
         * Sets the conversion between decimal and integer data representations for the column data. If the table is
         * read from a FITS input, the column's quantizer is automatically set if the Table HDU's header defines any of
         * the TSCALn, TZEROn, or TNULLn keywords for the column. Users can override that by specifying another quatizer
         * to use for the column, or dicard qunatizing by calling this method with a <code>null</code>argument.
         * 
         * @param q the quantizer that converts between floating-point and integer data representations, or
         *              <code>null</code> to not use any quantization, and instead rely on the generic rounding for
         *              decimal-integer conversions for this column.
         * 
         * @see     #getQuantizer()
         * 
         * @since   1.20
         */
        public void setQuantizer(Quantizer q) {
            this.quant = q;
        }

        /**
         * Recalculate the FITS element count based on the shape of the data
         */
        private void calcFitsCount() {
            fitsCount = 1;
            for (int size : fitsShape) {
                fitsCount *= size;
            }
        }

        /**
         * Sets the shape of entries in the older Java array format of this library (used exclusively prior to 1.18).
         * For complex columns, there is an extra <code>[2]</code> dimension, such that single complex values are stored
         * as an array of <code>[2]</code>, and an array of <i>n</i> complex values are stored as arrays of
         * <code>[n][2]</code>. Otherwise it's the same as {@link #setBoxedShape(int...)}.
         * 
         * @param dim The Java dimensions for legacy arrays, such as returned by
         *                {@link BinaryTable#getElement(int, int)}
         */
        private void setLegacyShape(int... dim) {
            legacyShape = dim;
            calcFitsShape();
            calcFitsCount();
        }

        /**
         * Sets the shape of entries as stored in the FITS header by hte TDIM keyword.
         * 
         * @param dim The dimensions for the TDIM keyword in Java order (outer dimensions first), which is the reverse
         *                of FITS order (inner-dimensions first).
         */
        private void setFitsShape(int... dim) {
            fitsShape = dim;
            calcLegacyShape();
            calcFitsCount();
        }

        /**
         * Sets the shape of boxed Java array entries. For complex columns, all single entries, including strings and
         * complex values, have scalar shape <code>[]</code>, whereas an array of <i>n</i> have shape <code>[n]</code>.
         * 
         * @param dim The Java dimensions for legacy arrays, such as returned by
         *                {@link BinaryTable#getElement(int, int)}
         */
        private void setBoxedShape(int... dim) {
            if (isComplex()) {
                setFitsShape(dim);
            } else {
                setLegacyShape(dim);
            }
        }

        /**
         * Returns the maximum length of string elements contained in this colum, or -1 if this is not a string based
         * column, or if there is no limit set to string size (e.g. in a variable-length column)
         * 
         * @return the maximum length of string values stored in this column, or -1 if it is not as string column, or if
         *             its a string column containing strings of unconstrained variable length.
         * 
         * @see    #getEntryShape()
         * 
         * @since  1.18
         */
        public final int getStringLength() {
            return stringLength;
        }

        /**
         * Sets the maximum length of string elements in this column.
         *
         * @param len The fixed string length in bytes.
         */
        private void setStringLength(int len) {
            stringLength = len;

            if (!isVariableSize()) {
                calcFitsShape();
                calcFitsCount();
            }
        }

        /**
         * Returns the string delimiter that separates packed substrings in variable-length string arrays.
         * 
         * @return the delimiter byte value (usually between 0x20 and 0x7e) or 0 if no delimiter was set.
         * 
         * @see    #getStringLength()
         */
        public final byte getStringDelimiter() {
            return delimiter;
        }

        /**
         * Creates a new column descriptor for a non-string based scalar column. The type may be any primitive type, or
         * <code>Boolean.class</code> (for FITS logicals), <code>ComplexValue.class</code> or
         * <code>ComplexValue.Float.class</code> (for complex values with 64-bit and 32-bit precision, respectively).
         * Whereas {@link Boolean} type columns will be stored as FITS logicals (1 element per byte),
         * <code>boolean</code> types will be stored as packed bits (with up to 8 bits per byte).
         * 
         * @param  type                     The Java type of base elements that this column is designated to contain.
         *                                      For example <code>int.class</code> if the column will contain integers
         *                                      or arrays of integers. It must not be <code>String.class</code>. To
         *                                      create scalar {@link String} columns use {@link #createForStrings(int)}
         *                                      instead.
         * 
         * @return                          the new column descriptor.
         * 
         * @throws IllegalArgumentException if the type is <code>String.class</code>, for which you should be using
         *                                      {@link #createForStrings(int)} instead.
         * @throws FitsException            if the base type is not one that can be used in binary table columns.
         * 
         * @see                             #createForFixedArrays(Class, int[])
         * @see                             #createForVariableSize(Class)
         * 
         * @since                           1.18
         */
        public static ColumnDesc createForScalars(Class<?> type) throws IllegalArgumentException, FitsException {
            if (String.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException("Use the createStrings(int) method for scalar strings.");
            }
            return new ColumnDesc(type, SINGLETON_SHAPE);
        }

        /**
         * Creates a new column descriptor for fixed-shape non-string arrays. The type may be any primitive type, or
         * else <code>Boolean.class</code> (for FITS logicals), <code>ComplexValue.class</code> or
         * <code>ComplexValue.Float.class</code> (for complex values with 64-bit and 32-bit precision, respectively).
         * Whereas {@link Boolean} type columns will be stored as FITS logicals (1 element per byte),
         * <code>boolean</code> types will be stored as packed bits (with up to 8 bits per byte).
         * 
         * @param  type                     The Java type of base elements that this column is designated to contain.
         *                                      For example <code>int.class</code> if the column will contain integers
         *                                      or arrays of integers. It must not be <code>String.class</code>. To
         *                                      create scalar {@link String} columns use {@link #createForStrings(int)}
         *                                      instead.
         * @param  dim                      the fixed dimensions of the table entries. For strings the trailing
         *                                      dimension must specify the fixed length of strings.
         * 
         * @return                          the new column descriptor.
         * 
         * @throws IllegalArgumentException if the type is <code>String.class</code>, for which you should be using
         *                                      {@link #createForStrings(int, int[])} instead.
         * @throws FitsException            if the base type is not one that can be used in binary table columns.
         * 
         * @see                             #createForScalars(Class)
         * @see                             #createForStrings(int)
         * @see                             #createForStrings(int, int[])
         * @see                             #createForVariableSize(Class)
         * 
         * @since                           1.18
         */
        public static ColumnDesc createForFixedArrays(Class<?> type, int... dim)
                throws IllegalArgumentException, FitsException {
            if (String.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException("Use the createStrings(int) method for scalar strings.");
            }
            return new ColumnDesc(type, dim);
        }

        /**
         * Creates a new column descriptor for single string entries of fixed maximum length.
         * 
         * @param  len           The fixed string length in bytes.
         * 
         * @return               the new column descriptor
         * 
         * @throws FitsException if the base type is not one that can be used in binary table columns.
         * 
         * @see                  #createForScalars(Class)
         * @see                  #createForStrings(int, int[])
         * 
         * @since                1.18
         */
        public static ColumnDesc createForStrings(int len) throws FitsException {
            return createForStrings(len, SINGLETON_SHAPE);
        }

        /**
         * Creates a new column descriptor for arrays of string entries of fixed maximum length.
         * 
         * @param  len           The fixed string length in bytes.
         * @param  outerDims     The shape of string arrays
         * 
         * @return               the new column descriptor
         * 
         * @throws FitsException if the base type is not one that can be used in binary table columns.
         * 
         * @see                  #createForVariableStringArrays(int)
         * @see                  #createForStrings(int)
         * 
         * @since                1.18
         */
        public static ColumnDesc createForStrings(int len, int... outerDims) throws FitsException {
            ColumnDesc c = new ColumnDesc(String.class);
            c.setLegacyShape(outerDims);
            c.setStringLength(len);
            return c;
        }

        /**
         * Creates a new column descriptor for variable-length arrays of fixed-length string entries. Each string
         * component will occupy exactly <code>len</code> bytes.
         * 
         * @param  len           The fixed string storage length in bytes.
         * 
         * @return               the new column descriptor
         * 
         * @throws FitsException if the column could not be created.
         *
         * @see                  #createForDelimitedStringArrays(byte)
         * @see                  #createForStrings(int, int[])
         * 
         * @since                1.18
         */
        public static ColumnDesc createForVariableStringArrays(int len) throws FitsException {
            ColumnDesc c = createForVariableSize(String.class);
            c.setStringLength(len);
            return c;
        }

        /**
         * Creates a new column descriptor for variable-length arrays of delimited string entries.
         * 
         * @param  delim         the byte value that delimits strings that are shorter than the storage length. It
         *                           should be in the ASCII range of 0x20 through 0x7e.
         * 
         * @return               the new column descriptor
         * 
         * @throws FitsException if the column could not be created.
         * 
         * @see                  #createForDelimitedStringArrays(byte)
         * @see                  #createForStrings(int, int[])
         * 
         * @since                1.18
         */
        public static ColumnDesc createForDelimitedStringArrays(byte delim) throws FitsException {
            ColumnDesc c = createForVariableStringArrays(-1);
            c.setStringDelimiter(delim);
            return c;
        }

        /**
         * Creates a new column descriptor for variable length 1D arrays or strings. The type may be any primitive type,
         * or else <code>String.class</code>, <code>Boolean.class</code> (for FITS logicals),
         * <code>ComplexValue.class</code> or <code>ComplexValue.Float.class</code> (for complex values with 64-bit and
         * 32-bit precision, respectively). Whereas {@link Boolean} type columns will be stored as FITS logicals (1
         * element per byte), <code>boolean</code> types will be stored as packed bits (with up to 8 elements per byte).
         * 
         * @param  type          The Java type of base elements that this column is designated to contain. For example
         *                           <code>int.class</code> if the column will contain integers or arrays of integers.
         * 
         * @return               the new column descriptor
         * 
         * @throws FitsException if the base type is not one that can be used in binary table columns.
         * 
         * @see                  #createForScalars(Class)
         * @see                  #createForStrings(int)
         * @see                  #createForStrings(int, int[])
         * @see                  #ColumnDesc(Class, int[])
         * 
         * @since                1.18
         */
        public static ColumnDesc createForVariableSize(Class<?> type) throws FitsException {
            ColumnDesc c = new ColumnDesc(type);
            c.setVariableSize(false);
            return c;
        }

        /**
         * Recalculate the legacy Java entry shape from the FITS shape (as stored by TDIM). Strings drop the last
         * dimension from the FITS shape (which becomes the string length), while complex values add a dimension of
         * <code>[2]</code> to the FITS shape, reflecting the shape of their real-valued components.
         */
        private void calcLegacyShape() {
            if (isString()) {
                legacyShape = Arrays.copyOf(fitsShape, fitsShape.length - 1);
                stringLength = fitsShape[fitsShape.length - 1];
            } else if (isComplex()) {
                legacyShape = Arrays.copyOf(fitsShape, fitsShape.length + 1);
                legacyShape[fitsShape.length] = 2;
            } else {
                legacyShape = fitsShape;
            }
        }

        /**
         * Recalculate the FITS storage shape (as reported by TDIM) from the legacy Java array shape
         */
        private void calcFitsShape() {
            if (isString()) {
                fitsShape = Arrays.copyOf(legacyShape, legacyShape.length + 1);
                fitsShape[legacyShape.length] = stringLength;
            } else if (isComplex()) {
                fitsShape = Arrays.copyOf(legacyShape, legacyShape.length - 1);
            } else {
                fitsShape = legacyShape;
            }
        }

        /**
         * Returns the size of table entries in their trailing dimension.
         * 
         * @return the number of elemental components in the trailing dimension of table entries.
         * 
         * @see    #getLeadingShape()
         */
        private int getLastFitsDim() {
            return fitsShape[fitsShape.length - 1];
        }

        @Override
        public ColumnDesc clone() {
            try {
                ColumnDesc copy = (ColumnDesc) super.clone();
                fitsShape = fitsShape.clone();
                legacyShape = legacyShape.clone();

                // Model should not be changed...
                return copy;
            } catch (CloneNotSupportedException e) {
                return null;
            }
        }

        /**
         * Specifies that this columns contains single (not array) boxed entrie, such as single primitives, strings, or
         * complex values.
         */
        private void setSingleton() {
            setBoxedShape(SINGLETON_SHAPE);
        }

        /**
         * Checks if this column contains single (scalar / non-array) elements only, including single strings or single
         * complex values.
         * 
         * @return <code>true</code> if the column contains individual elements of its type, or else <code>false</code>
         *             if it contains arrays.
         * 
         * @since  1.18
         */
        public final boolean isSingleton() {
            if (isVariableSize()) {
                return isString() ? (stringLength < 0 && delimiter == 0) : false;
            }

            if (isComplex()) {
                return fitsShape.length == 0;
            }

            return legacyShape.length == 0;
        }

        /**
         * Checks if this column contains logical values. FITS logicals can each hve <code>true</code>,
         * <code>false</code> or <code>null</code> (undefined) values. It is the support for these undefined values that
         * set it apart from typical booleans. Also, logicals are stored as one byte per element. So if using only
         * <code>true</code>, <code>false</code> values without <code>null</code> bits will offer more compact storage
         * (by up to a factor of 8). You can convert existing logical columns to bits via
         * {@link BinaryTable#convertToBits(int)}.
         * 
         * @return <code>true</code> if this column contains logical values.
         * 
         * @see    #isBits()
         * @see    BinaryTable#convertToBits(int)
         * 
         * @since  1.18
         */
        public final boolean isLogical() {
            return base == Boolean.class || (base == boolean.class && !isBits);
        }

        /**
         * Checks if this column contains only true boolean values (bits). Unlike logicals, bits can have only
         * <code>true</code>, <code>false</code> values with no support for <code>null</code> , but offer more compact
         * storage (by up to a factor of 8) than logicals. You can convert existing logical columns to bits via
         * {@link BinaryTable#convertToBits(int)}.
         * 
         * @return <code>true</code> if this column contains <code>true</code> / <code>false</code> bits only.
         * 
         * @see    #isLogical()
         * @see    BinaryTable#convertToBits(int)
         * 
         * @since  1.18
         */
        public final boolean isBits() {
            return base == boolean.class && isBits;
        }

        /**
         * Checks if this column stores ASCII strings.
         * 
         * @return <code>true</code> if this column contains only strings.
         * 
         * @see    #isVariableSize()
         */
        public final boolean isString() {
            return base == String.class;
        }

        /**
         * Checks if this column contains complex values. You can convert suitable columns of <code>float</code> or
         * <code>double</code> elements to complex using {@link BinaryTable#setComplexColumn(int)}, as long as the last
         * dimension is 2, ir if the variable-length columns contain even-number of values exclusively.
         * 
         * @return <code>true</code> if this column contains complex values.
         */
        public final boolean isComplex() {
            return isComplex;
        }

        /**
         * Checks if this column contains numerical values, such as any primitive number type (e.g.
         * <code>nt.class</code> or <code>double.class</code>) or else a {@link ComplexValue} type. type.
         * 
         * @return <code>true</code> if this column contains numerical data, including complex-valued data. String,
         *             bits, and FITS logicals are not numerical (but all other column types are).
         * 
         * @since  1.20
         */
        public final boolean isNumeric() {
            return !isLogical() && !isBits() && !isString();
        }

        /**
         * Returns the Java array element type that is used in Java to represent data in this column. When accessing
         * columns or their elements in the old way, through arrays, this is the type that arrays from the Java side
         * will expect or provide. For example, when storing {@link String} values (regular or variable-sized), this
         * will return <code>String.class</code>. Arrays returned by {@link BinaryTable#getColumn(int)},
         * {@link BinaryTable#getRow(int)}, and {@link BinaryTable#getElement(int, int)} will return arrays of this
         * type, and the equivalent methods for setting data will expect arrays of this type as their argument.
         * 
         * @return     the Java class, arrays of which, packaged data for this column on the Java side.
         * 
         * @deprecated Ambiguous, use {@link #getLegacyBase()} instead. It can be confusing since it is not clear if it
         *                 refers to array element types used in FITS storage or on the java side when using the older
         *                 array access, or if it refers to the class of entries in the main table, which may be heap
         *                 pointers. It is also distinct from {@link #getElementClass()}, which returns the boxed type
         *                 used by {@link BinaryTable#get(int, int)} or {@link BinaryTable#set(int, int, Object)}.
         */
        public Class<?> getBase() {
            return getLegacyBase();
        }

        /**
         * Returns the primitive type that is used to store the data for this column in the FITS representation. This is
         * the class for the actual data type, whether regularly shaped (multidimensional) arrays or variable length
         * arrays (on the heap). For example, when storing {@link String} values (regular or variable-sized), this will
         * return <code>byte.class</code>.
         * 
         * @return the primitive class, in used for storing data in the FITS representation.
         * 
         * @see    #getLegacyBase()
         * 
         * @since  1.18
         */
        final Class<?> getFitsBase() {
            return fitsBase;
        }

        /**
         * <p>
         * Returns the Java array element type that is used in Java to represent data in this column for the legacy
         * table access methods. When accessing columns or their elements in the old way, through arrays, this is the
         * type that arrays from the Java side will expect or provide. For example, when storing complex values (regular
         * or variable-sized), this will return <code>float.class</code> or <code>double.class</code>. Arrays returned
         * by {@link BinaryTable#getColumn(int)}, {@link BinaryTable#getRow(int)}, and
         * {@link BinaryTable#getElement(int, int)} will return arrays of this type.
         * </p>
         * <p>
         * This is different from {@link #getElementClass()}, which in turn returns the boxed type of objects returned
         * by {@link BinaryTable#get(int, int)}.
         * 
         * @return the Java class, arrays of which, packaged data for this column on the Java side.
         * 
         * @see    #getElementClass()
         * 
         * @since  1.18
         */
        public Class<?> getLegacyBase() {
            return base;
        }

        /**
         * (<i>for internal use</i>) Returns the primitive data class which is used for storing entries in the main
         * (regular) table. For variable-sized columns, this will be the heap pointer class, not the FITS data class.
         * 
         * @return the class in which main table entries are stored.
         * 
         * @see    #isVariableSize()
         */
        private Class<?> getTableBase() {
            return isVariableSize() ? pointerClass() : getFitsBase();
        }

        /**
         * Returns the dimensions of elements in this column. As of 1.18, this method returns a copy ot the array used
         * internally, which is safe to modify.
         * 
         * @return     an array with the element dimensions.
         * 
         * @deprecated (<i>for internal use</i>) Use {@link #getEntryShape()} instead. Not useful to users since it
         *                 returns the dimensions of the primitive storage types, which is not always the dimension of
         *                 table entries on the Java side.
         */
        public int[] getDimens() {
            return fitsShape.clone();
        }

        /**
         * (<i>for internal use</i>) The dimension of elements in the FITS representation.
         * 
         * @return the dimension of elements in the FITS representation. For example an array of string will be 2
         *             (number of string, number of bytes per string).
         * 
         * @see    #getEntryDimension()
         */
        private int fitsDimension() {
            return fitsShape.length;
        }

        /**
         * Returns the boxed Java type of elements stored in a column.
         * 
         * @return The java type of elements in the columns. For columns containing strings, FITS logicals, or complex
         *             values it will be <code>String.class</code>, <code>Boolean.class</code> or
         *             <code>ComplexValue.class</code> respectively. For all other column types the primitive class of
         *             the elements contained (e.g. <code>char.class</code>, <code>float.class</code>) is returned.
         * 
         * @since  1.18
         * 
         * @see    ColumnDesc#getElementCount()
         * @see    ColumnDesc#getEntryShape()
         * @see    ColumnDesc#getLegacyBase()
         */
        public final Class<?> getElementClass() {
            if (isLogical()) {
                return Boolean.class;
            }
            if (isComplex()) {
                return ComplexValue.class;
            }
            return base;
        }

        /**
         * Returns the dimensionality of the 'boxed' elements as returned by {@link BinaryTable#get(int, int)} or
         * expected by {@link BinaryTable#set(int, int, Object)}. That is it returns the dimnesion of 'boxed' elements,
         * such as strings or complex values, rather than the dimension of characters or real components stored in the
         * FITS for these.
         * 
         * @return the number of array dimensions in the 'boxed' Java type for this column. Variable-sized columns will
         *             always return 1.
         * 
         * @see    #getEntryShape()
         * @see    #getElementCount()
         * 
         * @since  1.18
         */
        public final int getEntryDimension() {
            if (isVariableSize()) {
                return 1;
            }
            return isString() ? legacyShape.length : fitsShape.length;
        }

        /**
         * Returns the array shape of the 'boxed' elements as returned by {@link BinaryTable#get(int, int)} or expected
         * by {@link BinaryTable#set(int, int, Object)}. That is it returns the array shape of 'boxed' elements, such as
         * strings or complex values, rather than the shape of characters or real components stored in the FITS for
         * these.
         * 
         * @return the array sized along each of the dimensions in the 'boxed' Java type for this column, or
         *             <code>null</code> if the data is stored as variable-sized one-dimensional arrays of the boxed
         *             element type. (Note, that accordingly variable-length string columns containing single strings
         *             will thus return <code>{1}</code>, not <code>null</code>).
         * 
         * @see    #getEntryShape()
         * @see    #getElementCount()
         * @see    #isVariableSize()
         * 
         * @since  1.18
         */
        public final int[] getEntryShape() {
            if (isVariableSize()) {
                return null;
            }

            if (isComplex) {
                return fitsShape.clone();
            }

            return legacyShape.clone();
        }

        /**
         * Returns the number of primitive elements (sych as bytes) that constitute a Java element (such as a String) in
         * this table.
         * 
         * @return The number of primitives per Java element in the column, that is 1 for columns of primitive types, 2
         *             for complex-valued columns, or the number of bytes (characters) in a String element.
         *             Variable-length strings will return -1.
         * 
         * @since  1.18
         * 
         * @see    #getElementCount()
         * @see    #getLegacyBase()
         */
        public final int getElementWidth() {
            if (isComplex()) {
                return 2;
            }
            if (isString()) {
                return getStringLength();
            }
            return 1;
        }

        /**
         * Returns the number of 'boxed' elements as returned by {@link BinaryTable#get(int, int)} or expected by
         * {@link BinaryTable#set(int, int, Object)}. That is it returns the number of strings or complex values per
         * table entry, rather than the number of of characters or real components stored in the FITS for these.
         * 
         * @return the number of array elements in the 'boxed' Java type for this column, or -1 if the column contains
         *             elements of varying size.
         * 
         * @see    #getEntryShape()
         * @see    #getEntryDimension()
         * @see    #isVariableSize()
         * 
         * @since  1.18
         */
        public final int getElementCount() {
            if (isVariableSize()) {
                return isString() ? 1 : -1;
            }

            if (isString()) {
                return fitsCount / getStringLength();
            }

            return fitsCount;
        }

        /**
         * Returns the number of primitive base elements for a given FITS element count.
         * 
         * @param  fitsLen the FITS element count, sucj a a number of integers, complex-values, or bits
         * 
         * @return         the number of Java primitives that will be used to represent the number of FITS values for
         *                     this type of column.
         * 
         * @see            #getFitsBase()
         */
        private int getFitsBaseCount(int fitsLen) {
            if (isBits) {
                return (fitsLen + Byte.SIZE - 1) / Byte.SIZE;
            }
            if (isComplex) {
                return fitsLen << 1;
            }
            return fitsLen;
        }

        /**
         * Returns the number of regular primitive table elements in this column. For example, variable-length columns
         * will always return 2, and complex-valued columns will return twice the number of complex values stored in
         * each table entry.
         * 
         * @return the number of primitive table elements
         * 
         * @since  1.18
         */
        public final int getTableBaseCount() {
            if (isVariableSize()) {
                return 2;
            }
            return getFitsBaseCount(fitsCount);
        }

        /**
         * Checks if this column contains entries of different size. Data for variable length coulmns is stored on the
         * heap as one-dimemnsional arrays. As such information about the 'shape' of data is lost when they are stored
         * that way.
         * 
         * @return <code>true</code> if the column contains elements of variable size, or else <code>false</code> if all
         *             entries have the same size and shape.
         */
        public final boolean isVariableSize() {
            return pointerType != POINTER_NONE;
        }

        /**
         * @deprecated      (<i>for internal use</i>) This method should be private in the future.
         * 
         * @return          new instance of the array with space for the specified number of rows.
         *
         * @param      nRow the number of rows to allocate the array for
         */
        public Object newInstance(int nRow) {
            return ArrayFuncs.newInstance(getTableBase(), getTableBaseCount() * nRow);
        }

        /**
         * @deprecated (<i>for internal use</i>) It may be reduced to private visibility in the future. Returns the
         *                 number of bytes that each element occupies in its FITS serialized form in the stored row
         *                 data.
         * 
         * @return     the number of bytes an element occupies in the FITS binary table data representation
         */
        public int rowLen() {
            return getTableBaseCount() * ElementType.forClass(getTableBase()).size();
        }

        /**
         * Checks if this column used 64-bit heap pointers.
         * 
         * @return <code>true</code> if the column uses 64-bit heap pointers, otherwise <code>false</code>
         * 
         * @see    #createForVariableSize(Class)
         * 
         * @since  1.18
         */
        public boolean hasLongPointers() {
            return pointerType == POINTER_LONG;
        }

        /**
         * Returns the <code>TFORM</code><i>n</i> character code for the heap pointers in this column or 0 if this is
         * not a variable-sized column.
         * 
         * @return <code>int.class</code> or <code>long.class</code>
         */
        private char pointerType() {
            return pointerType;
        }

        /**
         * Returns the primitive class used for sotring heap pointers for this column
         * 
         * @return <code>int.class</code> or <code>long.class</code>
         */
        private Class<?> pointerClass() {
            return pointerType == POINTER_LONG ? long.class : int.class;
        }

        /**
         * Sets whether this column will contain variable-length data, rather than fixed-shape data.
         * 
         * @param useLongPointers <code>true</code> to use 64-bit heap pointers for variable-length arrays or else
         *                            <code>false</code> to use 32-bit pointers.
         */
        private void setVariableSize(boolean useLongPointers) {
            pointerType = useLongPointers ? POINTER_LONG : POINTER_INT;
            fitsCount = 2;
            fitsShape = new int[] {2};
            legacyShape = fitsShape;
            stringLength = -1;
        }

        /**
         * Sets a custom substring delimiter byte for variable length string arrays, between ASCII 0x20 and 0x7e. We
         * will however tolerate values outside of that range, but log an appropriate warning to alert users of the
         * violation of the standard. User's can either 'fix' it, or suppress the warning if they want to stick to their
         * guns.
         * 
         * @param delim the delimiter byte value, between ASCII 0x20 and 0x7e (inclusive).
         * 
         * @since       1.18
         */
        private void setStringDelimiter(byte delim) {
            if (delim < FitsUtil.MIN_ASCII_VALUE || delim > FitsUtil.MAX_ASCII_VALUE) {
                LOG.warning("WARNING! Substring terminator byte " + (delim & FitsIO.BYTE_MASK)
                        + " outside of the conventional range of " + FitsUtil.MIN_ASCII_VALUE + " through "
                        + FitsUtil.MAX_ASCII_VALUE + " (inclusive)");
            }
            delimiter = delim;
        }

        /**
         * Checks if <code>null</code> array elements are permissible for this column. It is for strings (which map to
         * empty strings), and for logical columns, where they signify undefined values.
         * 
         * @return <code>true</code> if <code>null</code> entries are considered valid for this column.
         */
        private boolean isNullAllowed() {
            return isLogical() || isString();
        }

        /**
         * Parses the substring array convention from a TFORM value, to set string length (if desired) and a delimiter
         * in variable-length string arrays.
         * 
         * @param tform     the TFORM header value for this column
         * @param pos       the parse position immediately after the 'A'
         * @param setLength Whether to use the substring definition to specify the max string component length, for
         *                      example because it is not defined otherwise by TDIM.
         */
        private void parseSubstringConvention(String tform, ParsePosition pos, boolean setLength) {

            if (setLength) {
                // Default string length...
                setStringLength(isVariableSize() ? -1 : fitsCount);
            }

            // Parse substring array convention...
            if (pos.getIndex() >= tform.length()) {
                return;
            }

            // Try 'rAw' format...
            try {
                int len = AsciiFuncs.parseInteger(tform, pos);
                if (setLength) {
                    setStringLength(len);
                }
                return;
            } catch (Exception e) {
                // Keep going...
            }

            // Find if and where is the ":SSTR" marker in the format
            int iSub = tform.indexOf(SUBSTRING_MARKER, pos.getIndex());
            if (iSub < 0) {
                // No substring definition...
                return;
            }

            pos.setIndex(iSub + SUBSTRING_MARKER.length());

            // Set the substring width....
            try {
                int len = AsciiFuncs.parseInteger(tform, pos);
                if (setLength) {
                    setStringLength(len);
                }
            } catch (Exception e) {
                LOG.warning("WARNING! Could not parse substring length from TFORM: [" + tform + "]");
            }

            // Parse substring array convention...
            if (pos.getIndex() >= tform.length()) {
                return;
            }

            if (AsciiFuncs.extractChar(tform, pos) != '/') {
                return;
            }

            try {
                setStringDelimiter((byte) AsciiFuncs.parseInteger(tform, pos));
            } catch (NumberFormatException e) {
                // Warn if the delimiter is outside of the range supported by the convention.
                LOG.warning("WARNING! Could not parse substring terminator from TFORM: [" + tform + "]");
            }
        }

        private void appendSubstringConvention(StringBuffer tform) {
            if (getStringLength() > 0) {
                tform.append(SUBSTRING_MARKER);
                tform.append(getStringLength());

                if (delimiter != 0) {
                    tform.append('/');
                    tform.append(new DecimalFormat("000").format(delimiter & FitsIO.BYTE_MASK));
                }
            }
        }

        /**
         * Returns the TFORM header value to use for this column.
         * 
         * @return               The TFORM value that describes this column
         * 
         * @throws FitsException If the column itself is invalid.
         */
        String getTFORM() throws FitsException {

            StringBuffer tform = new StringBuffer();

            tform.append(isVariableSize() ? "1" + pointerType() : fitsCount);

            if (base == int.class) {
                tform.append('J');
            } else if (base == short.class) {
                tform.append('I');
            } else if (base == byte.class) {
                tform.append('B');
            } else if (base == char.class) {
                if (FitsFactory.isUseUnicodeChars()) {
                    tform.append('I');
                } else {
                    tform.append('A');
                }
            } else if (base == float.class) {
                tform.append(isComplex() ? 'C' : 'E');
            } else if (base == double.class) {
                tform.append(isComplex() ? 'M' : 'D');
            } else if (base == long.class) {
                tform.append('K');
            } else if (isLogical()) {
                tform.append('L');
            } else if (isBits()) {
                tform.append('X');
            } else if (isString()) {
                tform.append('A');
                if (isVariableSize()) {
                    appendSubstringConvention(tform);
                }
            } else {
                throw new FitsException("Invalid column data class:" + base);
            }

            return tform.toString();
        }

        /**
         * Returns the TDIM header value that descrives the shape of entries in this column
         * 
         * @return the TDIM header value to use, or <code>null</code> if this column is not suited for a TDIM entry for
         *             example because it is variable-sized, or because its entries are not multidimensional. .
         */
        String getTDIM() {
            if (isVariableSize()) {
                return null;
            }

            if (fitsShape.length < 2) {
                return null;
            }

            StringBuffer tdim = new StringBuffer();
            char prefix = '(';
            for (int i = fitsShape.length - 1; i >= 0; i--) {
                tdim.append(prefix);
                tdim.append(fitsShape[i]);
                prefix = ',';
            }
            tdim.append(')');
            return tdim.toString();
        }

        private boolean setFitsType(char type) throws FitsException {
            switch (type) {
            case 'A':
                fitsBase = byte.class;
                base = String.class;
                break;

            case 'X':
                fitsBase = byte.class;
                base = boolean.class;
                break;

            case 'L':
                fitsBase = byte.class;
                base = boolean.class;
                break;

            case 'B':
                fitsBase = byte.class;
                base = byte.class;
                break;

            case 'I':
                fitsBase = short.class;
                base = short.class;
                break;

            case 'J':
                fitsBase = int.class;
                base = int.class;
                break;

            case 'K':
                fitsBase = long.class;
                base = long.class;
                break;

            case 'E':
            case 'C':
                fitsBase = float.class;
                base = float.class;
                break;

            case 'D':
            case 'M':
                fitsBase = double.class;
                base = double.class;
                break;

            default:
                return false;
            }

            return true;
        }
    }

    /**
     * The enclosing binary table's properties
     * 
     * @deprecated (<i>for internal use</i>) no longer used, and will be removed in the future.
     */
    protected static class SaveState {
        /**
         * Create a new saved state
         * 
         * @param      columns the column descriptions to save
         * @param      heap    the heap to save
         * 
         * @deprecated         (<i>for internal use</i>) no longer in use. Will remove in the future.
         */
        public SaveState(List<ColumnDesc> columns, FitsHeap heap) {
        }
    }

    /**
     * Our own Logger instance, for nothing various non-critical issues.
     */
    private static final Logger LOG = Logger.getLogger(BinaryTable.class.getName());

    /**
     * This is the area in which variable length column data lives.
     */
    private FitsHeap heap;

    /**
     * The heap start from the head of the HDU
     */
    private long heapAddress;

    /**
     * (bytes) Empty space to leave after the populated heap area for future additions.
     */
    private int heapReserve;

    /**
     * The original heap size (from the header)
     */
    private int heapFileSize;

    /**
     * A list describing each of the columns in the table
     */
    private List<ColumnDesc> columns;

    /**
     * The number of rows in the table.
     */
    private int nRow;

    /**
     * The length in bytes of each row.
     */
    private int rowLen;

    /**
     * Where the data is actually stored.
     */
    private ColumnTable<?> table;

    private FitsEncoder encoder;

    /**
     * Creates an empty binary table, which can be populated with columns / rows as desired.
     */
    public BinaryTable() {
        table = new ColumnTable<>();
        columns = new ArrayList<>();
        heap = new FitsHeap(0);
        nRow = 0;
        rowLen = 0;
    }

    /**
     * Creates a binary table from an existing column table. <b>WARNING!</b>, as of 1.18 we no longer use the column
     * data extra state to carry information about an enclosing class, because it is horribly bad practice. You should
     * not use this constructor to create imperfect copies of binary tables. Rather, use {@link #copy()} if you want to
     * create a new binary table, which properly inherits <b>ALL</b> of the properties of an original one. As for this
     * constructor, you should assume that it will not use anything beyond what's available in any generic vanilla
     * column table.
     *
     * @param      tab           the column table to create the binary table from. It must be a regular column table
     *                               that contains regular data of scalar or fixed 1D arrays only (not heap pointers).
     *                               No information beyond what a generic vanilla column table provides will be used.
     *                               Column tables don't store imensions for their elements, and don't have
     *                               variable-sized entries. Thus, if the table was the used in another binary table to
     *                               store flattened multidimensional data, we'll detect that data as 1D arrays. Andm if
     *                               the table was used to store heap pointers for variable length arrays, we'll detect
     *                               these as regular <code>int[2]</code> or <code>long[2]</code> values.
     * 
     * @deprecated               DO NOT USE -- it will be removed in the future.
     * 
     * @throws     FitsException if the table could not be copied and threw a {@link nom.tam.util.TableException}, which
     *                               is preserved as the cause.
     * 
     * @see                      #copy()
     */
    public BinaryTable(ColumnTable<?> tab) throws FitsException {
        this();

        table = new ColumnTable<>();
        nRow = tab.getNRows();
        columns = new ArrayList<>();

        for (int i = 0; i < tab.getNCols(); i++) {
            int n = tab.getElementSize(i);
            ColumnDesc c = new ColumnDesc(tab.getElementClass(i), n > 1 ? new int[] {n} : SINGLETON_SHAPE);
            addFlattenedColumn(tab.getColumn(i), nRow, c, true);
        }

    }

    /**
     * Creates a binary table from a given FITS header description. The table columns are initialized but no data will
     * be available, at least initially. Data may be loaded later (e.g. deferred read mode), provided the table is
     * associated to an input (usually only if this constructor is called from a {@link Fits} object reading an input).
     * When the table has an input configured via a {@link Fits} object, the table entries may be accessed in-situ in
     * the file while in deferred read mode, but operations affecting significant portions of the table (e.g. retrieving
     * all data via {@link #getData()} or accessing entire columns) may load the data in memory. You can also call
     * {@link #detach()} any time to force loading the data into memory, so that alterations after that will not be
     * reflected in the original file, at least not unitl {@link #rewrite()} is called explicitly.
     * 
     * @param      header        A FITS header describing what the binary table should look like.
     *
     * @throws     FitsException if the specified header is not usable for a binary table
     * 
     * @deprecated               (<i>for internal use</i>) This constructor should only be called from a {@link Fits}
     *                               object reading an input; visibility may be reduced to the package level in the
     *                               future.
     * 
     * @see                      #isDeferred()
     */
    public BinaryTable(Header header) throws FitsException {
        String ext = header.getStringValue(Standard.XTENSION, Standard.XTENSION_IMAGE);

        if (!ext.equalsIgnoreCase(Standard.XTENSION_BINTABLE) && !ext.equalsIgnoreCase(NonStandard.XTENSION_A3DTABLE)) {
            throw new FitsException(
                    "Not a binary table header (XTENSION = " + header.getStringValue(Standard.XTENSION) + ")");
        }

        synchronized (this) {
            nRow = header.getIntValue(Standard.NAXIS2);
        }

        long tableSize = nRow * header.getLongValue(Standard.NAXIS1);
        long paramSizeL = header.getLongValue(Standard.PCOUNT);
        long heapOffsetL = header.getLongValue(Standard.THEAP, tableSize);

        // Subtract out the size of the regular table from
        // the heap offset.
        long heapSizeL = (tableSize + paramSizeL) - heapOffsetL;

        if (heapSizeL < 0) {
            throw new FitsException("Inconsistent THEAP and PCOUNT");
        }
        if (heapSizeL > Integer.MAX_VALUE) {
            throw new FitsException("Heap size > 2 GB");
        }
        if (heapSizeL == 0L) {
            // There is no heap. Forget the offset
            heapAddress = 0;
        }

        heapAddress = (int) heapOffsetL;
        heapFileSize = (int) heapSizeL;

        int nCol = header.getIntValue(Standard.TFIELDS);

        synchronized (this) {
            rowLen = 0;

            columns = new ArrayList<>();
            for (int col = 0; col < nCol; col++) {
                rowLen += processCol(header, col, rowLen);
            }

            HeaderCard card = header.getCard(Standard.NAXIS1);
            card.setValue(rowLen);
        }
    }

    /**
     * Creates a binary table from existing table data int row-major format. That is the first array index is the row
     * index while the second array index is the column index.
     *
     * @param      rowColTable   Row / column array. Scalars elements are wrapped in arrays of 1, s.t. a single
     *                               <code>int</code> elements is stored as <code>int[1]</code> at its
     *                               <code>[row][col]</code> index.
     *
     * @throws     FitsException if the argument is not a suitable representation of data in rows.
     * 
     * @deprecated               The constructor is ambiguous, use {@link #fromRowMajor(Object[][])} instead. You can
     *                               have a column-major array that has no scalar primitives which would also be an
     *                               <code>Object[][]</code> and could be passed erroneously.
     */
    public BinaryTable(Object[][] rowColTable) throws FitsException {
        this();
        for (Object[] row : rowColTable) {
            addRow(row);
        }
    }

    /**
     * Creates a binary table from existing table data in row-major format. That is the first array index is the row
     * index while the second array index is the column index;
     *
     * @param  table         Row / column array. Scalars elements are wrapped in arrays of 1, s.t. a single
     *                           <code>int</code> elements is stored as <code>int[1]</code> at its
     *                           <code>[row][col]</code> index.
     * 
     * @return               a new binary table with the data. The tables data may be partially independent from the
     *                           argument. Modifications to the table data, or that to the argument have undefined
     *                           effect on the other object. If it is important to decouple them, you can use a
     *                           {@link ArrayFuncs#deepClone(Object)} of your original data as an argument.
     *
     * @throws FitsException if the argument is not a suitable representation of FITS data in rows.
     * 
     * @see                  #fromColumnMajor(Object[])
     * 
     * @since                1.18
     */
    public static BinaryTable fromRowMajor(Object[][] table) throws FitsException {
        BinaryTable tab = new BinaryTable();
        for (Object[] row : table) {
            tab.addRow(row);
        }
        return tab;
    }

    /**
     * Create a binary table from existing data in column-major format order.
     *
     * @param      columns       array of columns. The data for scalar entries is a primive array. For all else, the
     *                               entry is an <code>Object[]</code> array of sorts.
     * 
     * @throws     FitsException if the data for the columns could not be used as coulumns
     * 
     * @deprecated               The constructor is ambiguous, use {@link #fromColumnMajor(Object[])} instead. One could
     *                               call this method with any row-major <code>Object[][]</code> table by mistake.
     * 
     * @see                      #defragment()
     */
    public BinaryTable(Object[] columns) throws FitsException {
        this();

        for (Object element : columns) {
            addColumn(element);
        }
    }

    /**
     * Creates a binary table from existing data in column-major format order.
     *
     * @param  columns       array of columns. The data for scalar entries is a primive array. For all else, the entry
     *                           is an <code>Object[]</code> array of sorts.
     * 
     * @return               a new binary table with the data. The tables data may be partially independent from the
     *                           argument. Modifications to the table data, or that to the argument have undefined
     *                           effect on the other object. If it is important to decouple them, you can use a
     *                           {@link ArrayFuncs#deepClone(Object)} of your original data as an argument.
     * 
     * @throws FitsException if the argument is not a suitable representation of FITS data in rows.
     * 
     * @see                  #fromColumnMajor(Object[])
     * 
     * @since                1.18
     */
    public static BinaryTable fromColumnMajor(Object[] columns) throws FitsException {
        BinaryTable t = new BinaryTable();
        for (Object element : columns) {
            t.addColumn(element);
        }
        return t;
    }

    @Override
    protected BinaryTable clone() {
        try {
            return (BinaryTable) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * Returns an independent copy of the binary table.
     * 
     * @return               a new binary that tnat contains an exact copy of us, but is completely independent.
     * 
     * @throws FitsException if the table could not be copied
     * 
     * @since                1.18
     */
    public synchronized BinaryTable copy() throws FitsException {
        BinaryTable copy = clone();

        synchronized (copy) {
            if (table != null) {
                copy.table = table.copy();
            }
            if (heap != null) {
                copy.heap = heap.copy();
            }

            copy.columns = new ArrayList<>();
            for (ColumnDesc c : columns) {
                c = c.clone();
                copy.columns.add(c);
            }
        }

        return copy;
    }

    /**
     * (<i>for internal use</i>) Discards all variable-length arrays from this table, that is all data stored on the
     * heap, and resets all heap descritors to (0,0).
     * 
     * @since 1.19.1
     */
    protected synchronized void discardVLAs() {
        for (int col = 0; col < columns.size(); col++) {
            ColumnDesc c = columns.get(col);

            if (c.isVariableSize()) {
                for (int row = 0; row < nRow; row++) {
                    table.setElement(row, col, c.hasLongPointers() ? new long[2] : new int[2]);
                }
            }
        }

        heap = new FitsHeap(0);
    }

    /**
     * Returns the number of bytes per regular table row
     * 
     * @return the number of bytes in a regular table row.
     */
    final synchronized int getRowBytes() {
        return rowLen;
    }

    /**
     * @deprecated               (<i>for internal use</i>) It may become a private method in the future.
     *
     * @param      table         the table to create the column data.
     *
     * @throws     FitsException if the data could not be created.
     */
    public static void createColumnDataFor(BinaryTable table) throws FitsException {
        synchronized (table) {
            table.createTable(table.nRow);
        }
    }

    /**
     * @deprecated       (<i>for internal use</i>) It may be reduced to private visibility in the future. Parse the
     *                       TDIMS value. If the TDIMS value cannot be deciphered a one-d array with the size given in
     *                       arrsiz is returned.
     *
     * @param      tdims The value of the TDIMSn card.
     *
     * @return           An int array of the desired dimensions. Note that the order of the tdims is the inverse of the
     *                       order in the TDIMS key.
     */
    public static int[] parseTDims(String tdims) {
        if (tdims == null) {
            return null;
        }

        // The TDIMs value should be of the form: "(i,j...)"
        int start = tdims.indexOf('(');

        if (start < 0) {
            return null;
        }

        int end = tdims.indexOf(')', start);
        if (end < 0) {
            end = tdims.length();
        }

        StringTokenizer st = new StringTokenizer(tdims.substring(start + 1, end), ",");
        int dim = st.countTokens();

        if (dim > 0) {
            int[] dims = new int[dim];
            for (int i = dim; --i >= 0;) {
                dims[i] = Integer.parseInt(st.nextToken().trim());
            }
            return dims;
        }

        return null;
    }

    /**
     * <p>
     * Adds a column of complex values stored as the specified decimal type of components in the FITS. While you can
     * also use {@link #addColumn(Object)} to add complex values, that method will always add them as 64-bit
     * double-precision values. So, this method is provided to allow users more control over how they want their complex
     * data be stored.
     * </p>
     * <p>
     * The new column will be named as "Column <i>n</i>" (where <i>n</i> is the 1-based index of the column) by default,
     * which can be changed by {@link ColumnDesc#name(String)} after.
     * </p>
     * 
     * @param  o             A {@link ComplexValue} or an array (possibly multi-dimensional) thereof.
     * @param  decimalType   <code>float.class</code> or <code>double.class</code> (all other values default to
     *                           <code>double.class</code>).
     * 
     * @return               the number of column in the table including the new column.
     * 
     * @throws FitsException if the object contains values other than {@link ComplexValue} types or if the array is not
     *                           suitable for storing in the FITS, e.g. because it is multi-dimensional but varying in
     *                           shape / size.
     * 
     * @since                1.18
     * 
     * @see                  #addColumn(Object)
     */
    public int addComplexColumn(Object o, Class<?> decimalType) throws FitsException {
        int col = columns.size();
        int eSize = addColumn(ArrayFuncs.complexToDecimals(o, decimalType));
        ColumnDesc c = columns.get(col);
        c.isComplex = true;
        c.setLegacyShape(c.fitsShape);
        return eSize;
    }

    /**
     * <p>
     * Adds a column of string values (one per row), optimized for storage size. Unlike {@link #addColumn(Object)},
     * which always store strings in fixed format, this method will automatically use variable-length columns for
     * storing the strings if their lengths vary sufficiently to make that form of storage more efficient, or if the
     * array contains nulls (which may be defined later).
     * </p>
     * <p>
     * The new column will be named as "Column <i>n</i>" (where <i>n</i> is the 1-based index of the column) by default,
     * which can be changed by {@link ColumnDesc#name(String)} after.
     * </p>
     * 
     * @param  o             A 1D string array, with 1 string element per table row. The array may contain
     *                           <code>null</code> entries, in which case variable-length columns will be used, since
     *                           these may be defined later...
     * 
     * @return               the number of column in the table including the new column.
     * 
     * @throws FitsException if the object contains values other than {@link ComplexValue} types or if the array is not
     *                           suitable for storing in the FITS, e.g. because it is multi-dimensional but varying in
     *                           shape / size.
     * 
     * @since                1.18
     * 
     * @see                  #addColumn(Object)
     */
    public int addStringColumn(String[] o) throws FitsException {
        checkRowCount(o);

        ColumnDesc c = new ColumnDesc(String.class);

        // Check if we should be using variable-length strings
        // (provided its a scalar string column with sufficiently varied strings sizes to make it worth..
        int min = FitsUtil.minStringLength(o);
        int max = FitsUtil.maxStringLength(o);

        if (max - min > 2 * ElementType.forClass(c.pointerClass()).size()) {
            c = ColumnDesc.createForVariableSize(String.class);
            return addVariableSizeColumn(o, c);
        }

        c = ColumnDesc.createForStrings(max);
        return addFlattenedColumn(o, o.length, c, false);
    }

    /**
     * <p>
     * Adds a column of bits. This uses much less space than if adding boolean values as logicals (the default behaviot
     * of {@link #addColumn(Object)}, since logicals take up 1 byte per element, whereas bits are really single bits.
     * </p>
     * <p>
     * The new column will be named as "Column <i>n</i>" (where <i>n</i> is the 1-based index of the column) by default,
     * which can be changed by {@link ColumnDesc#name(String)} after.
     * </p>
     * 
     * @param  o                        An any-dimensional array of <code>boolean</code> values.
     * 
     * @return                          the number of column in the table including the new column.
     * 
     * @throws IllegalArgumentException if the argument is not an array of <code>boolean</code> values.
     * @throws FitsException            if the object is not an array of <code>boolean</code> values.
     * 
     * @since                           1.18
     * 
     * @see                             #addColumn(Object)
     */
    public int addBitsColumn(Object o) throws FitsException {
        if (ArrayFuncs.getBaseClass(o) != boolean.class) {
            throw new IllegalArgumentException("Not an array of booleans: " + o.getClass());
        }
        return addColumn(o, false);
    }

    /**
     * <p>
     * Adds a new empty column to the table to the specification. This is useful when the user may want ot have more
     * control on how columns are configured before calling {@link #addRow(Object[])} to start populating. The new
     * column will be named as "Column <i>n</i>" (where <i>n</i> is the 1-based index of the column) by default, unless
     * already named otherwise.
     * </p>
     * <p>
     * The new column will be named as "Column <i>n</i>" (where <i>n</i> is the 1-based index of the column) by default,
     * which can be changed by {@link ColumnDesc#name(String)} after.
     * </p>
     * 
     * @param  descriptor            the column descriptor
     * 
     * @return                       the number of table columns after the addition
     * 
     * @throws IllegalStateException if the table already contains data rows that prevent the addition of empty
     *                                   comlumns.
     * 
     * @see                          #addRow(Object[])
     * @see                          ColumnDesc#name(String)
     */
    public synchronized int addColumn(ColumnDesc descriptor) throws IllegalStateException {
        if (nRow != 0) {
            throw new IllegalStateException("Cannot add empty columns to table already containing data rows");
        }

        descriptor.offset = rowLen;
        rowLen += descriptor.rowLen();

        if (descriptor.name() == null) {
            // Set default column name;
            descriptor.name(TableHDU.getDefaultColumnName(columns.size()));
        }
        columns.add(descriptor);
        return columns.size();
    }

    /**
     * <p>
     * Adds a new column with the specified data array, with some default mappings. This method will always use
     * double-precision representation for {@link ComplexValue}-based data, and will represent <code>boolean</code>
     * based array data as one-byte-per element FITS logical values (for back compatibility). It will also store strings
     * as fixed sized (sized for the longest string element contained).
     * </p>
     * <p>
     * The new column will be named as "Column <i>n</i>" (where <i>n</i> is the 1-based index of the column) by default,
     * which can be changed by {@link ColumnDesc#name(String)} after.
     * </p>
     * <p>
     * If you want other complex-valued representations use {@link #addComplexColumn(Object, Class)} instead, and if you
     * want to pack <code>boolean</code>-based data more efficiently (using up to 8 times less space), use
     * {@link #addBitsColumn(Object)} instead, or else convert the column to bits afterwards using
     * {@link #convertToBits(int)}. And, if you want to allow storing strings more effiently in variable-length columns,
     * you should use {@link #addStringColumn(String[])} instead.
     * </p>
     * <p>
     * As of 1.18, the argument can be a boxed primitive for a coulmn containing a single scalar-valued entry (row).
     * </p>
     * 
     * @see #addVariableSizeColumn(Object)
     * @see #addComplexColumn(Object, Class)
     * @see #addBitsColumn(Object)
     * @see #convertToBits(int)
     * @see #addStringColumn(String[])
     * @see ColumnDesc#name(String)
     */
    @Override
    public int addColumn(Object o) throws FitsException {
        return addColumn(o, true);
    }

    private synchronized int checkRowCount(Object o) throws FitsException {
        if (!o.getClass().isArray()) {
            throw new TableException("Not an array: " + o.getClass().getName());
        }

        int rows = Array.getLength(o);

        if (columns.size() != 0 && rows != nRow) {
            throw new TableException("Mismatched number of rows: " + rows + ", expected " + nRow);
        }

        return rows;
    }

    /**
     * Like {@link #addColumn(Object)}, but allows specifying whether we use back compatible mode. This mainly just
     * affects how <code>boolean</code> arrays are stored (as logical bytes in compatibility mode, or as packed bits
     * otherwise).
     * 
     * @param Whether to add the column in a back compatibility mode with versions prior to 1.18. If <code>true</code>
     *                    <code>boolean</code> arrays will stored as logical bytes, otherwise as packed bits.
     */
    private int addColumn(Object o, boolean compat) throws FitsException {
        o = ArrayFuncs.objectToArray(o, compat);

        int rows = checkRowCount(o);

        ColumnDesc c = new ColumnDesc(ArrayFuncs.getBaseClass(o));

        if (ArrayFuncs.getBaseClass(o) == ComplexValue.class) {
            o = ArrayFuncs.complexToDecimals(o, double.class);
            c.isComplex = true;
        }

        try {
            int[] dim = ArrayFuncs.checkRegularArray(o, c.isNullAllowed());

            if (c.isString()) {
                c.setStringLength(FitsUtil.maxStringLength(o));
            }

            if (c.isComplex) {
                // Drop the railing 2 dimension, keep only outer dims...
                dim = Arrays.copyOf(dim, dim.length - 1);
                o = ArrayFuncs.flatten(o);
            }

            if (dim.length <= 1) {
                c.setSingleton();
            } else {
                int[] shape = new int[dim.length - 1];
                System.arraycopy(dim, 1, shape, 0, shape.length);
                c.setLegacyShape(shape);
                o = ArrayFuncs.flatten(o);
            }
        } catch (IllegalArgumentException e) {
            c.setVariableSize(false);
            return addVariableSizeColumn(o, c);
        }
        // getBaseClass() prevents heterogeneous columns, so no need to catch ClassCastException here.

        return addFlattenedColumn(o, rows, c, compat);
    }

    /**
     * <p>
     * Adds a new variable-length data column, populating it with the specified data object. Unlike
     * {@link #addColumn(Object)} which will use fixed-size data storage provided the data allows it, this method forces
     * the use of variable-sized storage regardless of the data layout -- for example to accommodate addiing rows /
     * elements of different sized at a later time.
     * </p>
     * <p>
     * The new column will be named as "Column <i>n</i>" (where <i>n</i> is the 1-based index of the column) by default,
     * which can be changed by {@link ColumnDesc#name(String)} after.
     * </p>
     * 
     * @param  o             An array containing one entry per row. Multi-dimensional entries will be flattened to 1D
     *                           for storage on the heap.
     * 
     * @return               the number of table columns after the addition.
     * 
     * @throws FitsException if the column could not be created as requested.
     * 
     * @see                  #addColumn(Object)
     * @see                  #addColumn(ColumnDesc)
     * @see                  ColumnDesc#createForVariableSize(Class)
     * @see                  ColumnDesc#isVariableSize()
     * 
     * @since                1.18
     */
    public int addVariableSizeColumn(Object o) throws FitsException {
        Class<?> base = ArrayFuncs.getBaseClass(o);
        ColumnDesc c = ColumnDesc.createForVariableSize(base);
        return addVariableSizeColumn(o, c);
    }

    /**
     * Adds a new column with data directly, without performing any checks on the data. This should only be use
     * internally, after ansuring the data integrity and suitability for this table.
     * 
     * @param  o             the column data, whose integrity was verified previously
     * @param  rows          the number of rows the data contains (in flattened form)
     * @param  c             the new column's descriptor
     * 
     * @return               the number of table columns after the addition
     * 
     * @throws FitsException if the data is not the right type or format for internal storage.
     */
    private synchronized int addDirectColumn(Object o, int rows, ColumnDesc c) throws FitsException {
        c.offset = rowLen;
        rowLen += c.rowLen();

        // Load any deferred data (we will not be able to do that once we alter the column structure)
        ensureData();

        // Set the default column name
        c.name(TableHDU.getDefaultColumnName(columns.size()));

        table.addColumn(o, c.getTableBaseCount());
        columns.add(c);

        if (nRow == 0) {
            // Set the table row count to match first colum
            nRow = rows;
        }

        return columns.size();
    }

    private int addVariableSizeColumn(Object o, ColumnDesc c) throws FitsException {
        checkRowCount(o);

        Object[] array = (Object[]) o;

        o = Array.newInstance(c.pointerClass(), array.length * 2);

        for (int i = 0; i < array.length; i++) {
            boolean multi = c.isComplex() ? array[i] instanceof Object[][] : array[i] instanceof Object[];

            if (multi) {
                boolean canBeComplex = false;

                if (c.getFitsBase() == float.class || c.getFitsBase() == double.class) {
                    int[] dim = ArrayFuncs.getDimensions(array[i]);
                    if (dim[dim.length - 1] == 2) {
                        canBeComplex = true;
                    }
                }

                if (!canBeComplex && !c.warnedFlatten) {
                    LOG.warning("Table entries of " + array[i].getClass()
                            + " will be stored as 1D arrays in variable-length columns. "
                            + "Array shape(s) and intermittent null subarrays (if any) will be lost.");

                    c.warnedFlatten = true;
                }
            }

            Object p = putOnHeap(c, array[i], null);
            System.arraycopy(p, 0, o, 2 * i, 2);
        }

        return addDirectColumn(o, array.length, c);
    }

    /**
     * Add a column where the data is already flattened.
     *
     * @param      o             The new column data. This should be a one-dimensional primitive array.
     * @param      dims          The dimensions of an element in the column, or null for singleton (scalar) columns
     *
     * @return                   the new column size
     *
     * @throws     FitsException if the array could not be flattened
     * 
     * @deprecated               (<i>for internal use</i>) No longer used, will be removed in the future
     */
    public int addFlattenedColumn(Object o, int... dims) throws FitsException {
        ColumnDesc c = new ColumnDesc(ArrayFuncs.getBaseClass(o));

        try {
            ArrayFuncs.checkRegularArray(o, c.isNullAllowed());
        } catch (IllegalArgumentException e) {
            throw new FitsException("Irregular array: " + o.getClass() + ": " + e.getMessage(), e);
        }

        if (c.isString()) {
            c.setStringLength(FitsUtil.maxStringLength(o));
        }

        int n = 1;

        c.setLegacyShape(dims);
        for (int dim : dims) {
            n *= dim;
        }

        int rows = Array.getLength(o) / n;

        return addFlattenedColumn(o, rows, c, true);
    }

    /**
     * Checks that a flattened column has a compatible size for storing in a fixed-width column. It will also log a
     * warning if the storage size of the object is zero.
     * 
     * @param  c             the column descriptor
     * @param  o             the column data
     * 
     * @throws FitsException if the data is not the right size for the column
     */
    private synchronized void checkFlattenedColumnSize(ColumnDesc c, Object o) throws FitsException {
        if (c.getTableBaseCount() == 0) {
            LOG.warning("Elements of column + " + columns.size() + " have zero storage size.");
        } else if (columns.size() > 0) {
            // Check that the number of rows is consistent.
            int l = Array.getLength(o);
            if (nRow > 0 && l != nRow * c.getTableBaseCount()) {
                throw new TableException("Mismatched element count " + l + ", expected " + (nRow * c.getTableBaseCount()));
            }
        }
    }

    /**
     * This function is needed since we had made addFlattenedColumn public so in principle a user might have called it
     * directly.
     *
     * @param  o             The new column data. This should be a one-dimensional primitive array.
     * @param  c             The column description
     *
     * @return               the new column size
     *
     * @throws FitsException if the data type, format, or element count is inconsistent with this table.
     */
    private int addFlattenedColumn(Object o, int rows, ColumnDesc c, boolean compat) throws FitsException {
        // For back compatibility this method will add boolean values as logicals always...
        if (compat) {
            c.isBits = false;
        }

        if (c.isBits) {
            // Special handling for bits, which have to be segmented into bytes...
            boolean[] bits = (boolean[]) o;
            o = FitsUtil.bitsToBytes(bits, bits.length / rows);
        } else {
            o = javaToFits1D(c, o);
        }

        checkFlattenedColumnSize(c, o);

        return addDirectColumn(o, rows, c);
    }

    /**
     * <p>
     * Adds a row to the table. If this is the first row in a new table, fixed-length columns will be created from the
     * data type automatically. If you want more control over the column formats, you may want to specify columns
     * beforehand such as:
     * </p>
     * 
     * <pre>
     *   BinaryTable table = new BinaryTable();
     *   
     *   // A column containing 64-bit floating point scalar values, 1 per row...
     *   table.addColumn(ColumnDesc.createForScalars(double.class));
     *   
     *   // A column containing 5x4 arrays of single-precision complex values...
     *   table.addColumn(ColumnDesc.createForArrays(ComplexValue.Float.class, 5, 4)
     *  
     *   // A column containing Strings of variable length using 32-bit heap pointers...
     *   table.addColumn(ColumnDesc.creatForVariableStrings(false);
     * </pre>
     * <p>
     * For scalar columns of primitive types, the argument may be the corresponding java boxed type (new style), or a
     * primitive array of 1 (old style). Thus, you can write either:
     * </p>
     * 
     * <pre>
     * table.addRow(1, 3.14159265);
     * </pre>
     * <p>
     * or,
     * </p>
     * 
     * <pre>
     *   table.addRow(new Object[] { new int[] {1}, new double[] {3.14159265} };
     * </pre>
     * 
     * @see #addColumn(ColumnDesc)
     */
    @Override
    public synchronized int addRow(Object[] o) throws FitsException {
        if (columns.isEmpty()) {
            for (Object element : o) {
                if (element == null) {
                    throw new TableException("Prototype row may not contain null");
                }

                Class<?> cl = element.getClass();

                if (cl.isArray()) {
                    if (cl.getComponentType().isPrimitive() && Array.getLength(element) == 1) {
                        // Primitives of 1 (e.g. short[1]) are wrapped and should be added as is.
                        addColumn(element);
                    } else {
                        // Wrap into array of 1, as leading dimension becomes the number of rows, which must be 1...
                        Object wrapped = Array.newInstance(element.getClass(), 1);
                        Array.set(wrapped, 0, element);
                        addColumn(wrapped);
                    }
                } else {
                    addColumn(ArrayFuncs.objectToArray(element, true));
                }
            }

            return 1;
        }

        if (o.length != columns.size()) {
            throw new TableException("Mismatched row size: " + o.length + ", expected " + columns.size());
        }

        ensureData();

        Object[] flatRow = new Object[getNCols()];

        for (int i = 0; i < flatRow.length; i++) {
            ColumnDesc c = columns.get(i);
            if (c.isVariableSize()) {
                flatRow[i] = putOnHeap(c, o[i], null);
            } else {
                flatRow[i] = javaToFits1D(c, ArrayFuncs.flatten(o[i]));

                int nexp = c.getElementCount();
                if (c.stringLength > 0) {
                    nexp *= c.stringLength;
                }

                if (Array.getLength(flatRow[i]) != nexp) {
                    throw new IllegalArgumentException("Mismatched element count for column " + i + ": got "
                            + Array.getLength(flatRow[i]) + ", expected " + nexp);
                }
            }
        }

        table.addRow(flatRow);
        nRow++;

        return nRow;
    }

    @Override
    public synchronized void deleteColumns(int start, int len) throws FitsException {
        ensureData();

        table.deleteColumns(start, len);

        ArrayList<ColumnDesc> remain = new ArrayList<>(columns.size() - len);
        rowLen = 0;

        for (int i = 0; i < columns.size(); i++) {
            if (i < start || i >= start + len) {
                ColumnDesc c = columns.get(i);
                c.offset = rowLen;
                rowLen += c.rowLen();
                remain.add(c);
            }
        }
        columns = remain;
    }

    @Override
    public synchronized void deleteRows(int row, int len) throws FitsException {
        ensureData();
        table.deleteRows(row, len);
        nRow -= len;
    }

    /**
     * Returns the Java type of elements returned or expected by the older srray-based access methods. It can be
     * confusing, because:
     * <ul>
     * <li>Columns with variable sized entries report <code>int.class</code> or <code>long.class</code> regardless of
     * data type.</li>
     * <li>Regular logical and bit columns bith report <code>boolean.class</code>.</li>
     * <li>Regular complex valued columns report <code>float.class</code> or <code>double.class</code>.</li>
     * </ul>
     * 
     * @return     the types in the table, not the underlying types (e.g., for varying length arrays or booleans).
     * 
     * @deprecated (<i>for internal use</i>) Ambiguous, use {@link ColumnDesc#getElementClass()} instead. Will remove in
     *                 the future.
     */
    public synchronized Class<?>[] getBases() {
        return table.getBases();
    }

    /**
     * <p>
     * Returns the data for a particular column in as an array of elements. See {@link #addColumn(Object)} for more
     * information about the format of data elements in general.
     * </p>
     * 
     * @param  col           The zero-based column index.
     * 
     * @return               an array of primitives (for scalar columns), or else an <code>Object[]</code> array, or
     *                           possibly <code>null</code>
     * 
     * @throws FitsException if the table could not be accessed
     * 
     * @see                  #setColumn(int, Object)
     * @see                  #getElement(int, int)
     * @see                  #getNCols()
     */
    @Override
    public synchronized Object getColumn(int col) throws FitsException {
        ColumnDesc c = columns.get(col);

        if (!c.isVariableSize() && c.fitsDimension() == 0 && !c.isComplex()) {
            return getFlattenedColumn(col);
        }

        ensureData();

        Object[] data = null;

        for (int i = 0; i < nRow; i++) {
            Object e = getElement(i, col);
            if (data == null) {
                data = (Object[]) Array.newInstance(e.getClass(), nRow);
            }
            data[i] = e;
        }

        return data;
    }

    /**
     * Returns the Java index of the first column by the specified name.
     * 
     * @param  name the name of the column (case sensitive).
     * 
     * @return      The column index, or else -1 if this table does not contain a column by the specified name.
     * 
     * @see         #getDescriptor(String)
     * @see         ColumnDesc#name(String)
     * 
     * @since       1.20
     * 
     * @author      Attila Kovacs
     */
    public int indexOf(String name) {
        for (int col = 0; col < columns.size(); col++) {
            if (name.equals(getDescriptor(col).name())) {
                return col;
            }
        }
        return -1;
    }

    @Override
    protected synchronized ColumnTable<?> getCurrentData() {
        return table;
    }

    @Override
    public ColumnTable<?> getData() throws FitsException {
        return (ColumnTable<?>) super.getData();
    }

    /**
     * Returns the dimensions of elements in each column.
     * 
     * @return     an array of arrays with the dimensions of each column's data.
     * 
     * @see        ColumnDesc#getDimens()
     * 
     * @deprecated (<i>for internal use</i>) Use {@link ColumnDesc#getEntryShape()} to access the shape of Java elements
     *                 individually for columns instead. Not useful to users since it returns the dimensions of the
     *                 primitive storage types, which is not always the dimension of elements on the Java side (notably
     *                 for string entries).
     */
    public int[][] getDimens() {
        int[][] dimens = new int[columns.size()][];
        for (int i = 0; i < dimens.length; i++) {
            dimens[i] = columns.get(i).getDimens();
        }
        return dimens;
    }

    /**
     * @deprecated               (<i>for internal use</i>) It may be private in the future.
     * 
     * @return                   An array with flattened data, in which each column's data is represented by a 1D array
     * 
     * @throws     FitsException if the reading of the data failed.
     */
    public Object[] getFlatColumns() throws FitsException {
        ensureData();
        return table.getColumns();
    }

    /**
     * @deprecated               (<i>for internal use</i>) It may be reduced to private visibility in the future.
     * 
     * @return                   column in flattened format. This is sometimes useful for fixed-sized columns.
     *                               Variable-sized columns will still return an <code>Object[]</code> array in which
     *                               each entry is the variable-length data for a row.
     *
     * @param      col           the column to flatten
     *
     * @throws     FitsException if the column could not be flattened
     */
    public synchronized Object getFlattenedColumn(int col) throws FitsException {
        if (!validColumn(col)) {
            throw new TableException("Invalid column index " + col + " in table of " + getNCols() + " columns");
        }

        ColumnDesc c = columns.get(col);
        if (c.isVariableSize()) {
            throw new TableException("Cannot flatten variable-sized column data");
        }

        ensureData();

        if (c.isBits()) {
            boolean[] bits = new boolean[nRow * c.fitsCount];
            for (int i = 0; i < nRow; i++) {
                boolean[] seg = (boolean[]) fitsToJava1D(c, table.getElement(i, col), c.fitsCount, false);
                System.arraycopy(seg, 0, bits, i * c.fitsCount, c.fitsCount);
            }
            return bits;
        }

        return fitsToJava1D(c, table.getColumn(col), 0, false);
    }

    /**
     * <p>
     * Reserves space for future addition of rows at the end of the regular table. In effect, this pushes the heap to
     * start at an offset value, leaving a gap between the main table and the heap in the FITS file. If your table
     * contains variable-length data columns, you may also want to reserve extra heap space for these via
     * {@link #reserveHeapSpace(int)}.
     * </p>
     * <p>
     * Note, that (C)FITSIO, as of version 4.4.0, has no proper support for offset heaps, and so you may want to be
     * careful using this function as the resulting FITS files, while standard, may not be readable by other tools due
     * to their own lack of support. Note, however, that you may also use this function to undo an offset heap with an
     * argument &lt;=0;
     * </p>
     * 
     * @param  rows The number of future rows fow which space should be reserved (relative to the current table size)
     *                  for future additions, or &lt;=0 to ensure that the heap always follows immediately after the
     *                  main table, e.g. for better (C)FITSIO interoperability.
     * 
     * @see         #reserveHeapSpace(int)
     * 
     * @since       1.19.1
     * 
     * @author      Attila Kovacs
     */
    public synchronized void reserveRowSpace(int rows) {
        heapAddress = rows > 0 ? getRegularTableSize() + (long) rows * getRowBytes() : 0;
    }

    /**
     * Reserves space in the file at the end of the heap for future heap growth (e.g. different/longer or new VLA
     * entries). You may generally want to call this along with {@link #reserveRowSpace(int)} if yuor table contains
     * variable-length columns, to ensure storage for future data in these. You may call with &lt;=0 to discards any
     * previously reserved space.
     * 
     * @param  bytes The number of bytes of unused space to reserve at the end of the heap, e.g. for future
     *                   modifications or additions, when writing the data to file.
     * 
     * @see          #reserveRowSpace(int)
     * 
     * @since        1.19.1
     * 
     * @author       Attila Kovacs
     */
    public synchronized void reserveHeapSpace(int bytes) {
        heapReserve = Math.max(0, bytes);
    }

    /**
     * Returns the address of the heap from the star of the HDU in the file.
     * 
     * @return (bytes) the start of the heap area from the beginning of the HDU.
     */
    final synchronized long getHeapAddress() {
        long tableSize = getRegularTableSize();
        return heapAddress > tableSize ? heapAddress : tableSize;
    }

    /**
     * Returns the offset from the end of the main table
     * 
     * @return the offset to the heap
     */
    final long getHeapOffset() {
        return getHeapAddress() - getRegularTableSize();
    }

    /**
     * It returns the heap size for storing in the FITS, which is the larger of the actual space occupied by the current
     * heap, or the original heap size based on the header when the HDU was read from an input. In the former case it
     * will also include heap space reserved for future additions.
     * 
     * @return (byte) the size of the heap in the FITS file.
     * 
     * @see    #compact()
     * @see    #reserveHeapSpace(int)
     */
    private synchronized int getHeapSize() {
        if (heap != null && heap.size() + heapReserve > heapFileSize) {
            return heap.size() + heapReserve;
        }
        return heapFileSize;
    }

    /**
     * @return the size of the heap -- including the offset from the end of the table data, and reserved space after.
     */
    synchronized long getParameterSize() {
        return getHeapOffset() + getHeapSize();
    }

    /**
     * Returns an empty row for the table. Such model rows are useful when low-level reading binary tables from an input
     * row-by-row. You can simply all {@link nom.tam.util.ArrayDataInput#readArrayFully(Object)} to populate it with
     * data from a stream. You may also use model rows to add additional rows to an existing table.
     * 
     * @return     a row that may be used for direct i/o to the table.
     * 
     * @deprecated (<i>for internal use</i>) Use {@link #getElement(int, int)} instead for low-level reading of tables
     *                 in deferred mode. Not recommended for uses because it requires a deep understanding of how data
     *                 (especially varialbe length columns) are represented in the FITS. Will reduce visibility to
     *                 private in the future.
     */
    public Object[] getModelRow() {
        Object[] modelRow = new Object[columns.size()];
        for (int i = 0; i < modelRow.length; i++) {
            ColumnDesc c = columns.get(i);
            if (c.fitsDimension() < 2) {
                modelRow[i] = Array.newInstance(c.getTableBase(), c.getTableBaseCount());
            } else {
                modelRow[i] = Array.newInstance(c.getTableBase(), c.fitsShape);
            }
        }
        return modelRow;
    }

    @Override
    public int getNCols() {
        return columns.size();
    }

    @Override
    public synchronized int getNRows() {
        return nRow;
    }

    /**
     * Reads a regular table element in the main table from the input. This method should never be called unless we have
     * a random-accessible input associated, which is a requirement for deferred read mode.
     * 
     * @param  o             The array element to populate
     * @param  c             the column descriptor
     * @param  row           the zero-based row index of the element
     * 
     * @throws IOException   If there was an I/O error accessing the input
     * @throws FitsException If there was some other error
     */
    private synchronized void readTableElement(Object o, ColumnDesc c, int row) throws IOException, FitsException {
        @SuppressWarnings("resource")
        RandomAccess in = getRandomAccessInput();

        synchronized (this) {
            in.position(getFileOffset() + row * (long) rowLen + c.offset);
        }

        if (c.isLogical()) {
            in.readArrayFully(o);
        } else {
            in.readImage(o);
        }
    }

    /**
     * Returns an unprocessed element from the table as a 1D array of the elements that are stored in the regular table
     * data, whithout reslving heap references. That is this call will return flattened versions of multidimensional
     * arrays, and will return only the heap locator (offset and size) for variable-sized columns.
     * 
     * @return                   a particular element from the table but do no processing of this element (e.g.,
     *                               dimension conversion or extraction of variable length array elements/)
     *
     * @param      row           The row of the element.
     * @param      col           The column of the element.
     * 
     * @deprecated               (<i>for internal use</i>) Will reduce visibility in the future.
     *
     * @throws     FitsException if the operation failed
     */
    public synchronized Object getRawElement(int row, int col) throws FitsException {
        if (!validRow(row) || !validColumn(col)) {
            throw new TableException("No such element (" + row + "," + col + ")");
        }

        if (table == null) {
            try {
                ColumnDesc c = columns.get(col);
                Object e = c.newInstance(1);
                readTableElement(e, c, row);
                return e;
            } catch (IOException e) {
                throw new FitsException("Error reading from input: " + e.getMessage(), e);
            }
        }

        ensureData();
        return table.getElement(row, col);
    }

    /**
     * Returns a table element as a Java array. Consider using the more Java-friendly {@link #get(int, int)} or one of
     * the scalar access methods with implicit type conversion support.
     * 
     * @see #get(int, int)
     * @see #getLogical(int, int)
     * @see #getNumber(int, int)
     * @see #getLong(int, int)
     * @see #getDouble(int, int)
     * @see #getString(int, int)
     */
    @Override
    public Object getElement(int row, int col) throws FitsException {
        return getElement(row, col, false);
    }

    /**
     * Returns a a table entry, with control over how FITS logical values are to be handled.
     * 
     * @param  row           zero-based row index
     * @param  col           zero-based column index
     * @param  isEnhanced    Whether logicals should be returned as {@link Boolean} (rather than <code>boolean</code>)
     *                           and complex values as {@link ComplexValue} (rather than <code>float[2]</code> or
     *                           <code>double[2]</code>), or arrays thereof. Methods prior to 1.18 should set this to
     *                           <code>false</code> for back compatible behavior.
     * 
     * @return               The entry as a primitive array, or {@link String}, {@link Boolean} or {@link ComplexValue},
     *                           or arrays thereof.
     * 
     * @throws FitsException If the requested element could not be accessed.
     */
    private Object getElement(int row, int col, boolean isEnhanced) throws FitsException {
        if (!validRow(row) || !validColumn(col)) {
            throw new TableException("No such element (" + row + "," + col + ")");
        }

        ColumnDesc c = columns.get(col);
        Object o = getRawElement(row, col);

        if (c.isVariableSize()) {
            return getFromHeap(c, o, isEnhanced);
        }

        o = fitsToJava1D(c, o, c.isBits() ? c.fitsCount : 0, isEnhanced);

        if (c.legacyShape.length > 1) {
            return ArrayFuncs.curl(o, c.legacyShape);
        }

        return o;
    }

    /**
     * Returns a table element as an array of the FITS storage type. Similar to the original
     * {@link #getElement(int, int)}, except that FITS logicals are returned as arrays of <code>Boolean</code> (rather
     * than <code>boolean</code>), bits are returned as arrays of <code>boolean</code>, and complex values are returned
     * as arrays of {@link ComplexValue} rather than arrays of <code>double[2]</code> or <code>float[2]</code>.
     * Singleton (scalar) table elements are not boxed to an enclosing Java type (unlike {@link #get(int, int)}), an
     * instead returned as arrays of just one element. For example, a single logical as a <code>Boolean[1]</code>, a
     * single float as a <code>float[1]</code> or a single double-precision complex value as
     * <code>ComplexValue[1]</code>.
     * 
     * @param  row zero-based row index
     * @param  col zero-based column index
     * 
     * @return     The table entry as an array of the stored Java type, without applying any type or quantization
     *                 conversions.
     * 
     * @see        #getArrayElementAs(int, int, Class)
     * @see        #get(int, int)
     * 
     * @since      1.20
     */
    public Object getArrayElement(int row, int col) {
        return getElement(row, col, true);
    }

    /**
     * <p>
     * Returns a numerical table element as an array of a specific underlying other numerical type. Similar
     * {@link #getArrayElement(int, int)} except that table entries are converted to the specified array type before
     * returning. If an integer-decimal conversion is involved, it will be performed through the column's quantizer (if
     * any) or else via a simple rounding as necessary.
     * </p>
     * <p>
     * For example, if you have an <code>short</code>-type column, and you want is an array of <code>double</code>
     * values that are represented by the 16-bit integers, then the conversion will use the column's quantizer scaling
     * and offset before returning the result either as an array of doubles, and the designated <code>short</code>
     * blanking values will be converted to NaNs.
     * </p>
     * 
     * @param  row                      zero-based row index
     * @param  col                      zero-based column index
     * @param  asType                   The desired underlying type, a primitive class or a {@link ComplexValue} type
     *                                      for appropriate numerical arrays (with a trailing Java dimension of 2 for
     *                                      the real/imaginary pairs).
     * 
     * @return                          An array of the desired type (e.g. <code>double[][]</code> if
     *                                      <code>asType</code> is <code>double.class</code> and the column contains 2D
     *                                      arrays of some numerical type).
     * 
     * @throws IllegalArgumentException if the numerical conversion is not possible for the given column type or if the
     *                                      type argument is not a supported numerical primitive or {@link ComplexValue}
     *                                      type.
     * 
     * @see                             #getArrayElement(int, int)
     * 
     * @since                           1.20
     */
    public Object getArrayElementAs(int row, int col, Class<?> asType) throws IllegalArgumentException {
        ColumnDesc c = getDescriptor(col);
        Object e = getElement(row, col, true);
        return asType.isAssignableFrom(c.getFitsBase()) ? e : ArrayFuncs.convertArray(e, asType, c.getQuantizer());
    }

    /**
     * <p>
     * Returns a table element using the usual Java boxing for primitive scalar (singleton) entries, or packaging
     * complex values as {@link ComplexValue}, or as appropriate primitive or object arrays. FITS string columns return
     * {@link String} values. Logical (<code>boolean</code> columns will return a {@link Boolean}, which may be
     * <code>null</code> if undefined (as per the FITS standard). Multibit FITS bits colums return arrays of
     * <code>boolean</code>.
     * </p>
     * <p>
     * As opposed to {@link #getElement(int, int)} scalar (singleton) values are not wrapped into primitive arrays, but
     * return either a singular object, such as a ({@link String}, or a {@link ComplexValue}, or a boxed Java primitive.
     * Thus, columns containing single <code>short</code> entries will return the selected element as a {@link Short},
     * or columns containing single <code>double</code> values will return the element as a {@link Double} and so on.
     * </p>
     * <p>
     * Array columns will return the expected arrays of primitive values, or arrays of one of the mentioned types. Note
     * however, that logical arrays are returned as arrays of {@link Boolean}, e.g. <code>Boolean[][]</code>, <b>not</b>
     * <code>boolean[][]</code>. This is because FITS allows <code>null</code> values for logicals beyond <code>
     * true</code> and <code>false</code>, which is reproduced by the boxed type, but not by the primitive type. FITS
     * columns of bits (generally preferrably to logicals if support for <code>null</code> values is not required) will
     * return arrays of <code>boolean</code>.
     * </p>
     * <p>
     * Columns containing multidimensional arrays, will return the expected multidimensional array of the above
     * mentioned types for the FITS storage type. You can then convert numerical arrays to other types as required for
     * your application via {@link ArrayFuncs#convertArray(Object, Class, Quantizer)}, including any appropriate
     * quantization for the colummn (see {@link ColumnDesc#getQuantizer()}).
     * </p>
     * 
     * @param  row           the zero-based row index
     * @param  col           the zero-based column index
     * 
     * @return               the element, either as a Java boxed type (for scalar entries), a singular Java Object, or
     *                           as a (possibly multi-dimensional) array of {@link String}, {@link Boolean},
     *                           {@link ComplexValue}, or primitives.
     * 
     * @throws FitsException if the element could not be obtained
     * 
     * @see                  #getNumber(int, int)
     * @see                  #getLogical(int, int)
     * @see                  #getString(int, int)
     * @see                  #getArrayElementAs(int, int, Class)
     * @see                  #set(int, int, Object)
     * 
     * @since                1.18
     */
    public Object get(int row, int col) throws FitsException {
        ColumnDesc c = columns.get(col);
        Object e = getElement(row, col, true);
        return (c.isSingleton() && e.getClass().isArray()) ? Array.get(e, 0) : e;
    }

    /**
     * Returns the numerical value, if possible, for scalar elements. Scalar numerical columns return the boxed type of
     * their primitive type. Thus, a column of <code>long</code> values will return {@link Long}, whereas a column of
     * <code>float</code> values will return a {@link Float}. Logical columns will return 1 if <code>true</code> or 0 if
     * <code>false</code>, or <code>null</code> if undefined. Array columns and other column types will throw an
     * exception.
     * 
     * @param  row                   the zero-based row index
     * @param  col                   the zero-based column index
     * 
     * @return                       the number value of the specified scalar entry
     * 
     * @throws FitsException         if the element could not be obtained
     * @throws ClassCastException    if the specified column in not a numerical scalar type.
     * @throws NumberFormatException if the it's a string column but the entry does not seem to be a number
     * 
     * @see                          #getDouble(int, int)
     * @see                          #getLong(int, int)
     * @see                          #get(int, int)
     * 
     * @since                        1.18
     */
    public final Number getNumber(int row, int col) throws FitsException, ClassCastException, NumberFormatException {
        Object o = get(row, col);
        if (o instanceof String) {
            try {
                return Long.parseLong((String) o);
            } catch (NumberFormatException e) {
                return Double.parseDouble((String) o);
            }
        }
        if (o instanceof Boolean) {
            return ((Boolean) o) ? 1 : 0;
        }
        return (Number) o;
    }

    /**
     * <p>
     * Returns the decimal value, if possible, of a scalar table entry. See {@link #getNumber(int, int)} for more
     * information on the conversion process.
     * </p>
     * <p>
     * Since version 1.20, if the column has a quantizer and stores integer elements, the conversion to double-precision
     * will account for the quantization of the column, if any, and will return NaN if the stored integer is the
     * designated blanking value (if any). To bypass quantization, you can use {@link #getNumber(int, int)} instead
     * followed by {@link Number#doubleValue()} to to get the stored integer values as a double.
     * </p>
     * 
     * @param  row                the zero-based row index
     * @param  col                the zero-based column index
     * 
     * @return                    the number value of the specified scalar entry
     * 
     * @throws FitsException      if the element could not be obtained
     * @throws ClassCastException if the specified column in not a numerical scalar type.
     * 
     * @see                       #getNumber(int, int)
     * @see                       #getLong(int, int)
     * @see                       #get(int, int)
     * @see                       ColumnDesc#getQuantizer()
     * 
     * @since                     1.18
     */
    public final double getDouble(int row, int col) throws FitsException, ClassCastException {
        Number n = getNumber(row, col);

        if (!(n instanceof Float || n instanceof Double)) {
            Quantizer q = getDescriptor(col).getQuantizer();
            if (q != null) {
                return q.toDouble(n.longValue());
            }
        }

        return n == null ? Double.NaN : n.doubleValue();
    }

    /**
     * <p>
     * Returns a 64-bit integer value, if possible, of a scalar table entry. Boolean columns will return 1 if
     * <code>true</code> or 0 if <code>false</code>, or throw a {@link NullPointerException} if undefined. See
     * {@link #getNumber(int, int)} for more information on the conversion process of the stored data element.
     * </p>
     * <p>
     * Additionally, since version 1.20, if the column has a quantizer and stores floating-point elements, the
     * conversion to integer will include the quantization, and NaN values will be converted to the designated integer
     * blanking values. To bypass quantization, you can use {@link #getNumber(int, int)} instead followed by
     * {@link Number#longValue()} to to get the stored floating point values rounded directly to a long.
     * </p>
     * 
     * @param  row                   the zero-based row index
     * @param  col                   the zero-based column index
     * 
     * @return                       the 64-bit integer number value of the specified scalar table entry.
     * 
     * @throws FitsException         if the element could not be obtained
     * @throws ClassCastException    if the specified column in not a numerical scalar type.
     * @throws IllegalStateException if the column contains a undefined (blanking value), such as a {@link Double#NaN}
     *                                   when no quantizer is set for the column, or a {@link Boolean} <code>null</code>
     *                                   value.
     * 
     * @see                          #getNumber(int, int)
     * @see                          #getDouble(int, int)
     * @see                          #get(int, int)
     * 
     * @since                        1.18
     */
    public final long getLong(int row, int col) throws FitsException, ClassCastException, IllegalStateException {
        Number n = getNumber(row, col);

        if (n instanceof Float || n instanceof Double) {
            Quantizer q = getDescriptor(col).getQuantizer();
            if (q != null) {
                return q.toLong(n.doubleValue());
            }
        }

        if (Double.isNaN(n.doubleValue())) {
            throw new IllegalStateException("Cannot convert NaN to long without Quantizer");
        }
        return n.longValue();
    }

    /**
     * Returns the boolean value, if possible, for scalar elements. It will will return<code>true</code>, or
     * <code>false</code>, or <code>null</code> if undefined. Numerical columns will return <code>null</code> if the
     * corresponding decimal value is NaN, or <code>false</code> if the value is 0, or else <code>true</code> for all
     * non-zero values (just like in C).
     * 
     * @param  row                the zero-based row index
     * @param  col                the zero-based column index
     * 
     * @return                    the boolean value of the specified scalar entry, or <code>null</code> if undefined.
     * 
     * @throws ClassCastException if the specified column in not a scalar boolean type.
     * @throws FitsException      if the element could not be obtained
     * 
     * @see                       #get(int, int)
     * 
     * @since                     1.18
     */
    @SuppressFBWarnings(value = "NP_BOOLEAN_RETURN_NULL", justification = "null has specific meaning here")
    public final Boolean getLogical(int row, int col) throws FitsException, ClassCastException {
        Object o = get(row, col);
        if (o == null) {
            return null;
        }

        if (o instanceof Number) {
            Number n = (Number) o;
            if (Double.isNaN(n.doubleValue())) {
                return null;
            }
            return n.longValue() != 0;
        }

        if (o instanceof Character) {
            char c = (Character) o;
            if (c == 'T' || c == 't' || c == '1') {
                return true;
            }
            if (c == 'F' || c == 'f' || c == '0') {
                return false;
            }
            return null;
        }

        if (o instanceof String) {
            return FitsUtil.parseLogical((String) o);
        }

        return (Boolean) o;
    }

    /**
     * Returns the string value, if possible, for scalar elements. All scalar columns will return the string
     * representation of their values, while <code>byte[]</code> and <code>char[]</code> are converted to appropriate
     * strings.
     * 
     * @param  row                the zero-based row index
     * @param  col                the zero-based column index
     * 
     * @return                    the string representatiof the specified table entry
     * 
     * @throws ClassCastException if the specified column contains array elements other than <code>byte[]</code> or
     *                                <code>char[]</code>
     * @throws FitsException      if the element could not be obtained
     * 
     * @see                       #get(int, int)
     * 
     * @since                     1.18
     */
    public final String getString(int row, int col) throws FitsException, ClassCastException {
        ColumnDesc c = columns.get(col);
        Object value = get(row, col);

        if (value == null) {
            return "null";
        }

        if (!value.getClass().isArray()) {
            return value.toString();
        }

        if (c.fitsDimension() > 1) {
            throw new ClassCastException("Cannot convert multi-dimensional array element to String");
        }

        if (value instanceof char[]) {
            return String.valueOf((char[]) value).trim();
        }
        if (value instanceof byte[]) {
            return AsciiFuncs.asciiString((byte[]) value).trim();
        }

        throw new ClassCastException("Cannot convert " + value.getClass().getName() + " to String.");
    }

    @Override
    public Object[] getRow(int row) throws FitsException {
        if (!validRow(row)) {
            throw new TableException("Invalid row index " + row + " in table of " + getNRows() + " rows");
        }

        Object[] data = new Object[columns.size()];
        for (int col = 0; col < data.length; col++) {
            data[col] = getElement(row, col);
        }
        return data;
    }

    /**
     * Returns the flattened (1D) size of elements in each column of this table. As of 1.18, this method returns a copy
     * ot the array used internally, which is safe to modify.
     * 
     * @return     an array with the byte sizes of each column
     * 
     * @deprecated (<i>for internal use</i>) Use {@link ColumnDesc#getElementCount()} instead. This one returns the
     *                 number of elements in the FITS representation, not in the java representation. For example, for
     *                 {@link String} entries, this returns the number of bytes stored, not the number of strings.
     *                 Similarly, for complex values it returns the number of components not the number of values.
     */
    public int[] getSizes() {
        int[] sizes = new int[columns.size()];
        for (int i = 0; i < sizes.length; i++) {
            sizes[i] = columns.get(i).getTableBaseCount();
        }
        return sizes;
    }

    /**
     * Returns the size of the regular table data, before the heap area.
     * 
     * @return the size of the regular table in bytes
     */
    private synchronized long getRegularTableSize() {
        return (long) nRow * rowLen;
    }

    @Override
    protected long getTrueSize() {
        return getRegularTableSize() + getParameterSize();
    }

    /**
     * Get the characters describing the base classes of the columns. As of 1.18, this method returns a copy ot the
     * array used internally, which is safe to modify.
     *
     * @return     An array of type characters (Java array types), one for each column.
     * 
     * @deprecated (<i>for internal use</i>) Use {@link ColumnDesc#getElementClass()} instead. Not very useful to users
     *                 since this returns the FITS primitive storage type for the data column.
     */
    public char[] getTypes() {
        char[] types = new char[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            types[i] = ElementType.forClass(columns.get(i).getTableBase()).type();
        }
        return types;
    }

    @Override
    public synchronized void setColumn(int col, Object o) throws FitsException {
        ColumnDesc c = columns.get(col);

        if (c.isVariableSize()) {
            Object[] array = (Object[]) o;
            for (int i = 0; i < nRow; i++) {
                Object p = putOnHeap(c, ArrayFuncs.flatten(array[i]), getRawElement(i, col));
                setTableElement(i, col, p);
            }
        } else {
            setFlattenedColumn(col, o);
        }
    }

    /**
     * Writes an element directly into the random accessible FITS file. Note, this call will not modify the table in
     * memory (if loaded). This method should never be called unless we have a valid encoder object that can handle the
     * writing, which is a requirement for deferred read mode.
     * 
     * @param  row         the zero-based row index
     * @param  col         the zero-based column index
     * @param  array       an array object containing primitive types, in FITS storage format. It may be
     *                         multi-dimensional.
     * 
     * @throws IOException the there was an error writing to the FITS output
     * 
     * @see                #setTableElement(int, int, Object)
     */
    @SuppressWarnings("resource")
    private void writeTableElement(int row, int col, Object array) throws IOException {
        synchronized (this) {
            ColumnDesc c = columns.get(col);
            getRandomAccessInput().position(getFileOffset() + row * (long) rowLen + c.offset);
        }
        encoder.writeArray(array);
    }

    /**
     * Sets a table element to an array in the FITS storage format. If the data is in deferred mode it will write the
     * table entry directly into the file. Otherwise it will update the table entry in memory. For variable sized
     * column, the heap will always be updated in memory, so you may want to call {@link #rewrite()} when done updating
     * all entries.
     * 
     * @param  row           the zero-based row index
     * @param  col           the zero-based column index
     * @param  o             an array object containing primitive types, in FITS storage format. It may be
     *                           multi-dimensional.
     *
     * @throws FitsException if the array is invalid for the given column, or if the table could not be accessed in the
     *                           file / input.
     * 
     * @see                  #setTableElement(int, int, Object)
     * @see                  #getRawElement(int, int)
     */
    private synchronized void setTableElement(int row, int col, Object o) throws FitsException {
        if (table == null) {
            try {
                writeTableElement(row, col, o);
            } catch (IOException e) {
                throw new FitsException(e.getMessage(), e);
            }
        } else {
            ensureData();
            table.setElement(row, col, o);
        }
    }

    /**
     * Consider using the more Java-friendly {@link #set(int, int, Object)} with implicit scalar type conversions.
     * 
     * @see #set(int, int, Object)
     */
    @Override
    public void setElement(int row, int col, Object o) throws FitsException {
        ColumnDesc c = columns.get(col);
        o = c.isVariableSize() ? putOnHeap(c, o, getRawElement(row, col)) : javaToFits1D(c, ArrayFuncs.flatten(o));
        setTableElement(row, col, o);
    }

    /**
     * <p>
     * The Swiss-army knife of setting table entries, including Java boxing, and with some support for automatic type
     * conversions. The argument may be one of the following type:
     * </p>
     * <ul>
     * <li>Scalar values -- any Java primitive with its boxed type, such as a {@link Double}, or a
     * {@link Character}.</li>
     * <li>A single {@link String} or {@link ComplexValue} object.
     * <li>An array (including multidimensional) of primitive types, or that of {@link Boolean}, {@link ComplexValue},
     * or {@link String}.</li>
     * </ul>
     * <p>
     * For array-type columns the argument needs to match the column type exactly. However, you may call
     * {@link ArrayFuncs#convertArray(Object, Class, Quantizer)} prior to setting values to convert arrays to the
     * desired numerical types, including the quantization that is appropriate for the column (see
     * {@link ColumnDesc#getQuantizer()}).
     * </p>
     * <p>
     * For scalar (single element) columns, automatic type conversions may apply, to make setting scalar columns more
     * flexible:
     * </p>
     * <ul>
     * <li>Any numerical column can take any {@link Number} value. The conversion is as if an explicit Java cast were
     * applied. For example, if setting a <code>double</code> value for a column of single <code>short</code> values it
     * as if a <code>(short)</code> cast were applied to the value.</li>
     * <li>Numerical colums can also take {@link Boolean} values which set the entry to 1, or 0, or to
     * {@link Double#isNaN()} (or the equivalent integer minimum value) if the argument is <code>null</code>. Numerical
     * columns can also set {@link String} values, by parsing the string according to the numerical type of the
     * column.</li>
     * <li>Logical columns can set {@link Boolean} values, including <code>null</code>values, but also any
     * {@link Number} type. In case of numbers, zero values map to <code>false</code> while definite non-zero values map
     * to <code>true</code>. {@link Double#isNaN()} maps to a <code>null</code> (or undefined) entry. Loginal columns
     * can be also set to the {@link String} values of 'true' or 'false', or to a {@link Character} of 'T'/'F' (or
     * equivalently '1'/'0') and 0 (undefined)</li>
     * <li>Singular string columns can be set to any scalar type owing to Java's {@link #toString()} method performing
     * the conversion, as long as the string representation fits into the size constraints (if any) for the string
     * column.</li>
     * </ul>
     * <p>
     * Additionally, scalar columns can take single-element array arguments, just like
     * {@link #setElement(int, int, Object)}.
     * </p>
     * 
     * @param  row                      the zero-based row index
     * @param  col                      the zero-based column index
     * @param  o                        the new value to set. For array columns this must match the Java array type
     *                                      exactly, but for scalar columns additional flexibility is provided for fuzzy
     *                                      type matching (see description above).
     * 
     * @throws FitsException            if the column could not be set
     * @throws IllegalArgumentException if the argument cannot be converted to a value for the specified column type.
     * 
     * @since                           1.18
     * 
     * @see                             #get(int, int)
     */
    public void set(int row, int col, Object o) throws FitsException, IllegalArgumentException {
        ColumnDesc c = columns.get(col);

        if (o == null) {
            // Only logicals and strings support 'null' values
            if (!c.isSingleton()) {
                throw new TableException("No null values allowed for column of " + c.getLegacyBase() + " arrays.");
            } else if (c.isString()) {
                setElement(row, col, "");
            } else {
                setLogical(row, col, null);
            }
        } else if (o.getClass().isArray()) {
            Class<?> eType = ArrayFuncs.getBaseClass(o);
            if (!c.getFitsBase().isAssignableFrom(eType) && c.isNumeric()) {
                o = ArrayFuncs.convertArray(o, c.getFitsBase(), c.getQuantizer());
            }
            setElement(row, col, o);
        } else if (o instanceof String) {
            setString(row, col, (String) o);
        } else if (!c.isSingleton()) {
            throw new TableException("Cannot set scalar values in non-scalar columns");
        } else if (c.isString()) {
            setElement(row, col, o.toString());
        } else if (o instanceof Boolean) {
            setLogical(row, col, (Boolean) o);
        } else if (o instanceof Character) {
            setCharacter(row, col, (Character) o);
        } else if (o instanceof Number) {
            setNumber(row, col, (Number) o);
        } else if (o instanceof ComplexValue) {
            setElement(row, col, o);
        } else {
            throw new IllegalArgumentException("Unsupported scalar type: " + o.getClass());
        }
    }

    /**
     * Sets a scalar table entry to the specified numerical value.
     * 
     * @param  row                the zero-based row index
     * @param  col                the zero-based column index
     * @param  value              the new number value
     * 
     * @throws ClassCastException if the specified column in not a numerical scalar type.
     * @throws FitsException      if the table element could not be altered
     * 
     * @see                       #getNumber(int, int)
     * @see                       #set(int, int, Object)
     * 
     * @since                     1.18
     */
    private void setNumber(int row, int col, Number value) throws FitsException, ClassCastException {
        ColumnDesc c = columns.get(col);

        // Already checked before calling...
        // if (!c.isSingleton()) {
        // throw new ClassCastException("Cannot set scalar value for array column " + col);
        // }

        if (c.isLogical()) {
            Boolean b = null;
            if (!Double.isNaN(value.doubleValue())) {
                b = value.longValue() != 0;
            }
            setTableElement(row, col, new byte[] {FitsEncoder.byteForBoolean(b)});
            return;
        }

        Class<?> base = c.getLegacyBase();

        // quantize / unquantize as necessary...
        Quantizer q = c.getQuantizer();

        if (q != null) {
            boolean decimalBase = (base == float.class || base == double.class);
            boolean decimalValue = (value instanceof Float || value instanceof Double || value instanceof BigInteger
                    || value instanceof BigDecimal);

            if (decimalValue && !decimalBase) {
                value = q.toLong(value.doubleValue());
            } else if (!decimalValue && decimalBase) {
                value = q.toDouble(value.longValue());
            }
        }

        Object wrapped = null;

        if (base == byte.class) {
            wrapped = new byte[] {value.byteValue()};
        } else if (base == short.class) {
            wrapped = new short[] {value.shortValue()};
        } else if (base == int.class) {
            wrapped = new int[] {value.intValue()};
        } else if (base == long.class) {
            wrapped = new long[] {value.longValue()};
        } else if (base == float.class) {
            wrapped = new float[] {value.floatValue()};
        } else if (base == double.class) {
            wrapped = new double[] {value.doubleValue()};
        } else {
            // This could be a char based column...
            throw new ClassCastException("Cannot set number value for column of type " + base);
        }

        setTableElement(row, col, wrapped);
    }

    /**
     * Sets a boolean scalar table entry to the specified value.
     * 
     * @param  row                the zero-based row index
     * @param  col                the zero-based column index
     * @param  value              the new boolean value
     * 
     * @throws ClassCastException if the specified column in not a boolean scalar type.
     * @throws FitsException      if the table element could not be altered
     * 
     * @see                       #getLogical(int, int)
     * @see                       #set(int, int, Object)
     * 
     * @since                     1.18
     */
    private void setLogical(int row, int col, Boolean value) throws FitsException, ClassCastException {
        ColumnDesc c = columns.get(col);

        // Already checked before calling...
        // if (!c.isSingleton()) {
        // throw new ClassCastException("Cannot set scalar value for array column " + col);
        // }

        if (c.isLogical()) {
            setTableElement(row, col, new byte[] {FitsEncoder.byteForBoolean(value)});
        } else if (c.getLegacyBase() == char.class) {
            setTableElement(row, col, new char[] {value == null ? '\0' : (value ? 'T' : 'F')});
        } else {
            setNumber(row, col, value == null ? Double.NaN : (value ? 1 : 0));
        }
    }

    /**
     * Sets a Unicode character scalar table entry to the specified value.
     * 
     * @param  row                the zero-based row index
     * @param  col                the zero-based column index
     * @param  value              the new Unicode character value
     * 
     * @throws ClassCastException if the specified column in not a boolean scalar type.
     * @throws FitsException      if the table element could not be altered
     * 
     * @see                       #getString(int, int)
     * 
     * @since                     1.18
     */
    private void setCharacter(int row, int col, Character value) throws FitsException, ClassCastException {
        ColumnDesc c = columns.get(col);

        // Already checked before calling...
        // if (!c.isSingleton()) {
        // throw new IllegalArgumentException("Cannot set scalar value for array column " + col);
        // }

        if (c.isLogical()) {
            setLogical(row, col, FitsUtil.parseLogical(value.toString()));
        } else if (c.fitsBase == char.class) {
            setTableElement(row, col, new char[] {value});
        } else if (c.fitsBase == byte.class) {
            setTableElement(row, col, new byte[] {(byte) (value & FitsIO.BYTE_MASK)});
        } else {
            throw new ClassCastException("Cannot convert char value to " + c.fitsBase.getName());
        }
    }

    /**
     * Sets a table entry to the specified string value. Scalar column will attempt to parse the value, while
     * <code>byte[]</code> and <coce>char[]</code> type columns will convert the string provided the string's length
     * does not exceed the entry size for these columns (the array elements will be padded with zeroes). Note, that
     * scalar <code>byte</code> columns will parse the string as a number (not as a single ASCII character).
     * 
     * @param  row                      the zero-based row index
     * @param  col                      the zero-based column index
     * @param  value                    the new boolean value
     * 
     * @throws ClassCastException       if the specified column is not a scalar type, and neither it is a
     *                                      <code>byte[]</code> or <coce>char[]</code> column.
     * @throws IllegalArgumentException if the String is too long to contain in the column.
     * @throws NumberFormatException    if the numerical value could not be parsed.
     * @throws FitsException            if the table element could not be altered
     * 
     * @see                             #getString(int, int)
     * @see                             #set(int, int, Object)
     * 
     * @since                           1.18
     */
    private void setString(int row, int col, String value)
            throws FitsException, ClassCastException, IllegalArgumentException, NumberFormatException {
        ColumnDesc c = columns.get(col);

        // Already checked before calling...
        // if (!c.isSingleton()) {
        // throw new IllegalArgumentException("Cannot set scalar value for array column " + col);
        // }

        if (c.isLogical()) {
            setLogical(row, col, FitsUtil.parseLogical(value));
        } else if (value.length() == 1) {
            setCharacter(row, col, value.charAt(0));
        } else if (c.fitsDimension() > 1) {
            throw new ClassCastException("Cannot convert String to multi-dimensional array");
        } else if (c.fitsDimension() == 1) {
            if (c.fitsBase != char.class && c.fitsBase != byte.class) {
                throw new ClassCastException("Cannot cast String to " + c.fitsBase.getName());
            }
            int len = c.isVariableSize() ? value.length() : c.fitsCount;
            if (value.length() > len) {
                throw new IllegalArgumentException("String size " + value.length() + " exceeds entry size of " + len);
            }
            if (c.fitsBase == char.class) {
                setTableElement(row, col, Arrays.copyOf(value.toCharArray(), len));
            } else {
                setTableElement(row, col, FitsUtil.stringToByteArray(value, len));
            }
        } else {
            try {
                setNumber(row, col, Long.parseLong(value));
            } catch (NumberFormatException e) {
                setNumber(row, col, Double.parseDouble(value));
            }
        }
    }

    /**
     * @deprecated               (<i>for internal use</i>) It may be reduced to private visibility in the future. Sets a
     *                               column with the data already flattened.
     *
     * @param      col           The index of the column to be replaced.
     * @param      data          The new data array. This should be a one-d primitive array.
     *
     * @throws     FitsException Thrown if the type of length of the replacement data differs from the original.
     */
    public synchronized void setFlattenedColumn(int col, Object data) throws FitsException {
        ensureData();

        Object oldCol = table.getColumn(col);
        if (data.getClass() != oldCol.getClass() || Array.getLength(data) != Array.getLength(oldCol)) {
            throw new TableException("Replacement column mismatch at column:" + col);
        }
        table.setColumn(col, javaToFits1D(columns.get(col), data));
    }

    @Override
    public void setRow(int row, Object[] data) throws FitsException {
        ensureData();

        if (data.length != getNCols()) {
            throw new TableException("Mismatched number of columns: " + data.length + ", expected " + getNCols());
        }

        for (int col = 0; col < data.length; col++) {
            set(row, col, data[col]);
        }
    }

    /**
     * @deprecated It is not entirely foolproof for keeping the header in sync -- it is better to (re)wrap tables in a
     *                 new HDU after column deletions, and then edit the new header as necessary to incorporate custom
     *                 entries. May be removed from the API in the future.
     */
    @Override
    public synchronized void updateAfterDelete(int oldNcol, Header hdr) throws FitsException {
        hdr.addValue(Standard.NAXIS1, rowLen);
        int l = 0;
        for (ColumnDesc d : columns) {
            d.offset = l;
            l += d.rowLen();
        }
    }

    @SuppressWarnings("resource")
    @Override
    public void write(ArrayDataOutput os) throws FitsException {
        synchronized (this) {

            try {
                if (isDeferred() && os == getRandomAccessInput()) {
                    // It it's a deferred mode re-write, then data were edited in place if at all,
                    // so we can skip the main table.
                    ((RandomAccess) os).skipAllBytes(getRegularTableSize());
                } else {
                    // otherwise make sure we loaded all data before writing to the output
                    ensureData();

                    // Write the regular table (if any)
                    if (getRegularTableSize() > 0) {
                        table.write(os);
                    }
                }

                // Now check if we need to write the heap
                if (getParameterSize() > 0) {
                    for (long rem = getHeapOffset(); rem > 0;) {
                        byte[] b = new byte[(int) Math.min(getHeapOffset(), 1 << Short.SIZE)];
                        os.write(b);
                        rem -= b.length;
                    }

                    getHeap().write(os);

                    if (heapReserve > 0) {
                        byte[] b = new byte[heapReserve];
                        os.write(b);
                    }
                }

                FitsUtil.pad(os, getTrueSize(), (byte) 0);
            } catch (IOException e) {
                throw new FitsException("Unable to write table:" + e, e);
            }
        }
    }

    /**
     * Returns the heap offset component from a pointer.
     * 
     * @param  p the pointer, either a <code>int[2]</code> or a <code>long[2]</code>.
     * 
     * @return   the offset component from the pointer
     */
    private long getPointerOffset(Object p) {
        return (p instanceof long[]) ? ((long[]) p)[1] : ((int[]) p)[1];
    }

    /**
     * Returns the number of elements reported in a heap pointer.
     * 
     * @param  p the pointer, either a <code>int[2]</code> or a <code>long[2]</code>.
     * 
     * @return   the element count component from the pointer
     */
    private long getPointerCount(Object p) {
        return (p instanceof long[]) ? ((long[]) p)[0] : ((int[]) p)[0];
    }

    /**
     * Puts a FITS data array onto our heap, returning its locator pointer. The data will overwrite the previous heap
     * entry, if provided, so long as the new data fits in the same place. Otherwise the new data is placed at the end
     * of the heap.
     * 
     * @param  c             The column descriptor, specifying the data type
     * @param  o             The variable-length data
     * @param  p             The heap pointer, where this element was stored on the heap before, or <code>null</code> if
     *                           we aren't replacing an earlier entry.
     * 
     * @return               the heap pointer information, either <code>int[2]</code> or else a <code>long[2]</code>
     * 
     * @throws FitsException if the data could not be accessed in full from the heap.
     */
    @SuppressFBWarnings(value = "RR_NOT_CHECKED", justification = "not propagated or used locally")
    private Object putOnHeap(ColumnDesc c, Object o, Object oldPointer) throws FitsException {
        return putOnHeap(getHeap(), c, o, oldPointer);
    }

    /**
     * Puts a FITS data array onto a specific heap, returning its locator pointer. The data will overwrite the previous
     * heap entry, if provided, so long as the new data fits in the same place. Otherwise the new data is placed at the
     * end of the heap.
     * 
     * @param  h             The heap object to use.
     * @param  c             The column descriptor, specifying the data type
     * @param  o             The variable-length data in Java form.
     * @param  p             The heap pointer, where this element was stored on the heap before, or <code>null</code> if
     *                           we aren't replacing an earlier entry.
     * 
     * @return               the heap pointer information, either <code>int[2]</code> or else a <code>long[2]</code>
     * 
     * @throws FitsException if the data could not be accessed in full from the heap.
     */
    @SuppressFBWarnings(value = "RR_NOT_CHECKED", justification = "not propagated or used locally")
    private Object putOnHeap(FitsHeap h, ColumnDesc c, Object o, Object oldPointer) throws FitsException {
        // Flatten data for heap
        o = ArrayFuncs.flatten(o);

        // By default put data at the end of the heap;
        int off = h.size();

        // The number of Java elements is the same as the number of FITS elements, except for strings and complex
        // numbers
        int len = (c.isComplex() || c.isString()) ? -1 : Array.getLength(o);

        // Convert to FITS storage array
        o = javaToFits1D(c, o);

        // For complex values and strings, determine length from converted object....
        if (len < 0) {
            len = Array.getLength(o);

            // If complex in primitive 1D form, then length is half the number of elements.
            if (c.isComplex() && o.getClass().getComponentType().isPrimitive()) {
                len >>>= 1;
            }
        }

        if (oldPointer != null) {
            if (len <= getPointerCount(oldPointer)) {
                // Write data back at the old heap location
                off = (int) getPointerOffset(oldPointer);
            }
        }

        h.putData(o, off);

        return c.hasLongPointers() ? new long[] {len, off} : new int[] {len, off};
    }

    /**
     * Returns a FITS data array from the heap
     * 
     * @param  c             The column descriptor, specifying the data type
     * @param  p             The heap pointer, either <code>int[2]</code> or else a <code>long[2]</code>
     * @param  isEnhanced    Whether logicals should be returned as {@link Boolean} (rather than <code>boolean</code>)
     *                           and complex values as {@link ComplexValue} (rather than <code>float[2]</code> or
     *                           <code>double[2]</code>), or arrays thereof. Methods prior to 1.18 should set this to
     *                           <code>false</code> for back compatible behavior.
     * 
     * @return               the FITS array object retrieved from the heap
     * 
     * @throws FitsException if the data could not be accessed in full from the heap.
     */
    protected Object getFromHeap(ColumnDesc c, Object p, boolean isEnhanced) throws FitsException {
        long len = getPointerCount(p);
        long off = getPointerOffset(p);

        if (off > Integer.MAX_VALUE || len > Integer.MAX_VALUE) {
            throw new FitsException("Data located beyond 32-bit accessible heap limit: off=" + off + ", len=" + len);
        }

        Object e = null;

        if (c.isComplex()) {
            e = Array.newInstance(c.getFitsBase(), (int) len, 2);
        } else {
            e = Array.newInstance(c.getFitsBase(), c.getFitsBaseCount((int) len));
        }

        readHeap(off, e);

        return fitsToJava1D(c, e, (int) len, isEnhanced);
    }

    /**
     * Convert Java arrays to their FITS representation. Transformation include boolean &rightarrow; 'T'/'F' or '\0';
     * Strings &rightarrow; byte arrays; variable length arrays &rightarrow; pointers (after writing data to heap).
     *
     * @param  c             The column descritor
     * @param  o             A one-dimensional Java array
     *
     * @return               An one-dimensional array with values as stored in FITS.
     * 
     * @throws FitsException if the operation failed
     */
    private static Object javaToFits1D(ColumnDesc c, Object o) throws FitsException {

        if (c.isBits()) {
            if (o instanceof Boolean && c.isSingleton()) {
                // Scalar boxed boolean...
                return FitsUtil.bitsToBytes(new boolean[] {(Boolean) o});
            }
            return FitsUtil.bitsToBytes((boolean[]) o);
        }

        if (c.isLogical()) {
            // Convert true/false to 'T'/'F', or null to '\0'
            return FitsUtil.booleansToBytes(o);
        }

        if (c.isComplex()) {
            if (o instanceof ComplexValue || o instanceof ComplexValue[]) {
                return ArrayFuncs.complexToDecimals(o, c.fitsBase);
            }
        }

        if (c.isString()) {
            // Convert strings to array of bytes.
            if (o == null) {
                if (c.isVariableSize()) {
                    return new byte[0];
                }

                return Array.newInstance(byte.class, c.fitsShape);
            }

            if (o instanceof String) {
                int l = c.getStringLength();
                if (l < 0) {
                    // Not fixed width, write the whole string.
                    l = ((String) o).length();
                }
                return FitsUtil.stringToByteArray((String) o, l);
            }

            if (c.isVariableSize() && c.delimiter != 0) {
                // Write variable-length string arrays in delimited form

                for (String s : (String[]) o) {
                    // We set the string length to that of the longest element + 1
                    c.setStringLength(Math.max(c.stringLength, s == null ? 1 : s.length() + 1));
                }

                return FitsUtil.stringsToDelimitedBytes((String[]) o, c.getStringLength(), c.delimiter);
            }

            // Fixed length substring array (not delimited).
            // For compatibility with tools that do not process array dimension, ASCII NULL should not
            // be used between components (permissible only at the end of all strings)
            return FitsUtil.stringsToByteArray((String[]) o, c.getStringLength(), FitsUtil.BLANK_SPACE);
        }

        return ArrayFuncs.objectToArray(o, true);
    }

    /**
     * Converts from the FITS representation of data to their basic Java array representation.
     *
     * @param  c             The column descritor
     * @param  o             A one-dimensional array of values as stored in FITS
     * @param  bits          A bit count for bit arrays (otherwise unused).
     * @param  isEnhanced    Whether logicals should be returned as {@link Boolean} (rather than <code>boolean</code>)
     *                           and complex values as {@link ComplexValue} (rather than <code>float[2]</code> or
     *                           <code>double[2]</code>), or arrays thereof. Methods prior to 1.18 should set this to
     *                           <code>false</code> for back compatible behavior.
     *
     * @return               A {@link String} or a one-dimensional array with the matched basic Java type
     * 
     * @throws FitsException if the operation failed
     */
    private Object fitsToJava1D(ColumnDesc c, Object o, int bits, boolean isEnhanced) {

        if (c.isBits()) {
            return FitsUtil.bytesToBits((byte[]) o, bits);
        }

        if (c.isLogical()) {
            return isEnhanced ? FitsUtil.bytesToBooleanObjects(o) : FitsUtil.byteToBoolean((byte[]) o);
        }

        if (c.isComplex() && isEnhanced) {
            return ArrayFuncs.decimalsToComplex(o);
        }

        if (c.isString()) {
            byte[] bytes = (byte[]) o;

            int len = c.getStringLength();

            if (c.isVariableSize()) {
                if (c.delimiter != 0) {
                    // delimited array of strings
                    return FitsUtil.delimitedBytesToStrings(bytes, c.getStringLength(), c.delimiter);
                }
            }

            // If fixed or variable length arrays of strings...
            if (c.isSingleton()) {
                // Single fixed string -- get it all but trim trailing spaces
                return FitsUtil.extractString(bytes, new ParsePosition(0), bytes.length, FitsUtil.ASCII_NULL);
            }

            // Array of fixed-length strings -- we trim trailing spaces in each component
            String[] s = new String[bytes.length / len];
            for (int i = 0; i < s.length; i++) {
                s[i] = FitsUtil.extractString(bytes, new ParsePosition(i * len), len, FitsUtil.ASCII_NULL);
            }
            return s;
        }

        return o;
    }

    /**
     * Create a column table with the specified number of rows. This is used when we defer instantiation of the
     * ColumnTable until the user requests data from the table.
     * 
     * @param  rows          the number of rows to allocate
     * 
     * @throws FitsException if the operation failed
     */
    protected synchronized void createTable(int rows) throws FitsException {
        int nfields = columns.size();
        Object[] data = new Object[nfields];
        int[] sizes = new int[nfields];
        for (int i = 0; i < nfields; i++) {
            ColumnDesc c = columns.get(i);
            sizes[i] = c.getTableBaseCount();
            data[i] = c.newInstance(rows);
        }

        table = createColumnTable(data, sizes);
        nRow = rows;
    }

    /**
     * Sets the input to use for reading (and possibly writing) this table. If the input implements
     * {@link ReadWriteAccess}, then it can be used for both reading and (re)writing the data, including editing in
     * deferred mode.
     * 
     * @param in The input from which we can read the table data.
     */
    private void setInput(ArrayDataInput in) {
        encoder = (in instanceof ReadWriteAccess) ? new FitsEncoder((ReadWriteAccess) in) : null;
    }

    @Override
    public void read(ArrayDataInput in) throws FitsException {
        setInput(in);
        super.read(in);
    }

    @Override
    protected void loadData(ArrayDataInput in) throws IOException, FitsException {
        setInput(in);
        synchronized (this) {
            createTable(nRow);
        }
        readTrueData(in);
    }

    /**
     * Extracts a column descriptor from the FITS header for a given column index
     * 
     * @param  header        the FITS header containing the column description(s)
     * @param  col           zero-based column index
     * 
     * @return               the Descriptor for that column.
     * 
     * @throws FitsException if the header deswcription is invalid or incomplete
     */
    public static ColumnDesc getDescriptor(Header header, int col) throws FitsException {
        String tform = header.getStringValue(Standard.TFORMn.n(col + 1));

        if (tform == null) {
            throw new FitsException("Missing TFORM" + (col + 1));
        }

        int count = 1;
        char type = 0;

        ParsePosition pos = new ParsePosition(0);

        try {
            count = AsciiFuncs.parseInteger(tform, pos);
        } catch (Exception e) {
            // Keep going...
        }

        try {
            type = Character.toUpperCase(AsciiFuncs.extractChar(tform, pos));
        } catch (Exception e) {
            throw new FitsException("Missing data type in TFORM: [" + tform + "]");
        }

        ColumnDesc c = new ColumnDesc();

        if (header.containsKey(Standard.TTYPEn.n(col + 1))) {
            c.name(header.getStringValue(Standard.TTYPEn.n(col + 1)));
        }

        if (type == POINTER_INT || type == POINTER_LONG) {
            // Variable length column...
            c.setVariableSize(type == POINTER_LONG);

            // Get the data type...
            try {
                type = Character.toUpperCase(AsciiFuncs.extractChar(tform, pos));
            } catch (Exception e) {
                throw new FitsException("Missing variable-length data type in TFORM: [" + tform + "]");
            }
        }

        // The special types...
        if (type == 'C' || type == 'M') {
            c.isComplex = true;
        } else if (type == 'X') {
            c.isBits = true;
        }

        if (!c.setFitsType(type)) {
            throw new FitsException("Invalid type '" + type + "' in column:" + col);
        }

        if (!c.isVariableSize()) {
            // Fixed sized column...
            int[] dims = parseTDims(header.getStringValue(Standard.TDIMn.n(col + 1)));

            if (dims == null) {
                c.setFitsShape((count == 1 && type != 'A') ? SINGLETON_SHAPE : new int[] {count});
                c.stringLength = -1; // T.B.D. further below...
            } else {
                c.setFitsShape(dims);
            }
        }

        if (c.isString()) {
            // For vairable-length columns or of TDIM was not defined determine substring length from TFORM.
            c.parseSubstringConvention(tform, pos, c.getStringLength() < 0);
        }

        // Force to use the count in the header, even if it does not match up with the dimension otherwise.
        c.fitsCount = count;

        c.quant = Quantizer.fromTableHeader(header, col);
        if (c.quant.isDefault()) {
            c.quant = null;
        }

        return c;
    }

    /**
     * Process one column from a FITS Header.
     * 
     * @throws FitsException if the operation failed
     */
    private int processCol(Header header, int col, int offset) throws FitsException {
        ColumnDesc c = getDescriptor(header, col);
        c.offset = offset;
        columns.add(c);

        return c.rowLen();
    }

    /**
     * @deprecated (<i>for internal use</i>) Used Only by {@link nom.tam.image.compression.hdu.CompressedTableData} so
     *                 it would make a better private method in there.. `
     */
    protected void addByteVaryingColumn() {
        addColumn(ColumnDesc.createForVariableSize(byte.class));
    }

    /**
     * @deprecated (<i>for internal use</i>) This method should have visibility reduced to private
     */
    @SuppressWarnings("javadoc")
    protected ColumnTable<?> createColumnTable(Object[] arrCol, int[] sizes) throws TableException {
        return new ColumnTable<>(arrCol, sizes);
    }

    /**
     * Returns the heap, after initializing it from the input as necessary
     * 
     * @return               the initialized heap
     * 
     * @throws FitsException if we had trouble initializing it from the input.
     */
    @SuppressWarnings("resource")
    private synchronized FitsHeap getHeap() throws FitsException {
        if (heap == null) {
            readHeap(getRandomAccessInput());
        }
        return heap;
    }

    /**
     * Reads an array from the heap. Subclasses may override this, for example to provide read-only access to a related
     * table's heap area.
     * 
     * @param  offset        the heap offset
     * @param  array         the array to populate from the heap area
     * 
     * @throws FitsException if there was an issue accessing the heap
     */
    protected void readHeap(long offset, Object array) throws FitsException {
        getHeap().getData((int) offset, array);
    }

    /**
     * Read the heap which contains the data for variable length arrays. A. Kovacs (4/1/08) Separated heap reading, s.t.
     * the heap can be properly initialized even if in deferred read mode. columnToArray() checks and initializes the
     * heap as necessary.
     *
     * @param      input         stream to read from.
     *
     * @throws     FitsException if the heap could not be read from the stream
     * 
     * @deprecated               (<i>for internal use</i>) unused.
     */
    protected synchronized void readHeap(ArrayDataInput input) throws FitsException {
        if (input instanceof RandomAccess) {
            FitsUtil.reposition(input, getFileOffset() + getHeapAddress());
        }
        heap = new FitsHeap(heapFileSize);
        if (input != null) {
            heap.read(input);
        }
    }

    /**
     * Read table, heap and padding
     *
     * @param  i             the stream to read the data from.
     *
     * @throws FitsException if the reading failed
     */
    protected synchronized void readTrueData(ArrayDataInput i) throws FitsException {
        try {
            table.read(i);
            i.skipAllBytes(getHeapOffset());
            if (heap == null) {
                readHeap(i);
            }
        } catch (IOException e) {
            throw new FitsException("Error reading binary table data:" + e, e);
        }
    }

    /**
     * Check if the column number is valid.
     *
     * @param  j The Java index (first=0) of the column to check.
     *
     * @return   <code>true</code> if the column is valid
     */
    protected boolean validColumn(int j) {
        return j >= 0 && j < getNCols();
    }

    /**
     * Check to see if this is a valid row.
     *
     * @param  i The Java index (first=0) of the row to check.
     *
     * @return   <code>true</code> if the row is valid
     */
    protected boolean validRow(int i) {
        return getNRows() > 0 && i >= 0 && i < getNRows();
    }

    /**
     * @deprecated (<i>for internal use</i>) Visibility should be reduced to protected.
     */
    @Override
    public void fillHeader(Header h) throws FitsException {
        fillHeader(h, true);
    }

    /**
     * Fills (updates) the essential header description of this table in the header, optionally updating the essential
     * column descriptions also if desired.
     * 
     * @param  h             The FITS header to populate
     * @param  updateColumns Whether to update the essential column descriptions also
     * 
     * @throws FitsException if there was an error accessing the header.
     */
    void fillHeader(Header h, boolean updateColumns) throws FitsException {
        h.deleteKey(Standard.SIMPLE);
        h.deleteKey(Standard.EXTEND);

        Standard.context(BinaryTable.class);

        Cursor<String, HeaderCard> c = h.iterator();
        c.add(HeaderCard.create(Standard.XTENSION, Standard.XTENSION_BINTABLE));
        c.add(HeaderCard.create(Standard.BITPIX, Bitpix.BYTE.getHeaderValue()));
        c.add(HeaderCard.create(Standard.NAXIS, 2));

        synchronized (this) {
            c.add(HeaderCard.create(Standard.NAXIS1, rowLen));
            c.add(HeaderCard.create(Standard.NAXIS2, nRow));
        }

        if (h.getLongValue(Standard.PCOUNT, -1L) < getParameterSize()) {
            c.add(HeaderCard.create(Standard.PCOUNT, getParameterSize()));
        }

        c.add(HeaderCard.create(Standard.GCOUNT, 1));
        c.add(HeaderCard.create(Standard.TFIELDS, columns.size()));

        if (getHeapOffset() == 0) {
            h.deleteKey(Standard.THEAP);
        } else {
            c.add(HeaderCard.create(Standard.THEAP, getHeapAddress()));
        }

        if (updateColumns) {
            for (int i = 0; i < columns.size(); i++) {
                c.setKey(Standard.TFORMn.n(i + 1).key());
                fillForColumn(h, c, i);
            }
        }

        Standard.context(null);
    }

    /**
     * Update the header to reflect the details of a given column.
     *
     * @throws FitsException if the operation failed
     */
    void fillForColumn(Header header, Cursor<String, HeaderCard> hc, int col) throws FitsException {
        ColumnDesc c = columns.get(col);

        try {
            Standard.context(BinaryTable.class);

            if (c.name() != null) {
                hc.add(HeaderCard.create(Standard.TTYPEn.n(col + 1), c.name()));
            }

            hc.add(HeaderCard.create(Standard.TFORMn.n(col + 1), c.getTFORM()));

            String tdim = c.getTDIM();
            if (tdim != null) {
                hc.add(HeaderCard.create(Standard.TDIMn.n(col + 1), tdim));
            }

            if (c.quant != null) {
                c.quant.editTableHeader(header, col);
            }

        } finally {
            Standard.context(null);
        }
    }

    /**
     * Returns the column descriptor of a given column in this table
     * 
     * @param  column                         the zero-based column index
     * 
     * @return                                the column's descriptor
     * 
     * @throws ArrayIndexOutOfBoundsException if this table does not contain a column with that index.
     * 
     * @see                                   #getDescriptor(String)
     */
    public ColumnDesc getDescriptor(int column) throws ArrayIndexOutOfBoundsException {
        return columns.get(column);
    }

    /**
     * Returns the (first) column descriptor whose name matches the specified value.
     * 
     * @param  name The column name (case sensitive).
     * 
     * @return      The descriptor of the first column by that name, or <code>null</code> if the table contains no
     *                  column by that name.
     * 
     * @see         #getDescriptor(int)
     * @see         #indexOf(String)
     * 
     * @since       1.20
     * 
     * @author      Attila Kovacs
     */
    public ColumnDesc getDescriptor(String name) {
        int col = indexOf(name);
        return col < 0 ? null : getDescriptor(col);
    }

    /**
     * Converts a column from FITS logical values to bits. Null values (allowed in logical columns) will map to
     * <code>false</code>.
     *
     * @param  col The zero-based index of the column to be reset.
     *
     * @return     Whether the conversion was possible. *
     * 
     * @since      1.18
     */
    public boolean convertToBits(int col) {
        ColumnDesc c = columns.get(col);

        if (c.isBits) {
            return true;
        }

        if (c.base != boolean.class) {
            return false;
        }

        c.isBits = true;
        return true;
    }

    /**
     * Convert a column from float/double to float complex/double complex. This is only possible for certain columns.
     * The return status indicates if the conversion is possible.
     *
     * @param  index         The zero-based index of the column to be reset.
     *
     * @return               Whether the conversion is possible. *
     *
     * @throws FitsException if the operation failed
     * 
     * @since                1.18
     * 
     * @see                  ColumnDesc#isComplex()
     * @see                  #addComplexColumn(Object, Class)
     */
    public synchronized boolean setComplexColumn(int index) throws FitsException {

        if (!validColumn(index)) {
            return false;
        }

        ColumnDesc c = columns.get(index);
        if (c.isComplex()) {
            return true;
        }

        if (c.base != float.class && c.base != double.class) {
            return false;
        }

        if (!c.isVariableSize()) {
            if (c.getLastFitsDim() != 2) {
                return false;
            }
            // Set the column to complex
            c.isComplex = true;

            // Update the legacy (wrapped array) shape
            c.setLegacyShape(c.fitsShape);
            return true;
        }

        // We need to make sure that for every row, there are
        // an even number of elements so that we can
        // convert to an integral number of complex numbers.
        for (int i = 1; i < nRow; i++) {
            if (getPointerCount(getRawElement(i, index)) % 2 != 0) {
                return false;
            }
        }

        // Halve the length component of array descriptors (2 reals = 1 complex)
        for (int i = 1; i < nRow; i++) {
            Object p = getRawElement(i, index);
            long len = getPointerCount(p) >>> 1;
            if (c.hasLongPointers()) {
                ((long[]) p)[0] = len;
            } else {
                ((int[]) p)[0] = (int) len;
            }
            setTableElement(i, index, p);
        }

        // Set the column to complex
        c.isComplex = true;

        return true;
    }

    /**
     * Checks if this table contains a heap for storing variable length arrays (VLAs).
     * 
     * @return <code>true</code> if the table contains a heap, or else <code>false</code>.
     * 
     * @since  1.19.1
     */
    public final boolean containsHeap() {
        return getParameterSize() > 0;
    }

    /**
     * <p>
     * Defragments the heap area of this table, compacting the heap area, and returning the number of bytes by which the
     * heap size has been reduced. When tables with variable-sized columns are modified, the heap may retain old data as
     * columns are removed or elements get replaced with new data of different size. The data order in the heap may also
     * get jumbled, causing what would appear to be sequential reads to jump all over the heap space with the caching.
     * And, depending on how the heap was constructed in the first place, it may not be optimal for the row-after-row
     * table access that is the most typical use case.
     * </p>
     * <p>
     * This method rebuilds the heap by taking elements in table read order (by rows, and columns) and puts them on a
     * new heap.
     * </p>
     * <p>
     * For best squential read performance, you should defragment all tables that have been built column-by-column
     * before writing them to a FITS file. The only time defragmentation is really not needed is if the table was built
     * row-by-row, with no modifications to variable-length content after the fact.
     * </p>
     * 
     * @return               the number of bytes by which the heap has shrunk as a result of defragmentation.
     * 
     * @throws FitsException if there was an error accessing the heap or the main data table comntaining the heap
     *                           locators. In case of an error the table content may be left in a damaged state.
     * 
     * @see                  #compact()
     * @see                  #setElement(int, int, Object)
     * @see                  #addColumn(Object)
     * @see                  #deleteColumns(int, int)
     * @see                  #setColumn(int, Object)
     * 
     * @since                1.18
     */
    public synchronized long defragment() throws FitsException {
        if (!containsHeap()) {
            return 0L;
        }

        int[] eSize = new int[columns.size()];

        for (int j = 0; j < columns.size(); j++) {
            ColumnDesc c = columns.get(j);
            if (c.isVariableSize()) {
                eSize[j] = ElementType.forClass(c.getFitsBase()).size();
            }
        }

        FitsHeap hp = getHeap();
        long oldSize = hp.size();
        FitsHeap compact = new FitsHeap(0);

        for (int i = 0; i < nRow; i++) {
            for (int j = 0; j < columns.size(); j++) {
                ColumnDesc c = columns.get(j);
                if (c.isVariableSize()) {
                    Object p = getRawElement(i, j);

                    int len = (int) getPointerCount(p);

                    // Copy to new heap...
                    int pos = compact.copyFrom(hp, (int) getPointerOffset(p), c.getFitsBaseCount(len) * eSize[j]);

                    // Same length as before...
                    if (p instanceof long[]) {
                        ((long[]) p)[1] = pos;
                    } else {
                        ((int[]) p)[1] = pos;
                    }

                    // Update pointers in table
                    setTableElement(i, j, p);
                }
            }
        }

        heap = compact;
        return oldSize - compact.size();
    }

    /**
     * Discard the information about the original heap size (if this table was read from an input), and instead use the
     * real size of the actual heap (plus reserved space around it) when writing to an output. Compacted tables may not
     * be re-writeable to the same file from which they were read, since they may be shorter than the original, but they
     * can always be written to a different file, which may at times be smaller than the original. It may be used along
     * with {@link #defragment()} to create FITS files with optimized storage from FITS files that may contain wasted
     * space.
     * 
     * @see    #defragment()
     * 
     * @since  1.19.1
     * 
     * @author Attila Kovacs
     */
    public synchronized void compact() {
        heapFileSize = 0;
    }

    @Override
    public BinaryTableHDU toHDU() throws FitsException {
        Header h = new Header();
        fillHeader(h);
        return new BinaryTableHDU(h, this);
    }
}
