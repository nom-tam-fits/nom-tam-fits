package nom.tam.fits.header;

import nom.tam.fits.AsciiTable;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTable;
import nom.tam.fits.ImageData;
import nom.tam.fits.RandomGroupsData;
import nom.tam.fits.TableData;
import nom.tam.fits.UndefinedData;

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
 * This data dictionary lists the 53 keywords currently defined in the FITS
 * Standard.
 * 
 * <pre>
 *  @see <a href="http://heasarc.gsfc.nasa.gov/docs/fcg/standard_dict.html">http://heasarc.gsfc.nasa.gov/docs/fcg/standard_dict.html</a>
 * </pre>
 * 
 * @author Richard van Nieuwenhoven
 */
public enum Standard implements IFitsHeader {
    /**
     * The value field shall contain a character string identifying who compiled
     * the information in the data associated with the key. This keyword is
     * appropriate when the data originate in a published paper or are compiled
     * from many sources.
     */
    AUTHOR(SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "author of the data"),
    /**
     * The value field shall contain an integer. The absolute value is used in
     * computing the sizes of data structures. It shall specify the number of
     * bits that represent a data value. RANGE: -64,-32,8,16,32
     */
    BITPIX(SOURCE.MANDATORY, HDU.ANY, VALUE.INTEGER, "bits per data value", //
            replaceable("header:bitpix", Object.class) //
    ),
    /**
     * This keyword shall be used only in primary array headers or IMAGE
     * extension headers with positive values of BITPIX (i.e., in arrays with
     * integer data). Columns 1-8 contain the string, `BLANK ' (ASCII blanks in
     * columns 6-8). The value field shall contain an integer that specifies the
     * representation of array values whose physical values are undefined.
     */
    BLANK(SOURCE.RESERVED, HDU.IMAGE, VALUE.INTEGER, "value used for undefined array elements"),

    /**
     * Columns 1-8 contain ASCII blanks. This keyword has no associated value.
     * Columns 9-80 may contain any ASCII text. Any number of card images with
     * blank keyword fields may appear in a key.
     */
    BLANKS("        ", SOURCE.RESERVED, HDU.ANY, VALUE.NONE, "descriptive comment"),

    /**
     * This keyword may be used only in the primary key. It shall appear within
     * the first 36 card images of the FITS file. (Note: This keyword thus
     * cannot appear if NAXIS is greater than 31, or if NAXIS is greater than 30
     * and the EXTEND keyword is present.) Its presence with the required
     * logical value of T advises that the physical block size of the FITS file
     * on which it appears may be an integral multiple of the logical record
     * length, and not necessarily equal to it. Physical block size and logical
     * record length may be equal even if this keyword is present or unequal if
     * it is absent. It is reserved primarily to prevent its use with other
     * meanings. Since the issuance of version 1 of the standard, the BLOCKED
     * keyword has been deprecated.
     * 
     * @deprecated no blocksize other that 2880 may be used.
     */
    @Deprecated
    BLOCKED(SOURCE.RESERVED, HDU.PRIMARY, VALUE.LOGICAL, "is physical blocksize a multiple of 2880?"),

    /**
     * This keyword shall be used, along with the BZERO keyword, when the array
     * pixel values are not the true physical values, to transform the primary
     * data array values to the true physical values they represent, using the
     * equation: physical_value = BZERO + BSCALE * array_value. The value field
     * shall contain a floating point number representing the coefficient of the
     * linear term in the scaling equation, the ratio of physical value to array
     * value at zero offset. The default value for this keyword is 1.0.
     */
    BSCALE(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "linear factor in scaling equation"),

    /**
     * The value field shall contain a character string, describing the physical
     * units in which the quantities in the array, after application of BSCALE
     * and BZERO, are expressed. The units of all FITS key keyword values, with
     * the exception of measurements of angles, should conform with the
     * recommendations in the IAU Style Manual. For angular measurements given
     * as floating point values and specified with reserved keywords, degrees
     * are the recommended units (with the units, if specified, given as 'deg').
     */
    BUNIT(SOURCE.RESERVED, HDU.IMAGE, VALUE.STRING, "physical units of the array values"),

    /**
     * This keyword shall be used, along with the BSCALE keyword, when the array
     * pixel values are not the true physical values, to transform the primary
     * data array values to the true values using the equation: physical_value =
     * BZERO + BSCALE * array_value. The value field shall contain a floating
     * point number representing the physical value corresponding to an array
     * value of zero. The default value for this keyword is 0.0.
     */
    BZERO(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "zero point in scaling equation"),

    /**
     * The value field shall contain a floating point number giving the partial
     * derivative of the coordinate specified by the CTYPEn keywords with
     * respect to the pixel index, evaluated at the reference point CRPIXn, in
     * units of the coordinate specified by the CTYPEn keyword. These units must
     * follow the prescriptions of section 5.3 of the FITS Standard.
     */
    CDELTn(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "coordinate increment along axis"),

    /**
     * This keyword shall have no associated value; columns 9-80 may contain any
     * ASCII text. Any number of COMMENT card images may appear in a key.
     */
    COMMENT(SOURCE.RESERVED, HDU.ANY, VALUE.NONE, "descriptive comment"),

    /**
     * This keyword is used to indicate a rotation from a standard coordinate
     * system described by the CTYPEn to a different coordinate system in which
     * the values in the array are actually expressed. Rules for such rotations
     * are not further specified in the Standard; the rotation should be
     * explained in comments. The value field shall contain a floating point
     * number giving the rotation angle in degrees between axis n and the
     * direction implied by the coordinate system defined by CTYPEn. In unit
     * degrees.
     */
    CROTAn(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "coordinate system rotation angle"),

    /**
     * The value field shall contain a floating point number, identifying the
     * location of a reference point along axis n, in units of the axis index.
     * This value is based upon a counter that runs from 1 to NAXISn with an
     * increment of 1 per pixel. The reference point value need not be that for
     * the center of a pixel nor lie within the actual data array. Use comments
     * to indicate the location of the index point relative to the pixel.
     */
    CRPIXn(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "coordinate system reference pixel"),

    /**
     * The value field shall contain a floating point number, giving the value
     * of the coordinate specified by the CTYPEn keyword at the reference point
     * CRPIXn. Units must follow the prescriptions of section 5.3 of the FITS
     * Standard.
     */
    CRVALn(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "coordinate system value at reference pixel"),

    /**
     * The value field shall contain a character string, giving the name of the
     * coordinate represented by axis n.
     */
    CTYPEn(SOURCE.RESERVED, HDU.IMAGE, VALUE.STRING, "name of the coordinate axis"),

    /**
     * The value field shall always contain a floating point number, regardless
     * of the value of BITPIX. This number shall give the maximum valid physical
     * value represented by the array, exclusive of any special values.
     */
    DATAMAX(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "maximum data value"),

    /**
     * The value field shall always contain a floating point number, regardless
     * of the value of BITPIX. This number shall give the minimum valid physical
     * value represented by the array, exclusive of any special values.
     */
    DATAMIN(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "minimum data value"),

    /**
     * The date on which the HDU was created, in the format specified in the
     * FITS Standard. The old date format was 'yy/mm/dd' and may be used only
     * for dates from 1900 through 1999. the new Y2K compliant date format is
     * 'yyyy-mm-dd' or 'yyyy-mm-ddTHH:MM:SS[.sss]'.
     */

    DATE(SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "date of file creation"),

    /**
     * The date of the observation, in the format specified in the FITS
     * Standard. The old date format was 'yy/mm/dd' and may be used only for
     * dates from 1900 through 1999. The new Y2K compliant date format is
     * 'yyyy-mm-dd' or 'yyyy-mm-ddTHH:MM:SS[.sss]'.
     */
    DATE_OBS("DATE-OBS", SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "date of the observation"),

    /**
     * This keyword has no associated value. Columns 9-80 shall be filled with
     * ASCII blanks.
     */
    END(SOURCE.MANDATORY, HDU.ANY, VALUE.NONE, "marks the end of the key keywords"),

    /**
     * The value field shall contain a floating point number giving the equinox
     * in years for the celestial coordinate system in which positions are
     * expressed. Starting with Version 1, the Standard has deprecated the use
     * of the EPOCH keyword and thus it shall not be used in FITS files created
     * after the adoption of the standard; rather, the EQUINOX keyword shall be
     * used.
     * 
     * @deprecated use {@link #EQUINOX} in stead
     */
    @Deprecated
    EPOCH(SOURCE.RESERVED, HDU.ANY, VALUE.REAL, "equinox of celestial coordinate system"),
    /**
     * The value field shall contain a floating point number giving the equinox
     * in years for the celestial coordinate system in which positions are
     * expressed.
     */
    EQUINOX(SOURCE.RESERVED, HDU.ANY, VALUE.REAL, "equinox of celestial coordinate system"),

    /**
     * If the FITS file may contain extensions, a card image with the keyword
     * EXTEND and the value field containing the logical value T must appear in
     * the primary key immediately after the last NAXISn card image, or, if
     * NAXIS=0, the NAXIS card image. The presence of this keyword with the
     * value T in the primary key does not require that extensions be present.
     */
    EXTEND(SOURCE.RESERVED, HDU.PRIMARY, VALUE.LOGICAL, "may the FITS file contain extensions?", //
            replaceable("basichdu:extend", BasicHDU.class, "Allow extensions"), //
            replaceable("header:extend", Object.class, "Extensions are permitted"), //
            replaceable("imagedata:extend", ImageData.class, "Extension permitted"), //
            replaceable("undefineddata:extend", UndefinedData.class, "Extensions are permitted")

    ),

    /**
     * The value field shall contain an integer, specifying the level in a
     * hierarchy of extension levels of the extension key containing it. The
     * value shall be 1 for the highest level; levels with a higher value of
     * this keyword shall be subordinate to levels with a lower value. If the
     * EXTLEVEL keyword is absent, the file should be treated as if the value
     * were 1. This keyword is used to describe an extension and should not
     * appear in the primary key.RANGE: [1:] DEFAULT: 1
     */
    EXTLEVEL(SOURCE.RESERVED, HDU.EXTENSION, VALUE.INTEGER, "hierarchical level of the extension"),

    /**
     * The value field shall contain a character string, to be used to
     * distinguish among different extensions of the same type, i.e., with the
     * same value of XTENSION, in a FITS file. This keyword is used to describe
     * an extension and should not appear in the primary key.
     */
    EXTNAME(SOURCE.RESERVED, HDU.EXTENSION, VALUE.STRING, "name of the extension"),

    /**
     * The value field shall contain an integer, to be used to distinguish among
     * different extensions in a FITS file with the same type and name, i.e.,
     * the same values for XTENSION and EXTNAME. The values need not start with
     * 1 for the first extension with a particular value of EXTNAME and need not
     * be in sequence for subsequent values. If the EXTVER keyword is absent,
     * the file should be treated as if the value were 1. This keyword is used
     * to describe an extension and should not appear in the primary key.RANGE:
     * [1:] DEFAULT: 1
     */
    EXTVER(SOURCE.RESERVED, HDU.EXTENSION, VALUE.INTEGER, "version of the extension"),

    /**
     * The value field shall contain an integer that shall be used in any way
     * appropriate to define the data structure, consistent with Eq. 5.2 in the
     * FITS Standard. This keyword originated for use in FITS Random Groups
     * where it specifies the number of random groups present. In most other
     * cases this keyword will have the value 1.
     */
    GCOUNT(SOURCE.MANDATORY, HDU.EXTENSION, VALUE.INTEGER, "group count", //
            replaceable("randomgroupsdata:gcount", RandomGroupsData.class), //
            replaceable("asciitable:gcount", AsciiTable.class), //
            replaceable("basichdu:gcount", Object.class, "Required value"), //
            replaceable("binarytable:gcount", BinaryTable.class), //
            replaceable("imagedata:gcount", ImageData.class, "No extra parameters"), //
            replaceable("undefineddata:gcount", UndefinedData.class)

    ),

    /**
     * The value field shall contain the logical constant T. The value T
     * associated with this keyword implies that random groups records are
     * present.
     */
    GROUPS(SOURCE.MANDATORY, HDU.GROUPS, VALUE.LOGICAL, "indicates random groups structure", //
            replaceable("randomgroupsdata:groups", RandomGroupsData.class) //
    ),

    /**
     * This keyword shall have no associated value; columns 9-80 may contain any
     * ASCII text. The text should contain a history of steps and procedures
     * associated with the processing of the associated data. Any number of
     * HISTORY card images may appear in a key.
     */
    HISTORY(SOURCE.RESERVED, HDU.ANY, VALUE.NONE, "processing history of the data"),

    /**
     * The value field shall contain a character string identifying the
     * instrument used to acquire the data associated with the key.
     */
    INSTRUME(SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "name of instrument"),

    /**
     * The value field shall contain a non-negative integer no greater than 999,
     * representing the number of axes in the associated data array. A value of
     * zero signifies that no data follow the key in the HDU. In the context of
     * FITS 'TABLE' or 'BINTABLE' extensions, the value of NAXIS is always
     * 2.RANGE: [0:999]
     */
    NAXIS(SOURCE.MANDATORY, HDU.ANY, VALUE.INTEGER, "number of axes", //
            replaceable("header:naxis", Object.class) //
    ),

    /**
     * The value field of this indexed keyword shall contain a non-negative
     * integer, representing the number of elements along axis n of a data
     * array. The NAXISn must be present for all values n = 1,...,NAXIS, and for
     * no other values of n. A value of zero for any of the NAXISn signifies
     * that no data follow the key in the HDU. If NAXIS is equal to 0, there
     * should not be any NAXISn keywords.RANGE: [0:]
     */
    NAXISn(SOURCE.MANDATORY, HDU.ANY, VALUE.INTEGER, "size of the n'th axis", //
            replaceable("asciitable:naxis1", AsciiTable.class, "Size of row in bytes"), //
            replaceable("binarytable:naxis1", BinaryTable.class, "Bytes per row"), //
            replaceable("randomgroupsdata:naxis1", RandomGroupsData.class), //
            replaceable("randomgroupsdata:naxisN", RandomGroupsData.class), //
            replaceable("tablehdu:naxis2", TableData.class), //
            replaceable("header:naxisN", Object.class), //
            replaceable("undefineddata:naxis1", UndefinedData.class, "Number of Bytes") //
    ),

    /**
     * The value field shall contain a character string giving a name for the
     * object observed.
     */
    OBJECT(SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "name of observed object"),

    /**
     * The value field shall contain a character string identifying who acquired
     * the data associated with the key.
     */
    OBSERVER(SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "observer who acquired the data"),

    /**
     * The value field shall contain a character string identifying the
     * organization or institution responsible for creating the FITS file.
     */
    ORIGIN(SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "organization responsible for the data"),

    /**
     * The value field shall contain an integer that shall be used in any way
     * appropriate to define the data structure, consistent with Eq. 5.2 in the
     * FITS Standard. This keyword was originated for use with FITS Random
     * Groups and represented the number of parameters preceding each group. It
     * has since been used in 'BINTABLE' extensions to represent the size of the
     * data heap following the main data table. In most other cases its value
     * will be zero.
     */
    PCOUNT(SOURCE.MANDATORY, HDU.EXTENSION, VALUE.INTEGER, "parameter count", //
            replaceable("asciitable:pcount", AsciiTable.class, "No group data"), //
            replaceable("basichdu:pcount", Object.class, "Required value"), //
            replaceable("binarytablehdu:pcount", BinaryTable.class, "Includes heap"), //
            replaceable("binarytable:pcount", BinaryTable.class), //
            replaceable("imagedata:pcount", ImageData.class, "One group"), //
            replaceable("randomgroupsdata:pcount", RandomGroupsData.class), //
            replaceable("undefineddata:pcount", UndefinedData.class) //

    ),

    /**
     * This keyword is reserved for use within the FITS Random Groups structure.
     * This keyword shall be used, along with the PZEROn keyword, when the nth
     * FITS group parameter value is not the true physical value, to transform
     * the group parameter value to the true physical values it represents,
     * using the equation, physical_value = PZEROn + PSCALn *
     * group_parameter_value. The value field shall contain a floating point
     * number representing the coefficient of the linear term, the scaling
     * factor between true values and group parameter values at zero offset. The
     * default value for this keyword is 1.0.
     */
    PSCALn(SOURCE.RESERVED, HDU.GROUPS, VALUE.REAL, "parameter scaling factor"),

    /**
     * This keyword is reserved for use within the FITS Random Groups structure.
     * The value field shall contain a character string giving the name of
     * parameter n. If the PTYPEn keywords for more than one value of n have the
     * same associated name in the value field, then the data value for the
     * parameter of that name is to be obtained by adding the derived data
     * values of the corresponding parameters. This rule provides a mechanism by
     * which a random parameter may have more precision than the accompanying
     * data array elements; for example, by summing two 16-bit values with the
     * first scaled relative to the other such that the sum forms a number of up
     * to 32-bit precision.
     */
    PTYPEn(SOURCE.RESERVED, HDU.GROUPS, VALUE.STRING, "name of random groups parameter"),

    /**
     * This keyword is reserved for use within the FITS Random Groups structure.
     * This keyword shall be used, along with the PSCALn keyword, when the nth
     * FITS group parameter value is not the true physical value, to transform
     * the group parameter value to the physical value. The value field shall
     * contain a floating point number, representing the true value
     * corresponding to a group parameter value of zero. The default value for
     * this keyword is 0.0. The transformation equation is as follows:
     * physical_value = PZEROn + PSCALn * group_parameter_value.DEFAULT: 0.0
     */
    PZEROn(SOURCE.RESERVED, HDU.GROUPS, VALUE.REAL, "parameter scaling zero point"),

    /**
     * Coordinate reference frame of major/minor axes.If absent the default
     * value is 'FK5'.
     */
    RADESYS(SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "Coordinate reference frame of major/minor axes."),
    /**
     * Coordinate reference frame of major/minor axes. use RADESYS instead.
     * 
     * @deprecated use {@link #RADESYS} instead.
     */
    @Deprecated
    RADECSYS(SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "Coordinate reference frame of major/minor axes."),

    /**
     * The value field shall contain a character string citing a reference where
     * the data associated with the key are published.
     */
    REFERENC(SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "bibliographic reference"),

    /**
     * The SIMPLE keyword is required to be the first keyword in the primary key
     * of all FITS files. The value field shall contain a logical constant with
     * the value T if the file conforms to the standard. This keyword is
     * mandatory for the primary key and is not permitted in extension headers.
     * A value of F signifies that the file does not conform to this standard.
     */
    SIMPLE(SOURCE.MANDATORY, HDU.PRIMARY, VALUE.LOGICAL, "does file conform to the Standard?", //
            replaceable("header:simple", Object.class, "Java FITS: " + new java.util.Date()) //
    ),

    /**
     * The value field of this indexed keyword shall contain an integer
     * specifying the column in which field n starts in an ASCII TABLE
     * extension. The first column of a row is numbered 1.RANGE: [1:]
     */
    TBCOLn(SOURCE.MANDATORY, HDU.ASCII_TABLE, VALUE.INTEGER, "begining column number", //
            replaceable("asciitable:tbcolN", AsciiTable.class, "Column offset") //
    ),

    /**
     * The value field of this indexed keyword shall contain a character string
     * describing how to interpret the contents of field n as a multidimensional
     * array, providing the number of dimensions and the length along each axis.
     * The form of the value is not further specified by the Standard. A
     * proposed convention is described in Appendix B.2 of the FITS Standard in
     * which the value string has the format '(l,m,n...)' where l, m, n,... are
     * the dimensions of the array.
     */
    TDIMn(SOURCE.RESERVED, HDU.BINTABLE, VALUE.STRING, "dimensionality of the array ", //
            replaceable("binarytablehdu:tdimN", BinaryTable.class), //
            replaceable("headercard:tdimN", Object.class) //
    ),

    /**
     * The value field of this indexed keyword shall contain a character string
     * describing the format recommended for the display of the contents of
     * field n. If the table value has been scaled, the physical value shall be
     * displayed. All elements in a field shall be displayed with a single,
     * repeated format. For purposes of display, each byte of bit (type X) and
     * byte (type B) arrays is treated as an unsigned integer. Arrays of type A
     * may be terminated with a zero byte. Only the format codes in Table 8.6,
     * discussed in section 8.3.4 of the FITS Standard, are permitted for
     * encoding. The format codes must be specified in upper case. If the Bw.m,
     * Ow.m, and Zw.m formats are not readily available to the reader, the Iw.m
     * display format may be used instead, and if the ENw.d and ESw.d formats
     * are not available, Ew.d may be used. The meaning of this keyword is not
     * defined for fields of type P in the Standard but may be defined in
     * conventions using such fields.
     */
    TDISPn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "display format"),

    /**
     * The value field shall contain a character string identifying the
     * telescope used to acquire the data associated with the key.
     */
    TELESCOP(SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "name of telescope"),

    /**
     * The value field shall contain a non-negative integer representing the
     * number of fields in each row of a 'TABLE' or 'BINTABLE' extension. The
     * maximum permissible value is 999. RANGE: [0:999]
     */
    TFIELDS(SOURCE.MANDATORY, HDU.TABLE, VALUE.INTEGER, "number of columns in the table", //
            replaceable("asciitablehdu:tfields", AsciiTable.class), //
            replaceable("binarytable:tfields", BinaryTable.class), //
            replaceable("tablehdu:tfields", TableData.class, "Number of table fields"), //
            replaceable("asciitable:tfields", AsciiTable.class, "Number of fields in table") //
    ),
    /**
     * The value field of this indexed keyword shall contain a character string
     * describing the format in which field n is encoded in a 'TABLE' or
     * 'BINTABLE' extension.
     */
    TFORMn(SOURCE.MANDATORY, HDU.TABLE, VALUE.STRING, "column data format", //
            replaceable("asciitable:tformN", AsciiTable.class), //
            replaceable("binarytable:tformN", BinaryTable.class) //

    ),

    /**
     * The value field of this keyword shall contain an integer providing the
     * separation, in bytes, between the start of the main data table and the
     * start of a supplemental data area called the heap. The default value
     * shall be the product of the values of NAXIS1 and NAXIS2. This keyword
     * shall not be used if the value of PCOUNT is zero. A proposed application
     * of this keyword is presented in Appendix B.1 of the FITS Standard.
     */
    THEAP(SOURCE.RESERVED, HDU.BINTABLE, VALUE.INTEGER, "offset to starting data heap address", //
            replaceable("binarytablehdu:theap", BinaryTable.class) //
    ),

    /**
     * In ASCII 'TABLE' extensions, the value field for this indexed keyword
     * shall contain the character string that represents an undefined value for
     * field n. The string is implicitly blank filled to the width of the field.
     * In binary 'BINTABLE' table extensions, the value field for this indexed
     * keyword shall contain the integer that represents an undefined value for
     * field n of data type B, I, or J. The keyword may not be used in
     * 'BINTABLE' extensions if field n is of any other data type.
     */
    TNULLn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "value used to indicate undefined table element"),

    /**
     * This indexed keyword shall be used, along with the TZEROn keyword, when
     * the quantity in field n does not represent a true physical quantity. The
     * value field shall contain a floating point number representing the
     * coefficient of the linear term in the equation, physical_value = TZEROn +
     * TSCALn * field_value, which must be used to compute the true physical
     * value of the field, or, in the case of the complex data types C and M, of
     * the real part of the field with the imaginary part of the scaling factor
     * set to zero. The default value for this keyword is 1.0. This keyword may
     * not be used if the format of field n is A, L, or X.DEFAULT: 1.0
     */
    TSCALn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "linear data scaling factor"),

    /**
     * The value field for this indexed keyword shall contain a character
     * string, giving the name of field n. It is recommended that only letters,
     * digits, and underscore (hexadecimal code 5F, ('_') be used in the name.
     * String comparisons with the values of TTYPEn keywords should not be case
     * sensitive. The use of identical names for different fields should be
     * avoided.
     */
    TTYPEn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column name"),

    /**
     * The value field shall contain a character string describing the physical
     * units in which the quantity in field n, after any application of TSCALn
     * and TZEROn, is expressed. The units of all FITS key keyword values, with
     * the exception of measurements of angles, should conform with the
     * recommendations in the IAU Style Manual. For angular measurements given
     * as floating point values and specified with reserved keywords, degrees
     * are the recommended units (with the units, if specified, given as 'deg').
     */
    TUNITn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column units"),

    /**
     * This indexed keyword shall be used, along with the TSCALn keyword, when
     * the quantity in field n does not represent a true physical quantity. The
     * value field shall contain a floating point number representing the true
     * physical value corresponding to a value of zero in field n of the FITS
     * file, or, in the case of the complex data types C and M, in the real part
     * of the field, with the imaginary part set to zero. The default value for
     * this keyword is 0.0. This keyword may not be used if the format of field
     * n is A, L, or X.DEFAULT: 0.0
     */
    TZEROn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column scaling zero point"),

    /**
     * The value field shall contain a character string giving the name of the
     * extension type. This keyword is mandatory for an extension key and must
     * not appear in the primary key. For an extension that is not a standard
     * extension, the type name must not be the same as that of a standard
     * extension.
     */
    XTENSION(SOURCE.MANDATORY, HDU.EXTENSION, VALUE.STRING, "marks beginning of new HDU");

    public static final IFitsHeader NAXIS1 = NAXISn.n(1);

    public static final IFitsHeader NAXIS2 = NAXISn.n(2);

    private static final ThreadLocal<Class<?>> COMMENT_CONTEXT = new ThreadLocal<Class<?>>();

    /**
     * The value of the XTENSION keword in case of a binary table.
     */
    public static final String XTENSION_BINTABLE = "BINTABLE";

    /**
     * The value of the XTENSION keword in case of an image.
     */
    public static final String XTENSION_IMAGE = "IMAGE";

    private final StandardCommentReplacement[] commentReplacements;

    @SuppressWarnings("CPD-START")
    private final IFitsHeader key;

    Standard(SOURCE status, HDU hdu, VALUE valueType, String comment, StandardCommentReplacement... replacements) {
        this.key = new FitsHeaderImpl(name(), status, hdu, valueType, comment);
        this.commentReplacements = replacements;
    }

    Standard(String headerName, SOURCE status, HDU hdu, VALUE valueType, String comment, StandardCommentReplacement... replacements) {
        this.key = new FitsHeaderImpl(headerName == null ? name() : headerName, status, hdu, valueType, comment);
        this.commentReplacements = replacements;
    }

    @Override
    public String comment() {
        Class<?> contextClass = COMMENT_CONTEXT.get();
        if (contextClass == null) {
            contextClass = Object.class;
        }
        for (StandardCommentReplacement stdCommentReplacement : commentReplacements) {
            if (stdCommentReplacement.getContext().isAssignableFrom(contextClass)) {
                if (stdCommentReplacement.getComment() != null) {
                    return stdCommentReplacement.getComment();
                }
            }
        }
        return this.key.comment();
    }

    @Override
    public HDU hdu() {
        return this.key.hdu();
    }

    @Override
    public String key() {
        return this.key.key();
    }

    @Override
    public IFitsHeader n(int... number) {
        return this.key.n(number);
    }

    @Override
    public SOURCE status() {
        return this.key.status();
    }

    @Override
    @SuppressWarnings("CPD-END")
    public VALUE valueType() {
        return this.key.valueType();
    }

    public static void context(Class<?> clazz) {
        COMMENT_CONTEXT.set(clazz);
    }

    /**
     * scan for a comment with the specified reference key.
     * 
     * @param commentKey
     *            the reference key
     * @return the comment for the reference key
     */
    public String getCommentByKey(String commentKey) {
        for (StandardCommentReplacement commentReplacement : commentReplacements) {
            if (commentReplacement.getRef().equals(commentKey)) {
                String foundcommentReplacement = commentReplacement.getComment();
                if (foundcommentReplacement == null) {
                    return comment();
                } else {
                    return foundcommentReplacement;
                }
            }
        }
        return null;
    }

    /**
     * set the comment for the specified reference key.
     * 
     * @param commentKey
     *            the reference key
     * @param value
     *            the comment to set when the fits key is used.
     */
    public void setCommentByKey(String commentKey, String value) {
        for (StandardCommentReplacement commentReplacement : commentReplacements) {
            if (commentReplacement.getRef().equals(commentKey)) {
                commentReplacement.setComment(value);
                return;
            }
        }
    }

    private static StandardCommentReplacement replaceable(String string, Class<?> clazz) {
        return new StandardCommentReplacement(string, clazz);
    }

    private static StandardCommentReplacement replaceable(String string, Class<?> clazz, String comment) {
        return new StandardCommentReplacement(string, clazz, comment);
    }
}
