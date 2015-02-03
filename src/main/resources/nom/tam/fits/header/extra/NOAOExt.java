package nom.tam.fits.header.extra;

/**
 * NOAO FITS Keyword Dictionary
 * @author nir
 *
 */
public enum NOAOExt {
    NAME:         Time.date
    KEYWORD:      DATEOBS
    DEFAULT:      DATE-OBS
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %s (date)
    UNITS:        
    COMMENT:      Date of observation
    EXAMPLE:      '05/04/87'
    DESCRIPTION:
        Default date for the observation.  This keyword is generally not used
        and is DATE-OBS keyword for the start of the exposure on the detector
        is used.


    NAME:         Time.utc
    KEYWORD:      UTCOBS
    DEFAULT:      UTC-OBS
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %h
    UNITS:        UNITHOUR
    COMMENT:      UTC of observation
    EXAMPLE:      '09:27:27.00'
    DESCRIPTION:
        Default UTC time for the observation.  This keyword is generally not
        used and is UTC-OBS keyword for the start of the exposure on the
        detector is used.


    NAME:         Time.mjd
    KEYWORD:      MJDOBS
    DEFAULT:      MJD-OBS
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      MJD of observation
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Default modified Julian date for the observation.  The fractional part
        of the date is given to better than a second of time.  This keyword is
        generally not used and is MJD-OBS keyword for the start of the exposure
        on the detector is used.


    NAME:         Time.lst
    KEYWORD:      LSTOBS
    DEFAULT:      LST-OBS
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %h
    UNITS:        UNITHOUR
    COMMENT:      LST of observation
    EXAMPLE:      '14:53:42.00'
    DESCRIPTION:
        Default local siderial time for the observation.  This keyword is
        generally not used and is LST-OBS keyword for the start of the exposure
        on the detector is used.


    NAME:         Time.timesys
    KEYWORD:      TIMESYS
    DEFAULT:      'UTC'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Default time system
    EXAMPLE:      'UTC approximate'
    DESCRIPTION:
        Default time system.  All times which do not have a "timesys" element
        associated with them in this dictionary default to this keyword.


    NAME:         Image.simple
    KEYWORD:      SIMPLE
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %b
    UNITS:        
    COMMENT:      File conforms to FITS standard
    EXAMPLE:      T
    DESCRIPTION:
        Required FITS format keyword.


    NAME:         Image.bitpix
    KEYWORD:      BITPIX
    DEFAULT:      none
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %d
    UNITS:        
    COMMENT:      Number of bits per pixel
    EXAMPLE:      16
    DESCRIPTION:
        Required FITS format keyword.


    NAME:         Image.naxis
    KEYWORD:      NAXIS
    DEFAULT:      none
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %d
    UNITS:        
    COMMENT:      Number of image axes
    EXAMPLE:      2.
    DESCRIPTION:
        Required FITS format keyword.


    NAME:         Image.naxis[i]
    KEYWORD:      NAXIS%d
    DEFAULT:      none
    INDEX:        1-9
    HDU:          primary & extension
    VALUE:        %d
    UNITS:        
    COMMENT:      Number of pixels along axis
    EXAMPLE:      2048
    DESCRIPTION:
        Required FITS format keywords.


    NAME:         Image.bzero
    KEYWORD:      BZERO
    DEFAULT:      0.
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %g
    UNITS:        
    COMMENT:      Zero factor
    EXAMPLE:      -32768.
    DESCRIPTION:
        Conversion from file value to data value: data = BSCALE * file + BZERO.
        Only used if BITPIX is positive.  A value of -32768 is used to record
        unsigned short integer data.


    NAME:         Image.bscale
    KEYWORD:      BSCALE
    DEFAULT:      1.
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %g
    UNITS:        
    COMMENT:      Scale factor
    EXAMPLE:      2.
    DESCRIPTION:
        Conversion from file value to data value: data = BSCALE * file + BZERO.
        Only used if BITPIX is positive.


    NAME:         Image.comment
    KEYWORD:      COMMENT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %s
    UNITS:        
    COMMENT:      
    EXAMPLE:      This is a comment.
    DESCRIPTION:
        Comment keyword.  This keyword does not normally have an equal
        sign.


    NAME:         Image.history
    KEYWORD:      HISTORY
    DEFAULT:      none
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %s
    UNITS:        
    COMMENT:      
    EXAMPLE:      This is a history.
    DESCRIPTION:
        History keyword.  This keyword does not normally have an equal
        sign.


    NAME:         Image.extend
    KEYWORD:      EXTEND
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %b
    UNITS:        
    COMMENT:      File contains extensions
    EXAMPLE:      T
    DESCRIPTION:
        Required FITS keyword when there are extensions.


    NAME:         Image.nextend
    KEYWORD:      NEXTEND
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %d
    UNITS:        
    COMMENT:      Number of extensions
    EXAMPLE:      8
    DESCRIPTION:
        Number of extensions in FITS file.


    NAME:         Image.xtension
    KEYWORD:      XTENSION
    DEFAULT:      none
    INDEX:        none
    HDU:          extension
    VALUE:        %s
    UNITS:        
    COMMENT:      FITS extension type
    EXAMPLE:      'IMAGE   '
    DESCRIPTION:
        Required FITS keyword for extensions.  Extensions for CCD image
        data have the value 'IMAGE'.


    NAME:         Image.pcount
    KEYWORD:      PCOUNT
    DEFAULT:      none
    INDEX:        none
    HDU:          extension
    VALUE:        %d
    UNITS:        
    COMMENT:      Number of pixels following data
    EXAMPLE:      0
    DESCRIPTION:
        Required FITS standard keyword in extensions.


    NAME:         Image.gcount
    KEYWORD:      GCOUNT
    DEFAULT:      none
    INDEX:        none
    HDU:          extension
    VALUE:        %d
    UNITS:        
    COMMENT:      Number of groups
    EXAMPLE:      1
    DESCRIPTION:
        Required FITS standard keyword in extensions.


    NAME:         Image.extname
    KEYWORD:      EXTNAME
    DEFAULT:      none
    INDEX:        none
    HDU:          extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Extension name
    EXAMPLE:      'im1     '
    DESCRIPTION:
        The extension name which can be used to reference a specific image in
        the image extension format.  For ease of use it should be short.  The
        recommended name is im#, where # is the amplifier/extension version
        number.


    NAME:         Image.extver
    KEYWORD:      EXTVER
    DEFAULT:      none
    INDEX:        none
    HDU:          extension
    VALUE:        %d
    UNITS:        
    COMMENT:      Extension version
    EXAMPLE:      3
    DESCRIPTION:
        The extension version number which is identical to IMAGEID.
        For detectors with CCDs this would be the unique amplifier number
        within the detector.


    NAME:         Image.inherit
    KEYWORD:      INHERIT
    DEFAULT:      none
    INDEX:        none
    HDU:          extension
    VALUE:        %b
    UNITS:        
    COMMENT:      Inherit global keywords?
    EXAMPLE:      T
    DESCRIPTION:
        Parameter to indicate whether the global keywords are to be inherited
        by the image extension.


    NAME:         Image.Header.Time.timesys
    KEYWORD:      TSYSHDR
    DEFAULT:      TIMESYS
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Time system for header creation
    EXAMPLE:      'UTC     '
    DESCRIPTION:
        Time system for the header creation keywords.


    NAME:         Image.Header.Time.time
    KEYWORD:      TIMEHDR
    DEFAULT:      TIMESYS
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %h
    UNITS:        UNITHOUR
    COMMENT:      Time of header creation
    EXAMPLE:      '09:27:27.00'
    DESCRIPTION:
        Time of header creation.


    NAME:         Image.Header.Time.date
    KEYWORD:      DATEHDR
    DEFAULT:      DATE-OBS
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %s (date)
    UNITS:        
    COMMENT:      Date of header creation
    EXAMPLE:      '05/04/87'
    DESCRIPTION:
        Date header creation.  The format follows the FITS 'date' standard.


    NAME:         Image.Header.utc
    KEYWORD:      UTCHDR
    DEFAULT:      UTC-OBS
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %h
    UNITS:        UNITHOUR
    COMMENT:      UTC of header creation
    EXAMPLE:      '09:27:27.00'
    DESCRIPTION:
        UTC of header creation.


    NAME:         Image.Header.mjd
    KEYWORD:      MJDHDR
    DEFAULT:      MJD-OBS
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      MJD of header creation
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Modified Julian date when the image header was created by the software.
        The fractional part of the date is given to better than a second of time.
        Many header keywords may be sampled or computed at this time and this
        keyword is the default for these.


    NAME:         Image.Header.lst
    KEYWORD:      LSTHDR
    DEFAULT:      LST-OBS
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %h
    UNITS:        UNITHOUR
    COMMENT:      LST of header creation
    EXAMPLE:      '14:53:42.00'
    DESCRIPTION:
        Local siderial time of the header creation.


    NAME:         Image.filename
    KEYWORD:      FILENAME
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Original host filename
    EXAMPLE:      'dflat313'
    DESCRIPTION:
        Filename used to record the original data.


    NAME:         Image.Checksum.header
    KEYWORD:      CHECKSUM
    DEFAULT:      none
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %s (checksum)
    UNITS:        
    COMMENT:      Header checksum
    EXAMPLE:      '0WDA3T940TA90T99'
    DESCRIPTION:
        Header checksum value for verifying the data.  This follows the
        proposed FITS checksum standard.


    NAME:         Image.Checksum.data
    KEYWORD:      DATASUM
    DEFAULT:      none
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %s (datasum)
    UNITS:        
    COMMENT:      Data checksum
    EXAMPLE:      'aMmjbMkhaMkhaMkh'
    DESCRIPTION:
        Data checksum value for verifying the data.  This follows the
        proposed FITS checksum standard.


    NAME:         Image.Checksum.version
    KEYWORD:      CHECKVER
    DEFAULT:      none
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Checksum version
    EXAMPLE:      'complement'
    DESCRIPTION:
        Version of checksum method used for the checksums.


    NAME:         Image.Version.hardware
    KEYWORD:      IMAGEHWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Image creation hardware version
    EXAMPLE:      'Solaris Telescope System V1'
    DESCRIPTION:
        Image creation system hardware version.


    NAME:         Image.Version.software
    KEYWORD:      IMAGESWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Image creation software version
    EXAMPLE:      'DCA (Jun96) with mosaic.tcl (Aug97)'
    DESCRIPTION:
        Image creation system software version.


    NAME:         Image.end
    KEYWORD:      END
    DEFAULT:      none
    INDEX:        none
    HDU:          primary & extension
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Required FITS keyword ending a header.  This may be moved to the
        end of the last FITS 2880 byte header record leaving blank lines between
        the last card and the END card for runtime expansion.


    NAME:         Observation.obsid
    KEYWORD:      OBSID
    DEFAULT:      none
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Observation identification
    EXAMPLE:      'kpno.36in.870405.257752'
    DESCRIPTION:
        The unique observatory observation identification.  This serves
        to identify all data from the same observation.


    NAME:         Observation.imageid
    KEYWORD:      IMAGEID
    DEFAULT:      none
    INDEX:        none
    HDU:          extension
    VALUE:        %d
    UNITS:        
    COMMENT:      Image identification
    EXAMPLE:      1
    DESCRIPTION:
        The image identification when there are multiple images within
        an observation.  For detectors with CCDs this would be a unique
        number assigned to each amplifier in the detector.


    NAME:         Observation.title
    KEYWORD:      OBJECT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary & extnsion
    VALUE:        %s
    UNITS:        
    COMMENT:      Observation title
    EXAMPLE:      'm51 V 600s'
    DESCRIPTION:
        The observation title given by the observer.  This will sometimes be
        the name of the astronomical target object but often it is not or has
        additional information so the standard object name(s) should be given
        by the OBJNAME keyword(s).


    NAME:         Observation.Obstype.type
    KEYWORD:      OBSTYPE
    DEFAULT:      none
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Observation type
    EXAMPLE:      'OBJECT  '
    DESCRIPTION:
        The type of observation such as an astronomical exposure or a
        particular type of calibration exposure.


    NAME:         Observation.exprequest
    KEYWORD:      EXPREQ
    DEFAULT:      EXPTIME
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %g
    UNITS:        UNITTIME
    COMMENT:      Requested exposure time
    EXAMPLE:      600
    DESCRIPTION:
        Requested exposure time of the observation.


    NAME:         Observation.status
    KEYWORD:      OBSSTAT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Observation status
    EXAMPLE:      'ok     '
    DESCRIPTION:
        Status of the observation.


    NAME:         Observation.Coordinate.Equatorial.system
    KEYWORD:      OBSRADEC
    DEFAULT:      TELRADEC
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Observation coordinate system
    EXAMPLE:      'FK5     '
    DESCRIPTION:
        Coordinate system used in observation coordinates.


    NAME:         Observation.Coordinate.Equatorial.equinox
    KEYWORD:      OBSEQUIN
    DEFAULT:      TELEQUIN
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        'yr'
    COMMENT:      Observation coordinate equinox
    EXAMPLE:      2000.0
    DESCRIPTION:
        Equinox of coordinates used in observation coordinates.  A value before
        1984 is Besselian otherwise it is Julian.


    NAME:         Observation.Coordinate.Equatorial.ra
    KEYWORD:      OBSRA
    DEFAULT:      DETRA
    INDEX:        none
    HDU:          primary
    VALUE:        %h
    UNITS:        OBSRAU
    COMMENT:      Observation right ascension
    EXAMPLE:      '13:29:24.00'
    DESCRIPTION:
        Right ascension of the observation.  This may be distinct from the object
        coordinates and the telescope coordinates.  It may be used to indicate
        the requested observation coordinates.


    NAME:         Observation.Coordinate.Equatorial.dec
    KEYWORD:      OBSDEC
    DEFAULT:      DETDEC
    INDEX:        none
    HDU:          primary
    VALUE:        %h
    UNITS:        OBSDECU
    COMMENT:      Observation declination
    EXAMPLE:      '47:15:34.00'
    DESCRIPTION:
        Declination of the observation.  This may be distinct from the object
        coordinates and the telescope coordinates.  It may be used to indicate
        the requested observation coordinates.


    NAME:         Observation.Coordinate.Equatorial.raunit
    KEYWORD:      OBSRAU
    DEFAULT:      DETRAU
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Right ascension unit
    EXAMPLE:      'hr'
    DESCRIPTION:
        Right ascension unit.


    NAME:         Observation.Coordinate.Equatorial.decunit
    KEYWORD:      OBSDECU
    DEFAULT:      DETDECU
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Declination unit
    EXAMPLE:      'deg'
    DESCRIPTION:
        Declination unit.


    NAME:         Observation.Coordinate.Equatorial.epoch
    KEYWORD:      OBSEPOCH
    DEFAULT:      TELEPOCH
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        'yr'
    COMMENT:      Observation coordinate epoch
    EXAMPLE:      1950.0
    DESCRIPTION:
        Epoch of the coordinates used in observation coordinates.


    NAME:         Observation.airmass[n]
    KEYWORD:      AIRMASS AIRMASS%d
    DEFAULT:      none none
    INDEX:        none 1-9
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      Airmass
    EXAMPLE:      1.080
    DESCRIPTION:
        The computed airmass(es) at the time(s) given by the AMMJDn keywords. 


    NAME:         Observation.airmass-mjd[n]
    KEYWORD:      AMMJD AMMJD%d
    DEFAULT:      MJDHDR AMMJD
    INDEX:        none 1-9
    HDU:          primary
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      MJD of airmass
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the airmass calculation given as modified Julian dates.  The
        MJDHDR keyword may be used for the time at which the image header is
        created or the MJD-OBS keyword may be used for the time of
        observation.


    NAME:         Observation.error[n]
    KEYWORD:      ERROR%3d
    DEFAULT:      none
    INDEX:        1-999
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Error information.  The sequence numbers are used to order the information.


    NAME:         Observation.Obstype.Focusseq.nexposures
    KEYWORD:      FOCNEXPO
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %d
    UNITS:        
    COMMENT:      Number of focus exposures
    EXAMPLE:      7
    DESCRIPTION:
        Number of focus exposures in a focus sequence.


    NAME:         Observation.Obstype.Focusseq.start
    KEYWORD:      FOCSTART
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        'instrumental'
    COMMENT:      Starting focus
    EXAMPLE:      1910.
    DESCRIPTION:
        Starting focus value in focus sequence.


    NAME:         Observation.Obstype.Focusseq.step
    KEYWORD:      FOCSTEP
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        'instrumental'
    COMMENT:      Focus step
    EXAMPLE:      20.
    DESCRIPTION:
        Focus increment step in focus sequence.


    NAME:         Observation.Obstype.Focusseq.shift
    KEYWORD:      FOCSHIFT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        'pixel'
    COMMENT:      Shift between focus exposures
    EXAMPLE:      50.
    DESCRIPTION:
        Pixel shift on the detector between exposures in a focus sequence.


    NAME:         Observation.Obstype.Lamp.name
    KEYWORD:      LAMP
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Calibration lamp
    EXAMPLE:      'He-Ne-Ar #2'
    DESCRIPTION:
        Calibration lamp name


    NAME:         Observation.Obstype.Lamp.type
    KEYWORD:      LAMPTYPE
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Lamp type
    EXAMPLE:      'He-Ne-Ar'
    DESCRIPTION:
        Calibration lamp type.


    NAME:         Coordinate.system
    KEYWORD:      RADECSYS
    DEFAULT:      'FK5'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Default coordinate system
    EXAMPLE:      'FK5     '
    DESCRIPTION:
        Default coordinate system type.  If absent the default value is 'FK5'.


    NAME:         Coordinate.equinox
    KEYWORD:      RADECEQ
    DEFAULT:      2000.
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %g
    UNITS:        'yr'
    COMMENT:      Default coordinate equinox
    EXAMPLE:      2000.0
    DESCRIPTION:
        Default coordinate system equinox.  A value before 1984 is Besselian
        otherwise it is Julian.  If absent the default is J2000.


    NAME:         Coordinate.ra
    KEYWORD:      RA
    DEFAULT:      none
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %h
    UNITS:        RAUNIT
    COMMENT:      Right ascension
    EXAMPLE:      '13:29:24.00'
    DESCRIPTION:
        Default right ascension.


    NAME:         Coordinate.dec
    KEYWORD:      DEC
    DEFAULT:      none
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %h
    UNITS:        DECUNIT
    COMMENT:      Declination
    EXAMPLE:      '47:15:34.00'
    DESCRIPTION:
        Default declination.


    NAME:         Coordinate.raunit
    KEYWORD:      RAUNIT
    DEFAULT:      UNITRA
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Right ascension unit
    EXAMPLE:      'hr'
    DESCRIPTION:
        Default right ascension units.


    NAME:         Coordinate.decunit
    KEYWORD:      DECUNIT
    DEFAULT:      UNITDEC
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Declination unit
    EXAMPLE:      'deg'
    DESCRIPTION:
        Default declination units.


    NAME:         Coordinate.epoch
    KEYWORD:      EPOCH
    DEFAULT:      MJD-OBS
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        'yr'
    COMMENT:      Default coordinate epoch
    EXAMPLE:      1987.257752
    DESCRIPTION:
        Default coordinate epoch.  The default is the epoch of date for the
        observation as derived from the MJD-OBS keyword.


    NAME:         Coordinate.Spec2d.dispval
    KEYWORD:      DISPVAL
    DEFAULT:      none
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %g
    UNITS:        DISPUNIT
    COMMENT:      Dispersion coordinate
    EXAMPLE:      4000.
    DESCRIPTION:
        Default dispersion coordinate value.


    NAME:         Coordinate.Spec2d.crossval
    KEYWORD:      CROSVAL
    DEFAULT:      none
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %g
    UNITS:        CROSUNIT
    COMMENT:      Cross dispersion coordinate
    EXAMPLE:      0.
    DESCRIPTION:
        Default cross dispersion coordinate value.


    NAME:         Coordinate.Spec2d.dispunit
    KEYWORD:      DISPUNIT
    DEFAULT:      'Angstrom'
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Dispersion coordinate unit
    EXAMPLE:      'Angstrom'
    DESCRIPTION:
        Default dispersion coordinate unit.


    NAME:         Coordinate.Spec2d.crossunit
    KEYWORD:      CROSUNIT
    DEFAULT:      'arcsec'
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Declination unit
    EXAMPLE:      'arcsec'
    DESCRIPTION:
        Default cross dispersion unit.


    NAME:         Object[n].name
    KEYWORD:      OBJNAME OBJ%4d
    DEFAULT:      OBJECT OBJNAME
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Target object
    EXAMPLE:      'M 51    '
    DESCRIPTION:
        Standard reference or catalog name for the target astronomical
        object(s).  The name should follow IAU standards.  These keywords
        differ from the OBJECT keyword which is used to identify the
        observation.


    NAME:         Object[n].type
    KEYWORD:      OBJTYPE OBJT%4d
    DEFAULT:      none OBJTYPE
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Type of object
    EXAMPLE:      'galaxy  '
    DESCRIPTION:
        Type of target astronomical object(s).  This is taken from a dictionary
        of names yet to be defined.  Some common types are 'galaxy', 'star',
        and 'sky'.  If not particular object is targeted the type 'field'
        may be used.


    NAME:         Object[n].Coordinate.Equatorial.ra
    KEYWORD:      OBJRA ORA%4d
    DEFAULT:      RA none
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %h
    UNITS:        OBJRAU ORAU%4d
    COMMENT:      Right ascension of object
    EXAMPLE:      '13:29:24.00'
    DESCRIPTION:
        Right ascension of the target astronomical object(s).


    NAME:         Object[n].Coordinate.Equatorial.dec
    KEYWORD:      OBJDEC ODEC%4d
    DEFAULT:      DEC none
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %h
    UNITS:        OBJDECU ODEU%d
    COMMENT:      Declination of object
    EXAMPLE:      '47:15:34.00'
    DESCRIPTION:
        Declination of the target astronomical object(s).


    NAME:         Object[n].Coordinate.Equatorial.raunit
    KEYWORD:      OBJRAU ORAU%4d
    DEFAULT:      RAUNIT OBJRAU
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Right ascension unit
    EXAMPLE:      'h'
    DESCRIPTION:
        Right ascension unit.


    NAME:         Object[n].Coordinate.Equatorial.decunit
    KEYWORD:      OBJDECU ODEU%4d
    DEFAULT:      DECUNIT OBJDECU
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Declination unit
    EXAMPLE:      'deg'
    DESCRIPTION:
        Declination unit.


    NAME:         Object[n].Coordinate.Equatorial.epoch
    KEYWORD:      OBJEPOCH OEPO%4d
    DEFAULT:      EPOCH OBJEPOCH
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %g
    UNITS:        'yr'
    COMMENT:      Epoch of object coordinates
    EXAMPLE:      1950.0
    DESCRIPTION:
        Epoch of the target astronomical object coordinate(s).  This is given
        in years.


    NAME:         Object[n].Coordinate.Equatorial.system
    KEYWORD:      OBJRADEC ORDS%4d
    DEFAULT:      RADECSYS OBJRADEC
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Object coordinate system
    EXAMPLE:      'FK5     '
    DESCRIPTION:
        Coordinate system type for the target astronomical object(s).


    NAME:         Object[n].Coordinate.Equatorial.equinox
    KEYWORD:      OBJEQUIN OEQU%4d
    DEFAULT:      EQUINOX OBJEQUIN
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %g
    UNITS:        'yr'
    COMMENT:      Object coordinate equinox
    EXAMPLE:      2000.0
    DESCRIPTION:
        Coordinate system equinox for the target astronomical object(s).
        A value before 1984 is Besselian otherwise it is Julian.


    NAME:         Site.observatory
    KEYWORD:      OBSERVAT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Observatory
    EXAMPLE:      'KPNO    '
    DESCRIPTION:
        Observatory identification for the site of the observation.


    NAME:         Site.weather
    KEYWORD:      WEATHER
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Weather conditions
    EXAMPLE:      'clear   '
    DESCRIPTION:
        Weather condition description.  Generally this would be either 'clear'
        or 'partly cloudy'.


    NAME:         Site.photometric
    KEYWORD:      PHOTOMET
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Photometric conditions
    EXAMPLE:      'photometric'
    DESCRIPTION:
        Photometric conditions during the observation.


    NAME:         Site.seeing[n]
    KEYWORD:      SEEING SEEING%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITSEP
    COMMENT:      FWHM
    EXAMPLE:      1.30
    DESCRIPTION:
        Seeing estimates specified as the stellar full-width at half-maximum in
        arc seconds.  There may be more than one estimate.  The times of the
        estimates are given by the SEEMJDn keyword.


    NAME:         Site.seeing-mjd[n]
    KEYWORD:      SEEMJD SEEMJD%d
    DEFAULT:      MJDHDR SEEMJD
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      MJD for seeing estimate
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the seeing estimates given as modified Julian dates.


    NAME:         Site.Environment.temperature[n]
    KEYWORD:      ENVTEM ENVTEM%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITTEMP
    COMMENT:      Site temperature
    EXAMPLE:      61.10
    DESCRIPTION:
        Temperatures outside the dome at times given by ENVMJDn keywords.


    NAME:         Site.Environment.pressure[n]
    KEYWORD:      ENVPRE ENVPRE%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITPRES
    COMMENT:      Air pressure
    EXAMPLE:      972.1
    DESCRIPTION:
        Atmospheric pressure measurements at times given by ENVMJDn keywords.


    NAME:         Site.Environment.humidity[n]
    KEYWORD:      ENVHUM ENVHUM%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        '%'
    COMMENT:      Relative humidity
    EXAMPLE:      22.2
    DESCRIPTION:
        Relative humidity measurements at times given by ENVMJDn keywords.


    NAME:         Site.Environment.watervapor[n]
    KEYWORD:      ENVWAT ENVWAT%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        'mm'
    COMMENT:      Precipitable water vapor
    EXAMPLE:      
    DESCRIPTION:
        Precipitable water vapor measurements at times given by ENVMJDn keywords.


    NAME:         Site.Environment.windspeed[n]
    KEYWORD:      ENVWIN ENVWIN%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITVEL
    COMMENT:      Average wind speed
    EXAMPLE:      10.21
    DESCRIPTION:
        Average wind speeds over the sampling period outside the dome at times
        given by ENVMJDn keywords.


    NAME:         Site.Environment.winddir[n]
    KEYWORD:      ENVDIR ENVDIR%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITPA
    COMMENT:      Average wind direction
    EXAMPLE:      84.1
    DESCRIPTION:
        Average wind direction measurements measured east of north over
        the sampling period outside the dome at times given by ENVMJDn keywords.


    NAME:         Site.Environment.windgusts[n]
    KEYWORD:      ENVGUS ENVGUS%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITVEL
    COMMENT:      Maximum gust speed
    EXAMPLE:      18.20
    DESCRIPTION:
        Maximum wind speed in km/s over the sampling period outside the
        dome at times given by ENVMJDn keywords.


    NAME:         Site.Environment.period[n]
    KEYWORD:      ENVPER ENVPER%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITTIME
    COMMENT:      Wind sampling period
    EXAMPLE:      60.0
    DESCRIPTION:
        Wind sampling period for the wind measurements outside the dome at
        times given by ENVMJDn keywords.  If no value is given then the
        measurements are assumed to be 'instantaneous'.


    NAME:         Site.Environment.mjd[n]
    KEYWORD:      ENVMJD ENVMJD%d
    DEFAULT:      MJDHDR ENVMJD
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      Environment measurement time
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the site environment measurements given as modified Julian.
        For the wind measurements this is the start of the sampling period.


    NAME:         Dome.Environment.temperature[n]
    KEYWORD:      DMETEM DMETEM%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITTEMP
    COMMENT:      Dome temperature
    EXAMPLE:      63.4
    DESCRIPTION:
        Temperatures Celsius inside the dome.


    NAME:         Dome.Environment.windspeed[n]
    KEYWORD:      DMEWIN DMEWIN%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITVEL
    COMMENT:      Average dome wind speed
    EXAMPLE:      3.22
    DESCRIPTION:
        Average wind speeds over the sampling period inside the dome.


    NAME:         Dome.Environment.winddir[n]
    KEYWORD:      DMEDIR DMEDIR%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITPA
    COMMENT:      Average wind direction
    EXAMPLE:      
    DESCRIPTION:
        Average wind direction measurements measured east of north over
        the sampling period inside the dome.


    NAME:         Dome.Environment.windgusts[n]
    KEYWORD:      DMEGUS DMEGUS%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITVEL
    COMMENT:      Maximum dome wind speed
    EXAMPLE:      8.20
    DESCRIPTION:
        Maximum wind speed over the sampling period inside the
        dome.


    NAME:         Dome.Environment.period[n]
    KEYWORD:      DMEPER DMEPER%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITTIME
    COMMENT:      Dome wind sampling
    EXAMPLE:      60.0
    DESCRIPTION:
        Wind sampling period for the wind measurements inside the dome.
        If no value is given then the measurements are assumed to be
        'instantaneous'.


    NAME:         Dome.Environment.mjd[n]
    KEYWORD:      DMEMJD DMEMJD%d
    DEFAULT:      MJDHDR DMEMJD
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the dome environment measurements given as modified Julian.
        For the wind measurements this is the start of the sampling period.


    NAME:         Dome.status
    KEYWORD:      DOMSTAT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Dome status
    EXAMPLE:      'open tracking'
    DESCRIPTION:
        Dome status.


    NAME:         Telescope.name
    KEYWORD:      TELESCOP
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Telescope
    EXAMPLE:      '0.9m    '
    DESCRIPTION:
        Telescope used for the observation.


    NAME:         Telescope.config
    KEYWORD:      TELCONF
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Telescope configuration
    EXAMPLE:      'prime   '
    DESCRIPTION:
        Telescope configuration.  The configuration defines the mirrors,
        correctors, light paths, etc.


    NAME:         Telescope.Version.software
    KEYWORD:      TELTCS
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Telescope control system
    EXAMPLE:      'TCS V1.0'
    DESCRIPTION:
        Telescope control system software version.


    NAME:         Telescope.Version.hardware
    KEYWORD:      TELVER
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Telescope version
    EXAMPLE:      'V2.1    '
    DESCRIPTION:
        Telescope hardware version.


    NAME:         Telescope.Coordinate.Equatorial.ra
    KEYWORD:      TELRA
    DEFAULT:      RA
    INDEX:        none
    HDU:          primary
    VALUE:        %h
    UNITS:        TELRAU
    COMMENT:      Telescope right ascension
    EXAMPLE:      '13:29:24.00'
    DESCRIPTION:
        Telescope pointing right ascension.


    NAME:         Telescope.Coordinate.Equatorial.dec
    KEYWORD:      TELDEC
    DEFAULT:      DEC
    INDEX:        none
    HDU:          primary
    VALUE:        %h
    UNITS:        TELDECU
    COMMENT:      Telescope declination
    EXAMPLE:      '47:15:34.00'
    DESCRIPTION:
        Telescope pointing declination.


    NAME:         Telescope.Coordinate.Equatorial.raunit
    KEYWORD:      TELRAU
    DEFAULT:      RAUNIT
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Right ascension unit
    EXAMPLE:      'h'
    DESCRIPTION:
        Right ascension unit.


    NAME:         Telescope.Coordinate.Equatorial.decunit
    KEYWORD:      TELDECU
    DEFAULT:      DECUNIT
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Declination unit
    EXAMPLE:      '47:15:34.00'
    DESCRIPTION:
        Declination unit.


    NAME:         Telescope.Coordinate.Equatorial.epoch
    KEYWORD:      TELEPOCH
    DEFAULT:      EPOCH
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        'yr'
    COMMENT:      Telescope coordinate epoch
    EXAMPLE:      1987.123456
    DESCRIPTION:
        Telescope pointing coordinate epoch.


    NAME:         Telescope.Coordinate.Equatorial.system
    KEYWORD:      TELRADEC
    DEFAULT:      RADECSYS
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Telescope coordinate system
    EXAMPLE:      'FK5     '
    DESCRIPTION:
        Telescope pointing coordinate system type.


    NAME:         Telescope.Coordinate.Equatorial.equinox
    KEYWORD:      TELEQUIN
    DEFAULT:      EQUINOX
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        'yr'
    COMMENT:      Telescope coordinate equinox
    EXAMPLE:      2000.0
    DESCRIPTION:
        Telescope pointing coordinate system equinox.  A value before 1984 is
        Besselian otherwise it is Julian.


    NAME:         Telescope.status
    KEYWORD:      TELSTAT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Telescope status
    EXAMPLE:      'ok     '
    DESCRIPTION:
        Telescope status.


    NAME:         Telescope.focus
    KEYWORD:      TELFOCUS
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        'instrumental'
    COMMENT:      Telescope focus
    EXAMPLE:      1430
    DESCRIPTION:
        Telescope focus value in available units.


    NAME:         Telescope.mjd
    KEYWORD:      TELMJD
    DEFAULT:      MJDHDR
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      Time of zenith distance and hour angle
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Time of zenith distance and hour angle


    NAME:         Telescope.zenith
    KEYWORD:      ZD
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITANG
    COMMENT:      Zenith distance
    EXAMPLE:      42.359
    DESCRIPTION:
        Zenith distance of telescope pointing at TELMJD.


    NAME:         Telescope.hourangle
    KEYWORD:      HA
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %h
    UNITS:        UNITRA
    COMMENT:      Hour angle
    EXAMPLE:      '-01:33:55.21'
    DESCRIPTION:
        Hour angle at TELMJD.


    NAME:         Telescope.ratrackrate
    KEYWORD:      TELTKRA
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITRATE
    COMMENT:      Tracking rate from siderial
    EXAMPLE:      1.2
    DESCRIPTION:
        Right ascension telescope tracking rate from siderial in arc seconds
        per second.


    NAME:         Telescope.dectrackrate
    KEYWORD:      TELTKDEC
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITRATE
    COMMENT:      Tracking rate from siderial
    EXAMPLE:      -3.4
    DESCRIPTION:
        Declination telescope tracking rate in arc seconds per second.


    NAME:         Telescope.Active.Version.hardware
    KEYWORD:      ACTHWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Telescope.Active.Version.software
    KEYWORD:      ACTSWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Telescope.Active.frequency
    KEYWORD:      ACTFREQ
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Telescope.Active.status
    KEYWORD:      ACTSTAT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Telescope.Adc.Version.hardware
    KEYWORD:      ADC
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      ADC Identification
    EXAMPLE:      'Mayall ADC'
    DESCRIPTION:
        Atmospheric dispersion compensator hardware identification.


    NAME:         Telescope.Adc.Version.software
    KEYWORD:      ADCSWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      ADC software version
    EXAMPLE:      
    DESCRIPTION:
        Atmospheric dispersion compensator software identification.


    NAME:         Telescope.Adc.status
    KEYWORD:      ADCSTAT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        'dictionary'
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        ADC status.


    NAME:         Telescope.Adaptive.Version.hardware
    KEYWORD:      ADOHWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Telescope.Adaptive.Version.software
    KEYWORD:      ADOSWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Telescope.Adaptive.type
    KEYWORD:      ADOTYPE
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Adaptive optics object type
    EXAMPLE:      'Na beacon'
    DESCRIPTION:
        Type of object used for wavefront sensing.


    NAME:         Telescope.Adaptive.status
    KEYWORD:      ADOSTAT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Telescope.Adaptive.frequency
    KEYWORD:      ADOFREQ
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Telescope.Adaptive.Coordinate.Equatorial.ra
    KEYWORD:      ADORA
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %h
    UNITS:        ADORAU
    COMMENT:      Adaptive optics object right ascension
    EXAMPLE:      '13:29:24.00'
    DESCRIPTION:
        Object right ascension for wavefront sensing.


    NAME:         Telescope.Adaptive.Coordinate.Equatorial.dec
    KEYWORD:      ADODEC
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %h
    UNITS:        ADODECU
    COMMENT:      Adaptive optics object declination
    EXAMPLE:      '47:15:34.00'
    DESCRIPTION:
        Object declination for wavefront sensing.


    NAME:         Telescope.Adaptive.Coordinate.Equatorial.raunit
    KEYWORD:      ADORAU
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Right ascension unit
    EXAMPLE:      'h'
    DESCRIPTION:
        Right ascension unit.


    NAME:         Telescope.Adaptive.Coordinate.Equatorial.decunit
    KEYWORD:      ADODECU
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Declination unit
    EXAMPLE:      'deg'
    DESCRIPTION:
        Declination unit.


    NAME:         Telescope.Adaptive.Coordinate.Equatorial.epoch
    KEYWORD:      ADOEPOCH
    DEFAULT:      TELEPOCH
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Object coordinate epoch for wavefront sensing.


    NAME:         Telescope.Adaptive.Coordinate.Equatorial.system
    KEYWORD:      ADORADEC
    DEFAULT:      TELRADEC
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Object coordinate system type for wavefront sensing.


    NAME:         Telescope.Adaptive.Coordinate.Equatorial.equinox
    KEYWORD:      ADOEQUIN
    DEFAULT:      TELEQUIN
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Object coordinate system equinox for wavefront sensing.
        A value before 1984 is Besselian otherwise it is Julian.


    NAME:         Telescope.Altaz.altitude
    KEYWORD:      TELALT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %h
    UNITS:        UNITALT
    COMMENT:      Telescope altitude
    EXAMPLE:      '38:59:03.12'
    DESCRIPTION:
        Telescope pointing altitude at the time given by TELAAMJD.


    NAME:         Telescope.Altaz.azimuth
    KEYWORD:      TELAZ
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %h
    UNITS:        UNITAZ
    COMMENT:      Telescope azimuth
    EXAMPLE:      '01:22:15.10'
    DESCRIPTION:
        Telescope pointing azimuth at the time given by TELAAMJD.


    NAME:         Telescope.Altaz.mjd
    KEYWORD:      TELAAMJD
    DEFAULT:      MJDHDR
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      MJD at for alt/az
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Modified Julian date at the time of the altitude/azimuth keywords.


    NAME:         Telescope.Chop.Version.hardware
    KEYWORD:      CHPHWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Telescope.Chop.Version.software
    KEYWORD:      CHPSWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Telescope.Chop.status
    KEYWORD:      CHPSTAT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Telescope.Chop.angle
    KEYWORD:      CHPANGLE
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Telescope.Chop.distance
    KEYWORD:      CHPDIST
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Telescope.Chop.frequency
    KEYWORD:      CHPFREQ
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Telescope.Chop.cycles
    KEYWORD:      CHPNCHOP
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Telescope.Corrector[1]
    KEYWORD:      CORRCTOR
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Corrector Identification
    EXAMPLE:      'doublet '
    DESCRIPTION:
        Correctors in the optical path.


    NAME:         Telescope.Corrector[m]
    KEYWORD:      CORRCT CORRCT%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Corrector
    EXAMPLE:      'doublet '
    DESCRIPTION:
        Correctors in the optical path.


    NAME:         Telescope.Nod.Version.hardware
    KEYWORD:      NODHWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Telescope.Nod.Version.software
    KEYWORD:      NODSWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Telescope.Nod.status
    KEYWORD:      NODSTAT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Telescope.Nod.angle
    KEYWORD:      NODANGLE
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Telescope.Nod.distance
    KEYWORD:      NODDIST
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Telescope.Nod.frequency
    KEYWORD:      NODFREQ
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Telescope.Nod.cycles
    KEYWORD:      NODNCHOP
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Tv[n].name
    KEYWORD:      TV TV%d
    DEFAULT:      none none
    INDEX:        none 1-9
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      TV
    EXAMPLE:      'Mosaic TV #1'
    DESCRIPTION:
        TV name.


    NAME:         Tv[n].status
    KEYWORD:      TVSTAT TV%dSTAT
    DEFAULT:      none none
    INDEX:        none 1-9
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        TV status.


    NAME:         Tv[n].Version.hardware
    KEYWORD:      TVHWV TV%dHWV
    DEFAULT:      none none
    INDEX:        none 1-9
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      TV Hardware
    EXAMPLE:      'RCA 1202 #4'
    DESCRIPTION:
        TV identification and hardware version.


    NAME:         Tv[n].Version.software
    KEYWORD:      TVSWV TV%dSWV
    DEFAULT:      none none
    INDEX:        none 1-9
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        TV software version.


    NAME:         Tv[n].focus[n]
    KEYWORD:      TVFOC%d TV%dFOC%d
    DEFAULT:      none none
    INDEX:        1-9 1-9,1-9
    HDU:          primary
    VALUE:        %g
    UNITS:        'instrumental'
    COMMENT:      Television focus
    EXAMPLE:      1430
    DESCRIPTION:
        Television focus value in available units.


    NAME:         Tv[n].Filter[n].name
    KEYWORD:      TVFILT%d TV%dFILT%d
    DEFAULT:      none none
    INDEX:        1-9 1-9,1-9
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Filter name
    EXAMPLE:      'V       '
    DESCRIPTION:
        TV filter names.  This name is the astronomical standard
        name if applicable; i.e. U, B, Gunn I, etc.  The filter type and filter
        device position are given by other keywords.


    NAME:         Tv[n].Filter[n].type
    KEYWORD:      TVFTYP%d TV%dFTYP%d
    DEFAULT:      none none
    INDEX:        1-9 1-9,1-9
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Filter type
    EXAMPLE:      'KP1408 2x2 6mm DDO Set'
    DESCRIPTION:
        TV filter type.  This is the technical specification or
        observatory identification name.


    NAME:         Tv[n].Filter[n].position
    KEYWORD:      TVFPOS%d TV%dFPOS%d
    DEFAULT:      none none
    INDEX:        1-9 1-9,1-9
    HDU:          primary
    VALUE:        %g
    UNITS:        'instrumental'
    COMMENT:      Filter system position
    EXAMPLE:      5.
    DESCRIPTION:
        TV filter position given as filter wheel number or other filter
        system position measurement.


    NAME:         Guider.name
    KEYWORD:      GUIDER
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Guider name
    EXAMPLE:      'manual  '
    DESCRIPTION:
        Guider name.  Two of the names are 'manual' and 'none' for manual
        guiding or no guider, respectively.


    NAME:         Guider.status
    KEYWORD:      GUISTAT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Guider status.


    NAME:         Guider.Version.hardware
    KEYWORD:      GUIDEHWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Guider identification and hardware version.


    NAME:         Guider.Version.software
    KEYWORD:      GUIDESWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Guider software version.


    NAME:         Guider.Coordinate.Equatorial.ra
    KEYWORD:      GUIRA
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %h
    UNITS:        GUIRAU
    COMMENT:      Guider right ascension
    EXAMPLE:      '13:29:24.00'
    DESCRIPTION:
        Guide object right ascension.


    NAME:         Guider.Coordinate.Equatorial.dec
    KEYWORD:      GUIDEC
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %h
    UNITS:        GUIDECU
    COMMENT:      Guider declination
    EXAMPLE:      '47:15:34.00'
    DESCRIPTION:
        Guide object declination.


    NAME:         Guider.Coordinate.Equatorial.raunit
    KEYWORD:      GUIRAU
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Right ascension unit
    EXAMPLE:      'h'
    DESCRIPTION:
        Right ascension unit.


    NAME:         Guider.Coordinate.Equatorial.decunit
    KEYWORD:      GUIDECU
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Declination unit
    EXAMPLE:      'deg'
    DESCRIPTION:
        Declination unit.


    NAME:         Guider.Coordinate.Equatorial.epoch
    KEYWORD:      GUIEPOCH
    DEFAULT:      TELEPOCH
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Epoch of the guide object coordinates.


    NAME:         Guider.Coordinate.Equatorial.system
    KEYWORD:      GUIRADEC
    DEFAULT:      TELRADEC
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Guide object coordinate system type.


    NAME:         Guider.Coordinate.Equatorial.equinox
    KEYWORD:      GUIEQUIN
    DEFAULT:      TELEQUIN
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Guide object coordinate system equinox.  A value before 1984 is
        Besselian otherwise it is Julian.


    NAME:         Guider.rate
    KEYWORD:      GUIRATE
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITFREQ
    COMMENT:      Guider rate
    EXAMPLE:      10.0
    DESCRIPTION:
        Guider correction rate.


    NAME:         Guider.Tv.name
    KEYWORD:      GTV
    DEFAULT:      TV
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Guider TV
    EXAMPLE:      'TV #1   '
    DESCRIPTION:
        Guider TV name.


    NAME:         Guider.Tv.status
    KEYWORD:      GTVSTAT
    DEFAULT:      TVSTAT
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Guider TV status.


    NAME:         Guider.Tv.Version.hardware
    KEYWORD:      GTVHWV
    DEFAULT:      TVHWV
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      
    EXAMPLE:      'RCA 1202 #4'
    DESCRIPTION:
        Guider TV identification and hardware version.


    NAME:         Guider.Tv.Version.software
    KEYWORD:      GTVSWV
    DEFAULT:      TVSWV
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Guider TV software version.


    NAME:         Guider.Tv.Filter[n].name
    KEYWORD:      GTVFIL GTVFIL%d
    DEFAULT:      TVFILT GTVFIL
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Filter name
    EXAMPLE:      'V       '
    DESCRIPTION:
        Guider TV filter names.  This name is the astronomical standard name if
        applicable; i.e. U, B, Gunn I, etc.  The filter type and filter device
        position are given by other keywords.


    NAME:         Guider.Tv.Filter[n].type
    KEYWORD:      GTVFTY GTVFTY%d
    DEFAULT:      TVFTYP TVFTYP%d
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Filter type
    EXAMPLE:      'KP1408 2x2 6mm DDO Set'
    DESCRIPTION:
        Guider TV filter type.  This is the technical specification or observatory
        identification name.


    NAME:         Guider.Tv.Filter[n].position
    KEYWORD:      GTVFPO GTVFPO%d
    DEFAULT:      TVFPOS TVFPO%d
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        'instrumental'
    COMMENT:      Filter system position
    EXAMPLE:      5.
    DESCRIPTION:
        Guider TV filter position given as filter wheel number or other filter
        system position measurement.


    NAME:         Adapter.Version.hardware
    KEYWORD:      ADAPTER
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Adapter.Version.software
    KEYWORD:      ADAPSWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Adapter.status
    KEYWORD:      ADASTAT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:


    NAME:         Instrument.name
    KEYWORD:      INSTRUME
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Instrument
    EXAMPLE:      'Hydra   '
    DESCRIPTION:
        Instrument name.


    NAME:         Instrument.config
    KEYWORD:      INSTCONF
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Instrument configuration
    EXAMPLE:      'Red Cable'
    DESCRIPTION:
        Instrument configuration.


    NAME:         Instrument.Version.hardware
    KEYWORD:      INSTHWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Instrument hardware version
    EXAMPLE:      'Prototype V0.5'
    DESCRIPTION:
        Instrument hardware version.


    NAME:         Instrument.Version.software
    KEYWORD:      INSTSWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Instrument software version
    EXAMPLE:      'Hydra Control System V2.2'
    DESCRIPTION:
       Instrument software version.


    NAME:         Instrument.status
    KEYWORD:      INSSTAT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Instrument status
    EXAMPLE:      'ok      '
    DESCRIPTION:
       Instrument status.


    NAME:         Instrument.focus
    KEYWORD:      INSFOCUS
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        'instrumental'
    COMMENT:      Instrument focus
    EXAMPLE:      21.3
    DESCRIPTION:
        Instrument focus.


    NAME:         Aperture[n].apertureid
    KEYWORD:      APERTURE APER%4d
    DEFAULT:      none APERTURE
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Aperture identification
    EXAMPLE:      'Mask 12345'
    DESCRIPTION:
       Aperture identification.  This can be a physical aperture identification,
       the name of a mask, a fiber configuration, etc.  When there are many
       apertures the keyword APERTURE may be used to specify a configuration
       or mask identification and the APER%4d keywords can be used to identify
       some information about the aperture such as a fiber number.


    NAME:         Aperture[n].aperturetype
    KEYWORD:      APTYPE APTY%4d
    DEFAULT:      none APTYPE
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %s (dictionary)
    UNITS:        
    COMMENT:      Aperture type
    EXAMPLE:      'hexlens+fiber'
    DESCRIPTION:
       Aperture type.  This is an from a dictionary.  The common types are
       "slit", "hole", "fiber", "hexlens", "hexlens+fiber" and "none".  The
       last type is for aperture-less spectroscopy such as with objective
       prisms.  Typically for multiobject spectroscopy all the aperture types
       will be the same and the keyword will be APTYPE.


    NAME:         Aperture[n].Coordinate.Equatorial.ra
    KEYWORD:      APRA ARA%4d
    DEFAULT:      OBJRA ORA%4d
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %h
    UNITS:        APRAU ARAU%4d
    COMMENT:      Aperture right ascension
    EXAMPLE:      '13:29:24.00'
    DESCRIPTION:
        Right ascension of the aperture(s).


    NAME:         Aperture[n].Coordinate.Equatorial.dec
    KEYWORD:      APDEC ADEC%4d
    DEFAULT:      OBJDEC ODEC%4d
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %h
    UNITS:        APDECU ADEU%4d
    COMMENT:      Aperture declination
    EXAMPLE:      '47:15:34.00'
    DESCRIPTION:
        Declination of the aperture(s).


    NAME:         Aperture[n].Coordinate.Equatorial.raunit
    KEYWORD:      APRAU ARAU%4d
    DEFAULT:      OBJRAU APRAU
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Right ascension unit
    EXAMPLE:      'h'
    DESCRIPTION:
        Right ascension unit.


    NAME:         Aperture[n].Coordinate.Equatorial.decunit
    KEYWORD:      APDECU ADEU%4d
    DEFAULT:      OBJDECU APDECU
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Declination unit
    EXAMPLE:      'deg'
    DESCRIPTION:
        Declination unit.


    NAME:         Aperture[n].Coordinate.Equatorial.epoch
    KEYWORD:      APEPOCH AEPO%4d
    DEFAULT:      OBJEPOCH OEPO%4d
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %g
    UNITS:        'yr'
    COMMENT:      Aperture coordinate epoch
    EXAMPLE:      1950.0
    DESCRIPTION:
        Epoch of the coordinates for the aperture(s).


    NAME:         Aperture[n].Coordinate.Equatorial.system
    KEYWORD:      APRADEC ARDS%4d
    DEFAULT:      OBJRADEC ORDS%4d
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Aperture coordinate system
    EXAMPLE:      'FK5     '
    DESCRIPTION:
        Aperture coordinate system type for the aperture(s).


    NAME:         Aperture[n].Coordinate.Equatorial.equinox
    KEYWORD:      APEQUIN AEQU%4d
    DEFAULT:      OBJEQUIN OEQU%4d
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %g
    UNITS:        'yr'
    COMMENT:      Aperture coordinate equinox
    EXAMPLE:      2000.0
    DESCRIPTION:
        Coordinate system equinox for the aperture(s).  A value before 1984 is
        Besselian otherwise it is Julian.


    NAME:         Aperture[n].diameter
    KEYWORD:      APERDIA APDI%4d
    DEFAULT:      none APERDIA
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %g
    UNITS:        APUNIT
    COMMENT:      Aperture diameter
    EXAMPLE:      1.
    DESCRIPTION:
        Aperture diameter of the aperture(s) for circular apertures and
        fibers.  This is also used as an approximation to the size
        of hexagonal lenses.


    NAME:         Aperture[n].length
    KEYWORD:      APERLEN APLE%4d
    DEFAULT:      none APERLEN
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        APUNIT
    COMMENT:      Slit length
    EXAMPLE:      10.
    DESCRIPTION:
        Aperture length of the aperture(s) for slit apertures.


    NAME:         Aperture[n].width
    KEYWORD:      APERWID APWI%4d
    DEFAULT:      none APERWID
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %g
    UNITS:        APUNIT
    COMMENT:      Slit width
    EXAMPLE:      1.
    DESCRIPTION:
        Aperture width of the aperture(s) for slit apertures.


    NAME:         Aperture[n].apunit
    KEYWORD:      APUNIT APUN%4d
    DEFAULT:      UNITAP APUNIT
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Aperture dimension unit
    EXAMPLE:      'arcsec  '
    DESCRIPTION:
        Units of aperture dimensions.  This applies to slit widths and lengths,
        fiber diameters, lenslet diameters, etc.  It may be a physical dimension
        or a projected angle on the sky.


    NAME:         Aperture[n].posangle
    KEYWORD:      APERPA APPA%4d
    DEFAULT:      none APERPA
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %g
    UNITS:        APPAUNIT
    COMMENT:      Aperture position angle
    EXAMPLE:      90.0
    DESCRIPTION:
        Aperture position angle of the aperture(s) on the sky.  This is
        measured using the longest dimension from north to east for slits.
        For hexagonal lenslets it gives the position angle for one of
        the sides.


    NAME:         Aperture[n].paunit
    KEYWORD:      APPAUNIT APAU%4d
    DEFAULT:      PAUNIT APPAUNIT
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Aperture position angle unit
    EXAMPLE:      'deg    '
    DESCRIPTION:
        Aperture position angle unit.


    NAME:         Aperture[n].fiberid
    KEYWORD:      FIBER FIB%4d
    DEFAULT:      none none
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      
    EXAMPLE:      '7 0 13:29:24.1 47:15:34 Sky'
    DESCRIPTION:
        Fiber identification for the fiber(s).  The string consists of a
        fiber number, an object type number (0=sky, 1=object, etc.), the right
        ascension and declination, and the object name or title.  This can
        replace OBJNAME, APRA/OBJRA, and APDEC/OBJDEC.


    NAME:         Aperture[n].slitid
    KEYWORD:      SLIT SLIT%4d
    DEFAULT:      none none
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      
    EXAMPLE:      '7 0 13:29:24.1 47:15:34 Sky'
    DESCRIPTION:
        Slit or mask hole identification for the aperture(s).  The string
        consists of a number, an object type number (0=sky, 1=object, etc.),
        the right ascension and declination, and the object name or title.
        declination, and the object name or title.  This can replace OBJNAME,
        APRA/OBJRA, and APDEC/OBJDEC.


    NAME:         Aperture[n].Spectrum.Wcs.ctype[1]
    KEYWORD:      CTYPE1 CTY1%4d
    DEFAULT:      'LINEAR' CTYPE1
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Spectrum coordinate type
    EXAMPLE:      'WAVE-WAV'
    DESCRIPTION:
        Spectrum dispersion coordinate type.  These are the FITS defined types.


    NAME:         Aperture[n].Spectrum.Wcs.ctype[2]
    KEYWORD:      CTYPE2 CTY2%4d
    DEFAULT:      'LINEAR' CTYPE2
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Spectrum coordinate type
    EXAMPLE:      'LINEAR  '
    DESCRIPTION:
        Spectrum cross-dispersion coordinate type.  These are the FITS
        defined types.


    NAME:         Aperture[n].Spectrum.Wcs.Coordinate.Spec2d.dispval
    KEYWORD:      CRVAL1 CRV1%4d
    DEFAULT:      none CRVAL1
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        CUNIT1 CUN1%4d
    COMMENT:      Spectrum dispersion center
    EXAMPLE:      5015.0
    DESCRIPTION:
        Spectrum reference dispersion coordinate corresponding to the spectrum
        reference pixel coordinate.  Note that by definition WCS axis 1 is
        always the dispersion axis.  The mapping of this WCS axis to the
        dispersion direction in the image is given by the coordinate
        transformation matrix keywords.  In raw data the reference dispersion
        coordinate may be approximately predicted.  This will be refined during
        data reductions.


    NAME:         Aperture[n].Spectrum.Wcs.Coordinate.Spec2d.crossval
    KEYWORD:      CRVAL2 CRV2%4d
    DEFAULT:      none CRVAL2
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        CUNIT2 CUN2%4d
    COMMENT:      Spectrum cross-dispersion center
    EXAMPLE:      0.
    DESCRIPTION:
        Spectrum reference cross-dispersion coordinate corresponding to the
        spectrum reference pixel coordinate.  Note that by definition WCS axis
        2 is always the cross-dispersion axis.  The mapping of this WCS axis to
        the cross-dispersion direction in the image is given by the coordinate
        transformation matrix keywords.  In raw data the reference
        cross-dispersion coordinate may be approximately predicted.  This will
        be refined during data reductions.


    NAME:         Aperture[n].Spectrum.Wcs.dispunit
    KEYWORD:      CUNIT1 CUN1%4d
    DEFAULT:      DISPUNIT CUNIT1
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Spectrum coordinate unit
    EXAMPLE:      'Angstrom'
    DESCRIPTION:
        Spectrum dispersion coordinate unit.


    NAME:         Aperture[n].Spectrum.Wcs.crossunit
    KEYWORD:      CUNIT2 CUN2%4d
    DEFAULT:      CROSUNIT CUNIT2
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Spectrum coordinate unit
    EXAMPLE:      'arcsec'
    DESCRIPTION:
        Spectrum corss-dispersion coordinate unit.


    NAME:         Aperture[n].Spectrum.Wcs.crpix[1]
    KEYWORD:      CRPIX1 CRP1%4d
    DEFAULT:      none CRPIX1
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        'pixel' 'pixel'
    COMMENT:      Spectrum center
    EXAMPLE:      1024.0
    DESCRIPTION:
        Reference spectrum pixel coordinate.  Generally this should be the at
        the center of the spectrum.  In raw data the spectrum position(s) may be
        predicted apart from an offset that will be determined during data
        reduction.


    NAME:         Aperture[n].Spectrum.Wcs.crpix[2]
    KEYWORD:      CRPIX2 CRP2%4d
    DEFAULT:      none CRPIX2
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        'pixel' 'pixel'
    COMMENT:      Spectrum center
    EXAMPLE:      1024.0
    DESCRIPTION:
        Reference spectrum pixel coordinate.  Generally this should be the at
        the center of the spectrum.  In raw data the spectrum position(s) may be
        predicted apart from an offset that will be determined during data
        reduction.


    NAME:         Aperture[n].Spectrum.Wcsregion.wcsmin[1]
    KEYWORD:      CMIN1 CMN1%4d
    DEFAULT:      none CMIN1
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        CUNIT1 CUN1%4d
    COMMENT:      Spectrum dispersion limit
    EXAMPLE:      3000.0
    DESCRIPTION:
        Dispersion limit for the region occupied by the spectrum.


    NAME:         Aperture[n].Spectrum.Wcsregion.wcsmax[1]
    KEYWORD:      CMAX1 CMX1%4d
    DEFAULT:      none CMAX1
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        CUNIT1 CUN1%4d
    COMMENT:      Spectrum dispersion limit
    EXAMPLE:      6000.0
    DESCRIPTION:
        Dispersion limit for the region occupied by the spectrum.


    NAME:         Aperture[n].Spectrum.Wcsregion.wcsmin[2]
    KEYWORD:      CMIN2 CMN2%4d
    DEFAULT:      none CMIN2
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        CUNIT2 CUN2%4d
    COMMENT:      Spectrum cross-dispersion limit
    EXAMPLE:      -1.0
    DESCRIPTION:
        Cross-dispersion limit for the region occupied by the spectrum.


    NAME:         Aperture[n].Spectrum.Wcsregion.wcsmax[2]
    KEYWORD:      CMAX2 CMX2%4d
    DEFAULT:      none CMAX2
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        CUNIT2 CUN2%4d
    COMMENT:      Spectrum cross-dispersion limit
    EXAMPLE:      1.0
    DESCRIPTION:
        Cross-dispersion limit for the region occupied by the spectrum.


    NAME:         Aperture[n].Spectrum.Wcsregion.pixmin[1]
    KEYWORD:      PMIN1 PMN1%4d
    DEFAULT:      none PMIN1
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        'pixel' 'pixel'
    COMMENT:      Spectrum pixel limit
    EXAMPLE:      3000.0
    DESCRIPTION:
        Pixel limit for region occupied by the spectrum.


    NAME:         Aperture[n].Spectrum.Wcsregion.pixmax[1]
    KEYWORD:      PMAX1 PMX1%d
    DEFAULT:      none PMAX1
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        'pixel' 'pixel'
    COMMENT:      Spectrum pixel limit
    EXAMPLE:      6000.0
    DESCRIPTION:
        Pixel limit for region occupied by the spectrum.


    NAME:         Aperture[n].Spectrum.Wcsregion.pixmin[2]
    KEYWORD:      PMIN2 PMN2%d
    DEFAULT:      none CMIN2
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        'pixel' 'pixel'
    COMMENT:      Spectrum pixel limit
    EXAMPLE:      1.0
    DESCRIPTION:
        Pixel limit for region occupied by the spectrum.


    NAME:         Aperture[n].Spectrum.Wcsregion.pixmax[2]
    KEYWORD:      PMAX2 PMX2%d
    DEFAULT:      none PMAX2
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        'pixel' 'pixel'
    COMMENT:      Spectrum pixel limit
    EXAMPLE:      2048.0
    DESCRIPTION:
        Pixel limit for region occupied by the spectrum.


    NAME:         Aperture[n].Spectrum.Wcs.cd[1,1]
    KEYWORD:      CD1_1 CD11%4d
    DEFAULT:      1. CD1_1
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        CUNIT1/pixel CUN1%4d/pixel
    COMMENT:      Spec coord matrix
    EXAMPLE:      1.0
    DESCRIPTION:
        Spectrum coordinate matrix.  World coordinate axis 1 is defined
        to be the dispersion and the other axes are spatial.  The matrix
        implies a dispersion axis in the image coordinates.


    NAME:         Aperture[n].Spectrum.Wcs.cd[1,2]
    KEYWORD:      CD1_2 CD12%4d
    DEFAULT:      0. CD1_2
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        CUNIT1/pixel CUN1%4d/pixel
    COMMENT:      Spec coord matrix
    EXAMPLE:      0.0
    DESCRIPTION:
        Spectrum coordinate matrix.  World coordinate axis 1 is defined
        to be the dispersion and the other axes are spatial.  The matrix
        implies a dispersion axis in the image coordinates.


    NAME:         Aperture[n].Spectrum.Wcs.cd[2,1]
    KEYWORD:      CD2_1 CD21%4d
    DEFAULT:      0. CD2_1
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        CUNIT2/pixel CUN2%4d/pixel
    COMMENT:      Spec coord matrix
    EXAMPLE:      0.0
    DESCRIPTION:
        Spectrum coordinate matrix.  World coordinate axis 1 is defined
        to be the dispersion and the other axes are spatial.  The matrix
        implies a dispersion axis in the image coordinates.


    NAME:         Aperture[n].Spectrum.Wcs.cd[2,2]
    KEYWORD:      CD2_2 CD22%4d
    DEFAULT:      1. CD2_2
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        CUNIT2/pixel CUN2%4d/pixel
    COMMENT:      Spec coord matrix
    EXAMPLE:      1.0
    DESCRIPTION:
        Spectrum coordinate matrix.  World coordinate axis 1 is defined
        to be the dispersion and the other axes are spatial.  The matrix
        implies a dispersion axis in the image coordinates.


    NAME:         Aperture[n].Spectrum.fwhm
    KEYWORD:      SPECFWHM SWID%4d
    DEFAULT:      none SPECFWHM
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        CUNIT2 CUN2%4d
    COMMENT:      FWHM of spectrum
    EXAMPLE:      1.0
    DESCRIPTION:
        FWHM of the object spectrum profile on the detector.  The width is
        in the units of the spatial world coordinate system.  This may be
        approximate.  It is particularly useful for specifying the profile
        width of fiber fed spectra.


    NAME:         Disperser[n].name
    KEYWORD:      DISPER DISPER%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Disperser
    EXAMPLE:      'RC-181  '
    DESCRIPTION:
        Disperser identification names.


    NAME:         Filter[n].name
    KEYWORD:      FILTER FILTER%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary & extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Filter name
    EXAMPLE:      'V       '
    DESCRIPTION:
        Filter names.  This name is the astronomical standard name if
        applicable; i.e. U, B, Gunn I, etc.  The filter type and filter device
        position are given by other keywords.


    NAME:         Filter[n].type
    KEYWORD:      FILTYP FILTYP%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Filter type
    EXAMPLE:      'KP1408 2x2 6mm DDO Set'
    DESCRIPTION:
        Filter type.  This is the technical specification or observatory
        identification name.


    NAME:         Filter[n].position
    KEYWORD:      FILPOS FILPOS%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        'instrumental'
    COMMENT:      Filter system position
    EXAMPLE:      5.
    DESCRIPTION:
        Filter position given as filter wheel number or other filter system
        position measurement.


    NAME:         Camera.name
    KEYWORD:      CAMERA
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Camera name
    EXAMPLE:      'Simmons'
    DESCRIPTION:
        Camera name.


    NAME:         Camera.config
    KEYWORD:      CAMCONF
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Camera Configuration
    EXAMPLE:      
    DESCRIPTION:
        Camera configuration.


    NAME:         Camera.Version.hardware
    KEYWORD:      CAMHWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Camera version
    EXAMPLE:      'CCD Mosaic V1'
    DESCRIPTION:
        Camera hardware version.


    NAME:         Camera.Version.software
    KEYWORD:      CAMSWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Camera software version
    EXAMPLE:      
    DESCRIPTION:
        Camera software version.


    NAME:         Camera.status
    KEYWORD:      CAMSTAT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Camera status
    EXAMPLE:      'ok     '
    DESCRIPTION:
        Camera status.


    NAME:         Camera.focus
    KEYWORD:      CAMFOCUS
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        'instrumental'
    COMMENT:      Camera focus
    EXAMPLE:      -0.054
    DESCRIPTION:
        Camera focus.


    NAME:         Shutter.Version.hardware
    KEYWORD:      SHUTHWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Shutter hardware version
    EXAMPLE:      'ShutCo Model 7'
    DESCRIPTION:
       Shutter identification and hardware version.


    NAME:         Shutter.Version.software
    KEYWORD:      SHUTSWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Shutter software version
    EXAMPLE:      'ShutCo Model 7'
    DESCRIPTION:
        Shutter software version.


    NAME:         Shutter.status
    KEYWORD:      SHUTSTAT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Shutter status
    EXAMPLE:      'triggered'
    DESCRIPTION:
        Shutter status.


    NAME:         Shutter.open
    KEYWORD:      SHUTOPEN
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        'ms'
    COMMENT:      Shutter open time
    EXAMPLE:      5.0
    DESCRIPTION:
        Time for the shutter to open fully.


    NAME:         Shutter.close
    KEYWORD:      SHUTCLOS
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        'ms'
    COMMENT:      Shutter close time
    EXAMPLE:      2.0
    DESCRIPTION:
        Time for the shutter to close fully.


    NAME:         Detector.name
    KEYWORD:      DETECTOR
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Detector name
    EXAMPLE:      'T2KA    '
    DESCRIPTION:
        Detector name.


    NAME:         Detector.config
    KEYWORD:      DETCONF
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Detector Configuration
    EXAMPLE:      
    DESCRIPTION:
        Detector configuration.


    NAME:         Detector.Version.hardware
    KEYWORD:      DETHWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Detector version
    EXAMPLE:      'CCD Mosaic V1'
    DESCRIPTION:
        Detector hardware version.


    NAME:         Detector.Version.software
    KEYWORD:      DETSWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Detector software version
    EXAMPLE:      
    DESCRIPTION:
        Detector software version.  This will not generally be used and
        the controller software version will apply.


    NAME:         Detector.status
    KEYWORD:      DETSTAT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Detector status
    EXAMPLE:      'ok     '
    DESCRIPTION:
        Detector status.


    NAME:         Detector.size
    KEYWORD:      DETSIZE
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s (section)
    UNITS:        
    COMMENT:      Detector size
    EXAMPLE:      '[1:2048,1:2048]'
    DESCRIPTION:
        The logical unbinned size of the  detector in section notation.  This
        is the full pixel raster size including, if applicable, drift scanning
        or a mosaic format.  This is the full size even when subraster
        readouts are done.


    NAME:         Detector.nccds
    KEYWORD:      NCCDS
    DEFAULT:      1
    INDEX:        none
    HDU:          primary
    VALUE:        %d
    UNITS:        
    COMMENT:      Number of CCDs
    EXAMPLE:      1
    DESCRIPTION:
        The number of CCDs in the detector.  This is used with mosaics of CCD
        detectors.  For a single CCD it may be absent since the default value
        is 1.


    NAME:         Detector.namps
    KEYWORD:      NAMPS
    DEFAULT:      1
    INDEX:        none
    HDU:          primary
    VALUE:        %d
    UNITS:        
    COMMENT:      Number of Amplifiers
    EXAMPLE:      1
    DESCRIPTION:
        The number of amplifiers in the detector.  When there is only a single
        amplifier used it may be absent since the default value is 1.


    NAME:         Detector.Coordinate.Equatorial.ra
    KEYWORD:      DETRA
    DEFAULT:      TELRA
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %h
    UNITS:        DETRAU
    COMMENT:      Detector right ascension
    EXAMPLE:      '13:29:24.00'
    DESCRIPTION:
        Right ascension of the detector center.


    NAME:         Detector.Coordinate.Equatorial.dec
    KEYWORD:      DETDEC
    DEFAULT:      TELDEC
    INDEX:        none
    HDU:          primary
    VALUE:        %h
    UNITS:        DETDECU
    COMMENT:      Detector delination
    EXAMPLE:      '47:15:34.00'
    DESCRIPTION:
        Declination of the detector center.


    NAME:         Detector.Coordinate.Equatorial.raunit
    KEYWORD:      DETRAU
    DEFAULT:      TELRAU
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Right ascension unit
    EXAMPLE:      'h'
    DESCRIPTION:
        Right ascension unit.


    NAME:         Detector.Coordinate.Equatorial.decunit
    KEYWORD:      DETDECU
    DEFAULT:      TELDECU
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Delination unit
    EXAMPLE:      'deg'
    DESCRIPTION:
        Declination unit.


    NAME:         Detector.Coordinate.Equitorial.epoch
    KEYWORD:      DETEPOCH
    DEFAULT:      TELEPOCH
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        'yr'
    COMMENT:      Detector coordinate epoch
    EXAMPLE:      1987.123456
    DESCRIPTION:
        Epoch of the detector center coordinates.


    NAME:         Detector.Coordinate.Equitorial.equinox
    KEYWORD:      DETEQUIN
    DEFAULT:      TELEQUIN
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        'yr'
    COMMENT:      Detector coordinate equinox
    EXAMPLE:      2000.0
    DESCRIPTION:
        Detector coordinate system equinox.  A value before 1984 is Besselian
        otherwise it is Julian.


    NAME:         Detector.Coordinate.Equitorial.system
    KEYWORD:      DETRADEC
    DEFAULT:      TELRADEC
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Detector coordinate system
    EXAMPLE:      'FK5     '
    DESCRIPTION:
        Detector coordinate system type.


    NAME:         Detector.Projection.raposangle
    KEYWORD:      RAPANGL
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITPA
    COMMENT:      Position angle of RA axis
    EXAMPLE:      -90.0
    DESCRIPTION:
        Projected position angle of the positive right ascension axis on the
        detector.  The position angle is measured clockwise from the image y
        axis.


    NAME:         Detector.Projection.decposangle
    KEYWORD:      DECPANGL
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITPA
    COMMENT:      Position angle of Dec axis
    EXAMPLE:      0.0
    DESCRIPTION:
        Projected position angle of the positive declination axis on the
        detector.  The position angle is measured clockwise from the image y
        axis.


    NAME:         Detector.Projection.pixscale[i]
    KEYWORD:      PIXSCAL%d
    DEFAULT:      none
    INDEX:        1-9
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITSEP/pixel
    COMMENT:      Pixel scale
    EXAMPLE:      15.9
    DESCRIPTION:
        Projected pixel scale along axis n.


    NAME:         Detector.Projection.dispaxis
    KEYWORD:      DISPAXIS
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %d
    UNITS:        
    COMMENT:      Dispersion axis
    EXAMPLE:      1
    DESCRIPTION:
        The detector axis along which the dispersion is most closely aligned.


    NAME:         Detector.Projection.wavelength
    KEYWORD:      DISPWC
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        DISPUNIT
    COMMENT:      Central dispersion coordinate
    EXAMPLE:      4250
    DESCRIPTION:
        Approximate central dispersion coordinate on the detector.


    NAME:         Detector.Projection.wdispersion
    KEYWORD:      DISPDW
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %g
    UNITS:        DISPUNIT
    COMMENT:      Dispersion
    EXAMPLE:      4.2
    DESCRIPTION:
        Approximate central dispersion/pixel on the detector.


    NAME:         Detector.Ccd[n].Dewar.name
    KEYWORD:      DEWAR
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Dewar
    EXAMPLE:      'Universal Dewar #2'
    DESCRIPTION:
        Dewar identification.


    NAME:         Detector.Ccd[n].Dewar.Version.hardware
    KEYWORD:      DEWHWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Dewar hardware
    EXAMPLE:      'Universal dewar V2.0'
    DESCRIPTION:
        Dewar hardware version.


    NAME:         Detector.Ccd[n].Dewar.Version.software
    KEYWORD:      DEWSWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Dewar software version
    EXAMPLE:      
    DESCRIPTION:
        Dewar software version.


    NAME:         Detector.Ccd[n].Dewar.status
    KEYWORD:      DEWSTAT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Dewar status
    EXAMPLE:      'ok     '
    DESCRIPTION:
        Dewar status.


    NAME:         Detector.Ccd[n].name
    KEYWORD:      CCDNAME
    DEFAULT:      DETECTOR
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      CCD identification
    EXAMPLE:      'T2KA    '
    DESCRIPTION:
        CCD identification.


    NAME:         Detector.Ccd[n].Version.hardware
    KEYWORD:      CCDHWV
    DEFAULT:      DETHWV
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      CCD version
    EXAMPLE:      'Arcon V1.0'
    DESCRIPTION:
        CCD hardware version


    NAME:         Detector.Ccd[n].Version.software
    KEYWORD:      CCDSWV
    DEFAULT:      DETSWV
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      CCD software version
    EXAMPLE:      'Arcon V1.0'
    DESCRIPTION:
        CCD software version


    NAME:         Detector.Ccd[n].Coordinate.Equitorial.ra
    KEYWORD:      CCDRA
    DEFAULT:      DETRA
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %h
    UNITS:        CCDRAU
    COMMENT:      CCD right ascension
    EXAMPLE:      '13:29:24.00'
    DESCRIPTION:
        Right ascension of the CCD center.


    NAME:         Detector.Ccd[n].Coordinate.Equitorial.dec
    KEYWORD:      CCDDEC
    DEFAULT:      DETDEC
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %h
    UNITS:        CCDDECU
    COMMENT:      CCD declination
    EXAMPLE:      '47:15:34.00'
    DESCRIPTION:
        Declination of the CCD center.


    NAME:         Detector.Ccd[n].Coordinate.Equitorial.raunit
    KEYWORD:      CCDRAU
    DEFAULT:      DETRAU
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Right ascension unit
    EXAMPLE:      'h'
    DESCRIPTION:
        Right ascension unit.


    NAME:         Detector.Ccd[n].Coordinate.Equitorial.decunit
    KEYWORD:      CCDDECU
    DEFAULT:      DETDECU
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Declination unit
    EXAMPLE:      'deg'
    DESCRIPTION:
        Declination unit.


    NAME:         Detector.Ccd[n].Coordinate.Equitorial.epoch
    KEYWORD:      CCDEPOCH
    DEFAULT:      DETEPOCH
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        'yr'
    COMMENT:      CCD coordinate epoch
    EXAMPLE:      1987.123456
    DESCRIPTION:
        Epoch of the CCD center coordinates.


    NAME:         Detector.Ccd[n].Coordinate.Equitorial.system
    KEYWORD:      CCDRADEC
    DEFAULT:      DETRADEC
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      CCD coordinate system
    EXAMPLE:      'FK5     '
    DESCRIPTION:
        CCD coordinate system type.


    NAME:         Detector.Ccd[n].Coordinate.Equitorial.equinox
    KEYWORD:      CCDEQUIN
    DEFAULT:      DETEQUIN
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        'yr'
    COMMENT:      CCD coordinate equinox
    EXAMPLE:      2000.0
    DESCRIPTION:
        CCD coordinate system equinox.  A value before 1984 is Besselian
        otherwise it is Julian.


    NAME:         Detector.Ccd[n].pixsize[i]
    KEYWORD:      PIXSIZE%d
    DEFAULT:      none
    INDEX:        1-9
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        'um'
    COMMENT:      Pixel size
    EXAMPLE:      
    DESCRIPTION:
        Unbinned pixel size along each dimension given in appropriate units.
        The units should be indicated in the comment.  The projected pixel
        size in arc seconds or wavelength are given by other parameters.


    NAME:         Detector.Ccd[n].preflash
    KEYWORD:      PREFLASH
    DEFAULT:      0.
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %g
    UNITS:        UNITTIME
    COMMENT:      Preflash time
    EXAMPLE:      2.0
    DESCRIPTION:
        CCD preflash time.  If the times in the extension are different the primary
        HDU gives one of the extension times.


    NAME:         Detector.Ccd[n].ccdsize
    KEYWORD:      CCDSIZE
    DEFAULT:      DETSIZE
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s (section)
    UNITS:        
    COMMENT:      CCD size
    EXAMPLE:      '[1:2048,1:2048]'
    DESCRIPTION:
        The logical unbinned size of the CCD in section notation.  Normally
        this would be the physical size of the CCD unless drift scanning
        is done.  This is the full size even when subraster readouts are
        done.


    NAME:         Detector.Ccd[n].physical
    KEYWORD:      CCDPSIZE
    DEFAULT:      none
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s (section)
    UNITS:        
    COMMENT:      CCD size
    EXAMPLE:      '[1:2048,1:2048]'
    DESCRIPTION:
       The actual format size of the CCD.  This is the same as the CCDSIZE
       keyword except in the case of drift scanning.


    NAME:         Detector.Ccd[n].namps
    KEYWORD:      CCDNAMPS
    DEFAULT:      1
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %d
    UNITS:        
    COMMENT:      Number of amplifiers used
    EXAMPLE:      1
    DESCRIPTION:
        Number of amplifiers used to readout the CCD.  This keyword may
        be absent if only one amplifier is used.


    NAME:         Detector.Ccd[n].Amp[n].name
    KEYWORD:      AMPNAME
    DEFAULT:      none
    INDEX:        none
    HDU:          extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Amplifier name
    EXAMPLE:      'Amplifier 1'
    DESCRIPTION:
        Amplifier name.


    NAME:         Detector.Ccd[n].Amp[n].size
    KEYWORD:      AMPSIZE
    DEFAULT:      none
    INDEX:        none
    HDU:          extension
    VALUE:        %s (section)
    UNITS:        
    COMMENT:      Amplifier readout size
    EXAMPLE:      '[1:2048,1:2048]'
    DESCRIPTION:
        The logical unbinned size of the amplifier readout in section
        notation.  This includes drift scanning if applicable.


    NAME:         Detector.Ccd[n].Amp[n].section
    KEYWORD:      CCDSEC
    DEFAULT:      CCDSIZE
    INDEX:        none
    HDU:          extension
    VALUE:        %s (section)
    UNITS:        
    COMMENT:      Region of CCD read
    EXAMPLE:      '[1:2048,1:2048]'
    DESCRIPTION:
        The unbinned section of the logical CCD pixel raster covered by the
        amplifier readout in section notation.  The section must map directly
        to the specified data section through the binning and CCD to
        image coordiante transformation.  The image data section (DATASEC)
        is specified with the starting pixel less than the ending pixel.
        Thus the order of this section may be flipped depending on the
        coordinate transformation (which depends on how the CCD coordinate
        system is defined).


    NAME:         Detector.Ccd[n].Amp[n].binning
    KEYWORD:      CCDSUM
    DEFAULT:      '1 1'
    INDEX:        none
    HDU:          extension
    VALUE:        %s (%d or %d %d)
    UNITS:        
    COMMENT:      CCD on-chip summing
    EXAMPLE:      '1 1     '
    DESCRIPTION:
        CCD on-chip summing given as two or four integer numbers.  These define
        the summing of CCD pixels in the amplifier readout order.  The first
        two numbers give the number of pixels summed in the serial and parallel
        directions respectively.  If the first pixel read out consists of fewer
        unbinned pixels along either direction the next two numbers give the
        number of pixels summed for the first serial and parallel pixels.  From
        this it is implicit how many pixels are summed for the last pixels
        given the size of the CCD section (CCDSEC).  It is highly recommended
        that controllers read out all pixels with the same summing in which
        case the size of the CCD section will be the summing factors times the
        size of the data section.


    NAME:         Detector.Ccd[n].Amp[n].biassec[1]
    KEYWORD:      BIASSEC BIAS%4d
    DEFAULT:      none none
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %s (section)
    UNITS:        
    COMMENT:      Bias section
    EXAMPLE:      '[2049:2080,1:2048]'
    DESCRIPTION:
        Section of the recorded image containing overscan or prescan data.  This
        will be in binned pixels if binning is done.  Multiple regions may be
        recorded and specified, such as both prescan and overscan, but the
        first section given by this parameter is likely to be the one used
        during calibration.


    NAME:         Detector.Ccd[n].Amp[n].trimsec
    KEYWORD:      TRIMSEC
    DEFAULT:      DATASEC
    INDEX:        none
    HDU:          extension
    VALUE:        %s (section)
    UNITS:        
    COMMENT:      Section of useful data
    EXAMPLE:      '[1:2048,1:2048]'
    DESCRIPTION:
        Section of the recorded image to be kept after calibration processing.
        This is generally the part of the data section containing useful
        data.  The section is in in binned pixels if binning is done.


    NAME:         Detector.Ccd[n].Amp[n].maxnscan
    KEYWORD:      MAXNSCAN
    DEFAULT:      none
    INDEX:        none
    HDU:          extension
    VALUE:        %d
    UNITS:        
    COMMENT:      Maximum number of scanned lines
    EXAMPLE:      32
    DESCRIPTION:
        The maximum number of scanned (unbinned) lines used to form an output
        line.  This is used with drift scanning or a scan table.  For long
        drift scans this will be the number of lines in the CCD.


    NAME:         Detector.Ccd[n].Amp[n].minnscan
    KEYWORD:      MINNSCAN
    DEFAULT:      MAXNSCAN
    INDEX:        none
    HDU:          extension
    VALUE:        %d
    UNITS:        
    COMMENT:      Minimum number of scanned lines
    EXAMPLE:      1
    DESCRIPTION:
       The minimum number of scanned (unbinned) lines used to form an output
       line.  This is used with drift scanning or a scan table.  This will only
       differ from MAXNSCAN if the initial lines in the output image are from
       the initial ramp-up.


    NAME:         Detector.Ccd[n].Amp[n].Amptrans.tm[i,j]
    KEYWORD:      ATM%d_%d
    DEFAULT:      0.(i!=j),1.(i=j)
    INDEX:        1-9,1-9
    HDU:          extension
    VALUE:        %g
    UNITS:        
    COMMENT:      Amplifier transformation matrix
    EXAMPLE:      1.0
    DESCRIPTION:
        Transformation matrix between CCD and amplifier coordinates.
        Normally only two values will be non-zero and will have values of
        1 or -1.  If missing the default is an identify matrix.


    NAME:         Detector.Ccd[n].Amp[n].Amptrans.tv[i]
    KEYWORD:      ATV%d
    DEFAULT:      0.
    INDEX:        1-9
    HDU:          extension
    VALUE:        %g
    UNITS:        
    COMMENT:      Amplifier transformation vector
    EXAMPLE:      1.0
    DESCRIPTION:
        Transformation origin vector between CCD and amplifier coordinates.


    NAME:         Detector.Ccd[n].Amp[n].Amptrans.section
    KEYWORD:      AMPSEC
    DEFAULT:      none
    INDEX:        none
    HDU:          extension
    VALUE:        %s (section)
    UNITS:        
    COMMENT:      Amplifier section
    EXAMPLE:      '[1:2048,2048:1]'
    DESCRIPTION:
        Mapping of the CCD section to amplifier coordinates.


    NAME:         Detector.Ccd[n].Amp[n].Imagetrans.tm[i,j]
    KEYWORD:      LTM%d_%d
    DEFAULT:      0.(i!=j),1.(i=j)
    INDEX:        1-9,1-9
    HDU:          extension
    VALUE:        %g
    UNITS:        
    COMMENT:      Image transformation matrix
    EXAMPLE:      1.0
    DESCRIPTION:
        Transformation matrix between CCD and image coordinates.
        If missing the default is an identify matrix.


    NAME:         Detector.Ccd[n].Amp[n].Imagetrans.tv[i]
    KEYWORD:      LTV%d
    DEFAULT:      0.
    INDEX:        1-9
    HDU:          extension
    VALUE:        %g
    UNITS:        
    COMMENT:      Image transformation vector
    EXAMPLE:      1.0
    DESCRIPTION:
        Transformation origin vector between CCD and image coordinates.


    NAME:         Detector.Ccd[n].Amp[n].Imagetrans.section
    KEYWORD:      DATASEC
    DEFAULT:      none
    INDEX:        none
    HDU:          extension
    VALUE:        %s (section)
    UNITS:        
    COMMENT:      Image data section
    EXAMPLE:      '[33:2080,1:2048]'
    DESCRIPTION:
        Mapping of the CCD section to image coordinates.


    NAME:         Detector.Ccd[n].Amp[n].Dettrans.tm[i,j]
    KEYWORD:      DTM%d_%d
    DEFAULT:      0.(i!=j),1.(i=j)
    INDEX:        1-9,1-9
    HDU:          extension
    VALUE:        %g
    UNITS:        
    COMMENT:      Detector transformation matrix
    EXAMPLE:      1.0
    DESCRIPTION:
        Transformation matrix between CCD and detector coordinates.
        If missing the default is an identify matrix.


    NAME:         Detector.Ccd[n].Amp[n].Dettrans.tv[i]
    KEYWORD:      DTV%d
    DEFAULT:      0.
    INDEX:        1-9
    HDU:          extension
    VALUE:        %g
    UNITS:        
    COMMENT:      Detector transformation vector
    EXAMPLE:      1.0
    DESCRIPTION:
        Transformation origin vector between CCD and detector coordinates.


    NAME:         Detector.Ccd[n].Amp[n].Dettrans.section
    KEYWORD:      DETSEC
    DEFAULT:      none
    INDEX:        none
    HDU:          extension
    VALUE:        %s (section)
    UNITS:        
    COMMENT:      Detector data section
    EXAMPLE:      '[2049:4096,1:2048]'
    DESCRIPTION:
        Mapping of the CCD section to detector coordinates.


    NAME:         Detector.Ccd[n].Amp[n].Exp.Expstart.date
    KEYWORD:      DATE-OBS
    DEFAULT:      none
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %s (date)
    UNITS:        
    COMMENT:      Date of exposure start
    EXAMPLE:      '05/04/87'
    DESCRIPTION:
        Date of observation in TIMESYS time system at the start of the exposure.
        The format follows the FITS 'date' standard.


    NAME:         Detector.Ccd[n].Amp[n].Exp.Expstart.utc
    KEYWORD:      UTC-OBS
    DEFAULT:      none
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %h
    UNITS:        UNITHOUR
    COMMENT:      UTC of exposure start
    EXAMPLE:      '09:27:27.00'
    DESCRIPTION:
        UTC time at the start of the exposure.


    NAME:         Detector.Ccd[n].Amp[n].Exp.Expstart.mjd
    KEYWORD:      MJD-OBS
    DEFAULT:      none
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      MJD of exposure start
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Modified Julian date at the start of the exposure.  The fractional
        part of the date is given to better than a second of time.


    NAME:         Detector.Ccd[n].Amp[n].Exp.Expstart.lst
    KEYWORD:      LST-OBS
    DEFAULT:      none
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %h
    UNITS:        UNITHOUR
    COMMENT:      LST of exposure start
    EXAMPLE:      '14:53:42.00'
    DESCRIPTION:
        Local siderial time at the start of the exposure.


    NAME:         Detector.Ccd[n].Amp[n].Exp.exptime
    KEYWORD:      EXPTIME
    DEFAULT:      none
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %g
    UNITS:        UNITTIME
    COMMENT:      Exposure time
    EXAMPLE:      600
    DESCRIPTION:
        Total exposure time of the observation.  This is the total time during
        which photons are collected by the detector.  It includes any shutter
        correction.  If the times in the extension are different the primary
        HDU gives one of the extension times.


    NAME:         Detector.Ccd[n].Amp[n].Exp.darktime
    KEYWORD:      DARKTIME
    DEFAULT:      EXPTIME
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %g
    UNITS:        UNITTIME
    COMMENT:      Dark time
    EXAMPLE:      600
    DESCRIPTION:
        Total dark time of the observation.  This is the total time during
        which dark current is collected by the detector.  If the times in the
        extension are different the primary HDU gives one of the extension
        times.


    NAME:         Detector.Ccd[n].Amp[n].Exp.nsubexposures
    KEYWORD:      NSUBEXPS
    DEFAULT:      1
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %d
    UNITS:        
    COMMENT:      Number of subexposures
    EXAMPLE:      10
    DESCRIPTION:
        Number of coadded subexposures.  When charge shuffling this
        gives the number of charge shuffled exposures.


    NAME:         Detector.Ccd[n].Amp[n].Exp.subexptime
    KEYWORD:      SEXP SEXP%4d
    DEFAULT:      none SEXP
    INDEX:        none 1-9999
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        UNITTIME
    COMMENT:      Subexposure time
    EXAMPLE:      10.0
    DESCRIPTION:
        Exposure time of the nth subexposure.  If all subexposures are the
        same length then only the first keyword, SEXP, is needed.
        For charge shuffling the subexposure time is the total time
        for each charge shuffled exposure.  There is no finer division
        of the exposure times.  Comments would be used to describe the
        subexposures of each charge shuffled subexposure.


    NAME:         Detector.Ccd[n].Amp[n].Exp.Expstart.timesys
    KEYWORD:      TSYSOBS
    DEFAULT:      TIMESYS
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Time system for TIME-OBS
    EXAMPLE:      'UTC     '
    DESCRIPTION:
        Time system for the TIME-OBS keyword.


    NAME:         Detector.Ccd[n].Amp[n].Exp.Expstart.time
    KEYWORD:      TIME-OBS
    DEFAULT:      none
    INDEX:        none
    HDU:          primary & extension
    VALUE:        %h
    UNITS:        UNITHOUR
    COMMENT:      Time of observation start
    EXAMPLE:      '09:27:27.00'
    DESCRIPTION:
        Time of exposure start in the TSYSOBS system.


    NAME:         Detector.Ccd[n].Amp[n].Exp.Expend.date
    KEYWORD:      DATEEND
    DEFAULT:      none
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s (date)
    UNITS:        
    COMMENT:      Date at end of exposure
    EXAMPLE:      '05/04/87'
    DESCRIPTION:
        Date at the end of the exposure.  The format follows the FITS standard.


    NAME:         Detector.Ccd[n].Amp[n].Exp.Expend.utc
    KEYWORD:      UTCEND
    DEFAULT:      none
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %h
    UNITS:        UNITHOUR
    COMMENT:      UTC at end of exposure
    EXAMPLE:      '09:27:27.00'
    DESCRIPTION:
        UTC at the end of the exposure.


    NAME:         Detector.Ccd[n].Amp[n].Exp.Expend.mjd
    KEYWORD:      MJDEND
    DEFAULT:      none
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      MJD at end of exposure
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Modified Julian date at the end of the exposure.  The fractional
        part of the date is given to better than a second of time.


    NAME:         Detector.Ccd[n].Amp[n].Exp.Expend.lst
    KEYWORD:      LSTEND
    DEFAULT:      none
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %h
    UNITS:        UNITHOUR
    COMMENT:      LST at end of exposure
    EXAMPLE:      '14:53:42.00'
    DESCRIPTION:
        Local siderial time at the end of the exposure.


    NAME:         Detector.Ccd[n].Amp[n].Exp.Expend.timesys
    KEYWORD:      TSYSEND
    DEFAULT:      TIMESYS
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Time system for TIMEEND
    EXAMPLE:      'UTC     '
    DESCRIPTION:
        Time system for the TIMEEND keyword.


    NAME:         Detector.Ccd[n].Amp[n].Exp.Expend.time
    KEYWORD:      TIMEEND
    DEFAULT:      none
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %h
    UNITS:        UNITHOUR
    COMMENT:      Time of exposure end
    EXAMPLE:      '09:27:27.00'
    DESCRIPTION:
        Time of exposure end in the TSYSEND system.


    NAME:         Detector.Ccd[n].Amp[n].Exp.subutstart
    KEYWORD:      SUT SUT%d
    DEFAULT:      none SUT
    INDEX:        none 1-9999
    HDU:          primary | extension
    VALUE:        %h
    UNITS:        UNITHOUR
    COMMENT:      UTC of subexposure start
    EXAMPLE:      '09:27:27.00'
    DESCRIPTION:
        UTC of the start of each subexposure.


    NAME:         Detector.Ccd[n].Amp[n].Wcs[n].ctype[1]
    KEYWORD:      CTYPE1 CTY1%4d
    DEFAULT:      'LINEAR' CTYPE1
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Coordinate type
    EXAMPLE:      'RA---TAN'
    DESCRIPTION:
        Coordinate type for image world coordinates.  The IRAF WCS standards
        are used (which is generally the FITS standard).


    NAME:         Detector.Ccd[n].Amp[n].Wcs[n].ctype[2]
    KEYWORD:      CTYPE2 CTYP2%4d
    DEFAULT:      'LINEAR' CTYPE2
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Coordinate type
    EXAMPLE:      'DEC--TAN'
    DESCRIPTION:
        Coordinate type for image world coordinates.  The IRAF WCS standards
        are used (which is generally the FITS standard).


    NAME:         Detector.Ccd[n].Amp[n].Wcs[n].Coordinate.Equitorial.ra
    KEYWORD:      CRVAL1 CRV1%4d
    DEFAULT:      0. CRVAL1
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        CUNIT1 CUN1%4d
    COMMENT:      Coordinate reference value
    EXAMPLE:      1.0
    DESCRIPTION:
        Reference right ascension coordinate corresponding to the image
        reference pixel coordinate.  Note that by definition WCS axis 1 is
        always the right ascension axis.  The mapping of this WCS axis to the
        right ascension direction in the image is given by the coordinate
        transformation matrix keywords.  In raw data the reference right
        ascension coordinate may be only approximate.  This will be refined
        during data reductions.


    NAME:         Detector.Ccd[n].Amp[n].Wcs[n].Coordinate.Equitorial.dec
    KEYWORD:      CRVAL2 CRV2%4d
    DEFAULT:      0. CRVAL2
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        CUNIT2 CUN2%4d
    COMMENT:      Coordinate reference value
    EXAMPLE:      1.0
    DESCRIPTION:
        Reference declination coordinate corresponding to the image reference
        pixel coordinate.  Note that by definition WCS axis 1 is always the
        declination axis.  The mapping of this WCS axis to the declination
        direction in the image is given by the coordinate transformation matrix
        keywords.  In raw data the reference right ascension coordinate may be
        only approximate.  This will be refined during data reductions.


    NAME:         Detector.Ccd[n].Amp[n].Wcs[n].Coordinate.Equitorial.raunit
    KEYWORD:      CUNIT1 CUN1%4d
    DEFAULT:      RAUNIT CUNIT1
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Coordinate reference unit
    EXAMPLE:      'deg'
    DESCRIPTION:
        Coordinate reference unit for direct imaging world coordinates.


    NAME:         Detector.Ccd[n].Amp[n].Wcs[n].Coordinate.Equitorial.decunit
    KEYWORD:      CUNIT2 CUN2%4d
    DEFAULT:      DECUNIT CUNIT2
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Coordinate reference unit
    EXAMPLE:      'deg'
    DESCRIPTION:
        Coordinate reference unit for direct imaging world coordinates.


    NAME:         Detector.Ccd[n].Amp[n].Wcs[n].Coordinate.Spec2d.dispval
    KEYWORD:      CRVAL1 CRV1%4d
    DEFAULT:      0. CRVAL1
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        CUNIT1 CUN1%4d
    COMMENT:      Coordinate reference value
    EXAMPLE:      5015.
    DESCRIPTION:
        Spectrum reference dispersion coordinate corresponding to the spectrum
        reference pixel coordinate.  Note that by definition WCS axis 1 is
        always the dispersion axis.  The mapping of this WCS axis to the
        dispersion direction in the image is given by the coordinate
        transformation matrix keywords.  In raw data the reference dispersion
        coordinate may be approximately predicted.  This will be refined during
        data reductions.


    NAME:         Detector.Ccd[n].Amp[n].Wcs[n].Coordinate.Spec2d.crossval
    KEYWORD:      CRVAL2 CRV2%4d
    DEFAULT:      0. CRVAL2
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        CUNIT2 CUN2%4d
    COMMENT:      Coordinate reference value
    EXAMPLE:      0.
    DESCRIPTION:
        Spectrum reference cross-dispersion coordinate corresponding to the
        spectrum reference pixel coordinate.  Note that by definition WCS axis
        2 is always the cross-dispersion axis.  The mapping of this WCS axis to
        the cross-dispersion direction in the image is given by the coordinate
        transformation matrix keywords.  In raw data the reference
        cross-dispersion coordinate may be approximately predicted.  This will
        be refined during data reductions.


    NAME:         Detector.Ccd[n].Amp[n].Wcs.Coordinate.Spec2d.dispunit
    KEYWORD:      CUNIT1 CUN1%4d
    DEFAULT:      DISPUNIT CUNIT1
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Coordinate reference unit
    EXAMPLE:      'Angstroms'
    DESCRIPTION:
        Coordinate reference unit for 2D spectral world coordinates.


    NAME:         Detector.Ccd[n].Amp[n].Wcs[n].Coordinate.Spec2d.crossunit
    KEYWORD:      CUNIT2 CUN2%4d
    DEFAULT:      CROSUNIT CUNIT2
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Coordinate reference unit
    EXAMPLE:      'arcsec'
    DESCRIPTION:
        Coordinate reference unit for 2D spectral world coordinates.


    NAME:         Detector.Ccd[n].Amp[n].Wcs[n].crpix[1]
    KEYWORD:      CRPIX1 CRP1%4d
    DEFAULT:      0. CRPIX1
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        'pixel' 'pixel'
    COMMENT:      Coordinate reference pixel
    EXAMPLE:      1.0
    DESCRIPTION:
        Coordinate reference pixel for image world coordinates.


    NAME:         Detector.Ccd[n].Amp[n].Wcs[n].crpix[2]
    KEYWORD:      CRPIX2 CRP2%4d
    DEFAULT:      0. CRPIX2
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        'pixel' 'pixel'
    COMMENT:      Coordinate reference pixel
    EXAMPLE:      1.0
    DESCRIPTION:
        Coordinate reference pixel for image world coordinates.


    NAME:         Detector.Ccd[n].Amp[n].Wcs[n].cd[1,1]
    KEYWORD:      CD1_1 CD11%4d
    DEFAULT:      1. CD1_1
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        CUNIT1/pixel CUN1%4d/pixel
    COMMENT:      Coordinate scale matrix
    EXAMPLE:      1.0
    DESCRIPTION:
        Coordinate scale matrix for image world coordinates.  This describes
        the scales and rotations of the coordinate axes.


    NAME:         Detector.Ccd[n].Amp[n].Wcs[n].cd[1,2]
    KEYWORD:      CD1_2 CD12%4d
    DEFAULT:      0. CD1_2
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        CUNIT1/pixel CUN1%4d/pixel
    COMMENT:      Coordinate scale matrix
    EXAMPLE:      0.0
    DESCRIPTION:
        Coordinate scale matrix for image world coordinates.  This describes
        the scales and rotations of the coordinate axes.


    NAME:         Detector.Ccd[n].Amp[n].Wcs[n].cd[2,1]
    KEYWORD:      CD2_1 CD21%4d
    DEFAULT:      0. CD2_1
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        CUNIT2/pixel CUN2%4d/pixel
    COMMENT:      Coordinate scale matrix
    EXAMPLE:      0.0
    DESCRIPTION:
        Coordinate scale matrix for image world coordinates.  This describes
        the scales and rotations of the coordinate axes.


    NAME:         Detector.Ccd[n].Amp[n].Wcs[n].cd[2,2]
    KEYWORD:      CD2_2 CD22%4d
    DEFAULT:      1. CD2_2
    INDEX:        none 1-9999
    HDU:          extension
    VALUE:        %g
    UNITS:        CUNIT2/pixel CUN2%4d/pixel
    COMMENT:      Coordinate scale matrix
    EXAMPLE:      1.0
    DESCRIPTION:
        Coordinate scale matrix for image world coordinates.  This describes
        the scales and rotations of the coordinate axes.


    NAME:         Detector.Ccd[n].Amp[n].Wcs[n].wcsastrom
    KEYWORD:      WCSASTRM WCSA%4d
    DEFAULT:      none WCSASTRM
    INDEX:        none 1-9999
    HDU:          primary || extension
    VALUE:        %s
    UNITS:        
    COMMENT:      WCS Source
    EXAMPLE:      'kp4m.19981010T033900 (Tr 37 I Mosaic) by L. Davis 19981010'
    DESCRIPTION:
        Descriptive string identifying the source of the astrometry used to
        derive the WCS.  One example is the exposure used to derive a WCS
        apart from the reference coordinate.


    NAME:         Detector.Ccd[n].Amp[n].Wcs[n].Coordinate.Equitorial.epoch
    KEYWORD:      WCSEPOCH WCSE%4d
    DEFAULT:      CCDEPOCH WCSEPOCH
    INDEX:        none 1-9999
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        'yr' 'yr'
    COMMENT:      WCS coordinate epoch
    EXAMPLE:      1950.0
    DESCRIPTION:
        Epoch of the coordinates used in the world coordinate system.


    NAME:         Detector.Ccd[n].Amp[n].Wcs[n].Coordinate.Equitorial.system
    KEYWORD:      WCSRADEC WCSR%4d
    DEFAULT:      CCDRADEC WCSRADEC
    INDEX:        none 1-9999
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      WCS coordinate system
    EXAMPLE:      'FK5     '
    DESCRIPTION:
        Coordinate system type when equatorial coordinates are used in the
        world coordinate system.


    NAME:         Detector.Ccd[n].Amp[n].Wcs[n].Coordinate.Equitorial.equinox
    KEYWORD:      EQUINOX WCSE%4d
    DEFAULT:      CCDEQUIN EQUINOX
    INDEX:        none 1-9999
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        'yr' 'yr'
    COMMENT:      WCS coordinate equinox 
    EXAMPLE:      2000.0
    DESCRIPTION:
        Equinox when equatorial coordinates are used in the world coordinate
        system.  A value before 1984 is Besselian otherwise it is Julian.


    NAME:         Detector.Ccd[n].Amp[n].Wcs[n].Irafwcs.wcsdim
    KEYWORD:      WCSDIM
    DEFAULT:      none
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %d
    UNITS:        
    COMMENT:      WCS dimensionality
    EXAMPLE:      2
    DESCRIPTION:
        Dimensionality of the WCS physical system.  In IRAF a WCS can have
        a higher dimensionality than the image.


    NAME:         Detector.Ccd[n].Amp[n].Wcs[n].Irafwcs.wat[0,n]
    KEYWORD:      WAT_%3d
    DEFAULT:      none
    INDEX:        1-999
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      
    EXAMPLE:      'wtype=tnx axtype=ra lngcor = "1. 4. 4. 2. 0.001356153522318032 0.296'
    DESCRIPTION:
        IRAF WCS attribute strings for all axes.  These are defined by the
        IRAF WCS system.


    NAME:         Detector.Ccd[n].Amp[n].Wcs[n].Irafwcs.wat[i,n]
    KEYWORD:      WAT%d_%3d
    DEFAULT:      none
    INDEX:        1-9,1-999
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      
    EXAMPLE:      'wtype=tnx axtype=ra lngcor = "1. 4. 4. 2. 0.001356153522318032 0.296'
    DESCRIPTION:
        IRAF WCS attribute strings.  These are defined by the IRAF WCS system.


    NAME:         Detector.Ccd[n].Amp[n].Controller.name
    KEYWORD:      CONTROLR
    DEFAULT:      none
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Detector controller
    EXAMPLE:      'Arcon V1.0'
    DESCRIPTION:
        Detector controller name.


    NAME:         Detector.Ccd[n].Amp[n].Controller.hardware
    KEYWORD:      CONHWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Controller hardware version
    EXAMPLE:      'Arcon V1.0'
    DESCRIPTION:
        Controller hardware version.


    NAME:         Detector.Ccd[n].Amp[n].Controller.software
    KEYWORD:      CONSWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Controller software version
    EXAMPLE:      'Arcon V1.0'
    DESCRIPTION:
        Controller software version.


    NAME:         Detector.Ccd[n].Amp[n].Controller.status
    KEYWORD:      CONSTAT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Controller status
    EXAMPLE:      'ok     '
    DESCRIPTION:
        Controller status.


    NAME:         Detector.Ccd[n].Amp[n].Controller.gain
    KEYWORD:      GAIN
    DEFAULT:      none
    INDEX:        none
    HDU:          extension
    VALUE:        %g
    UNITS:        'e/count'
    COMMENT:      Amplifier gain
    EXAMPLE:      4.3
    DESCRIPTION:
        Amplifier gain in electrons per analog unit.  This is the most current
        estimate of the gain.


    NAME:         Detector.Ccd[n].Amp[n].Controller.readnoise
    KEYWORD:      RDNOISE
    DEFAULT:      none
    INDEX:        none
    HDU:          extension
    VALUE:        %g
    UNITS:        'e'
    COMMENT:      Readout noise
    EXAMPLE:      12.
    DESCRIPTION:
        CCD readout noise in rms electrons.  This is the most current estimate.


    NAME:         Detector.Ccd[n].Amp[n].Controller.saturate
    KEYWORD:      SATURATE
    DEFAULT:      none
    INDEX:        none
    HDU:          extension
    VALUE:        %g
    UNITS:        'counts'
    COMMENT:      Saturation value
    EXAMPLE:      65000.
    DESCRIPTION:
        Pixel value above which the detector is saturated.


    NAME:         Detector.Ccd[n].Amp[n].Controller.integration
    KEYWORD:      AMPINTEG
    DEFAULT:      none
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        'ns'
    COMMENT:      Amplifier integration/sample time
    EXAMPLE:      15000.
    DESCRIPTION:
        Amplifier integration or sample time.


    NAME:         Detector.Ccd[n].Amp[n].Controller.readtime
    KEYWORD:      AMPREAD READTIME
    DEFAULT:      none none
    INDEX:        none none
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        'ns'
    COMMENT:      Unbinned pixel read time
    EXAMPLE:      39480
    DESCRIPTION:
        Amplifier unbinned pixel read time.


    NAME:         Detector.Ccd[n].Amp[n].Controller.sample
    KEYWORD:      AMPSAMPL
    DEFAULT:      none
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Amplifier sampling method
    EXAMPLE:      'fast mode - dual correlated sampling'
    DESCRIPTION:
        CCD amplifier sampling method used.  This may also include any
        integration times.


    NAME:         Detector.Ccd[n].Amp[n].Controller.Arcon.gain
    KEYWORD:      ARCONG
    DEFAULT:      none
    INDEX:        none
    HDU:          extension
    VALUE:        %g
    UNITS:        'e/counts'
    COMMENT:      Predicted gain
    EXAMPLE:      3.2
    DESCRIPTION:
        Arcon predicted gain.  This is the gain measured in the laboratory.
        The GAIN keyword may also have this value initially but it is
        updated to the most recent estimate of the gain.


    NAME:         Detector.Ccd[n].Amp[n].Controller.Arcon.gainindex
    KEYWORD:      ARCONGI
    DEFAULT:      none
    INDEX:        none
    HDU:          extension
    VALUE:        %d
    UNITS:        
    COMMENT:      Gain selection
    EXAMPLE:      2
    DESCRIPTION:
        Arcon gain index value.


    NAME:         Detector.Ccd[n].Amp[n].Controller.Arcon.readnoise
    KEYWORD:      ARCONRN
    DEFAULT:      none
    INDEX:        none
    HDU:          extension
    VALUE:        %g
    UNITS:        'e'
    COMMENT:      Predicted readout noise
    EXAMPLE:      3.9
    DESCRIPTION:
        Arcon predicted RMS readout noise.  This is the value measured in
        the laboratory.  The RDNOISE keyword may also have this value initially
        but it is updated to the most current estimate.


    NAME:         Detector.Ccd[n].Amp[n].Controller.Arcon.wavedate
    KEYWORD:      ARCONWD
    DEFAULT:      none
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Date CCD waveforms last compiled
    EXAMPLE:      'Obs Fri Nov 10 22:50:17 1995'
    DESCRIPTION:
        Arcon waveform complilation date.


    NAME:         Detector.Ccd[n].Amp[n].Controller.Arcon.wavemode
    KEYWORD:      ARCONWM
    DEFAULT:      none
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Arcon waveform options enabled
    EXAMPLE:      'OverlapXmit EarlyReset'
    DESCRIPTION:
        Arcon waveform options enabled.


    NAME:         Detector.Ccd[n].Badpixels.badpixels
    KEYWORD:      BPM
    DEFAULT:      none
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Bad pixels
    EXAMPLE:      'mosccd7.pl'
    DESCRIPTION:
        Description of bad pixels.  The value is an IRAF bad pixel mask name.


    NAME:         Observer.name[n]
    KEYWORD:      OBSERVER OBSRVR%2d
    DEFAULT:      none none
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Observer(s)
    EXAMPLE:      'G. Jacoby, D. Tody, F. Valdes'
    DESCRIPTION:
        Name(s) of the observers.


    NAME:         Observer.proposer[n]
    KEYWORD:      PROPOSER PROPSR%2d
    DEFAULT:      none none
    INDEX:        none 1-9999
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Proposer(s)
    EXAMPLE:      'G. Jacoby, D. Tody, F. Valdes'
    DESCRIPTION:
        Name(s) of the proposers.


    NAME:         Observer.proposal
    KEYWORD:      PROPOSAL
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Proposal title
    EXAMPLE:      'Search for primeval galaxies'
    DESCRIPTION:
        The name or title of the proposal.


    NAME:         Observer.proposalid
    KEYWORD:      PROPID
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Proposal identification
    EXAMPLE:      'KPNO 12345'
    DESCRIPTION:
        The unique observatory proposal identification.


    NAME:         Observer.comment[n]
    KEYWORD:      CMMT%4d
    DEFAULT:      none
    INDEX:        1-9999
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      
    EXAMPLE:      Filter may be in wrong position
    DESCRIPTION:
        Observer comments.


    NAME:         Processing.status
    KEYWORD:      PROCSTAT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Processing status
    EXAMPLE:      'unprocessed'
    DESCRIPTION:
        Processing status.


    NAME:         Processing.pipeline.name
    KEYWORD:      PIPELINE
    DEFAULT:      none
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Pipeline used
    EXAMPLE:      'standard'
    DESCRIPTION:
        Name of processing pipeline applied.


    NAME:         Processing.pipeline.Version.hardware
    KEYWORD:      PIPEHW PIPEHW%d
    DEFAULT:      none none
    INDEX:        none 1-99
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Processing hardware
    EXAMPLE:      'Mosaic system V1'
    DESCRIPTION:
        Processing hardware used.


    NAME:         Processing.pipeline.Version.software
    KEYWORD:      PIPESW PIPESW%d
    DEFAULT:      none none
    INDEX:        none 1-99
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      Processing software
    EXAMPLE:      'IRAF V2.11'
    DESCRIPTION:
        Processing software version.


    NAME:         Processing.log[n]
    KEYWORD:      PROC%4d
    DEFAULT:      none
    INDEX:        1-9999
    HDU:          primary | extension
    VALUE:        %s
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Processing log information formatted as FITS comments.


    NAME:         Processing.photoncal
    KEYWORD:      PHOTCAL
    DEFAULT:      F
    INDEX:        none
    HDU:          primary | extension
    VALUE:        %b
    UNITS:        
    COMMENT:      Data proportional to photons?
    EXAMPLE:      T
    DESCRIPTION:
        Status of calibration to data proportional to photons.  For CCD data
        this means bias section correction, zero level calibration, dark count
        calibration, and flat field calibration.


    NAME:         Archive.name
    KEYWORD:      ARCHIVE
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Archive
    EXAMPLE:      'KPNO STB'
    DESCRIPTION:
        The archive name in which the observation is archived.


    NAME:         Archive.status
    KEYWORD:      ARCHSTAT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Archive status
    EXAMPLE:      'archived'
    DESCRIPTION:
        Archive status of data.


    NAME:         Archive.Version.hardware
    KEYWORD:      ARCHHWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Archive hardware
    EXAMPLE:      'Exabyte EXB8500'
    DESCRIPTION:
        Archive hardware version.


    NAME:         Archive.Version.software
    KEYWORD:      ARCHSWV
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Archive software version
    EXAMPLE:      'STB Version 2'
    DESCRIPTION:
        Archive software version.


    NAME:         Archive.archiveid
    KEYWORD:      ARCHID RECNO
    DEFAULT:      OBSID none
    INDEX:        none none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Archive identification
    EXAMPLE:      'STB 870405 231'
    DESCRIPTION:
        Archive identification.  This may be the same as the observation
        identification.


    NAME:         Archive.dictionary
    KEYWORD:      KWDICT
    DEFAULT:      none
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Keyword dictionary
    EXAMPLE:      'NOAO FITS Keyword Dictionary (Aug97)'
    DESCRIPTION:
        The keyword dictionary defining the keywords.  This dictionary
        should be archived with the data.


    NAME:         Units.ra
    KEYWORD:      UNITRA
    DEFAULT:      'hr'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Right ascension unit
    EXAMPLE:      'hr'
    DESCRIPTION:
        Right ascension unit.


    NAME:         Units.dec
    KEYWORD:      UNITDEC
    DEFAULT:      'deg'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Declination unit
    EXAMPLE:      'deg'
    DESCRIPTION:
        Delination unit.


    NAME:         Units.longitude
    KEYWORD:      UNITLONG
    DEFAULT:      UNITANG
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Longitude unit
    EXAMPLE:      'deg'
    DESCRIPTION:
        Longitude unit.


    NAME:         Units.latitude
    KEYWORD:      UNITLAT
    DEFAULT:      UNITANG
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Latitude unit
    EXAMPLE:      'deg'
    DESCRIPTION:
        Latitude unit.


    NAME:         Units.azimuth
    KEYWORD:      UNITAZ
    DEFAULT:      UNITANG
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Azimuth unit
    EXAMPLE:      'deg'
    DESCRIPTION:
        Azimuth unit.


    NAME:         Units.altitude
    KEYWORD:      UNITALT
    DEFAULT:      UNITANG
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Altitude unit
    EXAMPLE:      'deg'
    DESCRIPTION:
        Altitude unit.


    NAME:         Units.aperture
    KEYWORD:      UNITAP
    DEFAULT:      'arcsec'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Aperture size unit
    EXAMPLE:      'arcsec'
    DESCRIPTION:
        Focal plane aperture size unit.


    NAME:         Units.sepangle
    KEYWORD:      UNITSEP
    DEFAULT:      'arcsec'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Separation unit
    EXAMPLE:      'arcsec'
    DESCRIPTION:
        Celestial separation unit.


    NAME:         Units.posangle
    KEYWORD:      UNITPA
    DEFAULT:      UNITANG
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Position angle unit
    EXAMPLE:      'deg'
    DESCRIPTION:
        Position angle unit.


    NAME:         Units.timeofday
    KEYWORD:      UNITHOUR
    DEFAULT:      h
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Time of day unit
    EXAMPLE:      'h'
    DESCRIPTION:
        Time of day unit.


    NAME:         Units.rate
    KEYWORD:      UNITRATE
    DEFAULT:      arcsec/sec
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Celestial rate of motion
    EXAMPLE:      'arcsec/sec'
    DESCRIPTION:
        Celestial rate of motion.


    NAME:         Units.length
    KEYWORD:      UNITLEN
    DEFAULT:      'm'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Length unit
    EXAMPLE:      'm'
    DESCRIPTION:
        Length unit.  A wavelength unit is also provided so this unit is
        primarily used to instrumental descriptions.


    NAME:         Units.time
    KEYWORD:      UNITTIME
    DEFAULT:      's'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Time unit
    EXAMPLE:      's'
    DESCRIPTION:
        Time unit.


    NAME:         Units.velocity
    KEYWORD:      UNITVEL
    DEFAULT:      'km/s'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Velocity unit
    EXAMPLE:      'km/s'
    DESCRIPTION:
        Velocity unit.


    NAME:         Units.mass
    KEYWORD:      UNITMASS
    DEFAULT:      'kg'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Mass unit
    EXAMPLE:      'solMass'
    DESCRIPTION:
        Mass unit.


    NAME:         Units.angle
    KEYWORD:      UNITANG
    DEFAULT:      'deg'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Plane angle unit
    EXAMPLE:      'deg'
    DESCRIPTION:
        Plane angle unit.


    NAME:         Units.sangle
    KEYWORD:      UNITSANG
    DEFAULT:      'sr'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Solid angle unit
    EXAMPLE:      'arcsec^(2)'
    DESCRIPTION:
        Solid angle unit.


    NAME:         Units.temperature
    KEYWORD:      UNITTEMP
    DEFAULT:      'K'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Temperature unit
    EXAMPLE:      'K'
    DESCRIPTION:
        Temperature unit.


    NAME:         Units.current
    KEYWORD:      UNITCUR
    DEFAULT:      'A'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Current unit
    EXAMPLE:      'mA'
    DESCRIPTION:
        Current unit.


    NAME:         Units.lumintensity
    KEYWORD:      UNITLINT
    DEFAULT:      'cd'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Luminous intensity unit
    EXAMPLE:      'cd'
    DESCRIPTION:
        Luminous intensity unit.


    NAME:         Units.frequency
    KEYWORD:      UNITFREQ
    DEFAULT:      'Hz'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Frequency unit
    EXAMPLE:      'GHz'
    DESCRIPTION:
        Frequency unit.


    NAME:         Units.energy
    KEYWORD:      UNITENER
    DEFAULT:      'J'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Energy unit
    EXAMPLE:      'eV'
    DESCRIPTION:
        Energy unit.


    NAME:         Units.power
    KEYWORD:      UNITPOW
    DEFAULT:      'W'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Wavelength unit
    EXAMPLE:      'mW'
    DESCRIPTION:
        Power unit.


    NAME:         Units.volt
    KEYWORD:      UNITVOLT
    DEFAULT:      'V'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Voltage unit
    EXAMPLE:      'mV'
    DESCRIPTION:
        Voltage unit.


    NAME:         Units.force
    KEYWORD:      UNITFORC
    DEFAULT:      'N'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Force unit
    EXAMPLE:      'N'
    DESCRIPTION:
        Force unit.


    NAME:         Units.pressure
    KEYWORD:      UNITPRES
    DEFAULT:      'Pa'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Pressure unit
    EXAMPLE:      'mPa'
    DESCRIPTION:
        Pressure unit.


    NAME:         Units.charge
    KEYWORD:      UNITCHAR
    DEFAULT:      'C'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Charge unit
    EXAMPLE:      'uC'
    DESCRIPTION:
        Charge unit.


    NAME:         Units.resistance
    KEYWORD:      UNITRES
    DEFAULT:      'Ohm'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Resistance unit
    EXAMPLE:      'KOhm'
    DESCRIPTION:
        Resistance unit.


    NAME:         Units.conductance
    KEYWORD:      UNITCOND
    DEFAULT:      'S'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Conductance unit
    EXAMPLE:      'S'
    DESCRIPTION:
        Conductance unit.


    NAME:         Units.capacitance
    KEYWORD:      UNITCAP
    DEFAULT:      'F'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Capacitance unit
    EXAMPLE:      'pF'
    DESCRIPTION:
        Capacitance unit.


    NAME:         Units.magflux
    KEYWORD:      UNITMFLX
    DEFAULT:      'Wb'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Magnetic flux unit
    EXAMPLE:      'Wb'
    DESCRIPTION:
        Magnetic flux unit.


    NAME:         Units.magdensity
    KEYWORD:      UNITMDEN
    DEFAULT:      'T'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Magnetic density unit
    EXAMPLE:      'T'
    DESCRIPTION:
        Magnetic density unit.


    NAME:         Units.inductance
    KEYWORD:      UNITINDU
    DEFAULT:      'H'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Inductance unit
    EXAMPLE:      'H'
    DESCRIPTION:
        Inductance unit.


    NAME:         Units.lumflux
    KEYWORD:      UNITLFLX
    DEFAULT:      'lm'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Luminous flux unit
    EXAMPLE:      'lm'
    DESCRIPTION:
        Luminous flux unit.


    NAME:         Units.illuminance
    KEYWORD:      UNITILLU
    DEFAULT:      'lux'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Illuminance unit
    EXAMPLE:      'lux'
    DESCRIPTION:
        Illuminance unit.


    NAME:         Units.event
    KEYWORD:      UNITEVNT
    DEFAULT:      'count'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Event unit
    EXAMPLE:      'photon'
    DESCRIPTION:
        Event unit.


    NAME:         Units.flux
    KEYWORD:      UNITFLUX
    DEFAULT:      'Jy'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Flux unit
    EXAMPLE:      'mag'
    DESCRIPTION:
        Flux unit.


    NAME:         Units.magnetic
    KEYWORD:      UNITMFLD
    DEFAULT:      'G'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Magnetic field unit
    EXAMPLE:      'mG'
    DESCRIPTION:
        Magnetic field unit.


    NAME:         Units.area
    KEYWORD:      UNITAREA
    DEFAULT:      'pixel'
    INDEX:        none
    HDU:          primary
    VALUE:        %s
    UNITS:        
    COMMENT:      Area unit
    EXAMPLE:      'cm^(2)'
    DESCRIPTION:
        Area unit.


    NAME:         Observation.Obstype.Lamp.Sensors.temperature[n]
    KEYWORD:      LMPTEM LMPTEM%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITTEMP
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Calibration lamp temperature sensor measurements in degrees Celsius.


    NAME:         Observation.Obstype.Lamp.Sensors.voltage[n]
    KEYWORD:      LMPVOL LMPVOL%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITVOLT
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Calibration lamp voltage sensor measurements in volts.


    NAME:         Observation.Obstype.Lamp.Sensors.position[n]
    KEYWORD:      LMPPOS LMPPOS%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Calibration lamp linear position sensor measurements in appropriate units.


    NAME:         Observation.Obstype.Lamp.Sensors.pressure[n]
    KEYWORD:      LMPPRE LMPPRE%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITPRES
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Calibration lamp pressure sensor measurements in appropriate units.


    NAME:         Observation.Obstype.Lamp.Sensors.posangle[n]
    KEYWORD:      LMPPAN LMPPAN%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Calibration lamp position angle measurements in appropriate units.


    NAME:         Observation.Obstype.Lamp.Sensors.mjd[n]
    KEYWORD:      LMPMJD LMPMJD%d
    DEFAULT:      MJD-OBS LMPMJD
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the lamp sensor measurements given as modified Julian dates.
        The MJDHDR keyword may be used for the time at
        which the image header is created or the MJD-OBS keyword may be used
        for the time of observation.


    NAME:         Dome.Sensors.temperature[n]
    KEYWORD:      DOMTEM DOMTEM%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITTEMP
    COMMENT:      Dome temperature
    EXAMPLE:      62.1
    DESCRIPTION:
        Dome temperature sensor measurements in degrees Celsius.


    NAME:         Dome.Sensors.voltage[n]
    KEYWORD:      DOMVOL DOMVOL%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITVOLT
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Dome voltage sensor measurements in volts.


    NAME:         Dome.Sensors.position[n]
    KEYWORD:      DOMPOS DOMPOS%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Dome linear position sensor measurements in appropriate units.


    NAME:         Dome.Sensors.pressure[n]
    KEYWORD:      DOMPRE DOMPRE%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITPRES
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Dome pressure sensor measurements in appropriate units.


    NAME:         Dome.Sensors.posangle[n]
    KEYWORD:      DOMPAN DOMPAN%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Dome position angle sensor measurements.  This should be in
        degrees east of north for the center of the dome slit.


    NAME:         Dome.Sensors.mjd[n]
    KEYWORD:      DOMMJD DOMMJD%d
    DEFAULT:      MJDHDR DOMMJD
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the dome sensor measurements given as modified Julian dates.


    NAME:         Telescope.Sensors.mjd[n]
    KEYWORD:      TELMJD TELMJD%d
    DEFAULT:      MJDHDR TELMJD
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the telescope sensor measurements given as modified Julian dates.


    NAME:         Telescope.Sensors.posangle[n]
    KEYWORD:      TELPAN TELPAN%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Telescope position angle measurements in appropriate units.
        This could include altitude and azimuth measurements.


    NAME:         Telescope.Sensors.position[n]
    KEYWORD:      TELPOS TELPOS%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Telescope linear position sensor measurements in appropriate units.


    NAME:         Telescope.Sensors.pressure[n]
    KEYWORD:      TELPRE TELPRE%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITPRES
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Telescope pressure sensor measurements in appropriate units.


    NAME:         Telescope.Sensors.temperature[n]
    KEYWORD:      TELTEM TELTEM%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITTEMP
    COMMENT:      Telescope temperature
    EXAMPLE:      63.2
    DESCRIPTION:
        Telescope temperature sensor measurements in degrees Celsius.
        The comment string may be modified to indicate the location of
        the measurement.


    NAME:         Telescope.Sensors.voltage[n]
    KEYWORD:      TELVOL TELVOL%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITVOLT
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Telescope voltage sensor measurements in volts.


    NAME:         Telescope.Active.Sensors.mjd[n]
    KEYWORD:      ACTMJD ACTMJD%d
    DEFAULT:      MJDHDR ACTMJD
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the active optics sensor measurements given as modified
        Julian dates.


    NAME:         Telescope.Active.Sensors.posangle[n]
    KEYWORD:      ACTPAN ACTPAN%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Active optics system position angle measurements in appropriate units.


    NAME:         Telescope.Active.Sensors.position[n]
    KEYWORD:      ACTPOS ACTPOS%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Active optics system linear position sensor measurements in appropriate
        units.


    NAME:         Telescope.Active.Sensors.pressure[n]
    KEYWORD:      ACTPRE ACTPRE%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITPRES
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Active optics system pressure sensor measurements in appropriate units.


    NAME:         Telescope.Active.Sensors.temperature[n]
    KEYWORD:      ACTTEM ACTTEM%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITTEMP
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Active optics system temperature sensor measurements in degrees Celsius.


    NAME:         Telescope.Active.Sensors.voltage[n]
    KEYWORD:      ACTVOL ACTVOL%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITVOLT
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Active optics voltage sensor measurements in volts.


    NAME:         Telescope.Adc.Sensors.mjd[n]
    KEYWORD:      ADCMJD ADCMJD%d
    DEFAULT:      MJDHDR ADCMJD
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the ADC sensor measurements given as modified Julian dates.


    NAME:         Telescope.Adc.Sensors.posangle[n]
    KEYWORD:      ADCPAN ADCPAN%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        ADC position angle measurements in appropriate units.


    NAME:         Telescope.Adc.Sensors.position[n]
    KEYWORD:      ADCPOS ADCPOS%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        ADC linear position sensor measurements in appropriate units.


    NAME:         Telescope.Adc.Sensors.pressure[n]
    KEYWORD:      ADCPRE ADCPRE%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITPRES
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        ADC pressure sensor measurements in appropriate units.


    NAME:         Telescope.Adc.Sensors.temperature[n]
    KEYWORD:      ADCTEM ADCTEM%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITTEMP
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        ADC temperature sensor measurements in degrees Celsius.


    NAME:         Telescope.Adc.Sensors.voltage[n]
    KEYWORD:      ADCVOL ADCVOL%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITVOLT
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        ADC voltage sensor measurements in volts.


    NAME:         Telescope.Adaptive.Sensors.mjd[n]
    KEYWORD:      ADOMJD ADOMJD%d
    DEFAULT:      MJDHDR ADOMJD
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the adaptive optics sensor measurements given as modified
        Julian dates.


    NAME:         Telescope.Adaptive.Sensors.posangle[n]
    KEYWORD:      ADOPAN ADOPAN%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Adaptive optics system position angle measurements in appropriate units.


    NAME:         Telescope.Adaptive.Sensors.position[n]
    KEYWORD:      ADOPOS ADOPOS%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Adaptive optics system linear position sensor measurements in
        appropriate units.


    NAME:         Telescope.Adaptive.Sensors.pressure[n]
    KEYWORD:      ADOPRE ADOPRE%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITPRES
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Adaptive optics system pressure sensor measurements in appropriate units.


    NAME:         Telescope.Adaptive.Sensors.temperature[n]
    KEYWORD:      ADOTEM ADOTEM%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITTEMP
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Adaptive optics system temperature sensor measurements in degrees Celsius.


    NAME:         Telescope.Adaptive.Sensors.voltage[n]
    KEYWORD:      ADOVOL ADOVOL%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITVOLT
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Adaptive optics system voltage sensor measurements in volts.


    NAME:         Telescope.Chop.Sensors.mjd[n]
    KEYWORD:      CHPMJD CHPMJD%d
    DEFAULT:      MJDHDR CHPMJD
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the chopping system sensor measurements given as modified
        Julian dates.


    NAME:         Telescope.Chop.Sensors.posangle[n]
    KEYWORD:      CHPPAN CHPPAN%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Chopping system position angle measurements in appropriate units.
        Note that CHPANGLE should be used for the chopping angle and these
        keywords are for other system position angle measurements.


    NAME:         Telescope.Chop.Sensors.position[n]
    KEYWORD:      CHPPOS CHPPOS%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Chopping system linear position sensor measurements in appropriate units.


    NAME:         Telescope.Chop.Sensors.pressure[n]
    KEYWORD:      CHPPRE CHPPRE%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITPRES
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Chopping system pressure sensor measurements in appropriate units.


    NAME:         Telescope.Chop.Sensors.temperature[n]
    KEYWORD:      CHPTEM CHPTEM%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITTEMP
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Chopping system temperature sensor measurements in degrees Celsius.


    NAME:         Telescope.Chop.Sensors.voltage[n]
    KEYWORD:      CHPVOL CHPVOL%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITVOLT
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Chopping system voltage sensor measurements in volts.


    NAME:         Telescope.Nod.Sensors.mjd[n]
    KEYWORD:      NODMJD NODMJD%d
    DEFAULT:      MJDHDR NODMJD
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the nodding system sensor measurements given as modified
        Julian dates.


    NAME:         Telescope.Nod.Sensors.posangle[n]
    KEYWORD:      NODPAN NODPAN%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Nodding position angle measurements in appropriate units.
        Note that NODANGLE should be used for the nodding angle and these
        keywords are for other system position angle measurements.


    NAME:         Telescope.Nod.Sensors.position[n]
    KEYWORD:      NODPOS NODPOS%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Nodding system linear position sensor measurements in appropriate units.


    NAME:         Telescope.Nod.Sensors.pressure[n]
    KEYWORD:      NODPRE NODPRE%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITPRES
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Nodding system pressure sensor measurements in appropriate units.


    NAME:         Telescope.Nod.Sensors.temperature[n]
    KEYWORD:      NODTEM NODTEM%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITTEMP
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Nodding system temperature sensor measurements in degrees Celsius.


    NAME:         Telescope.Nod.Sensors.voltage[n]
    KEYWORD:      NODVOL NODVOL%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITVOLT
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Nodding system voltage sensor measurements in volts.


    NAME:         Tv[n].Sensors.temperature[n]
    KEYWORD:      TVTEM%d TV%dTEM%d
    DEFAULT:      none none
    INDEX:        1-9 1-9,1-9
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITTEMP
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Television temperature sensor measurements in degrees Celsius.


    NAME:         Tv[n].Sensors.voltage[n]
    KEYWORD:      TVVOL%d TV%dVOL%d
    DEFAULT:      none none
    INDEX:        1-9 1-9,1-9
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITVOLT
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Television voltage sensor measurements in volts.


    NAME:         Tv[n].Sensors.position[n]
    KEYWORD:      TVPOS%d TV%dPOS%d
    DEFAULT:      none none
    INDEX:        1-9 1-9,1-9
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      Television position ()
    EXAMPLE:      12.312 -3.121
    DESCRIPTION:
        Television linear position sensor measurements in
        appropriate units.


    NAME:         Tv[n].Sensors.pressure[n]
    KEYWORD:      TVPRE%d TV%dPRE%d
    DEFAULT:      none none
    INDEX:        1-9 1-9,1-9
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITPRES
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Television pressure sensor measurements in appropriate units.


    NAME:         Tv[n].Sensors.posangle[n]
    KEYWORD:      TVPAN%d TV%dPAN%d
    DEFAULT:      none none
    INDEX:        1-9 1-9,1-9
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Television position angle measurements in appropriate units.


    NAME:         Tv[n].Sensors.mjd[n]
    KEYWORD:      TVMJD%d TV%dMJD%d
    DEFAULT:      MJDHDR TVMJD%d
    INDEX:        1-9 1-9,1-9
    HDU:          primary
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the guider television sensor measurements given as modified
        Julian dates.


    NAME:         Guider.Sensors.mjd[n]
    KEYWORD:      GUIMJD GUIMJD%d
    DEFAULT:      MJDHDR GUIMJD
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the guider sensor measurements given as modified Julian dates.


    NAME:         Guider.Sensors.posangle[n]
    KEYWORD:      GUIPAN GUIPAN%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Guider position angle measurements in appropriate units.


    NAME:         Guider.Sensors.position[n]
    KEYWORD:      GUIPOS GUIPOS%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      Guider position ()
    EXAMPLE:      12.312 -3.121
    DESCRIPTION:
        Guider linear position sensor measurements in appropriate units.
        This might be used for guide probe positions.


    NAME:         Guider.Sensors.pressure[n]
    KEYWORD:      GUIPRE GUIPRE%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITPRES
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Guider pressure sensor measurements in appropriate units.


    NAME:         Guider.Sensors.temperature[n]
    KEYWORD:      GUITEM GUITEM%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITTEMP
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Guider temperature sensor measurements in degrees Celsius.


    NAME:         Guider.Sensors.voltage[n]
    KEYWORD:      GUIVOL GUIVOL%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITVOLT
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Guider voltage sensor measurements in volts.


    NAME:         Guider.Tv.Sensors.temperature[n]
    KEYWORD:      GTVTEM GTVTEM%d
    DEFAULT:      TVTEMP TVTEMP%d
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITTEMP
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Guider television temperature sensor measurements in degrees Celsius.


    NAME:         Guider.Tv.Sensors.voltage[n]
    KEYWORD:      GTVVOL GTVVOL%d
    DEFAULT:      TVVOL TVVOL%d
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITVOLT
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Guider television voltage sensor measurements in volts.


    NAME:         Guider.Tv.Sensors.position[n]
    KEYWORD:      GTVPOS GTVPOS%d
    DEFAULT:      TVPOS TVPOS%d
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      Television position ()
    EXAMPLE:      12.312 -3.121
    DESCRIPTION:
        Guider television linear position sensor measurements in appropriate units.


    NAME:         Guider.Tv.Sensors.pressure[n]
    KEYWORD:      GTVPRE GTVPRE%d
    DEFAULT:      TVPRE TVPRE%d
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITPRES
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Guider television pressure sensor measurements in appropriate units.


    NAME:         Guider.Tv.Sensors.posangle[n]
    KEYWORD:      GTVPAN GTVPAN%d
    DEFAULT:      TVPAN TVPAN%d
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Guider television position angle measurements in appropriate units.


    NAME:         Guider.Tv.Sensors.mjd[n]
    KEYWORD:      GTVMJD GTVMJD%d
    DEFAULT:      TVMJD%d none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the guider television sensor measurements given as modified
        Julian dates.


    NAME:         Adapter.Sensors.temperature[n]
    KEYWORD:      ADATEM ADATEM%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITTEMP
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Adapter temperature sensor measurements in degrees Celsius.


    NAME:         Adapter.Sensors.voltage[n]
    KEYWORD:      ADAVOL ADAVOL%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITVOLT
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Adapter voltage sensor measurements in volts.


    NAME:         Adapter.Sensors.position[n]
    KEYWORD:      ADAPOS ADAPOS%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Adapter linear position sensor measurements in appropriate units.


    NAME:         Adapter.Sensors.pressure[n]
    KEYWORD:      ADAPRE ADAPRE%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITPRES
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Adapter pressure sensor measurements in appropriate units.


    NAME:         Adapter.Sensors.posangle[n]
    KEYWORD:      ADAPAN ADAPAN%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Adapter position angle measurements in appropriate units.


    NAME:         Adapter.Sensors.mjd[n]
    KEYWORD:      ADAMJD ADAMJD%d
    DEFAULT:      MJDHDR ADAMJD
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the adapter sensor measurements given as modified Julian dates.


    NAME:         Instrument.Sensors.temperature[n]
    KEYWORD:      INSTEM INSTEM%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITTEMP
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Instrument temperature sensor measurements in degrees Celsius.


    NAME:         Instrument.Sensors.voltage[n]
    KEYWORD:      INSVOL INSVOL%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITVOLT
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Instrument voltage sensor measurements in volts.


    NAME:         Instrument.Sensors.position[n]
    KEYWORD:      INSPOS INSPOS%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Instrument linear position sensor measurements in appropriate units.


    NAME:         Instrument.Sensors.pressure[n]
    KEYWORD:      INSPRE INSPRE%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITPRES
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Instrument pressure sensor measurements in appropriate units.


    NAME:         Instrument.Sensors.posangle[n]
    KEYWORD:      INSPAN INSPAN%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Instrument position angle measurements in appropriate units.


    NAME:         Instrument.Sensors.mjd[n]
    KEYWORD:      INSMJD INSMJD%d
    DEFAULT:      MJD-OBS INSMJD
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the instrument sensor measurements given as modified Julian dates.


    NAME:         Disperser[n].Sensors.mjd
    KEYWORD:      DISMJD DISMJD%d
    DEFAULT:      MJD-OBS DISMJD
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the disperser sensor measurements given as modified Julian dates.


    NAME:         Disperser[n].Sensors.posangle
    KEYWORD:      DISPAN DISPAN%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Disperser position angle measurements in appropriate units.


    NAME:         Disperser[n].Sensors.position
    KEYWORD:      DISPOS DISPOS%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Disperser linear position sensor measurements in appropriate units.


    NAME:         Disperser[n].Sensors.pressure
    KEYWORD:      DISPRE DISPRE%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITPRES
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Disperser pressure sensor measurements in appropriate units.


    NAME:         Disperser[n].Sensors.temperature
    KEYWORD:      DISTEM DISTEM%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITTEMP
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Disperser temperature sensor measurements in degrees Celsius.


    NAME:         Disperser[n].Sensors.voltage
    KEYWORD:      DISVOL DISVOL%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITVOLT
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Disperser voltage sensor measurements in volts.


    NAME:         Camera.Sensors.mjd[n]
    KEYWORD:      CAMMJD CAMMJD%d
    DEFAULT:      MJD-OBS CAMMJD
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the instrument sensor measurements given as modified Julian dates.


    NAME:         Camera.Sensors.posangle[n]
    KEYWORD:      CAMPAN CAMPAN%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITANG
    COMMENT:      Camera position angle
    EXAMPLE:      30.0
    DESCRIPTION:
        Camera position angle measurements in appropriate units.


    NAME:         Camera.Sensors.position[n]
    KEYWORD:      CAMPOS CAMPOS%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Camera linear position sensor measurements in appropriate units.


    NAME:         Camera.Sensors.pressure[n]
    KEYWORD:      CAMPRE CAMPRE%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITPRES
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Camera pressure sensor measurements in appropriate units.


    NAME:         Camera.Sensors.temperature[n]
    KEYWORD:      CAMTEM CAMTEM%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITTEMP
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Camera temperature sensor measurements in degrees Celsius.


    NAME:         Camera.Sensors.voltage[n]
    KEYWORD:      CAMVOL CAMVOL%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITVOLT
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Camera voltage sensor measurements in volts.


    NAME:         Detector.Sensors.mjd[n]
    KEYWORD:      DETMJD DETMJD%d
    DEFAULT:      MJD-OBS DETMJD
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the detector sensor measurements given as modified Julian
        dates.  The MJDHDR keyword may be used for the time at which the image
        header is created or the MJD-OBS keyword may be used for the time of
        observation.


    NAME:         Detector.Sensors.posangle[n]
    KEYWORD:      DETPAN DETPAN%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Detector position angle measurements in appropriate units.


    NAME:         Detector.Sensors.position[n]
    KEYWORD:      DETPOS DETPOS%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Detector linear position sensor measurements in appropriate units.


    NAME:         Detector.Sensors.pressure[n]
    KEYWORD:      DETPRE DETPRE%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITPRES
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Detector pressure sensor measurements in appropriate units.


    NAME:         Detector.Sensors.temperature[n]
    KEYWORD:      DETTEM DETTEM%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITTEMP
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Detector temperature sensor measurements in degrees Celsius.


    NAME:         Detector.Sensors.voltage[n]
    KEYWORD:      DETVOL DETVOL%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary
    VALUE:        %g
    UNITS:        UNITVOLT
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Detector voltage sensor measurements in volts.


    NAME:         Detector.Ccd[n].Dewar.Sensors.mjd[n]
    KEYWORD:      DEWMJD DEWMJD%d
    DEFAULT:      MJD-OBS DEWMJD
    INDEX:        none 1-999
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the dewar sensor measurements given as modified Julian
        dates.  The MJDHDR keyword may be used for the time at which the image
        header is created or the MJD-OBS keyword may be used for the time of
        observation.


    NAME:         Detector.Ccd[n].Dewar.Sensors.posangle[n]
    KEYWORD:      DEWPAN DEWPAN%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Dewar position angle measurements in appropriate units.


    NAME:         Detector.Ccd[n].Dewar.Sensors.position[n]
    KEYWORD:      DEWPOS DEWPOS%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Dewar linear position sensor measurements in appropriate units.


    NAME:         Detector.Ccd[n].Dewar.Sensors.pressure[n]
    KEYWORD:      DEWPRE DEWPRE%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        UNITPRES
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Dewar pressure sensor measurements in appropriate units.


    NAME:         Detector.Ccd[n].Dewar.Sensors.temperature[n]
    KEYWORD:      DEWTEM DEWTEM%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        UNITTEMP
    COMMENT:      Dewar temperature
    EXAMPLE:      -91.
    DESCRIPTION:
        Dewar temperature sensor measurements in degrees Celsius.


    NAME:         Detector.Ccd[n].Dewar.Sensors.voltage[n]
    KEYWORD:      DEWVOL DEWVOL%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        UNITVOLT
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        Dewar voltage sensor measurements in volts.


    NAME:         Detector.Ccd[n].Sensors.mjd[n]
    KEYWORD:      CCDMJD CCDMJD%d
    DEFAULT:      MJDHDR CCDMJD
    INDEX:        none 1-999
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the CCD sensor measurements given as modified Julian dates.
        The MJDHDR keyword may be used for the time at which the image header
        is created or the MJD-OBS keyword may be used for the time of
        observation.


    NAME:         Detector.Ccd[n].Sensors.posangle[n]
    KEYWORD:      CCDPAN CCDPAN%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        CCD position angle measurements in appropriate units.


    NAME:         Detector.Ccd[n].Sensors.position[n]
    KEYWORD:      CCDPOS CCDPOS%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        CCD linear position sensor measurements in appropriate units.


    NAME:         Detector.Ccd[n].Sensors.pressure[n]
    KEYWORD:      CCDPRE CCDPRE%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        UNITPRES
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        CCD pressure sensor measurements in appropriate units.


    NAME:         Detector.Ccd[n].Sensors.temperature[n]
    KEYWORD:      CCDTEM CCDTEM%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        UNITTEMP
    COMMENT:      CCD temperature
    EXAMPLE:      -104.
    DESCRIPTION:
        CCD temperature sensor measurements in degrees Celsius.


    NAME:         Detector.Ccd[n].Sensors.voltage[n]
    KEYWORD:      CCDVOL CCDVOL%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        UNITVOLT
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        CCD voltage sensor measurements in volts.


    NAME:         Detector.Ccd[n].Amp[n].Controller.Sensors.mjd[n]
    KEYWORD:      AMPMJD AMPMJD%d
    DEFAULT:      MJD-OBS AMPMJD
    INDEX:        none 1-999
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        'd'
    COMMENT:      
    EXAMPLE:      46890.394063
    DESCRIPTION:
        Times for the amplifier sensor measurements given as modified Julian
        dates.  The MJDHDR keyword may be used for the time at which the image
        header is created or the MJD-OBS keyword may be used for the time of
        observation.


    NAME:         Detector.Ccd[n].Amp[n].Controller.Sensors.posangle[n]
    KEYWORD:      AMPPAN AMPPAN%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        CCD amplifier position angle measurements in appropriate units.


    NAME:         Detector.Ccd[n].Amp[n].Controller.Sensors.position[n]
    KEYWORD:      AMPPOS AMPPOS%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        CCD amplifier linear position sensor measurements in appropriate units.


    NAME:         Detector.Ccd[n].Amp[n].Controller.Sensors.pressure[n]
    KEYWORD:      AMPPRE AMPPRE%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        UNITPRES
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        CCD amplifier pressure sensor measurements in appropriate units.


    NAME:         Detector.Ccd[n].Amp[n].Controller.Sensors.temperature[n]
    KEYWORD:      AMPTEM AMPTEM%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        UNITTEMP
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        CCD amplifier temperature sensor measurements in degrees Celsius.


    NAME:         Detector.Ccd[n].Amp[n].Controller.Sensors.voltage[n]
    KEYWORD:      AMPVOL AMPVOL%d
    DEFAULT:      none none
    INDEX:        none 1-999
    HDU:          primary | extension
    VALUE:        %g
    UNITS:        UNITVOLT
    COMMENT:      
    EXAMPLE:      
    DESCRIPTION:
        CCD amplifier voltage sensor measurements in volts.


}
