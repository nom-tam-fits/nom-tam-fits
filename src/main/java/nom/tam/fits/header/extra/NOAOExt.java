package nom.tam.fits.header.extra;

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

import nom.tam.fits.header.FitsHeaderImpl;
import nom.tam.fits.header.IFitsHeader;

/**
 * This keyword dictionary defines keywords which may be used in image data
 * recorded by the data acquisition system. It currently does not consider
 * keywords for data processing. Most of the keywords defined here will not be
 * used. New parameters must be added to the logical class heirarchy and then a
 * keyword defined in this dictionary before use in image data.
 * 
 * <pre>
 *  @see <a href="http://iraf.noao.edu/iraf/web/projects/ccdmosaic/imagedef/fitsdic.html">http://iraf.noao.edu/iraf/web/projects/ccdmosaic/imagedef/fitsdic.html</a>
 * </pre>
 * 
 * @author Richard van Nieuwenhoven.
 */
public enum NOAOExt implements IFitsHeader {
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ACTFREQ(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ACTHWV(HDU.PRIMARY, VALUE.STRING, ""),
    /**
     * Times for the active optics sensor measurements given as modified Julian
     * dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR ACTMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ACTMJD(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Times for the active optics sensor measurements given as modified Julian
     * dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR ACTMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ACTMJDn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Active optics system position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ACTPAN(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Active optics system position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ACTPANn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Active optics system linear position sensor measurements in appropriate
     * units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ACTPOS(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Active optics system linear position sensor measurements in appropriate
     * units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ACTPOSn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Active optics system pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ACTPRE(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Active optics system pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ACTPREn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ACTSTAT(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ACTSWV(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * Active optics system temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ACTTEM(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Active optics system temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ACTTEMn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Active optics voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ACTVOL(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Active optics voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ACTVOLn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Times for the adapter sensor measurements given as modified Julian dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR ADAMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADAMJD(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Times for the adapter sensor measurements given as modified Julian dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR ADAMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADAMJDn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Adapter position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADAPAN(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Adapter position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADAPANn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Adapter linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADAPOS(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Adapter linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADAPOSn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Adapter pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADAPRE(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Adapter pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADAPREn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ADAPSWV(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ADAPTER(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ADASTAT(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * Adapter temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADATEM(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Adapter temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADATEMn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Adapter voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADAVOL(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Adapter voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADAVOLn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Atmospheric dispersion compensator hardware identification.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ADC(HDU.PRIMARY, VALUE.STRING, "ADC Identification"),
    /**
     * Times for the ADC sensor measurements given as modified Julian dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR ADCMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADCMJD(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Times for the ADC sensor measurements given as modified Julian dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR ADCMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADCMJDn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * ADC position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADCPAN(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * ADC position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADCPANn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * ADC linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADCPOS(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * ADC linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADCPOSn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * ADC pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADCPRE(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * ADC pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADCPREn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * ADC status.
     * <p>
     * units = 'dictionary'
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ADCSTAT(HDU.PRIMARY, VALUE.STRING, ""),
    /**
     * Atmospheric dispersion compensator software identification.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ADCSWV(HDU.PRIMARY, VALUE.STRING, "ADC software version"),
    /**
     * ADC temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADCTEM(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * ADC temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADCTEMn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * ADC voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADCVOL(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * ADC voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADCVOLn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Declination of the aperture(s).
     * <p>
     * units = APDECU ADEU%4d
     * </p>
     * <p>
     * default value = OBJDEC ODEC%4d
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    ADECnnn(HDU.PRIMARY, VALUE.STRING, "Aperture declination"),
    /**
     * Declination unit.
     * <p>
     * default value = OBJDECU APDECU
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    ADEUnnn(HDU.PRIMARY, VALUE.STRING, "Declination unit"),
    /**
     * Object declination for wavefront sensing.
     * <p>
     * units = ADODECU
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ADODEC(HDU.PRIMARY, VALUE.STRING, "Adaptive optics object declination"),
    /**
     * Declination unit.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ADODECU(HDU.PRIMARY, VALUE.STRING, "Declination unit"),
    /**
     * Object coordinate epoch for wavefront sensing.
     * <p>
     * default value = TELEPOCH
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ADOEPOCH(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * Object coordinate system equinox for wavefront sensing. A value before
     * 1984 is Besselian otherwise it is Julian.
     * <p>
     * default value = TELEQUIN
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ADOEQUIN(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ADOFREQ(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ADOHWV(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * Times for the adaptive optics sensor measurements given as modified
     * Julian dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR ADOMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADOMJD(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Times for the adaptive optics sensor measurements given as modified
     * Julian dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR ADOMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADOMJDn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Adaptive optics system position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADOPAN(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Adaptive optics system position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADOPANn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Adaptive optics system linear position sensor measurements in appropriate
     * units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADOPOS(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Adaptive optics system linear position sensor measurements in appropriate
     * units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADOPOSn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Adaptive optics system pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADOPRE(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Adaptive optics system pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADOPREn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Object right ascension for wavefront sensing.
     * <p>
     * units = ADORAU
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ADORA(HDU.PRIMARY, VALUE.STRING, "Adaptive optics object right ascension"),
    /**
     * Object coordinate system type for wavefront sensing.
     * <p>
     * default value = TELRADEC
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ADORADEC(HDU.PRIMARY, VALUE.STRING, ""),
    /**
     * Right ascension unit.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ADORAU(HDU.PRIMARY, VALUE.STRING, "Right ascension unit"),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ADOSTAT(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ADOSWV(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * Adaptive optics system temperature sensor measurements in degrees
     * Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADOTEM(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Adaptive optics system temperature sensor measurements in degrees
     * Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADOTEMn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Type of object used for wavefront sensing.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ADOTYPE(HDU.PRIMARY, VALUE.STRING, "Adaptive optics object type"),
    /**
     * Adaptive optics system voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADOVOL(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Adaptive optics system voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ADOVOLn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Epoch of the coordinates for the aperture(s).
     * <p>
     * units = 'yr'
     * </p>
     * <p>
     * default value = OBJEPOCH OEPO%4d
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    AEPOnnn(HDU.PRIMARY, VALUE.REAL, "Aperture coordinate epoch"),
    /**
     * Coordinate system equinox for the aperture(s). A value before 1984 is
     * Besselian otherwise it is Julian.
     * <p>
     * units = 'yr'
     * </p>
     * <p>
     * default value = OBJEQUIN OEQU%4d
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    AEQUnnn(HDU.PRIMARY, VALUE.REAL, "Aperture coordinate equinox"),
    /**
     * The computed airmass(es) at the time(s) given by the AMMJDn keywords.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-9
     * </p>
     */
    AIRMASSn(HDU.PRIMARY, VALUE.REAL, "Airmass"),
    /**
     * Times for the airmass calculation given as modified Julian dates. The
     * MJDHDR keyword may be used for the time at which the image header is
     * created or the MJD-OBS keyword may be used for the time of observation.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR AMMJD
     * </p>
     * <p>
     * index = none 1-9
     * </p>
     */
    AMMJD(HDU.PRIMARY, VALUE.REAL, "MJD of airmass"),
    /**
     * Times for the airmass calculation given as modified Julian dates. The
     * MJDHDR keyword may be used for the time at which the image header is
     * created or the MJD-OBS keyword may be used for the time of observation.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR AMMJD
     * </p>
     * <p>
     * index = none 1-9
     * </p>
     */
    AMMJDn(HDU.PRIMARY, VALUE.REAL, "MJD of airmass"),
    /**
     * Amplifier integration or sample time.
     * <p>
     * units = 'ns'
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    AMPINTEG(HDU.PRIMARY_EXTENSION, VALUE.REAL, "Amplifier integration/sample time"),
    /**
     * Times for the amplifier sensor measurements given as modified Julian
     * dates. The MJDHDR keyword may be used for the time at which the image
     * header is created or the MJD-OBS keyword may be used for the time of
     * observation.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJD-OBS AMPMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    AMPMJD(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * Times for the amplifier sensor measurements given as modified Julian
     * dates. The MJDHDR keyword may be used for the time at which the image
     * header is created or the MJD-OBS keyword may be used for the time of
     * observation.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJD-OBS AMPMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    AMPMJDn(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * Amplifier name.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    AMPNAME(HDU.EXTENSION, VALUE.STRING, "Amplifier name"),
    /**
     * CCD amplifier position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    AMPPAN(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * CCD amplifier position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    AMPPANn(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * CCD amplifier linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    AMPPOS(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * CCD amplifier linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    AMPPOSn(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * CCD amplifier pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    AMPPRE(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * CCD amplifier pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    AMPPREn(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * Amplifier unbinned pixel read time.
     * <p>
     * units = 'ns'
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none none
     * </p>
     */
    AMPREAD(HDU.PRIMARY_EXTENSION, VALUE.REAL, "Unbinned pixel read time"),
    /**
     * CCD amplifier sampling method used. This may also include any integration
     * times.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    AMPSAMPL(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Amplifier sampling method"),
    /**
     * Mapping of the CCD section to amplifier coordinates.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    AMPSEC(HDU.EXTENSION, VALUE.STRING, "Amplifier section"),
    /**
     * The logical unbinned size of the amplifier readout in section notation.
     * This includes drift scanning if applicable.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    AMPSIZE(HDU.EXTENSION, VALUE.STRING, "Amplifier readout size"),
    /**
     * CCD amplifier temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    AMPTEM(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * CCD amplifier temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    AMPTEMn(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * CCD amplifier voltage sensor measurements in volts. }
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    AMPVOL(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * CCD amplifier voltage sensor measurements in volts. }
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    AMPVOLn(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * Aperture position angle unit.
     * <p>
     * default value = PAUNIT APPAUNIT
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    APAUnnn(HDU.PRIMARY, VALUE.STRING, "Aperture position angle unit"),
    /**
     * Declination of the aperture(s).
     * <p>
     * units = APDECU ADEU%4d
     * </p>
     * <p>
     * default value = OBJDEC ODEC%4d
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    APDEC(HDU.PRIMARY, VALUE.STRING, "Aperture declination"),
    /**
     * Declination unit.
     * <p>
     * default value = OBJDECU APDECU
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    APDECU(HDU.PRIMARY, VALUE.STRING, "Declination unit"),
    /**
     * Aperture diameter of the aperture(s) for circular apertures and fibers.
     * This is also used as an approximation to the size of hexagonal lenses.
     * <p>
     * units = APUNIT
     * </p>
     * <p>
     * default value = none APERDIA
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    APDInnn(HDU.PRIMARY, VALUE.REAL, "Aperture diameter"),
    /**
     * Epoch of the coordinates for the aperture(s).
     * <p>
     * units = 'yr'
     * </p>
     * <p>
     * default value = OBJEPOCH OEPO%4d
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    APEPOCH(HDU.PRIMARY, VALUE.REAL, "Aperture coordinate epoch"),
    /**
     * Coordinate system equinox for the aperture(s). A value before 1984 is
     * Besselian otherwise it is Julian.
     * <p>
     * units = 'yr'
     * </p>
     * <p>
     * default value = OBJEQUIN OEQU%4d
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    APEQUIN(HDU.PRIMARY, VALUE.REAL, "Aperture coordinate equinox"),
    /**
     * Aperture diameter of the aperture(s) for circular apertures and fibers.
     * This is also used as an approximation to the size of hexagonal lenses.
     * <p>
     * units = APUNIT
     * </p>
     * <p>
     * default value = none APERDIA
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    APERDIA(HDU.PRIMARY, VALUE.REAL, "Aperture diameter"),
    /**
     * Aperture length of the aperture(s) for slit apertures.
     * <p>
     * units = APUNIT
     * </p>
     * <p>
     * default value = none APERLEN
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    APERLEN(HDU.PRIMARY, VALUE.REAL, "Slit length"),
    /**
     * Aperture identification. This can be a physical aperture identification,
     * the name of a mask, a fiber configuration, etc. When there are many
     * apertures the keyword APERTURE may be used to specify a configuration or
     * mask identification and the APER%4d keywords can be used to identify some
     * information about the aperture such as a fiber number.
     * <p>
     * default value = none APERTURE
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    APERnnn(HDU.PRIMARY, VALUE.STRING, "Aperture identification"),
    /**
     * Aperture position angle of the aperture(s) on the sky. This is measured
     * using the longest dimension from north to east for slits. For hexagonal
     * lenslets it gives the position angle for one of the sides.
     * <p>
     * units = APPAUNIT
     * </p>
     * <p>
     * default value = none APERPA
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    APERPA(HDU.PRIMARY, VALUE.REAL, "Aperture position angle"),
    /**
     * Aperture identification. This can be a physical aperture identification,
     * the name of a mask, a fiber configuration, etc. When there are many
     * apertures the keyword APERTURE may be used to specify a configuration or
     * mask identification and the APER%4d keywords can be used to identify some
     * information about the aperture such as a fiber number.
     * <p>
     * default value = none APERTURE
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    APERWID(HDU.PRIMARY, VALUE.REAL, "Slit width"),
    /**
     * Aperture length of the aperture(s) for slit apertures.
     * <p>
     * units = APUNIT
     * </p>
     * <p>
     * default value = none APERLEN
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    APLEnnn(HDU.PRIMARY, VALUE.REAL, "Slit length"),
    /**
     * Aperture position angle of the aperture(s) on the sky. This is measured
     * using the longest dimension from north to east for slits. For hexagonal
     * lenslets it gives the position angle for one of the sides.
     * <p>
     * units = APPAUNIT
     * </p>
     * <p>
     * default value = none APERPA
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    APPAnnn(HDU.PRIMARY, VALUE.REAL, "Aperture position angle"),
    /**
     * Aperture position angle unit.
     * <p>
     * default value = PAUNIT APPAUNIT
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    APPAUNIT(HDU.PRIMARY, VALUE.STRING, "Aperture position angle unit"),
    /**
     * Right ascension of the aperture(s).
     * <p>
     * units = APRAU ARAU%4d
     * </p>
     * <p>
     * default value = OBJRA ORA%4d
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    APRA(HDU.PRIMARY, VALUE.STRING, "Aperture right ascension"),
    /**
     * Aperture coordinate system type for the aperture(s).
     * <p>
     * default value = OBJRADEC ORDS%4d
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    APRADEC(HDU.PRIMARY, VALUE.STRING, "Aperture coordinate system"),
    /**
     * Right ascension unit.
     * <p>
     * default value = OBJRAU APRAU
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    APRAU(HDU.PRIMARY, VALUE.STRING, "Right ascension unit"),
    /**
     * Aperture type. This is an from a dictionary. The common types are "slit",
     * "hole", "fiber", "hexlens", "hexlens+fiber" and "none". The last type is
     * for aperture-less spectroscopy such as with objective prisms. Typically
     * for multiobject spectroscopy all the aperture types will be the same and
     * the keyword will be APTYPE.
     * <p>
     * default value = none APTYPE
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    APTYnnn(HDU.PRIMARY, VALUE.STRING, "Aperture type"),
    /**
     * Aperture type. This is an from a dictionary. The common types are "slit",
     * "hole", "fiber", "hexlens", "hexlens+fiber" and "none". The last type is
     * for aperture-less spectroscopy such as with objective prisms. Typically
     * for multiobject spectroscopy all the aperture types will be the same and
     * the keyword will be APTYPE.
     * <p>
     * default value = none APTYPE
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    APTYPE(HDU.PRIMARY, VALUE.STRING, "Aperture type"),
    /**
     * Units of aperture dimensions. This applies to slit widths and lengths,
     * fiber diameters, lenslet diameters, etc. It may be a physical dimension
     * or a projected angle on the sky.
     * <p>
     * default value = UNITAP APUNIT
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    APUNIT(HDU.PRIMARY, VALUE.STRING, "Aperture dimension unit"),
    /**
     * Units of aperture dimensions. This applies to slit widths and lengths,
     * fiber diameters, lenslet diameters, etc. It may be a physical dimension
     * or a projected angle on the sky.
     * <p>
     * default value = UNITAP APUNIT
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    APUNnnn(HDU.PRIMARY, VALUE.STRING, "Aperture dimension unit"),
    /**
     * Aperture width of the aperture(s) for slit apertures.
     * <p>
     * units = APUNIT
     * </p>
     * <p>
     * default value = none APERWID
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    APWInnn(HDU.PRIMARY, VALUE.REAL, "Slit width"),
    /**
     * Right ascension of the aperture(s).
     * <p>
     * units = APRAU ARAU%4d
     * </p>
     * <p>
     * default value = OBJRA ORA%4d
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    ARAnnn(HDU.PRIMARY, VALUE.STRING, "Aperture right ascension"),
    /**
     * Right ascension unit.
     * <p>
     * default value = OBJRAU APRAU
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    ARAUnnn(HDU.PRIMARY, VALUE.STRING, "Right ascension unit"),
    /**
     * Archive hardware version.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ARCHHWV(HDU.PRIMARY, VALUE.STRING, "Archive hardware"),
    /**
     * Archive identification. This may be the same as the observation
     * identification.
     * <p>
     * default value = OBSID none
     * </p>
     * <p>
     * index = none none
     * </p>
     */
    ARCHID(HDU.PRIMARY, VALUE.STRING, "Archive identification"),
    /**
     * The archive name in which the observation is archived.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ARCHIVE(HDU.PRIMARY, VALUE.STRING, "Archive"),
    /**
     * Archive status of data.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ARCHSTAT(HDU.PRIMARY, VALUE.STRING, "Archive status"),
    /**
     * Archive software version.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ARCHSWV(HDU.PRIMARY, VALUE.STRING, "Archive software version"),
    /**
     * Arcon predicted gain. This is the gain measured in the laboratory. The
     * GAIN keyword may also have this value initially but it is updated to the
     * most recent estimate of the gain.
     * <p>
     * units = 'e/counts'
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ARCONG(HDU.EXTENSION, VALUE.REAL, "Predicted gain"),
    /**
     * Arcon gain index value.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ARCONGI(HDU.EXTENSION, VALUE.INTEGER, "Gain selection"),
    /**
     * Arcon predicted RMS readout noise. This is the value measured in the
     * laboratory. The RDNOISE keyword may also have this value initially but it
     * is updated to the most current estimate.
     * <p>
     * units = 'e'
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ARCONRN(HDU.EXTENSION, VALUE.REAL, "Predicted readout noise"),
    /**
     * Arcon waveform complilation date.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ARCONWD(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Date CCD waveforms last compiled"),
    /**
     * Arcon waveform options enabled.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ARCONWM(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Arcon waveform options enabled"),
    /**
     * Aperture coordinate system type for the aperture(s).
     * <p>
     * default value = OBJRADEC ORDS%4d
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    ARDSnnn(HDU.PRIMARY, VALUE.STRING, "Aperture coordinate system"),
    /**
     * Transformation matrix between CCD and amplifier coordinates. Normally
     * only two values will be non-zero and will have values of 1 or -1. If
     * missing the default is an identify matrix.
     * <p>
     * default value = 0.(i!=j),1.(i=j)
     * </p>
     * <p>
     * index = 1-9,1-9
     * </p>
     */
    ATMn_n(HDU.EXTENSION, VALUE.REAL, "Amplifier transformation matrix"),
    /**
     * Transformation origin vector between CCD and amplifier coordinates.
     * <p>
     * default value = 0.
     * </p>
     * <p>
     * index = 1-9
     * </p>
     */
    ATVn(HDU.EXTENSION, VALUE.REAL, "Amplifier transformation vector"),
    /**
     * Section of the recorded image containing overscan or prescan data. This
     * will be in binned pixels if binning is done. Multiple regions may be
     * recorded and specified, such as both prescan and overscan, but the first
     * section given by this parameter is likely to be the one used during
     * calibration.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    BIASnnn(HDU.EXTENSION, VALUE.STRING, "Bias section"),
    /**
     * Section of the recorded image containing overscan or prescan data. This
     * will be in binned pixels if binning is done. Multiple regions may be
     * recorded and specified, such as both prescan and overscan, but the first
     * section given by this parameter is likely to be the one used during
     * calibration.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    BIASSEC(HDU.EXTENSION, VALUE.STRING, "Bias section"),
    /**
     * Description of bad pixels. The value is an IRAF bad pixel mask name.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    BPM(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Bad pixels"),
    /**
     * Camera configuration.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CAMCONF(HDU.PRIMARY, VALUE.STRING, "Camera Configuration"),
    /**
     * Camera name.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CAMERA(HDU.PRIMARY, VALUE.STRING, "Camera name"),
    /**
     * Camera focus.
     * <p>
     * units = 'instrumental'
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CAMFOCUS(HDU.PRIMARY, VALUE.REAL, "Camera focus"),
    /**
     * Camera hardware version.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CAMHWV(HDU.PRIMARY, VALUE.STRING, "Camera version"),
    /**
     * Times for the instrument sensor measurements given as modified Julian
     * dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJD-OBS CAMMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CAMMJD(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Times for the instrument sensor measurements given as modified Julian
     * dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJD-OBS CAMMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CAMMJDn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Camera position angle measurements in appropriate units.
     * <p>
     * units = UNITANG
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CAMPAN(HDU.PRIMARY, VALUE.REAL, "Camera position angle"),
    /**
     * Camera position angle measurements in appropriate units.
     * <p>
     * units = UNITANG
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CAMPANn(HDU.PRIMARY, VALUE.REAL, "Camera position angle"),
    /**
     * Camera linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CAMPOS(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Camera linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CAMPOSn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Camera pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CAMPRE(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Camera pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CAMPREn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Camera status.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CAMSTAT(HDU.PRIMARY, VALUE.STRING, "Camera status"),
    /**
     * Camera software version.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CAMSWV(HDU.PRIMARY, VALUE.STRING, "Camera software version"),
    /**
     * Camera temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CAMTEM(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Camera temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CAMTEMn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Camera voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CAMVOL(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Camera voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CAMVOLn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Declination of the CCD center.
     * <p>
     * units = CCDDECU
     * </p>
     * <p>
     * default value = DETDEC
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CCDDEC(HDU.PRIMARY_EXTENSION, VALUE.STRING, "CCD declination"),
    /**
     * Declination unit.
     * <p>
     * default value = DETDECU
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CCDDECU(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Declination unit"),
    /**
     * Epoch of the CCD center coordinates.
     * <p>
     * units = 'yr'
     * </p>
     * <p>
     * default value = DETEPOCH
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CCDEPOCH(HDU.PRIMARY_EXTENSION, VALUE.REAL, "CCD coordinate epoch"),
    /**
     * CCD coordinate system equinox. A value before 1984 is Besselian otherwise
     * it is Julian.
     * <p>
     * units = 'yr'
     * </p>
     * <p>
     * default value = DETEQUIN
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CCDEQUIN(HDU.PRIMARY_EXTENSION, VALUE.REAL, "CCD coordinate equinox"),
    /**
     * CCD hardware version
     * <p>
     * default value = DETHWV
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CCDHWV(HDU.PRIMARY_EXTENSION, VALUE.STRING, "CCD version"),
    /**
     * Times for the CCD sensor measurements given as modified Julian dates. The
     * MJDHDR keyword may be used for the time at which the image header is
     * created or the MJD-OBS keyword may be used for the time of observation.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR CCDMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CCDMJD(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * Times for the CCD sensor measurements given as modified Julian dates. The
     * MJDHDR keyword may be used for the time at which the image header is
     * created or the MJD-OBS keyword may be used for the time of observation.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR CCDMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CCDMJDn(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * CCD identification.
     * <p>
     * default value = DETECTOR
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CCDNAME(HDU.PRIMARY_EXTENSION, VALUE.STRING, "CCD identification"),
    /**
     * Number of amplifiers used to readout the CCD. This keyword may be absent
     * if only one amplifier is used.
     * <p>
     * default value = 1
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CCDNAMPS(HDU.PRIMARY_EXTENSION, VALUE.INTEGER, "Number of amplifiers used"),
    /**
     * CCD position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CCDPAN(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * CCD position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CCDPANn(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * CCD linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CCDPOS(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * CCD linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CCDPOSn(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * CCD pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CCDPRE(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * CCD pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CCDPREn(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * The actual format size of the CCD. This is the same as the CCDSIZE
     * keyword except in the case of drift scanning.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CCDPSIZE(HDU.PRIMARY_EXTENSION, VALUE.STRING, "CCD size"),
    /**
     * Right ascension of the CCD center.
     * <p>
     * units = CCDRAU
     * </p>
     * <p>
     * default value = DETRA
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CCDRA(HDU.PRIMARY_EXTENSION, VALUE.STRING, "CCD right ascension"),
    /**
     * CCD coordinate system type.
     * <p>
     * default value = DETRADEC
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CCDRADEC(HDU.PRIMARY_EXTENSION, VALUE.STRING, "CCD coordinate system"),
    /**
     * Right ascension unit.
     * <p>
     * default value = DETRAU
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CCDRAU(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Right ascension unit"),
    /**
     * The unbinned section of the logical CCD pixel raster covered by the
     * amplifier readout in section notation. The section must map directly to
     * the specified data section through the binning and CCD to image
     * coordiante transformation. The image data section (DATASEC) is specified
     * with the starting pixel less than the ending pixel. Thus the order of
     * this section may be flipped depending on the coordinate transformation
     * (which depends on how the CCD coordinate system is defined).
     * <p>
     * default value = CCDSIZE
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CCDSEC(HDU.EXTENSION, VALUE.STRING, "Region of CCD read"),
    /**
     * The logical unbinned size of the CCD in section notation. Normally this
     * would be the physical size of the CCD unless drift scanning is done. This
     * is the full size even when subraster readouts are done.
     * <p>
     * default value = DETSIZE
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CCDSIZE(HDU.PRIMARY_EXTENSION, VALUE.STRING, "CCD size"),
    /**
     * CCD on-chip summing given as two or four integer numbers. These define
     * the summing of CCD pixels in the amplifier readout order. The first two
     * numbers give the number of pixels summed in the serial and parallel
     * directions respectively. If the first pixel read out consists of fewer
     * unbinned pixels along either direction the next two numbers give the
     * number of pixels summed for the first serial and parallel pixels. From
     * this it is implicit how many pixels are summed for the last pixels given
     * the size of the CCD section (CCDSEC). It is highly recommended that
     * controllers read out all pixels with the same summing in which case the
     * size of the CCD section will be the summing factors times the size of the
     * data section.
     * <p>
     * default value = '1 1'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CCDSUM(HDU.EXTENSION, VALUE.STRING, "CCD on-chip summing"),
    /**
     * CCD software version
     * <p>
     * default value = DETSWV
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CCDSWV(HDU.PRIMARY_EXTENSION, VALUE.STRING, "CCD software version"),
    /**
     * CCD temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CCDTEM(HDU.PRIMARY_EXTENSION, VALUE.REAL, "CCD temperature"),
    /**
     * CCD temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CCDTEMn(HDU.PRIMARY_EXTENSION, VALUE.REAL, "CCD temperature"),
    /**
     * CCD voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CCDVOL(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * CCD voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CCDVOLn(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * Spectrum coordinate matrix. World coordinate axis 1 is defined to be the
     * dispersion and the other axes are spatial. The matrix implies a
     * dispersion axis in the image coordinates.
     * <p>
     * units = CUNIT1/pixel CUN1%4d/pixel
     * </p>
     * <p>
     * default value = 1. CD1_1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CD1_1(HDU.EXTENSION, VALUE.REAL, "Coordinate scale matrix"),
    /**
     * Spectrum coordinate matrix. World coordinate axis 1 is defined to be the
     * dispersion and the other axes are spatial. The matrix implies a
     * dispersion axis in the image coordinates.
     * <p>
     * units = CUNIT1/pixel CUN1%4d/pixel
     * </p>
     * <p>
     * default value = 0. CD1_2
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CD1_2(HDU.EXTENSION, VALUE.REAL, "Coordinate scale matrix"),
    /**
     * Spectrum coordinate matrix. World coordinate axis 1 is defined to be the
     * dispersion and the other axes are spatial. The matrix implies a
     * dispersion axis in the image coordinates.
     * <p>
     * units = CUNIT1/pixel CUN1%4d/pixel
     * </p>
     * <p>
     * default value = 1. CD1_1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CD11nnn(HDU.EXTENSION, VALUE.REAL, "Coordinate scale matrix"),
    /**
     * Spectrum coordinate matrix. World coordinate axis 1 is defined to be the
     * dispersion and the other axes are spatial. The matrix implies a
     * dispersion axis in the image coordinates.
     * <p>
     * units = CUNIT1/pixel CUN1%4d/pixel
     * </p>
     * <p>
     * default value = 0. CD1_2
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CD12nnn(HDU.EXTENSION, VALUE.REAL, "Coordinate scale matrix"),
    /**
     * Spectrum coordinate matrix. World coordinate axis 1 is defined to be the
     * dispersion and the other axes are spatial. The matrix implies a
     * dispersion axis in the image coordinates.
     * <p>
     * units = CUNIT2/pixel CUN2%4d/pixel
     * </p>
     * <p>
     * default value = 0. CD2_1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CD2_1(HDU.EXTENSION, VALUE.REAL, "Coordinate scale matrix"),
    /**
     * Spectrum coordinate matrix. World coordinate axis 1 is defined to be the
     * dispersion and the other axes are spatial. The matrix implies a
     * dispersion axis in the image coordinates.
     * <p>
     * units = CUNIT2/pixel CUN2%4d/pixel
     * </p>
     * <p>
     * default value = 1. CD2_2
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CD2_2(HDU.EXTENSION, VALUE.REAL, "Coordinate scale matrix"),
    /**
     * Spectrum coordinate matrix. World coordinate axis 1 is defined to be the
     * dispersion and the other axes are spatial. The matrix implies a
     * dispersion axis in the image coordinates.
     * <p>
     * units = CUNIT2/pixel CUN2%4d/pixel
     * </p>
     * <p>
     * default value = 0. CD2_1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CD21nnn(HDU.EXTENSION, VALUE.REAL, "Coordinate scale matrix"),
    /**
     * Spectrum coordinate matrix. World coordinate axis 1 is defined to be the
     * dispersion and the other axes are spatial. The matrix implies a
     * dispersion axis in the image coordinates.
     * <p>
     * units = CUNIT2/pixel CUN2%4d/pixel
     * </p>
     * <p>
     * default value = 1. CD2_2
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CD22nnn(HDU.EXTENSION, VALUE.REAL, "Spec coord matrix"),
    /**
     * Coordinate scale matrix for image world coordinates. This describes the
     * scales and rotations of the coordinate axes.
     * <p>
     * units = CUNIT2/pixel CUN2%4d/pixel
     * </p>
     * <p>
     * default value = 1. CD2_2
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CHPANGLE(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CHPDIST(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CHPFREQ(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CHPHWV(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * Times for the chopping system sensor measurements given as modified
     * Julian dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR CHPMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CHPMJD(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Times for the chopping system sensor measurements given as modified
     * Julian dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR CHPMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CHPMJDn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CHPNCHOP(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * Chopping system position angle measurements in appropriate units. Note
     * that CHPANGLE should be used for the chopping angle and these keywords
     * are for other system position angle measurements.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CHPPAN(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Chopping system position angle measurements in appropriate units. Note
     * that CHPANGLE should be used for the chopping angle and these keywords
     * are for other system position angle measurements.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CHPPANn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Chopping system linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CHPPOS(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Chopping system linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CHPPOSn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Chopping system pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CHPPRE(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Chopping system pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CHPPREn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CHPSTAT(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CHPSWV(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * Chopping system temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CHPTEM(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Chopping system temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CHPTEMn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Chopping system voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CHPVOL(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Chopping system voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CHPVOLn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Dispersion limit for the region occupied by the spectrum.
     * <p>
     * units = CUNIT1 CUN1%4d
     * </p>
     * <p>
     * default value = none CMAX1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CMAX1(HDU.EXTENSION, VALUE.REAL, "Spectrum dispersion limit"),
    /**
     * Cross-dispersion limit for the region occupied by the spectrum.
     * <p>
     * units = CUNIT2 CUN2%4d
     * </p>
     * <p>
     * default value = none CMAX2
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CMAX2(HDU.EXTENSION, VALUE.REAL, "Spectrum cross-dispersion limit"),
    /**
     * Dispersion limit for the region occupied by the spectrum.
     * <p>
     * units = CUNIT1 CUN1%4d
     * </p>
     * <p>
     * default value = none CMIN1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CMIN1(HDU.EXTENSION, VALUE.REAL, "Spectrum dispersion limit"),
    /**
     * Cross-dispersion limit for the region occupied by the spectrum.
     * <p>
     * units = CUNIT2 CUN2%4d
     * </p>
     * <p>
     * default value = none CMIN2
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CMIN2(HDU.EXTENSION, VALUE.REAL, "Spectrum cross-dispersion limit"),
    /**
     * Observer comments.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = 1-9999
     * </p>
     */
    CMMTnnn(HDU.PRIMARY, VALUE.STRING, ""),
    /**
     * Dispersion limit for the region occupied by the spectrum.
     * <p>
     * units = CUNIT1 CUN1%4d
     * </p>
     * <p>
     * default value = none CMIN1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CMN1nnn(HDU.EXTENSION, VALUE.REAL, "Spectrum dispersion limit"),
    /**
     * Cross-dispersion limit for the region occupied by the spectrum.
     * <p>
     * units = CUNIT2 CUN2%4d
     * </p>
     * <p>
     * default value = none CMIN2
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CMN2nnn(HDU.EXTENSION, VALUE.REAL, "Spectrum cross-dispersion limit"),
    /**
     * Dispersion limit for the region occupied by the spectrum.
     * <p>
     * units = CUNIT1 CUN1%4d
     * </p>
     * <p>
     * default value = none CMAX1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CMX1nnn(HDU.EXTENSION, VALUE.REAL, "Spectrum dispersion limit"),
    /**
     * Cross-dispersion limit for the region occupied by the spectrum.
     * <p>
     * units = CUNIT2 CUN2%4d
     * </p>
     * <p>
     * default value = none CMAX2
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CMX2nnn(HDU.EXTENSION, VALUE.REAL, "Spectrum cross-dispersion limit"),
    /**
     * Controller hardware version.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CONHWV(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Controller hardware version"),
    /**
     * Controller status.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CONSTAT(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Controller status"),
    /**
     * Controller software version.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CONSWV(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Controller software version"),
    /**
     * Detector controller name.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CONTROLR(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Detector controller"),
    /**
     * Correctors in the optical path.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CORRCT(HDU.PRIMARY, VALUE.STRING, "Corrector"),
    /**
     * Correctors in the optical path.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    CORRCTn(HDU.PRIMARY, VALUE.STRING, "Corrector"),
    /**
     * Correctors in the optical path.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CORRCTOR(HDU.PRIMARY, VALUE.STRING, "Corrector Identification"),
    /**
     * Default cross dispersion unit.
     * <p>
     * default value = 'arcsec'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CROSUNIT(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Declination unit"),
    /**
     * Default cross dispersion coordinate value.
     * <p>
     * units = CROSUNIT
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    CROSVAL(HDU.PRIMARY_EXTENSION, VALUE.REAL, "Cross dispersion coordinate"),
    /**
     * Reference spectrum pixel coordinate. Generally this should be the at the
     * center of the spectrum. In raw data the spectrum position(s) may be
     * predicted apart from an offset that will be determined during data
     * reduction.
     * <p>
     * units = 'pixel' 'pixel'
     * </p>
     * <p>
     * default value = none CRPIX1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CRP1nnn(HDU.EXTENSION, VALUE.REAL, "Coordinate reference pixel"),
    /**
     * Reference spectrum pixel coordinate. Generally this should be the at the
     * center of the spectrum. In raw data the spectrum position(s) may be
     * predicted apart from an offset that will be determined during data
     * reduction.
     * <p>
     * units = 'pixel' 'pixel'
     * </p>
     * <p>
     * default value = none CRPIX2
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CRP2nnn(HDU.EXTENSION, VALUE.REAL, "Coordinate reference pixel"),
    /**
     * Reference spectrum pixel coordinate. Generally this should be the at the
     * center of the spectrum. In raw data the spectrum position(s) may be
     * predicted apart from an offset that will be determined during data
     * reduction.
     * <p>
     * units = 'pixel' 'pixel'
     * </p>
     * <p>
     * default value = none CRPIX1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CRPIX1(HDU.EXTENSION, VALUE.REAL, "Coordinate reference pixel"),
    /**
     * Reference spectrum pixel coordinate. Generally this should be the at the
     * center of the spectrum. In raw data the spectrum position(s) may be
     * predicted apart from an offset that will be determined during data
     * reduction.
     * <p>
     * units = 'pixel' 'pixel'
     * </p>
     * <p>
     * default value = none CRPIX2
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CRPIX2(HDU.EXTENSION, VALUE.REAL, "Coordinate reference pixel"),
    /**
     * Spectrum reference dispersion coordinate corresponding to the spectrum
     * reference pixel coordinate. Note that by definition WCS axis 1 is always
     * the dispersion axis. The mapping of this WCS axis to the dispersion
     * direction in the image is given by the coordinate transformation matrix
     * keywords. In raw data the reference dispersion coordinate may be
     * approximately predicted. This will be refined during data reductions.
     * <p>
     * units = CUNIT1 CUN1%4d
     * </p>
     * <p>
     * default value = none CRVAL1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CRV1nnn(HDU.EXTENSION, VALUE.REAL, "Coordinate reference value"),
    /**
     * Spectrum reference dispersion coordinate corresponding to the spectrum
     * reference pixel coordinate. Note that by definition WCS axis 1 is always
     * the dispersion axis. The mapping of this WCS axis to the dispersion
     * direction in the image is given by the coordinate transformation matrix
     * keywords. In raw data the reference dispersion coordinate may be
     * approximately predicted. This will be refined during data reductions.
     * <p>
     * units = CUNIT1 CUN1%4d
     * </p>
     * <p>
     * default value = 0. CRVAL1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CRV2nnn(HDU.EXTENSION, VALUE.REAL, "Coordinate reference value"),
    /**
     * Spectrum reference dispersion coordinate corresponding to the spectrum
     * reference pixel coordinate. Note that by definition WCS axis 1 is always
     * the dispersion axis. The mapping of this WCS axis to the dispersion
     * direction in the image is given by the coordinate transformation matrix
     * keywords. In raw data the reference dispersion coordinate may be
     * approximately predicted. This will be refined during data reductions.
     * <p>
     * units = CUNIT1 CUN1%4d
     * </p>
     * <p>
     * default value = none CRVAL1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CRVAL1(HDU.EXTENSION, VALUE.REAL, "Spectrum dispersion center"),
    /**
     * Reference right ascension coordinate corresponding to the image reference
     * pixel coordinate. Note that by definition WCS axis 1 is always the right
     * ascension axis. The mapping of this WCS axis to the right ascension
     * direction in the image is given by the coordinate transformation matrix
     * keywords. In raw data the reference right ascension coordinate may be
     * only approximate. This will be refined during data reductions.
     * <p>
     * units = CUNIT1 CUN1%4d
     * </p>
     * <p>
     * default value = 0. CRVAL1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CRVAL2(HDU.EXTENSION, VALUE.REAL, "Spectrum cross-dispersion center"),
    /**
     * Reference declination coordinate corresponding to the image reference
     * pixel coordinate. Note that by definition WCS axis 1 is always the
     * declination axis. The mapping of this WCS axis to the declination
     * direction in the image is given by the coordinate transformation matrix
     * keywords. In raw data the reference right ascension coordinate may be
     * only approximate. This will be refined during data reductions.
     * <p>
     * units = CUNIT2 CUN2%4d
     * </p>
     * <p>
     * default value = 0. CRVAL2
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CTY1nnn(HDU.EXTENSION, VALUE.STRING, "Spectrum coordinate type"),
    /**
     * Coordinate type for image world coordinates. The IRAF WCS standards are
     * used (which is generally the FITS standard).
     * <p>
     * default value = 'LINEAR' CTYPE1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CTY2nnn(HDU.EXTENSION, VALUE.STRING, "Spectrum coordinate type"),
    /**
     * Coordinate type for image world coordinates. The IRAF WCS standards are
     * used (which is generally the FITS standard).
     * <p>
     * default value = 'LINEAR' CTYPE2
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CTYP2nnn(HDU.EXTENSION, VALUE.STRING, "Coordinate type"),
    /**
     * Spectrum dispersion coordinate type. These are the FITS defined types.
     * <p>
     * default value = 'LINEAR' CTYPE1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CTYPE1(HDU.EXTENSION, VALUE.STRING, "Spectrum coordinate type"),
    /**
     * Coordinate type for image world coordinates. The IRAF WCS standards are
     * used (which is generally the FITS standard).
     * <p>
     * default value = 'LINEAR' CTYPE1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CTYPE2(HDU.EXTENSION, VALUE.STRING, "Spectrum coordinate type"),
    /**
     * Coordinate type for image world coordinates. The IRAF WCS standards are
     * used (which is generally the FITS standard).
     * <p>
     * default value = 'LINEAR' CTYPE2
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CUN1nnn(HDU.EXTENSION, VALUE.STRING, "Spectrum coordinate unit"),
    /**
     * Coordinate reference unit for direct imaging world coordinates.
     * <p>
     * default value = RAUNIT CUNIT1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CUN2nnn(HDU.EXTENSION, VALUE.STRING, "Spectrum coordinate unit"),
    /**
     * Coordinate reference unit for direct imaging world coordinates.
     * <p>
     * default value = DECUNIT CUNIT2
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CUNIT1(HDU.EXTENSION, VALUE.STRING, "Spectrum coordinate unit"),
    /**
     * Coordinate reference unit for direct imaging world coordinates.
     * <p>
     * default value = RAUNIT CUNIT1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    CUNIT2(HDU.EXTENSION, VALUE.STRING, "Coordinate reference unit"),
    /**
     * Total dark time of the observation. This is the total time during which
     * dark current is collected by the detector. If the times in the extension
     * are different the primary HDU gives one of the extension times.
     * <p>
     * units = UNITTIME
     * </p>
     * <p>
     * default value = EXPTIME
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DARKTIME(HDU.PRIMARY_EXTENSION, VALUE.REAL, "Dark time"),
    /**
     * Mapping of the CCD section to image coordinates.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DATASEC(HDU.EXTENSION, VALUE.STRING, "Image data section"),
    /**
     * Date at the end of the exposure. The format follows the FITS standard.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DATEEND(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Date at end of exposure"),
    /**
     * Date header creation. The format follows the FITS 'date' standard.
     * <p>
     * default value = DATE-OBS
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DATEHDR(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Date of header creation"),
    /**
     * Default date for the observation. This keyword is generally not used and
     * is DATE-OBS keyword for the start of the exposure on the detector is
     * used.
     * <p>
     * default value = DATE-OBS
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DATEOBS(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Date of observation"),
    /**
     * Projected position angle of the positive declination axis on the
     * detector. The position angle is measured clockwise from the image y axis.
     * <p>
     * units = UNITPA
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DECPANGL(HDU.PRIMARY, VALUE.REAL, "Position angle of Dec axis"),
    /**
     * Default declination units.
     * <p>
     * default value = UNITDEC
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DECUNIT(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Declination unit"),
    /**
     * Detector configuration.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DETCONF(HDU.PRIMARY, VALUE.STRING, "Detector Configuration"),
    /**
     * Declination of the detector center.
     * <p>
     * units = DETDECU
     * </p>
     * <p>
     * default value = TELDEC
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DETDEC(HDU.PRIMARY, VALUE.STRING, "Detector delination"),
    /**
     * Declination unit.
     * <p>
     * default value = TELDECU
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DETDECU(HDU.PRIMARY, VALUE.STRING, "Delination unit"),
    /**
     * Detector name.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DETECTOR(HDU.PRIMARY, VALUE.STRING, "Detector name"),
    /**
     * Epoch of the detector center coordinates.
     * <p>
     * units = 'yr'
     * </p>
     * <p>
     * default value = TELEPOCH
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DETEPOCH(HDU.PRIMARY, VALUE.REAL, "Detector coordinate epoch"),
    /**
     * Detector coordinate system equinox. A value before 1984 is Besselian
     * otherwise it is Julian.
     * <p>
     * units = 'yr'
     * </p>
     * <p>
     * default value = TELEQUIN
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DETEQUIN(HDU.PRIMARY, VALUE.REAL, "Detector coordinate equinox"),
    /**
     * Detector hardware version.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DETHWV(HDU.PRIMARY, VALUE.STRING, "Detector version"),
    /**
     * Times for the detector sensor measurements given as modified Julian
     * dates. The MJDHDR keyword may be used for the time at which the image
     * header is created or the MJD-OBS keyword may be used for the time of
     * observation.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJD-OBS DETMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DETMJD(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Times for the detector sensor measurements given as modified Julian
     * dates. The MJDHDR keyword may be used for the time at which the image
     * header is created or the MJD-OBS keyword may be used for the time of
     * observation.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJD-OBS DETMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DETMJDn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Detector position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DETPAN(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Detector position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DETPANn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Detector linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DETPOS(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Detector linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DETPOSn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Detector pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DETPRE(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Detector pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DETPREn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Right ascension of the detector center.
     * <p>
     * units = DETRAU
     * </p>
     * <p>
     * default value = TELRA
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DETRA(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Detector right ascension"),
    /**
     * Detector coordinate system type.
     * <p>
     * default value = TELRADEC
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DETRADEC(HDU.PRIMARY, VALUE.STRING, "Detector coordinate system"),
    /**
     * Right ascension unit.
     * <p>
     * default value = TELRAU
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DETRAU(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Right ascension unit"),
    /**
     * Mapping of the CCD section to detector coordinates.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DETSEC(HDU.EXTENSION, VALUE.STRING, "Detector data section"),
    /**
     * The logical unbinned size of the detector in section notation. This is
     * the full pixel raster size including, if applicable, drift scanning or a
     * mosaic format. This is the full size even when subraster readouts are
     * done.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DETSIZE(HDU.PRIMARY, VALUE.STRING, "Detector size"),
    /**
     * Detector status.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DETSTAT(HDU.PRIMARY, VALUE.STRING, "Detector status"),
    /**
     * Detector software version. This will not generally be used and the
     * controller software version will apply.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DETSWV(HDU.PRIMARY, VALUE.STRING, "Detector software version"),
    /**
     * Detector temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DETTEM(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Detector temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DETTEMn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Detector voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DETVOL(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Detector voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DETVOLn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Dewar identification.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DEWAR(HDU.PRIMARY, VALUE.STRING, "Dewar"),
    /**
     * Dewar hardware version.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DEWHWV(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Dewar hardware"),
    /**
     * Times for the dewar sensor measurements given as modified Julian dates.
     * The MJDHDR keyword may be used for the time at which the image header is
     * created or the MJD-OBS keyword may be used for the time of observation.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJD-OBS DEWMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DEWMJD(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * Times for the dewar sensor measurements given as modified Julian dates.
     * The MJDHDR keyword may be used for the time at which the image header is
     * created or the MJD-OBS keyword may be used for the time of observation.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJD-OBS DEWMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DEWMJDn(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * Dewar position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DEWPAN(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * Dewar position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DEWPANn(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * Dewar linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DEWPOS(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * Dewar linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DEWPOSn(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * Dewar pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DEWPRE(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * Dewar pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DEWPREn(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * Dewar status.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DEWSTAT(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Dewar status"),
    /**
     * Dewar software version.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DEWSWV(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Dewar software version"),
    /**
     * Dewar temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DEWTEM(HDU.PRIMARY_EXTENSION, VALUE.REAL, "Dewar temperature"),
    /**
     * Dewar temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DEWTEMn(HDU.PRIMARY_EXTENSION, VALUE.REAL, "Dewar temperature"),
    /**
     * Dewar voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DEWVOL(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * Dewar voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DEWVOLn(HDU.PRIMARY_EXTENSION, VALUE.REAL, ""),
    /**
     * Times for the disperser sensor measurements given as modified Julian
     * dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJD-OBS DISMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DISMJD(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Times for the disperser sensor measurements given as modified Julian
     * dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJD-OBS DISMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DISMJDn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Disperser position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DISPAN(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Disperser position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DISPANn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * The detector axis along which the dispersion is most closely aligned.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DISPAXIS(HDU.PRIMARY, VALUE.INTEGER, "Dispersion axis"),
    /**
     * Approximate central dispersion/pixel on the detector.
     * <p>
     * units = DISPUNIT
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DISPDW(HDU.PRIMARY, VALUE.REAL, "Dispersion"),
    /**
     * Disperser identification names.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DISPER(HDU.PRIMARY, VALUE.STRING, "Disperser"),
    /**
     * Disperser identification names.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DISPERn(HDU.PRIMARY, VALUE.STRING, "Disperser"),
    /**
     * Disperser linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DISPOS(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Disperser linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DISPOSn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Disperser pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DISPRE(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Disperser pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DISPREn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Default dispersion coordinate unit.
     * <p>
     * default value = 'Angstrom'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DISPUNIT(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Dispersion coordinate unit"),
    /**
     * Default dispersion coordinate value.
     * <p>
     * units = DISPUNIT
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DISPVAL(HDU.PRIMARY_EXTENSION, VALUE.REAL, "Dispersion coordinate"),
    /**
     * Approximate central dispersion coordinate on the detector.
     * <p>
     * units = DISPUNIT
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DISPWC(HDU.PRIMARY, VALUE.REAL, "Central dispersion coordinate"),
    /**
     * Disperser temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DISTEM(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Disperser temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DISTEMn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Disperser voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DISVOL(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Disperser voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DISVOLn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Average wind direction measurements measured east of north over the
     * sampling period inside the dome.
     * <p>
     * units = UNITPA
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DMEDIR(HDU.PRIMARY, VALUE.REAL, "Average wind direction"),
    /**
     * Average wind direction measurements measured east of north over the
     * sampling period inside the dome.
     * <p>
     * units = UNITPA
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DMEDIRn(HDU.PRIMARY, VALUE.REAL, "Average wind direction"),
    /**
     * Maximum wind speed over the sampling period inside the dome.
     * <p>
     * units = UNITVEL
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DMEGUS(HDU.PRIMARY, VALUE.REAL, "Maximum dome wind speed"),
    /**
     * Maximum wind speed over the sampling period inside the dome.
     * <p>
     * units = UNITVEL
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DMEGUSn(HDU.PRIMARY, VALUE.REAL, "Maximum dome wind speed"),
    /**
     * Times for the dome environment measurements given as modified Julian. For
     * the wind measurements this is the start of the sampling period.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR DMEMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DMEMJD(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Times for the dome environment measurements given as modified Julian. For
     * the wind measurements this is the start of the sampling period.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR DMEMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DMEMJDn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Wind sampling period for the wind measurements inside the dome. If no
     * value is given then the measurements are assumed to be 'instantaneous'.
     * <p>
     * units = UNITTIME
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DMEPER(HDU.PRIMARY, VALUE.REAL, "Dome wind sampling"),
    /**
     * Wind sampling period for the wind measurements inside the dome. If no
     * value is given then the measurements are assumed to be 'instantaneous'.
     * <p>
     * units = UNITTIME
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DMEPERn(HDU.PRIMARY, VALUE.REAL, "Dome wind sampling"),
    /**
     * Temperatures Celsius inside the dome.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DMETEM(HDU.PRIMARY, VALUE.REAL, "Dome temperature"),
    /**
     * Temperatures Celsius inside the dome.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DMETEMn(HDU.PRIMARY, VALUE.REAL, "Dome temperature"),
    /**
     * Average wind speeds over the sampling period inside the dome.
     * <p>
     * units = UNITVEL
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DMEWIN(HDU.PRIMARY, VALUE.REAL, "Average dome wind speed"),
    /**
     * Average wind speeds over the sampling period inside the dome.
     * <p>
     * units = UNITVEL
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DMEWINn(HDU.PRIMARY, VALUE.REAL, "Average dome wind speed"),
    /**
     * Times for the dome sensor measurements given as modified Julian dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR DOMMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DOMMJD(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Times for the dome sensor measurements given as modified Julian dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR DOMMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DOMMJDn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Dome position angle sensor measurements. This should be in degrees east
     * of north for the center of the dome slit.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DOMPAN(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Dome position angle sensor measurements. This should be in degrees east
     * of north for the center of the dome slit.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DOMPANn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Dome linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DOMPOS(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Dome linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DOMPOSn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Dome pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DOMPRE(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Dome pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DOMPREn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Dome status.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    DOMSTAT(HDU.PRIMARY, VALUE.STRING, "Dome status"),
    /**
     * Dome temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DOMTEM(HDU.PRIMARY, VALUE.REAL, "Dome temperature"),
    /**
     * Dome temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DOMTEMn(HDU.PRIMARY, VALUE.REAL, "Dome temperature"),
    /**
     * Dome voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DOMVOL(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Dome voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    DOMVOLn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Transformation matrix between CCD and detector coordinates. If missing
     * the default is an identify matrix.
     * <p>
     * default value = 0.(i!=j),1.(i=j)
     * </p>
     * <p>
     * index = 1-9,1-9
     * </p>
     */
    DTMn_n(HDU.EXTENSION, VALUE.REAL, "Detector transformation matrix"),
    /**
     * Transformation origin vector between CCD and detector coordinates.
     * <p>
     * default value = 0.
     * </p>
     * <p>
     * index = 1-9
     * </p>
     */
    DTVn(HDU.EXTENSION, VALUE.REAL, "Detector transformation vector"),
    /**
     * Average wind direction measurements measured east of north over the
     * sampling period outside the dome at times given by ENVMJDn keywords.
     * <p>
     * units = UNITPA
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ENVDIR(HDU.PRIMARY, VALUE.REAL, "Average wind direction"),
    /**
     * Average wind direction measurements measured east of north over the
     * sampling period outside the dome at times given by ENVMJDn keywords.
     * <p>
     * units = UNITPA
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ENVDIRn(HDU.PRIMARY, VALUE.REAL, "Average wind direction"),
    /**
     * Maximum wind speed in km/s over the sampling period outside the dome at
     * times given by ENVMJDn keywords.
     * <p>
     * units = UNITVEL
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ENVGUS(HDU.PRIMARY, VALUE.REAL, "Maximum gust speed"),
    /**
     * Maximum wind speed in km/s over the sampling period outside the dome at
     * times given by ENVMJDn keywords.
     * <p>
     * units = UNITVEL
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ENVGUSn(HDU.PRIMARY, VALUE.REAL, "Maximum gust speed"),
    /**
     * Relative humidity measurements at times given by ENVMJDn keywords.
     * <p>
     * units = '%'
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ENVHUM(HDU.PRIMARY, VALUE.REAL, "Relative humidity"),
    /**
     * Relative humidity measurements at times given by ENVMJDn keywords.
     * <p>
     * units = '%'
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ENVHUMn(HDU.PRIMARY, VALUE.REAL, "Relative humidity"),
    /**
     * Times for the site environment measurements given as modified Julian. For
     * the wind measurements this is the start of the sampling period.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR ENVMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ENVMJD(HDU.PRIMARY, VALUE.REAL, "Environment measurement time"),
    /**
     * Times for the site environment measurements given as modified Julian. For
     * the wind measurements this is the start of the sampling period.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR ENVMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ENVMJDn(HDU.PRIMARY, VALUE.REAL, "Environment measurement time"),
    /**
     * Wind sampling period for the wind measurements outside the dome at times
     * given by ENVMJDn keywords. If no value is given then the measurements are
     * assumed to be 'instantaneous'.
     * <p>
     * units = UNITTIME
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ENVPER(HDU.PRIMARY, VALUE.REAL, "Wind sampling period"),
    /**
     * Wind sampling period for the wind measurements outside the dome at times
     * given by ENVMJDn keywords. If no value is given then the measurements are
     * assumed to be 'instantaneous'.
     * <p>
     * units = UNITTIME
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ENVPERn(HDU.PRIMARY, VALUE.REAL, "Wind sampling period"),
    /**
     * Atmospheric pressure measurements at times given by ENVMJDn keywords.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ENVPRE(HDU.PRIMARY, VALUE.REAL, "Air pressure"),
    /**
     * Atmospheric pressure measurements at times given by ENVMJDn keywords.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ENVPREn(HDU.PRIMARY, VALUE.REAL, "Air pressure"),
    /**
     * Temperatures outside the dome at times given by ENVMJDn keywords.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ENVTEM(HDU.PRIMARY, VALUE.REAL, "Site temperature"),
    /**
     * Temperatures outside the dome at times given by ENVMJDn keywords.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ENVTEMn(HDU.PRIMARY, VALUE.REAL, "Site temperature"),
    /**
     * Precipitable water vapor measurements at times given by ENVMJDn keywords.
     * <p>
     * units = 'mm'
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ENVWAT(HDU.PRIMARY, VALUE.REAL, "Precipitable water vapor"),
    /**
     * Precipitable water vapor measurements at times given by ENVMJDn keywords.
     * <p>
     * units = 'mm'
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ENVWATn(HDU.PRIMARY, VALUE.REAL, "Precipitable water vapor"),
    /**
     * Average wind speeds over the sampling period outside the dome at times
     * given by ENVMJDn keywords.
     * <p>
     * units = UNITVEL
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ENVWIN(HDU.PRIMARY, VALUE.REAL, "Average wind speed"),
    /**
     * Average wind speeds over the sampling period outside the dome at times
     * given by ENVMJDn keywords.
     * <p>
     * units = UNITVEL
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    ENVWINn(HDU.PRIMARY, VALUE.REAL, "Average wind speed"),
    /**
     * Error information. The sequence numbers are used to order the
     * information.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = 1-999
     * </p>
     */
    ERRORnnn(HDU.PRIMARY, VALUE.STRING, ""),
    /**
     * Requested exposure time of the observation.
     * <p>
     * units = UNITTIME
     * </p>
     * <p>
     * default value = EXPTIME
     * </p>
     * <p>
     * index = none
     * </p>
     */
    EXPREQ(HDU.PRIMARY_EXTENSION, VALUE.REAL, "Requested exposure time"),
    /**
     * Fiber identification for the fiber(s). The string consists of a fiber
     * number, an object type number (0=sky, 1=object, etc.), the right
     * ascension and declination, and the object name or title. This can replace
     * OBJNAME, APRA/OBJRA, and APDEC/OBJDEC.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    FIBER(HDU.PRIMARY, VALUE.STRING, ""),
    /**
     * Fiber identification for the fiber(s). The string consists of a fiber
     * number, an object type number (0=sky, 1=object, etc.), the right
     * ascension and declination, and the object name or title. This can replace
     * OBJNAME, APRA/OBJRA, and APDEC/OBJDEC.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    FIBnnn(HDU.PRIMARY, VALUE.STRING, ""),
    /**
     * Filter position given as filter wheel number or other filter system
     * position measurement.
     * <p>
     * units = 'instrumental'
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    FILPOS(HDU.PRIMARY, VALUE.REAL, "Filter system position"),
    /**
     * Filter position given as filter wheel number or other filter system
     * position measurement.
     * <p>
     * units = 'instrumental'
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    FILPOSn(HDU.PRIMARY, VALUE.REAL, "Filter system position"),
    /**
     * Filter type. This is the technical specification or observatory
     * identification name.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    FILTYP(HDU.PRIMARY, VALUE.STRING, "Filter type"),
    /**
     * Filter type. This is the technical specification or observatory
     * identification name.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    FILTYPn(HDU.PRIMARY, VALUE.STRING, "Filter type"),
    /**
     * Number of focus exposures in a focus sequence.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    FOCNEXPO(HDU.PRIMARY, VALUE.INTEGER, "Number of focus exposures"),
    /**
     * Pixel shift on the detector between exposures in a focus sequence.
     * <p>
     * units = 'pixel'
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    FOCSHIFT(HDU.PRIMARY, VALUE.REAL, "Shift between focus exposures"),
    /**
     * Starting focus value in focus sequence.
     * <p>
     * units = 'instrumental'
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    FOCSTART(HDU.PRIMARY, VALUE.REAL, "Starting focus"),
    /**
     * Focus increment step in focus sequence.
     * <p>
     * units = 'instrumental'
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    FOCSTEP(HDU.PRIMARY, VALUE.REAL, "Focus step"),
    /**
     * Amplifier gain in electrons per analog unit. This is the most current
     * estimate of the gain.
     * <p>
     * units = 'e/count'
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    GAIN(HDU.EXTENSION, VALUE.REAL, "Amplifier gain"),
    /**
     * Guider TV name.
     * <p>
     * default value = TV
     * </p>
     * <p>
     * index = none
     * </p>
     */
    GTV(HDU.PRIMARY, VALUE.STRING, "Guider TV"),
    /**
     * Guider TV filter names. This name is the astronomical standard name if
     * applicable; i.e. U, B, Gunn I, etc. The filter type and filter device
     * position are given by other keywords.
     * <p>
     * default value = TVFILT GTVFIL
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GTVFIL(HDU.PRIMARY, VALUE.STRING, "Filter name"),
    /**
     * Guider TV filter names. This name is the astronomical standard name if
     * applicable; i.e. U, B, Gunn I, etc. The filter type and filter device
     * position are given by other keywords.
     * <p>
     * default value = TVFILT GTVFIL
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GTVFILn(HDU.PRIMARY, VALUE.STRING, "Filter name"),
    /**
     * Guider TV filter position given as filter wheel number or other filter
     * system position measurement.
     * <p>
     * units = 'instrumental'
     * </p>
     * <p>
     * default value = TVFPOS TVFPO%d
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GTVFPO(HDU.PRIMARY, VALUE.REAL, "Filter system position"),
    /**
     * Guider TV filter position given as filter wheel number or other filter
     * system position measurement.
     * <p>
     * units = 'instrumental'
     * </p>
     * <p>
     * default value = TVFPOS TVFPO%d
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GTVFPOn(HDU.PRIMARY, VALUE.REAL, "Filter system position"),
    /**
     * Guider TV filter type. This is the technical specification or observatory
     * identification name.
     * <p>
     * default value = TVFTYP TVFTYP%d
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GTVFTY(HDU.PRIMARY, VALUE.STRING, "Filter type"),
    /**
     * Guider TV filter type. This is the technical specification or observatory
     * identification name.
     * <p>
     * default value = TVFTYP TVFTYP%d
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GTVFTYn(HDU.PRIMARY, VALUE.STRING, "Filter type"),
    /**
     * Guider TV identification and hardware version.
     * <p>
     * default value = TVHWV
     * </p>
     * <p>
     * index = none
     * </p>
     */
    GTVHWV(HDU.PRIMARY, VALUE.STRING, ""),
    /**
     * Times for the guider television sensor measurements given as modified
     * Julian dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = TVMJD%d none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GTVMJD(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Times for the guider television sensor measurements given as modified
     * Julian dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = TVMJD%d none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GTVMJDn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Guider television position angle measurements in appropriate units.
     * <p>
     * default value = TVPAN TVPAN%d
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GTVPAN(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Guider television position angle measurements in appropriate units.
     * <p>
     * default value = TVPAN TVPAN%d
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GTVPANn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Guider television linear position sensor measurements in appropriate
     * units.
     * <p>
     * default value = TVPOS TVPOS%d
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GTVPOS(HDU.PRIMARY, VALUE.REAL, "Television position ()"),
    /**
     * Guider television linear position sensor measurements in appropriate
     * units.
     * <p>
     * default value = TVPOS TVPOS%d
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GTVPOSn(HDU.PRIMARY, VALUE.REAL, "Television position ()"),
    /**
     * Guider television pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = TVPRE TVPRE%d
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GTVPRE(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Guider television pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = TVPRE TVPRE%d
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GTVPREn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Guider TV status.
     * <p>
     * default value = TVSTAT
     * </p>
     * <p>
     * index = none
     * </p>
     */
    GTVSTAT(HDU.PRIMARY, VALUE.STRING, ""),
    /**
     * Guider TV software version.
     * <p>
     * default value = TVSWV
     * </p>
     * <p>
     * index = none
     * </p>
     */
    GTVSWV(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * Guider television temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = TVTEMP TVTEMP%d
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GTVTEM(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Guider television temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = TVTEMP TVTEMP%d
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GTVTEMn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Guider television voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = TVVOL TVVOL%d
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GTVVOL(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Guider television voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = TVVOL TVVOL%d
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GTVVOLn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Guide object declination.
     * <p>
     * units = GUIDECU
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    GUIDEC(HDU.PRIMARY, VALUE.STRING, "Guider declination"),
    /**
     * Declination unit.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    GUIDECU(HDU.PRIMARY, VALUE.STRING, "Declination unit"),
    /**
     * Guider identification and hardware version.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    GUIDEHWV(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * Guider name. Two of the names are 'manual' and 'none' for manual guiding
     * or no guider, respectively.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    GUIDER(HDU.PRIMARY, VALUE.STRING, "Guider name"),
    /**
     * Guider software version.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    GUIDESWV(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * Epoch of the guide object coordinates.
     * <p>
     * default value = TELEPOCH
     * </p>
     * <p>
     * index = none
     * </p>
     */
    GUIEPOCH(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * Guide object coordinate system equinox. A value before 1984 is Besselian
     * otherwise it is Julian.
     * <p>
     * default value = TELEQUIN
     * </p>
     * <p>
     * index = none
     * </p>
     */
    GUIEQUIN(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Times for the guider sensor measurements given as modified Julian dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR GUIMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GUIMJD(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Times for the guider sensor measurements given as modified Julian dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR GUIMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GUIMJDn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Guider position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GUIPAN(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Guider position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GUIPANn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Guider linear position sensor measurements in appropriate units. This
     * might be used for guide probe positions.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GUIPOS(HDU.PRIMARY, VALUE.REAL, "Guider position ()"),
    /**
     * Guider linear position sensor measurements in appropriate units. This
     * might be used for guide probe positions.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GUIPOSn(HDU.PRIMARY, VALUE.REAL, "Guider position ()"),
    /**
     * Guider pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GUIPRE(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Guider pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GUIPREn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Guide object right ascension.
     * <p>
     * units = GUIRAU
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    GUIRA(HDU.PRIMARY, VALUE.STRING, "Guider right ascension"),
    /**
     * Guide object coordinate system type.
     * <p>
     * default value = TELRADEC
     * </p>
     * <p>
     * index = none
     * </p>
     */
    GUIRADEC(HDU.PRIMARY, VALUE.STRING, ""),
    /**
     * Guider correction rate.
     * <p>
     * units = UNITFREQ
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    GUIRATE(HDU.PRIMARY, VALUE.REAL, "Guider rate"),
    /**
     * Right ascension unit.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    GUIRAU(HDU.PRIMARY, VALUE.STRING, "Right ascension unit"),
    /**
     * Guider status.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    GUISTAT(HDU.PRIMARY, VALUE.STRING, ""),
    /**
     * Guider temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GUITEM(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Guider temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GUITEMn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Guider voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GUIVOL(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Guider voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    GUIVOLn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Hour angle at TELMJD.
     * <p>
     * units = UNITRA
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    HA(HDU.PRIMARY, VALUE.STRING, "Hour angle"),
    /**
     * Image creation system hardware version.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    IMAGEHWV(HDU.PRIMARY, VALUE.STRING, "Image creation hardware version"),
    /**
     * The image identification when there are multiple images within an
     * observation. For detectors with CCDs this would be a unique number
     * assigned to each amplifier in the detector.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    IMAGEID(HDU.EXTENSION, VALUE.INTEGER, "Image identification"),
    /**
     * Image creation system software version.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    IMAGESWV(HDU.PRIMARY, VALUE.STRING, "Image creation software version"),
    /**
     * Instrument focus.
     * <p>
     * units = 'instrumental'
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    INSFOCUS(HDU.PRIMARY, VALUE.REAL, "Instrument focus"),
    /**
     * Times for the instrument sensor measurements given as modified Julian
     * dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJD-OBS INSMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    INSMJD(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Times for the instrument sensor measurements given as modified Julian
     * dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJD-OBS INSMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    INSMJDn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Instrument position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    INSPAN(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Instrument position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    INSPANn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Instrument linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    INSPOS(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Instrument linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    INSPOSn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Instrument pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    INSPRE(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Instrument pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    INSPREn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Instrument status.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    INSSTAT(HDU.PRIMARY, VALUE.STRING, "Instrument status"),
    /**
     * Instrument configuration.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    INSTCONF(HDU.PRIMARY, VALUE.STRING, "Instrument configuration"),
    /**
     * Instrument temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    INSTEM(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Instrument temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    INSTEMn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Instrument hardware version.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    INSTHWV(HDU.PRIMARY, VALUE.STRING, "Instrument hardware version"),
    /**
     * Instrument software version.
     * ------------------------------------------------------------------
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    INSTSWV(HDU.PRIMARY, VALUE.STRING, "Instrument software version"),
    /**
     * Instrument voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    INSVOL(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Instrument voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    INSVOLn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * The keyword dictionary defining the keywords. This dictionary should be
     * archived with the data.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    KWDICT(HDU.PRIMARY, VALUE.STRING, "Keyword dictionary"),
    /**
     * Calibration lamp name
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    LAMP(HDU.PRIMARY, VALUE.STRING, "Calibration lamp"),
    /**
     * Calibration lamp type.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    LAMPTYPE(HDU.PRIMARY, VALUE.STRING, "Lamp type"),
    /**
     * Times for the lamp sensor measurements given as modified Julian dates.
     * The MJDHDR keyword may be used for the time at which the image header is
     * created or the MJD-OBS keyword may be used for the time of observation.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJD-OBS LMPMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    LMPMJD(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Times for the lamp sensor measurements given as modified Julian dates.
     * The MJDHDR keyword may be used for the time at which the image header is
     * created or the MJD-OBS keyword may be used for the time of observation.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJD-OBS LMPMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    LMPMJDn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Calibration lamp position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    LMPPAN(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Calibration lamp position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    LMPPANn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Calibration lamp linear position sensor measurements in appropriate
     * units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    LMPPOS(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Calibration lamp linear position sensor measurements in appropriate
     * units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    LMPPOSn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Calibration lamp pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    LMPPRE(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Calibration lamp pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    LMPPREn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Calibration lamp temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    LMPTEM(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Calibration lamp temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    LMPTEMn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Calibration lamp voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    LMPVOL(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Calibration lamp voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    LMPVOLn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Local siderial time at the start of the exposure.
     * <p>
     * units = UNITHOUR
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    LST_OBS("LST-OBS", HDU.PRIMARY_EXTENSION, VALUE.STRING, "LST of exposure start"),
    /**
     * Local siderial time at the end of the exposure.
     * <p>
     * units = UNITHOUR
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    LSTEND(HDU.PRIMARY_EXTENSION, VALUE.STRING, "LST at end of exposure"),
    /**
     * Local siderial time of the header creation.
     * <p>
     * units = UNITHOUR
     * </p>
     * <p>
     * default value = LST-OBS
     * </p>
     * <p>
     * index = none
     * </p>
     */
    LSTHDR(HDU.PRIMARY_EXTENSION, VALUE.STRING, "LST of header creation"),
    /**
     * Default local siderial time for the observation. This keyword is
     * generally not used and is LST-OBS keyword for the start of the exposure
     * on the detector is used.
     * <p>
     * units = UNITHOUR
     * </p>
     * <p>
     * default value = LST-OBS
     * </p>
     * <p>
     * index = none
     * </p>
     */
    LSTOBS(HDU.PRIMARY_EXTENSION, VALUE.STRING, "LST of observation"),
    /**
     * Transformation matrix between CCD and image coordinates. If missing the
     * default is an identify matrix.
     * <p>
     * default value = 0.(i!=j),1.(i=j)
     * </p>
     * <p>
     * index = 1-9,1-9
     * </p>
     */
    LTMn_n(HDU.EXTENSION, VALUE.REAL, "Image transformation matrix"),
    /**
     * Transformation origin vector between CCD and image coordinates.
     * <p>
     * default value = 0.
     * </p>
     * <p>
     * index = 1-9
     * </p>
     */
    LTVn(HDU.EXTENSION, VALUE.REAL, "Image transformation vector"),
    /**
     * The maximum number of scanned (unbinned) lines used to form an output
     * line. This is used with drift scanning or a scan table. For long drift
     * scans this will be the number of lines in the CCD.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    MAXNSCAN(HDU.EXTENSION, VALUE.INTEGER, "Maximum number of scanned lines"),
    /**
     * The minimum number of scanned (unbinned) lines used to form an output
     * line. This is used with drift scanning or a scan table. This will only
     * differ from MAXNSCAN if the initial lines in the output image are from
     * the initial ramp-up.
     * <p>
     * default value = MAXNSCAN
     * </p>
     * <p>
     * index = none
     * </p>
     */
    MINNSCAN(HDU.EXTENSION, VALUE.INTEGER, "Minimum number of scanned lines"),

    /**
     * Modified Julian date when the image header was created by the software.
     * The fractional part of the date is given to better than a second of time.
     * Many header keywords may be sampled or computed at this time and this
     * keyword is the default for these.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJD-OBS
     * </p>
     * <p>
     * index = none
     * </p>
     */
    MJDHDR(HDU.PRIMARY_EXTENSION, VALUE.REAL, "MJD of header creation"),
    /**
     * Default modified Julian date for the observation. The fractional part of
     * the date is given to better than a second of time. This keyword is
     * generally not used and is MJD-OBS keyword for the start of the exposure
     * on the detector is used.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJD-OBS
     * </p>
     * <p>
     * index = none
     * </p>
     */
    MJDOBS(HDU.PRIMARY_EXTENSION, VALUE.REAL, "MJD of observation"),
    /**
     * The number of amplifiers in the detector. When there is only a single
     * amplifier used it may be absent since the default value is 1.
     * <p>
     * default value = 1
     * </p>
     * <p>
     * index = none
     * </p>
     */
    NAMPS(HDU.PRIMARY, VALUE.INTEGER, "Number of Amplifiers"),
    /**
     * The number of CCDs in the detector. This is used with mosaics of CCD
     * detectors. For a single CCD it may be absent since the default value is
     * 1.
     * <p>
     * default value = 1
     * </p>
     * <p>
     * index = none
     * </p>
     */
    NCCDS(HDU.PRIMARY, VALUE.INTEGER, "Number of CCDs"),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    NODANGLE(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    NODDIST(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    NODFREQ(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    NODHWV(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * Times for the nodding system sensor measurements given as modified Julian
     * dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR NODMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    NODMJD(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Times for the nodding system sensor measurements given as modified Julian
     * dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR NODMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    NODMJDn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    NODNCHOP(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * Nodding position angle measurements in appropriate units. Note that
     * NODANGLE should be used for the nodding angle and these keywords are for
     * other system position angle measurements.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    NODPAN(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Nodding position angle measurements in appropriate units. Note that
     * NODANGLE should be used for the nodding angle and these keywords are for
     * other system position angle measurements.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    NODPANn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Nodding system linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    NODPOS(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Nodding system linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    NODPOSn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Nodding system pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    NODPRE(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Nodding system pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    NODPREn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    NODSTAT(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    NODSWV(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * Nodding system temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    NODTEM(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Nodding system temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    NODTEMn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Nodding system voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    NODVOL(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Nodding system voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    NODVOLn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Number of coadded subexposures. When charge shuffling this gives the
     * number of charge shuffled exposures.
     * <p>
     * default value = 1
     * </p>
     * <p>
     * index = none
     * </p>
     */
    NSUBEXPS(HDU.PRIMARY_EXTENSION, VALUE.INTEGER, "Number of subexposures"),
    /**
     * Declination of the target astronomical object(s).
     * <p>
     * units = OBJDECU ODEU%d
     * </p>
     * <p>
     * default value = DEC none
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    OBJDEC(HDU.PRIMARY, VALUE.STRING, "Declination of object"),
    /**
     * Declination unit.
     * <p>
     * default value = DECUNIT OBJDECU
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    OBJDECU(HDU.PRIMARY, VALUE.STRING, "Declination unit"),
    /**
     * Epoch of the target astronomical object coordinate(s). This is given in
     * years.
     * <p>
     * units = 'yr'
     * </p>
     * <p>
     * default value = EPOCH OBJEPOCH
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    OBJEPOCH(HDU.PRIMARY, VALUE.REAL, "Epoch of object coordinates"),
    /**
     * Coordinate system equinox for the target astronomical object(s). A value
     * before 1984 is Besselian otherwise it is Julian.
     * <p>
     * units = 'yr'
     * </p>
     * <p>
     * default value = EQUINOX OBJEQUIN
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    OBJEQUIN(HDU.PRIMARY, VALUE.REAL, "Object coordinate equinox"),
    /**
     * Standard reference or catalog name for the target astronomical object(s).
     * The name should follow IAU standards. These keywords differ from the
     * OBJECT keyword which is used to identify the observation.
     * <p>
     * default value = OBJECT OBJNAME
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    OBJnnn(HDU.PRIMARY, VALUE.STRING, "Target object"),
    /**
     * Right ascension of the target astronomical object(s).
     * <p>
     * units = OBJRAU ORAU%4d
     * </p>
     * <p>
     * default value = RA none
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    OBJRA(HDU.PRIMARY, VALUE.STRING, "Right ascension of object"),
    /**
     * Coordinate system type for the target astronomical object(s).
     * <p>
     * default value = RADECSYS OBJRADEC
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    OBJRADEC(HDU.PRIMARY, VALUE.STRING, "Object coordinate system"),
    /**
     * Right ascension unit.
     * <p>
     * default value = RAUNIT OBJRAU
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    OBJRAU(HDU.PRIMARY, VALUE.STRING, "Right ascension unit"),
    /**
     * Type of target astronomical object(s). This is taken from a dictionary of
     * names yet to be defined. Some common types are 'galaxy', 'star', and
     * 'sky'. If not particular object is targeted the type 'field' may be used.
     * <p>
     * default value = none OBJTYPE
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    OBJTnnn(HDU.PRIMARY, VALUE.STRING, "Type of object"),
    /**
     * Type of target astronomical object(s). This is taken from a dictionary of
     * names yet to be defined. Some common types are 'galaxy', 'star', and
     * 'sky'. If not particular object is targeted the type 'field' may be used.
     * <p>
     * default value = none OBJTYPE
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    OBJTYPE(HDU.PRIMARY, VALUE.STRING, "Type of object"),
    /**
     * Declination of the observation. This may be distinct from the object
     * coordinates and the telescope coordinates. It may be used to indicate the
     * requested observation coordinates.
     * <p>
     * units = OBSDECU
     * </p>
     * <p>
     * default value = DETDEC
     * </p>
     * <p>
     * index = none
     * </p>
     */
    OBSDEC(HDU.PRIMARY, VALUE.STRING, "Observation declination"),
    /**
     * Declination unit.
     * <p>
     * default value = DETDECU
     * </p>
     * <p>
     * index = none
     * </p>
     */
    OBSDECU(HDU.PRIMARY, VALUE.STRING, "Declination unit"),
    /**
     * Epoch of the coordinates used in observation coordinates.
     * <p>
     * units = 'yr'
     * </p>
     * <p>
     * default value = TELEPOCH
     * </p>
     * <p>
     * index = none
     * </p>
     */
    OBSEPOCH(HDU.PRIMARY, VALUE.REAL, "Observation coordinate epoch"),
    /**
     * Equinox of coordinates used in observation coordinates. A value before
     * 1984 is Besselian otherwise it is Julian.
     * <p>
     * units = 'yr'
     * </p>
     * <p>
     * default value = TELEQUIN
     * </p>
     * <p>
     * index = none
     * </p>
     */
    OBSEQUIN(HDU.PRIMARY, VALUE.REAL, "Observation coordinate equinox"),
    /**
     * Observatory identification for the site of the observation.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    OBSERVAT(HDU.PRIMARY, VALUE.STRING, "Observatory"),
    /**
     * The unique observatory observation identification. This serves to
     * identify all data from the same observation.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    OBSID(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Observation identification"),
    /**
     * Right ascension of the observation. This may be distinct from the object
     * coordinates and the telescope coordinates. It may be used to indicate the
     * requested observation coordinates.
     * <p>
     * units = OBSRAU
     * </p>
     * <p>
     * default value = DETRA
     * </p>
     * <p>
     * index = none
     * </p>
     */
    OBSRA(HDU.PRIMARY, VALUE.STRING, "Observation right ascension"),
    /**
     * Coordinate system used in observation coordinates.
     * <p>
     * default value = TELRADEC
     * </p>
     * <p>
     * index = none
     * </p>
     */
    OBSRADEC(HDU.PRIMARY, VALUE.STRING, "Observation coordinate system"),
    /**
     * Right ascension unit.
     * <p>
     * default value = DETRAU
     * </p>
     * <p>
     * index = none
     * </p>
     */
    OBSRAU(HDU.PRIMARY, VALUE.STRING, "Right ascension unit"),
    /**
     * Name(s) of the observers.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    OBSRVRnn(HDU.PRIMARY, VALUE.STRING, "Observer(s)"),
    /**
     * Status of the observation.
     * -----------------------------------------------------------------
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    OBSSTAT(HDU.PRIMARY, VALUE.STRING, "Observation status"),
    /**
     * The type of observation such as an astronomical exposure or a particular
     * type of calibration exposure.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    OBSTYPE(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Observation type"),
    /**
     * Declination of the target astronomical object(s).
     * <p>
     * units = OBJDECU ODEU%d
     * </p>
     * <p>
     * default value = DEC none
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    ODECnnn(HDU.PRIMARY, VALUE.STRING, "Declination of object"),
    /**
     * Declination unit.
     * <p>
     * default value = DECUNIT OBJDECU
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    ODEUnnn(HDU.PRIMARY, VALUE.STRING, "Declination unit"),
    /**
     * Epoch of the target astronomical object coordinate(s). This is given in
     * years.
     * <p>
     * units = 'yr'
     * </p>
     * <p>
     * default value = EPOCH OBJEPOCH
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    OEPOnnn(HDU.PRIMARY, VALUE.REAL, "Epoch of object coordinates"),
    /**
     * Coordinate system equinox for the target astronomical object(s). A value
     * before 1984 is Besselian otherwise it is Julian.
     * <p>
     * units = 'yr'
     * </p>
     * <p>
     * default value = EQUINOX OBJEQUIN
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    OEQUnnn(HDU.PRIMARY, VALUE.REAL, "Object coordinate equinox"),
    /**
     * Right ascension of the target astronomical object(s).
     * <p>
     * units = OBJRAU ORAU%4d
     * </p>
     * <p>
     * default value = RA none
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    ORAnnn(HDU.PRIMARY, VALUE.STRING, "Right ascension of object"),
    /**
     * Right ascension unit.
     * <p>
     * default value = RAUNIT OBJRAU
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    ORAUnnn(HDU.PRIMARY, VALUE.STRING, "Right ascension unit"),
    /**
     * Coordinate system type for the target astronomical object(s).
     * <p>
     * default value = RADECSYS OBJRADEC
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    ORDSnnn(HDU.PRIMARY, VALUE.STRING, "Object coordinate system"),
    /**
     * Status of calibration to data proportional to photons. For CCD data this
     * means bias section correction, zero level calibration, dark count
     * calibration, and flat field calibration.
     * <p>
     * default value = F
     * </p>
     * <p>
     * index = none
     * </p>
     */
    PHOTCAL(HDU.PRIMARY_EXTENSION, VALUE.LOGICAL, "Data proportional to photons?"),
    /**
     * Photometric conditions during the observation.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    PHOTOMET(HDU.PRIMARY, VALUE.STRING, "Photometric conditions"),
    /**
     * Processing hardware used.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-99
     * </p>
     */
    PIPEHW(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Processing hardware"),
    /**
     * Processing hardware used.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-99
     * </p>
     */
    PIPEHWn(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Processing hardware"),
    /**
     * Name of processing pipeline applied.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    PIPELINE(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Pipeline used"),
    /**
     * Processing software version.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-99
     * </p>
     */
    PIPESW(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Processing software"),
    /**
     * Processing software version.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-99
     * </p>
     */
    PIPESWn(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Processing software"),
    /**
     * Projected pixel scale along axis n.
     * <p>
     * units = UNITSEP/pixel
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = 1-9
     * </p>
     */
    PIXSCALn(HDU.PRIMARY, VALUE.REAL, "Pixel scale"),
    /**
     * Unbinned pixel size along each dimension given in appropriate units. The
     * units should be indicated in the comment. The projected pixel size in arc
     * seconds or wavelength are given by other parameters.
     * <p>
     * units = 'um'
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = 1-9
     * </p>
     */
    PIXSIZEn(HDU.PRIMARY_EXTENSION, VALUE.REAL, "Pixel size"),
    /**
     * Pixel limit for region occupied by the spectrum.
     * <p>
     * units = 'pixel' 'pixel'
     * </p>
     * <p>
     * default value = none PMAX1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    PMAX1(HDU.EXTENSION, VALUE.REAL, "Spectrum pixel limit"),
    /**
     * Pixel limit for region occupied by the spectrum.
     * <p>
     * units = 'pixel' 'pixel'
     * </p>
     * <p>
     * default value = none PMAX2
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    PMAX2(HDU.EXTENSION, VALUE.REAL, "Spectrum pixel limit"),
    /**
     * Pixel limit for region occupied by the spectrum.
     * <p>
     * units = 'pixel' 'pixel'
     * </p>
     * <p>
     * default value = none PMIN1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    PMIN1(HDU.EXTENSION, VALUE.REAL, "Spectrum pixel limit"),
    /**
     * Pixel limit for region occupied by the spectrum.
     * <p>
     * units = 'pixel' 'pixel'
     * </p>
     * <p>
     * default value = none CMIN2
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    PMIN2(HDU.EXTENSION, VALUE.REAL, "Spectrum pixel limit"),
    /**
     * Pixel limit for region occupied by the spectrum.
     * <p>
     * units = 'pixel' 'pixel'
     * </p>
     * <p>
     * default value = none PMIN1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    PMN1nnn(HDU.EXTENSION, VALUE.REAL, "Spectrum pixel limit"),
    /**
     * Pixel limit for region occupied by the spectrum.
     * <p>
     * units = 'pixel' 'pixel'
     * </p>
     * <p>
     * default value = none CMIN2
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    PMN2n(HDU.EXTENSION, VALUE.REAL, "Spectrum pixel limit"),
    /**
     * Pixel limit for region occupied by the spectrum.
     * <p>
     * units = 'pixel' 'pixel'
     * </p>
     * <p>
     * default value = none PMAX1
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    PMX1n(HDU.EXTENSION, VALUE.REAL, "Spectrum pixel limit"),
    /**
     * Pixel limit for region occupied by the spectrum.
     * <p>
     * units = 'pixel' 'pixel'
     * </p>
     * <p>
     * default value = none PMAX2
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    PMX2n(HDU.EXTENSION, VALUE.REAL, "Spectrum pixel limit"),
    /**
     * CCD preflash time. If the times in the extension are different the
     * primary HDU gives one of the extension times.
     * <p>
     * units = UNITTIME
     * </p>
     * <p>
     * default value = 0.
     * </p>
     * <p>
     * index = none
     * </p>
     */
    PREFLASH(HDU.PRIMARY_EXTENSION, VALUE.REAL, "Preflash time"),
    /**
     * Processing log information formatted as FITS comments.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = 1-9999
     * </p>
     */
    PROCnnn(HDU.PRIMARY_EXTENSION, VALUE.STRING, ""),
    /**
     * Processing status.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    PROCSTAT(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Processing status"),
    /**
     * The unique observatory proposal identification.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    PROPID(HDU.PRIMARY, VALUE.STRING, "Proposal identification"),
    /**
     * The name or title of the proposal.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    PROPOSAL(HDU.PRIMARY, VALUE.STRING, "Proposal title"),
    /**
     * Name(s) of the proposers.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    PROPOSER(HDU.PRIMARY, VALUE.STRING, "Proposer(s)"),
    /**
     * Name(s) of the proposers.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    PROPSRnn(HDU.PRIMARY, VALUE.STRING, "Proposer(s)"),
    /**
     * Default coordinate system equinox. A value before 1984 is Besselian
     * otherwise it is Julian. If absent the default is J2000.
     * <p>
     * units = 'yr'
     * </p>
     * <p>
     * default value = 2000.
     * </p>
     * <p>
     * index = none
     * </p>
     */
    RADECEQ(HDU.PRIMARY_EXTENSION, VALUE.REAL, "Default coordinate equinox"),

    /**
     * Projected position angle of the positive right ascension axis on the
     * detector. The position angle is measured clockwise from the image y axis.
     * <p>
     * units = UNITPA
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    RAPANGL(HDU.PRIMARY, VALUE.REAL, "Position angle of RA axis"),
    /**
     * Default right ascension units.
     * <p>
     * default value = UNITRA
     * </p>
     * <p>
     * index = none
     * </p>
     */
    RAUNIT(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Right ascension unit"),
    /**
     * CCD readout noise in rms electrons. This is the most current estimate.
     * <p>
     * units = 'e'
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    RDNOISE(HDU.EXTENSION, VALUE.REAL, "Readout noise"),
    /**
     * Amplifier unbinned pixel read time.
     * <p>
     * units = 'ns'
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none none
     * </p>
     */
    READTIME(HDU.PRIMARY_EXTENSION, VALUE.REAL, "Unbinned pixel read time"),
    /**
     * Archive identification. This may be the same as the observation
     * identification.
     * <p>
     * default value = OBSID none
     * </p>
     * <p>
     * index = none none
     * </p>
     */
    RECNO(HDU.PRIMARY, VALUE.STRING, "Archive identification"),
    /**
     * Seeing estimates specified as the stellar full-width at half-maximum in
     * arc seconds. There may be more than one estimate. The times of the
     * estimates are given by the SEEMJDn keyword.
     * <p>
     * units = UNITSEP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    SEEING(HDU.PRIMARY, VALUE.REAL, "FWHM"),
    /**
     * Seeing estimates specified as the stellar full-width at half-maximum in
     * arc seconds. There may be more than one estimate. The times of the
     * estimates are given by the SEEMJDn keyword.
     * <p>
     * units = UNITSEP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    SEEINGn(HDU.PRIMARY, VALUE.REAL, "FWHM"),
    /**
     * Times for the seeing estimates given as modified Julian dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR SEEMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    SEEMJD(HDU.PRIMARY, VALUE.REAL, "MJD for seeing estimate"),
    /**
     * Times for the seeing estimates given as modified Julian dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR SEEMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    SEEMJDn(HDU.PRIMARY, VALUE.REAL, "MJD for seeing estimate"),
    /**
     * Exposure time of the nth subexposure. If all subexposures are the same
     * length then only the first keyword, SEXP, is needed. For charge shuffling
     * the subexposure time is the total time for each charge shuffled exposure.
     * There is no finer division of the exposure times. Comments would be used
     * to describe the subexposures of each charge shuffled subexposure.
     * <p>
     * units = UNITTIME
     * </p>
     * <p>
     * default value = none SEXP
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    SEXP(HDU.PRIMARY_EXTENSION, VALUE.REAL, "Subexposure time"),
    /**
     * Exposure time of the nth subexposure. If all subexposures are the same
     * length then only the first keyword, SEXP, is needed. For charge shuffling
     * the subexposure time is the total time for each charge shuffled exposure.
     * There is no finer division of the exposure times. Comments would be used
     * to describe the subexposures of each charge shuffled subexposure.
     * <p>
     * units = UNITTIME
     * </p>
     * <p>
     * default value = none SEXP
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    SEXPnnn(HDU.PRIMARY_EXTENSION, VALUE.REAL, "Subexposure time"),
    /**
     * Time for the shutter to close fully.
     * <p>
     * units = 'ms'
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    SHUTCLOS(HDU.PRIMARY, VALUE.REAL, "Shutter close time"),
    /**
     * Shutter identification and hardware version.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    SHUTHWV(HDU.PRIMARY, VALUE.STRING, "Shutter hardware version"),
    /**
     * Time for the shutter to open fully.
     * <p>
     * units = 'ms'
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    SHUTOPEN(HDU.PRIMARY, VALUE.REAL, "Shutter open time"),
    /**
     * Shutter status.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    SHUTSTAT(HDU.PRIMARY, VALUE.STRING, "Shutter status"),
    /**
     * Shutter software version.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    SHUTSWV(HDU.PRIMARY, VALUE.STRING, "Shutter software version"),
    /**
     * Slit or mask hole identification for the aperture(s). The string consists
     * of a number, an object type number (0=sky, 1=object, etc.), the right
     * ascension and declination, and the object name or title. declination, and
     * the object name or title. This can replace OBJNAME, APRA/OBJRA, and
     * APDEC/OBJDEC.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    SLIT(HDU.PRIMARY, VALUE.STRING, ""),
    /**
     * Slit or mask hole identification for the aperture(s). The string consists
     * of a number, an object type number (0=sky, 1=object, etc.), the right
     * ascension and declination, and the object name or title. declination, and
     * the object name or title. This can replace OBJNAME, APRA/OBJRA, and
     * APDEC/OBJDEC.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    SLITnnn(HDU.PRIMARY, VALUE.STRING, ""),
    /**
     * FWHM of the object spectrum profile on the detector. The width is in the
     * units of the spatial world coordinate system. This may be approximate. It
     * is particularly useful for specifying the profile width of fiber fed
     * spectra.
     * <p>
     * units = CUNIT2 CUN2%4d
     * </p>
     * <p>
     * default value = none SPECFWHM
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    SPECFWHM(HDU.EXTENSION, VALUE.REAL, "FWHM of spectrum"),
    /**
     * UTC of the start of each subexposure.
     * <p>
     * units = UNITHOUR
     * </p>
     * <p>
     * default value = none SUT
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    SUT(HDU.PRIMARY_EXTENSION, VALUE.STRING, "UTC of subexposure start"),
    /**
     * UTC of the start of each subexposure.
     * <p>
     * units = UNITHOUR
     * </p>
     * <p>
     * default value = none SUT
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    SUTn(HDU.PRIMARY_EXTENSION, VALUE.STRING, "UTC of subexposure start"),
    /**
     * FWHM of the object spectrum profile on the detector. The width is in the
     * units of the spatial world coordinate system. This may be approximate. It
     * is particularly useful for specifying the profile width of fiber fed
     * spectra.
     * <p>
     * units = CUNIT2 CUN2%4d
     * </p>
     * <p>
     * default value = none SPECFWHM
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    SWIDnnn(HDU.EXTENSION, VALUE.REAL, "FWHM of spectrum"),
    /**
     * Modified Julian date at the time of the altitude/azimuth keywords.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TELAAMJD(HDU.PRIMARY, VALUE.REAL, "MJD at for alt/az"),
    /**
     * Telescope pointing altitude at the time given by TELAAMJD.
     * <p>
     * units = UNITALT
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TELALT(HDU.PRIMARY, VALUE.STRING, "Telescope altitude"),
    /**
     * Telescope pointing azimuth at the time given by TELAAMJD.
     * <p>
     * units = UNITAZ
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TELAZ(HDU.PRIMARY, VALUE.STRING, "Telescope azimuth"),
    /**
     * Telescope configuration. The configuration defines the mirrors,
     * correctors, light paths, etc.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TELCONF(HDU.PRIMARY, VALUE.STRING, "Telescope configuration"),
    /**
     * Telescope pointing declination.
     * <p>
     * units = TELDECU
     * </p>
     * <p>
     * default value = DEC
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TELDEC(HDU.PRIMARY, VALUE.STRING, "Telescope declination"),
    /**
     * Declination unit.
     * <p>
     * default value = DECUNIT
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TELDECU(HDU.PRIMARY, VALUE.STRING, "Declination unit"),
    /**
     * Telescope pointing coordinate epoch.
     * <p>
     * units = 'yr'
     * </p>
     * <p>
     * default value = EPOCH
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TELEPOCH(HDU.PRIMARY, VALUE.REAL, "Telescope coordinate epoch"),
    /**
     * Telescope pointing coordinate system equinox. A value before 1984 is
     * Besselian otherwise it is Julian.
     * <p>
     * units = 'yr'
     * </p>
     * <p>
     * default value = EQUINOX
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TELEQUIN(HDU.PRIMARY, VALUE.REAL, "Telescope coordinate equinox"),
    /**
     * Telescope focus value in available units.
     * <p>
     * units = 'instrumental'
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TELFOCUS(HDU.PRIMARY, VALUE.REAL, "Telescope focus"),
    /**
     * Time of zenith distance and hour angle
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TELMJD(HDU.PRIMARY, VALUE.REAL, "Time of zenith distance and hour angle"),
    /**
     * Times for the telescope sensor measurements given as modified Julian
     * dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR TELMJD
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    TELMJDn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Telescope position angle measurements in appropriate units. This could
     * include altitude and azimuth measurements.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    TELPAN(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Telescope position angle measurements in appropriate units. This could
     * include altitude and azimuth measurements.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    TELPANn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Telescope linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    TELPOS(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Telescope linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    TELPOSn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Telescope pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    TELPRE(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Telescope pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    TELPREn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Telescope pointing right ascension.
     * <p>
     * units = TELRAU
     * </p>
     * <p>
     * default value = RA
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TELRA(HDU.PRIMARY, VALUE.STRING, "Telescope right ascension"),
    /**
     * Telescope pointing coordinate system type.
     * <p>
     * default value = RADECSYS
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TELRADEC(HDU.PRIMARY, VALUE.STRING, "Telescope coordinate system"),
    /**
     * Right ascension unit.
     * <p>
     * default value = RAUNIT
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TELRAU(HDU.PRIMARY, VALUE.STRING, "Right ascension unit"),
    /**
     * Telescope status.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TELSTAT(HDU.PRIMARY, VALUE.STRING, "Telescope status"),
    /**
     * Telescope control system software version.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TELTCS(HDU.PRIMARY, VALUE.STRING, "Telescope control system"),
    /**
     * Telescope temperature sensor measurements in degrees Celsius. The comment
     * string may be modified to indicate the location of the measurement.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    TELTEM(HDU.PRIMARY, VALUE.REAL, "Telescope temperature"),
    /**
     * Telescope temperature sensor measurements in degrees Celsius. The comment
     * string may be modified to indicate the location of the measurement.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    TELTEMn(HDU.PRIMARY, VALUE.REAL, "Telescope temperature"),
    /**
     * Declination telescope tracking rate in arc seconds per second.
     * <p>
     * units = UNITRATE
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TELTKDEC(HDU.PRIMARY, VALUE.REAL, "Tracking rate from siderial"),
    /**
     * Right ascension telescope tracking rate from siderial in arc seconds per
     * second.
     * <p>
     * units = UNITRATE
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TELTKRA(HDU.PRIMARY, VALUE.REAL, "Tracking rate from siderial"),
    /**
     * Telescope hardware version.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TELVER(HDU.PRIMARY, VALUE.STRING, "Telescope version"),
    /**
     * Telescope voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    TELVOL(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Telescope voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-999
     * </p>
     */
    TELVOLn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Time of exposure end in the TSYSEND system.
     * <p>
     * units = UNITHOUR
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TIMEEND(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Time of exposure end"),
    /**
     * Time of header creation.
     * <p>
     * units = UNITHOUR
     * </p>
     * <p>
     * default value = TIMESYS
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TIMEHDR(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Time of header creation"),
    /**
     * Default time system. All times which do not have a "timesys" element
     * associated with them in this dictionary default to this keyword. .
     * <p>
     * default value = 'UTC'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TIMESYS(HDU.PRIMARY, VALUE.STRING, "Default time system"),
    /**
     * Section of the recorded image to be kept after calibration processing.
     * This is generally the part of the data section containing useful data.
     * The section is in in binned pixels if binning is done.
     * <p>
     * default value = DATASEC
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TRIMSEC(HDU.EXTENSION, VALUE.STRING, "Section of useful data"),
    /**
     * Time system for the TIMEEND keyword.
     * <p>
     * default value = TIMESYS
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TSYSEND(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Time system for TIMEEND"),
    /**
     * Time system for the header creation keywords.
     * <p>
     * default value = TIMESYS
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TSYSHDR(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Time system for header creation"),
    /**
     * Time system for the TIME-OBS keyword.
     * <p>
     * default value = TIMESYS
     * </p>
     * <p>
     * index = none
     * </p>
     */
    TSYSOBS(HDU.PRIMARY_EXTENSION, VALUE.STRING, "Time system for TIME-OBS"),
    /**
     * TV name.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-9
     * </p>
     */
    TV(HDU.PRIMARY, VALUE.STRING, "TV"),
    /**
     * TV filter names. This name is the astronomical standard name if
     * applicable; i.e. U, B, Gunn I, etc. The filter type and filter device
     * position are given by other keywords.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = 1-9 1-9,1-9
     * </p>
     */
    TVFILTn(HDU.PRIMARY, VALUE.STRING, "Filter name"),
    /**
     * Television focus value in available units.
     * <p>
     * units = 'instrumental'
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = 1-9 1-9,1-9
     * </p>
     */
    TVFOCn(HDU.PRIMARY, VALUE.REAL, "Television focus"),
    /**
     * TV filter position given as filter wheel number or other filter system
     * position measurement.
     * <p>
     * units = 'instrumental'
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = 1-9 1-9,1-9
     * </p>
     */
    TVFPOSn(HDU.PRIMARY, VALUE.REAL, "Filter system position"),
    /**
     * TV filter type. This is the technical specification or observatory
     * identification name.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = 1-9 1-9,1-9
     * </p>
     */
    TVFTYPn(HDU.PRIMARY, VALUE.STRING, "Filter type"),
    /**
     * TV identification and hardware version.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-9
     * </p>
     */
    TVHWV(HDU.PRIMARY, VALUE.STRING, "TV Hardware"),
    /**
     * Times for the guider television sensor measurements given as modified
     * Julian dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR TVMJD%d
     * </p>
     * <p>
     * index = 1-9 1-9,1-9
     * </p>
     */
    TVMJDn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * TV name.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-9
     * </p>
     */
    TVn(HDU.PRIMARY, VALUE.STRING, "TV"),
    /**
     * TV filter names. This name is the astronomical standard name if
     * applicable; i.e. U, B, Gunn I, etc. The filter type and filter device
     * position are given by other keywords.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = 1-9 1-9,1-9
     * </p>
     */
    TVnFILTn(HDU.PRIMARY, VALUE.STRING, "Filter name"),
    /**
     * Television focus value in available units.
     * <p>
     * units = 'instrumental'
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = 1-9 1-9,1-9
     * </p>
     */
    TVnFOCn(HDU.PRIMARY, VALUE.REAL, "Television focus"),
    /**
     * TV filter position given as filter wheel number or other filter system
     * position measurement.
     * <p>
     * units = 'instrumental'
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = 1-9 1-9,1-9
     * </p>
     */
    TVnFPOSn(HDU.PRIMARY, VALUE.REAL, "Filter system position"),
    /**
     * TV filter type. This is the technical specification or observatory
     * identification name.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = 1-9 1-9,1-9
     * </p>
     */
    TVnFTYPn(HDU.PRIMARY, VALUE.STRING, "Filter type"),
    /**
     * TV identification and hardware version.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-9
     * </p>
     */
    TVnHWV(HDU.PRIMARY, VALUE.STRING, "TV Hardware"),
    /**
     * Times for the guider television sensor measurements given as modified
     * Julian dates.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = MJDHDR TVMJD%d
     * </p>
     * <p>
     * index = 1-9 1-9,1-9
     * </p>
     */
    TVnMJDn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Television position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = 1-9 1-9,1-9
     * </p>
     */
    TVnPANn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Television linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = 1-9 1-9,1-9
     * </p>
     */
    TVnPOSn(HDU.PRIMARY, VALUE.REAL, "Television position ()"),
    /**
     * Television pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = 1-9 1-9,1-9
     * </p>
     */
    TVnPREn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * TV status.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-9
     * </p>
     */
    TVnSTAT(HDU.PRIMARY, VALUE.STRING, ""),
    /**
     * TV software version.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-9
     * </p>
     */
    TVnSWV(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * Television temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = 1-9 1-9,1-9
     * </p>
     */
    TVnTEMn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Television voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = 1-9 1-9,1-9
     * </p>
     */
    TVnVOLn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Television position angle measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = 1-9 1-9,1-9
     * </p>
     */
    TVPANn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Television linear position sensor measurements in appropriate units.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = 1-9 1-9,1-9
     * </p>
     */
    TVPOSn(HDU.PRIMARY, VALUE.REAL, "Television position ()"),
    /**
     * Television pressure sensor measurements in appropriate units.
     * <p>
     * units = UNITPRES
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = 1-9 1-9,1-9
     * </p>
     */
    TVPREn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * TV status.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-9
     * </p>
     */
    TVSTAT(HDU.PRIMARY, VALUE.STRING, ""),
    /**
     * TV software version.
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = none 1-9
     * </p>
     */
    TVSWV(HDU.PRIMARY, VALUE.NONE, ""),
    /**
     * Television temperature sensor measurements in degrees Celsius.
     * <p>
     * units = UNITTEMP
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = 1-9 1-9,1-9
     * </p>
     */
    TVTEMn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Television voltage sensor measurements in volts.
     * <p>
     * units = UNITVOLT
     * </p>
     * <p>
     * default value = none none
     * </p>
     * <p>
     * index = 1-9 1-9,1-9
     * </p>
     */
    TVVOLn(HDU.PRIMARY, VALUE.REAL, ""),
    /**
     * Altitude unit.
     * <p>
     * default value = UNITANG
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITALT(HDU.PRIMARY, VALUE.STRING, "Altitude unit"),
    /**
     * Plane angle unit.
     * <p>
     * default value = 'deg'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITANG(HDU.PRIMARY, VALUE.STRING, "Plane angle unit"),
    /**
     * Focal plane aperture size unit.
     * <p>
     * default value = 'arcsec'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITAP(HDU.PRIMARY, VALUE.STRING, "Aperture size unit"),
    /**
     * Area unit.
     * <p>
     * default value = 'pixel'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITAREA(HDU.PRIMARY, VALUE.STRING, "Area unit"),
    /**
     * Azimuth unit.
     * <p>
     * default value = UNITANG
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITAZ(HDU.PRIMARY, VALUE.STRING, "Azimuth unit"),
    /**
     * Capacitance unit.
     * <p>
     * default value = 'F'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITCAP(HDU.PRIMARY, VALUE.STRING, "Capacitance unit"),
    /**
     * Charge unit.
     * <p>
     * default value = 'C'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITCHAR(HDU.PRIMARY, VALUE.STRING, "Charge unit"),
    /**
     * Conductance unit.
     * <p>
     * default value = 'S'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITCOND(HDU.PRIMARY, VALUE.STRING, "Conductance unit"),
    /**
     * Current unit.
     * <p>
     * default value = 'A'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITCUR(HDU.PRIMARY, VALUE.STRING, "Current unit"),
    /**
     * Delination unit.
     * <p>
     * default value = 'deg'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITDEC(HDU.PRIMARY, VALUE.STRING, "Declination unit"),
    /**
     * Energy unit.
     * <p>
     * default value = 'J'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITENER(HDU.PRIMARY, VALUE.STRING, "Energy unit"),
    /**
     * Event unit.
     * <p>
     * default value = 'count'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITEVNT(HDU.PRIMARY, VALUE.STRING, "Event unit"),
    /**
     * Flux unit.
     * <p>
     * default value = 'Jy'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITFLUX(HDU.PRIMARY, VALUE.STRING, "Flux unit"),
    /**
     * Force unit.
     * <p>
     * default value = 'N'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITFORC(HDU.PRIMARY, VALUE.STRING, "Force unit"),
    /**
     * Frequency unit.
     * <p>
     * default value = 'Hz'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITFREQ(HDU.PRIMARY, VALUE.STRING, "Frequency unit"),
    /**
     * Time of day unit.
     * <p>
     * default value = h
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITHOUR(HDU.PRIMARY, VALUE.STRING, "Time of day unit"),
    /**
     * Illuminance unit.
     * <p>
     * default value = 'lux'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITILLU(HDU.PRIMARY, VALUE.STRING, "Illuminance unit"),
    /**
     * Inductance unit.
     * <p>
     * default value = 'H'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITINDU(HDU.PRIMARY, VALUE.STRING, "Inductance unit"),
    /**
     * Latitude unit.
     * <p>
     * default value = UNITANG
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITLAT(HDU.PRIMARY, VALUE.STRING, "Latitude unit"),
    /**
     * Length unit. A wavelength unit is also provided so this unit is primarily
     * used to instrumental descriptions.
     * <p>
     * default value = 'm'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITLEN(HDU.PRIMARY, VALUE.STRING, "Length unit"),
    /**
     * Luminous flux unit.
     * <p>
     * default value = 'lm'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITLFLX(HDU.PRIMARY, VALUE.STRING, "Luminous flux unit"),
    /**
     * Luminous intensity unit.
     * <p>
     * default value = 'cd'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITLINT(HDU.PRIMARY, VALUE.STRING, "Luminous intensity unit"),
    /**
     * Longitude unit.
     * <p>
     * default value = UNITANG
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITLONG(HDU.PRIMARY, VALUE.STRING, "Longitude unit"),
    /**
     * Mass unit.
     * <p>
     * default value = 'kg'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITMASS(HDU.PRIMARY, VALUE.STRING, "Mass unit"),
    /**
     * Magnetic density unit.
     * <p>
     * default value = 'T'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITMDEN(HDU.PRIMARY, VALUE.STRING, "Magnetic density unit"),
    /**
     * Magnetic field unit.
     * <p>
     * default value = 'G'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITMFLD(HDU.PRIMARY, VALUE.STRING, "Magnetic field unit"),
    /**
     * Magnetic flux unit.
     * <p>
     * default value = 'Wb'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITMFLX(HDU.PRIMARY, VALUE.STRING, "Magnetic flux unit"),
    /**
     * Position angle unit.
     * <p>
     * default value = UNITANG
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITPA(HDU.PRIMARY, VALUE.STRING, "Position angle unit"),
    /**
     * Power unit.
     * <p>
     * default value = 'W'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITPOW(HDU.PRIMARY, VALUE.STRING, "Wavelength unit"),
    /**
     * Pressure unit.
     * <p>
     * default value = 'Pa'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITPRES(HDU.PRIMARY, VALUE.STRING, "Pressure unit"),
    /**
     * Right ascension unit.
     * <p>
     * default value = 'hr'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITRA(HDU.PRIMARY, VALUE.STRING, "Right ascension unit"),
    /**
     * Celestial rate of motion.
     * <p>
     * default value = arcsec/sec
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITRATE(HDU.PRIMARY, VALUE.STRING, "Celestial rate of motion"),
    /**
     * Resistance unit.
     * <p>
     * default value = 'Ohm'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITRES(HDU.PRIMARY, VALUE.STRING, "Resistance unit"),
    /**
     * Solid angle unit.
     * <p>
     * default value = 'sr'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITSANG(HDU.PRIMARY, VALUE.STRING, "Solid angle unit"),
    /**
     * Celestial separation unit.
     * <p>
     * default value = 'arcsec'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITSEP(HDU.PRIMARY, VALUE.STRING, "Separation unit"),
    /**
     * Temperature unit.
     * <p>
     * default value = 'K'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITTEMP(HDU.PRIMARY, VALUE.STRING, "Temperature unit"),
    /**
     * Time unit.
     * <p>
     * default value = 's'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITTIME(HDU.PRIMARY, VALUE.STRING, "Time unit"),
    /**
     * Velocity unit.
     * <p>
     * default value = 'km/s'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITVEL(HDU.PRIMARY, VALUE.STRING, "Velocity unit"),
    /**
     * Voltage unit.
     * <p>
     * default value = 'V'
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UNITVOLT(HDU.PRIMARY, VALUE.STRING, "Voltage unit"),
    /**
     * UTC time at the start of the exposure.
     * <p>
     * units = UNITHOUR
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UTC_OBS("UTC-OBS", HDU.PRIMARY_EXTENSION, VALUE.STRING, "UTC of exposure start"),
    /**
     * UTC at the end of the exposure.
     * <p>
     * units = UNITHOUR
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UTCEND(HDU.PRIMARY_EXTENSION, VALUE.STRING, "UTC at end of exposure"),
    /**
     * UTC of header creation.
     * <p>
     * units = UNITHOUR
     * </p>
     * <p>
     * default value = UTC-OBS
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UTCHDR(HDU.PRIMARY_EXTENSION, VALUE.STRING, "UTC of header creation"),
    /**
     * Default UTC time for the observation. This keyword is generally not used
     * and is UTC-OBS keyword for the start of the exposure on the detector is
     * used.
     * <p>
     * units = UNITHOUR
     * </p>
     * <p>
     * default value = UTC-OBS
     * </p>
     * <p>
     * index = none
     * </p>
     */
    UTCOBS(HDU.PRIMARY_EXTENSION, VALUE.STRING, "UTC of observation"),
    /**
     * IRAF WCS attribute strings for all axes. These are defined by the IRAF
     * WCS system.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = 1-999
     * </p>
     */
    WAT_nnn(HDU.PRIMARY_EXTENSION, VALUE.STRING, ""),
    /**
     * IRAF WCS attribute strings. These are defined by the IRAF WCS system.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = 1-9,1-999
     * </p>
     */
    WATn_nnn(HDU.PRIMARY_EXTENSION, VALUE.STRING, ""),
    /**
     * Descriptive string identifying the source of the astrometry used to
     * derive the WCS. One example is the exposure used to derive a WCS apart
     * from the reference coordinate.
     * <p>
     * default value = none WCSASTRM
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    WCSAnnn(HDU.PRIMARY_EXTENSION, VALUE.STRING, "WCS Source"),
    /**
     * Descriptive string identifying the source of the astrometry used to
     * derive the WCS. One example is the exposure used to derive a WCS apart
     * from the reference coordinate.
     * <p>
     * default value = none WCSASTRM
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    WCSASTRM(HDU.PRIMARY_EXTENSION, VALUE.STRING, "WCS Source"),
    /**
     * Dimensionality of the WCS physical system. In IRAF a WCS can have a
     * higher dimensionality than the image.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    WCSDIM(HDU.PRIMARY_EXTENSION, VALUE.INTEGER, "WCS dimensionality"),
    /**
     * Epoch of the coordinates used in the world coordinate system.
     * <p>
     * units = 'yr' 'yr'
     * </p>
     * <p>
     * default value = CCDEPOCH WCSEPOCH
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    WCSEnnn(HDU.PRIMARY_EXTENSION, VALUE.REAL, "WCS coordinate epoch"),
    /**
     * Equinox when equatorial coordinates are used in the world coordinate
     * system. A value before 1984 is Besselian otherwise it is Julian.
     * <p>
     * units = 'yr' 'yr'
     * </p>
     * <p>
     * default value = CCDEQUIN EQUINOX
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    WCSEPOCH(HDU.PRIMARY_EXTENSION, VALUE.REAL, "WCS coordinate epoch"),
    /**
     * Coordinate system type when equatorial coordinates are used in the world
     * coordinate system.
     * <p>
     * default value = CCDRADEC WCSRADEC
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    WCSRADEC(HDU.PRIMARY_EXTENSION, VALUE.STRING, "WCS coordinate system"),
    /**
     * Coordinate system type when equatorial coordinates are used in the world
     * coordinate system.
     * <p>
     * default value = CCDRADEC WCSRADEC
     * </p>
     * <p>
     * index = none 1-9999
     * </p>
     */
    WCSRnnn(HDU.PRIMARY_EXTENSION, VALUE.STRING, "WCS coordinate system"),
    /**
     * Weather condition description. Generally this would be either 'clear' or
     * 'partly cloudy'.
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    WEATHER(HDU.PRIMARY, VALUE.STRING, "Weather conditions"),
    /**
     * Zenith distance of telescope pointing at TELMJD.
     * <p>
     * units = UNITANG
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    ZD(HDU.PRIMARY, VALUE.REAL, "Zenith distance"),
    /**
     * Modified Julian date at the start of the exposure. The fractional part of
     * the date is given to better than a second of time.
     * <p>
     * units = 'd'
     * </p>
     * <p>
     * default value = none
     * </p>
     * <p>
     * index = none
     * </p>
     */
    MJD_OBS("MJD-OBS", HDU.PRIMARY_EXTENSION, VALUE.REAL, "MJD of exposure start");

    @SuppressWarnings("CPD-START")
    private final IFitsHeader key;

    NOAOExt(HDU hdu, VALUE valueType, String comment) {
        this.key = new FitsHeaderImpl(name(), IFitsHeader.SOURCE.NOAO, hdu, valueType, comment);
    }

    NOAOExt(String key, HDU hdu, VALUE valueType, String comment) {
        this.key = new FitsHeaderImpl(name(), IFitsHeader.SOURCE.NOAO, hdu, valueType, comment);
    }

    @Override
    public String comment() {
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
}
