package nom.tam.fits.header;

/*-
 * #%L
 * nom.tam.fits
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

/**
 * Standard FITS keywords for defining world coordinate systems (WCS). Many (but not all) keywords listed here support
 * alternative coordinate systems. These have a lower case 'a' at the end of their enum names, e.g.
 * <code>WCSNAMEa</code>. The alternative coordinate system for these can be set via the {@link #alt(char)} method.
 * 
 * @author Attila Kovacs
 * 
 * @see    DateTime
 * 
 * @since  1.19
 */
public enum WCS implements IFitsHeader {

    /**
     * World coordinate system name
     * 
     * @since 1.19
     */
    WCSNAMEa(SOURCE.RESERVED, HDU.IMAGE, VALUE.STRING, "coordinate system name"),

    /**
     * Dimensionality of image coordinate system
     * 
     * @since 1.19
     */
    WCSAXESa(SOURCE.RESERVED, HDU.IMAGE, VALUE.INTEGER, "coordinate dimensions"),

    /**
     * Coordinate reference frame of major/minor axes.If absent the default value is 'FK5'.
     * 
     * @since 1.19
     */
    RADESYSa(SOURCE.RESERVED, HDU.IMAGE, VALUE.STRING, "celestial coordinate reference frame."),

    /**
     * Coordinate reference frame of major/minor axes (generic).
     *
     * @deprecated Deprecated in the current FITS standard, use {@link #RADESYSa} instead.
     */
    RADECSYS(SOURCE.RESERVED, HDU.ANY, VALUE.STRING, "celestial coordinate reference frame."),

    /**
     * [deg] The longitude of the celestial pole (for spherical coordinates).
     * 
     * @since 1.19
     */
    LONPOLEa(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "[deg] celestial pole longitude"),

    /**
     * [deg] The latitude of the celestial pole (for spherical coordinates).
     * 
     * @since 1.19
     */
    LATPOLEa(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "[deg] celestial pole latitude"),

    /**
     * The value field shall contain a floating point number giving the equinox in years for the celestial coordinate
     * system in which positions are expressed. Starting with Version 1, the Standard has deprecated the use of the
     * EPOCH keyword and thus it shall not be used in FITS files created after the adoption of the standard; rather, the
     * EQUINOX keyword shall be used.
     *
     * @deprecated Deprecated in the current FITS standard, use {@link #EQUINOXa} instead.
     */
    @Deprecated
    EPOCH(SOURCE.RESERVED, HDU.ANY, VALUE.REAL, "[yr] equinox of celestial coordinate system"),

    /**
     * The value field shall contain a floating point number giving the equinox in years for the celestial coordinate
     * system in which positions are expressed.
     * 
     * @since 1.19
     */
    EQUINOXa(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "[yr] equinox of celestial coordinate system"),

    /**
     * Coordinate axis name.
     * 
     * @since 1.19
     */
    CNAMEna(SOURCE.RESERVED, HDU.IMAGE, VALUE.STRING, "coordinate system display name"),

    /**
     * The value field shall contain a floating point number, identifying the location of a reference point along axis
     * n, in units of the axis index. This value is based upon a counter that runs from 1 to NAXISn with an increment of
     * 1 per pixel. The reference point value need not be that for the center of a pixel nor lie within the actual data
     * array. Use comments to indicate the location of the index point relative to the pixel.
     * 
     * @since 1.19
     */
    CRPIXna(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "coordinate axis reference pixel"),

    /**
     * The value field shall contain a floating point number, giving the value of the coordinate specified by the CTYPEn
     * keyword at the reference point CRPIXn. Units must follow the prescriptions of section 5.3 of the FITS Standard.
     * 
     * @since 1.19
     */
    CRVALna(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "coordinate axis value at reference pixel"),

    /**
     * The value field shall contain a character string, giving the name of the coordinate represented by axis n.
     * 
     * @since 1.19
     */
    CTYPEna(SOURCE.RESERVED, HDU.IMAGE, VALUE.STRING, "name of the coordinate axis"),

    /**
     * The value field shall contain a floating point number giving the partial derivative of the coordinate specified
     * by the CTYPEn keywords with respect to the pixel index, evaluated at the reference point CRPIXn, in units of the
     * coordinate specified by the CTYPEn keyword. These units must follow the prescriptions of section 5.3 of the FITS
     * Standard.
     * 
     * @since 1.19
     */
    CDELTna(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "coordinate spacing along axis"),

    /**
     * Random coordinate error on axis <i>n</i> in the physical coordinate unit (if defined).
     * 
     * @since 1.19
     */
    CRDERna(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "random error in coordinate"),

    /**
     * Systematic coordinate error on axis <i>n</i> in the physical coordinate unit (if defined).
     * 
     * @since 1.19
     */
    CSYERna(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "systematic error in coordinate"),

    /**
     * Phase axis zero point
     * 
     * @since 1.19
     */
    CZPHSna(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "phase axis zero point"),

    /**
     * Phase axis period
     */
    CPERIna(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "phase axis period"),

    /**
     * [Hz] Rest frequency of observed spectral line.
     * 
     * @since 1.19
     */
    RESTFRQa(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "[Hz] line rest frequency"),

    /**
     * [Hz] Rest frequeny of observed spectral line (generic).
     *
     * @deprecated Deprecated in the current FITS standard, use {@link #RESTFRQa} instead.
     */
    RESTFREQ(SOURCE.RESERVED, HDU.ANY, VALUE.REAL, "[Hz] observed line rest frequency"),

    /**
     * [m] Rest wavelength of observed spectral line in image.
     * 
     * @since 1.19
     */
    RESTWAVa(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "[m] line rest wavelength"),

    /**
     * Image spectral reference system name.
     * 
     * @since 1.19
     */
    SPECSYSa(SOURCE.RESERVED, HDU.IMAGE, VALUE.STRING, "spectral reference frame"),

    /**
     * Image spectral reference system name of observer.
     * 
     * @since 1.19
     */
    SSYSOBSa(SOURCE.RESERVED, HDU.IMAGE, VALUE.STRING, "spectral reference frame of observer"),

    /**
     * Spectral reference system name of source.
     * 
     * @since 1.19
     */
    SSYSSRCa(SOURCE.RESERVED, HDU.IMAGE, VALUE.STRING, "spectral reference frame of source"),

    /**
     * [m/s] Radial velocity of source in the spectral reference frame.
     * 
     * @since 1.19
     */
    VELOSYSa(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "[m/s] source radial velocity"),

    /**
     * Redshift value of source in the spectral reference frame.
     * 
     * @since 1.19
     */
    ZSOURCEa(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "source redshift value"),

    /**
     * [deg] True velocity angle of source
     * 
     * @since 1.19
     */
    VELANGLa(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, "[deg] true velocity angle"),

    /**
     * [m] Geodetic location of observer (<i>x</i> coordinate).
     * 
     * @since 1.19
     */
    OBSGEO_X("OBSGEO-X", SOURCE.RESERVED, HDU.ANY, VALUE.REAL, "[m] geodetic location x of observer"),

    /**
     * [m] Geodetic location of observer (<i>y</i> coordinate).
     * 
     * @since 1.19
     */
    OBSGEO_Y("OBSGEO-Y", SOURCE.RESERVED, HDU.ANY, VALUE.REAL, "[m] geodetic location y of observer"),

    /**
     * [m] Geodetic location of observer (<i>z</i> coordinate).
     * 
     * @since 1.19
     */
    OBSGEO_Z("OBSGEO-Z", SOURCE.RESERVED, HDU.ANY, VALUE.REAL, "[m] geodetic location z of observer"),

    /**
     * WCS name for the array entries in the given column index.
     * 
     * @since 1.19
     */
    WCSNna(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column coordinate syste name"),

    /**
     * [deg] The longitude of the celestial pole for the entries in the given column index (for spherical coordinates).
     * 
     * @since 1.19
     */
    LONPna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "[deg] column celestial pole longitude"),

    /**
     * [deg] The latitude of the celestial pole for the entries in the given column index (for spherical coordinates).
     * 
     * @since 1.19
     */
    LATPna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "[deg] column celestial pole latitude"),

    /**
     * [yr] Coordinate epoch for which the celestial coorinate system is defined for the given column index.
     * 
     * @since 1.19
     */
    EQUIna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "[yr] column coordinate epoch"),

    /**
     * The equatorial coordinate frame used for the given column index, e.g. 'FK4', 'FK5', or 'ICRS'.
     * 
     * @since 1.19
     */
    RADEna(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column equatorial coordinate frame"),

    /**
     * [Hz] The rest frequency of the line in the given column index.
     * 
     * @since 1.19
     */
    RFRQna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "[Hz] line rest frequency in column"),

    /**
     * [Hz] The rest wavelength of the line in the given column index.
     * 
     * @since 1.19
     */
    RWAVna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "[m] line rest wavelength in column"),

    /**
     * Spectral reference frame for the given column index.
     * 
     * @since 1.19
     */
    SPECna(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column spectral reference frame"),

    /**
     * Spectral reference system of observer for the given column index.
     * 
     * @since 1.19
     */
    SOBSna(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column observer spectral frame"),

    /**
     * Spectral reference system of source for the given column index.
     * 
     * @since 1.19
     */
    SSRCna(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column source spectral frame"),

    /**
     * [m/s] Source radial velocity for the given column index.
     * 
     * @since 1.19
     */
    VSYSna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "[m/s] column radial velocity"),

    /**
     * [deg] Angle of true velocity for the given column index.
     * 
     * @since 1.19
     */
    VANGna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "[deg] angle of velocity in column"),

    /**
     * Source redshift value for the given column index.
     * 
     * @since 1.19
     */
    ZSOUna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column redshift value"),

    /**
     * [m] Geodetic location (<i>x</i> coordinate) of observer for he given column index.
     * 
     * @since 1.19
     */
    OBSGXn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "[m] column geodetic location x"),

    /**
     * [m] Geodetic location (<i>y</i> coordinate) of observer for he given column index.
     * 
     * @since 1.19
     */
    OBSGYn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "[m] column geodetic location y"),

    /**
     * [m] Geodetic location (<i>z</i> coordinate) of observer for he given column index.
     * 
     * @since 1.19
     */
    OBSGZn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "[m] column geodetic location z"),

    /**
     * The coordinate axis type for array entries in this column (trailing index). The number of coordinate axes
     * (leading index) defined this way should match the dimensionality of the array elements in the column. This
     * version does not support alternative coordinates systems.
     * 
     * @see   #nCTYna
     * 
     * @since 1.19
     */
    nCTYPn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column coordinate axis type"),

    /**
     * The coordinate axis type for array entries in this column (trailing index). The number of coordinate axes
     * (leading index) defined this way should match the dimensionality of the array elements in the column. This
     * version supports alternative coordinates systems.
     * 
     * @see   #nCTYPn
     * 
     * @since 1.19
     */
    nCTYna(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column coordinate axis type"),

    /**
     * The coordinate axis name for array entries in this column (trailing index). The number of coordinate axes
     * (leading index) defined this way should match the dimensionality of the array elements in the column.
     * 
     * @since 1.19
     */
    nCNAna(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column coordinate axis type"),

    /**
     * The physical coordinate unit for array entries in this column (trailing index). The number of coordinate axes
     * (leading index) defined this way should match the dimensionality of the array elements in the column. This
     * version does not support alternative coordinates systems.
     * 
     * @see   #nCUNna
     * 
     * @since 1.19
     */
    nCUNIn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column coordinate axis unit"),

    /**
     * The physical coordinate unit for array entries in this column (trailing index). The number of coordinate axes
     * (leading index) defined this way should match the dimensionality of the array elements in the column. This
     * version supports alternative coordinates systems.
     * 
     * @see   #nCUNIn
     * 
     * @since 1.19
     */
    nCUNna(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column coordinate axis unit"),

    /**
     * The coordinate reference value in the physical unit of the axis (if defined) for array entries in this column
     * (trailing index). The number of coordinate axes (leading index) defined this way should match the dimensionality
     * of the array elements in the column. This version does not support alternative coordinates systems.
     * 
     * @see   #nCRVna
     * 
     * @since 1.19
     */
    nCRVLn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column coordinate axis reference value"),

    /**
     * The coordinate reference value in the physical unit of the axis (if defined) for array entries in this column
     * (trailing index). The number of coordinate axes (leading index) defined this way should match the dimensionality
     * of the array elements in the column. This version supports alternative coordinates systems.
     * 
     * @see   #nCRVLn
     * 
     * @since 1.19
     */
    nCRVna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column coordinate axis reference value"),

    /**
     * The coordinate axis random error in the physical unit of the axis (if defined) for array entries in this column
     * (trailing index). The number of coordinate axes (leading index) defined this way should match the dimensionality
     * of the array elements in the column.
     * 
     * @since 1.19
     */
    nCRDna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column coordinate axis random error"),

    /**
     * The coordinate axis systematic error in the physical unit of the axis (if defined) for array entries in this
     * column (trailing index). The number of coordinate axes (leading index) defined this way should match the
     * dimensionality of the array elements in the column.
     * 
     * @since 1.19
     */
    nCSYna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column coordinate axis systematic error"),

    /**
     * Phase axis zero point on axis (leading index) for array entries in this column (trailing index)
     * 
     * @since 1.19
     */
    nCZPna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "phase axis zero point"),

    /**
     * Phase axis period on axis (leading index) for array entries in this column (trailing index)
     * 
     * @since 1.19
     */
    nCPRna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "phase axis period"),

    /**
     * The coordinate axis spacing in the physical unit of the axis (if defined) for array entries in this column
     * (trailing index). The number of coordinate axes (leading index) defined this way should match the dimensionality
     * of the array elements in the column. This version does not support alternative coordinates systems.
     * 
     * @see   #nCDEna
     * 
     * @since 1.19
     */
    nCDLTn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column coordinate axis spacing"),

    /**
     * The coordinate axis spacing in the physical unit of the axis (if defined) for array entries in this column
     * (trailing index). The number of coordinate axes (leading index) defined this way should match the dimensionality
     * of the array elements in the column. This version supports alternative coordinates systems.
     * 
     * @see   #nCDLTn
     * 
     * @since 1.19
     */
    nCDEna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column coordinate axis spacing"),

    /**
     * The 1-based coordinate reference pixel index in the physical unit of the axis (if defined) for array entries in
     * this column (trailing index). The number of coordinate axes (leading index) defined this way should match the
     * dimensionality of the array elements in the column. This version does not support alternative coordinates
     * systems.
     * 
     * @see   #nCRPna
     * 
     * @since 1.19
     */
    nCRPXn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column coordinate axis reference pixel"),

    /**
     * The 1-based coordinate reference pixel index in the physical unit of the axis (if defined) for array entries in
     * this column (trailing index). The number of coordinate axes (leading index) defined this way should match the
     * dimensionality of the array elements in the column. This version supports alternative coordinates systems.
     * 
     * @see   #nCRPXn
     * 
     * @since 1.19
     */
    nCRPna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column coordinate axis reference pixel"),

    /**
     * @deprecated The FITS standard deprecated this keyword. Use {@link WCS#nnPCna} and {@link WCS#nnCDna}instead.
     *                 [deg] The coordinate axis rotation in the physical unit of the axis (if defined) for array
     *                 entries in this column (trailing index). The number of coordinate axes (leading index) defined
     *                 this way should match the dimensionality of the array elements in the column.
     * 
     * @since      1.19
     */
    nCROTn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "[deg] column coordinate axis rotation"),

    /**
     * Coordinate transformation matrix in the PC convention. from rectilinear coordinate index <i>i</i> (leading index)
     * to coordinate index <i>j</i> (second index) for the for array entries in this column (trailing index). The number
     * of coordinate axes (leading index) defined this way should match the dimensionality of the array elements in the
     * column.
     * 
     * @since 1.19
     */
    nnPCna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column coord. trans. matrix element"),

    /**
     * Coordinate transformation matrix in the CD convention. from rectilinear coordinate index <i>i</i> (leading index)
     * to coordinate index <i>j</i> (second index) for the for array entries in this column (trailing index). The number
     * of coordinate axes (leading index) defined this way should match the dimensionality of the array elements in the
     * column.
     * 
     * @since 1.19
     */
    nnCDna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column coord. trans. matrix element"),

    /**
     * The coordinate string parameter <i>m</i> (trailing index) for axis <i>i</i> (leading index) for array entries in
     * this column (middle index). The number of coordinate axes (leading index) defined this way should match the
     * dimensionality of the array elements in the column. The shorter {@link #nSn_na} form may be required for column
     * indices &gt;99 with alternate coordinate systems.
     * 
     * @since 1.19
     */
    nPSn_na(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column axis parameter name"),

    /**
     * The coordinate string parameter <i>m</i> (trailing index) for axis <i>i</i> (leading index) for array entries in
     * this column (middle index). The number of coordinate axes (leading index) defined this way should match the
     * dimensionality of the array elements in the column. Same as {@link #nPSn_na}. This shorter form may be required
     * for column indices &gt;99 with alternate coordinate systems.
     * 
     * @since 1.19
     */
    nSn_na(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column axis parameter name"),

    /**
     * The coordinate parameter value <i>m</i> (trailing index) for axis <i>i</i> (leading index) for array entries in
     * this column (middle index). The number of coordinateaxes defined this way should match the dimensionality of the
     * array elements in the column. The shorter {@link #nVn_na} form may be required for column indices &gt;99 with
     * alternate coordinate systems.
     * 
     * @since 1.19
     */
    nPVn_na(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column axis parameter value"),

    /**
     * The coordinate parameter value <i>m</i> (trailing index) for axis <i>i</i> (leading index) for array entries in
     * this column (middle index). The number of coordinateaxes defined this way should match the dimensionality of the
     * array elements in the column. Same as {@link #nPVn_na}. This shorter form may be required for column indices
     * &gt;99 with alternate coordinate systems.
     * 
     * @since 1.19
     */
    nVn_na(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column axis parameter value"),

    /**
     * The coordinate parameter array for axis <i>i</i> (leading index) in this column (middle index). The number of
     * coordinate axes defined this way should match the dimensionality of the array elements in the column.
     * 
     * @since 1.19
     */
    nVn_Xa(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column parameter array"),

    /**
     * WCS name for the pixe list entries in the given column index. Same as {@link #TWCSna}. This shorter form may be
     * required for column indices &gt;99 with alternate coordinate systems.
     * 
     * @since 1.19
     */
    WCSna(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column WCS name"),

    /**
     * WCS name for the pixe list entries in the given column index. The shorter form {@link #WCSna} may be required for
     * column indices &gt;99 with alternate coordinate systems.
     * 
     * @since 1.19
     */
    TWCSna(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column WCS name"),

    /**
     * WCS dimensions for given column index.
     * 
     * @since 1.19
     */
    WCAXna(SOURCE.RESERVED, HDU.TABLE, VALUE.INTEGER, "column coordinate dimensions"),

    /**
     * The coordinate axis type for (1D) pixel lists in this column (trailing index). This version does not support
     * alternative coordinates systems.
     * 
     * @see   #TCTYna
     * 
     * @since 1.19
     */
    TCTYPn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column pixel axis type"),

    /**
     * The coordinate axis type for (1D) pixel lists in this column (trailing index). This version supports alternative
     * coordinates systems.
     * 
     * @see   #TCTYPn
     * 
     * @since 1.19
     */
    TCTYna(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column pixel axis type"),

    /**
     * The coordinate axis name for (1D) pixel lists in this column (trailing index).
     * 
     * @since 1.19
     */
    TCNAna(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column pixel axis name"),

    /**
     * The physical coordinate unit for (1D) pixel lists in this column (trailing index). This version does not support
     * alternative coordinates systems.
     * 
     * @see   #TCUNna
     * 
     * @since 1.19
     */
    TCUNIn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column pixel axis unit"),

    /**
     * The physical coordinate unit for (1D) pixel lists in this column (trailing index). This version supports
     * alternative coordinates systems.
     * 
     * @see   #TCUNIn
     * 
     * @since 1.19
     */
    TCUNna(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column pixel axis unit"),

    /**
     * The coordinate reference value in the physical unit of the axis (if defined) for the (1D) pixel lists in this
     * column (trailing index). This version does not support alternative coordinates systems.
     * 
     * @see   #TCRVna
     * 
     * @since 1.19
     */
    TCRVLn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column pixel axis reference value"),

    /**
     * The coordinate reference value in the physical unit of the axis (if defined) for the (1D) pixel lists in this
     * column (trailing index). This version supports alternative coordinates systems.
     * 
     * @see   #TCRVLn
     * 
     * @since 1.19
     */
    TCRVna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column pixel axis reference value"),

    /**
     * The coordinate axis spacing in the physical unit of the axis (if defined) for the (1D_) pixel lists in this
     * column (trailing index). This version does not support alternative coordinates systems.
     * 
     * @see   #TCDEna
     * 
     * @since 1.19
     */
    TCDLTn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column pixel axis spacing"),

    /**
     * The coordinate axis spacing in the physical unit of the axis (if defined) for the (1D_) pixel lists in this
     * column (trailing index). This version supports alternative coordinates systems.
     * 
     * @see   #TCDLTn
     * 
     * @since 1.19
     */
    TCDEna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column pixel axis spacing"),

    /**
     * The coordinate axis random error in the physical unit of the axis (if defined) for the (1D_) pixel lists in this
     * column (trailing index).
     * 
     * @since 1.19
     */
    TCRDna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column pixel axis random error"),

    /**
     * The coordinate axis systematics error in the physical unit of the axis (if defined) for the (1D_) pixel lists in
     * this column (trailing index).
     * 
     * @since 1.19
     */
    TCSYna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column pixel axis random error"),

    /**
     * Phase axis zero point on axis (leading index) for array entries in this column (trailing index)
     * 
     * @since 1.19
     */
    TCZPna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "phase axis zero point"),

    /**
     * Phase axis period on axis (leading index) for array entries in this column (trailing index)
     * 
     * @since 1.19
     */
    TCPRna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "phase axis period"),

    /**
     * The 1-based coordinate reference pixel index in the physical unit of the axis (if defined) for the (1D) pixel
     * lists in this column (trailing index). This version does not support alternative coordinates systems.
     * 
     * @see   #TCRPna
     * 
     * @since 1.19
     */
    TCRPXn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column pixel axis reference pixel"),

    /**
     * The 1-based coordinate reference pixel index in the physical unit of the axis (if defined) for the (1D) pixel
     * lists in this column (trailing index). This version supports alternative coordinates systems.
     * 
     * @see   #TCRPXn
     * 
     * @since 1.19
     */
    TCRPna(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column pixel axis reference pixel"),

    /**
     * Coordinate transformation matrix in the PC convention. from column index <i>n</i> (leading index) to column index
     * <i>k</i> (second index) for the for the (1D) pixel lists in this column. The shorter form {@link #TPn_na} may be
     * required for column indices &gt;99 with alternate coordinate systems.
     * 
     * @see   #TPn_na
     * 
     * @since 1.19
     */
    TPCn_na(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column pix trans. matrix element"),

    /**
     * Coordinate transformation matrix in the PC convention. from column index <i>n</i> (leading index) to column index
     * <i>k</i> (second index) for the for the (1D) pixel lists in this column. Same as {@link #TPCn_na}. This shorter
     * form may be required for column indices &gt;99 with alternate coordinate systems.
     * 
     * @see   #TPCn_na
     * 
     * @since 1.19
     */
    TPn_na(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column pix trans. matrix element"),

    /**
     * @deprecated The FITS standard deprecated this keyword. Use {@link WCS#TPCn_na} and {@link WCS#TCDn_na}instead.
     *                 [deg] The coordinate axis rotation in the physical unit of the axis (if defined) for the (1D)
     *                 pixel lists in this column (trailing index).
     * 
     * @since      1.19
     */
    TCROTn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "[deg] column pixel axis rotation"),

    /**
     * Coordinate transformation matrix in the CD convention. from column index <i>n</i> (leading index) to column index
     * <i>k</i> (second index) for the for the (1D) pixel lists in this column. The shorter form {@link #TCn_na} may be
     * required for column indices &gt;99 with alternate coordinate systems.
     * 
     * @since 1.19
     */
    TCDn_na(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column pix trans. matrix element"),

    /**
     * Coordinate transformation matrix in the CD convention. from column index <i>n</i> (leading index) to column index
     * <i>k</i> (second index) for the for the (1D) pixel lists in this column. Same as {@link #TCDn_na}. This shorter
     * form may be required for column indices &gt;99 with alternate coordinate systems.
     * 
     * @since 1.19
     */
    TCn_na(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column pix trans. matrix element"),

    /**
     * The coordinate string parameter <i>m</i> (trailing index) for the (1D) pixel list entries in this column (leading
     * index). This shorter form {@link #TSn_na} may be required for column indices &gt;99 with alternate coordinate
     * systems.
     * 
     * @since 1.19
     */
    TPSn_na(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column axis parameter name"),

    /**
     * The coordinate string parameter <i>m</i> (trailing index) for the (1D) pixel list entries in this column (leading
     * index). Same as {@link #TPSn_na}. This shorter form may be required for column indices &gt;99 with alternate
     * coordinate systems.
     * 
     * @since 1.19
     */
    TSn_na(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, "column axis parameter name"),

    /**
     * The coordinate parameter value <i>m</i> (trailing index) for the (1D) pixel list entries in this column (leading
     * index). The shorter form {@link #TVn_na} may be required for column indices &gt;99 with alternate coordinate
     * systems.
     * 
     * @since 1.19
     */
    TPVn_na(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column pixel axis parameter value"),

    /**
     * The coordinate parameter value <i>m</i> (trailing index) for the (1D) pixel list entries in this column (leading
     * index). Same as {@link #TPVn_na}. This shorter form may be required for column indices &gt;99 with alternate
     * coordinate systems.
     * 
     * @since 1.19
     */
    TVn_na(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, "column pixel axis parameter value");

    /** ICRS coordinate reference frame */
    public static final int WCSAXES_MAX_VALUE = 9;

    /** ICRS coordinate reference frame */
    public static final String RADESYS_ICRS = "ICRS";

    /** IAU 1984 FK5 coordinate reference frame */
    public static final String RADESYS_FK5 = "FK5";

    /** Bessel-Newcomb FK4 coordinate reference frame */
    public static final String RADESYS_FK4 = "FK4";

    /** Bessel-Newcomb FK4 coordinate reference frame, without eccentricity terms */
    public static final String RADESYS_FK4_NO_E = "FK4-NO-E";

    /** Geocentric apparent place (IAU 1984) */
    public static final String RADESYS_GAPPT = "GAPPT";

    /** Logarithmically sampled algorithm code for {@link #CTYPEna} keywords */
    public static final String ALGO_LOG = "LOG";

    /** Detector sampling algorithm code for {@link #CTYPEna} keywords */
    public static final String ALGO_GRI = "GRI";

    /** Detector sampling algorithm code for {@link #CTYPEna} keywords */
    public static final String ALGO_GRA = "GRA";

    /** Irregular sampling algorithm code for {@link #CTYPEna} keywords */
    public static final String ALGO_TAB = "TAB";

    /** Spectral frequency coordinate value for {@link #CTYPEna} keywords */
    public static final String SPECTRAL_TYPE_FREQ = "FREQ";

    /** Spectral energy coordinate value for {@link #CTYPEna} keywords */
    public static final String SPECTRAL_TYPE_ENER = "ENER";

    /** Spectral wavenumber coordinate value for {@link #CTYPEna} keywords */
    public static final String SPECTRAL_TYPE_WAVN = "WAVN";

    /** Spectral radial velocity coordinate value for {@link #CTYPEna} keywords */
    public static final String SPECTRAL_TYPE_VRAD = "VRAD";

    /** Spectral vacuum wavenlength coordinate value for {@link #CTYPEna} keywords */
    public static final String SPECTRAL_TYPE_WAVE = "WAVE";

    /** Spectral optical velocity coordinate value for {@link #CTYPEna} keywords */
    public static final String SPECTRAL_TYPE_VOPT = "VOPT";

    /** Spectral redshift coordinate value for {@link #CTYPEna} keywords */
    public static final String SPECTRAL_TYPE_ZOPT = "ZOPT";

    /** Spectral wavelength in air coordinate value for {@link #CTYPEna} keywords */
    public static final String SPECTRAL_TYPE_AWAV = "AWAV";

    /** Spectral apparent radial velocity coordinate value for {@link #CTYPEna} keywords */
    public static final String SPECTRAL_TYPE_VELO = "VELO";

    /** Spectral beta factor (<i>v/c</i>) coordinate value for {@link #CTYPEna} keywords */
    public static final String SPECTRAL_TYPE_BETA = "BETA";

    /** Spectral frequency expressed as wavelength transformation code for {@link #CTYPEna} keywords */
    public static final String SPECTRAL_ALGO_F2W = "F2W";

    /** Spectral frequency expressed as apparent radial velocity transformation code for {@link #CTYPEna} keywords */
    public static final String SPECTRAL_ALGO_F2V = "F2V";

    /** Spectral frequency expressed as air wavelength transformation code for {@link #CTYPEna} keywords */
    public static final String SPECTRAL_ALGO_F2A = "F2A";

    /** Spectral wavelength expressed as frequency transformation code for {@link #CTYPEna} keywords */
    public static final String SPECTRAL_ALGO_W2F = "W2F";

    /** Spectral wavelength expressed as apparent radial velocity transformation code for {@link #CTYPEna} keywords */
    public static final String SPECTRAL_ALGO_W2V = "W2V";

    /** Spectral wavelength expressed as air wavelength transformation code for {@link #CTYPEna} keywords */
    public static final String SPECTRAL_ALGO_W2A = "W2A";

    /** Spectral radial velocity expressed as frequency transformation code for {@link #CTYPEna} keywords */
    public static final String SPECTRAL_ALGO_V2F = "V2F";

    /** Spectral radial velocity expressed as wavelength transformation code for {@link #CTYPEna} keywords */
    public static final String SPECTRAL_ALGO_V2W = "V2W";

    /** Spectral radial velocity expressed as air wavelength transformation code for {@link #CTYPEna} keywords */
    public static final String SPECTRAL_ALGO_V2A = "V2A";

    /** Spectral air wavelength expressed as frequency transformation code for {@link #CTYPEna} keywords */
    public static final String SPECTRAL_ALGO_A2F = "A2F";

    /** Spectral air wavelength expressed as vacuum wavelength transformation code for {@link #CTYPEna} keywords */
    public static final String SPECTRAL_ALGO_A2W = "A2W";

    /**
     * Spectral air wavelength expressed as apparent radial velocity transformation code for {@link #CTYPEna} keywords
     */
    public static final String SPECTRAL_ALGO_A2V = "A2V";

    private final FitsKey key;

    WCS(SOURCE status, HDU hdu, VALUE valueType, String comment) {
        this(null, status, hdu, valueType, comment);
    }

    WCS(String headerName, SOURCE status, HDU hdu, VALUE valueType, String comment) {
        key = new FitsKey(headerName == null ? name() : headerName, status, hdu, valueType, comment);
        FitsKey.registerStandard(this);
    }

    @Override
    public final FitsKey impl() {
        return key;
    }

    /**
     * Specifying an alternative coordinate system. Alternative systems are labelled 'A' through 'Z'. This call is
     * available only for the enums, which have a lower-case 'a' at the end of their Java names (such as
     * {@link #WCSNAMEa}). Attempting to call this on WCS keywords that do not end with lower-case 'a' in their Java
     * names (such as {@link #OBSGEO_X} will throw and {@link UnsupportedOperationException}. You will want to call this
     * before chaining other calls to {@link IFitsHeader}.
     * 
     * @param  c                             The alternative coordinate system marker 'A' through 'Z' (case
     *                                           insensitive).
     * 
     * @return                               The standard FITS keyword with the alternate coordinate system marker
     *                                           attached.
     * 
     * @throws IllegalArgumentException      if the marker is outside of the legal range of 'A' through 'Z' (case
     *                                           insensitive).
     * @throws UnsupportedOperationException if the keyword does not support alternative coordinate systems
     * 
     * @since                                1.19
     */
    public IFitsHeader alt(char c) throws IllegalArgumentException, UnsupportedOperationException {
        if (!name().endsWith("a")) {
            throw new UnsupportedOperationException("WCS keyword " + key.key() + " does not support alternatives.");
        }

        c = Character.toUpperCase(c);
        if (c < 'A' || c > 'Z') {
            throw new IllegalArgumentException("Expected 'A' through 'Z': Got '%c'");
        }

        return new FitsKey(key() + Character.toUpperCase(c), status(), hdu(), valueType(), comment());
    }

}
