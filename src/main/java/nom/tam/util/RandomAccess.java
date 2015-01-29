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

/**
 * These packages define the methods which indicate that an i/o stream may be
 * accessed in arbitrary order. The method signatures are taken from
 * RandomAccessFile though that class does not implement this interface.
 */
public interface RandomAccess extends ArrayDataInput {

    /** Move to a specified location in the stream. */
    public void seek(long offsetFromStart) throws java.io.IOException;

    /** Get the current position in the stream */
    public long getFilePointer();
}
