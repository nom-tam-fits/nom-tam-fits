package nom.tam.fits;

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
 * This class allows FITS binary and ASCII tables to be accessed via a common
 * interface.
 */

public interface TableData {

    public abstract Object[] getRow(int row) throws FitsException;

    public abstract Object getColumn(int col) throws FitsException;

    public abstract Object getElement(int row, int col) throws FitsException;

    public abstract void setRow(int row, Object[] newRow) throws FitsException;

    public abstract void setColumn(int col, Object newCol) throws FitsException;

    public abstract void setElement(int row, int col, Object element) throws FitsException;

    public abstract int addRow(Object[] newRow) throws FitsException;

    public abstract int addColumn(Object newCol) throws FitsException;

    public abstract void deleteRows(int row, int len) throws FitsException;

    public abstract void deleteColumns(int row, int len) throws FitsException;

    public abstract void updateAfterDelete(int oldNcol, Header hdr) throws FitsException;

    public abstract int getNCols();

    public abstract int getNRows();

}
