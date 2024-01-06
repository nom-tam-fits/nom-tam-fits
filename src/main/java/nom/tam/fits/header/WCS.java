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

import nom.tam.fits.HeaderCard;

/**
 * Standard FITS keywords for defining world coordinate systems (WCS). Many (but not all) keywords listed here support
 * alternative coordinate systems, which can be set via the {@link #alt(char)} method.
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
    WCSNAME(SOURCE.RESERVED, HDU.IMAGE, VALUE.STRING, true, "Coordinate system name"),

    /**
     * Dimensionality of image coordinate system
     * 
     * @since 1.19
     */
    WCSAXES(SOURCE.RESERVED, HDU.IMAGE, VALUE.INTEGER, true, "Coordinate system dimensions"),

    /**
     * Coordinate reference frame of major/minor axes.If absent the default value is 'FK5'.
     */
    RADESYS(SOURCE.RESERVED, HDU.IMAGE, VALUE.STRING, true, "Coordinate reference frame of major/minor axes."),

    /**
     * Coordinate reference frame of major/minor axes (generic).
     *
     * @deprecated Deprecated in the current FITS standard, use {@link #RADESYS} instead.
     */
    RADECSYS(SOURCE.RESERVED, HDU.ANY, VALUE.STRING, false, "Coordinate reference frame of major/minor axes."),

    /**
     * [deg] The longitude of the celestial pole (for spherical coordinates).
     * 
     * @since 1.19
     */
    LONPOLE(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, true, "[deg] Celestial pole longitude"),

    /**
     * [deg] The latitude of the celestial pole (for spherical coordinates).
     * 
     * @since 1.19
     */
    LATPOLE(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, true, "[deg] Celestial pole latitude"),

    /**
     * The value field shall contain a floating point number giving the equinox in years for the celestial coordinate
     * system in which positions are expressed. Starting with Version 1, the Standard has deprecated the use of the
     * EPOCH keyword and thus it shall not be used in FITS files created after the adoption of the standard; rather, the
     * EQUINOX keyword shall be used.
     *
     * @deprecated Deprecated in the current FITS standard, use {@link #EQUINOX} instead.
     */
    @Deprecated
    EPOCH(SOURCE.RESERVED, HDU.ANY, VALUE.REAL, false, "equinox of celestial coordinate system"),

    /**
     * The value field shall contain a floating point number giving the equinox in years for the celestial coordinate
     * system in which positions are expressed.
     * 
     * @since 1.19
     */
    EQUINOX(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, true, "equinox of celestial coordinate system"),

    /**
     * Coordinate axis name.
     * 
     * @since 1.19
     */
    CNAMEn(SOURCE.RESERVED, HDU.IMAGE, VALUE.STRING, true, "coordinate system display name"),

    /**
     * The value field shall contain a floating point number, identifying the location of a reference point along axis
     * n, in units of the axis index. This value is based upon a counter that runs from 1 to NAXISn with an increment of
     * 1 per pixel. The reference point value need not be that for the center of a pixel nor lie within the actual data
     * array. Use comments to indicate the location of the index point relative to the pixel.
     * 
     * @since 1.19
     */
    CRPIXn(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, true, "coordinate system reference pixel"),

    /**
     * The value field shall contain a floating point number, giving the value of the coordinate specified by the CTYPEn
     * keyword at the reference point CRPIXn. Units must follow the prescriptions of section 5.3 of the FITS Standard.
     * 
     * @since 1.19
     */
    CRVALn(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, true, "coordinate system value at reference pixel"),

    /**
     * The value field shall contain a character string, giving the name of the coordinate represented by axis n.
     * 
     * @since 1.19
     */
    CTYPEn(SOURCE.RESERVED, HDU.IMAGE, VALUE.STRING, true, "name of the coordinate axis"),

    /**
     * The value field shall contain a floating point number giving the partial derivative of the coordinate specified
     * by the CTYPEn keywords with respect to the pixel index, evaluated at the reference point CRPIXn, in units of the
     * coordinate specified by the CTYPEn keyword. These units must follow the prescriptions of section 5.3 of the FITS
     * Standard.
     * 
     * @since 1.19
     */
    CDELTn(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, true, "coordinate increment along axis"),

    /**
     * Random coordinate error on axis <i>n</i> in the physical coordinate unit (if defined).
     * 
     * @since 1.19
     */
    CRDERn(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, true, "random error in coordinate"),

    /**
     * Systematic coordinate error on axis <i>n</i> in the physical coordinate unit (if defined).
     * 
     * @since 1.19
     */
    CSYERn(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, true, "systematic error in coordinate"),

    /**
     * Phase axis zero point
     * 
     * @since 1.19
     */
    CZPHSn(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, true, "phase axis zero point"),

    /**
     * Phase axis period
     */
    CPERIn(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, true, "phase axis period"),

    /**
     * [Hz] Rest frequency of observed spectral line.
     * 
     * @since 1.19
     */
    RESTFRQ(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, true, "[Hz] Line rest frequency"),

    /**
     * [Hz] Rest frequeny of observed spectral line (generic).
     *
     * @deprecated Deprecated in the current FITS standard, use {@link #RESTFRQ} instead.
     */
    RESTFREQ(SOURCE.RESERVED, HDU.ANY, VALUE.STRING, false, "[Hz] Observed line rest frequency"),

    /**
     * [m] Rest wavelength of observed spectral line in image.
     * 
     * @since 1.19
     */
    RESTWAV(SOURCE.RESERVED, HDU.IMAGE, VALUE.REAL, true, "[m] Line rest wavelength"),

    /**
     * Image spectral reference system name.
     * 
     * @since 1.19
     */
    SPECSYS(SOURCE.RESERVED, HDU.IMAGE, VALUE.STRING, true, "Spectral reference frame"),

    /**
     * Image spectral reference system name of observer.
     * 
     * @since 1.19
     */
    SSYSOBS(SOURCE.RESERVED, HDU.IMAGE, VALUE.STRING, true, "Spectral reference frame of observer"),

    /**
     * Spectral reference system name of source.
     * 
     * @since 1.19
     */
    SSYSSRC(SOURCE.RESERVED, HDU.IMAGE, VALUE.STRING, true, "Spectral reference frame of source"),

    /**
     * [m/s] Radial velocity of source in the spectral reference frame.
     * 
     * @since 1.19
     */
    VELOSYS(SOURCE.RESERVED, HDU.IMAGE, VALUE.STRING, true, "[m/s] source radial velocity"),

    /**
     * Redshift value of source in the spectral reference frame.
     * 
     * @since 1.19
     */
    ZSOURCE(SOURCE.RESERVED, HDU.IMAGE, VALUE.STRING, true, "source redshift value"),

    /**
     * [deg] True velocity angle of source
     * 
     * @since 1.19
     */
    VELANGL(SOURCE.RESERVED, HDU.IMAGE, VALUE.STRING, true, "[deg] True velocity angle"),

    /**
     * [m] Geodetic location of observer (<i>x</i> coordinate).
     * 
     * @since 1.19
     */
    OBSGEO_X("OBSGEO-X", SOURCE.RESERVED, HDU.ANY, VALUE.STRING, false, "[m] Geodetic location x of observer"),

    /**
     * [m] Geodetic location of observer (<i>y</i> coordinate).
     * 
     * @since 1.19
     */
    OBSGEO_Y("OBSGEO-Y", SOURCE.RESERVED, HDU.ANY, VALUE.STRING, false, "[m] Geodetic location y of observer"),

    /**
     * [m] Geodetic location of observer (<i>z</i> coordinate).
     * 
     * @since 1.19
     */
    OBSGEO_Z("OBSGEO-Z", SOURCE.RESERVED, HDU.ANY, VALUE.STRING, false, "[m] Geodetic location z of observer"),

    /**
     * WCS name for the array entries in the given column index.
     * 
     * @since 1.19
     */
    WCSNn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, true, "column WCS name"),

    /**
     * [deg] The longitude of the celestial pole for the entries in the given column index (for spherical coordinates).
     * 
     * @since 1.19
     */
    LONPn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "[deg] column celestial pole longitude"),

    /**
     * [deg] The latitude of the celestial pole for the entries in the given column index (for spherical coordinates).
     * 
     * @since 1.19
     */
    LATPn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "[deg] column celestial pole latitude"),

    /**
     * [yr] Coordinate epoch for which the celestial coorinate system is defined for the given column index.
     * 
     * @since 1.19
     */
    EQUIn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "[yr] column coordinate epoch"),

    /**
     * The equatorial coordinate frame used for the given column index, e.g. 'FK4', 'FK5', or 'ICRS'.
     * 
     * @since 1.19
     */
    RADEn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, true, "column equatorial coordinate frame"),

    /**
     * [Hz] The rest frequency of the line in the given column index.
     * 
     * @since 1.19
     */
    RFRQn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "[Hz] line rest frequency in column"),

    /**
     * [Hz] The rest wavelength of the line in the given column index.
     * 
     * @since 1.19
     */
    RWAVn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "[m] line rest wavelength in column"),

    /**
     * Spectral reference frame for the given column index.
     * 
     * @since 1.19
     */
    SPECn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, true, "column spectral reference frame"),

    /**
     * Spectral reference system of observer for the given column index.
     * 
     * @since 1.19
     */
    SOBSn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, true, "column observer spectral frame"),

    /**
     * Spectral reference system of source for the given column index.
     * 
     * @since 1.19
     */
    SSRCn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, true, "column source spectral frame"),

    /**
     * [m/s] Source radial velocity for the given column index.
     * 
     * @since 1.19
     */
    VSYSn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "[m/s] column radial velocity"),

    /**
     * [deg] Angle of true velocity for the given column index.
     * 
     * @since 1.19
     */
    VANGn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "[deg] angle of velocity in column"),

    /**
     * Source redshift value for the given column index.
     * 
     * @since 1.19
     */
    ZSOUn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "column redshift value"),

    /**
     * [m] Geodetic location (<i>x</i> coordinate) of observer for he given column index.
     * 
     * @since 1.19
     */
    OBSGXn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, false, "[m] column geodetic location x"),

    /**
     * [m] Geodetic location (<i>y</i> coordinate) of observer for he given column index.
     * 
     * @since 1.19
     */
    OBSGYn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, false, "[m] column geodetic location y"),

    /**
     * [m] Geodetic location (<i>z</i> coordinate) of observer for he given column index.
     * 
     * @since 1.19
     */
    OBSGZn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, false, "[m] column geodetic location z"),

    /**
     * The coordinate axis type for array entries in this column (trailing index). The number of coordinate axes
     * (leading index) defined this way should match the dimensionality of the array elements in the column. This
     * version does not support alternative coordinates systems.
     * 
     * @see   #nCTYn
     * 
     * @since 1.19
     */
    nCTYPn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, false, "column coordinate axis type"),

    /**
     * The coordinate axis type for array entries in this column (trailing index). The number of coordinate axes
     * (leading index) defined this way should match the dimensionality of the array elements in the column. This
     * version supports alternative coordinates systems.
     * 
     * @see   #nCTYPn
     * 
     * @since 1.19
     */
    nCTYn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, true, "column coordinate axis type"),

    /**
     * The coordinate axis name for array entries in this column (trailing index). The number of coordinate axes
     * (leading index) defined this way should match the dimensionality of the array elements in the column.
     * 
     * @since 1.19
     */
    nCNAn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, true, "column coordinate axis type"),

    /**
     * The physical coordinate unit for array entries in this column (trailing index). The number of coordinate axes
     * (leading index) defined this way should match the dimensionality of the array elements in the column. This
     * version does not support alternative coordinates systems.
     * 
     * @see   #nCUNn
     * 
     * @since 1.19
     */
    nCUNIn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, false, "column coordinate axis unit"),

    /**
     * The physical coordinate unit for array entries in this column (trailing index). The number of coordinate axes
     * (leading index) defined this way should match the dimensionality of the array elements in the column. This
     * version supports alternative coordinates systems.
     * 
     * @see   #nCUNIn
     * 
     * @since 1.19
     */
    nCUNn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, true, "column coordinate axis unit"),

    /**
     * The coordinate reference value in the physical unit of the axis (if defined) for array entries in this column
     * (trailing index). The number of coordinate axes (leading index) defined this way should match the dimensionality
     * of the array elements in the column. This version does not support alternative coordinates systems.
     * 
     * @see   #nCRVn
     * 
     * @since 1.19
     */
    nCRVLn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, false, "column coordinate axis reference value"),

    /**
     * The coordinate reference value in the physical unit of the axis (if defined) for array entries in this column
     * (trailing index). The number of coordinate axes (leading index) defined this way should match the dimensionality
     * of the array elements in the column. This version supports alternative coordinates systems.
     * 
     * @see   #nCRVLn
     * 
     * @since 1.19
     */
    nCRVn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "column coordinate axis reference value"),

    /**
     * The coordinate axis random error in the physical unit of the axis (if defined) for array entries in this column
     * (trailing index). The number of coordinate axes (leading index) defined this way should match the dimensionality
     * of the array elements in the column.
     * 
     * @since 1.19
     */
    nCRDn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "column coordinate axis random error"),

    /**
     * The coordinate axis systematic error in the physical unit of the axis (if defined) for array entries in this
     * column (trailing index). The number of coordinate axes (leading index) defined this way should match the
     * dimensionality of the array elements in the column.
     * 
     * @since 1.19
     */
    nCSYn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "column coordinate axis systematic error"),

    /**
     * Phase axis zero point on axis (leading index) for array entries in this column (trailing index)
     * 
     * @since 1.19
     */
    nCZPn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "phase axis zero point"),

    /**
     * Phase axis period on axis (leading index) for array entries in this column (trailing index)
     * 
     * @since 1.19
     */
    nCPRn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "phase axis period"),

    /**
     * The coordinate axis spacing in the physical unit of the axis (if defined) for array entries in this column
     * (trailing index). The number of coordinate axes (leading index) defined this way should match the dimensionality
     * of the array elements in the column. This version does not support alternative coordinates systems.
     * 
     * @see   #nCDEn
     * 
     * @since 1.19
     */
    nCDLTn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, false, "column coordinate axis spacing"),

    /**
     * The coordinate axis spacing in the physical unit of the axis (if defined) for array entries in this column
     * (trailing index). The number of coordinate axes (leading index) defined this way should match the dimensionality
     * of the array elements in the column. This version supports alternative coordinates systems.
     * 
     * @see   #nCDLTn
     * 
     * @since 1.19
     */
    nCDEn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "column coordinate axis spacing"),

    /**
     * The 1-based coordinate reference pixel index in the physical unit of the axis (if defined) for array entries in
     * this column (trailing index). The number of coordinate axes (leading index) defined this way should match the
     * dimensionality of the array elements in the column. This version does not support alternative coordinates
     * systems.
     * 
     * @see   #nCRPn
     * 
     * @since 1.19
     */
    nCRPXn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, false, "column coordinate axis reference pixel"),

    /**
     * The 1-based coordinate reference pixel index in the physical unit of the axis (if defined) for array entries in
     * this column (trailing index). The number of coordinate axes (leading index) defined this way should match the
     * dimensionality of the array elements in the column. This version supports alternative coordinates systems.
     * 
     * @see   #nCRPXn
     * 
     * @since 1.19
     */
    nCRPn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "column coordinate axis reference pixel"),

    /**
     * @deprecated The FITS standard deprecated this keyword. Use {@link WCS#nnPCn} and {@link WCS#nnCDn}instead. [deg]
     *                 The coordinate axis rotation in the physical unit of the axis (if defined) for array entries in
     *                 this column (trailing index). The number of coordinate axes (leading index) defined this way
     *                 should match the dimensionality of the array elements in the column.
     * 
     * @since      1.19
     */
    nCROTn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, false, "[deg] column coordinate axis rotation"),

    /**
     * Coordinate transformation matrix in the PC convention. from rectilinear coordinate index <i>i</i> (leading index)
     * to coordinate index <i>j</i> (second index) for the for array entries in this column (trailing index). The number
     * of coordinate axes (leading index) defined this way should match the dimensionality of the array elements in the
     * column.
     * 
     * @since 1.19
     */
    nnPCn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "column coord. trans. matrix element"),

    /**
     * Coordinate transformation matrix in the CD convention. from rectilinear coordinate index <i>i</i> (leading index)
     * to coordinate index <i>j</i> (second index) for the for array entries in this column (trailing index). The number
     * of coordinate axes (leading index) defined this way should match the dimensionality of the array elements in the
     * column.
     * 
     * @since 1.19
     */
    nnCDn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "column coord. trans. matrix element"),

    /**
     * The coordinate parameter name <i>m</i> (trailing index) for axis <i>i</i> (leading index) for array entries in
     * this column (middle index). The number of coordinate axes (leading index) defined this way should match the
     * dimensionality of the array elements in the column. The shorter {@link #nSn_n} form may be required for column
     * indices &gt;99 with alternate coordinate systems.
     * 
     * @since 1.19
     */
    nPSn_n(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, true, "column axis parameter name"),

    /**
     * The coordinate parameter name <i>m</i> (trailing index) for axis <i>i</i> (leading index) for array entries in
     * this column (middle index). The number of coordinate axes (leading index) defined this way should match the
     * dimensionality of the array elements in the column. Same as {@value #nPSn_n}. This shorter form may be required
     * for column indices &gt;99 with alternate coordinate systems.
     * 
     * @since 1.19
     */
    nSn_n(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, true, "column axis parameter name"),

    /**
     * The coordinate parameter value <i>m</i> (trailing index) for axis <i>i</i> (leading index) for array entries in
     * this column (middle index). The number of coordinateaxes defined this way should match the dimensionality of the
     * array elements in the column. The shorter {@link #nVn_n} form may be required for column indices &gt;99 with
     * alternate coordinate systems.
     * 
     * @since 1.19
     */
    nPVn_n(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "column axis parameter value"),

    /**
     * The coordinate parameter value <i>m</i> (trailing index) for axis <i>i</i> (leading index) for array entries in
     * this column (middle index). The number of coordinateaxes defined this way should match the dimensionality of the
     * array elements in the column. Same as {@link #nPVn_n}. This shorter form may be required for column indices
     * &gt;99 with alternate coordinate systems.
     * 
     * @since 1.19
     */
    nVn_n(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "column axis parameter value"),

    /**
     * The coordinate parameter value for axis <i>i</i> (leading index) for array entries in this column (middle index).
     * The number of coordinate axes defined this way should match the dimensionality of the array elements in the
     * column.
     * 
     * @since 1.19
     */
    nVn_X(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "column axis parameter value"),

    /**
     * WCS name for the pixe list entries in the given column index. Same as {@link #TWCSn}. This shorter form may be
     * required for column indices &gt;99 with alternate coordinate systems.
     * 
     * @since 1.19
     */
    WCSn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, true, "column WCS name"),

    /**
     * WCS name for the pixe list entries in the given column index. The shorter form {@link #WCSn} may be required for
     * column indices &gt;99 with alternate coordinate systems.
     * 
     * @since 1.19
     */
    TWCSn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, true, "column WCS name"),

    /**
     * WCS dimensions for given column index.
     * 
     * @since 1.19
     */
    WCAXn(SOURCE.RESERVED, HDU.TABLE, VALUE.INTEGER, true, "Coordinate dimensions"),

    /**
     * The coordinate axis type for (1D) pixel lists in this column (trailing index). This version does not support
     * alternative coordinates systems.
     * 
     * @see   #TCTYn
     * 
     * @since 1.19
     */
    TCTYPn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, false, "column pixel axis type"),

    /**
     * The coordinate axis type for (1D) pixel lists in this column (trailing index). This version supports alternative
     * coordinates systems.
     * 
     * @see   #TCTYPn
     * 
     * @since 1.19
     */
    TCTYn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, true, "column pixel axis type"),

    /**
     * The coordinate axis name for (1D) pixel lists in this column (trailing index).
     * 
     * @since 1.19
     */
    TCNAn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, true, "column pixel axis name"),

    /**
     * The physical coordinate unit for (1D) pixel lists in this column (trailing index). This version does not support
     * alternative coordinates systems.
     * 
     * @see   #TCUNn
     * 
     * @since 1.19
     */
    TCUNIn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, false, "column pixel axis unit"),

    /**
     * The physical coordinate unit for (1D) pixel lists in this column (trailing index). This version supports
     * alternative coordinates systems.
     * 
     * @see   #TCUNIn
     * 
     * @since 1.19
     */
    TCUNn(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, true, "column pixel axis unit"),

    /**
     * The coordinate reference value in the physical unit of the axis (if defined) for the (1D) pixel lists in this
     * column (trailing index). This version does not support alternative coordinates systems.
     * 
     * @see   #TCRVn
     * 
     * @since 1.19
     */
    TCRVLn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, false, "column pixel axis reference value"),

    /**
     * The coordinate reference value in the physical unit of the axis (if defined) for the (1D) pixel lists in this
     * column (trailing index). This version supports alternative coordinates systems.
     * 
     * @see   #TCRVn
     * 
     * @since 1.19
     */
    TCRVn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "column pixel axis reference value"),

    /**
     * The coordinate axis spacing in the physical unit of the axis (if defined) for the (1D_) pixel lists in this
     * column (trailing index). This version does not support alternative coordinates systems.
     * 
     * @see   #TCDEn
     * 
     * @since 1.19
     */
    TCDLTn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, false, "column pixel axis spacing"),

    /**
     * The coordinate axis spacing in the physical unit of the axis (if defined) for the (1D_) pixel lists in this
     * column (trailing index). This version supports alternative coordinates systems.
     * 
     * @see   #TCDLTn
     * 
     * @since 1.19
     */
    TCDEn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "column pixel axis spacing"),

    /**
     * The coordinate axis random error in the physical unit of the axis (if defined) for the (1D_) pixel lists in this
     * column (trailing index).
     * 
     * @since 1.19
     */
    TCRDn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "column pixel axis random error"),

    /**
     * The coordinate axis systematics error in the physical unit of the axis (if defined) for the (1D_) pixel lists in
     * this column (trailing index).
     * 
     * @since 1.19
     */
    TCSYn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "column pixel axis random error"),

    /**
     * Phase axis zero point on axis (leading index) for array entries in this column (trailing index)
     * 
     * @since 1.19
     */
    TCZPn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "phase axis zero point"),

    /**
     * Phase axis period on axis (leading index) for array entries in this column (trailing index)
     * 
     * @since 1.19
     */
    TCPRn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "phase axis period"),

    /**
     * The 1-based coordinate reference pixel index in the physical unit of the axis (if defined) for the (1D) pixel
     * lists in this column (trailing index). This version does not support alternative coordinates systems.
     * 
     * @see   #TCRPn
     * 
     * @since 1.19
     */
    TCRPXn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, false, "column pixel axis reference pixel"),

    /**
     * The 1-based coordinate reference pixel index in the physical unit of the axis (if defined) for the (1D) pixel
     * lists in this column (trailing index). This version supports alternative coordinates systems.
     * 
     * @see   #TCRPXn
     * 
     * @since 1.19
     */
    TCRPn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "column pixel axis reference pixel"),

    /**
     * Coordinate transformation matrix in the PC convention. from column index <i>n</i> (leading index) to column index
     * <i>k</i> (second index) for the for the (1D) pixel lists in this column. The shorter form {@link #TPn_n} may be
     * required for column indices &gt;99 with alternate coordinate systems.
     * 
     * @see   #TPn_n
     * 
     * @since 1.19
     */
    TPCn_n(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "column pix trans. matrix element"),

    /**
     * Coordinate transformation matrix in the PC convention. from column index <i>n</i> (leading index) to column index
     * <i>k</i> (second index) for the for the (1D) pixel lists in this column. Same as {@link #TPCn_n}. This shorter
     * form may be required for column indices &gt;99 with alternate coordinate systems.
     * 
     * @see   #TPCn_n
     * 
     * @since 1.19
     */
    TPn_n(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "column pix trans. matrix element"),

    /**
     * @deprecated The FITS standard deprecated this keyword. Use {@link WCS#TPCn_n} and {@link WCS#TCDn_n}instead.
     *                 [deg] The coordinate axis rotation in the physical unit of the axis (if defined) for the (1D)
     *                 pixel lists in this column (trailing index).
     * 
     * @since      1.19
     */
    TCROTn(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, false, "[deg] column pixel axis rotation"),

    /**
     * Coordinate transformation matrix in the CD convention. from column index <i>n</i> (leading index) to column index
     * <i>k</i> (second index) for the for the (1D) pixel lists in this column. The shorter form {@link #TCn_n} may be
     * required for column indices &gt;99 with alternate coordinate systems.
     * 
     * @since 1.19
     */
    TCDn_n(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "column pix trans. matrix element"),

    /**
     * Coordinate transformation matrix in the CD convention. from column index <i>n</i> (leading index) to column index
     * <i>k</i> (second index) for the for the (1D) pixel lists in this column. Same as {@link #TCDn_n}. This shorter
     * form may be required for column indices &gt;99 with alternate coordinate systems.
     * 
     * @since 1.19
     */
    TCn_n(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "column pix trans. matrix element"),

    /**
     * The coordinate parameter name <i>m</i> (trailing index) for the (1D) pixel list entries in this column (leading
     * index). This shorter form {@link #TSn_n} may be required for column indices &gt;99 with alternate coordinate
     * systems.
     * 
     * @since 1.19
     */
    TPSn_n(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, true, "column axis parameter name"),

    /**
     * The coordinate parameter name <i>m</i> (trailing index) for the (1D) pixel list entries in this column (leading
     * index). Same as {@link #TPSn_n}. This shorter form may be required for column indices &gt;99 with alternate
     * coordinate systems.
     * 
     * @since 1.19
     */
    TSn_n(SOURCE.RESERVED, HDU.TABLE, VALUE.STRING, true, "column axis parameter name"),

    /**
     * The coordinate parameter value <i>m</i> (trailing index) for the (1D) pixel list entries in this column (leading
     * index). The shorter form {@link #TVn_n} may be required for column indices &gt;99 with alternate coordinate
     * systems.
     * 
     * @since 1.19
     */
    TPVn_n(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "column pixel axis parameter value"),

    /**
     * The coordinate parameter value <i>m</i> (trailing index) for the (1D) pixel list entries in this column (leading
     * index). Same as {@value #TPVn_n}. This shorter form may be required for column indices &gt;99 with alternate
     * coordinate systems.
     * 
     * @since 1.19
     */
    TVn_n(SOURCE.RESERVED, HDU.TABLE, VALUE.REAL, true, "column pixel axis parameter value");

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

    /** Logarithmically sampled algorithm code for {@link #CTYPEn} keywords */
    public static final String ALGO_LOG = "LOG";

    /** Detector sampling algorithm code for {@link #CTYPEn} keywords */
    public static final String ALGO_GRI = "GRI";

    /** Detector sampling algorithm code for {@link #CTYPEn} keywords */
    public static final String ALGO_GRA = "GRA";

    /** Irregular sampling algorithm code for {@link #CTYPEn} keywords */
    public static final String ALGO_TAB = "TAB";

    /** Spectral frequency coordinate value for {@link #CTYPEn} keywords */
    public static final String SPECTRAL_TYPE_FREQ = "FREQ";

    /** Spectral energy coordinate value for {@link #CTYPEn} keywords */
    public static final String SPECTRAL_TYPE_ENER = "ENER";

    /** Spectral wavenumber coordinate value for {@link #CTYPEn} keywords */
    public static final String SPECTRAL_TYPE_WAVN = "WAVN";

    /** Spectral radial velocity coordinate value for {@link #CTYPEn} keywords */
    public static final String SPECTRAL_TYPE_VRAD = "VRAD";

    /** Spectral vacuum wavenlength coordinate value for {@link #CTYPEn} keywords */
    public static final String SPECTRAL_TYPE_WAVE = "WAVE";

    /** Spectral optical velocity coordinate value for {@link #CTYPEn} keywords */
    public static final String SPECTRAL_TYPE_VOPT = "VOPT";

    /** Spectral redshift coordinate value for {@link #CTYPEn} keywords */
    public static final String SPECTRAL_TYPE_ZOPT = "ZOPT";

    /** Spectral wavelength in air coordinate value for {@link #CTYPEn} keywords */
    public static final String SPECTRAL_TYPE_AWAV = "AWAV";

    /** Spectral apparent radial velocity coordinate value for {@link #CTYPEn} keywords */
    public static final String SPECTRAL_TYPE_VELO = "VELO";

    /** Spectral beta factor (<i>v/c</i>) coordinate value for {@link #CTYPEn} keywords */
    public static final String SPECTRAL_TYPE_BETA = "BETA";

    /** Spectral frequency expressed as wavelength transformation code for {@link #CTYPEn} keywords */
    public static final String SPECTRAL_ALGO_F2W = "F2W";

    /** Spectral frequency expressed as apparent radial velocity transformation code for {@link #CTYPEn} keywords */
    public static final String SPECTRAL_ALGO_F2V = "F2V";

    /** Spectral frequency expressed as air wavelength transformation code for {@link #CTYPEn} keywords */
    public static final String SPECTRAL_ALGO_F2A = "F2A";

    /** Spectral wavelength expressed as frequency transformation code for {@link #CTYPEn} keywords */
    public static final String SPECTRAL_ALGO_W2F = "W2F";

    /** Spectral wavelength expressed as apparent radial velocity transformation code for {@link #CTYPEn} keywords */
    public static final String SPECTRAL_ALGO_W2V = "W2V";

    /** Spectral wavelength expressed as air wavelength transformation code for {@link #CTYPEn} keywords */
    public static final String SPECTRAL_ALGO_W2A = "W2A";

    /** Spectral radial velocity expressed as frequency transformation code for {@link #CTYPEn} keywords */
    public static final String SPECTRAL_ALGO_V2F = "V2F";

    /** Spectral radial velocity expressed as wavelength transformation code for {@link #CTYPEn} keywords */
    public static final String SPECTRAL_ALGO_V2W = "V2W";

    /** Spectral radial velocity expressed as air wavelength transformation code for {@link #CTYPEn} keywords */
    public static final String SPECTRAL_ALGO_V2A = "V2A";

    /** Spectral air wavelength expressed as frequency transformation code for {@link #CTYPEn} keywords */
    public static final String SPECTRAL_ALGO_A2F = "A2F";

    /** Spectral air wavelength expressed as vacuum wavelength transformation code for {@link #CTYPEn} keywords */
    public static final String SPECTRAL_ALGO_A2W = "A2W";

    /**
     * Spectral air wavelength expressed as apparent radial velocity transformation code for {@link #CTYPEn} keywords
     */
    public static final String SPECTRAL_ALGO_A2V = "A2V";

    private final IFitsHeader key;

    private boolean supportsAlt;

    WCS(SOURCE status, HDU hdu, VALUE valueType, boolean allowsAlt, String comment) {
        key = new FitsHeaderImpl(name(), status, hdu, valueType, comment);
        supportsAlt = allowsAlt;
    }

    WCS(String headerName, SOURCE status, HDU hdu, VALUE valueType, boolean allowsAlt, String comment) {
        key = new FitsHeaderImpl(headerName == null ? name() : headerName, status, hdu, valueType, comment);
        supportsAlt = allowsAlt;
    }

    @Override
    public final IFitsHeader impl() {
        return key;
    }

    /**
     * Use for specifying an alternative coordinate system.
     * 
     * @param  c                             The alternativce coordinate system marker 'A' through 'Z' (case
     *                                           insensitive).
     * 
     * @return                               The standard FITS keyword with the alternate coordinate system marker
     *                                           attached.
     * 
     * @throws IllegalArgumentException      if the marker is outside of the legal range of 'A' through 'Z' (case
     *                                           insensitive).
     * @throws IllegalStateException         if the keyword with the alternate coordinate marker exceeeds the maximum
     *                                           8-byre length of standard FITS keywords. You might want to use an
     *                                           equivalent shorter alternative keyword.
     * @throws UnsupportedOperationException if the keyword does not support alternative coordinate systems
     * 
     * @see                                  #supportsAlt()
     */
    public IFitsHeader alt(char c) throws IllegalArgumentException, IllegalStateException, UnsupportedOperationException {
        if (!supportsAlt) {
            throw new UnsupportedOperationException("WCS keyword " + key.key() + " does not support alternatives.");
        }

        c = Character.toUpperCase(c);
        if (c < 'A' || c > 'Z') {
            throw new IllegalArgumentException("Expected 'A' through 'Z': Got '%c'");
        }

        StringBuffer headerName = new StringBuffer(key.key());
        headerName.append(Character.toUpperCase(c));

        if (headerName.length() > HeaderCard.MAX_KEYWORD_LENGTH) {
            throw new IllegalStateException(
                    "Alternate keyword " + headerName.toString() + " is too long. Use shorter equivalent instead.");
        }

        return new FitsHeaderImpl(headerName.toString(), status(), hdu(), valueType(), comment());
    }

    /**
     * Checks if this keyword can be used with alternative coordinate systems.
     * 
     * @return <code>true</code> if the keyword is capable of supporting alternative coordinate systems, or else
     *             <code>false</code>
     * 
     * @see    #alt(char)
     */
    public boolean supportsAlt() {
        return supportsAlt;
    }

}
