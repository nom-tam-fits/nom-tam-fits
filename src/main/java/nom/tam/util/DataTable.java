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
 * This interface defines the properties that a generic table should have.
 */
public interface DataTable {

    public abstract void setRow(int row, Object newRow) throws TableException;

    public abstract Object getRow(int row);

    public abstract void setColumn(int column, Object newColumn) throws TableException;

    public abstract Object getColumn(int column);

    public abstract void setElement(int row, int col, Object newElement) throws TableException;

    public abstract Object getElement(int row, int col);

    public abstract int getNRows();

    public abstract int getNCols();
}
