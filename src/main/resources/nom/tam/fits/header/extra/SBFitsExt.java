package nom.tam.fits.header.extra;

/**
 * A Set of
FITS Standard Extensions for Amateur
Astronomical Processing Software Packages published by SBIG
 * @author Richard van Nieuwenhoven.
 *
 */
public enum SBFitsExt {
    Mandatory
    Extension Keywords
    Th
    e following Keywords are not defined in the FITS Standard but are defined in this Standard.
    All
    must be included
    .
    EXPTIME
    Floating Point
    -
    The total exposure time in seconds. For Track
    and Accumulate Images this is the sum of all the exposure times.
    CCD
    -
    TEMP
    Floating Point
    -
    Temperature of CCD when exposure taken
    XPIXSZ
    Floating Point
    -
    Pixel width in microns (after binning)
    YPIXSZ
    Floating Point
    -
    Pixel height in microns (after binning)
    XBINNING
    Integer
    -
    Binning factor in width
    YBINNING
    Integer
    -
    Binni
    ng factor in height
    XORGSUBF
    Integer
    -
    Sub frame X position of upper
    -
    left pixel relative to
    whole frame in binned pixel units
    YORGSUBF
    “ “ Y “
    EGAIN
    Floating Point
    -
    Electronic gain in e
    -
    /ADU
    FOCALLEN
    Floating Point
    -
    Focal Length of the Telescope u
    sed in
    millimeters
    APTDIA
    Floating Point
    -
    Aperture Diameter of the Telescope used in
    millimeters.
    APTAREA
    Floating Point
    -
    Aperture Area of the Telescope used in square
    -
    millimeters. Note that we are specifying the area as well as the
    diameter because we
    want to be able to correct for any central
    obstruction.
    CBLACK
    Integer
    -
    Upon initial display of this image use this ADU level
    for the Black level.
    CWHITE
    Integer
    -
    Upon initial display of this image use this ADU level
    as the White level. For the SBIG me
    thod of displaying images
    using
    Background
    and
    Range
    the following conversions would be
    used:
    Background
    =
    CBLACK
    Range
    =
    CWHITE
    -
    CBLACK
    PEDESTAL
    Integer
    -
    Add this ADU count to each pixel value to get to a
    zero
    -
    based ADU. For example in SBIG
    images we add 100 ADU to
    each pixel to stop underflow at Zero ADU from noise. We would
    set PEDESTAL to
    -
    100 in this case.
    DATAMAX
    Integer
    -
    Pixels at or above DATAMAX are considered to be
    saturated and thus unusable for Photometric calculations
    SWCREATE
    S
    tring
    -
    This indicates the name and version of the Software that
    initially created this file ie ‘SBIGs CCDOps Version 5.10’
    SWMODIFY
    String
    -
    This indicates the name and version of the Software that
    modified this file ie ‘SBIGs CCDOps Version 5.10’ and the
    re can
    be multiple copies of this keyword. Only add this keyword if you 
    actually modified the image and we suggest placing this above the
    HISTORY
    keywords corresponding to the modifications made to the
    image.
    SBSTDVER
    String
    -
    This string indicates the ve
    rsion of this standard that
    the image was created to ie ‘SBFITSEXT Version 1.0’
    Optional Keywords
    The following Keywords are not defined in the FITS Standard but are defined in this Standard. They
    may or may not be included by AIP Software Packages adher
    ing to this Standard. Any of these
    keywords read by an AIP Package must be preserved in files written.
    FILTER
    String
    -
    Optical Filter used to take image, ie H
    -
    Alpha. If this
    keyword is not included there was no filter used to take the
    image.
    TRAKTIME
    Fl
    oating Point
    -
    If the image was auto
    -
    guided this is the
    exposure time in seconds of the tracker used to acquire this
    image. If this keyword is not present then the image was
    unguided or hand guided.
    SNAPSHOT
    Integer
    -
    Number of images combined to make thi
    s image as in
    Track and Accumulate or Co
    -
    Added images.
    SET
    -
    TEMP
    Floating Point
    -
    This is the setpoint of the cooling in degrees
    C. If it is not specified the setpoint is assumed to be the
    CCD
    -
    TEMP
    specified above.
    IMAGETYP
    String
    -
    This indicates the type
    of image and should be one of
    the following:
    Light Frame
    Dark Frame
    Bias Frame
    Flat Field
    OBJCTRA
    String
    -
    This is the Right Ascension of the center of the image
    in hours, minutes and secon
    ds. The format for this is
    ’12
    24
    23.123’ (HH
    MM
    SS.SSS)
    using a space as the separator.
    OBJCTDEC
    String
    -
    This is the Declination of the center of the image in
    degree
    s. The format for this is ‘+25
    12
    34.111’ (SDD
    MM
    SS.SSS)
    using a space as the separator
    . For the sign, North is + and
    South is
    -
    .
    CENTAZ
    String
    -
    Azimuth in degrees of the center of the image in
    degrees. Format is
    the same as the
    OBJCTDEC
    keyword.
    CENTALT
    String
    -
    Altitude in degrees of the center of the image in
    degrees. Format is the same as the
    OBJCTDEC
    keyword.
    SITELAT
    String
    -
    Latitude of the imaging location in degrees. Format is
    the same as the
    OBJCTDEC
    key
    word.
    SITELONG
    String
    -
    Longitude of the imaging location in degrees. Format is
    the same as the
    OBJCTDEC
    keyword.
    
    
    Optional Keywords
    The following Keywords are not defined in the FITS Standard but are defined in this Standard. They
    may or may not be included by AIP Software Packages adher
    ing to this Standard. Any of these
    keywords read by an AIP Package must be preserved in files written.
    FILTER
    String
    -
    Optical Filter used to take image, ie H
    -
    Alpha. If this
    keyword is not included there was no filter used to take the
    image.
    TRAKTIME
    Fl
    oating Point
    -
    If the image was auto
    -
    guided this is the
    exposure time in seconds of the tracker used to acquire this
    image. If this keyword is not present then the image was
    unguided or hand guided.
    SNAPSHOT
    Integer
    -
    Number of images combined to make thi
    s image as in
    Track and Accumulate or Co
    -
    Added images.
    SET
    -
    TEMP
    Floating Point
    -
    This is the setpoint of the cooling in degrees
    C. If it is not specified the setpoint is assumed to be the
    CCD
    -
    TEMP
    specified above.
    IMAGETYP
    String
    -
    This indicates the type
    of image and should be one of
    the following:
    Light Frame
    Dark Frame
    Bias Frame
    Flat Field
    OBJCTRA
    String
    -
    This is the Right Ascension of the center of the image
    in hours, minutes and secon
    ds. The format for this is
    ’12
    24
    23.123’ (HH
    MM
    SS.SSS)
    using a space as the separator.
    OBJCTDEC
    String
    -
    This is the Declination of the center of the image in
    degree
    s. The format for this is ‘+25
    12
    34.111’ (SDD
    MM
    SS.SSS)
    using a space as the separator
    . For the sign, North is + and
    South is
    -
    .
    CENTAZ
    String
    -
    Azimuth in degrees of the center of the image in
    degrees. Format is
    the same as the
    OBJCTDEC
    keyword.
    CENTALT
    String
    -
    Altitude in degrees of the center of the image in
    degrees. Format is the same as the
    OBJCTDEC
    keyword.
    SITELAT
    String
    -
    Latitude of the imaging location in degrees. Format is
    the same as the
    OBJCTDEC
    key
    word.
    SITELONG
    String
    -
    Longitude of the imaging location in degrees. Format is
    the same as the
    OBJCTDEC
    keyword.
}
